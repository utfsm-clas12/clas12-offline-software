package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.rec.tof.track.Track;

/**
 *
 * @author ziegler
 *
 */
public class TrackReader {

    public TrackReader() {
        // TODO Auto-generated constructor stub
    }


    public ArrayList<Track> setTracksFromBank(DataBank bankDC) {

        ArrayList<Track> Tracks = new ArrayList<Track>();

        if(bankDC!=null) {
            // select good tracks
            int rows = bankDC.rows();
            for (int i = 0; i < rows; i++) {
                // if(fitChisq[i]>1)
                // continue; // check this
                int id      = bankDC.getShort("id", i);
                double x    = bankDC.getFloat("xCrossReg3", i);
                double y    = bankDC.getFloat("yCrossReg3", i);
                double z    = bankDC.getFloat("zCrossReg3", i);
                double ux   = bankDC.getFloat("xCrossReg3UnitDir", i);
                double uy   = bankDC.getFloat("yCrossReg3UnitDir", i);
                double uz   = bankDC.getFloat("zCrossReg3UnitDir", i);
                double path = bankDC.getFloat("pathlength", i);

                Line3d line = new Line3d(new Vector3d(x,y,z), new Vector3d(x+5*ux,y+5*uy, z+5*uz));
                Track track = new Track(id,line,path);
                Tracks.add(track);
            }
        }
        return Tracks;
    }
    
    public ArrayList<Track> fetch_Trks(DataEvent event) {

        ArrayList<Track> Tracks = new ArrayList<Track>();

        if (event.hasBank("DCTB::tracks") == true || event.hasBank("DCHB::tracks") == true) {
            DataBank bankDC = null;
            if(event.hasBank("DCTB::tracks") == true) {
                bankDC = event.getBank("DCTB::tracks");
                Tracks = this.setTracksFromBank(bankDC);
            }
            if(event.hasBank("DCHB::tracks")==true && event.hasBank("DCTB::tracks") == false) {
                bankDC = event.getBank("DCHB::tracks");
                Tracks = this.setTracksFromBank(bankDC);
            }
        }
        return Tracks;
    }

}
