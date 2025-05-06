package acousticfield3d.gui.misc;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.Animation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.utils.Parse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportAnimationForm extends javax.swing.JFrame {
    final MainForm mf;
    
    public ImportAnimationForm(MainForm mf) {
        this.mf = mf;
        initComponents();
    }

 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mergeAnimsCheck = new javax.swing.JCheckBox();
        selectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Animation from CSV");

        mergeAnimsCheck.setText("Merge Animations");
        mergeAnimsCheck.setEnabled(false);

        selectButton.setText("Select Files");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mergeAnimsCheck)
                    .addComponent(selectButton))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mergeAnimsCheck)
                .addGap(18, 18, 18)
                .addComponent(selectButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        final boolean mergeAnim = mergeAnimsCheck.isSelected();
      
        final String[] files = FileUtils.selectFiles(this, "parse", "csv", null);

        for (String file : files) {
            String fileName = FileUtils.getFileName(file);
            String ampFile = file.replaceAll("0_Phase", "0_Amp");
            String phaseFile = file + "";
            Animation anim = new Animation();
            anim.setName(fileName);
            mf.simulation.animations.add(anim);
            try {
                String[] linesPhase = FileUtils.getLinesFromFile(new File(phaseFile));
                String[] linesAmp = FileUtils.getLinesFromFile(new File(ampFile));
                for (int i = 0; i < linesPhase.length; i++) {
                    String[] phases = linesPhase[i].split(",");
                    String[] amps = linesAmp[i].split(",");
                    int nTrans = Math.min(phases.length, mf.simulation.transducers.size());
                    AnimKeyFrame key = new AnimKeyFrame();
                    for (int j = 0; j < nTrans; j++) {
                        float amp = Parse.toFloat( amps[j] );
                        float phase = Parse.toFloat( phases[j] );
                        Transducer t = mf.simulation.transducers.get(j);
                        key.getTransPhases().put(t, phase / M.PI);
                        key.getTransAmplitudes().put(t, amp);
                        key.setDuration(1);
                        key.setNumber(i);
                    }
                    anim.getKeyFrames().add(key);
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ImportAnimationForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        mf.needUpdate();
        dispose();
    }//GEN-LAST:event_selectButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox mergeAnimsCheck;
    private javax.swing.JButton selectButton;
    // End of variables declaration//GEN-END:variables
}
