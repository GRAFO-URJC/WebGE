package com.engine.algorithm;

import jeco.core.problem.Problem;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import jeco.core.util.random.RandomGenerator;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;


public class ProblemDE extends Problem<Variable<Double>> {

    String model;
    String objective;
    String[][] func;
    public List<String> parameters;

    public ProblemDE(int numberOfVariables, double lowerBnd, double upperBnd, String objective, String[][] func, String model,
                     List<String> parameters) {
        super(numberOfVariables, 1);
        this.objective = objective;
        // Upper and lower bounds for the weights
        for (int i = 0; i < numberOfVariables; i++) {
            lowerBound[i] = lowerBnd;
            upperBound[i] = upperBnd;
        }
        this.func = func;
        this.model = model;
        this.parameters = parameters;

    }

    @Override
    public Solutions<Variable<Double>> newRandomSetOfSolutions(int size) {
        Solutions<Variable<Double>> solutions = new Solutions<>();
        for (int i=0; i<size; ++i) {
            Solution<Variable<Double>> solI = new Solution<>(numberOfObjectives);
            for (int j = 0; j < numberOfVariables; ++j) {
                Variable<Double> varJ = new Variable<>(RandomGenerator.nextDouble(lowerBound[j], upperBound[j]));
                solI.getVariables().add(varJ);
            }
            solutions.add(solI);
        }
        return solutions;
    }

    @Override
    public void evaluate(Solution<Variable<Double>> solution) {
        // The individual contains the parameters for the model. Hence, the expression of the model
        // has to be created and then evaluated:

        // First, include the identifiers of the input variables.
        ExpressionBuilder eb = ModelEvaluator.includeInputVariables(model, func);

        // Include the labels of the parameters:
        for (String param : parameters) {
            eb.variable(param);
        }
        Expression exp = eb.build();

        // Include the values of the parameters:
        for (int j = 0; j < solution.getVariables().size(); j++) {
            exp.setVariable(parameters.get(j), solution.getVariable(j).getValue());
        }


        // Now evaluate the expression using the input variables:
        double v = ModelEvaluator.evaluateModel(exp, objective, func);


        // To avoid problems sorting NaNs, we perform this slight change.
        if (Double.isNaN(v))
            v = Double.MAX_VALUE;
        solution.getObjectives().set(0,v);

    }

    @Override
    public ProblemDE clone() {
        List<String> clonedParams = new ArrayList<>(parameters);

        return new ProblemDE(numberOfVariables,this.getLowerBound(0),this.getUpperBound(0),
                objective, func.clone(), model, clonedParams);
    }

    public void setModel(String model) {
        this.model = model;
    }

}
