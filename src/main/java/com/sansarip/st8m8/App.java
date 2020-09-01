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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;

import static com.sansarip.st8m8.Utilities.*;

public class App implements ToolWindowFactory {
    public Digraph digraph;
    public Boolean isLoading = false;
    public SmartGraphPanel graphView = null;
    JFXPanel panel;
    ToolWindow toolWindow = null;
    private final SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();

    public App() {
        this.digraph = new DigraphEdgeList();
        this.panel = new JFXPanel();
    }

    private Scene newScene(Parent view) {
        Component parentComponent = this.toolWindow.getComponent().getParent();
        Scene scene = new Scene(view, parentComponent.getWidth(), parentComponent.getHeight());
        scene.getStylesheets().add(resourceToUri("./st8m8.css", "css/st8m8.css"));
        return scene;
    }

    private void setScene(Parent view) {
        this.panel.setScene(newScene(view));
    }

    private void reloadPanel() {
        JComponent component = this.toolWindow.getComponent();
        component.getParent().add(this.panel);
    }

    private void setProperties(Digraph dg, SmartGraphPanel graphView) {
        this.digraph = dg;
        this.graphView = graphView;
    }

    public void load(String message) {
        this.isLoading = true;
        Platform.runLater(() -> {
            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);

            ProgressIndicator loadingSpinner = new ProgressIndicator();
            loadingSpinner.getStyleClass().add("loader");

            Label loadingMessage = new Label(message);
            loadingMessage.getStyleClass().add("loading-message");

            layout.getChildren().addAll(loadingMessage, loadingSpinner);
            layout.getStyleClass().add("container");

            setScene(layout);
        });
        // This must remain outside of the runLater
        reloadPanel();
    }

    public void setGraphPanelScene(Digraph dg) {
        Platform.runLater(() -> {
            SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(dg, this.strategy);
            setProperties(dg, graphView);
            setScene(new Container(graphView));
            graphView.init();
        });
        reloadPanel();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createScripts();
        Platform.setImplicitExit(false);
        this.toolWindow = toolWindow;

        // Store object properties for action-access
        new DB(this);
        setGraphPanelScene(this.digraph);

        // Watch for changes in file focus
        Utilities.watchAndLoad(project);
    }
}