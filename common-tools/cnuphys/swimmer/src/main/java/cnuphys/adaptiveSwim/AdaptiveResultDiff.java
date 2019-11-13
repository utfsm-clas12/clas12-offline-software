package cnuphys.adaptiveSwim;

public class AdaptiveResultDiff {

	/**
	 * Used for debugging, to compare the difference between two swims
	 */

	//the two results
	public AdaptiveSwimResult result1;
	public AdaptiveSwimResult result2;

	private double _diff[] = new double[6];

	//final absolute position and momentum (actually t) differences
	private double _finalAbsRDiff;
	private double _finalAbsPDiff;

	//diff in final path length
	private double _sDiff;

	public AdaptiveResultDiff(AdaptiveSwimResult res1, AdaptiveSwimResult res2) {
		result1 = res1;
		result2 = res2;

		_sDiff = result2.getFinalS() - result1.getFinalS();

		double uf1[] = res1.getUf();
		double uf2[] = res1.getUf();

		for (int i = 0; i < 6; i++) {
			_diff[i] = uf2[i] - uf1[i];

			if (i < 3) {
				_finalAbsRDiff += (_diff[i] * _diff[i]);
			} else {
				_finalAbsPDiff += (_diff[i] * _diff[i]);
			}
		}

		_finalAbsRDiff = Math.sqrt(_finalAbsRDiff);
		_finalAbsPDiff = Math.sqrt(_finalAbsPDiff);
	}

	/**
	 * Get the signed difference between the final path lengths (2-1)
	 * 
	 * @return the signed difference between the final path lengths
	 */
	public double getSFinalDiff() {
		return _sDiff;
	}

	/**
	 * Get the signed difference in the final state vectors 2 - 1
	 * 
	 * @return the signed difference in the final state vectors
	 */
	public double[] getDiff() {
		return _diff;
	}

	public double getFinalAbsPositionDiff() {
		return _finalAbsRDiff;
	}

	public double getFinalAbsMomentumDiff() {
		return _finalAbsPDiff;
	}
	

	/**
	 * Get the signed final difference in z, z2 - z1
	 * @return the signed final difference in z
	 */
	public double getFinalZDiff() {
		return _diff[2];
	}
}
