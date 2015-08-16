
package com.asoroka.trippi.impl.jena.testframe;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.junit.experimental.theories.ParametersSuppliedBy;

@Retention(RUNTIME)
@ParametersSuppliedBy(TripleSetsSupplier.class)
public @interface RandomTripleSets {

    /**
     * @return number of triplesets
     */
    short sets();

    /**
     * @return number of triples in each set
     */
    short triples();

}