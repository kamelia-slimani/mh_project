package tsp.projects.competitor.bot;

import tsp.evaluation.Path;

import java.util.*;

public class GeneticAlgorithm {

    private final double initialMutationRate = 0.1;
    private double mutationRate = initialMutationRate;
    private final int maxIterations = 1000; // Critère d'arrêt : nombre maximal d'itérations
    private final int NUM_ANTS = 100;
    private final int STAGNATION_THRESHOLD = 50; // Nombre maximal d'itérations sans amélioration

    public List<Path> runGeneticAlgorithm(List<Path> parentPaths) {
        List<Path> childPaths = new ArrayList<>();
        int iterationsWithoutImprovement = 0;
        double bestFitness = Double.POSITIVE_INFINITY;
        Random random = new Random();

        while (iterationsWithoutImprovement < STAGNATION_THRESHOLD) {
            for (int i = 0; i < NUM_ANTS / 2; i++) {
                // Sélection de deux parents
                Path parent1 = selectParent(parentPaths, random);
                Path parent2 = selectParent(parentPaths, random);

                // Croisement des parents pour créer deux enfants
                Path child1 = crossover(parent1, parent2, random);
                Path child2 = crossover(parent2, parent1, random);

                // Mutation des enfants avec une certaine probabilité
                if (random.nextDouble() < mutationRate) {
                    mutate(child1, random);
                }
                if (random.nextDouble() < mutationRate) {
                    mutate(child2, random);
                }

                // Ajout des enfants à la liste des chemins enfants
                childPaths.add(child1);
                childPaths.add(child2);
            }

            // Mettre à jour le taux de mutation
            updateMutationRate();

            // Évaluer la meilleure solution
            double currentBestFitness = evaluatePath(parentPaths.get(0)); // Supposons que le premier chemin est le meilleur
            if (currentBestFitness < bestFitness) {
                bestFitness = currentBestFitness;
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
            }
        }

        return childPaths;
    }

    private void updateMutationRate() {
        // Ajustement dynamique du taux de mutation (facultatif)
        // Vous pouvez ajuster le taux de mutation ici en fonction de la convergence de l'algorithme
        // Par exemple, vous pouvez réduire le taux de mutation au fil du temps pour une exploration plus locale
    }


    private double evaluatePath(Path path) {
        // Évaluation de la qualité du chemin
        return 1.0 / path.getPath().length; // Simple évaluation pour cet exemple
    }

    private Path selectParent(List<Path> paths, Random random) {
        // Sélection aléatoire d'un parent basée sur la qualité du chemin
        double totalFitness = paths.stream().mapToDouble(this::evaluatePath).sum();
        double rand = random.nextDouble() * totalFitness;
        double currentSum = 0.0;
        for (Path path : paths) {
            currentSum += evaluatePath(path);
            if (currentSum >= rand) {
                return path;
            }
        }
        // Ne devrait pas arriver, mais au cas où, retourner le dernier chemin
        return paths.get(paths.size() - 1);
    }

    private Path crossover(Path parent1, Path parent2, Random random) {
        // Croisement des chemins parents
        int[] parent1Array = parent1.getPath();
        int[] parent2Array = parent2.getPath();
        int length = parent1Array.length;

        int[] childArray = new int[length];
        int startPos = random.nextInt(length);
        int endPos = random.nextInt(length);

        for (int i = 0; i < length; i++) {
            if (startPos < endPos && i > startPos && i < endPos) {
                childArray[i] = parent1Array[i];
            } else if (startPos > endPos && !(i < startPos && i > endPos)) {
                childArray[i] = parent1Array[i];
            }
        }

        for (int i = 0; i < length; i++) {
            int finalI = i;
            if (!Arrays.stream(childArray).anyMatch(x -> x == parent2Array[finalI])) {
                for (int j = 0; j < length; j++) {
                    if (childArray[j] == 0) {
                        childArray[j] = parent2Array[i];
                        break;
                    }
                }
            }
        }

        return new Path(childArray);
    }

    private void mutate(Path path, Random random) {
        // Mutation d'un chemin
        int[] pathArray = path.getPath();
        int length = pathArray.length;

        int pos1 = random.nextInt(length);
        int pos2 = random.nextInt(length);

        int temp = pathArray[pos1];
        pathArray[pos1] = pathArray[pos2];
        pathArray[pos2] = temp;
    }
}
