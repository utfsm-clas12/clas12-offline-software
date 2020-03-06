package cnuphys.snr;

import java.util.ArrayList;

public class ClusterFinder {
	
	public static final int LEFT_LEAN = NoiseReductionParameters.LEFT_LEAN;
	public static final int RIGHT_LEAN = NoiseReductionParameters.RIGHT_LEAN;


	//the parameters
	private NoiseReductionParameters _params;
	
	//the collections of clusters
	protected ArrayList<Cluster> leftClusters;
	protected ArrayList<Cluster> rightClusters;

	
	public ClusterFinder(NoiseReductionParameters params) {
		_params = params;
	}
	
	/**
	 * Find the clusters.
	 */
	public void findClusters() {
		
		if (leftClusters == null) {
			leftClusters = new ArrayList<>();
			rightClusters = new ArrayList<>();
		}
		else {
			leftClusters.clear();
			rightClusters.clear();
		}
		
		//left clusters
		if (!_params.leftSegments.isZero()) {
			boolean connected = false;
			Cluster currentCluster = null;
			
			int maxCount = _params.maxShift(LEFT_LEAN) + 1;
			int count = 0;
			
			for (int wire = 0; wire < _params.getNumWire(); wire++) {
				if (_params.leftSegments.checkBit(wire)) {
					if (!connected) { //create a new one
						currentCluster = new Cluster(_params.getNumLayer(), LEFT_LEAN);
						leftClusters.add(currentCluster);
						count = 0;
						connected = true;
					}
					currentCluster.addSegmentStart(wire, _params.missingLayersUsed(LEFT_LEAN, wire));
					
					fillWireLists(currentCluster, wire, LEFT_LEAN);
					
					count++;
					connected = count < maxCount;
				} //end wire was hit (checkBit)
				else {
					connected = false;
				}
			}
		}
		
		//right clusters
		if (!_params.rightSegments.isZero()) {
			boolean connected = false;
			Cluster currentCluster = null;
			
			int maxCount = _params.maxShift(RIGHT_LEAN) + 1;
			int count = 0;

			
			for (int wire = (_params.getNumWire()-1); wire >=0; wire--) {
				if (_params.rightSegments.checkBit(wire)) {
					if (!connected) { //create a new one
						currentCluster = new Cluster(_params.getNumLayer(), RIGHT_LEAN);
						rightClusters.add(currentCluster);
						count = 0;
						connected = true;
					}
					currentCluster.addSegmentStart(wire, _params.missingLayersUsed(RIGHT_LEAN, wire));
					fillWireLists(currentCluster, wire, RIGHT_LEAN);
					
					count++;
					connected = count < maxCount;
				} //end wire was hit (checkBit)
				else {
					connected = false;
				}
			}
		}

		//split clusters
		
	}
	
	
	//fill the wire lists
	private void fillWireLists(Cluster cluster, int segStartWire, int direction) {
		for (int lay = 0; lay < _params.getNumLayer(); lay++) {
			_params.addHitsInMask(lay, segStartWire, direction, cluster.wireLists[lay]);
		}

	}
	
	
	/**
	 * Get the list of left leaning clusters
	 * @return left leaning clusters
	 */
	public ArrayList<Cluster> getLeftClusters() {
		return leftClusters;
	}
	
	/**
	 * Get the list of right leaning clusters
	 * @return right leaning clusters
	 */
	public ArrayList<Cluster> getRightClusters() {
		return rightClusters;
	}

}
