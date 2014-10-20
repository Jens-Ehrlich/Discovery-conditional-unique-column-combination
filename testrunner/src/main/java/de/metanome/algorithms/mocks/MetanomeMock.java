package de.metanome.algorithms.mocks;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.dcucc.Dcucc;
import de.metanome.backend.input.csv.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultsCache;
import de.uni_potsdam.hpi.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class MetanomeMock {

  public static void executeDCUCC(Path file, Path result) {
    Dcucc dcucc;
    int frequency = 80;
    FileInputGenerator fileInputGenerators = null;
    try {
      fileInputGenerators = new DefaultFileInputGenerator(file.toFile());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    ResultsCache resultReceiver = new ResultsCache();

    try {
      dcucc = new Dcucc();
      dcucc.setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG,
                                           fileInputGenerators);
      dcucc.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);
      dcucc.setBooleanConfigurationValue(Dcucc.SELFCONDITIONS_TAG, false);
      dcucc.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, frequency);
      dcucc.setListBoxConfigurationValue(Dcucc.ALGORITHM_TAG, "SingleValueMultipleCondition");
      dcucc.setResultReceiver(resultReceiver);


      long time = System.currentTimeMillis();
      dcucc.execute();
      time = System.currentTimeMillis() - time;


      FileUtils.writeToFile(
          dcucc.toString() + "\r\n\r\n" + "Runtime: " + time + "\r\n\r\n",
          result.toString());
      FileUtils.writeToFile(format(resultReceiver.getNewResults()),
                            result.toString());

    } catch (AlgorithmExecutionException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static String format(List<Result> results) {
    HashMap<String, List<String>> ref2Deps = new HashMap<String, List<String>>();

    StringBuilder builder = new StringBuilder();
    for (Result result : results) {
      ConditionalUniqueColumnCombination
          conditionalUniqueColumnCombination =
          (ConditionalUniqueColumnCombination) result;

//      StringBuilder refBuilder = new StringBuilder("(");
//      Iterator<ColumnIdentifier>
//          refIterator =
//          ind.getReferenced().getColumnIdentifiers().iterator();
//      while (refIterator.hasNext()) {
//        refBuilder.append(refIterator.next().toString());
//        if (refIterator.hasNext()) {
//          refBuilder.append(",");
//        } else {
//          refBuilder.append(")");
//        }
//      }
//      String ref = refBuilder.toString();
//
//      StringBuilder depBuilder = new StringBuilder("(");
//      Iterator<ColumnIdentifier> depIterator = ind.getDependant().getColumnIdentifiers().iterator();
//      while (depIterator.hasNext()) {
//        depBuilder.append(depIterator.next().toString());
//        if (depIterator.hasNext()) {
//          depBuilder.append(",");
//        } else {
//          depBuilder.append(")");
//        }
//      }
//      String dep = depBuilder.toString();
//
//      if (!ref2Deps.containsKey(ref)) {
//        ref2Deps.put(ref, new ArrayList<String>());
//      }
//      ref2Deps.get(ref).add(dep);
//    }
//
//    StringBuilder builder = new StringBuilder();
//    ArrayList<String> referenced = new ArrayList<String>(ref2Deps.keySet());
//    Collections.sort(referenced);
//    for (String ref : referenced) {
//      List<String> dependants = ref2Deps.get(ref);
//      Collections.sort(dependants);
//
//      if (!dependants.isEmpty()) {
//        builder.append(ref + " > ");
//      }
//      for (String dependant : dependants) {
//        builder.append(dependant + "  ");
//      }
//      if (!dependants.isEmpty()) {
//        builder.append("\r\n");
//      }
//    }
    }
    return builder.toString();
  }
}
