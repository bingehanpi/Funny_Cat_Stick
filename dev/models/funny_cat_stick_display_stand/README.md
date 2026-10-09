# 逗猫棒展示架（模型资产）

本目录只包含独立木制展示架，不包含逗猫棒几何体，也不包含 Java、BlockEntity、渲染器、交互或方块注册代码。设计参考武士刀刀架的横向双托点，外观采用樱花木风格的浅粉木纹和深色托架。

## 文件

- `funny_cat_stick_display_stand.bbmodel`：Blockbench 可编辑源文件；分为 `base`、`central_post` 和 `display_north` / `display_south` / `display_east` / `display_west` 六组。
- `render_preview.js`：在 Blockbench MCP 中生成真实视口预览的脚本。
- `preview_sheet.png`：正、背、左右侧、45° 和顶视图。
- `src/main/resources/assets/funny_cat_stick/models/block/funny_cat_stick_display_stand.json`：普通 Java 方块模型导出文件。
- `src/main/resources/assets/funny_cat_stick/textures/block/funny_cat_stick_display_stand.png`：32×32 樱花木风格像素贴图。

## 主要部件

- `base_foot`、`base_platform`：两层方形底座，提供稳定的落地轮廓；不是实心方块。
- `post_main`、`post_collar`、`post_cap`：中央立柱、连接箍与顶盖，支撑四面的横托梁。
- 每个 `display_*` 组含 1 根伸出的连接臂、1 根水平横托梁、2 个托点和 2 个外侧短挡唇。挡唇与托点之间为将来摆放一根细长逗猫棒的空位。

总计 29 个 cuboid。模型边界为 `X/Z = 1.5–14.5`、`Y = 0–10.85`（单位：每格 16），占地 13×13 模型单位，高约 0.68 格。全部几何体位于单个方块内。

## 留给后续代码的四个展示位置

以下为普通方块模型使用的 0–16 本地坐标。逗猫棒应水平摆放，长轴沿表中的切向轴。建议将可见杆身中心置于所列坐标附近，再按实际物品模型做少量微调；这些位置**没有**烘焙进当前方块模型。

| 展示面 | 建议杆身中心 `(X, Y, Z)` | 杆身长轴 | 对应源文件分组 |
| --- | --- | --- | --- |
| NORTH（-Z） | `(8, 10.45, 4.18)` | X | `display_north` |
| SOUTH（+Z） | `(8, 10.45, 11.82)` | X | `display_south` |
| EAST（+X） | `(11.82, 10.45, 8)` | Z | `display_east` |
| WEST（-X） | `(4.18, 10.45, 8)` | Z | `display_west` |

四个中心都与中央立柱保留间隔；预留细杆走廊（约 6.6 单位长、0.5 单位厚、0.7 单位高）与展示架几何体不相交。实际物品模型的宽度、长度和旋转仍需在将来的渲染代码中确认。普通 Java 方块模型不能在运行时把 `display_*` 源文件分组当作可动画骨骼；这些分组仅用于编辑和标注。

## 导出与接入边界

本模型适合导出为普通静态方块模型。JSON 已使用 `funny_cat_stick:block/funny_cat_stick_display_stand` 贴图引用及同名粒子贴图。当前没有添加 blockstate、方块注册或物品模型，因此这些资产本身不会让游戏中出现可放置的展示架。后续四根真实逗猫棒的渲染也不在本次范围内。

Blockbench 模型检查为 0 问题；已经检查正面、背面、左右侧、45° 与顶视图。模型与贴图的源文件和导出文件已核对，四个预留走廊均无展示架 cuboid 穿入。

2026-10-09 验证：重新打开 `.bbmodel` 后 29 个 cuboid、6 个分组、1 张贴图完整恢复，贴图命名空间与粒子设置仍正确；`gradlew.bat build` 成功，开发客户端在 Forge 47.3.22 / Minecraft 1.20.1 下启动并完成纹理图集初始化。由于本次不注册方块或编写渲染代码，未在游戏世界中放置展示架，也未测试真实逗猫棒的摆放效果。
