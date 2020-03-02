package cnuphys.snr.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cnuphys.snr.NoiseReductionParameters;

@SuppressWarnings("serial")
public class NoiseTest extends JFrame {

	private DetectorTest detectorTest;

	private NoiseReductionParameters parameters = NoiseReductionParameters.getDefaultParameters();

	/**
	 * Constructor
	 */
	public NoiseTest() {
		super("Noise and Segment Finding Test");
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		};
		addWindowListener(wa);

		addComponents();

		setSize(1200, 900);
		setLocation(200, 100);
	};

	/**
	 * Add the GUI components
	 */
	public void addComponents() {
		double bw = 10.0;
		double xmin = 0.15;
		double width = bw - 2.0 * xmin;

		double csize = width / parameters.getNumWire();

		double height = parameters.getNumLayer() * csize;

		double dy = height / 6;

		// space fopr two extra superlayer (left/right composite superlayers)
		double bh = (6 + 2.5) * (dy + height) + dy;

		detectorTest = new DetectorTest(parameters, 0.0, 0.0, bw, bh);

		for (int i = 0; i < 6; i++) {
			double y = -0.7 + (i + 1.5) * (3.2*dy + height);
			detectorTest.addChamber(new Rectangle2D.Double(xmin, y, width, height));
		}

		add(detectorTest, BorderLayout.CENTER);

		addMenus();
	}

	/**
	 * Add all the menus.
	 */
	private void addMenus() {
		JMenuBar menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createEventMenu());
		menubar.add(createOptionMenu());
		// menubar.add(createTestMenu());

		addCleanHotSpot(menubar);
		setJMenuBar(menubar);
	}

	private void addCleanHotSpot(JMenuBar menubar) {
		menubar.add(Box.createHorizontalStrut(40));
		final JLabel clean = new JLabel(" Show Cleaned Data ");
		clean.setOpaque(true);
		clean.setBackground(Color.darkGray);
		clean.setForeground(Color.cyan);
		clean.setBorder(BorderFactory.createEtchedBorder());
		menubar.add(clean);

		MouseAdapter ml = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent me) {
				clean.setBackground(Color.darkGray);
				clean.setForeground(Color.yellow);
				TestParameters.noiseOff = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent me) {
				clean.setBackground(Color.darkGray);
				clean.setForeground(Color.cyan);
				TestParameters.noiseOff = false;
				repaint();
			}

		};

		clean.addMouseListener(ml);
	}

	/**
	 * Create the option menu.
	 * 
	 * @return the option menu.
	 */
	private JMenu createOptionMenu() {
		JMenu menu = new JMenu("Options");
		menu.add(detectorTest.getDisplayOptionMenu());
		return menu;
	}

	/**
	 * Create the event menu.
	 * 
	 * @return the event menu.
	 */
	private JMenu createEventMenu() {

		JMenu menu = new JMenu("Event");
		JMenuItem nextItem = new JMenuItem("Next Pretend Event");

		nextItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		nextItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				detectorTest.nextEvent(true);
			}
		});

		menu.add(nextItem);

		JMenuItem sbitem = new JMenuItem("screwball event");
		sbitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				detectorTest.screwballEvent();
				;
			}
		});
		menu.add(sbitem);

		return menu;
	}

	// create the file menu
	private JMenu createFileMenu() {

		JMenu menu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		menu.add(quitItem);
		return menu;
	}

	/**
	 * Main program for testing
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		final NoiseTest noiseTest = new NoiseTest();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				noiseTest.setVisible(true);
			}
		});
	}
}
