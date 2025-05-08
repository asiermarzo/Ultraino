package acousticfield3d.renderer;

import acousticfield3d.gui.panels.VolumetricPanel;
import acousticfield3d.math.M;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Simulation;
import com.jogamp.opengl.GL2;
import java.nio.FloatBuffer;
import javax.swing.Timer;

public class VolumetricShader extends ShaderTransducers{
    int vpMatrixHandle;
        
    int maxCube, minCube;
    int rayStep;
    int isTimeDomain;
    int timestamp;
    int renderType;
    int outlineCutSize;
    
    int colouring;
    int minPosColor, maxPosColor;
    int minNegColor, maxNegColor;
    int isoValue;
         
    final long milliStart;
    public VolumetricShader(String vProgram, String fProgram) {
        super(vProgram, fProgram, ORDER_OPAQUE, 10);
        milliStart = System.currentTimeMillis();
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
        vpMatrixHandle = gl.glGetUniformLocation(shaderProgramID, "projectionViewMatrix");
        
        maxCube = gl.glGetUniformLocation(shaderProgramID, "maxCube");
        minCube = gl.glGetUniformLocation(shaderProgramID, "minCube");
        rayStep = gl.glGetUniformLocation(shaderProgramID, "rayStep");
        isTimeDomain = gl.glGetUniformLocation(shaderProgramID, "isTimeDomain");
        timestamp = gl.glGetUniformLocation(shaderProgramID, "timestamp");
        renderType = gl.glGetUniformLocation(shaderProgramID, "renderType");
        isoValue = gl.glGetUniformLocation(shaderProgramID, "isoValue");
        outlineCutSize = gl.glGetUniformLocation(shaderProgramID, "outlineCutSize");
        
        minPosColor = gl.glGetUniformLocation(shaderProgramID, "minPosColor");
        maxPosColor = gl.glGetUniformLocation(shaderProgramID, "maxPosColor");
        colouring = gl.glGetUniformLocation(shaderProgramID, "colouring");
        
        minNegColor = gl.glGetUniformLocation(shaderProgramID, "minNegColor");
        maxNegColor = gl.glGetUniformLocation(shaderProgramID, "maxNegColor");
    }
    
    
    @Override
    void bindUniforms(GL2 gl, Scene scene,Renderer renderer, Simulation s, MeshEntity me, 
            Matrix4f projectionViewModel, Matrix4f projectionView, Matrix4f viewModel, Matrix4f model, FloatBuffer fb) {
        super.bindUniforms(gl, scene, renderer, s, me, projectionViewModel, projectionView, viewModel, model, fb);
       
       gl.glUniformMatrix4fv(vpMatrixHandle, 1, false, projectionView.fillFloatBuffer(fb, true));
        
       final VolumetricPanel panel = renderer.getForm().volPanel;
       Vector3f cubePos = scene.getCubeHelper().getTransform().getTranslation();
       Vector3f cubeS = scene.getCubeHelper().getTransform().getScale();
      
       gl.glUniform3f(maxCube, cubePos.x + M.abs(cubeS.x) / 2f, cubePos.y + M.abs(cubeS.y) / 2f, cubePos.z + M.abs(cubeS.z) / 2f);
       gl.glUniform3f(minCube, cubePos.x - M.abs(cubeS.x) / 2f, cubePos.y - M.abs(cubeS.y) / 2f, cubePos.z - M.abs(cubeS.z) / 2f);
       gl.glUniform1f(rayStep, cubeS.length() / panel.getDensity() );
       
       gl.glUniform1i(renderType, panel.getRenderType() ); //1 MIPS, 2 ISO
       gl.glUniform1f(isoValue, panel.getIsoValue() );
       final float tVal = (System.currentTimeMillis()-milliStart) / 1000.0f * panel.getTimeScale();
       gl.glUniform1f(timestamp, tVal );
       gl.glUniform1i(isTimeDomain, panel.isTimeDomain() ? 1 : 0);
       
       final float minAmp = panel.getMinAmp();
       final float maxAmp = panel.getMaxAmp();
       gl.glUniform1f(minPosColor, minAmp);
       gl.glUniform1f(maxPosColor, maxAmp);
       gl.glUniform1f(minNegColor, -maxAmp);
       gl.glUniform1f(maxNegColor, -minAmp);
       gl.glUniform1i(colouring, 4); //fire/ice gradient
       gl.glUniform1f(outlineCutSize, panel.getOutlineCutSize() );
       
    }
  
}
