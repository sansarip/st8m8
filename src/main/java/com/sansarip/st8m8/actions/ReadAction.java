package com.sansarip.st8m8.actions;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sansarip.st8m8.App;
import com.sansarip.st8m8.EdgeLabel;
import com.sansarip.st8m8.Utilities;
import javafx.application.Platform;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReadAction extends AnAction {

    private void updateGraph(App app, Map<String, Map<String, String>> nodeMap) {
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            String fileName = Utilities.targetFileName(project);
            App app = Utilities.getApp(project);
            if (app != null && !app.isLoading) {
                app.load("Making graph");

                // Read in Clojure file and update graph
                Platform.runLater(() -> {
                    Map<String, Map<String, String>> nodeMap = Utilities.readClojureFile(fileName);
                    updateGraph(app, nodeMap);
                    app.isLoading = false;
                });
            }
        }
    }
}
