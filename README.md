# RikkaHub RP 改造记录

> 基于 RikkaHub 源码2.4.1 169二次开发，改造为 AI 角色扮演（RP）专用客户端。
> 纯本地私有项目，不发布不上 Git。（该版本破例发布）

---

## 一、构建环境修改

### 1.1 中文路径兼容

**文件**: `gradle.properties`

```properties
android.overridePathCheck=true
```

AGP 在 Windows 上检测到项目路径含非 ASCII 字符（`工作区`）时会拒绝编译。添加此配置跳过路径检查，满足国内编译环境。

**影响**: 仅构建期，零运行期影响。

---

### 1.2 Gradle 镜像源

**文件**: `gradle/wrapper/gradle-wrapper.properties`

```
- distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip
+ distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-9.4.1-bin.zip
```

中国网络环境下 Gradle 官方源连接超时，改为腾讯云镜像。

---

### 1.3 Web 模块构建跳过

**文件**: `web/build.gradle.kts`

将 `buildWebUi` 任务从 `Exec` 类型（执行 pnpm）改为直接创建空静态资源目录。原任务会调用 `pnpm run build` 构建 Web 管理界面（React + Vite），但该功能在纯客户端使用场景下不需要。

**影响**: 浏览器端管理页面不可用，Android App 端不受影响。

---

### 1.4 Crashlytics 上传禁用

**文件**: `app/build.gradle.kts`

```kotlin
afterEvaluate {
    tasks.matching { it.name.startsWith("uploadCrashlyticsMappingFile") }.configureEach {
        enabled = false
    }
}
```

Release 编译时 Firebase Crashlytics 插件会上传混淆映射文件，中国网络下连接超时导致编译失败。禁用此任务。此外 `google-services.json` 为占位文件，Firebase 相关功能（Analytics、Crashlytics）均不可用。

**影响**: 运行期无影响（Firebase 本身在中国不可用）。

---

### 1.5 Compose Material3 内部 API 兼容

**文件**: `app/src/main/java/me/rerere/rikkahub/ui/theme/CustomTheme.kt`

**问题**: Compose BOM `2026.06.01` / Material3 `1.5.0-alpha23` 移除了 `dynamiccolor.DynamicScheme` 等内部 API。原代码直接依赖这些内部类生成自定义主题色方案。

**方案**: 改用公开 API `lightColorScheme()` / `darkColorScheme()` + 手动 HSV 色相偏移算法，从种子颜色衍生全套调色板。

```kotlin
// 替代原 HCT (CAM16) 色彩科学方案
// HSV 近似，精度有妥协但对正常使用影响小
fun generateColorScheme(dark: Boolean): ColorScheme {
    val seedColor = Color(primaryColorArgb.toInt())
    return if (dark) generateDarkScheme(seedColor)
    else generateLightScheme(seedColor)
}
```

---

### 1.6 Workspace 模块 CMake 构建跳过

**文件**: `workspace/build.gradle.kts`

注释了 `externalNativeBuild { cmake { ... } }` 块。该模块的 C++ 源码（`workspace.cpp`、`termux_pty.cpp`）依赖 NDK 编译。

**影响验证**: `workspace.cpp` 为空模板无实际代码；`termux_pty.cpp` 的 PTY 功能由 Gradle 依赖 `termux.terminal.view` 替代提供。预编译的 `libproot_exec.so`、`libproot_loader.so` 位于 `workspace/src/main/jniLibs/`，不依赖 CMake。**实际运行零影响。**

---

## 二、功能修改

### 2.1 双通道上下文过滤（核心）

**文件**: `app/src/main/java/me/rerere/rikkahub/data/ai/GenerationHandler.kt`

**改动位置**: `generateInternal()` 方法，`internalMessages` 组装之前。

**原理**: 多步 Agent 循环中，过往轮次的工具调用记录（`UIMessagePart.Tool`）和思维链（`UIMessagePart.Reasoning`）会随消息历史一起送入 LLM，导致：
- Token 被大量无用信息消耗
- 上下文快速膨胀llm抓不住会话重点
- LLM 行为受到工具调用历史污染（表征漂移/流口水）

**过滤逻辑**:

```kotlin
val cleanMessages = buildList {
    // 过往消息：剥离 Tool 和 Reasoning part
    val historical = messages.dropLast(1).map { msg ->
        msg.copy(parts = msg.parts.filter { 
            it !is UIMessagePart.Tool && it !is UIMessagePart.Reasoning 
        })
    }
    addAll(historical)
    // 当前消息：保留全部 part（多步 Agent 循环需要读取 tool 调用/结果）
    messages.lastOrNull()?.let { add(it) }
}
```

**影响**: 过往消息只剩纯对话文本，Token 节约约 70-90%（取决于 Reasoning 长度）。当前轮次的 Agent 循环不受影响。实际使用为单条消息在接入Deekpeekv4flash的情况下仅仅使用1-2分钱，1元起码可以玩50次

---

### 2.2 Workspace 提示词中文化 + 精简

**文件**: `app/src/main/java/me/rerere/rikkahub/data/ai/transformers/WorkspaceReminderTransformer.kt`

**改动**: `buildWorkspacePrompt()` 方法全部重写。

**删除的内容**:
- `/skills` 技能目录说明（角色扮演不需要）
- `/upload` 用户上传文件说明（无用）
- `cwd` 当前工作目录说明（RP 不需要文件导航）
- 图片文件支持提示（`workspace_read_file` description 中去除）

**新增的内容**:
- 明确 `/workspace` 为"输出正文前的临时编辑工作目录"
- 引导工作流程：`workspace_write_file` 覆写 → `workspace_edit_file` 修改 → `workspace_read_file` 读取输出
- 每轮开始自动清理旧内容（覆写即清空，无需额外步骤）

**中文化**: 描述性文本改为中文，技术名词（工具名、路径格式、proot、rootfs）保留英文。防止llm频繁进入英文思考（进入英文思考不会导致输出异常和工作流异常，但是影响使用体验）

---

### 2.3 Release 签名配置

**文件**: `local.properties`

```properties
storeFile=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX(脱敏)
storePassword=android
keyAlias=androiddebugkey
keyPassword=android
```

使用 Android Debug 证书签名 Release APK，方便本地安装测试。

---

## 三、当前状态

| 模块 | 状态 | 备注 |
|---|---|---|
| 编译 Debug APK | ✅ 通过 | |
| 编译 Release APK | ✅ 通过 | |
| 双通道上下文过滤 | ✅ 生效 | 过往消息剥离 Tool + Reasoning |
| Workspace 提示词 | ✅ 生效 | 中文 + 工作流引导 |
| 自定义主题色 | ✅ 正常 | HSV 近似方案 |
| AWS 工具调用 | ✅ 正常 | 经用户测试验证 |
| Web 管理界面 | ❌ 不可用 | pnpm 构建被跳过 |（启用该功能会在本地网络暴露一个web页面以供访问使用，但是个人rp客户端用不到）
| Firebase 分析/崩溃收集 | ❌ 不可用 | 占位配置 |（国内环境开了也没用）
