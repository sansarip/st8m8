package com.sansarip.st8m8;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LoadInAppAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            String fileContent = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument().getText();
            Parse.parse(fileContent);
            ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow("St8m8");
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getContent(0);
            assert content != null;
            App app = ((DB) content.getComponent()).app;
            app.digraph = new DigraphEdgeList<>();
            app.digraph.insertVertex("X");
            app.digraph.insertVertex("Y");
            app.digraph.insertEdge("X", "Y", "XY");
            app.addPanel(app.digraph);
            toolWindow.getIcon();
        }
    }
}
