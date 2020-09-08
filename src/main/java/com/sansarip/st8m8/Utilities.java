package com.sansarip.st8m8;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import javafx.application.Platform;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
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

    public static String resourceToUri(String fname, String resourcePath) {
        File f = resourceToFile(fname, resourcePath);
        try {
            return f.toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
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
        FileEditorManager fem = FileEditorManager.getInstance(project);
        VirtualFile[] virtualFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        //Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        //VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        VirtualFile currentFile = virtualFiles[0];
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

    public static Map<String, Map<String, String>> toHashMap(String json) throws IOException {
        return (Map<String, Map<String, String>>) new ObjectMapper().readValue(json, Map.class);

    }

    public static Map<String, Map<String, String>> readClojureFile(String fileName) {
        try {
            String json = execCmd(String.format("./bb.sh \"%s\"", fileName))
                    .replaceAll("\\\\\"", "\"") // \" to "
                    .replaceAll("\\\\\\\\\"", "\\\\\"") // \\\" to \"
                    .replaceAll("^\"+|\"+$", "");
            return toHashMap(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }


    public static void updateGraph(App app, Map<String, Map<String, String>> nodeMap) {
        app.digraph = new DigraphEdgeList<>();

        // vertices
        for (Map.Entry<String, Map<String, String>> nodeMapEntry : nodeMap.entrySet()) {
            String k = nodeMapEntry.getKey();
            app.digraph.insertVertex(k);
        }

        // edges
        for (Map.Entry<String, Map<String, String>> nodeMapEntry : nodeMap.entrySet()) {
            String k = nodeMapEntry.getKey();
            for (Map.Entry<String, String> edgeEntry : nodeMapEntry.getValue().entrySet()) {
                app.digraph.insertEdge(k, edgeEntry.getValue(), new EdgeLabel(k, edgeEntry.getValue(), edgeEntry.getKey()));
            }
        }

        app.setGraphPanelScene(app.digraph);
    }

    public static void loadClojureFile(Project project) {
        if (project != null) {
            String fileName = targetFileName(project);
            App app = getApp(project);
            if (app != null && !app.isLoading) {
                app.load("Making graph");

                // Read in Clojure file and update graph
                Platform.runLater(() -> {
                    Map<String, Map<String, String>> nodeMap = readClojureFile(fileName);
                    updateGraph(app, nodeMap);
                    app.isLoading = false;
                });
            }
        }
    }

    public static Boolean loadableFile(Project project, String fileName, Boolean equals) {
        return project != null &&
                ((fileName.endsWith(".edn") ||
                        fileName.endsWith(".clj") ||
                        fileName.endsWith(".cljs") ||
                        fileName.endsWith(".cljc")) ||
                        fileName.equals("")) &&
                (equals == fileName.equals(targetFileName(project)));
    }

    public static void watchAndLoad(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            new Thread(() -> {
                String fileName = "";
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (loadableFile(project, fileName, false)) {
                            loadClojureFile(project);
                            fileName = targetFileName(project);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });

    }
}



