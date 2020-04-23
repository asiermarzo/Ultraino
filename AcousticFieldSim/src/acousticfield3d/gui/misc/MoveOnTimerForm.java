package acousticfield3d.gui.misc;

import acousticfield3d.gui.MainForm;
import acousticfield3d.utils.Parse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author am14010
 */
public class MoveOnTimerForm extends javax.swing.JFrame {
    final MainForm mf;
    
    WorkerThread worker = null;
    
    public MoveOnTimerForm(MainForm mf) {
        this.mf = mf;
        initComponents();
        
        mf.pointsPanel.selectAll();
        mf.movePanel.snapParticlesPosition();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        msText = new javax.swing.JTextField();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        pushTogetherToggle = new javax.swing.JToggleButton();
        rotateToggle = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Move Timer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Every ms:");

        msText.setText("2000");

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        pushTogetherToggle.setText("Push together");

        rotateToggle.setText("Rotate");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(msText, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(startButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pushTogetherToggle)
                        .addGap(18, 18, 18)
                        .addComponent(rotateToggle)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(msText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startButton)
                    .addComponent(stopButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pushTogetherToggle)
                    .addComponent(rotateToggle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        stopButtonActionPerformed(evt);
        worker = new WorkerThread();
        worker.start();
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if(worker != null){
            worker.interrupt();
            worker = null;
        }
        
        try {
            Thread.sleep( 200 );
        } catch (InterruptedException ex) {
            Logger.getLogger(MoveOnTimerForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        mf.movePanel.resetParticlePos();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        stopButtonActionPerformed( null );
    }//GEN-LAST:event_formWindowClosing

  
    public class WorkerThread extends Thread{

        @Override
        public void run() {
            while (! isInterrupted()){
                if (pushTogetherToggle.isSelected()){
                    mf.movePanel.applyScaleRepeat( -1 );
                }
                if (rotateToggle.isSelected()){
                    mf.movePanel.applyRotationRepeat(0, 1, 0);
                }
                
                final int millis = Parse.toInt( msText.getText() );
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField msText;
    private javax.swing.JToggleButton pushTogetherToggle;
    private javax.swing.JToggleButton rotateToggle;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
}
