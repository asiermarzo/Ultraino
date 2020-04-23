package acousticfield3d.shapes;

import acousticfield3d.math.BoundingBox;
import acousticfield3d.math.BoundingSphere;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.utils.BufferUtils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Asier
 */
public class Mesh {
    private BoundingSphere bSphere;
    private BoundingBox bBox;
    
    private FloatBuffer position;
    private FloatBuffer texture;
    private FloatBuffer normal;
    private int vertCount;
    
    private ShortBuffer indices;
    private int trianCount;

    public Mesh() {
        bSphere = new BoundingSphere();
        bBox = new BoundingBox();
    }

    public Mesh(final int vertices,final int tris){
        createVertsTris(vertices, tris);
    }
    
    private void createVertsTris(final int vertices, final int tris){
        vertCount = vertices;
        position = BufferUtils.createFloatBuffer( vertices * 3);
        texture = BufferUtils.createFloatBuffer( vertices * 2);
        normal = BufferUtils.createFloatBuffer( vertices * 3);
        
        trianCount = tris;
        indices = BufferUtils.createShortBuffer(tris * 3);
    }
    
    public void setVerticesAndTris(int vertices, int tris){
        vertCount = vertices;
        trianCount = tris;
    }
    
    public FloatBuffer getPosition() {
        return position;
    }

    public void setPosition(FloatBuffer position) {
        this.position = position;
    }

    public FloatBuffer getTexture() {
        return texture;
    }

    public void setTexture(FloatBuffer texture) {
        this.texture = texture;
    }

    public FloatBuffer getNormal() {
        return normal;
    }

    public void setNormal(FloatBuffer normal) {
        this.normal = normal;
    }

    public int getVertCount() {
        return vertCount;
    }

    public void setVertCount(int vertCount) {
        this.vertCount = vertCount;
    }

    public ShortBuffer getIndices() {
        return indices;
    }

    public void setIndices(ShortBuffer indices) {
        this.indices = indices;
    }

    public int getTrianCount() {
        return trianCount;
    }

    public void setTrianCount(int trianCount) {
        this.trianCount = trianCount;
    }

    public BoundingSphere getbSphere() {
        return bSphere;
    }

    public void setbSphere(BoundingSphere bSphere) {
        this.bSphere = bSphere;
    }

    public BoundingBox getbBox() {
        return bBox;
    }

    public void setbBox(BoundingBox bBox) {
        this.bBox = bBox;
    }
    
    void updateBound(){
        rewindBuffers();
        bSphere.computeFromPoints(position);
        rewindBuffers();
        bBox.computeFromPoints(position);
        rewindBuffers();
    }
    
    public void updateCounts(){
        vertCount = position.limit() / 3;
        trianCount = indices.limit() / 3;
    }
    
    public void rewindBuffers(){
        if (position != null ) { position.rewind(); }
        if (texture != null ) { texture.rewind(); }
        if (normal != null ) { normal.rewind(); }
        if (indices != null ) { indices.rewind(); }
    }
    
    
    public static void writeMeshesToObj(List<Mesh> meshes, DataOutputStream os) throws IOException{
        int[] nVertices = new int[ meshes.size() ];
        
        int meshIndex = 0;        
        //write vertices information
        for(Mesh m : meshes){
            final int nv = m.getVertCount();
            
            FloatBuffer positions = m.position;
            for(int i = 0; i < nv; ++i){
                final int ti = i*3;
                os.writeBytes("v " + positions.get(ti+0) + " " + positions.get(ti+1) + " " + positions.get(ti+2) + "\n");
            }
            os.writeUTF("\n");
            FloatBuffer normals = m.normal;
            for(int i = 0; i < nv; ++i){
                final int ti = i*3;
                os.writeBytes("vn " + normals.get(ti+0) + " " + normals.get(ti+1) + " " + normals.get(ti+2) + "\n");
            }
            os.writeUTF("\n");
            FloatBuffer texture = m.texture;
            for(int i = 0; i < nv; ++i){
                final int ti = i*2;
                os.writeBytes("vt " + texture.get(ti+0) + " " + texture.get(ti+1) +  "\n");
            }
            os.writeUTF("\n");
            
            nVertices[meshIndex] = nv;
            meshIndex++;
        }
        
        
        //write indices
        int indexOffset = 1;
        meshIndex = 0;
        for(Mesh m : meshes){
            final int nt = m.getTrianCount();
            ShortBuffer indices = m.indices;
            for(int i = 0; i < nt; ++i){
                final int ti = i*3;
                int i0 = indices.get(ti + 0) + indexOffset;
                int i1 = indices.get(ti + 1) + indexOffset;
                int i2 = indices.get(ti + 2) + indexOffset;
                os.writeBytes("f " + 
                        i0+"/"+i0+"/"+i0 + " " +
                        i1+"/"+i1+"/"+i1 + " " +
                        i2+"/"+i2+"/"+i2 + "\n");
            }

            indexOffset += nVertices[meshIndex];
            meshIndex++;
        }
    }
    
    int currentIndex = 0;
    public void parse(String content){
        final ArrayList<Vector3f> vv = new ArrayList<>();
        final ArrayList<Vector2f> vt = new ArrayList<>();
        final ArrayList<Vector3f> vn = new ArrayList<>();
        // vx_vy_vz_vu_vv_nx_ny_nz -> indexFinalVertex indexV indexT indexN
        final HashMap<String,int[]> finalVertices = new HashMap<>();
        final ArrayList<int[]> tempTriangles = new ArrayList<>();

        currentIndex = 0;
        int firstIndex = 0;
        int lastIndex = content.indexOf("\n");
        while (lastIndex != -1){
            String line = content.substring(firstIndex,lastIndex).trim().replaceAll("\\s+", " ");
            String[] splitted = line.split(" ");
            if (splitted[0].toLowerCase().equals("v")){
                Vector3f v = new Vector3f(splitted[1],splitted[2],splitted[3]);
                vv.add( v );
            }else if (splitted[0].toLowerCase().equals("vt")){
                vt.add( new Vector2f( splitted[1], splitted[2] ) );
            }else if (splitted[0].toLowerCase().equals("vn")){
                vn.add( new Vector3f( splitted[1], splitted[2], splitted[3]));
            }else if (splitted[0].toLowerCase().equals("f")){
                int nVertices = splitted.length - 1;
                int nTriangles = nVertices - 2;
                for(int i = 0; i < nTriangles; ++i){
                    String[] v1 = splitted[1].split("/");
                    String[] v2 = splitted[i+2].split("/");
                    String[] v3 = splitted[i+3].split("/");

                    int v1i  = createIndex(v1, finalVertices);
                    int v2i  = createIndex(v2, finalVertices);
                    int v3i  = createIndex(v3, finalVertices);

                    tempTriangles.add(new int[]{v1i,v2i,v3i});
                } 
            }
            firstIndex = lastIndex + 1;
            lastIndex = content.indexOf("\n",firstIndex);
        }

       final int nVertex = finalVertices.size();
       final int nTriangles = tempTriangles.size();       
       createVertsTris(nVertex, nTriangles);
       for(int i = 0; i < nTriangles; i++){
           final int[] tr = tempTriangles.get(i);
           indices.put(i*3 + 0, (short) tr[0]);
           indices.put(i*3 + 1, (short) tr[1]);
           indices.put(i*3 + 2, (short) tr[2]);
        }
 
       for(String key : finalVertices.keySet()){
           final int[] tempVs = finalVertices.get(key);
           final int index = tempVs[0];
           final Vector3f pos = vv.get(tempVs[1]);
          
           final Vector3f nor = vn.get(tempVs[3]);
           
           position.put(index*3 + 0 , pos.x);
           position.put(index*3 + 1 , pos.y);
           position.put(index*3 + 2 , pos.z);
           
           normal.put(index*3 + 0 , nor.x);
           normal.put(index*3 + 1 , nor.y);
           normal.put(index*3 + 2 , nor.z);
           
           if (tempVs[2] != -1) {
               final Vector2f tex = vt.get(tempVs[2]);
               texture.put(index * 2 + 0, tex.x);
               texture.put(index * 2 + 1, tex.y);
           } else {
               texture.put(index * 2 + 0, 0);
               texture.put(index * 2 + 1, 0);
           }
       }
       updateBound();
    }

    private int createIndex(String[] v1, HashMap<String, int[]> finalVertices) throws NumberFormatException {
        int v1i;
        int v1v = Integer.parseInt(v1[0]) - 1;
        int v1t = -1;
        if (! v1[1].isEmpty()){
            v1t = Integer.parseInt(v1[1]) - 1;
        }
         
        int v1n = Integer.parseInt(v1[2]) - 1;
        String id1 = v1v + "_" + v1t + "_" + v1n;
        if (finalVertices.containsKey(id1)){
            v1i = finalVertices.get(id1)[0];
        }else{
            v1i = currentIndex; currentIndex++;
            finalVertices.put(id1, new int[]{v1i, v1v, v1t, v1n});
        }
        return v1i;
    }
}
