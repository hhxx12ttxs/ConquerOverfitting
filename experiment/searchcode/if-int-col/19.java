/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Grid.java
 *
 * Created on 06-Jan-2009, 15:55:11
 */
package ga.robot;

import ga.Util.SquareType;

/**
 *
 * @author mark
 */
public class Grid extends javax.swing.JFrame {

    GameSquare[][] gameSquares;

    /** Creates new form Grid */
    public Grid() {
        initComponents();
        gameSquares = new GameSquare[8][8];

        gameSquares[0][0] = gameSquare1_1;
        gameSquares[0][1] = gameSquare1_2;
        gameSquares[0][2] = gameSquare1_3;
        gameSquares[0][3] = gameSquare1_4;
        gameSquares[0][4] = gameSquare1_5;
        gameSquares[0][5] = gameSquare1_6;
        gameSquares[0][6] = gameSquare1_7;
        gameSquares[0][7] = gameSquare1_8;

        gameSquares[1][0] = gameSquare2_1;
        gameSquares[1][1] = gameSquare2_2;
        gameSquares[1][2] = gameSquare2_3;
        gameSquares[1][3] = gameSquare2_4;
        gameSquares[1][4] = gameSquare2_5;
        gameSquares[1][5] = gameSquare2_6;
        gameSquares[1][6] = gameSquare2_7;
        gameSquares[1][7] = gameSquare2_8;

        gameSquares[2][0] = gameSquare3_1;
        gameSquares[2][1] = gameSquare3_2;
        gameSquares[2][2] = gameSquare3_3;
        gameSquares[2][3] = gameSquare3_4;
        gameSquares[2][4] = gameSquare3_5;
        gameSquares[2][5] = gameSquare3_6;
        gameSquares[2][6] = gameSquare3_7;
        gameSquares[2][7] = gameSquare3_8;

        gameSquares[3][0] = gameSquare4_1;
        gameSquares[3][
                1] = gameSquare4_2;
        gameSquares[3][2] = gameSquare4_3;
        gameSquares[3][3] = gameSquare4_4;
        gameSquares[3][4] = gameSquare4_5;
        gameSquares[3][5] = gameSquare4_6;
        gameSquares[3][6] = gameSquare4_7;
        gameSquares[3][7] = gameSquare4_8;

        gameSquares[4][0] = gameSquare5_1;
        gameSquares[4][1] = gameSquare5_2;
        gameSquares[4][2] = gameSquare5_3;
        gameSquares[4][3] = gameSquare5_4;
        gameSquares[4][4] = gameSquare5_5;
        gameSquares[4][5] = gameSquare5_6;
        gameSquares[4][6] = gameSquare5_7;
        gameSquares[4][7] = gameSquare5_8;

        gameSquares[5][0] = gameSquare6_1;
        gameSquares[5][1] = gameSquare6_2;
        gameSquares[5][2] = gameSquare6_3;
        gameSquares[5][3] = gameSquare6_4;
        gameSquares[5][4] = gameSquare6_5;
        gameSquares[5][5] = gameSquare6_6;
        gameSquares[5][6] = gameSquare6_7;
        gameSquares[5][7] = gameSquare6_8;

        gameSquares[6][0] = gameSquare7_1;
        gameSquares[6][1] = gameSquare7_2;
        gameSquares[6][2] = gameSquare7_3;
        gameSquares[6][3] = gameSquare7_4;
        gameSquares[6][4] = gameSquare7_5;
        gameSquares[6][5] = gameSquare7_6;
        gameSquares[6][6] = gameSquare7_7;
        gameSquares[6][7] = gameSquare7_8;

        gameSquares[7][0] = gameSquare8_1;
        gameSquares[7][1] = gameSquare8_2;
        gameSquares[7][2] = gameSquare8_3;
        gameSquares[7][3] = gameSquare8_4;
        gameSquares[7][4] = gameSquare8_5;
        gameSquares[7][5] = gameSquare8_6;
        gameSquares[7][6] = gameSquare8_7;
        gameSquares[7][7] = gameSquare8_8;

    }

    public String toString() {
        String out = "";
        for (int row = 0; row < gameSquares.length; row++) {
            int cols = gameSquares[row].length;
            for (int col = 0; col < cols; col++) {
                out += getSquare(row, col);
            }
            out += "\n";
        }
        return out;
    }
  
    public int getSquare(int row, int col) {
        SquareType type = gameSquares[row][col].getType();
        switch (type) {
            case EMPTY:
                return 0;
            case OK:
                return 1;
            case ROBOT_EMPTY:
                return 2;
            case ROBOT_OK:
                return 3;
        }
        return -1;
    }

    public String getSquareAsString(int row, int col) {
        return gameSquares[row][col].getType().toString();
    }

    public void setSquare(int row, int col, SquareType type) {
        gameSquares[row][col].setType(type);
        this.repaint();
    }

    public void setSquare(int row, int col, String type) {
        SquareType s = SquareType.EMPTY;
        if (type.equals("EMPTY")) {
            s = SquareType.EMPTY;
        }

        if (type.equals("OK")) {
            s = SquareType.OK;
        }
        if (type.equals("ROBOT_EMPTY")) {
            s = SquareType.ROBOT_EMPTY;
        }
        if (type.equals("ROBOT_OK")) {
            s = SquareType.ROBOT_OK;
        }
        gameSquares[row][col].setType(s);
        this.repaint();
    }
    private int[][] gridToResetTo;

    public void resetGrid() {
        for (int row = 0; row < gridToResetTo.length; row++) {
            for (int col = 0; col < gridToResetTo[row].length; col++) {
                setSquare(gridToResetTo, row, col);
            }
        }


    }

    private void setSquare(int[][] grid, int row, int col) {
        if (grid[row][col] == 0) {
            gameSquares[row][col].setType(SquareType.EMPTY);

        }
        if (grid[row][col] == 1) {
            gameSquares[row][col].setType(SquareType.OK);
        }
        if (grid[row][col] == 2) {
            gameSquares[row][col].setType(SquareType.ROBOT_EMPTY);
        }
        if (grid[row][col] == 3) {
            gameSquares[row][col].setType(SquareType.ROBOT_OK);
        }
            this.repaint();
    }

    public void setGrid(int[][] grid) {
        gridToResetTo = new int[grid.length][grid[0].length];
        for (int row = 0; row < grid.length; row++) {

            for (int col = 0; col < grid[row].length; col++) {
                //copy to this variable so can easily be reset
                gridToResetTo[row][col] = grid[row][col];
                setSquare(grid, row, col);

            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        gameSquare1_1 = new ga.robot.GameSquare();
        gameSquare1_2 = new ga.robot.GameSquare();
        gameSquare1_3 = new ga.robot.GameSquare();
        gameSquare1_4 = new ga.robot.GameSquare();
        gameSquare1_5 = new ga.robot.GameSquare();
        gameSquare1_6 = new ga.robot.GameSquare();
        gameSquare1_7 = new ga.robot.GameSquare();
        gameSquare1_8 = new ga.robot.GameSquare();
        gameSquare2_1 = new ga.robot.GameSquare();
        gameSquare2_2 = new ga.robot.GameSquare();
        gameSquare2_3 = new ga.robot.GameSquare();
        gameSquare2_4 = new ga.robot.GameSquare();
        gameSquare2_5 = new ga.robot.GameSquare();
        gameSquare2_6 = new ga.robot.GameSquare();
        gameSquare2_7 = new ga.robot.GameSquare();
        gameSquare2_8 = new ga.robot.GameSquare();
        gameSquare3_1 = new ga.robot.GameSquare();
        gameSquare3_2 = new ga.robot.GameSquare();
        gameSquare3_3 = new ga.robot.GameSquare();
        gameSquare3_4 = new ga.robot.GameSquare();
        gameSquare3_5 = new ga.robot.GameSquare();
        gameSquare3_6 = new ga.robot.GameSquare();
        gameSquare3_7 = new ga.robot.GameSquare();
        gameSquare3_8 = new ga.robot.GameSquare();
        gameSquare4_1 = new ga.robot.GameSquare();
        gameSquare4_2 = new ga.robot.GameSquare();
        gameSquare4_3 = new ga.robot.GameSquare();
        gameSquare4_4 = new ga.robot.GameSquare();
        gameSquare4_5 = new ga.robot.GameSquare();
        gameSquare4_6 = new ga.robot.GameSquare();
        gameSquare4_7 = new ga.robot.GameSquare();
        gameSquare4_8 = new ga.robot.GameSquare();
        gameSquare5_1 = new ga.robot.GameSquare();
        gameSquare5_2 = new ga.robot.GameSquare();
        gameSquare5_3 = new ga.robot.GameSquare();
        gameSquare5_4 = new ga.robot.GameSquare();
        gameSquare5_5 = new ga.robot.GameSquare();
        gameSquare5_6 = new ga.robot.GameSquare();
        gameSquare5_7 = new ga.robot.GameSquare();
        gameSquare5_8 = new ga.robot.GameSquare();
        gameSquare6_1 = new ga.robot.GameSquare();
        gameSquare6_2 = new ga.robot.GameSquare();
        gameSquare6_3 = new ga.robot.GameSquare();
        gameSquare6_4 = new ga.robot.GameSquare();
        gameSquare6_5 = new ga.robot.GameSquare();
        gameSquare6_6 = new ga.robot.GameSquare();
        gameSquare6_7 = new ga.robot.GameSquare();
        gameSquare6_8 = new ga.robot.GameSquare();
        gameSquare7_1 = new ga.robot.GameSquare();
        gameSquare7_2 = new ga.robot.GameSquare();
        gameSquare7_3 = new ga.robot.GameSquare();
        gameSquare7_4 = new ga.robot.GameSquare();
        gameSquare7_5 = new ga.robot.GameSquare();
        gameSquare7_6 = new ga.robot.GameSquare();
        gameSquare7_7 = new ga.robot.GameSquare();
        gameSquare7_8 = new ga.robot.GameSquare();
        gameSquare8_1 = new ga.robot.GameSquare();
        gameSquare8_2 = new ga.robot.GameSquare();
        gameSquare8_3 = new ga.robot.GameSquare();
        gameSquare8_4 = new ga.robot.GameSquare();
        gameSquare8_5 = new ga.robot.GameSquare();
        gameSquare8_6 = new ga.robot.GameSquare();
        gameSquare8_7 = new ga.robot.GameSquare();
        gameSquare8_8 = new ga.robot.GameSquare();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(234, 161, 239));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(233, 214, 214));

        javax.swing.GroupLayout gameSquare1_1Layout = new javax.swing.GroupLayout(gameSquare1_1);
        gameSquare1_1.setLayout(gameSquare1_1Layout);
        gameSquare1_1Layout.setHorizontalGroup(
            gameSquare1_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_1Layout.setVerticalGroup(
            gameSquare1_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_2Layout = new javax.swing.GroupLayout(gameSquare1_2);
        gameSquare1_2.setLayout(gameSquare1_2Layout);
        gameSquare1_2Layout.setHorizontalGroup(
            gameSquare1_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_2Layout.setVerticalGroup(
            gameSquare1_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_3Layout = new javax.swing.GroupLayout(gameSquare1_3);
        gameSquare1_3.setLayout(gameSquare1_3Layout);
        gameSquare1_3Layout.setHorizontalGroup(
            gameSquare1_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_3Layout.setVerticalGroup(
            gameSquare1_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_4Layout = new javax.swing.GroupLayout(gameSquare1_4);
        gameSquare1_4.setLayout(gameSquare1_4Layout);
        gameSquare1_4Layout.setHorizontalGroup(
            gameSquare1_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_4Layout.setVerticalGroup(
            gameSquare1_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_5Layout = new javax.swing.GroupLayout(gameSquare1_5);
        gameSquare1_5.setLayout(gameSquare1_5Layout);
        gameSquare1_5Layout.setHorizontalGroup(
            gameSquare1_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_5Layout.setVerticalGroup(
            gameSquare1_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_6Layout = new javax.swing.GroupLayout(gameSquare1_6);
        gameSquare1_6.setLayout(gameSquare1_6Layout);
        gameSquare1_6Layout.setHorizontalGroup(
            gameSquare1_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_6Layout.setVerticalGroup(
            gameSquare1_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_7Layout = new javax.swing.GroupLayout(gameSquare1_7);
        gameSquare1_7.setLayout(gameSquare1_7Layout);
        gameSquare1_7Layout.setHorizontalGroup(
            gameSquare1_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_7Layout.setVerticalGroup(
            gameSquare1_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare1_8Layout = new javax.swing.GroupLayout(gameSquare1_8);
        gameSquare1_8.setLayout(gameSquare1_8Layout);
        gameSquare1_8Layout.setHorizontalGroup(
            gameSquare1_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare1_8Layout.setVerticalGroup(
            gameSquare1_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare2_1Layout = new javax.swing.GroupLayout(gameSquare2_1);
        gameSquare2_1.setLayout(gameSquare2_1Layout);
        gameSquare2_1Layout.setHorizontalGroup(
            gameSquare2_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_1Layout.setVerticalGroup(
            gameSquare2_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare2_2Layout = new javax.swing.GroupLayout(gameSquare2_2);
        gameSquare2_2.setLayout(gameSquare2_2Layout);
        gameSquare2_2Layout.setHorizontalGroup(
            gameSquare2_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_2Layout.setVerticalGroup(
            gameSquare2_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare2_3Layout = new javax.swing.GroupLayout(gameSquare2_3);
        gameSquare2_3.setLayout(gameSquare2_3Layout);
        gameSquare2_3Layout.setHorizontalGroup(
            gameSquare2_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_3Layout.setVerticalGroup(
            gameSquare2_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare2_4Layout = new javax.swing.GroupLayout(gameSquare2_4);
        gameSquare2_4.setLayout(gameSquare2_4Layout);
        gameSquare2_4Layout.setHorizontalGroup(
            gameSquare2_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_4Layout.setVerticalGroup(
            gameSquare2_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare2_5Layout = new javax.swing.GroupLayout(gameSquare2_5);
        gameSquare2_5.setLayout(gameSquare2_5Layout);
        gameSquare2_5Layout.setHorizontalGroup(
            gameSquare2_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_5Layout.setVerticalGroup(
            gameSquare2_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

  
        javax.swing.GroupLayout gameSquare2_6Layout = new javax.swing.GroupLayout(gameSquare2_6);
        gameSquare2_6.setLayout(gameSquare2_6Layout);
        gameSquare2_6Layout.setHorizontalGroup(
            gameSquare2_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_6Layout.setVerticalGroup(
            gameSquare2_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare2_7Layout = new javax.swing.GroupLayout(gameSquare2_7);
        gameSquare2_7.setLayout(gameSquare2_7Layout);
        gameSquare2_7Layout.setHorizontalGroup(
            gameSquare2_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_7Layout.setVerticalGroup(
            gameSquare2_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare2_8Layout = new javax.swing.GroupLayout(gameSquare2_8);
        gameSquare2_8.setLayout(gameSquare2_8Layout);
        gameSquare2_8Layout.setHorizontalGroup(
            gameSquare2_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare2_8Layout.setVerticalGroup(
            gameSquare2_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_1Layout = new javax.swing.GroupLayout(gameSquare3_1);
        gameSquare3_1.setLayout(gameSquare3_1Layout);
        gameSquare3_1Layout.setHorizontalGroup(
            gameSquare3_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_1Layout.setVerticalGroup(
            gameSquare3_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_2Layout = new javax.swing.GroupLayout(gameSquare3_2);
        gameSquare3_2.setLayout(gameSquare3_2Layout);
        gameSquare3_2Layout.setHorizontalGroup(
            gameSquare3_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_2Layout.setVerticalGroup(
            gameSquare3_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_3Layout = new javax.swing.GroupLayout(gameSquare3_3);
        gameSquare3_3.setLayout(gameSquare3_3Layout);
        gameSquare3_3Layout.setHorizontalGroup(
            gameSquare3_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_3Layout.setVerticalGroup(
            gameSquare3_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_4Layout = new javax.swing.GroupLayout(gameSquare3_4);
        gameSquare3_4.setLayout(gameSquare3_4Layout);
        gameSquare3_4Layout.setHorizontalGroup(
            gameSquare3_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_4Layout.setVerticalGroup(
            gameSquare3_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_5Layout = new javax.swing.GroupLayout(gameSquare3_5);
        gameSquare3_5.setLayout(gameSquare3_5Layout);
        gameSquare3_5Layout.setHorizontalGroup(
            gameSquare3_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_5Layout.setVerticalGroup(
            gameSquare3_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        

        javax.swing.GroupLayout gameSquare3_6Layout = new javax.swing.GroupLayout(gameSquare3_6);
        gameSquare3_6.setLayout(gameSquare3_6Layout);
        gameSquare3_6Layout.setHorizontalGroup(
            gameSquare3_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_6Layout.setVerticalGroup(
            gameSquare3_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_7Layout = new javax.swing.GroupLayout(gameSquare3_7);
        gameSquare3_7.setLayout(gameSquare3_7Layout);
        gameSquare3_7Layout.setHorizontalGroup(
            gameSquare3_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_7Layout.setVerticalGroup(
            gameSquare3_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare3_8Layout = new javax.swing.GroupLayout(gameSquare3_8);
        gameSquare3_8.setLayout(gameSquare3_8Layout);
        gameSquare3_8Layout.setHorizontalGroup(
            gameSquare3_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare3_8Layout.setVerticalGroup(
            gameSquare3_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_1Layout = new javax.swing.GroupLayout(gameSquare4_1);
        gameSquare4_1.setLayout(gameSquare4_1Layout);
        gameSquare4_1Layout.setHorizontalGroup(
            gameSquare4_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_1Layout.setVerticalGroup(
            gameSquare4_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_2Layout = new javax.swing.GroupLayout(gameSquare4_2);
        gameSquare4_2.setLayout(gameSquare4_2Layout);
        gameSquare4_2Layout.setHorizontalGroup(
            gameSquare4_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_2Layout.setVerticalGroup(
            gameSquare4_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_3Layout = new javax.swing.GroupLayout(gameSquare4_3);
        gameSquare4_3.setLayout(gameSquare4_3Layout);
        gameSquare4_3Layout.setHorizontalGroup(
            gameSquare4_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_3Layout.setVerticalGroup(
            gameSquare4_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_4Layout = new javax.swing.GroupLayout(gameSquare4_4);
        gameSquare4_4.setLayout(gameSquare4_4Layout);
        gameSquare4_4Layout.setHorizontalGroup(
            gameSquare4_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_4Layout.setVerticalGroup(
            gameSquare4_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_5Layout = new javax.swing.GroupLayout(gameSquare4_5);
        gameSquare4_5.setLayout(gameSquare4_5Layout);
        gameSquare4_5Layout.setHorizontalGroup(
            gameSquare4_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_5Layout.setVerticalGroup(
            gameSquare4_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare4_6Layout = new javax.swing.GroupLayout(gameSquare4_6);
        gameSquare4_6.setLayout(gameSquare4_6Layout);
        gameSquare4_6Layout.setHorizontalGroup(
            gameSquare4_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_6Layout.setVerticalGroup(
            gameSquare4_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_7Layout = new javax.swing.GroupLayout(gameSquare4_7);
        gameSquare4_7.setLayout(gameSquare4_7Layout);
        gameSquare4_7Layout.setHorizontalGroup(
            gameSquare4_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_7Layout.setVerticalGroup(
            gameSquare4_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare4_8Layout = new javax.swing.GroupLayout(gameSquare4_8);
        gameSquare4_8.setLayout(gameSquare4_8Layout);
        gameSquare4_8Layout.setHorizontalGroup(
            gameSquare4_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare4_8Layout.setVerticalGroup(
            gameSquare4_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare5_1Layout = new javax.swing.GroupLayout(gameSquare5_1);
        gameSquare5_1.setLayout(gameSquare5_1Layout);
        gameSquare5_1Layout.setHorizontalGroup(
            gameSquare5_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_1Layout.setVerticalGroup(
            gameSquare5_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare5_2Layout = new javax.swing.GroupLayout(gameSquare5_2);
        gameSquare5_2.setLayout(gameSquare5_2Layout);
        gameSquare5_2Layout.setHorizontalGroup(
            gameSquare5_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_2Layout.setVerticalGroup(
            gameSquare5_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare5_3Layout = new javax.swing.GroupLayout(gameSquare5_3);
        gameSquare5_3.setLayout(gameSquare5_3Layout);
        gameSquare5_3Layout.setHorizontalGroup(
            gameSquare5_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_3Layout.setVerticalGroup(
            gameSquare5_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare5_4Layout = new javax.swing.GroupLayout(gameSquare5_4);
        gameSquare5_4.setLayout(gameSquare5_4Layout);
        gameSquare5_4Layout.setHorizontalGroup(
            gameSquare5_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_4Layout.setVerticalGroup(
            gameSquare5_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare5_5Layout = new javax.swing.GroupLayout(gameSquare5_5);
        gameSquare5_5.setLayout(gameSquare5_5Layout);
        gameSquare5_5Layout.setHorizontalGroup(
            gameSquare5_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_5Layout.setVerticalGroup(
            gameSquare5_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare5_6Layout = new javax.swing.GroupLayout(gameSquare5_6);
        gameSquare5_6.setLayout(gameSquare5_6Layout);
        gameSquare5_6Layout.setHorizontalGroup(
            gameSquare5_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_6Layout.setVerticalGroup(
            gameSquare5_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare5_7Layout = new javax.swing.GroupLayout(gameSquare5_7);
        gameSquare5_7.setLayout(gameSquare5_7Layout);
        gameSquare5_7Layout.setHorizontalGroup(
            gameSquare5_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_7Layout.setVerticalGroup(
            gameSquare5_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare5_8Layout = new javax.swing.GroupLayout(gameSquare5_8);
        gameSquare5_8.setLayout(gameSquare5_8Layout);
        gameSquare5_8Layout.setHorizontalGroup(
            gameSquare5_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare5_8Layout.setVerticalGroup(
            gameSquare5_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare6_1Layout = new javax.swing.GroupLayout(gameSquare6_1);
        gameSquare6_1.setLayout(gameSquare6_1Layout);
        gameSquare6_1Layout.setHorizontalGroup(
            gameSquare6_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_1Layout.setVerticalGroup(
            gameSquare6_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare6_2Layout = new javax.swing.GroupLayout(gameSquare6_2);
        gameSquare6_2.setLayout(gameSquare6_2Layout);
        gameSquare6_2Layout.setHorizontalGroup(
            gameSquare6_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_2Layout.setVerticalGroup(
            gameSquare6_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare6_3Layout = new javax.swing.GroupLayout(gameSquare6_3);
        gameSquare6_3.setLayout(gameSquare6_3Layout);
        gameSquare6_3Layout.setHorizontalGroup(
            gameSquare6_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_3Layout.setVerticalGroup(
            gameSquare6_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare6_4Layout = new javax.swing.GroupLayout(gameSquare6_4);
        gameSquare6_4.setLayout(gameSquare6_4Layout);
        gameSquare6_4Layout.setHorizontalGroup(
            gameSquare6_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_4Layout.setVerticalGroup(
            gameSquare6_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare6_5Layout = new javax.swing.GroupLayout(gameSquare6_5);
        gameSquare6_5.setLayout(gameSquare6_5Layout);
        gameSquare6_5Layout.setHorizontalGroup(
            gameSquare6_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_5Layout.setVerticalGroup(
            gameSquare6_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare6_6Layout = new javax.swing.GroupLayout(gameSquare6_6);
        gameSquare6_6.setLayout(gameSquare6_6Layout);
        gameSquare6_6Layout.setHorizontalGroup(
            gameSquare6_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_6Layout.setVerticalGroup(
            gameSquare6_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare6_7Layout = new javax.swing.GroupLayout(gameSquare6_7);
        gameSquare6_7.setLayout(gameSquare6_7Layout);
        gameSquare6_7Layout.setHorizontalGroup(
            gameSquare6_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_7Layout.setVerticalGroup(
            gameSquare6_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare6_8Layout = new javax.swing.GroupLayout(gameSquare6_8);
        gameSquare6_8.setLayout(gameSquare6_8Layout);
        gameSquare6_8Layout.setHorizontalGroup(
            gameSquare6_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare6_8Layout.setVerticalGroup(
            gameSquare6_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare7_1Layout = new javax.swing.GroupLayout(gameSquare7_1);
        gameSquare7_1.setLayout(gameSquare7_1Layout);
        gameSquare7_1Layout.setHorizontalGroup(
            gameSquare7_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_1Layout.setVerticalGroup(
            gameSquare7_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare7_2Layout = new javax.swing.GroupLayout(gameSquare7_2);
        gameSquare7_2.setLayout(gameSquare7_2Layout);
        gameSquare7_2Layout.setHorizontalGroup(
            gameSquare7_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_2Layout.setVerticalGroup(
            gameSquare7_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare7_3Layout = new javax.swing.GroupLayout(gameSquare7_3);
        gameSquare7_3.setLayout(gameSquare7_3Layout);
        gameSquare7_3Layout.setHorizontalGroup(
            gameSquare7_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_3Layout.setVerticalGroup(
            gameSquare7_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare7_4Layout = new javax.swing.GroupLayout(gameSquare7_4);
        gameSquare7_4.setLayout(gameSquare7_4Layout);
        gameSquare7_4Layout.setHorizontalGroup(
            gameSquare7_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_4Layout.setVerticalGroup(
            gameSquare7_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare7_5Layout = new javax.swing.GroupLayout(gameSquare7_5);
        gameSquare7_5.setLayout(gameSquare7_5Layout);
        gameSquare7_5Layout.setHorizontalGroup(
            gameSquare7_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_5Layout.setVerticalGroup(
            gameSquare7_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        
        javax.swing.GroupLayout gameSquare7_6Layout = new javax.swing.GroupLayout(gameSquare7_6);
        gameSquare7_6.setLayout(gameSquare7_6Layout);
        gameSquare7_6Layout.setHorizontalGroup(
            gameSquare7_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_6Layout.setVerticalGroup(
            gameSquare7_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare7_7Layout = new javax.swing.GroupLayout(gameSquare7_7);
        gameSquare7_7.setLayout(gameSquare7_7Layout);
        gameSquare7_7Layout.setHorizontalGroup(
            gameSquare7_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_7Layout.setVerticalGroup(
            gameSquare7_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare7_8Layout = new javax.swing.GroupLayout(gameSquare7_8);
        gameSquare7_8.setLayout(gameSquare7_8Layout);
        gameSquare7_8Layout.setHorizontalGroup(
            gameSquare7_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare7_8Layout.setVerticalGroup(
            gameSquare7_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_1Layout = new javax.swing.GroupLayout(gameSquare8_1);
        gameSquare8_1.setLayout(gameSquare8_1Layout);
        gameSquare8_1Layout.setHorizontalGroup(
            gameSquare8_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_1Layout.setVerticalGroup(
            gameSquare8_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_2Layout = new javax.swing.GroupLayout(gameSquare8_2);
        gameSquare8_2.setLayout(gameSquare8_2Layout);
        gameSquare8_2Layout.setHorizontalGroup(
            gameSquare8_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_2Layout.setVerticalGroup(
            gameSquare8_2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_3Layout = new javax.swing.GroupLayout(gameSquare8_3);
        gameSquare8_3.setLayout(gameSquare8_3Layout);
        gameSquare8_3Layout.setHorizontalGroup(
            gameSquare8_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_3Layout.setVerticalGroup(
            gameSquare8_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_4Layout = new javax.swing.GroupLayout(gameSquare8_4);
        gameSquare8_4.setLayout(gameSquare8_4Layout);
        gameSquare8_4Layout.setHorizontalGroup(
            gameSquare8_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_4Layout.setVerticalGroup(
            gameSquare8_4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_5Layout = new javax.swing.GroupLayout(gameSquare8_5);
        gameSquare8_5.setLayout(gameSquare8_5Layout);
        gameSquare8_5Layout.setHorizontalGroup(
            gameSquare8_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_5Layout.setVerticalGroup(
            gameSquare8_5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_6Layout = new javax.swing.GroupLayout(gameSquare8_6);
        gameSquare8_6.setLayout(gameSquare8_6Layout);
        gameSquare8_6Layout.setHorizontalGroup(
            gameSquare8_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_6Layout.setVerticalGroup(
            gameSquare8_6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_7Layout = new javax.swing.GroupLayout(gameSquare8_7);
        gameSquare8_7.setLayout(gameSquare8_7Layout);
        gameSquare8_7Layout.setHorizontalGroup(
            gameSquare8_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_7Layout.setVerticalGroup(
            gameSquare8_7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout gameSquare8_8Layout = new javax.swing.GroupLayout(gameSquare8_8);
        gameSquare8_8.setLayout(gameSquare8_8Layout);
        gameSquare8_8Layout.setHorizontalGroup(
            gameSquare8_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );
        gameSquare8_8Layout.setVerticalGroup(
            gameSquare8_8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare3_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare3_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare4_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare4_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare5_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare5_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare6_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare6_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare7_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare7_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(gameSquare8_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameSquare8_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(gameSquare1_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gameSquare1_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare1_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(gameSquare2_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(gameSquare2_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(228, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare1_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare1_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare2_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare2_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare3_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare3_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare4_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare4_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare5_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare5_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare6_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare6_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare7_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare7_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameSquare8_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameSquare8_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 610, 410));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Grid().setVisible(true);
            }
        });
    }

    public int getGridHeight() {
        return this.gameSquares.length;
    }

    public int getGridWidth() {
        return this.gameSquares[0].length;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ga.robot.GameSquare gameSquare1_1;
    private ga.robot.GameSquare gameSquare1_2;
    private ga.robot.GameSquare gameSquare1_3;
    private ga.robot.GameSquare gameSquare1_4;
    private ga.robot.GameSquare gameSquare1_5;
    private ga.robot.GameSquare gameSquare1_6;
    private ga.robot.GameSquare gameSquare1_7;
    private ga.robot.GameSquare gameSquare1_8;
    private ga.robot.GameSquare gameSquare2_1;
    private ga.robot.GameSquare gameSquare2_2;
    private ga.robot.GameSquare gameSquare2_3;
    private ga.robot.GameSquare gameSquare2_4;
    private ga.robot.GameSquare gameSquare2_5;
    private ga.robot.GameSquare gameSquare2_6;
    private ga.robot.GameSquare gameSquare2_7;
    private ga.robot.GameSquare gameSquare2_8;
    private ga.robot.GameSquare gameSquare3_1;
    private ga.robot.GameSquare gameSquare3_2;
    private ga.robot.GameSquare gameSquare3_3;
    private ga.robot.GameSquare gameSquare3_4;
    private ga.robot.GameSquare gameSquare3_5;
    private ga.robot.GameSquare gameSquare3_6;
    private ga.robot.GameSquare gameSquare3_7;
    private ga.robot.GameSquare gameSquare3_8;
    private ga.robot.GameSquare gameSquare4_1;
    private ga.robot.GameSquare gameSquare4_2;
    private ga.robot.GameSquare gameSquare4_3;
    private ga.robot.GameSquare gameSquare4_4;
    private ga.robot.GameSquare gameSquare4_5;
    private ga.robot.GameSquare gameSquare4_6;
    private ga.robot.GameSquare gameSquare4_7;
    private ga.robot.GameSquare gameSquare4_8;
    private ga.robot.GameSquare gameSquare5_1;
    private ga.robot.GameSquare gameSquare5_2;
    private ga.robot.GameSquare gameSquare5_3;
    private ga.robot.GameSquare gameSquare5_4;
    private ga.robot.GameSquare gameSquare5_5;
    private ga.robot.GameSquare gameSquare5_6;
    private ga.robot.GameSquare gameSquare5_7;
    private ga.robot.GameSquare gameSquare5_8;
    private ga.robot.GameSquare gameSquare6_1;
    private ga.robot.GameSquare gameSquare6_2;
    private ga.robot.GameSquare gameSquare6_3;
    private ga.robot.GameSquare gameSquare6_4;
    private ga.robot.GameSquare gameSquare6_5;
    private ga.robot.GameSquare gameSquare6_6;
    private ga.robot.GameSquare gameSquare6_7;
    private ga.robot.GameSquare gameSquare6_8;
    private ga.robot.GameSquare gameSquare7_1;
    private ga.robot.GameSquare gameSquare7_2;
    private ga.robot.GameSquare gameSquare7_3;
    private ga.robot.GameSquare gameSquare7_4;
    private ga.robot.GameSquare gameSquare7_5;
    private ga.robot.GameSquare gameSquare7_6;
    private ga.robot.GameSquare gameSquare7_7;
    private ga.robot.GameSquare gameSquare7_8;
    private ga.robot.GameSquare gameSquare8_1;
    private ga.robot.GameSquare gameSquare8_2;
    private ga.robot.GameSquare gameSquare8_3;
    private ga.robot.GameSquare gameSquare8_4;
    private ga.robot.GameSquare gameSquare8_5;
    private ga.robot.GameSquare gameSquare8_6;
    private ga.robot.GameSquare gameSquare8_7;
    private ga.robot.GameSquare gameSquare8_8;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

