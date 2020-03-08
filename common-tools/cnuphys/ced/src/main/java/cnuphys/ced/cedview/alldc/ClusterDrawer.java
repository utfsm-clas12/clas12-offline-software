package cnuphys.ced.cedview.alldc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCCluster;
import cnuphys.ced.event.data.DCClusterList;
import cnuphys.ced.frame.CedColors;

public class ClusterDrawer {
	
	//the parent view
	private AllDCView _view;
	
	//cluster colors
	private static Color _clusterLineColor = Color.black;
	private static Color _clusterFillColor;
	
	/**
	 * A cluster drawer for the all dc view
	 * @param view the all dc parent view
	 */
	public ClusterDrawer(AllDCView view) {
		_view = view;
	}
	
	
	/**
	 * Draw the hit based DC clusters
	 */
	public void drawHBDCClusters(Graphics g, IContainer container) {
		_clusterFillColor = CedColors.HB_COLOR;
		DCClusterList list = DC.getInstance().getHBClusters();
		if (list != null) {
			System.err.println("Drawing " + list.size() + " hit based clusters");
			drawDCClusterList(g, container, list);
		}
	}
	
	/**
	 * Draw the time based DC clusters
	 */
	public void drawTBDCClusters(Graphics g, IContainer container) {
		_clusterFillColor = CedColors.TB_COLOR;
		DCClusterList list = DC.getInstance().getTBClusters();
		if (list != null) {
			System.err.println("Drawing " + list.size() + " time based clusters");
			drawDCClusterList(g, container, list);
		}
	}
	
	/**
	 * Draw the snr left leaning DC clusters
	 */
	public void drawSNRLeftDCClusters(Graphics g, IContainer container) {
		_clusterFillColor = CedColors.SNR_LEFT_COLOR;
	}
	
	/**
	 * Draw the snr right leaning DC clusters
	 */
	public void drawSNRRightDCClusters(Graphics g, IContainer container) {
		_clusterFillColor = CedColors.SNR_RIGHT_COLOR;
	}
	
	//draws the HB or TB clusters
	private void drawDCClusterList(Graphics g, IContainer container, DCClusterList list) {
		
		for (DCCluster cluster : list) {
			int sector = cluster.sector;
			int superlayer = cluster.superlayer;
			
			
			for (int hitId : cluster.hitID) {
				if (hitId > 0) {
					int index = hitId-1;
					int layer = DC.getInstance().layer6[index];
					int wire = DC.getInstance().wire[index];
					
					System.err.println("hitID=" + hitId + "  sector " + sector + " supl " + superlayer + " layer " + layer + "  wire " + wire);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param g
	 * @param container
	 * @param sector 1-based sector
	 * @param superlayer 1-based superlayer
	 * @param wires list of 1-based wires
	 */
	private void drawSingleClusterOfWires(Graphics g, IContainer container, 
			int sector, int superlayer, List<Integer> wires) {
		
		if (wires.isEmpty()) {
			return;
		}
		
		Graphics2D g2 = (Graphics2D)g;
		Rectangle sr = new Rectangle();
		Rectangle.Double wr = new Rectangle.Double();
		
		Area area = new Area();
		
		for (int wire : wires) {
		//	_view.getCell(sector, superlayer, layer, wire, wr);
		}

		
	}

}
