package br.com.dragonrise.filesizeanalyzer.gui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.SortOrder;

import com.google.common.collect.Lists;

import br.com.dragonrise.filesizeanalyzer.DirectoryNode;
import br.com.dragonrise.filesizeanalyzer.FileNode;
import br.com.dragonrise.filesizeanalyzer.FileSize;
import br.com.dragonrise.filesizeanalyzer.FileTreeCreator;
import br.com.dragonrise.filesizeanalyzer.FileTreeCreator.TreeResult;

public class ResultsPanel
		extends
			JPanel {

	private static final Logger logger = LogManager
			.getLogger ( ResultsPanel.class );

	private static final long serialVersionUID = -3037977071271230414L;

	private JTree fileTree;
	private ChartPanel graphic;
	private JTextArea console;

	private final StatusBar statusBar;

	private abstract class AbstractTreeAction
			extends
				AbstractAction {
		private static final long serialVersionUID = 1907361405302375363L;

		public AbstractTreeAction ( final String name ) {
			super ( name );
		}

		public void actionPerformed ( final ActionEvent e ) {
			final FileNode fileNode = (FileNode) fileTree
					.getSelectionModel ( )
						.getSelectionPath ( )
						.getLastPathComponent ( );

			doAction ( fileTree, fileNode );
		}

		public abstract void doAction ( JTree tree, FileNode selection );
	};

	public ResultsPanel ( ) {
		this ( null );
	}

	public ResultsPanel ( final StatusBar progress ) {
		statusBar = progress;
		initLayout ( );
	}

	private void initLayout ( ) {
		setLayout ( new BorderLayout ( ) );

		fileTree = new JTree ( (TreeModel) null );
		fileTree.setShowsRootHandles ( true );
		fileTree.setRootVisible ( true );
		fileTree.addTreeSelectionListener ( new TreeSelectionListener ( ) {
			public void valueChanged ( final TreeSelectionEvent e ) {
				final TreePath selection = e.getNewLeadSelectionPath ( );
				if ( selection != null ) {
					final FileNode fileNode = (FileNode) selection
							.getLastPathComponent ( );
					treeClicked ( fileNode );
				} else {
					graphic.setChart ( null );
				}
			}
		} );
		fileTree.getSelectionModel ( ).setSelectionMode (
				TreeSelectionModel.SINGLE_TREE_SELECTION );

		final JPopupMenu popupMenu = createContextMenu ( );
		fileTree.addMouseListener ( new MouseAdapter ( ) {
			@Override
			public void mousePressed ( final java.awt.event.MouseEvent e ) {
				handlePopupTrigger ( popupMenu, e );
			};

			@Override
			public void mouseReleased ( final java.awt.event.MouseEvent e ) {
				handlePopupTrigger ( popupMenu, e );
			};
		} );

		graphic = new ChartPanel ( null );
		graphic.addChartMouseListener ( new ChartMouseListener ( ) {

			public void chartMouseMoved ( final ChartMouseEvent event ) {
				final ChartEntity eventEntity = event.getEntity ( );
				if ( eventEntity instanceof PieSectionEntity ) {
					final PieSectionEntity entity = (PieSectionEntity) eventEntity;
					final Comparable<?> sectionKey = entity.getSectionKey ( );
					final Number value = entity.getDataset ( ).getValue (
							sectionKey );

					statusBar.setStatusText ( sectionKey.toString ( ) );
					statusBar.setSecondaryText ( value.toString ( ) );
				} else {
					final FileNode fileNode = (FileNode) fileTree
							.getSelectionModel ( )
								.getSelectionPath ( )
								.getLastPathComponent ( );
					statusBar.setStatusText ( fileNode.toString ( ) );
					statusBar.setSecondaryText ( fileNode
							.getNodeSize ( )
								.toString ( ) );
				}
			}

			public void chartMouseClicked ( final ChartMouseEvent event ) {
				final ChartEntity eventEntity = event.getEntity ( );
				if ( eventEntity instanceof PieSectionEntity ) {
					final PieSectionEntity entity = (PieSectionEntity) eventEntity;
					final FileNode sectionKey = (FileNode) entity
							.getSectionKey ( );
					final TreePath selectionPath = fileTree
							.getSelectionModel ( )
								.getSelectionPath ( )
								.pathByAddingChild ( sectionKey );
					fileTree.scrollPathToVisible ( selectionPath );
					fileTree.setSelectionPath ( selectionPath );
				}
			}
		} );

		final JSplitPane splitPane = new JSplitPane (
				JSplitPane.HORIZONTAL_SPLIT, new JScrollPane ( fileTree ),
				graphic );
		add ( splitPane, BorderLayout.CENTER );
		splitPane.setDividerLocation ( 200 );

		console = new JTextArea ( );
		final JScrollPane scroll = new JScrollPane ( console );
		scroll.setPreferredSize ( new Dimension ( 200, 200 ) );
		add ( scroll, BorderLayout.SOUTH );
	}

	private JPopupMenu createContextMenu ( ) {
		final JPopupMenu popupMenu = new JPopupMenu ( );

		popupMenu.add ( new JMenuItem ( new AbstractTreeAction (
				"Open on system" ) {
			/**
					 * 
					 */
			private static final long serialVersionUID = -3186023306321317497L;

			@Override
			public void doAction ( final JTree tree, final FileNode selection ) {
				final File file = selection.getFile ( );
				try {
					Desktop.getDesktop ( ).open ( file );
				} catch ( final IOException e ) {
					logger.error ( e );
					e.printStackTrace ( );
					JOptionPane.showMessageDialog ( ResultsPanel.this,
							"It wasn't possible to open the file",
							"Error opening", JOptionPane.ERROR_MESSAGE );
				}
			}

		} ) );

		popupMenu.add ( new JMenuItem ( new AbstractTreeAction (
				"Show on folder" ) {
			/**
					 * 
					 */
			private static final long serialVersionUID = -3990797586289866438L;

			@Override
			public void doAction ( final JTree tree, final FileNode selection ) {
				final File file = selection.getFile ( );
				final File parentFile = file.getParentFile ( );
				try {
					Desktop.getDesktop ( ).open ( parentFile );
				} catch ( final IOException e ) {
					logger.error ( e );
					e.printStackTrace ( );
					JOptionPane.showMessageDialog ( ResultsPanel.this,
							"It wasn't possible to open the file",
							"Error opening", JOptionPane.ERROR_MESSAGE );
				}
			}

		} ) );

		popupMenu.add ( new JMenuItem ( new AbstractTreeAction ( "Remove" ) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -9151860712195494829L;

			@Override
			public void doAction ( final JTree tree, final FileNode selection ) {
				final File file = selection.getFile ( );
				if ( JOptionPane.showConfirmDialog ( ResultsPanel.this,
						"Are you sure you want to remove the file \"" + file
								+ "\"", "Confirm deletion",
						JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {
					logger.info ( "Remove file " + file );
				}
			}
		} ) );
		popupMenu.addSeparator ( );

		popupMenu.add ( new JMenuItem ( new AbstractTreeAction ( "Refresh" ) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3151807575318194958L;

			@Override
			public void doAction ( final JTree tree, final FileNode selection ) {
				logger.info ( "Update node " + selection );
			}
		} ) );
		popupMenu.addSeparator ( );

		popupMenu.add ( new JMenuItem (
				new AbstractTreeAction ( "Save report" ) {
					private static final long serialVersionUID = -3151807575318194958L;

					@Override
					public void doAction ( final JTree tree,
							final FileNode selection ) {
						final JFileChooser fileChooser = new JFileChooser ( );
						fileChooser
								.setFileSelectionMode ( JFileChooser.FILES_ONLY );
						if ( fileChooser.showSaveDialog ( ResultsPanel.this ) == JFileChooser.APPROVE_OPTION ) {
							File selectedFile = fileChooser.getSelectedFile ( );
							if ( !hasTxtExtension ( selectedFile ) ) {
								selectedFile = new File ( selectedFile
										.getAbsolutePath ( ) + ".txt" );
							}
							final String LINE_SEPARATOR = System
									.getProperty ( "line.separator" );

							if ( selectedFile.exists ( )
									&& ( JOptionPane.showConfirmDialog (
											ResultsPanel.this,
											"The file "
													+ selectedFile
															.getAbsolutePath ( )
													+ " already exists."
													+ LINE_SEPARATOR
													+ "Do you wish to override it?",
											"Overwrite confirmation",
											JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION ) ) {
								return;
							}

							final FileSize selectionSize = selection
									.getNodeSize ( );

							final StringBuilder report = new StringBuilder ( );
							report.append ( "Directory scanned: " );
							report.append (
									selection.getFile ( ).getAbsolutePath ( ) )
										.append ( LINE_SEPARATOR );
							report.append ( "Date: " )
										.append ( new Date ( ) )
										.append ( LINE_SEPARATOR );
							report.append ( "Total files on directory: " )
										.append (
												selection.getTotalChildCount ( ) )
										.append ( LINE_SEPARATOR );
							report.append ( "Total size: " )
										.append ( selectionSize )
										.append ( LINE_SEPARATOR );
							report.append ( LINE_SEPARATOR );
							final List<FileNode> childs = Lists
									.newArrayList ( selection.getChilds ( ) );
							Collections.sort ( childs,
									new Comparator<FileNode> ( ) {
										public int compare ( final FileNode o1,
												final FileNode o2 ) {
											return new CompareToBuilder ( )
													.append (
															o2.getNodeSize ( ),
															o1.getNodeSize ( ) )
														.append (
																o1.getFile ( ),
																o2.getFile ( ) )
														.toComparison ( );
										}
									} );
							for ( final FileNode child : childs ) {
								final FileSize childSize = child.getNodeSize ( );
								final double childPercentage = ( childSize
										.asBytes ( ) * 100.0 )
										/ selectionSize.asBytes ( );
								report.append ( String.format (
										"%6.2f\t%25s  %s%s", childPercentage,
										childSize, child
												.getFile ( )
													.getAbsolutePath ( ),
										LINE_SEPARATOR ) );
							}

							try {
								final Writer writer = new FileWriter (
										selectedFile );
								writer.append ( report );
								writer.close ( );
							} catch ( final IOException e ) {
								// TODO Auto-generated catch block
								e.printStackTrace ( );
							}
						}
					}

					private boolean hasTxtExtension ( final File file ) {
						final String name = file.getName ( );
						final int dot = name.lastIndexOf ( '.' );
						if ( dot == -1 ) {
							return false;
						}

						final String extension = name.substring ( dot + 1 );
						return "txt".equals ( extension );
					}
				} ) );

		return popupMenu;
	}

	public void analyzeFile ( final File file ) {
		checkNotNull ( file, "file is null" );
		statusBar.startProgressBar ( );
		final SwingWorker<TreeResult, Void> worker = new SwingWorker<TreeResult, Void> ( ) {
			@Override
			protected TreeResult doInBackground ( )
					throws Exception {
				return FileTreeCreator.createTree ( file );
			}

			@Override
			protected void done ( ) {
				try {
					statusBar.stopProgressBar ( );
					resultsReadyCallback ( get ( ) );
				} catch ( final Exception exception ) {
					throw new RuntimeException ( exception );
				}
			}
		};
		worker.execute ( );
	}

	private void resultsReadyCallback ( final TreeResult result ) {
		checkNotNull ( result, "result is null" );

		fileTree.setModel ( new FileResultTreeModel ( result ) );
		final TreePath rootPath = new TreePath ( result.getRootNode ( ) );
		fileTree.setSelectionPath ( rootPath );
		fileTree.scrollPathToVisible ( rootPath );

		console.setText ( null );
		final StringBuilder consoleText = new StringBuilder ( );
		consoleText.append ( String.format ( "Scanned %d files.\n", result
				.getRootNode ( )
					.getTotalChildCount ( ) ) );
		final List<File> filesWithError = result.getFilesWithError ( );
		if ( !filesWithError.isEmpty ( ) ) {
			consoleText
					.append ( "\nIt was't possible to read the following files:\n" );

			for ( final File file : filesWithError ) {
				consoleText.append ( file ).append ( '\n' );
			}
			consoleText
					.append ( "\nTry running again with administrator powers." );
		}
		console.setText ( consoleText.toString ( ) );
	}

	private void treeClicked ( final FileNode fileNode ) {
		statusBar.startProgressBar ( );
		final SwingWorker<JFreeChart, Void> worker = new SwingWorker<JFreeChart, Void> ( ) {
			@Override
			protected JFreeChart doInBackground ( )
					throws Exception {
				final DefaultPieDataset dataSet = new DefaultPieDataset ( );

				if ( fileNode instanceof DirectoryNode ) {
					final DirectoryNode dirNode = (DirectoryNode) fileNode;
					for ( final FileNode child : dirNode.getChilds ( ) ) {
						addNodeToDataSet ( dataSet, child );
					}
				} else {
					addNodeToDataSet ( dataSet, fileNode );
				}

				dataSet.sortByValues ( SortOrder.DESCENDING );
				final JFreeChart chart = ChartFactory.createPieChart (
						fileNode.toString ( ), dataSet, false, true, false );
				( (PiePlot) chart.getPlot ( ) )
						.setToolTipGenerator ( new PieToolTipGenerator ( ) {
							public String
									generateToolTip ( final PieDataset dataset,
											@SuppressWarnings ( "rawtypes" ) final Comparable key ) {
								final Number value = dataset.getValue ( key );
								final double total = DatasetUtilities
										.calculatePieDatasetTotal ( dataset );
								final double percent = ( value.doubleValue ( ) * 100.0 )
										/ total;

								return String.format ( "%s, %s (%.2f%%)", key,
										value.toString ( ), percent );
							}
						} );
				return chart;
			}

			private void addNodeToDataSet ( final DefaultPieDataset dataSet,
					final FileNode child ) {
				dataSet.setValue ( child, child.getNodeSize ( ) );
			}

			@Override
			protected void done ( ) {
				try {
					statusBar.stopProgressBar ( );
					showPlot ( get ( ) );
				} catch ( final Exception exception ) {
					throw new RuntimeException ( exception );
				}
			}
		};
		worker.execute ( );
	}

	private void showPlot ( final JFreeChart chart ) {
		final FileNode fileNode = (FileNode) fileTree
				.getSelectionModel ( )
					.getSelectionPath ( )
					.getLastPathComponent ( );
		statusBar.setStatusText ( fileNode.toString ( ) );
		statusBar.setSecondaryText ( fileNode.getNodeSize ( ).toString ( ) );

		graphic.setChart ( chart );

		graphic.revalidate ( );
		graphic.repaint ( );
	}

	private void handlePopupTrigger ( final JPopupMenu popupMenu,
			final MouseEvent e ) {
		if ( e.isPopupTrigger ( ) ) {
			final JTree tree = (JTree) e.getComponent ( );
			final int x = e.getX ( );
			final int y = e.getY ( );

			final TreePath path = tree.getPathForLocation ( x, y );

			tree.getSelectionModel ( ).setSelectionPath ( path );
			if ( path != null ) {
				popupMenu.show ( tree, x, y );
			}
		}
	}
}

