package cnuphys.snr;

import java.util.ArrayList;

/**
 * 
 * @author heddle
 * A wire list is a list of wires (0-based) For CLAS12 [0..111]
 */
public class WireList extends ArrayList<Integer> {
	
	
	/**
	 * Create a wirelist
	 */
	public WireList() {
		super();
	}
	
	/**
	 * Add a value, do not allow duplicates
	 * @param val the value to add
	 * @return <code>true</code> as required.
	 */
	@Override
	public boolean add(Integer val) {
		remove(val);
		return super.add(val);
	}
	
	/**
	 *  A string representation.  Note wires are zero-based,
	 *  but we print them out 1-based. Ugh. 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(128);
		sb.append("[");

		if (!isEmpty()) {
			int len = size();
			for (int i = 0; i < len - 1; i++) {
				sb.append((get(i)+1) + " "); //print 1-based
			}
			sb.append(get(len-1)+1); //print 1-based
		}

		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Get the average wire position (zero-based)
	 * @return the average wire position
	 */
	public double averageWirePosition() {
		if (isEmpty()) {
			return Double.NaN;
		}
		
		double sum = 0;
		for (int wire : this) {
			sum += wire;
		}
		
		return sum/size();
	}
	

}
