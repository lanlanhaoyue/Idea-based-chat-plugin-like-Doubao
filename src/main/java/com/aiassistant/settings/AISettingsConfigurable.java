package com.aiassistant.settings;

import com.aiassistant.AISettingsState;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AISettingsConfigurable implements Configurable {

    private JBTextField baseUrlField;
    private JBPasswordField apiKeyField;
    private JBTextField modelField;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "AI Code Assistant";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("Base URL:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baseUrlField = new JBTextField(40);
        panel.add(baseUrlField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JBLabel("API Key:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        apiKeyField = new JBPasswordField();
        panel.add(apiKeyField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JBLabel("Model:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        modelField = new JBTextField(40);
        panel.add(modelField, gbc);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    @Override
    public boolean isModified() {
        AISettingsState settings = AISettingsState.getInstance();
        boolean modified = false;
        modified |= !settings.baseUrl.equals(baseUrlField.getText());
        modified |= !settings.apiKey.equals(new String(apiKeyField.getPassword()));
        modified |= !settings.model.equals(modelField.getText());
        return modified;
    }

    @Override
    public void apply() {
        AISettingsState settings = AISettingsState.getInstance();
        settings.baseUrl = baseUrlField.getText();
        settings.apiKey = new String(apiKeyField.getPassword());
        settings.model = modelField.getText();
    }

    @Override
    public void reset() {
        AISettingsState settings = AISettingsState.getInstance();
        baseUrlField.setText(settings.baseUrl);
        apiKeyField.setText(settings.apiKey);
        modelField.setText(settings.model);
    }
}