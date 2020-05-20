package com.sansarip.st8m8;

import com.brunomnsilva.smartgraph.containers.ContentZoomPane;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class Container extends ContentZoomPane {
    public Container(SmartGraphPanel graphView) {
        super(graphView);
        // TODO: Move styling to css file
        this.setStyle("-fx-background-color:#DEE6FF");
        // Create top pane with controls
        HBox top = new HBox(10);
        top.setPadding(new Insets(10, 10, 10, 10));
        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty());
        top.getChildren().add(automatic);
        setTop(top);

    }
}

