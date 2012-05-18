
package org.inria.acqua.forms;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import org.inria.acqua.forms.labelnotification.LabelNotifierEmitter;
import org.inria.acqua.forms.labelnotification.LabelNotifierReceptor;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.parsers.ConfigParser;
import org.inria.acqua.plugins.campaigngenerator.pingabstraction.Pinger;


public class ConfigurationWindow extends javax.swing.JFrame {
    private static ConfigurationWindow instance = null; /** Singleton stuff. */
    private static final int TAB_CASE1 = 0; /** IDs to identify which tab the user has selected. */
    private static final int TAB_CASE2 = 1;
    private static final int TAB_CASE3 = 2;
    private static final int TAB_CASEMANUAL = 3;
    private static boolean visibleMode = true;
    private ConfigParser configParser;
    private LabelNotifierEmitter labelNotifierEmitter;
    private LabelNotifierReceptor labelNotifierReceptor;

    public static boolean getVisibleMode(){
        return visibleMode;
    }

    public static void setVisibleMode(boolean val){
        visibleMode = val;
    }

    /** Look & feel stuff. */
    static{
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            System.err.println("ERROR: Cannot initialize display: '" + e.getMessage() + "'.");
            System.exit(-1);
        }
   }

    /** Singleton stuff. */
    public static ConfigurationWindow getConfigurationWindow(){
        if (instance==null){
            instance = new ConfigurationWindow();
        }
        return instance; 
    }

    /** Creates new form ConfigurationWindow */
    private ConfigurationWindow() {

        
        try{
            this.configParser = new ConfigParser(ConfigParser.DEFAULT_CONFIG_FILE_NAME);
        }catch(Exception e){
            System.err.println("ERROR: Configuration file XML is not valid or is not present.");
            e.printStackTrace();
            System.exit(-1);
        }
        
        if (visibleMode==true){
            /* Init automatically and by hand the components. */
            initComponents();
            initComponentsOurWay();

            labelNotifierReceptor = new LabelNotifierReceptor(reportLabel);
            labelNotifierEmitter = new LabelNotifierEmitter();
            labelNotifierEmitter.addLabelNotifierReceptor(labelNotifierReceptor);

            /* Put the window in the center of the screeen. */
            this.setLocation((
            Toolkit.getDefaultToolkit().getScreenSize().
             width-this.getSize().width)/2,
             (Toolkit.getDefaultToolkit().getScreenSize().
             height-this.getSize().height)/2);
        }else{
            /* Go directly. */
            new MainWindow(ConfigParser.DEFAULT_CONFIG_FILE_NAME, false);
        }
        this.setVisible(visibleMode);
    }

  /** Manual initialization of components. */
    private void initComponentsOurWay() {

        jLabel2.setVisible(false);
        ifePathText.setVisible(false);
        browseIFEButton.setVisible(false);
        jScrollPane2.setVisible(false);
        
        jLabel4.setVisible(false);
        signLevelSpin.setVisible(false);
        
        jLabel5.setVisible(false);
        numberOfSavedEntriesSpin.setVisible(false);
        
        
        jLabel13.setVisible(false);
        numberOfSavedEntriesGUISpin.setVisible(false);


        /** Icon of the frame. */
        try{
            URL b = getClass().getResource("/resources/icon.gif");
            Image a = new ImageIcon(b).getImage();
            this.setIconImage(a);
        }catch(Exception e){}


        /** What to do when this window closes. */
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        /** Get configuration file's info and update the GUI fields. */
        this.getInfoFromFileAndUpdateWindow();

        this.caseSelectorTabberPanel.setSelectedIndex(ConfigurationWindow.TAB_CASEMANUAL);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        caseSelectorTabberPanel = new javax.swing.JTabbedPane();
        jPanelScene1 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jPanelScene2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanelScene3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanelManual = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        landmarksText = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ifePathText = new javax.swing.JTextPane();
        signLevelSpin = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        numberOfSavedEntriesSpin = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        timeoutSpin = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        ifeExecutionPeriodSpin = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        numberOfPingsSpin = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        browseIFEButton = new javax.swing.JButton();
        numberOfSavedEntriesGUISpin = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        reportLabel = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IFE Configuration ");
        setResizable(false);

        caseSelectorTabberPanel.setToolTipText("");
        caseSelectorTabberPanel.setFont(new java.awt.Font("Tahoma", 1, 11));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel12.setText("Measurements will be done with parameters such that...");

        javax.swing.GroupLayout jPanelScene1Layout = new javax.swing.GroupLayout(jPanelScene1);
        jPanelScene1.setLayout(jPanelScene1Layout);
        jPanelScene1Layout.setHorizontalGroup(
            jPanelScene1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addContainerGap(179, Short.MAX_VALUE))
        );
        jPanelScene1Layout.setVerticalGroup(
            jPanelScene1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addContainerGap(357, Short.MAX_VALUE))
        );

        caseSelectorTabberPanel.addTab("Case 1", jPanelScene1);

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel11.setText("Measurements will be done with parameters such that...");

        javax.swing.GroupLayout jPanelScene2Layout = new javax.swing.GroupLayout(jPanelScene2);
        jPanelScene2.setLayout(jPanelScene2Layout);
        jPanelScene2Layout.setHorizontalGroup(
            jPanelScene2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addContainerGap(179, Short.MAX_VALUE))
        );
        jPanelScene2Layout.setVerticalGroup(
            jPanelScene2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addContainerGap(357, Short.MAX_VALUE))
        );

        caseSelectorTabberPanel.addTab("Case 2", jPanelScene2);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel10.setText("Measurements will be done with parameters such that...");

        javax.swing.GroupLayout jPanelScene3Layout = new javax.swing.GroupLayout(jPanelScene3);
        jPanelScene3.setLayout(jPanelScene3Layout);
        jPanelScene3Layout.setHorizontalGroup(
            jPanelScene3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(179, Short.MAX_VALUE))
        );
        jPanelScene3Layout.setVerticalGroup(
            jPanelScene3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScene3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(357, Short.MAX_VALUE))
        );

        caseSelectorTabberPanel.addTab("Case 3", jPanelScene3);

        landmarksText.setText("set of landmarks");
        landmarksText.setToolTipText("Introduce the set of landmarks, separated by space (example: '127.0.0.1 127.0.0.2').");
        jScrollPane1.setViewportView(landmarksText);

        jLabel1.setText("Set of landmarks:");

        jLabel2.setText("Measurement tool path:");

        ifePathText.setText("measurement tool path");
        ifePathText.setToolTipText("Leave empty if path by default.");
        jScrollPane2.setViewportView(ifePathText);

        jLabel4.setText("Significance level [x1000]:");

        jLabel5.setText("Learning window length (IFE):");

        jLabel6.setText("Ping timeout [s]:");

        jLabel8.setText("Period of measurement [ms]:");

        jLabel9.setText("Number of pings/campaign:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Manual configuration");

        browseIFEButton.setText("Browse...");
        browseIFEButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseIFEButtonActionPerformed(evt);
            }
        });

        jLabel13.setText("Number of saved entries for session:");

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout jPanelManualLayout = new javax.swing.GroupLayout(jPanelManual);
        jPanelManual.setLayout(jPanelManualLayout);
        jPanelManualLayout.setHorizontalGroup(
            jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelManualLayout.createSequentialGroup()
                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelManualLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                .addGap(18, 18, 18))
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9))
                                .addGap(18, 18, 18))
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(browseIFEButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(signLevelSpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanelManualLayout.createSequentialGroup()
                                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(numberOfSavedEntriesSpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(timeoutSpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(ifeExecutionPeriodSpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(numberOfPingsSpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(34, 34, 34)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(numberOfSavedEntriesGUISpin, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 173, Short.MAX_VALUE))
                            .addGroup(jPanelManualLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))))
                    .addGroup(jPanelManualLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel3)))
                .addGap(18, 18, 18))
        );

        jPanelManualLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel13, jLabel2, jLabel4, jLabel5, jLabel6, jLabel8, jLabel9});

        jPanelManualLayout.setVerticalGroup(
            jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelManualLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelManualLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, Short.MAX_VALUE)
                        .addComponent(browseIFEButton)))
                .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelManualLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(signLevelSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(numberOfSavedEntriesSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(timeoutSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(36, 36, 36)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(ifeExecutionPeriodSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(numberOfPingsSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numberOfSavedEntriesGUISpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)))
                    .addGroup(jPanelManualLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(41, 41, 41))
        );

        caseSelectorTabberPanel.addTab("Manual", jPanelManual);

        okButton.setText("Run");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        reportLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(caseSelectorTabberPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(caseSelectorTabberPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportLabel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** Get configuration file's info and update the GUI fields. */
    private void getInfoFromFileAndUpdateWindow(){
        ConfigParser cp = this.configParser;
        try{
//            this.signLevelSpin.setModel(
//                    new SpinnerNumberModel(
//                        Integer.valueOf(cp.getSignificanceLevel()).intValue(), 0, 1000, 1));

            this.numberOfSavedEntriesSpin.setModel(
                    new SpinnerNumberModel(
                        Integer.valueOf(cp.getNumberOfSavedEntriesIFE()).intValue(), 0, 100000, 1)
                    );

            this.numberOfSavedEntriesGUISpin.setModel(
                    new SpinnerNumberModel(
                        Integer.valueOf(cp.getNumberOfSavedEntriesGUI()).intValue(), 0, 100000, 1)
                    );


            this.timeoutSpin.setModel(
                        new SpinnerNumberModel(
                            Integer.valueOf(cp.getTimeoutSeconds()).intValue(), 0, 100, 1)
                        );

            this.numberOfPingsSpin.setModel(
                        new SpinnerNumberModel(
                            Integer.valueOf(cp.getNumberOfPings()).intValue(), 0, 1000, 1)
                        );

            

            this.ifeExecutionPeriodSpin.setModel(
                        new SpinnerNumberModel(
                            Integer.valueOf(cp.getIFEExecutionPeriod()).intValue(), 0, 600000, 1)
                        );

            this.landmarksText.setText(cp.getRawProvidedLandmarksSerialized(cp.getRawProvidedLandmarks()));
            this.ifePathText.setText(cp.getIFEPathAndName());
        }catch(Exception e){
            System.err.println("Some of the values of configuration file are not valid.");
            e.printStackTrace();
        }
    }

    /** Get windows' fields' info and write it in the file. */
    private void takeInfoFromWindowAndWriteFile() throws Exception{
        ConfigParser cp = this.configParser;
        //cp.setSignificanceLevel(((Integer)this.signLevelSpin.getValue()).intValue());
        cp.setNumberOfSavedEntriesIFE((Integer)this.numberOfSavedEntriesSpin.getValue());
        cp.setTimeoutSeconds((Integer)this.timeoutSpin.getValue());
        cp.setNumberOfPings((Integer)this.numberOfPingsSpin.getValue());
        
        cp.setIFEExecutionPeriod((Integer)this.ifeExecutionPeriodSpin.getValue());
        String text = this.landmarksText.getText();
        cp.setRawProvidedLandmarks(cp.getRawProvidedLandmarksFromText(text));
        cp.setIFEPathAndName(this.ifePathText.getText());
        cp.setNumberOfSavedEntriesGUI((Integer)this.numberOfSavedEntriesGUISpin.getValue());
        cp.writeFile();
        this.getInfoFromFileAndUpdateWindow();
    }


    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        Runnable runnable = new Runnable() {
            public void run() {
                goNext();
            }
        };
        Thread th = new Thread(runnable);
        th.start();

    }//GEN-LAST:event_okButtonActionPerformed

    private void goNext(){
        labelNotifierEmitter.showMessageInGUI("Checking parameters...",
                    LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
        /** For the next screen, use the file according to the tab the user has selected. */
        String selectedConfFile;
        try{
            switch (this.caseSelectorTabberPanel.getSelectedIndex()){
                case TAB_CASE1: selectedConfFile = "config-case1.xml"; break;
                case TAB_CASE2: selectedConfFile = "config-case2.xml"; break;
                case TAB_CASE3: selectedConfFile = "config-case3.xml"; break;
                case TAB_CASEMANUAL:
                    takeInfoFromWindowAndWriteFile();
                    selectedConfFile = "config.xml";
                    break;
                default:
                    selectedConfFile = null;
                    break;
            }

            checkParameters();

            File file = new File(selectedConfFile);

            if (file.exists()){
                final String configFile = selectedConfFile;
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new MainWindow(configFile, true).setVisible(true);
                    }
                });
                this.setVisible(false);
            }else{
                throw new Exception("Cannot find the specified file: '" + selectedConfFile + "'...");
            }
        }catch(Exception e){
            labelNotifierEmitter.showMessageInGUI(e.getMessage(),
                    LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
        }
        
    }
    private void checkParameters() throws Exception{

        File execut = new File(this.ifePathText.getText());

        if (!execut.exists() && (!ifePathText.getText().trim().equals(""))){
            
            throw new Exception("Cannot find the IFE: '" + this.ifePathText.getText() + "'...");
        }

        
        int wind = (Integer)this.numberOfSavedEntriesSpin.getValue();
        if (wind < 5){
            throw new Exception("Window size must be greater than 4.");
        }

        ArrayList<String> landm =
                this.configParser.getRawProvidedLandmarksFromText(this.landmarksText.getText());

        String rep;
        if((rep = Misc.areThereEqualElements(landm))!=null){
            throw new Exception("There are repeated IP adresses ("+rep+").");
        }

        for (String l: landm){
            float resp = 1.0f;
            resp = Pinger.ping(l, 5, 32);
            if (resp<0){
                throw new Exception("Unreachable landmark: '" + l + "'.");
            }
        }


        int sign = (Integer)this.signLevelSpin.getValue();
        /*
        if ((sign!=500) && (sign!=600) && (sign!=700) && (sign!=800) && (sign!=900) &&
            (sign!=950) && (sign!=980) && (sign!=990) && (sign!=995) && (sign!=998) &&
            (sign!=999)){
            throw new Exception("Invalid sign. level (500,600,700,800,900,950,980,990,995,998,999).");
        }*/

    }

    private void browseIFEButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseIFEButtonActionPerformed
        final JFileChooser fc = new JFileChooser(".");
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            ifePathText.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_browseIFEButtonActionPerformed

    public static void execute(String args) {
        boolean visible = true;
        
        if (args!=null && args.equals("-i")){
            visible = false;
        }
        System.setProperty("java.awt.headless", "true");
        
        ConfigurationWindow.setVisibleMode(visible);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ConfigurationWindow();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseIFEButton;
    private javax.swing.JTabbedPane caseSelectorTabberPanel;
    private javax.swing.JSpinner ifeExecutionPeriodSpin;
    private javax.swing.JTextPane ifePathText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelManual;
    private javax.swing.JPanel jPanelScene1;
    private javax.swing.JPanel jPanelScene2;
    private javax.swing.JPanel jPanelScene3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane landmarksText;
    private javax.swing.JSpinner numberOfPingsSpin;
    private javax.swing.JSpinner numberOfSavedEntriesGUISpin;
    private javax.swing.JSpinner numberOfSavedEntriesSpin;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel reportLabel;
    private javax.swing.JSpinner signLevelSpin;
    private javax.swing.JSpinner timeoutSpin;
    // End of variables declaration//GEN-END:variables

}
