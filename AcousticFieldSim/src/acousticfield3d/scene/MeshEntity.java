package acousticfield3d.scene;

import acousticfield3d.math.Frustrum;
import acousticfield3d.math.Ray;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Texture;
import acousticfield3d.shapes.Mesh;

/**
 *
 * @author Asier
 */
public class MeshEntity extends Entity{
    public float distanceToCamera;
    public int renderingOrder;
    
    String mesh;
    Texture texture;
    int shader;
    boolean visible = true;
    boolean doubledSided = false;
    
    public Mesh customMesh;
    
    public MeshEntity() {
        super();
    }

    public MeshEntity(String mesh, Texture texture, int shader) {
        this.mesh = mesh;
        this.texture = texture;
        this.shader = shader;
    }
    
    
    public float rayToSphere(final Ray r){
        //transform the ray with inverse transform
        Ray rSpace = new Ray(r.origin, r.direction, false);
        transform.transformInversePoint(rSpace.origin, rSpace.origin);
        transform.transformInverseVector(rSpace.direction, rSpace.direction);
        
        //intersection points with the sphere
        Mesh m = Resources.get().getMesh(mesh);
        if(m == null) { return -1.0f; }
        Vector3f p = m.getbSphere().intersectPoint(rSpace);
        if (p == null){return -1.0f;}
        
        //apply transform to points
        transform.transformPoint(p, p);
        
        //return distance if there was a collision
        return p.distance( r.origin );
    }
    
    public float rayToBox(final Ray r){
        //transform the ray with inverse transform
        Ray rSpace = new Ray(r.origin, r.direction, false);
        transform.transformInversePoint(rSpace.origin, rSpace.origin);
        transform.transformInverseVector(rSpace.direction, rSpace.direction);
        
        //intersection points with the box
        Mesh m = Resources.get().getMesh(mesh);
        if(m == null) { return -1.0f; }
        Vector3f p = m.getbBox().intersectPoint(rSpace);
        if (p == null){return -1.0f;}
        
        //apply transform to points
        transform.transformPoint(p, p);
        
        //return distance if there was a collision
        return p.distance( r.origin );
    }
    
    public boolean boxInside(final Frustrum frustrum){
       /* Mesh m = Resources.get().getMesh(mesh);
        if(m == null) { return false; }
        
        Ray rSpace = new Ray(r.origin, r.direction, false);
        transform.transformInversePoint(rSpace.origin, rSpace.origin);
        transform.transformInverseVector(rSpace.direction, rSpace.direction);
        
        //intersection points with the box
        
        Vector3f p = m.getbBox().intersectPoint(rSpace);
        if (p == null){return false;}
        
        //apply transform to points
        transform.transformPoint(p, p);
        
        return true;*/
        return false;
    }

    public boolean isDoubledSided() {
        return doubledSided;
    }

    public void setDoubledSided(boolean isDoubledSided) {
        this.doubledSided = isDoubledSided;
    }

    
    public String getMesh() {
        return mesh;
    }

    public void setMesh(String mesh) {
        this.mesh = mesh;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public int getShader() {
        return shader;
    }

    public void setShader(int shader) {
        this.shader = shader;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

 
    
    
    
}
