# 鲸鱼娘模型资产

参考：用户提供的 `dsWhale.jpg` 三视图。模型通过本机 Blockbench MCP 制作，独立几何，不使用玩家或村民模型。

## 文件

- `whale_maid.bbmodel`：含骨骼、UV、嵌入贴图及两段动画的可编辑源文件。
- `preview_sheet.png`：正面、侧面、背面和斜前方预览；同目录有四张独立视图。
- `../../../src/main/resources/assets/funny_cat_stick/geo/whale_maid.geo.json`：模型几何。
- `../../../src/main/resources/assets/funny_cat_stick/animations/whale_maid.animation.json`：动画。
- `../../../src/main/resources/assets/funny_cat_stick/textures/entity/whale_maid.png`：512×512 PNG 图集。

以上三个资源文件的实际位置是仓库根目录下的 `src/main/resources/assets/funny_cat_stick/`。

## 规格与动画

224 个立方体，32 个骨骼；全模型采用可导出的立方体几何和逐面 UV。含呆毛约 26.5 模型单位高，按 16 单位/格约为 1.66 格。

外观修订：加宽刘海、减小发束间夹角，并增加内侧发片遮住额头缝隙；双眼围绕各自中心约缩小 8%，虹膜由亮蓝改为较深的蓝色，保留高光。现有骨骼与 idle / walk 动画保持不变。

- `animation.whale_maid.idle`：4 秒循环；轻微呼吸、头部转动、发束、呆毛、侧鳍及鲸尾的缓慢摆动，脚部固定。
- `animation.whale_maid.walk`：1.2 秒原地循环；左右脚交替、双臂反向摆动、肘膝弯曲、裙摆和头发跟随。实体位移需由后续游戏代码控制。
- 时间刻度设为 120，确保 0.15 秒间隔及 1.2 秒末帧能精确导出。

骨骼主干：`root → hips → spine → chest → neck → head`。双臂和双腿各有上段、下段、手/脚三个关节。附属骨骼包括裙摆、围裙、后发两段、左右侧发两段、呆毛、左右侧鳍和鲸尾三段。

## 根据参考作出的补全

1. 三视图没有真实尺寸，采用约 1.66 格高的大头短身比例。
2. 长发遮挡的后背补为深蓝衣身；鲸尾从腰后伸出，向角色左侧弯曲，并为动画预留三段骨骼。
3. 两侧蓝色鱼鳍状装饰解释为带白色下缘的小侧鳍，蓝色蝴蝶结固定在其根部。
4. 蕾丝、卷发及金色裙纹转译成 Minecraft 风格的分层方块和像素图案；不逐笔复制插画曲线。
5. 参考没有动作设计，待机采用轻柔呼吸，行走采用短步幅，避免夸张摆动。
6. 当前面部为固定的睁眼表情，没有添加用户未要求的眨眼或其他动画。

## GeckoLib 与编辑器兼容性

本机 Blockbench 为 4.8.3。此次安装的 GeckoLib Models & Animations 插件报告版本 4.2.5，但在创建、重新加载 GeckoLib 项目时出现 `Cannot read properties of undefined (reading 'value')`。

为保证源文件能实际重新打开，最终 `.bbmodel` 保存为 Blockbench 内置的 **Bedrock Entity** 动画格式，并已重新加载验证。几何导出使用 `format_version: 1.12.0`，动画导出使用 `format_version: 1.8.0`，使用标准骨骼及 `catmullrom` 插值，不依赖 GeckoLib 5 专有模型扩展。目标是供 Forge 1.20.1 的 GeckoLib 4 接入。

GeckoLib 官方说明支持把 Bedrock 模型转换成 GeckoLib 模型；编辑器兼容问题解决后可以再转换源项目：
https://github.com/bernie-g/geckolib/wiki/Making-Your-Models-(Blockbench)

本次没有升级 Minecraft、Forge、Gradle、映射、Java 或 Blockbench。

## 验证及边界

- Blockbench 模型检查：0 问题；骨骼检查通过；两段动画检查均为 0 问题，首尾位置闭合。
- 已查看多角度模型和两段动画的抽样姿势。行走脚底采样未发现穿地；这不是碰撞模拟或完整游戏内验收。
- 初版自动正面轮廓比较约 81% IoU，只衡量轮廓，不代表美术还原度或用户认可；刘海和眼睛修订后以更新的预览图为准。
- `gradlew.bat build` 成功。
- `gradlew.bat runClient` 已启动 Forge 47.4.10 / Minecraft 1.20.1，并完成资源重载、纹理图集和声音系统初始化。
- **游戏内 NPC 渲染和动画控制尚未测试**：当前项目没有 GeckoLib 依赖，也尚未注册该实体、渲染器或动画控制器。因此客户端启动通过只证明现有工程能启动，不能证明这套模型已在游戏里渲染。
- 本次只交付模型资产，没有改动多人逻辑、网络或服务端行为，因此没有增加专用服务器测试。

后续接入时使用资源标识 `funny_cat_stick:geo/whale_maid.geo.json`、`funny_cat_stick:textures/entity/whale_maid.png` 和 `funny_cat_stick:animations/whale_maid.animation.json`，根据移动状态在上述 idle / walk 动画名之间切换。
