package tsp.projects.competitor.bot;

import tsp.evaluation.Coordinates;
import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.*;

public class Genetic extends CompetitorProject
{
    private int length;
    private Random random;
    private int N = 100;
    private static double MUTATION = 0.05;

    ArrayList<Path> population = new ArrayList<Path>();

    public Genetic(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("GENETIC");
        setAuthors("Alexandre", "Kamelia");
    }


    public Path HillClimbing(int length)
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

        Path path = HillClimbing(this.length);
        this.population.add(path);
        for (int i = 1; i < this.N/2  ; i++)
        {
            path = two_opt(HillClimbing(this.length));
            this.population.add(path);
        }
        for (int i = this.N/2; i < this.N  ; i++)
        {
            path = two_opt(new Path(this.length));
            this.population.add(path);
        }
        this.sortList();
    }

    Path two_opt(Path P) {
        Path S = new Path(P);

        boolean ameliorable = true;

        while (ameliorable)
        for (int i = 0; i < length; i++)
        {
            ameliorable = false;
            Coordinates i1 = this.problem.getCoordinates(S.getPath()[i]);
            int l = i + 1;

            if (l == length)
            {
                l = 0;
            }

            Coordinates i2 = this.problem.getCoordinates(S.getPath()[l]);

            int n = l + 1;
            int m = n + 1;
            for (int j = i ; j < length; j++)
            {
                if (n >= length)
                {
                    n = 0;
                    m = 1;
                }

                if (m >= length)
                {
                    m = 0;
                }

                Coordinates j1 = this.problem.getCoordinates(S.getPath()[n]);
                Coordinates j2 = this.problem.getCoordinates(S.getPath()[m]);

                double dist1 = i1.distance(i2);
                double dist2 = j1.distance(j2);
                double dist3 = i2.distance(j2);
                double dist4 = i1.distance(j1);

                if ((dist1 + dist2) > (dist3 + dist4))
                {
                    ameliorable = true;
                    invercePath(S, l ,n );
                    i2 = this.problem.getCoordinates(S.getPath()[l]);
                    /*j1 = this.problem.getCoordinates(S.getPath()[n]);
                    j2 = this.problem.getCoordinates(S.getPath()[m]);

                    dist1 = i1.distance(i2);
                    dist2 = j1.distance(j2);
                    dist3 = i2.distance(j2);
                    dist4 = i1.distance(j1);

                    double oo = this.evaluation.evaluate(P);
                    double ff = this.evaluation.evaluate(S);

                    if((dist1 + dist2) > (dist3 + dist4))
                    {
                        System.err.println("test  : " + (dist1 + dist2) + " < " + (dist3 + dist4));
                    }

                    if(oo < ff)
                    {
                        System.err.println(oo + " < " + ff);
                        System.err.println(P);
                        System.err.println(S);
                    }*/
                }

                n = n + 1;
                m = n + 1;
            }
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
        for (int i = 10; i < this.N  - 1 ; i++)
        {
            int index1 = random.nextInt(100);
            int index2;
            do {
                index2 = random.nextInt(100);
            } while (index2 == index1);

            Path path1 = population.get(index1);
            Path path2 = population.get(index2);
            int rand = random.nextInt(this.length);
            Path pathc1 = Children(path1,path2,rand);

            this.population.set(i,two_opt(pathc1));
        }
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

    int randomIndex()
    {
        return random.nextInt(length);
    }

    int randomIndex(int i)
    {
        int j;
        do {
            j = randomIndex();
        } while (i == j);

        return j;
    }

    void invercePath(Path P,int index1,int index2)
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

    void MutationInvertion(Path P)
    {
        int index1 = randomIndex();
        int index2 = randomIndex(index1);
        invercePath(P,index1,index2);
    }

    void MutationSwap(Path P)
    {
        int[] p = P.getPath();

        int index1 = randomIndex();
        int index2 = index1 + 1;
        if(index1 == length - 1)
            index2 = 0;

        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }

    void MutationEchange(Path P)
    {
        int[] p = P.getPath();

        int index1 = randomIndex();
        int index2 = randomIndex(index1);

        int temp = p[index1];
        p[index1] = p[index2];
        p[index2] = temp;
    }
}
