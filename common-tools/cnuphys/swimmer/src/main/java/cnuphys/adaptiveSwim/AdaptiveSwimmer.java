package cnuphys.adaptiveSwim;

import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.rk4.IDerivative;
import cnuphys.rk4.IRkListener;
import cnuphys.swim.DefaultDerivative;
import cnuphys.swim.SwimTrajectory;

/**
 * A swimmer for adaptive stepsize integrators. These swimmers are not thread safe. Every thread that needs an
 * AdaptiveSwimmer should create its own.
 * 
 * @author heddle
 *
 */
public class AdaptiveSwimmer {
	
	//result status values
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
	
	
	
	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	//tolerance when swimmimg to a max path length
	public static final double SMAX_TOLERANCE = 1.0e-4;  //meters

	//step size limits in meters
	private static final double _minStepSize = 1.0e-5; // meters
	private static final double _maxStepSize = 0.4; //meters


	// The magnetic field probe.
	// NOTE: the method of interest in FieldProbe takes a position in cm
	// and returns a field in kG.This swim package works in SI (meters and
	// Tesla.) So care has to be taken when using the field object
	
	private FieldProbe _probe;

	/**
	 * Create a swimmer using the current active field
	 */
	public AdaptiveSwimmer() {
		// make a probe using the current active field
		_probe = FieldProbe.factory();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * 
	 * @param magneticField the magnetic field
	 */
	public AdaptiveSwimmer(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * 
	 * @param magneticField the magnetic field
	 */
	public AdaptiveSwimmer(IMagField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}
	

	/**
	 * Swim using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetRho the target rho in meters
	 * @param accuracy the requested accuracy for the target rho in meters
	 * @param s0 the initial value of the independent variable (pathlength) in meters.
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimRho(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double targetRho, final double accuracy, final double s0, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		//the dimension for this swimmer is 6
		int nDim = 6;
		
		//running stepsize
		double h = h0;
		
		//clear old trajectory
		if (result.getTrajectory() != null) {
			result.getTrajectory().clear();
		}
		
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double px = sinTheta*Math.cos(phiRad); //px/p
		double py = sinTheta*Math.sin(phiRad); //py/p
		double pz = Math.cos(thetaRad); //pz/p
		
		// set uf (int the result container) to the starting state vector
		double uf[] = result.getUf();
		uf[0] = xo;
		uf[1] = yo;
		uf[2] = xo;
		uf[3] = px;
		uf[4] = py;
		uf[5] = pz;

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum fixed rho swim");
			result.setNStep(0);
			result.setFinalS(0);
			
			//give this a result status of -2
			result.setStatus(SWIM_BELOW_MIN_P);
			return;
		}
		
		//cutoff value of s with tolerance 
		double sCutoff = sf - SMAX_TOLERANCE;
		
		double del = Double.POSITIVE_INFINITY;
		int maxtry = 11;
		int count = 0;
		double sCurrent = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target rho or if the
		//pathlength reaches sf
		AdaptiveRhoStopper stopper = new AdaptiveRhoStopper(uf, targetRho, accuracy);

		//use a half-step advancer
		RK4HalfStepAdvance advancer = new RK4HalfStepAdvance(6);

		while ((count < maxtry) && (del > accuracy)) {
			
			uf = result.getUf();

			double rhoCurr = FastMath.hypot(uf[0], uf[1]);
			
			stopper.set(sCurrent, sf, rhoCurr);
			
			if ((sCurrent + h) > sf) {
				h = (sf-sCurrent)/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target rho
			
			try {
				ns += driver(uf, sf - sCurrent, h, deriv, stopper, null, advancer, eps, uf, result.getTrajectory());
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
			
			System.arraycopy(stopper.getFinalU(), 0, result.getUf(), 0, nDim);
			
			sCurrent = stopper.getFinalS();
			if ((sCurrent) > sCutoff) {
				break;
			}
			
			double rholast = FastMath.hypot(result.getUf()[0], result.getUf()[1]);
			del = Math.abs(rholast - targetRho);
						
			count++;
			h = Math.min(h, (sf-sCurrent)/4);
		}
		
		
		result.setFinalS(sCurrent);
		result.setUf(uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}
	
	
	/**
	 * Driver that uses the RungeKutta advance with an adaptive step size
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next step
	 * has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, px/p, py/p, pz/p and the independent
	 * variable is time.
	 * 
	 * @param uo           initial values. Probably something like (xo, yo, zo, vxo,
	 *                     vyo, vzo).
	 * @param sf           the maximum value of the independent variable.
	 * @param h0           the step size at the start
	 * @param deriv        the derivative computer (interface). This is where the
	 *                     problem specificity resides.
	 * @param stopper      if not <code>null</code> will be used to exit the
	 *                     integration early because some condition has been
	 *                     reached.
	 * @param listener     listens for each step
	 * @param advancer     takes the next step
	 * @param eps          sort of relative tolerance (e.g., 1.0e-6)
	 * @param uf           will hold final state vector
	 *                     
	 * @return the number of steps used.
	 * @throw AdaptiveSwimException
	 */
	private int driver(double uo[], final double sf, double h, IDerivative deriv, IAdaptiveStopper stopper,
			IRkListener listener, IAdaptiveAdvance advancer, double eps, double uf[], AdaptiveSwimTrajectory trajectory) throws AdaptiveSwimException {
		
		// the dimensionality of the problem. E.., 6 if (x, y, z, vx, vy, vz)
		int nDim = uo.length;
		
		//if traj not null, add the first point
		
		double s0 = (trajectory == null) ? 0 : trajectory.getTotalS();
		if ((trajectory != null) && trajectory.isEmpty()) {
			trajectory.add(uo, 0);
		}
		
		//running val of independent variable
		double s = 0;
				
		// ut is the running value of the state vector,
		// typically [x, y, z, px/p, py/p, pz/p]
		double ut[] = new double[nDim];
		
		//du is for derivatives
		double du[] = new double[nDim];

		//init the final value at the start value
		System.arraycopy(uo, 0, uf, 0, nDim);
		
		int nstep = 0;
				
		AdaptiveStepResult result = new AdaptiveStepResult();
		
		while (s < sf) {
			
			System.arraycopy(uf, 0, ut, 0, nDim);

			//compute derivs at current step
			deriv.derivative(s, ut, du);
			advancer.advance(s, ut, du, h, deriv, uf, eps, result);
			
			h = result.getHNew();
			s = result.getSNew();
			
			
			if (Math.abs(h) < _minStepSize) {
				h = _minStepSize;
				System.err.println("Stepsize TOO SMALL: " + h);
//				throw (new AdaptiveSwimException("Step size too small in AdaptiveSwim driver"));
			}
			else {
				nstep++;
				if (trajectory != null) {
					trajectory.add(uf, s0 + s);
				}
				
				// someone listening?
				if (listener != null) {
					listener.nextStep(s, uf, h);
				}
				
				if (stopper.stopIntegration(s, uf)) {
					return nstep;
				}


			}

		}
		
		return nstep;
	}


	//sign function
	private int sign(double v) {
		if (v > 0) {
			return 1;
		}
		else if (v < 0) {
			return -1;
		}
		return 0;
	}
	

}
