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
public class Genetic extends CompetitorProject
{
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
    public Genetic(Evaluation evaluation) throws InvalidProjectException {
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
        int [] path = new int [length];
        boolean [] used = new boolean [length];
        int init = this.random.nextInt (length);
        path [0] = init;
        used [init] = true;
        for (int i = 1; i < length; i++)
        {
            Coordinates current = this.problem.getCoordinates (path [i - 1]);
            double minDist = Double.MAX_VALUE;
            int nn = -1;
            for (int j = 0; j < length; j++)
            {
                if (!used [j])
                {
                    Coordinates tmp = this.problem.getCoordinates (j);
                    double dist = current.distance (tmp);
                    if (dist < minDist)
                    {
                        minDist = dist;
                        nn = j;
                    }
                }
            }
            path [i] = nn;
            used [nn] = true;
        }

        return new Path (path);
    }

    @Override
    public void initialization ()
    {
        this.random = new Random();
        this.length = this.problem.getLength ();

        Path path;
        for (int i = 0; i < this.N  ; i++)
        {
            path = two_opt(Greedy(this.length));
            this.population.add(path);
        }
        this.sortList();
    }

    Path two_opt(Path P) {
        Path S = new Path(P);
        boolean amelioration = true;

        while (amelioration)
        for (int i = 0; i < length; i++)
        {
            amelioration = false;
            Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);

            int l = i + 1;
            if (l == length) l = 0;

            Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);

            int n = l + 1;
            for (int j = i ; j < length; j++)
            {
                if (n >= length) n = 0;

                int m = n + 1;
                if (m >= length) m = 0;

                Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                if ((i1.distance(i2) + j1.distance(j2)) > (i2.distance(j2) + i1.distance(j1)))
                {
                    amelioration = true;
                    inversePath(S, l ,n );
                    i2 = this.problem.getCoordinates(S.getPath()[l]);
                }
                n ++;
            }
        }
        return S;
    }

    Path one_opt(Path P) {
        Path S = new Path(P);
        int max = 0;
        boolean amelioration = true;

        while (amelioration && max < length)
            for (int i = 0; i < length; i++)
            {
                amelioration = false;
                Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);

                int l = i + 1;
                if (l == length) l = 0;

                int k = l + 1;
                if (k == length) k = 0;

                Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);
                Coordinates i3 = this.problem.getCoordinates(S.getPath()[k]);

                int n = k;
                for (int j = 0; j < length - 4; j++)
                {
                    if (n >= length) n = 0;

                    int m = n + 1;

                    if (m >= length) m = 0;

                    Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                    Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                    if ((i1.distance(i2) + i2.distance(i3) + j1.distance(j2)) > (j1.distance(i2) + i2.distance(j2) + i1.distance(i3)))
                    {
                        amelioration = true;
                        shiftPath(S, l ,n );
                        i1 = this.problem.getCoordinates(S.getPath()[i]);
                        i2 = this.problem.getCoordinates(S.getPath()[l]);
                        i3 = this.problem.getCoordinates(S.getPath()[k]);
                    }
                    n++;
                }
                max++;
            }
        return S;
    }

    public void sortList()
    {
        Collections.sort(this.population, Comparator.comparingDouble(this.evaluation::evaluate));
    }

    @Override
    public void loop ()
    {
        for (int i = this.N/4; i < this.N ; i++)
        {
            int index1 = random.nextInt(this.N/2);
            int index2;
            do {
                index2 =  random.nextInt(this.N/2);
            } while (index2 == index1);
            
            int rand = random.nextInt(this.length);
            Path pathChildren = Children(population.get(index1),population.get(index2),rand);

            if (random.nextDouble() < this.MUTATION)
            {
                MutationShift(pathChildren);
                MutationChange(pathChildren);
            }
            pathChildren = one_opt(two_opt(pathChildren));

            this.population.set(i,pathChildren);
        }

        //this.population.set(this.N - 1, two_opt(Greedy(this.length)));
        this.sortList();
    }

    Path Children(Path P1,Path P2,int n)
    {
        int[] p1 = P1.getPath().clone();
        int[] p2 = P2.getPath().clone();
        int[] c1 = p1.clone();
        for (int i = n ; i < length ; i++)
        {
            for (int j = 0 ; j < length ; j++)
            {
                int k = 0;
                while (!(p2[j] == p1[k]) && (k < n))
                {
                    k++;
                }

                if(k == n)
                {
                    c1[i] = p2[j];
                    i++;
                }
            }
        }
        return new Path(c1);
    }

    int randomIndex(int i)
    {
        int j;
        do {
            j = random.nextInt(length);
        } while (i == j);

        return j;
    }

    void inversePath(Path P, int index1, int index2)
    {
        int[] p = P.getPath();
        int tmp;
        int n = index1, m = index2;
        int length = (index2 - index1 + 1) / 2;

        if(index1 > index2)
        {
            length = ((this.length  - index1) + index2 + 1)/ 2;
        }

        for (int i = 0 ; i < length  ; i++)
        {

            if (n == this.length) n = 0;
            if (m == -1) m = this.length - 1;

            tmp = p[n];
            p[n] = p[m];
            p[m] = tmp;

            n++;
            m--;
        }

    }

    void MutationShift(Path P)
    {
        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);
        shiftPath(P,index1,index2);
    }

    void shiftPath(Path P,int index1,int index2)
    {
        int[] p = P.getPath();
        int tmp;
        int n = index1;
        int length = (index2 - index1 );

        if(index1 > index2)
        {
            length = ((this.length  - index1) + index2);
        }
        for (int i = 0 ; i < length  ; i++)
        {
            if (n == this.length)n = 0;

            int m = n + 1;

            if (m == this.length) m = 0;

            tmp = p[n];
            p[n] = p[m];
            p[m] = tmp;

            m++;
            n++;
        }

    }

    void MutationInversion(Path P)
    {
        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);
        inversePath(P,index1,index2);
    }

    void MutationSwap(Path P)
    {
        int[] p = P.getPath();

        int index1 = random.nextInt(length);
        int index2 = index1 + 1;
        if(index1 == length - 1)
            index2 = 0;

        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }

    void MutationChange(Path P)
    {
        int[] p = P.getPath();

        int index1 = random.nextInt(length);
        int index2 = randomIndex(index1);

        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }
}
