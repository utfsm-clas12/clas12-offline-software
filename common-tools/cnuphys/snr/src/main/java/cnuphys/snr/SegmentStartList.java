package cnuphys.snr;

import java.util.ArrayList;

/**
 * 
 * @author heddle This is used in the stage 2 analysis, not in the basic SNR one
 *         stage analysis
 */
public class SegmentStartList extends ArrayList<SegmentStart> {

	/**
	 * Holds a list of segment starts for a cluster
	 */
	public SegmentStartList() {
		super();
	}

	/**
	 * 
	 * @param wire the zero-based wire, for CLAS 
	 * @param numMissing the number of missing layers required
	 */
	public void add(int wire, int numMissing) {
		add(new SegmentStart(wire, numMissing));
	}

}
