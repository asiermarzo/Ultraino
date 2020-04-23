
package acousticfield3d.scene;

import acousticfield3d.math.M;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;

/**
 *
 * @author Asier
 */
public class Camera extends Entity{
    Matrix4f projection;
    float fov;
    float near, far;
    boolean ortho;
    
    boolean observationMode;
    Vector3f observationPoint;
    float inclination, azimuth, distance;
    
    public Camera() {
        super();
        fov = 45;
        ortho = true;
        near = 1.0f;
        far = 1000.f;
        projection = new Matrix4f(Matrix4f.IDENTITY);
        
        observationMode = false;
        observationPoint = new Vector3f();
    }
    
    public void activateObservation(boolean activate, final Vector3f point){
        if (activate){
            observationMode = true;
            observationPoint.set( point );
            Vector3f diff = getTransform().getTranslation().subtract(point);
            
            distance = diff.length();
            inclination = M.acos( diff.y / distance);
            azimuth = M.atan2(-diff.z, diff.x);
            
            getTransform().lookAt( observationPoint );
        }else{
            updateObservation();
            observationMode = false;
        }
    }
    
    public void updateObservation(){
        final Vector3f translation = getTransform().getTranslation();
        translation.fromPolar(distance, azimuth, inclination).addLocal( observationPoint );
        
        getTransform().lookAt( observationPoint );
    }
    
    public void updateProjection(float aspect){
        if (ortho){
            projection.setProjection(near, far, -aspect, aspect, 0.5f, -0.5f, true);
        }else{
            projection.setProjectionFOV(fov, near, far, aspect);
        }
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public void setProjection(Matrix4f projection) {
        this.projection = projection;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public boolean isOrtho() {
        return ortho;
    }

    public void setOrtho(boolean ortho) {
        this.ortho = ortho;
    }

    public boolean isObservationMode() {
        return observationMode;
    }

    public void setObservationMode(boolean observationMode) {
        this.observationMode = observationMode;
    }

    public Vector3f getObservationPoint() {
        return observationPoint;
    }

    public void setObservationPoint(Vector3f observationPoint) {
        this.observationPoint = observationPoint;
    }

    public float getInclination() {
        return inclination;
    }

    public void setInclination(float inclination) {
        this.inclination = inclination;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

  
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getNear() {
        return near;
    }

    public void setNear(float near) {
        this.near = near;
    }

    public float getFar() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
    }

    public void moveAzimuthAndInclination(float diffAzimuth, float diffInclination) {
        azimuth += diffAzimuth;
        inclination += diffInclination;
        final float margin = 0.0001f;
        if (inclination < margin) {
            inclination = margin;
        } else if (inclination > M.PI - margin) {
            inclination = M.PI - margin;
        }
    }
    
    
}
