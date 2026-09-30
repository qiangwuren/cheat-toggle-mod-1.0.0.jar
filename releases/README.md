# 已发布的 jar 汇总

本目录集中存放**所有版本**编译好的 mod jar，方便按 Minecraft 版本查找。

| mod 版本 | Minecraft 版本 | 文件名 | Java | 大小 | SHA256 |
|---|---|---|---|---|---|
| 1.0.0 | 26.2 | `cheat-toggle-mod-1.0.0.jar` | >= 26 | 136,071 字节 | `BEDCEF8049EAD4DD4311BDD287A7128B2A3B9019C0C39411412E1894E092A3AB` |
| 1.0.1 | **26.3** | `cheat-toggle-mod-1.0.1+mc26.3.jar` | >= 25 | 136,055 字节 | `4218106C86A218ACC9AAD79169D0BB9DBFB0DA325C219DABE4D9294F7F07068F` |

## 该下哪个

- 玩 **Minecraft 26.3** → 用 `cheat-toggle-mod-1.0.1+mc26.3.jar`
- 玩 **Minecraft 26.2** → 用 `cheat-toggle-mod-1.0.0.jar`

> 26.3 与 26.2 的 jar **互不通用**，请按自己的游戏版本选择。

## 安装

1. 安装对应版本的 **Fabric Loader** 与 **Fabric API**
2. 把 jar 放进 `.minecraft/mods/`
3. 启动游戏，进入**单人世界**后使用 `/cheattoggle` 指令

## 也可以从 Releases 页面下载

同一批 jar 也发布在 GitHub Releases：

- [v26.3](https://github.com/qiangwuren/cheat-toggle-mod-1.0.0.jar/releases/tag/v26.3) —— 对应 1.0.1 / Minecraft 26.3
- [V1.0](https://github.com/qiangwuren/cheat-toggle-mod-1.0.0.jar/releases/tag/V1.0) —— 对应 1.0.0 / Minecraft 26.2

## 说明

- `cheat-toggle-mod-1.0.1+mc26.3.jar` 同时也会生成在**仓库根目录**（构建输出位置），内容与本目录中的完全一致；
  构建时会自动同步一份到本目录，历史版本不会被覆盖或删除。
- 本目录中的 `1.0.1` 与 Release `v26.3` 的附件**功能完全相同**，但字节不完全一致：
  Release 附件为 136,147 字节，本目录的为 136,055 字节，
  差异仅在 `META-INF/MANIFEST.MF`（Loom 在不同构建环境下写入的元信息长度不同）。
  所有 class 文件、`fabric.mod.json`、`mixins.json` 与图标均逐字节相同。
- 构建产物中的 `-sources.jar` 是源码包、`-dev.jar` 是开发版，**都不能直接放进游戏**。
