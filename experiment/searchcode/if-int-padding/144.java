package touch;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class TouchTableCanvas extends Canvas {
    /* Conteúdo da tabela */
    protected int numCols;
    protected int numRows; // não conta o cabeçalho da tabela
    protected String[] colNames;
    protected String[][] data;

    /* Cores da tabela */
    protected int backgroundColor;
    protected int headerBackgroundColor;
    protected int borderColor;
    protected int textColor;

    /* Viadagens pra desenhar */
    protected int padding; // espaçamento entre a borda e o conteúdo das células

    /* Posição do conteúdo da tabela */
    protected int scrollx;
    protected int scrolly;

    protected int pressedX = 0;
    protected int pressedY = 0;

    public TouchTableCanvas(String[] colNames, String[][] data) {
        this.colNames = colNames;
        this.numCols = colNames.length;
        this.data = data;
        this.numRows = data.length;

        scrollx = 0;
        scrolly = 0;

        // cores padrões
        backgroundColor = 0xFFFFFF;
        headerBackgroundColor = 0xCDCDCD;
        borderColor = 0x000000;
        textColor = 0x000000;

        // viadagens padrões
        padding = 5;
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);

        switch(gameAction) {
            case UP:
                scrolly += 5;
                break;
            case DOWN:
                scrolly -= 5;
                break;
            case LEFT:
                scrollx += 5;
                break;
            case RIGHT:
                scrollx -= 5;
                break;
        }

        repaint();
    }
    protected void pointerPressed(int x, int y){
        pressedX = x;
        pressedY = y;
    }
    protected void pointerReleased(int x, int y){
        scrollx += (x - pressedX);
        scrolly += (y - pressedY);
        this.repaint();
    }
    protected void pointerDragged(int x, int y){
        scrollx += (x-pressedX);
        scrolly += (y-pressedY);
        pressedX = x;
        pressedY = y;
        this.repaint();
        System.out.println("X = " + x + " Y = " + y);
    }

    protected void paintHeader(Graphics g) {
        int cont = 0;
        for(int i = 0; i < numCols-1; cont+=getColumnWidth(i++, g));
        if(scrollx > this.getWidth() - getColumnWidth(0, g)) scrollx = this.getWidth() - getColumnWidth(0, g);
        else if(scrollx < -cont) scrollx = -cont;
       
        int posx = scrollx, posy = 0;
        for(int i = 0; i < numCols; i++) {
            g.setColor(headerBackgroundColor);
            g.fillRect(posx, posy, getColumnWidth(i, g), getHeaderHeight(g));

            g.setColor(borderColor);
            g.drawLine(posx, posy, posx+getColumnWidth(i, g), posy);
            g.drawLine(posx, posy, posx, posy+getRowHeight(g));
            g.drawLine(posx+getColumnWidth(i, g), posy, posx+getColumnWidth(i, g), posy+getRowHeight(g));
            g.drawLine(posx, posy+getRowHeight(g), posx+getColumnWidth(i, g), posy+getRowHeight(g));

            g.setColor(textColor);
            g.drawString(colNames[i], posx+padding, posy+padding, g.TOP | g.LEFT);

            posx += getColumnWidth(i, g);
        }
    }

    protected void paintCells(Graphics g) {
        // Limite de deslocamento das colunas
        int cont = 0;
        for(int i = 0; i < numCols-1; cont+=getColumnWidth(i++, g));
        if(scrollx > this.getWidth() - getColumnWidth(0, g)) scrollx = this.getWidth() - getColumnWidth(0, g);
        else if(scrollx < -cont) scrollx = -cont;
        
        // Limite de deslocamento das linhas
        if(scrolly > this.getHeight()-2*getRowHeight(g)) scrolly = this.getHeight()-2*getRowHeight(g);
        else if(scrolly < -(numRows-1)*getRowHeight(g)) scrolly = -(numRows-1)*getRowHeight(g);

        int posx = scrollx;
        int posy = scrolly + getHeaderHeight(g);
        for(int j = 0; j < numRows; j++) {
            for(int i = 0; i < numCols; i++) {
                g.setColor(borderColor);
                g.drawLine(posx, posy, posx+getColumnWidth(i, g), posy);
                g.drawLine(posx, posy, posx, posy+getRowHeight(g));
                g.drawLine(posx+getColumnWidth(i, g), posy, posx+getColumnWidth(i, g), posy+getRowHeight(g));
                g.drawLine(posx, posy+getRowHeight(g), posx+getColumnWidth(i, g), posy+getRowHeight(g));

                g.setColor(textColor);
                g.drawString(data[j][i], posx+padding, posy+padding, g.TOP | g.LEFT);

                posx += getColumnWidth(i, g);
            }
            posy += getRowHeight(g);
            posx = scrollx;
        }
    }

    protected void paint(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintCells(g);
        paintHeader(g);
    }

    public void setBackgroundColor(int r, int g, int b) {
        backgroundColor = (r << 16) + (g << 8) + b;
    }

    public void setHeaderBackgroundColor(int r, int g, int b) {
        headerBackgroundColor = (r << 16) + (g << 8) + b;
    }

    public void setBorderColor(int r, int g, int b) {
        borderColor = (r << 16) + (g << 8) + b;
    }

    public void setTextColor(int r, int g, int b) {
        textColor = (r << 16) + (g << 8) + b;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    protected int getColumnWidth(int columnIndex, Graphics g) {
        int columnWidth = g.getFont().stringWidth(colNames[columnIndex]);
        for(int i = 0; i < numRows; i++)
            columnWidth = Math.max(columnWidth, g.getFont().stringWidth(data[i][columnIndex]));
        return columnWidth + 2*padding;
    }

    protected int getRowHeight(Graphics g) {
        return g.getFont().getHeight() + 2*padding;
    }

    protected int getHeaderHeight(Graphics g) {
        return getRowHeight(g);
    }
}

