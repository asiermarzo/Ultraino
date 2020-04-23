package acousticfield3d.renderer;

import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.FieldSource;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.utils.Color;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL2;

/**
 *
 * @author Asier
 */
public class SliceRTQuickAmpShader extends SliceRTShader{   
    int uniK;
    int uniApperture;
    int amplitudeConst;
            
    public SliceRTQuickAmpShader(String vProgram, String fProgram) {
        super(vProgram, fProgram, FieldSource.sourceAmp);
    }

    @Override
    void getUniforms(GL2 gl) {
        super.getUniforms(gl);
 
        uniK = gl.glGetUniformLocation(shaderProgramID, "k");
        uniApperture = gl.glGetUniformLocation(shaderProgramID, "apperture");
        amplitudeConst = gl.glGetUniformLocation(shaderProgramID, "amplitudeConstant");
    }
    
    @Override
    void bindUniforms(GL2 gl, Scene scene, Renderer renderer,Simulation s, MeshEntity me, Matrix4f projectionViewModel, Matrix4f viewModel, Matrix4f model, FloatBuffer fb) {
       //Shader
        fb.rewind();
        gl.glUniformMatrix4fv(mvpMatrixHandle, 1, false, projectionViewModel.fillFloatBuffer(fb, true));
        gl.glUniformMatrix4fv(mvMatrixHandle, 1, false, viewModel.fillFloatBuffer(fb, true));
        gl.glUniformMatrix4fv(mMatrixHandle, 1, false, model.fillFloatBuffer(fb, true));
        
        Vector3f lightPos = scene.getLight().getTransform().getTranslation();
        Vector3f eyePos = scene.getCamera().getTransform().getTranslation();
        
        gl.glUniform4f(lightPosHandle, lightPos.x, lightPos.y, lightPos.z, 1);
        gl.glUniform4f(eyePosHandle, eyePos.x, eyePos.y, eyePos.z, 1);

        float r = Color.red(me.getColor()) / 255.0f;
        float g = Color.green(me.getColor()) / 255.0f;
        float b = Color.blue(me.getColor()) / 255.0f;
        float a = Color.alpha(me.getColor()) / 255.0f;
        gl.glUniform4f(colorHandle, r, g, b, a);

        gl.glUniform1f(ambient, me.getMaterial().getAmbient());
        gl.glUniform1f(diffuse, me.getMaterial().getDiffuse());
        gl.glUniform1f(specular, me.getMaterial().getSpecular());
        gl.glUniform1f(shininess, me.getMaterial().getShininess());
        
       //Transducers Shader
        gl.glUniform3fv(transPosition, renderer.nTransducers, renderer.positions);
        gl.glUniform1fv(transSpecs, renderer.nTransducers, renderer.phase);
        
       //Slice RT   
       gl.glUniform1f(minPosColor, renderer.getForm().rtSlicePanel.getAmpColorMin());
       gl.glUniform1f(maxPosColor, renderer.getForm().rtSlicePanel.getAmpColorMax());
       
       gl.glUniform1f(minNegColor, -renderer.getForm().rtSlicePanel.getAmpColorMax());
       gl.glUniform1f(maxNegColor, -renderer.getForm().rtSlicePanel.getAmpColorMin());
       
       gl.glUniform1i(colouring, 4); //ice gradient
       
       if (renderer.nTransducers > 0){
            gl.glUniform1f(uniK, renderer.specs.get(0) );
            gl.glUniform1f(uniApperture, renderer.specs.get(3));
            gl.glUniform1f(amplitudeConst, renderer.specs.get(1));
            
        }
    }
 
}
