package acousticfield3d.simulation;

import acousticfield3d.algorithms.SimplePhaseAlgorithms;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.utils.Parse;
import java.util.List;

/**
 *
 * @author am14010
 */
public class Trap {
    public enum TrapType{
        None, Twin, Vortex
    };
    
    public boolean focus = true;
    public TrapType type = TrapType.None;
    public float parameter1; //angle for the twin, m for the vortex

    public Trap() {
    }

    //<editor-fold defaultstate="collapsed" desc="props">
    public boolean isFocus() {
        return focus;
    }
    
    public void setFocus(boolean focus) {
        this.focus = focus;
    }
    
    public TrapType getType() {
        return type;
    }
    
    public void setType(TrapType type) {
        this.type = type;
    }
    
    public float getParameter1() {
        return parameter1;
    }
    
    public void setParameter1(float parameter1) {
        this.parameter1 = parameter1;
    }
//</editor-fold>
    
    public void apply(final Simulation s, final List<Transducer> transducers, final Vector3f target){
        final float mSpeed = s.getMediumSpeed();
        
        if (focus){
            SimplePhaseAlgorithms.focus( transducers, target, mSpeed );
        }
        if(type == TrapType.Twin){
            SimplePhaseAlgorithms.addTwinSignature(transducers, parameter1 * M.DEG_TO_RAD);
        }else if(type == TrapType.Vortex){
            SimplePhaseAlgorithms.addVortexSignature(transducers, parameter1 );
        }
    }
    
}
