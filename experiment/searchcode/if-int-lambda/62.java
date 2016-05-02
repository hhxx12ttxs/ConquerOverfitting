package smartant.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import laboratory.plugin.task.ant.SimpleAnt;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.examples.AbstractExampleApplet;

import smartant.es.EvolutionStrategyMealyAutomatonEngine;

/**
 * Applet which does all the work for visualizing this damned
 * not-more-than-eighty-eating smart ant problem.
 * 
 * @author pasa
 * @see EvolutionStrategyMealyAutomatonEngine
 */
public class SmartAntApplet extends AbstractExampleApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4902492122389137683L;

	private Thread esRunsRunnerThread;

	public SmartAntApplet() {
		cases = new ArrayList<ESLaboratoryCase>();
		cases.add(new RunsLaboratoryCase());
		cases.add(new HoldsBeforeRestartCase());
		rng = new MersenneTwisterRNG();
		try {
			SimpleAnt.setField(SimpleAnt.readField(new BufferedReader(
					new FileReader("field.in"))));
		} catch (FileNotFoundException e) {
			System.err.println("Field file not found!");
		}
		startButton = new JButton("start");
	}

	private JLabel minMutationsLabel;
	private JLabel maxMutationsLabel;
	private JLabel muLabel;
	private JLabel lambdaLabel;
	private JLabel holdsBeforeRestartLabel;
	private JLabel numOfStatesLabel;
	private JLabel plusStrategyLabel;
	private JLabel changeEndProbabilityLabel;
	private JLabel switchConditionProbabilityLabel;
	private JSpinner minMutationsSpinner;
	private JSpinner maxMutationSpinner;
	private JSpinner muSpinner;
	private JSpinner lambdaSpinner;
	private JSpinner holdsBeforeRestartSpinner;
	private JSpinner numOfStatesSpinner;
	private JCheckBox plusStrategyCheckBox;
	private JSlider changeEndProbabilitySlider;
	private JSlider switchConditionProbabilittySlider;

	private JTabbedPane casesPane;
	private JPanel preferencesPanel;
	private final JButton startButton;

	private final Random rng;
	private final List<ESLaboratoryCase> cases;

	private void initPreferencesPanel() {
		preferencesPanel = new JPanel();
		minMutationsLabel = new JLabel("minMutatinos");
		maxMutationsLabel = new JLabel("maxMutations");
		muLabel = new JLabel("nu");
		lambdaLabel = new JLabel("lambda");
		holdsBeforeRestartLabel = new JLabel("beforeRestart");
		numOfStatesLabel = new JLabel("numberOfStates");
		plusStrategyLabel = new JLabel("isPlusStrategy");
		int minMutations = 0;
		int maxMutations = 30;
		int mu = 1;
		int lambda = 10;
		int holdsBeforeRestart = 10000;
		int numberOfStates = 9;
		boolean plusStrategy = true;
		double changeEndProbability = 0.25;
		double switchConditionProbability = 0.33;
		changeEndProbabilityLabel = new JLabel("changeEndProbability: "
				+ changeEndProbability);
		switchConditionProbabilityLabel = new JLabel(
				"switchConditionProbability: " + switchConditionProbability);
		minMutationsSpinner = new JSpinner(new SpinnerNumberModel(minMutations,
				0, 100, 1));
		maxMutationSpinner = new JSpinner(new SpinnerNumberModel(maxMutations,
				0, 100, 1));
		muSpinner = new JSpinner(new SpinnerNumberModel(mu, 1, 10000, 1));
		lambdaSpinner = new JSpinner(
				new SpinnerNumberModel(lambda, 1, 10000, 1));
		holdsBeforeRestartSpinner = new JSpinner(new SpinnerNumberModel(
				holdsBeforeRestart, 1, 100000, 1));
		numOfStatesSpinner = new JSpinner(new SpinnerNumberModel(
				numberOfStates, 1, 20, 1));
		plusStrategyCheckBox = new JCheckBox("isPlusStrategy", plusStrategy);
		changeEndProbabilitySlider = new JSlider(0, 100,
				(int) Math.round(changeEndProbability * 100));
		changeEndProbabilitySlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				double changeEndProbability = changeEndProbabilitySlider
						.getValue() / (double) 100;
				changeEndProbabilityLabel.setText("changeEndProbability: "
						+ changeEndProbability);
			}
		});
		switchConditionProbabilittySlider = new JSlider(0, 100,
				(int) Math.round(switchConditionProbability * 100));
		switchConditionProbabilittySlider
				.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						double switchConditionProbability = switchConditionProbabilittySlider
								.getValue() / (double) 100;
						switchConditionProbabilityLabel
								.setText("switchConditionProbability: "
										+ switchConditionProbability);
					}
				});
		preferencesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		preferencesPanel.setLayout(new GridLayout(0, 6, 5, 5));
		preferencesPanel.add(minMutationsLabel);
		preferencesPanel.add(minMutationsSpinner);
		preferencesPanel.add(maxMutationsLabel);
		preferencesPanel.add(maxMutationSpinner);
		preferencesPanel.add(numOfStatesLabel);
		preferencesPanel.add(numOfStatesSpinner);
		preferencesPanel.add(muLabel);
		preferencesPanel.add(muSpinner);
		preferencesPanel.add(lambdaLabel);
		preferencesPanel.add(lambdaSpinner);
		preferencesPanel.add(holdsBeforeRestartLabel);
		preferencesPanel.add(holdsBeforeRestartSpinner);
		preferencesPanel.add(plusStrategyLabel);
		preferencesPanel.add(plusStrategyCheckBox);
		preferencesPanel.add(changeEndProbabilityLabel);
		preferencesPanel.add(changeEndProbabilitySlider);
		preferencesPanel.add(switchConditionProbabilityLabel);
		preferencesPanel.add(switchConditionProbabilittySlider);
	}

	private ESEngineProperties getProperties() {
		return new ESEngineProperties(rng,
				(Integer) minMutationsSpinner.getValue(),
				(Integer) maxMutationSpinner.getValue(),
				(Integer) muSpinner.getValue(),
				(Integer) lambdaSpinner.getValue(),
				(Integer) holdsBeforeRestartSpinner.getValue(),
				(Integer) numOfStatesSpinner.getValue(),
				switchConditionProbabilittySlider.getValue()
						/ (double) switchConditionProbabilittySlider
								.getMaximum(),
				plusStrategyCheckBox.isSelected(),
				changeEndProbabilitySlider.getValue()
						/ (double) changeEndProbabilitySlider.getMaximum());
	}

	private JTabbedPane createStatistics() {
		JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);
		for (ESLaboratoryCase eslc : cases) {
			pane.addTab(eslc.getCaseName(), eslc.getPanel());
		}
		return pane;
	}

	@Override
	protected void prepareGUI(final Container container) {
		initPreferencesPanel();
		startButton.setActionCommand("start");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("start")) {
					final ESLaboratoryCase currentCase = cases.get(casesPane
							.getSelectedIndex());
					currentCase.setProperties(getProperties());
					new Thread() {
						public void run() {
							try {
								esRunsRunnerThread = new Thread(currentCase);
								esRunsRunnerThread.start();
								esRunsRunnerThread.join();
							} catch (InterruptedException e) {
								return;
							} finally {
								startButton.setActionCommand("start");
								startButton.setText("start");
							}
						}
					}.start();
					startButton.setActionCommand("stop");
					startButton.setText("stop");
				} else if (e.getActionCommand().equals("stop")) {
					esRunsRunnerThread.interrupt();
					startButton.setActionCommand("start");
					startButton.setText("start");
				}
			}
		});
		casesPane = createStatistics();
		container.setLayout(new BorderLayout());
		container.add(casesPane, BorderLayout.CENTER);
		container.add(preferencesPanel, BorderLayout.NORTH);
		container.add(startButton, BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		new SmartAntApplet().displayInFrame("Smart Ant");
	}
}

