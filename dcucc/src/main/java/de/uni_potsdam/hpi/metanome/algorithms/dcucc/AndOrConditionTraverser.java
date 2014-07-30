package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Hildebrandt
 */
public class AndOrConditionTraverser extends OrConditionTraverser {

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
    }

    for (LongArrayList condition : conditions) {
      this.algorithm.addConditionToResult(partialUnique, conditionColumn, condition);
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