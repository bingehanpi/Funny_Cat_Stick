# Funny Cat Stick / 逗猫棒

[English](README_EN.md) | 简体中文

Funny Cat Stick 是一个适用于 Minecraft 1.20.1 Forge 的多人趣味内容模组，以聊天整蛊、性格附魔和动画 NPC 为主要内容。

## 模组内容

- 一次性的逗猫棒可以对其他玩家施加猫娘聊天效果，并保留玩家原始消息。
- 多种性格附魔会改变聊天风格，其中包含可通过常规原版途径获得的普通附魔，以及特殊宝藏附魔。
- 效果可跨死亡和重连保存，并在剩余时间接近结束时提醒玩家。
- 特殊性格支持由管理员控制的专属音乐；音乐仅向相关玩家播放。
- 普通箱子战利品有较低概率额外生成逗猫棒。
- 新增动画 NPC“dswhale / 大肥鱼”及其创造模式刷怪蛋。大肥鱼拥有基础活动、观察、开门、回家和夜间睡眠行为。
- 新生成且属于 `#minecraft:village` 结构标签的村庄有机会在附近生成大肥鱼住宅，因此也可兼容正确使用该标签的其他模组村庄。
- 住宅内会生成一只绑定住所的大肥鱼和一个专用战利品箱。
- 提供简体中文和英文语言支持。

## 管理员命令

以下正式命令要求至少 2 级 OP 权限。玩家参数支持原版选择器；持续时间支持 `s`、`m`、`h` 和 `d`，例如 `30m`、`1h`、`0.5d`。

```text
/funnycatstick tame <主人> <目标玩家> <持续时间>
/funnycatstick query <目标玩家>
/funnycatstick reduce <目标玩家> <减少时间>
/funnycatstick music
/funnycatstick music <true|false>
/fcs locate dswhale_residence
```

- `tame`：直接为一个或多个目标玩家赋予经典性格的逗猫棒效果，并把指定玩家设为主人。
- `query`：查询一个或多个目标玩家的剩余效果时间。
- `reduce`：减少一个或多个目标玩家的剩余效果时间；减少到零后效果结束。
- `music`：查询全服“及你太美”音乐开关。
- `music true|false`：开启或关闭音乐播放。默认值为 `false`，设置保存在当前世界中。
- `fcs locate dswhale_residence`：查找当前世界数据中距离执行位置最近、已经成功生成的大肥鱼住宅，并返回可点击的坐标。该命令不会查找尚未生成的住宅，也不会追溯旧区块。

示例：

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
/fcs locate dswhale_residence
```

## 安装要求

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- GeckoLib 4.8.4 或兼容的 GeckoLib 4.x 版本
- 客户端和服务端均需安装相同版本的 Funny Cat Stick 与 GeckoLib

从 Releases 页面下载 `funny_cat_stick-0.3.0.jar`，与适用 Minecraft 1.20.1 Forge 的 GeckoLib 一同放入客户端和服务端的 `mods` 文件夹。

## 许可证

All Rights Reserved。
