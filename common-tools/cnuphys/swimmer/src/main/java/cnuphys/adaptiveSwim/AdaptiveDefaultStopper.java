package cnuphys.adaptiveSwim;

public class AdaptiveDefaultStopper extends AAdaptiveStopper {

	private static final double TOL = 1.0e-5; //meters
	
	private double _sCutoff;
			
	/**
	 * Rho  stopper  (does check max path length)
	 * @param u0              initial state vector
	 * @param targetRho       stopping rho in meters
	 */
	public AdaptiveDefaultStopper(final double[] u0, final double sf, AdaptiveSwimTrajectory trajectory) {
		super(u0, sf, Double.NaN, trajectory);
		_sCutoff = sf - TOL;
	}

	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		accept(snew, unew);
		
		// within tolerance?
		return (snew > _sCutoff);
	}
	
}