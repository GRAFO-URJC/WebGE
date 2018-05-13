package com.engine.algorithm;

import java.util.HashMap;
import java.util.HashSet;

import jeco.core.algorithm.moga.NSGAII;
import jeco.core.operator.crossover.CrossoverOperator;
import jeco.core.operator.mutation.MutationOperator;
import jeco.core.operator.selection.SelectionOperator;
import jeco.core.problem.Problem;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;

/**
 * NSGAII algorithm modified to provide the observer the current population.
 *
 * @author J. M. Colmenar
 */
public class ModifiedNSGAII extends NSGAII<Variable<Integer>> {

    public ModifiedNSGAII(Problem<Variable<Integer>> problem, int maxPopulationSize, int maxGenerations, MutationOperator<Variable<Integer>> mutationOperator, CrossoverOperator<Variable<Integer>> crossoverOperator, SelectionOperator<Variable<Integer>> selectionOperator) {
        super(problem, maxPopulationSize, maxGenerations, mutationOperator, crossoverOperator, selectionOperator);
    }

    @Override
    public Solutions<Variable<Integer>> execute() {
        int nextPercentageReport = 10;
        HashMap<String, Object> obsData = new HashMap<>();
        // For observers:
        obsData.put("MaxGenerations", String.valueOf(maxGenerations));
        stop = false;
        while ((currentGeneration < maxGenerations) && !stop) {
            step();

            // Report Hv each 10 generations to observer:
            if ((this.countObservers() > 0) && ((currentGeneration % 10) == 0)) {
                double[][] objs = generateObjectivesMatrix(population);
                obsData.put("CurrentGeneration", String.valueOf(currentGeneration));
                obsData.put("Objectives", objs);
                this.setChanged();
                this.notifyObservers(obsData);
            }

            int percentage = Math.round((currentGeneration * 100) / maxGenerations);
            if (percentage == nextPercentageReport) {
                logger.info(percentage + "% performed ... ");
                nextPercentageReport += 10;
            }

        }

        // Return a reduced number of solutions considering the crowding distance:
        logger.info("Removing repetitions and returning the best 10 non-dominated solutions according to the crowding distance ...");

        Solutions<Variable<Integer>> sols = removeRepetitions(this.getCurrentSolution());
        Solutions<Variable<Integer>> finalSols = this.reduce(sols, 10);
        
        // Report final front to plot:
        if (this.countObservers() > 0) {
            double[][] objs = generateObjectivesMatrix(finalSols);
            obsData.put("CurrentGeneration", String.valueOf(currentGeneration));
            obsData.put("Objectives", objs);
            this.setChanged();
            this.notifyObservers(obsData);
        }
        
        return finalSols;
    }

    protected Solutions<Variable<Integer>> removeRepetitions(Solutions<Variable<Integer>> sols) {
        Solutions<Variable<Integer>> filteredSols = new Solutions<>();
        HashSet<String> hash = new HashSet<>(sols.size());

        for (Solution<Variable<Integer>> s : sols) {
            String phen = ((SymbolicRegressionGE) problem).generatePhenotype(s).toString();
            if (!hash.contains(phen)) {
                filteredSols.add(s);
                hash.add(phen);
            }
        }

        return filteredSols;
    }

    
    protected double[][] generateObjectivesMatrix(Solutions<Variable<Integer>> sols) {
        double[][] objs = new double[sols.size()][problem.getNumberOfObjectives()];
        for (int i = 0; i < sols.size(); i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                objs[i][j] = sols.get(i).getObjective(j);
            }
        }
        return objs;
    }

}
