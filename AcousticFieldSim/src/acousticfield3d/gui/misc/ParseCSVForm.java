package acousticfield3d.gui.misc;

import acousticfield3d.gui.AddTransducersForm;
import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.utils.Parse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParseCSVForm extends javax.swing.JFrame {
    final MainForm mf;
    
    public ParseCSVForm(MainForm mf) {
        this.mf = mf;
        initComponents();
    }

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        fileText = new javax.swing.JTextField();
        selectButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        formatText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        skipLinesText = new javax.swing.JTextField();
        importButton = new javax.swing.JButton();
        animationCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Parse CSV");

        jLabel1.setText("File:");

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Format:x,y,z,nx,ny,nz,phase,amplitude,power,fr,apper,Type,sx,sy,sz");

        formatText.setText("0,1,2,a,a,a,3,4,a,a,a,a,a,a,a");

        jLabel3.setText("Skip first lines:");

        skipLinesText.setText("1");

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        animationCheck.setText("Animation");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatText)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileText, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(selectButton))
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(skipLinesText, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(animationCheck))
                            .addComponent(importButton))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(skipLinesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(animationCheck))
                .addGap(18, 18, 18)
                .addComponent(importButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        final String file = FileUtils.selectFile(this, "Import CSV", "", null);
        if (file != null)
            fileText.setText(file);
    }//GEN-LAST:event_selectButtonActionPerformed

    private static float parse(float defValue, int index, String[] indices, String[] vals){
        if (index >= indices.length)
            return defValue;
        String is = indices[index];
        
        try{
            int i = Integer.parseInt( is );
            int absI = Math.abs(i);
            if ( absI >= vals.length )
                return defValue;
            float val = Float.parseFloat( vals[absI] );
            return (i >= 0) ? val : -val;
        }catch( NumberFormatException ne){
            return defValue;
        }
    }
    
    private static int parse(int defValue, int index, String[] indices, String[] vals){
        if (index >= indices.length)
            return defValue;
        String is = indices[index];
        
        try{
            int i = Integer.parseInt( is );
            int absI = Math.abs(i);
            if ( absI >= vals.length )
                return defValue;
            int val = Integer.parseInt( vals[absI] );
            return (i >= 0) ? val : -val;
        }catch( NumberFormatException ne){
            return defValue;
        }
    }
    
    private static void parse(Vector3f defValue, int startIndex, String[] indices, String[] vals){
        defValue.x = parse(defValue.x, startIndex+0, indices, vals);
        defValue.y = parse(defValue.y, startIndex+1, indices, vals);
        defValue.z = parse(defValue.z, startIndex+2, indices, vals);
    }
    
    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        final String file = fileText.getText();
        final String[] indices = formatText.getText().split(",");
        final int skip = Parse.toInt( skipLinesText.getText() );
        final boolean animation = animationCheck.isSelected();
        try {
            String[] lines = FileUtils.getLinesFromFile(new File(file));
            final int nLines = lines.length;
            int nTransducer = 0;
            for (int i = skip; i < nLines; i++) {
                final String[] s = lines[i].trim().split(",");
                final Transducer t = mf.addTransducersForm.createTransducer();
                   
                //Format:x,y,z,nx,ny,nz,phase,amplitude,power,fr,apper,Type,sx,sy,sz
                
                final Vector3f trans = t.getTransform().getTranslation();
                parse(trans, 0, indices, s);
                
                final Vector3f normal = new Vector3f();
                parse(normal, 3, indices, s);
                t.pointToTarget(normal.addLocal(t.getTransform().getTranslation()));

                t.setPhase( parse(t.getPhase(), 6, indices, s) );
                t.setpAmplitude( parse(t.getpAmplitude(), 7, indices, s) );
                
                t.setPower( parse(t.getPower(), 8, indices, s)  );
                t.setFrequency( parse(t.getFrequency(), 9, indices, s)  );
                t.setApperture( parse(t.getApperture(), 10, indices, s)  );

                t.setType( parse(t.getType(), 11, indices, s) );
                final Vector3f scale = t.getTransform().getScale();
                parse(scale, 12, indices, s);
              
                t.setDriverPinNumber(nTransducer);
                t.setOrderNumber(nTransducer);
                nTransducer += 1;
                mf.scene.getEntities().add(t);
                mf.simulation.transducers.add(t);
                mf.simulation.addTransToAnimationsKeys(t);
            }
            mf.updateBoundaries();
            mf.adjustGUIGainAndCameras();
        } catch (IOException ex) {
            Logger.getLogger(AddTransducersForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_importButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox animationCheck;
    private javax.swing.JTextField fileText;
    private javax.swing.JTextField formatText;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton selectButton;
    private javax.swing.JTextField skipLinesText;
    // End of variables declaration//GEN-END:variables
}
