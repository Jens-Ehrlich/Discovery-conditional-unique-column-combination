package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConditionTest {

  @Test
  public void testAddToResultReceiver() throws Exception {
    //Setup
    ColumnCombinationBitset partialUnique = new ColumnCombinationBitset(0, 1);
    Map<ColumnCombinationBitset, LongArrayList> conditions = new HashMap<>();
    long[] cluster1 = {1, 2, 3, 4};
    long[] cluster2 = {4, 5, 6, 7};
    conditions.put(new ColumnCombinationBitset(4), new LongArrayList(cluster1));
    conditions.put(new ColumnCombinationBitset(5), new LongArrayList(cluster2));
    Condition actualCondition = new Condition(partialUnique, conditions);

    ConditionalUniqueColumnCombinationResultReceiver resultReceiver = mock(
        ConditionalUniqueColumnCombinationResultReceiver.class);
    RelationalInput input = mock(RelationalInput.class);

    when(input.relationName()).thenReturn("table");
    when(input.columnNames())
        .thenReturn(ImmutableList.of("col1", "col2", "col3", "col4", "col5", "col6"));

    ColumnCombination
        partialUniqueCombination =
        new ColumnCombination(new ColumnIdentifier(input.relationName(), input.columnNames().get(
            0)), new ColumnIdentifier(input.relationName(), input.columnNames().get(1)));
    ColumnCondition
        condition1 =
        new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames().get(4)),
                            "1", "2", "3", "4");
    ColumnCondition
        condition2 =
        new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames().get(5)),
                            "4", "5", "6", "7");

    ConditionalUniqueColumnCombination
        excpectedConditionalUnique =
        new ConditionalUniqueColumnCombination(partialUniqueCombination, condition1, condition2);
    //Execute functionality
    actualCondition.addToResultReceiver(resultReceiver, input);
    //Check result
    verify(resultReceiver).receiveResult(excpectedConditionalUnique);
  }
}