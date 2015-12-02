package com.asoroka.trippi.impl.sparql.integration;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import org.junit.Assert;

public abstract class IT extends Assert {

	private static final String CONTAINER_PORT_PROPERTY = "dynamic.test.port";

	public final static int PORT = parseInt(getProperty(CONTAINER_PORT_PROPERTY, "8080"));

}
