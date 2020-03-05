package cnuphys.snr;

/**
 * 
 * @author heddle
 * This is used in the stage 2 analysis, not in the basic SNR one stage analysis
 */
public class SegmentStart {
	
	
	/** the zero based wire. For CLAS12, [0..111] */
	public int wire = -1;
	
	public int numMissing = -1;
	
	/**
	 * The start of a segment
	 * @param wire the zero-based wire, for CLAS12 [0..111]
	 * @param numMissing the number of missing layers required
	 * to complete the segment. The highest quality have 0
	 */
	public SegmentStart(int wire, int numMissing) {
		this.wire = wire;
		this.numMissing = numMissing;
	}

}
