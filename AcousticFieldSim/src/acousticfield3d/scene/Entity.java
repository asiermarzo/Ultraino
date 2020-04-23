package acousticfield3d.scene;

import acousticfield3d.math.Frustrum;
import acousticfield3d.math.Transform;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Material;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.utils.Color;
import java.util.ArrayList;

/**
 *
 * @author Asier
 */
public class Entity {
    public static final int TAG_NONE = 1<<0;
    public static final int TAG_TRANSDUCER = 1<<1;
    public static final int TAG_CONTROL_POINT = 1<<2;
    public static final int TAG_SLICE = 1<<3;
    public static final int TAG_SIMULATION_BOUNDINGS = 1<<4;
    public static final int TAG_CUBE_HELPER = 1<<5;
    public static final int TAG_GROUND_LINE = 1<<6;
    public static final int TAG_MASK = 1<<9;
    public static final int TAG_OBJ = 1<<11;

    Material material;
    int color;
    Transform transform;
    ArrayList<Behaviour> behaviours;
    int tag;
    int number;
    public boolean selected;
    
    
    public Entity() {
        tag = TAG_NONE;
        color = Color.WHITE;
        material = new Material();
        transform = new Transform();
        behaviours = new ArrayList<>();
        selected = false;
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    
    public int getColor() {
        if(!selected){
            return color;
        }else{
            return Color.GREEN;
        }
    }
    
    
    public void setAlpha(float alpha){
        color = Color.changeAlpha(color, (int) (alpha * 255));
    }
    
    public int getRealColor(){
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public Transform getTransform() {
        return transform;
    }
    
    public void setTransform(Transform transform) {
        this.transform = transform;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public int getTag() {
        return tag;
    }
    
    public void setTag(int tag) {
        this.tag = tag;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    
    
    public ArrayList<Behaviour> getBehaviours() {
        return behaviours;
    }
    
    public void setBehaviours(ArrayList<Behaviour> behaviours) {
        this.behaviours = behaviours;
    }
//</editor-fold>

    public void update(Simulation s){
        
    }
    
    public void lookAt(Entity other){
        Vector3f dir = other.getTransform().getTranslation().subtract( getTransform().getTranslation() );
        dir.negateLocal();
        getTransform().getRotation().lookAt(dir, Vector3f.UNIT_Y);
    }
    
    public void rotateAround(final Vector3f pivot, float rx, float ry, float rz){
        getTransform().rotateAround( pivot, rx, ry, rz);
    }

    public boolean boxInside(final Frustrum frustrum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public float distanceTo(final Entity e){
        return getTransform().getTranslation().distance( e.getTransform().getTranslation());
    }
    
    public float distanceTo(final Vector3f v){
        return getTransform().getTranslation().distance( v );
    }
}
