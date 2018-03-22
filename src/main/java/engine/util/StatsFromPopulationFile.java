package engine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;

/**
 * Obtains several statistics from a population file log.
 * 
 * @author J. M. Colmenar
 */
public class StatsFromPopulationFile {
    
    static double[] target;
    
    
    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.out.println("Not enough parameters.\n\n\tUsage: java -jar StatsFromPopulationFile.jar <pathToPopulationFile> <NumberOfIndividuals>\n\n");
            //System.exit(0);
            args = new String[2];
            args[0] = "/Users/chema/Desktop/Log_Population_2017-11-30_09-32-14.csv";
            args[1] = "200";
        }
        
        String ficheroEvolucion = args[0];
        int indivs = Integer.valueOf(args[1]);
        
        
        BufferedReader reader = null;
        try {
            // Apertura de fichero:
            reader = new BufferedReader(new FileReader(new File(ficheroEvolucion)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StatsFromPopulationFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Comprueba datos de log:
        String line = "";
        try {
            line = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(StatsFromPopulationFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!line.trim().startsWith("Reported stats;;Genotype;Used genes;Fitness;Phenotype;")) {
            System.out.println("\nERROR: not enough log values. Required stats: Genotype, Used genes, Fitness, Phenotype.\n");
            System.exit(0);
        }
        
        int generacion = 0;
        int indiv = 0;
        int[][] genotipos = new int[indivs][];
        double[] usedGenes = new double[indivs];
        // Fitness es una lista porque no sabemos el número de no válidos.
        ArrayList<Double> fitness = new ArrayList<>(indivs);
        int noFactibles = 0;
        boolean inicio = true;
        System.out.println("Generation;Avg. Variability;Std. Dev. Variability;Avg. Used Genes;Std. Dev. Used Genes;Mode Genes;Best fitness;Avg. fitness (among valids);# Infactible");
        do {
            try {
                line = reader.readLine();

                if (line != null) {
                    // Formato: genotipo ;; genes usados ;; fitness ;; fenotipo ;; evaluacion
                    String[] parts = line.trim().split(";;");
                    if (parts.length == 1) {
                        // Inicio de generacion
                        if (line.trim().startsWith("Generation")) {
                            if (!inicio) {
                                imprimeStats(generacion, genotipos, fitness, noFactibles,usedGenes);
                                // Resetea valores:
                                genotipos = new int[indivs][];
                                fitness = new ArrayList<>(indivs);
                                usedGenes = new double[indivs];
                                indiv = 0;
                                noFactibles = 0;
                                generacion++;
                            } else {
                                inicio = false;
                            }
                        }
                    } else {
                        // Cromosoma
                        String[] genes = parts[0].split(";");
                        int[] cromosoma = new int[genes.length];
                        for (int i = 0; i < genes.length; i++) {
                            cromosoma[i] = Integer.valueOf(genes[i]);
                        }
                        genotipos[indiv] = cromosoma;
                        // Genes usados
                        usedGenes[indiv] = Double.valueOf(parts[1].trim());
                        // Fitness
                        if ((parts[2].trim().equals("Infinity")) ||
                                (Double.valueOf(parts[2].trim()) == Double.MAX_VALUE) ||
                                (parts[3].trim().equals("Double.POSITIVE_INFINITY"))) {
                            noFactibles++;
                        } else {
                            fitness.add(Double.valueOf(parts[2]));
                        }
                        indiv++;
                    }
                } else {
                    if (genotipos[0] != null) {
                        // Ultima generacion del fichero:
                        imprimeStats(generacion, genotipos, fitness, noFactibles,usedGenes);
                    }
                }
                
            } catch (IOException ex) {
                Logger.getLogger(StatsFromPopulationFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (line != null);
        
        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(StatsFromPopulationFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Imprime, para cada generación, los datos que necesitamos.
     * Generacion;Variabilidad Media;Variabilidad Desv. Est;Best fitness;Avg. fitness (válidos);# No factibles
     * 
     * @param generacion
     * @param genotipos 
     */
    private static void imprimeStats(int generacion, int[][] genotipos, ArrayList<Double> fitness, int noFactibles, double[] usedGenes) {
        StringBuilder str = new StringBuilder();
        str.append(generacion).append(";");
        // Variabilidad genética:
        double[] data = new double[genotipos[0].length];
        for (int j=0; j<genotipos[0].length; j++) {
            HashSet<Integer> vals = new HashSet<>(genotipos.length);
            for (int i = 0; i < genotipos.length; i++) {
                vals.add(genotipos[i][j]);
            }
            data[j] = vals.size();
        }
        str.append(StatUtils.mean(data)).append(";").append(Math.sqrt(StatUtils.variance(data)));
        // Genes usados:
        str.append(";").append(StatUtils.mean(usedGenes)).append(";").append(Math.sqrt(StatUtils.variance(usedGenes)));
        // Incluye la moda (tantos elementos como haya si empatan en numero de apariciones
        double[] mode = StatUtils.mode(usedGenes);
        for (int i=0; i<mode.length; i++) {
            str.append(";").append(mode[i]);
            if (i != (mode.length - 1)) {
                str.append(",");
            }
        }
        // Fitness
        double[] fitnessArr = new double[fitness.size()];
        for (int i=0; i<fitnessArr.length; i++) {
            fitnessArr[i] = fitness.get(i);
        }
        str.append(";").append(StatUtils.min(fitnessArr)).append(";").append(StatUtils.mean(fitnessArr));
        str.append(";").append(noFactibles);
        System.out.println(str.toString());
    }
    
}
