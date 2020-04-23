package acousticfield3d.simulation;

import acousticfield3d.math.M;
import acousticfield3d.math.Transform;

/**
 *
 * @author Asier
 */
public class TransState {
    Transducer transducer;
    
    Transform transform = new Transform();
    float amplitude, phase;

    public TransState() {
    }
    
    //<editor-fold defaultstate="collapsed" desc="props">
    public Transducer getTransducer() {
        return transducer;
    }
    
    public void setTransducer(Transducer transducer) {
        this.transducer = transducer;
    }
    
    public float getAmplitude() {
        return amplitude;
    }
    
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }
    
    public float getPhase() {
        return phase;
    }
    
    public void setPhase(float phase) {
        this.phase = phase;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }
    
    
//</editor-fold>
    
    void apply() {
        transducer.amplitude = (float)amplitude;
        transducer.phase = (float)phase;
        transducer.getTransform().set( transform );
    }

    void applyMixed(TransState tsB, float p) {
        transducer.amplitude = M.lerp(amplitude, tsB.amplitude, p);
        transducer.phase = M.lerp(phase, tsB.phase, p);
        transducer.getTransform().setLerp(transform, tsB.transform, p);
    }

    void snap() {
        amplitude = transducer.amplitude;
        phase = transducer.phase;
        transform.set( transducer.getTransform() );
    }
}
