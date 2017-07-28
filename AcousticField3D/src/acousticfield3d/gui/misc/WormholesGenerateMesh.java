/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.gui.misc;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Quaternion;
import acousticfield3d.math.Vector3f;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.utils.Parse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amarz
 */
public class WormholesGenerateMesh extends javax.swing.JFrame {
    final MainForm mf;
    
    public WormholesGenerateMesh(MainForm mf) {
        this.mf = mf;
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        tubesInputFile = new javax.swing.JTextField();
        ropesSelectbutton = new javax.swing.JButton();
        generateMeshButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        radiousText = new javax.swing.JTextField();
        sidesText = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        topCheck = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        topRadiousText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        topNormalText = new javax.swing.JTextField();
        topPointToCheck = new javax.swing.JCheckBox();
        bottomPointToCheck = new javax.swing.JCheckBox();
        bottomNormalText = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        bottomRadiousText = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        bottomCheck = new javax.swing.JCheckBox();
        topSepText = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        bottomSepText = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Wormholes");

        jLabel3.setText("Input File:");

        ropesSelectbutton.setText("Select");
        ropesSelectbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ropesSelectbuttonActionPerformed(evt);
            }
        });

        generateMeshButton.setText("Generate Mesh");
        generateMeshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateMeshButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Radious:");

        radiousText.setText("0.1");

        sidesText.setText("16");

        jLabel6.setText("Sides:");

        topCheck.setText("top");

        jLabel7.setText("R:");

        topRadiousText.setText("0.5");

        jLabel4.setText("Normal:");

        topNormalText.setText("0 -1 0");

        topPointToCheck.setText("point to");

        bottomPointToCheck.setText("point to");

        bottomNormalText.setText("0 -1 0");

        jLabel8.setText("Normal:");

        bottomRadiousText.setText("0.5");

        jLabel9.setText("R:");

        bottomCheck.setText("bottom");

        topSepText.setText("0.5");

        jLabel10.setText("Sep:");

        bottomSepText.setText("0.5");

        jLabel11.setText("Sep:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tubesInputFile)
                        .addGap(18, 18, 18)
                        .addComponent(ropesSelectbutton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(radiousText, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel6))
                                    .addComponent(generateMeshButton))
                                .addGap(18, 18, 18)
                                .addComponent(sidesText, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bottomCheck)
                                    .addComponent(topCheck))
                                .addGap(51, 51, 51)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(topRadiousText))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bottomRadiousText, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(topSepText, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bottomSepText, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bottomNormalText, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(topNormalText, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bottomPointToCheck)
                                    .addComponent(topPointToCheck, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tubesInputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ropesSelectbutton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(radiousText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(sidesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(topCheck)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(bottomCheck))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(topSepText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(topRadiousText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(bottomSepText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(bottomRadiousText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(topNormalText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(bottomNormalText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(topPointToCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bottomPointToCheck)))
                .addGap(18, 18, 18)
                .addComponent(generateMeshButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ropesSelectbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ropesSelectbuttonActionPerformed
        String file = FileUtils.selectFile(this, "open", ".txt", null);
        if (file != null){
            tubesInputFile.setText( file );
        }
    }//GEN-LAST:event_ropesSelectbuttonActionPerformed

    
    public class Tube {
        public List<Vector3f> segments = new ArrayList<>();
        public List<Vector3f> normals = new ArrayList<>();
        public List<Float> radious = new ArrayList<>();
        public List<Boolean> calcNormal = new ArrayList<>();
    }
       
    private void generateMeshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMeshButtonActionPerformed
        final File inputFile = new File(tubesInputFile.getText());
        
        //open file & parse tubes
        try (BufferedReader br = new BufferedReader(new FileReader( inputFile ))) {
            final float radious = Parse.toFloat( radiousText.getText() );
            final int sides = Parse.toInt(sidesText.getText() );
            List<Vector3f> circle = new ArrayList<>(sides);
            for(int i = 0; i < sides; ++i){
                final float angle = i * M.TWO_PI / sides;
                circle.add( new Vector3f( M.cos(angle),0,  M.sin(angle)));
            }
            
            final boolean top = topCheck.isSelected();
            final int topNSegments = 1;
            final float topSeparation = Parse.toFloat( topSepText.getText() );
            final float topRadious = Parse.toFloat( topRadiousText.getText() );
            final Vector3f topNormal = new Vector3f( topNormalText.getText() );
            final boolean topPointTo = topPointToCheck.isSelected();
            
            final boolean bottom = bottomCheck.isSelected();
            final int bottomNSegments = 1;
            final float bottomSeparation = Parse.toFloat( bottomSepText.getText() );
            final float bottomRadious = Parse.toFloat( bottomRadiousText.getText() );
            final Vector3f bottomNormal = new Vector3f( bottomNormalText.getText() );
            final boolean bottomPointTo = bottomPointToCheck.isSelected();
            
            String line = br.readLine();
            final int nTubes = Parse.toInt(line);
            final List< Tube> tubes = new ArrayList<>(nTubes);
            
            for(int i = 0; i < nTubes; ++i){
                line = br.readLine();
                final int nSegments = Parse.toInt(line);
                final Tube tube = new Tube();
                
                tubes.add(tube);
                    
                for (int j = 0; j < nSegments; ++j){
                    final Vector3f v = new Vector3f( br.readLine() );
                    tube.segments.add(v);
                    tube.radious.add(radious);
                    tube.normals.add( new Vector3f() );
                    tube.calcNormal.add( true );
                }
                
                if (top) {
                    for(int j = 0; j < topNSegments; ++j){
                        
                        final Vector3f pos = tube.segments.get(0).add(0, topSeparation, 0);
                        final Vector3f norm = topNormal.clone();
                        tube.segments.add(0, pos );
                        tube.radious.add(0, M.lerp(radious, topRadious, (j+1) / (float)topNSegments ) );
                        tube.calcNormal.add(0, false);
                        if (topPointTo){
                            norm.subtractLocal( pos ).normalizeLocal();
                        }
                        tube.normals.add(0, norm );
                        
                    }
                }
               
                if (bottom) {
                    for(int j = 0; j < bottomNSegments; ++j){
                        
                        final Vector3f pos = tube.segments.get( tube.segments.size() - 1).add(0, -bottomSeparation, 0);
                        final Vector3f norm = bottomNormal.clone();
                        tube.segments.add( pos );
                        tube.radious.add( M.lerp(radious, bottomRadious, (j+1) / (float)bottomNSegments ) );
                        tube.calcNormal.add(false);
                        if (bottomPointTo){
                            norm.subtractLocal( pos ).normalizeLocal();
                        }
                        tube.normals.add( norm );
                    }
                }
            }
            
            //calc normals
            final Vector3f n1 = new Vector3f();
            final Vector3f n2 = new Vector3f();
            for (Tube t : tubes){
                final int nSegments = t.segments.size();
                for(int i = 0; i < nSegments; ++i){
                    if (t.calcNormal.get(i)){
                        final Vector3f norm = t.normals.get(i);
                        if (i == 0){ 
                            norm.set( t.segments.get(i+1) ).subtractLocal( t.segments.get(i)).normalizeLocal();
                        }else if (i == nSegments - 1){
                            norm.set( t.segments.get(i) ).subtractLocal( t.segments.get(i-1)).normalizeLocal();
                        }else{
                           n1.set( t.segments.get(i+1) ).subtractLocal( t.segments.get(i)).normalizeLocal();
                           n2.set( t.segments.get(i) ).subtractLocal( t.segments.get(i-1)).normalizeLocal();
                           norm.set(n1).addLocal(n2).divideLocal(2.0f).normalizeLocal();
                        }
                    }
                }
            }
            
            StringBuilder sb = new StringBuilder();
            
            final Quaternion q = new Quaternion();
            
            //generate tubes
            final int n = tubes.size();
            Vector3f v = new Vector3f();
            int globalVIndex = 1;
           
            for(int i = 0; i < n; ++i){
                final Tube t = tubes.get(i);
                final int nSeg = t.segments.size();
                sb.append("\n");
                for (int j = 0; j < nSeg; ++j){ //vertices
                    final Vector3f pos = t.segments.get(j);
                    final Vector3f norm = t.normals.get(j);
                    final float rad = t.radious.get(j);
                    
                    
                    q.lookAtY(norm, Vector3f.UNIT_Z);
                    
                    for(int k = 0; k < sides; ++k){
                        v.set( circle.get(k) );
                        v.multLocal(rad);
                        q.multLocal( v );
                        v.addLocal( pos );
                        sb.append("v " + v.x + " " + v.y + " " + v.z + "\n");
                    }  
                }
                
                sb.append("\n");
                sb.append("g tube" + i + "\n");
                for (int j = 0; j < nSeg; ++j){ //faces
                    final int currentFace = sides * j;
                    
                    if (j == 0 ){ // generate face
                        gFace(sb, currentFace + globalVIndex, sides);
                    }else if(j ==  nSeg-1){
                        gFaceInv(sb, currentFace + globalVIndex, sides);
                    }
                    
                            
                    if (j > 0){ //sold current ring with the previous ring
                        final int prevFace = sides * (j-1);
                        gSoldInv(sb, currentFace + globalVIndex, prevFace + globalVIndex, sides);
                    }
                }
                
                globalVIndex += nSeg * sides;
            }
            
            
            //write file
            FileUtils.writeBytesInFile(new File(inputFile.getParent(), FileUtils.getFileName(inputFile) + ".obj"), sb.toString());
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WormholesGenerateMesh.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WormholesGenerateMesh.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }//GEN-LAST:event_generateMeshButtonActionPerformed

    private void gFace(StringBuilder sb, int startV, int sides){
        sb.append("f ");
        for(int i = 0; i < sides; ++i){
            sb.append( (startV+i) + " ");
        }
        sb.append("\n");
    }
    
    private void gFaceInv(StringBuilder sb, int startV, int sides){
        sb.append("f ");
        for(int i = sides-1; i >= 0; --i){
            sb.append( (startV+i) + " ");
        }
        sb.append("\n");
    }
    
    private void gSold(StringBuilder sb, int faceA, int faceB, int sides){
        for(int i = 0; i < sides-1; ++i){
            final int a = faceA + i;
            final int b = faceB + i;
            sb.append("f " + b + " " + (b+1) + " " + (a+1) + " " + a + "\n");
        }
        final int a = faceA + sides - 1;
        final int b = faceB + sides - 1;
        sb.append("f " + b + " " + (faceB) + " " + (faceA) + " " + a + "\n");
        
    }
    
    private void gSoldInv(StringBuilder sb, int faceA, int faceB, int sides){
        for(int i = 0; i < sides-1; ++i){
            final int a = faceA + i;
            final int b = faceB + i;
            sb.append("f " + a + " " + (a+1) + " " + (b+1) + " " + b + "\n");
        }
        final int a = faceA + sides - 1;
        final int b = faceB + sides - 1;
        sb.append("f " + a + " " + (faceA) + " " + (faceB) + " " + b + "\n");
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox bottomCheck;
    private javax.swing.JTextField bottomNormalText;
    private javax.swing.JCheckBox bottomPointToCheck;
    private javax.swing.JTextField bottomRadiousText;
    private javax.swing.JTextField bottomSepText;
    private javax.swing.JButton generateMeshButton;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField radiousText;
    private javax.swing.JButton ropesSelectbutton;
    private javax.swing.JTextField sidesText;
    private javax.swing.JCheckBox topCheck;
    private javax.swing.JTextField topNormalText;
    private javax.swing.JCheckBox topPointToCheck;
    private javax.swing.JTextField topRadiousText;
    private javax.swing.JTextField topSepText;
    private javax.swing.JTextField tubesInputFile;
    // End of variables declaration//GEN-END:variables
}
