/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.gui.misc;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.Scene;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.utils.Parse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amarz
 */
public class WormholesGenerateTubes extends javax.swing.JFrame {
    final MainForm mf;

    
    
    
    public class Tube{
        public final Vector3f top = new Vector3f();
        public final Vector3f bottomTarget = new Vector3f();
        public final Vector3f bottomStart = new Vector3f();
        
        public Tube() {
        }
        
        public Tube(Vector3f top, Vector3f bottomTarget, Vector3f bottomStart) {
            this.top.set(top);
            this.bottomTarget.set(bottomTarget);
            this.bottomStart.set(bottomStart);
        }
    }
    
    public WormholesGenerateTubes(MainForm mf) {
        this.mf = mf;
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tubesTypesGroup = new javax.swing.ButtonGroup();
        cloakOutGroup = new javax.swing.ButtonGroup();
        cloakInGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        fileText = new javax.swing.JTextField();
        selectButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        scaleText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        thicknessText = new javax.swing.JTextField();
        transducersCheck = new javax.swing.JRadioButton();
        lensCheck = new javax.swing.JRadioButton();
        flipCheck = new javax.swing.JRadioButton();
        cloakCheck = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        focalLenghtText = new javax.swing.JTextField();
        bottomPadText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        nmText = new javax.swing.JTextField();
        separationText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        padText = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        internalSquareCheck = new javax.swing.JRadioButton();
        externalSquareRadio = new javax.swing.JRadioButton();
        externalTriangularRadio = new javax.swing.JRadioButton();
        internalTriangularCheck = new javax.swing.JRadioButton();
        internalSphericalCheck = new javax.swing.JRadioButton();
        externalSphericalCheck = new javax.swing.JRadioButton();
        externalHeightText = new javax.swing.JTextField();
        internalHeightText = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Wormholes");

        jLabel1.setText("file:");

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("scale:");

        scaleText.setText("100 100 100");

        jLabel3.setText("thickness:");

        thicknessText.setText("0.02");

        tubesTypesGroup.add(transducersCheck);
        transducersCheck.setSelected(true);
        transducersCheck.setText("phases from transducers");

        tubesTypesGroup.add(lensCheck);
        lensCheck.setText("lens");

        tubesTypesGroup.add(flipCheck);
        flipCheck.setText("flip");

        tubesTypesGroup.add(cloakCheck);
        cloakCheck.setText("cloak");

        jLabel6.setText("focal lenght");

        focalLenghtText.setText("0.03");

        bottomPadText.setText("0.01");

        jLabel4.setText("bottomPad:");

        jLabel5.setText("NxM:");

        nmText.setText("12 12");

        separationText.setText("0.01");

        jLabel7.setText("separation");

        jLabel8.setText("internal height:");

        jLabel9.setText("external height:");

        padText.setText("0.01");

        jLabel10.setText("pad:");

        cloakInGroup.add(internalSquareCheck);
        internalSquareCheck.setSelected(true);
        internalSquareCheck.setText("square");

        cloakOutGroup.add(externalSquareRadio);
        externalSquareRadio.setSelected(true);
        externalSquareRadio.setText("square");

        cloakOutGroup.add(externalTriangularRadio);
        externalTriangularRadio.setText("triangular");

        cloakInGroup.add(internalTriangularCheck);
        internalTriangularCheck.setText("triangular");

        cloakInGroup.add(internalSphericalCheck);
        internalSphericalCheck.setText("spherical");

        cloakOutGroup.add(externalSphericalCheck);
        externalSphericalCheck.setText("spherical");

        externalHeightText.setText("0.03");

        internalHeightText.setText("0.03");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileText)
                        .addGap(18, 18, 18)
                        .addComponent(selectButton)
                        .addGap(18, 18, 18)
                        .addComponent(exportButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scaleText, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(thicknessText, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 27, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nmText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separationText, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)))
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(padText, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bottomPadText, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(transducersCheck)
                            .addComponent(flipCheck)
                            .addComponent(cloakCheck)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(externalHeightText, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(internalHeightText)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lensCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(focalLenghtText, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(externalSquareRadio)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(externalTriangularRadio)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(externalSphericalCheck))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(internalSquareCheck)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(internalTriangularCheck)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(internalSphericalCheck)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton)
                    .addComponent(exportButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(scaleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(thicknessText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(bottomPadText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(padText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(separationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(nmText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transducersCheck)
                    .addComponent(lensCheck)
                    .addComponent(jLabel6)
                    .addComponent(focalLenghtText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(flipCheck)
                .addGap(18, 18, 18)
                .addComponent(cloakCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(externalHeightText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(externalSquareRadio)
                    .addComponent(externalTriangularRadio)
                    .addComponent(externalSphericalCheck))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(internalHeightText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(internalSquareCheck)
                    .addComponent(internalTriangularCheck)
                    .addComponent(internalSphericalCheck))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        final List<Tube> tubes = new ArrayList<>();
        
        if(transducersCheck.isSelected()){
            transducersPhase(tubes);
            addRegularPadding(tubes);
        }else if(lensCheck.isSelected()){
            lensTubes(tubes);
            addRegularPadding(tubes);
        }else if(flipCheck.isSelected()){
            flipTubes(tubes);
        }else if(cloakCheck.isSelected()){
            cloakTubes(tubes);
        }
        
        center(tubes);
        applyScale(tubes);
        writeTubes(tubes);
    }//GEN-LAST:event_exportButtonActionPerformed

     private void cloakTubes(List<Tube> tubes) {
         final float externalHeigth = Parse.toFloat( externalHeightText.getText() );
         final float internalHeigth = Parse.toFloat( internalHeightText.getText() );
         final int externalShape = externalSquareRadio.isSelected() ? 1 : (externalTriangularRadio.isSelected() ? 2 : 3 );
         final int internalShape = internalSquareCheck.isSelected() ? 1 : (internalTriangularCheck.isSelected() ? 2 : 3 );
         
         generateEmptyTubes(tubes);
         float maxDist = 0;
         for(Tube t : tubes){
             maxDist = M.max(maxDist, M.sqrt( t.top.x*t.top.x + t.top.z*t.top.z ) );
         }
         for(Tube t : tubes){
             final float dist = M.sqrt( t.top.x*t.top.x + t.top.z*t.top.z );
             final float distP = dist / maxDist;
             t.top.y = sampleHeightFunction(externalShape, distP) * externalHeigth;
             t.bottomTarget.y = sampleHeightFunction(internalShape, distP) * internalHeigth;
         }
     }
     
     private static float sampleHeightFunction(final int type, final float p){
         if (type == 1){
             return 1f;
         }else if (type == 2){
             return (1f - p);
         }else if (type == 3){
             return M.cos( p * M.HALF_PI);
         }
         return 0;
     }
     
    private void flipTubes(List<Tube> tubes) {
        generateEmptyTubes(tubes);
        
        final float thickness = Parse.toFloat( thicknessText.getText() );
        final float pad = Parse.toFloat( padText.getText() );
        
        
        float maxDistance = 0;
        for (Tube t : tubes){
            final Vector3f top = t.top;
            
            //symetry 90 deg
            if(top.x > 0 && top.z > 0){ //1st quadrant
                t.bottomTarget.z = -t.bottomTarget.z;
            }else if (top.x > 0 && top.z < 0){ //2nd 
                t.bottomTarget.x = -t.bottomTarget.x;
            }else if (top.x < 0 && top.z < 0){ //3rd
                t.bottomTarget.z = -t.bottomTarget.z;
            }else if (top.x < 0 && top.z > 0){ //4th
                t.bottomTarget.x = -t.bottomTarget.x;
            }
            t.bottomTarget.y -= pad;
            
            maxDistance = M.max(maxDistance, t.bottomTarget.distance( t.top) );
        }
        
        for (Tube t : tubes){
            final float diffDist = maxDistance - t.top.distance( t.bottomTarget );
            t.bottomStart.set(t.bottomTarget);
            t.bottomStart.y -= diffDist - pad;
        }
    }
    
    private void lensTubes(final List<Tube> tubes){
        final float focalLenght = Parse.toFloat( focalLenghtText.getText() );
        generateEmptyTubes(tubes);
        final Vector3f focalPoint = new Vector3f(0, focalLenght, 0);
        
        float minDistance = 0;
        for (Tube t : tubes){
            final Vector3f pos = t.top;
            minDistance = M.min(minDistance , pos.distance(focalPoint));
        }
        
        for (Tube t : tubes){
            final Vector3f pos = t.top;
            final float length = pos.distance(focalPoint) - minDistance;
            
            t.bottomTarget.set( pos ).subtractLocal(0, length, 0);
        }
    }
    
    private Vector3f tubesTopCenter(final List<Tube> tubes){
        final Vector3f center = new Vector3f();
        for(Tube e : tubes){
            center.addLocal( e.top);
        }
        center.divideLocal( tubes.size() );
        
        return center;
    }
    
    private void generateEmptyTubes(final List<Tube> tubes){
        final Vector2f nTubes = new Vector2f().parse( nmText.getText() );
        final Vector2f spacing = new Vector2f().parse( separationText.getText() );
        
        for(int ix = 0; ix < nTubes.x; ++ix){
            for(int iy = 0; iy < nTubes.y; ++iy){
                final float x = ix * spacing.x;
                final float z = iy * spacing.y;
                
                final Vector3f pos = new Vector3f(x, 0, z);
                Tube tube = new Tube( pos, pos, pos );
                tubes.add( tube );
            }
        }
        
        //center them
        final Vector3f center = tubesTopCenter( tubes );
        for (Tube t : tubes){
            t.top.subtractLocal( center );
            t.bottomStart.subtractLocal( center );
            t.bottomTarget.subtractLocal( center );
        }
    }
    
    private void transducersPhase(final List<Tube> tubes){
        for (Transducer t : mf.simulation.transducers){
            final Vector3f pos = t.getTransform().getTranslation();
            final float phase = t.getPhase();
            final float length = phase / 2 * (mf.simulation.getMediumSpeed() / t.getFrequency());
            
            Tube tube = new Tube( pos, pos, pos.subtract(0, length, 0) );
            tubes.add( tube );
        }
    }
    
    private void addRegularPadding(final List<Tube> tubes){
        final float pad = Parse.toFloat( thicknessText.getText() );
        for (Tube t : tubes){
            t.bottomStart.y -= pad;
            t.bottomTarget.y -= pad;
        }
    }
    
    private void applyScale(final List<Tube> tubes){
        final Vector3f scale = new Vector3f( scaleText.getText() );
        for (Tube t : tubes){
            t.top.multLocal( scale );
            t.bottomTarget.multLocal( scale );
            t.bottomStart.multLocal( scale );
        }
    }
    
    private void center(final List<Tube> tubes){
        final float bottomPad = Parse.toFloat( bottomPadText.getText() );
        final Vector3f min = new Vector3f(Float.MAX_VALUE);
        final Vector3f max = new Vector3f(-Float.MAX_VALUE);
        for (Tube t : tubes){
            min.minLocal( t.top ).minLocal( t.bottomStart ).minLocal( t.bottomTarget );
            max.maxLocal(t.top ).maxLocal( t.bottomStart ).maxLocal( t.bottomTarget );
        }
        final Vector3f center = max.add(min).divideLocal(2);
        for( Tube t : tubes ){
            t.top.x -= center.x;
            t.bottomStart.x -= center.x;
            t.bottomTarget.x -= center.x;
            
            t.top.y -= min.y - bottomPad;
            t.bottomStart.y -= min.y - bottomPad;
            t.bottomTarget.y -= min.y - bottomPad;
            
            t.top.z -= center.z;
            t.bottomStart.z -= center.z;
            t.bottomTarget.z -= center.z;
        }
    }
    
    private void writeTubes(final List<Tube> tubes){
       StringBuilder sb = new StringBuilder();
        
        for (Tube t : tubes){
            sb.append( t.top.toStringSimple(" ") + " ");
            sb.append( t.bottomTarget.toStringSimple(" ") + " ");
            sb.append( t.bottomStart.toStringSimple(" ") + "\n");
        }
        
        try {
            FileUtils.writeBytesInFile(new File(fileText.getText()), sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(WormholesGenerateTubes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        String file = FileUtils.selectNonExistingFile(this, ".txt");
        if (file != null){
            fileText.setText(file);
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    
 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bottomPadText;
    private javax.swing.JRadioButton cloakCheck;
    private javax.swing.ButtonGroup cloakInGroup;
    private javax.swing.ButtonGroup cloakOutGroup;
    private javax.swing.JButton exportButton;
    private javax.swing.JTextField externalHeightText;
    private javax.swing.JRadioButton externalSphericalCheck;
    private javax.swing.JRadioButton externalSquareRadio;
    private javax.swing.JRadioButton externalTriangularRadio;
    private javax.swing.JTextField fileText;
    private javax.swing.JRadioButton flipCheck;
    private javax.swing.JTextField focalLenghtText;
    private javax.swing.JTextField internalHeightText;
    private javax.swing.JRadioButton internalSphericalCheck;
    private javax.swing.JRadioButton internalSquareCheck;
    private javax.swing.JRadioButton internalTriangularCheck;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton lensCheck;
    private javax.swing.JTextField nmText;
    private javax.swing.JTextField padText;
    private javax.swing.JTextField scaleText;
    private javax.swing.JButton selectButton;
    private javax.swing.JTextField separationText;
    private javax.swing.JTextField thicknessText;
    private javax.swing.JRadioButton transducersCheck;
    private javax.swing.ButtonGroup tubesTypesGroup;
    // End of variables declaration//GEN-END:variables
}
