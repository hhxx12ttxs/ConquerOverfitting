package dulw;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MainWindow extends JFrame {

	private static final String VERSION = " v0.1";
	private static final long serialVersionUID = 1535507887459497461L;
	private ChartComponent chartComponent;
	private int menuItemIndex;
	private XYSeries actualXYSeries;
	private GroupLayout layout;
	// private String menuSelectedItemName;
	// private JFrame thisFrame;
	private UnivariateRealFunction func;
	private SignalType signalType;
	private SignalWrapper actualWrapper;
	boolean isRecovered = false;
	boolean isFromFile;

	private void createMenu() {

		// Menu creation in following order:
		// 1. make menu/menuItem
		// 2. add menu/menuItem to the container of higher level

		// Create menu bar
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		// File menu
		JMenu fileMenu = new JMenu("Plik");
		menuBar.add(fileMenu);

		JMenuItem saveItem = new JMenuItem("Zapisz do pliku");
		fileMenu.add(saveItem);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int frequency = 0;
					if (!isFromFile) {
						String s = JOptionPane
								.showInputDialog("Częstotliwoć próbkowania w Hz");
						if (s == null || s.isEmpty())
							s = "1.0";
						frequency = Integer.parseInt(s);
					} else {
						frequency = actualWrapper.getSimplingFrequency();
					}
					JFileChooser fc = new JFileChooser();
					fc.showSaveDialog(MainWindow.this);
					File file = fc.getSelectedFile();
					double t1 = Double.parseDouble(jTextField1.getText());
					double d = Double.parseDouble(jTextField3.getText());
					double A = Double.parseDouble(jTextField2.getText());
					double T = Double.parseDouble(jTextField4.getText());
					double kw = Double.parseDouble(jTextField5.getText());
					int ns = (int) Double.parseDouble(jumpSampleNumberTextField
							.getText());
					double ts = Double.parseDouble(jumpTimeTextField.getText());
					double probability = Double
							.parseDouble(probabilityTextField.getText());
					double f = Double.parseDouble(frequencySamplingTextField
							.getText());
					SignalWrapper wrapper = new SignalWrapper();
					wrapper.setA(A);
					wrapper.setD(d);
					wrapper.setF(f);
					wrapper.setKw(kw);
					wrapper.setNs(ns);
					wrapper.setProbability(probability);
					wrapper.setSimplingFrequency(frequency);
					wrapper.setT(T);
					wrapper.setT1(t1);
					wrapper.setTs(ts);
					wrapper.setValuesType(ValuesType.REAL);
					wrapper.setIsRecovered(isRecovered);
					wrapper.setSignalType(signalType);
					if (!isFromFile) {
						wrapper.setData(SignalsUtils.simplingSignal(func, t1,
								d, frequency));
					} else {
						wrapper.setData(SignalsUtils
								.fromXYSeriesToList(actualXYSeries));
					}

					FilesUtils.writeSignal(file, wrapper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		JMenuItem openItem = new JMenuItem("Odczytaj z pliku");
		fileMenu.add(openItem);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();

					SignalWrapper wrapper = FilesUtils.readSignal(file);
					// wazne !!!!!!!!!!!!!!!!!!
					actualWrapper = wrapper;
					actualXYSeries = SignalsUtils.fromListToXYSeries(wrapper
							.getData());
					XYSeriesCollection dataset = new XYSeriesCollection(
							actualXYSeries);

					jTextField1.setText(String.format(Locale.ENGLISH, "%.3f%n",
							wrapper.getT1()));
					jTextField2.setText(String.format(Locale.ENGLISH, "%.3f%n",
							wrapper.getA()));
					jTextField3.setText(String.format(Locale.ENGLISH, "%.3f%n",
							wrapper.getD()));
					jTextField4.setText(String.format(Locale.ENGLISH, "%.3f%n",
							wrapper.getT()));
					jTextField5.setText(String.format(Locale.ENGLISH, "%.3f%n",
							wrapper.getKw()));
					jumpSampleNumberTextField.setText(String.format(
							Locale.ENGLISH, "%d", wrapper.getNs()));
					jumpTimeTextField.setText(String.format(Locale.ENGLISH,
							"%.3f%n", wrapper.getTs()));
					probabilityTextField.setText(String.format(Locale.ENGLISH,
							"%.3f%n", wrapper.getProbability()));
					frequencySamplingTextField.setText(String.format(
							Locale.ENGLISH, "%.3f%n", wrapper.getF()));


					if (wrapper.getIsRecovered() == true) {
						isRecovered = true;
						chartComponent.changeChartForLinear(
								new GridLayout(1, 0), dataset,
								SignalType.LINEAR);
					} else {
						isRecovered = false;
						chartComponent.changeChartForLinear(
								new GridLayout(1, 0), dataset,
								SignalType.NONLINEAR);
					}

					signalType = wrapper.getSignalType();
					isFromFile = true;

				} catch (Exception e) {
				}
			}
		});
		JMenuItem actualSignalValuesItem = new JMenuItem(
				"Pokaż zawartoć aktualnego pliku");
		fileMenu.add(actualSignalValuesItem);
		actualSignalValuesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					JDialog dialog = new DialogWindow(null, actualWrapper);
					dialog.setVisible(true);

				} catch (Exception e) {
				}
			}
		});

		JMenuItem otherSignalValuesItem = new JMenuItem(
				"Pokaż zawartoć wskazanego pliku");
		fileMenu.add(otherSignalValuesItem);
		otherSignalValuesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();

					SignalWrapper wrapper = FilesUtils.readSignal(file);
					JDialog dialog = new DialogWindow(null, wrapper);
					dialog.setVisible(true);

				} catch (Exception e) {
				}
			}
		});

		// Signals menu
		JMenu signalsMenu = new JMenu("Sygnały");
		menuBar.add(signalsMenu);

		JMenuItem constNoise = new JMenuItem("Szum jednostajny");
		signalsMenu.add(constNoise);
		constNoise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				menuItemIndex = 0;
				// menuSelectedItemName = new String("constNoise");
				paintButton.doClick();
			}
		});

		JMenuItem gaussNoise = new JMenuItem("Szum gaussowski");
		signalsMenu.add(gaussNoise);
		gaussNoise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				menuItemIndex = 1;
				// menuSelectedItemName = new String("gaussNoise");
				paintButton.doClick();
			}
		});

		JMenuItem sinusItem = new JMenuItem("Sinusoidalny");
		signalsMenu.add(sinusItem);
		sinusItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				menuItemIndex = 2;
				// menuSelectedItemName = new String("sinusItem");
				paintButton.doClick();
			}
		});

		JMenuItem sinusOneHalfItem = new JMenuItem(
				"Sinusoidalny jednopołówkowo");
		signalsMenu.add(sinusOneHalfItem);
		sinusOneHalfItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				menuItemIndex = 3;
				// menuSelectedItemName = new String("sinusOneHalfItem");
				paintButton.doClick();
			}
		});

		JMenuItem sinusTwoHalfItem = new JMenuItem("Sinusoidalny dwupołówkowo");
		signalsMenu.add(sinusTwoHalfItem);
		sinusTwoHalfItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 4;
				// //menuSelectedItemName = new String("sinusTwoHalfItem");
				paintButton.doClick();
			}
		});

		JMenuItem rectItem = new JMenuItem("Prostokštny");
		signalsMenu.add(rectItem);
		rectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 5;
				// menuSelectedItemName = new String("rectItem");
				paintButton.doClick();
			}
		});

		JMenuItem rectSimmItem = new JMenuItem("Prostokštny symetryczny");
		signalsMenu.add(rectSimmItem);
		rectSimmItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 6;
				// menuSelectedItemName = new String("rectSimmItem");
				paintButton.doClick();
			}
		});

		JMenuItem triItem = new JMenuItem("Trójkštny");
		signalsMenu.add(triItem);
		triItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 7;
				// menuSelectedItemName = new String("triItem");
				paintButton.doClick();
			}
		});

		JMenuItem jumpItem = new JMenuItem("Skok jednostkowy");
		signalsMenu.add(jumpItem);
		jumpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 8;
				// menuSelectedItemName = new String("jumpItem");
				paintButton.doClick();
			}
		});

		JMenuItem impulseItem = new JMenuItem("Impuls jednostkowy");
		signalsMenu.add(impulseItem);
		impulseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 9;
				// menuSelectedItemName = new String("impulseItem");
				paintButton.doClick();
			}
		});

		JMenuItem impulseNoiseItem = new JMenuItem("Szum impulsowy");
		signalsMenu.add(impulseNoiseItem);
		impulseNoiseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				menuItemIndex = 10;
				// menuSelectedItemName = new String("impulseNoiseItem");
				paintButton.doClick();
			}
		});

		// Operations
		JMenu operationsMenu = new JMenu("Operacje");
		menuBar.add(operationsMenu);

		JMenuItem addItem = new JMenuItem("Dodawanie");
		operationsMenu.add(addItem);
		addItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!isFromFile) {
						throw new IllegalArgumentException(
								"Sygnał do operacji musi pochodzić z pliku");
					}

					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();
					SignalWrapper wrapper = FilesUtils.readSignal(file);
					List<double[]> result = ParameterCalc.addSignals(
							SignalsUtils.fromXYSeriesToList(actualXYSeries),
							wrapper.getData());
					actualXYSeries = SignalsUtils.fromListToXYSeries(result);
					XYSeriesCollection dataset = new XYSeriesCollection(
							actualXYSeries);
					chartComponent.changeChartForLinear(new GridLayout(1, 0),
							dataset, wrapper.getSignalType());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		JMenuItem subItem = new JMenuItem("Odejmowanie");
		operationsMenu.add(subItem);
		subItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!isFromFile) {
						throw new IllegalArgumentException(
								"Sygnał do operacji musi pochodzić z pliku");
					}

					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();
					SignalWrapper wrapper = FilesUtils.readSignal(file);
					List<double[]> result = ParameterCalc.substractSignals(
							SignalsUtils.fromXYSeriesToList(actualXYSeries),
							wrapper.getData());
					actualXYSeries = SignalsUtils.fromListToXYSeries(result);
					XYSeriesCollection dataset = new XYSeriesCollection(
							actualXYSeries);
					chartComponent.changeChartForLinear(new GridLayout(1, 0),
							dataset, wrapper.getSignalType());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		JMenuItem mulItem = new JMenuItem("Mnożenie");
		operationsMenu.add(mulItem);
		mulItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!isFromFile) {
						throw new IllegalArgumentException(
								"Sygnał do operacji musi pochodzić z pliku");
					}

					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();
					SignalWrapper wrapper = FilesUtils.readSignal(file);
					List<double[]> result = ParameterCalc.multiplySignals(
							SignalsUtils.fromXYSeriesToList(actualXYSeries),
							wrapper.getData());
					actualXYSeries = SignalsUtils.fromListToXYSeries(result);
					XYSeriesCollection dataset = new XYSeriesCollection(
							actualXYSeries);
					chartComponent.changeChartForLinear(new GridLayout(1, 0),
							dataset, wrapper.getSignalType());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		JMenuItem divItem = new JMenuItem("Dzielenie");
		operationsMenu.add(divItem);
		divItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!isFromFile) {
						throw new IllegalArgumentException(
								"Sygnał do operacji musi pochodzić z pliku");
					}

					JFileChooser fc = new JFileChooser();
					fc.showOpenDialog(MainWindow.this);
					File file = fc.getSelectedFile();
					SignalWrapper wrapper = FilesUtils.readSignal(file);
					List<double[]> result = ParameterCalc.divideSignals(
							SignalsUtils.fromXYSeriesToList(actualXYSeries),
							wrapper.getData());
					actualXYSeries = SignalsUtils.fromListToXYSeries(result);
					XYSeriesCollection dataset = new XYSeriesCollection(
							actualXYSeries);
					chartComponent.changeChartForLinear(new GridLayout(1, 0),
							dataset, wrapper.getSignalType());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		JMenuItem histItem = new JMenuItem("Histogram");
		operationsMenu.add(histItem);
		signalsMenu.add(histItem);
		histItem.addActionListener(new HistogramLisener());

		JMenu convertMenu = new JMenu("Konwersje");
		menuBar.add(convertMenu);

		JMenuItem zohItem = new JMenuItem("ZOH");
		convertMenu.add(zohItem);
		zohItem.addActionListener(new ZohListener());

		JMenuItem fohItem = new JMenuItem("FOH");
		convertMenu.add(fohItem);
		fohItem.addActionListener(new FohListener());

		JMenuItem sincItem = new JMenuItem("SINC");
		convertMenu.add(sincItem);
		sincItem.addActionListener(new SincListener());

		JMenuItem quantCutItem = new JMenuItem("Kwantyzacja z obcięciem");
		convertMenu.add(quantCutItem);
		quantCutItem.addActionListener(new QuantCutListener());

		JMenuItem quantRoundItem = new JMenuItem("Kwantyzacja z zaokršgleniem");
		convertMenu.add(quantRoundItem);
		quantRoundItem.addActionListener(new QuantRoundListener());

		JMenuItem mseItem = new JMenuItem("Błędy rekonstrukcji");
		convertMenu.add(mseItem);
		mseItem.addActionListener(new MSEListener());

		JMenu filterMenu = new JMenu("Filtry");
		menuBar.add(filterMenu);

		JMenuItem basicLowerFilterItem = new JMenuItem(
				"Filtr dolnoprzepustowy - okno prostokštne");
		filterMenu.add(basicLowerFilterItem);
		basicLowerFilterItem
				.addActionListener(new FilterListener(false, false));

		JMenuItem hammingLowerFilterItem = new JMenuItem(
				"Filtr dolnoprzepustowy - okno Hamminga");
		filterMenu.add(hammingLowerFilterItem);
		hammingLowerFilterItem
				.addActionListener(new FilterListener(false, true));

		JMenuItem basicUpperFilterItem = new JMenuItem(
				"Filtr górnoprzepustowy - okno prostokštne");
		filterMenu.add(basicUpperFilterItem);
		basicUpperFilterItem.addActionListener(new FilterListener(true, false));

		JMenuItem hammingUpperFilterItem = new JMenuItem(
				"Filtr górnoprzepustowy - okno Hamminga");
		filterMenu.add(hammingUpperFilterItem);
		hammingUpperFilterItem
				.addActionListener(new FilterListener(true, true));

		JMenuItem convItem = new JMenuItem("Splot dwóch sygnałów");
		filterMenu.add(convItem);
		convItem.addActionListener(new ConvListener());
		
		JMenuItem corItem = new JMenuItem("Korelacja dwóch sygnałów");
		filterMenu.add(corItem);
		corItem.addActionListener(new CorListener(false));

		JMenuItem corConvItem = new JMenuItem("Korelacja dwóch sygnałów z użyciem splotu");
		filterMenu.add(corConvItem);
		corConvItem.addActionListener(new CorListener(true));
		
		JMenuItem simulatorItem = new JMenuItem("Symulator");
		filterMenu.add(simulatorItem);
		simulatorItem.addActionListener(new SimulatorListener());

	}

	private void createLayout() {

		layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														this.chartComponent,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		averageValueLabel)
																.addGap(18, 18,
																		18)
																.addComponent(
																		averageValueTextField,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		44,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		absoluteAverageValueLabel)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		absoluteAverageValueTextField,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		44,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		effectiveValueLabel)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		effectiveValueTextField,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		44,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		varianceLabel)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		varianceTextField,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		44,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		averagePowerLabel)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		averagePowerTextField,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		44,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addComponent(
																										jLabel1)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jTextField2,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										jLabel2)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jTextField1,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										jLabel3)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jTextField3,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE))
																				.addGroup(
																						layout.createSequentialGroup()
																								.addComponent(
																										jumpSampleNumberLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jumpSampleNumberTextField,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(12,
																										12,
																										12)
																								.addComponent(
																										jumpTimeLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(
																										jumpTimeTextField,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										frequencySamplingLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										frequencySamplingTextField,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)))
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										jLabel4)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jTextField4,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										jLabel5)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										jTextField5,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE))
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										probabilityLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										probabilityTextField,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(
																										histogramBinsLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addComponent(
																										histogramBinsTextField,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										44,
																										javax.swing.GroupLayout.PREFERRED_SIZE))))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		paintButton)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(
																		warningLabel)))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(11, 11, 11)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel1)
												.addComponent(
														jTextField2,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel2)
												.addComponent(
														jTextField1,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel3)
												.addComponent(
														jTextField3,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel4)
												.addComponent(
														jTextField4,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel5)
												.addComponent(
														jTextField5,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														jumpSampleNumberLabel)
												.addComponent(jumpTimeLabel)
												.addComponent(
														jumpTimeTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														frequencySamplingLabel)
												.addComponent(
														frequencySamplingTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jumpSampleNumberTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(probabilityLabel)
												.addComponent(
														probabilityTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														histogramBinsLabel)
												.addComponent(
														histogramBinsTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(averageValueLabel)
												.addComponent(
														averageValueTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														absoluteAverageValueLabel)
												.addComponent(
														effectiveValueLabel)
												.addComponent(
														effectiveValueTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(varianceLabel)
												.addComponent(
														varianceTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(averagePowerLabel)
												.addComponent(
														averagePowerTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														absoluteAverageValueTextField,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(paintButton)
												.addComponent(warningLabel))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(this.chartComponent,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addContainerGap()));
	}

	private void createControls() {
		warningLabel = new javax.swing.JLabel("");
		jLabel1 = new javax.swing.JLabel("Amplituda (A) :");
		jLabel2 = new javax.swing.JLabel("Czas poczštkowy (t1) :");
		jLabel3 = new javax.swing.JLabel("Czas trwania sygnału (d) :");
		jLabel4 = new javax.swing.JLabel("Okres (T) :");
		jLabel5 = new javax.swing.JLabel("Współczynnik wypełnienia (kw) :");
		jumpSampleNumberLabel = new javax.swing.JLabel(
				"Numer próbki skoku (ns) :");
		jumpTimeLabel = new javax.swing.JLabel("Czas skoku (ts) :");
		frequencySamplingLabel = new javax.swing.JLabel(
				"Częstotliwoć próbkowania (f) :");
		probabilityLabel = new javax.swing.JLabel("Prawdopodobieństwo (p) :");
		histogramBinsLabel = new javax.swing.JLabel("Przedziały histogramu :");
		averageValueLabel = new javax.swing.JLabel("Wartoć rednia :");
		absoluteAverageValueLabel = new javax.swing.JLabel(
				"Wartoć rednia bezwzględna :");
		effectiveValueLabel = new javax.swing.JLabel("Wartoć skuteczna :");
		varianceLabel = new javax.swing.JLabel("Wariancja :");
		averagePowerLabel = new javax.swing.JLabel("Moc rednia:");
		jTextField1 = new javax.swing.JTextField("1.0");
		jTextField2 = new javax.swing.JTextField("1.0");
		jTextField3 = new javax.swing.JTextField("2.0");
		jTextField4 = new javax.swing.JTextField("2.0");
		jTextField5 = new javax.swing.JTextField("0.5");
		averagePowerTextField = new javax.swing.JTextField("0.0");
		varianceTextField = new javax.swing.JTextField("0.0");
		effectiveValueTextField = new javax.swing.JTextField("0.0");
		absoluteAverageValueTextField = new javax.swing.JTextField("0.0");
		averageValueTextField = new javax.swing.JTextField("0.0");
		frequencySamplingTextField = new javax.swing.JTextField("0.0");
		jumpTimeTextField = new javax.swing.JTextField("0.0");
		jumpSampleNumberTextField = new javax.swing.JTextField("0");
		probabilityTextField = new javax.swing.JTextField("0.0");
		histogramBinsTextField = new javax.swing.JTextField("5");

		paintButton = new javax.swing.JButton("Rysuj");

		jLabel1.setName("jLabel1"); // NOI18N
		jLabel2.setName("jLabel2"); // NOI18N
		jLabel3.setName("jLabel3"); // NOI18N
		jLabel4.setName("jLabel4"); // NOI18N
		jLabel5.setName("jLabel5"); // NOI18N
		jTextField1.setName("jTextField1"); // NOI18N
		jTextField2.setName("jTextField2"); // NOI18N
		jTextField3.setName("jTextField3"); // NOI18N
		jTextField4.setName("jTextField4"); // NOI18N
		jTextField5.setName("jTextField5"); // NOI18N
		averagePowerTextField.setEditable(false);
		averagePowerTextField.setName("jTextField6"); // NOI18N
		averageValueLabel.setName("jLabel6"); // NOI18N
		varianceTextField.setEditable(false);
		varianceTextField.setName("jTextField7"); // NOI18N
		varianceLabel.setName("jLabel7"); // NOI18N
		averagePowerLabel.setName("jLabel8"); // NOI18N
		absoluteAverageValueLabel.setName("jLabel9"); // NOI18N
		effectiveValueLabel.setName("jLabel10"); // NOI18N
		effectiveValueTextField.setEditable(false);
		effectiveValueTextField.setName("jTextField8"); // NOI18N
		absoluteAverageValueTextField.setEditable(false);
		absoluteAverageValueTextField.setName("jTextField9"); // NOI18N
		averageValueTextField.setEditable(false);
		averageValueTextField.setName("jTextField10"); // NOI18N
		paintButton.setName("jButton1"); // NOI18N

	}

	public MainWindow() {
		super("Cyfrowe Przetwarzanie Sygnałów" + VERSION);
		// thisFrame = this;
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setResizable(false);

		menuItemIndex = -1;

		this.createMenu();
		this.createControls();
		this.chartComponent = new ChartComponent();
		this.createLayout();

		paintButton.addActionListener(this.new PaintLisener(this));
	}

	private javax.swing.JButton paintButton;
	private javax.swing.JLabel warningLabel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel effectiveValueLabel;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel averageValueLabel;
	private javax.swing.JLabel varianceLabel;
	private javax.swing.JLabel averagePowerLabel;
	private javax.swing.JLabel absoluteAverageValueLabel;
	private javax.swing.JLabel probabilityLabel;
	private javax.swing.JLabel frequencySamplingLabel;
	private javax.swing.JLabel jumpTimeLabel;
	private javax.swing.JLabel jumpSampleNumberLabel;
	private javax.swing.JLabel histogramBinsLabel;
	private javax.swing.JTextField jTextField1;
	private javax.swing.JTextField averageValueTextField;
	private javax.swing.JTextField jTextField2;
	private javax.swing.JTextField jTextField3;
	private javax.swing.JTextField jTextField4;
	private javax.swing.JTextField jTextField5;
	private javax.swing.JTextField averagePowerTextField;
	private javax.swing.JTextField varianceTextField;
	private javax.swing.JTextField effectiveValueTextField;
	private javax.swing.JTextField absoluteAverageValueTextField;
	private javax.swing.JTextField frequencySamplingTextField;
	private javax.swing.JTextField jumpTimeTextField;
	private javax.swing.JTextField jumpSampleNumberTextField;
	private javax.swing.JTextField probabilityTextField;
	private javax.swing.JTextField histogramBinsTextField;

	class PaintLisener implements ActionListener {
		private MainWindow mainWindow;

		public PaintLisener() {
		}

		public PaintLisener(MainWindow window) {
			this.mainWindow = window;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			isRecovered = false;
			func = null;
			double A = 0;
			double t1 = 0;
			double d = 0;
			double T = 0;
			double kw = 0;
			int ns = 0;
			double ts = 0;
			double probability = 0;
			double f = 0;
			try {
				A = Double.parseDouble(jTextField2.getText());
				t1 = Double.parseDouble(jTextField1.getText());
				d = Double.parseDouble(jTextField3.getText());
				T = Double.parseDouble(jTextField4.getText());
				kw = Double.parseDouble(jTextField5.getText());
				ns = (int) Double.parseDouble(jumpSampleNumberTextField
						.getText());
				ts = Double.parseDouble(jumpTimeTextField.getText());
				probability = Double
						.parseDouble(probabilityTextField.getText());
				f = Double.parseDouble(frequencySamplingTextField.getText());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			switch (menuItemIndex) {
			case 0:
				//func = SignalsFactory.createConstNoiseSignal(A);
				func=SignalsFactory.createSensorSignal(0, 5);
				signalType = SignalType.LINEAR;
				break;
			case 1:
				func = SignalsFactory.createGausseNoiseSignal(A);
				signalType = SignalType.NOISE;
				break;
			case 2:
				func = SignalsFactory.createSinusSignal(A, 0, T);
				//func = SignalsFactory.createSensorSignal(0, 2);
				signalType = SignalType.LINEAR;
				break;
			case 3:
				func = SignalsFactory.createSinusHalfSignal(A, 0, T);
				signalType = SignalType.LINEAR;
				break;
			case 4:
				func = SignalsFactory.createSinusBothSignal(A, 0, T);
				signalType = SignalType.LINEAR;
				break;
			case 5:
				func = SignalsFactory.createRectangleSignal(A, T, kw, t1);
				signalType = SignalType.LINEAR;
				break;
			case 6:
				func = SignalsFactory.createRectangleSimmetricSignal(A, T, kw,
						t1);
				signalType = SignalType.LINEAR;
				break;
			case 7:
				func = SignalsFactory.createTriangleSignal(A, T, kw, t1);
				signalType = SignalType.LINEAR;
				break;
			case 8:
				func = SignalsFactory.createJumpSignal(A, ts);
				signalType = SignalType.LINEAR;
				break;
			case 9:
				func = SignalsFactory.createImpulseSignal(A, ns);
				signalType = SignalType.NONLINEAR;
				break;
			case 10:
				func = SignalsFactory.createImpulseNoise(A, probability);
				signalType = SignalType.NONLINEAR;
				break;
			default:
				System.out.println("Nie ma takiego elementu menu!!!! :/");
			}
			if (func != null) {
				try {

					XYSeriesCollection dataset = null;
					XYSeries series = null;
					if (signalType == SignalType.LINEAR
							|| signalType == SignalType.NOISE) {
						series = SignalsUtils.packLinearFunctionToXYSeries(
								func, t1, d);
						dataset = new XYSeriesCollection(series);
					} else if (signalType == SignalType.NONLINEAR) {
						series = SignalsUtils.packDiscretFunctionToXYSeries(
								func, t1, d, f);
						dataset = new XYSeriesCollection(series);

					}
					actualXYSeries = series;
					chartComponent.changeChartForLinear(new GridLayout(1, 0),
							dataset, signalType);
					this.mainWindow.validate();

//					if (signalType == SignalType.LINEAR) {
//						averageValueTextField.setText(String.format("%.3f%n",
//								ParameterCalc.relativeAverageForLinearFunction(
//										func, t1, t1 + d)));
//						absoluteAverageValueTextField.setText(String.format(
//								"%.3f%n", ParameterCalc
//										.absoluteAverageForLinearFunction(func,
//												t1, t1 + d)));
//						effectiveValueTextField.setText(String.format(
//								"%.3f%n",
//								ParameterCalc.rmsForLinearFunction(func, t1, t1
//										+ d)));
//						varianceTextField.setText(String.format("%.3f%n",
//								ParameterCalc.variationForLinearFunction(func,
//										t1, t1 + d)));
//						averagePowerTextField.setText(String.format("%.3f%n",
//								ParameterCalc.averagePowerForLinearFunction(
//										func, t1, t1 + d)));
//					} else {
//						averageValueTextField.setText(String.format("%.3f%n",
//								ParameterCalc
//										.relativeAverageForDiscretFunction(
//												func, t1, t1 + d, f)));
//						absoluteAverageValueTextField.setText(String.format(
//								"%.3f%n", ParameterCalc
//										.absoluteAverageForDiscretFunction(
//												func, t1, t1 + d, f)));
//						effectiveValueTextField.setText(String.format("%.3f%n",
//								ParameterCalc.rmsForDiscretFunction(func, t1,
//										t1 + d, f)));
//						varianceTextField.setText(String.format("%.3f%n",
//								ParameterCalc.variationForDiscretFunction(func,
//										t1, t1 + d, f)));
//						averagePowerTextField.setText(String.format("%.3f%n",
//								ParameterCalc.averagePowerForDiscretFunction(
//										func, t1, t1 + d, f)));
//					}
					isFromFile = false;
				} catch (FunctionEvaluationException e) {
					e.printStackTrace();
//				} catch (MaxIterationsExceededException e) {
//					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}

	}

	class HistogramLisener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int bins = Integer.parseInt(histogramBinsTextField.getText());
			HistogramDataset datasetHistogram = SignalsUtils.toHistogram(
					actualXYSeries, "", bins);
			XYSeriesCollection datasetNormal = new XYSeriesCollection(
					actualXYSeries);
			try {
				chartComponent.changeChartForLinear(new GridLayout(2, 0),
						datasetNormal, signalType);
			} catch (FunctionEvaluationException e1) {
				e1.printStackTrace();
			}
			chartComponent.addHistogram(datasetHistogram);

		}

	}

	class ZohListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				XYSeries xs = SignalsUtils.ZohInterpolation(actualWrapper);// new
																			// XYSeries("");
				actualXYSeries = xs;
				XYSeriesCollection dataset = new XYSeriesCollection(xs);
				chartComponent.changeChartForLinear(null, dataset, signalType);
				isRecovered = true;
			} catch (Exception ex) {
				// blabla
			}
		}
	}

	class FohListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				XYSeries xs = SignalsUtils.FohInterpolation(actualWrapper);// new
																			// XYSeries("");
				actualXYSeries = xs;
				XYSeriesCollection dataset = new XYSeriesCollection(xs);
				chartComponent.changeChartForLinear(null, dataset, signalType);
				isRecovered = true;
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}

	class SincListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// TODO pokazac okienko kiedy noSamples za duże
				String s = JOptionPane
						.showInputDialog("Podaj liczbę próbek dla przybliżenia");
				int precision = Integer.parseInt(s);
				XYSeries xs = SignalsUtils.sincInterpolation(actualWrapper,
						precision);
				actualXYSeries = xs;
				XYSeriesCollection dataset = new XYSeriesCollection(xs);
				chartComponent.changeChartForLinear(null, dataset, signalType);
				isRecovered = true;
			} catch (Exception ex) {
				// blabla
			}
		}

	}

	class QuantCutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String s = JOptionPane
						.showInputDialog("Podaj liczbę poziomów kwantyzacji");
				int noQuants = Integer.parseInt(s);
				XYSeries xs = SignalsUtils.fromListToXYSeries(SignalsUtils
						.quantCutTreeSet(actualWrapper.getData(),
								actualWrapper.getA(), noQuants));
				actualXYSeries = xs;
				chartComponent.changeChartForLinear(null,
						new XYSeriesCollection(xs), SignalType.NONLINEAR);
			} catch (Exception ex) {
				// blabla
			}
		}

	}

	class QuantRoundListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String s = JOptionPane
						.showInputDialog("Podaj liczbę poziomów kwantyzacji");
				int noQuants = Integer.parseInt(s);
				XYSeries xs = SignalsUtils.fromListToXYSeries(SignalsUtils
						.quantRoundTreeSet(actualWrapper.getData(),
								actualWrapper.getA(), noQuants));
				actualXYSeries = xs;
				chartComponent.changeChartForLinear(null,
						new XYSeriesCollection(xs), SignalType.NONLINEAR);
			} catch (Exception ex) {
				// blabla
			}
		}

	}

	class MSEListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(MainWindow.this);
				File file = fc.getSelectedFile();
				SignalWrapper wrapper1 = FilesUtils.readSignal(file);
				fc.showOpenDialog(MainWindow.this);
				file = fc.getSelectedFile();
				SignalWrapper wrapper2 = FilesUtils.readSignal(file);
				double mse = SignalsUtils.MSE(wrapper1.getData(),
						wrapper2.getData());
				double snr = SignalsUtils.SNR(wrapper1.getData(),
						wrapper2.getData(), mse);
				double psnr = SignalsUtils.PSNR(wrapper1.getData(),
						wrapper2.getData(), wrapper1.getA());
				double md = SignalsUtils.MD(wrapper1.getData(),
						wrapper2.getData());
				JOptionPane.showConfirmDialog(null, "MSE: " + mse + "\nSNR: "
						+ snr + "\nPSNR: " + psnr + "\nMD: " + md,
						"Błędy rekonstrukcji", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null);
				System.out.println("mse: " + mse);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}

	class FilterListener implements ActionListener {
		private boolean isUpper;
		private boolean isHamming;

		public FilterListener(boolean upper, boolean hamming) {
			this.isUpper = upper;
			this.isHamming = hamming;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (actualWrapper == null) {
					throw new IllegalStateException("Nie ma wrappera");
				}
				String s = JOptionPane.showInputDialog("Podaj rzšd filtru");
				int m = Integer.parseInt(s);
				s = JOptionPane
						.showInputDialog("Podaj częstotliwoć obczięcia");
				int f0 = Integer.parseInt(s);

				double[] h = SignalsUtils.filter(m, f0,
						actualWrapper.getSimplingFrequency(), isUpper,
						isHamming);
				double[] y = SignalsUtils.convolution(h,
						SignalsUtils.fromXYSeriesToList(actualXYSeries));
				actualXYSeries = SignalsUtils.fromTableToXYSeries(y,
						actualWrapper.getT1(),
						actualWrapper.getSimplingFrequency(), false);
				chartComponent.changeChartForLinear(null,
						new XYSeriesCollection(actualXYSeries),
						SignalType.NONLINEAR);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}

	class ConvListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(MainWindow.this);
				File file = fc.getSelectedFile();
				SignalWrapper wrapper1 = FilesUtils.readSignal(file);
				fc.showOpenDialog(MainWindow.this);
				file = fc.getSelectedFile();
				SignalWrapper wrapper2 = FilesUtils.readSignal(file);
				double[] y = SignalsUtils.convolution(wrapper1.getData(),
						wrapper2.getData());
				XYSeries xs = SignalsUtils.fromTableToXYSeries(y,
						wrapper1.getT1(), wrapper1.getSimplingFrequency(), false);
				actualXYSeries = xs;
				chartComponent.changeChartForLinear(new GridLayout(0, 1),
						new XYSeriesCollection(xs), SignalType.NONLINEAR);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}
	
	class CorListener implements ActionListener {
		private boolean useConv;
		public CorListener(boolean useConv){
			this.useConv = useConv;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(MainWindow.this);
				File file = fc.getSelectedFile();
				SignalWrapper wrapper1 = FilesUtils.readSignal(file);
				fc.showOpenDialog(MainWindow.this);
				file = fc.getSelectedFile();
				SignalWrapper wrapper2 = FilesUtils.readSignal(file);
				
				double[] y;
				if(useConv == false){//normalnie
					y = SignalsUtils.corellation(wrapper1.getData(),
							wrapper2.getData());
				} else if(useConv == true){//za pomocš splotu
					List<double[]> revData = wrapper2.getData();
					Collections.reverse(revData);
					y = SignalsUtils.convolution(wrapper1.getData(),
							revData);
				} else{
					throw new IllegalStateException();
				}
				XYSeries xs = SignalsUtils.fromTableToXYSeries(y,
						wrapper1.getT1(), wrapper1.getSimplingFrequency(), false);
				actualXYSeries = xs;
				chartComponent.changeChartForLinear(new GridLayout(0, 1),
						new XYSeriesCollection(actualXYSeries), SignalType.NONLINEAR);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}
	
	class SimulatorListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Podaj prękoć obiektu");
				String str = bf.readLine();
				double v = Double.parseDouble(str);
				System.out.println("Podaj prędkoć rozchodzenia się fali w orodku");
				str = bf.readLine();
				double V = Double.parseDouble(str);
				System.out.println("Podaj okres sygnału sondujšcego");
				str = bf.readLine();
				double T = Double.parseDouble(str);
				System.out.println("Podaj częstotliwoć próbkowania");
				str = bf.readLine();
				int fp = Integer.parseInt(str);
				System.out.println("Podaj długoć buforów");
				str = bf.readLine();
				double buffLength = Double.parseDouble(str);
				System.out.println("Podaj okres raportowania czujnika");
				str = bf.readLine();
				double sensorT = Double.parseDouble(str);
				
				UnivariateRealFunction orginalFunc = SignalsFactory.createSensorSignal(0, T);
				List<double[]> orginalSignal = SignalsUtils.simplingSignal(orginalFunc, 0, buffLength/fp, fp);
				
				UnivariateRealFunction reflectedFunc = SignalsFactory.createSensorSignal(0, T);
				List<double[]> reflectedSignal = SignalsUtils.simplingSignal(reflectedFunc, 0, buffLength/fp, fp);
				
				//for (int i = 1; i <= 10; ++i){
					
					int timeDiff = (int)((v * sensorT * fp * 2) / V);				
					Collections.rotate(reflectedSignal, timeDiff);
					double[] cor = SignalsUtils.corellation(reflectedSignal, orginalSignal);
					int indexDiff = SignalsUtils.indexDiff(cor, T, fp);
					double distance = SignalsUtils.distance(indexDiff, fp, V);
				
					XYSeries xs = SignalsUtils.fromTableToXYSeries(cor, 0, fp, false);
					chartComponent.changeChartForLinear(new GridLayout(0, 1),
						new XYSeriesCollection(xs), SignalType.LINEAR);
				
					System.err.println("Dystans wynosi: " + distance);
				//}
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}

	}
}

