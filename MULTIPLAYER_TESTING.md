# Funny Cat Stick 本地多人测试

本项目提供一个 Forge 专用服务端和两个独立客户端：

- `runServer`：本地服务端，目录为 `run/server`
- `runClientPlayer1`：玩家 `DevPlayer1`，目录为 `run/client_player_1`
- `runClientPlayer2`：玩家 `DevPlayer2`，目录为 `run/client_player_2`
- `stopLocalServer`：保存世界并优雅关闭本地服务端

`runServer` 每次启动前都会从 `dev/local_server/ops.json` 安装本地 OP 配置，确保 `DevPlayer1` 和 `DevPlayer2` 都拥有最高的 4 级命令权限。

## 启动与连接

1. 运行 `gradlew.bat runServer`，等待日志出现 `Done`。
2. 分别启动 `gradlew.bat runClientPlayer1` 和 `gradlew.bat runClientPlayer2`。
3. 两个客户端都通过“多人游戏 → 直接连接”进入 `127.0.0.1:25565`。

IntelliJ 中同名配置必须显示为 Gradle 配置，不应是 Application 配置。不要执行 `genIntellijRuns` 覆盖这些配置，否则可能再次产生重复 Java 模块错误。

## 正确关闭服务端

IntelliJ 的 Gradle 运行控制台不一定会把键盘输入转发给内部 Minecraft 进程，因此在其中输入 `stop` 可能没有反应。这不是服务端卡死。

请运行 IntelliJ 配置 `stopLocalServer`，或另开 PowerShell 执行：

```powershell
.\gradlew.bat stopLocalServer
```

该任务通过仅限本机的 RCON 向 Minecraft 发送原生 `stop` 命令，服务端仍会正常保存玩家和世界数据。看到 `Stopping server`、`Saving players` 和 `Saving worlds` 后即为正常关闭。

## 本地服务端安全范围

`run/server/server.properties` 只监听 `127.0.0.1`，并为开发测试关闭正版验证、启用本地 RCON。此配置只能用于本机开发，不要将游戏端口或 RCON 端口暴露到局域网或公网。

## 常用命令

```powershell
.\gradlew.bat runServer
.\gradlew.bat stopLocalServer
.\gradlew.bat runClientPlayer1
.\gradlew.bat runClientPlayer2
.\gradlew.bat build
```

## 管理员命令

以下命令要求至少 2 级 OP 权限。时间支持 `s`（秒）、`m`（分钟）、`h`（小时）和 `d`（天），例如 `30m`、`10s`、`1h`。

```text
/funnycatstick tame <主人> <目标玩家> <持续时间>
/funnycatstick query <目标玩家>
/funnycatstick reduce <目标玩家> <减少时间>
/funnycatstick music
/funnycatstick music <true|false>
```

目标玩家参数支持原版选择器，例如 `@a`、`@r`、`@p`。主人必须是单个在线玩家，但同样可以使用只会选中一人的原版选择器。

`music` 用于查询或修改全服“及你太美”逗猫棒的音乐播放开关。默认值为 `false`，设置会保存在当前世界的服务端配置中。

示例：

```text
/funnycatstick tame DevPlayer1 DevPlayer2 30m
/funnycatstick query @a
/funnycatstick reduce DevPlayer2 5m
```

## 开发环境战利品统计

开发运行环境中，4 级权限管理员可执行：

```text
/fcs_testloot <战利品表 ID> <迭代次数>
```

例如：

```text
/fcs_testloot minecraft:chests/simple_dungeon 10000
```

命令在服务端内存中反复运行真实战利品表及 Forge Global Loot Modifier 链路，不会创建箱子、物品实体或向玩家物品栏添加内容。单次最多允许 `100000` 次迭代，避免误操作长时间阻塞服务器线程。

该命令在 Forge 生产环境中不会注册，即使安装了发布 JAR 也不能在正式服务器中调用。
