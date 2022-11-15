/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.rabbitmq.MQConfig;
import com.gramevapp.web.service.rabbitmq.ReportRabbitmqMessage;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.de.DifferentialEvolution;
import jeco.core.algorithm.ga.SimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithmBestWithPopRenovation;
import jeco.core.algorithm.ge.SimpleGrammaticalEvolution;
import jeco.core.algorithm.moga.NSGAII;
import jeco.core.algorithm.moge.AbstractProblemGE;
import jeco.core.algorithm.moge.MultiObjectiveGrammaticalEvolution;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.algorithm.sge.VariableList;
import jeco.core.operator.comparator.SimpleDominance;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.operator.mutation.IntegerFlipMutation;
import jeco.core.operator.selection.TournamentSelect;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Carlos García Moreno, J. M. Colmenar
 */
public class SymbolicRegressionGE extends AbstractProblemGE {
    private static final Logger logger = Logger.getLogger(SymbolicRegressionGE.class.getName());

    public static String name = "Gramatical Evolution";
    private Algorithm<Variable<Integer>> algorithm;

    private static int logPopulation = 0;
    private static int gen;
    private CommonBehaviour<Variable<Integer>> common;

    private boolean de;
    private int numObjectives;

    protected Properties properties;

    public SymbolicRegressionGE(Properties properties,int numObjectives,String objective, boolean de,RabbitTemplate rabbitTemplate){
        super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives,
                Integer.valueOf(properties.getProperty(com.engine.util.Common.CHROMOSOME_LENGTH_PROP)),
                Integer.valueOf(properties.getProperty(com.engine.util.Common.MAX_WRAPS_PROP)),
                AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);

        if (properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION) != null) {
            this.setSensibleInitialization(true,Double.valueOf(properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION)));
        }

        this.de = de;
        this.numObjectives = numObjectives;
        this.properties = properties;

        common = new CommonBehaviour<Variable<Integer>>(properties,numObjectives, objective, rabbitTemplate);


    }

    public CommonBehaviour<Variable<Integer>> getCommon() {
        return this.common;
    }

    @Override
    public SymbolicRegressionGE clone() {
        /*SymbolicRegressionGE clone = null;
        try {
            clone = new SymbolicRegressionGE(common.properties, common.threadId + 1, numberOfObjectives);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        return clone;*/

        return null;
    }

    public void runGE(RunGeObserver obs, String experimentDatatypeInfo, Run run, RunService runService, SaveDBService saveDBService) {
        logger.setLevel(Level.ALL);

        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.valueOf(properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP));
        }
        double mutationProb = 1.0 / this.reader.getRules().size();
        if (properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.valueOf(properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP));
        }

        int tournamentSize = 2;
        if (properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP) != null) {
            tournamentSize = Integer.valueOf(properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP));
        }

        // Set weight for CEG penalty
        UtilStats.setCEGPenalties(properties);

        // Second create the algorithm
        IntegerFlipMutation<Variable<Integer>> mutationOperator = new IntegerFlipMutation<>(this, mutationProb);
        SinglePointCrossover<Variable<Integer>> crossoverOperator = new SinglePointCrossover<>(this, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);
        SimpleDominance<Variable<Integer>> comparator = new SimpleDominance<>();
        TournamentSelect<Variable<Integer>> selectionOp = new TournamentSelect<>(tournamentSize,comparator);

        if (numObjectives == 2) {
            algorithm =  new NSGAII<Variable<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
        } else {
            //algorithm = new SimpleGeneticAlgorithm<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
            if(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE)== "SSGA") {
                algorithm = new StaticSimpleGeneticAlgorithm<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
            }else if(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE)== "SGA") {
                algorithm = new SimpleGeneticAlgorithm<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
            }else if(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE)== "BGAP") {
                algorithm = new StaticSimpleGeneticAlgorithmBestWithPopRenovation<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp, 0.1);
            }

        }

        if (obs != null) {
            algorithm.addObserver(obs);
        }

        common.runGE(obs,experimentDatatypeInfo, run,runService,saveDBService, algorithm, this, numObjectives);


    }


    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        if(de) {
            common.evaluateWithDe(solution, phenotype);
        } else {
            common.evaluateWithoutDe(solution, phenotype);
        }

    }


}