package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class OrConditionTraverser extends SimpleConditionTraverser {

  Map<ColumnCombinationBitset, List<ConditionEntry>> singleConditions;

  public OrConditionTraverser(Dcucc algorithm) {
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
        //if ((cluster.size() - unsatisfactionCount) >= frequency) {
          unsatisfiedClusters.add(cluster);
        //}
      }
    }
    return result;
  }

  protected Long2ObjectOpenHashMap<LongArrayList> purgeIntersectingClusterEntries(
      Long2ObjectOpenHashMap<LongArrayList> result) {
    Long2ObjectOpenHashMap<LongArrayList> purgedResult = new Long2ObjectOpenHashMap<>();
    Iterator<Long> resultIterator = result.keySet().iterator();
    while (resultIterator.hasNext()) {
      long uniqueCluster = resultIterator.next();
      if (result.get(uniqueCluster).size() != 1) {
        purgedResult.put(uniqueCluster, result.get(uniqueCluster));
      }
    }
    return purgedResult;
  }

  protected Set<ColumnCombinationBitset> getConditionStartPoints() {
    return this.singleConditions.keySet();
  }

  protected void combineClusterIntoResult(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    LongArrayList touchedCluster = new LongArrayList();
    Long2LongOpenHashMap partialUniqueHash = this.algorithm.getPLI(partialUnique).asHashMap();
    for (ColumnCombinationBitset minimalConditionStartPoint : this.getConditionStartPoints()) {

//      //check if current condition will result in minimal conditions
//      boolean minimal = false;
//      for (ConditionEntry entry : this.singleConditions.get(minimalConditionStartPoint)) {
//        if (entry.condition.size() == 1) {
//          minimal = true;
//          break;
//        }
//      }
//      if (!minimal) {
//        for (ConditionEntry entry : this.singleConditions.get(minimalConditionStartPoint)) {
//          checkNextCondition:
//          for (ColumnCombinationBitset associatedCondition : entry.condition.minus(minimalConditionStartPoint)
//              .getContainedOneColumnCombinations()) {
//            for (ConditionEntry associatedEntry : this.singleConditions.get(associatedCondition)) {
//              if (associatedEntry.condition.size() == 1) {
//                continue checkNextCondition;
//              }
//            }
//            throw new AlgorithmExecutionException(
//                "Some valid conditions are skipped due to wrong minimality pruning");
//          }
//        }
//        continue;
//      }

      List<ConditionEntry> satisfiedCluster = new ArrayList<>();
      Long2ObjectOpenHashMap<LongArrayList> intersectingCluster = new Long2ObjectOpenHashMap<>();
      int clusterNumber = 0;
      //build intersecting cluster
      for (ConditionEntry singleCluster : this.singleConditions.get(minimalConditionStartPoint)) {
        satisfiedCluster.add(singleCluster);
        touchedCluster.clear();
        for (long rowNumber : singleCluster.cluster) {
          if (partialUniqueHash.containsKey(rowNumber)) {
            touchedCluster.add(partialUniqueHash.get(rowNumber));
          }
        }
        for (long partialUniqueClusterNumber : touchedCluster) {
          if (intersectingCluster.containsKey(partialUniqueClusterNumber)) {
//            intersectingCluster.get(partialUniqueClusterNumber).add(singleCluster.cluster.get(0));
            intersectingCluster.get(partialUniqueClusterNumber).add(clusterNumber);
          } else {
            LongArrayList newConditionClusterNumbers = new LongArrayList();
//            newConditionClusterNumbers.add(singleCluster.cluster.get(0));
            newConditionClusterNumbers.add(clusterNumber);
            intersectingCluster.put(partialUniqueClusterNumber, newConditionClusterNumbers);
          }
        }
        clusterNumber++;
      }
      intersectingCluster = purgeIntersectingClusterEntries(intersectingCluster);

      List<List<ConditionEntry>>
          clustergroups =
          this.combineClusters(this.algorithm.frequency, satisfiedCluster,
                               intersectingCluster);

      for (List<ConditionEntry> singleCondition : clustergroups) {
        ResultSingleton.getInstance().addConditionToResult(partialUnique, singleCondition);
      }
    }
  }

  protected List<List<ConditionEntry>> combineClusters(int frequency,
                                                       List<ConditionEntry> satisfiedClusters,
                                                       Long2ObjectOpenHashMap<LongArrayList> intersectingClusters) {
    List<List<ConditionEntry>> result = new LinkedList<>();
    LinkedList<ConditionTask> queue = new LinkedList();
    LongArrayList satisfiedClusterNumbers = new LongArrayList();
    long totalSize = 0;
    int i = 0;
    for (ConditionEntry clusters : satisfiedClusters) {
      //satisfiedClusterNumbers.add(conditionMap.get(clusters.get(0)));
      satisfiedClusterNumbers.add(i);
      i++;
      totalSize = totalSize + clusters.cluster.size();
    }
    if (totalSize < frequency) {
      return result;
    }

    LongArrayList
        uniqueClusterNumbers =
        new LongArrayList(intersectingClusters.keySet().toLongArray());
    ConditionTask
        firstTask =
        new ConditionTask(0, satisfiedClusterNumbers, new LongArrayList(), totalSize);
    queue.add(firstTask);

    while (!queue.isEmpty()) {
      ConditionTask currentTask = queue.remove();
      //finished cluster iterate -> return result
      if (currentTask.uniqueClusterNumber >= uniqueClusterNumbers.size()) {
        List<ConditionEntry> validCondition = new LinkedList<>();
        for (long conditionClusterNumber : currentTask.conditionClusters) {
          validCondition.add(satisfiedClusters.get((int) conditionClusterNumber));
        }
        result.add(validCondition);
        continue;
      }
      //remove at least one cluster for the current intersecting (unique cluster number) cluster
      for (long conditionCluster : currentTask.conditionClusters) {
        long intersectingClusterNumber = uniqueClusterNumbers.get(currentTask.uniqueClusterNumber);
        if (intersectingClusters.get(intersectingClusterNumber).contains(conditionCluster)) {
          ConditionTask newTask = currentTask.generateNextTask();
          boolean fullfillsFrequency = true;
          for (long clusterItem : intersectingClusters.get(intersectingClusterNumber)) {
            if (clusterItem == conditionCluster) {
              continue;
            }
            if (!newTask
                .remove(clusterItem, satisfiedClusters.get((int) clusterItem).cluster.size(),
                        frequency)) {
              fullfillsFrequency = false;
              break;
            }
          }
          if (fullfillsFrequency) {
            queue.add(newTask);
          }

//          ConditionTask newTask = currentTask.generateNextTask();
//          if (newTask.remove(conditionCluster,
//                             satisfiedClusters.get((int) conditionCluster).cluster.size(),
//                             frequency)) {
//            queue.add(newTask);
//          }
        }
      }
      //no cluster is removed... because all relevant cluster where removed before -> generate the same task
      for (long removedConditionCluster : currentTask.removedConditionClusters) {
        if (intersectingClusters.get((uniqueClusterNumbers.get(currentTask.uniqueClusterNumber)))
            .contains(removedConditionCluster)) {
          ConditionTask newTask = currentTask.generateNextTask();
          queue.add(newTask);
          break;
        }
      }
    }
    return result;
  }

  protected class ConditionTask {

    protected int uniqueClusterNumber;
    protected LongArrayList conditionClusters;
    protected LongArrayList removedConditionClusters;
    long size = -1;

    public ConditionTask(int uniqueCluster, LongArrayList conditionClusters,
                         LongArrayList removedClusters, long size) {
      this.uniqueClusterNumber = uniqueCluster;
      this.conditionClusters = conditionClusters.clone();
      this.removedConditionClusters = removedClusters.clone();
      this.size = size;
    }

    public ConditionTask generateNextTask() {
      ConditionTask
          newTask =
          new ConditionTask(this.uniqueClusterNumber + 1, this.conditionClusters,
                            this.removedConditionClusters, this.size);
      return newTask;
    }

    public boolean remove(long conditionCluster, int size, int frequency) {
      if (this.size - size >= frequency) {
        this.size = this.size - size;
        this.conditionClusters.remove(conditionCluster);
        this.removedConditionClusters.add(conditionCluster);
        return true;
      } else {
        return false;
      }
    }
  }
}
