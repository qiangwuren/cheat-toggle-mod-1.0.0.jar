# 更新日志 / Changelog

所有对项目的显著修改都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

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
