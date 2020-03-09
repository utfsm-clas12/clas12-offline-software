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
import cnuphys.ced.noise.NoiseManager;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.SNRCluster;
import cnuphys.snr.WireList;

public class ClusterDrawer {
	
	//the parent view
	private AllDCView _view;
	
	//cluster colors
	private static Color _clusterLineColor = Color.cyan;
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
		NoiseManager nm = NoiseManager.getInstance();
		
		int ntot = 0;
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {
				NoiseReductionParameters params = nm.getParameters(sect0, supl0);
				List<SNRCluster> leftClusters = params.getLeftClusters();
				if (leftClusters != null) {
					for (SNRCluster cluster : leftClusters) {
						for (int lay0 = 0; lay0< 6; lay0++) {
							drawSingleSNRClusterOfWires(g, container, sect0, supl0, lay0, 
									cluster.wireLists[lay0]);
						}

					}
					ntot += leftClusters.size();
				}
			}
			
		}
		System.err.println("Drew " + ntot +  "  left SNR clusters");
	}
	
	/**
	 * Draw the snr right leaning DC clusters
	 */
	public void drawSNRRightDCClusters(Graphics g, IContainer container) {
		_clusterFillColor = CedColors.SNR_RIGHT_COLOR;
		NoiseManager nm = NoiseManager.getInstance();
		
		int ntot = 0;
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {
				NoiseReductionParameters params = nm.getParameters(sect0, supl0);
				List<SNRCluster> rightClusters = params.getRightClusters();
				if (rightClusters != null) {
					
					for (SNRCluster cluster : rightClusters) {
						for (int lay0 = 0; lay0< 6; lay0++) {
							drawSingleSNRClusterOfWires(g, container, sect0, supl0, lay0, 
									cluster.wireLists[lay0]);
						}

					}
					ntot += rightClusters.size();
				}
			}
			
		}
		System.err.println("Drew " + ntot +  "  right SNR clusters");

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
	
	/**
	 * 
	 * @param g
	 * @param container
	 * @param sector 0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer 0-based layer
	 * @param wireList list of 0-based wires
	 */
	private void drawSingleSNRClusterOfWires(Graphics g, IContainer container, 
			int sector, int superlayer, int layer, WireList wireList) {
		
		
		Graphics2D g2 = (Graphics2D)g;
		Rectangle sr = new Rectangle();
		Rectangle.Double wr = new Rectangle.Double();
		
		Area area = new Area();
		
		sector += 1;
		superlayer += 1;
		layer += 1;
		
		for (int wire : wireList) {
			//drawing hack
			int hackSL = (sector < 4) ? superlayer : 7-superlayer;
			_view.getCell(sector, hackSL, layer, wire+1, wr);
			
			
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
