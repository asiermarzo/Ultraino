package acousticfield3d.renderer;

import acousticfield3d.gui.panels.VolumetricPanel;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Simulation;
import com.jogamp.opengl.GL2;
import java.nio.FloatBuffer;

public class VolumetricShader extends ShaderTransducers{
    int centerCube, sizeCube;
    int raySteps;
    int isTimeDomain;
    int timestamp;
    int renderType;
    
    int colouring;
    int minPosColor, maxPosColor;
    int minNegColor, maxNegColor;
    int isoValue;
     
    public VolumetricShader(String vProgram, String fProgram) {
        super(vProgram, fProgram, ORDER_OPAQUE, 10);
    }
        
    @Override
    void changeGLStatus(GL2 gl, Renderer renderer, Simulation s, MeshEntity e) {
        renderer.enableBlend(gl, false);
        renderer.enableCullFace(gl, true);
        renderer.enableDepthTest(gl, true);
        renderer.enableTexture2D(gl, false);
        renderer.enableTexture3D(gl, false);
    }
    

    @Override
    void getUniforms(GL2 gl) {
        super.getUniforms(gl);
        centerCube = gl.glGetUniformLocation(shaderProgramID, "cubeCenter");
        sizeCube = gl.glGetUniformLocation(shaderProgramID, "cubeSize");
        raySteps = gl.glGetUniformLocation(shaderProgramID, "raySteps");
        isTimeDomain = gl.glGetUniformLocation(shaderProgramID, "isTimeDomain");
        timestamp = gl.glGetUniformLocation(shaderProgramID, "timestamp");
        renderType = gl.glGetUniformLocation(shaderProgramID, "renderType");
        isoValue = gl.glGetUniformLocation(shaderProgramID, "isoValue");
        
        minPosColor = gl.glGetUniformLocation(shaderProgramID, "minPosColor");
        maxPosColor = gl.glGetUniformLocation(shaderProgramID, "maxPosColor");
        colouring = gl.glGetUniformLocation(shaderProgramID, "colouring");
        
        minNegColor = gl.glGetUniformLocation(shaderProgramID, "minNegColor");
        maxNegColor = gl.glGetUniformLocation(shaderProgramID, "maxNegColor");
    }
    
    
    @Override
    void bindUniforms(GL2 gl, Scene scene, Renderer renderer, Simulation s, MeshEntity me, Matrix4f projectionViewModel, Matrix4f viewModel, Matrix4f model, FloatBuffer fb) {
       super.bindUniforms(gl, scene, renderer, s, me, projectionViewModel, viewModel, model, fb);
       
       final VolumetricPanel panel = renderer.getForm().volPanel;
       Vector3f cubeT = scene.getCubeHelper().getTransform().getTranslation();
       Vector3f cubeS = scene.getCubeHelper().getTransform().getScale();
       gl.glUniform3f(centerCube, cubeT.x, cubeT.y, cubeT.z);
       gl.glUniform3f(sizeCube, cubeS.x, cubeS.y, cubeS.z);
       gl.glUniform1f(raySteps, 1.0f / panel.getDensity() );
       
       gl.glUniform1i(renderType, panel.getRenderType() ); //1 MIPS, 2 ISO
       gl.glUniform1f(isoValue, panel.getIsoValue() );
       gl.glUniform1f(timestamp, System.currentTimeMillis() / 1000 * panel.getTimeScale() );
       gl.glUniform1i(isTimeDomain, panel.isTimeDomain() ? 1 : 0);
       
       final float minAmp = panel.getMinAmp();
       final float maxAmp = panel.getMaxAmp();
       gl.glUniform1f(minPosColor, minAmp);
       gl.glUniform1f(maxPosColor, maxAmp);
       gl.glUniform1f(minNegColor, -maxAmp);
       gl.glUniform1f(maxNegColor, -minAmp);
       gl.glUniform1i(colouring, 4); //fire/ice gradient
       
    }
  
}
