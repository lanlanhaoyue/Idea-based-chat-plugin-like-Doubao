package com.aiassistant;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class AIChatToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatPanel chatPanel = new ChatPanel(project);
        project.putUserData(AIActionHelper.CHAT_PANEL_KEY, chatPanel);
        Content content = ContentFactory.getInstance().createContent(chatPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}