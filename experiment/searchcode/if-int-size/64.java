/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessLogic;

/**
 *
 * @author SONY
 */
import DTO.Zone;
import DataAccess.DataUtility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.JPanel;

public class DrawChart extends JPanel {

    /**
     * array contain values to draw
     */
    private double[] values;

    /**
     * vector list zone used to load Zone from database
     */
    private Vector<Zone> listZone;
    String date = "";
    /**
     * array day use to get date from today. Ex: 08/21/2010 - day[0] = 08/11/2010
     */
    private int[] day = {10, 6, 3, 0};

    /**
     * initial DrawChart and load list Zone
     */
    public DrawChart() {
        //load list zone
        loadZone();
        setBackground(Color.WHITE);
    }
    /**
     * Load list zone from database
     */
    public void loadZone() {
        listZone = new ZoneManagement().loadZone();
    }

    /**
     * compute array double percent. (numberPrent/total)*100
     * @param numberDay
     */
    public void loadValues(int numberDay) {
        //get date
        date = DateUtility.subToDate(Calendar.getInstance(), numberDay);
        //initial array values
        values = new double[listZone.size()];
        
        for (int i = 0; i < listZone.size(); i++) {
            try {
                Zone zone = listZone.get(i);
                ResultSet rs = DataUtility.excuteQuery("select count(*) as number from booth where status = 1 and idzone = " + "\'" + zone.getIDzone() + "\'" + " and dateCreate <= " + "\'" + date + "\'");

                rs.next();
                int numberBooth = rs.getInt("number");
                values[i] = (numberBooth*100)/zone.getNumberBooth();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(listZone.size() == 0){
            return;
        }
        Dimension d = getSize();
        int clientWidth = d.width - 150;
        int clientHeight = d.height;
        //Max height of col
        int barHeight = clientHeight - 20;
        //width of col
        int barWidth = clientWidth/(4*listZone.size());//int barWidth = clientWidth / values.length;
        //1% = number pixel
        int scale = barHeight / 100;
        int valueX = 20 - barWidth;
        //int pointXDate = (listZone.size() * barWidth/4) + 25;
        for (int x = 0; x < 4; x++) {
            //load value follow date
            loadValues(day[x]);
            valueX += 15;
            //Draw date for group bar
            //pointXDate += (x * barWidth * values.length) + 15;
            g.drawString(date, valueX + barWidth + 30, barHeight + 15);
            //Draw a group bar
            for (int i = 0; i < values.length; i++) {
                //compute height of this bar
                int height = (int) (values[i] * scale);

                valueX += barWidth;
                int valueY = barHeight - height;

                g.setColor(setColor(i));
                g.fillRect(valueX, valueY, barWidth, height);
                g.setColor(Color.black);
                g.drawRect(valueX, valueY, barWidth, height);
                //draw lable value of bar
                String strValue = values[i] + "%";
                g.drawString(strValue, valueX + 2, valueY - 5);

            }
        }
        //draw 2 vertex x and y
        g.setColor(Color.black);
        g.drawLine(25, barHeight, valueX + barWidth + 20, barHeight);//x
        g.drawLine(25, barHeight, 25, 0);//y
        //set value for vertex y
        for(int i = 0; i <= 5; i++){
            g.drawString("" + (20*i), 5, barHeight - (i * scale * 20));
        }
        //
        for(int i = 0; i < listZone.size(); i++){
            g.setColor(setColor(i));
            g.fillRect(clientWidth + 100, (i * 30) + 50, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(clientWidth + 100, (i * 30) + 50, 20, 20);
            g.drawString(listZone.get(i).getName() , clientWidth + 125, (i * 30) + 64);
        }

    }

    /**
     *
     * @param index
     * @return a color
     */
    public Color setColor(int index) {
        switch (index) {
            case 0:
                return Color.cyan;
            case 1:
                return Color.YELLOW;
            case 2:
                return Color.pink;
            case 3:
                return Color.lightGray;
            case 4:
                return Color.GREEN;
            case 5:
                return Color.ORANGE;
            case 6:
                return Color.darkGray;
            case 7:
                return Color.magenta;
            default:
                return Color.BLUE;
        }
    }
}

