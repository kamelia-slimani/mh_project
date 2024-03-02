package tsp.projects.competitor.bot;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.evaluation.Problem;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot extends CompetitorProject {
    protected Evaluation evaluation;
    protected Problem problem;
    private double mutationRate = 0.1; // Taux de mutation
    private AntColony antColony; // Instance de l'ACO
    private int numAnts = 100; // Nombre de fourmis
    private int numIterations = 10000; // Nombre d'itérations de l'ACO
    private List<Coordinates> coordinates; // Liste des coordonnées des villes

    public Bot(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("Bot");
        setAuthors("Alexandre", "Kamelia");
    }

    @Override
    public void initialization() {
        this.evaluation = super.evaluation;
        this.problem = super.problem;
        this.coordinates = initializeCitiesWithCoordinates(this.problem); // Initialiser les coordonnées des villes
        this.antColony = new AntColony(numAnts, coordinates, 0.5, 1.0, 1.0, 2.0); // Initialiser l'instance de l'ACO
    }

    private List<Coordinates> initializeCitiesWithCoordinates(Problem problem) {
        List<Coordinates> coordinates = new ArrayList<>();
        for (int i = 0; i < problem.getLength(); i++) {
            coordinates.add(problem.getCoordinates(i));
        }
        return coordinates;
    }

    @Override
    public void loop() {
        // Boucle principale de votre algorithme ici
        long startTime = System.currentTimeMillis(); // Temps de départ de la boucle

        // Création de la population initiale à l'aide de l'algorithme génétique
        ArrayList<Path> population = createInitialPopulation(100); // Nombre de chemins dans la population initiale

        // Conversion de la liste de coordonnées en tableau de coordonnées
        Coordinates[] coordinatesArray = coordinates.toArray(new Coordinates[coordinates.size()]);

        // Optimisation de la population avec l'algorithme 2-opt
        for (int i = 0; i < population.size(); i++) {
            Path path = population.get(i);
            Path optimizedPath = TwoOpt.optimize(path, coordinatesArray);
            population.set(i, optimizedPath); // Mettre à jour le chemin dans la population
        }

        // Optimisation de la population avec l'algorithme des colonies de fourmis
        //population = antColony.runAntColonyOptimization(population, numIterations);

        // Sélection de la meilleure solution de la population finale
        Path meilleureSolution = selectBestSolution(population);

        // Mettre à jour la meilleure solution si nécessaire
        evaluation.evaluate(meilleureSolution);
    }



    private ArrayList<Path> createInitialPopulation(int populationSize) {
        ArrayList<Path> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            int[] chemin = generateRandomPath();
            population.add(new Path(chemin));
        }
        return population;
    }

    private int[] generateRandomPath() {
        int[] chemin = new int[evaluation.getProblem().getLength()];
        for (int i = 0; i < chemin.length; i++) {
            chemin[i] = i;
        }
        shuffleArray(chemin);
        return chemin;
    }

    private void shuffleArray(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private Path selectBestSolution(ArrayList<Path> population) {
        // Sélectionnez simplement le meilleur chemin de la population en fonction de la fitness
        Path bestSolution = population.get(0);
        double bestFitness = evaluation.evaluate(bestSolution);
        for (Path path : population) {
            double fitness = evaluation.evaluate(path);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = path;
            }
        }
        return bestSolution;
    }
}