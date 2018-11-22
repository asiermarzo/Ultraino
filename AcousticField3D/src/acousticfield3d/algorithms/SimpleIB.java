package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author am14010
 */
public class SimpleIB {
    //A are the real parts, B are the imaginary parts
    final int P; //n points
    final float[] pointA, pointB; //target field at each virtual point 
            
    final int T; //n Transducers
    final float[] transA, transB; //complex emission of each transducer
    
    final float[][] tpA, tpB; //propagator from transducer to point [T][P]

    
    public SimpleIB(final MainForm mf, 
            final List<? extends Transducer> transducers, 
            final List<? extends Entity> controlPoints){
        
        T = transducers.size();
        transA = new float[T];
        transB = new float[T];
        
        P = controlPoints.size();
        pointA = new float[P];
        pointB = new float[P];
                
        tpA = new float[T][P];
        tpB = new float[T][P];
        
        //initialise control points complex number
        for (int i = 0; i<P; ++i){
            final Vector3f pos = controlPoints.get(i).getTransform().getTranslation();
            pointA[i] = 1; pointB[i] = 0; //we just want amplitude 1, any phase does the job to initiate them
            
            //calculate the propagators from the transducers into point i
            addPropagators(mf, transducers, i, pos.x, pos.y, pos.z);
        }
    }
    
    private void addPropagators(final MainForm mf, 
            final List<? extends Transducer> transducers,
            final int index, final float x, final float y, final float z){
        
        for (int i = 0; i<T; ++i){
            final Transducer t = transducers.get(i);
                //calculates the propagator from transducer t into point (x,y,z). Or in other words the field that t generates with phase 0
                final Vector2f field = CalcField.calcFieldForTrans(t, 0, x, y, z, mf.simulation);
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
        
        //normalize control points
            for (int i = 0; i < P; ++i) {
                final float dist = M.sqrt(pointA[i]*pointA[i] + pointB[i]*pointB[i]);
                pointA[i] /= dist;
                pointB[i] /= dist; 
            }
            

        //backproject control points into transducers (use the conjugate to backpropagate)
        for (int i = 0; i<T; ++i){
            transA[i] = transB[i] = 0;
            for (int j = 0; j<P; ++j){
                transA[i] += pointA[j]*tpA[i][j] - pointB[j]*-tpB[i][j];
                transB[i] += pointA[j]*-tpB[i][j] + pointB[j]*tpA[i][j];
            }
        }
        
        //normalize transducer amplitude
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
            t.setPhase( c.getAngle() );
        }
    }
    
  
}
