package acousticfield3d.gui.misc;

import acousticfield3d.algorithms.BFGSOptimization;
import acousticfield3d.algorithms.CalcField;
import acousticfield3d.algorithms.DivTransFocus;
import acousticfield3d.algorithms.Kinoforms;
import acousticfield3d.algorithms.SimplePhaseAlgorithms;
import acousticfield3d.algorithms.bfgs.BFGSProgressListener;
import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Parse;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 *
 * @author Asier
 */
public class AlgorithmsForm extends javax.swing.JFrame implements BFGSProgressListener{
    final MainForm mf;
    final BFGSOptimization bfgs;
            
    private int reportEvery = 1;
    
    public AlgorithmsForm(MainForm mf) {
        this.mf = mf;
        initComponents();
        bfgs = new BFGSOptimization(this);
    }

    public MainForm getMf() {
        return mf;
    }
    
    public void setBarValue(int p){
        progressBar.setValue( p );
    }
    
    public double getAlpha(){
        return Parse.toDouble( alphaText.getText() );
    }
    
    public int getSteps(){
        return Parse.toInt(stepsText.getText() );
    }
    
    public int getReportEvery(){
        return Parse.toInt( reportEveryText.getText() );
    }
    
    public double getXMin(){
        return Parse.toDouble(xMinText.getText() );
    }
    
    public double getGMin(){
        return Parse.toDouble( gMinText.getText() );
    }
    
    public Vector3f getLaplacianConstants(){
        return new Vector3f().parse( laplacianConstantsText.getText() );
    }
    
    public double getLowPressureK(){
        return Parse.toDouble( lowPressureKText.getText() );
    }
    
    public boolean isPressure(){
        return pressureCheck.isSelected();
    }
    public boolean isIterGorkPressure(){
        return simpleGorkovCheck.isSelected();
    }
    public boolean isGorkov(){
        return gorkovCheck.isSelected();
    }
    public boolean isForce(){
        return forceCheck.isSelected();
    }
    public boolean isBottle(){
        return bottleCheck.isSelected();
    }
    public boolean isGLaplacian(){
        return maxGLaplacian.isSelected();
    }
    
    
    public boolean isEqualizer(){
        return equalizerCheck.isSelected();
    }
    
    public double getEqualizerWeight(){
        return Parse.toDouble( equalizerWeightText.getText() );
    }

    public Vector3f getWeightsN(){
        return new Vector3f().parse( weightsNText.getText() );
    }
    
    public Vector3f getWeightsP(){
        return new Vector3f().parse( weightsPText.getText() );
    }
    
    public Vector3f getDistances(){
        return new Vector3f().parse( distancesText.getText() );
    }
    
    float[] prevPressPhases = null;
    private void calcAlgorithm(boolean autoSelectAllParticles){
        final Simulation sim = mf.simulation; 
        final boolean reusePre = resurePreCheck.isSelected();
        final int iters = getSteps();
        final int kickstartIters = Parse.toInt( itersMultiFocalKickText.getText() );
        
        //gather the control points
        bfgs.controlPoints.clear();
        if (!autoSelectAllParticles) {
            for (Entity e : mf.getSelection()) {
                if ((e.getTag() & Entity.TAG_CONTROL_POINT) != 0) {
                    bfgs.controlPoints.add(e);
                }
            }
        } else {
            bfgs.controlPoints.addAll(mf.simulation.controlPoints);
        }

        if (bfgs.controlPoints.isEmpty()) {
            return;
        }
        
        if (kinoformsCheck.isSelected()){
            final boolean normAmp = normAmpCheck.isSelected();
            Kinoforms smp = Kinoforms.create(mf, bfgs.controlPoints);
            for (int i = 0; i<iters; ++i){
                smp.iterate(! normAmp );
            }
            if(normAmp){
                smp.normalizeTransducersAmplitude();
            }
           smp.applySolution();
           
            //add PI into the top array
            if (piStartTop.isSelected()) {
                mf.simulation.addPiToTopTransducers();
            } else if (piStartHalfCheck.isSelected()) {
                mf.simulation.addPiToHalfRightTransducers();
            }
                
        }else if (divTransCheck.isSelected()){
            DivTransFocus.calcMultiFocus(mf, mf.simulation.transducers, bfgs.controlPoints, divTransMethodCombo.getSelectedIndex());
        }else{ //regular algorithms
            //kickstart?
            if (kickstartCheck.isSelected()) {
                
                //optimize pressure
                if (focalKickCheck.isSelected()) {
                    
                    if (kickstartBFGSorHoloCheck.isSelected()){
                        //only one particle --> simple focal
                        if (bfgs.controlPoints.size() == 1) {
                            SimplePhaseAlgorithms.focus(mf.simulation.transducers,
                                    bfgs.controlPoints.get(0).getTransform().getTranslation(),
                                    mf.simulation.getMediumSpeed());
                        } else { //more particles -> use BFGS with pressure
                            if (reusePre){ //apply the precalc phases
                                applyPhases(); 
                            }
                            
                            mf.renderer.updateTransducersBuffers(sim);
                            bfgs.calcMultiPressure(mf, kickstartIters);
                            if (reusePre){ //get the results
                               gatherPhases(); 
                            }
                        }
                    }else{
                        if (reusePre) { //apply the precalc phases
                            applyPhases();
                        }

                        Kinoforms smp = Kinoforms.create(mf, bfgs.controlPoints);
                        for (int i = 0; i < kickstartIters; ++i) {
                            smp.iterate(true);
                        }
                        smp.applySolution();

                        if (reusePre) { //get the results
                            gatherPhases();
                        }
                    }
                }

                //add PI into the top array
                if (piStartTop.isSelected()) {
                    mf.simulation.addPiToTopTransducers();
                }else if(piStartHalfCheck.isSelected()){
                    mf.simulation.addPiToHalfRightTransducers();
                }
            }

            //update the buffers
            mf.renderer.updateTransducersBuffers(sim);

            //run the algorithm
            bfgs.calc(mf);
        }
        
    }
    
    private void gatherPhases(){
        final ArrayList<Transducer> trans = mf.simulation.transducers;
        final int nTrans = trans.size();
        if (prevPressPhases == null || prevPressPhases.length != nTrans){
            prevPressPhases = new float[nTrans];
        }
        for(int i = 0; i < nTrans; ++i){
            prevPressPhases[i] = trans.get(i).getPhase();
        }
    }
    private void applyPhases(){
        if (prevPressPhases == null) { return; }
        final ArrayList<Transducer> trans = mf.simulation.transducers;
        final int n = M.min( trans.size(), prevPressPhases.length);
        for(int i = 0; i < n; ++i){
            trans.get(i).setPhase( prevPressPhases[i] );
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        varToOptimizeGroup = new javax.swing.ButtonGroup();
        bottleCheck = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        distancesText = new javax.swing.JTextField();
        centralWText = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        weightsPText = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        weightsNText = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        groupHoloOrigin = new javax.swing.ButtonGroup();
        forceCheck = new javax.swing.JRadioButton();
        okButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        calcBFGSButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        stepsText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        xMinText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        gMinText = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        area = new javax.swing.JTextArea();
        clearAreaButton = new javax.swing.JButton();
        reportEveryText = new javax.swing.JTextField();
        pressureCheck = new javax.swing.JRadioButton();
        gorkovCheck = new javax.swing.JRadioButton();
        lowPressureKText = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        maxGLaplacian = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        laplacianConstantsText = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        alphaText = new javax.swing.JTextField();
        simpleGorkovCheck = new javax.swing.JRadioButton();
        equalizerCheck = new javax.swing.JCheckBox();
        equalizerWeightText = new javax.swing.JTextField();
        kickstartCheck = new javax.swing.JCheckBox();
        focalKickCheck = new javax.swing.JCheckBox();
        piStartTop = new javax.swing.JCheckBox();
        reportCheck = new javax.swing.JCheckBox();
        itersMultiFocalKickText = new javax.swing.JTextField();
        resurePreCheck = new javax.swing.JCheckBox();
        piStartHalfCheck = new javax.swing.JCheckBox();
        kinoformsCheck = new javax.swing.JRadioButton();
        kickstartBFGSorHoloCheck = new javax.swing.JCheckBox();
        iterButton = new javax.swing.JButton();
        divTransCheck = new javax.swing.JRadioButton();
        divTransMethodCombo = new javax.swing.JComboBox();
        normAmpCheck = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        presetsCombo = new javax.swing.JComboBox();
        presetButton = new javax.swing.JButton();

        jButton1.setText("jButton1");

        jButton2.setText("jButton2");

        varToOptimizeGroup.add(bottleCheck);
        bottleCheck.setText("bottle");

        jLabel7.setText("distances:");

        distancesText.setText("10E-3 10E-3 10E-3");

        centralWText.setText("4");

        jLabel9.setText("Central Weight:");

        weightsPText.setText("-1 0 -1");

        jLabel11.setText("weightsP:");

        weightsNText.setText("-1 -4 -1");

        jLabel8.setText("weightsN:");

        varToOptimizeGroup.add(forceCheck);
        forceCheck.setText("Force");

        setTitle("Algorithms");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("BFGS");

        calcBFGSButton.setText("Calc");
        calcBFGSButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcBFGSButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Steps:");

        stepsText.setText("10");

        jLabel4.setText("xMin:");

        xMinText.setText("1E-25");

        jLabel5.setText("gMin:");

        gMinText.setText("1E-25");

        area.setColumns(20);
        area.setRows(5);
        jScrollPane1.setViewportView(area);

        clearAreaButton.setText("Clear");
        clearAreaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAreaButtonActionPerformed(evt);
            }
        });

        reportEveryText.setText("1000");

        varToOptimizeGroup.add(pressureCheck);
        pressureCheck.setText("Pressure");

        varToOptimizeGroup.add(gorkovCheck);
        gorkovCheck.setText("MinGorkov");

        lowPressureKText.setText("1");

        jLabel10.setText("LowPK");

        varToOptimizeGroup.add(maxGLaplacian);
        maxGLaplacian.setText("MaxLapMinAmp");

        jLabel12.setText("WeightsXYZ:");

        laplacianConstantsText.setText("1.0 1.0 1.0");

        jLabel13.setText("Alpha:");

        alphaText.setText("1.0");

        varToOptimizeGroup.add(simpleGorkovCheck);
        simpleGorkovCheck.setText("SGork");

        equalizerCheck.setText("equalizer");

        equalizerWeightText.setText("1");

        kickstartCheck.setText("KickStart");

        focalKickCheck.setText("Focal");

        piStartTop.setText("PI on top");

        reportCheck.setText("report");

        itersMultiFocalKickText.setText("10");

        resurePreCheck.setText("ReusePre");

        piStartHalfCheck.setText("PI on half");

        varToOptimizeGroup.add(kinoformsCheck);
        kinoformsCheck.setSelected(true);
        kinoformsCheck.setText("ITR");

        kickstartBFGSorHoloCheck.setText("BFGS or HOLO");

        iterButton.setText("Iter");
        iterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterButtonActionPerformed(evt);
            }
        });

        varToOptimizeGroup.add(divTransCheck);
        divTransCheck.setText("divTrans");

        divTransMethodCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Checker", "Line", "Random", "Closest" }));

        normAmpCheck.setText("NormAmp");

        jLabel1.setText("presets:");

        presetsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ITR focal points", "ITR Standing Waves", "BFGS max laplacian" }));

        presetButton.setText("Set");
        presetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(clearAreaButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(calcBFGSButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(reportCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reportEveryText, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(iterButton)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(laplacianConstantsText, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowPressureKText))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(equalizerCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(equalizerWeightText)
                                .addGap(122, 122, 122))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(resurePreCheck)
                                .addGap(10, 10, 10)
                                .addComponent(kickstartBFGSorHoloCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(piStartTop)
                        .addGap(2, 2, 2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(kickstartCheck)
                        .addGap(14, 14, 14)
                        .addComponent(focalKickCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(itersMultiFocalKickText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(piStartHalfCheck))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stepsText))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(xMinText, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gMinText, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alphaText))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(kinoformsCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(normAmpCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(divTransCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(divTransMethodCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pressureCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gorkovCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxGLaplacian)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(simpleGorkovCheck)
                        .addGap(0, 39, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(presetsCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(presetButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(presetsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(presetButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(calcBFGSButton)
                            .addComponent(reportCheck)
                            .addComponent(reportEveryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iterButton))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(stepsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(xMinText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(gMinText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(alphaText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(kinoformsCheck)
                    .addComponent(divTransCheck)
                    .addComponent(divTransMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(normAmpCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pressureCheck)
                    .addComponent(gorkovCheck)
                    .addComponent(maxGLaplacian)
                    .addComponent(simpleGorkovCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(laplacianConstantsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lowPressureKText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(equalizerCheck)
                    .addComponent(equalizerWeightText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(piStartHalfCheck)
                    .addComponent(kickstartCheck)
                    .addComponent(focalKickCheck)
                    .addComponent(itersMultiFocalKickText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(piStartTop)
                    .addComponent(resurePreCheck)
                    .addComponent(kickstartBFGSorHoloCheck))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(clearAreaButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        setVisible( false );
    }//GEN-LAST:event_okButtonActionPerformed

    private void calcBFGSButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcBFGSButtonActionPerformed
        runBFGS( true, true, false );
    }//GEN-LAST:event_calcBFGSButtonActionPerformed

    public void runBFGS(boolean runInParallel, boolean updateAfter, boolean autoselectAllParticles){
        reportEvery = getReportEvery();
        
        if (runInParallel){    
            WorkingCalc wc = new WorkingCalc(updateAfter);
            wc.start();
        }else{
            calcAlgorithm( autoselectAllParticles );
            if(updateAfter){
                mf.needUpdate();
            }
        }
        
    }
    
    private void clearAreaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAreaButtonActionPerformed
        area.setText( "" );
    }//GEN-LAST:event_clearAreaButtonActionPerformed

    private void iterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iterButtonActionPerformed
        final ArrayList<Entity> sel = new ArrayList<>( mf.selection );
        final int n = sel.size();
        final float[] gorkovs = new float[n];
        int index = 0;
        float totalGorkov = 0;
        float maxGorkov = -Float.MAX_VALUE;
        for(Entity e : sel){
            final Vector3f pos = e.getTransform().getTranslation();
            final double g = CalcField.calcGorkovAt(pos.x, pos.y, pos.z, 0.0005f, mf);
            
            totalGorkov += g;
            gorkovs[index] = (float) g;
            maxGorkov = M.max(maxGorkov, (float)g);
                    
            index++;
        }
       final float avgGorkov = totalGorkov / n;
        index = 0;
        for(Entity e : sel){
            if (gorkovs[index] >= maxGorkov){
                mf.setSelection(e);
                runBFGS(false,false,false);
            }
            index++;
        }
        
        mf.setSelection(sel);
        mf.needUpdate();
    }//GEN-LAST:event_iterButtonActionPerformed

    private void presetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetButtonActionPerformed
        //clear to the default options
        reportCheck.setSelected(false);
        reportEveryText.setText("1000");
        stepsText.setText("0");
        xMinText.setText("1E-25");
        gMinText.setText("1E-25");
        alphaText.setText("1.0");
        normAmpCheck.setSelected(false);
        varToOptimizeGroup.clearSelection();
        laplacianConstantsText.setText("1.0 1.0 1.0");
        lowPressureKText.setText("1");
        equalizerCheck.setSelected(false);
        equalizerWeightText.setText("1");
        kickstartCheck.setSelected(false);
        focalKickCheck.setSelected(false);
        itersMultiFocalKickText.setText("10");
        piStartHalfCheck.setSelected(false);
        piStartTop.setSelected(false);
        resurePreCheck.setSelected(false);
        kickstartBFGSorHoloCheck.setSelected(false);
        
        final int selectedPreset = presetsCombo.getSelectedIndex();
        if (selectedPreset == 0){ // ITR Focal points
            stepsText.setText("50");
            kinoformsCheck.setSelected(true);
        }else if (selectedPreset == 1){ //ITR standing waves
            pressureCheck.setSelected(true);
            kickstartCheck.setSelected(true);
            resurePreCheck.setSelected(true);
            focalKickCheck.setSelected(true);
            piStartTop.setSelected(true);
        }else if (selectedPreset == 2){ //BFGS max laplacian
            kickstartCheck.setSelected(true);
            stepsText.setText("5000");
            maxGLaplacian.setSelected(true);
        } 
    }//GEN-LAST:event_presetButtonActionPerformed

    @Override
    public void bfgsOnStep(int currentSteps, int totalSteps, double diffX, double diffG, int hessian) {
            progressBar.setValue( currentSteps * 100 / totalSteps);
            if (currentSteps % reportEvery == 0){
                area.append( currentSteps + "/" + totalSteps + " -> " + 
                        "diffX=" + diffX + " | diffG=" + diffG + "| hessians = " + hessian + "\n");
            }
    }
    
    @Override
    public void bfgsOnFinish(int iters, boolean didExitX, boolean didExitG, int hessian, double fx, double[] x){
            area.append( "Finish iters = " + iters + " x = " + didExitX + " g = " + didExitG + "| hessians = " + hessian +"\n");
    }

    public void selectDefaultAlg(int algNumber) {
        if (algNumber == 0){
            pressureCheck.setSelected( true );
        }else if (algNumber == 1){
            forceCheck.setSelected( true );
        }else if (algNumber == 2){
            gorkovCheck.setSelected( true );
        }else if (algNumber == 3){
            maxGLaplacian.setSelected( true );
        }
    }

    void setSteps(int steps){
        stepsText.setText( steps + "");
    }
    
    void setAlgorithm(int i) {
        pressureCheck.setSelected(false);
        simpleGorkovCheck.setSelected(false);
        gorkovCheck.setSelected(false);
        maxGLaplacian.setSelected(false);
        kinoformsCheck.setSelected(false);
        
        if(i == 0){
            pressureCheck.setSelected(true);
        }else if(i == 1){
            simpleGorkovCheck.setSelected(true);
        }else if(i == 2){
            gorkovCheck.setSelected(true);
        }else if(i == 3){
            maxGLaplacian.setSelected(true);
        }else if(i == 4){
            kinoformsCheck.setSelected(true);
        }
    }
  
    public boolean isReport(){
        return reportCheck.isSelected();
    }
    
    public class WorkingCalc extends Thread {
        private final boolean needUpdate;

        public WorkingCalc(boolean needUpdate) {
            this.needUpdate = needUpdate;
        }
        
        @Override
        public void run() {
            calcBFGSButton.setEnabled( false );
            try{
                calcAlgorithm( false );
            }catch (Exception e){
                e.printStackTrace();
            }
            
            calcBFGSButton.setEnabled( true );
            if(needUpdate){
                mf.needUpdate();
            }
        }
        
    }

    public JCheckBox getKickstartCheck() {
        return kickstartCheck;
    }

    public JTextField getItersMultiFocalKickText() {
        return itersMultiFocalKickText;
    }

    public JCheckBox getResurePreCheck() {
        return resurePreCheck;
    }

    public JCheckBox getKickstartBFGSorHoloCheck() {
        return kickstartBFGSorHoloCheck;
    }
       
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alphaText;
    private javax.swing.JTextArea area;
    private javax.swing.JRadioButton bottleCheck;
    private javax.swing.JButton calcBFGSButton;
    private javax.swing.JTextField centralWText;
    private javax.swing.JButton clearAreaButton;
    private javax.swing.JTextField distancesText;
    private javax.swing.JRadioButton divTransCheck;
    private javax.swing.JComboBox divTransMethodCombo;
    private javax.swing.JCheckBox equalizerCheck;
    private javax.swing.JTextField equalizerWeightText;
    private javax.swing.JCheckBox focalKickCheck;
    private javax.swing.JRadioButton forceCheck;
    private javax.swing.JTextField gMinText;
    private javax.swing.JRadioButton gorkovCheck;
    private javax.swing.ButtonGroup groupHoloOrigin;
    private javax.swing.JButton iterButton;
    private javax.swing.JTextField itersMultiFocalKickText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox kickstartBFGSorHoloCheck;
    private javax.swing.JCheckBox kickstartCheck;
    private javax.swing.JRadioButton kinoformsCheck;
    private javax.swing.JTextField laplacianConstantsText;
    private javax.swing.JTextField lowPressureKText;
    private javax.swing.JRadioButton maxGLaplacian;
    private javax.swing.JCheckBox normAmpCheck;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox piStartHalfCheck;
    private javax.swing.JCheckBox piStartTop;
    private javax.swing.JButton presetButton;
    private javax.swing.JComboBox presetsCombo;
    private javax.swing.JRadioButton pressureCheck;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox reportCheck;
    private javax.swing.JTextField reportEveryText;
    private javax.swing.JCheckBox resurePreCheck;
    private javax.swing.JRadioButton simpleGorkovCheck;
    private javax.swing.JTextField stepsText;
    private javax.swing.ButtonGroup varToOptimizeGroup;
    private javax.swing.JTextField weightsNText;
    private javax.swing.JTextField weightsPText;
    private javax.swing.JTextField xMinText;
    // End of variables declaration//GEN-END:variables


}
