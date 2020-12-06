package com.sansarip.st8m8;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.HashMap;

import static com.sansarip.st8m8.Utilities.*;

/**
 * Creates and manages St8M8 tool windows.
 * Will be instantiated once by IntelliJ, regardless of how many open windows/projects there are.
 */
public class App implements ToolWindowFactory {
    public Digraph digraph;
    public Boolean isLoading = false;
    public SmartGraphPanel graphView = null;
    public static Logger logger = Logger.getInstance(App.class);
    // This stores App instance references for different open projects
    public HashMap<String, App> apps;
    JFXPanel panel;
    ToolWindow toolWindow = null;

    private final SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();

    public App() {
        this.apps = new HashMap<>();
    }

    public App(ToolWindow toolWindow) {
        this.panel = new JFXPanel();
        this.digraph = new DigraphEdgeList();
        this.toolWindow = toolWindow;
    }

    private App initialize(ToolWindow toolWindow, String appId) {
        App newApp = new App(toolWindow);
        this.apps.put(appId, newApp);
        return newApp;
    }

    private static Scene newScene(Parent view, Integer width, Integer height) {
        Scene scene = new Scene(view, width, height);
        scene.getStylesheets().add(resourceFromHomeDir("st8m8.css", "css"));
        return scene;
    }

    private static void setScene(Parent view, App thisApp) {
        Component parentComponent = thisApp.toolWindow.getComponent().getParent();
        thisApp.panel.setScene(newScene(view, parentComponent.getWidth(), parentComponent.getHeight()));
    }

    private static void reloadPanel(App thisApp) {
        JComponent component = thisApp.toolWindow.getComponent();
        component.getParent().add(thisApp.panel);
    }

    private static void setProperties(Digraph dg, SmartGraphPanel graphView, App thisApp) {
        thisApp.digraph = dg;
        thisApp.graphView = graphView;
    }

    public static void startLoading(String message, App thisApp) {
        thisApp.isLoading = true;
        Platform.runLater(() -> {
            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);

            ProgressIndicator loadingSpinner = new ProgressIndicator();
            loadingSpinner.getStyleClass().add("loader");

            Label loadingMessage = new Label(message);
            loadingMessage.getStyleClass().add("loading-message");

            layout.getChildren().addAll(loadingMessage, loadingSpinner);
            layout.getStyleClass().add("container");

            setScene(layout, thisApp);
        });
        // This must remain outside of the runLater
        reloadPanel(thisApp);
    }

    public static void stopLoading(App thisApp) {
        if (thisApp != null) {
            thisApp.isLoading = false;
        }
    }

    public static void setGraphPanelScene(Digraph dg, App thisApp) {
        Platform.runLater(() -> {
            SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(dg, thisApp.strategy);
            setProperties(dg, graphView, thisApp);
            setScene(new Container(graphView), thisApp);
            graphView.init();
        });
        reloadPanel(thisApp);
    }

    /**
     * Called by IntelliJ once for each project to instantiate tool window content panels
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        String projectName = project.getName();
        App thisApp = initialize(toolWindow, projectName);
        Platform.setImplicitExit(false);

        // Store object properties for action-access
        new AppRef(this, projectName);
        setGraphPanelScene(thisApp.digraph, thisApp);

        // Watch for changes in file focus
        Utilities.watchAndLoad(project);
    }
}