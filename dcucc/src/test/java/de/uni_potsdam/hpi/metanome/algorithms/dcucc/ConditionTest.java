package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCondition;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnConditionOr;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ConditionValue;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.isA;
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
    ColumnIdentifier
        identifier4 =
        new ColumnIdentifier(input.relationName(), input.columnNames().get(4));
    ColumnIdentifier
        identifier5 =
        new ColumnIdentifier(input.relationName(), input.columnNames().get(5));
    ColumnCondition
        condition1 =
        new ColumnConditionOr();
    condition1.add(new ConditionValue(identifier4, "1"));
    condition1.add(new ConditionValue(identifier4, "2"));
    condition1.add(new ConditionValue(identifier4, "3"));
    condition1.add(new ConditionValue(identifier4, "4"));

    condition1.add(new ConditionValue(identifier5, "4"));
    condition1.add(new ConditionValue(identifier5, "5"));
    condition1.add(new ConditionValue(identifier5, "6"));
    condition1.add(new ConditionValue(identifier5, "7"));


    ConditionalUniqueColumnCombination
        excpectedConditionalUnique =
        new ConditionalUniqueColumnCombination(partialUniqueCombination, condition1);

    List<Map<Long, String>> inputMap = new ArrayList();
    HashMap<Long, String> mockMap = mock(HashMap.class);
    when(mockMap.get(isA(Long.class))).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return invocation.getArguments()[0].toString();
      }
    });
    for (int i = 0; i < 8; i++) {
      inputMap.add(mockMap);
    }


    //Execute functionality
    actualCondition.addToResultReceiver(resultReceiver, input, inputMap);
    //Check result
    verify(resultReceiver).receiveResult(excpectedConditionalUnique);
  }
}