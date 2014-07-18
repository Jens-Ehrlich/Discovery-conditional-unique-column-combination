package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

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
  public static List<LongArrayList> calculateConditions(PositionListIndex partialUnique,
                                                        PositionListIndex PLICondition,
                                                        int frequency) {
    List<LongArrayList> result = new LinkedList<>();
    Long2LongOpenHashMap uniqueHashMap = partialUnique.asHashMap();
    LongArrayList touchedClusters = new LongArrayList();
    nextCluster:
    for (LongArrayList cluster : PLICondition.getClusters()) {
      if (cluster.size() < frequency) {
        continue;
      }
      touchedClusters.clear();
      for (long rowNumber : cluster) {
        if (uniqueHashMap.containsKey(rowNumber)) {
          if (touchedClusters.contains(uniqueHashMap.get(rowNumber))) {
            continue nextCluster;
          } else {
            touchedClusters.add(uniqueHashMap.get(rowNumber));
          }
        }
      }
      result.add(cluster);
    }
    return result;
  }

  public static List<LongArrayList> calculateNotConditions(PositionListIndex partialUnique,
                                                           PositionListIndex PLIcondition,
                                                           int frequency, int numberOfTuples) {
    List<LongArrayList> result = new LinkedList<>();

    outer:
    for (LongArrayList cluster : PLIcondition.getClusters()) {
      for (LongArrayList uniqueCluster : partialUnique.getClusters()) {
        if (!cluster.containsAll(uniqueCluster)) {
          continue outer;
        }
      }
      result.add(cluster);
    }
    return result;
  }
}


