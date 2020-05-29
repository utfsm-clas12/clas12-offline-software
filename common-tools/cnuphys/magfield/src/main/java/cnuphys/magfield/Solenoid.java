/*
 * 
 */
package cnuphys.magfield;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * The Class Solenoid.
 *
 * @author Sebouh Paul
 * @version 1.0
 */
public final class Solenoid extends MagneticField {

	// private constructor
	/**
	 * Instantiates a new solenoid.
	 */
	private Solenoid() {
		setCoordinateNames("phi", "rho", "z");
	}

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public final boolean isZeroField() {
		if (isActive()) {
			return super.isZeroField();
		} else {
			return true;
		}
	}

	/**
	 * Checks this field active.
	 * 
	 * @return <code>true</code> if this field is active;
	 */
	@Override
	public boolean isActive() {
		return MagneticFields.getInstance().hasActiveSolenoid();
	}

	/**
	 * Obtain a solenoid object from a binary file, probably
	 * "clas12_solenoid_fieldmap_binary.dat"
	 *
	 * @param file the file to read
	 * @return the solenoid object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static Solenoid fromBinaryFile(File file) throws FileNotFoundException {
		Solenoid solenoid = new Solenoid();
		solenoid.readBinaryMagneticField(file);
		// is the field ready to use?
		System.out.println(solenoid.toString());
		return solenoid;
	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Solenoid";
	}

	/**
	 * Get some data as a string.
	 * 
	 * @return a string representation.
	 */
	@Override
	public final String toString() {
		String s = "Solenoid\n";
		s += super.toString();
		return s;
	}

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println(String.format("SOLENOID scale: %6.3f file: %s", _scaleFactor,
				MagneticFields.getInstance().getSolenoidBaseName()));
	}
	
	private static void binaryToAscii() {
		System.out.println("Converting to ASCII");
	}

	/**
	 * main method used for testing.
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		
		if (true) {
			binaryToAscii();
			return;
		}

		if (true) {
			processODUMap();
			return;
		}
		
		// covert the new ascii to binary
		File asciiFile = new File("../../../data/clas12SolenoidFieldMap.dat.txt");
		if (!asciiFile.exists()) {
			System.out.println("File not found: " + asciiFile.getPath());
		} else {
			System.out.println("File found: " + asciiFile.getPath());

			FileReader fileReader;
			try {
				fileReader = new FileReader(asciiFile);
				final BufferedReader bufferedReader = new BufferedReader(fileReader);

				// prepare the binary file
				String binaryFileName = "../../../data/clas12-fieldmap-solenoid.dat";
				// String binaryFileName = "data/solenoid-srr_V3.dat";
				int nPhi = 1;
				int nRho = 601;
				int nZ = 1201;
				float phimin = 0.0f;
				float phimax = 360.0f;
				float rhomin = 0.0f;
				float rhomax = 300.0f;
				float zmin = -300.0f;
				float zmax = 300.0f;

				DataOutputStream dos = new DataOutputStream(new FileOutputStream(binaryFileName));
				writeHeader(dos, phimin, phimax, nPhi, rhomin, rhomax,
					 nRho, zmin, zmax, nZ);

				boolean reading = true;
				while (reading) {
					String s = nextNonComment(bufferedReader);
					// System.out.println("s: [" + s + "]");

					if (s != null) {
						String tokens[] = tokens(s, " ");
						dos.writeFloat(0f);
						dos.writeFloat(10 * Float.parseFloat(tokens[2]));
						dos.writeFloat(10 * Float.parseFloat(tokens[3]));
						// System.out.println(s);
					} else {
						reading = false;
					}
				}

				dos.close();
				System.out.println("done");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static void writeHeader(DataOutputStream dos, float phimin, float phimax, int nPhi, float rhomin, float rhomax,
			int nRho, float zmin, float zmax, int nZ) {
		try {
			// write the header
			dos.writeInt(0xced);
			dos.writeInt(0);// cylindrical
			dos.writeInt(0);// cylindrical
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeFloat(phimin);
			dos.writeFloat(phimax);
			dos.writeInt(nPhi);
			dos.writeFloat(rhomin);
			dos.writeFloat(rhomax);
			dos.writeInt(nRho);
			dos.writeFloat(zmin);
			dos.writeFloat(zmax);
			dos.writeInt(nZ);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//process the file fro ODU
	private static void processODUMap() {
		File asciiFile = new File("/Users/heddle/magfield/SolenoidMarch2019");
		
		if (!asciiFile.exists()) {
			System.out.println("File not found: " + asciiFile.getPath());
			return;
		} 

		System.out.println("File found: " + asciiFile.getPath());

		FileReader fileReader;
		try {
			fileReader = new FileReader(asciiFile);
			final BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String binaryFileName = "/Users/heddle/magfield/SolenoidMarch2019_BIN.dat";
			
			int nPhi = 1;
			int nRho = 501;
			int nZ = 2001;
			float phimin = 0.0f;
			float phimax = 360.0f;
			
			//mm
			float rhomin = 0.0f;
			float rhomax = 50.0f;
			float zmin = -100.0f;
			float zmax = 100.0f;


			DataOutputStream dos = new DataOutputStream(new FileOutputStream(binaryFileName));
			writeHeader(dos, phimin, phimax, nPhi, rhomin, rhomax,
				 nRho, zmin, zmax, nZ);
			
			boolean reading = true;
						
			double Bmax = -1;
			float phiAtMax = 0;
			float rhoAtMax = Float.NaN;
			float zAtMax = Float.NaN;
			
			
			int nline = 0;
			while (reading) {
				String s = nextNonComment(bufferedReader);

				if (s != null) {
					nline++;
					String tokens[] = tokens(s, " ");
					
					//convert to cm
					float rho = Float.parseFloat(tokens[0])/10;
					float phi = 0;
					float z = Float.parseFloat(tokens[2])/10;
					
					//convert to kG
					float Brho = Float.parseFloat(tokens[3])/100;
					float Bphi = Float.parseFloat(tokens[4])/100;
					float Bz= Float.parseFloat(tokens[5])/100;
					double B = Math.sqrt(Brho*Brho + Bphi*Bphi + Bz*Bz);
					
					if (B > Bmax) {
						Bmax = B;
						rhoAtMax = rho;
						zAtMax = z;
					}
					
					dos.writeFloat(Bphi);
					dos.writeFloat(Brho);
					dos.writeFloat(Bz);

					
					
				} else {
					reading = false;
				}
			}
			
			System.out.println("Number of data lines: " + nline);
			
			
			String os = String.format("Bmax = %-6.2f kG at (phi, rho, z) = (%-6.2f, %-6.2f, %-6.2f)", Bmax, phiAtMax, rhoAtMax, zAtMax);
			System.out.println(os);
			
			dos.flush();
			dos.close();
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		System.out.println("Done processing ODU file.");
	}
	
	//tokenize a string
	private static String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * Get the next non comment line
	 * 
	 * @param bufferedReader a buffered reader which should be linked to an ascii
	 *                       file
	 * @return the next non comment line (or <code>null</code>)
	 */
	
	private static String nextNonComment(BufferedReader bufferedReader) {
		String s = null;
		try {
			s = bufferedReader.readLine();
			if (s != null) {
				s = s.trim();
			}
			
			
			while ((s != null) && (s.startsWith("<") || (s.length() < 1) || s.startsWith("r(mm"))) {
				
				s = bufferedReader.readLine();

				if (s != null) {
					s = s.trim();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return s;
	}
	
	/**
	 * Get the field in kG
	 * 
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result holds the resuts, the Cartesian coordinates of B in kG
	 */
	@Override
	public void field(float x, float y, float z, float result[]) {

		if (isZeroField()) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
			return;
		}

		// note that the contains functions handles the shifts
		if (!contains(x, y, z)) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
			return;
		}

		// apply the shifts
		x -= getShiftX();
		y -= getShiftY();
		z -= getShiftZ();

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
	}
	
	
	private static boolean DEBUG = false;
	/**
	 * Get the field by bilinear interpolation.
	 * 
	 * @param phi    azimuthal angle in degrees.
	 * @param rho    the cylindrical rho coordinate in cm.
	 * @param z      coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	public void fieldCylindrical(double phi, double rho, double z, float result[]) {
		
		// get the nearest neighbors
		int N2 = q2Coordinate.getIndex(rho);
		if (N2 < 0) {
			System.err.println("rho value out of range in Solenoid fieldCylindrical: " + rho);
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			return;
		}
		int N3 = q3Coordinate.getIndex(z);
		if (N3 < 0) {
			System.err.println("z value out of range in Solenoid fieldCylindrical: " + z);
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			return;
		}

		int N2P1 = N2+1;
		int N3P1 = N3+1;
		
		double r1 = q2Coordinate.getValue(N2);
		double r2 = q2Coordinate.getValue(N2P1);
		double z1 = q3Coordinate.getValue(N3);
		double z2 = q3Coordinate.getValue(N3P1);
				
		
		if ((r1 > rho) || (r2 < rho)) {
			System.err.println(String.format("rho bracket error: [%-7.3f, %-7.3f, %-7.3f]", r1, rho, r2));
			System.exit(-1);
		}


		if ((z1 > z) || (z2 < z)) {
			System.err.println(String.format("z bracket error: [%-7.3f, %-7.3f, %-7.3f]", z1, z, z2));
			System.exit(-1);
		}
		
		
		if (MagneticField.isInterpolate()) {
			interpolate(phi, rho, z, r1, z1, r2, z2, N2, N3, result);
		}
		else {
			double t1 = q2Coordinate.getFraction(rho);
			double t2 = q3Coordinate.getFraction(z);
			int nn2 = (t1 < 0.5) ? N2 : N2P1;
			int nn3 = (t2 < 0.5) ? N3 : N3P1;

			nearestNeighbor(phi, getCompositeIndex(0, nn2, nn3), result);
		}
		
		result[0] *= _scaleFactor;
		result[1] *= _scaleFactor;
		result[2] *= _scaleFactor;

		
		if (DEBUG) {
			System.err.println(String.format("rho bracket error: [%-7.3f, %-7.3f, %-7.3f]", r1, rho, r2));
			System.err.println(String.format("z bracket error: [%-7.3f, %-7.3f, %-7.3f]", z1, z, z2));
			System.err.println(String.format("field (%-8.5f,  %-8.5f, %-8.5f)", result[0], result[1], result[2]));
		}

		
	}
	
	//does the interpolation
	private void interpolate(double phi, double x, double y, double x0, double y0, double x1, double y1, int nx0, int ny0, float[] result) {
		
		int nx1 = nx0 + 1;
		int ny1 = ny0 + 1;
		
		double delx = x1 - x0;
		double dely = y1 - y0;
		double invdelxy = 1./(delx*dely);
		
		double dx0 = x-x0;
		double dx1 = x1-x;
		double dy0 = y-y0;
		double dy1 = y1-y;

		
		int c00 = getCompositeIndex(0, nx0, ny0);
		int c10 = getCompositeIndex(0, nx1, ny0);
		int c01 = getCompositeIndex(0, nx0, ny1);
		int c11 = getCompositeIndex(0, nx1, ny1);

		for (int i = 2; i <= 3; i++) {
			
			double q00 = getBComponent(i, c00);
			double q10 = getBComponent(i, c10);
			double q01 = getBComponent(i, c01);
			double q11 = getBComponent(i, c11);
			
			result[i-1] = (float)(invdelxy*(q00*dx1*dy1 + q10*dx0*dy1 + q01*dx1*dy0 + q11*dx0*dy0));
		}
		
		//get Cartesian field
		double rphi = Math.toRadians(phi);
		double Brho = result[1];
		result[0] = (float) (Brho*Math.cos(rphi));
		result[1] = (float) (Brho*Math.sin(rphi));
	}
	
	//get the nearest neighbor value
	private void nearestNeighbor(double phi, int index, float result[]) {
		
		//get Cartesian field
		double rphi = Math.toRadians(phi);
		double Brho = getB2(index);
		result[0] = (float) (Brho*Math.cos(rphi));
		result[1] = (float) (Brho*Math.sin(rphi));
		result[2] = getB3(index);
		
	}
	
	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates for the sector system. The other "field" methods are for the lab
	 * system. The field is returned as a Cartesian vector in kiloGauss.
	 * 
	 * @param sector the sector [1..6]
	 * @param x      the x sector coordinate in cm
	 * @param y      the y sector coordinate in cm
	 * @param z      the z sector coordinate in cm
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	@Override
	public void field(int sector, float xs, float ys, float zs, float[] result) {
		System.err.println("CALLING UNIMPLEMENTED METHOD field is sector coordinates in class Solenoid");
		System.exit(-1);
	}


	/**
	 * Obtain an approximation for the magnetic field gradient at a given location
	 * expressed in Cartesian coordinates. The field is returned as a Cartesian
	 * vector in kiloGauss/cm.
	 *
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result a float array holding the retrieved field in kiloGauss. The 0,1
	 *               and 2 indices correspond to x, y, and z components.
	 */
	@Override
	public void gradient(float x, float y, float z, float[] result) {
		System.err.println("CALLING UNIMPLEMENTED METHOD gradient in class Solenoid");
		System.exit(-1);
	}
	


}
