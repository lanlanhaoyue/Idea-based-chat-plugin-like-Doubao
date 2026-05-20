package com.aiassistant;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.aiassistant.AISettingsState",
    storages = @Storage("AICodeAssistantSettings.xml")
)
public class AISettingsState implements PersistentStateComponent<AISettingsState> {

    public String baseUrl = "https://api.openai.com/v1";
    public String apiKey = "";
    public String model = "gpt-3.5-turbo";

    public static AISettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AISettingsState.class);
    }

    @Nullable
    @Override
    public AISettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AISettingsState state) {
        this.baseUrl = state.baseUrl;
        this.apiKey = state.apiKey;
        this.model = state.model;
    }
}