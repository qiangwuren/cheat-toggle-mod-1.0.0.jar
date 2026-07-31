# 修复26.2极限模式无法秒开仙人的Bug (Cheat Toggle Mod)

一个面向 Minecraft 26.2 (Fabric) 的单人游戏实用模组，修复极限模式下无法直接开启作弊/修改难度的问题，无需再通过 NBT 修改器。

> "秒开仙人" = 快速开启作弊。本模组让玩家在极限模式存档中无需退出世界即可开启作弊、解锁难度锁、修改难度。

## 功能

| 指令 | 说明 |
|---|---|
| `/cheattoggle cheats <true\|false>` | 开启/关闭作弊，热加载生效，无需重启世界 |
| `/cheattoggle lockdifficulty <true\|false>` | 解锁/锁定难度（极限模式下不可用） |
| `/cheattoggle difficulty <peaceful\|easy\|normal\|hard>` | 修改难度，绕过极限模式强制 HARD 的限制，极限模式下必须使用该指令才可以正常修改难度 |
| `/cheattoggle operatoritems <true\|false>` | 显示/隐藏创造物品栏中的管理员物品分栏 |

## 实现原理

### 作弊开关 & 难度锁

直接修改存档 `level.dat` 中的 NBT 标签：
- `Data.allowCommands` — 作弊开关
- `Data.DifficultyLocked` — 难度锁

通过 `server.setDifficultyLocked()` / `server.setDifficulty()` 并调用 `sendDifficultyUpdate()` 向客户端广播更新，实现**热加载**，无需退出存档。



### 管理员物品分栏

参考 [Wurst Client](https://github.com/Wurst-Imperium/Wurst-MC) 的 `CreativeModeInventoryScreenMixin`，通过客户端 Mixin 拦截 `hasPermissions()`，忽略 `canUseGameMasterBlocks()` 检查，仅由 `operatorItemsTab` 选项控制分栏可见性。

## 环境要求

- Minecraft **26.2**
- Fabric Loader `>= 0.19.3`
- Fabric API `>= 0.155.2+26.2`
- Java `>= 26`
- **仅单人游戏**（多人服务器不可用）

## 构建

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

构建产物位于 `build/libs/` 目录。

## 安装

1. 安装 Fabric Loader 与 Fabric API
2. 将 `releases/` 中的 JAR 放入 `.minecraft/mods/` 文件夹
3. 启动游戏

## 开源许可

本项目基于 [MIT License](LICENSE) 开源。

作者：**qiangwuren**

## 免责声明

本模组仅用于单人游戏，请遵守游戏协议与服务器规则。
