package com.sansarip.st8m8;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class EdgeLabel {
    String inboundEdge;
    String outboundEdge;
    String label;

    public EdgeLabel(String outboundEdge, String inboundEdge, String label) {
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

