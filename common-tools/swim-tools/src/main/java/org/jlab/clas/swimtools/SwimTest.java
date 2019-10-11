/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.swimtools;

import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.geometry.Line;
import cnuphys.rk4.IStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler
 */

public class SwimTest extends SwimPars {

    final double stepSize = 0.01;  //initial ss = 1 cm
    final double eps = 1.0e-6;
    public double[] SwimToPlaneTiltSecSys(int sector, double z_cm) {
        double z = z_cm / 100; // the magfield method uses meters
        double[] value = new double[8];

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }

        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        try {
            
            PC.ARCF.sectorSwimZ(sector, _charge, _x0, _y0, _z0, _pTot, _theta, _phi, z, accuracy, 10,
                            stepSize, eps, swimResult);
           
            traj = swimResult.getTrajectory();
		
            traj.sectorComputeBDL(sector, PC.RCP);

            double lastY[] = swimResult.getTrajectory().lastElement();
            value[0] = lastY[0] * 100; // convert back to cm
            value[1] = lastY[1] * 100; // convert back to cm
            value[2] = lastY[2] * 100; // convert back to cm
            value[3] = lastY[3] * _pTot;
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;
            value[6] = lastY[6] * 100;
            value[7] = lastY[7] * 10;

        } catch (Exception e) {
            System.out.println("what !!!! sectorSwimZ failed...");
                e.printStackTrace();
        }
        return value;

    }
    /**
     * 
     * @param z_cm
     * @return state  x,y,z,px,py,pz, pathlength, iBdl at the plane surface
     */
    public double[] SwimToPlaneLab(double z_cm) {
        double z = z_cm / 100; // the magfield method uses meters
        double[] value = new double[8];

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }

        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        try {
            
            PC.ACF.swimZ(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, z, accuracy, 10,
                            stepSize, eps, swimResult);
           
            traj = swimResult.getTrajectory();
		
            traj.computeBDL(PC.CP);

            double lastY[] = swimResult.getTrajectory().lastElement();
            value[0] = lastY[0] * 100; // convert back to cm
            value[1] = lastY[1] * 100; // convert back to cm
            value[2] = lastY[2] * 100; // convert back to cm
            value[3] = lastY[3] * _pTot;
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;
            value[6] = lastY[6] * 100;
            value[7] = lastY[7] * 10;

        } catch (Exception e) {
            System.out.println("what !!!! sectorZ failed...");
                e.printStackTrace();
        }
        return value;

    }
     /**
     * 
     * @param Rad
     * @return state  x,y,z,px,py,pz, pathlength, iBdl at the surface 
     */
    public double[] SwimToCylinder(double Rad) {
        double rho = Rad / 100; // the magfield method uses meters
        double[] value = new double[8];

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }

        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        try {
            
            PC.ACF.swimRho(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, rho, accuracy, 10,
                            stepSize, eps, swimResult);
           
            traj = swimResult.getTrajectory();
		
            traj.computeBDL(PC.CP);

            double lastY[] = swimResult.getTrajectory().lastElement();
            value[0] = lastY[0] * 100; // convert back to cm
            value[1] = lastY[1] * 100; // convert back to cm
            value[2] = lastY[2] * 100; // convert back to cm
            value[3] = lastY[3] * _pTot;
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;
            value[6] = lastY[6] * 100;
            value[7] = lastY[7] * 10;

        } catch (Exception e) {
                e.printStackTrace();
        }
        return value;

    }

    /**
     * 
     * @param Rad
     * @return state  x,y,z,px,py,pz, pathlength, iBdl at the surface 
     */
    /*
    public double[] SwimToSphere(double Rad) {
        
        double R = Rad / 100; // the magfield method uses meters
        double[] value = new double[8];

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }

        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        try {
            
            PC.ACF.swimRho(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, R, accuracy, _rMax,
                            stepSize, eps, swimResult);
           
            
            if(traj==null)
                return null;

            traj = swimResult.getTrajectory();
		
            traj.computeBDL(PC.CP);

            double lastY[] = swimResult.getUf();
            value[0] = lastY[0] * 100; // convert back to cm
            value[1] = lastY[1] * 100; // convert back to cm
            value[2] = lastY[2] * 100; // convert back to cm
            value[3] = lastY[3] * _pTot;
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;
            value[6] = lastY[6] * 100;
            value[7] = lastY[7] * 10;

        } catch (Exception e) {
                e.printStackTrace();
        }
        return value;

    }
    */
    private class SphericalBoundarySwimStopper implements IStopper {

        private double _finalPathLength = Double.NaN;

        private double _Rad;

        /**
         * A swim stopper that will stop if the boundary of a plane is crossed
         *
         * @param maxR
         *            the max radial coordinate in meters.
         */
        private SphericalBoundarySwimStopper(double Rad) {
                // DC reconstruction units are cm. Swim units are m. Hence scale by
                // 100
                _Rad = Rad;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {

                double r = Math.sqrt(y[0] * y[0] + y[1] * y[1] + y[2] * y[2]) * 100.;

                return (r > _Rad);

        }

        /**
         * Get the final path length in meters
         *
         * @return the final path length in meters
         */
        @Override
        public double getFinalT() {
                return _finalPathLength;
        }

        /**
         * Set the final path length in meters
         *
         * @param finalPathLength
         *            the final path length in meters
         */
        @Override
        public void setFinalT(double finalPathLength) {
                _finalPathLength = finalPathLength;
        }
    }
    /**
     * 
     * @param Rad
     * @return state  x,y,z,px,py,pz, pathlength, iBdl at the surface 
     */
    public double[] SwimToSphere(double Rad) {

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }
        double[] value = new double[8];
        
        SphericalBoundarySwimStopper stopper = new SphericalBoundarySwimStopper(Rad);

        SwimTrajectory st = PC.CF.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, stopper, _maxPathLength, stepSize,
                        0.0005);
        if(st==null)
            return null;
        st.computeBDL(PC.CP);
        // st.computeBDL(compositeField);

        double[] lastY = st.lastElement();

        value[0] = lastY[0] * 100; // convert back to cm
        value[1] = lastY[1] * 100; // convert back to cm
        value[2] = lastY[2] * 100; // convert back to cm
        value[3] = lastY[3] * _pTot; // normalized values
        value[4] = lastY[4] * _pTot;
        value[5] = lastY[5] * _pTot;
        value[6] = lastY[6] * 100;
        value[7] = lastY[7] * 10; // Conversion from kG.m to T.cm

        return value;

    }

    /**
     * 
     * @param d_cm
     * @param n
     * @param dir
     * @return return state  x,y,z,px,py,pz, pathlength, iBdl at the plane surface in the lab frame
     */
    public double[] SwimToPlaneBoundary(double d_cm, Vector3D n, int dir) {
        
        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }
        double[] value = new double[8];
        
        double d = d_cm / 100;

        Plane plane = this.getSwimPlane(n, d);

        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        
        // swim backwards?
        double vertexR = Math.sqrt(_x0 * _x0 + _y0 * _y0 + _z0 * _z0);
        if ((vertexR > d) && (dir > 0)) {

            //trying to swim forward, but already beyond the plane. Just return the starting values
            double thetaRad = Math.toRadians(_theta);
            double phiRad = Math.toRadians(_phi);
            double pz = _pTot * Math.cos(thetaRad);
            double pperp = _pTot * Math.sin(thetaRad);
            double px = pperp * Math.cos(phiRad);
            double py = pperp * Math.sin(phiRad);

            value[0] = _x0 * 100; // convert back to cm
            value[1] = _y0 * 100; // convert back to cm
            value[2] = _z0 * 100; // convert back to cm

            value[3] = px;
            value[4] = py;
            value[5] = pz;
            value[6] = 0;
            value[7] = 0;

            return value;

        }

        else {
            try {    
                PC.ACF.swimPlane(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, plane, accuracy, _rMax,
                                stepSize, eps, swimResult);

                traj = swimResult.getTrajectory();

                traj.computeBDL(PC.CP);

                double lastY[] = swimResult.getTrajectory().lastElement();
                value[0] = lastY[0] * 100; // convert back to cm
                value[1] = lastY[1] * 100; // convert back to cm
                value[2] = lastY[2] * 100; // convert back to cm
                value[3] = lastY[3] * _pTot;
                value[4] = lastY[4] * _pTot;
                value[5] = lastY[5] * _pTot;
                value[6] = lastY[6] * 100;
                value[7] = lastY[7] * 10;

            } catch (Exception e) {
                    e.printStackTrace();
            }
        }
        return value;
    }

    private List<Double> _n = new ArrayList<Double>();
    private Plane getSwimPlane(Vector3D n, double d) {
        
        _n.clear();
        _n.add(n.x());
        _n.add(n.y());
        _n.add(n.z());
        
        Plane pl = null;
        Collections.sort(_n, Comparator.comparingDouble(Math::abs));
        
        if(_n.get(2)==n.x()) {
            pl = new Plane(new Vector(n.x(), n.y(), n.z()), 
                    new Point(d/_n.get(2), 0, 0)); 
        }
        if(_n.get(2)==n.y()) {
            pl = new Plane(new Vector(n.x(), n.y(), n.z()), 
                    new Point(0, d/_n.get(2), 0)); 
        }
        if(_n.get(2)==n.z()) {
            pl = new Plane(new Vector(n.x(), n.y(), n.z()), 
                    new Point(0, 0, d/_n.get(2))); 
        }
        
        return pl;
    }

    public double[] SwimToBeamLine(double xB, double yB) {
        
        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }
        double x = xB / 100; // the magfield method uses meters
        double y = yB / 100; // 
        double[] value = new double[8];

        Line lineTarget = new Line(new Point(x, y, 0), new Point(x, y, 1000));
        AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
        SwimTrajectory traj = null;
        try {
            
            PC.ACF.swimLine(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, lineTarget, accuracy, _rMax,
                            stepSize, eps, swimResult);
           
            
            if(traj==null)
                return null;

            traj = swimResult.getTrajectory();
		
            traj.computeBDL(PC.CP);

            double lastY[] = swimResult.getTrajectory().lastElement();
            value[0] = lastY[0] * 100; // convert back to cm
            value[1] = lastY[1] * 100; // convert back to cm
            value[2] = lastY[2] * 100; // convert back to cm
            value[3] = lastY[3] * _pTot;
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;
            value[6] = lastY[6] * 100;
            value[7] = lastY[7] * 10;

        } catch (Exception e) {
                e.printStackTrace();
        }
        return value;

    }
    
}
