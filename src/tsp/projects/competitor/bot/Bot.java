package tsp.projects.competitor.bot;

import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.evaluation.Problem;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Bot extends CompetitorProject {
    protected Evaluation evaluation;
    protected Problem problem;
    private double mutationRate = 0.1; // Taux de mutation
    private Map<Path, Double> fitnessMap; // Map pour stocker la fitness de chaque chemin

    public Bot(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("Bot");
        setAuthors("Alexandre", "Kamelia");
        fitnessMap = new HashMap<>(); // Initialisation de la Map
    }

    @Override
    public void initialization() {
        // Initialisation de votre algorithme ici
        this.evaluation = super.evaluation;
        this.problem = super.problem;
    }

    @Override
    public void loop() {
        // Boucle principale de votre algorithme ici
        long startTime = System.currentTimeMillis(); // Temps de départ de la boucle

        // Création de la population initiale
        ArrayList<Path> population = createInitialPopulation(100); // Nombre de chemins dans la population initiale

        // Boucle principale de l'algorithme génétique
        for (int generation = 0; generation < 1000; generation++) { // Nombre maximal de générations
            // Évaluation de la population
            evaluatePopulation(population);

            // Sélection des parents
            ArrayList<Path> parents = selectParents(population);

            // Croisement des parents pour créer de nouveaux enfants
            ArrayList<Path> enfants = crossover(parents);

            // Mutation des enfants
            mutate(enfants);

            // Remplacement de la population par les enfants
            population = enfants;
        }

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

    private ArrayList<Path> selectParents(ArrayList<Path> population) {
        // Sélectionnez simplement deux chemins aléatoires de la population comme parents
        ArrayList<Path> parents = new ArrayList<>();
        Random random = new Random();
        parents.add(population.get(random.nextInt(population.size())));
        parents.add(population.get(random.nextInt(population.size())));
        return parents;
    }

    private ArrayList<Path> crossover(ArrayList<Path> parents) {
        // Implémentez le croisement des chemins pour créer de nouveaux enfants (par exemple, à deux points de coupure)
        // Pour cette implémentation simplifiée, je vais simplement retourner les parents sans croisement
        return parents;
    }

    private void mutate(ArrayList<Path> enfants) {
        // Pour cette implémentation simplifiée, je vais juste appliquer une mutation aléatoire à chaque enfant
        Random random = new Random();
        for (Path enfant : enfants) {
            if (random.nextDouble() < mutationRate) {
                // Appliquer une mutation au hasard au chemin de l'enfant
                mutatePath(enfant);
            }
        }
    }

    private void mutatePath(Path path) {
        // Implémentez la logique de mutation (par exemple, échangez deux villes aléatoires dans le chemin)
        // Pour cette implémentation simplifiée, je vais simplement mélanger le chemin
        Random random = new Random();
        int[] pathArray = path.getPath();
        shuffleArray(pathArray);
    }

    private void evaluatePopulation(ArrayList<Path> population) {
        // Évaluez la fitness de chaque chemin dans la population
        for (Path path : population) {
            double fitness = evaluation.evaluate(path);
            // Stocker la fitness dans la Map
            fitnessMap.put(path, fitness);
        }
    }

    private Path selectBestSolution(ArrayList<Path> population) {
        // Sélectionnez simplement le meilleur chemin de la population en fonction de la fitness
        Path bestSolution = population.get(0);
        for (Path path : population) {
            // Utilisez la fitness stockée dans la Map
            double fitness = fitnessMap.get(path);
            if (fitness < evaluation.evaluate(bestSolution)) {
                bestSolution = path;
            }
        }
        return bestSolution;
    }
}
