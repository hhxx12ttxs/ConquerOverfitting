import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

public class ChartPanel extends JPanel {
  private List<Double> values;
  private ArrayList<Party> parties;

  private ArrayList<Color> partyColors;

  private String title;

  public ChartPanel(List<Double> values, ArrayList<Party> parties, String t) {
    this.values = new ArrayList<Double>();
    this.values.addAll(values);
    this.parties = new ArrayList<Party>();
    this.parties.addAll(parties);
    title = t;

    partyColors = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.DARK_GRAY));
  }

  private double scale = -1.0;

  public void update(List<Double> values, ArrayList<Party> parties, String t) {
    this.values.clear();
    this.values.addAll(values);
    this.parties.clear();
    this.parties.addAll(parties);
    title = t;
    this.repaint();
    this.revalidate();
    this.validate();
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (values == null || values.size() == 0)
      return;
    double minValue = 0;
    double maxValue = 0;
    for (int i = 0; i < values.size(); i++) {
      if (minValue > values.get(i))
        minValue = values.get(i);
      if (maxValue < values.get(i))
        maxValue = values.get(i);
    }

    Dimension d = getSize();
    int clientWidth = d.width;
    int clientHeight = d.height;
    int barWidth = clientWidth / values.size();
    Font titleFont = new Font("SansSerif", Font.BOLD, 20);
    FontMetrics titleFontMetrics = g.getFontMetrics(titleFont);
    Font labelFont = new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics labelFontMetrics = g.getFontMetrics(labelFont);

    int titleWidth = titleFontMetrics.stringWidth(title);
    int y = titleFontMetrics.getAscent();
    int x = (clientWidth - titleWidth) / 2;
    g.setFont(titleFont);
    g.drawString(title, x, y);

//    if(this.scale == -1.0) {
      int top = titleFontMetrics.getHeight();
      int bottom = labelFontMetrics.getHeight();
      if (maxValue == minValue)
        return;
      double scale = (clientHeight - top - bottom) / (maxValue - minValue);
      this.scale = scale;
//    }

    y = clientHeight - labelFontMetrics.getDescent();
    g.setFont(labelFont);

    for (int i = 0; i < values.size(); i++) {
      int valueX = i * barWidth + 1;
      int valueY = top;//clientHeight - 60;
      int height = (int) (values.get(i) * this.scale);
      if (values.get(i) >= 0)
        valueY += (int) ((maxValue - values.get(i)) * this.scale);
      else {
        valueY += (int) (maxValue * this.scale);
        height = -height;
      }
//      valueY -= height;

      Color color = Color.red;
      // check if current index in any party, if so then color it the corresponding color
      for(int p=0; p<parties.size(); p++) {
        ArrayList<Integer> party = parties.get(p).indexes;
        if(party.contains(i)) {
          color = partyColors.get(p%partyColors.size());
          break;
        }
      }
      g.setColor(color);
      g.fillRect(valueX, valueY, barWidth - 2, height);
      g.setColor(Color.black);
      g.drawRect(valueX, valueY, barWidth - 2, height);
      int labelWidth = labelFontMetrics.stringWidth(Integer.toString(i));
      x = i * barWidth + (barWidth - labelWidth) / 2;
      g.drawString(Integer.toString(i), x, y);

      // also draw value above each bar if it is greater than 1
      //if(values.get(i) > 1.0) {
//        RTextLine text = new RTextLine();
//        text.setDrawingComponent(this);
//        text.setText(Double.toString(values.get(i)));//Double.toString(Math.floor(values.get(i)*100.0)/100.0));
//        text.setColor(Color.BLACK);
//        text.setRotation(90);
//        text.draw(g, x + 10, valueY);
      //}
    }
  }
}
