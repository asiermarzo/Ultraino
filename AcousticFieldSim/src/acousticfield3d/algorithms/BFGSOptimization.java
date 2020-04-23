package acousticfield3d.algorithms;

import acousticfield3d.algorithms.bfgs.BFGS;
import acousticfield3d.algorithms.bfgs.BFGSProgressListener;
import acousticfield3d.algorithms.bfgs.IFunction;
import acousticfield3d.gui.MainForm;
import acousticfield3d.gui.misc.AlgorithmsForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Renderer;
import acousticfield3d.scene.Entity;
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
    final AlgorithmsForm form;
    public final ArrayList<Entity> controlPoints;
    
    public BFGSOptimization(AlgorithmsForm form) {
        this.form = form;
        controlPoints = new ArrayList<>();
    }
 
    
    public void calc(final MainForm mf){
        IFunction function;
        if (form.isPressure()){
            function = new MultiplePressureAdaptor(controlPoints,  mf );
        }else if(form.isGorkov()){
            function = new MultiGorkovAdaptor(controlPoints,  mf );
            //function = new MultiGorkovAdaptor(controlPoints.get(0).getTransform().getTranslation(),  mf );
        }else if(form.isGLaplacian()){
            function = new MaxGorkovLaplacianMinPressureAdaptor(
                    controlPoints.get(0).getTransform().getTranslation(), form.getLowPressureK(), form.getLaplacianConstants(),
                    mf );
        }else if(form.isIterGorkPressure()){
            function = new SingleGorkovAdaptor(
                    controlPoints.get(0),
                    mf );
        }else{
            //Default
            function = new MultiplePressureAdaptor(controlPoints,  mf );
        }
        calc(mf, function);
    }
    
    public void calcMultiPressure(final MainForm mf, final int iters){
        calc(mf, new MultiplePressureAdaptor(controlPoints, mf), iters);
    }
    
    public void calc(final MainForm mf, final IFunction function) {
        final int steps = form.getSteps();
        calc(mf,function,steps);
    }
    
    public void calc(final MainForm mf, final IFunction function, final int nIterations) {
        
        final double xMin = form.getXMin();
        final double gMin = form.getGMin();
        final double alpha = form.getAlpha();
        
        final BFGSProgressListener listener = form.isReport() ? form : null;
                
        final Simulation s = mf.simulation;
        
        if(controlPoints.isEmpty()){
            return;
        }
        
        final double[] phases = s.getTransPhasesAsArray();

        BFGS bfgs = new BFGS();
        bfgs.setAlpha( alpha );
        bfgs.setListener( listener );
        bfgs.setcMaxIterations( nIterations );
        bfgs.setcTolX( xMin );
        bfgs.setcTolGradient( gMin );
        
        final double fx = bfgs.minimize(function, phases, phases);
        
        
        if(listener != null){
            listener.bfgsOnFinish(bfgs.getLastNumberOfIterations(), 
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
    
    public class SingleGorkovAdaptor implements IFunction{
        public final CachedPointFieldCalc point;
       
        public SingleGorkovAdaptor(final Entity p, MainForm mf){
            final Renderer r = mf.renderer;
            
            point = CachedPointFieldCalc.create(p.getTransform().getTranslation(), mf);
            point.allocateAndInit(mf);
        }
        
        @Override
        public int getDimensions() {
            return point.getNTrans();
        }

        @Override
        public double evaluate(double[] vars) {
            point.updateGorkov(vars );
            return point.evalGorkov();
        }

        @Override
        public void gradient(double[] vars, double[] g) {
            point.updateGorkov( vars );
            point.gradientGorkov( g ); 
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
            
            final double lpk = form.getLowPressureK();
            equalize = form.isEqualizer();
            equalizerStrength = form.getEqualizerWeight();

            int i = 0;
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
                        g[j] += equalizerStrength * 2.0f * (g0*tempG0[j] - g0*tempG1[j] - tempG0[j]*g1 + g1*tempG1[j]);
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
   
      public class MultiLapAdaptor implements IFunction{
        public final int nPoints;
        public final CachedPointFieldCalc[] points;
        public final double[] tempG0;
        public final double[] tempG1;
        

        public MultiLapAdaptor(final ArrayList<Entity> p, MainForm mf){
            final Renderer r = mf.renderer;
            
            nPoints = p.size();
            points = new CachedPointFieldCalc[nPoints];
            tempG0 = new double[r.getnTransducers()];
            tempG1 = new double[r.getnTransducers()];
            final double lpk = form.getLowPressureK();
           
            int i = 0;
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
                points[i].updateGorkovLaplacian(vars );
                v += -points[i].evalGorkovLaplacian();
            }
            
            return v;
        }

        @Override
        public void gradient(double[] vars, double[] g) {
            Arrays.fill(g, 0.0);
            final int d = g.length;
           
                for(int i = 0; i < nPoints; ++i){
                    points[i].updateGorkovLaplacian(vars );
                    points[i].gradientGorkovLaplacian( tempG0 );
                    for (int j = 0; j < d; ++j) {
                        g[j] += - tempG0[j];
                    }
                }
            
        }
        
    } 
}
