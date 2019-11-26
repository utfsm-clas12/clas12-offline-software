package cnuphys.ced.event.data;

public class HBHits extends TbHbHits {
	
	private static HBHits _instance;
	
	private HBHits() {
		super("DCHB::tracks");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static HBHits getInstance() {
		if (_instance == null) {
			_instance = new HBHits();
		}
		return _instance;
	}
	
}
