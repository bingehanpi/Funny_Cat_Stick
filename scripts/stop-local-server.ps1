param(
    [string]$ServerCommand = 'stop'
)

$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$propertiesPath = Join-Path $projectDirectory 'run\server\server.properties'

if (-not (Test-Path -LiteralPath $propertiesPath)) {
    throw "Server properties not found: $propertiesPath"
}

$properties = @{}
foreach ($line in Get-Content -LiteralPath $propertiesPath) {
    if ($line -match '^\s*([^#!][^=]*?)\s*=\s*(.*)$') {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

if ($properties['enable-rcon'] -ne 'true') {
    throw 'RCON is disabled in run/server/server.properties.'
}

$rconPassword = $properties['rcon.password']
if ([string]::IsNullOrWhiteSpace($rconPassword)) {
    throw 'rcon.password is empty in run/server/server.properties.'
}

$rconPort = [int]$properties['rcon.port']
$client = [System.Net.Sockets.TcpClient]::new()

function Send-RconPacket {
    param(
        [System.IO.Stream]$Stream,
        [int]$RequestId,
        [int]$PacketType,
        [string]$Body
    )

    [byte[]]$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($Body)
    $packetLength = 4 + 4 + $bodyBytes.Length + 2
    $buffer = [System.IO.MemoryStream]::new()
    $writer = [System.IO.BinaryWriter]::new($buffer, [System.Text.Encoding]::UTF8, $true)
    try {
        $writer.Write([int]$packetLength)
        $writer.Write([int]$RequestId)
        $writer.Write([int]$PacketType)
        $writer.Write($bodyBytes, 0, $bodyBytes.Length)
        $writer.Write([byte]0)
        $writer.Write([byte]0)
        $writer.Flush()

        [byte[]]$packet = $buffer.ToArray()
        $Stream.Write($packet, 0, $packet.Length)
        $Stream.Flush()
    }
    finally {
        $writer.Dispose()
        $buffer.Dispose()
    }
}

function Receive-RconPacket {
    param([System.IO.Stream]$Stream)

    $reader = [System.IO.BinaryReader]::new($Stream, [System.Text.Encoding]::UTF8, $true)
    $packetLength = $reader.ReadInt32()
    if ($packetLength -lt 10) {
        throw "Invalid RCON packet length: $packetLength"
    }

    $requestId = $reader.ReadInt32()
    $packetType = $reader.ReadInt32()
    $bodyLength = $packetLength - 10
    $body = [System.Text.Encoding]::UTF8.GetString($reader.ReadBytes($bodyLength))
    [void]$reader.ReadByte()
    [void]$reader.ReadByte()

    return [pscustomobject]@{
        RequestId = $requestId
        PacketType = $packetType
        Body = $body
    }
}

try {
    $client.Connect('127.0.0.1', $rconPort)
    $stream = $client.GetStream()
    $stream.ReadTimeout = 5000
    $stream.WriteTimeout = 5000

    Send-RconPacket -Stream $stream -RequestId 1 -PacketType 3 -Body $rconPassword
    $authResponse = Receive-RconPacket -Stream $stream
    if ($authResponse.RequestId -eq -1) {
        throw 'RCON authentication failed.'
    }

    Send-RconPacket -Stream $stream -RequestId 2 -PacketType 2 -Body $ServerCommand
    $commandResponse = Receive-RconPacket -Stream $stream
    if ($commandResponse.RequestId -eq -1) {
        throw 'RCON command failed.'
    }

    if (-not [string]::IsNullOrEmpty($commandResponse.Body)) {
        Write-Host $commandResponse.Body
    }
    if ($ServerCommand -eq 'stop') {
        Write-Host 'Stop command sent. The server is saving the world and shutting down.'
    }
}
catch [System.Net.Sockets.SocketException] {
    throw "Cannot connect to the local server RCON port 127.0.0.1:$rconPort. Make sure runServer has reached Done."
}
finally {
    $client.Dispose()
}
