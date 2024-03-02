package tsp.projects.competitor.bot;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Path;

public class TwoOpt {

    public static Path optimize(Path path, Coordinates[] coordinates) {
        int[] cities = path.getPath();
        int size = cities.length;

        int[] newPath = new int[size];
        System.arraycopy(cities, 0, newPath, 0, size);

        boolean improved = true;

        while (improved) {
            improved = false;
            for (int i = 0; i < size - 1; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (j - i == 1) continue; // No need to reverse adjacent edges
                    int[] swappedPath = twoOptSwap(newPath, i, j);
                    double newDistance = calculateDistance(swappedPath, coordinates);
                    double currentDistance = calculateDistance(newPath, coordinates);
                    if (newDistance < currentDistance) {
                        System.arraycopy(swappedPath, 0, newPath, 0, size);
                        improved = true;
                    }
                }
            }
        }

        return new Path(newPath);
    }

    private static int[] twoOptSwap(int[] path, int i, int j) {
        int size = path.length;
        int[] newPath = new int[size];
        System.arraycopy(path, 0, newPath, 0, size);
        int dec = 0;
        for (int k = i; k <= j; k++) {
            newPath[k] = path[j - dec];
            dec++;
        }
        return newPath;
    }

    private static double calculateDistance(int[] path, Coordinates[] coordinates) {
        double distance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            distance += coordinates[path[i]].distance(coordinates[path[i + 1]]);
        }
        distance += coordinates[path[path.length - 1]].distance(coordinates[path[0]]);
        return distance;
    }
}
