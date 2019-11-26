package org.jlab.service.dc;

import cnuphys.magfield.MagneticFields;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;

import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.CLASResources;
import org.jlab.utils.system.ClasUtilsFile;


/**
 *
 * @author naharrison
 */
public class DCReconstructionTest {

  @Test
  public void testDCReconstruction() {
    System.setProperty("CLAS12DIR", "../../");
    
    String mapDir = CLASResources.getResourcePath("etc")+"/data/magfield";
    try {
        MagneticFields.getInstance().initializeMagneticFields(mapDir,
                "Symm_torus_r2501_phi16_z251_24Apr2018.dat","Symm_solenoid_r601_phi1_z1201_13June2018.dat");
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    
    String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
    SchemaFactory schemaFactory = new SchemaFactory();
    schemaFactory.initFromDirectory(dir);
		
    DataEvent testEvent = TestEvent.getDCSector1ElectronEvent(schemaFactory);

    MagFieldsEngine enf = new MagFieldsEngine();
    enf.init();
    enf.processDataEvent(testEvent);
    DCHBEngine engineHB = new DCHBEngine();
    engineHB.init();
    engineHB.processDataEvent(testEvent);
    if(testEvent.hasBank("DCHB::tracks")) {
        testEvent.getBank("DCHB::tracks").show();
    }
    
    assertEquals(testEvent.hasBank("DCHB::tracks"), true);
    assertEquals(testEvent.getBank("DCHB::tracks").rows(), 1);
    assertEquals(testEvent.getBank("DCHB::tracks").getByte("q", 0), -1);
    assertEquals(ClasMath.isWithinXPercent(16.0, testEvent.getBank("DCHB::tracks").getFloat("px", 0), 1.057), true);
    assertEquals(testEvent.getBank("DCHB::tracks").getFloat("py", 0) > -0.1, true);
    assertEquals(testEvent.getBank("DCHB::tracks").getFloat("py", 0) < 0.1, true);
    assertEquals(ClasMath.isWithinXPercent(16.0, testEvent.getBank("DCHB::tracks").getFloat("pz", 0), 2.266), true);

    DCTBEngine engineTB = new DCTBEngine();
    engineTB.init();
    engineTB.processDataEvent(testEvent);
    if(testEvent.hasBank("DCTB::tracks")) {
        testEvent.getBank("DCTB::tracks").show();
    }
    
    assertEquals(testEvent.hasBank("DCTB::tracks"), true);
    assertEquals(testEvent.getBank("DCTB::tracks").rows(), 1);
    assertEquals(testEvent.getBank("DCTB::tracks").getByte("q", 0), -1);
    assertEquals(ClasMath.isWithinXPercent(5.0, testEvent.getBank("DCTB::tracks").getFloat("px", 0), 1.057), true);
    assertEquals(testEvent.getBank("DCTB::tracks").getFloat("py", 0) > -0.05, true);
    assertEquals(testEvent.getBank("DCTB::tracks").getFloat("py", 0) < 0.05, true);
    assertEquals(ClasMath.isWithinXPercent(5.0, testEvent.getBank("DCTB::tracks").getFloat("pz", 0), 2.266), true);
    
    }

}
