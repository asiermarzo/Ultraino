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
    public ControlPoint() {
        super(Resources.MESH_SPHERE, null, Resources.SHADER_SOLID);
        setTag(Entity.TAG_CONTROL_POINT);
    }

    public ControlPoint(String mesh, Texture texture, int shader) {
        super(mesh, texture, shader);
        setTag(Entity.TAG_CONTROL_POINT);
    }

    //<editor-fold defaultstate="collapsed" desc="props">

//</editor-fold>

  
}
