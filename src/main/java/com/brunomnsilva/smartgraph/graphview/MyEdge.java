package com.brunomnsilva.smartgraph.graphview;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;

public class MyEdge implements Edge<Object, Object> {
    Object element;
    Vertex<Object> vertexOutbound;
    Vertex<Object> vertexInbound;

    public MyEdge(Object element, Vertex<Object> vertexOutbound, Vertex<Object> vertexInbound) {
        this.element = element;
        this.vertexOutbound = vertexOutbound;
        this.vertexInbound = vertexInbound;
    }

    @Override
    public Object element() {
        return this.element;
    }

    public boolean contains(Vertex<Object> v) {
        return (vertexOutbound == v || vertexInbound == v);
    }

    @Override
    public Vertex<Object>[] vertices() {
        Vertex[] vertices = new Vertex[2];
        vertices[0] = vertexOutbound;
        vertices[1] = vertexInbound;

        return vertices;
    }

    @Override
    public String toString() {
        return "Edge{{" + element + "}, vertexOutbound=" + vertexOutbound.toString()
                + ", vertexInbound=" + vertexInbound.toString() + '}';
    }

    public Vertex<Object> getOutbound() {
        return vertexOutbound;
    }

    public Vertex<Object> getInbound() {
        return vertexInbound;
    }
}

