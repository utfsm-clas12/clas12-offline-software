package cnuphys.adaptiveSwim;

public class AdaptiveRhoStopper implements IAdaptiveStopper {

	private double _targetRho;
	private double _accuracy;
	private double _s;

	/**
	 * Rho  stopper that doesn't check max R (does check max path length)
	 * 
	 * @param s0              starting path length in meters
	 * @param targetRho       stopping rho in meters
	 * @param accuracy        the accuracy in meters
	 */
	public AdaptiveRhoStopper(double s0, double targetRho, double accuracy) {
		_s = s0;
		_targetRho = targetRho;
		_accuracy = accuracy;
	}
	
	@Override
	public boolean stopIntegration(double s, double[] u) {
		_s = s;
		double rho = Math.hypot(u[0], u[1]);
		return Math.abs((rho-_targetRho)) < _accuracy;
	}

	/**
	 * Get the final path length in meters
	 * 
	 * @return the final path length in meters
	 */
	@Override
	public double getFinalS() {
		return _s;
	}

}