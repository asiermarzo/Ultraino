package acousticfield3d.scene;

import acousticfield3d.math.Frustrum;
import acousticfield3d.math.M;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Quaternion;
import acousticfield3d.math.Ray;
import acousticfield3d.math.Vector3f;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.utils.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Asier
 */
public class Scene {
    Camera camera;
    Light light;
    ArrayList<MeshEntity> entities;
    
    MeshEntity cubeHelper;
  
    public Scene() {
        camera = new Camera();
        light = new Light();
        entities = new ArrayList<>();
        initScene();
    }


    public void adjustCameraNearAndFar(Simulation s){
        getCamera().setNear( s.getMinSize() / 2.0f);
        getCamera().setFar( s.maxDistanceBoundary() * 10.0f);
    }
    
    private void adjustCameraToPoint(Simulation s, float aspect, Vector3f position){
        Vector3f simCenter = s.getSimulationCenter();
        
        adjustCameraNearAndFar(s);
        getCamera().setOrtho(false);
        getCamera().updateProjection( aspect );
        getCamera().getTransform().getTranslation().set(
                position.x, 
                position.y,
                position.z);
        getCamera().activateObservation(true, simCenter);
    }
    
    public void adjustCameraToSimulation(Simulation s, float aspect){
        Vector3f simCenter = s.getSimulationCenter();
        Vector3f simMax = s.getBoundaryMax();
        
        adjustCameraToPoint( s, aspect,
                new Vector3f( simCenter.x, 
                simMax.y + getCamera().getNear() * 8.0f,
                simMax.z + getCamera().getNear() * 8.0f ));
    }
    
    public void adjustCameraToTop(Simulation s, float aspect){
        Vector3f simCenter = s.getSimulationCenter();
        Vector3f simMax = s.getBoundaryMax();
        
        adjustCameraToPoint( s, aspect,
                new Vector3f( simCenter.x, 
                simMax.y + getCamera().getNear() * 8.0f,
                simCenter.z ));
    }
    
    public void adjustCameraToFront(Simulation s, float aspect){
        Vector3f simCenter = s.getSimulationCenter();
        Vector3f simMax = s.getBoundaryMax();
        
        adjustCameraToPoint( s, aspect,
                new Vector3f( simCenter.x, 
                simCenter.y,
                simMax.z + getCamera().getNear() * 8.0f ));
    }
    
    public void adjustCameraToCover(Entity e) {
        final Vector3f pos = e.getTransform().getTranslation();
        final Quaternion rot = e.getTransform().getRot();
        final Vector3f scale = e.getTransform().getScale();
        
        final float w2 = scale.getX() / 2.0f;
        final float h2 = scale.getY() / 2.0f;
        final float dist = w2 + h2;
        final Vector3f normal = rot.mult( Vector3f.UNIT_Z );
        
        final Vector3f camPos = camera.getTransform().getTranslation();
        camPos.set( normal );
        camPos.multLocal( dist );
        camPos.addLocal( pos );
       
        camera.getTransform().lookAt( pos );
        camera.getProjection().setProjection(
                camera.getNear(), camera.getFar(), 
                -w2, w2, h2, -h2, true);
    }
    
  
    public void addTransducersFromSimulation(Simulation simulation){
        entities.addAll( simulation.getTransducers() );
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }

    public ArrayList<MeshEntity> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<MeshEntity> entities) {
        this.entities = entities;
    }

    private void initScene() {
        camera.getTransform().setTranslation(0, 0, 20);
        camera.setFov(45); camera.setOrtho(false);
        camera.updateProjection(1.5f);
        camera.setObservationMode(true);
        
        
        light.getTransform().getTranslation().set(10,10,-10);
        //light.getBehaviours().add( new RotateAround(new Vector3f(0, 10, 0), 10, 1f));
        
        MeshEntity me;
        me = new MeshEntity(Resources.MESH_SPHERE, null, Resources.SHADER_SOLID);
        me.setColor(Color.WHITE);
        me.getTransform().setScale(0.0001f);
        me.getTransform().getTranslation().set(light.getTransform().getTranslation());
        //me.getBehaviours().add(new Follow(light));
        entities.add(me);
       
    }
    
    //x and y should be in the range -1, 1
    public Vector3f screenPointToVector(float x, float y){
        x = x * 2.0f - 1.0f;
        y = y * 2.0f - 1.0f;
        
        Vector3f toReturn =  new Vector3f(x, y, -camera.getNear());
        
        Matrix4f invProjection = getCamera().getProjection().invert();
        
        invProjection.multiplyPoint(toReturn, toReturn);
        
        getCamera().getTransform().transformPoint(toReturn, toReturn);
        
        return toReturn;
    }

    public Ray pointToRay(float x, float y) {
        Ray r = new Ray();
        r.fromTwoPoints(camera.getTransform().getTranslation(), screenPointToVector(x, y));
        return r;
    }
    
    public Vector3f clickToObject(float x, float y, MeshEntity entity){
        Ray r = pointToRay(x, y);
        float currentDistance = entity.rayToBox(r);
        return r.pointAtDistance( currentDistance );
    }
    
    public MeshEntity pickObject(float x, float y, int tagBits){
        Ray r = pointToRay(x, y);
        
        float minDistance = Float.MAX_VALUE;
        MeshEntity pick = null;
        for( MeshEntity me : entities){
            if((me.tag & tagBits) != 0 ){
                float currentDistance = me.rayToBox(r);
                if (currentDistance >= 0.0f && currentDistance < minDistance){
                    minDistance = currentDistance;
                    pick = me;
                }
            }
        }
        return pick;
    }
    
    public static void setVisible(List<MeshEntity> list, int tagBit, int number, boolean visible){
        Iterator<MeshEntity> i = list.iterator();
        while (i.hasNext()){
            MeshEntity e = i.next();
            if ((e.tag & tagBit) != 0){
                if ( (number == -1 || e.number == number)){
                    e.setVisible(visible);
                }else{
                    e.setVisible(! visible);
                }
            }
        }
    }
    
    
    public static void setVisible(List<MeshEntity> list, int tagBit, boolean visible){
        Iterator<MeshEntity> i = list.iterator();
        while (i.hasNext()){
            MeshEntity e = i.next();
            if ((e.tag & tagBit) != 0){
                e.setVisible(visible);
            }
        }
    }
    
    public static void setShader(List<MeshEntity> list, int tagBit, int shader){
        Iterator<MeshEntity> i = list.iterator();
        while (i.hasNext()){
            MeshEntity e = i.next();
            if ((e.tag & tagBit) != 0){
                e.setShader( shader );
            }
        }
    }
    
    public static void removeWithTag(List<? extends Entity> list, int tagBit){
        Iterator<? extends Entity> i = list.iterator();
        while (i.hasNext()){
            Entity e = i.next();
            if ((e.tag & tagBit) != 0){
                i.remove();
            }
        }
    }
    
    public static void keepWithTag(List<? extends Entity> list, int tagBit){
        Iterator<? extends Entity> i = list.iterator();
        while (i.hasNext()){
            Entity e = i.next();
            if ((e.tag & tagBit) == 0){
                i.remove();
            }
        }
    }
    
    public static Entity getFirstWithTag(List<? extends Entity> list, int tag){
        for(Entity e : list){
            if( (e.getTag() & tag) != 0){
                return e;
            }
        }
        return null;
    }
    
    public Entity getFirstWithTag(int tag){
        for(MeshEntity e : entities){
            if( (e.getTag() & tag) != 0){
                return e;
            }
        }
        return null;
    }
    
    public void gatherMeshEntitiesWithTag(ArrayList<MeshEntity> a, int tag){
        for(MeshEntity e : entities){
            if( (e.getTag() & tag) != 0){
                a.add(e);
            }
        }
    }
    
    public void updateBoundaryBoxes(Simulation simulation){
        removeWithTag(entities, Entity.TAG_SIMULATION_BOUNDINGS);
        
        MeshEntity me;
        final float boxWidth = simulation.getMinSize() / 16.0f;
        Vector3f min = simulation.getBoundaryMin();
        Vector3f max = simulation.getBoundaryMax();
        final float simWidth = max.y - min.y;
        final float midY = (max.y + min.y) / 2.0f;
        
        final int barColor = Color.WHITE;
        
        me = new MeshEntity(Resources.MESH_BOX, null, Resources.SHADER_SOLID);
        me.setColor(barColor);
        me.tag = Entity.TAG_SIMULATION_BOUNDINGS;
        me.getTransform().getTranslation().set(min.x, midY, min.z);
        me.getTransform().getScale().set(boxWidth,simWidth,boxWidth);
        entities.add(me);
        
        me = new MeshEntity(Resources.MESH_BOX, null, Resources.SHADER_SOLID);
        me.setColor(barColor);
        me.tag = Entity.TAG_SIMULATION_BOUNDINGS;
        me.getTransform().getTranslation().set(min.x, midY, max.z);
        me.getTransform().getScale().set(boxWidth,simWidth,boxWidth);
        entities.add(me);
        
        me = new MeshEntity(Resources.MESH_BOX, null, Resources.SHADER_SOLID);
        me.setColor(barColor);
        me.tag = Entity.TAG_SIMULATION_BOUNDINGS;
        me.getTransform().getTranslation().set(max.x, midY, min.z);
        me.getTransform().getScale().set(boxWidth,simWidth,boxWidth);
        entities.add(me);
        
        me = new MeshEntity(Resources.MESH_BOX, null, Resources.SHADER_SOLID);
        me.setColor(barColor);
        me.tag = Entity.TAG_SIMULATION_BOUNDINGS;
        me.getTransform().getTranslation().set(max.x, midY, max.z);
        me.getTransform().getScale().set(boxWidth,simWidth,boxWidth);
        entities.add(me);
        
    }


    public void initHelperObjects() {
        removeWithTag(entities, Entity.TAG_CUBE_HELPER);
        
        cubeHelper = new MeshEntity(Resources.MESH_BOX, null, Resources.SHADER_SOLID);
        cubeHelper.setTag( Entity.TAG_CUBE_HELPER );
        cubeHelper.visible = false;
        cubeHelper.color = Color.WHITE;
        cubeHelper.getMaterial().ambient = 0.7f;
        cubeHelper.getMaterial().diffuse = 0.5f;
        cubeHelper.getMaterial().specular = 0.5f;
        cubeHelper.getMaterial().shininess = 10;
        getEntities().add(cubeHelper );    
    }
    
   
   

  
    public MeshEntity getCubeHelper() {
        return cubeHelper;
    }

  
    public void removeWithTag(int tag) {
        Iterator<MeshEntity> iter = entities.iterator();
        while(iter.hasNext()){
            MeshEntity me = iter.next();
            if( (me.getTag() & tag) != 0 ){
                iter.remove();
            }
        }
    }

    public static float smallestObject(List<? extends Entity> entities){
        if(entities.isEmpty()){
            return 0.0f;
        }
        
        float min = Float.MAX_VALUE;
        for(Entity t : entities){
            final Vector3f scale = t.getTransform().getScale();
            min = M.min( min , scale.minComponent());
        }
        
        return min;
    }
    
    public static Vector3f calcSize(final List<? extends Entity> entities){
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        calcBoundaries(entities,min,max);
        return max.subtract(min);
    }
    
    public static void calcBoundaries(List<? extends Entity> entities, Vector3f min, Vector3f max){
        min.set(Float.MAX_VALUE);
        max.set(-Float.MAX_VALUE);
        for(Entity t : entities){
            final Vector3f pos = t.getTransform().getTranslation();
            min.setMin( pos );
            max.setMax( pos );
        }
    }

  
      public static Vector3f calcCenter(final List<? extends Entity> entities){
        final Vector3f center = new Vector3f();
        for(Entity e : entities){
            center.addLocal( e.getTransform().getTranslation());
        }
        center.divideLocal( entities.size() );
        
        return center;
    }
      
      public static float averageDistance(final Vector3f center, final List<? extends Entity> entities){
          float dist = 0;
          for(Entity e : entities){
              dist += e.getTransform().getTranslation().distance(center);
          }
          return dist / entities.size();
      }
      
    public void recenterTo(final List<? extends Entity> entities, final Vector3f center) {       
        for(Entity e :entities){
            e.getTransform().getTranslation().subtractLocal(center);
        }

    }
 
    public float getParticleRadious(){
        Entity bead = getFirstWithTag( Entity.TAG_CONTROL_POINT );
        if (bead == null){
            return 0.0005f;
        }
       
        return bead.getTransform().getScale().maxComponent() / 2.0f;
    }

    public ArrayList<Entity> pickObjectsWithDrag(float sx, float sy, float ex, float ey, int tags) {
        //order the points
        final float minX = M.min(sx, ex);
        final float minY = M.min(sy, ey);
        final float maxX = M.max(sx, ex);
        final float maxY = M.max(sy, ey);
        
        final Frustrum frustrum = new Frustrum(camera, minX, minY, maxX, maxY);
        
        final ArrayList<Entity> selected = new ArrayList<>();
        for (MeshEntity e : entities){
            if ( (e.getTag() & tags) != 0 && e.boxInside(frustrum)){
                selected.add(e);
            }
        }
        return selected;
    }
}
