package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * The Class Torus.
 *
 * @author David Heddle
 * @author Nicole Schumacher
 */

public class Torus extends MagneticField {

	// has part of the solenoid been added in to remove the overlap?
	protected boolean _addedSolenoid;

	// if is full, then no assumed 12-fold symmetry
	private boolean _fullMap;

	/**
	 * Instantiates a new torus. Note q1 = phi, q2 = rho, q3 = z
	 */
	private Torus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
		_addedSolenoid = false;
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
		return MagneticFields.getInstance().hasActiveTorus();
	}

	/**
	 * Has part of the solenoid been added in to remove the overlap?
	 * @return<code>true</code> if the solenoid was added in.
	 */
	public boolean isSolenoidAdded() {
		return _addedSolenoid;
	}

	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12-fieldmap-torus.dat"
	 *
	 * @param file the file to read
	 * @return the torus object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static Torus fromBinaryFile(File file) throws FileNotFoundException {
		Torus torus = new Torus();
		torus.readBinaryMagneticField(file);
		double phiMax = torus.getPhiMax();

		torus._fullMap = (phiMax > 100.);

		System.out.println(torus.toString());

		return torus;
	}

	/**
	 * Get the maximum phi coordinate of the field boundary (deg)
	 * 
	 * @return the maximum phi coordinate of the field boundary
	 */
	public double getPhiMax() {
		double phimax = q1Coordinate.getMax();
		while (phimax < 0) {
			phimax += 360.;
		}
		return phimax;
	}

	/**
	 * Get the minimum phi coordinate of the field boundary (deg)
	 * 
	 * @return the minimum phi coordinate of the field boundary
	 */
	public double getPhiMin() {
		return q1Coordinate.getMin();
	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Torus";
	}

	/**
	 * Check whether there is an assume 12-fold symmetry
	 * 
	 * @return <code>true</code> if this is a full map
	 */
	public boolean isFullMap() {
		return _fullMap;
	}

	/**
	 * Get some data as a string.
	 * 
	 * @return a string representation.
	 */
	@Override
	public final String toString() {
		String s = "Torus " + (_fullMap ? " (Full Map)" : " (12-fold symmetry)\n");
		s += super.toString();
		return s;
	}

	/**
	 * Used to add the solenoid into the torus. Experimental!!
	 * 
	 * @param compositeIndex the composite index
	 * @param result         the solenoid field added in
	 */
	public void addToField(int compositeIndex, float[] result) {
		int index = 3 * compositeIndex;
		for (int i = 0; i < 3; i++) {
			int j = index + i;
			field.put(j, field.get(j) + (float) (_scaleFactor * result[i]));
		}
		_addedSolenoid = true;
	}

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println(String.format("TORUS scale: %6.3f file: %s", _scaleFactor,
				MagneticFields.getInstance().getTorusBaseName()));
	}

	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 *
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result a float array holding the retrieved field in kiloGauss. The 0,1
	 *               and 2 indices correspond to x, y, and z components.
	 */
	@Override
	public void field(float x, float y, float z, float[] result) {

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
	
	private static boolean DEBUG = true;
	
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
		if (isFullMap()) {
			if (phi < 0) {
				phi += 360;
			}
			calculate(phi, rho, z, result);
		}
		else { //symmetric
			// relativePhi (-30, 30) phi relative to middle of sector
			double relativePhi = relativePhi(phi);

			boolean flip = (relativePhi < 0.0);
			calculate(phi, rho, z, result);
			
			// negate change x and z components
			if (flip) {
				result[0] = -result[0];
				result[2] = -result[2];
			}

		}
		
		result[0] *= _scaleFactor;
		result[1] *= _scaleFactor;
		result[2] *= _scaleFactor;


	}
	
	/**
	 * Must deal with the fact that we only have the field between 0 and 30 degrees.
	 *
	 * @param absolutePhi the absolute phi
	 * @return the relative phi (-30, 30) from the nearest middle of a sector in
	 *         degrees.
	 */
	private double relativePhi(double absolutePhi) {
		if (absolutePhi < 0.0) {
			absolutePhi += 360.0;
		}

		// make relative phi between 0 -30 and 30
		double relativePhi = absolutePhi;
		while (Math.abs(relativePhi) > 30.0) {
			relativePhi -= 60.0;
		}
		return relativePhi;
	}
	
	private void calculate(double phi, double rho, double z, float result[]) {
		
		int N1 = q1Coordinate.getIndex(phi);
		if (N1 < 0) {
			System.err.println("phi value out of range in Solenoid fieldCylindrical: " + rho);
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			return;
		}

		
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

		int N1P1 = N1+1;
		int N2P1 = N2+1;
		int N3P1 = N3+1;
		
		double p1 = q1Coordinate.getValue(N1);
		double p2 = q1Coordinate.getValue(N1P1);
		double r1 = q2Coordinate.getValue(N2);
		double r2 = q2Coordinate.getValue(N2P1);
		double z1 = q3Coordinate.getValue(N3);
		double z2 = q3Coordinate.getValue(N3P1);
		
		if ((p1 > phi) || (p2 < phi)) {
			System.err.println(String.format("phi bracket error: [%-7.3f, %-7.3f, %-7.3f]", p1, phi, p2));
			System.exit(-1);
		}


		if ((r1 > rho) || (r2 < rho)) {
			System.err.println(String.format("rho bracket error: [%-7.3f, %-7.3f, %-7.3f]", r1, rho, r2));
			System.exit(-1);
		}


		if ((z1 > z) || (z2 < z)) {
			System.err.println(String.format("z bracket error: [%-7.3f, %-7.3f, %-7.3f]", z1, z, z2));
			System.exit(-1);
		}

		if (MagneticField.isInterpolate()) {
			interpolate(phi, rho, z, p1, r1, z1, p2, r2, z2, N1, N2, N3, result);
		}
		else {
			double t1 = q1Coordinate.getFraction(phi);
			double t2 = q2Coordinate.getFraction(rho);
			double t3 = q3Coordinate.getFraction(z);
			int nn1 = (t1 < 0.5) ? N1 : N1P1;
			int nn2 = (t2 < 0.5) ? N2 : N2P1;
			int nn3 = (t3 < 0.5) ? N3 : N3P1;

			nearestNeighbor(getCompositeIndex(nn1, nn2, nn3), result);
		}

	}
	
	//does the interpolation
	private void interpolate(double x, double y, double z,
			double x0, double y0, double z0, 
			double x1, double y1, double z1, 
			int nx0, int ny0, int nz0, float[] result) {
		
		int nx1 = nx0 + 1;
		int ny1 = ny0 + 1;
		int nz1 = nz0 + 1;
		
		double xd = (x-x0)/(x1 - x0);
		double yd = (y-y0)/(y1 - y0);
		double zd = (z-z0)/(z1 - z0);
		
		double omxd = 1.-xd;
		double omyd = 1.-yd;
		double omzd = 1.-zd;
		
		int cidx000 = getCompositeIndex(nx0, ny0, nz0);
		int cidx001 = getCompositeIndex(nx0, ny0, nz1);
		int cidx010 = getCompositeIndex(nx0, ny1, nz0);
		int cidx011 = getCompositeIndex(nx0, ny1, nz1);
		int cidx100 = getCompositeIndex(nx1, ny0, nz0);
		int cidx101 = getCompositeIndex(nx1, ny0, nz1);
		int cidx110 = getCompositeIndex(nx1, ny1, nz0);
		int cidx111 = getCompositeIndex(nx1, ny1, nz1);


		for (int i = 1; i <= 3; i++) {
			double c000 = getBComponent(i, cidx000);
			double c001 = getBComponent(i, cidx001);
			double c010 = getBComponent(i, cidx010);
			double c011 = getBComponent(i, cidx011);
			double c100 = getBComponent(i, cidx100);
			double c101 = getBComponent(i, cidx101);
			double c110 = getBComponent(i, cidx110);
			double c111 = getBComponent(i, cidx111);
			
			double c00 = c000*omxd + c100*xd;
			double c01 = c001*omxd + c101*xd;
			double c10 = c010*omxd + c110*xd;
			double c11 = c011*omxd + c111*xd;
			
			double c0 = c00*omyd + c10*yd;
			double c1 = c01*omyd + c11*yd;
			
			result[i-1] = (float) (c0*omzd + c1*zd);


		}

	}
	
	//get the nearest neighbor value
	private void nearestNeighbor(int index, float result[]) {
		
		result[0] = getB1(index);
		result[1] = getB2(index);
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
		System.err.println("CALLING UNIMPLEMENTED METHOD field is sector coordinates in class Torus");
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
		System.err.println("CALLING UNIMPLEMENTED METHOD gradient in class Torus");
		System.exit(-1);
	}
	




}
