package cnuphys.swimtest;

import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwim;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimResult;
import cnuphys.swim.Swimmer;

public class RhoTest {

	/** Test swimming to a fixed rho (cylinder) */
	public static void rhoTest() {
		
		long seed = 5459363;
		System.out.println("TEST swimming to a fixed rho (cylinder)");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);
		
		double xo = 0; // m
		double yo = 0; // m
		double zo = 0; // m
		double stepsize = 5e-04;  //m
		
		double maxPathLength = 3; //m
		double accuracy = 5e-3; //m
		double fixedRho = 0.26;  //m 
		
		int num = 10000;
//		num = 516;
		
		SwimResult uniform = new SwimResult(6);
		SwimResult adaptive = new SwimResult(6);
		SwimResult newadaptive = new SwimResult(6);
		
		//generate some random initial conditions
		Random rand = new Random(seed);
		
		int charge[] = new int[num];
		double p[] = new double[num];
		double theta[] = new double[num];
		double phi[] = new double[num];
		int adaptStatus[] = new int[num];
		
		for (int i = 0; i < num; i++) {
			charge[i] = (rand.nextDouble() < 0.5) ? -1 : 1;
//			p[i] = 1 + 8*rand.nextDouble();
//			theta[i] = 45 + 25*rand.nextDouble();
//			phi[i] = 360*rand.nextDouble();
			p[i] = 0.25 + 0.75*rand.nextDouble();
			theta[i] = 40 + 30*rand.nextDouble();
			phi[i] = 360*rand.nextDouble();
		}
		
		
		long time;
        double rhof;
		double sum;
		double delMax;
		Swimmer swimmer = new Swimmer();
		int badStatusCount;
		
		// adaptive step
		try {
			
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			time = System.currentTimeMillis();

			for (int i = 0; i < num; i++) {
				swimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], fixedRho, accuracy, maxPathLength,
						stepsize, Swimmer.CLAS_Tolerance, adaptive);

				rhof = Math.hypot(adaptive.getUf()[0], adaptive.getUf()[1]);
				double dd = Math.abs(fixedRho - rhof);
				delMax = Math.max(delMax, dd);
				sum += dd;
				
				adaptStatus[i] = adaptive.getStatus();
				
				if (adaptive.getStatus() != 0) {
					badStatusCount += 1;
	//				num = i+1;
				}
			}
			
			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Fixed Rho,  Adaptive step size", adaptive.getNStep(), p[num - 1],
					adaptive.getUf(), null);
			System.out.println(String.format("Adaptive time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
			System.out.println("Adaptive Path length = " + adaptive.getFinalS() + " m\n\n");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
		
		// NEW adaptive step
		try {
			
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			time = System.currentTimeMillis();

			for (int i = 0; i < num; i++) {
				AdaptiveSwim.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], fixedRho, accuracy, 0, maxPathLength, stepsize, 1.0e-6, newadaptive);
				swimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], fixedRho, accuracy, maxPathLength,
						stepsize, Swimmer.CLAS_Tolerance, adaptive);

				rhof = Math.hypot(adaptive.getUf()[0], adaptive.getUf()[1]);
				double dd = Math.abs(fixedRho - rhof);
				delMax = Math.max(delMax, dd);
				sum += dd;
				
				adaptStatus[i] = adaptive.getStatus();
				
				if (adaptive.getStatus() != 0) {
					badStatusCount += 1;
	//				num = i+1;
				}
			}
			
			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Fixed Rho,  Adaptive step size", adaptive.getNStep(), p[num - 1],
					adaptive.getUf(), null);
			System.out.println(String.format("Adaptive time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
			System.out.println("Adaptive Path length = " + adaptive.getFinalS() + " m\n\n");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}


		// uniform step
		time = System.currentTimeMillis();

		sum = 0;
		badStatusCount = 0;

		delMax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < num; i++) {
			swimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], fixedRho, accuracy, maxPathLength, stepsize, uniform);			
			rhof = Math.hypot(uniform.getUf()[0], uniform.getUf()[1]);
			double dd = Math.abs(fixedRho - rhof);
			delMax = Math.max(delMax, dd);
			sum += dd;
			
			if (uniform.getStatus() != adaptStatus[i]) {
				System.out.println("Status differs for i = " + i + "     adaptiveStat = " + adaptStatus[i] + "    uniform status = " + uniform.getStatus());
			}

			if (uniform.getStatus() != 0) {
				badStatusCount += 1;
			}
		}
		time = System.currentTimeMillis() - time;
		SwimTest.printSummary("Fixed Rho,  Uniform step size", uniform.getNStep(), p[num - 1],
				uniform.getUf(), null);
		System.out.println(String.format("Uniform time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
		System.out.println("Uniform Path length = " + uniform.getFinalS() + " m\n\n");
		
		
		
		
	}

}
