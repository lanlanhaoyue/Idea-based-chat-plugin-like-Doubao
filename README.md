# Idea-based Chat Plugin like Doubao

![Version](https://img.shields.io/badge/version-1.0.0-2f6fed)
![IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2024.1%2B-111827)
![Java](https://img.shields.io/badge/Java-17-f97316)
![API](https://img.shields.io/badge/API-OpenAI%20Compatible-16a34a)

一个运行在 IntelliJ IDEA 里的 AI 编程助手插件，目标是在 IDE 右侧提供类似豆包 / ChatGPT 的代码对话体验。它可以读取选中的代码，调用 OpenAI 兼容接口进行解释、优化、生成注释，也可以在聊天窗口里围绕当前项目继续提问。

[下载插件压缩包](https://github.com/lanlanhaoyue/Idea-based-chat-plugin-like-Doubao/releases/download/v1.0.0/ai-code-assistant-1.0.0.zip) · [查看 Releases](https://github.com/lanlanhaoyue/Idea-based-chat-plugin-like-Doubao/releases/tag/v1.0.0)

## 功能概览

| 功能 | 说明 |
| --- | --- |
| 右侧 AI 聊天窗口 | 在 IntelliJ IDEA 右侧打开 `AI Assistant` 工具窗口，支持连续对话和流式输出。 |
| 选中代码右键发送 | 在编辑器中选中代码后，通过右键菜单让 AI 解释代码、优化代码、生成注释或基于代码继续对话。 |
| OpenAI 兼容接口 | 可配置 `Base URL`、`API Key` 和 `Model`，支持 OpenAI 以及兼容 Chat Completions 的代理或模型服务。 |
| 项目结构查看 | 聊天窗口内置“项目结构”按钮，可以快速把当前项目目录结构展示给 AI 对话上下文使用。 |
| 项目文件读取 | 通过“读文件”按钮输入相对路径，让插件读取项目文件内容并展示在聊天窗口中。 |
| AI 生成文件写入 | 当 AI 回复包含 `<!-- FILE: path --> ... <!-- END_FILE -->` 文件块时，插件会自动识别，并提供预览、写入、全部写入操作。 |
| 中文提示词 | 解释、优化、注释等内置操作默认使用中文提示词，更适合中文开发场景。 |

## 使用场景

- 看不懂一段代码时，选中代码后右键选择解释代码。
- 需要重构或排查潜在问题时，让 AI 分析性能、可读性、可维护性和安全风险。
- 写文档注释时，让 AI 根据已有代码生成方法注释、参数说明和关键逻辑注释。
- 想像豆包一样在 IDEA 侧边栏聊天时，直接打开 `AI Assistant` 工具窗口连续提问。
- 想让 AI 生成或修改项目文件时，让它按文件块格式返回内容，再由插件预览并写入。

## 安装方式

1. 下载 Release 中的插件包：
   [ai-code-assistant-1.0.0.zip](https://github.com/lanlanhaoyue/Idea-based-chat-plugin-like-Doubao/releases/download/v1.0.0/ai-code-assistant-1.0.0.zip)
2. 打开 IntelliJ IDEA。
3. 进入 `Settings` -> `Plugins`。
4. 点击齿轮按钮，选择 `Install Plugin from Disk...`。
5. 选择下载的 `ai-code-assistant-1.0.0.zip`。
6. 重启 IDE。

## 配置方式

安装后进入：

```text
File -> Settings -> Tools -> AI Code Assistant
```

填写以下配置：

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| `Base URL` | Chat Completions 接口根地址。 | `https://api.openai.com/v1` |
| `API Key` | 模型服务的访问密钥。 | 空 |
| `Model` | 要调用的模型名称。 | `gpt-3.5-turbo` |

只要服务兼容 OpenAI 的 `/chat/completions` 接口，就可以在这里替换为自己的模型网关、代理服务或兼容平台地址。

## 右键菜单

在编辑器中选中代码后，右键菜单会出现以下操作：

| 菜单项 | 作用 |
| --- | --- |
| `AI: 解释代码` | 分析代码整体功能、关键逻辑和实现技巧。 |
| `AI: 优化代码` | 给出性能、可读性、可维护性、安全性建议，并生成优化示例。 |
| `AI: 生成注释` | 为代码生成方法注释、参数说明和关键逻辑注释。 |
| `AI: 对话此代码` | 将选中代码带入聊天窗口，继续自由提问。 |

## 聊天窗口能力

聊天窗口位于 IDEA 右侧 `AI Assistant` 工具窗口中，支持：

- `Ctrl + Enter` 快速发送消息。
- “清空”按钮重置当前会话。
- “项目结构”按钮读取当前项目目录概览。
- “读文件”按钮按相对路径读取项目文件。
- 流式显示 AI 回复，减少等待感。
- 检测 AI 回复中的文件块，并提供写入按钮。

AI 需要创建或修改文件时，可以返回下面的格式：

```text
<!-- FILE: src/main/java/com/example/Hello.java -->
文件内容
<!-- END_FILE -->
```

插件会识别这些文件块，并在聊天窗口中显示“预览”“写入”“全部写入”等操作。

## 本地构建

项目基于 Gradle IntelliJ Platform Plugin 构建：

```bash
./gradlew buildPlugin
```

构建完成后，插件压缩包会生成在：

```text
build/distributions/
```

当前版本产物：

```text
build/distributions/ai-code-assistant-1.0.0.zip
```

## 技术信息

| 项目 | 内容 |
| --- | --- |
| 插件名称 | `AI Code Assistant` |
| 插件 ID | `com.aiassistant.codeai` |
| 版本 | `1.0.0` |
| IDEA 兼容范围 | `2024.1+`，`sinceBuild=241`，`untilBuild=261.*` |
| Java 版本 | `17` |
| 主要依赖 | IntelliJ Platform、Gson、Java HttpClient |

## 目录结构

```text
src/main/java/com/aiassistant
├── AIService.java                  # 调用 OpenAI 兼容 Chat Completions 接口
├── ChatPanel.java                  # 右侧聊天窗口 UI 和交互逻辑
├── AIActionHelper.java             # 右键菜单动作公共逻辑
├── ProjectFileService.java         # 项目结构读取、文件读取和文件写入
├── AISettingsState.java            # 插件配置持久化
├── settings/
│   └── AISettingsConfigurable.java # Settings 配置页面
└── actions/
    ├── ExplainCodeAction.java      # 解释代码
    ├── OptimizeCodeAction.java     # 优化代码
    ├── GenerateCommentAction.java  # 生成注释
    └── ChatAboutCodeAction.java    # 带代码上下文聊天
```

## 注意事项

- 需要先配置 `API Key`，否则右键菜单和聊天请求不会发送。
- 插件通过 OpenAI 兼容的 Chat Completions 接口工作，请确认你的模型服务支持流式或非流式聊天返回。
- 文件写入能力只会写入当前项目目录内的相对路径，避免越界修改项目外文件。
