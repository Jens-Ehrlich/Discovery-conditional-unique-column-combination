package de.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class Condition {

  protected ColumnCombinationBitset partialUnique;
  protected Map<ColumnCombinationBitset, SingleCondition> conditions;

  public Condition(ColumnCombinationBitset partialUnique,
                   Map<ColumnCombinationBitset, SingleCondition> conditions) {
    this.partialUnique = partialUnique;
    this.conditions = conditions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Condition condition = (Condition) o;

    if (!conditions.equals(condition.conditions)) {
      return false;
    }
    if (!partialUnique.equals(condition.partialUnique)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = partialUnique.hashCode();
    result = 31 * result + conditions.hashCode();
    return result;
  }
}

