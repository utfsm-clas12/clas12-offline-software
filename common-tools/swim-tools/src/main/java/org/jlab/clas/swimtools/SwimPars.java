/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.swimtools;

import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author ziegler
 */

public class SwimPars {

    public double _x0;
    public double _y0;
    public double _z0;
    public double _phi;
    public double _theta;
    public double _pTot;
    public final double _rMax = 5 + 3; // increase to allow swimming to outer
    // detectors
    public double _maxPathLength = 9;
    public boolean SwimUnPhys = false; //Flag to indicate if track is swimmable
    public int _charge;

    public double SWIMZMINMOM = 0.75; // GeV/c
    public double MINTRKMOM = 0.05; // GeV/c
    public double accuracy = 20e-6; // 20 microns

    public ProbeCollection PC;
    
    /**
     * Class for swimming to various surfaces.  The input and output units are cm and GeV/c
     */
    public SwimPars() {
        PC = Swimmer.getProbeCollection(Thread.currentThread());
        if (PC == null) {
            PC = new ProbeCollection();
            Swimmer.put(Thread.currentThread(), PC);
        }
    }

    /**
     *
     * @param direction
     *            +1 for out -1 for in
     * @param x0
     * @param y0
     * @param z0
     * @param thx
     * @param thy
     * @param p
     * @param charge
     */
    public void SetSwimParameters(int direction, double x0, double y0, double z0, double thx, double thy, double p,
                    int charge) {
        
        // x,y,z in m = swimmer units
        _x0 = x0 / 100;
        _y0 = y0 / 100;
        _z0 = z0 / 100;
        this.checkR(_x0, _y0, _z0);
        double pz = direction * p / Math.sqrt(thx * thx + thy * thy + 1);
        double px = thx * pz;
        double py = thy * pz;
        _phi = Math.toDegrees(FastMath.atan2(py, px));
        _pTot = Math.sqrt(px * px + py * py + pz * pz);
        _theta = Math.toDegrees(Math.acos(pz / _pTot));

        _charge = direction * charge;
    }

    /**
     * Sets the parameters used by swimmer based on the input track state vector
     * parameters swimming outwards
     *
     * @param superlayerIdx
     * @param layerIdx
     * @param x0
     * @param y0
     * @param thx
     * @param thy
     * @param p
     * @param charge
     */
    public void SetSwimParameters(int superlayerIdx, int layerIdx, double x0, double y0, double z0, double thx,
                    double thy, double p, int charge) {
        // z at a given DC plane in the tilted coordinate system
        // x,y,z in m = swimmer units
        _x0 = x0 / 100;
        _y0 = y0 / 100;
        _z0 = z0 / 100;
        this.checkR(_x0, _y0, _z0);
        double pz = p / Math.sqrt(thx * thx + thy * thy + 1);
        double px = thx * pz;
        double py = thy * pz;
        _phi = Math.toDegrees(FastMath.atan2(py, px));
        _pTot = Math.sqrt(px * px + py * py + pz * pz);
        _theta = Math.toDegrees(Math.acos(pz / _pTot));

        _charge = charge;

    }

    /**
     * Sets the parameters used by swimmer based on the input track parameters
     *
     * @param x0
     * @param y0
     * @param z0
     * @param px
     * @param py
     * @param pz
     * @param charge
     */
    public void SetSwimParameters(double x0, double y0, double z0, double px, double py, double pz, int charge) {
        _x0 = x0 / 100;
        _y0 = y0 / 100;
        _z0 = z0 / 100;
         this.checkR(_x0, _y0, _z0);
        _phi = Math.toDegrees(FastMath.atan2(py, px));
        _pTot = Math.sqrt(px * px + py * py + pz * pz);
        _theta = Math.toDegrees(Math.acos(pz / _pTot));

        _charge = charge;

    }

    /**
     * 
     * @param xcm
     * @param ycm
     * @param zcm
     * @param phiDeg
     * @param thetaDeg
     * @param p
     * @param charge
     * @param maxPathLength
     */
    public void SetSwimParameters(double xcm, double ycm, double zcm, double phiDeg, double thetaDeg, double p,
                    int charge, double maxPathLength) {

        _maxPathLength = maxPathLength;
        _charge = charge;
        _phi = phiDeg;
        _theta = thetaDeg;
        _pTot = p;
        _x0 = xcm / 100;
        _y0 = ycm / 100;
        _z0 = zcm / 100;

    }
    
    private void printV(String pfx, double v[]) {
            double x = v[0] / 100;
            double y = v[1] / 100;
            double z = v[2] / 100;
            double r = Math.sqrt(x * x + y * y + z * z);
            System.out.println(String.format("%s: (%-8.5f, %-8.5f, %-8.5f) R: %-8.5f", pfx, z, y, z, r));
    }

    /**
     * 
     * @param sector
     * @param x_cm
     * @param y_cm
     * @param z_cm
     * @param result B field components in T in the tilted sector system
     */
    public void Bfield(int sector, double x_cm, double y_cm, double z_cm, float[] result) {

        PC.RCP.field(sector, (float) x_cm, (float) y_cm, (float) z_cm, result);
        // rcompositeField.field((float) x_cm, (float) y_cm, (float) z_cm,
        // result);
        result[0] = result[0] / 10;
        result[1] = result[1] / 10;
        result[2] = result[2] / 10;

    }
    /**
     * 
     * @param x_cm
     * @param y_cm
     * @param z_cm
     * @param result B field components in T in the lab frame
     */
    public void BfieldLab(double x_cm, double y_cm, double z_cm, float[] result) {

        PC.CP.field((float) x_cm, (float) y_cm, (float) z_cm, result);
        result[0] = result[0] / 10;
        result[1] = result[1] / 10;
        result[2] = result[2] / 10;

    }

    private void checkR(double _x0, double _y0, double _z0) {
        if(Math.sqrt(_x0*_x0 + _y0*_y0)>this._rMax || 
                Math.sqrt(_x0*_x0 + _y0*_y0 + _z0*_z0)>this._maxPathLength)
            this.SwimUnPhys=true;
    }
    
    public static void main(String[] args) {
        SwimTest sa = new SwimTest();
        
    }
}
