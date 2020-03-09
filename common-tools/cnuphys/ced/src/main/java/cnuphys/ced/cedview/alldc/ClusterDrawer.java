package cnuphys.ced.cedview.alldc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCCluster;
import cnuphys.ced.event.data.DCClusterList;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.GeoConstants;

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
			if ((cluster != null) && (cluster.hitID != null)) {
				

				int length = 0;
				for (int i = 0; i < cluster.hitID.length; i++) {
					if (cluster.hitID[i] > 0) {
						length++;
					}
					else {
						break;
					}
				}
				
				
				if (length > 0) {
					int sector = cluster.sector;
					int superlayer = cluster.superlayer;
					int layer[] = new int[length];
					int wire[] = new int[length];
					
					for (int i = 0; i < length; i++) {
						int index = cluster.hitID[i]-1;
						layer[i] = DC.getInstance().layer6[index];
						wire[i] = DC.getInstance().wire[index];
					}
					
					drawSingleClusterOfWires(g, container, sector, superlayer, layer, wire);
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
	 * @param layer array of 1-based wires
	 * @param wire array of 1-based wires
	 */
	private void drawSingleClusterOfWires(Graphics g, IContainer container, 
			int sector, int superlayer, int layer[], int wire[]) {
		
		
		Graphics2D g2 = (Graphics2D)g;
		Rectangle sr = new Rectangle();
		Rectangle.Double wr = new Rectangle.Double();
		
		Area area = new Area();
		
		for (int i = 0; i < wire.length; i++) {
			//drawing hack
			int hackSL = (sector < 4) ? superlayer : 7-superlayer;
			_view.getCell(sector, hackSL, layer[i], wire[i], wr);
			
			
			container.worldToLocal(sr, wr);
			area.add(new Area(sr));
		}
		
		if (!area.isEmpty()) {
			g2.setColor(_clusterFillColor);
            g2.fill(area);
            g2.setColor(_clusterLineColor);
            g2.draw(area);
		}

		
	}

}
