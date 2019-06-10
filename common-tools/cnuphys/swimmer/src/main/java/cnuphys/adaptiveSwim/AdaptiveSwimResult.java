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

}
