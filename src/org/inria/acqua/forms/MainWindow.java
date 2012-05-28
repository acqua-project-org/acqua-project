package org.inria.acqua.forms;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import org.inria.acqua.forms.labelnotification.LabelNotifierEmitter;
import org.inria.acqua.forms.labelnotification.LabelNotifierReceptor;
import org.inria.acqua.layers.*;
import org.inria.acqua.parsers.ConfigFileParser;
import org.inria.acqua.plugins.PipelineCreator;
import org.inria.acqua.plugins.anomalydetector.AnomalyDetector;
import java.util.ArrayList;

/** 
 * Class that shows to the user the parameters measured. 
 * @author mjost
 */
public class MainWindow extends javax.swing.JFrame {
    
    private ArrayList<Curve> curves;        /* Set of curves' objects in this frame. */
    private ExternalModule externalModule;  /* External module (it comunicates with IFE). */
    private ConfigFileParser configParser;      /* Parser of the config.xml file. */
    private LabelNotifierEmitter labelNotifierEmitter;
    private LabelNotifierReceptor labelNotifierReceptor; 
    
    
    /** Look & feel stuff. */
    static{
        try  {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch  (Exception e) {
            e.printStackTrace();
        }
    }

    public MainWindow(String configFile, boolean visibleMode) {

        labelNotifierEmitter = new LabelNotifierEmitter();
        try {
            configParser = ConfigFileParser.getConfigFileParser(configFile);
        } catch (Exception ex) {
            System.err.println("ERROR: Cannot process configuration file '" + configFile + "'.");
            ex.printStackTrace();
            System.exit(-1);
        }

        
        
        initComponents();                       /* Initialize window components.                */
        labelNotifierReceptor = new LabelNotifierReceptor(reportLabel);
        initComponentsOurWay();                 /* Initialize window components by hand.        */

        
        this.setLocation((                      /* Put the window in the center of the screeen. */
        Toolkit.getDefaultToolkit().getScreenSize().
            width-this.getSize().width)/2,
            (Toolkit.getDefaultToolkit().getScreenSize().
            height-this.getSize().height)/2);


        try{
            this.externalModule.runNLoops(visibleMode?-1:configParser.getNumberOfLoops());      /* Start the periodic execution of IFE. */
        }catch(Exception e){
            labelNotifierEmitter.showMessageInGUI(e.getMessage(),
                    LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
            e.printStackTrace();
        }

        
    }
    
    /** Manual initialization of components. */
    private void initComponentsOurWay(){

        /** Icon of the window. */
        try{
            URL b = getClass().getResource("/resources/icon.gif");
            Image a = new ImageIcon(b).getImage();
            this.setIconImage(a);
        }catch(Exception e){}
        
        



        labelNotifierEmitter.addLabelNotifierReceptor(labelNotifierReceptor);

        try{
            /* Initialization of external module, and its parsers.*/
            PipelineCreator pc = PipelineCreator.getGUIPipeline(configParser);
        
        
            externalModule = new ExternalModule(pc, configParser, labelNotifierReceptor, configParser.getIFEExecutionPeriod());
        }catch(Exception e){
            labelNotifierEmitter.showMessageInGUI(e.getMessage(), LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
            e.printStackTrace();
        }

        curves = new ArrayList<Curve>();

        /** Each curve has its own way of behaving. */
        final String[] funcCode = {"rtt", "ife", "shift" , "time"};

        for (int i=0;i<funcCode.length;i++){
            Curve cu = new Curve("");
            curves.add(i, cu);
            externalModule.addChannelController(new ChannelController(funcCode[i], curves.get(i)));
        }


        GridBagLayout gbl = new GridBagLayout();            /* Set layout for channels. */
        GridBagConstraints gbc = new GridBagConstraints();
        CurvesPanel.setLayout(gbl);

         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;

        for (int i=0;i<funcCode.length;i++){
            gbl.setConstraints(curves.get(i),gbc);
            CurvesPanel.add(curves.get(i));
            gbc.gridy=i+1;
        }

        externalModule.initToolEnvironment();


        this.getContentPane().setBackground(menu.getBackground());

        /** What to do when this window closes. */
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                comeBack();
            }
        });



        this.addKeyListener(
                new EventsListener(this.labelNotifierReceptor, this.externalModule)
                );
        this.setFocusable(true);



    }

    /** What to do when this window closes. */
    public void comeBack(){
        ConfigurationWindow.getConfigurationWindow().setVisible(true);
        externalModule.stopAnyThread();
        dispose();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CurvesPanel = new javax.swing.JPanel();
        reportLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        changeSignaturesLabel = new javax.swing.JLabel();
        returnLabel = new javax.swing.JLabel();
        loadLabel = new javax.swing.JLabel();
        saveLabel = new javax.swing.JLabel();
        pauseLabel = new javax.swing.JLabel();
        snapshotLabel = new javax.swing.JLabel();
        menu = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        MenuOpen = new javax.swing.JMenuItem();
        MenuSave = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        menuExit = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        jSeparator14 = new javax.swing.JSeparator();
        menuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IFE");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        CurvesPanel.setBackground(new java.awt.Color(0, 0, 0));
        CurvesPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout CurvesPanelLayout = new javax.swing.GroupLayout(CurvesPanel);
        CurvesPanel.setLayout(CurvesPanelLayout);
        CurvesPanelLayout.setHorizontalGroup(
            CurvesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1039, Short.MAX_VALUE)
        );
        CurvesPanelLayout.setVerticalGroup(
            CurvesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        reportLabel.setText("hello");

        changeSignaturesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        changeSignaturesLabel.setText("Signatures");
        changeSignaturesLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        changeSignaturesLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changeSignaturesLabel.setFocusable(false);
        changeSignaturesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                changeSignaturesLabelMouseClicked(evt);
            }
        });

        returnLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        returnLabel.setText("Return");
        returnLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        returnLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        returnLabel.setFocusable(false);
        returnLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                returnLabelMouseClicked(evt);
            }
        });

        loadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadLabel.setText("Load");
        loadLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        loadLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        loadLabel.setFocusable(false);
        loadLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadLabelMouseClicked(evt);
            }
        });

        saveLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saveLabel.setText("Save");
        saveLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        saveLabel.setFocusable(false);
        saveLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveLabelMouseClicked(evt);
            }
        });

        pauseLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pauseLabel.setText("Running");
        pauseLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pauseLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pauseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pauseLabelMouseClicked(evt);
            }
        });

        snapshotLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        snapshotLabel.setText("Calibrate");
        snapshotLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        snapshotLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        snapshotLabel.setFocusable(false);
        snapshotLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                snapshotLabelMouseClicked(evt);
            }
        });

        menuFile.setText("File");

        MenuOpen.setText("Funct [0]");
        MenuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuOpenActionPerformed(evt);
            }
        });
        menuFile.add(MenuOpen);

        MenuSave.setText("Funct [1]");
        MenuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSaveActionPerformed(evt);
            }
        });
        menuFile.add(MenuSave);
        menuFile.add(jSeparator13);

        menuExit.setText("Return");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        menuFile.add(menuExit);

        menu.add(menuFile);

        menuHelp.setText("Help");
        menuHelp.add(jSeparator14);

        menuAbout.setText("About IFE");
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuAbout);

        menu.add(menuHelp);

        setJMenuBar(menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(reportLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
                    .addComponent(CurvesPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pauseLabel)
                        .addGap(60, 60, 60)
                        .addComponent(snapshotLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                        .addComponent(saveLabel)
                        .addGap(18, 18, 18)
                        .addComponent(loadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(281, 281, 281)
                        .addComponent(changeSignaturesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(returnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {changeSignaturesLabel, loadLabel, pauseLabel, returnLabel, saveLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CurvesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(snapshotLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pauseLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(returnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(changeSignaturesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(loadLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {changeSignaturesLabel, loadLabel, pauseLabel, returnLabel, saveLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
     JOptionPane.showMessageDialog(this, 
     "Impact Factor Estimator\n"
   , "About this project...", JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_menuAboutActionPerformed

/* Acciones para un click en boton Capturar. */
private void MenuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuOpenActionPerformed
    
}//GEN-LAST:event_MenuOpenActionPerformed

private void MenuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSaveActionPerformed
    
}//GEN-LAST:event_MenuSaveActionPerformed

private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
    this.comeBack();
}//GEN-LAST:event_menuExitActionPerformed

private void changeSignaturesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_changeSignaturesLabelMouseClicked
     /** How to change the signatures of the curves. */
    String s = (String)JOptionPane.showInputDialog(
        this, "Enter the new set of signatures separated by a space character. \n\n" +
        "A signature is a code that tells what must be represented in the curves. \n" +
        "For each curve there is one subcode. Each subcode: \n" +
        "    Is separated from the other by a SPACE.\n" +
        "    Is formed by a keyword ('RTT', 'Shift', 'IFE' or 'Time') and could be added an index.\n" +
        "    An index is an IP address surrounded by []. With an index we see more specific information about certain host.\n" +
        "    Only certain keywords accept indexes ('Time' and 'IFE' does not accept).\n" +
        "    In the whole signature there must be 4 subcodes.\n\n" +
        "Some examples: \n"+
        "    'RTT RTT[127.0.0.1] RTT[192.168.0.1] Time'.\n" +
        "    'IFE Shift[127.0.0.1] RTT Time'.\n" +
        "    'IFE RTT[127.0.0.1] RTT Time'.\n" +
        "    'IFE RTT Shift Time'.\n",
        "New signatures", JOptionPane.PLAIN_MESSAGE, null, null, externalModule.getAllSignaturesFromChannelControllers());

    if ((s != null) && (s.length() > 0)) {
        externalModule.setAllSignaturesOfChannelControllers(s);
    }
}//GEN-LAST:event_changeSignaturesLabelMouseClicked

private void returnLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_returnLabelMouseClicked
    this.comeBack();
}//GEN-LAST:event_returnLabelMouseClicked

private void loadLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadLabelMouseClicked
    final JFileChooser fc = new JFileChooser(".");
    int returnVal = fc.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
        try{
            externalModule.loadContentofChannelControllers(fc.getSelectedFile(), curves);
        }catch(Exception e){
            labelNotifierEmitter.showMessageInGUI("Cannot load file '" + fc.getSelectedFile()+ "'.", LabelNotifierReceptor.SERIOUSNESS_ATTENTION);
        }
    }
}//GEN-LAST:event_loadLabelMouseClicked

private void saveLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveLabelMouseClicked
    final JFileChooser fc = new JFileChooser(".");
    int returnVal = fc.showSaveDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
        externalModule.saveContentofChannelControllers(fc.getSelectedFile());
    }
}//GEN-LAST:event_saveLabelMouseClicked

private void pauseLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseLabelMouseClicked
    externalModule.setExecutionPaused(!externalModule.getExecutionPaused());
    updateExecutionStatusPausedRunning();
}//GEN-LAST:event_pauseLabelMouseClicked

private void snapshotLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_snapshotLabelMouseClicked
        try {
            externalModule.putCommand(AnomalyDetector.COMMAND_SNAPSHOT);
        } catch (Exception ex) {
            labelNotifierEmitter.showMessageInGUI(ex.getMessage(), LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
        }
}//GEN-LAST:event_snapshotLabelMouseClicked


private void updateExecutionStatusPausedRunning(){
    boolean isPaused = externalModule.getExecutionPaused();
    String caption = (isPaused?"Paused":"Running");
    pauseLabel.setText(caption);
    if (isPaused){
        labelNotifierEmitter.showMessageInGUI("Paused...", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
    }else{
        labelNotifierEmitter.showMessageInGUI("Running...", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
    }
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CurvesPanel;
    private javax.swing.JMenuItem MenuOpen;
    private javax.swing.JMenuItem MenuSave;
    private javax.swing.JLabel changeSignaturesLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JLabel loadLabel;
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JLabel pauseLabel;
    private javax.swing.JLabel reportLabel;
    private javax.swing.JLabel returnLabel;
    private javax.swing.JLabel saveLabel;
    private javax.swing.JLabel snapshotLabel;
    // End of variables declaration//GEN-END:variables

}
