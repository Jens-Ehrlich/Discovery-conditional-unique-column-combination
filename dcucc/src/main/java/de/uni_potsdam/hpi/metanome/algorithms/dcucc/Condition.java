package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
                                  RelationalInput input)
      throws Exception {

    //build condition
    List<ColumnCondition> conditions = new LinkedList<>();
    for (ColumnCombinationBitset conditionColumn : this.conditions.keySet()) {
      if (conditionColumn.size() != 1) {
        throw new AlgorithmExecutionException(
            "only a single column was expected for a conditional, but multiple were found");
      }

      List<String> conditionValues = new LinkedList<>();
      for (Long index : this.conditions.get(conditionColumn)) {
        //TODO add correct strings
        conditionValues.add(index.toString());
      }
      ColumnCondition
          condition =
          new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames()
              .get(conditionColumn.getSetBits().get(0))), conditionValues);
      conditions.add(condition);
    }

    //build partial ColumnCombination
    List<ColumnIdentifier> partialColumnCombination = new LinkedList<>();
    for (int columnIndex : this.partialUnique.getSetBits()) {
      partialColumnCombination
          .add(new ColumnIdentifier(input.relationName(), input.columnNames().get(columnIndex)));
    }

    ConditionalUniqueColumnCombination
        conditionalUniqueColumnCombination =
        new ConditionalUniqueColumnCombination(new ColumnCombination(partialColumnCombination
                                                                         .toArray(
                                                                             new ColumnIdentifier[partialColumnCombination
                                                                                 .size()])),
                                               conditions);

    receiver.receiveResult(conditionalUniqueColumnCombination);
  }

}
