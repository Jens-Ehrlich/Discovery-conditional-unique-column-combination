package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
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

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.HashMap;
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
  protected List<ColumnCombinationBitset> baseColumn;

  protected ImmutableList<ColumnCombinationBitset> uccs;
  protected ImmutableList<ColumnCombinationBitset> partialUccs;
  protected Map<ColumnCombinationBitset, PositionListIndex> pliMap;
  protected List<Condition> foundConditions;

  protected RelationalInputGenerator inputGenerator;
  protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

  public Dcucc() {
    this.foundConditions = new ArrayList<>();
  }

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
    RelationalInput input = this.calculateInput();
    this.createBaseColumns(input);

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

    this.calculateConditionalUniques();
  }


  protected void calculateConditionalUniques() {
    List<ColumnCombinationBitset> firstLevel = this.calculateFirstLevel();
    for (ColumnCombinationBitset partialUnique : firstLevel) {
      for (ColumnCombinationBitset conditionColumn : this.baseColumn) {
        //check which conditions hold
        List<LongArrayList>
            conditions =
            ConditionalPositionListIndex.calculateConditionUnique(this.pliMap.get(partialUnique),
                                                                  this.pliMap.get(conditionColumn));
        for (LongArrayList condition : conditions) {
          if (condition.size() >= this.frequency) {
            addConditonToResult(partialUnique, conditionColumn, condition);
          }
        }
      }
    }
  }

  protected void addConditonToResult(ColumnCombinationBitset partialUnique,
                                     ColumnCombinationBitset conditionColumn,
                                     LongArrayList condition) {
    Map<ColumnCombinationBitset, LongArrayList> conditionMap = new HashMap<>();
    for (ColumnCombinationBitset oneColumn : conditionColumn.getContainedOneColumnCombinations()) {
      conditionMap.put(oneColumn, condition);
    }
    this.foundConditions.add(new Condition(partialUnique, conditionMap));
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

  protected void createBaseColumns(RelationalInput input) {
    this.baseColumn = new ArrayList<>();
    for (int i = 0; i < input.numberOfColumns(); i++) {
      this.baseColumn.add(new ColumnCombinationBitset(i));
    }
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

