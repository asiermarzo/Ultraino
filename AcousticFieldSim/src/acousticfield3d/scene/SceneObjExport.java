package acousticfield3d.scene;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.Matrix4f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Material;
import acousticfield3d.shapes.Mesh;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.utils.Color;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Asier
 */
public class SceneObjExport {
    MainForm mf;
    
    public SceneObjExport(MainForm mf) {
        this.mf = mf;
    }
    
    public void export(final boolean exportMaterials) {
        String file = FileUtils.selectNonExistingFile(mf, ".obj");
        if (file != null) {
            final ArrayList<MeshEntity> objs = mf.scene.getEntities();
            StringBuilder sb = new StringBuilder();
            Matrix4f model = new Matrix4f();
            int index = 0;
            int totalV = 1;
            int[] indices = new int[objs.size()];

            final File objFile = new File(file);
            final String mtlFileStr = FileUtils.getFileName(objFile) + ".mtl";
            final File mtlFile = new File(objFile.getParentFile(), mtlFileStr);
            
            if(exportMaterials){
                sb.append("mtllib  " + mtlFileStr + "\n");
            }
            
            Vector3f v = new Vector3f();
            Vector3f transV = new Vector3f();
            for (MeshEntity me : objs) {
                if (! me.isVisible()) { continue; }
                
                Mesh mesh;
                if (me.getMesh().equals(Resources.MESH_CUSTOM)) {
                    mesh = me.customMesh;
                } else {
                    mesh = Resources.get().getMesh(me.getMesh());
                }
                if (mesh == null) {
                    continue;
                }

                me.getTransform().copyTo(model);

                final FloatBuffer positions = mesh.getPosition();
                final FloatBuffer normals = mesh.getNormal();
                final int vCount = mesh.getVertCount();
                final int coordsCount = vCount * 3;
                for (int i = 0; i < coordsCount; i += 3) {
                    v.set(positions.get(i), positions.get(i + 1), positions.get(i + 2));
                    model.multiplyPoint(v, transV);
                    sb.append("v " + transV.x + " " + transV.y + " " + transV.z + "\n");

                    v.set(normals.get(i), normals.get(i + 1), normals.get(i + 2));
                    model.multiplyVector(v, transV);
                    sb.append("vn " + transV.x + " " + transV.y + " " + transV.z + "\n");

                }
                sb.append("\n");
                indices[index] = totalV;
                totalV += vCount;
                index++;
            }
            index = 0;
            for (MeshEntity me : objs) {
                if (! me.isVisible()) { continue; }
                
                Mesh mesh;
                if (me.getMesh().equals(Resources.MESH_CUSTOM)) {
                    mesh = me.customMesh;
                } else {
                    mesh = Resources.get().getMesh(me.getMesh());
                }
                if (mesh == null) {
                    continue;
                }

                sb.append("\ng " + index + "\n\n");
                if(exportMaterials){
                    sb.append("usemtl  " + "m" + index + "\n");
                }
                final ShortBuffer trians = mesh.getIndices();
                final int iCount = mesh.getTrianCount() * 3;
                final int offsetI = indices[index];
                for (int i = 0; i < iCount; i += 3) {
                    int i0 = trians.get(i + 0) + offsetI;
                    int i1 = trians.get(i + 1) + offsetI;
                    int i2 = trians.get(i + 2) + offsetI;
                    sb.append("f " + i0 + "//" + i0 + " " + i1 + "//" + i1 + " " + i2 + "//" + i2 + "\n");
                }
                index++;
            }
            sb.append("\n");

            try {
                FileUtils.writeBytesInFile(objFile, sb.toString().getBytes());
            } catch (IOException ex) {
                Logger.getLogger(SceneObjExport.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create the mtl file
            if (exportMaterials){
                sb = new StringBuilder();
                index = 0;
                for (MeshEntity me : objs) {
                    if (! me.isVisible()) { continue; }

                    
                   
                    
                    sb.append("newmtl  " + "m" + index + "\n");
                    final Material mtl = me.getMaterial();
                    final int color = me.getColor();
                    sb.append("Ka  " + Color.RGBToStringF(color) + "\n");
                    sb.append("Kd  " + Color.RGBToStringF(color) + "\n");
                   
                    sb.append("\n");
                    index++;
                }
                sb.append("\n");

                try {
                    FileUtils.writeBytesInFile( mtlFile, sb.toString().getBytes());
                } catch (IOException ex) {
                    Logger.getLogger(SceneObjExport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static String triplicateToStr(float f){
        return f + " " + f + " " + f;
    }
    
}
