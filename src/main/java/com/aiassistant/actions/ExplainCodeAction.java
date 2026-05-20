package com.aiassistant.actions;

import com.aiassistant.AIActionHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ExplainCodeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        String systemPrompt = "你是一名资深软件工程师，擅长代码分析。请用中文对用户提供的代码进行详细解释，包括：\n"
                + "1. 代码的整体功能和目的\n"
                + "2. 关键逻辑和算法说明\n"
                + "3. 使用的编程模式和技巧\n"
                + "请使用清晰、易懂的语言。";

        String userPrompt = "请详细解释这段代码。";

        AIActionHelper.executeAction(project, editor, systemPrompt, userPrompt);
    }
}