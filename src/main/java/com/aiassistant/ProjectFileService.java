package com.aiassistant;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectFileService {

    private static final Pattern FILE_BLOCK_PATTERN = Pattern.compile(
            "<!--\\s*FILE:\\s*([^\\-]+?)-->\\s*\\n(.*?)\\n<!--\\s*END_FILE\\s*-->",
            Pattern.DOTALL
    );

    public static ProjectFileService getInstance() {
        return ApplicationManager.getApplication().getService(ProjectFileService.class);
    }

    public String getProjectBasePath(Project project) {
        if (project.getBasePath() != null) {
            return project.getBasePath();
        }
        return System.getProperty("user.dir");
    }

    public String buildProjectTree(Project project) {
        String basePath = getProjectBasePath(project);
        if (basePath == null) {
            return "";
        }
        return buildTree(Paths.get(basePath), "", 3);
    }

    private String buildTree(Path dir, String prefix, int maxDepth) {
        if (maxDepth <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        java.io.File[] files = dir.toFile().listFiles();
        if (files == null) {
            return "";
        }
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        List<java.io.File> dirs = new ArrayList<>();
        List<java.io.File> regularFiles = new ArrayList<>();

        for (java.io.File f : files) {
            String name = f.getName();
            if (name.startsWith(".") || "node_modules".equals(name) || "target".equals(name)
                    || "build".equals(name) || "dist".equals(name) || "__pycache__".equals(name)
                    || ".git".equals(name) || ".idea".equals(name) || "out".equals(name)
                    || ".gradle".equals(name) || "bin".equals(name) || "obj".equals(name)) {
                continue;
            }
            if (f.isDirectory()) {
                dirs.add(f);
            } else {
                regularFiles.add(f);
            }
        }

        for (java.io.File f : dirs) {
            sb.append(prefix).append("├── ").append(f.getName()).append("/\n");
            sb.append(buildTree(f.toPath(), prefix + "│   ", maxDepth - 1));
        }
        for (int i = 0; i < regularFiles.size(); i++) {
            java.io.File f = regularFiles.get(i);
            String connector = (i == regularFiles.size() - 1) ? "└── " : "├── ";
            sb.append(prefix).append(connector).append(f.getName()).append("\n");
        }
        return sb.toString();
    }

    public String readFile(Project project, String relativePath) throws IOException {
        String basePath = getProjectBasePath(project);
        Path filePath = Paths.get(basePath, relativePath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IOException("文件路径超出项目范围: " + relativePath);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    public void writeFile(Project project, String relativePath, String content) throws IOException {
        String basePath = getProjectBasePath(project);
        Path filePath = Paths.get(basePath, relativePath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IOException("文件路径超出项目范围: " + relativePath);
        }

        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteAction.run(() -> {
                try {
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            VirtualFileManager.getInstance().syncRefresh();
        });
    }

    public List<FileBlock> parseFileBlocks(String aiResponse) {
        List<FileBlock> blocks = new ArrayList<>();
        Matcher matcher = FILE_BLOCK_PATTERN.matcher(aiResponse);
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            blocks.add(new FileBlock(filePath, content));
        }
        return blocks;
    }

    public static class FileBlock {
        private final String filePath;
        private final String content;

        public FileBlock(String filePath, String content) {
            this.filePath = filePath;
            this.content = content;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getContent() {
            return content;
        }
    }
}