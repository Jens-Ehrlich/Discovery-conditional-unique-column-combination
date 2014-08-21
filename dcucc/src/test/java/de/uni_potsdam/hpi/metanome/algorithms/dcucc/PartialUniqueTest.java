package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithms.ducc.DuccAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.HepatitisFixture;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * @author Jens Ehrlich
 */
public class PartialUniqueTest {

  @Test
  public void partialUniqueTest()
      throws AlgorithmExecutionException, FileNotFoundException, UnsupportedEncodingException {
    HepatitisFixture fixture = new HepatitisFixture();
    RelationalInput input = fixture.getInputGenerator().generateNewCopy();
    DuccAlgorithm
        ducc =
        new DuccAlgorithm(input.relationName(), input.columnNames(),
                          fixture.getUCCResultReceiver());
    ducc.setRawKeyError(152);
    PLIBuilder builder = new PLIBuilder(input);
    ducc.run(builder.getPLIList());

    fixture.verifyConditionalUniqueColumnCombination();
  }

}
