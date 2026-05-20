plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.aiassistant"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.1")
        bundledPlugins()
    }

    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {
    pluginConfiguration {
        name = "AI Code Assistant"
        id = "com.aiassistant.codeai"
        description = """
            AI-powered code assistant plugin for IntelliJ IDEA.
            - Send selected code to AI for explanation, optimization, or comment generation
            - Independent chat window like ChatGPT
            - Supports any OpenAI-compatible API (e.g., NewAPI)
            - Custom Base URL, API Key, and Model selection
        """.trimIndent()

        changeNotes = """
            1.0.0 - Initial release with code analysis, chat window, and API configuration
        """.trimIndent()

        ideaVersion {
            sinceBuild = "241"
            untilBuild = "261.*"
        }

        vendor {
            name = "AI Assistant"
            url = "https://github.com/ai-assistant"
        }
    }

    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named("buildSearchableOptions") {
    enabled = false
}