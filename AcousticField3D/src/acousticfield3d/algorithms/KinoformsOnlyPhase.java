/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Color;
import java.util.List;

/**
 *
 * @author am14010
 */
public class KinoformsOnlyPhase {
    //Specular  property --> phase

    final int P; //n points
    final float[] pointA, pointB; //target field at each virtual point
    final float[] phaseA, phaseB; //target phase for the point 
            
    final int T; //n Transducers
    final float[] transA, transB; //amplitude and phase of the transducer
    
    final float[][] tpA, tpB; //propagator from transducer to point
        
    public boolean forceAmp;
    
    public KinoformsOnlyPhase(final MainForm mf, 
            final List<? extends Transducer> transducers, 
            final List<? extends Entity> controlPoints){
        
        //initialize transducers 
        T = transducers.size();
        transA = new float[T];
        transB = new float[T];
        for (int i = 0; i<T; ++i){
            final float amp = transducers.get(i).getpAmplitude();
            final float phase = transducers.get(i).getPhase() * M.PI;
            transA[i] = amp * M.cos( phase );
            transB[i] = amp * M.sin( phase );
        }
        
        P = controlPoints.size();
        pointA = new float[P];
        pointB = new float[P];
        phaseA = new float[P];
        phaseB = new float[P];
                
        tpA = new float[T][P];
        tpB = new float[T][P];
        
        //initialise control points complex number
        for (int i = 0; i<P; ++i){
            final Vector3f pos = controlPoints.get(i).getTransform().getTranslation();
            final float phase = controlPoints.get(i).getMaterial().getSpecular();
            
            phaseA[i] = M.cos( phase );
            phaseB[i] = M.sin( phase );
            
            pointA[i] = phaseA[i]; pointB[i] = phaseB[i];
            addPropagator(mf, transducers, i, pos.x, pos.y, pos.z);
        }
    }
    
    private void addPropagator(final MainForm mf, 
            final List<? extends Transducer> transducers,
            final int index, final float x, final float y, final float z){
        
        for (int i = 0; i<T; ++i){
            final Transducer t = transducers.get(i);
                final Vector2f field = CalcField.calcFieldForTrans(t, 0, x, y, z, mf);
                tpA[i][index] = field.x;
                tpB[i][index] = field.y;
        }
        
    }
    
    
    public void iterate(){
        //project transducers into control points
        for (int j = 0; j<P; ++j){
            pointA[j] = pointB[j] = 0;
            for (int i = 0; i<T; ++i){
                pointA[j] += transA[i]*tpA[i][j] - transB[i]*tpB[i][j];
                pointB[j] += transA[i]*tpB[i][j] + transB[i]*tpA[i][j];
            }
        }
        
        //fix phase of the control points, and possible amplitude
        
        if (forceAmp) {
            for (int i = 0; i < P; ++i) {
                pointA[i] = phaseA[i];
                pointB[i] = phaseB[i];
            }
        } else {
            for (int i = 0; i < P; ++i) {
                final float pA = pointA[i];
                final float pB = pointB[i];
                final float dist = M.sqrt(pA * pA + pB * pB);

                pointA[i] = phaseA[i] / dist;
                pointB[i] = phaseB[i] / dist; //phase offset 0
            }
        }      
        
        
        //project control points into transducers (use the conjugate to backpropagate)
        for (int i = 0; i<T; ++i){
            transA[i] = transB[i] = 0;
            for (int j = 0; j<P; ++j){
                transA[i] += pointA[j]*tpA[i][j] - pointB[j]*-tpB[i][j];
                transB[i] += pointA[j]*-tpB[i][j] + pointB[j]*tpA[i][j];
            }
        }
        
        //set transducer amplitude
        for (int i = 0; i<T; ++i){
            final float dist = M.sqrt(transA[i]*transA[i] + transB[i]*transB[i]);
            transA[i] /= dist;
            transB[i] /= dist;
        }
    }
    
    public void applySolution(final Simulation s){
        for (int i = 0; i<T ; ++i){
            final Vector2f c = new Vector2f(transA[i], transB[i]);
            final Transducer t = s.getTransducers().get(i);
            t.setpAmplitude( c.length() );
            t.setPhase(c.getAngle() / M.PI);
        }
    }
    
    public static KinoformsOnlyPhase create(final MainForm mf, final List<? extends Entity> controlPoints){
        final KinoformsOnlyPhase smp = new KinoformsOnlyPhase(mf, mf.simulation.transducers, controlPoints);
        return smp;
    }
    
     //0 focal point, 1 vortexClockwise, 2 vortexCounter, 3 twin-trap, 4 standing-wave
    public static int typeOfTrap(final int color){
        if ( Color.red(color) + Color.green(color) + Color.blue(color) == 255*3){
            return 0;
        }else if (Color.red(color) == 255 && Color.green(color) + Color.blue(color) == 0){
            return 1;
        }else if (Color.blue(color) == 255 && Color.green(color) + Color.red(color) == 0){
            return 2;
        }else if ( Color.red(color) + Color.green(color) + Color.blue(color) == 0 ){
            return 4;
        }else if (Color.blue(color) + Color.red(color) == 0){
            return 3;
        }
        
        return -1;
    }
    
    private final static int N_POINTS_VORTEX = 16;
    private final static int[] N_POINTS = {1,N_POINTS_VORTEX,N_POINTS_VORTEX,2,2};
    
    
    public static float getTwinTrapRotation(final int color){
        return Color.green(color) * M.TWO_PI  / 255.0f;
    }

}
