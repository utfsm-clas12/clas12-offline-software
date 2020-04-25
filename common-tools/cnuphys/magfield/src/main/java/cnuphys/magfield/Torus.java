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
		// TODO Auto-generated method stub
		
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
