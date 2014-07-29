package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.ConditionalUniqueAndOrFixture;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jens Hildebrandt
 */
public class DuccAndOrTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
    algorithm.conditionLatticeTraverser = new AndOrConditionTraverser(algorithm);
  }

  @Test
  public void testAlgorithmFixtureExecute4OrConditions() throws Exception {
    //Setup
    AlgorithmTestFixture fixture = new AlgorithmTestFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getConditionalUniqueResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, false);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 4);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifyConditionalUniqueColumnCombinationFor4OrConditions();
  }

  @Test
  @Ignore
  public void testConditionalUniqueAndOrFixture3() throws Exception {
    //Setup
    ConditionalUniqueAndOrFixture fixture = new ConditionalUniqueAndOrFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getConditionalUniqueResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, false);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 3);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifiyConditionalUniqueColumnCombinationForAndOr();
  }
}
