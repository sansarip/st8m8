package com.sansarip.st8m8;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Base64;
import java.util.Objects;

public class Utilities {
    public static File resourceToFile(String fname, String resourcePath) {
        InputStream inputStream = Utilities.class.getClassLoader().getResourceAsStream(resourcePath);
        File file = new File(fname);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void createScripts() {
        resourceToFile("parse.clj", "scripts/parse.clj");
        resourceToFile("bb", "scripts/bb");
        resourceToFile("bb.sh", "scripts/bb.sh");
        try {
            execCmd("chmod +x ./bb");
            execCmd("chmod +x ./bb.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String targetFileName(Project project) {
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        if (currentFile != null) {
            return currentFile.getPath();
        }
        return "";
    }

    public static App getApp(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("St8m8");
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getContent(0);
        if (content != null) {
            return ((DB) content.getComponent()).app;
        }
        return null;
    }
}



