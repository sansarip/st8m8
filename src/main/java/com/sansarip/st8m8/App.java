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
import javafx.scene.layout.AnchorPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.sansarip.st8m8.Utilities.createScripts;


public class App implements ToolWindowFactory {
    public Digraph digraph;
    SmartGraphPanel graphView = null;
    JFXPanel panel;
    ToolWindow toolWindow = null;
    private final SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();

    public App() {
        this.digraph = new DigraphEdgeList();
        this.panel = new JFXPanel();
    }

    private void setScene(Container view) {
        this.panel.setScene(new Scene(view));
    }

    private void setProperties(Digraph dg, SmartGraphPanel graphView) {
        this.digraph = dg;
        this.graphView = graphView;
    }

    public void addPanel(Digraph dg) {
        Platform.runLater(() -> {
            SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(dg, this.strategy);
            setProperties(dg, graphView);
            setScene(new Container(graphView));
            graphView.init();
        });
        JComponent component = this.toolWindow.getComponent();
        component.getParent().add(this.panel);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createScripts();
        Platform.setImplicitExit(false);
        this.toolWindow = toolWindow;
        // store object properties for action-access
        new DB(this);
        addPanel(this.digraph);
    }
}