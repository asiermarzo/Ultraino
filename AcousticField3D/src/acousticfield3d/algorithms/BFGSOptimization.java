/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.algorithms;

import acousticfield3d.algorithms.bfgs.BFGS;
import acousticfield3d.algorithms.bfgs.IFunction;
import acousticfield3d.gui.MainForm;
import acousticfield3d.gui.misc.AlgorithmsForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Renderer;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Color;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Asier
 */
public class BFGSOptimization{
    final acousticfield3d.gui.misc.AlgorithmsForm form;
    public final ArrayList<MeshEntity> controlPoints;
    
    public BFGSOptimization(AlgorithmsForm form) {
        this.form = form;
        controlPoints = new ArrayList<>();
    }
 
    public void calc(MainForm mf, Simulation s) {
        final int steps = form.getSteps();
        final double xMin = form.getXMin();
        final double gMin = form.getGMin();
        final double alpha = form.getAlpha();
        
        final ArrayList<Entity> selection = mf.selection;
        
        if (selection.isEmpty()){
            return;
        }
        
        final double[] phases = s.getTransPhasesAsArray();
        
        IFunction function;
        if (form.isPressure()){
            function = new MultiplePressureAdaptor(selection,  mf );
        }else if(form.isGorkov()){
            function = new MultiGorkovAdaptor(selection,  mf );
        }else if(form.isGLaplacian()){
            function = new MaxGorkovLaplacianMinPressureAdaptor(
                    selection.get(0).getTransform().getTranslation(), form.getLowPressureK(), form.getLaplacianConstants(),
                    mf );
        }else{
            //Default
            function = new MultiplePressureAdaptor(selection,  mf );
        }
        
        BFGS bfgs = new BFGS();
        bfgs.setAlpha( alpha );
        bfgs.setListener( form );
        bfgs.setcMaxIterations( steps );
        bfgs.setcTolX( xMin );
        bfgs.setcTolGradient( gMin );
        
        final double fx = bfgs.minimize(function, phases, phases);

        if(form != null){
            form.bfgsOnFinish(bfgs.getLastNumberOfIterations(), 
                    bfgs.isMinTolX(), bfgs.isMinTolGradient(), bfgs.getHessianUpdates(),
                    fx, phases);
        }
        
        final ArrayList<Transducer> trans = s.getTransducers();
        for(int i = phases.length - 1; i >= 0; --i){
            trans.get(i).setPhase( (float)phases[i] / M.PI);
        }
    }
    
    public class MultiplePressureAdaptor implements IFunction{
        public final int nPoints;
        public final CachedPointFieldCalc[] points;
        public final boolean[] maximize;
        public final double[] tempG;
        
        public MultiplePressureAdaptor(final ArrayList<Entity> p, MainForm mf){
            final Renderer r = mf.renderer;
            nPoints = p.size();
            points = new CachedPointFieldCalc[nPoints];
            maximize = new boolean[nPoints];
            tempG = new double[r.getnTransducers()];
            int i = 0;
            
            for(Entity e : p){
                CachedPointFieldCalc cp = CachedPointFieldCalc.create(e.getTransform().getTranslation(), mf);
                
                cp.allocateAndInit(mf);
                points[i] = cp;
                maximize[i] = e.getRealColor() == Color.WHITE;
                i++;
            }
        }
        
        @Override
        public int getDimensions() {
            return points[0].getNTrans();
        }

        @Override
        public double evaluate(double[] vars) {
            double v = 0;
            for(int i = 0; i < nPoints; ++i){
                points[i].updatePressure( vars );
                v += (maximize[i] ? -1.0 : 1.0) * points[i].evalPressure();
            }
            return v;
        }

        @Override
        public void gradient(double[] vars, double[] g) {
            Arrays.fill(g, 0.0);
            final int d = g.length;
            for(int i = 0; i < nPoints; ++i){
                points[i].updatePressure( vars );
                points[i].gradientPressure( tempG );
                if (maximize[i]){
                    for (int j = 0; j < d; ++j){
                        g[j] -= tempG[j];
                    }
                }else{
                    for (int j = 0; j < d; ++j){
                        g[j] += tempG[j];
                    }
                }
            }
        }
        
    }
    
  
    public class MaxGorkovLaplacianMinPressureAdaptor implements IFunction{
        public final CachedPointFieldCalc center;
        private final double kLowPressure;
        private final double[] tempGX;
        private final double[] tempGY;
        private final double[] tempGZ;
        private final Vector3f compConst;
        
        public MaxGorkovLaplacianMinPressureAdaptor(final Vector3f p, double kLowPressure, Vector3f componentConstants, MainForm mf){
           final Renderer r = mf.renderer;
           
           this.compConst = componentConstants;
           this.kLowPressure = kLowPressure;
           tempGX = new double[r.getnTransducers()];
           tempGY = new double[r.getnTransducers()];
           tempGZ = new double[r.getnTransducers()];
             
           center = CachedPointFieldCalc.create(p, mf);
           center.allocateAndInit(mf);
        }
       
      
        @Override
        public int getDimensions() {
            return center.getNTrans();
        }

        @Override
        public double evaluate(double[] vars) {
            center.updateGorkovLaplacian(vars);
            return center.evalPressure()*kLowPressure - (
                    compConst.x * center.evalGorkovLaplacianX() +
                     compConst.y * center.evalGorkovLaplacianY() +
                     compConst.z * center.evalGorkovLaplacianZ());
        }

        @Override
        public void gradient(double[] vars, double[] g) {
           center.updateGorkovLaplacian(vars);
           center.gradientGorkovLaplacianX(tempGX);
           center.gradientGorkovLaplacianY(tempGY);
           center.gradientGorkovLaplacianZ(tempGZ);
           center.gradientPressure(g);
           final int d = g.length;
           for(int i = 0; i < d; ++i){
                g[i] = kLowPressure*g[i] 
                        - compConst.x * tempGX[i]
                        - compConst.y * tempGY[i]
                        - compConst.z * tempGZ[i];
            }
        }
    }
        
    public class MultiGorkovAdaptor implements IFunction{
        public final int nPoints;
        public final CachedPointFieldCalc[] points;
        public final double[] tempG0;
        public final double[] tempG1;
        
        final boolean equalize;
        final double equalizerStrength;
        
        public MultiGorkovAdaptor(final ArrayList<Entity> p, MainForm mf){
            final Renderer r = mf.renderer;
            
            nPoints = p.size();
            points = new CachedPointFieldCalc[nPoints];
            tempG0 = new double[r.getnTransducers()];
            tempG1 = new double[r.getnTransducers()];
            int i = 0;
            final double lpk = form.getLowPressureK();
            equalize = form.isEqualizer();
            equalizerStrength = form.getEqualizerWeight();

            for(Entity e : p){
                CachedPointFieldCalc cp = CachedPointFieldCalc.create( e.getTransform().getTranslation(), mf);
                cp.setLowPressureK( lpk );
                cp.allocateAndInit(mf);
                points[i] = cp;
                i++;
            }
        }
        
        @Override
        public int getDimensions() {
            return points[0].getNTrans();
        }

        @Override
        public double evaluate(double[] vars) {
            double v = 0;
            for(int i = 0; i < nPoints; ++i){
                points[i].updateGorkov(vars );
                v += points[i].evalGorkov();
            }
            if (equalize){
                for(int i = 0; i < nPoints-1; ++i){
                    final double diff = points[i].evalGorkov() - points[i+1].evalGorkov();
                    v += equalizerStrength * diff * diff;
                }
            }
            return v;
        }

        @Override
        public void gradient(double[] vars, double[] g) {
            Arrays.fill(g, 0.0);
            final int d = g.length;
            if (!equalize){
                for(int i = 0; i < nPoints; ++i){
                    points[i].updateGorkov(vars );
                    points[i].gradientGorkov( tempG0 );

                    for (int j = 0; j < d; ++j) {
                        g[j] += tempG0[j];
                    }
                }
            }else{
                final int n1 = nPoints-1;
                for(int i = 0; i < n1; ++i){
                    points[i].updateGorkov(vars);
                    points[i+1].updateGorkov(vars);
                    final double g0 = points[i].evalGorkov();
                    final double g1 = points[i+1].evalGorkov();
                    points[i].gradientGorkov( tempG0 );
                    points[i+1].gradientGorkov( tempG1 );
                    
                    for (int j = 0; j < d; ++j) {
                        g[j] += equalizerStrength * 2.0f * (tempG0[j] + tempG1[j] - (tempG0[j]*g1 + g0*tempG1[j]));
                        g[j] += tempG0[j];
                    }
                    if (i == n1-1){
                        for (int j = 0; j < d; ++j) {
                            g[j] += tempG1[j];
                        }
                    }
                }
            }
            
        }
        
    }
   
   
    public class ForceLevitatorAdaptor implements IFunction{
        private final CachedPointFieldCalc cpa;
        private final double yForce;
        
        public ForceLevitatorAdaptor(final Vector3f pos, double yForce, MainForm mf){
            final Renderer r = mf.renderer;
            
            this.yForce = yForce;
           
            cpa = CachedPointFieldCalc.create(pos, mf);
            cpa.allocateAndInit(mf);
        }

        public CachedPointFieldCalc getCpa() {
            return cpa;
        }
 
        @Override
        public int getDimensions() {
            return cpa.getNTrans();
        }

        @Override
        public double evaluate(double[] vars) {
            cpa.updateGorkovGradient(vars);
            final double x = cpa.evalGorkovGradientX();
            final double y = cpa.evalGorkovGradientY() - yForce;
            final double z = cpa.evalGorkovGradientZ();
            return x*x + z*z + y*y;
        }

        @Override
        public void gradient(double[] vars, double[] g) {
            cpa.updateGorkovGradient(vars);
            final double fx = cpa.evalGorkovGradientX();
            final double fy = cpa.evalGorkovGradientY();
            final double fz = cpa.evalGorkovGradientZ();
            final int n = g.length;
            double[] gx = new double[n];
            double[] gy = new double[n];
            double[] gz = new double[n];
            cpa.gradientGorkovGradientX(gx);
            cpa.gradientGorkovGradientY(gy);
            cpa.gradientGorkovGradientZ(gz);

            for(int l = g.length -1; l >= 0; l--){
                g[l] =  2.0*fx*gx[l] + 2.0*fz*gz[l] + 2.0*fy*gy[l] - 2.0*yForce*gy[l];
            }
            
        }  
    }
}
