package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AbaloneFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.BridgesFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.ConditionalUniqueFixture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DcuccTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
  }

  @After
  public void tearDown() throws Exception {
    algorithm = null;
  }

  @Test
  public void testAlgorithmFixtureExecute4() throws Exception {
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
    fixture.verifyConditionalUniqueColumnCombinationFor4();
  }

  @Test
  public void testAlgorithmFixtureExecute3() throws Exception {
    //Setup
    AlgorithmTestFixture fixture = new AlgorithmTestFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getConditionalUniqueResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, false);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 3);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifyConditionalUniqueColumnCombinationFor3();
  }

  @Test
  public void testConditionalUniqueFixtureExecute() throws Exception {
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


  @Test
  public void testAbaloneFixtureExecute() throws Exception {
    //Setup
    AbaloneFixture fixture = new AbaloneFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getCUCCResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 30);
    //Execute
    algorithm.execute();

    //verify result
    //fixture.verifyConditionalUniqueColumnCombinationFor4();
  }

  @Test
  public void testBridgesFixtureExecute() throws Exception {
    //Setup
    BridgesFixture fixture = new BridgesFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getCUCCResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 30);
    //Execute
    algorithm.execute();

    //verify result
    //fixture.verifyConditionalUniqueColumnCombinationFor4();
  }

  @Test
  public void testCalculateInputAndConfiguration()
      throws AlgorithmConfigurationException, CouldNotReceiveResultException,
             UnsupportedEncodingException, FileNotFoundException, InputGenerationException,
             InputIterationException {
    //Setup
    AbaloneFixture fixture = new AbaloneFixture();

    RelationalInputGenerator expectedRelationalInputGenerator = fixture.getInputGenerator();
    algorithm.setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG,
                                                   expectedRelationalInputGenerator);

    ConditionalUniqueColumnCombinationResultReceiver
        expectedResultReceiver =
        mock(ConditionalUniqueColumnCombinationResultReceiver.class);
    algorithm.setResultReceiver(expectedResultReceiver);

    int expectedFrequency = 80;
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 80);
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);

    //execute functionality
    algorithm.calculateInput();

    //Check Result
    assertEquals(true, algorithm.percentage);
    // 4177*0.8 = 3341.6 ~ 3342
    assertEquals(3342, algorithm.frequency);
    assertEquals(expectedResultReceiver, algorithm.resultReceiver);


  }
}