param(
    [Parameter(Mandatory = $true)]
    [string]$CandidateDirectory
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $CandidateDirectory "textures"
New-Item -ItemType Directory -Force -Path $textureDirectory | Out-Null

$specs = @(
    @{ Name = "big_white_rice"; MaxWidth = 14; MaxHeight = 12 },
    @{ Name = "pot_of_white_rice"; MaxWidth = 16; MaxHeight = 12 },
    @{ Name = "iron_pot"; MaxWidth = 16; MaxHeight = 11 },
    @{ Name = "fragrant_big_white_rice"; MaxWidth = 14; MaxHeight = 15 },
    @{ Name = "pot_of_fragrant_rice"; MaxWidth = 16; MaxHeight = 15 }
)

function Convert-ToItemTexture {
    param(
        [string]$SourcePath,
        [string]$DestinationPath,
        [int]$MaxWidth,
        [int]$MaxHeight
    )

    $source = [System.Drawing.Bitmap]::new($SourcePath)
    try {
        $minX = $source.Width
        $minY = $source.Height
        $maxX = -1
        $maxY = -1

        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = 0; $x -lt $source.Width; $x++) {
                if ($source.GetPixel($x, $y).A -ge 128) {
                    $minX = [Math]::Min($minX, $x)
                    $minY = [Math]::Min($minY, $y)
                    $maxX = [Math]::Max($maxX, $x)
                    $maxY = [Math]::Max($maxY, $y)
                }
            }
        }

        if ($maxX -lt $minX -or $maxY -lt $minY) {
            throw "No opaque subject pixels found in $SourcePath"
        }

        $cropWidth = $maxX - $minX + 1
        $cropHeight = $maxY - $minY + 1
        $scale = [Math]::Min($MaxWidth / $cropWidth, $MaxHeight / $cropHeight)
        $targetWidth = [Math]::Max(1, [int][Math]::Round($cropWidth * $scale))
        $targetHeight = [Math]::Max(1, [int][Math]::Round($cropHeight * $scale))
        $targetX = [int][Math]::Floor((16 - $targetWidth) / 2)
        $targetY = 16 - $targetHeight

        $texture = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($texture)
            try {
                $graphics.Clear([System.Drawing.Color]::Transparent)
                $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
                $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
                $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
                $graphics.DrawImage(
                    $source,
                    [System.Drawing.Rectangle]::new($targetX, $targetY, $targetWidth, $targetHeight),
                    [System.Drawing.Rectangle]::new($minX, $minY, $cropWidth, $cropHeight),
                    [System.Drawing.GraphicsUnit]::Pixel
                )
            }
            finally {
                $graphics.Dispose()
            }

            for ($y = 0; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    $pixel = $texture.GetPixel($x, $y)
                    if ($pixel.A -lt 128) {
                        $texture.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
                    }
                    else {
                        $red = [Math]::Min(255, [int]([Math]::Round($pixel.R / 16.0) * 16))
                        $green = [Math]::Min(255, [int]([Math]::Round($pixel.G / 16.0) * 16))
                        $blue = [Math]::Min(255, [int]([Math]::Round($pixel.B / 16.0) * 16))
                        $texture.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $red, $green, $blue))
                    }
                }
            }

            $texture.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        }
        finally {
            $texture.Dispose()
        }
    }
    finally {
        $source.Dispose()
    }
}

foreach ($spec in $specs) {
    Convert-ToItemTexture `
        -SourcePath (Join-Path $CandidateDirectory "source/$($spec.Name).png") `
        -DestinationPath (Join-Path $textureDirectory "$($spec.Name).png") `
        -MaxWidth $spec.MaxWidth `
        -MaxHeight $spec.MaxHeight
}

# The generated images establish the silhouettes and palette direction. These
# compact maps are the hand-cleaned 16x16 pass: hard alpha, fewer colors, and
# one logical source pixel per Minecraft texture pixel.
$palette = @{
    '.' = [System.Drawing.Color]::Transparent
    'W' = [System.Drawing.Color]::FromArgb(255, 255, 253, 239)
    'C' = [System.Drawing.Color]::FromArgb(255, 244, 240, 222)
    'R' = [System.Drawing.Color]::FromArgb(255, 228, 222, 203)
    'S' = [System.Drawing.Color]::FromArgb(255, 190, 187, 176)
    'E' = [System.Drawing.Color]::FromArgb(255, 160, 158, 150)
    'Q' = [System.Drawing.Color]::FromArgb(255, 255, 248, 220)
    'T' = [System.Drawing.Color]::FromArgb(255, 235, 218, 178)
    'Y' = [System.Drawing.Color]::FromArgb(255, 255, 229, 154)
    'G' = [System.Drawing.Color]::FromArgb(255, 224, 176, 74)
    'K' = [System.Drawing.Color]::FromArgb(255, 20, 22, 26)
    'D' = [System.Drawing.Color]::FromArgb(255, 43, 46, 52)
    'M' = [System.Drawing.Color]::FromArgb(255, 67, 71, 78)
    'L' = [System.Drawing.Color]::FromArgb(255, 105, 110, 118)
    'H' = [System.Drawing.Color]::FromArgb(255, 157, 162, 168)
}

$sprites = @{
    big_white_rice = @(
        '................',
        '................',
        '................',
        '.......WW.......',
        '.....WCCRCW.....',
        '....WCRRRRCW....',
        '...WCRWCRRRCW...',
        ('..WC' + ('R' * 8) + 'CW..'),
        ('.WC' + ('R' * 10) + 'CW.'),
        '.CRRWRRRRWRRRRC.',
        ('.C' + ('R' * 12) + 'C.'),
        '.SRRCRRRRRRCRRS.',
        ('..S' + ('R' * 10) + 'S..'),
        '...SSRRRRRRSS...',
        '....EEEEEEEE....',
        '................'
    )
    iron_pot = @(
        '................',
        '................',
        '................',
        '................',
        '....HHLLLLHH....',
        '...LMMMMMMMML...',
        '.HHMKKKKKKKKMHH.',
        ('HLM' + ('K' * 10) + 'MLH'),
        '.HHMKKKKKKKKMHH.',
        '...LMMMLLMMML...',
        '...DMMMMMMMMD...',
        '....DMMMMMMD....',
        '.....DMMMMD.....',
        '......DDDD......',
        '................',
        '................'
    )
    pot_of_white_rice = @(
        '................',
        '................',
        '.......WW.......',
        '.....WCCRCW.....',
        '....WCRRRRCW....',
        '...WCRWCRRRCW...',
        ('.HHWC' + ('R' * 6) + 'CWHH.'),
        ('HLM' + 'C' + ('R' * 8) + 'C' + 'MLH'),
        '.HHMSSSSSSSSMHH.',
        '...LMMMLLMMML...',
        '...DMMMMMMMMD...',
        '....DMMMMMMD....',
        '.....DMMMMD.....',
        '......DDDD......',
        '................',
        '................'
    )
    fragrant_big_white_rice = @(
        '.......Y........',
        '......Y.........',
        '...Y..Y...Y.....',
        '....Y.....Y.....',
        '...Y...Y....Y...',
        '.......QQ.......',
        '.....QTTYTQ.....',
        '....QTRRRRTQ....',
        '...QTRQTRRRTQ...',
        ('..QT' + ('R' * 8) + 'TQ..'),
        ('.QT' + ('R' * 10) + 'TQ.'),
        '.TRRYRRRRRYRRRT.',
        ('.T' + ('R' * 12) + 'T.'),
        ('..T' + ('R' * 10) + 'T..'),
        '...TTRRRRRRTT...',
        '....EEEEEEEE....'
    )
    pot_of_fragrant_rice = @(
        '.......Y........',
        '......Y.........',
        '...Y..Y...Y.....',
        '....Y.....Y.....',
        '.......QQ.......',
        '.....QTTYTQ.....',
        '....QTRRRRTQ....',
        ('..QT' + ('R' * 8) + 'TQ..'),
        '.HHQTRRYRRRTQHH.',
        ('HLM' + 'T' + ('R' * 8) + 'T' + 'MLH'),
        '.HHMSSSSSSSSMHH.',
        '...LMMMLLMMML...',
        '...DMMMMMMMMD...',
        '....DMMMMMMD....',
        '.....DMMMMD.....',
        '......DDDD......'
    )
}

foreach ($name in $sprites.Keys) {
    $rows = $sprites[$name]
    if ($rows.Count -ne 16) {
        throw "$name has $($rows.Count) rows instead of 16"
    }

    $texture = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt 16; $y++) {
            if ($rows[$y].Length -ne 16) {
                throw "$name row $y has $($rows[$y].Length) columns instead of 16"
            }
            for ($x = 0; $x -lt 16; $x++) {
                $key = [string]$rows[$y][$x]
                $texture.SetPixel($x, $y, $palette[$key])
            }
        }
        $texture.Save((Join-Path $textureDirectory "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $texture.Dispose()
    }
}

$preview = [System.Drawing.Bitmap]::new(1100, 176, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
try {
    $graphics = [System.Drawing.Graphics]::FromImage($preview)
    try {
        $graphics.Clear([System.Drawing.Color]::FromArgb(255, 35, 35, 39))
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $font = [System.Drawing.Font]::new("Consolas", 11)
        $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
        try {
            for ($index = 0; $index -lt $specs.Count; $index++) {
                $originX = $index * 216 + 44
                for ($checkerY = 0; $checkerY -lt 8; $checkerY++) {
                    for ($checkerX = 0; $checkerX -lt 8; $checkerX++) {
                        $shade = if ((($checkerX + $checkerY) % 2) -eq 0) { 92 } else { 62 }
                        $checkerBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, $shade, $shade, $shade))
                        try {
                            $graphics.FillRectangle($checkerBrush, $originX + $checkerX * 16, 8 + $checkerY * 16, 16, 16)
                        }
                        finally {
                            $checkerBrush.Dispose()
                        }
                    }
                }

                $texture = [System.Drawing.Bitmap]::new((Join-Path $textureDirectory "$($specs[$index].Name).png"))
                try {
                    $graphics.DrawImage($texture, [System.Drawing.Rectangle]::new($originX, 8, 128, 128))
                }
                finally {
                    $texture.Dispose()
                }
                $graphics.DrawString($specs[$index].Name, $font, $textBrush, $originX, 144)
            }
        }
        finally {
            $font.Dispose()
            $textBrush.Dispose()
        }
    }
    finally {
        $graphics.Dispose()
    }
    $preview.Save((Join-Path $CandidateDirectory "preview.png"), [System.Drawing.Imaging.ImageFormat]::Png)
}
finally {
    $preview.Dispose()
}
