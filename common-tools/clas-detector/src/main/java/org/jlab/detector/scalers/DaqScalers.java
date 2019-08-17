package org.jlab.detector.scalers;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * Read the occasional scaler bank, extract beam charge, livetime, etc.
 *
 * We have at least two relevant scaler hardware boards, STRUCK and DSC2, both
 * readout on helicity flips and with DAQ-busy gating, both decoded into RAW::scaler.
 * This class reads RAW::scaler and converts to more user-friendly information.
 *
 * STRUCK.  Latching on helicity states, zeroed upon readout, with both helicity
 * settle (normally 500 us) and non-settle counts, useful for "instantaneous"
 * livetime, beam charge asymmetry, beam trip studies, ...
 *
 * DSC2.  Integrating since beginning of run, useful for beam charge normalization.
 *
 * @see <a href="https://logbooks.jlab.org/comment/14616">logbook entry</a>
 * and common-tools/clas-detector/doc
 *
 * The EPICS equation for converting Faraday Cup raw scaler S to beam current I:
 *   I [nA] = (S [Hz] - offset ) / slope * attenuation;
 *
 * offset/slope/attenuation are read from CCDB
 *
 * Accounting for the offset in accumulated beam charge requires knowledge of
 * time duration.  Currently, the (32 bit) DSC2 clock is zeroed at run start
 * but at 1 Mhz rolls over every 35 seconds, and the (48 bit) 250 MHz TI timestamp
 * can also rollover within a run since only zeroed upon reboot.  Instead we allow
 * run duration to be passed in, e.g. using run start time from RCDB and event
 * unix time from RUN::config.
 *
 * @author baltzell
 */
public class DaqScalers {

    private float beamCharge=0;
    private float beamChargeGated=0;
    private float livetime=0;
    private long timestamp   = 0;
    public RawReading.Dsc2 dsc2=null;
    public RawReading.Struck struck=null;
    public void setTimestamp(long timestamp) { this.timestamp=timestamp; }
    private void setBeamCharge(float q) { this.beamCharge=q; }
    private void setBeamChargeGated(float q) { this.beamChargeGated=q; }
    private void setLivetime(float l) { this.livetime=l; }
    public float getBeamCharge() { return beamCharge; }
    public float getBeamChargeGated() { return beamChargeGated; }
    public float getLivetime()   { return livetime; }
    public long getTimestamp() { return timestamp; }
    public void show() { System.out.println("BCG=%.3f   LT=%.3f"); }

    public DaqScalers() {}

    /**
    * @param runScalerBank HIPO RUN::scaler bank
    */
    public static DaqScalers create(Bank runScalerBank) {
        DaqScalers ds=new DaqScalers();
        for (int ii=0; ii<runScalerBank.getRows(); ii++) {
            ds.livetime=runScalerBank.getFloat("livetime", ii);
            ds.beamCharge=runScalerBank.getFloat("fcup",ii);
            ds.beamChargeGated=runScalerBank.getFloat("fcupgated",ii);
            break; 
        }
        return ds;
    }

    /**
     * @param rawScalerBank
    * @param seconds duration between run start and current event
     * @param runno
     * @param cm
    * @return DaqScalers object with calibrated beam charge
    */
    public static DaqScalers create(Bank rawScalerBank,
            ConstantsManager cm,
            int runno,
            double seconds) {

        IndexedTable struckTable=cm.getConstants(runno,"/daq/config/scalers/struck12");
        IndexedTable dsc2Table=cm.getConstants(runno,"/daq/config/scalers/dsc1");
        IndexedTable fcupTable=cm.getConstants(runno,"/runcontrol/fcup");
        
        DaqScalers ds=new DaqScalers();
        ds.dsc2=new RawReading.Dsc2(rawScalerBank,dsc2Table);
        ds.struck=new RawReading.Struck(rawScalerBank,struckTable);
        
        // retrieve fcup calibrations:
        final double fcup_slope  = fcupTable.getDoubleValue("slope",0,0,0);
        final double fcup_offset = fcupTable.getDoubleValue("offset",0,0,0);
        final double fcup_atten  = fcupTable.getDoubleValue("atten",0,0,0);

        if (ds.dsc2.getClock() > 0) {

            float live = ds.dsc2.getGatedSlm() / ds.dsc2.getSlm();
            float q  = (float)(ds.dsc2.getFcup()      - fcup_offset * seconds );
            float qg = (float)(ds.dsc2.getGatedFcup() - fcup_offset * seconds * live);
            q  *= fcup_atten / fcup_slope;
            qg *= fcup_atten / fcup_slope;
            float l = -1;
            if (ds.struck.getClock()>0) {
                l = (float)ds.struck.getGatedClock() / ds.struck.getClock();
            }
            ds.setBeamCharge(q);
            ds.setBeamChargeGated(qg);
            ds.setLivetime(l);
            return ds;
        }
        return null;
    }

    /**
    * Same as create(Bank,IndexedTable,double), except relies on DSC2's clock.
    *
     * @param rawScalerBank
     * @param cm
     * @param runno
     * @return 
    */
    public static DaqScalers create(Bank rawScalerBank,ConstantsManager cm,int runno) {
        IndexedTable dsc2Table=cm.getConstants(runno,"/daq/config/dsc1");
        RawReading.Dsc2 dsc2=new RawReading.Dsc2(rawScalerBank,dsc2Table);
        return create(rawScalerBank,cm,runno,dsc2.getGatedClockTime());
    }

}
