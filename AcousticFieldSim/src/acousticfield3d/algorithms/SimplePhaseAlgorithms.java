package acousticfield3d.algorithms;

import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author Asier
 */
public class SimplePhaseAlgorithms {
  
    public static void focus(final Transducer t, final Vector3f target, float speedOfSound){
            final float distance = target.distance( t.getTransform().getTranslation() );
            final float waveLength = speedOfSound / t.getFrequency();
            final float targetPhase = (1.0f - M.decPart(distance / waveLength) ) * 2.0f * M.PI;
            t.setPhase(targetPhase / M.PI );
    }
    
    public static void focus(final List<Transducer> trans, final Vector3f target, float speedOfSound){
        for(Transducer t : trans){
            final float distance = target.distance( t.getTransform().getTranslation() );
            final float waveLength = speedOfSound / t.getFrequency();
            final float targetPhase = (1.0f - M.decPart(distance / waveLength) ) * 2.0f * M.PI;
            t.setPhase(targetPhase / M.PI );
        }
    }
    
    public static void addTwinSignature(final List<Transducer> trans, final float angle){
        Vector3f min = new Vector3f(), max = new Vector3f();
        Scene.calcBoundaries(trans, min, max);
        final Vector3f size = max.subtract( min );
        final Vector3f center = max.add(min).divideLocal( 2 );
        
        for(Transducer t : trans){
            final Vector3f pos = t.getTransform().getTranslation();
            final Vector3f npos3 = pos.subtract( center ).divideLocal( size );
            final Vector2f p = new Vector2f( npos3.x, npos3.z);
            
            float value = 0;
            value = (p.getAngle() + angle) / M.PI % 2.0f;
            if (value >= 0.0f && value <= 1.0f) { value = 0.0f;}
            else { value = 1.0f; }
            
            t.phase += value;
        }
    }
    
    public static void addVortexSignature(final List<Transducer> trans, final float m){
        Vector3f min = new Vector3f(), max = new Vector3f();
        Scene.calcBoundaries(trans, min, max);
        final Vector3f size = max.subtract( min );
        final Vector3f center = max.add(min).divideLocal( 2 );
        
        for(Transducer t : trans){
            final Vector3f pos = t.getTransform().getTranslation();
            final Vector3f npos3 = pos.subtract( center ).divideLocal( size );
            final Vector2f p = new Vector2f( npos3.x, npos3.z);
            
            float value = 0;
            value = (p.getAngle() * m) / M.PI % 2.0f;
            
            t.phase += value ;
        }
    }
            
}
