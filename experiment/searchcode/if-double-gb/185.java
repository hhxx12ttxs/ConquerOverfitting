package client.gui;

import client.Configuration;
import communication.BandwidthConnection;
import constans.ConfigurationConstans;
import constans.SizeConstans;
import java.awt.Frame;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import utils.MachineUtils;
import utils.ScreenUtils;

public class PreferencesDialog extends javax.swing.JDialog {


    public PreferencesDialog(Frame parent, boolean modal, ResourceBundle resourceBundle,
            Configuration configuration, Logger logger) {
        super(parent, modal);
        initAttributes(resourceBundle,configuration,logger);              
        initComponents();
        setMiddleScreen();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(rb.getString("rArcPreferences"));
        setResizable(false);

        jButton1.setText(rb.getString("button_cancel"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(rb.getString("button_apply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS));
        TitledBorder tb = BorderFactory.createTitledBorder(rb.getString("SetupMainConfigurationLabel5"));
        tb.setTitleFont(new java.awt.Font("Verdana", 1,12));
        jPanel1.setBorder(tb);

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel4.setText(rb.getString("SetupMainConfigurationLabel4") );

        jComboBox1.setFont(new java.awt.Font("Verdana", 0, 12));
        String dontKnow = rb.getString("SetupMainConfigurationLabelCombox");
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] {dontKnow, "56 Kb", "128 Kb", "512 Kb", "1 Mb", "2 Mb", "4 Mb", "8 Mb", "10 Mb", "100 Mb", "1 Gb","10 Gb" }));
        jComboBox1.setAutoscrolls(true);
        jComboBox1.setFocusCycleRoot(true);
        jComboBox1.setOpaque(false);
        BandwidthConnection [] arrayBC = BandwidthConnection.values();

        jComboBox1.setSelectedIndex(0);
        for(int i = 0 ;i < arrayBC.length;i++){
            if(arrayBC[i].equals(configuration.getBandwidth())){
                jComboBox1.setSelectedIndex(++i);
                break;
            }
        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(246, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.PAGE_AXIS));
        tb = BorderFactory.createTitledBorder(rb.getString("SetupMainConfigurationLabel10"));
        tb.setTitleFont(new java.awt.Font("Verdana", 1,12));
        jPanel2.setBorder(tb);

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        double freeSpaceGBLabel7 = ((double)  MachineUtils.getFreeSpace(ConfigurationConstans.LOCAL_DIRECTORY)/SizeConstans.GByte);
        jLabel7.setText(df.format(freeSpaceGBLabel7)+" GB");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("0,1 GB");

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        double freeSpaceGBLabel8 = ((double)  MachineUtils.getFreeSpace(ConfigurationConstans.LOCAL_DIRECTORY)/SizeConstans.GByte);
        double offeredSpace = ((double) configuration.getOfferSpace()/SizeConstans.GByte);

        freeSpaceGBLabel8+=offeredSpace;
        jLabel8.setText(df.format(freeSpaceGBLabel8)+" GB");

        jLabel6.setText(rb.getString("SetupMainConfigurationLabel6"));
        jLabel6.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        double offerSpaceLabel3GB = ((double)configuration.getOfferSpace())/SizeConstans.GByte;
        jLabel3.setText(df.format(offerSpaceLabel3GB)+" GB");

        jLabel2.setText(rb.getString("SetupMainConfigurationLabel2"));
        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        double offerSpaceSlider1GB = ((double)configuration.getOfferSpace())/SizeConstans.GByte;
        int offerSpaceSlider1 = (int) Math.round(offerSpaceSlider1GB*10);
        jSlider1.setMinimum(1);
        double tempFreeSpaceSlider1GB = ((double)MachineUtils.getFreeSpace(ConfigurationConstans.LOCAL_DIRECTORY)/SizeConstans.GByte)*10;
        double offeredSpaceTeste = ((double) configuration.getOfferSpace()/SizeConstans.GByte)*10;

        tempFreeSpaceSlider1GB+=offeredSpaceTeste;

        int freeSpaceSlider1GB = (int)( tempFreeSpaceSlider1GB);

        jSlider1.setMaximum(freeSpaceSlider1GB);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(offerSpaceSlider1);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 378, Short.MAX_VALUE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)))
                .addGap(23, 23, 23))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void initAttributes(ResourceBundle resourceBundle, Configuration configuration,
            Logger logger) {
        this.rb = resourceBundle;
        this.configuration = configuration;
        this.logger = logger;
        this.df = new DecimalFormat("0.0");
    }

    
    private void setMiddleScreen(){
		ScreenUtils.setMiddleScreen(this);
	}

    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed


    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

            try {

                double offerSpaceGB =( (double) jSlider1.getValue()/10);
                long offerSpaceBytes = (long) ((offerSpaceGB) * SizeConstans.GByte);


                int index = jComboBox1.getSelectedIndex();
                BandwidthConnection bc = null;

                if(index > 0)
                    bc = BandwidthConnection.values()[index-1];
                else
                    bc = BandwidthConnection.KB_56;

                configuration.setOfferSpace(offerSpaceBytes);
                configuration.setBandwidth(bc);
                configuration.save(ConfigurationConstans.CONFIGURATION_FILENAME);

            } catch (IOException ex) {
                logger.log(Level.WARNING,ex.getMessage());
                JOptionPane.showMessageDialog(null,rb.getString("ProblemStoreConfiguration"),rb.getString("Preferences"), JOptionPane.WARNING_MESSAGE);
            }
              
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed


    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        double valueGB = ( (double) jSlider1.getValue()/10);
        jLabel3.setText(df.format(valueGB)+" GB");
}//GEN-LAST:event_jSlider1StateChanged

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSlider jSlider1;
    // End of variables declaration//GEN-END:variables
    private DecimalFormat df;
    private ResourceBundle rb;
    private Configuration configuration;
    private Logger logger;
    
}

