/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.simulation;

import acousticfield3d.renderer.Texture;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import java.util.List;

/**
 *
 * @author am14010
 */
public class ControlPoint extends MeshEntity{
    private boolean autoTrap = false;
    private Trap trap = null;
    private List<Transducer> trans = null;
    
    
    public ControlPoint() {
        super(Resources.MESH_SPHERE, null, Resources.SHADER_SOLID);
        setTag(Entity.TAG_CONTROL_POINT);
    }

    public ControlPoint(String mesh, Texture texture, int shader) {
        super(mesh, texture, shader);
        setTag(Entity.TAG_CONTROL_POINT);
    }

    //<editor-fold defaultstate="collapsed" desc="props">

    public Trap getTrap() {
        return trap;
    }

    public void setTrap(Trap trap) {
        this.trap = trap;
    }
    
    
    public boolean isAutoTrap() {
        return autoTrap;
    }
    
    public void setAutoTrap(boolean autoTrap) {
        this.autoTrap = autoTrap;
    }
    
    public List<Transducer> getTrans() {
        return trans;
    }
    
    public void setTrans(List<Transducer> trans) {
        this.trans = trans;
    }
//</editor-fold>

    @Override
    public void update(Simulation s) {
        if(autoTrap && trans != null && trap != null){
            trap.apply(s, trans, this.getTransform().getTranslation());
        }
    }

    public void removeAutoTrap(){
        autoTrap = false;
        trans = null;
        trap = null;
    }
    
}
