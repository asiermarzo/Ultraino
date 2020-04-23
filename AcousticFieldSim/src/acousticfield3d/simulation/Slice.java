package acousticfield3d.simulation;

import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Texture;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.utils.Color;

/**
 *
 * @author am14010
 */
public class Slice extends MeshEntity{
    Entity followEntity = null;
    boolean stickyFollow = false;
    
    public Slice() {
        super(Resources.MESH_QUAD, null, Resources.SHADER_SOLID);
        setTag( Entity.TAG_SLICE );
    }
    
    public Slice(String mesh, Texture texture, int shader) {
        super(mesh, texture, shader);
        setTag( Entity.TAG_SLICE );
    }

    public Entity getFollowEntity() {
        return followEntity;
    }

    public void setFollowEntity(Entity followEntity) {
        this.followEntity = followEntity;
    }

    public boolean isStickyFollow() {
        return stickyFollow;
    }

    public void setStickyFollow(boolean stickyFollow) {
        this.stickyFollow = stickyFollow;
    }

    public void clearSticky(){
        followEntity = null;
    }
    
    @Override
    public void update(Simulation s) {
        if (followEntity != null) {
            final Vector3f target = followEntity.getTransform().getTranslation();
            final Vector3f myPos = getTransform().getTranslation();
            
            if (stickyFollow) {
                myPos.set( target );
            } else {
                final Vector3f rot = getTransform().getRotation().toAngles(null).multLocal( M.RAD_TO_DEG );
                
                if (M.abs(rot.x) > 45.0f) {
                    myPos.y = target.y;
                } else if (M.abs(rot.y) > 45.0f) {
                    myPos.x = target.x;
                } else {
                    myPos.z = target.z;
                }
            }
        }
    }
    
        
    @Override
    public int getColor() {
        final int c = super.getColor();
        if(!selected){
            return c;
        }else{
            return Color.changeAlpha(c, Color.alpha(c) * 3 / 4);
        }
    }
    
}
