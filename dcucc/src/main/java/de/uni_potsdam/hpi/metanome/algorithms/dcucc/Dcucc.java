package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.*;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.ConditionalUniqueColumnCombinationAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.*;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputGenerationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import java.util.LinkedList;
import java.util.List;

/** Mockup comment
 * @author Jens Hildebrandt
 */
public class Dcucc implements ConditionalUniqueColumnCombinationAlgorithm, RelationalInputParameterAlgorithm, IntegerParameterAlgorithm, BooleanParameterAlgorithm{
    protected static final String INPUT_FILE_TAG = "csvIterator";
    protected static final String FREQUENCY_TAG = "frequency";
    protected static final String PERCENTAGE_TAG = "percentage";

    protected int frequency = 1;
    protected boolean percentage = false;
    protected List<PositionListIndex> basePLI;

    protected RelationalInputGenerator inputGenerator;
    protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

    @Override
    public List<ConfigurationSpecification> getConfigurationRequirements() {
        LinkedList<ConfigurationSpecification> spec = new LinkedList<>();
        ConfigurationSpecificationCsvFile csvFile = new ConfigurationSpecificationCsvFile(INPUT_FILE_TAG);
        spec.add(csvFile);
        ConfigurationSpecificationInteger frequency = new ConfigurationSpecificationInteger(FREQUENCY_TAG);
        spec.add(frequency);
        ConfigurationSpecificationBoolean percentage = new ConfigurationSpecificationBoolean(PERCENTAGE_TAG);
        spec.add(percentage);
        return spec;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        RelationalInput input = calculateInput();

        List<ColumnCondition> list = new LinkedList<ColumnCondition>();
        list.add(new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames().get(1)), "a", "b"));
        resultReceiver.receiveResult(new ConditionalUniqueColumnCombination(new ColumnCombination(new ColumnIdentifier(input.relationName(), input.columnNames().get(0))), list.toArray(new ColumnCondition[list.size()])));
        //DuccAlgorithm duccAlgorithm = new DuccAlgorithm(input.relationName(), input.columnNames(), this.resultReceiver);
        //duccAlgorithm.run(plis);
    }

    protected RelationalInput calculateInput() throws InputGenerationException, InputIterationException {
        RelationalInput input;
        input = inputGenerator.generateNewCopy();
        PLIBuilder pliBuilder = new PLIBuilder(input);
        basePLI = pliBuilder.getPLIList();
        int numberOfTuples = (int) pliBuilder.getNumberOfTuples();
        if (percentage) {
            frequency = (int) Math.ceil(numberOfTuples*frequency*1.0d/100);
        }
        return input;
    }


    @Override
    public void setResultReceiver(ConditionalUniqueColumnCombinationResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;

    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
        if (identifier.equals(INPUT_FILE_TAG)) {
            inputGenerator = values[0];
        } else {
            throw new AlgorithmConfigurationException("Operation should not be called");
        }
    }

    @Override
    public void setBooleanConfigurationValue(String identifier, boolean... values) throws AlgorithmConfigurationException {
        if (identifier.equals(PERCENTAGE_TAG)) {
            this.percentage = values[0];
        } else {
            throw new AlgorithmConfigurationException("Operation should not be called");
        }
    }

    @Override
    public void setIntegerConfigurationValue(String identifier, int... values) throws AlgorithmConfigurationException {
        if (identifier.equals(FREQUENCY_TAG)) {
            this.frequency = values[0];
        } else {
            throw new AlgorithmConfigurationException("Operation should not be called");
        }
    }
}

