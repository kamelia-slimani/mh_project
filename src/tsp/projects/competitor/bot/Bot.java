package tsp.projects.competitor.bot;

import tsp.evaluation.Evaluation;
import tsp.evaluation.Path;
import tsp.evaluation.Problem;
import tsp.projects.CompetitorProject;
import tsp.projects.InvalidProjectException;

import java.util.ArrayList;
import java.util.Random;

public class Bot extends CompetitorProject {
    protected Evaluation evaluation;
    protected Problem problem;

    public Bot(Evaluation evaluation) throws InvalidProjectException {
        super(evaluation);
        setMethodName("Bot");
        setAuthors("Alexandre", "Kamelia");
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

        // Construction gloutonne stochastique d'une solution initiale
        Path meilleureSolution = GRASP();

        // Mettre à jour la meilleure solution si nécessaire
        evaluation.evaluate(meilleureSolution);
    
    }

    private Path GRASP() {
        Path meilleureSolution = null;
        double meilleureEvaluation = Double.MAX_VALUE;
        int nombreIterations = 10; // Nombre d'itérations de l'algorithme GRASP

        // Répéter l'algorithme GRASP avec plusieurs points de départ
        for (int i = 0; i < nombreIterations; i++) {
            // Initialisation : liste des villes non visitées
            ArrayList<Integer> villesNonVisitees = new ArrayList<>();
            for (int j = 0; j < evaluation.getProblem().getLength(); j++) {
                villesNonVisitees.add(j);
            }

            // Sélection aléatoire du point de départ
            int villeDepart = choisirVilleAleatoire(villesNonVisitees);
            int[] chemin = new int[evaluation.getProblem().getLength()];
            chemin[0] = villeDepart; // Première ville du chemin
            villesNonVisitees.remove(Integer.valueOf(villeDepart));

            // Construction de la solution par un algorithme glouton stochastique
            for (int k = 1; k < evaluation.getProblem().getLength(); k++) {
                // Établir une liste restreinte de villes candidates
                ArrayList<Integer> RCL = construireRCL(villeDepart, villesNonVisitees);

                // Sélection aléatoire dans la liste restreinte
                int prochaineVille = choisirVilleAleatoire(RCL);
                chemin[k] = prochaineVille;
                villesNonVisitees.remove(Integer.valueOf(prochaineVille));

                // Mettre à jour la ville de départ pour la prochaine itération
                villeDepart = prochaineVille;
            }

            // Création de l'objet Path avec le chemin construit
            Path solution = new Path(chemin);

            // Application de Hill Climbing
            solution = hillClimbing(solution);

            // Mettre à jour la meilleure solution trouvée
            double evaluationActuelle = evaluation.evaluate(solution);
            if (evaluationActuelle < meilleureEvaluation) {
                meilleureSolution = solution;
                meilleureEvaluation = evaluationActuelle;
            }
        }

        return meilleureSolution;
    }

    private int choisirVilleAleatoire(ArrayList<Integer> villes) {
        Random random = new Random();
        return villes.get(random.nextInt(villes.size()));
    }

    private ArrayList<Integer> construireRCL(int villeDepart, ArrayList<Integer> villesNonVisitees) {
        // Implémenter la construction de la liste restreinte de candidats (RCL)
        // Vous pouvez utiliser une méthode de sélection basée sur une certaine heuristique
        // Par exemple, la probabilité inversement proportionnelle à la distance
        return villesNonVisitees;
    }

    private Path hillClimbing(Path solutionInitiale) {
        // Implémenter l'algorithme de Hill Climbing pour améliorer la solution initiale
        // Retourner la meilleure solution trouvée
        return solutionInitiale;
    }
}
