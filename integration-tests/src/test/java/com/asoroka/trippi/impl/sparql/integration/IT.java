package com.asoroka.trippi.impl.sparql.integration;

import static com.yourmediashelf.fedora.client.request.FedoraRequest.setDefaultClient;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraCredentials;

public abstract class IT extends Assert {

	private static final String CONTAINER_PORT_PROPERTY = "dynamic.test.port";

	public final static int PORT = parseInt(getProperty(CONTAINER_PORT_PROPERTY, "8080"));

	@BeforeClass
	public static void setUp() throws MalformedURLException {
		final String baseUrl = "http://localhost:" + PORT + "/fedora";
		setDefaultClient(new FedoraClient(new FedoraCredentials(baseUrl, "fedoraAdmin", "fc")));
	}
}
