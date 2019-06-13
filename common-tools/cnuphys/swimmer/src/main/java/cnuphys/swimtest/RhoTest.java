package cnuphys.swimtest;

import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Vector;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.Swimmer;

public class RhoTest {

	/** Test swimming to a fixed rho (cylinder) */
	public static void rhoTest() {
		
		long seed = 9459363;
		System.out.println("TEST swimming to a fixed rho (cylinder)");
//		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);
		
		double xo = 0; // m
		double yo = 0; // m
		double zo = 0; // m
		double stepsizeAdaptive = 0.01; //starting
		double stepsizeUniform = 5e-04;  //m
		
		double maxPathLength = 3; //m
		double accuracy = 5e-3; //m
		double rhoTarg = 0.30;  //m 
		double eps = 1.0e-6;
		
		int num = 100000;
//		num = 1;
		int n0= 0;
		
		AdaptiveSwimResult uniform = new AdaptiveSwimResult(6, false);
		AdaptiveSwimResult adaptive = new AdaptiveSwimResult(6, false);
		AdaptiveSwimResult newadaptive = new AdaptiveSwimResult(6, false);
		AdaptiveSwimResult newadaptiveplus = new AdaptiveSwimResult(6, true);
		AdaptiveSwimResult newadaptiveS = new AdaptiveSwimResult(6, true);
		
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
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		int badStatusCount;
		
		int nsMax = 0;
		
		
		long nStepTotal = 0;
		
//		 adaptive step
//		try {
//			
//			sum = 0;
//			badStatusCount = 0;
//			delMax = Double.NEGATIVE_INFINITY;
//			time = System.currentTimeMillis();
//
//			
//			for (int i = n0; i < num; i++) {
//				swimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], rhoTarg, accuracy, maxPathLength,
//						stepsizeAdaptive, Swimmer.CLAS_Tolerance, adaptive);
//
//				rhof = Math.hypot(adaptive.getUf()[0], adaptive.getUf()[1]);
//				double dd = Math.abs(rhoTarg - rhof);
//				delMax = Math.max(delMax, dd);
//				sum += dd;
//				
//				adaptStatus[i] = adaptive.getStatus();
//				nStepTotal += adaptive.getNStep();
//				
//				nsMax = Math.max(nsMax, adaptive.getNStep());
//				
//				if (adaptive.getStatus() != 0) {
//					badStatusCount += 1;
//	//				num = i+1;
//				}
//			}
//			
//			time = System.currentTimeMillis() - time;
//			SwimTest.printSummary("Fixed Rho,  Adaptive step size", adaptive.getNStep(), p[num - 1],
//					adaptive.getUf(), null);
//			System.out.println(String.format("Adaptive time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
//			System.out.println("Adaptive Avg NS = " +  (int)(((double)nStepTotal)/num) + "   MAX NS: " + nsMax);
//			System.out.println("Adaptive Path length = " + adaptive.getFinalS() + " m\n\n");
//
//		} catch (RungeKuttaException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		// NEW adaptive step no traj
//		try {
//			nsMax = 0;
//			sum = 0;
//			badStatusCount = 0;
//			delMax = Double.NEGATIVE_INFINITY;
//			nStepTotal = 0;
//			
//			time = System.currentTimeMillis();
//			for (int i = n0; i < num; i++) {
//				
//				adaptiveSwimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], rhoTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, newadaptive);
//
//				rhof = Math.hypot(newadaptive.getUf()[0], newadaptive.getUf()[1]);
//				double dd = Math.abs(rhoTarg - rhof);
//				delMax = Math.max(delMax, dd);
//				sum += dd;
//				
//				if (newadaptive.getStatus() != adaptStatus[i]) {
//					System.out.println("Adaptive v. NEW Adaptive Status differs for i = " + i + "     adaptiveStat = " + adaptStatus[i]
//							+ "    NEW adaptive status = " + newadaptive.getStatus());
//				}
//				
//				nStepTotal += newadaptive.getNStep();
//				nsMax = Math.max(nsMax, newadaptive.getNStep());
//				
//				if (newadaptive.getStatus() != 0) {
//					badStatusCount += 1;
//				}
//			}
//			
//			time = System.currentTimeMillis() - time;
//			SwimTest.printSummary("Fixed Rho,  NEW Adaptive step size (NO TRAJ)", newadaptive.getNStep(), p[num - 1],
//					newadaptive.getUf(), null);
//			System.out.println(String.format("NEW Adaptive time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
//			System.out.println("NEW Adaptive Avg NS = " +  (int)(((double)nStepTotal)/num) + "   MAX NS: " + nsMax);
//			System.out.println("NEW Adaptive Path length = " + newadaptive.getFinalS() + " m\n\n");
//
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//		
//		// NEW adaptive step with traj
//		try {
//			
//			sum = 0;
//			badStatusCount = 0;
//			delMax = Double.NEGATIVE_INFINITY;
//			nStepTotal = 0;
//			
//			time = System.currentTimeMillis();
//			for (int i = n0; i < num; i++) {
//				
//				adaptiveSwimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], rhoTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, newadaptiveplus);
//
//				rhof = Math.hypot(newadaptiveplus.getUf()[0], newadaptiveplus.getUf()[1]);
//				double dd = Math.abs(rhoTarg - rhof);
//				delMax = Math.max(delMax, dd);
//				sum += dd;
//				
////				if (newadaptive.getStatus() != adaptStatus[i]) {
////					System.out.println("Adaptive v. NEW Adaptive Status differs for i = " + i + "     adaptiveStat = " + adaptStatus[i]
////							+ "    NEW adaptive status = " + newadaptive.getStatus());
////				}
//				
//				nStepTotal += newadaptiveplus.getNStep();
//				if (newadaptiveplus.getStatus() != 0) {
//					badStatusCount += 1;
//				}
//			}
//			
//			time = System.currentTimeMillis() - time;
//			SwimTest.printSummary("Fixed Rho,  NEW Adaptive step size (WITH TRAJ)", newadaptiveplus.getNStep(), p[num - 1],
//					newadaptiveplus.getUf(), null);
//			newadaptiveplus.getTrajectory().dumpInCylindrical(System.out);
//			System.out.println(String.format("NEW Adaptive time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
//			System.out.println("NEW Adaptive Avg NS = " +  (int)(((double)nStepTotal)/num));
//			System.out.println("NEW Adaptive Path length = " + newadaptiveplus.getFinalS() + " m\n\n");
//
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//
//
//
//		// uniform step
//		time = System.currentTimeMillis();
//
//		sum = 0;
//		badStatusCount = 0;
//		nStepTotal = 0;
//		delMax = Double.NEGATIVE_INFINITY;
//		for (int i = n0; i < num; i++) {
//			swimmer.swimRho(charge[i], xo, yo, zo, p[i], theta[i], phi[i], rhoTarg, accuracy, maxPathLength, stepsizeUniform, uniform);			
//			rhof = Math.hypot(uniform.getUf()[0], uniform.getUf()[1]);
//			double dd = Math.abs(rhoTarg - rhof);
//			delMax = Math.max(delMax, dd);
//			sum += dd;
//			
//			if (uniform.getStatus() != adaptStatus[i]) {
//				System.out.println("Status differs for i = " + i + "     adaptiveStat = " + adaptStatus[i] + "    uniform status = " + uniform.getStatus());
//			}
//
//			nStepTotal += uniform.getNStep();
//			if (uniform.getStatus() != 0) {
//				badStatusCount += 1;
//			}
//		}
//		time = System.currentTimeMillis() - time;
//		SwimTest.printSummary("Fixed Rho,  Uniform step size", uniform.getNStep(), p[num - 1],
//				uniform.getUf(), null);
//		System.out.println(String.format("Uniform time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d", ((double)time)/1000., sum/num, delMax, badStatusCount));
//		System.out.println("Uniform Avg NS = " +  (int)(((double)nStepTotal)/num));
//		System.out.println("Uniform Path length = " + uniform.getFinalS() + " m\n\n");
		
		
		
//		System.out.println("\nSet up backwards swim test");
		xo = 0;
		yo = 0;
		zo = 0;
		double zTarg = 5.75; //m
		double thetV = 15;
		double phiV = 0;
		double pV = 2;
		int Q = 1;
		accuracy = 1.0e-5; //m
		maxPathLength = 8; //m
//		
//		try {
//			adaptiveSwimmer.swimZ(Q, xo, yo, zo, pV, thetV, phiV, zTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, newadaptiveplus);
//			SwimTest.printSummary("FORWARDS Fixed Rho,  NEW Adaptive step size (WITH TRAJ)", newadaptiveplus.getNStep(), p[num - 1],
//					newadaptiveplus.getUf(), null);
////			newadaptiveplus.getTrajectory().dumpInCylindrical(System.out);
//			System.out.println("NEW Adaptive Path length = " + newadaptiveplus.getFinalS() + " m\n\n");
//			
//			
//			double txf = newadaptiveplus.getUf()[3];
//			double tyf = newadaptiveplus.getUf()[4];
//			double tzf = newadaptiveplus.getUf()[5];
//			
//			double phiFinal = FastMath.atan2Deg(tyf, txf);
//			double thetaFinal = FastMath.acos2Deg(tzf);
//			
//			System.out.println("Final angles   theta = " + thetaFinal + "   phi = " + phiFinal);
//			
//			txf *= -1;
//			tyf *= -1;
//			tzf *= -1;
//			
//			phiFinal = FastMath.atan2Deg(tyf, txf);
//			thetaFinal = FastMath.acos2Deg(tzf);
//			System.out.println("Swim backward angles   theta = " + thetaFinal + "   phi = " + phiFinal);
//
//			
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//
//		
//		xo = 2.1906418; // m
//		yo = 0; // m
//		zo = 5.7499933; // m
//		maxPathLength = 8; //m
//		thetV = 150.48254287687868;
//		phiV = -180;
//		pV = 2;
//		
//		//To make a particle retrace you must reverse its velocity AND change its sign. Think about
//		//uniform field and particle moving in a circle. If you flip v (but not q) it will not retrace
//		//the circle, it will be on a different circle
//		//Q --> -Q
//		
//	//	rhoTarg = 0.3033;  //m 
//		
//		zTarg = 0;
//		
//		System.out.println("\nBACKWARDS SWIM to RHO = " + rhoTarg + " m");
//		
//		try {
//			adaptiveSwimmer.swimZ(-Q, xo, yo, zo, pV, thetV, phiV, zTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, newadaptiveplus);
//			SwimTest.printSummary("BACKWARDS Fixed Rho,  NEW Adaptive step size (WITH TRAJ)", newadaptiveplus.getNStep(), pV,
//					newadaptiveplus.getUf(), null);
////			newadaptiveplus.getTrajectory().dumpInCylindrical(System.out);
//			System.out.println("NEW Adaptive Path length = " + newadaptiveplus.getFinalS() + " m\n\n");
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//
//		
		//test basic pathlength swimmer to be used by ced
		
		xo = 0;
		yo = 0;
		zo = 0;
		Q = 1;
		maxPathLength = 5.001537404349655; 
		thetV = 15;
		phiV = 0;
		try {
			adaptiveSwimmer.swim(Q, xo, yo, zo, pV, thetV, phiV, maxPathLength, stepsizeAdaptive, eps, newadaptiveS);
			SwimTest.printSummary("Base S swimmer,  NEW Adaptive step size (WITH TRAJ)", newadaptiveS.getNStep(), pV,
					newadaptiveS.getUf(), null);
			System.out.println("Adaptive S Path length = " + newadaptiveS.getFinalS() + " m\n\n");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

		
		
		System.out.println("swim to a plane");
		xo = 0;
		yo = 0;
		zo = 0;
		thetV = 15;
		phiV = 0;
		pV = 2;
		Q = 1;
		accuracy = 1.0e-5; //m
		maxPathLength = 8; //m

		//create a plane like front of dc region 3
		double r = 4.9014; //m
		double x1 = r*Math.sin(Math.toRadians(25));
		double y1 = 0;
		double z1 = r*Math.cos(Math.toRadians(25));
		Point p1 = new Point(x1, y1, z1);
		Vector v = new Vector(-x1, 0, -z1);
		Plane plane = new Plane(v, p1);
		
		try {
			adaptiveSwimmer.swimPlane(Q, xo, yo, zo, pV, thetV, phiV, plane, accuracy, maxPathLength, stepsizeAdaptive, eps, newadaptiveS);
			SwimTest.printSummary("Swim to plane,  NEW Adaptive step size (WITH TRAJ)", newadaptiveS.getNStep(), pV,
					newadaptiveS.getUf(), null);
			System.out.println("Swim to Plane S Path length = " + newadaptiveS.getFinalS() + " m\n\n");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}

}
