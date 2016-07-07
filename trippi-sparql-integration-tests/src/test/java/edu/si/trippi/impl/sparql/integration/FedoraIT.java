/*
 * Copyright 2015-2016 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 *
 */

package edu.si.trippi.impl.sparql.integration;

import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.riot.Lang.NTRIPLES;
import static org.apache.jena.riot.RDFDataMgr.loadModel;
import static org.apache.jena.riot.web.HttpOp.execHttpDelete;
import static org.apache.jena.riot.web.HttpOp.execHttpPost;
import static org.apache.jena.sparql.util.FmtUtils.stringForNode;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

public class FedoraIT extends IT {

    private static final String REPOSITORY_ADMINISTRATOR_NAME = "admin";

    private static final String FEDORA_URI = "http://localhost:" + PORT + "/trippi-sparql-fcrepo-webapp";

    private static final String RI_URI = FEDORA_URI + "/risearch";

    private static final String graphName = stringForNode(createURI("#ri"));

    final List<Resource> systemObjectNames = asList("<info:fedora/fedora-system:ServiceDeployment-3.0>",
                    "<info:fedora/fedora-system:ServiceDefinition-3.0>", "<info:fedora/fedora-system:FedoraObject-3.0>",
                    "<info:fedora/fedora-system:ContentModel-3.0>").stream().map(ResourceFactory::createResource)
                                    .collect(toList());

    private static final Literal admin = createPlainLiteral(REPOSITORY_ADMINISTRATOR_NAME);

    /**
     * All system objects should be owned by the repository administrator (admin), which provides a simple query test.
     */
    @Test
    public void testSystemObjectTriples() {
        systemObjectNames.forEach(name -> {
            Model triples = triplesForObjectName(name);
            final Property ownerId = triples.createProperty("<info:fedora/fedora-system:def/model#ownerId>");
            triples.listObjectsOfProperty(name, ownerId).forEachRemaining(o -> assertEquals(admin, o));
        });
    }

    @Test
    public void addAndRemoveAnObject() {
        // create an object
        execHttpPost(FEDORA_URI + "/objects/test:1", "text/xml", "");
        final Resource testObjectName = createResource("<info:fedora/test:1>");
        // find some triples for it
        Model triples = triplesForObjectName(testObjectName);
        // check that a few appropriate triples have been indexed
        final Property ownerId = triples.createProperty("<info:fedora/fedora-system:def/model#ownerId>");
        triples.listObjectsOfProperty(testObjectName, ownerId).forEachRemaining(o -> assertEquals(admin, o));
        // delete the test object
        execHttpDelete(FEDORA_URI + "/objects/test:1");
        triples = triplesForObjectName(testObjectName);
        // check that the indexed triples are gone
        assertTrue(triples.isEmpty());
    }

    private static Model triplesForObjectName(final Resource r) {
        try {
            final String queryString = "CONSTRUCT { " + r + " ?p ?o } WHERE { GRAPH " + graphName + " { " + r +
                            " ?p ?o } . }";
            final String query = encode(queryString, "UTF8");
            final String queryUri = RI_URI + "?type=triples&lang=sparql&format=N-Triples&query=" + query;
            return loadModel(queryUri, NTRIPLES);
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
