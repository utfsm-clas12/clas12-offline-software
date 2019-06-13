package cnuphys.adaptiveSwim;

import cnuphys.swim.SwimTrajectory;

public class AdaptiveSwimResult {
	
	//the final state vector
	private double[] _uf;
	
	//the number of integration steps
	private int _nStep;
	
	//the final path length
	private double _finalS;
	
	//a status, one of the class constants
	private int _status;
	
	//optionally holds a trajectory of [x, y, z, tx, ty, tz] (coords in meters)
	private AdaptiveSwimTrajectory _trajectory;
	
	//integer charge of particle
	protected int q;
	
	//initial x coordinate in meters
	protected double xo;
	
	//initial x coordinate in meters
	protected double yo;

	//initial x coordinate in meters
	protected double zo;
	
	//momentum in GeV/c
	protected double p;
	
	//polar angle in degrees
	protected double theta;
	
	//azimuthal angle in degrees;
	protected double phi;

	
	/**
	 * Create a container for the swim results
	 * @param dim the dimension of the system (probably 6)
	 * @param saveTrajectory if true, we will save the trajectory
	 */
	public AdaptiveSwimResult(int dim, boolean saveTrajectory) {
		_uf = new double[dim];
		
		if (saveTrajectory) {
			_trajectory = new AdaptiveSwimTrajectory();
		}
	}
	
	/**
	 * Get the trajectory
	 * @return the trajectory (might be <code>null</code>
	 */
	public AdaptiveSwimTrajectory getTrajectory() {
		return _trajectory;
	}
	
	/**
	 * Get the final state vector, usually [x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters
	 * @return the final state vector
	 */
	public double[] getUf() {
		return _uf;
	}

	/**
	 * Set the final state vector, usually [x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters
	 * @param uf the final state vector
	 */
	public void setUf(double[] uf) {
		_uf = uf;
	}


	/**
	 * Get the number of steps of the swim
	 * @return the number of steps
	 */
	public int getNStep() {
		return _nStep;
	}


	/**
	 * Set the number of steps of the swim
	 * @param nStep the number of steps
	 */
	public void setNStep(int nStep) {
		_nStep = nStep;
	}


	/**
	 * Get the final path length of the swim
	 * @return the final path length in meters
	 */
	public double getFinalS() {
		return _finalS;
	}


	/**
	 * Set the final path length of the swim
	 * @param finalS the final path length in meters
	 */
	public void setFinalS(double finalS) {
		_finalS = finalS;
	}

	/**
	 * Set the status of the swim
	 * @param status the status of the swim
	 */
	public void setStatus(int status) {
		_status = status;
	}
	
	/**
	 * Get the status of the swim
	 * @return the status
	 */
	public int getStatus() {
		return _status;
	}

	/**
	 * Get the integer charge
	 * @return the integer charge
	 */
	public int getQ() {
		return q;
	}

	/**
	 * Get the initial x coordinate
	 * @return the initial x coordinate in meters
	 */
	public double getXo() {
		return xo;
	}

	/**
	 * Get the initial x coordinate
	 * @return the initial y coordinate in meters
	 */
	public double getYo() {
		return yo;
	}

	/**
	 * Get the initial x coordinate
	 * @return the initial z coordinate in meters
	 */
	public double getZo() {
		return zo;
	}

	/**
	 * Get the initial momentum
	 * @return the initial momentum in GeV/c
	 */
	public double getP() {
		return p;
	}

	/**
	 *  Get the nitial polar angle
	 * @return the initial polar angle in degrees
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 *  Get the nitial azimuthal angle
	 * @return the initial azimuthal angle in degrees
	 */
	public double getPhi() {
		return phi;
	}
	
	/**
	 * Get a string representing the initial conditions.
	 * @return a string representing the initial conditions.
	 */
	public String initialConditionString() {
		return String.format("vertex(m): [%9.6e, %9.6e, %9.6e]  p(Gev/C): %9.6e  theta(deg) %7.3f  phi(deg) %7.3f"
				, xo, yo, zo, p, theta, phi);
	}

}
