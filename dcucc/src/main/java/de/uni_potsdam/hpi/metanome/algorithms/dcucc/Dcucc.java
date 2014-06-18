package de.uni_potsdam.hpi.metanome.algorithms.dcucc;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.uni_potsdam.hpi.metanome.algorithm_integration.*;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.ConditionalUniqueColumnCombinationAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecification;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecificationCsvFile;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInput;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jens Hildebrandt
 */
public class Dcucc implements ConditionalUniqueColumnCombinationAlgorithm, RelationalInputParameterAlgorithm {
    protected static final String CSV_HANDLE = "csvIterator";
    protected RelationalInputGenerator inputGenerator;
    protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

    @Override
    public List<ConfigurationSpecification> getConfigurationRequirements() {
        LinkedList<ConfigurationSpecification> spec = new LinkedList<>();
        ConfigurationSpecificationCsvFile csvFile = new ConfigurationSpecificationCsvFile(CSV_HANDLE);
        spec.add(csvFile);
        return spec;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        RelationalInput input;

        input = inputGenerator.generateNewCopy();

        PLIBuilder pliBuilder = new PLIBuilder(input);
        List<PositionListIndex> plis = pliBuilder.getPLIList();
        List<ColumnCondition> list = new LinkedList<ColumnCondition>();
        list.add(new ColumnCondition(new ColumnIdentifier(input.relationName(), input.columnNames().get(1)), "a", "b"));
        resultReceiver.receiveResult(new ConditionalUniqueColumnCombination(new ColumnCombination(new ColumnIdentifier(input.relationName(), input.columnNames().get(0))), list));
        //DuccAlgorithm duccAlgorithm = new DuccAlgorithm(input.relationName(), input.columnNames(), this.resultReceiver);
        //duccAlgorithm.run(plis);
    }



    @Override
    public void setResultReceiver(ConditionalUniqueColumnCombinationResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;

    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
        if (identifier.equals(CSV_HANDLE)) {
            inputGenerator = values[0];
        } else {
            throw new AlgorithmConfigurationException("Operation should not be called");
        }
    }
}

