package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionAnd;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionOr;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionValue;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Jens Ehrlich
 */
public class Condition {

  protected ColumnCombinationBitset partialUnique;
  protected Map<ColumnCombinationBitset, SingleCondition> conditions;

  public Condition(ColumnCombinationBitset partialUnique,
                   Map<ColumnCombinationBitset, SingleCondition> conditions) {
    this.partialUnique = partialUnique;
    this.conditions = conditions;
  }


  public void addToResultReceiver(RelationalInput input, List<Map<Long, String>> valuesMap)
      throws AlgorithmExecutionException {

    ColumnConditionOr columnCondition = new ColumnConditionOr();
    //build condition
    List<ColumnCondition> conditions = new LinkedList<>();
    for (ColumnCombinationBitset conditionColumn : this.conditions.keySet()) {
      if (conditionColumn.size() == 1) {
        addValuesToCondition(input, valuesMap, columnCondition, conditionColumn,
                             this.conditions.get(conditionColumn));
      } else {
        ColumnConditionAnd andCondition = new ColumnConditionAnd();
        for (ColumnCombinationBitset singleBitset : conditionColumn
            .getContainedOneColumnCombinations()) {
          addValuesToCondition(input, valuesMap, andCondition, singleBitset,
                               this.conditions.get(conditionColumn));
        }
        columnCondition.add(andCondition);
      }
    }

    ConditionalUniqueColumnCombination
        conditionalUniqueColumnCombination =
        new ConditionalUniqueColumnCombination(
            this.partialUnique.createColumnCombination(input.relationName(), input.columnNames()),
            columnCondition);

    ResultSingleton.getInstance().receiveResult(conditionalUniqueColumnCombination);
  }

  protected void addValuesToCondition(RelationalInput input, List<Map<Long, String>> valuesMap,
                                      ColumnCondition columnCondition,
                                      ColumnCombinationBitset conditionColumn,
                                      SingleCondition singleCondition) {
    TreeSet<String> conditionValues = new TreeSet<>();
    for (long index : singleCondition.cluster) {
      conditionValues.add(valuesMap.get(conditionColumn.getSetBits().get(0)).get(index));
    }
    for (String conditionValue : conditionValues) {
      ColumnIdentifier
          identifier =
          new ColumnIdentifier(input.relationName(),
                               input.columnNames().get(conditionColumn.getSetBits().get(0)));
      columnCondition
          .add(new ColumnConditionValue(identifier, conditionValue, singleCondition.isNegated));
    }
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

