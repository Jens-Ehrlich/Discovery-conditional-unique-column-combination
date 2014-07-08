package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import it.unimi.dsi.fastutil.longs.LongArrayList;

/**
 * @author Jens Hildebrandt
 */
public class ConditionTask {

  public int clusterIndex;
  public LongArrayList containedClusters;
  public LongArrayList containedRows;

  public ConditionTask(int clusterIndex) {
    this.clusterIndex = clusterIndex;
    this.containedClusters = new LongArrayList();
    this.containedRows = new LongArrayList();
  }


  public ConditionTask(int clusterIndex, LongArrayList containedClusters,
                       LongArrayList containedRows) {
    this.clusterIndex = clusterIndex;
    this.containedClusters = new LongArrayList(containedClusters);
    this.containedRows = new LongArrayList(containedRows);
  }
}
