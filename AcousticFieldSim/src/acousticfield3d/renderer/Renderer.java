package acousticfield3d.renderer;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.TempVars;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.BufferUtils;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Comparator;

import com.jogamp.opengl.GL2;

/**
 *
 * @author Asier
 */
public class Renderer {
    private final Scene scene;
    private final MainForm form;
    
    private boolean cullFace;
    private boolean depthTest;
    private boolean blend;
    private boolean texture2d;
    private boolean texture3d;
    private boolean writeColor;
    
    
    int nTransducers;
    FloatBuffer positions;
    FloatBuffer normals;
    FloatBuffer specs;
    FloatBuffer phase;
    
    boolean needToReuploadTexture;
    private boolean needToReloadShaders;

    public MainForm getForm() {
        return form;
    }
    
    public Renderer(Scene scene, MainForm form) {
        
        needToReloadShaders = false;
        needToReuploadTexture = false;
        this.scene = scene;
        this.form = form;
    }

    public void reloadShaders() {
        needToReloadShaders = true;
    }
    
    
    public void init(GL2 gl, int w, int h){

        Resources.init(gl, true);
        
        gl.glClearColor(0, 0, 0.0f, 1);
        //gl.glClearColor(1, 1, 1, 1);
        
        
        //set camera
        reshape(gl, w, h);
    }
    
    public void reshape(GL2 gl, int w, int h){
        gl.glViewport( 0, 0, w, h );
        
        //set camera
        scene.getCamera().updateProjection(w/(float)h);
    }
    
    public void dispose(GL2 gl){
        //deatach shader
        gl.glUseProgram(0);
        
        //deattach textures
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_3D, 0);
        
        //relase shaders and textures
        Resources.get().releaseResources(gl);
    }
    
    private void preRender(GL2 gl){
        if (needToReloadShaders){
            needToReloadShaders = false;
            Resources.get().reloadShaders(gl);
        }
        
 
        Simulation simulation = form.getSimulation();
        updateTransducersBuffers(simulation);
        
        //check if the shaders using transducers have been compiled with the current number of transducers
        if( nTransducers > 0 ){
            Resources.get().updateShaderTransducers(nTransducers, gl);
        }
   
        //tick the entities in case that they needed something to do, (probably not)
        for(Entity e : scene.getEntities()){
            e.update( simulation );
        }
        
        //update the ground lines
        if (form.pointsPanel.isGroundLineSelected()){
            form.pointsPanel.updateGroundLines();
        }
    }


    public void updateTransducersBuffers(Simulation simulation) {
        //transducers data
        nTransducers = simulation.getTransducers().size();
        final int minCapacity1 = 1  * nTransducers;
        final int minCapacity2 = 2  * nTransducers;
        final int minCapacity3 = 3  * nTransducers;
        final int minCapacity4 = 4  * nTransducers;
        if (positions != null && positions.capacity() < minCapacity3) { BufferUtils.destroyDirectBuffer(positions); positions = null;}
        if (normals != null && normals.capacity() < minCapacity3) { BufferUtils.destroyDirectBuffer(normals); normals = null;}
        if (specs != null && specs.capacity() < minCapacity4) { BufferUtils.destroyDirectBuffer(specs); specs = null;}
        
        if (phase != null && phase.capacity() < minCapacity1) { BufferUtils.destroyDirectBuffer(phase); phase = null;}
        
        if (positions == null){ positions = BufferUtils.createFloatBuffer(minCapacity3); }
        if (normals == null){ normals = BufferUtils.createFloatBuffer( minCapacity3); }
        if (specs == null){ specs = BufferUtils.createFloatBuffer(minCapacity4); }
        
        if (phase == null){ phase = BufferUtils.createFloatBuffer(minCapacity1); }
        
        
        positions.rewind();
        normals.rewind();
        specs.rewind();
        phase.rewind();
        
        //calculate transducer uniform
        // x y z 1 -> positions
        // nx ny nz 1 -> normals
        // k amp phase w -> specs
        
        Vector3f transNormal = new Vector3f();
        final float mSpeed = form.simForm.getMediumSpeed();
        
        final boolean discAmp = form.miscPanel.isAmpDiscretizer();
        final boolean discPhase = form.miscPanel.isPhaseDiscretizer();
        final float ampDiscStep = 1.0f / form.miscPanel.getAmpDiscretization();
        final float phaseDiscStep = 1.0f / form.miscPanel.getPhaseDiscretization();
        
        for(Transducer t : simulation.getTransducers()){
            positions.put( t.getTransform().getTranslation().x );
            positions.put( t.getTransform().getTranslation().y );
            positions.put( t.getTransform().getTranslation().z );
            
            t.getTransform().getRotation().mult( Vector3f.UNIT_Y, transNormal);
            transNormal.normalizeLocal();
            normals.put( transNormal.x );
            normals.put( transNormal.y );
            normals.put( transNormal.z );
            
            final float omega = M.TWO_PI * t.getFrequency();      // angular frequency
            final float k = omega / mSpeed;        // wavenumber
            final float amp = t.calcRealDiscAmplitude(discAmp, ampDiscStep );
            final float phase = t.calcRealDiscPhase(discPhase, phaseDiscStep);
            specs.put( k ); // k
            specs.put( amp ); // amp
            specs.put( phase ); // phase
            specs.put( t.getApperture() ); // width
            
            this.phase.put( phase );
        }
        
        positions.rewind();
        normals.rewind();
        specs.rewind();
        phase.rewind();
    }
    
    private void postRender(GL2 gl){
 
    }
    
    public void render(GL2 gl, int w, int h){
        preRender(gl);
        
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        
        Matrix4f projection = scene.getCamera().getProjection();
        
        Matrix4f view = new Matrix4f();
        scene.getCamera().getTransform().copyTo(view);
        view.invertLocal();
     
        TempVars tv = TempVars.get();
        
        gl.glEnable(GL2.GL_CULL_FACE); cullFace = true;
	gl.glEnable(GL2.GL_DEPTH_TEST); depthTest = true;
	gl.glDisable(GL2.GL_BLEND); blend = false;
        gl.glDisable(GL2.GL_TEXTURE_2D); texture2d = false;
        gl.glDisable(GL2.GL_TEXTURE_3D); texture3d = false;
        gl.glColorMask(true, true, true, true); writeColor = true;

        Texture lastTexture = null;
	Shader lastShader = null;
	Matrix4f model = tv.tempMat4;
        Matrix4f viewModel = tv.tempMat42;
        Matrix4f projectionViewModel = tv.tempMat43;

        synchronized (form) {
            calcDistanceToCameraOfEntities();
            sortEntities();
            
            for (MeshEntity me : scene.getEntities()) {

                if (!me.isVisible()) {
                    continue;
                }

                me.getTransform().copyTo(model);

                int shaderId = me.getShader();
                Shader currentShader = Resources.get().getShader(shaderId);
                if (currentShader == null) {
                    continue;
                }
                if (lastShader != currentShader) {
                    lastShader = currentShader;
                    gl.glUseProgram(lastShader.shaderProgramID);
                    gl.glUniform1i(lastShader.texDiffuse, 0);
                }
                if(lastShader != null) { lastShader.changeGLStatus(gl, this, form.getSimulation(), me); }

                if (lastTexture != me.getTexture()) {
                    lastTexture = me.getTexture();
                    if (lastTexture != null) {
                        gl.glBindTexture(GL2.GL_TEXTURE_2D, lastTexture.getId());
                    } else {
                        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
                    }
                }

                //check for negative scale
                boolean usesCull = true;
                boolean needReverseCulling = usesCull && (model.get(0, 0) * model.get(1, 1) * model.get(2, 2) < 0);

                if (needReverseCulling) {
                    //gl.glCullFace(GL2.GL_FRONT);
                }

                view.mult(model, viewModel);
                projection.mult(viewModel, projectionViewModel);

                lastShader.bindAttribs(gl, form.getSimulation(), me);

                lastShader.bindUniforms(gl, scene, this, form.getSimulation(), me, projectionViewModel, viewModel, model, tv.floatBuffer16);

                lastShader.render(gl, form.getSimulation(), me);

                lastShader.unbindAttribs(gl, form.getSimulation(), me);

                if (needReverseCulling) {
                    //gl.glCullFace(GL2.GL_BACK);
                }

            }
        }

        tv.release();

        postRender(gl);
    }
        
    void enableCullFace(GL2 gl, boolean enabled){
        if (enabled != cullFace){
            if (enabled){
                gl.glEnable(GL2.GL_CULL_FACE);
            }else{
                gl.glDisable(GL2.GL_CULL_FACE);
            }
            cullFace = enabled;
        }
    }
    
    void enableDepthTest(GL2 gl, boolean enabled){
        if (enabled != depthTest){
            if (enabled){
                gl.glEnable(GL2.GL_DEPTH_TEST);
            }else{
                gl.glDisable(GL2.GL_DEPTH_TEST);
            }
            depthTest = enabled;
        }
    }
        
    void enableBlend(GL2 gl, boolean enabled){
        if (enabled != blend){
            if (enabled){
                gl.glEnable(GL2.GL_BLEND);
                gl.glBlendFunc (GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            }else{
                gl.glDisable(GL2.GL_BLEND);
            }
            blend = enabled;
        }
    }
            
    void enableTexture2D(GL2 gl, boolean enabled){
        if (enabled != texture2d){
            if (enabled){
                gl.glEnable(GL2.GL_TEXTURE_2D);
            }else{
                gl.glDisable(GL2.GL_TEXTURE_2D);
            }
            texture2d = enabled;
        }
    }
    
    void enableTexture3D(GL2 gl, boolean enabled){
        if (enabled != texture3d){
            if (enabled){
                gl.glEnable(GL2.GL_TEXTURE_3D);
            }else{
                gl.glDisable(GL2.GL_TEXTURE_3D);
            }
            texture3d = enabled;
        }
    }
    
    void enableWriteColor(GL2 gl, boolean enabled){
        if (enabled != writeColor){
            if (enabled){
                gl.glColorMask(true, true, true, true);
            }else{
                gl.glColorMask(false, false, false, false);
            }
            writeColor = enabled;
        }
    }
    
    

    private void sortEntities() {
        Collections.sort(scene.getEntities(), new Comparator<MeshEntity>() {
                @Override
                public int compare(MeshEntity o1, MeshEntity o2) {
                    final int r1 = o1.renderingOrder;
                    final int r2 = o2.renderingOrder;
                    if (r1 == r2){
                        if (r1 == Shader.ORDER_TRANSLUCENT){
                            return Float.compare( o1.distanceToCamera, o2.distanceToCamera);
                        }else{
                            return Float.compare( o2.distanceToCamera, o1.distanceToCamera);
                        }
                    }else{
                        return Integer.compare(r1, r2);
                    }
                  
                }
            });
    }

    private void calcDistanceToCameraOfEntities() {
        Vector3f camRay = new Vector3f( Vector3f.UNIT_Z );
        Vector3f oPos = new Vector3f();
        Vector3f cPos = scene.getCamera().getTransform().getTranslation();
        scene.getCamera().getTransform().getRotation().mult(camRay, camRay);
        for(MeshEntity me : scene.getEntities()){
            if(me.isVisible()){
                oPos.set( me.getTransform().getTranslation() ).subtractLocal( cPos );
                me.distanceToCamera = camRay.dot( oPos );
                me.renderingOrder = Resources.get().getShader( me.getShader() ).getRenderingOrder( me );
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="getters">
   
    public int getnTransducers() {
        return nTransducers;
    }
    
    public FloatBuffer getPositions() {
        return positions;
    }
    
    public FloatBuffer getNormals() {
        return normals;
    }
    
    public FloatBuffer getSpecs() {
        return specs;
    }
    
  
//</editor-fold>

  
}
