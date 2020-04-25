package cnuphys.magfield;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import cnuphys.magfield.MagneticFields.FieldType;

/**
 * Static testing of the magnetic field
 * 
 * @author heddle
 *
 */
public class MagTests {
	private static final JMenuItem reconfigItem = new JMenuItem("Remove Solenoid and Torus Overlap");
	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
			MagneticFieldCanvas.CSType.XZ);
//	final static MagneticFieldCanvas canvas2 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
//			MagneticFieldCanvas.CSType.YZ);

	private static String options[] = { "Random, with active field", " Along line, with active field",
			"Random, with active PROBE", " Along line, with active PROBE" };

	private static String _homeDir = System.getProperty("user.home");

	// test many traj on different threads
	private static void threadTest(final int num, final int numThread) {

		memoryReport("starting thread test");

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		final long seed = 5347632765L;

		final Random rand = new Random(seed);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				System.err.println("Starting thread " + Thread.currentThread().getName());
				long time = System.currentTimeMillis();
				float[] result = new float[3];
				FieldProbe probe = FieldProbe.factory();

				for (int i = 0; i < num; i++) {

					float z = 400 * rand.nextFloat();
					double rho = 400 * rand.nextFloat();
					double phi = Math.toRadians(-25 + 50 * rand.nextFloat());
					float x = (float) (rho * FastMath.cos(phi));
					float y = (float) (rho * FastMath.sin(phi));
					probe.field(x, y, z, result);

				}
				System.err.println("Thread " + Thread.currentThread().getName() + "  ending millis: "
						+ (System.currentTimeMillis() - time));
			}

		};

		for (int i = 0; i < numThread; i++) {
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}

	// sameness tests (overlap not overlap)
	public static void samenessTest() {
		System.err.println("Sameness Test Overlap Removal");

		// int num = 10000000;
		int num = 5000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		long seed = 5347632765L;

		Random rand = new Random(seed);

		System.err.println("Creating " + num + " random points");
		for (int i = 0; i < num; i++) {

			z[i] = 400 * rand.nextFloat();
			float rho = 400 * rand.nextFloat();
//			double phi = Math.toRadians(-25 + 50*rand.nextFloat());
//			double phi = Math.toRadians(-2 + 4*rand.nextFloat());
			double phi = 0;

			if (i == 0) {
				z[i] = 299.99f;
				rho = 299.99f;
				phi = 0;
			} else if (i == 1) {
				z[i] = 301.01f;
				rho = 301.01f;
				phi = 0;
			}

			x[i] = (float) (rho * FastMath.cos(phi));
			y[i] = (float) (rho * FastMath.sin(phi));
		}

		float result[][][] = new float[2][num][3];
		float diff[] = new float[3];

		System.err.println("Creating space");
		for (int i = 0; i < num; i++) {
			result[0][i] = new float[3];
			result[1][i] = new float[3];
		}

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		FieldProbe ifield = FieldProbe.factory();

		System.err.println("Computing with overlapping");
		long time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result[0][i]);
		}
		time = System.currentTimeMillis() - time;
		System.err.println("Time for overlapping field: " + (time) / 1000.);

		// now remove overlap

		System.err.println("Computing with non-overlapping");
		MagneticFields.getInstance().removeMapOverlap();
		ifield = FieldProbe.factory();

		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result[1][i]);
		}
		time = System.currentTimeMillis() - time;
		System.err.println("Time for non-overlapping field: " + (time) / 1000.);

		System.err.println("Computing biggest diff");
		// now get biggest diff
		double maxDiff = -1;
		int iMax = -1;

		for (int i = 0; i < num; i++) {

			for (int j = 0; j < 3; j++) {
				diff[j] = result[1][i][j] - result[0][i][j];
			}
			double dlen = FastMath.vectorLength(diff);

			if (dlen > maxDiff) {
				iMax = i;
				maxDiff = dlen;
			}
		}

		System.err.println("maxDiff = " + maxDiff + "   at index: " + iMax);
		System.err.println(String.format("xyz = (%8.4f, %8.4f, %8.4f)", x[iMax], y[iMax], z[iMax]));
		double phi = FastMath.atan2Deg(y[iMax], x[iMax]);
		if (phi < 0) {
			phi += 360;
		}
		double rho = FastMath.hypot(x[iMax], y[iMax]);
		System.err.println(String.format("cyl = (%8.4f, %8.4f, %8.4f)", phi, rho, z[iMax]));
		System.err.println(String.format("   Overlapping (%8.4f, %8.4f, %8.4f) %8.4f kG", result[0][iMax][0],
				result[0][iMax][1], result[0][iMax][2], FastMath.vectorLength(result[0][iMax])));
		System.err.println(String.format("NonOverlapping (%8.4f, %8.4f, %8.4f) %8.4f kG", result[1][iMax][0],
				result[1][iMax][1], result[1][iMax][2], FastMath.vectorLength(result[1][iMax])));

	}

	// sameness tests (overlap not overlap)
	private static void mathTest() {

		FastMath.MathLib libs[] = { FastMath.MathLib.DEFAULT, FastMath.MathLib.FAST, FastMath.MathLib.SUPERFAST };
		System.err.println("Sameness Test Math Lib");

		int num = 10000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		long seed = 5347632765L;

		Random rand = new Random(seed);

		System.err.println("Creating " + num + " random points");
		for (int i = 0; i < num; i++) {

			z[i] = 400 * rand.nextFloat();
			float rho = 400 * rand.nextFloat();
			// double phi = Math.toRadians(75 + 30*rand.nextFloat());
			double phi = Math.toRadians(89 * rand.nextFloat());

			x[i] = (float) (rho * FastMath.cos(phi));
			y[i] = (float) (rho * FastMath.sin(phi));
		}

		float result[][][] = new float[3][num][3];
		float diff[] = new float[3];

		System.err.println("Creating space");
		for (int i = 0; i < num; i++) {
			result[0][i] = new float[3];
			result[1][i] = new float[3];
			result[2][i] = new float[3];
		}

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		IField ifield = FieldProbe.factory();

		for (int index = 0; index < 3; index++) {
			FastMath.setMathLib(libs[index]);
			System.err.println("Computing with " + FastMath.getMathLib());
			long time = System.currentTimeMillis();
			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result[index][i]);
			}
			time = System.currentTimeMillis() - time;
			System.err.println("Time for " + FastMath.getMathLib() + " math: " + (time) / 1000.);
		}

		// now get biggest diff superfast to default
		double maxDiff = -1;
		int iMax = -1;

		for (int i = 0; i < num; i++) {

			for (int j = 0; j < 3; j++) {
				diff[j] = result[2][i][j] - result[0][i][j];
			}
			double dlen = FastMath.vectorLength(diff);

			if (dlen > maxDiff) {
				iMax = i;
				maxDiff = dlen;
			}
		}

		System.err.println("maxDiff = " + maxDiff + "   at index: " + iMax);
		System.err.println(String.format("xyz = (%8.4f, %8.4f, %8.4f)", x[iMax], y[iMax], z[iMax]));
		double phi = FastMath.atan2Deg(y[iMax], x[iMax]);
		if (phi < 0) {
			phi += 360;
		}
		double rho = FastMath.hypot(x[iMax], y[iMax]);
		System.err.println(String.format("cyl = (%8.4f, %8.4f, %8.4f)", phi, rho, z[iMax]));

		for (int index = 0; index < 3; index++) {
			System.err.println(String.format("%s (%8.4f, %8.4f, %8.4f) %8.4f kG", libs[index].toString(),
					result[index][iMax][0], result[index][iMax][1], result[index][iMax][2],
					FastMath.vectorLength(result[index][iMax])));
		}

	}

	// check active field
	private static void checkSectors() {
		IField field = FieldProbe.factory();

		long seed = 3344632211L;

		int num = 1000000;
		// int num = 1;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result[][][] = new float[6][num][3];
		float diff[] = new float[3];

		Random rand = new Random(seed);

		for (int i = 0; i < num; i++) {
			z[i] = 300 + (200 * rand.nextFloat());
//			z[i] = 26f;

			double phi = -10f + 20. * rand.nextFloat();

//			phi = 0;
			phi = Math.toRadians(phi);

			float rho = 50 + 400 * rand.nextFloat();

//			rho = 50;

			x[i] = (float) (rho * Math.cos(phi));
			y[i] = (float) (rho * Math.sin(phi));
		}

		System.err.println("\nSector test created points");

		float delMax = 0;
		int iMax = -1;
		int sectMax = -1;

		for (int i = 0; i < num; i++) {

			float locDelMax = 0;
			int locSectMax = -1;

			// System.err.println("--------");
			for (int sect = 1; sect <= 6; sect++) {

				field.field(sect, x[i], y[i], z[i], result[sect - 1][i]);

//				System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", 
//						sect, result[sect-1][i][0], result[sect-1][i][1], result[sect-1][i][2],
//						FastMath.vectorLength(result[sect-1][i])));

				if (sect > 0) {
					for (int k = 0; k < 3; k++) {
						diff[k] = result[sect - 1][i][k] - result[0][i][k];
					}
					double dlen = FastMath.vectorLength(diff);
					if (dlen > locDelMax) {
						locDelMax = (float) dlen;
						locSectMax = sect;
					}
				}

			} // end sector loop

			if (locDelMax > delMax) {
				delMax = locDelMax;
				iMax = i;
				sectMax = locSectMax;
			}

		}
		System.err.println("\nSector test calculated field");
		System.err.println(" Biggest diff: " + delMax + "  in sector " + sectMax);
		System.err.println(String.format("xyz = (%8.4f, %8.4f, %8.4f)", x[iMax], y[iMax], z[iMax]));

		double phi = FastMath.atan2Deg(y[iMax], x[iMax]);
		if (phi < 0) {
			phi += 360;
		}
		double rho = FastMath.hypot(x[iMax], y[iMax]);
		System.err.println(String.format("cyl = (%8.4f, %8.4f, %8.4f)", phi, rho, z[iMax]));

//		System.err.println(String.format("Sector 1 (%9.5f, %9.5f, %9.5f) %9.5f", 
//				result[0][iMax][0], result[0][iMax][1], result[0][iMax][2], 
//				FastMath.vectorLength(result[0][iMax])));

		for (int sect = 1; sect <= 6; sect++) {
			int sm1 = sect - 1;
			System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", sect, result[sm1][iMax][0],
					result[sm1][iMax][1], result[sm1][iMax][2], FastMath.vectorLength(result[sm1][iMax])));
		}

	}

	// chectk sectors for rotated composite
	private static void checkRotatedSectors() {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		RotatedCompositeProbe probe = (RotatedCompositeProbe) FieldProbe.factory();

		float result[] = new float[3];

//		double x = -60. + 20*Math.random();
		double x = 100. + 20 * Math.random();
		double y = -40. + 20 * Math.random();
		double z = 290. + 30 * Math.random();

		System.err.println(String.format("\n xyz = (%8.4f, %8.4f, %8.4f)", x, y, z));
		for (int sector = 1; sector <= 6; sector++) {
			probe.field(sector, (float) x, (float) y, (float) z, result);
			double Bx = result[0];
			double By = result[1];
			double Bz = result[2];
			double bmag = Math.sqrt(Bx * Bx + By * By + Bz * Bz);
			System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", sector, result[0], result[1],
					result[2], bmag));
		}

	}

	// timing tests
	private static void timingTest(int option) {
		System.out.println("Timing tests: [" + options[option] + "]");
		long seed = 5347632765L;

		int num = 10000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result[] = new float[3];

		IField ifield = FieldProbe.factory();

		Random rand = new Random(seed);

		if (option == 0) {
			for (int i = 0; i < num; i++) {
				z[i] = 600 * rand.nextFloat();
				float rho = 600 * rand.nextFloat();
				double phi = Math.toRadians(30 * rand.nextFloat());

				x[i] = (float) (rho * FastMath.cos(phi));
				y[i] = (float) (rho * FastMath.sin(phi));
			}
		} else if (option == 1) {
			double dT = 1. / (num - 1);
			for (int i = 0; i < num; i++) {
				double t = i * dT;
				x[i] = (float) (85. * t);
				y[i] = (float) (15. * t);
				z[i] = (float) (372. * t);
			}
		}

		// prime the pump
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result);
		}

		double sum = 0;
		for (int outer = 0; outer < 5; outer++) {
			long time = System.currentTimeMillis();

			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result);
			}

			double del = (System.currentTimeMillis() - time) / 1000.;
			sum += del;

			System.out.println("loop " + (outer + 1) + " time  = " + del + " sec");

		}
		System.out.println("avg " + (sum / 5.) + " sec");

	}
	
	//load the ascii torus
	private static void loadAsciiTorus() {	
		ToAscii.readAsciiTorus("/Users/heddle/magfield/FullTorus.txt");
	}
	
	
	private static void compareGEMCTorus() {
		System.out.println("Setting field to torus only.");
		
		
		double gemcdata[][] = {
				//NN TORUS ONLY		
				{496.225, 8.5994, 264.179,   3.13083e-18, 0.00496269, 1.6076e-17},
				{35.7457, 496.431, 499.971,   0.000185855, -0.00197376, -0.000705167},
				{159.022, 388.459, 306.278,   -0.0174959, 0.0128095, 0.000542118},
				{341.313, 171.239, 275.274,   -0.0139845, -0.0666581, 0.012125},
				{140.612, 58.5985, 368.63,   -0.342639, 0.686831, -0.0648507},
				{334.661, 239.04, 252.056,   -0.0106836, -0.0256641, 0.00765755},
				{145.925, 251.197, 299.973,   7.19425e-18, 0.151853, -4.40537e-17},
				{431.29, 232.83, 527.776,   0.000536185, -0.0015445, -0.000317776},
				{53.312, 338.198, 138.065,   -0.00284981, -0.0125471, 0.0154393},
				{240.461, 376.897, 219.262,   -0.00204967, 0.00843449, 0.0008723},
				{362.697, 175.685, 585.088,   0.000476148, -0.00145595, -0.000737294},
				{84.6628, 469.569, 222.828,   -0.00473728, -0.00461568, 0.00268136},
				{54.9528, 395.486, 535.359,   -0.000321198, -0.00278942, -0.00299359},
				{32.779, 249.869, 546.559,   0.0023272, -0.00483037, -0.00654778},
				{67.6333, 290.384, 106.685,   -0.00138529, -0.000476482, 0.0143488},
				{134.167, 272.762, 193.851,   -0.0139352, 0.0525363, 0.0166309},
				{134.116, 197.083, 416.29,   -0.0277987, 0.140604, -0.0361505},
				{29.6759, 460.195, 238.084,   -0.000461795, -0.00956103, 0.00177496},
				{29.2697, 413.947, 521.362,   0.000754557, -0.00373464, -0.00171003},
				{80.4731, 234.619, 483.998,   -0.00900032, 0.0141529, -0.0357867},
				{190.112, 263.89, 557.21,   -0.000753205, 0.00352481, -0.00272293},
				{1.2454, 8.6383, 429.798,   0.0381581, -0.0137955, -0.00265154},
				{201.851, 418.178, 552.956,   -0.000461119, 0.0013911, -0.000604042},
				{36.1727, 179.856, 506.01,   0.00364127, -0.00705238, -0.0311974},
				{60.0228, 160.559, 297.228,   -0.111066, 0.530351, 0.0628344},
				{125.535, 223.825, 374.389,   -2.90434e-17, 0.186396, 1.93623e-17},
				{435.529, 240.783, 216.499,   0.00111699, -0.00436352, 0.000476382},
				{259.091, 118.064, 442.853,   0.00127204, -0.0661337, -0.071331},
				{114.992, 73.3266, 597.031,   0.000490728, -0.00069606, -0.000147822},
				{124.332, 172.3, 428.433,   -0.0403489, 0.150592, -0.0641344},
				{6.7452, 273.481, 435.941,   0.051118, -0.12767, -0.045554},
				{394.045, 14.1724, 179.829,   -0.00259152, 0.0131244, 0.00201429},
				{300.868, 383.929, 385.222,   -0.00464421, 0.00309322, -0.00148602}};
				
				//LINEAR TORUS ONLY
//				{366.629, 329.81, 161.527,   -0.00232142, -0.00161447, 0.00205713},
//				{189.419, 259.688, 323.83,   -0.0536138, 0.0943717, -0.00428765},
//				{89.3459, 488.484, 559.761,   -0.000418277, -0.000674752, -0.000926266},
//				{107.136, 330.186, 164.13,   -0.0146765, 0.00757199, 0.0196491},
//				{164.972, 464.112, 167.272,   -0.00277481, 0.00103968, 0.00184618},
//				{77.326, 248.237, 229.988,   -0.0948449, 0.155724, 0.118877},
//				{344.167, 313.799, 282.395,   -0.0102413, -0.0048692, 0.00180889},
//				{423.224, 193.059, 530.111,   2.04705e-05, -0.00195426, -0.00112192},
//				{315.44, 45.4511, 310.08,   -0.0768206, 0.0979897, 0.00218099},
//				{283.239, 108.465, 329.792,   -0.375792, 0.165271, -0.0437638},
//				{220.565, 382.024, 409.899,   -3.23425e-07, 0.00909005, -1.52479e-07},
//				{267.87, 4.5812, 329.623,   -0.0116908, 0.203975, -0.00174792},
//				{41.7517, 411.961, 295.976,   -0.0136935, -0.0303596, 0.00331154},
//				{227.516, 264.168, 235.055,   -0.0454887, 0.0285154, 0.025703},
//				{301.96, 304.395, 125.734,   -0.00324427, -0.000468936, 0.00425765},
//				{417.969, 115.335, 596.217,   -0.000409981, -0.000149529, -0.00123322},
//				{215.998, 445.462, 440.052,   -0.00158064, 0.00298779, -0.00084553},
//				{431.296, 199.375, 545.912,   8.58893e-05, -0.00144998, -0.000800907},
//				{318.548, 258.858, 360.97,   -0.0239484, -0.0204575, -0.00863209},
//				{177.614, 310.383, 242.565,   -0.000905491, 0.0411508, 0.00041588},
//				{3.6658, 83.895, 567.996,   0.000525766, -0.000673149, -0.000165032},
//				{89.2927, 328.219, 536.384,   -0.00181469, -2.64414e-05, -0.00681004},
//				{355.959, 156.774, 502.686,   -0.000138133, -0.00639797, -0.00480567},
//				{205.799, 173.921, 409.74,   -0.10757, 0.00643856, -0.205873},
//				{251.52, 311.871, 275.055,   -0.0239442, 0.0155505, 0.00521413},
//				{182.11, 58.0895, 100.126,   0.00479812, -0.000653249, 0.00750149},
//				{81.6234, 224.81, 134.782,   0.00107068, 0.0168382, 0.0242348},
//				{240.243, 174.453, 504.248,   0.00347424, -0.0143142, -0.0124724},
//				{445.355, 108.282, 300.215,   -0.012844, -0.000152211, 0.000902808},
//				{348.07, 12.453, 255.227,   -0.0104777, 0.051657, 0.00408978},
//				{397.474, 75.8644, 228.094,   -0.0183995, 0.00740824, 0.00911058},
//				{49.3129, 66.9799, 502.284,   0.00322557, 0.0391014, -0.0277967},
//				{10.971, 198.443, 162.911,   0.0392195, -0.0510594, 0.0262758},
//				{315.213, 269.016, 260.962,   -0.0224619, -0.0147817, 0.00853889},
//				{86.8587, 182.792, 363.769,   -0.0618355, 0.376072, -0.0303046},
//				{150.014, 448.645, 243.39,   -0.00752705, 0.00154571, 0.00244632},
//				{392.852, 281.981, 554.167,   2.00263e-05, -0.00118697, -0.000721873},
//				{55.3992, 101.96, 418.825,   -0.030452, 0.680444, -0.0221325},
//				{183.034, 356.119, 433.418,   -0.00379388, 0.0130062, -0.00269095},
//				{289.666, 199.557, 175.273,   -0.000231725, -0.0308991, 0.0171294},
//				{99.8994, 231.762, 289.084,   -0.0794568, 0.249999, 0.0244828},
//				{276.76, 81.6615, 352.762,   -0.216858, 0.167099, -0.0953727},
//				{342.839, 64.4238, 380.279,   -0.0500868, 0.0331108, -0.0256372},
//				{412.443, 7.0653, 439.365,   -0.00107715, 0.0106576, -0.000762709},
//				{6.365, 140.874, 407.073,   -0.666679, 1.01706, -0.225049},
//				{308.957, 108.63, 452.839,   -0.0160342, -0.0153965, -0.0370362},
//				{233.028, 245.915, 448.958,   -0.020465, 0.00647778, -0.0278078},
//				{82.6704, 398.577, 303.638,   -0.0347859, -0.0138368, 0.00295343},
//				{28.1, 400.638, 165.575,   0.000838222, -0.0119659, 0.00475135},
//				{237.333, 292.731, 337.437,   -0.0380671, 0.0279619, -0.00554967},
//				{59.6523, 371.81, 268.355,   -0.0519199, -0.0365464, 0.0210971},
//				{311.325, 363.116, 312.166,   -0.00820864, 0.00238206, 6.56877e-05},
//				{186.921, 341.028, 487.537,   -0.000855395, 0.00826369, -0.000988882},
//				{102.722, 267.352, 386.342,   -0.0728642, 0.109292, -0.0559448},
//				{302.812, 301.443, 495.719,   -0.00351331, -0.000706434, -0.00465053},
//				{427.862, 214.651, 523.21,   0.000390563, -0.0019132, -0.00064562},
//				{249.736, 166.341, 324.334,   -0.799659, 0.425416, 0.0319515},
//				{149.519, 318.178, 165.518,   -0.00697461, 0.0173186, 0.00806498},
//				{250.634, 44.749, 266.852,   -0.110963, 0.234663, 0.0657394},
//				{244.011, 375.984, 186.156,   -0.00209715, 0.0060837, 0.00127834},
//				{411.142, 82.0612, 337.167,   -0.0229376, 0.00715367, -0.00285788},
//				{157.962, 21.8575, 473.121,   -0.021042, 0.0828296, -0.0696792},
//				{70.6773, 330.743, 266.186,   -0.12589, -0.000958788, 0.0500548},
//				{239.826, 146.4, 350.827,   -0.692893, 0.915046, -0.197591},
//				{417.318, 153.336, 358.921,   -0.0122611, -0.010296, -0.0036529},
//				{159.224, 390.827, 583.647,   -0.000552232, 0.000975542, -0.00114789},
//				{209.716, 374.116, 109.349,   -0.00021277, 0.00356678, 0.000250038},
//				{481.99, 17.217, 525.943,   -0.000328109, 0.00173643, -0.000334186},
//				{387.661, 8.8408, 334.819,   -0.00474576, 0.0313905, -0.000572443},
//				{8.3922, 104.13, 370.218,   -0.46079, 0.942276, -0.00823988},
//				{143.863, 129.696, 336.948,   -0.241248, 0.521787, -0.0292818},
//				{88.368, 134.897, 234.677,   0.00406202, 0.232881, 0.0450338},
//				{33.6332, 397.551, 192.186,   -0.0017838, -0.0182512, 0.00774834},
//				{233.071, 208.241, 268.937,   -0.208806, 0.050473, 0.0822881}};
		
        MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		
		IField ifield = FieldProbe.factory();
		float result[] = new float[3];
		
		double delsumrel = 0;
		double delsumabs = 0;
		
		System.err.println();
		for (double v[] : gemcdata) {
			double x = v[0];
			double y = v[1];
			double z = v[2];
			
			ifield.field((float)x, (float)y, (float)z, result);
			
			double gBx = v[3]*10000; //T to gauss
			double gBy = v[4]*10000;
			double gBz = v[5]*10000;;
			double gB = Math.sqrt(gBx*gBx + gBy*gBy + gBz*gBz);
			
			double cBx = result[0]*1000; //kG to G
			double cBy = result[1]*1000;
			double cBz = result[2]*1000;
			double cB = Math.sqrt(cBx*cBx + cBy*cBy + cBz*cBz);
			
			double avg = 0.5*(gB+cB);
			double delabs = Math.abs(gB-cB);
			double delrel = delabs/avg;
			delsumabs += delabs;
			delsumrel += delrel;
			
			String s = String.format("(%-8.3f, %-8.3f, %-8.3f) BGSIM = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] Bced = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] delrel = %-8.4f delabs = %-8.4f Gauss", 
					x, y, z, 
					gBx, gBy, gBz, gB,
					cBx, cBy, cBz, cB, delrel, delabs);
			
			System.err.println(s);
		}
		System.err.println(String.format("avg delrel = %-8.5f", delsumrel/gemcdata.length));
		System.err.println(String.format("avg delabs = %-8.5f Gauss", delsumabs/gemcdata.length));

	}
	
	private static void compareGEMCSolenoid() {
		System.out.println("Setting field to solenoid only.");
		
		double gemcdata[][] = {
				//NN SOLEN ONLY
//				{73.822, 180.526, 11.5862,    -1.76004,  -4.30404,  34.2},
//				{83.4609, 36.8472, 271.273,    30.1339,  13.3038,  30.51},
//				{49.6422, 185.186, 75.6753,    -12.2808,  -45.8125,  10.48},
//				{39.9618, 204.784, -109.912,    5.19618,  26.6277,  -14.34},
//				{140.013, 108.221, -116.055,    36.9177,  28.5349,  -41.61},
//				{68.9909, 96.9404, -210.254,    -43.7774,  -61.5125,  6.84},
//				{76.3865, 78.1135, 243.842,   33.9512,  34.7188,  22.72},
//				{207.078, 77.4315, -167.772,    1.20829,  0.451809,  -16.44},
//				{99.2131, 65.2532, -170.018,    -103.074,  -67.7927,  -52.95},
//				{204.521, 138.17, 289.181,    4.78118,  3.23006,  -1.79},
//				{43.715, 54.0158, -121.354,    -923.823,  -1141.51,  5.43},
//				{228.752, 193.281, 203.377,    0.290261,  0.245252,  -3.92},
//				{155.742, 165.299, -57.4807,    13.187,  13.9963,  9.32},
//				{107.589, 110.217, 208.377,    25.5311,  26.1547,  -14.69},
//				{112.956, 126.821, 257.498,    13.1226,  14.7333,  -0.74},
//				{72.0802, 36.9867, -217.642,    -90.3139,  -46.343,  71.66},
//				{128.02, 241.639, -275.975,    -1.99902,  -3.77317,  -2.48},
//				{157.732, 68.1332, -276.739,    -15.7532,  -6.80466,  1.79},
//				{49.1934, 159.215, -265.476,    -5.98381,  -19.3666,  1.4},
//				{59.1272, 37.5827, 284.228,    21.2167,  13.4859,  36.73},
//				{148.134, 197.375, 126.488,    -6.12874,  -8.16595,  -7.11},
//				{160.22, 201.965, 236.195,    2.49219,  3.14151,  -4.94},
//				{70.8317, 272.381, -259.15,    -0.890933,  -3.42605,  -3.12},
//				{11.337, 183.953, 78.5665,    -3.54931,  -57.5907,  9.19},
//				{94.5989, 225.429, 140.949,    -3.07238,  -7.32148,  -9.2},
//				{122.537, 46.9626, -236.023,    -41.6649,  -15.9681,  7.71},
//				{125.117, 222.013, -68.4566,    5.813,  10.3148,  3.21},
//				{38.5663, 244.286, 4.3558,    -0.0358667,  -0.227186,  12.59},
//				{85.3342, 158.314, 128.972,    -14.0826,  -26.1263,  -41.43}};
		
				//LINEAR INTERP SOL ONLY
				{187.691, 159.811, 47.3208,  -7.67908,  -6.5384,  9.06023  },
				{164.229, 40.1344, 112.58 ,  -60.9808,  -14.9025,  -47.5493  },
				{59.9728, 70.585, 241.228,   37.1744,  43.7524,  39.1832  },
				{48.7519, 109.422, 244.992,   17.6347,  39.5805,  15.2529  },
				{150.382, 51.144, 140.373,   -11.1821,  -3.80298,  -73.5199  },
				{18.0697, 131.957, -133.863,   -1.82845,  -13.3526,  -161.079  },
				{84.1644, 90.2623, -140.76,   -56.5292,  -60.6249,  -168.356  },
				{216.666, 62.4497, 63.2686,   -18.8424,  -5.43097,  9.27143  },
				{203.636, 187.693, 142.927,   -3.41886,  -3.15118,  -4.86278  },
				{86.3591, 53.7414, 47.6946,   1084.31,  674.769,  571.271  },
				{22.553, 276.033, -209.349,   -0.130278,  -1.59451,  -5.46614  },
				{209.231, 126.264, -277.744,   -5.54475,  -3.34607,  -2.47414  },
				{40.2278, 294.298, -11.8372,   0.206677,  1.512,  4.56982  },
				{22.4589, 298.864, -97.1238,   0.389101,  5.17783,  -0.935768  },
				{102.786, 151.367, -262.625,   -9.31576,  -13.7188,  -1.39919  },
				{52.5945, 205.298, 86.9238,   -7.37084,  -28.7715,  0.218492  },
				{75.429, 71.645, 157.629,   155.303,  147.512,  -78.8034  },
				{198.831, 97.19, -59.7657,   19.8982,  9.72639,  9.75546  },
				{134.14, 24.1661, 131.512,   -10.5008,  -1.89177,  -152.257  },
				{258.632, 34.8282, -273.035,   -5.04712,  -0.679661,  -2.73791  },
				{27.1344, 203.246, -192.919,   -1.09949,  -8.23551,  -16.5786  },
				{231.759, 21.227, -41.4448,   13.5599,  1.24196,  12.172  },
				{143.973, 130.229, 295.353,   8.05824,  7.28895,  0.667539  },
				{209.832, 82.8324, 257.018,   7.5876,  2.99524,  -4.05822  },
				{126.527, 229.581, 8.3879,   -0.417419,  -0.757402,  9.3967  },
				{195.223, 200.525, -192.946,   -0.1803,  -0.185196,  -5.74216  },
				{283.692, 44.5934, 72.9987,   -5.82731,  -0.91599,  1.8131  },
				{259.022, 13.0388, 167.449,   -2.60386,  -0.131074,  -7.75096  }};
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);
		
		IField ifield = FieldProbe.factory();
				
		float result[] = new float[3];
		
		double delsumrel = 0;
		double delsumabs = 0;
		
		System.err.println();
		for (double v[] : gemcdata) {
			double x = v[0];
			double y = v[1];
			double z = v[2];
			
			ifield.field((float)x, (float)y, (float)z, result);
			
			double gBx = v[3];
			double gBy = v[4];
			double gBz = v[5];
			double gB = Math.sqrt(gBx*gBx + gBy*gBy + gBz*gBz);
			
			double cBx = result[0]*1000;
			double cBy = result[1]*1000;
			double cBz = result[2]*1000;
			double cB = Math.sqrt(cBx*cBx + cBy*cBy + cBz*cBz); //Gauss
			
			double avg = 0.5*(gB+cB);
			double delabs = Math.abs(gB-cB);
			double delrel = delabs/avg;
			delsumabs += delabs;
			delsumrel += delrel;
			
			String s = String.format("(%-8.3f, %-8.3f, %-8.3f) BGSIM = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] Bced = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] delrel = %-8.4f delabs = %-8.4f Gauss", 
					x, y, z, 
					gBx, gBy, gBz, gB,
					cBx, cBy, cBz, cB, delrel, delabs);
			
			System.err.println(s);
		}
		
		System.err.println(String.format("avg delrel = %-8.5f", delsumrel/gemcdata.length));
		System.err.println(String.format("avg delabs = %-8.5f Gauss", delsumabs/gemcdata.length));
		
	}

	private static void scanCSVFile() {

		try {
			FileReader fileReader = new FileReader("/Users/heddle/magfield/FullTorus.csv");
			final BufferedReader bufferedReader = new BufferedReader(fileReader);

			int lineCount = 0;

			String vals[] = new String[6];
			
			float bmax = Float.NEGATIVE_INFINITY;
			float phi = Float.NaN;
			float r = Float.NaN;
			float z = Float.NaN;
			
			boolean reading = true;
			while (reading) {
				String s = bufferedReader.readLine();
				if (s != null) {
					tokens(s, ",", vals);
					
					float bx = Float.parseFloat(vals[3]);
					float by = Float.parseFloat(vals[4]);
					float bz = Float.parseFloat(vals[5]);
					
					float b = (float)(Math.sqrt(bx*bx + by*by + bz*bz));
					if (b > bmax) {
						bmax = b;
						phi = Float.parseFloat(vals[0]);
						r = Float.parseFloat(vals[1]);
						z = Float.parseFloat(vals[2]);
					}
					
					lineCount++;
					if ((lineCount % 500000) == 0) {
						System.out.println("line count = " + lineCount);
					}
				} else {
					reading = false;
				}
			}

			System.out.println("line count = " + lineCount);
			System.out.println("bmax = " + bmax + " kG at (phi, rho, z) = (" + phi + ", " + r + ", " + z +")");
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void tokens(String str, String delimiter, String[] vals) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			vals[i] = t.nextToken();
		}
	}
	
	//convert the torus to ASCII
	private static void convertTorusToAscii() {		
//		ToAscii.torusToAscii(MagneticFields.getInstance().getTorus(), 
//				"/Users/heddle/magfield/FullTorus.txt", false);
		ToAscii.torusToAscii(MagneticFields.getInstance().getTorus(), 
				"/Users/heddle/magfield/FullTorus.csv", true);
	}
	
	//convert the solenoid to ASCII
	private static void convertSolenoidToAscii() {		
		ToAscii.solenoidToAscii((SolenoidProbe) FieldProbe.factory(MagneticFields.getInstance().getSolenoid()), 
				"/Users/heddle/magfield/Solenoid.csv");
	}

	public static JMenu getTestMenu() {

		JMenu testMenu = new JMenu("Tests");

		final JMenuItem test0Item = new JMenuItem("Timing Test Random Points");
		final JMenuItem test1Item = new JMenuItem("Timing Test Along a Line");
		final JMenuItem test4Item = new JMenuItem("Overlap/No overlap Sameness Test");
		final JMenuItem test5Item = new JMenuItem("MathLib Test");
		final JMenuItem threadItem = new JMenuItem("Thread Test");
		final JMenuItem asciiTorusItem = new JMenuItem("Convert Torus to ASCII");
		final JMenuItem asciiSolenoidItem = new JMenuItem("Convert Solenoid to ASCII");
		final JMenuItem scanItem = new JMenuItem("Scan csv file");
		final JMenuItem loadItem = new JMenuItem("Load ASCII Torus");
		final JMenuItem gemcSolenoidItem = new JMenuItem("Compare GEMC Solenoid");
		final JMenuItem gemcTorusItem = new JMenuItem("Compare GEMC Torus");


		ActionListener al1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == test0Item) {
					timingTest(0);
				} else if (e.getSource() == test1Item) {
					timingTest(1);
				} else if (e.getSource() == test4Item) {
					samenessTest();
				} else if (e.getSource() == test5Item) {
					mathTest();
				} else if (e.getSource() == threadItem) {
					threadTest(10000000, 8);
				} else if (e.getSource() == asciiTorusItem) {
					convertTorusToAscii();
				} else if (e.getSource() == asciiSolenoidItem) {
					convertSolenoidToAscii();
				} else if (e.getSource() == scanItem) {
					scanCSVFile();
				} else if (e.getSource() == loadItem) {
					loadAsciiTorus();
				}
				else if (e.getSource() == gemcSolenoidItem) {
					compareGEMCSolenoid();
				}
				else if (e.getSource() == gemcTorusItem) {
					compareGEMCTorus();
				}

			}

		};

		test0Item.addActionListener(al1);
		test1Item.addActionListener(al1);
		test4Item.addActionListener(al1);
		test5Item.addActionListener(al1);
		threadItem.addActionListener(al1);
		asciiTorusItem.addActionListener(al1);
		asciiSolenoidItem.addActionListener(al1);
		scanItem.addActionListener(al1);
		loadItem.addActionListener(al1);
		gemcSolenoidItem.addActionListener(al1);
		gemcTorusItem.addActionListener(al1);
		
		testMenu.add(test0Item);
		testMenu.add(test1Item);
		testMenu.add(test4Item);
		testMenu.add(test5Item);
		testMenu.add(threadItem);
		testMenu.addSeparator();
		testMenu.add(asciiTorusItem);
		testMenu.add(asciiSolenoidItem);
		testMenu.add(scanItem);
		testMenu.add(loadItem);
		testMenu.add(gemcSolenoidItem);
		testMenu.add(gemcTorusItem);
		testMenu.addSeparator();

		// now for rectangular grids
		final JMenuItem rotatedSectorItem = new JMenuItem("Check Sectors for Rotated Composite");
		final JMenuItem sectorItem = new JMenuItem("Check Sectors for (Normal) Composite");

		ActionListener al2 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == reconfigItem) {
					MagneticFields.getInstance().removeMapOverlap();
				} else if (e.getSource() == rotatedSectorItem) {
					checkRotatedSectors();
				} else if (e.getSource() == sectorItem) {
					checkSectors();
				}
			}
		};
		reconfigItem.addActionListener(al2);
		rotatedSectorItem.addActionListener(al2);
		sectorItem.addActionListener(al2);
		testMenu.add(reconfigItem);
		testMenu.add(rotatedSectorItem);
		testMenu.add(sectorItem);

		return testMenu;
	}

	/**
	 * Print a memory report
	 * 
	 * @param message a message to add on
	 */
	public static void memoryReport(String message) {
		System.gc();
		System.gc();

		StringBuilder sb = new StringBuilder(1024);
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		double used = total - free;
		sb.append("==== Memory Report =====\n");
		if (message != null) {
			sb.append(message + "\n");
		}
		sb.append("Total memory in JVM: " + String.format("%6.1f", total) + "MB\n");
		sb.append(" Free memory in JVM: " + String.format("%6.1f", free) + "MB\n");
		sb.append(" Used memory in JVM: " + String.format("%6.1f", used) + "MB\n");

		System.err.println(sb.toString());
	}

	private static void fixMenus() {
		boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();
		boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
		reconfigItem.setEnabled(hasSolenoid && hasTorus);
	}

	// set up the frame to run the tests
	public static void runTests() {
		final JFrame testFrame = new JFrame("Magnetic Field");
		testFrame.setLayout(new BorderLayout(4, 4));

		final MagneticFields mf = MagneticFields.getInstance();

		// test specific load
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			// mf.initializeMagneticFields(mfdir.getPath(), "torus.dat",
			// "Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
//					"SolenoidMarch2019_BIN.dat");
			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
					"Symm_solenoid_r601_phi1_z1201_13June2018.dat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		final JLabel label = new JLabel(" Torus: " + MagneticFields.getInstance().getTorusPath());
		label.setFont(new Font("SandSerif", Font.PLAIN, 10));
		testFrame.add(label, BorderLayout.SOUTH);

		// drawing canvas
		JPanel magPanel1 = canvas1.getPanelWithStatus(1000, 465);
//		JPanel magPanel2 = canvas2.getPanelWithStatus(1000, 465);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		MagneticFieldChangeListener mfcl = new MagneticFieldChangeListener() {

			@Override
			public void magneticFieldChanged() {
				label.setText(" Torus: " + MagneticFields.getInstance().getTorusPath());
				System.err.println("Field changed. Torus path: " + MagneticFields.getInstance().getTorusPath());
				fixMenus();
				MagneticFields.getInstance().printCurrentConfiguration(System.err);
			}

		};
		MagneticFields.getInstance().addMagneticFieldChangeListener(mfcl);

		// add the menu
		JMenuBar mb = new JMenuBar();
		testFrame.setJMenuBar(mb);
		mb.add(mf.getMagneticFieldMenu(true, true));

		mb.add(sectorMenu());

		JMenu testMenu = MagTests.getTestMenu();

		mb.add(testMenu);

		JPanel cpanel = new JPanel();
		cpanel.setLayout(new GridLayout(1, 1, 4, 4));

		cpanel.add(magPanel1);
//		cpanel.add(magPanel2);

		testFrame.add(cpanel, BorderLayout.CENTER);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}

	// the menu for changing sectors
	private static JMenu sectorMenu() {
		JMenu sectorMenu = new JMenu("Sector");
		final JRadioButtonMenuItem sectorButton[] = new JRadioButtonMenuItem[6];
		ButtonGroup bg = new ButtonGroup();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int sector = 1; sector <= 6; sector++) {
					if (sectorButton[sector - 1].isSelected()) {
						if (sector != _sector) {
							_sector = sector;
							System.out.println("Sector is now " + _sector);

							canvas1.setSector(_sector);
//							canvas2.setSector(_sector);

							canvas1.repaint();
//							canvas2.repaint();
						}
						break;
					}
				}
			}

		};

		for (int sector = 1; sector <= 6; sector++) {
			sectorButton[sector - 1] = new JRadioButtonMenuItem("Sector " + sector, sector == _sector);
			sectorButton[sector - 1].addActionListener(al);
			bg.add(sectorButton[sector - 1]);
			sectorMenu.add(sectorButton[sector - 1]);
		}

		return sectorMenu;
	}
}
