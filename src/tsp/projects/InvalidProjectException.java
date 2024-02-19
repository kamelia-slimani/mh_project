package tsp.projects;

/**
 * @author Alexandre Blansché
 * Exception indiquand qu'un bot n'est pas valide
 */
public class InvalidProjectException extends Exception
{
	private static final long serialVersionUID = 8987635886310738477L;

	/**
     * Constructeur...
     */
    public InvalidProjectException ()
    {
        super ("Bot invalide");
    }
    
    /**
     * @param message Le message à afficher
     */
    public InvalidProjectException (String message)
    {
        super ("Bot invalide : " + message);
    }
}
