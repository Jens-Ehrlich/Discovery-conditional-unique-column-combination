package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Hildebrandt
 */
public class SimpleAndConditionTraverser implements ConditionLatticeTraverser {

  protected Dcucc algorithm;

  public SimpleAndConditionTraverser(Dcucc algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
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
  }

  protected void calculateCondition(ColumnCombinationBitset partialUnique,
                                    Map<ColumnCombinationBitset, PositionListIndex> currentLevel,
                                    ColumnCombinationBitset conditionColumn,
                                    PositionListIndex conditionPLI)
      throws AlgorithmExecutionException {
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    //check which conditions hold
    List<LongArrayList>
        conditions =
        ConditionalPositionListIndex.calculateConditions(this.algorithm.getPLI(partialUnique),
                                                         conditionPLI,
                                                         this.algorithm.frequency,
                                                         unsatisfiedClusters);
    List<LongArrayList>
        negatedConditions =
        ConditionalPositionListIndex
            .calculateNotConditions(this.algorithm.getPLI(partialUnique), conditionPLI,
                                    this.algorithm.frequency,
                                    this.algorithm.numberOfTuples);
    if (!unsatisfiedClusters.isEmpty()) {
      currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
    }
    for (LongArrayList condition : conditions) {
      this.algorithm.addConditionToResult(partialUnique, conditionColumn, condition);
    }
  }


  protected Map<ColumnCombinationBitset, PositionListIndex> apprioriGenerate(
      Map<ColumnCombinationBitset, PositionListIndex> previousLevel) {
    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    int nextLevelCount = -1;
    ColumnCombinationBitset union = new ColumnCombinationBitset();
    for (ColumnCombinationBitset bitset : previousLevel.keySet()) {
      if (nextLevelCount == -1) {
        nextLevelCount = bitset.size() + 1;
      }
      union = bitset.union(union);
    }

    List<ColumnCombinationBitset> nextLevelCandidates;
    Map<ColumnCombinationBitset, Integer> candidateGenerationCount = new HashMap<>();
    for (ColumnCombinationBitset subset : previousLevel.keySet()) {
      nextLevelCandidates = union.getNSubsetColumnCombinationsSupersetOf(subset, nextLevelCount);
      for (ColumnCombinationBitset nextLevelCandidateBitset : nextLevelCandidates) {
        if (candidateGenerationCount.containsKey(nextLevelCandidateBitset)) {
          int count = candidateGenerationCount.get(nextLevelCandidateBitset);
          count++;
          candidateGenerationCount.put(nextLevelCandidateBitset, count);
        } else {
          candidateGenerationCount.put(nextLevelCandidateBitset, 1);
        }
      }
    }

    for (ColumnCombinationBitset candidate : candidateGenerationCount.keySet()) {
      if (candidateGenerationCount.get(candidate) == nextLevelCount) {
        nextLevel.put(candidate, getConditionPLI(candidate, previousLevel));
      }
    }
    return nextLevel;
  }

  protected PositionListIndex getConditionPLI(ColumnCombinationBitset candidate,
                                              Map<ColumnCombinationBitset, PositionListIndex> pliMap) {
    PositionListIndex firstChild = null;
    for (ColumnCombinationBitset subset : candidate.getDirectSubsets()) {
      if (pliMap.containsKey(subset)) {
        if (firstChild == null) {
          firstChild = pliMap.get(subset);
        } else {
          return pliMap.get(subset).intersect(firstChild);
        }
      }
    }
    //should never arrive here
    return null;
  }

}
