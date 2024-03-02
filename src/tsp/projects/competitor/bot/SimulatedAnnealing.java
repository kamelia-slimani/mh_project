package tsp.projects.competitor.bot;

import tsp.evaluation.Path;

import java.util.List;

public class SimulatedAnnealing {

    public Path findBestPath(Path path, int coolingSteps) {
        Path bestPath = new Path(path);
        double bestDistance = evaluatePath(bestPath);
        double currentTemperature = 1000; // Température initiale
        double coolingRate = 0.003; // Taux de refroidissement

        for (int i = 0; i < coolingSteps; i++) {
            Path newPath = generateNeighborPath(bestPath);
            double newDistance = evaluatePath(newPath);
            if (acceptanceProbability(bestDistance, newDistance, currentTemperature) > Math.random()) {
                bestPath = new Path(newPath);
                bestDistance = newDistance;
            }
            currentTemperature *= 1 - coolingRate;
        }

        return bestPath;
    }



    private static double evaluatePath(Path path) {
        // Évaluer la qualité du chemin
        return 1.0 / path.getPath().length; // Simple évaluation pour cet exemple
    }

    private static Path generateNeighborPath(Path path) {
        // Générer un voisin du chemin en inversant une séquence aléatoire de villes
        int[] cities = path.getPath().clone();
        int size = cities.length;
        int index1 = (int) (Math.random() * size);
        int index2 = (int) (Math.random() * size);
        int temp = cities[index1];
        cities[index1] = cities[index2];
        cities[index2] = temp;
        return new Path(cities);
    }

    private static double acceptanceProbability(double currentDistance, double newDistance, double temperature) {
        // Probabilité d'acceptation pour le recuit simulé
        if (newDistance < currentDistance) {
            return 1.0;
        }
        return Math.exp((currentDistance - newDistance) / temperature);
    }
}
