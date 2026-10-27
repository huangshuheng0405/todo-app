# 待办 · Todoist 风格 Android 待办应用

用 Jetpack Compose 写的待办清单，视觉与交互参照 Todoist：品牌红、圆形优先级复选框、底部导航、整页任务详情、左滑删除带撤销。

## 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| **JDK** | **25** | ⚠️ 必须。AGP 9.4.1 会在配置阶段请求 `languageVersion=25` 的工具链 |
| Android SDK | Platform **37** | `compileSdk = 37`，`minSdk = 24` |
| Gradle | 9.6 | wrapper 自带，不用单独装 |
| Android Studio | 较新版本（自带 JBR 25） | 推荐用它跑 |

### JDK 25 是唯一容易卡住的地方

如果没有 JDK 25，构建会失败并提示：

```
Unable to download toolchain matching the requirements ({languageVersion=25 ...})
```

两种解决办法：

1. **用 Android Studio 自带的 JBR**（最省事）
   - Windows：`C:\Program Files\Android\Android Studio\jbr`
   - macOS：`/Applications/Android Studio.app/Contents/jbr/Contents/Home`
2. 单独安装 [Temurin 25](https://adoptium.net/)，然后设置 `JAVA_HOME`

命令行编译前先设置：

```bash
# Windows (Git Bash)
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"

# macOS / Linux
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

## 快速开始

### 方式一：Android Studio（推荐）

1. `File → Open`，选**项目根目录**（含 `settings.gradle.kts` 的那层，不是 `app/`）
2. 等 Gradle Sync 完成（首次会下载依赖，几分钟）
3. 选一个设备或新建模拟器，点 Run ▶

> `local.properties`（SDK 路径）由 Android Studio 自动生成，不需要手动创建，也**不要提交**。

### 方式二：命令行

```bash
# 1. 告诉 Gradle SDK 在哪（路径换成你自己的）
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
#    或者直接设环境变量 export ANDROID_HOME=/path/to/Android/Sdk

# 2. 编译调试包 → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleDebug

# 3. 装到已连接的设备/模拟器
./gradlew :app:installDebug

# 4. 跑单元测试（26 个用例）
./gradlew :app:testDebugUnitTest
```

首次 clone 后 `gradlew` 可能没有执行权限（macOS/Linux）：

```bash
chmod +x gradlew
```

## release 包与签名

release 默认开启 R8 压缩与混淆，签名信息从**根目录** `keystore.properties` 读取：

```properties
storeFile=release.keystore
storePassword=你的口令
keyAlias=todoapp
keyPassword=你的口令
```

- 文件**不存在**时会产出一个未签名的 release 包：能编译通过，但不能直接安装或上架
- 自己发布时用 `keytool` 生成 keystore，这两个文件已被 `.gitignore` 排除

## 项目结构

```
app/src/main/java/com/huangshuheng/todo/
├── auth/                 认证：AuthRepository 接口 + LocalAuthRepository 占位实现
├── data/                 Room 实体、DAO、数据库、迁移、仓库
├── ui/
│   ├── AppRoot.kt        App 入口：按登录态切换登录页 / 主界面
│   ├── auth/             登录页与 ViewModel
│   ├── components/       任务行、复选框、滑动删除、分区标题等复用组件
│   ├── screens/          首页 / 列表 / 浏览 / 已完成 / 设置 / 任务详情 / 搜索
│   ├── editor/           快速添加任务的底部面板 + 共用选择器
│   ├── icons/TodoIcons   自绘矢量图标（不依赖 material-icons-extended）
│   ├── theme/            品牌色、字体、扩展配色 token
│   ├── TaskGrouping.kt   分组与排序的纯逻辑（有单测）
│   └── TodoViewModel.kt  按账号隔离的任务视图
└── util/Dates.kt         日期工具（Calendar 实现，兼容 minSdk 24）
```

## 功能

- 登录 / 注册（邮箱 + 密码），登录态持久化，退出登录
- 任务增删改查、优先级 P1–P4、到期日、描述、项目归属
- 首页按 逾期 / 今天 分组；即将到来按天分组；项目内按 逾期 / 今天 / 之后 / 无日期 分组
- 点任务进整页详情，标题描述直接编辑，改动自动保存
- 左滑删除、勾选完成，都可以在 Snackbar 里撤销
- 搜索（标题与描述）、已完成归档、深浅色主题（跟随系统 / 强制浅色 / 强制深色）
- 数据按 `ownerId` 隔离，换账号看不到别人的任务

## 已知限制

- **账号是本机占位实现**：密码用 PBKDF2 加盐哈希后存在 `SharedPreferences`，没有邮箱验证、没有找回密码。真实校验必须由后端完成，接入方式见 `auth/AuthRepository` 的注释
- 包名是 `com.huangshuheng.todo`，应用名「待办」，正式发布前可换成自己的域名反写
- 未实现：提醒通知、重复任务、子任务、标签、项目改名/换色/排序
- 首次安装没有示例数据，空状态文案会引导你新建第一条

## 国际化

所有界面文案都在 `res/values/strings.xml`，Kotlin 里不再有中文硬编码
（分区标题、鉴权错误提示都以字符串资源 id 传递）。

日期格式跟随系统语言：中文环境下显示「10月27日 周二」，英文环境下显示「Oct 27 Tue」。
目前只提供了中文资源，如需英文界面，复制一份 `res/values-en/strings.xml` 翻译即可。

## 数据迁移

数据库当前为 **v2**（v1 → v2 新增 `ownerId`）。迁移在 `data/TodoMigrations.kt`，schema 快照导出在 `app/schemas/`。

以后改表结构时**必须**补一条 `Migration` 并升版本号，否则老用户升级会崩。v1 遗留的无主数据会在首次登录时被认领给当前账号。
