package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.ConditionalUniqueFixture;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jens Hildebrandt
 */
public class DuccAndNotTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
    algorithm.conditionLatticeTraverser = new NotAndConditionTraverser(algorithm);
  }

  @Test
  @Ignore
  public void testConditionalUniqueFixtureExecuteWithNotConditions() throws Exception {
    //Setup
    ConditionalUniqueFixture fixture = new ConditionalUniqueFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getConditionalUniqueResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, false);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 3);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifyConditionalUniqueColumnCombinationFor();
  }
}
