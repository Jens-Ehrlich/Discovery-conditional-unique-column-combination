package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import it.unimi.dsi.fastutil.longs.LongArrayList;

/**
 * @author Jens Ehrlich
 */
public class ConditionEntry {

  public ColumnCombinationBitset condition;
  public LongArrayList cluster;
  public float coverage;

  public ConditionEntry(ColumnCombinationBitset condition, LongArrayList cluster) {
    this.condition = new ColumnCombinationBitset(condition);
    this.cluster = cluster.clone();
  }

  public ConditionEntry(ColumnCombinationBitset condition, LongArrayList cluster, float coverage) {
    this(condition, cluster);
    this.coverage = coverage;
  }
}
