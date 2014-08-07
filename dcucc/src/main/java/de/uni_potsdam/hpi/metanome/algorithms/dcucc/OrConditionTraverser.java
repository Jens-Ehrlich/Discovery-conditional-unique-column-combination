package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Hildebrandt
 */
public class OrConditionTraverser extends AndConditionTraverser {

  public OrConditionTraverser(Dcucc algorithm) {
    super(algorithm);
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
    //Intentionally add nothing to next level, only one column conditions should be found
    for (LongArrayList condition : conditions) {
      this.algorithm.addConditionToResult(partialUnique, conditionColumn, condition);
    }
  }

  @Override
  public List<LongArrayList> calculateConditions(PositionListIndex partialUnique,
                                                 PositionListIndex PLICondition,
                                                 int frequency,
                                                 List<LongArrayList> unsatisfiedClusters) {
    Long2LongOpenHashMap uniqueMap = partialUnique.asHashMap();
    Long2LongOpenHashMap conditionMap = PLICondition.asHashMap();
    List<LongArrayList> satisfiedClusters = new ArrayList<>();
    Long2ObjectOpenHashMap<LongArrayList>
        intersectingClusters =
        this.purgePossibleConditions(uniqueMap, conditionMap, PLICondition, satisfiedClusters,
                                     unsatisfiedClusters);

    return combineClusters(frequency, satisfiedClusters, intersectingClusters);
  }

  protected List<LongArrayList> combineClusters(int frequency,
                                                List<LongArrayList> satisfiedClusters,
                                                Long2ObjectOpenHashMap<LongArrayList> intersectingClusters) {
    List<LongArrayList> result = new LinkedList<>();
    LinkedList<ConditionTask> queue = new LinkedList();
    LongArrayList satisfiedClusterNumbers = new LongArrayList();
    long totalSize = 0;
    int i = 0;
    for (LongArrayList clusters : satisfiedClusters) {
      //satisfiedClusterNumbers.add(conditionMap.get(clusters.get(0)));
      satisfiedClusterNumbers.add(i);
      i++;
      totalSize = totalSize + clusters.size();
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

      if (currentTask.uniqueClusterNumber >= uniqueClusterNumbers.size()) {
        LongArrayList validCondition = new LongArrayList();
        for (long conditionClusterNumbers : currentTask.conditionClusters) {
          validCondition.addAll(satisfiedClusters.get((int) conditionClusterNumbers));
        }
        result.add(validCondition);
        continue;
      }
      for (long conditionCluster : currentTask.conditionClusters) {
        if (intersectingClusters.get(uniqueClusterNumbers.get(currentTask.uniqueClusterNumber))
            .contains(conditionCluster)) {
          ConditionTask newTask = currentTask.generateNextTask();
          if (newTask.remove(conditionCluster, satisfiedClusters.get((int) conditionCluster).size(),
                             frequency)) {
            queue.add(newTask);
          }
        }
      }
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

  protected Long2ObjectOpenHashMap<LongArrayList> purgePossibleConditions(
      Long2LongOpenHashMap uniqueMap,
      Long2LongOpenHashMap conditionMap,
      PositionListIndex PLICondition,
      List<LongArrayList> satisfiedClusters,
      List<LongArrayList> unsatisfiedClusters) {
    Long2ObjectOpenHashMap<LongArrayList> result = new Long2ObjectOpenHashMap<>();
    LongArrayList touchedClusters = new LongArrayList();
    nextCluster:
    for (LongArrayList cluster : PLICondition.getClusters()) {
      touchedClusters.clear();
      for (long rowNumber : cluster) {
        if (uniqueMap.containsKey(rowNumber)) {
          if (touchedClusters.contains(uniqueMap.get(rowNumber))) {
            unsatisfiedClusters.add(cluster);
            continue nextCluster;
          } else {
            touchedClusters.add(uniqueMap.get(rowNumber));
          }
        }
      }
      satisfiedClusters.add(cluster);
      long conditionClusterNumber = conditionMap.get(cluster.get(0));
      for (long touchedCluster : touchedClusters) {
        if (result.containsKey(touchedCluster)) {
          result.get(touchedCluster).add(conditionClusterNumber);
        } else {
          LongArrayList newLongArray = new LongArrayList();
          newLongArray.add(conditionClusterNumber);
          result.put(touchedCluster, newLongArray);
        }
      }
    }
    return purgeIntersectingClusterEntries(result);
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
