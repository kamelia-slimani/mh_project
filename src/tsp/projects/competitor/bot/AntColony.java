package tsp.projects.competitor.bot;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AntColony {

    private final int numAnts;
    private final List<Coordinates> cities;
    private final double[][] pheromones;
    private final double evaporationRate;
    private final double initialPheromone;
    private final double alpha;
    private final double beta;


    public AntColony(int numAnts, List<Coordinates> cities, double evaporationRate, double initialPheromone, double alpha, double beta) {
        this.numAnts = numAnts;
        this.cities = cities;
        this.evaporationRate = evaporationRate;
        this.initialPheromone = initialPheromone;
        this.alpha = alpha;
        this.beta = beta;
        this.pheromones = initializePheromones(cities.size());
    }

    public ArrayList<Path> runAntColonyOptimization(ArrayList<Path> population, int numIterations) {
        ArrayList<Path> optimizedPaths = new ArrayList<>();

        for (Path path : population) {
            List<Integer> citiesList = new ArrayList<>();
            for (int city : path.getPath()) {
                citiesList.add(city);
            }
            Path optimizedPath = findBestPath(citiesList, numIterations);
            optimizedPaths.add(optimizedPath);
        }
        return optimizedPaths;
    }

    public Path findBestPath(List<Integer> cities, int numIterations) {
        int[] bestPathArray = null;
        double bestDistance = Double.MAX_VALUE;

        for (int iteration = 0; iteration < numIterations; iteration++) {
            List<int[]> antPaths = new ArrayList<>();
            List<Double> distances = new ArrayList<>();

            for (int ant = 0; ant < numAnts; ant++) {
                List<Integer> path = constructAntPath();
                int[] pathArray = path.stream().mapToInt(i -> i).toArray();
                antPaths.add(pathArray);
                double distance = calculatePathDistance(path);
                distances.add(distance);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestPathArray = pathArray;
                }
            }

            updatePheromones(antPaths, distances);
        }

        return new Path(bestPathArray);
    }

    private List<Integer> constructAntPath() {
        List<Integer> path = new ArrayList<>(cities.size());
        List<Integer> antPath = new ArrayList<>(cities.size());
        for (int i = 0; i < cities.size(); i++) {
            path.add(i);
        }

        int startIndex = new Random().nextInt(cities.size());
        int currentIndex = startIndex;
        antPath.add(currentIndex);
        path.remove(Integer.valueOf(currentIndex));

        while (!path.isEmpty()) {
            int nextCity = chooseNextCity(currentIndex, path);
            antPath.add(nextCity);
            path.remove(Integer.valueOf(nextCity));
            currentIndex = nextCity;
        }

        antPath.add(startIndex); // Retour à la ville de départ pour compléter le cycle

        return antPath;
    }

    private int chooseNextCity(int currentIndex, List<Integer> availableCities) {
        double[] probabilities = calculateProbabilities(currentIndex, availableCities);
        double random = Math.random();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < availableCities.size(); i++) {
            cumulativeProbability += probabilities[i];
            if (random < cumulativeProbability) {
                return availableCities.get(i);
            }
        }
        return availableCities.get(new Random().nextInt(availableCities.size())); // Si aucune ville n'est choisie, choisissez une ville aléatoire
    }

    private double[] calculateProbabilities(int currentIndex, List<Integer> availableCities) {
        double[] probabilities = new double[availableCities.size()];
        double total = 0.0;

        for (int i = 0; i < availableCities.size(); i++) {
            int city = availableCities.get(i);
            double pheromone = pheromones[currentIndex][city];
            double visibility = 1.0 / calculateDistance(currentIndex, city);
            probabilities[i] = Math.pow(pheromone, alpha) * Math.pow(visibility, beta);
            total += probabilities[i];
        }

        for (int i = 0; i < availableCities.size(); i++) {
            probabilities[i] /= total; // Normalisation des probabilités
        }

        return probabilities;
    }

    private void updatePheromones(List<int[]> antPaths, List<Double> distances) {
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones[i].length; j++) {
                pheromones[i][j] *= (1 - evaporationRate); // Évaporation des phéromones
            }
        }

        for (int ant = 0; ant < numAnts; ant++) {
            int[] path = antPaths.get(ant);
            double pheromoneToAdd = 1.0 / distances.get(ant);
            for (int i = 0; i < path.length - 1; i++) {
                int city1 = path[i];
                int city2 = path[i + 1];
                pheromones[city1][city2] += pheromoneToAdd;
                pheromones[city2][city1] += pheromoneToAdd; // Symétrie
            }
        }
    }

    private double[][] initializePheromones(int numCities) {
        double[][] pheromones = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromones[i][j] = initialPheromone;
            }
        }
        return pheromones;
    }

    private double calculatePathDistance(List<Integer> path) {
        double distance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += calculateDistance(path.get(i), path.get(i + 1));
        }
        return distance;
    }

    private double calculateDistance(int city1, int city2) {
        Coordinates coord1 = cities.get(city1);
        Coordinates coord2 = cities.get(city2);
        return coord1.distance(coord2);
    }

    public int getNumCities() {
        return cities.size();
    }
}
