package tsp.projects.competitor.bot;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.*;

/**
 * Cette classe représente un algorithme combiné d'Optimisation par Colonie de Fourmis (ACO),
 * d'Algorithme Génétique (GA) et de Recuit Simulé (SA) pour résoudre le Problème du Voyageur de Commerce (TSP).
 * Elle étend la classe CompetitorProject et implémente l'algorithme de résolution du TSP.
 */
public class CombinedACO_GA_SA extends CompetitorProject {

    private int length;
    private Random random;
    private int N = 50; // Nombre de fourmis
    private final double ALPHA = 1.0; // Poids de la phéromone
    private final double BETA = 1.0;  // Poids de l'information heuristique
    private final double RHO = 0.3;    // Taux d'évaporation
    private final double Q = 50.0;     // Facteur de pénalité pour les chemins plus longs
    private final double MUTATION = 0.5; // Probabilité de mutation pour GA
    private final double TEMPERATURE = 100.0; // Température initiale pour SA
    private final double COOLING_RATE = 0.95; // Taux de refroidissement pour SA
    private final int MAX_ITERATIONS = 10000; // Nombre maximal d'itérations pour SA

    // Pistes de phéromones pour chaque paire de villes
    private double[][] pheromones;

    /**
     * Construit un nouvel objet CombinedACO_GA_SA avec la fonction d'évaluation spécifiée.
     *
     * @param evaluation La fonction d'évaluation pour le problème TSP.
     * @throws InvalidProjectException Si l'initialisation du projet échoue.
     */
    public CombinedACO_GA_SA(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("COMBINED_ACO_GA_SA");
        setAuthors("Alexandre", "Kamelia");
    }

    /**
     * Initialise les paramètres et les structures de données nécessaires pour l'algorithme.
     */
    @Override
    public void initialization() {
        this.random = new Random();
        this.length = this.problem.getLength();
        this.pheromones = new double[length][length];

        // Initialise les phéromones à une petite valeur
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                pheromones[i][j] = 0.1;
            }
        }
    }

    /**
     * Exécute l'algorithme principal qui combine l'ACO, le GA et le SA pour résoudre le problème TSP.
     * Cette méthode itère sur un nombre défini de fourmis pour construire des chemins, puis exécute
     * l'algorithme GA sur les chemins sélectionnés, suivi de l'algorithme SA pour améliorer la solution finale.
     * Enfin, elle évalue la solution finale à l'aide de la fonction d'évaluation.
     */
    @Override
    public void loop() {
        // Exécute l'algorithme ACO
        List<Path> antPaths = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            antPaths.add(constructAntPath());
        }
        updatePheromones(antPaths); // Met à jour les phéromones basées sur les chemins des fourmis

        // Exécute l'algorithme GA sur les chemins sélectionnés ou les chemins des fourmis d'origine
        ArrayList<Path> population = generateInitialPopulation(N);
        for (int i = 0; i < 100; i++) { // Ajustez le nombre d'itérations au besoin
            population = selection(population, N);
            population = mutation(population, MUTATION);
        }
        Path gaSolution = population.get(0); // Sélectionne le meilleur chemin de la population finale

        // Exécute l'algorithme SA sur la solution GA ou le meilleur chemin des fourmis
        Path finalSolution = simulatedAnnealing(gaSolution); // Vous pouvez choisir la solution de départ ici

        evaluation.evaluate(finalSolution);
    }

    /**
     * Met à jour les phéromones en fonction des chemins parcourus par les fourmis.
     * Ce processus implique l'évaporation des phéromones existantes et l'ajout de nouvelles phéromones
     * basées sur la longueur des chemins parcourus par les fourmis.
     *
     * @param antPaths La liste des chemins parcourus par les fourmis.
     */
    private void updatePheromones(List<Path> antPaths) {
        // Évaporation
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                pheromones[i][j] *= RHO;
            }
        }

        // Mise à jour basée sur la longueur des chemins
        for (Path path : antPaths) {
            double pathLength = evaluation.evaluate(path);
            for (int i = 0; i < path.getPath().length - 1; i++) {
                int city1 = path.getPath()[i];
                int city2 = path.getPath()[i + 1];
                pheromones[city1][city2] += Q / pathLength;
            }
            // Traitement de la connexion de la dernière ville à la première
            int lastCity = path.getPath()[path.getPath().length - 1];
            int firstCity = path.getPath()[0];
            pheromones[lastCity][firstCity] += Q / pathLength;
        }
    }

    /**
     * Calcule la distance entre deux villes spécifiées.
     *
     * @param city1 L'index de la première ville.
     * @param city2 L'index de la deuxième ville.
     * @return La distance entre les deux villes.
     */
    private double calculateDistance(int city1, int city2) {
        Coordinates c1 = problem.getCoordinates(city1);
        Coordinates c2 = problem.getCoordinates(city2);
        return c1.distance(c2);
    }

    /**
     * Calcule l'heuristique entre deux villes spécifiées.
     *
     * @param city1 L'index de la première ville.
     * @param city2 L'index de la deuxième ville.
     * @return L'heuristique entre les deux villes.
     */
    private double calculateHeuristic(int city1, int city2) {
        // Vous pouvez choisir une heuristique appropriée ici, par exemple, 1/distance
        return 1.0 / calculateDistance(city1, city2);
    }

    /**
     * Sélectionne la prochaine ville à visiter pour une fourmi spécifique, en utilisant un processus de sélection de la roulette.
     *
     * @param currentCity La ville actuelle.
     * @param visited     Un tableau de booléens indiquant les villes déjà visitées.
     * @return L'index de la prochaine ville à visiter.
     */
    private int selectNextCity(int currentCity, boolean[] visited) {
        double[] probabilities = new double[length];
        double sum = 0.0;

        for (int nextCity = 0; nextCity < length; nextCity++) {
            if (!visited[nextCity]) {
                double pheromone = Math.pow(pheromones[currentCity][nextCity], ALPHA);
                double heuristic = Math.pow(calculateHeuristic(currentCity, nextCity), BETA);
                probabilities[nextCity] = pheromone * heuristic;
                sum += probabilities[nextCity];
            } else {
                probabilities[nextCity] = 0.0;
            }
        }

        // Sélection de la ville suivante selon la méthode de la roulette
        double rand = random.nextDouble() * sum;
        double partialSum = 0.0;
        for (int nextCity = 0; nextCity < length; nextCity++) {
            partialSum += probabilities[nextCity];
            if (partialSum >= rand) {
                return nextCity;
            }
        }

        return -1; // Ne devrait pas se produire
    }

    /**
     * Construit le chemin parcouru par une fourmi, en utilisant l'algorithme de sélection de la prochaine ville basé sur la roulette.
     *
     * @return Le chemin parcouru par la fourmi.
     */
    private Path constructAntPath() {
        boolean[] visited = new boolean[length];
        int[] path = new int[length];

        // Initialise une ville de départ aléatoire
        int currentCity = random.nextInt(length);
        path[0] = currentCity;
        visited[currentCity] = true;

        for (int i = 1; i < length; i++) {
            // Sélectionne la prochaine ville en fonction de la probabilité en utilisant la sélection de la roulette
            double[] probabilities = new double[length];
            double sum = 0.0;

            for (int nextCity = 0; nextCity < length; nextCity++) {
                if (!visited[nextCity]) {
                    double pheromone = Math.pow(pheromones[currentCity][nextCity], ALPHA);  // Poids des phéromones
                    double heuristic = 1.0 / calculateDistance(currentCity, nextCity); // Heuristique (inverse de la distance)
                    double probability = pheromone * heuristic;
                    probabilities[nextCity] = probability;
                    sum += probability;
                }
            }

            double rand = random.nextDouble() * sum;
            double partialSum = 0.0;
            for (int nextCity = 0; nextCity < length; nextCity++) {
                partialSum += probabilities[nextCity];
                if (partialSum >= rand) {
                    currentCity = nextCity;
                    path[i] = currentCity;
                    visited[currentCity] = true;
                    break; // Sort de la boucle après avoir sélectionné la prochaine ville
                }
            }
        }

        return new Path(path);
    }

    /**
     * Applique l'algorithme de Hill Climbing pour générer une solution initiale.
     *
     * @param length La longueur du chemin à générer.
     * @return Un chemin généré par l'algorithme de Hill Climbing.
     */
    public Path HillClimbing(int length) {
        int[] path = new int[length];
        boolean[] used = new boolean[length];
        int init = this.random.nextInt(length);
        path[0] = init;
        used[init] = true;
        for (int i = 1; i < length; i++) {
            Coordinates current = this.problem.getCoordinates(path[i - 1]);
            double minDist = Double.MAX_VALUE;
            int nn = -1;
            for (int j = 0; j < length; j++) {
                if (!used[j]) {
                    Coordinates tmp = this.problem.getCoordinates(j);
                    double dist = current.distance(tmp);
                    if (dist < minDist) {
                        minDist = dist;
                        nn = j;
                    }
                }
            }
            path[i] = nn;
            used[nn] = true;
        }

        return new Path(path);
    }

    /**
     * Génère une population initiale pour l'algorithme génétique (GA).
     *
     * @param populationSize La taille de la population à générer.
     * @return Une liste de chemins, représentant la population initiale.
     */
    public ArrayList<Path> generateInitialPopulation(int populationSize) {
        ArrayList<Path> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Path path = two_opt(HillClimbing(this.length));
            population.add(path);
        }
        return population;
    }

    /**
     * Applique l'opérateur d'amélioration 2-opt sur un chemin spécifié.
     *
     * @param P Le chemin sur lequel appliquer l'opérateur 2-opt.
     * @return Le chemin résultant après l'application de l'opérateur 2-opt.
     */
    public Path two_opt(Path P) {
        Path S = new Path(P);

        boolean ameliorable = true;

        while (ameliorable) {
            for (int i = 0; i < length; i++) {
                ameliorable = false;
                Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);
                int l = i + 1;

                if (l == length) {
                    l = 0;
                }

                Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);

                int n = l + 1;
                for (int j = i; j < length; j++) {
                    if (n >= length) n = 0;

                    int m = n + 1;

                    if (m >= length) m = 0;

                    Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                    Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                    double dist1 = i1.distance(i2);
                    double dist2 = j1.distance(j2);
                    double distCross = i1.distance(j2) + j1.distance(i2);

                    // Vérifie si l'échange 2-opt améliore le chemin
                    if (distCross < dist1 + dist2) {
                        ameliorable = true;
                        // Effectue l'échange 2-opt
                        int[] newPath = Arrays.copyOf(S.getPath(), length);
                        System.arraycopy(S.getPath(), i + 1, newPath, n + 1, l - i - 1);
                        System.arraycopy(S.getPath(), l, newPath, i + 1, n - l);
                        S = new Path(newPath);
                    }

                    n++;
                }
            }
        }

        return S;
    }

    /**
     * Effectue la sélection des individus pour la prochaine génération à l'aide du tournoi.
     *
     * @param population     La population actuelle.
     * @param populationSize La taille de la population.
     * @return Une nouvelle population sélectionnée pour la prochaine génération.
     */
    public ArrayList<Path> selection(ArrayList<Path> population, int populationSize) {
        ArrayList<Path> newPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Path P1 = tournamentSelection(population);
            Path P2 = tournamentSelection(population);

            Path child = crossover(P1, P2);
            newPopulation.add(child);
        }
        return newPopulation;
    }

    /**
     * Sélectionne un individu à partir de la population donnée pour le tournoi.
     *
     * @param population La population parmi laquelle choisir un individu pour le tournoi.
     * @return Le chemin sélectionné pour le tournoi.
     */
    private Path tournamentSelection(ArrayList<Path> population) {
        int tournamentSize = 2;
        ArrayList<Path> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }

        Path bestPath = tournament.get(0);
        for (Path path : tournament) {
            if (evaluation.evaluate(path) < evaluation.evaluate(bestPath)) {
                bestPath = path;
            }
        }
        return bestPath;
    }

    /**
     * Effectue l'opérateur de croisement sur deux chemins donnés.
     *
     * @param P1 Le premier chemin pour le croisement.
     * @param P2 Le deuxième chemin pour le croisement.
     * @return Le chemin résultant du croisement des deux chemins donnés.
     */
    private Path crossover(Path P1, Path P2) {
        int crossoverPoint = random.nextInt(length - 1) + 1; // Choix d'un point de croisement aléatoire
        int[] childPath = new int[length];

        System.arraycopy(P1.getPath(), 0, childPath, 0, crossoverPoint);
        boolean[] used = new boolean[length];
        for (int i = 0; i < crossoverPoint; i++) {
            used[P1.getPath()[i]] = true;
        }

        int j = crossoverPoint;
        for (int i = crossoverPoint; i < length; i++) {
            int nextCity = -1;
            for (int k = 0; k < length; k++) {
                if (!used[P2.getPath()[k]]) {
                    nextCity = P2.getPath()[k];
                    break;
                }
            }
            childPath[i] = nextCity;
            used[nextCity] = true;
            j++;
        }

        return new Path(childPath);
    }

    /**
     * Applique l'opérateur de mutation sur une population donnée avec une probabilité donnée.
     *
     * @param population   La population sur laquelle appliquer l'opérateur de mutation.
     * @param mutationRate Le taux de mutation.
     * @return La population résultant de l'application de l'opérateur de mutation.
     */
    public ArrayList<Path> mutation(ArrayList<Path> population, double mutationRate) {
        for (Path path : population) {
            if (random.nextDouble() < mutationRate) {
                int index1 = random.nextInt(length);
                int index2 = (index1 + random.nextInt(length - 1) + 1) % length; // Assure des indices différents
                int temp = path.getPath()[index1];
                path.getPath()[index1] = path.getPath()[index2];
                path.getPath()[index2] = temp;
            }
        }
        return population;
    }

    /**
     * Applique l'algorithme du recuit simulé (SA) pour optimiser une solution initiale.
     *
     * @param initialSolution La solution initiale à optimiser.
     * @return La solution optimisée.
     */
    public Path simulatedAnnealing(Path initialSolution) {
        Path currentSolution = initialSolution;
        double temperature = TEMPERATURE;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Path newSolution = getNeighbor(currentSolution);
            double deltaE = evaluation.evaluate(currentSolution) - evaluation.evaluate(newSolution);

            // Accepte une solution pire avec une probabilité basée sur la température
            if (deltaE < 0 || Math.exp(-deltaE / temperature) > random.nextDouble()) {
                currentSolution = newSolution;
            }

            temperature *= COOLING_RATE;
        }

        return currentSolution;
    }

    /**
     * Génère un voisin de la solution donnée en effectuant un simple échange de villes.
     *
     * @param solution La solution actuelle.
     * @return Un voisin de la solution actuelle.
     */
    private Path getNeighbor(Path solution) {
        int index1 = random.nextInt(length);
        int index2 = (index1 + random.nextInt(length - 1) + 1) % length; // Assure des indices différents

        // Échange deux villes pour créer un voisin
        int[] newPath = Arrays.copyOf(solution.getPath(), length);
        int temp = newPath[index1];
        newPath[index1] = newPath[index2];
        newPath[index2] = temp;

        return new Path(newPath);
    }
}