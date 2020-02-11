package cnuphys.ced.cedview.alldc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class AllDCDisplayPanel extends JPanel implements ActionListener {
	
	// the parent view
	private AllDCView _view;
	
	//the toggle buttons
	private JToggleButton _rawHitsButton;
	private JToggleButton _hbHitsButton;
	private JToggleButton _tbHitsButton;
	private JToggleButton _nnHitsButton;

	public AllDCDisplayPanel(AllDCView view) {
		_view =  view;
		setup();
	}
	
	//create and lawout the components
	private void setup() {
		setLayout(new BorderLayout(4, 4));
		
		setBorder(new CommonBorder("Hit Display Control"));
	}
	
	/**
	 * Display raw DC hits?
	 * @return <code> if we should display raw hits
	 */
	public  boolean showRawHits() {
		return _rawHitsButton.isSelected();
	}
	
	/**
	 * Display hit based hits?
	 * @return <code> if we should display hit based hits
	 */
	public  boolean showHBHits() {
		return _hbHitsButton.isSelected();
	}
	
	/**
	 * Display time based hits?
	 * @return <code> if we should display hits
	 */
	public  boolean showTBHits() {
		return _tbHitsButton.isSelected();
	}
	
	/**
	 * Display neural net marked hits?
	 * @return <code> if we should display neural net marked hits
	 */
	public  boolean showNNHits() {
		return _nnHitsButton.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_view.refresh();
	}
}
