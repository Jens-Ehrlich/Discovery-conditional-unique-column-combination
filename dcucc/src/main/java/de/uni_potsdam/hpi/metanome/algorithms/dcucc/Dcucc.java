package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SuperSetGraph;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mockup comment
 *
 * @author Jens Hildebrandt
 */


/*TODO:
add use calculate not condition
adjust condition and result to return found not conditions
traverse condition lattice*/
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
  protected int numberOfColumns = -1;
  protected boolean percentage = false;
  protected List<PositionListIndex> basePLI;
  protected List<ColumnCombinationBitset> baseColumn;

  protected ImmutableList<ColumnCombinationBitset> uccs;
  protected ImmutableList<ColumnCombinationBitset> partialUccs;
  protected Map<ColumnCombinationBitset, PositionListIndex> pliMap;
  protected Set<Condition> foundConditions;

  protected SuperSetGraph lowerPruningGraph;
  protected SubSetGraph upperPruningGraph;
  protected SubSetGraph conditionMinimalityGraph;

  protected RelationalInputGenerator inputGenerator;
  protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

  public Dcucc() {
    this.foundConditions = new HashSet<>();
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    long start = System.nanoTime();
    RelationalInput input = this.calculateInput();
    this.createBaseColumns(input);
    System.out.println("Build plis: " + ((System.nanoTime() - start) / 1000000));

    start = System.nanoTime();
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
    System.out.println("Calculate partial uniques: " + ((System.nanoTime() - start) / 1000000));
    start = System.nanoTime();
    this.preparePruningGraphs();
    System.out.println("Prepare pruning graphs: " + ((System.nanoTime() - start) / 1000000));
    start = System.nanoTime();
    this.iteratePartialUniqueLattice();
    System.out
        .println("Calculated conditional uniques: " + ((System.nanoTime() - start) / 1000000));
    start = System.nanoTime();
    this.returnResult();
    System.out.println("return results: " + ((System.nanoTime() - start) / 1000000));
  }

  protected void preparePruningGraphs() {
    this.lowerPruningGraph = new SuperSetGraph(this.numberOfColumns);
    this.upperPruningGraph = new SubSetGraph();
    this.conditionMinimalityGraph = new SubSetGraph();

    this.lowerPruningGraph.addAll(this.partialUccs);
    this.conditionMinimalityGraph.addAll(this.partialUccs);
  }

  protected void iteratePartialUniqueLattice() throws AlgorithmExecutionException {
    List<ColumnCombinationBitset> currentLevel = this.calculateFirstLevel();
    while (!currentLevel.isEmpty()) {
      for (ColumnCombinationBitset partialUnique : currentLevel) {
        iterateConditionLattice(partialUnique);
      }
      currentLevel = calculateNextLevel(currentLevel);
    }
  }

  protected void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, PositionListIndex> currentLevel = new HashMap<>();

    //calculate first level - initialisation
    for (ColumnCombinationBitset conditionColumn : this.baseColumn) {
      //TODO better way to prune this columns
      if (partialUnique.containsColumn(conditionColumn.getSetBits().get(0))) {
        continue;
      }

      List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
      //check which conditions hold
      List<LongArrayList>
          conditions =
          ConditionalPositionListIndex.calculateConditions(this.getPLI(partialUnique),
                                                           this.getPLI
                                                               (conditionColumn),
                                                           this.frequency, unsatisfiedClusters);
      if (!unsatisfiedClusters.isEmpty()) {
        currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
      }
      for (LongArrayList condition : conditions) {
        addConditionToResult(partialUnique, conditionColumn, condition);
      }
    }

    currentLevel = apprioriGenerate(currentLevel);

    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    while (!currentLevel.isEmpty()) {
      for (ColumnCombinationBitset potentialCondition : currentLevel.keySet()) {
        List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
        List<LongArrayList>
            conditions =
            ConditionalPositionListIndex.calculateConditions(this.getPLI(partialUnique),
                                                             currentLevel.get(potentialCondition),
                                                             this.frequency, unsatisfiedClusters);

        if (!unsatisfiedClusters.isEmpty()) {
          //some times there only empty LongArrayLists, maybe?
          PositionListIndex newPLI = new PositionListIndex(unsatisfiedClusters);
          if (newPLI.isUnique()) {
            nextLevel.put(potentialCondition, new PositionListIndex(unsatisfiedClusters));
          }
        }
        for (LongArrayList validConditions : conditions) {
          addConditionToResult(partialUnique, potentialCondition, validConditions);
        }
      }
      currentLevel = apprioriGenerate(nextLevel);
    }
  }

  protected Map<ColumnCombinationBitset, PositionListIndex> apprioriGenerate(
      Map<ColumnCombinationBitset, PositionListIndex> previousLevel) {
    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    int level = -1;
    ColumnCombinationBitset union = new ColumnCombinationBitset();
    for (ColumnCombinationBitset bitset : previousLevel.keySet()) {
      if (level == -1) {
        level = bitset.size();
      }
      union = bitset.union(union);
    }

    List<ColumnCombinationBitset> nextLevelCandidates;
    Map<ColumnCombinationBitset, Integer> candidateGenerationCount = new HashMap<>();
    for (ColumnCombinationBitset subset : previousLevel.keySet()) {
      nextLevelCandidates = union.getNSubsetColumnCombinationsSupersetOf(subset, level + 1);
      for (ColumnCombinationBitset nextLevelCandidateBitset : nextLevelCandidates) {
        if (candidateGenerationCount.containsKey(nextLevelCandidateBitset)) {
          int count = candidateGenerationCount.get(nextLevelCandidateBitset);
          count++;
          candidateGenerationCount.put(nextLevelCandidateBitset, count);
        } else {
          candidateGenerationCount.put(nextLevelCandidateBitset, 1);
        }
      }
    }

    for (ColumnCombinationBitset candidate : candidateGenerationCount.keySet()) {
      if (candidateGenerationCount.get(candidate) == (level + 1)) {
        nextLevel.put(candidate, getConditionPLI(candidate, previousLevel));
      }
    }

    return nextLevel;
  }

  protected PositionListIndex getConditionPLI(ColumnCombinationBitset candidate,
                                              Map<ColumnCombinationBitset, PositionListIndex> pliMap) {
    PositionListIndex firstChild = null;
    for (ColumnCombinationBitset subset : candidate.getDirectSubsets()) {
      if (pliMap.containsKey(subset)) {
        if (firstChild == null) {
          firstChild = pliMap.get(subset);
        } else {
          return pliMap.get(subset).intersect(firstChild);
        }
      }
    }
    //should never arrive here
    return null;
  }

  protected List<ColumnCombinationBitset> calculateNextLevel(
      List<ColumnCombinationBitset> previousLevel) throws AlgorithmExecutionException {
    List<ColumnCombinationBitset> nextLevel = new LinkedList<>();
    Set<ColumnCombinationBitset> unprunedNexLevel = new HashSet<>();
    for (ColumnCombinationBitset currentColumnCombination : previousLevel) {
      calculateAllParents(currentColumnCombination, unprunedNexLevel);
    }

    for (ColumnCombinationBitset nextLevelBitset : unprunedNexLevel) {
      if ((this.lowerPruningGraph.containsSuperset(nextLevelBitset)) || (this.upperPruningGraph
                                                                             .containsSubset(
                                                                                 nextLevelBitset))) {
        continue;
      } else {
        PositionListIndex nextLevelPLI = this.getPLI(nextLevelBitset);
        if (nextLevelPLI.isUnique()) {
          this.upperPruningGraph.add(nextLevelBitset);
        } else {
          if (!this.checkForFD(nextLevelBitset)) {
            nextLevel.add(nextLevelBitset);
          }
          this.lowerPruningGraph.add(nextLevelBitset);
          this.conditionMinimalityGraph.add(nextLevelBitset);
        }
      }
    }
    return nextLevel;
  }

  protected boolean checkForFD(ColumnCombinationBitset bitset) {
    //TODO is this check really reasonable (performance)?
    for (ColumnCombinationBitset possibleChild : bitset
        .getNSubsetColumnCombinations(bitset.getSetBits().size() - 1)) {
      if (this.pliMap.containsKey(possibleChild)) {
        if (this.pliMap.get(possibleChild).getRawKeyError() == this.pliMap.get(bitset)
            .getRawKeyError()) {
          //FD found
          this.upperPruningGraph.add(bitset);
          return true;
        }
      }
    }
    return false;
  }

  protected void calculateAllParents(ColumnCombinationBitset child,
                                     Set<ColumnCombinationBitset> set) {
    for (int newColumn : child.getClearedBits(this.numberOfColumns)) {
      set.add(new ColumnCombinationBitset(child).addColumn(newColumn));
    }
  }

  protected void returnResult() throws AlgorithmExecutionException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    List<Map<Long, String>> inputMap = new ArrayList<>(input.numberOfColumns());
    for (int i = 0; i < input.numberOfColumns(); i++) {
      inputMap.add(new HashMap<Long, String>());
    }
    long row = 0;
    while (input.hasNext()) {
      ImmutableList<String> values = input.next();
      for (int i = 0; i < input.numberOfColumns(); i++) {
        inputMap.get(i).put(row, values.get(i));
      }
      row++;
    }

    for (Condition condition : this.foundConditions) {
      condition.addToResultReceiver(this.resultReceiver, input, inputMap);
    }
  }

  protected PositionListIndex getPLI(ColumnCombinationBitset bitset)
      throws AlgorithmExecutionException {
    PositionListIndex pli = this.pliMap.get(bitset);
    if (null == pli) {
      //the one of the previous plis always exist
      ColumnCombinationBitset previous = null;
      for (ColumnCombinationBitset previousCandidate : bitset
          .getNSubsetColumnCombinations(bitset.size() - 1)) {
        if (this.pliMap.containsKey(previousCandidate)) {
          previous = previousCandidate;
          break;
        }
      }

      if (previous == null) {
        throw new AlgorithmExecutionException("An expected PLI was not found in the hashmap");
      }

      PositionListIndex previousPLI = this.pliMap.get(previous);
      PositionListIndex missingColumn = this.pliMap.get(bitset.minus(previous));

      pli = previousPLI.intersect(missingColumn);
      this.pliMap.put(bitset, pli);
    }
    return pli;
  }

  protected void addConditionToResult(ColumnCombinationBitset partialUnique,
                                      ColumnCombinationBitset conditionColumn,
                                      LongArrayList conditionArray) {
    Map<ColumnCombinationBitset, LongArrayList> conditionMap = new HashMap<>();
    for (ColumnCombinationBitset oneColumn : conditionColumn.getContainedOneColumnCombinations()) {
      conditionMap.put(oneColumn, conditionArray);
    }
    Condition condition = new Condition(partialUnique, conditionMap);

    for (ColumnCombinationBitset subset : this.conditionMinimalityGraph
        .getExistingSubsets(partialUnique)) {
      condition.partialUnique = subset;
      if (this.foundConditions.contains(condition)) {
        return;
      }
    }
    condition.partialUnique = partialUnique;
    this.foundConditions.add(condition);
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
    numberOfColumns = input.numberOfColumns();
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
}

