package com.engine.algorithm;


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.engine.util.Common;
import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.ga.SimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithmBestWithPopRenovation;
import jeco.core.algorithm.moga.NSGAII;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.algorithm.sge.AbstractProblemSSGE;
import jeco.core.algorithm.sge.VariableArray;
import jeco.core.algorithm.sge.VariableList;
import jeco.core.operator.comparator.SimpleDominance;
import jeco.core.operator.crossover.CrossoverOperator;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.operator.crossover.SubTreeCrossover;
import jeco.core.operator.crossover.UniformCrossover;
import jeco.core.operator.mutation.*;
import jeco.core.operator.selection.SelectionOperator;
import jeco.core.operator.selection.TournamentSelect;
import jeco.core.problem.Problem;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class StaticSGE extends AbstractProblemSSGE {

	private static final Logger logger = Logger.getLogger(StaticSGE.class.getName());


	public static String name = "Structured GE";

	private Algorithm<VariableArray<Integer>> algorithm;
	//private static DynamicSGE problem = null;

	private CommonBehaviour<VariableArray<Integer>> common;
	private static int logPopulation = 0;
	private static int gen;

	public static ArrayList<String> executionReport = new ArrayList<>();

	private boolean de;
	private int numObjectives;

	protected Properties properties;

	public StaticSGE(String pathToBnf, int numberOfObjectives, int maxDepth, boolean bloatingControl, boolean TreeDepth, boolean de) {
		super(pathToBnf, numberOfObjectives, maxDepth);
		// TODO Auto-generated constructor stub
	}

	public StaticSGE(Properties properties, int numObjectives,String objective, boolean de,RabbitTemplate rabbitTemplate) {

		super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives, Integer.valueOf(properties.getProperty(com.engine.util.Common.MAX_TREE_DEPTH)));
		System.out.println("Depth: " +Integer.valueOf(properties.getProperty(Common.MAX_RECURSION_GRAMMAR_DEPTH)));

		this.de = de;
		this.numObjectives = numObjectives;
		this.properties = properties;
		common = new CommonBehaviour<VariableArray<Integer>>(properties,numObjectives, objective, rabbitTemplate );

	}


	public CommonBehaviour<VariableArray<Integer>> getCommon() {
		return this.common;
	}


	/**
	 * Called from the MainWindow and Launcher, it creates a new DynamicSGE problem with a set of properties and runs i
	 */
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
		MutationOperator<VariableArray<Integer>> mutationOperator = null;

		CrossoverOperator<VariableArray<Integer>> crossoverOperator = null;

		//Crossover type

		if(properties.get(com.engine.util.Common.CROSSOVER_TYPE_S).equals("Uniform")) {
			System.out.println("Uniform: " + Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_S)));
			crossoverOperator = new UniformCrossover<VariableArray<Integer>>(crossOverProb, Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_S)));

		}else if(properties.get(com.engine.util.Common.CROSSOVER_TYPE_S).equals("Tree")) {
			crossoverOperator = new SubTreeCrossover<VariableArray<Integer>>(this,crossOverProb, Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_S)));

		}
		else{
			crossoverOperator = new SinglePointCrossover<VariableArray<Integer>>(this, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);

		}

		//Mutation type
		if(properties.get(com.engine.util.Common.MUTATION_TYPE_S).equals("All")) {
			System.out.println("All");
			mutationOperator= new IntegerFlipMutationListAll<VariableArray<Integer>>(this, mutationProb);

		}else {
			System.out.println("One");
			mutationOperator= new IntegerFlipMutationList<VariableArray<Integer>>(this, mutationProb);

		}

		SimpleDominance<VariableArray<Integer>> comparator = new SimpleDominance<>();
		SelectionOperator<VariableArray<Integer>> selectionOp = new TournamentSelect<>(tournamentSize,comparator);

		//Algorithm used
		if (numObjectives == 2) {
			algorithm =  new NSGAII<VariableArray<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
		} else {
			algorithm = new SimpleGeneticAlgorithm<VariableArray<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);

			/*if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 1) {
				algorithm = new StaticSimpleGeneticAlgorithm<VariableArray<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);


			}else if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 2) {
				algorithm = new SimpleGeneticAlgorithm<VariableArray<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
			}else if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 3) {
				algorithm = new StaticSimpleGeneticAlgorithmBestWithPopRenovation<VariableArray<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp, 0.1);

			}*/

		}

		//Run the algorithm through the CommonBehaviour
		common.runGE(obs,experimentDatatypeInfo, run,runService,saveDBService, algorithm, this, numObjectives);

	}


	@Override
	public void evaluate(Solution<VariableArray<Integer>> solution, Phenotype phenotype) {
		if(de) {
			common.evaluateWithDe(solution, phenotype);
		} else {
			common.evaluateWithoutDe(solution, phenotype);
		}

	}

	@Override
	public void evaluate(Solution<VariableArray<Integer>> solution) {
		logger.severe("The solutions should be already evaluated. You should not see this message.");

	}

	@Override
	public Problem<VariableArray<Integer>> clone() {
		/*DynamicSGE clone = null;
		boolean treeDepth = false;
		if(Integer.valueOf(common.properties.getProperty(com.engine.util.Common.DYNAMIC_TYPE_DEPTH)) == 0) {
			treeDepth = true;
		}else if(Integer.valueOf(common.properties.getProperty(com.engine.util.Common.DYNAMIC_TYPE_DEPTH)) == 1) {
			treeDepth = false;
		}

        clone = new DynamicSGE(common.properties, common.threadId + 1, super.numberOfObjectives, treeDepth);
        return clone;*/

		return null;
	}

}
