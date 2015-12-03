
package com.asoroka.trippi.impl.sparql.integration;

import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.riot.Lang.NTRIPLES;
import static org.apache.jena.riot.RDFDataMgr.loadModel;
import static org.apache.jena.riot.web.HttpOp.execHttpDelete;
import static org.apache.jena.riot.web.HttpOp.execHttpPost;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

public class FedoraIT extends IT {

    private static final String REPOSITORY_ADMINISTRATOR_NAME = "admin";

    private static final String FEDORA_URI = "http://localhost:" + PORT + "/trippi-sparql-fcrepo-webapp";

    private static final String RI_URI = FEDORA_URI + "/risearch";

    final List<String> systemObjectNames = asList("<info:fedora/fedora-system:ServiceDeployment-3.0>",
                    "<info:fedora/fedora-system:ServiceDefinition-3.0>", "<info:fedora/fedora-system:FedoraObject-3.0>",
                    "<info:fedora/fedora-system:ContentModel-3.0>");

    private static final Literal admin = createPlainLiteral(REPOSITORY_ADMINISTRATOR_NAME);

    /**
     * All system objects should be owned by the repository administrator (admin), which provides a simple query test.
     */
    @Test
    public void testSystemObjectTriples() {
        systemObjectNames.forEach(name -> {
            Model triples = triplesForObjectName(name);
            final Property ownerId = triples.createProperty("<info:fedora/fedora-system:def/model#ownerId>");
            triples.listStatements(triples.createResource(name), ownerId, (RDFNode) null).mapWith(Statement::getObject)
                            .forEachRemaining(o -> assertEquals(admin, o));
        });
    }

    @Test
    public void addAndRemoveAnObject() {
        // create an object
        execHttpPost(FEDORA_URI + "/objects/test:1", "text/xml", "");
        final String testObjectName = "<info:fedora/test:1>";
        // find some triples for it
        Model triples = triplesForObjectName(testObjectName);
        final Resource subject = triples.createResource(testObjectName);
        // check that a few appropriate triples have been indexed
        triples.listStatements(subject, triples.createProperty("<info:fedora/fedora-system:def/model#ownerId>"),
                        (RDFNode) null).mapWith(Statement::getObject).forEachRemaining(o -> assertEquals(admin, o));
        // delete the test object
        execHttpDelete(FEDORA_URI + "/objects/test:1");
        triples = triplesForObjectName(testObjectName);
        // check that the indexed triples are gone
        assertTrue(triples.isEmpty());
    }

    private static Model triplesForObjectName(final String name) {
        try {
            final String query = encode("CONSTRUCT { " + name + " ?p ?o } WHERE { " + name + " ?p ?o }", "UTF8");
            final String queryUri = RI_URI + "?type=triples&lang=sparql&format=N-Triples&query=" + query;
            return loadModel(queryUri, NTRIPLES);
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
