package acousticfield3d.renderer;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.scene.Scene;
import acousticfield3d.shapes.Mesh;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.utils.BufferUtils;
import acousticfield3d.utils.Color;
import acousticfield3d.utils.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;


/**
 *
 * @author Asier
 */
public class Shader {
    private final String vProgram, fProgram;
   
    int shaderProgramID;
    int fragmentShader, vertexShader;
    
    int vertexHandle;
    int normalHandle;
    int textureCoordHandle;
    
    int lightPosHandle;
    int eyePosHandle;
    
    int mvpMatrixHandle;
    int mvMatrixHandle;
    int mMatrixHandle;
    
    int colorHandle;
    int texDiffuse;
    int ambient, diffuse, specular, shininess;
    
    private int renderingOrder;
    public static int ORDER_BACKGROUND = 0;
    public static int ORDER_MASK = 1;
    public static int ORDER_OPAQUE = 3;
    public static int ORDER_TRANSLUCENT = 4;
    public static int ORDER_GUI = 100;
    public Shader(String vProgram, String fProgram, int renderingOrder){
        this.vProgram = vProgram;
        this.fProgram = fProgram;
        this.renderingOrder = renderingOrder;
    }
    
   
    public int getRenderingOrder(MeshEntity me){
        if (Color.alpha( me.getColor() ) < 255){
            return ORDER_TRANSLUCENT;
        }
        return renderingOrder;
    }
    
    public void reload(GL2 gl, HashMap<String,String> templates){
        unloadShader(gl);
        init(gl, templates);
    }
    
    void bindUniforms(GL2 gl, Scene scene,Renderer renderer, Simulation s, MeshEntity me, 
            Matrix4f projectionViewModel, Matrix4f viewModel, Matrix4f model,
            FloatBuffer fb) {
        
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
    }

    void bindAttribs(GL2 gl, Simulation s, MeshEntity me) {
        Mesh mesh;
        if ( me.getMesh().equals(Resources.MESH_CUSTOM) ){
            mesh = me.customMesh;
        }else{
            mesh = Resources.get().getMesh( me.getMesh() ); 
        }
        if (mesh == null) { return; }
        
        gl.glVertexAttribPointer(vertexHandle, 3, GL.GL_FLOAT, false, 12, mesh.getPosition());
        gl.glVertexAttribPointer(textureCoordHandle, 2, GL2.GL_FLOAT, false, 8, mesh.getTexture());
        gl.glVertexAttribPointer(normalHandle, 3, GL2.GL_FLOAT, false, 12, mesh.getNormal());

        gl.glEnableVertexAttribArray(vertexHandle);
        gl.glEnableVertexAttribArray(normalHandle);
        gl.glEnableVertexAttribArray(textureCoordHandle);
    }
    
    void render(GL2 gl, Simulation s, MeshEntity me) {
        
        Mesh mesh;
        if ( me.getMesh().equals(Resources.MESH_CUSTOM) ){
            mesh = me.customMesh;
        }else{
            mesh = Resources.get().getMesh( me.getMesh() ); 
        }
        if (mesh != null){
            gl.glDrawElements(GL2.GL_TRIANGLES, mesh.getTrianCount() * 3, GL2.GL_UNSIGNED_SHORT, mesh.getIndices());
        }
    }

    void unbindAttribs(GL2 gl, Simulation s, MeshEntity me) {
        gl.glDisableVertexAttribArray(vertexHandle);
        gl.glDisableVertexAttribArray(normalHandle);
        gl.glDisableVertexAttribArray(textureCoordHandle);
    }
    
    void changeGLStatus(GL2 gl, Renderer r, Simulation s, MeshEntity e){
        r.enableBlend(gl, Color.alpha( e.getColor() ) < 255);
        
        r.enableCullFace(gl, ! e.isDoubledSided() );
        
        r.enableDepthTest(gl, true);
        r.enableTexture2D(gl, false);
        r.enableTexture3D(gl, false);
        r.enableWriteColor(gl, renderingOrder != ORDER_MASK);
    }
    
    public Shader init(GL2 gl, HashMap<String,String> templates){
	shaderProgramID = createProgramFromBuffer(gl, vProgram, fProgram, templates);
	getUniforms( gl );
        
        return this;
    }
    
    void getUniforms(GL2 gl){
        vertexHandle = gl.glGetAttribLocation(shaderProgramID, "vertexPosition");
	normalHandle = gl.glGetAttribLocation(shaderProgramID, "vertexNormal");
	textureCoordHandle = gl.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
	
        lightPosHandle = gl.glGetUniformLocation(shaderProgramID, "lightPos");
	eyePosHandle = gl.glGetUniformLocation(shaderProgramID, "eyePos");
        
        mvpMatrixHandle = gl.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
	mvMatrixHandle = gl.glGetUniformLocation(shaderProgramID, "modelViewMatrix");
        mMatrixHandle = gl.glGetUniformLocation(shaderProgramID, "modelMatrix");
        
	colorHandle = gl.glGetUniformLocation(shaderProgramID, "colorMod");
	ambient = gl.glGetUniformLocation(shaderProgramID, "ambient");
	diffuse = gl.glGetUniformLocation(shaderProgramID, "diffuse");
	specular = gl.glGetUniformLocation(shaderProgramID, "specular");
	shininess = gl.glGetUniformLocation(shaderProgramID, "shininess");
        texDiffuse = gl.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }

    public void unloadShader(GL2 gl) {
        //deatach shader just in case that the current one is attached
        gl.glUseProgram(0);
        
        gl.glDeleteShader(vertexShader); vertexShader = 0;
        gl.glDeleteShader(fragmentShader); fragmentShader = 0;
        gl.glDeleteProgram(shaderProgramID); shaderProgramID = 0;
    }

    // Create a shader program
    int createProgramFromBuffer(GL2 gl, String vProgram, String fProgram, HashMap<String,String> templates){
        int program = 0;
        vertexShader = initShader(gl, GL2.GL_VERTEX_SHADER, preProcessVertex( getSourceCode(vProgram), templates ));
        fragmentShader = initShader(gl, GL2.GL_FRAGMENT_SHADER, preProcessFragment( getSourceCode(fProgram), templates ));

        if (vertexShader != 0 && fragmentShader != 0) {
            program = gl.glCreateProgram();

            if (program != 0) {
                gl.glAttachShader(program, vertexShader);                
                gl.glAttachShader(program, fragmentShader);
                gl.glLinkProgram(program);
                IntBuffer linkStatus = BufferUtils.createIntBuffer(1);
                gl.glGetProgramiv(program, GL2.GL_LINK_STATUS, linkStatus);

                if (linkStatus.get(0) != GL2.GL_TRUE) {
                    IntBuffer infoLen = BufferUtils.createIntBuffer(1);
                    gl.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, infoLen);
                    int length = infoLen.get();
                    if (length != 0) {
                        ByteBuffer buf = BufferUtils.createByteBuffer(length);
                        infoLen.flip();
                        gl.glGetProgramInfoLog(program, length, infoLen, buf);
                        byte[] b = new byte[length];
                        buf.get(b);
                        System.err.println("Could not link program: " + new String(b));
                    }
                }
            }
        }

        return program;
    }

    // Initialise a shader
    int initShader(GL2 gl, int nShaderType, String source) {
        int shader = gl.glCreateShader(nShaderType);

        if (shader != 0) {
            String[] sources = new String[]{ source };
            gl.glShaderSource(shader, 1, sources, null);
            gl.glCompileShader(shader);
            IntBuffer compiled = BufferUtils.createIntBuffer(1);
            gl.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, compiled);

            if (compiled.get() == 0) {
                IntBuffer infoLen = BufferUtils.createIntBuffer(1);
                gl.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, infoLen);
                int length = infoLen.get();
                if (length > 0) {
                    ByteBuffer buf = BufferUtils.createByteBuffer(length);
                    infoLen.flip();
                    gl.glGetShaderInfoLog(shader, length, infoLen, buf);
                    byte[] b = new byte[infoLen.get()];
                    buf.get(b);
                    System.out.println("Prgram : " + fProgram);
                    System.out.println(source);
                    System.err.println("Error compiling shader " + vProgram + " " + fProgram + " -> " + new String(b));
                }
            }
        }

        return shader;
    }

    protected String preProcessVertex(String sourceCode, HashMap<String,String> templates) {
        return processMacros(sourceCode, templates);
    }

    protected String preProcessFragment(String sourceCode, HashMap<String,String> templates) {
        return processMacros(sourceCode, templates);
    }
    
    
    
    public static String processMacros(String input, HashMap<String,String> templates){
        ArrayList<Integer> positions = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        
        //insert templates
        gatherMacros(input, Resources.TEMPLATE_TAG, positions, names);
        if (! positions.isEmpty()){
            input = replaceTemplates(input, positions, names, templates);
        }
        
        //insert includes
        gatherMacros(input, Resources.INCLUDE_TAG, positions, names);
        if (! positions.isEmpty()){
            input = includeFiles(input, positions, names);
        }
        
        return input;
    }
    
    public static String replaceTemplates(String input, ArrayList<Integer> positions, ArrayList<String> names, HashMap<String, String> templates) {
        StringBuilder sb = new StringBuilder();
        sb.append( input );
        for(int i = positions.size()-1; i >= 0; --i){
            int pos = positions.get(i);
            String name = names.get(i);
            if (templates.containsKey( name )){
                sb.insert(pos, templates.get(name));
            }
        }
        return sb.toString();
    }

    public static String includeFiles(String input, ArrayList<Integer> positions, ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        sb.append( input );
        for(int i = positions.size()-1; i >= 0; --i){
            int pos = positions.get(i);
            String name = names.get(i);
            String content = getSourceCode(name);
            if (content != null){
                sb.insert(pos, content);
            }
        }
        return sb.toString();
    }
    
    public static void gatherMacros(String input, String macroStart, ArrayList<Integer> positions, ArrayList<String> names){
        positions.clear();
        names.clear();
        int si = 0;
        final int macroL = macroStart.length();
        while ( (si = input.indexOf(macroStart, si)) != -1){
            int lastIndex = input.indexOf("\n", si+1);
            String name = input.substring(si + macroL, lastIndex).trim();
            positions.add( si );
            names.add( name );
            si = lastIndex + 1;
        }
    }

    public static String getSourceCode(String fileName){
        InputStream is = Shader.class.getResourceAsStream("/acousticfield3d/resources/" + fileName);
        try {
            return new String(FileUtils.getBytesFromInputStream(is));
        } catch (IOException ex) {
            Logger.getLogger(Shader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
