
package com.asoroka.trippi.impl.sparql.integration;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.web.HttpOp;
import org.junit.Assert;

public abstract class IT extends Assert {

    private static final String CONTAINER_PORT_PROPERTY = "dynamic.test.port";

    public final static int PORT = parseInt(getProperty(CONTAINER_PORT_PROPERTY, "8080"));

    static {
        HttpOp.setDefaultAuthenticator(new SimpleAuthenticator("fedoraAdmin", "fc".toCharArray()));
        HttpOp.setUseDefaultClientWithAuthentication(true);
    }

}
