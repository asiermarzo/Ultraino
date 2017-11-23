/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.gui;


import acousticfield3d.Config;
import acousticfield3d.gui.misc.AddRadialTransducersForm;
import acousticfield3d.gui.misc.ExportPlotsForm;
import acousticfield3d.gui.misc.ForcePlotsFrame;
import acousticfield3d.gui.misc.HybridSingleBeamForm;
import acousticfield3d.gui.misc.ImportExportPhasesMatlabForm;
import acousticfield3d.gui.misc.ImportPhasesAmpForm;
import acousticfield3d.gui.misc.MetaSurfaces;
import acousticfield3d.gui.misc.ScatterObjectForm;
import acousticfield3d.gui.misc.SliderPanel;
import acousticfield3d.gui.misc.SweepTinyLevParameters;
import acousticfield3d.gui.misc.TubeGen;
import acousticfield3d.gui.misc.WormholesGenerateMesh;
import acousticfield3d.gui.misc.WormholesGenerateTubes;
import acousticfield3d.utils.DialogUtils;
import acousticfield3d.utils.FileUtils;
import acousticfield3d.gui.panels.AnimPanel;
import acousticfield3d.gui.panels.ControlPointPanel;
import acousticfield3d.gui.panels.MiscPanel;
import acousticfield3d.gui.panels.MovePanel;
import acousticfield3d.gui.panels.RtSlicePanel;
import acousticfield3d.gui.panels.DomainPanel;
import acousticfield3d.gui.panels.TransControlPanel;
import acousticfield3d.gui.panels.TransducersPanel;
import acousticfield3d.gui.panels.TrapsPanel;
import acousticfield3d.math.M;
import acousticfield3d.math.Quaternion;
import acousticfield3d.math.Transform;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.protocols.ArduinoMEGA64;
import acousticfield3d.protocols.ArduinoMEGA64_Anim;
import acousticfield3d.protocols.ArduinoNano;
import acousticfield3d.renderer.Renderer;
import acousticfield3d.scene.BehavioursThread;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.scene.SceneObjExport;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Parse;
import acousticfield3d.utils.StringFormats;
import acousticfield3d.utils.TextFrame;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 *
 * @author Asier
 */
public final class MainForm extends javax.swing.JFrame {
    public static final String CONFIG_PATH = "config.ini.xml";
    
    public final StringFormats formats;
    
    final SliderPanel sliderPanel;
    public final GLJPanel gljpanel;
    public final Renderer renderer;
    public final Scene scene;
    public Simulation simulation;
    
    final BehavioursThread animationThread;
    
    public final ArrayList<Entity> selection =  new ArrayList<>();
    public final ArrayList<Entity> bag = new ArrayList<>();
    boolean cameraLooked;
    boolean hasDragged;
    int firstDragX, firstDragY;
    
    public final RtSlicePanel rtSlicePanel;
    public final AnimPanel animPanel;
    public final MiscPanel miscPanel;
    public final ControlPointPanel cpPanel;
    public final DomainPanel domainPanel;
    public final TransducersPanel transPanel;
    public final TransControlPanel transControlPanel;
    public final MovePanel movePanel;
    public final TrapsPanel trapsPanel;
    
    public final HoloPatternsForm holoPatternsForm;
    public final AddTransducersForm addTransducersForm;
    public final SimulationConfigForm simForm;
   
    public MouseControlForm mouseControlForm = null;
    
    public final Config config;
    
    public MainForm(Config config) {
        this.config = config;
        
        formats = new StringFormats();
        
        sliderPanel = new SliderPanel(1, true);
        
        cameraLooked = true;
         
        simulation = new Simulation();
        scene = new Scene();
        miscPanel = new MiscPanel(this);
        renderer = new Renderer(scene, this);
        
        rtSlicePanel = new RtSlicePanel(this);
        animPanel = new AnimPanel(this);
        cpPanel = new ControlPointPanel(this);
        domainPanel = new DomainPanel(this);
        transPanel = new TransducersPanel(this);
        transControlPanel = new TransControlPanel(this);
        movePanel = new MovePanel(this);
        trapsPanel = new TrapsPanel(this);
        
        holoPatternsForm = new HoloPatternsForm(this);
        simForm = new SimulationConfigForm(this);
        addTransducersForm = new AddTransducersForm(this, simulation, scene);
 
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        gljpanel = new GLJPanel(glcapabilities);
        gljpanel.addGLEventListener( new GLEventListener() {      
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
                renderer.init(glautodrawable.getGL().getGL2(), 
                        glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
            }          
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                renderer.reshape( glautodrawable.getGL().getGL2(), width, height );
            }      
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
                renderer.dispose( glautodrawable.getGL().getGL2() );
            }
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
                //TimerUtil.get().tack("Render");
                //TimerUtil.get().tick("Render");
                renderer.render( glautodrawable.getGL().getGL2(), 
                        glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight() );
            }
        });
        
        initComponents();
        mainTabPanel.addTab("Trans", transPanel);
        mainTabPanel.addTab("Slices", rtSlicePanel);
        mainTabPanel.addTab("Anim", animPanel);
        mainTabPanel.addTab("Misc", miscPanel);
        mainTabPanel.addTab("Points", cpPanel);
        mainTabPanel.addTab("Sizes", domainPanel);
        mainTabPanel.addTab("Devs", transControlPanel);
        mainTabPanel.addTab("Move", movePanel);
        mainTabPanel.addTab("Traps", trapsPanel);
        
        initSimulation();
        
        animationThread = new BehavioursThread(scene, this);
        //animationThread.start();
        
        if (config != null && config.lastPath != null){
            FileUtils.setLastIndicatedPath( new File( config.lastPath ) );
            FileUtils.setLastChooserPath(new File( config.lastPath ) );
        }
    }

    
    public Simulation getSimulation() {
        return simulation;
    }

    public Renderer getRenderer() {
        return renderer;
    }
    
    public void initSimulation(){
        animPanel.initSimulation();
        
        //read the simulation constants
        simForm.objToGUI();
        
        //remove old transducers
        Scene.removeWithTag(scene.getEntities(), Entity.TAG_TRANSDUCER);
        //remove old Control Points
        Scene.removeWithTag(scene.getEntities(), Entity.TAG_CONTROL_POINT);
        //remove old Masks
        Scene.removeWithTag(scene.getEntities(), Entity.TAG_MASK);
        //remove old slices
        Scene.removeWithTag(scene.getEntities(), Entity.TAG_SLICE);
        
        //pass transducers to scene
        scene.addTransducersFromSimulation(simulation);
        //add control points
        scene.getEntities().addAll( simulation.getControlPoints() );
        //add all the masks
        scene.getEntities().addAll( simulation.getMaskObjects() );
        //add all the slices
        scene.getEntities().addAll( simulation.getSlices() );
        
        
        //load holomemory
        holoPatternsForm.setHoloMemory( simulation.getHoloMemory() );
        
        //init visualization helpers (cubes and that)
        scene.initHelperObjects();
        
        adjustGUIGainAndCameras();
        
        needUpdate();
    }

    public void updateBoundaries(){
        //update the boundaries
        simulation.updateSimulationBoundaries();
        simulation.copyToCube( scene.getCubeHelper() );
        scene.updateBoundaryBoxes(simulation);
        
    }
    
    public void adjustGUIGainAndCameras(){
        //set the GUI gain
        domainPanel.setGUIGain( simulation.getMinSize() );
        
        //init camera
        scene.adjustCameraToSimulation(simulation, getGLAspect());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wrapPlayButtonGroup = new javax.swing.ButtonGroup();
        slicesSource = new javax.swing.ButtonGroup();
        preCubeSource = new javax.swing.ButtonGroup();
        maskObjectsGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        panelSlider = sliderPanel;
        mainTabPanel = new javax.swing.JTabbedPane();
        sliderFieldLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        rzText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        rxText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        syText = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        xText = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        szText = new javax.swing.JTextField();
        ryText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        sxText = new javax.swing.JTextField();
        yText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        zText = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        containerPanel = new javax.swing.JPanel();
        panel = gljpanel;
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        loadSimMenu = new javax.swing.JMenuItem();
        saveSimMenu = new javax.swing.JMenuItem();
        exportObjMenu = new javax.swing.JMenuItem();
        exportObjWithMtlMenu = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        simEditParamMenu = new javax.swing.JMenuItem();
        recToSelMenu = new javax.swing.JMenuItem();
        selToBagMenu = new javax.swing.JMenuItem();
        addSelAsBeadMenu = new javax.swing.JMenuItem();
        normSimPosMenu = new javax.swing.JMenuItem();
        simTransformMenu = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        camViewMenu = new javax.swing.JMenuItem();
        camProjMenu = new javax.swing.JMenuItem();
        resetCamMenu = new javax.swing.JMenuItem();
        unlockCameraMenu = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        camLookSelectionMenu = new javax.swing.JMenuItem();
        originCamMenu = new javax.swing.JMenuItem();
        centerCamMenu = new javax.swing.JMenuItem();
        otherCamMenu = new javax.swing.JMenuItem();
        camCoverSelMenu = new javax.swing.JMenuItem();
        cameraMovMenu = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        delTransMenu = new javax.swing.JMenuItem();
        pointToTargetMenu = new javax.swing.JMenuItem();
        transSetPhase0Menu = new javax.swing.JMenuItem();
        transSetPhasePiMenu = new javax.swing.JMenuItem();
        transSetAmp0Menu = new javax.swing.JMenuItem();
        transSetAmp1Menu = new javax.swing.JMenuItem();
        offNextOnTransducerMenu = new javax.swing.JMenuItem();
        transOffsetMenu = new javax.swing.JMenuItem();
        transAssignmentMenu = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        addKeyFrameMenu = new javax.swing.JMenuItem();
        exportToArduinoMenu = new javax.swing.JMenuItem();
        exportNano8Menu = new javax.swing.JMenuItem();
        exportMatlabMenu = new javax.swing.JMenuItem();
        addTransMenu = new javax.swing.JMenu();
        arrayAddMenu = new javax.swing.JMenuItem();
        arrayFromObjMenu = new javax.swing.JMenuItem();
        importArrayMenu = new javax.swing.JMenuItem();
        arrayExportMenu = new javax.swing.JMenuItem();
        forceStudyMenu = new javax.swing.JMenuItem();
        jMenu9 = new javax.swing.JMenu();
        wormGenMenu = new javax.swing.JMenuItem();
        wormMesh = new javax.swing.JMenuItem();
        mouseControlMenu = new javax.swing.JMenuItem();
        phasePatternMenu = new javax.swing.JMenuItem();
        sonoTweezersEmuMenu = new javax.swing.JMenuItem();
        soundCalibDataMenu = new javax.swing.JMenuItem();
        keyboardControlMenu = new javax.swing.JMenuItem();
        tubeGenMenu = new javax.swing.JMenuItem();
        sendToDevicesMenu = new javax.swing.JMenuItem();
        metamaterialsMenu = new javax.swing.JMenuItem();
        polarPlotsMenu = new javax.swing.JMenuItem();
        forcePlotsMenu = new javax.swing.JMenuItem();
        matlabFieldMenu = new javax.swing.JMenuItem();
        ImportAmpPhasesMenu = new javax.swing.JMenuItem();
        hybridSingleBeamMenu = new javax.swing.JMenuItem();
        auxKeyMenu = new javax.swing.JMenuItem();
        scatterObjectMenu = new javax.swing.JMenuItem();
        bowlArrayMenu = new javax.swing.JMenuItem();
        matlabPhasesMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("3D Acoustic SIM");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onExit(evt);
            }
        });

        panelSlider.setBackground(new java.awt.Color(255, 255, 255));
        panelSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelSliderMouseDragged(evt);
            }
        });
        panelSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelSliderMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panelSliderMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout panelSliderLayout = new javax.swing.GroupLayout(panelSlider);
        panelSlider.setLayout(panelSliderLayout);
        panelSliderLayout.setHorizontalGroup(
            panelSliderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelSliderLayout.setVerticalGroup(
            panelSliderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        sliderFieldLabel.setText("MMM");

        rzText.setText("0");
        rzText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                rzTextFocusGained(evt);
            }
        });
        rzText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rzTextActionPerformed(evt);
            }
        });

        jLabel1.setText("X");

        jLabel6.setText("RY");

        rxText.setText("0");
        rxText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                rxTextFocusGained(evt);
            }
        });
        rxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rxTextActionPerformed(evt);
            }
        });

        jLabel5.setText("RZ");

        jLabel2.setText("Y");

        syText.setText("0");
        syText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                syTextFocusGained(evt);
            }
        });
        syText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syTextActionPerformed(evt);
            }
        });

        jLabel21.setText("SZ:");

        xText.setText("0");
        xText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                xTextFocusGained(evt);
            }
        });
        xText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTextActionPerformed(evt);
            }
        });

        jLabel15.setText("SX:");

        szText.setText("0");
        szText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                szTextFocusGained(evt);
            }
        });
        szText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                szTextActionPerformed(evt);
            }
        });

        ryText.setText("0");
        ryText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ryTextFocusGained(evt);
            }
        });
        ryText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ryTextActionPerformed(evt);
            }
        });

        jLabel4.setText("RX");

        sxText.setText("0");
        sxText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sxTextFocusGained(evt);
            }
        });
        sxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sxTextActionPerformed(evt);
            }
        });

        yText.setText("0");
        yText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                yTextFocusGained(evt);
            }
        });
        yText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yTextActionPerformed(evt);
            }
        });

        jLabel3.setText("Z");

        zText.setText("0");
        zText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                zTextFocusGained(evt);
            }
        });
        zText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zTextActionPerformed(evt);
            }
        });

        jLabel16.setText("SY:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(szText, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sxText))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zText))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yText))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xText)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(syText, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rzText))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rxText)
                            .addComponent(ryText))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(xText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(rxText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(yText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6)
                                .addComponent(ryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(zText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(rzText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(sxText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(syText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(szText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabPanel, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sliderFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sliderFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
        );

        containerPanel.setLayout(new java.awt.BorderLayout());

        panel.setBackground(new java.awt.Color(0, 0, 0));
        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelMouseDragged(evt);
            }
        });
        panel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                panelMouseWheelMoved(evt);
            }
        });
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelMousePressed(evt);
            }
        });

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 502, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        containerPanel.add(panel, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        loadSimMenu.setText("Load");
        loadSimMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSimMenuActionPerformed(evt);
            }
        });
        jMenu1.add(loadSimMenu);

        saveSimMenu.setText("Save");
        saveSimMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSimMenuActionPerformed(evt);
            }
        });
        jMenu1.add(saveSimMenu);

        exportObjMenu.setText("Export to obj");
        exportObjMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportObjMenuActionPerformed(evt);
            }
        });
        jMenu1.add(exportObjMenu);

        exportObjWithMtlMenu.setText("ExportToObjWithMtl");
        exportObjWithMtlMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportObjWithMtlMenuActionPerformed(evt);
            }
        });
        jMenu1.add(exportObjWithMtlMenu);

        jMenuBar1.add(jMenu1);

        jMenu5.setText("Simulation");

        simEditParamMenu.setText("Edit Parameters");
        simEditParamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simEditParamMenuActionPerformed(evt);
            }
        });
        jMenu5.add(simEditParamMenu);

        recToSelMenu.setText("Recenter to sel");
        recToSelMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recToSelMenuActionPerformed(evt);
            }
        });
        jMenu5.add(recToSelMenu);

        selToBagMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
        selToBagMenu.setText("Add sel to bag");
        selToBagMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selToBagMenuActionPerformed(evt);
            }
        });
        jMenu5.add(selToBagMenu);

        addSelAsBeadMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        addSelAsBeadMenu.setText("Add bead in selection");
        addSelAsBeadMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelAsBeadMenuActionPerformed(evt);
            }
        });
        jMenu5.add(addSelAsBeadMenu);

        normSimPosMenu.setText("Normalize Sim Pos");
        normSimPosMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                normSimPosMenuActionPerformed(evt);
            }
        });
        jMenu5.add(normSimPosMenu);

        simTransformMenu.setText("Transform");
        simTransformMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simTransformMenuActionPerformed(evt);
            }
        });
        jMenu5.add(simTransformMenu);

        jMenuBar1.add(jMenu5);

        jMenu2.setText("Camera");

        camViewMenu.setText("Edit View");
        camViewMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camViewMenuActionPerformed(evt);
            }
        });
        jMenu2.add(camViewMenu);

        camProjMenu.setText("Edit Projection");
        camProjMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camProjMenuActionPerformed(evt);
            }
        });
        jMenu2.add(camProjMenu);

        resetCamMenu.setText("reset");
        resetCamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCamMenuActionPerformed(evt);
            }
        });
        jMenu2.add(resetCamMenu);

        unlockCameraMenu.setText("Un/Lock cam");
        unlockCameraMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unlockCameraMenuActionPerformed(evt);
            }
        });
        jMenu2.add(unlockCameraMenu);

        jMenu3.setText("Look At");

        camLookSelectionMenu.setText("Selection");
        camLookSelectionMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camLookSelectionMenuActionPerformed(evt);
            }
        });
        jMenu3.add(camLookSelectionMenu);

        originCamMenu.setText("Origin 000");
        originCamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                originCamMenuActionPerformed(evt);
            }
        });
        jMenu3.add(originCamMenu);

        centerCamMenu.setText("Center");
        centerCamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centerCamMenuActionPerformed(evt);
            }
        });
        jMenu3.add(centerCamMenu);

        otherCamMenu.setText("Other");
        otherCamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherCamMenuActionPerformed(evt);
            }
        });
        jMenu3.add(otherCamMenu);

        jMenu2.add(jMenu3);

        camCoverSelMenu.setText("cover sel");
        camCoverSelMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camCoverSelMenuActionPerformed(evt);
            }
        });
        jMenu2.add(camCoverSelMenu);

        cameraMovMenu.setText("CameraMov");
        cameraMovMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraMovMenuActionPerformed(evt);
            }
        });
        jMenu2.add(cameraMovMenu);

        jMenuBar1.add(jMenu2);

        jMenu4.setText("Transducers");

        delTransMenu.setText("Del");
        delTransMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTransMenuActionPerformed(evt);
            }
        });
        jMenu4.add(delTransMenu);

        pointToTargetMenu.setText("PointToTarget");
        pointToTargetMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointToTargetMenuActionPerformed(evt);
            }
        });
        jMenu4.add(pointToTargetMenu);

        transSetPhase0Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0));
        transSetPhase0Menu.setText("Phase=0");
        transSetPhase0Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetPhase0MenuActionPerformed(evt);
            }
        });
        jMenu4.add(transSetPhase0Menu);

        transSetPhasePiMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
        transSetPhasePiMenu.setText("Phase=PI");
        transSetPhasePiMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetPhasePiMenuActionPerformed(evt);
            }
        });
        jMenu4.add(transSetPhasePiMenu);

        transSetAmp0Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
        transSetAmp0Menu.setText("Amp=0");
        transSetAmp0Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetAmp0MenuActionPerformed(evt);
            }
        });
        jMenu4.add(transSetAmp0Menu);

        transSetAmp1Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, 0));
        transSetAmp1Menu.setText("Amp=1");
        transSetAmp1Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetAmp1MenuActionPerformed(evt);
            }
        });
        jMenu4.add(transSetAmp1Menu);

        offNextOnTransducerMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, 0));
        offNextOnTransducerMenu.setText("offNextOn");
        offNextOnTransducerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offNextOnTransducerMenuActionPerformed(evt);
            }
        });
        jMenu4.add(offNextOnTransducerMenu);

        transOffsetMenu.setText("Offsets");
        transOffsetMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transOffsetMenuActionPerformed(evt);
            }
        });
        jMenu4.add(transOffsetMenu);

        transAssignmentMenu.setText("Assignment");
        transAssignmentMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transAssignmentMenuActionPerformed(evt);
            }
        });
        jMenu4.add(transAssignmentMenu);

        jMenuBar1.add(jMenu4);

        jMenu6.setText("Animations");

        addKeyFrameMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        addKeyFrameMenu.setText("AddKeyFrame");
        addKeyFrameMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeyFrameMenuActionPerformed(evt);
            }
        });
        jMenu6.add(addKeyFrameMenu);

        exportToArduinoMenu.setText("Export to ArduinoMega64");
        exportToArduinoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToArduinoMenuActionPerformed(evt);
            }
        });
        jMenu6.add(exportToArduinoMenu);

        exportNano8Menu.setText("Export to Nano8");
        exportNano8Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportNano8MenuActionPerformed(evt);
            }
        });
        jMenu6.add(exportNano8Menu);

        exportMatlabMenu.setText("Export to Matlab");
        exportMatlabMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMatlabMenuActionPerformed(evt);
            }
        });
        jMenu6.add(exportMatlabMenu);

        jMenuBar1.add(jMenu6);

        addTransMenu.setText("Arrays");

        arrayAddMenu.setText("Arrangement");
        arrayAddMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrayAddMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(arrayAddMenu);

        arrayFromObjMenu.setText("From Obj");
        arrayFromObjMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrayFromObjMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(arrayFromObjMenu);

        importArrayMenu.setText("Import");
        importArrayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importArrayMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(importArrayMenu);

        arrayExportMenu.setText("Export");
        arrayExportMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrayExportMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(arrayExportMenu);

        forceStudyMenu.setText("force study");
        forceStudyMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceStudyMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(forceStudyMenu);

        jMenuBar1.add(addTransMenu);

        jMenu9.setText("Utils");

        wormGenMenu.setText("worm gen");
        wormGenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wormGenMenuActionPerformed(evt);
            }
        });
        jMenu9.add(wormGenMenu);

        wormMesh.setText("worm mesh");
        wormMesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wormMeshActionPerformed(evt);
            }
        });
        jMenu9.add(wormMesh);

        mouseControlMenu.setText("mouseControl");
        mouseControlMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mouseControlMenuActionPerformed(evt);
            }
        });
        jMenu9.add(mouseControlMenu);

        phasePatternMenu.setText("HoloPatterns");
        phasePatternMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phasePatternMenuActionPerformed(evt);
            }
        });
        jMenu9.add(phasePatternMenu);

        sonoTweezersEmuMenu.setText("SonoTweezers emulator");
        sonoTweezersEmuMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sonoTweezersEmuMenuActionPerformed(evt);
            }
        });
        jMenu9.add(sonoTweezersEmuMenu);

        soundCalibDataMenu.setText("Gen SoundCalib data");
        soundCalibDataMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundCalibDataMenuActionPerformed(evt);
            }
        });
        jMenu9.add(soundCalibDataMenu);

        keyboardControlMenu.setText("Keyboard Controller");
        keyboardControlMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardControlMenuActionPerformed(evt);
            }
        });
        jMenu9.add(keyboardControlMenu);

        tubeGenMenu.setText("TubeGen");
        tubeGenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tubeGenMenuActionPerformed(evt);
            }
        });
        jMenu9.add(tubeGenMenu);

        sendToDevicesMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        sendToDevicesMenu.setText("Send to Devices");
        sendToDevicesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToDevicesMenuActionPerformed(evt);
            }
        });
        jMenu9.add(sendToDevicesMenu);

        metamaterialsMenu.setText("Metamaterials");
        metamaterialsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metamaterialsMenuActionPerformed(evt);
            }
        });
        jMenu9.add(metamaterialsMenu);

        polarPlotsMenu.setText("PolarPlots");
        polarPlotsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                polarPlotsMenuActionPerformed(evt);
            }
        });
        jMenu9.add(polarPlotsMenu);

        forcePlotsMenu.setText("ForcePlots");
        forcePlotsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forcePlotsMenuActionPerformed(evt);
            }
        });
        jMenu9.add(forcePlotsMenu);

        matlabFieldMenu.setText("MatlabField");
        matlabFieldMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matlabFieldMenuActionPerformed(evt);
            }
        });
        jMenu9.add(matlabFieldMenu);

        ImportAmpPhasesMenu.setText("Import Amp/Phases");
        ImportAmpPhasesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportAmpPhasesMenuActionPerformed(evt);
            }
        });
        jMenu9.add(ImportAmpPhasesMenu);

        hybridSingleBeamMenu.setText("HybridSingleBeams");
        hybridSingleBeamMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hybridSingleBeamMenuActionPerformed(evt);
            }
        });
        jMenu9.add(hybridSingleBeamMenu);

        auxKeyMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        auxKeyMenu.setText("AuxKey");
        auxKeyMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auxKeyMenuActionPerformed(evt);
            }
        });
        jMenu9.add(auxKeyMenu);

        scatterObjectMenu.setText("Scatter Object");
        scatterObjectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scatterObjectMenuActionPerformed(evt);
            }
        });
        jMenu9.add(scatterObjectMenu);

        bowlArrayMenu.setText("Bowl Array");
        bowlArrayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bowlArrayMenuActionPerformed(evt);
            }
        });
        jMenu9.add(bowlArrayMenu);

        matlabPhasesMenu.setText("MatlabPhases");
        matlabPhasesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matlabPhasesMenuActionPerformed(evt);
            }
        });
        jMenu9.add(matlabPhasesMenu);

        jMenuBar1.add(jMenu9);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private int lastButton, lastX, lastY;
    private void panelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMousePressed
        lastButton = evt.getButton();
        lastX = evt.getX();
        lastY = evt.getY();
        
        if(lastButton == 2){
            if (cameraLooked){
                scene.getCamera().activateObservation(true, scene.getCamera().getObservationPoint());
            }
        }else if(lastButton == 1){
            updateSelection(evt);
        }
    }//GEN-LAST:event_panelMousePressed

    private void panelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMouseDragged
        int x = evt.getX();
        int y = evt.getY();
        final float rotGain = 0.01f;
        final float moveGain =  domainPanel.getGUIGain()  * 0.5f;
        float diffX = (x - lastX);
        float diffY = (y - lastY);
        
        if(lastButton == 1){
            
        }else if(lastButton == 2){
            if (cameraLooked){
                scene.getCamera().moveAzimuthAndInclination(-diffX * rotGain, -diffY * rotGain);
                scene.getCamera().updateObservation();
            }else{
                scene.getCamera().getTransform().rotateLocal(-diffY * rotGain, -diffX * rotGain, 0);
            }
        }else if(lastButton == 3){
           scene.getCamera().getTransform().moveLocalSpace(-diffX * moveGain, diffY * moveGain, 0);
        }
        
        needUpdate();
        lastX = x;
        lastY = y;
    }//GEN-LAST:event_panelMouseDragged

    private void panelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_panelMouseWheelMoved
         float wheel = (float)evt.getPreciseWheelRotation();
         final float wheelGain = domainPanel.getGUIGain() * 6f;
         final float value = wheel * wheelGain;
         if (cameraLooked){
            scene.getCamera().setDistance(scene.getCamera().getDistance()+ value);  
            scene.getCamera().updateObservation();
         }else{
             scene.getCamera().getTransform().moveLocalSpace(0, 0, value);
         }
         needUpdate();
    }//GEN-LAST:event_panelMouseWheelMoved

    private void lookCamera(Vector3f v){
        scene.getCamera().setOrtho(false);
        scene.getCamera().updateProjection( getGLAspect());
        scene.getCamera().activateObservation(true, v);
    }
    
    private void originCamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originCamMenuActionPerformed
        lookCamera(Vector3f.ZERO);
    }//GEN-LAST:event_originCamMenuActionPerformed

    private void centerCamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_centerCamMenuActionPerformed
        lookCamera( simulation.getSimulationCenter() );
    }//GEN-LAST:event_centerCamMenuActionPerformed

    private void otherCamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherCamMenuActionPerformed
        String v = DialogUtils.getStringDialog(this, "Vector", "0.00 0.00 0.00");
        if (v != null){
            lookCamera( new Vector3f().parse(v) );
        }
    }//GEN-LAST:event_otherCamMenuActionPerformed

    public float getGLAspect(){
        return panel.getWidth() / (float) panel.getHeight();
    }
    
    private void loadSimMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSimMenuActionPerformed
        String target = FileUtils.selectFile(this, "open", ".xml.gz", null);
        if(target != null){
            loadSimulation(target);
        }
    }//GEN-LAST:event_loadSimMenuActionPerformed

    public void loadSimulation(String target) {
        try {
            simulation = (Simulation) FileUtils.readCompressedObject(new File(target));
            
            simulation.sortAnimations();
            simulation.sortTransducers();
            
            initSimulation();
            clearSelection();
            
            movePanel.snapFirstBead();
  
            needUpdate();
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clearSelection(){
        for(Entity e : selection){
            e.selected = false;
        }
        selection.clear();
    }
    
    public void setSelection(Entity e){
        clearSelection();
        e.selected = true;
        selection.add(e);
    }
        
    private void saveSimMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSimMenuActionPerformed
        String file = FileUtils.selectNonExistingFile(this, ".xml.gz");
        if ( file != null){
            try {
                simForm.guiToObj();
                simulation.labelNumberTransducers();
                simulation.setHoloMemory( holoPatternsForm.getHoloMemory() );
                
                simulation.getMaskObjects().clear();
                scene.gatherMeshEntitiesWithTag( simulation.getMaskObjects(), Entity.TAG_MASK);
                
                simulation.getSlices().clear();
                scene.gatherMeshEntitiesWithTag( simulation.getSlices(), Entity.TAG_SLICE);
                
                FileUtils.writeCompressedObject(new File(file), simulation);
            } catch (IOException ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveSimMenuActionPerformed

    private void delTransMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTransMenuActionPerformed
        ArrayList<Transducer> trans = new ArrayList<>();
        for( Entity e : selection){
            if ( e instanceof Transducer) { trans.add( (Transducer) e ); }
        }
        
        transPanel.deleteTransducers( trans );
        
        clearSelection();
        needUpdate();
    }//GEN-LAST:event_delTransMenuActionPerformed

    private void arrayAddMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrayAddMenuActionPerformed
        addTransducersForm.setVisible(true);
    }//GEN-LAST:event_arrayAddMenuActionPerformed

    private void resetCamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCamMenuActionPerformed
        scene.adjustCameraToSimulation(simulation, getGLAspect());
        needUpdate();
    }//GEN-LAST:event_resetCamMenuActionPerformed

    private void xTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xTextFocusGained
        changeSlider(FieldsToChange.xField, "X", simulation.getMinSize() * 8.0f, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_xTextFocusGained

    private void yTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yTextFocusGained
        changeSlider(FieldsToChange.yField, "Y", simulation.getMinSize() * 8.0f, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_yTextFocusGained

    private void zTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_zTextFocusGained
        changeSlider(FieldsToChange.zField, "Z", simulation.getMinSize() * 8.0f, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_zTextFocusGained

    private void rxTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rxTextFocusGained
        changeSlider(FieldsToChange.rxField, "RX", 360, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_rxTextFocusGained

    private void ryTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ryTextFocusGained
        changeSlider(FieldsToChange.ryField, "RY", 360, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_ryTextFocusGained

    private void rzTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rzTextFocusGained
        changeSlider(FieldsToChange.rzField, "RZ", 360, Float.MIN_VALUE, Float.MAX_VALUE);
    }//GEN-LAST:event_rzTextFocusGained

    private void panelSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelSliderMouseDragged
        float diff = sliderPanel.touchDrag(evt.getX(), evt.getY()); 
        changeSelectionField(sliderField, diff * sliderScale, false, true);
        needUpdate();
    }//GEN-LAST:event_panelSliderMouseDragged

    public void updateTransForField(FieldsToChange field, String text){
        if (text.length() < 1) {return;}
        boolean absolute;
        float value;
        if (text.charAt(0) == 'a'){
            absolute = false;
            value = Parse.toFloat( text.substring(1));
        }else{
            absolute = true;
            value = Parse.toFloat( text );
        }
        changeSelectionField(field, value, absolute, false);
        needUpdate();
    }
    
    private void xTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTextActionPerformed
        updateTransForField(FieldsToChange.xField, xText.getText());
    }//GEN-LAST:event_xTextActionPerformed

    private void rxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rxTextActionPerformed
        updateTransForField(FieldsToChange.rxField, rxText.getText());
    }//GEN-LAST:event_rxTextActionPerformed

    private void yTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yTextActionPerformed
        updateTransForField(FieldsToChange.yField, yText.getText());
    }//GEN-LAST:event_yTextActionPerformed

    private void ryTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ryTextActionPerformed
        updateTransForField(FieldsToChange.ryField, ryText.getText());
    }//GEN-LAST:event_ryTextActionPerformed

    private void zTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zTextActionPerformed
        updateTransForField(FieldsToChange.zField, zText.getText());
    }//GEN-LAST:event_zTextActionPerformed

    private void rzTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rzTextActionPerformed
        updateTransForField(FieldsToChange.rzField, rzText.getText());
    }//GEN-LAST:event_rzTextActionPerformed

    
    private void panelSliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelSliderMousePressed
        sliderPanel.touchDown(evt.getX(), evt.getY(), 0);
    }//GEN-LAST:event_panelSliderMousePressed

    private void panelSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelSliderMouseReleased
        sliderPanel.setShow( false );
        sliderPanel.repaint();
    }//GEN-LAST:event_panelSliderMouseReleased

    private void syTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syTextActionPerformed
        updateTransForField(FieldsToChange.syField, syText.getText());
    }//GEN-LAST:event_syTextActionPerformed

    private void syTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_syTextFocusGained
        changeSlider(FieldsToChange.syField, "SY", simulation.maxDistanceBoundary() / 8.0f, 0.0f, Float.MAX_VALUE);
    }//GEN-LAST:event_syTextFocusGained

    private void sxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sxTextActionPerformed
        updateTransForField(FieldsToChange.sxField, sxText.getText());
    }//GEN-LAST:event_sxTextActionPerformed

    private void sxTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sxTextFocusGained
        changeSlider(FieldsToChange.sxField, "SX", simulation.maxDistanceBoundary() / 8.0f, 0.0f, Float.MAX_VALUE);
    }//GEN-LAST:event_sxTextFocusGained

    private void szTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_szTextFocusGained
        changeSlider(FieldsToChange.szField, "SZ", simulation.maxDistanceBoundary() / 8.0f, 0.0f, Float.MAX_VALUE);
    }//GEN-LAST:event_szTextFocusGained

    private void szTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_szTextActionPerformed
       updateTransForField(FieldsToChange.szField, szText.getText());
    }//GEN-LAST:event_szTextActionPerformed
  
    
    
    public void addMeshEntityToSceneCenter( MeshEntity me){
        me.getTransform().getTranslation().set( simulation.getSimulationCenter() );
        me.getTransform().getScale().set( simulation.maxDistanceBoundary() );
        scene.getEntities().add( me );
    }
    
    private void camProjMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camProjMenuActionPerformed
        /*
        ProjectionForm pf = new ProjectionForm(scene.getCamera().getProjection());
        pf.setLocationRelativeTo(this);
        pf.setVisible(true);
        */
    }//GEN-LAST:event_camProjMenuActionPerformed

    private void camViewMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camViewMenuActionPerformed
        showNewFrame( new TransformForm(scene.getCamera().getTransform(), this) );
    }//GEN-LAST:event_camViewMenuActionPerformed

    private void unlockCameraMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unlockCameraMenuActionPerformed
       cameraLooked = !cameraLooked;
    }//GEN-LAST:event_unlockCameraMenuActionPerformed

    
    private void addKeyFrameMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyFrameMenuActionPerformed
        animPanel.pressAddKeyFrame();
    }//GEN-LAST:event_addKeyFrameMenuActionPerformed

    private void transSetPhase0MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetPhase0MenuActionPerformed
        transPanel.setTransPhase( 0.0f );
    }//GEN-LAST:event_transSetPhase0MenuActionPerformed

    private void transSetPhasePiMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetPhasePiMenuActionPerformed
        transPanel.setTransPhase( 1.0f );
    }//GEN-LAST:event_transSetPhasePiMenuActionPerformed

    private void transSetAmp0MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetAmp0MenuActionPerformed
        transPanel.setTransAmp( 0.0f );
    }//GEN-LAST:event_transSetAmp0MenuActionPerformed

    private void transSetAmp1MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetAmp1MenuActionPerformed
        transPanel.setTransAmp( 1.0f );
    }//GEN-LAST:event_transSetAmp1MenuActionPerformed

    private void camLookSelectionMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camLookSelectionMenuActionPerformed
        if(! selection.isEmpty() ){
            lookCamera( selection.get(0).getTransform().getTranslation() );
        }
    }//GEN-LAST:event_camLookSelectionMenuActionPerformed

    private void pointToTargetMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointToTargetMenuActionPerformed
        if (! selection.isEmpty()){
            final Vector3f target = selection.get(0).getTransform().getTranslation();
            for(Transducer t : simulation.getTransducers()){
                t.pointToTarget(target);
            }
        }
        needUpdate();
    }//GEN-LAST:event_pointToTargetMenuActionPerformed

    private void offNextOnTransducerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offNextOnTransducerMenuActionPerformed
        transControlPanel.offNextOnTransducerMenuActionPerformed();
    }//GEN-LAST:event_offNextOnTransducerMenuActionPerformed

    private void exportObjMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportObjMenuActionPerformed
        SceneObjExport soe = new SceneObjExport(this);
        soe.export( false );
    }//GEN-LAST:event_exportObjMenuActionPerformed

    private void camCoverSelMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camCoverSelMenuActionPerformed
        if (! selection.isEmpty()){
            scene.adjustCameraToCover( selection.get(0) );
            needUpdate();
        }
    }//GEN-LAST:event_camCoverSelMenuActionPerformed

    private void phasePatternMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phasePatternMenuActionPerformed
       holoPatternsForm.setVisible( true );
    }//GEN-LAST:event_phasePatternMenuActionPerformed

    private void cameraMovMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraMovMenuActionPerformed
        showNewFrame( new CameraMoveFrame(this) );
    }//GEN-LAST:event_cameraMovMenuActionPerformed

    private void exportObjWithMtlMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportObjWithMtlMenuActionPerformed
        SceneObjExport soe = new SceneObjExport(this);
        soe.export( true );
    }//GEN-LAST:event_exportObjWithMtlMenuActionPerformed

    private void addSelAsBeadMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelAsBeadMenuActionPerformed
        cpPanel.addSelAsBead();
    }//GEN-LAST:event_addSelAsBeadMenuActionPerformed

    private void mouseControlMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mouseControlMenuActionPerformed
        mouseControlForm = new MouseControlForm(this);
        mouseControlForm.setLocationRelativeTo(this);
        mouseControlForm.setVisible(true);
    }//GEN-LAST:event_mouseControlMenuActionPerformed

    private void recToSelMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recToSelMenuActionPerformed
         if (selection.size() != 1){
            return;
        }
        scene.recenterTo(scene.getEntities(), selection.get(0).getTransform().getTranslation().clone());
        needUpdate();
    }//GEN-LAST:event_recToSelMenuActionPerformed

    private void arrayFromObjMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrayFromObjMenuActionPerformed
        showNewFrame( new ImportTransducersFromObjForm(this) );
    }//GEN-LAST:event_arrayFromObjMenuActionPerformed

    private void importArrayMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importArrayMenuActionPerformed
        addTransducersForm.importArray();
    }//GEN-LAST:event_importArrayMenuActionPerformed

    private void arrayExportMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrayExportMenuActionPerformed
        addTransducersForm.exportArray();
    }//GEN-LAST:event_arrayExportMenuActionPerformed

    private void simEditParamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simEditParamMenuActionPerformed
        simForm.setVisible( true );
    }//GEN-LAST:event_simEditParamMenuActionPerformed

    private void sonoTweezersEmuMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sonoTweezersEmuMenuActionPerformed
        showNewFrame( new SonoTweezersEmulatorForm(this) );
    }//GEN-LAST:event_sonoTweezersEmuMenuActionPerformed

    private void selToBagMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selToBagMenuActionPerformed
        bag.clear();
        bag.addAll( selection );
    }//GEN-LAST:event_selToBagMenuActionPerformed

    private void transOffsetMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transOffsetMenuActionPerformed
        showNewFrame( new TransducersOffsetForm(this) );
    }//GEN-LAST:event_transOffsetMenuActionPerformed

    private void soundCalibDataMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundCalibDataMenuActionPerformed
        showNewFrame( new SoundCalibDataForm(this) );
    }//GEN-LAST:event_soundCalibDataMenuActionPerformed

    private void normSimPosMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normSimPosMenuActionPerformed
        final Transform t = scene.uniformPositions( selection );
        if (t != null){
            TextFrame.showText("Transform", t.toString(), this);
            needUpdate();
        }
    }//GEN-LAST:event_normSimPosMenuActionPerformed

    private void simTransformMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simTransformMenuActionPerformed
        ApplyTransformForm atf = new ApplyTransformForm(this);
        atf.setLocationRelativeTo(this);
        atf.setVisible(true);
    }//GEN-LAST:event_simTransformMenuActionPerformed

    private void onExit(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onExit
        try {
            //snap config
            final File f =  FileUtils.getLastChooserPath();
            if (f != null){
                config.lastPath = f.toString();
            }
            
            //try to save config
            FileUtils.writeObject(new File(CONFIG_PATH), config);
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.exit(0);
    }//GEN-LAST:event_onExit

    private void keyboardControlMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardControlMenuActionPerformed
        showNewFrame( new KeyboardControllerForm(this) );
    }//GEN-LAST:event_keyboardControlMenuActionPerformed

    private void tubeGenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tubeGenMenuActionPerformed
        showNewFrame( new TubeGen(this) );
    }//GEN-LAST:event_tubeGenMenuActionPerformed

    private void sendToDevicesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToDevicesMenuActionPerformed
        transControlPanel.sendPattern();
    }//GEN-LAST:event_sendToDevicesMenuActionPerformed

    private void metamaterialsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metamaterialsMenuActionPerformed
        showNewFrame( new MetaSurfaces(this) );
    }//GEN-LAST:event_metamaterialsMenuActionPerformed

    private void polarPlotsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_polarPlotsMenuActionPerformed
       showNewFrame( new ExportPlotsForm(this) );
    }//GEN-LAST:event_polarPlotsMenuActionPerformed

    private void transAssignmentMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transAssignmentMenuActionPerformed
       showNewFrame( new AssignTransducers(this) );
    }//GEN-LAST:event_transAssignmentMenuActionPerformed

    private void exportToArduinoMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToArduinoMenuActionPerformed
        ArduinoMEGA64.exportAnimation( this );
    }//GEN-LAST:event_exportToArduinoMenuActionPerformed

    private void exportMatlabMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMatlabMenuActionPerformed
        ArduinoMEGA64_Anim.exportAnimationMatlab( this );
    }//GEN-LAST:event_exportMatlabMenuActionPerformed

    private void forcePlotsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forcePlotsMenuActionPerformed
        showNewFrame( new ForcePlotsFrame( this ) );
    }//GEN-LAST:event_forcePlotsMenuActionPerformed

    private void matlabFieldMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matlabFieldMenuActionPerformed
        addTransducersForm.exportTransducersMatlab();
    }//GEN-LAST:event_matlabFieldMenuActionPerformed

    private void ImportAmpPhasesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportAmpPhasesMenuActionPerformed
        showNewFrame( new ImportPhasesAmpForm(this) );
        
    }//GEN-LAST:event_ImportAmpPhasesMenuActionPerformed

    private void hybridSingleBeamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hybridSingleBeamMenuActionPerformed
        showNewFrame( new HybridSingleBeamForm(this));
    }//GEN-LAST:event_hybridSingleBeamMenuActionPerformed

    private void forceStudyMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forceStudyMenuActionPerformed
        showNewFrame( new SweepTinyLevParameters(this));
    }//GEN-LAST:event_forceStudyMenuActionPerformed

    private void auxKeyMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auxKeyMenuActionPerformed
        
    }//GEN-LAST:event_auxKeyMenuActionPerformed

    private void wormGenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wormGenMenuActionPerformed
        showNewFrame( new WormholesGenerateTubes(this) );
    }//GEN-LAST:event_wormGenMenuActionPerformed

    private void wormMeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wormMeshActionPerformed
        showNewFrame( new WormholesGenerateMesh(this) );
    }//GEN-LAST:event_wormMeshActionPerformed

    private void exportNano8MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportNano8MenuActionPerformed
        ArduinoNano.exportAnimation( this );
    }//GEN-LAST:event_exportNano8MenuActionPerformed

    private void scatterObjectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scatterObjectMenuActionPerformed
        showNewFrame( new ScatterObjectForm(this) );
    }//GEN-LAST:event_scatterObjectMenuActionPerformed

    private void bowlArrayMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bowlArrayMenuActionPerformed
        showNewFrame( new AddRadialTransducersForm(this));
    }//GEN-LAST:event_bowlArrayMenuActionPerformed

    private void matlabPhasesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matlabPhasesMenuActionPerformed
        showNewFrame( new ImportExportPhasesMatlabForm(this));
    }//GEN-LAST:event_matlabPhasesMenuActionPerformed
 
    private void showNewFrame(final JFrame frame){
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem ImportAmpPhasesMenu;
    private javax.swing.JMenuItem addKeyFrameMenu;
    private javax.swing.JMenuItem addSelAsBeadMenu;
    private javax.swing.JMenu addTransMenu;
    private javax.swing.JMenuItem arrayAddMenu;
    private javax.swing.JMenuItem arrayExportMenu;
    private javax.swing.JMenuItem arrayFromObjMenu;
    private javax.swing.JMenuItem auxKeyMenu;
    private javax.swing.JMenuItem bowlArrayMenu;
    private javax.swing.JMenuItem camCoverSelMenu;
    private javax.swing.JMenuItem camLookSelectionMenu;
    private javax.swing.JMenuItem camProjMenu;
    private javax.swing.JMenuItem camViewMenu;
    private javax.swing.JMenuItem cameraMovMenu;
    private javax.swing.JMenuItem centerCamMenu;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JMenuItem delTransMenu;
    private javax.swing.JMenuItem exportMatlabMenu;
    private javax.swing.JMenuItem exportNano8Menu;
    private javax.swing.JMenuItem exportObjMenu;
    private javax.swing.JMenuItem exportObjWithMtlMenu;
    private javax.swing.JMenuItem exportToArduinoMenu;
    private javax.swing.JMenuItem forcePlotsMenu;
    private javax.swing.JMenuItem forceStudyMenu;
    private javax.swing.JMenuItem hybridSingleBeamMenu;
    private javax.swing.JMenuItem importArrayMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JMenuItem keyboardControlMenu;
    private javax.swing.JMenuItem loadSimMenu;
    private javax.swing.JTabbedPane mainTabPanel;
    private javax.swing.ButtonGroup maskObjectsGroup;
    private javax.swing.JMenuItem matlabFieldMenu;
    private javax.swing.JMenuItem matlabPhasesMenu;
    private javax.swing.JMenuItem metamaterialsMenu;
    private javax.swing.JMenuItem mouseControlMenu;
    private javax.swing.JMenuItem normSimPosMenu;
    private javax.swing.JMenuItem offNextOnTransducerMenu;
    private javax.swing.JMenuItem originCamMenu;
    private javax.swing.JMenuItem otherCamMenu;
    private javax.swing.JPanel panel;
    private javax.swing.JPanel panelSlider;
    private javax.swing.JMenuItem phasePatternMenu;
    private javax.swing.JMenuItem pointToTargetMenu;
    private javax.swing.JMenuItem polarPlotsMenu;
    private javax.swing.ButtonGroup preCubeSource;
    private javax.swing.JMenuItem recToSelMenu;
    private javax.swing.JMenuItem resetCamMenu;
    private javax.swing.JTextField rxText;
    private javax.swing.JTextField ryText;
    private javax.swing.JTextField rzText;
    private javax.swing.JMenuItem saveSimMenu;
    private javax.swing.JMenuItem scatterObjectMenu;
    private javax.swing.JMenuItem selToBagMenu;
    private javax.swing.JMenuItem sendToDevicesMenu;
    private javax.swing.JMenuItem simEditParamMenu;
    private javax.swing.JMenuItem simTransformMenu;
    private javax.swing.ButtonGroup slicesSource;
    private javax.swing.JLabel sliderFieldLabel;
    private javax.swing.JMenuItem sonoTweezersEmuMenu;
    private javax.swing.JMenuItem soundCalibDataMenu;
    private javax.swing.JTextField sxText;
    private javax.swing.JTextField syText;
    private javax.swing.JTextField szText;
    private javax.swing.JMenuItem transAssignmentMenu;
    private javax.swing.JMenuItem transOffsetMenu;
    private javax.swing.JMenuItem transSetAmp0Menu;
    private javax.swing.JMenuItem transSetAmp1Menu;
    private javax.swing.JMenuItem transSetPhase0Menu;
    private javax.swing.JMenuItem transSetPhasePiMenu;
    private javax.swing.JMenuItem tubeGenMenu;
    private javax.swing.JMenuItem unlockCameraMenu;
    private javax.swing.JMenuItem wormGenMenu;
    private javax.swing.JMenuItem wormMesh;
    private javax.swing.ButtonGroup wrapPlayButtonGroup;
    private javax.swing.JTextField xText;
    private javax.swing.JTextField yText;
    private javax.swing.JTextField zText;
    // End of variables declaration//GEN-END:variables

    public void needUpdate() {
        panel.repaint();
    }
    
    public MeshEntity getEntityWithClick(final int x, final int y){
         return scene.pickObject(
                    x / (float) panel.getWidth(),
                    1.0f - y / (float) panel.getHeight(), Entity.TAG_SLICE);
    }

    public Vector3f getClickOnObject(final MeshEntity e,final int x,final int y){
        return scene.clickToObject(x / (float) panel.getWidth(), 1.0f - y / (float) panel.getHeight(), e);      
    }
    
    private void updateSelection(MouseEvent evt) {
        final int x = evt.getX(); 
        final int y = evt.getY();
        int tags = Entity.TAG_NONE;
        
        final Component comp = mainTabPanel.getSelectedComponent();
        
        if (comp == transPanel || comp == transControlPanel){
            tags |= Entity.TAG_TRANSDUCER;
        }else if (comp == rtSlicePanel ){
            tags |= Entity.TAG_SLICE;
        }else if (comp == miscPanel){
            tags |= Entity.TAG_MASK;
        }else if (comp == domainPanel){
            tags |= Entity.TAG_CUBE_HELPER;
        }else if (comp == cpPanel){
            
            if ( cpPanel.isClickAndPlace()){
                
                MeshEntity e = getEntityWithClick(x, y);
                if (e != null) {
                    final Vector3f col = getClickOnObject(e, x, y);
                    cpPanel.addControlPoint(col.x, col.y, col.z, 0, -1, false);
                    needUpdate();
                }
                return;
            }
            tags |= Entity.TAG_CONTROL_POINT | Entity.TAG_BEAD;
        }else if (comp == trapsPanel){
            MeshEntity e = getEntityWithClick(x, y);
            if (e != null) {
                final Vector3f worldPos = getClickOnObject(e, x, y);
                trapsPanel.clickAt(worldPos);
            }                           
        }else if (comp == movePanel){
            tags |= Entity.TAG_CONTROL_POINT | Entity.TAG_BEAD;
        }
        
        Entity e = scene.pickObject(
                lastX / (float) panel.getWidth(),
                1.0f - lastY / (float) panel.getHeight(), tags);
        if ( e == null ){
            clearSelection();
            needUpdate();
            return;
        }

        
        if ((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
           if(selection.contains(e)){
                selection.remove(e);
                e.selected = false;
            }else{
                e.selected = true;
                selection.add(e);
                entityToGUI(e); 
            }
        } else {
            clearSelection();
            e.selected = true;
            selection.add(e);
            entityToGUI(e);
        }

        needUpdate();
    }

    private void vectorToTextFields(final Vector3f v, JTextField x, JTextField y, JTextField z){
        x.setText( formats.dc4( v.x ) );
        y.setText( formats.dc4( v.y ) );
        z.setText( formats.dc4( v.z ) );   
    }
    
    public void transformToGUI(final Transform t){
       vectorToTextFields(t.getTranslation(), xText, yText, zText );

       final Vector3f angles = t.getRotation().toAngles(null).multLocal( M.RAD_TO_DEG );
       vectorToTextFields(angles, rxText, ryText, rzText );

       vectorToTextFields(t.getScale(), sxText, syText, szText );
    }
    
    
    private void entityToGUI(Entity e) {
        transformToGUI(e.getTransform());

        if (e instanceof Transducer) {
            Transducer t = (Transducer) e;
            transPanel.getwText().setText(formats.dc4(t.getApperture()));
            transPanel.getPowerText().setText(formats.dc4(t.getPower()));
            transPanel.getFrText().setText(formats.dc4(t.getFrequency()));

            transPanel.getAmpText().setText(formats.dc4(t.getAmplitude()));
            transPanel.getPhaseText().setText(formats.dc4(t.getPhase()));
            transPanel.getLabelText().setText(t.getOrderNumber() + "");
            transPanel.getPinText().setText(t.getDriverPinNumber() + "");
        }

    }

    public enum FieldsToChange{
        xField, yField, zField, rxField, ryField, rzField,
        wField, frField,
        ampField, phaseField,
        sxField, syField, szField,
        powerField
    };
    private FieldsToChange sliderField;
    private float sliderMin, sliderMax, sliderScale;
    public void changeSlider(FieldsToChange field, String name, float scale, float min, float max){
        sliderField = field;
        sliderFieldLabel.setText(name);
        sliderMin = min;
        sliderMax = max;
        sliderScale = scale;
    }
    
    private void changeSelectionField(FieldsToChange field, float value, boolean absolute, boolean updateTextField){
        final Vector3f angles = new Vector3f();
        
        for(Entity e : selection){
            Transform tra = e.getTransform();
            
            if(field == FieldsToChange.xField){
                tra.getTranslation().x = absolute ? value : tra.getTranslation().x + value;
                if (updateTextField) { xText.setText( formats.dc4(tra.getTranslation().x)); }
            }else if(field == FieldsToChange.yField){
                tra.getTranslation().y = absolute ? value : tra.getTranslation().y + value;
                if (updateTextField) { yText.setText( formats.dc4(tra.getTranslation().y)); }
            }else if(field == FieldsToChange.zField){
                tra.getTranslation().z = absolute ? value : tra.getTranslation().z + value;
                if (updateTextField) { zText.setText( formats.dc4(tra.getTranslation().z)); }
            }else if(field == FieldsToChange.sxField){
                tra.getScale().x = absolute ? value : tra.getScale().x + value;
                if (updateTextField) { sxText.setText( formats.dc4( tra.getScale().x )); }
            }else if(field == FieldsToChange.syField){
                 tra.getScale().y = absolute ? value : tra.getScale().y + value;
                if (updateTextField) { syText.setText( formats.dc4( tra.getScale().y )); }
            }else if(field == FieldsToChange.szField){
                 tra.getScale().z = absolute ? value : tra.getScale().z + value;
                if (updateTextField) { szText.setText( formats.dc4( tra.getScale().z )); }
            }else if(field == FieldsToChange.rxField || 
                    field == FieldsToChange.ryField || 
                    field == FieldsToChange.rzField){
                float rads = value * M.DEG_TO_RAD;
                Quaternion q = tra.getRotation();
                q.toAngles(angles);
                if(field == FieldsToChange.rxField) {
                    angles.x = absolute ? rads : angles.x + rads;
                    if (updateTextField) { rxText.setText(formats.dc4(angles.x * M.RAD_TO_DEG)); }
                }else if (field == FieldsToChange.ryField) {
                    angles.y = absolute ? rads : angles.y + rads;
                    if (updateTextField) { ryText.setText(formats.dc4(angles.y * M.RAD_TO_DEG)); }
                }else if (field == FieldsToChange.rzField) {
                    angles.z = absolute ? rads : angles.z + rads;
                    if (updateTextField) { rzText.setText(formats.dc4(angles.z * M.RAD_TO_DEG)); }
                }
                q.fromAngles(angles);
            }
            
            if (e instanceof Transducer){
                Transducer t = (Transducer)e;
                if(field == FieldsToChange.wField){
                    t.apperture = absolute ? value : t.apperture + value;
                    if (updateTextField) { transPanel.getwText().setText( formats.dc4(t.apperture )); }
                }else if(field == FieldsToChange.frField){
                    t.frequency = absolute ? value : t.frequency + value;
                    if (updateTextField) {transPanel.getFrText().setText( formats.dc4( t.frequency )); }
                }else if(field == FieldsToChange.ampField){
                    t.amplitude = absolute ? value : t.amplitude + value;
                    if (updateTextField) {transPanel.getAmpText().setText( formats.dc4(t.amplitude )); }
                }else if(field == FieldsToChange.phaseField){
                    t.phase = absolute ? value : t.phase + value;
                    if (updateTextField) { transPanel.getPhaseText().setText( formats.dc4( t.phase )); }
                }else if(field == FieldsToChange.powerField){
                    t.power = absolute ? value : t.power + value;
                    if (updateTextField) { transPanel.getPowerText().setText( formats.dc4( t.power )); }
                }
            }
            
            updateTextField = false; //only use the first transducer, only one value can be displayed in the text field
        }
    }
    
    public Scene getScene() {
        return scene;
    }

    public ArrayList<Entity> getSelection() {
        return selection;
    }
       
    public BufferedImage captureScreen(){
        BufferedImage bi = new BufferedImage(gljpanel.getWidth(), gljpanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        gljpanel.paint( bi.getGraphics() );
        return bi;
    }
    
    
}
