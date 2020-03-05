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
	
	public void packData(NoiseReductionParameters params) {
		
	}
	

}
