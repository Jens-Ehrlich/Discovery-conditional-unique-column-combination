package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;

public class OrConditionTraverserTest {

  OrConditionFixture fixture;

  @Before
  public void setup() {
    fixture = new OrConditionFixture();
  }

  @Test
  public void testCalculateConditions() throws Exception {
    //Setup
    PositionListIndex uniquePLI = fixture.getUniquePLI();
    PositionListIndex conditionPLI = fixture.getConditionPLI();
    List<LongArrayList> expectedConditions = fixture.getExpectedConditions();
    //Execute functionality
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    OrConditionTraverser traverser = new OrConditionTraverser(new Dcucc());

    List<LongArrayList>
        actualConditions =
        traverser
            .calculateConditions(uniquePLI, conditionPLI, fixture.getFrequency(),
                                 unsatisfiedClusters);
    //Check result
    assertThat(actualConditions,
               IsIterableContainingInAnyOrder.containsInAnyOrder(
                   expectedConditions.toArray()
               )
    );
    //assertEquals(unsatisfiedClusters.get(0), fixture.getExpectedConditions().get(0));
  }
}