package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


/**
 *
 * @author jnewton
 */
public class ScintillatorResponse extends DetectorResponse {
   
    private float dedx=0;
    
    public float getDedx() { return dedx; }
    public void setDedx(float dedx) { this.dedx=dedx; }
    
    public ScintillatorResponse(){
        super();
    }
    
   public ScintillatorResponse(int sector, int layer, int component){
        this.getDescriptor().setSectorLayerComponent(sector, layer, component);
    }
   
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, int bankType){        

        List<DetectorResponse> responseList = new ArrayList<>();

        if(event.hasBank(bankName)==true){

            DataBank bank = event.getBank(bankName);

            for(int row = 0; row < bank.rows(); row++){

                int sector = bank.getByte("sector", row);
                int layer  = bank.getByte("layer", row);
                int paddle = bank.getShort("component", row);

                ScintillatorResponse  response = new ScintillatorResponse(sector,layer,paddle);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                response.setPosition(x, y, z);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                response.setStatus(bank.getInt("status",row));
                response.getDescriptor().setType(type);

                switch (bankType) {
                    case BANK_TYPE_DET:
                        response.setHitIndex(row);
                        // CND clusters do not have path length in bar (but its hits do!):
                        if (type != DetectorType.CND) {
                            float dx = bank.getFloat("pathLengthThruBar",row);
                            if (dx>0) response.setDedx(bank.getFloat("energy", row)/dx);
                        }
                        break;
                    case BANK_TYPE_DST:
                        response.setHitIndex(-1);
                        response.setDedx(bank.getFloat("dedx", row));
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                
                responseList.add(response);
            }
        }
        return responseList;
    }
    
}
