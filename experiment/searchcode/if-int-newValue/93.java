/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tnviet.proj.jna.meter;


import java.sql.SQLException;
import tnviet.proj.jna.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import org.jdesktop.swingx.JXButton;
import tnviet.proj.jna.utilities.ResourceManager;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.pushingpixels.trident.Timeline;
import tnviet.proj.jna.utilities.ComponentMover;

/**
 *
 * @author Administrator
 */
public class JNMeterPanel extends JDialog{
    JTextField inComingLabel;
    JTextField outGoingLabel;
    JXButton closeButton;
    JLayeredPane lPane;
    private ResourceManager resourceManager;
    ComponentMover mover;
    JNCaptor captor;
    javax.swing.Timer trafficUpdate, trafficDatabaseUpdate;
    Double inValue = 0.0d;
    Double outValue = 0.0d;
    Double currentMinuteInValue = 0.0d;
    Double currentMinuteOutValue = 0.0d;
    long inPacketsValue , outPacketsValue, currentMinuteInPacketsValue, currentMinuteOutPacketsValue;
    DecimalFormat format = new DecimalFormat("0.0");
    JNMeterChartPane chartPane;
    public Timeline closingEffect;
    int widthMeter;
    int heightMeter;

    private JNMeterPanel getInstace(){
        return this;
    }


    public void setHeightMeter(int newValue){
        this.heightMeter = newValue;
        this.setSize(this.getWidth(), heightMeter);
    }
    public void setWidthMeter(int newValue){
        this.widthMeter = newValue;
        this.setSize(widthMeter, this.getHeight());
    }



    public JNMeterPanel(){
        resourceManager = new tnviet.proj.jna.utilities.ResourceManager(this.getClass(), Program.locale);
        lPane = this.getLayeredPane();
        captor = JNCaptor.getInstance();

        setSize(220, 110);
        setPreferredSize(new Dimension(220, 110));
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setUndecorated(true);


        inComingLabel = new JTextField("");
        inComingLabel.setBackground(new Color(228, 233, 235));
        inComingLabel.setPreferredSize(new Dimension(95, 20));
        inComingLabel.setBounds(0, 75, 95, 16);
        inComingLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.gray, Color.white));
        inComingLabel.setEditable(false);
        inComingLabel.setHorizontalAlignment(JTextField.CENTER);
        inComingLabel.setForeground(Color.red);
        inComingLabel.setOpaque(true);
        inComingLabel.setFont(inComingLabel.getFont().deriveFont(10f));
        


        outGoingLabel = new JTextField("");
        outGoingLabel.setBackground(new Color(228, 233, 235));
        outGoingLabel.setPreferredSize(new Dimension(95, 20));
        outGoingLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.gray, Color.white));
        outGoingLabel.setBounds(106, 75, 95, 16);
        outGoingLabel.setEditable(false);
        outGoingLabel.setForeground(Color.blue);
        outGoingLabel.setOpaque(true);
        outGoingLabel.setHorizontalAlignment(JTextField.CENTER);
        outGoingLabel.setFont(outGoingLabel.getFont().deriveFont(10f));

        closeButton = new JXButton(resourceManager.createImageIcon("fileclose10.png", "Cancel"));
        closeButton.setBounds(180, 5, 20, 20);
        closeButton.setVisible(false);

        setIncomingText(inValue.toString() + " kbps");
        setOutGoingText(outValue.toString() + " kbps");
        chartPane = new JNMeterChartPane(120);
        chartPane.setBounds(-10, -2, 225, 67);
        JPanel translucentPanel = new JPanel();
        translucentPanel.setOpaque(false);
        translucentPanel.setBounds(-10, -2, 225, 67);
        //graphPane.add(chartPane);

        lPane.add(chartPane, new Integer(120));
        lPane.add(translucentPanel, new Integer(121));
        lPane.add(inComingLabel, new Integer(2));
        lPane.add(outGoingLabel, new Integer(3));
        lPane.add(closeButton, new Integer(122));

        trafficUpdate = new Timer(1000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                inValue = inValue + captor.bytesIn;
                inPacketsValue = inPacketsValue + captor.packetsInCount;
                outValue = outValue + captor.bytesOut;
                outPacketsValue = outPacketsValue + captor.packetsOutCount;
                captor.ResetCounter();
                //Kb
                inValue = inValue/128;
                currentMinuteInValue = currentMinuteInValue + inValue;
                currentMinuteInPacketsValue = currentMinuteInPacketsValue + inPacketsValue;
                outValue = outValue/128;
                currentMinuteOutValue = currentMinuteOutValue + outValue;
                currentMinuteOutPacketsValue = currentMinuteOutPacketsValue + outPacketsValue;
                chartPane.addObservation(inValue, outValue);
                setIncomingText(format.format(inValue) + " kbps");
                setOutGoingText(format.format(outValue) + " kbps");
                inValue = 0.0d;
                outValue = 0.0d;
                inPacketsValue = 0;
                outPacketsValue = 0;
            }
        });


        trafficDatabaseUpdate = new Timer(60000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    ResultSet rs = selectTraffic(Program.connection);
                    if(!rs.next()){
                        insertTraffic(Program.connection,currentMinuteInValue, currentMinuteOutValue, currentMinuteInPacketsValue, currentMinuteOutPacketsValue);
                    } else {
                        long id = rs.getLong(1);
                        updateTraffic(Program.connection, currentMinuteInValue, currentMinuteOutValue,currentMinuteInPacketsValue, currentMinuteOutPacketsValue,id);
                    }
                } catch (SQLException ex) {
                    System.out.println("error sql");
                }
                currentMinuteInValue = 0.0d;
                currentMinuteOutValue = 0.0d;
                currentMinuteInPacketsValue = 0;
                currentMinuteOutPacketsValue = 0;
            }
        });

        this.getRootPane().setBorder(new CompoundBorder(new DropShadowBorder(),
                new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
                new javax.swing.border.EmptyBorder(5, 5, 5, 5)) ));
        setMeterLocation();
        this.setMinimumSize(new Dimension(0, 0));
        this.setAlwaysOnTop(true);

        mover = new ComponentMover(this, this, inComingLabel, outGoingLabel, chartPane, translucentPanel);
        this.startUpdating();
        this.trafficDatabaseUpdate.setRepeats(true);
        this.trafficDatabaseUpdate.start();
        this.pack();

        closeButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Program.showItem.setSelected(false);
            }

        });
        this.addMouseListener( new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                int width = getWidth();
                int height = getHeight();
                if(mouseX < 0 || mouseY < 0 || mouseX > width || mouseY > height){
                    closeButton.setVisible(false);
                }
            }

        });
    }

    public void setIncomingText(String value){
        this.inComingLabel.setText(value);
    }

    public void setOutGoingText(String value){
        this.outGoingLabel.setText(value);
    }

    public void startUpdating(){
        trafficUpdate.setRepeats(true);
        trafficUpdate.start();
    }

    private ResultSet selectTraffic(Connection con) throws SQLException{
        StringBuilder selectString = new StringBuilder();
        selectString.append( "SELECT ID FROM PUBLIC.JNA_TRAFFIC WHERE ");
        selectString.append("CAST(TIME AS DATE) = CURRENT_DATE AND HOUR(TIME) = HOUR(NOW)");
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(selectString.toString());
        stm.close();
        return rs;
    }

    private int insertTraffic(Connection con, Double incoming, Double outgoing, Long inPackets, Long outPackets) throws SQLException{
        StringBuilder insertString = new StringBuilder();
        insertString.append("INSERT INTO PUBLIC.JNA_TRAFFIC (TIME, INCOMING, OUTGOING, IN_PACKETS, OUT_PACKETS ) VALUES( NOW,");
        insertString.append(incoming);
        insertString.append(",");
        insertString.append(outgoing);
        insertString.append(",");
        insertString.append(inPackets);
        insertString.append(",");
        insertString.append(outPackets);
        insertString.append(");");
        Statement stm = con.createStatement();
        return stm.executeUpdate(insertString.toString());
    }

    private int updateTraffic(Connection con, Double incoming, Double outgoing,Long inPackets, Long outPackets, long id) throws SQLException{
        StringBuilder updateString = new StringBuilder();
        updateString.append("UPDATE PUBLIC.JNA_TRAFFIC SET INCOMING = INCOMING + ");
        updateString.append(incoming);
        updateString.append(",");
        updateString.append("OUTGOING = OUTGOING + ");
        updateString.append(outgoing);
        updateString.append(",");
        updateString.append("IN_PACKETS = IN_PACKETS + ");
        updateString.append(inPackets);
        updateString.append(",");
        updateString.append("OUT_PACKETS = OUT_PACKETS + ");
        updateString.append(outPackets);
        updateString.append(" WHERE JNA_TRAFFIC.ID = ");
        updateString.append(id);
        Statement stm = con.createStatement();
        return stm.executeUpdate(updateString.toString());
    }
    private void setMeterLocation(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        int meterWinX = (int)size.getWidth() - 300;
        int meterWinY = (int)size.getHeight() - 200;
        String meterX = Program.JNProperty.getProperty("meter.winX",String.valueOf(meterWinX));
        Program.JNProperty.setProperty("meter.winX", meterX);
        String meterY = Program.JNProperty.getProperty("meter.winY",String.valueOf(meterWinY));
        Program.JNProperty.setProperty("meter.winY", meterY);
        this.setLocation(Integer.valueOf(meterX), Integer.valueOf(meterY));
    }
    public void storeMeterLocation(){
        int meterWinX = this.getLocation().x;
        int meterWinY = this.getLocation().y;
        Program.JNProperty.setProperty("meter.winX", String.valueOf(meterWinX));
        Program.JNProperty.setProperty("meter.winY", String.valueOf(meterWinY));
    }

}

