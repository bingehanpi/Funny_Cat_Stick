# Funny Cat Stick / 逗猫棒

[English](README_EN.md) | 简体中文

Funny Cat Stick 是一个适用于 Minecraft Forge 1.20.1 的多人整蛊模组。

## 环境要求

- Minecraft 1.20.1
- Forge 47.4.10 或兼容的 Forge 47.x 版本
- Java 17
- 客户端和服务端均需安装

## 0.1.0 更新内容

- 新增一次性、不可堆叠且无耐久的“逗猫棒”。
- 新增独立的“逗猫棒”创造模式物品栏。
- 玩家手持逗猫棒右键另一名玩家时消耗道具，并赋予目标持续 30 分钟的整蛊效果。
- 效果期间，目标玩家发送的聊天消息会随机添加可爱颜文字或话语，但不会修改其原始内容。
- 使用道具时会从 5 条内容中随机选择一条向全服播报主人与目标玩家。
- 在剩余 20 分钟、10 分钟、5 分钟、1 分钟和 10 秒时向目标玩家发送提示。
- 效果不能通过牛奶等普通方式移除，并可跨死亡、重连继续保存。
- 所有 `chests/*` 战利品表约有 0.6% 概率生成一根逗猫棒，每次生成最多加入一根。
- 新增 OP 管理命令，可赋予、查询和减少效果时间。
- 提供仅限开发环境的真实战利品统计命令。
- 提供中英文语言支持和 16×16 原版风格物品材质。

## 管理员命令

以下命令要求至少 2 级 OP 权限。持续时间支持 `s`、`m`、`h` 和 `d`。

```text
/funnycatstick tame <主人> <目标玩家> <持续时间>
/funnycatstick query <目标玩家>
/funnycatstick reduce <目标玩家> <减少时间>
```

示例：

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
```

## 安装

1. 安装 Minecraft 1.20.1 和 Forge 47.4.10。
2. 将发布页面中的 `funny_cat_stick-0.1.0.jar` 放入客户端和服务端的 `mods` 文件夹。
3. 使用相同模组版本启动客户端和服务端。

## 开发与测试

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
.\gradlew.bat runClientPlayer1
.\gradlew.bat runClientPlayer2
.\gradlew.bat stopLocalServer
```

本地测试玩家 `DevPlayer1` 和 `DevPlayer2` 会在 `runServer` 启动前自动获得 4 级 OP 权限。更多说明见 [MULTIPLAYER_TESTING.md](MULTIPLAYER_TESTING.md)。

## 许可证

All Rights Reserved。
