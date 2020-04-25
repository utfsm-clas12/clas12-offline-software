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
	
	private static void compareGEMCSolenoid() {
		System.out.println("Setting field to solenoid only.");
		
		double gemcdata[][] = {
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
			double cB = Math.sqrt(cBx*cBx + cBy*cBy + cBz*cBz);
			
			
			String s = String.format("(%-8.3f, %-8.3f, %-8.3f) BGSIM = (%-8.5f, %-8.5f, %-8.5f) [%-8.5f] Bced = (%-8.5f, %-8.5f, %-8.5f) [%-8.5f] BGSMIM/Bced = %-8.4f", 
					x, y, z, 
					gBx, gBy, gBz, gB,
					cBx, cBy, cBz, cB, gB/cB);
			
			System.err.println(s);
		}
		
		
		
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
