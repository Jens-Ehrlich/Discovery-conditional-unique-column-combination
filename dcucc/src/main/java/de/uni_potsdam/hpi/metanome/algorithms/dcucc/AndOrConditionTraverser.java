package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Hildebrandt
 */
public class AndOrConditionTraverser extends OrConditionTraverser {

  Map<ColumnCombinationBitset, Map<ColumnCombinationBitset, Long2ObjectOpenHashMap<LongArrayList>>>
      clusterCombinationMap =
      new HashMap<>();

  public AndOrConditionTraverser(Dcucc algorithm) {
    super(algorithm);
  }

  @Override
  protected void calculateCondition(ColumnCombinationBitset partialUnique,
                                    Map<ColumnCombinationBitset, PositionListIndex> currentLevel,
                                    ColumnCombinationBitset conditionColumn,
                                    PositionListIndex conditionPLI) throws
                                                                    AlgorithmExecutionException {
    boolean isLevelOne = conditionColumn.size() == 1;
    PositionListIndex partialUniquePLI = this.algorithm.getPLI(partialUnique);
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    //not first level
    if (!isLevelOne) {
      //check if new matching clusters exist
      if (!this.checkForMatchingClusters(partialUniquePLI, conditionPLI, unsatisfiedClusters)) {
        if (!unsatisfiedClusters.isEmpty()) {
          currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
        }
        return;
      } else {
        conditionPLI = this.algorithm.getPLI(conditionColumn);
      }

    }

    //check which conditions hold
    List<LongArrayList>
        conditions =
        this.calculateConditions(partialUniquePLI,
                                 conditionPLI,
                                 this.algorithm.frequency,
                                 unsatisfiedClusters);
    if (isLevelOne) {
      if (!unsatisfiedClusters.isEmpty()) {
        currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
      }
    }
    if (!isLevelOne) {
      //FIXME purge not minimal Results
      this.filterNonValidConditions(conditionColumn, conditions);
    }

    for (LongArrayList condition : conditions) {
      this.algorithm.addConditionToResult(partialUnique, conditionColumn, condition);
    }
  }

  protected void filterNonValidConditions(ColumnCombinationBitset conditionColumns,
                                          List<LongArrayList> conditions)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, Long2LongOpenHashMap> columnPLIHashMap = new HashMap<>();
    Map<ColumnCombinationBitset, Map<ColumnCombinationBitset, Long2ObjectOpenHashMap<LongArrayList>>>
        currentClusterCombinations =
        new HashMap<>();

    for (ColumnCombinationBitset currentColumn : conditionColumns
        .getContainedOneColumnCombinations()) {
      columnPLIHashMap.put(currentColumn, this.algorithm.getPLI(currentColumn).asHashMap());
    }
    //build clusterCombinationMap
    for (LongArrayList condition : conditions) {
      for (long rowNumber : condition) {
        for (ColumnCombinationBitset currentColumn : conditionColumns
            .getContainedOneColumnCombinations()) {
          long currentClusterNumber = columnPLIHashMap.get(currentColumn).get(rowNumber);
          for (ColumnCombinationBitset otherColumns : conditionColumns.minus(currentColumn)
              .getContainedOneColumnCombinations()) {
            if (currentClusterCombinations.containsKey(currentColumn)) {
              Map<ColumnCombinationBitset, Long2ObjectOpenHashMap<LongArrayList>>
                  currentClusterMap =
                  currentClusterCombinations.get(currentColumn);
              updateClusterMap(columnPLIHashMap, rowNumber, currentClusterNumber, otherColumns,
                               currentClusterMap);
            } else {
              Map<ColumnCombinationBitset, Long2ObjectOpenHashMap<LongArrayList>>
                  currentClusterMap =
                  new HashMap<>();
              updateClusterMap(columnPLIHashMap, rowNumber, currentClusterNumber, otherColumns,
                               currentClusterMap);
              currentClusterCombinations.put(currentColumn, currentClusterMap);
            }
          }
        }
      }
    }
    for (ColumnCombinationBitset currentColumn : conditionColumns
        .getContainedOneColumnCombinations()) {
      for (ColumnCombinationBitset otherColumns : conditionColumns.minus(currentColumn)
          .getContainedOneColumnCombinations()) {
        Long2ObjectOpenHashMap<LongArrayList>
            actualClusterMap =
            currentClusterCombinations.get(currentColumn).get(otherColumns);
        for (long actualCluster : actualClusterMap.keySet()) {
          for (long actualTouchedClusters : actualClusterMap.get(actualCluster)) {

          }
        }
      }
    }
  }

  protected void updateClusterMap(
      Map<ColumnCombinationBitset, Long2LongOpenHashMap> columnPLIHashMap, long rowNumber,
      long currentClusterNumber, ColumnCombinationBitset otherColumns,
      Map<ColumnCombinationBitset, Long2ObjectOpenHashMap<LongArrayList>> currentClusterMap) {
    if (currentClusterMap.containsKey(otherColumns)) {
      Long2ObjectOpenHashMap<LongArrayList>
          currentClusterToClusterMap =
          currentClusterMap.get(otherColumns);
      updateOtherClusterNumber(columnPLIHashMap, rowNumber, currentClusterNumber,
                               otherColumns,
                               currentClusterToClusterMap);
    } else {
      Long2ObjectOpenHashMap<LongArrayList>
          currentClusterToClusterMap =
          new Long2ObjectOpenHashMap<LongArrayList>();
      updateOtherClusterNumber(columnPLIHashMap, rowNumber, currentClusterNumber,
                               otherColumns,
                               currentClusterToClusterMap);
      currentClusterMap.put(otherColumns, currentClusterToClusterMap);
    }
  }

  protected void updateOtherClusterNumber(
      Map<ColumnCombinationBitset, Long2LongOpenHashMap> columnPLIHashMap, long rowNumber,
      long currentClusterNumber, ColumnCombinationBitset otherColumns,
      Long2ObjectOpenHashMap<LongArrayList> currentClusterToClusterMap) {
    long otherClusterNumber = columnPLIHashMap.get(otherColumns).get(rowNumber);
    if (currentClusterToClusterMap.containsKey(currentClusterNumber)) {
      LongArrayList touchedCluster = currentClusterToClusterMap.get(currentClusterNumber);
      if (!touchedCluster.contains(otherClusterNumber)) {
        touchedCluster.add(otherClusterNumber);
      }
    } else {
      LongArrayList touchedCluster = new LongArrayList();
      touchedCluster.add(otherClusterNumber);
      currentClusterToClusterMap.put(currentClusterNumber, touchedCluster);
    }
  }


  protected boolean checkForMatchingClusters(PositionListIndex partialUniquePLI,
                                             PositionListIndex conditionPLI,
                                             List<LongArrayList> unsatisfiedCluster) {

    List<LongArrayList> satisfiedCluster = new LinkedList<>();
    this.purgePossibleConditions(partialUniquePLI.asHashMap(), conditionPLI.asHashMap(),
                                 conditionPLI, satisfiedCluster, unsatisfiedCluster);

    return !satisfiedCluster.isEmpty();
  }
}