# Funny Cat Stick / 逗猫棒

[English](README_EN.md) | 简体中文

Funny Cat Stick 是一个适用于 Minecraft 1.20.1 Forge 的多人整蛊模组。玩家可用一次性的“逗猫棒”影响其他玩家，使其聊天内容在一段时间内自动带上猫娘语气。

## 主要内容

- 逗猫棒不可堆叠，右键另一名玩家后消耗，并赋予目标 30 分钟效果。
- 效果会保留玩家的原始消息，再随机添加对应性格的话语或颜文字。
- 效果可跨死亡和重连保存，并在剩余 20 分钟、10 分钟、5 分钟、1 分钟和 10 秒时提醒目标玩家。
- 使用逗猫棒时会向全服播报主人和目标玩家。
- 普通箱子战利品约有 0.6% 概率额外生成一根逗猫棒。
- 提供简体中文和英文语言支持。

## 0.2.0 更新内容

- 新增只能用于逗猫棒的“性格附魔”。不同附魔会让目标玩家获得不同的聊天风格。
- 新增九种普通性格附魔：强硬型、病娇型、傲娇型、元气型、慵懒型、毒舌型、胆小型、天然呆型、猫又/神明型。
- 普通性格附魔可通过附魔台、村民交易、钓鱼和附魔书战利品等原版途径获得；逗猫棒在附魔台中只会获得这些普通附魔。
- 新增宝藏附魔“及你太美型猫娘”，其附魔书目前仅能在开放水域钓鱼时低概率获得。
- 同一根逗猫棒不能同时拥有多个性格附魔；目标玩家的最终性格由最后一次对其生效的逗猫棒决定。
- “及你太美”聊天风格支持随机前缀、后缀或同时添加前后缀。
- “及你太美”逗猫棒可为使用者和目标玩家播放专属音乐。该功能默认关闭，由管理员控制，全服其他玩家不会听见。
- 修复满级附魔台仍可能显示“附魔能力被限制”、无法选择全部选项的问题。

## 管理员命令

以下命令要求至少 2 级 OP 权限。玩家参数支持原版选择器；持续时间支持 `s`、`m`、`h` 和 `d`，例如 `30m`、`1h`。

```text
/funnycatstick tame <主人> <目标玩家> <持续时间>
/funnycatstick query <目标玩家>
/funnycatstick reduce <目标玩家> <减少时间>
/funnycatstick music
/funnycatstick music <true|false>
```

- `tame`：直接赋予目标玩家经典性格的逗猫棒效果。
- `query`：查询目标玩家的剩余效果时间。
- `reduce`：减少目标玩家的剩余效果时间。
- `music`：查询全服“及你太美”音乐开关。
- `music true|false`：开启或关闭音乐播放。默认值为 `false`，设置保存在当前世界中。

示例：

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
```

## 安装要求

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- 客户端和服务端均需安装相同版本

将发布页面中的 `funny_cat_stick-0.2.0.jar` 放入客户端和服务端的 `mods` 文件夹后启动游戏。

## 开发与测试

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
.\gradlew.bat runClientPlayer1
.\gradlew.bat runClientPlayer2
.\gradlew.bat stopLocalServer
```

多人测试说明见 [MULTIPLAYER_TESTING.md](MULTIPLAYER_TESTING.md)。

## 许可证

All Rights Reserved。
