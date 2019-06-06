package cnuphys.adaptiveSwim;

import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IDerivative;

public class SwimUtilities {

	

	/**
	 * Take a single step using basic fourth order RK
	 * 
	 * @param s     the independent variable
	 * @param u     the current state vector
	 * @param du    the current derivatives
	 * @param h     the step size
	 * @param deriv can compute the rhs of the diffy q
	 * @param uf    the state vector after the step
	 */
	public static void singleRK4Step(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf) {

		int nDim = u.length;

		// note that du (input) is k1
		double k1[] = du; // the current derivatives

		double k2[] = new double[nDim];
		double k3[] = new double[nDim];
		double k4[] = new double[nDim];
		double utemp[] = new double[nDim];

		double hh = h * 0.5; // half step
		double h6 = h / 6.0;

		// advance t to mid point
		double sMid = s + hh;

		// first step: initial derivs to midpoint
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + hh * k1[i];
		}
		deriv.derivative(sMid, utemp, k2);

		// 2nd step (like 1st, but use midpoint just computed derivs dyt)
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + hh * k2[i];
		}
		deriv.derivative(sMid, utemp, k3);

		// third (full) step
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + h * k3[i];
		}
		deriv.derivative(s + h, utemp, k4);

		for (int i = 0; i < nDim; i++) {
			uf[i] = u[i] + h6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
		}

	}
	
	
	/**
	 * Take a single step using a Butcher tableau
	 * 
	 * @param s     the independent variable
	 * @param u     the current state vector
	 * @param du    the current derivatives
	 * @param h     the step size
	 * @param deriv can compute the rhs of the diffy q
	 * @param uf    the state vector after the step
	 * @param error the error estimate
	 * @param tableau the Butcher tableau
	 */
	public static void singleButcherStep(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf,
			double[] error, ButcherTableau tableau) {
		
		int nDim = u.length;
		int numStage = tableau.getS();

		double utemp[] = new double[nDim];
		double k[][] = new double[numStage + 1][];
		k[0] = null; // not used

		// k1 is just h*du
		k[1] = new double[nDim];
		for (int i = 0; i < nDim; i++) {
			k[1][i] = h * du[i];
		}

		// fill the numStage k vectors
		for (int stage = 2; stage <= numStage; stage++) {
			k[stage] =new double[nDim];

			double ts = s + tableau.c(stage);
			for (int i = 0; i < nDim; i++) {
				utemp[i] = u[i];
				for (int ss = 1; ss < stage; ss++) {
					utemp[i] += tableau.a(stage, ss) * k[ss][i];
				}
			}
			deriv.derivative(ts, utemp, k[stage]);
			for (int i = 0; i < nDim; i++) {
				k[stage][i] *= h;
			}
		}
		
		for (int i = 0; i < nDim; i++) {
			double sum = 0.0;
			for (int stage = 1; stage <= numStage; stage++) {
				sum += tableau.b(stage) * k[stage][i];
			}
			uf[i] = u[i] + sum;
		}

		// compute error?
		if (tableau.isAugmented() && (error != null)) {

			// absolute error
			for (int i = 0; i < nDim; i++) {
				error[i] = 0.0;
				// error diff 4th and 5th order
				for (int stage = 1; stage <= numStage; stage++) {
					error[i] += tableau.bdiff(stage) * k[stage][i]; // abs error
				}
			}

			// relative error
			// for (int i = 0; i < nDim; i++) {
			// double sum = 0.0;
			// for (int s = 1; s <= numStage; s++) {
			// sum += tableau.bstar(s)*k[s][i];
			// }
			// double ystar = y[i] + sum;
			// error[i] = relativeDiff(yout[i], ystar);
			// }

			// for (int i = 0; i < nDim; i++) {
			// System.out.print(String.format("[%-12.5e] ", error[i]));
			// }
			// System.out.println();

		}


	}
}
