package cnuphys.adaptiveSwim;

import cnuphys.magfield.FastMath;

/**
 * Stopper for swimming to a fixed cylindrical cs radius (rho) value
 * @author heddle
 *
 */
public class AdaptiveRhoStopper extends AAdaptiveStopper {

	//the rho you want to stop at in meters
	private double _targetRho;


	//is the starting rho bigger or smaller than the target
	private int _startSign;
			
	/**
	 * Rho  stopper  (does check max path length)
	 * @param u0              initial state vector
	 * @param targetRho       stopping rho in meters
	 * @param accuracy        the accuracy in meters
	 */
	public AdaptiveRhoStopper(final double[] u0, final double sf, final double targetRho, double accuracy, AdaptiveSwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetRho = targetRho;
		double rho0 = FastMath.hypot(u0[0], u0[1]);
		_startSign = sign(rho0);
	}

	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		double rho = Math.hypot(unew[0], unew[1]);

		// within accuracy?
		//note this could also result with s > smax
		if (Math.abs(rho - _targetRho) < _accuracy) {
			accept(snew, unew);
  			return true;
		}

		//stop and don't accept new data. We crossed the boundary or exceeded smax
		if ((snew > _sf) || (sign(rho) != _startSign)) {
			return true;
		}
				
		//accept new data and continue
		accept(snew, unew);
		return false;
	}
	
	
	//get the sign based on the current rho
	private int sign(double currentRho) {
		return ((currentRho < _targetRho) ? -1 : 1);
	}
	
}