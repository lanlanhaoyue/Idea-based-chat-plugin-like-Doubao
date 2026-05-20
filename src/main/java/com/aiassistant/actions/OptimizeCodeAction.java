package com.aiassistant.actions;

import com.aiassistant.AIActionHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OptimizeCodeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        String systemPrompt = "你是一名资深软件工程师，擅长代码优化。请对用户提供的代码进行优化分析，包括：\n"
                + "1. 性能瓶颈和优化建议\n"
                + "2. 代码可读性和可维护性改进\n"
                + "3. 潜在的 bug 和安全问题\n"
                + "4. 提供优化后的代码示例\n"
                + "请用中文回答，给出具体的优化建议和代码。";

        String userPrompt = "请分析并优化这段代码，给出改进后的版本。";

        AIActionHelper.executeAction(project, editor, systemPrompt, userPrompt);
    }
}