package com.sansarip.st8m8.actions;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sansarip.st8m8.App;
import com.sansarip.st8m8.Utilities;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReadAction extends AnAction {

    private class EdgeLabel {
        String inboundEdge;
        String outboundEdge;
        String label;

        EdgeLabel(String outboundEdge, String inboundEdge, String label) {
            this.outboundEdge = outboundEdge;
            this.inboundEdge = inboundEdge;
            this.label = label;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 31).
                    append(inboundEdge).
                    append(outboundEdge).
                    append(label).
                    toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EdgeLabel))
                return false;
            if (obj == this)
                return true;

            EdgeLabel other = (EdgeLabel) obj;
            return new EqualsBuilder().
                    append(inboundEdge, other.inboundEdge).
                    append(outboundEdge, other.outboundEdge).
                    append(label, other.label).
                    isEquals();
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

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

        app.addPanel(app.digraph);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            String fileName = Utilities.targetFileName(project);
            Map<String, Map<String, String>> nodeMap = Utilities.readClojureFile(fileName);
            App app = Utilities.getApp(project);
            if (app != null) {
                updateGraph(app, nodeMap);
            }
        }
    }
}
