package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


/**
 *
 * @author baltzell
 */
public class TaggerResponse extends DetectorResponse {
    
    private int     hitID = -1;
    private int     hitSize = -1;
    private double  hitRadius = 0.0;
   
    public int pindex=-1;
    
    private Vector3D hitMomentum = new Vector3D();
    private Point3D  hitWidth = new Point3D();
    
    public void setID(int id){ hitID = id;}
    public void setSize(int q){hitSize = q;}
    public void setRadius(double r) {hitRadius = r;}
    
    public int getSize(){return hitSize;}
    public int getID(){return hitID;}
    public double getRadius() {return this.hitRadius;}
    
    public Vector3D getMomentum(){
        return this.hitMomentum;
    }
    
    public void setMomentum(double px, double py, double pz){
        this.hitMomentum.setXYZ(px, py, pz);
    }
    
    public Point3D getPositionWidth(){
        return this.hitWidth;
    }
    
    public void setPositionWidth(double x, double y, double z){
        this.hitWidth.set(x, y, z);
    }

    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
        String bankName, DetectorType type, int bankType){        
        List<DetectorResponse> responseList = new ArrayList<>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                TaggerResponse ft = new TaggerResponse();
                float dx,dy;
                int id  = bank.getShort("id", row);
                int size = bank.getShort("size", row);
                double x = bank.getFloat("x",row);
                double y = bank.getFloat("y",row);
                double z = bank.getFloat("z",row);
                switch (bankType) {
                    case BANK_TYPE_DET:
                        ft.setHitIndex(row);
                        dx = bank.getFloat("widthX",row);
                        dy = bank.getFloat("widthY",row);
                        break;
                    case BANK_TYPE_DST:
                        ft.setHitIndex(-1);
                        ft.pindex = bank.getShort("pindex", row);
                        dx = bank.getFloat("dx",row);
                        dy = bank.getFloat("dy",row);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                double radius = bank.getFloat("radius", row);
                double time = bank.getFloat("time",row);
                double energy = bank.getFloat("energy",row);
               
                double z0 = 0; // FIXME vertex
                double path = Math.sqrt(x*x+y*y+(z-z0)*(z-z0)); 
                double cx = x / path;
                double cy = y / path;
                double cz = (z-z0) / path;
                ft.setMomentum(energy*cx,energy*cy,energy*cz);

                ft.setSize(size);
                ft.setID(id);
                ft.setEnergy(energy);
                ft.setRadius(radius);
                ft.setTime(time);
                ft.setPosition(x, y, z);
                ft.setPositionWidth(dx, dy, 0);

                ft.getDescriptor().setType(type);
                ft.getDescriptor().setSectorLayerComponent(0,1,0);

                responseList.add((DetectorResponse)ft);
            }
        }
        return responseList;
    }
    
}
