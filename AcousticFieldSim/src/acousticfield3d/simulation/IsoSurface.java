/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.simulation;

import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.shapes.Mesh;
import acousticfield3d.utils.Color;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Asier
 */
public class IsoSurface {
    float isoValue;
    float alphaValue;
    boolean visible;
    ArrayList<MeshEntity> meshes;

    public IsoSurface() {
        meshes = new ArrayList<>();
        visible = true;
        alphaValue = 1.0f;
    }
    
    
    public void setVisible(boolean visible){
        for(MeshEntity me : meshes){
            me.setVisible(visible);
        }
    }
    
    public void setAlpha(float alpha){
        for(MeshEntity me : meshes){
            int c = me.getColor();
            int i = (int)(alpha*255);
            me.setColor(  Color.changeAlpha(c, i) );
        }
    }
    
    public void removeFromScene(Scene s){
        s.getEntities().removeAll( meshes );
    }
    
    @Override
    public String toString() {
        int iValue = (int)(isoValue);
        int iAlpha = (int)(alphaValue*100);
        return iValue + " " + iAlpha + " " + (visible?"":"[]");
    }

    public void setIsoValue(float isoValue) {
        this.isoValue = isoValue;
    }

    public void setAlphaValue(float alphaValue) {
        this.alphaValue = alphaValue;
    }

    public ArrayList<MeshEntity> getMeshes() {
        return meshes;
    }
    
    public void saveToFile(String file){
        ArrayList<Mesh> m = new ArrayList<>( meshes.size() );
        for(MeshEntity me : meshes){
            m.add( me.customMesh );
        }
        
        File f = new File(file);
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(f, false);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            Mesh.writeMeshesToObj(m, dos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IsoSurface.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(IsoSurface.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if (dos != null) { try{ dos.close(); }catch(Exception e){} } 
        }
        
    }
}
