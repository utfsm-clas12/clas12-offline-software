/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.swimtools;

import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.util.Plane;
import cnuphys.swimZ.SwimZException;
import cnuphys.swimZ.SwimZResult;
import cnuphys.swimZ.SwimZStateVector;
import org.apache.commons.math3.util.FastMath;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler
 */

public class SwimTest extends SwimPars {

    final double stepSize = 0.0001;  //initial ss = 1 cm

    public double[] SwimToPlaneTiltSecSys(int sector, double z_cm) {
        double z = z_cm / 100; // the magfield method uses meters
        double[] value = new double[8];

        if (_pTot < MINTRKMOM || this.SwimUnPhys==true) // fiducial cut
        {
            return null;
        }

        // use a SwimZResult instead of a trajectory (dph)
        SwimZResult szr = null;

        SwimTrajectory traj = null;
        double hdata[] = new double[3];

        try {

            if (_pTot > SWIMZMINMOM) {

                // use the new z swimmer (dph)
                // NOTE THE DISTANCE, UNITS FOR swimZ are cm, NOT m like the old
                // swimmer (dph)

                double stepSizeCM = stepSize * 100; // convert to cm

                // create the starting SwimZ state vector
                SwimZStateVector start = new SwimZStateVector(_x0 * 100, _y0 * 100, _z0 * 100, _pTot, _theta, _phi);

                try {
                        szr = PC.RCF_z.sectorAdaptiveRK(sector, _charge, _pTot, start, z_cm, stepSizeCM, hdata);
                } catch (SwimZException e) {
                        szr = null;
                        //System.err.println("[WARNING] Tilted SwimZ Failed for p = " + _pTot);
                }
            }

            if (szr != null) {
                double bdl = szr.sectorGetBDL(sector, PC.RCF_z.getProbe());
                double pathLength = szr.getPathLength(); // already in cm

                SwimZStateVector last = szr.last();
                double p3[] = szr.getThreeMomentum(last);

                value[0] = last.x; // xf in cm
                value[1] = last.y; // yz in cm
                value[2] = last.z; // zf in cm
                value[3] = p3[0];
                value[4] = p3[1];
                value[5] = p3[2];
                value[6] = pathLength;
                value[7] = bdl / 10; // convert from kg*cm to T*cm
            } else { // use old swimmer. Either low momentum or SwimZ failed.
                                // (dph)

                traj = PC.RCF.sectorSwim(sector, _charge, _x0, _y0, _z0, _pTot, _theta, _phi, z, accuracy, _rMax,
                                _maxPathLength, stepSize, cnuphys.swim.Swimmer.CLAS_Tolerance, hdata);
                // traj.computeBDL(sector, rprob);
                if(traj==null)
                    return null;
                
                traj.sectorComputeBDL(sector, PC.RCP);
                // traj.computeBDL(rcompositeField);

                double lastY[] = traj.lastElement();
                value[0] = lastY[0] * 100; // convert back to cm
                value[1] = lastY[1] * 100; // convert back to cm
                value[2] = lastY[2] * 100; // convert back to cm
                value[3] = lastY[3] * _pTot;
                value[4] = lastY[4] * _pTot;
                value[5] = lastY[5] * _pTot;
                value[6] = lastY[6] * 100;
                value[7] = lastY[7] * 10;
            } // use old swimmer
        } catch (Exception e) {
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
        SwimTrajectory traj = null;
        double hdata[] = new double[3];

        // use a SwimZResult instead of a trajectory (dph)
        SwimZResult szr = null;

        try {

            if (_pTot > SWIMZMINMOM) {

                // use the new z swimmer (dph)
                // NOTE THE DISTANCE, UNITS FOR swimZ are cm, NOT m like the old
                // swimmer (dph)

                double stepSizeCM = stepSize * 100; // convert to cm

                // create the starting SwimZ state vector
                SwimZStateVector start = new SwimZStateVector(_x0 * 100, _y0 * 100, _z0 * 100, _pTot, _theta, _phi);

                try {
                        szr = PC.CF_z.adaptiveRK(_charge, _pTot, start, z_cm, stepSizeCM, hdata);
                } catch (SwimZException e) {
                        szr = null;
                        //System.err.println("[WARNING] SwimZ Failed for p = " + _pTot);

                }
            }

            if (szr != null) {
                double bdl = szr.getBDL(PC.CF_z.getProbe());
                double pathLength = szr.getPathLength(); // already in cm

                SwimZStateVector last = szr.last();
                double p3[] = szr.getThreeMomentum(last);

                value[0] = last.x; // xf in cm
                value[1] = last.y; // yz in cm
                value[2] = last.z; // zf in cm
                value[3] = p3[0];
                value[4] = p3[1];
                value[5] = p3[2];
                value[6] = pathLength;
                value[7] = bdl / 10; // convert from kg*cm to T*cm
            } else { // use old swimmer. Either low momentum or SwimZ failed.
                                    // (dph)
                traj = PC.CF.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, z, accuracy, _rMax, _maxPathLength,
                                stepSize, cnuphys.swim.Swimmer.CLAS_Tolerance, hdata);
                if(traj==null)
                    return null;
                traj.computeBDL(PC.CP);
                // traj.computeBDL(compositeField);

                double lastY[] = traj.lastElement();

                value[0] = lastY[0] * 100; // convert back to cm
                value[1] = lastY[1] * 100; // convert back to cm
                value[2] = lastY[2] * 100; // convert back to cm
                value[3] = lastY[3] * _pTot;
                value[4] = lastY[4] * _pTot;
                value[5] = lastY[5] * _pTot;
                value[6] = lastY[6] * 100;
                value[7] = lastY[7] * 10;
            } // old swimmer

        } catch (RungeKuttaException e) {
                e.printStackTrace();
        }
        return value;

    }

    /**
     * Cylindrical stopper
     */
    private class CylindricalcalBoundarySwimStopper implements IStopper {

        private double _finalPathLength = Double.NaN;

        private double _Rad;

        /**
         * A swim stopper that will stop if the boundary of a plane is crossed
         *
         * @param maxR
         *            the max radial coordinate in meters.
         */
        private CylindricalcalBoundarySwimStopper(double Rad) {
                // DC reconstruction units are cm. Swim units are m. Hence scale by
                // 100
                _Rad = Rad;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {

                double r = Math.sqrt(y[0] * y[0] + y[1] * y[1]) * 100.;

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
    public double[] SwimToCylinder(double Rad) {

        double[] value = new double[8];
        // using adaptive stepsize
        if(this.SwimUnPhys)
            return null;
        
        CylindricalcalBoundarySwimStopper stopper = new CylindricalcalBoundarySwimStopper(Rad);
        
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

        double[] value = new double[8];
        // using adaptive stepsize
        if(this.SwimUnPhys==true)
            return null;
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

    // added for swimming to outer detectors
    private class PlaneBoundarySwimStopper implements IStopper {

        private double _finalPathLength = Double.NaN;
        private double _d;
        private Vector3D _n;
        private double _dist2plane;
        private int _dir;

        /**
         * A swim stopper that will stop if the boundary of a plane is crossed
         *
         * @param maxR
         *            the max radial coordinate in meters.
         */
        private PlaneBoundarySwimStopper(double d, Vector3D n, int dir) {
                // DC reconstruction units are cm. Swim units are m. Hence scale by
                // 100
                _d = d;
                _n = n;
                _dir = dir;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {
            double dtrk = y[0] * _n.x() + y[1] * _n.y() + y[2] * _n.z();

            double accuracy = 20e-6; // 20 microns
            // System.out.println(" dist "+dtrk*100+ " state "+y[0]*100+",
            // "+y[1]*100+" , "+y[2]*100);
            if (_dir < 0) {
                    return dtrk < _d;
            } else {
                    return dtrk > _d;
            }

        }

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
     * @param d_cm
     * @param n
     * @param dir
     * @return return state  x,y,z,px,py,pz, pathlength, iBdl at the plane surface in the lab frame
     */
    public double[] SwimToPlaneBoundary(double d_cm, Vector3D n, int dir) {

        double[] value = new double[8];
        if(this.SwimUnPhys)
            return null;
        double d = d_cm / 100;

        double hdata[] = new double[3];
        // using adaptive stepsize

        // the new swim to plane in swimmer
        Plane plane = new Plane(n.x(), n.y(), n.z(), d);
        SwimTrajectory st;
        try {

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
                st = PC.CF.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, plane, accuracy, _maxPathLength, stepSize,
                                cnuphys.swim.Swimmer.CLAS_Tolerance, hdata);

                st.computeBDL(PC.CP);

                double[] lastY = st.lastElement();

                value[0] = lastY[0] * 100; // convert back to cm
                value[1] = lastY[1] * 100; // convert back to cm
                value[2] = lastY[2] * 100; // convert back to cm
                value[3] = lastY[3] * _pTot; // normalized values
                value[4] = lastY[4] * _pTot;
                value[5] = lastY[5] * _pTot;
                value[6] = lastY[6] * 100;
                value[7] = lastY[7] * 10; // Conversion from kG.m to T.cm
            }
        } catch (RungeKuttaException e) {
                e.printStackTrace();
        }
        return value;

    }

    
    
    private class BeamLineSwimStopper implements IStopper {

        private double _finalPathLength = Double.NaN;

        private double _xB;
        private double _yB;
        double min = Double.POSITIVE_INFINITY;
        double thetaRad = Math.toRadians(_theta);
        double phiRad = Math.toRadians(_phi);
        double pz = _pTot * Math.cos(thetaRad);
        private BeamLineSwimStopper(double xB, double yB) {
                // DC reconstruction units are cm. Swim units are m. Hence scale by
                // 100
                _xB = xB;
                _yB = yB;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {

                double r = Math.sqrt((_xB-y[0]* 100.) * (_xB-y[0]* 100.) + (_yB-y[1]* 100.) * (_yB-y[1]* 100.));
                if(r<min && y[2]<2.0) //start at about 2 meters before target.  Avoid inbending stopping when P dir changes
                    min = r;
                return (r > min );

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
    
    public double[] SwimToBeamLine(double xB, double yB) {

        double[] value = new double[8];
        
        if(this.SwimUnPhys==true)
            return null;
        BeamLineSwimStopper stopper = new BeamLineSwimStopper(xB, yB);

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

    
    
    
}
