package com.sansarip.st8m8;

import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.InvalidVertexException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import javafx.application.Platform;

import java.io.*;

import org.jetbrains.annotations.NotNull;
import st8m8.parsley;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
    public static final String UNKNOWN_ERROR_MESSAGE = "An unknown error occurred and has been logged.";

    public static String resourceFromHomeDir(String fname, String defaultResourcePath) {
        File file = new File(String.join(
                File.separator,
                System.getProperty("user.home"),
                ".st8m8",
                fname));
        if (file.exists()) {
            String fp = file.toURI().toString();
            return fp;
        }
        return Utilities.class.getResource(File.separator + defaultResourcePath + File.separator + fname).toExternalForm();
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
            return ((AppRef) content.getComponent()).app.apps.get(project.getName());
        }
        return null;
    }

    public static Map<String, Map<String, String>> toHashMap(String json) throws IOException {
        return (Map<String, Map<String, String>>) new ObjectMapper().readValue(json, Map.class);

    }

    public static Map<String, Map<String, String>> readClojureFile(String fileName) throws IOException, InvalidFSMException {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        final ClassLoader parentClassLoader = App.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(parentClassLoader);
        try {
            clojure.lang.RT.init(); // Removing this line will cause Compiler.LOADER to throw a null-pointer
            IPersistentMap bindings = clojure.lang.RT.map(Compiler.LOADER, parentClassLoader);
            clojure.lang.Var.pushThreadBindings(bindings);
            try {
                String json = parsley.find_fsm(fileName);
                if (json == null) {
                    throw new InvalidFSMException();
                }
                return toHashMap(json);
            } finally {
                clojure.lang.Var.popThreadBindings();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }


    public static void updateGraph(@NotNull App app, Map<String, Map<String, String>> nodeMap) {
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

        App.setGraphPanelScene(app.digraph, app);
    }

    public static void handleException(App app, Exception exception, String displayMessage, Boolean log) {
        App.displayMessage(displayMessage, app);
        if (log) {
            App.logger.error(exception);
        }
    }

    public  static  void handleException(App app, Exception exception, String displayMessage) {
        handleException(app, exception, displayMessage, true);
    }

    public static void handleException(App app, Exception exception) {
        if (exception instanceof InvalidVertexException || exception instanceof InvalidFSMException) {
            handleException(app, exception, exception.getMessage(), false);
        } else {
            handleException(app, exception, UNKNOWN_ERROR_MESSAGE);
        }
    }

    public static void loadClojureFile(@NotNull Project project) {
        String fileName = targetFileName(project);
        App app = getApp(project);
        if (app != null && !app.isLoading) {
            App.displayMessage("Making graph", app);

            // Read in Clojure file and update graph
            Platform.runLater(() -> {
                try {
                    Map<String, Map<String, String>> nodeMap = readClojureFile(fileName);
                    updateGraph(app, nodeMap);
                } catch (Exception e) {
                    handleException(app, e);
                }
            });
        }
    }

    public static Boolean loadableFile(Project project, String fileName, Boolean equals) {
        return project != null &&
                ((fileName.endsWith(".edn") ||
                        fileName.endsWith(".clj") ||
                        fileName.endsWith(".cljs") ||
                        fileName.endsWith(".cljc")) ||
                        fileName.equals("")) &&
                (equals == null || equals == fileName.equals(targetFileName(project)));
    }

    public static Boolean loadableFile(Project project, String fileName) {
        return loadableFile(project, fileName, null);
    }

    public static void watchAndLoad(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            new Thread(() -> {
                String fileName = "";
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (loadableFile(project, fileName, false)) {
                            fileName = targetFileName(project); // Should be first to prevent error-loop
                            loadClojureFile(project);
                        }
                    } catch (Exception e) {
                        App app = getApp(project);
                        handleException(app, e);
                    }
                }
            }).start();
        });

    }
}



