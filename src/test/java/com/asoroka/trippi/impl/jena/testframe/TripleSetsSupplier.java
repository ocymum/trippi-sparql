package com.asoroka.trippi.impl.jena.testframe;

import static com.asoroka.trippi.impl.jena.testframe.TripleSetsSupplier.LiteralType.randomLiteralType;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.experimental.theories.PotentialAssignment.forValue;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jrdf.graph.AbstractLiteral;
import org.jrdf.graph.AbstractTriple;
import org.jrdf.graph.AbstractURIReference;
import org.jrdf.graph.Literal;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

@SuppressWarnings("serial")
public class TripleSetsSupplier extends ParameterSupplier {

    @Override
    public List<PotentialAssignment> getValueSources(final ParameterSignature sig) {
        final RandomTripleSets annotation = sig.getAnnotation(RandomTripleSets.class);
        final short numSets = annotation.sets();
        final short numTriples = annotation.triples();

        final List<PotentialAssignment> tripleSets = new ArrayList<>(numSets);
        for (short setIndex = 0; setIndex < numSets; setIndex++) {
            final Builder<Triple> triples = ImmutableSet.builder();
            for (short tripleIndex = 0; tripleIndex < numTriples; tripleIndex++) {
                triples.add(randomTriple());
                tripleSets.add(forValue("", triples.build()));
            }
        }
        return tripleSets;
    }

    private static Triple randomTriple() {
        return new AbstractTriple() {

            private final SubjectNode s = uriRef("info:/subject/");

            private final PredicateNode p = uriRef("info:/predicate/");

            private final ObjectNode o = random() < 0.5 ? uriRef("info:/object/") : randomLiteralType().create();

            @Override
            public SubjectNode getSubject() {
                return s;
            }

            @Override
            public PredicateNode getPredicate() {
                return p;
            }

            @Override
            public ObjectNode getObject() {
                return o;
            }
        };
    }

    static URIReference uriRef(final String prefix) {
        final URI uri = URI.create(prefix + randomUUID());
        return new AbstractURIReference(uri) {/**/};
    }

    static enum LiteralType {
        PLAIN {

            @Override
            Literal create() {
                return new AbstractLiteral(randomLex()) {/**/};
            }
        },
        LANGED {

            @Override
            Literal create() {
                final String lang = randomAlphabetic(2);
                return new AbstractLiteral(randomLex(), lang) {/**/};
            }
        },
        TYPED {

            @Override
            Literal create() {
                final URI datatype = URI.create("info:/datatype/" + randomUUID());
                return new AbstractLiteral(randomLex(), datatype) {/**/};
            }
        };

        final static String randomLex() {
            return randomUUID().toString();
        }

        abstract Literal create();

        static TripleSetsSupplier.LiteralType randomLiteralType() {
            final int index = (int) round(random() * 2);
            return asList(values()).get(index);
        }
    }

    static final Logger log = getLogger(TripleSetsSupplier.class);

}