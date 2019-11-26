package cnuphys.ced.event.data;

public class TBCrosses extends Crosses {
	
	private static TBCrosses _instance;
	
	private TBCrosses() {
		super("DCTB::crosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static TBCrosses getInstance() {
		if (_instance == null) {
			_instance = new TBCrosses();
		}
		return _instance;
	}

}
