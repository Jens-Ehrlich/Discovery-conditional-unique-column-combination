package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;

public class ConditionalPositionListIndexTest {


  /**
   * Test method for {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex::calculateConditionUnique}
   */
  @Test
  public void testCalculateConditionUnique() {
    //Setup
    ConditionalPositionListIndexFixture fixture = new ConditionalPositionListIndexFixture();
    PositionListIndex uniquePLI = fixture.getUniquePLIForConditionTest();
    PositionListIndex conditionPLI = fixture.getConditionPLIForConditionTest();
    List<LongArrayList> expectedConditions = fixture.getExpectedConditions();
    //Execute functionality
    List<LongArrayList>
        actualConditions =
        ConditionalPositionListIndex.calculateConditionUnique(uniquePLI, conditionPLI);
    //Check result
    assertThat(actualConditions,
               IsIterableContainingInAnyOrder.containsInAnyOrder(
                   expectedConditions.toArray()
               )
    );

  }
}