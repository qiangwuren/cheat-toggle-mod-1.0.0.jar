# 更新日志 / Changelog

所有对项目的显著修改都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.1] - 2026

### 变更
- 目标版本由 Minecraft **26.2** 移植到 **26.3**
  - Fabric Loader `0.19.3` → `0.19.5`
  - Fabric API `0.155.2+26.2` → `0.161.0+26.3`
  - Fabric Loom `1.17-SNAPSHOT` → `1.18.2`（稳定版）
  - Gradle `9.5.1` → `9.7.1`
- **放宽 Java 版本要求**：`java_release` / `java_version_min` 由 `26` 降到 **`25`**
  （即 26.3 的字节码下限；JDK 25/26/27… 均可，不再强制最新版）
- 适配 26.3 全新的权限系统（`net.minecraft.server.permissions`）

### 修复
- 移除 26.3 中已被删除的 `PlayerList#setAllowCommandsForAllPlayers(boolean)` 调用，
  改为「写世界数据 + 重发权限」的热切换链路；`/cheattoggle cheats` 依然无需重启世界即可生效
- 修复 `cheattoggle.client.mixins.json` 的 `compatibilityLevel`：
  上游硬编码 `JAVA_17`，且 `processResources` 的 `filesMatching('**/*.mixins.json')`
  因 Gradle 不做部分匹配而从未命中该文件，现改为 `*.mixins.json` 并直接声明 `JAVA_21`

### 工程
- 全部依赖仓库改为国内镜像（Fabric 官方源 + 阿里云公共仓库 + 华为云）
- Gradle 发行包走腾讯云镜像并开启多线程下载（`parallelDownloads=8`）
- Minecraft 版本清单改走 BMCLAPI 国内镜像
- 新增 `setup-fabric-env.ps1` 一键环境配置 / 构建脚本（自动探测 JDK、构建、校验产物）

### 已知问题（未修复，仅声明）
- `/cheattoggle lockdifficulty` 在**极限模式下无效**：执行不报错、返回成功提示，
  但难度锁状态不会真正改变。普通模式下正常生效。
  这是游戏本身的既定行为，属上游已有问题，本次移植**不做修复**，
  仅在 README 与 Release 说明中如实声明。
- 极限模式下若难度锁处于开启状态，`/cheattoggle difficulty` 修改后的难度会被锁回 `HARD`。

## [1.0.0] - 2026-07-31

### 新增
- `/cheattoggle cheats <true|false>` — 开启/关闭作弊，热加载生效
- `/cheattoggle lockdifficulty <true|false>` — 解锁/锁定难度
- `/cheattoggle difficulty <peaceful|easy|normal|hard>` — 修改难度，绕过极限模式限制
- `/cheattoggle operatoritems <true|false>` — 显示/隐藏管理员物品分栏
- 极限模式兼容：通过反射临时覆盖 hardcore 字段，允许修改难度后立即恢复

### 修复
- 修复 26.2 版本极限模式下无法直接开启作弊的问题
- 修复极限模式下修改难度被强制为 HARD 的问题
- 修复客户端 UI 将硬核模式视为难度锁定的问题
