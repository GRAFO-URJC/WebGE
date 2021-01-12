package com.engine.algorithm;

import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.de.DifferentialEvolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

public class MainDE {

    public static final String NUM_GENERATIONS_PROP = "NumGenerations";
    public static final String MUTATION_FACTOR_DE_PROP = "MutationFactorDE";
    public static final String RECOMB_FACTOR_PROP = "RecombinationFactorDE";
    public static final String MODEL_PROP = "ModelDE";
    public static final String LOWER_BOUND_PROP = "LowerBoundDE";
    public static final String UPPER_BOUND_PROP = "UpperBoundDE";
    public static final String POPULATION_DE = "PopulationSizeDE";

    // Identificator of the parameters in the models.
    private static final char ID_FOR_PARAMS = 'w';

    private static Algorithm algorithm;
    public static ArrayList<String> parameters;
    public static String modelDE;

    public static void setupDE(String model, Properties props, String objective, String[][] func) {

        if (model == null) {
            // Means that DE is run standalone.
            model = props.getProperty(MODEL_PROP);
            modelDE = model;
        }

        // Parameters are stored in a list according to their position.
        // The number of elements to be tuned is the number of variables -> size of the list.
        // In addition, we control that repetition of parameters are not new parameters !
        HashSet<String> paramIds = new HashSet<>();
        parameters = new ArrayList<>();
        int i = 0;
        while (i<model.length()) {
            if (model.charAt(i) == ID_FOR_PARAMS) {
                // Parse element:
                String id = ID_FOR_PARAMS + "";
                i++;
                while ((i<model.length()) && (Character.isDigit(model.charAt(i)))) {
                    id += model.charAt(i);
                    i++;
                }
                if (!paramIds.contains(id)) {
                    parameters.add(id);
                    paramIds.add(id);
                }
            } else {
                i++;
            }
        }

        // Create the problem:
        ProblemDE problem = new ProblemDE(parameters.size(),
                Double.valueOf(props.getProperty(LOWER_BOUND_PROP)),
                        Double.valueOf(props.getProperty(UPPER_BOUND_PROP)), objective, func);
        problem.setModel(model);

        // Create algorithm:
        algorithm = new DifferentialEvolution(problem,
                Integer.valueOf(props.getProperty(POPULATION_DE)),
                Integer.valueOf(props.getProperty(NUM_GENERATIONS_PROP)),
                true,
                Double.valueOf(props.getProperty(MUTATION_FACTOR_DE_PROP)),
                Double.valueOf(props.getProperty(RECOMB_FACTOR_PROP)));
    }

    public static Algorithm getAlgorithm() {
        return algorithm;
    }
}
