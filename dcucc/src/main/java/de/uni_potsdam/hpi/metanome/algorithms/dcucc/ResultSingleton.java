package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class ResultSingleton {
  protected static ResultSingleton singleton;
  protected SubSetGraph conditionMinimalityGraph;
  protected List<Map<Long, String>> inputMap;
  protected Set<Condition> foundConditions;
  protected RelationalInput input;
  private ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

  protected ResultSingleton(RelationalInput input,
                            ImmutableList<ColumnCombinationBitset> partialUccs,
                            ConditionalUniqueColumnCombinationResultReceiver receiver)
      throws InputGenerationException, InputIterationException {
    prepareOutput(input);
    this.resultReceiver = receiver;
    this.input = input;
    this.conditionMinimalityGraph = new SubSetGraph();
    this.foundConditions = new HashSet<>();
    this.conditionMinimalityGraph.addAll(partialUccs);
  }

  public static ResultSingleton getInstance() {
    return singleton;
  }

  public static ResultSingleton createResultSingleton(RelationalInput input,
                                                      ImmutableList<ColumnCombinationBitset> partialUccs,
                                                      ConditionalUniqueColumnCombinationResultReceiver receiver)
      throws InputGenerationException, InputIterationException {
    singleton = new ResultSingleton(input, partialUccs, receiver);
    return singleton;
  }

  protected void prepareOutput(RelationalInput input) throws InputGenerationException, InputIterationException {
    this.inputMap = new ArrayList<>(input.numberOfColumns());
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
  }

  protected void addConditionToResult(ColumnCombinationBitset partialUnique,
                                      ColumnCombinationBitset conditionColumn,
                                      LongArrayList conditionArray)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, SingleCondition> conditionMap = new HashMap<>();
//    for (ColumnCombinationBitset oneColumn : conditionColumn.getContainedOneColumnCombinations()) {
    conditionMap.put(conditionColumn, new SingleCondition(conditionArray));
//    }
    Condition condition = new Condition(partialUnique, conditionMap);

    if (checkConditionMinimality(partialUnique, condition)) {
      return;
    }
    condition.partialUnique = partialUnique;
    this.foundConditions.add(condition);
    condition.addToResultReceiver(input, inputMap);
  }

  protected boolean checkConditionMinimality(ColumnCombinationBitset partialUnique,
                                             Condition condition) {
    for (ColumnCombinationBitset subset : this.conditionMinimalityGraph
        .getExistingSubsets(partialUnique)) {
      condition.partialUnique = subset;
      if (this.foundConditions.contains(condition)) {
        return true;
      }
    }
    return false;
  }

  public void receiveResult(ConditionalUniqueColumnCombination result)
      throws CouldNotReceiveResultException {
    this.resultReceiver.receiveResult(result);
  }

  public void receiveResult(Condition resultCondition) throws AlgorithmExecutionException {
    if (this.checkConditionMinimality(resultCondition.partialUnique, resultCondition)) {
      return;
    }
    this.foundConditions.add(resultCondition);
    resultCondition.addToResultReceiver(this.input, this.inputMap);
  }

}
