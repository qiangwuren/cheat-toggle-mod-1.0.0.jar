# Cheat Toggle Mod —— 修复 26.3 极限模式无法开启作弊 / 修改难度

一个面向 **Minecraft 26.3 (Fabric)** 的单人游戏实用模组，修复极限模式下无法直接开启作弊、解锁难度锁、修改难度的问题，无需再通过 NBT 修改器。

> "秒开仙人" = 快速开启作弊。本模组让玩家在极限模式存档中无需退出世界即可开启作弊、修改难度。
>
> 本工程由上游的 **26.2** 版本移植而来（上游仓库：[qiangwuren/cheat-toggle-mod-1.0.0.jar](https://github.com/qiangwuren/cheat-toggle-mod-1.0.0.jar)）。
>
> ⚠️ **已知问题**：`/cheattoggle lockdifficulty`（难度锁）在**极限模式下不可用**，执行后不报错但不会生效；普通模式正常。详见[实现原理](#难度锁--难度)。

## 功能

| 指令 | 说明 |
|---|---|
| `/cheattoggle cheats <true\|false>` | 开启/关闭作弊，热加载生效，无需重启世界 |
| `/cheattoggle lockdifficulty <true\|false>` | 解锁/锁定难度。**极限模式下不可用**（见下方说明） |
| `/cheattoggle difficulty <peaceful\|easy\|normal\|hard>` | 修改难度，绕过极限模式强制 HARD 的限制 |
| `/cheattoggle operatoritems <true\|false>` | 显示/隐藏创造物品栏中的管理员物品分栏 |

## 实现原理

### 作弊开关

写入存档 `level.dat` 中的 `Data.allowCommands`，再复刻集成服务端（`IntegratedServer`）的热切换链路：

```
世界数据.setAllowCommands(enable)
  -> IntegratedServer.setWorldAllowCommands(enable)
        -> updateGameModeBasedOnPermission()
        -> updatePermissions()        // 对每个玩家 sendPlayerPermissionLevel()
  -> Commands.sendCommands(player)    // 重建客户端指令树
  -> saveAllChunks(true, false, true)
```

**26.3 的重要变更**：26.2 使用的 `PlayerList#setAllowCommandsForAllPlayers(boolean)` 已被移除。
26.3 把权限体系重写为 `net.minecraft.server.permissions`：

- 权限集合 `PermissionSet` / `LevelBasedPermissionSet`（`ALL` / `MODERATOR` / `GAMEMASTER` / `ADMIN` / `OWNER`）
- `ServerPlayer#permissions()` 实时委托给 `MinecraftServer#getProfilePermissions(...)`
- 单人模式下 `IntegratedServer#getCustomPermissionLevel()` 直接读取 `WorldData#isAllowCommands()`

因此"写世界数据 + 刷新权限"即可实现热加载，无需重启存档，也不再需要 26.2 的那个已被删除的 API。

### 难度锁 & 难度

通过 `server.setDifficultyLocked()` / `server.setDifficulty(difficulty, true)` 生效。

**⚠️ 已知问题：`/cheattoggle lockdifficulty` 在极限模式下不可用。**

- **普通模式**：解锁/锁定难度正常生效。
- **极限模式**：该指令**无效**。执行后不会报错、指令也会正常返回成功提示，但难度锁状态不会真正改变。
  极限模式存档本身不允许解除难度锁，这是游戏的既定行为，**本模组不打算修复**（也不是移植过程中引入的问题）。
- 本次移植只是把这一限制**如实声明**出来，指令实现本身未做改动。

另外，极限模式下 `hardcore` 标志会把难度强制为 `HARD`：
`/cheattoggle difficulty` 会通过反射临时把 `LevelSettings.DifficultySettings.hardcore` 置为 `false`，
调用完成后再置回 `true`（`PrimaryLevelData.settings` 字段在 26.3 中依然存在），从而绕过该限制改难度。
但需注意：**若难度锁处于开启状态，改完的难度仍会被锁回 `HARD`**。

### 管理员物品分栏

参考 [Wurst Client](https://github.com/Wurst-Imperium/Wurst-MC) 的 `CreativeModeInventoryScreenMixin`，
通过客户端 Mixin 拦截 `hasPermissions()`，忽略 `canUseGameMasterBlocks()` 检查，仅由 `operatorItemsTab` 选项控制分栏可见性。

## 环境要求

| 项 | 版本 |
|---|---|
| Minecraft | **26.3** |
| Fabric Loader | `>= 0.19.5` |
| Fabric API | `0.161.0+26.3` |
| Java | `>= 25`（见下） |
| 游戏模式 | **仅单人游戏**（多人服务器不可用） |

### 关于 Java 版本

原工程强制 `Java >= 26`，本工程已**放宽到 25**，也就是实测能用的最低值：

1. Minecraft 26.3 的 `client.jar` 里**全部** class 文件版本为 **69 = Java 25**，`javac` 无法为它产出 `release < 25` 的编译结果；
2. Mojang 版本清单声明 26.3 的运行时是 `java-runtime-epsilon`，`majorVersion = 25`；
3. `fabric-loom 1.18.2` 自身字节码为 Java 25（`org.gradle.jvm.version=25`）。

所以下限只能是 25，无法再低；但 **JDK 25、26、27… 都能用，不再强制"最新版"**。
`setup-fabric-env.ps1` 会自动搜索本机 JDK 并优先选用恰好 25 的那个。

## 构建

推荐用一键脚本（自动探测 JDK、配置国内镜像、构建、校验产物）：

```powershell
# 在仓库根目录
.\setup-fabric-env.ps1
```

也可以直接调用 Gradle Wrapper：

```bat
:: Windows
gradlew.bat build

:: Linux / macOS
./gradlew build
```

构建产物：

- `cheat-toggle-mod-<版本>+mc26.3.jar` —— 直接生成在**源码文件夹根目录**，游戏可直接识别使用
- `build/libs/` 下另有同名 jar 与 `-sources.jar`（**注意 `-dev.jar` 不能直接放进游戏**）

### 国内网络优化

- Gradle 发行包走 **腾讯云镜像** `mirrors.cloud.tencent.com/gradle`（备用：华为云 `mirrors.huaweicloud.com/gradle`），并开启 `parallelDownloads=8` 多线程下载
- 依赖仓库在 `settings.gradle` 中统一为：Fabric 官方源（Fabric 构件只有这里有）→ 阿里云公共仓库 → 华为云 → Maven Central
- Minecraft 版本清单改走 **BMCLAPI**（`bmclapi2.bangbang93.com`），避免每次都慢速访问 `piston-meta.mojang.com`
- 构建参数开启 `org.gradle.parallel` / `parallel.download` / `caching`，并配置依赖下载失败自动重试

## 安装

1. 安装 **Fabric Loader**（26.3）与 **Fabric API**
2. 把 `cheat-toggle-mod-<版本>+mc26.3.jar` 放入 `.minecraft/mods/`
3. 启动游戏，进入单人世界后使用 `/cheattoggle` 指令

## 已知问题

| 指令 | 普通模式 | 极限模式 |
|---|---|---|
| `/cheattoggle cheats` | ✅ 正常 | ✅ 正常 |
| `/cheattoggle difficulty` | ✅ 正常 | ⚠️ 可改，但难度锁开启时会被锁回 `HARD` |
| `/cheattoggle lockdifficulty` | ✅ 正常 | ❌ **无效**（不报错，但难度锁状态不变） |
| `/cheattoggle operatoritems` | ✅ 正常 | ✅ 正常 |

`lockdifficulty` 在极限模式下无效是游戏本身的既定行为，**本模组不打算修复**，仅在此声明。

## 开源许可

本项目基于 [MIT License](LICENSE) 开源。

原作者：**qiangwuren**

## 免责声明

本模组仅用于单人游戏，请遵守游戏协议与服务器规则。

## 特别鸣谢

![感谢](icon/ds.png "所有代码都是他写的")

**特别鸣谢这一位开发者，所有代码都是他写的，为我节省了大量的时间。**
