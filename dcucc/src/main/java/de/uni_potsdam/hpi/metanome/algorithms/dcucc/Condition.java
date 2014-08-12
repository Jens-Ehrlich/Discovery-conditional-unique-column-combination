package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionOr;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ConditionValue;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Jens Hildebrandt
 */
public class Condition {

  protected ColumnCombinationBitset partialUnique;
  protected Map<ColumnCombinationBitset, LongArrayList> conditions;

  public Condition(ColumnCombinationBitset partialUnique,
                   Map<ColumnCombinationBitset, LongArrayList> conditions) {
    this.partialUnique = partialUnique;
    this.conditions = conditions;
  }


  public void addToResultReceiver(ConditionalUniqueColumnCombinationResultReceiver receiver,
                                  RelationalInput input, List<Map<Long, String>> valuesMap)
      throws AlgorithmExecutionException {

    ColumnConditionOr columnCondition = new ColumnConditionOr();
    //build condition
    List<ColumnCondition> conditions = new LinkedList<>();
    for (ColumnCombinationBitset conditionColumn : this.conditions.keySet()) {
//      if (conditionColumn.size() != 1) {
//        throw new AlgorithmExecutionException(
//            "only a single column was expected for a conditional, but multiple were found");
//      }

      TreeSet<String> conditionValues = new TreeSet<>();
      for (Long index : this.conditions.get(conditionColumn)) {

        conditionValues.add(valuesMap.get(conditionColumn.getSetBits().get(0)).get(index));
      }
      for (String conditionValue : conditionValues) {
        columnCondition.add(new ConditionValue(new ColumnIdentifier(input.relationName(),
                                                                    input.columnNames().get(
                                                                        conditionColumn.getSetBits()
                                                                            .get(0))),
                                               conditionValue));
      }
//      ColumnCondition
//          condition =
//          new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames()
//              .get(conditionColumn.getSetBits().get(0))), conditionValues);
//      conditions.add(condition);
    }

    ConditionalUniqueColumnCombination
        conditionalUniqueColumnCombination =
        new ConditionalUniqueColumnCombination(
            this.partialUnique.createColumnCombination(input.relationName(), input.columnNames()),
            columnCondition);

    receiver.receiveResult(conditionalUniqueColumnCombination);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Condition condition = (Condition) o;

    if (!conditions.equals(condition.conditions)) {
      return false;
    }
    if (!partialUnique.equals(condition.partialUnique)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = partialUnique.hashCode();
    result = 31 * result + conditions.hashCode();
    return result;
  }

}
