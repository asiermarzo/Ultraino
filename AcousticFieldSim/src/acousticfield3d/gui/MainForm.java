package acousticfield3d.gui;


import acousticfield3d.Config;
import acousticfield3d.gui.misc.BowlsForm;
import acousticfield3d.gui.misc.AlgorithmsForm;
import acousticfield3d.gui.misc.AmpLinePlot;
import acousticfield3d.gui.misc.ExportPlotsForm;
import acousticfield3d.gui.misc.ForcePlotsFrame;
import acousticfield3d.gui.misc.ImportExportPhasesMatlabForm;
import acousticfield3d.gui.misc.ImportPhasesAmpForm;
import acousticfield3d.gui.misc.RandPointsExpFrame;
import acousticfield3d.gui.misc.GenerateComplexAnimations;
import acousticfield3d.gui.misc.MoveOnTimerForm;
import acousticfield3d.gui.misc.ScatterObjectForm;
import acousticfield3d.gui.misc.ShapePointsFrame;
import acousticfield3d.gui.misc.SliceExperiments;
import acousticfield3d.gui.misc.SliderPanel;
import acousticfield3d.gui.misc.StructuralStiffnessForm;
import acousticfield3d.gui.misc.SwitchTimer;
import acousticfield3d.gui.misc.UDPRemoteControl;
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
import acousticfield3d.math.Vector3f;
import acousticfield3d.protocols.ArduinoMEGA64;
import acousticfield3d.protocols.ArduinoNano;
import acousticfield3d.protocols.SimpleFPGA_128;
import acousticfield3d.renderer.Renderer;
import acousticfield3d.scene.BehavioursThread;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.scene.Scene;
import acousticfield3d.scene.SceneObjExport;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Parse;
import acousticfield3d.utils.SimpleGUIPersistence;
import acousticfield3d.utils.StringFormats;

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
    
    public final ArrayList<Entity> selection =  new ArrayList<>();
    public final ArrayList<Entity> bag = new ArrayList<>();
    boolean cameraLooked;
    boolean hasDragged;
    int firstDragX, firstDragY;
    String lastSimulationSavedOrLoaded;
    
    public final Renderer renderer;
    public final Scene scene;
    public Simulation simulation;
    
    final BehavioursThread animationThread;
    
    final SliderPanel sliderPanel;
    public final GLJPanel gljpanel;
    
    public final RtSlicePanel rtSlicePanel;
    public final AnimPanel animPanel;
    public final MiscPanel miscPanel;
    public final ControlPointPanel pointsPanel;
    public final DomainPanel domainPanel;
    public final TransducersPanel transducersPanel;
    public final TransControlPanel transControlPanel;
    public final MovePanel movePanel;
    public final TrapsPanel trapsPanel;
    
    public final HoloPatternsForm holoPatternsForm;
    public final AddTransducersForm addTransducersForm;
    public final SimulationConfigForm simForm;
    public final AlgorithmsForm algForm;
    
    public final Config config;
    
    public MainForm(Config config) {
        this.config = config;
 
        sliderPanel = new SliderPanel(1, true);
        
        cameraLooked = true;
         
        simulation = new Simulation();
        scene = new Scene();
        miscPanel = new MiscPanel(this);
        renderer = new Renderer(scene, this);
        
        rtSlicePanel = new RtSlicePanel(this);
        animPanel = new AnimPanel(this);
        pointsPanel = new ControlPointPanel(this);
        domainPanel = new DomainPanel(this);
        transducersPanel = new TransducersPanel(this);
        transControlPanel = new TransControlPanel(this);
        movePanel = new MovePanel(this);
        trapsPanel = new TrapsPanel(this);
        
        holoPatternsForm = new HoloPatternsForm(this);
        simForm = new SimulationConfigForm(this);
        addTransducersForm = new AddTransducersForm(this, simulation, scene);
        algForm = new AlgorithmsForm(this);
      
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
        mainTabPanel.addTab("Trans", transducersPanel);
        mainTabPanel.addTab("Slices", rtSlicePanel);
        mainTabPanel.addTab("Anim", animPanel);
        mainTabPanel.addTab("Misc", miscPanel);
        mainTabPanel.addTab("Points", pointsPanel);
        mainTabPanel.addTab("Sizes", domainPanel);
        mainTabPanel.addTab("Devs", transControlPanel);
        mainTabPanel.addTab("Move", movePanel);
        mainTabPanel.addTab("Traps", trapsPanel);
        
        initSimulation();
        
        animationThread = new BehavioursThread(scene, this);
    }
    
    public void init(){
        //animationThread.start();
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
        
        movePanel.snapParticlesPosition();
        
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
        saveSameFileMenu = new javax.swing.JMenuItem();
        loadLastMenu = new javax.swing.JMenuItem();
        importTransMenu = new javax.swing.JMenuItem();
        exportObjMenu = new javax.swing.JMenuItem();
        exportObjWithMtlMenu = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        simEditParamMenu = new javax.swing.JMenuItem();
        simulationResizeMenu = new javax.swing.JMenuItem();
        recToSelMenu = new javax.swing.JMenuItem();
        selToBagMenu = new javax.swing.JMenuItem();
        simTransformMenu = new javax.swing.JMenuItem();
        assignSel2Menu = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        sel1Menu = new javax.swing.JMenuItem();
        sel2Menu = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        zoomInMenu = new javax.swing.JMenuItem();
        zoomOutMenu = new javax.swing.JMenuItem();
        camViewMenu = new javax.swing.JMenuItem();
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
        selectAllTransducersMenu = new javax.swing.JMenuItem();
        delTransMenu = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        transSetPhase0Menu = new javax.swing.JMenuItem();
        transSetPhasePiMenu = new javax.swing.JMenuItem();
        transSetAmp0Menu = new javax.swing.JMenuItem();
        transSetAmp1Menu = new javax.swing.JMenuItem();
        offNextOnTransducerMenu = new javax.swing.JMenuItem();
        selectTransTopMenu = new javax.swing.JMenuItem();
        selectTransTopMenu1 = new javax.swing.JMenuItem();
        phaseUpMenu = new javax.swing.JMenuItem();
        phaseDownMenu = new javax.swing.JMenuItem();
        phasePiMenu = new javax.swing.JMenuItem();
        randomTransducerOffMenu = new javax.swing.JMenuItem();
        randomTransducerOnMenu = new javax.swing.JMenuItem();
        pointToTargetMenu = new javax.swing.JMenuItem();
        transOffsetMenu = new javax.swing.JMenuItem();
        transAssignmentMenu = new javax.swing.JMenuItem();
        PointsMenu = new javax.swing.JMenu();
        selectAllPointsMenu = new javax.swing.JMenuItem();
        mergePointsMenu = new javax.swing.JMenuItem();
        pointsShapeMenu = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        addKeyFrameMenu = new javax.swing.JMenuItem();
        exportToArduinoMenu = new javax.swing.JMenuItem();
        exportNano8Menu = new javax.swing.JMenuItem();
        exportMatlabMenu = new javax.swing.JMenuItem();
        animExportRawMenu = new javax.swing.JMenuItem();
        animImportRawMenu = new javax.swing.JMenuItem();
        cloneSelectedMenu = new javax.swing.JMenuItem();
        interpolateAnimMenu = new javax.swing.JMenuItem();
        addTransMenu = new javax.swing.JMenu();
        arrayAddMenu = new javax.swing.JMenuItem();
        arrayFromObjMenu = new javax.swing.JMenuItem();
        importArrayMenu = new javax.swing.JMenuItem();
        arrayExportMenu = new javax.swing.JMenuItem();
        optimizerMenu = new javax.swing.JMenuItem();
        jMenu9 = new javax.swing.JMenu();
        phasePatternMenu = new javax.swing.JMenuItem();
        sendToDevicesMenu = new javax.swing.JMenuItem();
        polarPlotsMenu = new javax.swing.JMenuItem();
        forcePlotsMenu = new javax.swing.JMenuItem();
        AmpLinePlotMenu = new javax.swing.JMenuItem();
        matlabFieldMenu = new javax.swing.JMenuItem();
        ImportAmpPhasesMenu = new javax.swing.JMenuItem();
        auxKeyMenu = new javax.swing.JMenuItem();
        matlabPhasesMenu = new javax.swing.JMenuItem();
        udpControlMenu = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        scatterObjectMenu = new javax.swing.JMenuItem();
        bowlArrayMenu = new javax.swing.JMenuItem();
        sendSwitchbufMenu = new javax.swing.JMenuItem();
        rotateMultipleMenu = new javax.swing.JMenuItem();
        exportPhasesMenu = new javax.swing.JMenuItem();
        exportTransPhasePointsMenu = new javax.swing.JMenuItem();
        sliceExpMenu = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        structuralStiffnessMenu = new javax.swing.JMenuItem();
        moveOnTimerMenu = new javax.swing.JMenuItem();

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addComponent(mainTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
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
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panelMouseReleased(evt);
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

        saveSimMenu.setText("Save as");
        saveSimMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSimMenuActionPerformed(evt);
            }
        });
        jMenu1.add(saveSimMenu);

        saveSameFileMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveSameFileMenu.setText("Save");
        saveSameFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSameFileMenuActionPerformed(evt);
            }
        });
        jMenu1.add(saveSameFileMenu);

        loadLastMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        loadLastMenu.setText("Load last");
        loadLastMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadLastMenuActionPerformed(evt);
            }
        });
        jMenu1.add(loadLastMenu);

        importTransMenu.setText("Import trans");
        importTransMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importTransMenuActionPerformed(evt);
            }
        });
        jMenu1.add(importTransMenu);

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

        simulationResizeMenu.setText("Resize");
        simulationResizeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationResizeMenuActionPerformed(evt);
            }
        });
        jMenu5.add(simulationResizeMenu);

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

        simTransformMenu.setText("Transform All");
        simTransformMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simTransformMenuActionPerformed(evt);
            }
        });
        jMenu5.add(simTransformMenu);

        assignSel2Menu.setText("assign sel 1");
        assignSel2Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignSel2MenuActionPerformed(evt);
            }
        });
        jMenu5.add(assignSel2Menu);

        jMenuItem2.setText("assign sel 2");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem2);

        sel1Menu.setText("sel 1");
        sel1Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sel1MenuActionPerformed(evt);
            }
        });
        jMenu5.add(sel1Menu);

        sel2Menu.setText("sel 2");
        sel2Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sel2MenuActionPerformed(evt);
            }
        });
        jMenu5.add(sel2Menu);

        jMenuBar1.add(jMenu5);

        jMenu2.setText("Camera");

        zoomInMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, 0));
        zoomInMenu.setText("Zoom in");
        zoomInMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInMenuActionPerformed(evt);
            }
        });
        jMenu2.add(zoomInMenu);

        zoomOutMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, 0));
        zoomOutMenu.setText("Zoom out");
        zoomOutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutMenuActionPerformed(evt);
            }
        });
        jMenu2.add(zoomOutMenu);

        camViewMenu.setText("Edit View");
        camViewMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camViewMenuActionPerformed(evt);
            }
        });
        jMenu2.add(camViewMenu);

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

        selectAllTransducersMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        selectAllTransducersMenu.setText("Select all");
        selectAllTransducersMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllTransducersMenuActionPerformed(evt);
            }
        });
        jMenu4.add(selectAllTransducersMenu);

        delTransMenu.setText("Delete");
        delTransMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTransMenuActionPerformed(evt);
            }
        });
        jMenu4.add(delTransMenu);

        jMenu8.setText("shortcuts");

        transSetPhase0Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0));
        transSetPhase0Menu.setText("Phase=0");
        transSetPhase0Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetPhase0MenuActionPerformed(evt);
            }
        });
        jMenu8.add(transSetPhase0Menu);

        transSetPhasePiMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
        transSetPhasePiMenu.setText("Phase=PI");
        transSetPhasePiMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetPhasePiMenuActionPerformed(evt);
            }
        });
        jMenu8.add(transSetPhasePiMenu);

        transSetAmp0Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
        transSetAmp0Menu.setText("Amp=0");
        transSetAmp0Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetAmp0MenuActionPerformed(evt);
            }
        });
        jMenu8.add(transSetAmp0Menu);

        transSetAmp1Menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, 0));
        transSetAmp1Menu.setText("Amp=1");
        transSetAmp1Menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transSetAmp1MenuActionPerformed(evt);
            }
        });
        jMenu8.add(transSetAmp1Menu);

        offNextOnTransducerMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, 0));
        offNextOnTransducerMenu.setText("offNextOn");
        offNextOnTransducerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offNextOnTransducerMenuActionPerformed(evt);
            }
        });
        jMenu8.add(offNextOnTransducerMenu);

        selectTransTopMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, 0));
        selectTransTopMenu.setText("Select top");
        selectTransTopMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTransTopMenuActionPerformed(evt);
            }
        });
        jMenu8.add(selectTransTopMenu);

        selectTransTopMenu1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0));
        selectTransTopMenu1.setText("Select bottom");
        selectTransTopMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTransTopMenu1ActionPerformed(evt);
            }
        });
        jMenu8.add(selectTransTopMenu1);

        phaseUpMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, 0));
        phaseUpMenu.setText("phaseUp");
        phaseUpMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phaseUpMenuActionPerformed(evt);
            }
        });
        jMenu8.add(phaseUpMenu);

        phaseDownMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, 0));
        phaseDownMenu.setText("phaseDown");
        phaseDownMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phaseDownMenuActionPerformed(evt);
            }
        });
        jMenu8.add(phaseDownMenu);

        phasePiMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, 0));
        phasePiMenu.setText("phasePi");
        phasePiMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phasePiMenuActionPerformed(evt);
            }
        });
        jMenu8.add(phasePiMenu);

        randomTransducerOffMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
        randomTransducerOffMenu.setText("RandTransOff");
        randomTransducerOffMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomTransducerOffMenuActionPerformed(evt);
            }
        });
        jMenu8.add(randomTransducerOffMenu);

        randomTransducerOnMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
        randomTransducerOnMenu.setText("RandTransOn");
        randomTransducerOnMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomTransducerOnMenuActionPerformed(evt);
            }
        });
        jMenu8.add(randomTransducerOnMenu);

        jMenu4.add(jMenu8);

        pointToTargetMenu.setText("PointToTarget");
        pointToTargetMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointToTargetMenuActionPerformed(evt);
            }
        });
        jMenu4.add(pointToTargetMenu);

        transOffsetMenu.setText("Offsets and transforms");
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

        PointsMenu.setText("Points");

        selectAllPointsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, 0));
        selectAllPointsMenu.setText("Select All");
        selectAllPointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllPointsMenuActionPerformed(evt);
            }
        });
        PointsMenu.add(selectAllPointsMenu);

        mergePointsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, 0));
        mergePointsMenu.setText("Merge points");
        mergePointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergePointsMenuActionPerformed(evt);
            }
        });
        PointsMenu.add(mergePointsMenu);

        pointsShapeMenu.setText("Shapes");
        pointsShapeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsShapeMenuActionPerformed(evt);
            }
        });
        PointsMenu.add(pointsShapeMenu);

        jMenuBar1.add(PointsMenu);

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

        animExportRawMenu.setText("Export raw");
        animExportRawMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animExportRawMenuActionPerformed(evt);
            }
        });
        jMenu6.add(animExportRawMenu);

        animImportRawMenu.setText("Import raw");
        animImportRawMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animImportRawMenuActionPerformed(evt);
            }
        });
        jMenu6.add(animImportRawMenu);

        cloneSelectedMenu.setText("Clone selected");
        cloneSelectedMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneSelectedMenuActionPerformed(evt);
            }
        });
        jMenu6.add(cloneSelectedMenu);

        interpolateAnimMenu.setText("Interpolate");
        interpolateAnimMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpolateAnimMenuActionPerformed(evt);
            }
        });
        jMenu6.add(interpolateAnimMenu);

        jMenuBar1.add(jMenu6);

        addTransMenu.setText("Arrays");

        arrayAddMenu.setText("Arrangements");
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

        optimizerMenu.setText("Optimizers");
        optimizerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optimizerMenuActionPerformed(evt);
            }
        });
        addTransMenu.add(optimizerMenu);

        jMenuBar1.add(addTransMenu);

        jMenu9.setText("Utils");

        phasePatternMenu.setText("HoloPatterns");
        phasePatternMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phasePatternMenuActionPerformed(evt);
            }
        });
        jMenu9.add(phasePatternMenu);

        sendToDevicesMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        sendToDevicesMenu.setText("Send to Devices");
        sendToDevicesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToDevicesMenuActionPerformed(evt);
            }
        });
        jMenu9.add(sendToDevicesMenu);

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

        AmpLinePlotMenu.setText("AmpLinePlot");
        AmpLinePlotMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AmpLinePlotMenuActionPerformed(evt);
            }
        });
        jMenu9.add(AmpLinePlotMenu);

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

        auxKeyMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        auxKeyMenu.setText("AuxKey");
        auxKeyMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auxKeyMenuActionPerformed(evt);
            }
        });
        jMenu9.add(auxKeyMenu);

        matlabPhasesMenu.setText("MatlabPhases");
        matlabPhasesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matlabPhasesMenuActionPerformed(evt);
            }
        });
        jMenu9.add(matlabPhasesMenu);

        udpControlMenu.setText("UDP control");
        udpControlMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                udpControlMenuActionPerformed(evt);
            }
        });
        jMenu9.add(udpControlMenu);

        jMenuBar1.add(jMenu9);

        jMenu7.setText("VARIOUS");

        scatterObjectMenu.setText("Scatter Object");
        scatterObjectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scatterObjectMenuActionPerformed(evt);
            }
        });
        jMenu7.add(scatterObjectMenu);

        bowlArrayMenu.setText("Bowl Array");
        bowlArrayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bowlArrayMenuActionPerformed(evt);
            }
        });
        jMenu7.add(bowlArrayMenu);

        sendSwitchbufMenu.setText("Send switch buf");
        sendSwitchbufMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendSwitchbufMenuActionPerformed(evt);
            }
        });
        jMenu7.add(sendSwitchbufMenu);

        rotateMultipleMenu.setText("Gen Anims");
        rotateMultipleMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateMultipleMenuActionPerformed(evt);
            }
        });
        jMenu7.add(rotateMultipleMenu);

        exportPhasesMenu.setText("ExportPhases");
        exportPhasesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPhasesMenuActionPerformed(evt);
            }
        });
        jMenu7.add(exportPhasesMenu);

        exportTransPhasePointsMenu.setText("Export TransPhasePoints");
        exportTransPhasePointsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTransPhasePointsMenuActionPerformed(evt);
            }
        });
        jMenu7.add(exportTransPhasePointsMenu);

        sliceExpMenu.setText("Rand points Exp");
        sliceExpMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sliceExpMenuActionPerformed(evt);
            }
        });
        jMenu7.add(sliceExpMenu);

        jMenuItem1.setText("Slice Experiments");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem1);

        structuralStiffnessMenu.setText("Structural stiffness");
        structuralStiffnessMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                structuralStiffnessMenuActionPerformed(evt);
            }
        });
        jMenu7.add(structuralStiffnessMenu);

        moveOnTimerMenu.setText("MoveOnTimer");
        moveOnTimerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveOnTimerMenuActionPerformed(evt);
            }
        });
        jMenu7.add(moveOnTimerMenu);

        jMenuBar1.add(jMenu7);

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
        firstDragX = lastX = evt.getX();
        firstDragY = lastY = evt.getY();
        
        if(lastButton == 3){
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
            if (trapsPanel.isDrag()){
                //if drag is enabled on the calc trap, then we make this drag event into a click event that will calculate a trap that point
                updateSelection(evt);
            }
            //zoom( diffY * 1.5f * domainPanel.getGUIGain());
            
        }else if(lastButton == 3){
            if (cameraLooked){
                scene.getCamera().moveAzimuthAndInclination(-diffX * rotGain, -diffY * rotGain);
                scene.getCamera().updateObservation();
            }else{
                scene.getCamera().getTransform().rotateLocal(-diffY * rotGain, -diffX * rotGain, 0);
            }
        }else if(lastButton == 2){
           //scene.getCamera().getTransform().moveLocalSpace(-diffX * moveGain, diffY * moveGain, 0);
            zoom( diffY * 1.5f * domainPanel.getGUIGain());
        }
        
        needUpdate();
        lastX = x;
        lastY = y;
    }//GEN-LAST:event_panelMouseDragged

    private void panelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_panelMouseWheelMoved
         float wheel = (float)evt.getPreciseWheelRotation();
         final float wheelGain = domainPanel.getGUIGain() * 6f;
         final float value = wheel * wheelGain;
         zoom(value);
    }//GEN-LAST:event_panelMouseWheelMoved

    private void zoom(final float value){
        if (cameraLooked){
            scene.getCamera().setDistance(scene.getCamera().getDistance()+ value);  
            scene.getCamera().updateObservation();
         }else{
             scene.getCamera().getTransform().moveLocalSpace(0, 0, value);
         }
         needUpdate();
    }
    
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
            lastSimulationSavedOrLoaded = target;
        }
    }//GEN-LAST:event_loadSimMenuActionPerformed

    public void loadSimulation(String target) {
        try {
            simulation = (Simulation) FileUtils.readCompressedObject(new File(target));
            
            simulation.sortAnimations();
            simulation.sortTransducers();
            
            initSimulation();
            clearSelection();

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
    
    public void addToSelection(Entity e){
        e.selected = true;
        selection.add(e);
    }
        
    private void saveSimMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSimMenuActionPerformed
        String file = FileUtils.selectNonExistingFile(this, ".xml.gz");
        if ( file != null){
            saveSimulation(file);
        }
    }//GEN-LAST:event_saveSimMenuActionPerformed

    private void saveSimulation(final String file){
        try {
            simForm.guiToObj();
            simulation.labelNumberTransducers();
            simulation.setHoloMemory(holoPatternsForm.getHoloMemory());

            simulation.getMaskObjects().clear();
            scene.gatherMeshEntitiesWithTag(simulation.getMaskObjects(), Entity.TAG_MASK);

            simulation.getSlices().clear();
            scene.gatherMeshEntitiesWithTag(simulation.getSlices(), Entity.TAG_SLICE);

            FileUtils.writeCompressedObject(new File(file), simulation);
            lastSimulationSavedOrLoaded = file;
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void delTransMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTransMenuActionPerformed

        transducersPanel.deleteSelectedTransducers();

    }//GEN-LAST:event_delTransMenuActionPerformed

    private void arrayAddMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrayAddMenuActionPerformed
        addTransducersForm.setVisible(true);
    }//GEN-LAST:event_arrayAddMenuActionPerformed

    private void resetCamMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCamMenuActionPerformed
        scene.adjustCameraToSimulation(simulation, getGLAspect());
        needUpdate();
    }//GEN-LAST:event_resetCamMenuActionPerformed

    private void xTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xTextFocusGained
        changeSlider(FieldsToChange.xField, "X", simulation.getMinSize() * 8.0f);
    }//GEN-LAST:event_xTextFocusGained

    private void yTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yTextFocusGained
        changeSlider(FieldsToChange.yField, "Y", simulation.getMinSize() * 8.0f);
    }//GEN-LAST:event_yTextFocusGained

    private void zTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_zTextFocusGained
        changeSlider(FieldsToChange.zField, "Z", simulation.getMinSize() * 8.0f);
    }//GEN-LAST:event_zTextFocusGained

    private void rxTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rxTextFocusGained
        changeSlider(FieldsToChange.rxField, "RX", 360);
    }//GEN-LAST:event_rxTextFocusGained

    private void ryTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ryTextFocusGained
        changeSlider(FieldsToChange.ryField, "RY", 360);
    }//GEN-LAST:event_ryTextFocusGained

    private void rzTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rzTextFocusGained
        changeSlider(FieldsToChange.rzField, "RZ", 360);
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
        changeSlider(FieldsToChange.syField, "SY", simulation.maxDistanceBoundary() / 8.0f);
    }//GEN-LAST:event_syTextFocusGained

    private void sxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sxTextActionPerformed
        updateTransForField(FieldsToChange.sxField, sxText.getText());
    }//GEN-LAST:event_sxTextActionPerformed

    private void sxTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sxTextFocusGained
        changeSlider(FieldsToChange.sxField, "SX", simulation.maxDistanceBoundary() / 8.0f);
    }//GEN-LAST:event_sxTextFocusGained

    private void szTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_szTextFocusGained
        changeSlider(FieldsToChange.szField, "SZ", simulation.maxDistanceBoundary() / 8.0f);
    }//GEN-LAST:event_szTextFocusGained

    private void szTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_szTextActionPerformed
       updateTransForField(FieldsToChange.szField, szText.getText());
    }//GEN-LAST:event_szTextActionPerformed
  
    public void addMeshEntityToSceneCenter( MeshEntity me){
        me.getTransform().getTranslation().set( simulation.getSimulationCenter() );
        me.getTransform().getScale().set( simulation.maxDistanceBoundary() );
        scene.getEntities().add( me );
    }
    
    private void camViewMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camViewMenuActionPerformed
        showNewFrame( new TransformForm(scene.getCamera().getTransform(), this) );
    }//GEN-LAST:event_camViewMenuActionPerformed

    private void unlockCameraMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unlockCameraMenuActionPerformed
       cameraLooked = !cameraLooked;
    }//GEN-LAST:event_unlockCameraMenuActionPerformed

    
    private void addKeyFrameMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyFrameMenuActionPerformed
        animPanel.addKeyFrame();
    }//GEN-LAST:event_addKeyFrameMenuActionPerformed

    private void transSetPhase0MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetPhase0MenuActionPerformed
        transducersPanel.setTransPhase( 0.0f );
    }//GEN-LAST:event_transSetPhase0MenuActionPerformed

    private void transSetPhasePiMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetPhasePiMenuActionPerformed
        transducersPanel.setTransPhase( 1.0f );
    }//GEN-LAST:event_transSetPhasePiMenuActionPerformed

    private void transSetAmp0MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetAmp0MenuActionPerformed
        transducersPanel.setTransAmp( 0.0f );
    }//GEN-LAST:event_transSetAmp0MenuActionPerformed

    private void transSetAmp1MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transSetAmp1MenuActionPerformed
        transducersPanel.setTransAmp( 1.0f );
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

    private void selToBagMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selToBagMenuActionPerformed
        bag.clear();
        bag.addAll( selection );
    }//GEN-LAST:event_selToBagMenuActionPerformed

    private void transOffsetMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transOffsetMenuActionPerformed
        showNewFrame( new TransducersOffsetForm(this) );
    }//GEN-LAST:event_transOffsetMenuActionPerformed

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
            
            config.frameX = this.getLocation().x;
            config.frameY = this.getLocation().y;
            config.frameWidth = this.getSize().width;
            config.frameHeigh = this.getSize().height;
            
            config.lastSimFile = lastSimulationSavedOrLoaded;
            
            config.guiValues.clear();
            SimpleGUIPersistence.extractValuesFrom(config.guiValues, "", this);
            
            //try to save config
            FileUtils.writeObject(new File(CONFIG_PATH), config);
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.exit(0);
    }//GEN-LAST:event_onExit

    private void sendToDevicesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToDevicesMenuActionPerformed
        transControlPanel.sendPattern();
    }//GEN-LAST:event_sendToDevicesMenuActionPerformed

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
        SimpleFPGA_128.exportAnimationMatlab( this );
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

    private void auxKeyMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auxKeyMenuActionPerformed
        trapsPanel.reportGorkovs();
    }//GEN-LAST:event_auxKeyMenuActionPerformed

    private void exportNano8MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportNano8MenuActionPerformed
        ArduinoNano.exportAnimation( this );
    }//GEN-LAST:event_exportNano8MenuActionPerformed

    private void scatterObjectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scatterObjectMenuActionPerformed
        showNewFrame( new ScatterObjectForm(this) );
    }//GEN-LAST:event_scatterObjectMenuActionPerformed

    private void bowlArrayMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bowlArrayMenuActionPerformed
        showNewFrame(new BowlsForm(this));
    }//GEN-LAST:event_bowlArrayMenuActionPerformed

    private void matlabPhasesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matlabPhasesMenuActionPerformed
        showNewFrame( new ImportExportPhasesMatlabForm(this));
    }//GEN-LAST:event_matlabPhasesMenuActionPerformed

    private void optimizerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optimizerMenuActionPerformed
        showNewFrame( algForm );
    }//GEN-LAST:event_optimizerMenuActionPerformed

    private void sliceExpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sliceExpMenuActionPerformed
        showNewFrame( new RandPointsExpFrame(this) );
    }//GEN-LAST:event_sliceExpMenuActionPerformed

    private void selectTransTopMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTransTopMenuActionPerformed
        transducersPanel.selectTopTransducers(true);
    }//GEN-LAST:event_selectTransTopMenuActionPerformed

    private void selectTransTopMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTransTopMenu1ActionPerformed
        transducersPanel.selectTopTransducers(false);
    }//GEN-LAST:event_selectTransTopMenu1ActionPerformed

    private void sendSwitchbufMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendSwitchbufMenuActionPerformed
        showNewFrame( new SwitchTimer(this) );
    }//GEN-LAST:event_sendSwitchbufMenuActionPerformed

    private void importTransMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importTransMenuActionPerformed
        addTransducersForm.importFromSim();
    }//GEN-LAST:event_importTransMenuActionPerformed

    private void rotateMultipleMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateMultipleMenuActionPerformed
        showNewFrame(new GenerateComplexAnimations(this)); 
    }//GEN-LAST:event_rotateMultipleMenuActionPerformed

    private void assignSel2MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignSel2MenuActionPerformed
        movePanel.snapSelection(1);
    }//GEN-LAST:event_assignSel2MenuActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        movePanel.snapSelection(2);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void sel1MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sel1MenuActionPerformed
        movePanel.applySelection(1);
    }//GEN-LAST:event_sel1MenuActionPerformed

    private void sel2MenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sel2MenuActionPerformed
        movePanel.applySelection(2);
    }//GEN-LAST:event_sel2MenuActionPerformed

    private void animExportRawMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animExportRawMenuActionPerformed
        animPanel.exportRawAnimation();
    }//GEN-LAST:event_animExportRawMenuActionPerformed

    private void animImportRawMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animImportRawMenuActionPerformed
        animPanel.importRawAnimation();
    }//GEN-LAST:event_animImportRawMenuActionPerformed

    private void interpolateAnimMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolateAnimMenuActionPerformed
        animPanel.interpolateCurrentAnimation();
    }//GEN-LAST:event_interpolateAnimMenuActionPerformed

    private void cloneSelectedMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneSelectedMenuActionPerformed
        animPanel.cloneCurrentAnimation();
    }//GEN-LAST:event_cloneSelectedMenuActionPerformed

    private void exportPhasesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPhasesMenuActionPerformed
        animPanel.exportPhasesOfCurrentAnimation();
    }//GEN-LAST:event_exportPhasesMenuActionPerformed

    private void exportTransPhasePointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportTransPhasePointsMenuActionPerformed
        animPanel.exportTransPhasePoints();
    }//GEN-LAST:event_exportTransPhasePointsMenuActionPerformed

    private void udpControlMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_udpControlMenuActionPerformed
        showNewFrame( new UDPRemoteControl(this));
    }//GEN-LAST:event_udpControlMenuActionPerformed

    private void phaseUpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phaseUpMenuActionPerformed
        transControlPanel.addPhaseSteps(1);
    }//GEN-LAST:event_phaseUpMenuActionPerformed

    private void phaseDownMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phaseDownMenuActionPerformed
        transControlPanel.addPhaseSteps(-1);
    }//GEN-LAST:event_phaseDownMenuActionPerformed

    private void phasePiMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phasePiMenuActionPerformed
        transControlPanel.phasePi();
    }//GEN-LAST:event_phasePiMenuActionPerformed

    private void randomTransducerOffMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomTransducerOffMenuActionPerformed
        transducersPanel.switchOnRandom(-1);
    }//GEN-LAST:event_randomTransducerOffMenuActionPerformed

    private void randomTransducerOnMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomTransducerOnMenuActionPerformed
        transducersPanel.switchOnRandom(1);
    }//GEN-LAST:event_randomTransducerOnMenuActionPerformed

    private void mergePointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergePointsMenuActionPerformed
        pointsPanel.mergeClosestPoint();
    }//GEN-LAST:event_mergePointsMenuActionPerformed

    private void zoomInMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuActionPerformed
        zoom(1 * domainPanel.getGUIGain() * 6);
    }//GEN-LAST:event_zoomInMenuActionPerformed

    private void zoomOutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuActionPerformed
        zoom(-1 * domainPanel.getGUIGain() * 6);
    }//GEN-LAST:event_zoomOutMenuActionPerformed

    private void selectAllTransducersMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllTransducersMenuActionPerformed
        transducersPanel.selectAll();
    }//GEN-LAST:event_selectAllTransducersMenuActionPerformed

    private void selectAllPointsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllPointsMenuActionPerformed
        pointsPanel.selectAll();
        needUpdate();
    }//GEN-LAST:event_selectAllPointsMenuActionPerformed

    private void simulationResizeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationResizeMenuActionPerformed
        updateBoundaries();
        adjustGUIGainAndCameras();
        needUpdate();
    }//GEN-LAST:event_simulationResizeMenuActionPerformed

    private void loadLastMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadLastMenuActionPerformed
        String fileToUse = null;
        
        if (lastSimulationSavedOrLoaded != null){
            fileToUse = lastSimulationSavedOrLoaded;
        }else if (config.lastSimFile != null){
            fileToUse = config.lastSimFile;
        }
        
        if( fileToUse != null ){
            final int load = DialogUtils.getBooleanDialog(this, "Do you want to load " + fileToUse);
            if (load == 1){
                loadSimulation(fileToUse);
                lastSimulationSavedOrLoaded = fileToUse;
            }
        }
    }//GEN-LAST:event_loadLastMenuActionPerformed

    private void saveSameFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSameFileMenuActionPerformed
        if (lastSimulationSavedOrLoaded != null){
            saveSimulation(lastSimulationSavedOrLoaded);
        }
    }//GEN-LAST:event_saveSameFileMenuActionPerformed

    private void panelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMouseReleased
        final int button = evt.getButton();
        final int x = evt.getX();
        final int y = evt.getY();
        final float dist = M.distance(x, y, firstDragX, firstDragY);
        /*
        if (button == 1 && dist > 10){
            final int tags = addTagsForSelectionFilter(0);
            final ArrayList<Entity> selected = selectWithDrag(firstDragX,firstDragY,x,y, tags);
            setSelection(selected);
        }*/
    }//GEN-LAST:event_panelMouseReleased

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        showNewFrame( new SliceExperiments(this) );
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void AmpLinePlotMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AmpLinePlotMenuActionPerformed
        showNewFrame( new AmpLinePlot(this));
    }//GEN-LAST:event_AmpLinePlotMenuActionPerformed

    private void pointsShapeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsShapeMenuActionPerformed
        showNewFrame( new ShapePointsFrame(this));
    }//GEN-LAST:event_pointsShapeMenuActionPerformed

    private void structuralStiffnessMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_structuralStiffnessMenuActionPerformed
        showNewFrame( new StructuralStiffnessForm(this));
    }//GEN-LAST:event_structuralStiffnessMenuActionPerformed

    private void moveOnTimerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveOnTimerMenuActionPerformed
        showNewFrame( new MoveOnTimerForm(this) );
    }//GEN-LAST:event_moveOnTimerMenuActionPerformed
 
    private void showNewFrame(final JFrame frame){
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AmpLinePlotMenu;
    private javax.swing.JMenuItem ImportAmpPhasesMenu;
    private javax.swing.JMenu PointsMenu;
    private javax.swing.JMenuItem addKeyFrameMenu;
    private javax.swing.JMenu addTransMenu;
    private javax.swing.JMenuItem animExportRawMenu;
    private javax.swing.JMenuItem animImportRawMenu;
    private javax.swing.JMenuItem arrayAddMenu;
    private javax.swing.JMenuItem arrayExportMenu;
    private javax.swing.JMenuItem arrayFromObjMenu;
    private javax.swing.JMenuItem assignSel2Menu;
    private javax.swing.JMenuItem auxKeyMenu;
    private javax.swing.JMenuItem bowlArrayMenu;
    private javax.swing.JMenuItem camCoverSelMenu;
    private javax.swing.JMenuItem camLookSelectionMenu;
    private javax.swing.JMenuItem camViewMenu;
    private javax.swing.JMenuItem cameraMovMenu;
    private javax.swing.JMenuItem centerCamMenu;
    private javax.swing.JMenuItem cloneSelectedMenu;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JMenuItem delTransMenu;
    private javax.swing.JMenuItem exportMatlabMenu;
    private javax.swing.JMenuItem exportNano8Menu;
    private javax.swing.JMenuItem exportObjMenu;
    private javax.swing.JMenuItem exportObjWithMtlMenu;
    private javax.swing.JMenuItem exportPhasesMenu;
    private javax.swing.JMenuItem exportToArduinoMenu;
    private javax.swing.JMenuItem exportTransPhasePointsMenu;
    private javax.swing.JMenuItem forcePlotsMenu;
    private javax.swing.JMenuItem importArrayMenu;
    private javax.swing.JMenuItem importTransMenu;
    private javax.swing.JMenuItem interpolateAnimMenu;
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
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JMenuItem loadLastMenu;
    private javax.swing.JMenuItem loadSimMenu;
    private javax.swing.JTabbedPane mainTabPanel;
    private javax.swing.ButtonGroup maskObjectsGroup;
    private javax.swing.JMenuItem matlabFieldMenu;
    private javax.swing.JMenuItem matlabPhasesMenu;
    private javax.swing.JMenuItem mergePointsMenu;
    private javax.swing.JMenuItem moveOnTimerMenu;
    private javax.swing.JMenuItem offNextOnTransducerMenu;
    private javax.swing.JMenuItem optimizerMenu;
    private javax.swing.JMenuItem originCamMenu;
    private javax.swing.JMenuItem otherCamMenu;
    private javax.swing.JPanel panel;
    private javax.swing.JPanel panelSlider;
    private javax.swing.JMenuItem phaseDownMenu;
    private javax.swing.JMenuItem phasePatternMenu;
    private javax.swing.JMenuItem phasePiMenu;
    private javax.swing.JMenuItem phaseUpMenu;
    private javax.swing.JMenuItem pointToTargetMenu;
    private javax.swing.JMenuItem pointsShapeMenu;
    private javax.swing.JMenuItem polarPlotsMenu;
    private javax.swing.ButtonGroup preCubeSource;
    private javax.swing.JMenuItem randomTransducerOffMenu;
    private javax.swing.JMenuItem randomTransducerOnMenu;
    private javax.swing.JMenuItem recToSelMenu;
    private javax.swing.JMenuItem resetCamMenu;
    private javax.swing.JMenuItem rotateMultipleMenu;
    private javax.swing.JTextField rxText;
    private javax.swing.JTextField ryText;
    private javax.swing.JTextField rzText;
    private javax.swing.JMenuItem saveSameFileMenu;
    private javax.swing.JMenuItem saveSimMenu;
    private javax.swing.JMenuItem scatterObjectMenu;
    private javax.swing.JMenuItem sel1Menu;
    private javax.swing.JMenuItem sel2Menu;
    private javax.swing.JMenuItem selToBagMenu;
    private javax.swing.JMenuItem selectAllPointsMenu;
    private javax.swing.JMenuItem selectAllTransducersMenu;
    private javax.swing.JMenuItem selectTransTopMenu;
    private javax.swing.JMenuItem selectTransTopMenu1;
    private javax.swing.JMenuItem sendSwitchbufMenu;
    private javax.swing.JMenuItem sendToDevicesMenu;
    private javax.swing.JMenuItem simEditParamMenu;
    private javax.swing.JMenuItem simTransformMenu;
    private javax.swing.JMenuItem simulationResizeMenu;
    private javax.swing.JMenuItem sliceExpMenu;
    private javax.swing.ButtonGroup slicesSource;
    private javax.swing.JLabel sliderFieldLabel;
    private javax.swing.JMenuItem structuralStiffnessMenu;
    private javax.swing.JTextField sxText;
    private javax.swing.JTextField syText;
    private javax.swing.JTextField szText;
    private javax.swing.JMenuItem transAssignmentMenu;
    private javax.swing.JMenuItem transOffsetMenu;
    private javax.swing.JMenuItem transSetAmp0Menu;
    private javax.swing.JMenuItem transSetAmp1Menu;
    private javax.swing.JMenuItem transSetPhase0Menu;
    private javax.swing.JMenuItem transSetPhasePiMenu;
    private javax.swing.JMenuItem udpControlMenu;
    private javax.swing.JMenuItem unlockCameraMenu;
    private javax.swing.ButtonGroup wrapPlayButtonGroup;
    private javax.swing.JTextField xText;
    private javax.swing.JTextField yText;
    private javax.swing.JTextField zText;
    private javax.swing.JMenuItem zoomInMenu;
    private javax.swing.JMenuItem zoomOutMenu;
    // End of variables declaration//GEN-END:variables

    public void needUpdate() {
        panel.repaint();
    }
    
    public ArrayList<Entity> selectWithDrag(final int sx, final int sy, final int ex, final int ey, final int tags){
        final float panelWidth = panel.getWidth();
        final float panelHeight = panel.getHeight();
        
        return scene.pickObjectsWithDrag(
                    sx / panelWidth, 1.0f - sy / panelHeight, 
                    ex / panelWidth, 1.0f - ey / panelHeight, 
                tags);
    }
            
    public MeshEntity clickRaySelectEntity(final int x, final int y, final int tags){
         return scene.pickObject(
                    x / (float) panel.getWidth(),
                    1.0f - y / (float) panel.getHeight(), tags);
    }

    public Vector3f clickRayIntersectObject(final MeshEntity e,final int x,final int y){
        return scene.clickToObject(x / (float) panel.getWidth(), 1.0f - y / (float) panel.getHeight(), e);      
    }
    
    private int addTagsForSelectionFilter(int tags){
        final Component comp = mainTabPanel.getSelectedComponent();
        
        if (comp == transducersPanel || comp == transControlPanel){
            tags |= Entity.TAG_TRANSDUCER;
        }else if (comp == rtSlicePanel ){
            tags |= Entity.TAG_SLICE;
        }else if (comp == miscPanel){
            tags |= Entity.TAG_MASK;
        }else if (comp == domainPanel){
            tags |= Entity.TAG_CUBE_HELPER;
        }else if (comp == pointsPanel){
     
            tags |= Entity.TAG_CONTROL_POINT;
        }else if (comp == movePanel){
            tags |= Entity.TAG_CONTROL_POINT;
        }
        
        return tags;
    }
    
    private void updateSelection(MouseEvent evt) {
        final int x = evt.getX(); 
        final int y = evt.getY();
        int tags = Entity.TAG_NONE;
        
        final Component comp = mainTabPanel.getSelectedComponent();
        
        tags = addTagsForSelectionFilter(tags);
        if (comp == pointsPanel && pointsPanel.isClickAndPlace()){     
            final MeshEntity e = clickRaySelectEntity(x, y, Entity.TAG_SLICE);
            if (e != null) {
                final Vector3f col = clickRayIntersectObject(e, x, y);
                pointsPanel.addControlPoint(col.x, col.y, col.z);
                needUpdate();
            }
            return;
        }else if (comp == trapsPanel){
            MeshEntity e = clickRaySelectEntity(x, y, Entity.TAG_SLICE);
            if (e != null) {
                final Vector3f worldPos = clickRayIntersectObject(e, x, y);
                trapsPanel.clickAt(worldPos);
            }                           
        }
        
        Entity e = scene.pickObject(
                lastX / (float) panel.getWidth(),
                1.0f - lastY / (float) panel.getHeight(), tags);
        if ( e == null ){
            clearSelection();
            needUpdate();
            return;
        }

        
        if ((evt.getModifiersEx() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
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
        x.setText( StringFormats.get().dc4( v.x ) );
        y.setText( StringFormats.get().dc4( v.y ) );
        z.setText( StringFormats.get().dc4( v.z ) );   
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
            transducersPanel.updateFromTransducer((Transducer)e);
        }

    }

    public void setSelection(ArrayList<Entity> sel) {
        clearSelection();
        for(Entity e : sel){
            e.selected = true;
            selection.add(e);
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
    private float sliderScale;
    public void changeSlider(FieldsToChange field, String name, float scale){
        sliderField = field;
        sliderFieldLabel.setText(name);
        sliderScale = scale;
    }
    
    private void changeSelectionField(FieldsToChange field, float value, boolean absolute, boolean updateTextField){
        final Vector3f angles = new Vector3f();
        
        for(Entity e : selection){
            Transform tra = e.getTransform();
            
            if(field == FieldsToChange.xField){
                tra.getTranslation().x = absolute ? value : tra.getTranslation().x + value;
                if (updateTextField) { xText.setText( StringFormats.get().dc4(tra.getTranslation().x)); }
            }else if(field == FieldsToChange.yField){
                tra.getTranslation().y = absolute ? value : tra.getTranslation().y + value;
                if (updateTextField) { yText.setText( StringFormats.get().dc4(tra.getTranslation().y)); }
            }else if(field == FieldsToChange.zField){
                tra.getTranslation().z = absolute ? value : tra.getTranslation().z + value;
                if (updateTextField) { zText.setText( StringFormats.get().dc4(tra.getTranslation().z)); }
            }else if(field == FieldsToChange.sxField){
                tra.getScale().x = absolute ? value : tra.getScale().x + value;
                if (updateTextField) { sxText.setText( StringFormats.get().dc4( tra.getScale().x )); }
            }else if(field == FieldsToChange.syField){
                 tra.getScale().y = absolute ? value : tra.getScale().y + value;
                if (updateTextField) { syText.setText( StringFormats.get().dc4( tra.getScale().y )); }
            }else if(field == FieldsToChange.szField){
                 tra.getScale().z = absolute ? value : tra.getScale().z + value;
                if (updateTextField) { szText.setText( StringFormats.get().dc4( tra.getScale().z )); }
            }else if(field == FieldsToChange.rxField || 
                    field == FieldsToChange.ryField || 
                    field == FieldsToChange.rzField){
                float rads = value * M.DEG_TO_RAD;
                Quaternion q = tra.getRotation();
                q.toAngles(angles);
                if(field == FieldsToChange.rxField) {
                    angles.x = absolute ? rads : angles.x + rads;
                    if (updateTextField) { rxText.setText(StringFormats.get().dc4(angles.x * M.RAD_TO_DEG)); }
                }else if (field == FieldsToChange.ryField) {
                    angles.y = absolute ? rads : angles.y + rads;
                    if (updateTextField) { ryText.setText(StringFormats.get().dc4(angles.y * M.RAD_TO_DEG)); }
                }else if (field == FieldsToChange.rzField) {
                    angles.z = absolute ? rads : angles.z + rads;
                    if (updateTextField) { rzText.setText(StringFormats.get().dc4(angles.z * M.RAD_TO_DEG)); }
                }
                q.fromAngles(angles);
            }
            
            if (e instanceof Transducer){
                transducersPanel.updateField((Transducer)e, field,  value,  absolute,  updateTextField);
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
