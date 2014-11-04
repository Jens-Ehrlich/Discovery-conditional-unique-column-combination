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
          "Runtime: " + time + "\r\n\r\n",
          result.toString(), false);
      FileUtils.writeToFile(format(resultReceiver.getNewResults()),
                            result.toString(), true);

    } catch (AlgorithmExecutionException e) {
      System.out.println("Algorithm failed on file " + file.getFileName());
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Could not write to file " + file.getFileName());
      e.printStackTrace();
    }
  }


  private static String format(List<Result> results) {
    StringBuilder builder = new StringBuilder();
    for (Result result : results) {
      ConditionalUniqueColumnCombination
          conditionalUniqueColumnCombination =
          (ConditionalUniqueColumnCombination) result;
      builder.append(conditionalUniqueColumnCombination.toString());
      builder.append(System.lineSeparator());
    }
    return builder.toString();
  }
}
