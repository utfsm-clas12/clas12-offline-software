package cnuphys.snr;

import java.util.ArrayList;
import java.util.List;

public class SNRClusterFinder {
	
	private static double SLOPE_THRESHOLD = 0.21;
	
	public static final int LEFT_LEAN = NoiseReductionParameters.LEFT_LEAN;
	public static final int RIGHT_LEAN = NoiseReductionParameters.RIGHT_LEAN;


	//the parameters
	private NoiseReductionParameters _params;
	
	//the collections of clusters
	protected ArrayList<SNRCluster> leftClusters;
	protected ArrayList<SNRCluster> rightClusters;

	/**
	 * Create a cluster finder, tied to a parameters object
	 * @param params the owner of the cluster object
	 */
	public SNRClusterFinder(NoiseReductionParameters params) {
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
			SNRCluster currentCluster = null;
			
			int maxCount = _params.maxShift(LEFT_LEAN) + 1;
			int count = 0;
			
			for (int wire = 0; wire < _params.getNumWire(); wire++) {
				if (_params.leftSegments.checkBit(wire)) {
					if (!connected) { //create a new one
						currentCluster = createCluster(LEFT_LEAN);
						leftClusters.add(currentCluster);
						count = 0;
						connected = true;
					}
					currentCluster.addSegmentStart(wire, _params.missingLayersUsed(LEFT_LEAN, wire));
					
					fillWireLists(currentCluster, wire, LEFT_LEAN);
					
					count++;
					connected = count < maxCount;
					
					if (!connected) {
						checkCluster(currentCluster, LEFT_LEAN);
					}
				} //end wire was hit (checkBit)
				else {
					checkCluster(currentCluster, LEFT_LEAN);
					connected = false;
					currentCluster = null;
				}
			} //wire loop
		}
		
		//right clusters
		if (!_params.rightSegments.isZero()) {
			boolean connected = false;
			SNRCluster currentCluster = null;
			
			int maxCount = _params.maxShift(RIGHT_LEAN) + 1;
			int count = 0;

			
			for (int wire = (_params.getNumWire()-1); wire >=0; wire--) {
				if (_params.rightSegments.checkBit(wire)) {
					if (!connected) { //create a new one
						currentCluster = createCluster(RIGHT_LEAN);
						rightClusters.add(currentCluster);
						count = 0;
						connected = true;
					}
					currentCluster.addSegmentStart(wire, _params.missingLayersUsed(RIGHT_LEAN, wire));
					fillWireLists(currentCluster, wire, RIGHT_LEAN);
					
					count++;
					connected = count < maxCount;
					
					if (!connected) {
						checkCluster(currentCluster, RIGHT_LEAN);
					}

				} //end wire was hit (checkBit)
				else {
					checkCluster(currentCluster, RIGHT_LEAN);
					connected = false;
					currentCluster = null;
				}
			} //wire loop
		}

		//split clusters
		
	}
	
	//trim rows, check slope
	private void checkCluster(SNRCluster cluster, int direction) {
		if (cluster == null) {
			return;
		}
		
		cluster.clean();
		
		if (direction == LEFT_LEAN) {
			//slopetest
			if (cluster.getSlope() < -SLOPE_THRESHOLD) {
				leftClusters.remove(cluster);
			}
		}
		else {
			//slopetest
			if (cluster.getSlope() > SLOPE_THRESHOLD) {
				rightClusters.remove(cluster);
			}
		}
	}
	
	//create a cluster
	private SNRCluster createCluster(int direction) {
		return new SNRCluster(_params.getNumLayer(),  _params.getNumWire(), direction);
	}
	
	//fill the wire lists
	private void fillWireLists(SNRCluster cluster, int segStartWire, int direction) {
		for (int lay = 0; lay < _params.getNumLayer(); lay++) {
			_params.addHitsInMask(lay, segStartWire, direction, cluster.wireLists[lay]);
		}

	}
	
	
	/**
	 * Get the list of left leaning clusters
	 * @return left leaning clusters
	 */
	public ArrayList<SNRCluster> getLeftClusters() {
		return leftClusters;
	}
	
	/**
	 * Get the list of right leaning clusters
	 * @return right leaning clusters
	 */
	public ArrayList<SNRCluster> getRightClusters() {
		return rightClusters;
	}

}
