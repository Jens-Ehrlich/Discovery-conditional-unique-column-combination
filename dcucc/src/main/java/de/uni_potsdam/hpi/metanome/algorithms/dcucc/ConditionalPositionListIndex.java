package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ConditionTask;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jens Hildebrandt
 */
public class ConditionalPositionListIndex extends PositionListIndex {

  public ConditionalPositionListIndex(List<LongArrayList> clusters) {

  }

  /**
   * TODO update Calculates the condition for a {@link de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination}.
   * this is the {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex}
   * of the partial unique and PLICondition is the {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex}
   * of the columns that may form the condition.
   *
   * @param PLICondition a {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex}
   *                     that forms the condition
   * @return a list of conditions that hold. Each condition is maximal e.g. there exists no superset
   * for the condition. Only on of the condition holds at a time (xor).
   */
  public static List<LongArrayList> calculateConditionUnique(PositionListIndex partialUnique,
                                                             PositionListIndex PLICondition) {
    ConditionTask firstTask = new ConditionTask(0);
    LinkedList<ConditionTask> queue = new LinkedList<>();
    queue.add(firstTask);
    Long2LongOpenHashMap uniqueHashMap = partialUnique.asHashMap();
    List<LongArrayList> result = new ArrayList<>();

    discardCurrentTask:
    while (!queue.isEmpty()) {
      ConditionTask currentTask = queue.remove();
      if (currentTask.clusterIndex >= PLICondition.getClusters().size()) {
        //task is finished, add to result
        result.add(currentTask.containedRows);

      } else {
        ConditionTask
            nextTask =
            new ConditionTask(currentTask.clusterIndex + 1, currentTask.containedClusters,
                              currentTask.containedRows);
        queue.add(nextTask);

        for (long clusterItem : PLICondition.getClusters().get(currentTask.clusterIndex)) {
          if (!uniqueHashMap.containsKey(clusterItem)) {
            // no cluster exists, way to go
            continue;
          }

          long clusterID = uniqueHashMap.get(clusterItem);

          if (currentTask.containedClusters.contains(clusterID)) {
            continue discardCurrentTask;
          } else {
            currentTask.containedClusters.add(clusterID);
          }
        }
        //still here - added a cluster and spawn an additional task
        currentTask.containedRows.addAll(PLICondition.getClusters().get(currentTask.clusterIndex));
        ConditionTask
            nextTask2 =
            new ConditionTask(currentTask.clusterIndex + 1, currentTask.containedClusters,
                              currentTask.containedRows);
        queue.add(nextTask2);
      }

    }
    //clean result
    Iterator<LongArrayList> iterator = result.iterator();
    while (iterator.hasNext()) {
      LongArrayList currentList = iterator.next();
      boolean isSubset = false;
      for (LongArrayList list : result) {
        if (list.containsAll(currentList) && (!list.equals(currentList))) {
          isSubset = true;
          break;
        }
      }
      if (isSubset) {
        iterator.remove();
      }
    }

    return result;
  }
}
