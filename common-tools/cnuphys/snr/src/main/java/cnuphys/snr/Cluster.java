package cnuphys.snr;

/**
 * 
 * @author heddle
 * A cluster candidate is a list of wires for a 
 * given direction with the
 * requirement that there are no more than two wires
 * in a given layer
 */
public class Cluster {
		
	/** The number of layers, probably 6 */
	public final int numLayers;
	
	/** a wirelist for the segment starting points. */
	public final SegmentStartList segmentStartList = new SegmentStartList();
	
	/** one wirelist for each layer */
	public final WireList wireLists[];
	
	/** the direction, 0 for left, 1 for right */
	public int direction;
	
	private double _slope = Double.NaN;
	
	/**
	 * @param numLayers the number of layers, for CLAS12 6
	 * @param direction the direction, 0 for left, 1 for right
	 */
	public Cluster(int numLayers, int direction) {
		this.direction = direction;
		this.numLayers = numLayers;
		wireLists = new WireList[numLayers];

		clear();
	}
	
	/**
	 * Add a wire to the wire list for a given layer
	 * @param layer the zero-based layer, for CLAS12 [0..5]
	 * @param wire the zero-based wire, for CLAS12 [0..111]
	 */
	public void add(int layer, int wire) {
		wireLists[layer].add(wire);
	}
	
	/**
	 * Add a segment (candidate) start wire
	 * @param wire the zero-based wire, for CLAS12 [0..111]
	 * @param numMissing the number of missing layers required
	 */
	public void addSegmentStart(int wire, int numMissing) {
		segmentStartList.add(wire, numMissing);
	}
		
	/**
	 * Clear the wire lists. So the object can be reused, but only for the same
	 * sector, superlayer, and numLayers
	 */
	public void clear() {
		
		segmentStartList.clear();

		for (int layer = 0; layer < numLayers; layer++) {
			if (wireLists[layer] == null) {
				wireLists[layer] = new WireList();
			}
			else {
				wireLists[layer].clear();
			}
		}
		

	}
	
	/**
	 * String representation
	 * @return a String representation of the cluster
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		
		sb.append(segmentStartList + " ");
		sb.append("{");
		for (WireList wl : wireLists) {
			if (wl != null) {
				sb.append(wl + " ");
			}
		}
		sb.append("}  ");
		
		if (Double.isNaN(_slope)) {
			computeSlope();
		}
		sb.append(String.format(" M: %5.2f ", _slope));
		return sb.toString();
	}
	
	public void packData(NoiseReductionParameters params) {
		
	}
	
	private double getSlope() {
		return _slope;
	}
	
	/**
	 * This computes slope a line using the layer as the X value and the 
	 * average wire number as the Y so a slope of 0 looks "vetical".
	 * We do it this way to avoid the possibility of an infinite slope.
	 */
	public void computeSlope() {
		double sumx = 0;
		double sumy = 0;
		double sumxy = 0;
		double sumx2 = 0;
		
		int n = 0;
		for (int layer = 0; layer < numLayers; layer++) {
			double avgWire = wireLists[layer].averageWirePosition();
			if (!Double.isNaN(avgWire)) {
				n++;
				sumx += layer;
				sumy += avgWire;
				sumxy += layer*avgWire;
				sumx2 += (layer*layer);
			}
		}
		
		if (n >0) {
			_slope = (n*sumxy- sumx*sumy)/(n*sumx2 -sumx*sumx);
		}
	}
	

}
