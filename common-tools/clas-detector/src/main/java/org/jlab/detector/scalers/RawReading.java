package org.jlab.detector.scalers;

import org.jlab.detector.helicity.HelicityBit;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author baltzell
 */

public abstract class RawReading {

    protected static final String RAWBANKNAME="RAW::scaler";

    protected int crate=64;
    protected double clockFreq=1;   // Hz
    protected long fcup=-1;         // counts
    protected long clock=-1;        // counts
    protected long slm=-1;          // counts
    protected long gatedFcup=-1;    // counts
    protected long gatedClock=-1;   // counts
    protected long gatedSlm=-1;     // counts

    public long   getClock()       { return this.clock; }
    public long   getFcup()        { return this.fcup; }
    public long   getSlm()         { return this.slm; }
    public long   getGatedClock()  { return this.gatedClock; }
    public long   getGatedFcup()   { return this.gatedFcup; }
    public long   getGatedSlm()    { return this.gatedSlm; }
    public double getClockTime()   { return this.clock / this.clockFreq; }
    public double getGatedClockTime() { return this.gatedClock / this.clockFreq; }

    public abstract void setHardwareMapping(IndexedTable it);
    
    public static class Struck extends RawReading {

        private HelicityBit helicity=HelicityBit.UDF;
        private HelicityBit quartet=HelicityBit.UDF;
        public byte getHelicity() { return this.helicity.value(); }
        public byte getQuartet() { return this.quartet.value(); }

        private int slotGated=0;
        private int slotUngated=1;

        // these are the non-settle periods:
        private int chanFcup=0;
        private int chanSlm=1;
        private int chanClock=2;

        // these are the settle periods (currently unused):
        private int chanFcupSettle=32;
        private int chanSlmSettle=33;
        private int chanClockSettle=34;

        /**
         * 
         * @param it CCDB's /daq/config/struck12 
         */
        @Override
        public final void setHardwareMapping(IndexedTable it) {
            crate=it.getIntValue("crate",0,0,0);
            slotGated=it.getIntValue("slotGated",0,0,0);
            slotUngated=it.getIntValue("slotUngated",0,0,0);
            chanFcup=it.getIntValue("chanFcup",0,0,0);
            chanSlm=it.getIntValue("chanSlm",0,0,0);
            chanClock=it.getIntValue("chanClock",0,0,0);
            chanFcupSettle=it.getIntValue("chanFcupSettle",0,0,0);
            chanSlmSettle=it.getIntValue("chanSlmSettle",0,0,0);
            chanClockSettle=it.getIntValue("chanClockSettle",0,0,0);
            clockFreq=it.getDoubleValue("frequency",0,0,0);
        }

        public Struck(Bank bank,IndexedTable it) {

            setHardwareMapping(it);
            
            // this will get the last entries (most recent) in the bank
            for (int k=0; k<bank.getRows(); k++){
                if (bank.getInt("crate",k)!=crate) {
                    continue;
                }
                if (bank.getInt("slot",k)==slotGated) {
                    final int chan=bank.getInt("channel",k);
                    if (chan==chanFcup) {
                        helicity = bank.getByte("helicity",k) > 0 ? HelicityBit.PLUS : HelicityBit.MINUS;
                        quartet = bank.getByte("quartet",k)   > 0 ? HelicityBit.PLUS : HelicityBit.MINUS;
                        gatedFcup = bank.getLong("value",k);
                    }
                    else if (chan==chanSlm) {
                        gatedSlm = bank.getLong("value",k);
                        break;
                    }
                    else if (chan==chanClock) {
                        gatedClock = bank.getLong("value",k);
                    }
                }
                else if (bank.getInt("slot",k)==slotUngated) {
                    final int chan=bank.getInt("channel",k);
                    if (chan==chanFcup) {
                        fcup = bank.getLong("value",k);
                    }
                    else if (chan==chanSlm) {
                        slm = bank.getLong("value",k);
                    }
                    else if (chan==chanClock) {
                        clock = bank.getLong("value",k);
                    }
                }
            }
        }
    }

    public static class Dsc2 extends RawReading{

        private static final boolean GATEINVERTED=true;

        // DSC has TRG and TDC thresholds, we use only TDC here:
        private int slot=64;
        private int chanFcupGated=16;
        private int chanSlmGated=17;
        private int chanClockGated=18;
        private int chanFcup=48;
        private int chanSlm=49;
        private int chanClock=50;

        public Dsc2() {}
        
        /**
         * 
         * @param it CCDB's /daq/config/dsc1 
         */
        @Override
        public final void setHardwareMapping(IndexedTable it) {
            crate=it.getIntValue("crate",0,0,0);
            slot=it.getIntValue("slot",0,0,0);
            chanFcup=it.getIntValue("chanFcup",0,0,0);
            chanSlm=it.getIntValue("chanSlm",0,0,0);
            chanClock=it.getIntValue("chanClock",0,0,0);
            chanFcupGated=it.getIntValue("chanFcupGated",0,0,0);
            chanSlmGated=it.getIntValue("chanSlmGated",0,0,0);
            chanClockGated=it.getIntValue("chanClockGated",0,0,0);
            clockFreq=it.getDoubleValue("frequency",0,0,0);
        }

        public Dsc2(Bank bank,IndexedTable it) {

            setHardwareMapping(it);
            
            // this will get the last entries (most recent) in the bank
            for (int k=0; k<bank.getRows(); k++){

                if (bank.getInt("crate",k)!=crate || bank.getInt("slot",k)!=slot) {
                    continue;
                }
                final int chan=bank.getInt("channel",k);
                if (chan==chanFcupGated) {
                    gatedFcup = bank.getLong("value",k);
                }
                else if (chan==chanSlmGated) {
                    gatedSlm = bank.getLong("value",k);
                }
                else if (chan==chanClockGated) {
                    gatedClock = bank.getLong("value",k);
                }
                else if (chan==chanFcup) {
                    fcup = bank.getLong("value",k);
                }
                else if (chan==chanSlm) {
                    slm = bank.getLong("value",k);
                }
                else if (chan==chanClock) {
                    clock = bank.getLong("value",k);
                }
            }
        }
    }
}

