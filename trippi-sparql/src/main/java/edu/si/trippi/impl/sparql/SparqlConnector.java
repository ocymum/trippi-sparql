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

package edu.si.trippi.impl.sparql;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.riot.web.HttpOp.setDefaultHttpClient;
import static org.apache.jena.sparql.util.FmtUtils.stringForNode;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.jena.graph.Node;
import org.jrdf.graph.GraphElementFactory;
import org.slf4j.Logger;
import org.trippi.RDFUtil;
import org.trippi.TriplestoreConnector;
import org.trippi.TriplestoreReader;
import org.trippi.TriplestoreWriter;
import org.trippi.TrippiException;
import org.trippi.impl.base.ConcurrentTriplestoreWriter;
import org.trippi.impl.base.ConfigurableSessionPool;
import org.trippi.impl.base.DefaultAliasManager;
import org.trippi.impl.base.MemUpdateBuffer;
import org.trippi.impl.base.UpdateBuffer;
import org.trippi.io.TripleIteratorFactory;

/**
 * A {@link TriplestoreConnector} employing SPARQL Update.
 *
 * @author A. Soroka
 */
public class SparqlConnector extends TriplestoreConnector {

    private static final Logger log = getLogger(SparqlConnector.class);

    public static String DEFAULT_URI_BASE = "info:edu.si.fedora";

    public static String uriBase;

    /**
     * In order to resolve relative URIs in a repeatable way, this method should always be used to provide a BASE_URI
     * declaration.
     *
     * @param text a SPARQL Query or Update passage
     * @return the same test with a BASE_URI declaration prefixed
     */
    public static String rebase(final String text) {
        return format("BASE <" + uriBase + ">\n%1$s", text);
    }

    public static final String DEFAULT_BUFFER_FLUSH_BATCH_SIZE = "20000";

    public static final String DEFAULT_BUFFER_SAFE_CAPACITY = "40000";

    public static final String DEFAULT_AUTO_FLUSH_BUFFER_SIZE = "20000";

    public static final String DEFAULT_AUTO_FLUSH_DORMANT_SECONDS = "5";

    public static final String DEFAULT_MAX_HTTP_CONNECTIONS = "10";

    private Map<String, String> config;

    private SparqlSessionFactory factory;

    private TripleIteratorFactory tripleIteratorFactory;

    private TriplestoreWriter writer;

    private final GraphElementFactory elementFactory = new RDFUtil();

    @Override
    public void setConfiguration(final Map<String, String> configuration) throws TrippiException {
        config = configuration;
        open();
    }

    @Override
    public Map<String, String> getConfiguration() {
        return config;
    }

    @Override
    public void setTripleIteratorFactory(final TripleIteratorFactory factory) {
        if (tripleIteratorFactory != null) {
            tripleIteratorFactory.shutdown();
        }
        tripleIteratorFactory = factory;
        try {
            open();
        } catch (final TrippiException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    @Override
    public void init(final Map<String, String> configuration) throws TrippiException {
        setConfiguration(configuration);
    }

    @Override
    public TriplestoreReader getReader() {
        return getWriter();
    }

    @Override
    public TriplestoreWriter getWriter() {
        return writer;
    }

    @Override
    public GraphElementFactory getElementFactory() {
        return elementFactory;
    }

    @Override
    public void open() throws TrippiException {
        if (writer != null) {
            writer.close();
        }
        final int bufferFlushBatchSize = parseInt(config.getOrDefault("bufferFlushBatchSize",
                        DEFAULT_BUFFER_FLUSH_BATCH_SIZE));
        final int bufferSafeCapacity = parseInt(config.getOrDefault("bufferSafeCapacity",
                        DEFAULT_BUFFER_SAFE_CAPACITY));
        final int autoFlushBufferSize = parseInt(config.getOrDefault("autoFlushBufferSize",
                        DEFAULT_AUTO_FLUSH_BUFFER_SIZE));
        final int autoFlushDormantSeconds = parseInt(config.getOrDefault("autoFlushDormantSeconds",
                        DEFAULT_AUTO_FLUSH_DORMANT_SECONDS));
        final boolean readOnly = parseBoolean(config.getOrDefault("readOnly", "false"));
        log.info("This is {}a read-only connector.", readOnly ? "" : "not ");
        final String updateEndpoint = config.get("updateEndpoint");
        log.info("Using update endpoint {}", updateEndpoint);
        final String queryEndpoint = config.getOrDefault("queryEndpoint", updateEndpoint);
        log.info("Using query endpoint {}", queryEndpoint);
        final String constructEndpoint = config.getOrDefault("constructEndpoint", queryEndpoint);
        log.info("Using construct endpoint {}", constructEndpoint);
        final Node graphName = createURI(config.getOrDefault("graphName", "#ri"));
        log.info("Using graph name {}", stringForNode(graphName));
        final String uriBase = config.getOrDefault("uriBase", DEFAULT_URI_BASE);
        log.info("Using URI base {}", uriBase);

        if (factory != null) {
            factory.close();
        }
        factory = new SparqlSessionFactory(updateEndpoint, queryEndpoint, constructEndpoint, graphName, readOnly);

        final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        final int maxConnections = parseInt(config.getOrDefault("maxHttpConnections", DEFAULT_MAX_HTTP_CONNECTIONS));
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        setDefaultHttpClient(new DefaultHttpClient(connectionManager));

        if (tripleIteratorFactory == null) {
            tripleIteratorFactory = new TripleIteratorFactory();
        }

        final ConfigurableSessionPool pool = new ConfigurableSessionPool(factory, 1, 5, 1);
        final UpdateBuffer buffer = new MemUpdateBuffer(bufferFlushBatchSize, bufferSafeCapacity);
        try {
            writer = new ConcurrentTriplestoreWriter(pool, new DefaultAliasManager(), factory.newSession(), buffer,
                            tripleIteratorFactory, autoFlushBufferSize, autoFlushDormantSeconds);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws TrippiException {
        writer.close();
        factory.close();
        tripleIteratorFactory.shutdown();
    }
}
