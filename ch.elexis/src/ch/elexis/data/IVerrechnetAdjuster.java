package ch.elexis.data;

/**
 * Implementations of {@link IVerrechnetAdjuster} can adjust a {@link Verrechnet} as it is created.
 * 
 * @author thomas
 *
 */
public interface IVerrechnetAdjuster {
	public static final String EXTENSIONPOINTID = "ch.elexis.VerrechnungscodeAdjuster"; 
	
	/**
	 * Adjust the created {@link Verrechnet}.
	 * 
	 * @param verrechnet
	 */
	public void adjust(Verrechnet verrechnet);
}
