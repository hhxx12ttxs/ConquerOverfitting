/**
 * 
 */
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Calculations;
import model.Stock;
import model.StocksModel;

/**
 * @author alexander
 * 
 * Class - InfoStock
 * 
 * Diese Klasse definiert den "Aktie Info"-Dialog.
 * Nutzung des GridBag-Layouts.
 * Nutzung der vorhanden Statistik-Methoden.
 */
public class InfoStock extends JFrame {
	private ArrayList<Stock> list = StocksModel.getInstance().getStocksList();
	
	/*
	 * Panel für Bestandsdaten
	 */
	private Component createInventoryDataPanel(int selectedRow) {
		JPanel inventoryData = new JPanel();
		inventoryData.setBorder(BorderFactory.createTitledBorder("Bestandsdaten"));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		inventoryData.setLayout(gridBagLayout);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.FIRST_LINE_START;
		gbc.anchor = GridBagConstraints.WEST;
		
		
		// WKN
		gbc.gridy = 1;
		
		JLabel labelWkn = new JLabel("WKN: ");
		JLabel labelWknValue = new JLabel();
			labelWknValue.setText(list.get(selectedRow).getWkn());
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelWkn, gbc);
		inventoryData.add(labelWkn);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelWknValue, gbc);
		inventoryData.add(labelWknValue);
		
		
		// Name
		gbc.gridy = 2;
		
		JLabel labelName = new JLabel("Name: ");
		JLabel labelNameValue = new JLabel();
			labelNameValue.setText(list.get(selectedRow).getName());
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelName, gbc);
		inventoryData.add(labelName);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelNameValue, gbc);
		inventoryData.add(labelNameValue);
		
		
		// Anzahl
		gbc.gridy = 3;
		
		JLabel labelQuantity = new JLabel("Anzahl: ");
		JLabel labelQuantityValue = new JLabel();
			labelQuantityValue.setText(Integer.toString(list.get(selectedRow).getQuantity()));
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelQuantity, gbc);	
		inventoryData.add(labelQuantity);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelQuantityValue, gbc);
		inventoryData.add(labelQuantityValue);
		
		
		// aktueller Preis
		gbc.gridy = 4;
		
		JLabel labelCurrentRate = new JLabel("aktueller Preis: ");
		JLabel labelCurrentRateValue = new JLabel();
			labelCurrentRateValue.setText(Calculations.getFormat(list.get(selectedRow).getCurRate()) + " €");
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelCurrentRate, gbc);	
		inventoryData.add(labelCurrentRate);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelCurrentRateValue, gbc);
		inventoryData.add(labelCurrentRateValue);
		
		
		// Leere Zeile
		gbc.gridy = 5;
		
		JLabel labelDummySpace1 = new JLabel(" ");
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelDummySpace1, gbc);
		inventoryData.add(labelDummySpace1);
		
		JLabel labelDummySpace2 = new JLabel(" ");
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelDummySpace2, gbc);
		inventoryData.add(labelDummySpace2);
		
		
		// Kauf-Preis
		gbc.gridy = 6;
		
		JLabel labelBuyRate = new JLabel("Kauf-Preis: ");
		JLabel labelBuyRateValue = new JLabel();
			labelBuyRateValue.setText(Calculations.getFormat(list.get(selectedRow).getBuyRate()) + " €");
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelBuyRate, gbc);	
		inventoryData.add(labelBuyRate);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelBuyRateValue, gbc);
		inventoryData.add(labelBuyRateValue);
		
		
		// Kauf-Datum
		gbc.gridy = 7;
		
		JLabel labelBuyDate = new JLabel("Kauf-Datum: ");
		JLabel labelBuyDateValue = new JLabel();
			labelBuyDateValue.setText(list.get(selectedRow).getBuyDate());
			
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelBuyDate, gbc);
		inventoryData.add(labelBuyDate);
		
		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelBuyDateValue, gbc);
		inventoryData.add(labelBuyDateValue);
		
		
		// Leere Zeile
		gbc.gridy = 8;
		
		JLabel labelDummySpace3 = new JLabel(" ");
		JLabel labelDummySpace4 = new JLabel(" ");
		
		gbc.gridx = 1;
		gridBagLayout.setConstraints(labelDummySpace3, gbc);
		inventoryData.add(labelDummySpace3);

		gbc.gridx = 2;
		gridBagLayout.setConstraints(labelDummySpace4, gbc);
		inventoryData.add(labelDummySpace4);
		
		// Anzeige der Verkaufsdaten wenn Aktie verkauft wurde
		if (list.get(selectedRow).isSold()){
			// Verkaufs-Preis
			gbc.gridy = 9;
			
			JLabel labelSellRate = new JLabel("Verkaufs-Preis: ");
			JLabel labelSellRateValue = new JLabel();
				labelSellRateValue.setText(Calculations.getFormat(list.get(selectedRow).getSellRate()) + " €");
			
			gbc.gridx = 1;
			gridBagLayout.setConstraints(labelSellRate, gbc);	
			inventoryData.add(labelSellRate);
			
			gbc.gridx = 2;
			gridBagLayout.setConstraints(labelSellRateValue, gbc);
			inventoryData.add(labelSellRateValue);
			
			// Verkaufs-Datum
			gbc.gridy = 10;
			
			JLabel labelSellDate = new JLabel("Verkaufs-Datum: ");
			JLabel labelSellDateValue = new JLabel();
				labelSellDateValue.setText(list.get(selectedRow).getSellDate());
				
			gbc.gridx = 1;
			gridBagLayout.setConstraints(labelSellDate, gbc);
			inventoryData.add(labelSellDate);
			
			gbc.gridx = 2;
			gridBagLayout.setConstraints(labelSellDateValue, gbc);
			inventoryData.add(labelSellDateValue);
		} else {
			// Leere Zeile
			gbc.gridy = 9;
			
			JLabel labelDummySpace5 = new JLabel(" ");
			JLabel labelDummySpace6 = new JLabel(" ");
			
			gbc.gridx = 1;
			gridBagLayout.setConstraints(labelDummySpace5, gbc);
			inventoryData.add(labelDummySpace5);

			gbc.gridx = 2;
			gridBagLayout.setConstraints(labelDummySpace6, gbc);
			inventoryData.add(labelDummySpace6);
			
			
			// Leere Zeile
			gbc.gridy = 10;
			
			JLabel labelDummySpace7 = new JLabel(" ");
			JLabel labelDummySpace8 = new JLabel(" ");
			
			gbc.gridx = 1;
			gridBagLayout.setConstraints(labelDummySpace7, gbc);
			inventoryData.add(labelDummySpace7);

			gbc.gridx = 2;
			gridBagLayout.setConstraints(labelDummySpace8, gbc);
			inventoryData.add(labelDummySpace8);
		}
		
		return inventoryData;
	}
	
	/*
	 * Panel für Performance
	 */
	private Component createPerformancePanel(int selectedRow) {
		JPanel performancePanel = new JPanel();
		performancePanel.setBorder(BorderFactory.createTitledBorder("Differenz"));
		performancePanel.setSize(80, 40);
		
		GridLayout gridLayout = new GridLayout(1,2);
		performancePanel.setLayout(gridLayout);
		
		// aktuelle Performance
		JLabel labelPerformanceValue = new JLabel();
		// Aufruf der getPerformance(int) von StocksModel
		if (Calculations.getCurrentSpread(selectedRow) > 0.0)
			labelPerformanceValue.setForeground(Color.GREEN);
		else if (Calculations.getCurrentSpread(selectedRow) == 0.0)
			labelPerformanceValue.setForeground(Color.BLACK);
		else
			labelPerformanceValue.setForeground(Color.RED);
		labelPerformanceValue.setText(Calculations.getFormat(Calculations.getCurrentSpread(selectedRow)) +
				" € (" + Calculations.getFormat(Calculations.getCurrentSpreadPercent(selectedRow)) + " %)");
			performancePanel.add(labelPerformanceValue);
		
		return performancePanel;
	}
	
	/*
	 * Panel für Gesamtwerte
	 */
	private Component createValuePanel(int selectedRow) {		
		JPanel valuePanel = new JPanel();
		valuePanel.setBorder(BorderFactory.createTitledBorder("Aktienwert"));
		valuePanel.setSize(80, 40);
		
		GridLayout gridLayout = new GridLayout(3,2);
		valuePanel.setLayout(gridLayout);
		
		// aktueller Wert
		JLabel labelCurrentValue = new JLabel("aktueller Wert: ");
			valuePanel.add(labelCurrentValue);
			
		JLabel labelCurrentValueContent = new JLabel();
		labelCurrentValueContent.setText(Calculations.getFormat(Calculations.getCurrentValue(selectedRow)) + " €");
		labelCurrentValueContent.setToolTipText("Zeigt den aktuellen Wert der Aktie an");
			valuePanel.add(labelCurrentValueContent);
				
		// Kauf-Preis
		JLabel labelBuyVolume = new JLabel("Kaufwert: ");
			valuePanel.add(labelBuyVolume);
		
		JLabel labelBuyVolumeValue = new JLabel();
		labelBuyVolumeValue.setText(Calculations.getFormat(Calculations.getBuyValue(selectedRow)) + " €");
		labelBuyVolumeValue.setToolTipText("Zeigt den gesamten Kaufwert der ausgewählten Aktie an");
			valuePanel.add(labelBuyVolumeValue);

		// Change
		JLabel labelChangeVolume = new JLabel("Differenz: ");
			valuePanel.add(labelChangeVolume);
		
		JLabel labelChangeVolumeValue = new JLabel();
		labelChangeVolumeValue.setText(Calculations.getFormat(Calculations.getCurrentValueDifference(selectedRow)) + " €");
		labelChangeVolumeValue.setToolTipText("Zeigt die Differnez vom Kaufwert zum aktuellen Wert der ausgewählten Aktie an");
			valuePanel.add(labelChangeVolumeValue);
			
		return valuePanel;
	}
	
	/*
	 * Panel für Gewinn und Verlust
	 */
	private Component createWinLossPanel(int selectedRow) {
		JPanel winlossPanel = new JPanel();
		winlossPanel.setBorder(BorderFactory.createTitledBorder("GuV"));
		winlossPanel.setSize(80, 40);
		
		// Layout
		GridLayout gridLayout = new GridLayout(2,2);
		winlossPanel.setLayout(gridLayout);
		
		// Verkaufs-Preis
		JLabel labelSellRateVolume = new JLabel("Verkaufswert: ");
			winlossPanel.add(labelSellRateVolume);
		
		JLabel labelSellRateVolumeValue = new JLabel();
			labelSellRateVolumeValue.setText(Calculations.getFormat(Calculations.getSellValue(selectedRow)) + " €");
			winlossPanel.add(labelSellRateVolumeValue);
			
		// Change
		JLabel labelSellVolumeChange = new JLabel("Differenz: ");
			winlossPanel.add(labelSellVolumeChange);
			
		JLabel labelSellVolumeChangeValue = new JLabel();
		
		if (Calculations.getSellVolumeChangePercent(selectedRow) > 0.0)
			labelSellVolumeChangeValue.setForeground(Color.GREEN);
		else if (Calculations.getCurrentSpread(selectedRow) == 0.0)
			labelSellVolumeChangeValue.setForeground(Color.BLACK);
		else
			labelSellVolumeChangeValue.setForeground(Color.RED);
		
			labelSellVolumeChangeValue.setText(Calculations.getFormat(Calculations.getSellValueDifference(selectedRow)) + " € (" + Calculations.getFormat(Calculations.getSellVolumeChangePercent(selectedRow)) + " %)");
			winlossPanel.add(labelSellVolumeChangeValue);
		
		return winlossPanel;
	}
	
	/*
	 * Methode erzeugt eine ButtonBar und gibt diese zurueck
	 */
	private JPanel createButtonBar() {
		JPanel panel;
		JButton buttonClose = new JButton("Schließen");
		
		FlowLayout layout = new FlowLayout();
		
		this.setLayout(layout);
		this.add(buttonClose);
		
		panel = new JPanel(layout);
		
		// Action "Save" (Fenster schließen)
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				setVisible(false);
			}
		});
		
		return panel;
	}
	
	/*
	 * ContentPanel zum Zusammenfügen der vier einzelen Panels
	 */
	private Component createInfoStockLayout(int selectedRow) {
		JPanel infoStockLayout = new JPanel();
		GridBagLayout gridBag = new GridBagLayout();
		infoStockLayout.setLayout(gridBag);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_START;
		
		Component createInventoryDataPanel = createInventoryDataPanel(selectedRow);
		Component createPerformancePanel = createPerformancePanel(selectedRow);
		Component createVolumePanel = createValuePanel(selectedRow);
		Component createWinLossPanel = createWinLossPanel(selectedRow);
		
		gbc.gridy = 1;
		gbc.gridy = 1;
		gbc.gridheight = 3;
		gridBag.setConstraints(createInventoryDataPanel, gbc);
		infoStockLayout.add(createInventoryDataPanel);
		
		gbc.gridheight = 1;
		
		if (!list.get(selectedRow).isSold()) {
			gbc.gridy = 2;
			gbc.gridy = 1;
			gridBag.setConstraints(createPerformancePanel, gbc);
			infoStockLayout.add(createPerformancePanel);
		}
			
		gbc.gridy = 2;
		gbc.gridy = 2;
		gridBag.setConstraints(createVolumePanel, gbc);
		infoStockLayout.add(createVolumePanel);
		
		// Wenn Aktie bereits verkauft wurde, dann wird das Panel "Gewinn und Verlust" angezeigt
		if (list.get(selectedRow).isSold()) {
			gbc.gridy = 2;
			gbc.gridy = 3;
			gridBag.setConstraints(createWinLossPanel, gbc);
			infoStockLayout.add(createWinLossPanel);
		}
		
		return infoStockLayout;
	}
	
	/*
	 * Konstruktor der Klasse
	 */
	public InfoStock(String title, int x, int y, int selectedRow) {
		if (selectedRow < 0)
			MessageDialogs.createErrorDialog("Fehler", "Sie haben keine Aktie ausgewählt.");
		else {
			this.setTitle(title);
			this.setSize(x, y);
			// zentrale Positionierung des Fensters
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			
			// Aufruf der createLayout-Methode zum Hinzufügen der Panels
			this.add(createInfoStockLayout(selectedRow), BorderLayout.NORTH);
			this.add(createButtonBar(), BorderLayout.SOUTH);
		}
	}
}

