package cnuphys.snr;

/**
 * All the parameters needed for noise reduction. Each superlayer should have
 * its own object. Now should be thread safe.
 * 
 * @author heddle
 */
public class NoiseReductionParameters {

	// track leaning directions
	public static final int LEFT_LEAN = 0;
	public static final int RIGHT_LEAN = 1;

	// number of words needed for the number of wires in a layer
	private int _numWordsNeeded;

	/** the number of layers per superlayer */
	private int _numLayer;

	/** the number of wires per layer */
	private int _numWire;

	/** The number of missing layers allowed */
	private int _allowedMissingLayers;

	/**
	 * The shifts for left leaning tracks.
	 */
	private int _leftLayerShifts[];

	/**
	 * The shifts for right leaning tracks.
	 */
	private int _rightLayerShifts[];

	/**
	 * cumulative left segments. These are "results". When
	 * analysis is complete, this will contain an on bit
	 * at any location in layer 1 that is a potential start
	 * of a left leaning segment.
	 */
	private ExtendedWord _leftSegments;

	/**
	 * cumulative right segments. These are "results". When
	 * analysis is complete, this will contain an on bit
	 * at any location in layer 1 that is a potential start
	 * of a right leaning segment.
	 */
	private ExtendedWord _rightSegments;

	/**
	 * A workspace for a copy of the data.
	 */
	private ExtendedWord _copy[];

	/**
	 * A workspace used for storing a reservoir of misses. When
	 * the analysis is done, this can be used to determine a quality
	 * factor for the potential left segment, based on the number of
	 * misses that had to be used.
	 */
	private ExtendedWord _leftMisses[];
	
	/**
	 * A workspace used for storing a reservoir of misses. When
	 * the analysis is done, this can be used to determine a quality
	 * factor for the potential right segment, based on the number of
	 * misses that had to be used.
	 */
	private ExtendedWord _rightMisses[];

	/**
	 * More workspace. This array has numLayers + 1 entries.
	 */
	private ExtendedWord _workSpace[];

	/**
	 * This is the actual data. Before noise reduction analysis is run, this
	 * contains all the hits. After the analysis, the noise hits are removed.
	 */
	private ExtendedWord _packedData[];

	// keep a copy of the raw data
	private ExtendedWord _rawData[];

	// flag that specifies whether the data have been analyzed
	private boolean _analyzed = false;

	/**
	 * Create the parameters used for SNR analysis
	 */
	public NoiseReductionParameters() {
	}

	/**
	 * Create a NoiseReductionParameter using defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts      the shifts for left leaning tracks. Length should
	 *                             equal numLayers.
	 * @param rightLayerShifts     the shifts for right leaning tracks. Length
	 *                             should equal numLayers.
	 */
	public NoiseReductionParameters(int allowedMissingLayers, int[] leftLayerShifts, int[] rightLayerShifts) {
		this(6, 112, allowedMissingLayers, leftLayerShifts, rightLayerShifts);
	}

	/**
	 * Create a NoiseReductionParameter using defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts      the shifts for left leaning tracks. Length should
	 *                             equal numLayers.
	 * @param rightLayerShifts     the shifts for right leaning tracks. Length
	 *                             should equal numLayers.
	 */
	public NoiseReductionParameters(int numLayer, int numWire, int allowedMissingLayers, int[] leftLayerShifts,
			int[] rightLayerShifts) {
		_numLayer = numLayer;
		_numWire = numWire;
		_allowedMissingLayers = allowedMissingLayers;
		_leftLayerShifts = leftLayerShifts.clone();
		_rightLayerShifts = rightLayerShifts.clone();
		createWorkSpace();
	}

	/**
	 * Get a default set of parameters with CLAS-like numbers
	 * 
	 * @return a default set of parameters
	 */
	public static NoiseReductionParameters getDefaultParameters() {
		int numLay = 6;
		int numWire = 112;
		int numMissing = 2;
		int rightShifts[] = { 0, 3, 4, 4, 5, 5 };
		int leftShifts[] = { 0, 3, 4, 4, 5, 5 };
		return new NoiseReductionParameters(numLay, numWire, numMissing, leftShifts, rightShifts);
	}


	/**
	 * Get the number of layers per superlayer
	 * 
	 * @return the number of layers per superlayer
	 */
	public int getNumLayer() {
		return _numLayer;
	}

	/**
	 * Get the number of wires per layer
	 * 
	 * @return the number of wires per layer
	 */
	public int getNumWire() {
		return _numWire;
	}

	/**
	 * Copy those parameters that can be edited
	 * 
	 * @param source the source object.
	 */
	public void copyEditableParameters(NoiseReductionParameters source) {
		_allowedMissingLayers = source._allowedMissingLayers;
		_leftLayerShifts = source._leftLayerShifts.clone();
		_rightLayerShifts = source._rightLayerShifts.clone();
	}

	/**
	 * Create all the workspace needed to remove the noise.
	 */
	public void createWorkSpace() {
		int needed = 1 + (_numWire - 1) / 64;
		if (needed != _numWordsNeeded) {

			_numWordsNeeded = needed;

			// the segments
			_leftSegments = new ExtendedWord(_numWire);
			_rightSegments = new ExtendedWord(_numWire);

			_packedData = new ExtendedWord[_numLayer];
			_rawData = new ExtendedWord[_numLayer];
			_copy = new ExtendedWord[_numLayer];
			_leftMisses = new ExtendedWord[_numLayer];
			_rightMisses = new ExtendedWord[_numLayer];
			_workSpace = new ExtendedWord[_allowedMissingLayers + 1];
			
			
			for (int layer = 0; layer < _numLayer; layer++) {
				_packedData[layer] = new ExtendedWord(_numWire);
				_rawData[layer] = new ExtendedWord(_numWire);
				_copy[layer] = new ExtendedWord(_numWire);
				_leftMisses[layer] = new ExtendedWord(_numWire);
				_rightMisses[layer] = new ExtendedWord(_numWire);
			}

			for (int i = 0; i <= _allowedMissingLayers; i++) {
				_workSpace[i] = new ExtendedWord(_numWire);
			}

		}
	}

	/**
	 * Checks whether a given wire has a noise hit. Only sensible if analysis is
	 * complete.
	 * 
	 * @param layer the 0-based layer 0..5
	 * @param wire  the 0-base wire 0..
	 * @return true if this was a noise hit--i.e., it is in the raw data but not
	 *         the analyzed data
	 */
	public boolean isNoiseHit(int layer, int wire) {
		if (_analyzed) {
			boolean inRaw = _rawData[layer].checkBit(wire);
			if (inRaw) {
				boolean inPacked = _packedData[layer].checkBit(wire);
				return !inPacked;
			}
		}
		return false;
	}

	/**
	 * Clear all data
	 */
	public void clear() {
		for (int layer = 0; layer < _numLayer; layer++) {
			_packedData[layer].clear();
		}
		_leftSegments.clear();
		_rightSegments.clear();
		_analyzed = false;
	}

	/**
	 * Returns the number of allowed missing layers.
	 * 
	 * @return the number of allowed missing layers.
	 */
	public int getAllowedMissingLayers() {
		return _allowedMissingLayers;
	}

	/**
	 * Get the layer shifts for left leaning tracks.
	 * 
	 * @return the layer shifts for left leaning tracks.
	 */
	public int[] getLeftLayerShifts() {
		return _leftLayerShifts;
	}

	/**
	 * Set the left layer shifts used for left leaning segments
	 * 
	 * @param shifts the left layer shifts
	 */
	public void setLeftLayerShifts(int[] shifts) {
		_leftLayerShifts = shifts.clone();
	}

	/**
	 * Get the layer shifts for right leaning tracks.
	 * 
	 * @return the layer shifts for right leaning tracks.
	 */
	public int[] getRightLayerShifts() {
		return _rightLayerShifts;
	}

	/**
	 * Set the right layer shifts used for right leaning segments
	 * 
	 * @param shifts the right layer shifts
	 */
	public void setRightLayerShifts(int[] shifts) {
		_rightLayerShifts = shifts.clone();
	}

	/**
	 * Get the left leaning segment staring wire positions. This is meaningful only
	 * if the analysis has been performed.
	 * 
	 * @return the left leaning segment staring wire positions.
	 */
	public ExtendedWord getLeftSegments() {
		return _leftSegments;
	}

	/**
	 * Get the right leaning segment staring wire positions.This is meaningful only
	 * if the analysis has been performed.
	 * 
	 * @return the right leaning segment staring wire positions.
	 */
	public ExtendedWord getRightSegments() {
		return _rightSegments;
	}

	/**
	 * Get the packed data arrays.
	 * 
	 * @return the packedData this may be raw or may have had the noise removed--use
	 *         "is analyzed" to distinguish
	 */
	public ExtendedWord[] getPackedData() {
		return _packedData;
	}

	/**
	 * Get the packed data for a specific layer
	 * 
	 * @param layer the 0-based layer
	 * @return the packed data for the given layer
	 */
	public ExtendedWord getPackedData(int layer) {
		return _packedData[layer];
	}

	/**
	 * Get the raw data arrays.
	 * 
	 * @return the raw data
	 */
	public ExtendedWord[] getRawData() {
		return _rawData;
	}

	/**
	 * Get the raw data for a specific layer
	 * 
	 * @param layer the 0-based layer
	 * @return the raw data for the given layer
	 */
	public ExtendedWord getRawData(int layer) {
		return _rawData[layer];
	}

	/**
	 * pack a hit
	 * 
	 * @param layer the 0-based layer
	 * @param wire  the 0-based wire
	 */
	public void packHit(int layer, int wire) {
		_packedData[layer].setBit(wire);
	}


	/**
	 * Set new raw data. The analyzed flag is set to false.
	 * 
	 * @param packedData the packedData to set. This should be new raw data.
	 */
	public void setPackedData(ExtendedWord[] packedData) {
		_packedData = packedData;
		_analyzed = false;
	}

	/**
	 * @return the analyzed flag. If <code>tue</code> the data have been anaylzed,
	 *         and noise bits removed from the packedData arrays.
	 */
	public boolean isAnalyzed() {
		return _analyzed;
	}

	/**
	 * Remove the noise. This is the actual algorithm.
	 * 
	 */
	public void removeNoise() {
		
		// keep a copy of the raw data. Not needed but convenient.

		for (int layer = 0; layer < _numLayer; layer++) {
			ExtendedWord.copy(_packedData[layer], _rawData[layer]);
		}

		// first find the left and then the right leaning segments
		findPossibleSegments(LEFT_LEAN);
		findPossibleSegments(RIGHT_LEAN);

		//now clean the data (remove the noise)
		cleanFromSegments();
		_analyzed = true;
	}

	// this creates the masks and .ANDS. them with the data
	private void cleanFromSegments() {
		// now remove the noise first. Set packedData[0] to contain overlap
		// (union) of both sets of segments and its own hits.
		// NOTE: the first layer (layer 0) NEVER has a layer shift.*/
		ExtendedWord.bitwiseOr(_leftSegments, _rightSegments, _copy[0]);
		ExtendedWord.bitwiseAnd(_packedData[0], _copy[0], _packedData[0]);

		// start loop at 1 since layer 0 never bled
		for (int i = 1; i < _numLayer; i++) {

			// copy segments onto a given layer and bleed to create left and
			// right buckets

			ExtendedWord.copy(_leftSegments, _copy[i]);
			_copy[i].bleedLeft(_leftLayerShifts[i]);

			ExtendedWord.copy(_rightSegments, _workSpace[0]);
			_workSpace[0].bleedRight(_rightLayerShifts[i]);

			// combine left and right buckets
			ExtendedWord.bitwiseOr(_copy[i], _workSpace[0], _copy[i]);

			// now get overlap of original data with buckets
			ExtendedWord.bitwiseAnd(_packedData[i], _copy[i], _packedData[i]);
		}
	}


	/**
	 * Find possible segments.
	 * 
	 * @param data       the actual data.
	 * @param parameters the parameters and workspace.
	 * @param direction  either left or right.
	 */
	private void findPossibleSegments(int direction) {
		
		ExtendedWord misses[] = (direction == LEFT_LEAN) ? _leftMisses : _rightMisses;

		// set misses to all 1's. That makes our "reservoir" of misses
		for (int i = 0; i < _allowedMissingLayers; i++) {
			misses[i].fill();
		}

		ExtendedWord segments = null;

		// copy the data. Bleed based on lean. If looking for right leaners,
		// bleed left to try to find a complete "vertical" segment. Similarly
		// for left leaners--bleed right.
		// segments start out as copy of first layer.
		if (direction == LEFT_LEAN) {
			segments = _leftSegments;
			for (int i = 0; i < _numLayer; i++) {
				ExtendedWord.copy(_packedData[i], _copy[i]);
				_copy[i].bleedRight(_leftLayerShifts[i]);
			}
			ExtendedWord.copy(_packedData[0], segments);

		} else { // right leaners
			segments = _rightSegments;
			for (int i = 0; i < _numLayer; i++) {
				ExtendedWord.copy(_packedData[i], _copy[i]);
				_copy[i].bleedLeft(_rightLayerShifts[i]);
			}
			ExtendedWord.copy(_packedData[0], segments);
		}

		// now .AND. the other layers, which have been shifted to accommodate
		// the buckets
		int numCheck = 0;

		for (int i = 0; i < getNumLayer();) {
			if (i > 0) {
				ExtendedWord.bitwiseAnd(segments, _copy[i], segments);
			}

			// Now take missing layers into account. missingLayers
			// is the max number of missing layers allowed. However
			// there is no need to check more misses the layer that we
			// are presently investigating.

			if (++i < _allowedMissingLayers) { //note from this step i is the "NEXT" layer
				numCheck = i;
			} else {
				numCheck = _allowedMissingLayers;
			}

			// note: numCheck is always > 0 unless a level shift is set
			// to zero which is unlikely. (in which case segments will
			// be unnecessarily copied onto workspace[0] and back again.
			// The algorithm still would work.

			ExtendedWord.copy(segments, _workSpace[0]);
			for (int j = 0; j < numCheck; j++) {

				// first step: use whatever misses are left for this j
				ExtendedWord.bitwiseOr(_workSpace[j], misses[j], _workSpace[j + 1]);

				// second step: remove used up misses
				ExtendedWord.bitwiseAnd(misses[j], _workSpace[j], misses[j]);
				
			}
			ExtendedWord.copy(_workSpace[numCheck], segments);

		} /* end of layer loop */
	}
	
	/**
	 * Get the number of missing layers used to find a segment candidate
	 * starting at the given wire in layer 1 (1..6)
	 * @param direction LEFT_LEAN (0) or RIGHT_LEAN (1)
	 * @param wire the 0-based wire
	 * @return the number of missing layers used at that position
	 */
	public int missingLayersUsed(int direction, int wire) {
		ExtendedWord misses[] = (direction == LEFT_LEAN) ? _leftMisses : _rightMisses;
		
		int numUsed = 0;
		
		for (int lay = 0; lay < _allowedMissingLayers; lay++) {			

			if (misses[lay].checkBit(wire)) {
				return numUsed;
			}
			
			numUsed++;
		}
		
		return numUsed;
	}

	/**
	 * Get the occupancy of the raw data. This should only be used by ced proper,
	 * not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getRawOccupancy() {
		return getOccupancy(_rawData);
	}

	/**
	 * Get the occupancy of the packed data. This should only be used by ced proper,
	 * not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getNoiseReducedOccupancy() {
		return getOccupancy(_packedData);
	}

	/**
	 * Get the occupancy of a set of chamber data. This should only be used by ced
	 * proper, not the test program.
	 * 
	 * @param data either the raw or packed data.
	 * @return the occupancy. Multiply by 100 to express as percent.
	 */
	private double getOccupancy(ExtendedWord data[]) {
		int numBits = hitCount(data);
		int numWires = GeoConstants.NUM_LAYER * GeoConstants.NUM_WIRE;
		return ((double) numBits) / numWires;
	}

	/**
	 * Set the number of allowed missing layers.
	 * 
	 * @param allowedMissingLayers the number to set
	 */
	public void setAllowedMissingLayers(int allowedMissingLayers) {
		_allowedMissingLayers = allowedMissingLayers;
	}

	private int hitCount(ExtendedWord data[]) {
		int numBits = 0;
		for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
			numBits += data[layer].bitCount();
		}
		return numBits;
	}

	/**
	 * Total number of raw hits (all layers in this chamber/superlayer)
	 * 
	 * @return number of raw hits
	 */
	public int totalRawHitCount() {
		return (hitCount(_rawData));
	}

	/**
	 * Total number of noise reduced hits (all layers in this chamber/superlayer)
	 * 
	 * @return number of noise reduced hits
	 */
	public int totalReducedHitCount() {
		return (hitCount(_packedData));
	}

}
