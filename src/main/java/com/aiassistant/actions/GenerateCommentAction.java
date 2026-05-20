package com.aiassistant.actions;

import com.aiassistant.AIActionHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GenerateCommentAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        String systemPrompt = "你是一名资深软件工程师，擅长编写代码注释和文档。请为用户的代码生成详细注释，包括：\n"
                + "1. 函数/方法级别的文档注释（Javadoc/Docstring 等）\n"
                + "2. 关键逻辑的行内注释\n"
                + "3. 参数和返回值的说明\n"
                + "请生成可直接使用的注释代码，格式与原文保持一致，用中文编写注释内容。";

        String userPrompt = "请为这段代码生成详细的注释。";

        AIActionHelper.executeAction(project, editor, systemPrompt, userPrompt);
    }
}