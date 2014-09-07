package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class ResultSingleton {
  protected static ResultSingleton singleton;

  public static ResultSingleton getInstance() {
    return singleton;
  }

  public static ResultSingleton createResultSingleton(RelationalInput input, ImmutableList<ColumnCombinationBitset> partialUccs)
      throws InputGenerationException, InputIterationException {
    singleton = new ResultSingleton(input, partialUccs);
    return singleton;
  }


  protected SubSetGraph conditionMinimalityGraph;
  protected List<Map<Long, String>> inputMap;

  protected ResultSingleton(RelationalInput input, ImmutableList<ColumnCombinationBitset> partialUccs)
      throws InputGenerationException, InputIterationException {
    prepareOutput(input);
    this.conditionMinimalityGraph = new SubSetGraph();

    this.conditionMinimalityGraph.addAll(partialUccs);
  }

  protected void prepareOutput(RelationalInput input) throws InputGenerationException, InputIterationException {
    this.inputMap = new ArrayList<>(input.numberOfColumns());
    for (int i = 0; i < input.numberOfColumns(); i++) {
      inputMap.add(new HashMap<Long, String>());
    }
    long row = 0;
    while (input.hasNext()) {
      ImmutableList<String> values = input.next();
      for (int i = 0; i < input.numberOfColumns(); i++) {
        inputMap.get(i).put(row, values.get(i));
      }
      row++;
    }
  }

}
