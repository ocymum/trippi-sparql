
package edu.si.trippi.impl.sparql;

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

    public static final String BASE_URI = "info:edu.si.fedora";

    /**
     * In order to resolve relative URIs in a repeatable way, this method should always be used to provide a BASE_URI
     * declaration.
     * 
     * @param text a SPARQL Query or Update passage
     * @return the same test with a BASE_URI declaration prefixed
     */
    public static String rebase(String text) {
        return format("BASE <" + BASE_URI + ">\n%1$s", text);
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
        final String updateEndpoint = config.get("updateEndpoint");
        log.info("Using update endpoint {}", updateEndpoint);
        final String queryEndpoint = config.getOrDefault("queryEndpoint", updateEndpoint);
        log.info("Using query endpoint {}", queryEndpoint);
        final String constructEndpoint = config.getOrDefault("constructEndpoint", queryEndpoint);
        log.info("Using construct endpoint {}", constructEndpoint);
        final Node graphName = createURI(config.getOrDefault("graphName", "#ri"));
        log.info("Using graph name {}", stringForNode(graphName));

        if (factory != null) {
            factory.close();
        }
        factory = new SparqlSessionFactory(updateEndpoint, queryEndpoint, constructEndpoint, graphName);

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
