package edu.haw.ttvp2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import de.uniba.wiai.lspi.chord.data.ID;

public class GUIPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Font fontStandard = new Font("Verdana", Font.BOLD, 10);
	private static final int boxWidth = 800;
	private static final int boxHeight = 15;
	private static final int enemiesPerColumn = 9;
	
//	private static final Color colorHitOwn = Color.RED;
//	private static final Color colorHitOthers = Color.ORANGE;
	
	private static final Color colorHit = Color.RED;
	private static final Color colorShip = Color.GREEN;
	private static final Color colorShipHit = Color.DARK_GRAY;
	private static final Color colorWater = Color.WHITE;
	
	
	
	private TTVP gameObject;
	
	public GUIPanel(TTVP gameObject) {
		this.gameObject = gameObject;
	}
	
	@Override 
	protected void paintComponent(Graphics g) { 
		Graphics2D g2 = (Graphics2D) g;     
        g2.setFont(fontStandard);
        
        // clear screen
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        // data
        g2.setPaint(Color.BLACK);
//        drawSettings(g2);
        drawOwnShip(g2, 15); // 70
        drawOwnShipIntervals(g2, 15 + 54); // 124
        
        int yOffsetStart = 15 + 54 + 41;
        int xOffset = 5, yOffset = yOffsetStart, nr = 1; // 165
        for (Enemy e : gameObject.enemyList.values()) {
        	drawEnemyShip(g2, xOffset, yOffset, nr++, e);
        	drawEnemyShipIntervals(g2, xOffset, yOffset + 54, e);
        	yOffset += 54 + 41;
        	if (nr == enemiesPerColumn + 1) {
        		xOffset += 890;
        		yOffset = yOffsetStart;
        	}
        }
	}
	
//	private void drawSettings(Graphics2D g2) {
//        g2.drawString("Settings:", 5, 15);
//        g2.drawLine(5, 16, 55, 16);    
//        g2.drawString("Intervals:", 5, 28);
//        g2.drawString(""+gameObject.interval, 65, 28);
//        g2.drawString("Ships:", 5, 38);
//        g2.drawString(""+gameObject.ships, 65, 38);
//        g2.drawString("Enemies:", 5, 48);
//        g2.drawString(""+gameObject.enemyList.size(), 65, 48);
//	}
	
	private void drawOwnShip(Graphics2D g2, int yOffset) {
        g2.drawString("Own Ship:", 5, yOffset);
        g2.drawLine(5, yOffset+1, 60, yOffset+1);    
        g2.drawString("ID:", 5, yOffset+13);
        g2.drawString(gameObject.node.getID().toString(), 65, yOffset+13);
        g2.drawString("StartID:", 5, yOffset+23);
        g2.drawString(""+ID.valueOf(gameObject.startID).toString(), 65, yOffset+23);
        g2.drawString("Intervals:", 5, yOffset+33);
        g2.drawString(""+(gameObject.interval - gameObject.shotsAtUs + gameObject.shotsAtUsMulti), 65, yOffset+33);
        g2.drawString("Ships:", 5, yOffset+43);
        g2.drawString(""+gameObject.shipsAlive, 65, yOffset+43);	
        
        g2.drawString("ID (num):", 450, yOffset+13);
        g2.drawString(gameObject.nodeID.toString(), 520, yOffset+13);
        g2.drawString("EndID:", 450, yOffset+23);
        g2.drawString(""+ID.valueOf(gameObject.nodeID).toString(), 520, yOffset+23);
        g2.drawString("Shots:", 450, yOffset+33);
        g2.drawString(""+gameObject.shots, 520, yOffset+33);	
        g2.drawString("ShotsAtUs:", 450, yOffset+43);
        g2.drawString(""+gameObject.shotsAtUs+" (Multi: "+gameObject.shotsAtUsMulti+")", 520, yOffset+43);	
	}
	
	private void drawOwnShipIntervals(Graphics2D g2, int yOffset) {
		int step = boxWidth / gameObject.interval;
		int x = 5;
		for (int i = 0; i < gameObject.interval; i++) {
			BigInteger interval = gameObject.intervals.get(i);
			Color col = colorWater;
			if (gameObject.hitList.get(interval) > 0) col = colorHit;
			Boolean ship = gameObject.shipList.get(interval);
			if (ship != null) {
				if (ship) col = colorShip;
				else col = colorShipHit;		
			}
			
			g2.setPaint(col);
			g2.fillRect(x + (i * step), yOffset, step, boxHeight);
			g2.setPaint(Color.BLACK);
			g2.drawRect(x + (i * step), yOffset, step, boxHeight);
		}
	}
	
	private void drawEnemyShip(Graphics2D g2, int xOffset, int yOffset, int nr, Enemy enemy) {
        g2.drawString("Enemy Ship #"+nr+":", xOffset, yOffset);
        g2.drawLine(xOffset, yOffset+1, xOffset + 75, yOffset+1);    
        g2.drawString("ID:", xOffset, yOffset+13);
        g2.drawString(enemy.id.toString(), xOffset + 60, yOffset+13);
        g2.drawString("StartID:", xOffset, yOffset+23);
        g2.drawString(""+ID.valueOf(enemy.start).toString(), xOffset + 60, yOffset+23);
        g2.drawString("Intervals:", xOffset, yOffset+33);
        g2.drawString(""+(enemy.maxHits - enemy.hits + enemy.hitsMulti), xOffset + 60, yOffset+33);
        g2.drawString("Ships:", xOffset, yOffset+43);
        g2.drawString(""+enemy.ships, xOffset + 60, yOffset+43);	
        
        g2.drawString("IP:", xOffset + 445, yOffset+13);
        g2.drawString(enemy.url.toString(), xOffset + 515, yOffset+13);
        g2.drawString("EndID:", xOffset + 445, yOffset+23);
        g2.drawString(""+ID.valueOf(enemy.end).toString(), xOffset + 515, yOffset+23);
        g2.drawString("ShotsAt:", xOffset + 445, yOffset+33);
        g2.drawString(""+enemy.hits+" (Multi: "+enemy.hitsMulti+")", xOffset + 515, yOffset+33);	
        g2.drawString("Hitchance:", xOffset + 445, yOffset+43);
        g2.drawString(""+(enemy.hitChance*100)+"%", xOffset + 515, yOffset+43);	
	}
	
	private void drawEnemyShipIntervals(Graphics2D g2, int xOffset, int yOffset, Enemy enemy) {
		int step = boxWidth / enemy.maxHits;
		int x = xOffset;
		List<BigInteger> intervals = new ArrayList<BigInteger>(enemy.hitMap.keySet());
		for (int i = 0; i < enemy.maxHits; i++) {
			BigInteger interval = intervals.get(i);
			Color col = colorWater;		
			Integer hit = enemy.hitMap.get(interval);
			if (hit != null) {
				if (hit == 1) col = colorShipHit;
				else if (hit == -1) col = colorHit;		
			}
			
			g2.setPaint(col);
			g2.fillRect(x + (i * step), yOffset, step, boxHeight);
			g2.setPaint(Color.BLACK);
			g2.drawRect(x + (i * step), yOffset, step, boxHeight);
		}		
	}
}

