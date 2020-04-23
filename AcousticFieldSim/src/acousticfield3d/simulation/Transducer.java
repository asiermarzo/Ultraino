package acousticfield3d.simulation;

import acousticfield3d.gui.Gradients;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.utils.Color;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Asier
 */
public class Transducer extends MeshEntity{
    public float frequency = 40e3f;        // Hz
    public float apperture = 0.009f;     // apperture (diameter) for directivity calculation
    public float power = 2.53f;           // Transducer power (from microphone measurments).
    
    public String name;
    public float amplitude; //from 0 to 1
    public float phase; //in radians but divided by PI. That is, a phase of 2 means 2PI radians 
    public float phaseCorrection; //the deviation that this current transducer has in phase due to manufacturing, long wires or polarity.
    
    public int type = 0; //0=circle, 1=square... Unimplemented
    
    private int orderNumber;
    private int driverPinNumber; //in the driver board
    
    public boolean useGreyScale = false;
    public boolean useHeightRendering = false;
    private float lastHeight = 0;
 
    public Transducer() {
        super(Resources.MESH_TRANSDUCER, null, Resources.SHADER_SOLID_SPEC);
        
        getMaterial().ambient = 0.8f;
        getMaterial().diffuse = 0.2f;
        getMaterial().specular = 0.1f;
        
        useGreyScale = false;

        name = "no name";
    }

    public void pointToTarget(final Vector3f target){
        getTransform().lookAt( target );
        getTransform().rotateLocal(M.degToRad( -90 ), 0, 0);
    }
    
    public float getPhaseCorrection() {
        return phaseCorrection;
    }

    public void setPhaseCorrection(float phaseCorrection) {
        this.phaseCorrection = phaseCorrection;
    }

    public int getDriverPinNumber() {
        return driverPinNumber;
    }

    public void setDriverPinNumber(int driverPinNumber) {
        this.driverPinNumber = driverPinNumber;
    }
    
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public static int calcDiscPhase(final float phase, final int divs){
        int xPhase;

        xPhase = Math.round(phase * divs / 2) % divs;
        while (xPhase < 0) {
            xPhase += divs;
        }

        return xPhase;
    }
    
    public int getDiscPhase(final int divs){
        return calcDiscPhase(getPhase() + getPhaseCorrection(), divs);
    }

    @Override
    public int getColor() {
        final float p = M.abs( (phase + 1.0f) / 2.0f % 1.0f );
        if(useGreyScale){
            final int ip = (int)(p*255);
            final int ia = (int)(amplitude*255);
            return Color.create( ip, ip, ip, ia);
        }else{
            return Gradients.get().getGradientAmpAndPhase(amplitude, p);
        }
            
    }

    public float calculateTubeHeight(Simulation s){
        final float p = getPhase();
        final float mSpeed = s.getMediumSpeed();
        final float waveLength = mSpeed / getFrequency();
        
        return waveLength * (2.0f + p) / 2.0f;
    }
    
    @Override
    public void update( Simulation s) {
        if ( useHeightRendering ){
            getTransform().getScale().y = calculateTubeHeight(s);
        }
    }
    
    public void snapHeight(){
        lastHeight = getTransform().getScale().y;
    }
    
    public void restoreHeight(){
        getTransform().getScale().y = lastHeight;
    }
    

    @Override
    public String getMesh() {
        if (selected && !useHeightRendering){
            return Resources.MESH_BOX;
        }else{
            return super.getMesh(); 
        }
        
    }
    
    

    //<editor-fold defaultstate="collapsed" desc="getters and setters">

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public float calcRealDiscAmplitude(boolean disc, float discValue){
        if (disc){
            return (M.discretize(getAmplitude(),discValue) ) * getPower();
        }else{
            return (getAmplitude() ) * getPower();
        }
    }

    public float calcRealDiscPhase(boolean disc, float discValue){
        if (disc){
            return ( M.discretize(getPhase(),discValue) ) * M.PI;
        }else{
            return getPhase() * M.PI;
        }
    }
    
    
    public float getAmplitude() {
        return amplitude;
    }
    
    public void setAmplitude(float pAmplitude) {
        this.amplitude = pAmplitude;
    }
    
    public float getPhase() {
        return phase;
    }
    
    public void setPhase(float phase) {
        this.phase = phase;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getApperture() {
        return apperture;
    }

    public void setApperture(float width) {
        this.apperture = width;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public float getpAmplitude() {
        return amplitude;
    }

    public void setpAmplitude(float pAmplitude) {
        this.amplitude = pAmplitude;
    }
    
//</editor-fold>

    @Override
    public String toString() {
        return name;
    }


    public static int getMaxPin(final Set<Transducer> list){
        int max = -1;
        for (Transducer t : list){
             max = M.max(max, t.getDriverPinNumber());
        }
        return max;
    }
    
    public static int getMaxPin(final List<Transducer> list){
        int max = -1;
        for (Transducer t : list){
             max = M.max(max, t.getDriverPinNumber());
        }
        return max;
    }

    public int getDiscAmplitude(int divs) {
        return M.iclamp(Math.round(amplitude * divs / 2), 0, divs);
    }
    
}
