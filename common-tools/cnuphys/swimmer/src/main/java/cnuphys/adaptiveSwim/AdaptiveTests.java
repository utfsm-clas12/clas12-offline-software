package cnuphys.adaptiveSwim;

import java.util.Random;

import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Line;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Vector;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.Swimmer;
import cnuphys.swimtest.SwimTest;

public class AdaptiveTests {
	
	private static AdaptiveTests _instance;

	private static final double SMALL = 1.0e-8;

	public static void noStopperTest() {

		// test basic pathlength swimmer to be used by ced

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(6, true);

		double stepsizeAdaptive = 0.01; // starting
		double xo = 0;
		double yo = 0;
		double zo = 0;
		int Q = 1;
		double maxPathLength = 5.;
		double theta = 15;
		double phi = 0;
		double p = 2;
		double eps = 1.0e-6;

		try {
			adaptiveSwimmer.swim(Q, xo, yo, zo, p, theta, phi, maxPathLength, stepsizeAdaptive, eps, result);
			SwimTest.printSummary("Base S swimmer,  NEW Adaptive step size (WITH TRAJ)", result.getNStep(), p,
					result.getUf(), null);
			System.out.println("Adaptive S Path length = " + result.getFinalS() + " m\n\n");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}
	

	public static void lineTest() {
		Line targetLine = new Line(new Point(1, 0, 0), new Point(1, 0, 1));
		
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(6, true);

		double stepsizeAdaptive = 0.01; // starting
		double xo = 0;
		double yo = 0;
		double zo = 0;
		int Q = 1;
		double maxPathLength = 8.;
		double theta = 25;
		double phi = 0;
		double p = 1;
		double eps = 1.0e-6;
		double accuracy = 1.0e-5; //m


		try {
			adaptiveSwimmer.swimLine(Q, xo, yo, zo, p, theta, phi, targetLine, accuracy, maxPathLength, stepsizeAdaptive, eps, result);
			SwimTest.printSummary("Base S swimmer,  NEW Adaptive step size (WITH TRAJ)", result.getNStep(), p,
					result.getUf(), null);
			System.out.println("Adaptive S Path length = " + result.getFinalS() + " m\n\n");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}


	//RETRACE
	public static void retraceTest() {
		
		
		
		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 1000;
//		num = 1;
		int n0 = 0;

		int status[] = new int[num];

		InitVal[] ivals = getInitVals(rand, num, -1, false, 0., 0., 0., 0., 0., 0., 0.5, 5.0, 10., 25., -30., 30.);

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(6, true);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 8; // m
		double accuracy = 1e-4; // m
		double eps = 1.0e-6;


		// create a plane last layer reg 3
		double r = 5.3092; // m
		double x1 = r * Math.sin(Math.toRadians(25));
		double y1 = 0;
		double z1 = r * Math.cos(Math.toRadians(25));
		Point p1 = new Point(x1, y1, z1);
		Vector v = new Vector(-x1, 0, -z1);
		Plane plane = new Plane(v, p1);
		
		int goodCount = 0;

		try {
			
			InitVal revIv = createInitVal();
			
			InitVal iv = null;
			double[] uf = null;
			
			double sum = 0;
			double drMax = 0;
			int iMax = -1;
			
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				
				adaptiveSwimmer.swimPlane(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, plane, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
				uf = result.getUf();
				status[i] = result.getStatus();
				if (status[i] == AdaptiveSwimmer.SWIM_SUCCESS) {
					goodCount++;
					//try to swim back
					
					uf = result.getUf();
					
					double txf = uf[3];
					double tyf = uf[4];
					double tzf = uf[5];
					
					txf *= -1;
					tyf *= -1;
					tzf *= -1;
					
					revIv.charge = -iv.charge;
					revIv.p = iv.p;
					revIv.xo = uf[0];
					revIv.yo = uf[1];
					revIv.zo = uf[2];
					revIv.theta = FastMath.acos2Deg(tzf);
					revIv.phi = FastMath.atan2Deg(tyf, txf);
					
					double zTarg = 0;

					if (i == 711) {
						System.out.println("FORWARD " + iv);
						SwimTest.printSummary("FORWARD", result.getNStep(), iv.p,
								result.getUf(), null);
						System.out.println("FORWARD Path length = " + result.getFinalS() + " m\n\n");
					}
					
					
					adaptiveSwimmer.swimZ(revIv.charge, revIv.xo, revIv.yo, revIv.zo, revIv.p, revIv.theta, revIv.phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, result);

					double dr = FastMath.sqrt(uf[0]*uf[0] + uf[1]*uf[1] + uf[2]*uf[2]);
					
					if (i == 711) {
						System.out.println("BACKWARD " + revIv);
						SwimTest.printSummary("BACKWARD", result.getNStep(), revIv.p,
								result.getUf(), null);
						System.out.println("dr = " + dr);
						System.out.println("BACKWARD Path length = " + result.getFinalS() + " m\n\n");
					}

					sum += dr;
					
					if (dr > drMax) {
						drMax = dr;
						iMax = i;
					}
				}
				else {
					// System.out.println("Bad swim to plane for i = " + i + "  final pathlength = " + result.getFinalS());
				}
			}  //for

			System.out.println("average dr = " + (sum/goodCount) +   "     max dr: " + drMax + "   at i = " + iMax );

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}		
		
		
	
		
	}

	public static void zTest() {

	}

	public static void rhoTest() {

		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 100000;
//		num = 1;
		int n0 = 0;

		InitVal[] ivals = getInitVals(rand, num, 1, true, 0., 0., 0., 0., 0., 0., 0.25, 1.0, 40., 70., 0., 360.);

		System.out.println("TEST swimming to a fixed rho");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 3; // m
		double accuracy = 5e-3; // m
		double rhoTarg = 0.30; // m
		double eps = 1.0e-6;

		AdaptiveSwimResult adaptive = new AdaptiveSwimResult(6, false);
		AdaptiveSwimResult newadaptive = new AdaptiveSwimResult(6, false);

		// generate some random initial conditions

		int adaptStatus[] = new int[num];

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
		try {

			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			time = System.currentTimeMillis();

			InitVal iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];
				swimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, Swimmer.CLAS_Tolerance, adaptive);

				rhof = Math.hypot(adaptive.getUf()[0], adaptive.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);

				adaptStatus[i] = adaptive.getStatus();
				nStepTotal += adaptive.getNStep();
				

				nsMax = Math.max(nsMax, adaptive.getNStep());

				if (adaptive.getStatus() != AdaptiveSwimmer.SWIM_SUCCESS) {
					badStatusCount += 1;
				}
				else {
					sum += dd;
				}
			}

			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Fixed Rho,  Adaptive step size", adaptive.getNStep(), iv.p, adaptive.getUf(), null);
			System.out.println(
					String.format("Adaptive time: %-7.3f   avg good delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							((double) time) / 1000., sum / (num - badStatusCount), delMax, badStatusCount));
			System.out.println("Adaptive Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax);
			System.out.println("Adaptive Path length = " + adaptive.getFinalS() + " m\n\n");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// NEW adaptive step no traj
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitVal iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, newadaptive);

				rhof = Math.hypot(newadaptive.getUf()[0], newadaptive.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);
				

//				if (newadaptive.getStatus() != adaptStatus[i]) {
//					System.out.println("Adaptive v. NEW Adaptive Status differs for i = " + i + "     adaptiveStat = "
//							+ adaptStatus[i] + "    NEW adaptive status = " + newadaptive.getStatus());
//				}

				nStepTotal += newadaptive.getNStep();
				nsMax = Math.max(nsMax, newadaptive.getNStep());

				if (newadaptive.getStatus() != AdaptiveSwimmer.SWIM_SUCCESS) {
					badStatusCount += 1;
				}
				else {
					sum += dd;
				}
			}

			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Fixed Rho,  NEW Adaptive step size (NO TRAJ)", newadaptive.getNStep(), iv.p,
					newadaptive.getUf(), null);
			System.out.println(
					String.format("NEW Adaptive time: %-7.3f   avg good delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							((double) time) / 1000., sum / (num - badStatusCount), delMax, badStatusCount));
			System.out.println("NEW Adaptive Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax);
			System.out.println("NEW Adaptive Path length = " + newadaptive.getFinalS() + " m\n\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
	}

	//SWIM TO A PLANE
	public static void planeTest() {

		System.out.println("swim to a plane");
		
		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 1000;
//		num = 1;
		int n0 = 0;

		int status[] = new int[num];
		InitVal[] ivals = getInitVals(rand, num, -1, false, 0., 0., 0., 0., 0., 0., 0.5, 5.0, 10., 25., -30., 30.);

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(6, true);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 8; // m
		double accuracy = 1e-4; // m
		double eps = 1.0e-6;


		// create a plane last layer reg 3
		double r = 5.3092; // m
		double x1 = r * Math.sin(Math.toRadians(25));
		double y1 = 0;
		double z1 = r * Math.cos(Math.toRadians(25));
		Point p1 = new Point(x1, y1, z1);
		Vector v = new Vector(-x1, 0, -z1);
		Plane plane = new Plane(v, p1);
		

		try {
			
			InitVal iv = null;
			double[] uf = null;
			
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

//				if (i == 54) {
//					System.out.println();
//					System.out.println(iv);
//				}
				
				adaptiveSwimmer.swimPlane(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, plane, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
				uf = result.getUf();
				status[i] = result.getStatus();
				if (status[i] == AdaptiveSwimmer.SWIM_SUCCESS) {
					
				}
				else {
					System.out.println("Bad swim to plane for i = " + i + "  final pathlength = " + result.getFinalS());
				}


			}

			SwimTest.printSummary("Swim to plane,  NEW Adaptive step size (WITH TRAJ)", result.getNStep(), iv.p,
					result.getUf(), null);
			

			System.out.println(String.format("Distance to plane: %-8.6f m" , Math.abs(plane.distance(uf[0], uf[1], uf[2]))));
			System.out.println("Swim to Plane S Path length = " + result.getFinalS() + " m\n\n");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
	}

	public static void cylinderTest() {

		System.out.println("Cylinder around z axis should give us same result as rho swim.");
		

		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 10000;
	//	num = 1;
		int n0 = 0;

		InitVal[] ivals = getInitVals(rand, num, 1, true, 0., 0., 0., 0., 0., 0., 0.25, 1.0, 40., 70., 0., 360.);

		System.out.println("TEST swimming to a fixed rho");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 3; // m
		double accuracy = 5e-3; // m
		double rhoTarg = 0.30; // m
		double eps = 1.0e-6;
		
		Cylinder targCyl = new Cylinder(new Line(new Point(0, 0, 0), new Point(0, 0, 1)), rhoTarg);

		AdaptiveSwimResult rResult = new AdaptiveSwimResult(6, true);
		AdaptiveSwimResult cResult = new AdaptiveSwimResult(6, true);

		// generate some random initial conditions

		int adaptStatus[] = new int[num];

		long time;
		double rhof;
		double sum;
		double delMax;
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		int badStatusCount;

		int nsMax = 0;

		long nStepTotal = 0;


		// rho swim
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitVal iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, rResult);

				rhof = Math.hypot(rResult.getUf()[0], rResult.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);
				sum += dd;
				
				adaptStatus[i] = rResult.getStatus();

				nStepTotal += rResult.getNStep();
				nsMax = Math.max(nsMax, rResult.getNStep());

				if (rResult.getStatus() != 0) {
					badStatusCount += 1;
				}
			}

			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Fixed Rho", rResult.getNStep(), iv.p,
					rResult.getUf(), null);
			
			rResult.getTrajectory().dumpInCylindrical(System.out);
			System.out.println(
					String.format("Rho time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							((double) time) / 1000., sum / num, delMax, badStatusCount));
			System.out.println("Rho Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax);
			System.out.println("Rho Path length = " + rResult.getFinalS() + " m\n\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
		
		
		//cylinder swim
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitVal iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimCylinder(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, targCyl, accuracy,
						maxPathLength, stepsizeAdaptive, eps, cResult);

				double dd = targCyl.distance(cResult.getUf()[0], cResult.getUf()[1], cResult.getUf()[2]);
				dd = Math.abs(dd); //cyl dist can be neag if inside
				delMax = Math.max(delMax, dd);
				sum += dd;

				if (cResult.getStatus() != adaptStatus[i]) {
					System.out.println("Rho v. Cylinder Status differs for i = " + i + "     rho statust = "
							+ adaptStatus[i] + "   cylinder status = " + cResult.getStatus());
				}

				nStepTotal += cResult.getNStep();
				nsMax = Math.max(nsMax, cResult.getNStep());

				if (cResult.getStatus() != 0) {
					badStatusCount += 1;
				}
			}

			time = System.currentTimeMillis() - time;
			SwimTest.printSummary("Cylinder", cResult.getNStep(), iv.p,
					cResult.getUf(), null);
			cResult.getTrajectory().dumpInCylindrical(System.out);

			System.out.println(
					String.format("Cylinder time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							((double) time) / 1000., sum / num, delMax, badStatusCount));
			System.out.println("Cylinder Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax);
			System.out.println("Cylinder Path length = " + cResult.getFinalS() + " m\n\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}

	private static InitVal[] getInitVals(Random rand, int num, int charge, boolean randCharge, double xmin, double xmax,
			double ymin, double ymax, double zmin, double zmax, double pmin, double pmax, double thetamin,
			double thetamax, double phimin, double phimax) {

		InitVal[] initVals = new InitVal[num];

		for (int i = 0; i < num; i++) {
			initVals[i] = createInitVal();
			randomInitVal(rand, initVals[i], charge, randCharge, xmin, xmax, ymin, ymax, zmin, zmax, pmin, pmax,
					thetamin, thetamax, phimin, phimax);
		}

		return initVals;
	}

	private static void randomInitVal(Random rand, InitVal initVal, int charge, boolean randCharge, double xmin,
			double xmax, double ymin, double ymax, double zmin, double zmax, double pmin, double pmax, double thetamin,
			double thetamax, double phimin, double phimax) {

		if (randCharge) {
			initVal.charge = (rand.nextBoolean() ? -1 : 1);
		} else {
			initVal.charge = charge;
		}

		initVal.xo = randVal(rand, xmin, xmax);
		initVal.yo = randVal(rand, ymin, ymax);
		initVal.zo = randVal(rand, zmin, zmax);
		initVal.p = randVal(rand, pmin, pmax);
		initVal.theta = randVal(rand, thetamin, thetamax);
		initVal.phi = randVal(rand, phimin, phimax);
	}

	private static double randVal(Random rand, double vmin, double vmax) {
		double del = vmax - vmin;

		if (Math.abs(del) < SMALL) {
			return vmin;
		} else {
			return vmin + del * rand.nextDouble();
		}
	}
	
	private static InitVal createInitVal() {
		if (_instance == null) {
			_instance = new AdaptiveTests();
		}
		return _instance.new InitVal();
	}

	class InitVal {
		public int charge;
		public double xo;
		public double yo;
		public double zo;
		public double p;
		public double theta;
		public double phi;
		
		
		public String toString() {
			String s1 = "Q: " + charge + "\n";
			String s2 = "xo: " + xo*100 + " cm \n";
			String s3 = "yo: " + yo*100 + " cm \n";
			String s4 = "zo: " + zo*100 + " cm \n";
			String s5 = "p: " + p + "\n";
			String s6 = "theta: " + theta + "\n";
			String s7 = "phi: " + phi + "\n";
			
			return s1 + s2 + s3 + s4 + s5 + s6 + s7;
		}
	}
}
