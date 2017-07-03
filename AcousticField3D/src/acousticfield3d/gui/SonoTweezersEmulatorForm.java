/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.gui;

import acousticfield3d.math.M;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Parse;
import acousticfield3d.utils.VarConv;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author am14010
 */
public class SonoTweezersEmulatorForm extends javax.swing.JFrame {
    public static final int CHANNELS = 64;
    
    final MainForm mf;
    private Worker worker = null;
    
    public SonoTweezersEmulatorForm(MainForm mf) {
        this.mf = mf;
        initComponents();
    }

    public void startWorker(){
        stopWorker();
        
        try {
            worker = new Worker(mf, Parse.toInt( portText.getText()));
            worker.start();
        } catch (SocketException ex) {
            Logger.getLogger(SonoTweezersEmulatorForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void stopWorker(){
        if (worker != null){
            worker.interrupt();
            worker = null;
        }
    }
    
    public class Worker extends Thread{
        final MainForm mf;
        
        final DatagramSocket serverSocket;
        final byte[] receiveData = new byte[1024];
        final byte[] sendData = new byte[1024];
        final DatagramPacket rPacket;
        final DatagramPacket sPacket;
        
        final float[] phases = new float[CHANNELS];
        final float[] amplitudes = new float[CHANNELS];

        public Worker(MainForm mf, int port) throws SocketException {
            this.mf = mf;
            serverSocket = new DatagramSocket(port);
            rPacket = new DatagramPacket(receiveData, receiveData.length);
            sPacket = new DatagramPacket(sendData, sendData.length);
        }

        @Override
        public void run() {
            while (! isInterrupted()){
                try {
                    serverSocket.receive(rPacket);
                    final int len = rPacket.getLength();
                    if (len == 18){
                        final byte[] data = rPacket.getData();
                    
                        final byte command = data[0];
                        System.out.println ("received new packet: " + Arrays.toString(data));
                        
                        if (command == 50){ // Set phases and amplitudes
                            sendResponse(rPacket, rPacket.getLength());
                            
                            final int board = VarConv.uByteToInt( data[1] );
                            if (board >= 1 && board <= 8){
                                final int offset = (board-1) * 8;
                                //float freq = mf.simulation.getTransducers().get(0).frequency;
                                //float period = (1.0f / freq) / 6.25e-9f;
                                for(int i = 0; i < 8; ++i){
                                    phases[offset + i] = VarConv.uByteToInt( data[2 + i] ) / 255.0f * 2.0f; //remeber that our phases are always multiplied by PI
                                    //phases[offset + i] = VarConv.uByteToInt(data[2 + i]) / period * 2.0f; //remeber that our phases are always multiplied by PI
                                    amplitudes[offset + i] = VarConv.uByteToInt( data[10 + i] ) / 255.0f;
                                }
                            }
                        }else if (command == 51){ //set frequency
                            sendResponse(rPacket, rPacket.getLength());
                            
                            final int period = VarConv.uByteToInt( data[1] );
                            final float freq = 1.0f / (((float)period) * 6.25e-9f);
                            System.out.println("received frequency: " + freq);
                            for(Transducer t : mf.simulation.getTransducers()){
                                t.setFrequency( freq );
                            }
                            mf.needUpdate();
                            
                        }else if (command == 52){ //Apply phases&amps
                            sendResponse(rPacket, rPacket.getLength());
                            final List<Transducer> trans = mf.simulation.getTransducers();

                            final int N = M.min(CHANNELS, trans.size());
                            for (int i = 0; i < N; ++i) {
                                trans.get(i).setAmplitude( amplitudes[i] );
                                trans.get(i).setPhase( phases[i] );
                            }
                            mf.needUpdate();
                        }else if (command == 53){ //test
                            
                            sendResponse(rPacket, rPacket.getLength() ); 
                            
                        }else if (command == 54){ //power off
                            
                            sendResponse(rPacket, rPacket.getLength() ); 
                            
                            for(Transducer t : mf.simulation.getTransducers()){
                                t.setAmplitude( 0.0f );
                            }
                            mf.needUpdate();
                            
                        }else if (command == 55){ //power on
                            
                            sendResponse(rPacket, rPacket.getLength() ); 
                            
                            for(Transducer t : mf.simulation.getTransducers()){
                                t.setAmplitude( 1.0f );
                            }
                            mf.needUpdate();
                            
                        }
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SonoTweezersEmulatorForm.class.getName()).log(Level.SEVERE, null, ex);
                }    
            }
            
            try{
                serverSocket.close();
            }catch (Exception e){}
        }
        
        private void sendResponse(DatagramPacket p, int rsp){
            sendData[0] = (byte) rsp;
            sPacket.setData(sendData, 0, 1);
            sPacket.setAddress( p.getAddress() );
            sPacket.setPort( p.getPort() );
            
            try {
                serverSocket.send(sPacket);
            } catch (IOException ex) {
                Logger.getLogger(SonoTweezersEmulatorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        startButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("SonoTweezer emulator");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("UDP port:");

        portText.setText("9090");
        portText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portTextActionPerformed(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(startButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        final boolean on = startButton.isSelected();
        
        if (on){
            startWorker();
        }else{
            stopWorker();
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        stopWorker();
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void portTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portTextActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField portText;
    private javax.swing.JToggleButton startButton;
    // End of variables declaration//GEN-END:variables
}
