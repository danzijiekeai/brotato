Brotato-Like Game (Java + FXGL)
🎮 项目简介

本项目是一个基于 Java 与 FXGL 框架开发的 2D 动作射击游戏，灵感来源于《Brotato（土豆兄弟）》。
玩家需要在限定时间内击败一波波敌人，通过升级、装备和策略来生存下去。

🛠 技术栈

Java 17+ （本作品基于Java 21）

FXGL 17+ （基于 JavaFX 的游戏引擎，本作品基于FXGL21）

Gradle/Maven（依赖管理与构建工具）

GitHub Actions（CI/CD）

📦 项目结构
src/main/java/com/team/game
│── MainApp.java          # 游戏入口
│── config/               # 游戏配置（常量、参数）
│── entity/               # 玩家、敌人、子弹等实体类
│── component/            # 组件系统（血量、攻击力、速度等）
│── service/              # 游戏服务（音效、关卡生成、数据存储）
│── ui/                   # UI 界面（菜单、HUD、结算页面）
│── util/                 # 工具类

🔗 模块与接口关系
1. 核心模块

GameApp (MainApp)

游戏启动、场景切换、资源加载。

依赖 entity、service、ui。

Entity 模块

玩家、敌人、子弹、道具。

通过 Component 组件接口 扩展功能。

Component 模块

提供通用接口（如 HealthComponent, AttackComponent, MovementComponent）。

被 Entity 调用，解耦行为逻辑。

Service 模块

提供独立服务接口：

AudioService → 控制 BGM 和音效。

LevelService → 敌人波次生成。

DataService → 存档与统计。

UI 模块

调用 Service 获取数据并展示（血条、金币、升级选项）。

接收用户输入，传递到 GameApp。