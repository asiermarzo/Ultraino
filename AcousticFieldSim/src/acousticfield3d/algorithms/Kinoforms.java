package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author am14010
 */
public class Kinoforms {
    //white points -> focal points
    //red,blue points -> vortices
    //green -> twin-trap
    //black -> standing-waves

    final List<Transducer> onTrans;
    final int T; //n Transducers
    final int P; //n virtual points
    final int RP; //nreal points; 
    
    final int[] color; //color of the point
    final float[] pointA, pointB; //target field at each virtual point
    final float[] transA, transB; //amplitude and phase of the transducer
    final float[][] tpA, tpB; //propagator from transducer to point
    
    final float[] VORTEX_A, VORTEX_B;
    
    public Kinoforms(final MainForm mf, 
            final List<? extends Transducer> transducers, 
            final List<? extends Entity> controlPoints){
        
        //vortex phases
        VORTEX_A = new float[N_POINTS_VORTEX];
        VORTEX_B = new float[N_POINTS_VORTEX];
        for(int i = 0; i < N_POINTS_VORTEX; ++i){
            final float angle = i * M.TWO_PI / N_POINTS_VORTEX;
            VORTEX_A[i] = M.cos(angle);
            VORTEX_B[i] = M.sin(angle);
        }
        
        //initialize transducers 
        onTrans = new ArrayList<>();
        for(Transducer t : transducers){
            if (t.getpAmplitude() > 0){
                onTrans.add(t);
            }
        }
        T = onTrans.size();
        transA = new float[T];
        transB = new float[T];
        for (int i = 0; i<T; ++i){
            final float amp = onTrans.get(i).getpAmplitude();
            final float phase = onTrans.get(i).getPhase() * M.PI;
            transA[i] = amp * M.cos( phase );
            transB[i] = amp * M.sin( phase );
        }
        
        //count virtual control points (depending on the type of trap for each control points there will 1 or several virtual points)
        final int nPoints = controlPoints.size();
        RP = nPoints;
        color = new int[nPoints];
        int count = 0;
        int index = 0;
        for (Entity e : controlPoints){
            color[index] = e.getRealColor();
            final int type = typeOfTrap( color[index] );
            if (type != -1){
                count += N_POINTS[type];
            }
            ++index;
        }
        P = count;
        pointA = new float[P];
        pointB = new float[P];
        tpA = new float[T][P];
        tpB = new float[T][P];
        
        //initialise control points complex number
        
        final float wavelength = mf.getSimulation().getWavelenght();
        final float twinSep = wavelength / 1.5f;
        final float standingSep = wavelength / 2f;
        final float vortexSep = wavelength / 1.5f;
        index = 0;
        for (int i = 0; i<nPoints; ++i){
            final Vector3f pos = controlPoints.get(i).getTransform().getTranslation();
            final int c = color[i];
            final int trapType = typeOfTrap(c);
            
            
            if ( trapType == 0 ){ //focal point
                pointA[index+0] = 1; pointB[index+0] = 0;
                addPropagator(mf, onTrans, index+0, pos.x, pos.y, pos.z);
            }else if (trapType == 1 || trapType == 2){ //vortices
                for(int m = 0;m < N_POINTS_VORTEX; ++m){
                    final int mi = (trapType == 1) ? m : N_POINTS_VORTEX - m - 1;
                    final float vA = VORTEX_A[mi];
                    final float vB = VORTEX_B[mi];
                    pointA[index+m] = vA; pointB[index+m] = vB;
                    addPropagator(mf, onTrans, index+m, pos.x + vortexSep*vA, pos.y, pos.z + vortexSep*vB);
                }
            }else if (trapType == 3){ //twin-traps
                pointA[index+0] = 1; pointB[index+0] = 0;
                pointA[index+1] = -1; pointB[index+1] = 0;
                
                final float angle = getTwinTrapRotation( c );
                final Vector2f disp = new Vector2f().setAngle(angle).multLocal( twinSep );
                addPropagator(mf, onTrans, index+0, pos.x + disp.x, pos.y, pos.z + disp.y);
                addPropagator(mf, onTrans, index+1, pos.x - disp.x, pos.y, pos.z - disp.y);
            }else if (trapType == 4){ //standing-wave
                pointA[index+0] = 1; pointB[index+0] = 0;
                pointA[index+1] = -1; pointB[index+1] = 0;
                
                addPropagator(mf, onTrans, index+0, pos.x , pos.y + standingSep, pos.z );
                addPropagator(mf, onTrans, index+1, pos.x , pos.y - standingSep, pos.z );
            }
            
            index += N_POINTS[trapType];
        }
        
    }
    
    private void addPropagator(final MainForm mf, 
            final List<? extends Transducer> transducers,
            final int index, final float x, final float y, final float z){
        for (int i = 0; i<T; ++i){
            final Transducer t = transducers.get(i);
                final Vector2f field = CalcField.calcFieldForTrans(t, 0, x, y, z, mf.simulation);
                tpA[i][index] = field.x;
                tpB[i][index] = field.y;
        }
        
    }
    
    public void iterate(final boolean fixAmplitude){
        //project transducers into control points
        for (int j = 0; j<P; ++j){
            pointA[j] = pointB[j] = 0;
            for (int i = 0; i<T; ++i){
                pointA[j] += transA[i]*tpA[i][j] - transB[i]*tpB[i][j];
                pointB[j] += transA[i]*tpB[i][j] + transB[i]*tpA[i][j];
            }
        }
        
        //fix amplitude of control points and lock phase if necessary
        int index = 0;
        for (int i = 0; i<RP; ++i){
            final int c = color[i];
            final int trapType = typeOfTrap(c);
            
            final float pA = pointA[index];
            final float pB = pointB[index];
            final float dist = M.sqrt(pA*pA + pB*pB);
            final float A = pA / dist;
            final float B = pB / dist;
           
            
            if ( trapType == 0 ){ //focal point
                pointA[index+0] = A; pointB[index+0] = B; //phase offset 0
            }else if (trapType == 1 || trapType == 2){ //vortices
                for(int m = 0;m < N_POINTS_VORTEX; ++m){
                    //final int mi = (trapType == 1) ? m : N_POINTS_VORTEX - m - 1;
                    final int mi = m;
                    final float vA = VORTEX_A[mi];
                    final float vB = VORTEX_B[mi];
                    
                    pointA[index+m] = vA*A - vB*B; pointB[index+m] = vA*B + vB*A;
                }
            }else if (trapType == 3 || trapType == 4){ //twin-traps
                pointA[index+0] = A; pointB[index+0] = B; //phase offset 0
                pointA[index+1] = -A; pointB[index+1] = -B; //phase offset PI
            }
            index += N_POINTS[trapType];
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
        if (fixAmplitude){
            for (int i = 0; i<T; ++i){
                final float dist = M.sqrt(transA[i]*transA[i] + transB[i]*transB[i]);
                transA[i] /= dist;
                transB[i] /= dist;
            }
        }
       
    }
    
    public void normalizeTransducersAmplitude(){
        float maxDistance = 0;
        for (int i = 0; i<T; ++i){
            final float dist = M.sqrt(transA[i]*transA[i] + transB[i]*transB[i]);
            maxDistance = M.max(dist, maxDistance);
        }
        for (int i = 0; i<T; ++i){
            transA[i] /= maxDistance;
            transB[i] /= maxDistance;
        }
    }
    
    public void applySolution(){
        for (int i = 0; i<T ; ++i){
            final Vector2f c = new Vector2f(transA[i], transB[i]);
            final Transducer t = onTrans.get(i);
            t.setpAmplitude( c.length() );
            t.setPhase(c.getAngle() / M.PI);
        }
    }
    
    public static Kinoforms create(final MainForm mf, final List<? extends Entity> controlPoints){
        final Kinoforms smp = new Kinoforms(mf, mf.simulation.transducers, controlPoints);
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
