package com.engine.algorithm;

import static com.engine.util.Common.currentDateTimeAsFormattedString;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import com.gramevapp.web.service.rabbitmq.MQConfig;
import com.gramevapp.web.service.rabbitmq.ReportRabbitmqMessage;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.ga.SimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithm;
import jeco.core.algorithm.ga.StaticSimpleGeneticAlgorithmBestWithPopRenovation;
import jeco.core.algorithm.moga.NSGAII;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.algorithm.sge.*;
import jeco.core.algorithm.sge.VariableList;
import jeco.core.operator.comparator.SimpleDominance;
import jeco.core.operator.crossover.CrossoverOperator;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.operator.crossover.SubTreeCrossover;
import jeco.core.operator.crossover.UniformCrossover;
import jeco.core.operator.mutation.BasicMutationVariableList;
import jeco.core.operator.mutation.BasicMutationVariableListAll;
import jeco.core.operator.mutation.IntegerFlipMutationList;
import jeco.core.operator.mutation.MutationOperator;
import jeco.core.operator.selection.SelectionOperator;
import jeco.core.operator.selection.TournamentSelect;
import jeco.core.problem.Problem;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


public class DynamicSGE extends AbstractProblemDSGE {



private static final Logger logger = Logger.getLogger(DynamicSGE.class.getName());
	 
	
	public static String name = "Dynamic Structured GE";
	
	private Algorithm<VariableList<Integer>> algorithm;


    private CommonBehaviour<VariableList<Integer>> common;
    private static int logPopulation = 0;
    private static int gen;

	public static ArrayList<String> executionReport = new ArrayList<>();

	private boolean de;
	private int numObjectives;

	protected Properties properties;
	
	public DynamicSGE(String pathToBnf, int numberOfObjectives, int maxDepth, boolean bloatingControl, boolean TreeDepth, boolean de) {
		super(pathToBnf, numberOfObjectives, maxDepth, bloatingControl, TreeDepth);
		// TODO Auto-generated constructor stub
	}
	
	   public DynamicSGE(Properties properties, int numObjectives,String objective, boolean de,RabbitTemplate rabbitTemplate) {
		   
		   super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives, Integer.valueOf(properties.getProperty(com.engine.util.Common.MAX_TREE_DEPTH)), true, true);
		   System.out.println("Tree Depth: " +Integer.valueOf(properties.getProperty(com.engine.util.Common.MAX_TREE_DEPTH)));

		   this.de = de;
		   this.numObjectives = numObjectives;
		   this.properties = properties;
		   common = new CommonBehaviour<VariableList<Integer>>(properties,numObjectives, objective, rabbitTemplate );

	}
	   
	   
	public CommonBehaviour<VariableList<Integer>> getCommon() {
		return this.common;
	}

	
    /**
     * Called from the MainWindow and Launcher, it creates a new DynamicSGE problem with a set of properties and runs i
     */
    public void runGE(RunGeObserver obs, String experimentDatatypeInfo, Run run, RunService runService, SaveDBService saveDBService) {
        logger.setLevel(Level.ALL);


		boolean treeDepth = false;
		if( properties.getProperty(com.engine.util.Common.DYNAMIC_TYPE_DEPTH) == null || Integer.valueOf(properties.getProperty(com.engine.util.Common.DYNAMIC_TYPE_DEPTH)) == 0) {
			treeDepth = true;

		}else if(Integer.valueOf(properties.getProperty(com.engine.util.Common.DYNAMIC_TYPE_DEPTH)) == 1) {
			treeDepth = false;

		}


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
        MutationOperator<VariableList<Integer>> mutationOperator = null;

        CrossoverOperator<VariableList<Integer>> crossoverOperator = null;

        //Crossover type
        if(properties.get(com.engine.util.Common.CROSSOVER_TYPE_D).equals("Uniform")) {
        	System.out.println("Uniform: " + Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_D)));
        	crossoverOperator = new UniformCrossover<VariableList<Integer>>(crossOverProb, Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_D)));

        }else if(properties.get(com.engine.util.Common.CROSSOVER_TYPE_D).equals("Tree")) {
			System.out.println("Tree");
        	crossoverOperator = new SubTreeCrossover<VariableList<Integer>>(this,crossOverProb, Double.valueOf(properties.getProperty(com.engine.util.Common.CHANGE_PROB_D)));

        }
        else{
        	crossoverOperator = new SinglePointCrossover<VariableList<Integer>>(this, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);

        }

        //Mutation type
        if(properties.get(com.engine.util.Common.MUTATION_TYPE_D).equals("All")) {
			System.out.println("All");
        	mutationOperator= new BasicMutationVariableListAll<VariableList<Integer>>(mutationProb, this);

        }else {
			System.out.println("One");
        	mutationOperator= new BasicMutationVariableList<VariableList<Integer>>(mutationProb, this);

        }

        SimpleDominance<VariableList<Integer>> comparator = new SimpleDominance<>();
        SelectionOperator<VariableList<Integer>> selectionOp = new TournamentSelect<>(tournamentSize,comparator);

        //Algorithm used
        if (numObjectives == 2) {
        	 algorithm =  new NSGAII<VariableList<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
        } else {
			algorithm = new SimpleGeneticAlgorithm<VariableList<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
			/*if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 1) {
        		algorithm = new StaticSimpleGeneticAlgorithm<VariableList<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);


        	}else if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 2) {
        		algorithm = new SimpleGeneticAlgorithm<VariableList<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
        	}else if(Integer.parseInt(properties.getProperty(com.engine.util.Common.ALGORITHM_TYPE))== 3) {
        		algorithm = new StaticSimpleGeneticAlgorithmBestWithPopRenovation<VariableList<Integer>>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp, 0.1);

        	}*/

        }

      //Run the algorithm through the CommonBehaviour
        common.runGE(obs,experimentDatatypeInfo, run,runService,saveDBService, algorithm, this, numObjectives);
        
    }


	@Override
	public void evaluate(Solution<VariableList<Integer>> solution, Phenotype phenotype) {
		if(de) {
			common.evaluateWithDe(solution, phenotype);
		} else {
			common.evaluateWithoutDe(solution, phenotype);
		}
		
	}

	@Override
	public void evaluate(Solution<VariableList<Integer>> solution) {
		 logger.severe("The solutions should be already evaluated. You should not see this message.");

	}

	@Override
	public Problem<VariableList<Integer>> clone() {
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
