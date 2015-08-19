
package com.asoroka.trippi.impl.sparqlupdate;

import static java.lang.Integer.parseInt;
import static org.apache.jena.riot.web.HttpOp.setDefaultHttpClient;

import java.io.IOException;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jrdf.graph.GraphElementFactory;
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

public class SparqlUpdateConnector extends TriplestoreConnector {

    public static final String DEFAULT_BUFFER_FLUSH_BATCH_SIZE = "20000";

    public static final String DEFAULT_BUFFER_SAFE_CAPACITY = "40000";

    public static final String DEFAULT_AUTO_FLUSH_BUFFER_SIZE = "20000";

    public static final String DEFAULT_AUTO_FLUSH_DORMANT_SECONDS = "5";

    public static final String DEFAULT_MAX_HTTP_CONNECTIONS = "10";

    private Map<String, String> config;

    private SparqlUpdateSessionFactory factory;

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

    @SuppressWarnings("deprecation")
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
        final String endpoint = config.get("sparqlUpdateEndpoint");

        if (factory != null) {
            factory.close();
        }
        factory = new SparqlUpdateSessionFactory(endpoint);

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
