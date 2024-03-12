package tsp.projects.competitor.AlexandreUntereinerKameliaSlimani;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.*;

/**
 * Implémentation d'un algorithme génétique pour résoudre le problème du voyageur de commerce.
 */
public class BakiTKO extends CompetitorProject {
    private int length; // Longueur du problème
    private Random random; // Générateur de nombres aléatoires
    private int N = 20; // Taille de la population
    private final double MUTATION = 0.5; // Taux de mutation

    ArrayList<Path> population = new ArrayList<Path>(); // Population de chemins

    /**
     * Constructeur de la classe Genetic.
     *
     * @param evaluation L'instance de l'évaluation
     * @throws InvalidProjectException Si le projet est invalide
     */
    public BakiTKO(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("GENETIC");
        setAuthors("Alexandre Untereiner", "Kamelia Slimani");
    }

    /**
     * Implémente l'algorithme glouton pour générer un chemin initial.
     *
     * @param length Longueur du chemin
     * @return Le chemin généré
     */
    public Path Greedy(int length)
    {
        int [] path = new int [length]; // Initialise un tableau pour stocker le chemin
        boolean [] used = new boolean [length]; // Initialise un tableau pour suivre les villes déjà visitées
        int init = this.random.nextInt (length); // Choix aléatoire de la première ville
        path [0] = init; // Ajoute la première ville au chemin
        used [init] = true; // Marque la première ville comme visitée
        for (int i = 1; i < length; i++)
        {
            // Boucle pour ajouter les autres villes au chemin de manière gloutonne
            Coordinates current = this.problem.getCoordinates (path [i - 1]); // Coordonnées de la dernière ville visitée
            double minDist = Double.MAX_VALUE; // Initialise la distance minimale à une valeur très grande
            int nn = -1; // Indice de la ville la plus proche
            for (int j = 0; j < length; j++)
            {
                // Parcours de toutes les villes pour trouver la plus proche non visitée
                if (!used [j])
                {
                    Coordinates tmp = this.problem.getCoordinates (j); // Coordonnées de la ville en cours d'évaluation
                    double dist = current.distance (tmp); // Calcul de la distance entre les deux villes
                    if (dist < minDist)
                    {
                        // Si la distance est plus petite que la distance minimale actuelle
                        minDist = dist; // Met à jour la distance minimale
                        nn = j; // Met à jour l'indice de la ville la plus proche
                    }
                }
            }
            path [i] = nn; // Ajoute la ville la plus proche au chemin
            used [nn] = true; // Marque la ville ajoutée comme visitée
        }

        return new Path (path); // Retourne le chemin généré
    }


    /**
     * Initialise la population initiale en utilisant l'algorithme glouton et l'opérateur 2-opt.
     */
    @Override
    public void initialization ()
    {
        this.random = new Random(); // Initialise le générateur de nombres aléatoires
        this.length = this.problem.getLength (); // Récupère la longueur du problème

        Path path;
        for (int i = 0; i < this.N  ; i++)
        {
            // Boucle pour générer la population initiale
            path = two_opt(Greedy(this.length)); // Applique l'algorithme glouton suivi de l'opérateur 2-opt
            this.population.add(path); // Ajoute le chemin généré à la population
        }
        this.sortList(); // Trie la population en fonction de l'évaluation des chemins
    }
    /**
     * Applique l'opérateur 2-opt sur un chemin.
     *
     * @param P Le chemin sur lequel appliquer l'opérateur 2-opt
     * @return Le chemin résultant après l'application de l'opérateur 2-opt
     */
    Path two_opt(Path P) {
        // Créez une copie du chemin d'entrée
        Path S = new Path(P);
        boolean amelioration = true;

        // Continuez jusqu'à ce qu'aucune autre amélioration ne soit possible
        while (amelioration) {
            for (int i = 0; i < length; i++) {
                amelioration = false;
                Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);

                // Calculez l'index suivant (circulaire)
                int l = i + 1;
                if (l == length) l = 0;
                Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);

                // Initialisez l'index suivant (circulaire)
                int n = l + 1;
                for (int j = i; j < length; j++) {
                    if (n >= length) n = 0;

                    // Calculez l'index suivant-suivant (circulaire)
                    int m = n + 1;
                    if (m >= length) m = 0;

                    Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                    Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                    if ((i1.distance(i2) + j1.distance(j2)) > (i2.distance(j2) + i1.distance(j1))) {
                        amelioration = true;
                        inversePath(S, l, n);
                        i2 = this.problem.getCoordinates(S.getPath()[l]);
                    }
                    n++;
                }
            }
        }
        return S;
    }

    /**
     * Applique l'opérateur 1-opt sur un chemin.
     *
     * @param P Le chemin sur lequel appliquer l'opérateur 1-opt
     * @return Le chemin résultant après l'application de l'opérateur 1-opt
     */
    Path one_opt(Path P) {
        // Créez une copie du chemin d'entrée
        Path S = new Path(P);
        int max = 0;
        boolean amelioration = true;

        // Continuez jusqu'à ce qu'aucune autre amélioration ne soit possible ou jusqu'à atteindre la limite de boucles
        while (amelioration && max < length) {
            for (int i = 0; i < length; i++) {
                amelioration = false;
                Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);

                // Calculez l'index suivant (circulaire)
                int l = i + 1;
                if (l == length) l = 0;
                Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);

                // Initialisez l'index suivant (circulaire)
                int k = l + 1;
                if (k == length) k = 0;
                Coordinates i3 = this.problem.getCoordinates(S.getPath()[k]);

                int n = k;
                for (int j = 0; j < length - 4; j++) {
                    if (n >= length) n = 0;

                    // Calculez l'index suivant-suivant (circulaire)
                    int m = n + 1;
                    if (m >= length) m = 0;

                    Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                    Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                    if ((i1.distance(i2) + i2.distance(i3) + j1.distance(j2)) > (j1.distance(i2) + i2.distance(j2) + i1.distance(i3))) {
                        amelioration = true;
                        shiftPath(S, l, n);
                        i1 = this.problem.getCoordinates(S.getPath()[i]);
                        i2 = this.problem.getCoordinates(S.getPath()[l]);
                        i3 = this.problem.getCoordinates(S.getPath()[k]);
                    }
                    n++;
                }
            }
            max++;
        }
        return S;
    }

    /**
     * Trie la population en fonction de l'évaluation des chemins.
     */
    public void sortList()
    {
        Collections.sort(this.population, Comparator.comparingDouble(this.evaluation::evaluate));
    }


    /**
     * Boucle principale de l'algorithme génétique.
     */
    @Override
    public void loop() {
        // Boucle pour générer de nouveaux individus
        for (int i = this.N / 4; i < this.N; i++) {
            int index1 = random.nextInt(this.N / 2);
            int index2;
            do {
                index2 = random.nextInt(this.N / 2);
            } while (index2 == index1);

            int rand = random.nextInt(this.length);
            Path pathChildren = Children(population.get(index1), population.get(index2), rand);

            // Mutation avec une certaine probabilité
            if (random.nextDouble() < this.MUTATION) {
                MutationShift(pathChildren);
                MutationChange(pathChildren);
            }

            // Optimisation des chemins
            pathChildren = one_opt(two_opt(pathChildren));

            // Remplacement de l'individu actuel par le nouvel individu
            this.population.set(i, pathChildren);
        }

        // Tri de la liste d'individus
        this.sortList();
    }

    /**
     * Génère un enfant à partir de deux chemins parents.
     *
     * @param P1 Premier parent
     * @param P2 Deuxième parent
     * @param n Point de croisement
     * @return L'enfant généré
     */
    Path Children(Path P1, Path P2, int n) {
        // Cloner les chemins P1 et P2
        int[] p1 = P1.getPath().clone();
        int[] p2 = P2.getPath().clone();
        int[] c1 = p1.clone();

        // Parcourir les indices à partir de n
        for (int i = n; i < length; i++) {
            for (int j = 0; j < length; j++) {
                int k = 0;
                // Recherche de l'indice k tel que p2[j] == p1[k]
                while (!(p2[j] == p1[k]) && (k < n)) {
                    k++;
                }

                // Si k == n, alors p2[j] n'est pas dans les n premiers éléments de p1
                if (k == n) {
                    c1[i] = p2[j];
                    i++;
                }
            }
        }
        return new Path(c1);
    }

    /**
     * Génère un index aléatoire différent de i.
     *
     * @param i Index à éviter
     * @return L'index aléatoire différent de i
     */
    int randomIndex(int i) {
        int j;
        do {
            // Génère un indice aléatoire différent de i
            j = random.nextInt(length);
        } while (i == j);

        return j;
    }


    /**
     * Inverse une portion du chemin P entre index1 et index2.
     *
     * @param P       Le chemin à inverser
     * @param index1  Index de début
     * @param index2  Index de fin
     */
    void inversePath(Path P, int index1, int index2) {
        int[] p = P.getPath();
        int tmp;
        int n = index1, m = index2;
        int length = (index2 - index1 + 1) / 2;

        // Si index1 est plus grand que index2, ajustez la longueur
        if (index1 > index2) {
            length = ((this.length - index1) + index2 + 1) / 2;
        }

        for (int i = 0; i < length; i++) {
            // Gérez les indices circulaires
            if (n == this.length) n = 0;
            if (m == -1) m = this.length - 1;

            // Échangez les éléments aux indices n et m
            tmp = p[n];
            p[n] = p[m];
            p[m] = tmp;

            n++;
            m--;
        }
    }

    /**
     * Applique l'opérateur de mutation Shift sur le chemin P.
     * Rend deux villes voisines
     *
     * @param P Le chemin sur lequel appliquer l'opérateur de mutation Shift
     */
    void MutationShift(Path P) {
        // Génère deux indices aléatoires
        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);

        // Applique la fonction shiftPath avec les indices générés
        shiftPath(P, index1, index2);
    }


    /**
     * Applique un décalage sur le chemin P entre index1 et index2.
     *
     * @param P       Le chemin sur lequel appliquer le décalage
     * @param index1  Index de début
     * @param index2  Index de fin
     */
    void shiftPath(Path P, int index1, int index2) {
        int[] p = P.getPath();
        int tmp;
        int n = index1;
        int length = (index2 - index1);

        // Si index1 est plus grand que index2, ajustez la longueur
        if (index1 > index2) {
            length = ((this.length - index1) + index2);
        }

        for (int i = 0; i < length; i++) {
            if (n == this.length) n = 0;

            int m = n + 1;
            if (m == this.length) m = 0;

            // Échangez les éléments aux indices n et m
            tmp = p[n];
            p[n] = p[m];
            p[m] = tmp;

            n++;
        }
    }

    /**
     * Applique l'opérateur de mutation Inversion sur le chemin P.
     *
     * @param P Le chemin sur lequel appliquer l'opérateur de mutation Inversion
     */
    void MutationInversion(Path P) {
        // Génère deux indices aléatoires
        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);

        // Applique la fonction inversePath avec les indices générés
        inversePath(P, index1, index2);
    }


    /**
     * Applique l'opérateur de mutation Swap sur le chemin P.
     *
     * @param P Le chemin sur lequel appliquer l'opérateur de mutation Swap
     */
    void MutationSwap(Path P) {
        int[] p = P.getPath();

        // Génère un indice aléatoire
        int index1 = random.nextInt(length);

        // Calcule l'indice suivant (circulaire)
        int index2 = index1 + 1;
        if (index1 == length - 1)
            index2 = 0;

        // Échange les éléments aux indices index1 et index2
        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }


    /**
     * Applique l'opérateur de mutation Change sur le chemin P.
     * Echange 2 villes
     * @param P Le chemin sur lequel appliquer l'opérateur de mutation Change
     */
    void MutationChange(Path P) {
        int[] p = P.getPath();

        // Génère deux indices aléatoires
        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);

        // Échange les éléments aux indices index1 et index2
        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }

}
