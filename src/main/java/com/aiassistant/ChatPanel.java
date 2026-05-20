package com.aiassistant;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends JPanel implements Disposable {

    private final Project project;
    private final JTextPane chatArea;
    private final JBTextArea inputArea;
    private final JButton sendButton;
    private final JButton clearButton;
    private final JButton showTreeButton;
    private final JButton readFileButton;
    private final JPanel fileActionPanel;
    private final StyledDocument doc;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    private volatile boolean isStreaming = false;
    private String lastAiResponse = "";

    private static final Color USER_BUBBLE_BG = new JBColor(new Color(220, 235, 255), new Color(55, 75, 100));
    private static final Color AI_BUBBLE_BG = new JBColor(new Color(240, 240, 240), new Color(60, 60, 60));
    private static final Color SYSTEM_COLOR = new JBColor(new Color(150, 150, 150), new Color(130, 130, 130));
    private static final Color FILE_COLOR = new JBColor(new Color(200, 180, 100), new Color(180, 160, 80));

    public ChatPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty());

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();
        initStyles();

        JBScrollPane scrollPane = new JBScrollPane(chatArea);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        inputArea = new JBTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(JBUI.Borders.empty(5));
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && (e.isControlDown() || e.isMetaDown())) {
                    e.consume();
                    sendMessage();
                }
            }
        });

        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        clearButton = new JButton("清空");
        clearButton.addActionListener(e -> clearChat());

        showTreeButton = new JButton("项目结构");
        showTreeButton.setFont(showTreeButton.getFont().deriveFont(11f));
        showTreeButton.addActionListener(e -> showProjectTree());

        readFileButton = new JButton("读文件");
        readFileButton.setFont(readFileButton.getFont().deriveFont(11f));
        readFileButton.addActionListener(e -> promptReadFile());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(JBUI.Borders.empty(5));
        inputPanel.add(new JBScrollPane(inputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        buttonPanel.add(readFileButton);
        buttonPanel.add(showTreeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);

        JPanel southPanel = new JPanel(new BorderLayout(5, 0));
        southPanel.setBorder(JBUI.Borders.empty(5, 5, 5, 5));
        southPanel.add(inputPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        fileActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileActionPanel.setVisible(false);
        southPanel.add(fileActionPanel, BorderLayout.NORTH);

        JLabel hintLabel = new JLabel("Ctrl+Enter 发送 | 选中代码右键发送给 AI | 点击「项目结构」查看目录");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 11f));
        hintLabel.setForeground(SYSTEM_COLOR);
        hintLabel.setBorder(JBUI.Borders.empty(2, 5, 2, 5));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(hintLabel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        String basePath = project.getBasePath();
        String pathInfo = basePath != null ? basePath : "(未知)";
        appendSystemMessage("AI Code Assistant 已就绪。\n"
                + "项目路径: " + pathInfo + "\n"
                + "选中代码右键发送给 AI 进行解释/优化/注释。\n"
                + "点击「项目结构」查看目录，点击「读文件」读取项目文件。\n"
                + "AI 可使用 <!-- FILE: path -->...<!-- END_FILE --> 格式创建/修改文件。");
    }

    private void initStyles() {
        Style systemStyle = chatArea.addStyle("system", null);
        StyleConstants.setForeground(systemStyle, SYSTEM_COLOR);
        StyleConstants.setFontSize(systemStyle, 12);
        StyleConstants.setItalic(systemStyle, true);

        Style userLabelStyle = chatArea.addStyle("userLabel", null);
        StyleConstants.setForeground(userLabelStyle, new JBColor(new Color(30, 100, 200), new Color(100, 160, 255)));
        StyleConstants.setBold(userLabelStyle, true);
        StyleConstants.setFontSize(userLabelStyle, 12);

        Style aiLabelStyle = chatArea.addStyle("aiLabel", null);
        StyleConstants.setForeground(aiLabelStyle, new JBColor(new Color(50, 150, 50), new Color(100, 220, 100)));
        StyleConstants.setBold(aiLabelStyle, true);
        StyleConstants.setFontSize(aiLabelStyle, 12);

        Style userBubbleStyle = chatArea.addStyle("userBubble", null);
        StyleConstants.setBackground(userBubbleStyle, USER_BUBBLE_BG);
        StyleConstants.setFontSize(userBubbleStyle, 13);

        Style aiBubbleStyle = chatArea.addStyle("aiBubble", null);
        StyleConstants.setBackground(aiBubbleStyle, AI_BUBBLE_BG);
        StyleConstants.setFontSize(aiBubbleStyle, 13);

        Style errorStyle = chatArea.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, JBColor.RED);
        StyleConstants.setFontSize(errorStyle, 12);
        StyleConstants.setBold(errorStyle, true);

        Style fileStyle = chatArea.addStyle("fileAction", null);
        StyleConstants.setForeground(fileStyle, FILE_COLOR);
        StyleConstants.setFontSize(fileStyle, 12);
        StyleConstants.setBold(fileStyle, true);
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个代码助手，运行在 IntelliJ IDEA 插件中。你可以帮助用户编写、修改、解释代码。\n\n");
        String basePath = project.getBasePath();
        if (basePath != null) {
            sb.append("当前项目路径: ").append(basePath).append("\n");
            sb.append("你可以使用 readFile 工具读取项目文件，或使用 writeFile 工具创建/修改文件。\n");
            sb.append("如果用户询问项目结构，请让他们点击「项目结构」按钮或让你用 readFile 读取特定路径。\n\n");
        }
        sb.append("## 可用工具\n");
        sb.append("1. **readFile(path)** - 用户可点击「读文件」按钮让你读取项目中的文件\n");
        sb.append("2. **writeFile** - 使用以下格式创建/修改文件：\n");
        sb.append("```\n");
        sb.append("<!-- FILE: src/main/java/com/example/Hello.java -->\n");
        sb.append("文件内容代码\n");
        sb.append("<!-- END_FILE -->\n");
        sb.append("```\n");
        sb.append("系统会自动检测这些标记并提供\"写入文件\"按钮。\n");
        sb.append("文件路径是相对于项目根目录的相对路径。\n");
        return sb.toString();
    }

    private void showProjectTree() {
        appendSystemMessage("正在读取项目结构...");
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String tree = ProjectFileService.getInstance().buildProjectTree(project);
            SwingUtilities.invokeLater(() -> {
                if (tree.isEmpty()) {
                    appendSystemMessage("无法读取项目结构（项目路径可能为空）。");
                } else {
                    appendSystemMessage("项目文件结构:\n" + tree);
                }
            });
        });
    }

    private void promptReadFile() {
        String path = JOptionPane.showInputDialog(
                this,
                "请输入要读取的文件路径（相对于项目根目录）：",
                "读取文件",
                JOptionPane.QUESTION_MESSAGE
        );
        if (path != null && !path.isBlank()) {
            readProjectFile(path.trim());
        }
    }

    private void readProjectFile(String relativePath) {
        appendSystemMessage("正在读取: " + relativePath + " ...");
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                String content = ProjectFileService.getInstance().readFile(project, relativePath);
                SwingUtilities.invokeLater(() -> {
                    appendSystemMessage("--- " + relativePath + " ---\n" + content + "\n--- 文件结束 ---");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendErrorMessage("读取失败 " + relativePath + ": " + ex.getMessage());
                });
            }
        });
    }

    public void sendMessage() {
        sendMessage(inputArea.getText().trim());
    }

    public void sendMessage(String text) {
        if (isStreaming) {
            appendSystemMessage("AI 正在回复中，请稍候...");
            return;
        }

        String message = text;
        if (message == null || message.isBlank()) {
            message = inputArea.getText().trim();
        }

        if (message.isBlank()) {
            return;
        }

        inputArea.setText("");
        fileActionPanel.setVisible(false);
        fileActionPanel.removeAll();
        lastAiResponse = "";
        sendButton.setEnabled(false);
        isStreaming = true;

        if (conversationHistory.isEmpty()) {
            conversationHistory.add(new ChatMessage("system", buildSystemPrompt()));
        }

        conversationHistory.add(new ChatMessage("user", message));
        appendUserMessage(message);

        final StringBuilder aiResponse = new StringBuilder();
        appendAiLabel();

        AIService.getInstance().chat(
                new ArrayList<>(conversationHistory),
                flushText -> SwingUtilities.invokeLater(() -> {
                    aiResponse.append(flushText);
                    try {
                        doc.insertString(doc.getLength(), flushText, chatArea.getStyle("aiBubble"));
                        chatArea.setCaretPosition(doc.getLength());
                    } catch (BadLocationException e) {
                        // ignore
                    }
                }),
                error -> SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    sendButton.setEnabled(true);
                    if (aiResponse.length() == 0) {
                        appendErrorMessage(error.getMessage());
                    } else {
                        conversationHistory.add(new ChatMessage("assistant", aiResponse.toString()));
                    }
                    lastAiResponse = aiResponse.toString();
                    checkFileBlocks();
                    appendNewLine();
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    sendButton.setEnabled(true);
                    conversationHistory.add(new ChatMessage("assistant", aiResponse.toString()));
                    lastAiResponse = aiResponse.toString();
                    checkFileBlocks();
                    appendNewLine();
                })
        );
    }

    public void sendWithMessages(List<ChatMessage> messages) {
        if (isStreaming) {
            appendSystemMessage("AI 正在回复中，请稍候...");
            return;
        }

        conversationHistory.clear();
        conversationHistory.add(new ChatMessage("system", buildSystemPrompt()));
        conversationHistory.addAll(messages);

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if ("user".equals(msg.getRole())) {
                appendUserMessage(msg.getContent());
            }
        }

        fileActionPanel.setVisible(false);
        fileActionPanel.removeAll();
        lastAiResponse = "";
        sendButton.setEnabled(false);
        isStreaming = true;

        final StringBuilder aiResponse = new StringBuilder();
        appendAiLabel();

        AIService.getInstance().chat(
                new ArrayList<>(conversationHistory),
                flushText -> SwingUtilities.invokeLater(() -> {
                    aiResponse.append(flushText);
                    try {
                        doc.insertString(doc.getLength(), flushText, chatArea.getStyle("aiBubble"));
                        chatArea.setCaretPosition(doc.getLength());
                    } catch (BadLocationException ignored) {
                    }
                }),
                error -> SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    sendButton.setEnabled(true);
                    if (aiResponse.length() == 0) {
                        appendErrorMessage(error.getMessage());
                    } else {
                        conversationHistory.add(new ChatMessage("assistant", aiResponse.toString()));
                    }
                    lastAiResponse = aiResponse.toString();
                    checkFileBlocks();
                    appendNewLine();
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    sendButton.setEnabled(true);
                    conversationHistory.add(new ChatMessage("assistant", aiResponse.toString()));
                    lastAiResponse = aiResponse.toString();
                    checkFileBlocks();
                    appendNewLine();
                })
        );
    }

    private void checkFileBlocks() {
        if (lastAiResponse == null || lastAiResponse.isEmpty()) {
            return;
        }
        List<ProjectFileService.FileBlock> blocks = ProjectFileService.getInstance().parseFileBlocks(lastAiResponse);
        if (blocks.isEmpty()) {
            return;
        }

        fileActionPanel.removeAll();
        fileActionPanel.setVisible(true);

        appendFileActionLabel("检测到 " + blocks.size() + " 个文件操作:");

        for (int i = 0; i < blocks.size(); i++) {
            ProjectFileService.FileBlock block = blocks.get(i);
            String filePath = block.getFilePath();
            String content = block.getContent();

            JPanel fileRow = new JPanel(new BorderLayout(5, 0));
            JLabel pathLabel = new JLabel(filePath);
            pathLabel.setForeground(FILE_COLOR);
            fileRow.add(pathLabel, BorderLayout.CENTER);

            JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
            JButton previewBtn = new JButton("预览");
            previewBtn.setFont(previewBtn.getFont().deriveFont(11f));
            previewBtn.addActionListener(e -> {
                appendSystemMessage("--- 预览 " + filePath + " ---\n" + content + "\n--- 预览结束 ---");
            });

            JButton writeBtn = new JButton("写入");
            writeBtn.setFont(writeBtn.getFont().deriveFont(11f));
            writeBtn.addActionListener(e -> writeFileAndOpen(filePath, content));

            btnGroup.add(previewBtn);
            btnGroup.add(writeBtn);
            fileRow.add(btnGroup, BorderLayout.EAST);

            fileActionPanel.add(fileRow);
        }

        JButton writeAllBtn = new JButton("全部写入");
        writeAllBtn.setFont(writeAllBtn.getFont().deriveFont(11f));
        writeAllBtn.addActionListener(e -> {
            List<ProjectFileService.FileBlock> allBlocks = ProjectFileService.getInstance().parseFileBlocks(lastAiResponse);
            for (ProjectFileService.FileBlock block : allBlocks) {
                writeFileAndOpen(block.getFilePath(), block.getContent());
            }
            appendSystemMessage("已写入 " + allBlocks.size() + " 个文件。");
            fileActionPanel.setVisible(false);
        });
        fileActionPanel.add(writeAllBtn);

        fileActionPanel.revalidate();
        fileActionPanel.repaint();
    }

    private void writeFileAndOpen(String filePath, String content) {
        try {
            ProjectFileService.getInstance().writeFile(project, filePath, content);
            appendSystemMessage("已写入: " + filePath);

            ApplicationManager.getApplication().invokeLater(() -> {
                String basePath = project.getBasePath();
                if (basePath != null) {
                    VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                            basePath + "/" + filePath);
                    if (file != null) {
                        FileEditorManager.getInstance(project).openFile(file, true);
                    }
                }
            });
        } catch (Exception ex) {
            appendErrorMessage("写入失败 " + filePath + ": " + ex.getMessage());
        }
    }

    public void clearChat() {
        conversationHistory.clear();
        chatArea.setText("");
        fileActionPanel.setVisible(false);
        fileActionPanel.removeAll();
        lastAiResponse = "";
        appendSystemMessage("对话已清空。可以开始新的对话。");
    }

    private void appendSystemMessage(String text) {
        try {
            doc.insertString(doc.getLength(), text + "\n\n", chatArea.getStyle("system"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendUserMessage(String text) {
        try {
            doc.insertString(doc.getLength(), "You\n", chatArea.getStyle("userLabel"));
            doc.insertString(doc.getLength(), text + "\n\n", chatArea.getStyle("userBubble"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendAiLabel() {
        try {
            doc.insertString(doc.getLength(), "AI\n", chatArea.getStyle("aiLabel"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendFileActionLabel(String text) {
        try {
            doc.insertString(doc.getLength(), text + "\n", chatArea.getStyle("fileAction"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendErrorMessage(String text) {
        try {
            doc.insertString(doc.getLength(), "Error: " + text + "\n\n", chatArea.getStyle("error"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendNewLine() {
        try {
            doc.insertString(doc.getLength(), "\n", chatArea.getStyle("aiBubble"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    @Override
    public void dispose() {
    }
}