package de.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class AndOrConditionTraverser extends OrConditionTraverser {


  public AndOrConditionTraverser(Dcucc algorithm) {
    super(algorithm);
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    singleConditions = new HashMap<>();
    Map<ColumnCombinationBitset, PositionListIndex> currentLevel = new HashMap<>();

    //calculate first level - initialisation
    for (ColumnCombinationBitset conditionColumn : this.algorithm.baseColumn) {
      //TODO better way to prune this columns
      if (partialUnique.containsColumn(conditionColumn.getSetBits().get(0))) {
        continue;
      }
      calculateCondition(partialUnique, currentLevel, conditionColumn,
                         this.algorithm.getPLI(conditionColumn));
    }

    currentLevel = apprioriGenerate(currentLevel);

    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    while (!currentLevel.isEmpty()) {
      for (ColumnCombinationBitset potentialCondition : currentLevel.keySet()) {
        nextLevel.clear();
        calculateCondition(partialUnique, nextLevel, potentialCondition,
                           currentLevel.get(potentialCondition));
      }
      //TODO what if nextLevel is already empty?
      currentLevel = apprioriGenerate(nextLevel);
    }
    combineClusterIntoResult(partialUnique);
  }
}