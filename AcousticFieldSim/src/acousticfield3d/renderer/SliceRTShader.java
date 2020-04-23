package acousticfield3d.renderer;

import acousticfield3d.math.Matrix4f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.FieldSource;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.utils.Color;
import java.nio.FloatBuffer;
import java.util.HashMap;

import com.jogamp.opengl.GL2;

/**
 *
 * @author Asier
 */
public class SliceRTShader extends ShaderTransducers{
    private FieldSource source;
    
    int colouring;
    int minPosColor, maxPosColor;
    int minNegColor, maxNegColor;
 
    
    public SliceRTShader(String vProgram, String fProgram, FieldSource source) {
        super(vProgram, fProgram, ORDER_OPAQUE, 10);
        this.source = source;
    }

    @Override
    public int getRenderingOrder(MeshEntity me) {
        final float alpha = Color.alpha( me.getColor() ) / 255.0f;
        
        if (alpha < 1.0f){
            return ORDER_TRANSLUCENT;
        }else{
            return ORDER_OPAQUE;
        }
    }
    
    public FieldSource getSource() {
        return source;
    }

    public void setSource(FieldSource source) {
        this.source = source;
    }
    

    @Override
    void getUniforms(GL2 gl) {
        super.getUniforms(gl);
 
        minPosColor = gl.glGetUniformLocation(shaderProgramID, "minPosColor");
        maxPosColor = gl.glGetUniformLocation(shaderProgramID, "maxPosColor");
        colouring = gl.glGetUniformLocation(shaderProgramID, "colouring");
        
        minNegColor = gl.glGetUniformLocation(shaderProgramID, "minNegColor");
        maxNegColor = gl.glGetUniformLocation(shaderProgramID, "maxNegColor");
    }
    
    @Override
    void changeGLStatus(GL2 gl, Renderer renderer, Simulation s, MeshEntity e) {
        final float alpha = Color.alpha( e.getColor() ) / 255.0f;
        
        renderer.enableBlend(gl, alpha < 1.0f);
        if (e.getMesh().equals( Resources.MESH_QUAD ) || e.getMesh().equals( Resources.MESH_CUSTOM )){
            renderer.enableCullFace(gl, false);
        }else{
            renderer.enableCullFace(gl, true);
        }
        
        renderer.enableDepthTest(gl, true);
        renderer.enableTexture2D(gl, false);
    }

    @Override
    void bindUniforms(GL2 gl, Scene scene, Renderer renderer,Simulation s, MeshEntity me, Matrix4f projectionViewModel, Matrix4f viewModel, Matrix4f model, FloatBuffer fb) {
       super.bindUniforms(gl, scene, renderer, s, me, projectionViewModel, viewModel, model, fb);
        
       gl.glUniform1f(minPosColor, renderer.getForm().rtSlicePanel.getAmpColorMin());
       gl.glUniform1f(maxPosColor, renderer.getForm().rtSlicePanel.getAmpColorMax());
       
       gl.glUniform1f(minNegColor, -renderer.getForm().rtSlicePanel.getAmpColorMax());
       gl.glUniform1f(maxNegColor, -renderer.getForm().rtSlicePanel.getAmpColorMin());
       
       gl.glUniform1i(colouring, 4); //ice gradient


    }

    @Override
    protected String preProcessFragment(String sourceCode, HashMap<String,String> templates) {
        sourceCode = super.preProcessFragment(sourceCode, templates);
        
        sourceCode = sourceCode.replaceAll("_USE_AMP_", source == FieldSource.sourceAmp ? "" : "//");
        sourceCode = sourceCode.replaceAll("_USE_PHASE_", source == FieldSource.sourcePhase ? "" : "//");
        sourceCode = sourceCode.replaceAll("_USE_AMPPHASE_", source == FieldSource.sourceAmpPhase ? "" : "//");
        
        return sourceCode;
    }

    @Override
    void render(GL2 gl, Simulation s, MeshEntity me) {
        //TimerUtil.get().tick("Render slice");
        super.render(gl, s, me); //To change body of generated methods, choose Tools | Templates.
        //TimerUtil.get().tack("Render slice");
    }
    
    
}
