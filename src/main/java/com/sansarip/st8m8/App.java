package com.sansarip.st8m8;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.sansarip.st8m8.Utilities.createScripts;
import static com.sansarip.st8m8.Utilities.resourceToFile;


public class App implements ToolWindowFactory {
    Digraph digraph;
    SmartGraphPanel graphView = null;
    JFXPanel panel;
    ToolWindow toolWindow = null;
    private final SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();

    public App() {
        this.digraph = new DigraphEdgeList();
        this.panel = new JFXPanel();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createScripts();

        Platform.setImplicitExit(false);
        this.toolWindow = toolWindow;

        // store object properties for action-access
        final DB panel = new DB(this);

        this.digraph.insertVertex("A");
        this.digraph.insertVertex("B");

        this.digraph.insertEdge("A", "B", "AB");
        this.digraph.insertEdge("B", "A", "BA");

       addPanel(this.digraph);
    }

    private Scene createScene(SmartGraphPanel graphView) {
        return new Scene(graphView, 1024, 768);
    }

    void addPanel(Digraph dg) {
        Platform.runLater(() -> {
            SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(dg, this.strategy);
            graphView.setAutomaticLayout(true);
            this.digraph = dg;
            this.graphView = graphView;
            Scene scene = this.createScene(graphView);
            this.panel.setScene(scene);
            graphView.init();
        });
        JComponent component = this.toolWindow.getComponent();
        component.getParent().add(this.panel);
    }
}