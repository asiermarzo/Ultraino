package acousticfield3d.renderer;

import acousticfield3d.math.Matrix4f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Simulation;
import java.nio.FloatBuffer;
import java.util.HashMap;

import com.jogamp.opengl.GL2;

/**
 *
 * @author Asier
 */
public class ShaderTransducers extends Shader{
    private int numberOfTransducers;
    
    int transPosition;
    int transNormal;
    int transSpecs;
    
    public ShaderTransducers(String vProgram, String fProgram, int renderingOrder, int numberOfTransducers) {
        super(vProgram, fProgram, renderingOrder);
        this.numberOfTransducers = numberOfTransducers;
    }
    
    public void updateTransducersNumber(int n, GL2 gl, HashMap<String,String> templates){
        if (n != numberOfTransducers){
            numberOfTransducers = n;
            reload(gl,templates);
        }
    }
    
    public int getNumberOfTransducers() {
        return numberOfTransducers;
    }

    public void setNumberOfTransducers(int numberOfTransducers) {
        this.numberOfTransducers = numberOfTransducers;
    }

    @Override
    void getUniforms(GL2 gl) {
        super.getUniforms(gl);
        
        transPosition = gl.glGetUniformLocation(shaderProgramID, "tPos");
        transNormal = gl.glGetUniformLocation(shaderProgramID, "tNorm");
        transSpecs = gl.glGetUniformLocation(shaderProgramID, "tSpecs");
    }

    @Override
    protected String preProcessFragment(String sourceCode, HashMap<String,String> templates) {
        
        return super.preProcessFragment( 
                sourceCode.replaceAll("_N_TRANS_", getNumberOfTransducers() + ""), templates);
    }

    @Override
    void bindUniforms(GL2 gl, Scene scene, Renderer renderer, Simulation s, MeshEntity me, Matrix4f projectionViewModel, Matrix4f viewModel, Matrix4f model, FloatBuffer fb) {
        super.bindUniforms(gl, scene, renderer, s, me, projectionViewModel, viewModel, model, fb);
        
        gl.glUniform3fv(transPosition, renderer.nTransducers, renderer.positions);
        gl.glUniform3fv(transNormal, renderer.nTransducers, renderer.normals);
        gl.glUniform4fv(transSpecs, renderer.nTransducers, renderer.specs);
    }
    
    
}
