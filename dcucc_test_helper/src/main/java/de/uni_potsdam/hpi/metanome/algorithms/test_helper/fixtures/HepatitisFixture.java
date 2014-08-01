package de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;
import de.uni_potsdam.hpi.metanome.input.csv.CsvFileGenerator;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Jens Hildebrandt
 */
public class HepatitisFixture {

  protected ImmutableList<String>
      columnNames =
      ImmutableList.of("column1", "column2", "column3", "column4", "column5", "column6", "column7",
                       "column8", "column9", "column10", "column11", "column12", "column13",
                       "column14", "column15", "column16", "column17", "column18", "column19",
                       "column20");
  protected int numberOfColumns = 20;
  protected int rowPosition;
  protected String relationName = "hepatitis.csv";
  protected List<ImmutableList<String>> table = new LinkedList<>();
  protected FunctionalDependencyResultReceiver
      fdResultReceiver =
      mock(FunctionalDependencyResultReceiver.class);
  protected UniqueColumnCombinationResultReceiver
      uniqueColumnCombinationResultReceiver =
      mock(UniqueColumnCombinationResultReceiver.class);
  protected InclusionDependencyResultReceiver
      inclusionDependencyResultReceiver =
      mock(InclusionDependencyResultReceiver.class);
  protected ConditionalUniqueColumnCombinationResultReceiver
      cuccResultReceiver =
      mock(ConditionalUniqueColumnCombinationResultReceiver.class);

  public HepatitisFixture() throws CouldNotReceiveResultException {

    doAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        System.out.println(args[0]);
        return null;
      }
    }).when(cuccResultReceiver).receiveResult(isA(ConditionalUniqueColumnCombination.class));

  }

  public RelationalInputGenerator getInputGenerator()
      throws InputGenerationException, InputIterationException, UnsupportedEncodingException,
             FileNotFoundException {
    String
        pathToInputFile =
        URLDecoder.decode(
            Thread.currentThread().getContextClassLoader().getResource(relationName).getPath(),
            "utf-8");
    RelationalInputGenerator inputGenerator = new CsvFileGenerator(new File(pathToInputFile));
    return inputGenerator;
  }

  public ConditionalUniqueColumnCombinationResultReceiver getCUCCResultReceiver() {
    return cuccResultReceiver;
  }


  public UniqueColumnCombinationResultReceiver getUCCResultReceiver() {
    return uniqueColumnCombinationResultReceiver;
  }

  public void verifyConditionalUniqueColumnCombination() throws CouldNotReceiveResultException {

  }
}
