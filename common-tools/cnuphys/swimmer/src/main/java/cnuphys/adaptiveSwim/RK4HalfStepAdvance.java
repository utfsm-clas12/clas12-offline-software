package cnuphys.adaptiveSwim;

import cnuphys.rk4.IDerivative;

/**
 * This is a 4th order RungeKutta advancer
 * @author heddle
 *
 */
public class RK4HalfStepAdvance implements IAdaptiveAdvance {
	
	//a safety fudge factor
	private static final double _safety = 0.95;
	
	//power used when the step should grow
	private static final double _pgrow = -0.20;
	
	//power used when the step should shrink
	private static final double _pshrink = -0.25;
	
	//for error control
	private static final double _errControl = 1.0e-4;
	
	//to make a cirrection that gives us 5th order accuracy
	private static final double _correctFifth = 1. / 15;
	
	//a tiny number
	private static final double _tiny = 1.0e-30;
	
	//the dimension of the problem (length of state vector)
	private int _nDim;
	
	//some work arrays
//	private double _usave[];
//	private double _dusave[];
	private double _utemp[];
	private double _dutemp[];
	private double _uscale[];


	/**
	 * Create a RK4 half stepper
	 * @param nDim the dimension of the problem (length of state vector)
	 */
	public RK4HalfStepAdvance(int nDim) {
		_nDim = nDim;
		_utemp = new double[_nDim];
		_dutemp = new double[_nDim];
		_uscale = new double[_nDim];
	}
	
	@Override
	public void advance(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf, double eps,
			AdaptiveStepResult result) {

		boolean done = false;


		while (!done) {

			// almost relative error, but with safety when values of u are small
			for (int i = 0; i < _nDim; i++) {
				_uscale[i] = Math.abs(u[i]) + Math.abs(h * du[i]) + _tiny;
			}

			// advance two half steps after which uf will hold the value of 
			// which, if our steps size is acceptable, this will be our result
			double h2 = h / 2;
			double smid = s + h2;
			SwimUtilities.singleRK4Step(s, u, du, h2, deriv, _utemp);
			deriv.derivative(smid, _utemp, _dutemp);
			SwimUtilities.singleRK4Step(smid, _utemp, _dutemp, h2, deriv, uf);

			// take the full step
			SwimUtilities.singleRK4Step(s, u, du, h, deriv, _utemp);

			// compute the maximum error
			double errMax = 0;
			for (int i = 0; i < _nDim; i++) {
				_utemp[i] = uf[i] - _utemp[i];
				errMax = Math.max(errMax, Math.abs(_utemp[i] / _uscale[i]));
			}

			// scale based on tolerance in eps
			errMax = errMax / eps;

			if (errMax > 1) {
				//get smaller h, then try again since done = false
				
				double shrinkFact = _safety * Math.pow(errMax, _pshrink);
				
				//no more than a factor of 4
				shrinkFact = Math.max(shrinkFact, 0.25);
				h = h * shrinkFact;
			} else { // can grow
				double hnew;
				if (errMax > _errControl) {
					double growFact = _safety * Math.pow(errMax, _pgrow);
					hnew = h * growFact;
//					hnew = Math.max(h, _safety * h * growFact);
				} else {
					hnew = 4 * h;
				}
				
				result.setHNew(hnew);
				result.setHUsed(h);
				result.setSNew(s + h);
				done = true;
			}

		} // !done

		// mop up 5th order truncation error
		//so result is actuallky 5th order
		for (int i = 0; i < _nDim; i++) {
			uf[i] = uf[i] + _utemp[i] * _correctFifth;
		}
	} // end advance
}
