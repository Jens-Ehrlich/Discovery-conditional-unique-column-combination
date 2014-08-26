package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionAnd;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionValue;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class SelfConditionFinder {

  public static void calculateSelfConditions(ColumnCombinationBitset partialUnique,
                                             PositionListIndex partialUniquePLI, Dcucc algorithm)
      throws CouldNotReceiveResultException {

    ColumnCondition outerCondition = new ColumnConditionAnd();
    for (LongArrayList cluster : partialUniquePLI.getClusters()) {

      ColumnConditionAnd innerCondition = new ColumnConditionAnd();
      innerCondition.setIsNegated(true);
      outerCondition.add(innerCondition);
      for (ColumnCombinationBitset singleColumn : partialUnique
          .getContainedOneColumnCombinations()) {
        Set<String> values = new HashSet<>();
        for (long row : cluster) {
          values.add(algorithm.inputMap.get(singleColumn.getSetBits().get(0)).get(row));
        }

        for (String value : values) {
          ColumnCondition
              conditionValue =
              new ColumnConditionValue(new ColumnIdentifier(algorithm.input.relationName(),
                                                            algorithm.input.columnNames().get(
                                                                singleColumn.getSetBits().get(0))),
                                       value);
          innerCondition.add(conditionValue);
        }
      }
    }

    ConditionalUniqueColumnCombination
        result =
        new ConditionalUniqueColumnCombination(partialUnique.createColumnCombination(
            algorithm.input.relationName(), algorithm.input.columnNames()), outerCondition);
    algorithm.resultReceiver.receiveResult(result);
  }
}
