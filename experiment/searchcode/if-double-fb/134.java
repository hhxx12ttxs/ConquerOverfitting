package com.rocel.fabapp.vue.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.rocel.fabapp.controler.IControler;
import com.rocel.fabapp.vue.MainVue;

public class SearchDegatRep extends AbsSearch{
	private static final long serialVersionUID = 1L;

	private static SearchDegatRep instance = null;
	private static IControler controler;
	private static MainVue parent;

	private JTextField txtCout;
	private JTextField txtReparePar;
	private JTextPane txtObservation;
	
	private JCheckBox chbDateDegat;
	private JComboBox cbJourDate;
	private JComboBox cbMoisDate;
	private JComboBox cbAnneeDate;
	
	private JComboBox cbZone;
	private JComboBox cbImportance;
	private JComboBox cbBateau;
	private JComboBox cbType;
	private JComboBox cbRameur;

	public static SearchDegatRep getInstance(MainVue parentIN, IControler controlerIN){
		controler = controlerIN;
		parent = parentIN;
		if (instance == null){
			instance = new SearchDegatRep();
		}
		return instance;
	}

	private SearchDegatRep(){
		int i =0;
		setLayout(new BorderLayout());
		JLabel title = new JLabel("Rechercher Dégats / Réparations:");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		add(title,BorderLayout.NORTH);
		
		JPanel content = new JPanel();
		content.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("right:4dlu"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("max(75dlu;pref)"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(50dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		DocumentFilter docFilterOnlyDigits = new DocumentFilter() {
		    @Override
		    public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) 
		        throws BadLocationException {
		        fb.insertString(off, str.replaceAll("[a-zA-Z]", ""), attr);  // remove non-digits
		    } 
		    @Override
		    public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) 
		        throws BadLocationException {
		        fb.replace(off, len, str.replaceAll("[a-zA-Z]", ""), attr);  // remove non-digits
		    }
		};

		PlainDocument docCout = new PlainDocument();
		docCout.setDocumentFilter(docFilterOnlyDigits);
		
		JLabel lblNewLabel = new JLabel("Date du dégat :");
		content.add(lblNewLabel, "2, 2, right, default");
		
		JPanel panelDate = new JPanel();
		panelDate.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chbDateDegat = new JCheckBox("");
		panelDate.add(chbDateDegat, "1, 1");

		String[] jour = new String[31];
		i=0;
		for (int j = 1; j <= 31;j++){
			jour[i++] = String.valueOf(j);
		}
		cbJourDate = new JComboBox(jour);
		cbJourDate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chbDateDegat.setSelected(true);
			}
		});
		panelDate.add(cbJourDate, "2, 1, fill, default");

		String[] mois = new String[12];
		i=0;
		for (int j = 1; j <= 12;j++){
			mois[i++] = String.valueOf(j);
		}
		cbMoisDate = new JComboBox(mois);
		cbMoisDate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chbDateDegat.setSelected(true);
			}
		});
		panelDate.add(cbMoisDate, "3, 1, fill, default");

		String[] annee = new String[Calendar.getInstance().get(Calendar.YEAR) - 1900 +1];
		i=0;
		for (int j = Calendar.getInstance().get(Calendar.YEAR) ; j >= 1900;j--){
			annee[i++] = String.valueOf(j);
		}
		cbAnneeDate = new JComboBox(annee);
		cbAnneeDate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chbDateDegat.setSelected(true);
			}
		});
		panelDate.add(cbAnneeDate, "4, 1, fill, default");

		content.add(panelDate, "4, 2, fill, fill");
		
		JLabel lblNewLabel_1 = new JLabel("Zone du dégat :");
		content.add(lblNewLabel_1, "2, 4, right, default");
		
		String[]  zones = new String[controler.getAllSortieZones().length+1];
		for (int j = 1; j<controler.getAllSortieZones().length+1; j++){
			zones[j] = controler.getAllSortieZones()[j-1];
		}
		cbZone = new JComboBox(zones);
		content.add(cbZone, "4, 4, fill, default");
		
		JLabel lblImportanceDuDgat = new JLabel("Importance du dégat :");
		content.add(lblImportanceDuDgat, "2, 6, right, default");

		String[]  importances = new String[controler.getAllImportancesDegat().length+1];
		for (int j = 1; j<controler.getAllImportancesDegat().length+1; j++){
			importances[j] = controler.getAllImportancesDegat()[j-1];
		}
		cbImportance = new JComboBox(importances);
		content.add(cbImportance, "4, 6, fill, default");
		
		JLabel lblBateau = new JLabel("Bateau :");
		content.add(lblBateau, "2, 8, right, default");
		
		String[]  bateaux = new String[controler.getAllBateaux().length+1];
		for (int j = 1; j<controler.getAllBateaux().length+1; j++){
			bateaux[j] = controler.getAllBateaux()[j-1];
		}
		cbBateau = new JComboBox(bateaux);
		content.add(cbBateau, "4, 8, fill, default");
		
		JLabel lblRameur = new JLabel("Rameur :");
		content.add(lblRameur, "2, 10, right, default");
		
		String[]  rameurs = new String[controler.getAllRameurs().length+1];
		for (int j = 1; j<controler.getAllRameurs().length+1; j++){
			rameurs[j] = controler.getAllRameurs()[j-1];
		}
		cbRameur = new JComboBox(rameurs);
		content.add(cbRameur, "4, 10, fill, default");
		
		JLabel lblTypeDeRparation = new JLabel("Type de réparation :");
		content.add(lblTypeDeRparation, "2, 12, right, default");

		String[]  types = new String[controler.getAllTypesReparation().length+1];
		for (int j = 1; j<controler.getAllTypesReparation().length+1; j++){
			types[j] = controler.getAllTypesReparation()[j-1];
		}
		cbType = new JComboBox(types);
		content.add(cbType, "4, 12, fill, default");
		
		JLabel lblCo = new JLabel("Coût :");
		content.add(lblCo, "2, 14, right, default");
		
		txtCout = new JTextField();
		txtCout.setDocument(docCout);
		content.add(txtCout, "4, 14, fill, default");
		txtCout.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Reparé par :");
		content.add(lblNewLabel_2, "2, 16, right, top");
		
		txtReparePar = new JTextField();
		content.add(txtReparePar, "4, 16, fill, default");
		txtReparePar.setColumns(10);
		
		JLabel lblObservation = new JLabel("Observation :");
		content.add(lblObservation, "2, 18, right, default");
		
		txtObservation = new JTextPane();
		content.add(txtObservation, "4, 18, fill, fill");
		
		JPanel panelCommand = new JPanel();
		JButton btnSearch = new JButton("Rechercher");
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				search();
			}
		});
		JButton btnSearchAll = new JButton("Tout afficher");
		btnSearchAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				searchAll();
			}
		});
		panelCommand.add(btnSearch);
		panelCommand.add(btnSearchAll);
		content.add(panelCommand, "4, 20");
		
		add(content);
	}
	
	
	
	@Override
	public void search() {
		int refBateau = -1;
		if(cbBateau.getSelectedItem()!=null){
			refBateau = Integer.valueOf(((String) cbBateau.getSelectedItem()).split(":")[0].trim());
		}
		String refRameur = null;
		if (cbRameur.getSelectedItem()!=null){
			refRameur = ((String) cbRameur.getSelectedItem()).split(":")[1].trim();
		}
		Date date = null;
		if (chbDateDegat.isSelected()){
			date = controler.stringToDate((String)cbJourDate.getSelectedItem() +"/"+ (String)cbMoisDate.getSelectedItem() +"/"+ (String)cbAnneeDate.getSelectedItem());
		}
		double cout = -1;
		if(!txtCout.getText().isEmpty()){
			cout = Double.valueOf(txtCout.getText());
		}
		controler.searchDegatReps(parent, date,txtObservation.getText(), (String) cbZone.getSelectedItem(), refRameur, refBateau, 
				(String) cbImportance.getSelectedItem(), (String) cbType.getSelectedItem(), cout, txtReparePar.getText());
	}

	@Override
	public void searchAll() {
		controler.searchDegatReps(parent, null, null, null, null, 0, null, null, 0, null);
	}

}

