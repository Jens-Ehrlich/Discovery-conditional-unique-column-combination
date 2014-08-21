package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class SingleCondition {

  protected boolean isNegated = false;
  protected LongArrayList cluster;

  public SingleCondition(LongArrayList cluster) {
    this.cluster = cluster;
  }

  public SingleCondition(LongArrayList cluster, boolean isNegated) {
    this(cluster);
    this.isNegated = isNegated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SingleCondition that = (SingleCondition) o;

    if (isNegated != that.isNegated) {
      return false;
    }
    if (cluster != null ? !cluster.equals(that.cluster) : that.cluster != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (isNegated ? 1 : 0);
    result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
    return result;
  }
}
