package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import org.junit.Before;

/**
 * @author Jens Hildebrandt
 */
public class DcuccSimpleTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
    algorithm.conditionLatticeTraverser = new SimpleConditionTraverser(algorithm);
  }
}
