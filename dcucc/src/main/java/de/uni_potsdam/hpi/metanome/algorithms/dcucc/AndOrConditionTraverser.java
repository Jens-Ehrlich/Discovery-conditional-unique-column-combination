package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    //return result
    combineClusterIntoResult(partialUnique);
  }

  @Override
  protected void calculateCondition(ColumnCombinationBitset partialUnique,
                                    Map<ColumnCombinationBitset, PositionListIndex> currentLevel,
                                    ColumnCombinationBitset conditionColumn,
                                    PositionListIndex conditionPLI) throws
                                                                    AlgorithmExecutionException {
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    //check which conditions hold
    List<LongArrayList>
        conditions =
        this.calculateConditions(this.algorithm.getPLI(partialUnique),
                                 conditionPLI,
                                 this.algorithm.frequency,
                                 unsatisfiedClusters);

    if (!unsatisfiedClusters.isEmpty()) {
      currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
    }
    Long2LongOpenHashMap conditionHashMap = conditionPLI.asHashMap();

    List<ConditionEntry> clusters = new LinkedList<>();
    for (LongArrayList cluster : conditions) {
      clusters
          .add(new ConditionEntry(conditionColumn, cluster));
    }

    if (clusters.isEmpty()) {
      return;
    }
    for (ColumnCombinationBitset singeConditionColumn : conditionColumn
        .getContainedOneColumnCombinations()) {
      List<ConditionEntry> existingCluster;
      if (singleConditions.containsKey(singeConditionColumn)) {
        existingCluster = singleConditions.get(singeConditionColumn);
      } else {
        existingCluster = new LinkedList<>();
        singleConditions.put(singeConditionColumn, existingCluster);
      }
      existingCluster.addAll(clusters);
    }

//    for (LongArrayList condition : conditions) {
//      this.algorithm.addConditionToResult(partialUnique, conditionColumn, condition);
//    }
  }

  public List<LongArrayList> calculateConditions(PositionListIndex partialUnique,
                                                 PositionListIndex PLICondition,
                                                 int frequency,
                                                 List<LongArrayList> unsatisfiedClusters) {
    List<LongArrayList> result = new LinkedList<>();
    Long2LongOpenHashMap uniqueHashMap = partialUnique.asHashMap();
    LongArrayList touchedClusters = new LongArrayList();
    nextCluster:
    for (LongArrayList cluster : PLICondition.getClusters()) {
      int unsatisfactionCount = 0;
      touchedClusters.clear();
      for (long rowNumber : cluster) {
        if (uniqueHashMap.containsKey(rowNumber)) {
          if (touchedClusters.contains(uniqueHashMap.get(rowNumber))) {
            unsatisfactionCount++;
          } else {
            touchedClusters.add(uniqueHashMap.get(rowNumber));
          }
        }
      }
      if (unsatisfactionCount == 0) {
        result.add(cluster);
      } else {
        if ((cluster.size() - unsatisfactionCount) >= frequency) {
          unsatisfiedClusters.add(cluster);
        }
      }
    }
    return result;
  }
}