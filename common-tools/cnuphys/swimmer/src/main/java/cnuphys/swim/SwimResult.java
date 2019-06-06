package cnuphys.swim;

public class SwimResult {
	
	/** The swim was a success */
	public static final int SWIM_SUCCESS = 0;
	
	/** A target, such as a target rho or z, was not reached
	 * before the swim was stopped for some other reason
	 */
	public static final int SWIM_TARGET_MISSED = -1;
	
	/**
	 * A swim was requested for a particle with extremely low
	 * momentum
	 */
	public static final int SWIM_BELOW_MIN_P = -2;
	
	//the final state vector
	private double[] _uf;
	
	//the number of integration steps
	private int _nStep;
	
	//the final path length
	private double _finalS;
	
	//a status, one of the class constants
	private int _status;
	
	/**
	 * Create a container for the swim results
	 * @param dim the dimension of the system (probably 6)
	 */
	public SwimResult(int dim) {
		_uf = new double[dim];
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
