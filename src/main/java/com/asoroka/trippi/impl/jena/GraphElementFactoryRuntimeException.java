package com.asoroka.trippi.impl.jena;

import org.jrdf.graph.GraphElementFactoryException;

public class GraphElementFactoryRuntimeException extends RuntimeException {

    public GraphElementFactoryRuntimeException(final GraphElementFactoryException e) {
        super(e);
    }

    private static final long serialVersionUID = 1L;

}
