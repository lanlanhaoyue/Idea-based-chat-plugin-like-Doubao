package com.aiassistant;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

import java.util.ArrayList;
import java.util.List;

public class AIActionHelper {

    public static final Key<ChatPanel> CHAT_PANEL_KEY = Key.create("AIAssistant.ChatPanel");

    public static void executeAction(Project project, Editor editor, String systemPrompt, String userPrompt) {
        if (!checkSettings()) {
            return;
        }

        String code = getSelectedText(editor);
        if (code == null || code.isBlank()) {
            Messages.showWarningDialog(project, "请先选中代码", "AI Code Assistant");
            return;
        }

        String language = getLanguage(editor);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));

        String fullUserPrompt = "Language: " + language + "\n\nCode:\n```\n" + code + "\n```\n\n" + userPrompt;
        messages.add(new ChatMessage("user", fullUserPrompt));

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AI Assistant");
        if (toolWindow != null) {
            toolWindow.activate(() -> {
                ChatPanel chatPanel = project.getUserData(CHAT_PANEL_KEY);
                if (chatPanel != null) {
                    chatPanel.sendWithMessages(messages);
                }
            });
        }
    }

    public static void openChatWithCode(Project project, Editor editor) {
        if (!checkSettings()) {
            return;
        }

        String code = getSelectedText(editor);
        String language = getLanguage(editor);

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AI Assistant");
        if (toolWindow != null) {
            toolWindow.activate(() -> {
                ChatPanel chatPanel = project.getUserData(CHAT_PANEL_KEY);
                if (chatPanel != null) {
                    String context = code != null && !code.isBlank()
                            ? "我已经选中了以下 " + language + " 代码，请帮我分析：\n\n```\n" + code + "\n```"
                            : "你好，请帮我分析代码。";
                    chatPanel.sendMessage(context);
                }
            });
        }
    }

    private static String getSelectedText(Editor editor) {
        if (editor == null) {
            return null;
        }
        return editor.getSelectionModel().getSelectedText();
    }

    private static String getLanguage(Editor editor) {
        if (editor == null || editor.getVirtualFile() == null) {
            return "plaintext";
        }
        var fileType = editor.getVirtualFile().getFileType();
        return fileType != null ? fileType.getName() : "plaintext";
    }

    private static boolean checkSettings() {
        AISettingsState settings = AISettingsState.getInstance();
        if (settings.apiKey == null || settings.apiKey.isBlank()) {
            Messages.showErrorDialog(
                    "请先在 File → Settings → Tools → AI Code Assistant 中配置 API Key",
                    "AI Code Assistant"
            );
            return false;
        }
        return true;
    }
}