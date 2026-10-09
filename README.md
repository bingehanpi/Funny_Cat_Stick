# Funny Cat Stick / 逗猫棒

[English](README_EN.md) | 简体中文

Funny Cat Stick 是一个适用于 Minecraft 1.20.1 Forge 的多人趣味内容模组，围绕逗猫棒、聊天效果、探索、酿造与动画 NPC 展开。

## 模组内容

- 逗猫棒、多人聊天效果、多种性格附魔及相关特殊内容。
- 可同时陈列四根逗猫棒的展示架，以及稀有物品“逗?棒”。
- 解喵药水及其酿造过程、大白饭、香香大白饭和铁锅等内容。
- 动画 NPC“大肥鱼”、自然生成的住宅、日常行为与分阶段交易系统。
- 专属音乐、战利品、进度保存，以及简体中文和英文语言支持。

## 管理员命令

以下正式命令要求至少 2 级 OP 权限。玩家参数支持原版选择器；持续时间支持 `s`、`m`、`h` 和 `d`，例如 `30m`、`1h`、`0.5d`。

```text
/funnycatstick tame <主人> <目标玩家> <持续时间>
/funnycatstick query <目标玩家>
/funnycatstick reduce <目标玩家> <减少时间>
/funnycatstick music
/funnycatstick music <true|false>
/fcs locate dswhale_residence
/fcs place dswhale_residence <x> <y> <z>
```

- `tame`：直接为一个或多个目标玩家赋予经典性格的逗猫棒效果，并把指定玩家设为主人。
- `query`：查询一个或多个目标玩家的剩余效果时间。
- `reduce`：减少一个或多个目标玩家的剩余效果时间；减少到零后效果结束。
- `music`：查询全服“及你太美”音乐开关。
- `music true|false`：开启或关闭音乐播放。默认值为 `false`，设置保存在当前世界中。
- `fcs locate dswhale_residence`：查找当前世界数据中距离执行位置最近、已经成功生成的大肥鱼住宅，并返回可点击的坐标。该命令不会查找尚未生成的住宅，也不会追溯旧区块。
- `fcs place dswhale_residence <x> <y> <z>`：以给定坐标作为模板原点，在主世界直接生成完整的大肥鱼住宅、战利品箱和绑定住所的大肥鱼。支持绝对坐标和 `~` 相对坐标；生成结果会按自然生成住宅记录，并可被 `locate` 查询。该命令会覆盖模板范围内的方块，请先确认目标区域。

示例：

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
/fcs locate dswhale_residence
/fcs place dswhale_residence ~ ~ ~
```

## 安装要求

- Minecraft 1.20.1
- Forge 47.3.22 或更高的 Forge 47.x 版本
- Java 17
- GeckoLib 4.8.2 或更高的 GeckoLib 4.x 版本
- 客户端和服务端均需安装相同版本的 Funny Cat Stick 与 GeckoLib

从 Releases 页面下载 `funny_cat_stick-0.4.0.jar`，与适用 Minecraft 1.20.1 Forge 的 GeckoLib 一同放入客户端和服务端的 `mods` 文件夹。

## 许可证

All Rights Reserved。
