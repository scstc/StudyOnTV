# 版本检查与更新提示

客户端回到学习页时检查当前仓库 `scstc/StudyOnTV` 的最新正式 GitHub Release，**有新版才提示**，检查结果缓存 6 小时。家长管理面板「版本更新 → 检查更新」可以主动重新检查。不检查原仓库或蒲公英的版本。

```
GET https://api.github.com/repos/scstc/StudyOnTV/releases/latest
```

| 字段 | 用途 |
|---|---|
| `tag_name` | 带 `v` 前缀的版本号，例如 `v26.4.1.1` |
| `body` | 更新说明，包含 `[force]` 时强制更新 |
| `assets[].browser_download_url` | 对应 TV 或手机 / 平板的 APK 下载地址 |
| `assets[].digest` | GitHub 提供的 `sha256:...` 文件摘要 |

草稿和预发布版本不会推送给客户端。发版时需包含对应设备的 APK，下载地址必须属于当前仓库的 Release。

## 两种提示

| 类型 | 触发 | 表现 |
|---|---|---|
| 强制更新 | 版本号更高，且更新说明包含 `[force]` | 全屏页，返回键无效，需完成更新才能继续 |
| 普通更新 | 版本号更高，且更新说明没有 `[force]` | 右上角小卡片，可关闭 |

## 发版流程

### 分支与版本编号

- 开发分支使用 `年.季度.该季度第几个分支`，年份取后两位，各序号从 1 开始。例如 `26.4.1` 表示 2026 年第 4 季度的第 1 个分支。
- 发版版本使用 `当前分支.该分支第几次发版`。`26.4.1` 分支依次发布 `26.4.1.1`、`26.4.1.2`；新分支 `26.4.2` 从 `26.4.2.1` 开始。
- `app/native/app/build.gradle.kts` 中的 `versionName` 使用上述四段版本号，Git 标签和 Release 使用 `v` 前缀，例如 `v26.4.1.1`。
- Android `versionCode` 在所有正式版本之间保持递增，切换分支、季度或年份时不重置。本分支首版为 `versionName = "26.4.1.1"`、`versionCode = 27`；下一次发版两者分别改为 `26.4.1.2`、`28`。
- TV 和手机 / 平板包共用同一次发版编号。每次发版前检查当前分支、已有标签与已发布的 APK，避免版本号重复或回退；这套编号目前由发版时手动维护。

### 构建与发布

在当前开发分支更新版本号后，构建并签名 TV、手机 / 平板正式包，分别命名为 `StudyOnTV-TV-v26.4.1.1.apk`、`StudyOnTV-Mobile-v26.4.1.1.apk`，放入 `release-artifacts`。将版本修改提交到当前分支并推送后，再发布：

```bash
gh release create v26.4.1.1 release-artifacts/StudyOnTV-TV-v26.4.1.1.apk release-artifacts/StudyOnTV-Mobile-v26.4.1.1.apk \
  --repo scstc/StudyOnTV \
  --target 26.4.1 \
  --title "v26.4.1.1" \
  --notes "本次更新说明"
```

GitHub Release 发布完成并设为 latest 后，客户端即可检测到新版本，无需上传蒲公英。

需要强制更新时，在 Release 说明中加入 `[force]`。发布后客户端再次检查时会看到提示；若已有检查缓存，可在管理面板手动检查。

## 下载与安装

当前客户端从本仓库 GitHub Release 下载对应设备的 APK。更新页的「手动下载」也指向 https://github.com/scstc/StudyOnTV/releases/latest。GitHub 网络不可用时可用手机或电脑下载，再通过 U 盘或 ADB 安装。

下载后检查包名、递增的 `versionCode` 和签名；若 GitHub 返回 SHA-256，也会校验文件摘要。不符合要求时拒绝安装。安装通过 Android 系统安装页面完成，需要用户确认，部分设备需要允许安装未知应用。

首次迁移到本仓库版本需手动安装；原版 APK 的更新源不会因为本仓库发版而改变。若原版与本仓库的发布签名不同，无法直接覆盖安装。

### 可选的 update.json

仓库保留 `scripts/make-update-manifest.mjs`，可为 GitHub Release 生成含版本、下载地址和 SHA-256 的清单；当前客户端不读取这份清单。

```bash
node scripts/make-update-manifest.mjs release-artifacts --notes "· 修复……"
```
