package acousticfield3d.gui.panels;

import acousticfield3d.gui.MainForm;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Resources;
import acousticfield3d.utils.Parse;

/**
 *
 * @author asier.marzo
 */
public class VolumetricPanel extends javax.swing.JPanel {
    final MainForm mf;
 
    public VolumetricPanel(MainForm mf) {
        this.mf = mf;
        initComponents();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        volRenderCombo = new javax.swing.JComboBox<>();
        timeDomainCheckBox = new javax.swing.JCheckBox();
        timeDomainSpeed = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        minAmpText = new javax.swing.JTextField();
        maxAmpText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        rayStepsText = new javax.swing.JTextField();
        isoValSlider = new javax.swing.JSlider();

        volRenderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "Solid", "MIP", "ISO" }));
        volRenderCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volRenderComboActionPerformed(evt);
            }
        });

        timeDomainCheckBox.setText("Time domain");
        timeDomainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeDomainCheckBoxActionPerformed(evt);
            }
        });

        timeDomainSpeed.setText("1.0");
        timeDomainSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeDomainSpeedActionPerformed(evt);
            }
        });

        jLabel1.setText("amp:");

        minAmpText.setText("0");
        minAmpText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minAmpTextActionPerformed(evt);
            }
        });

        maxAmpText.setText("2000");
        maxAmpText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxAmpTextActionPerformed(evt);
            }
        });

        jLabel2.setText("ray steps:");

        rayStepsText.setText("16");
        rayStepsText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rayStepsTextActionPerformed(evt);
            }
        });

        isoValSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                isoValSliderStateChanged(evt);
            }
        });
        isoValSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                isoValSliderPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(isoValSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(volRenderCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeDomainSpeed)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                            .addComponent(minAmpText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rayStepsText)
                            .addComponent(maxAmpText, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeDomainCheckBox)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(volRenderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(timeDomainCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeDomainSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(isoValSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAmpText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxAmpText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rayStepsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void volRenderComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volRenderComboActionPerformed
        final String selected = (String)volRenderCombo.getSelectedItem();
        final MeshEntity cube = mf.scene.getCubeHelper();
        if (selected.equalsIgnoreCase("off")){
           cube.setVisible( false );
        }else{
            cube.setVisible( true );
            if (selected.equalsIgnoreCase("Solid")){
                cube.setShader( Resources.SHADER_SOLID_SPEC );
            }else{
                cube.setShader( Resources.SHADER_VOLUMETRIC );
            }
        }
        mf.needUpdate();
    }//GEN-LAST:event_volRenderComboActionPerformed

    private void timeDomainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeDomainCheckBoxActionPerformed
        mf.needUpdate();
    }//GEN-LAST:event_timeDomainCheckBoxActionPerformed

    private void timeDomainSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeDomainSpeedActionPerformed
        mf.needUpdate();
    }//GEN-LAST:event_timeDomainSpeedActionPerformed

    private void minAmpTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minAmpTextActionPerformed
        mf.needUpdate();
    }//GEN-LAST:event_minAmpTextActionPerformed

    private void maxAmpTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxAmpTextActionPerformed
        mf.needUpdate();
    }//GEN-LAST:event_maxAmpTextActionPerformed

    private void rayStepsTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rayStepsTextActionPerformed
        mf.needUpdate();
    }//GEN-LAST:event_rayStepsTextActionPerformed

    private void isoValSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_isoValSliderPropertyChange
        mf.needUpdate();
    }//GEN-LAST:event_isoValSliderPropertyChange

    private void isoValSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_isoValSliderStateChanged
        mf.needUpdate();
    }//GEN-LAST:event_isoValSliderStateChanged

    public float getMinAmp(){
        return Parse.toFloat( minAmpText.getText() );
    }
    public float getMaxAmp(){
        return Parse.toFloat( maxAmpText.getText() );
    }
    public float getDensity(){
        return Parse.toFloat( rayStepsText.getText() );
    }
    public int getRenderType() {
       final String selected = (String)volRenderCombo.getSelectedItem();
       if ("MIP".equalsIgnoreCase(selected)){
           return 1;
       }else if ("ISO".equalsIgnoreCase(selected)){
           return 2;
       }
       return 0;
    }
    public float getTimeScale() {
        return Parse.toFloat( timeDomainSpeed.getText() );
    }
    public boolean isTimeDomain() {
       return timeDomainCheckBox.isSelected();
    }
     public float getIsoValue() {
        return getMaxAmp() * isoValSlider.getValue() / 100f;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider isoValSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField maxAmpText;
    private javax.swing.JTextField minAmpText;
    private javax.swing.JTextField rayStepsText;
    private javax.swing.JCheckBox timeDomainCheckBox;
    private javax.swing.JTextField timeDomainSpeed;
    private javax.swing.JComboBox<String> volRenderCombo;
    // End of variables declaration//GEN-END:variables

   


}
