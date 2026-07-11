package me.rerere.rikkahub.data.ai.transformers

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.db.entity.WorkspaceEntity
import me.rerere.rikkahub.data.repository.WorkspaceRepository
import me.rerere.workspace.WorkspaceShellStatus

/**
 * Workspace 系统提示注入转换器
 *
 * 当助手绑定了一个 shell 已就绪的 workspace 时, 在系统提示词中追加一段引导,
 * 让模型了解 workspace 环境与 workspace_* 工具的使用方式。
 */
class WorkspaceReminderTransformer(
    private val workspaceRepository: WorkspaceRepository,
) : InputMessageTransformer {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        val workspaceId = ctx.assistant.workspaceId?.toString() ?: return messages
        val workspace = workspaceRepository.getById(workspaceId) ?: return messages
        // 与 ChatService.createWorkspaceToolsIfReady 保持一致: 仅在 shell 就绪时注入
        if (workspace.shellStatus != WorkspaceShellStatus.READY.name) return messages

        val prompt = buildWorkspacePrompt(workspace)

        // 追加到第一条 system 消息; 若不存在则插入一条
        val systemIndex = messages.indexOfFirst { it.role == MessageRole.SYSTEM }
        return if (systemIndex >= 0) {
            messages.toMutableList().apply {
                this[systemIndex] = this[systemIndex].appendText("\n\n$prompt")
            }
        } else {
            listOf(UIMessage.system(prompt)) + messages
        }
    }
}

private fun buildWorkspacePrompt(workspace: WorkspaceEntity): String = buildString {
    appendLine("<workspace>")
    appendLine("你可以访问一个名为「${workspace.name}」的持久化 Linux 工作区，运行在沙箱化的 proot rootfs 环境中。")
    appendLine("- 工作区文件目录挂载在 `/workspace`，请将其作为你输出正文前的临时编辑工作目录。")
    appendLine("- 所有传递给 workspace 工具的路径必须是绝对路径，且位于 Rootfs 内部（例如 `/workspace/temp.txt`）。")
    appendLine("- 可用工具：")
    appendLine("  - `workspace_read_file`：读取文件内容。")
    appendLine("  - `workspace_write_file` / `workspace_edit_file`：创建文件，或对已有文件做精确编辑。")
    appendLine("  - `workspace_shell`：执行 shell 命令（工作区文件目录挂载在 /workspace）。")
    appendLine("- 适合标准 Unix 工具处理的任务优先使用 `workspace_shell`，需要针对文件做精确修改时优先使用 `workspace_edit_file`。")
    appendLine("- 每轮回复开始前，使用 `workspace_write_file` 覆写 `/workspace/temp.txt` 即会自动清空旧内容，无需额外清理步骤。建议的流程：写入草稿 → `workspace_edit_file` 修改 → `workspace_read_file` 读取最终内容输出。")
    append("</workspace>")
}

private fun UIMessage.appendText(extra: String): UIMessage {
    val updatedParts = parts.toMutableList()
    val firstTextIndex = updatedParts.indexOfFirst { it is UIMessagePart.Text }
    if (firstTextIndex >= 0) {
        val text = updatedParts[firstTextIndex] as UIMessagePart.Text
        updatedParts[firstTextIndex] = text.copy(text = text.text + extra)
    } else {
        updatedParts.add(UIMessagePart.Text(extra))
    }
    return copy(parts = updatedParts)
}
