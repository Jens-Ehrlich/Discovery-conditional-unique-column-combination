package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.ConditionalUniqueColumnCombinationAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecification;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecificationBoolean;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecificationCsvFile;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecificationInteger;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.FileInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.OmniscientResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.BasicStatistic;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.FunctionalDependency;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.InclusionDependency;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithms.ducc.DuccAlgorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mockup comment
 *
 * @author Jens Hildebrandt
 */
public class Dcucc implements ConditionalUniqueColumnCombinationAlgorithm,
                              FileInputParameterAlgorithm,
                              RelationalInputParameterAlgorithm,
                              IntegerParameterAlgorithm,
                              BooleanParameterAlgorithm {

  protected static final String INPUT_FILE_TAG = "csvIterator";
  protected static final String FREQUENCY_TAG = "frequency";
  protected static final String PERCENTAGE_TAG = "percentage";

  protected int frequency = -1;
  protected int numberOfTuples = -1;
  protected boolean percentage = false;
  protected List<PositionListIndex> basePLI;

  protected ImmutableList<ColumnCombinationBitset> uccs;
  protected ImmutableList<ColumnCombinationBitset> partialUccs;
  protected Map<ColumnCombinationBitset, PositionListIndex> pliMap;

  protected RelationalInputGenerator inputGenerator;
  protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

  @Override
  public List<ConfigurationSpecification> getConfigurationRequirements() {
    LinkedList<ConfigurationSpecification> spec = new LinkedList<>();
    ConfigurationSpecificationCsvFile
        csvFile =
        new ConfigurationSpecificationCsvFile(INPUT_FILE_TAG);
    spec.add(csvFile);
    ConfigurationSpecificationInteger
        frequency =
        new ConfigurationSpecificationInteger(FREQUENCY_TAG);
    spec.add(frequency);
    ConfigurationSpecificationBoolean
        percentage =
        new ConfigurationSpecificationBoolean(PERCENTAGE_TAG);
    spec.add(percentage);
    return spec;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    RelationalInput input = calculateInput();

    UniqueColumnCombinationResultReceiver dummyReceiver = createDummyResultReceiver();

//        DuccAlgorithm UCCAlgorithm = new DuccAlgorithm(input.relationName(), input.columnNames(), dummyReceiver);
//        UCCAlgorithm.run(this.basePLI);
//        this.uccs = UCCAlgorithm.getMinimalUniqueColumnCombinations();

    DuccAlgorithm
        partialUCCalgorithm =
        new DuccAlgorithm(input.relationName(), input.columnNames(), dummyReceiver);
    partialUCCalgorithm.setRawKeyError(this.numberOfTuples - this.frequency);
    partialUCCalgorithm.run(this.basePLI);
    this.partialUccs = partialUCCalgorithm.getMinimalUniqueColumnCombinations();
    this.pliMap = partialUCCalgorithm.getCalculatedPlis();

    //this.calculateConditionalUniques();

    ConditionalUniqueColumnCombination cu = new ConditionalUniqueColumnCombination(
        new ColumnCombination(
            new ColumnIdentifier(input.relationName(), input.columnNames().get(0))),
        new ColumnCondition(
            new ColumnIdentifier(input.relationName(), input.columnNames().get(0)),
            "hello world"),
        new ColumnCondition(
            new ColumnIdentifier(input.relationName(), input.columnNames().get(0)),
            "foo bar"));

    this.resultReceiver.receiveResult(cu);
  }

  protected void calculateConditionalUniques() {
    List<ColumnCombinationBitset> firstLevel = this.calculateFirstLevel();


  }

  protected List<ColumnCombinationBitset> calculateFirstLevel() {
    List<ColumnCombinationBitset> firstLevel = new LinkedList<>();
    for (ColumnCombinationBitset columnCombination : this.partialUccs) {
      if (this.pliMap.get(columnCombination).isUnique()) {
        continue;
      }
      firstLevel.add(columnCombination);
    }
    return firstLevel;
  }

  protected RelationalInput calculateInput()
      throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    RelationalInput input;
    input = inputGenerator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input);
    basePLI = pliBuilder.getPLIList();
    numberOfTuples = (int) pliBuilder.getNumberOfTuples();
    if (percentage) {
      frequency = (int) Math.ceil(numberOfTuples * frequency * 1.0d / 100);
    }
    if (frequency < 0) {
      throw new AlgorithmConfigurationException();
    }

    return input;
  }


  @Override
  public void setResultReceiver(ConditionalUniqueColumnCombinationResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;

  }

  @Override
  public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(INPUT_FILE_TAG)) {
      inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }


  @Override
  public void setRelationalInputConfigurationValue(String identifier,
                                                   RelationalInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(INPUT_FILE_TAG)) {
      inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }

  }

  @Override
  public void setBooleanConfigurationValue(String identifier, boolean... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(PERCENTAGE_TAG)) {
      this.percentage = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  @Override
  public void setIntegerConfigurationValue(String identifier, int... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(FREQUENCY_TAG)) {
      this.frequency = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  protected UniqueColumnCombinationResultReceiver createDummyResultReceiver() {
    return new OmniscientResultReceiver() {
      @Override
      public void receiveResult(BasicStatistic statistic) throws CouldNotReceiveResultException {

      }

      @Override
      public void receiveResult(
          ConditionalUniqueColumnCombination conditionalUniqueColumnCombination)
          throws CouldNotReceiveResultException {

      }

      @Override
      public void receiveResult(FunctionalDependency functionalDependency)
          throws CouldNotReceiveResultException {

      }

      @Override
      public void receiveResult(InclusionDependency inclusionDependency)
          throws CouldNotReceiveResultException {

      }

      @Override
      public void receiveResult(UniqueColumnCombination uniqueColumnCombination)
          throws CouldNotReceiveResultException {

      }
    };
  }
}

