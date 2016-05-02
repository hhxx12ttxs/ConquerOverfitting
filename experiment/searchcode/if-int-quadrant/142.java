/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import raptor.util.RaptorLogger;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import chesspresso.Chess;
import chesspresso.pgn.PGNSyntaxError;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.chess.Game;
import raptor.chess.Result;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserError;
import raptor.chess.pgn.chesspresso.ChesspressoPgnParser;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.swt.ItemChangedListener;
import raptor.swt.RaptorTable;
import raptor.swt.RaptorTable.RaptorTableListener;
import raptor.swt.chess.controller.InactiveController;
import raptor.util.IntegerComparator;

/**
 * A window item that displays a list of games from a PGN file.
 * 
 * @author mindspan
 * 
 */
public class PgnParseResultsWindowItem implements RaptorWindowItem {
	private static final RaptorLogger LOG = RaptorLogger.getLog(PgnParseResultsWindowItem.class);

    public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };

	protected Composite composite;
	protected RaptorTable gamesTable;
	protected RaptorTable errorsTable;

	protected List<PgnParserError> errors;
	protected List<Game> games;
	protected ArrayList<chesspresso.game.Game> chprsGames;
	protected String title;
	protected boolean isPassive;
	protected String pathToFile;

	private List<PGNSyntaxError> chprsErrors;
	protected static L10n local = L10n.getInstance();

	public PgnParseResultsWindowItem(String title, List<PgnParserError> errors,
			List<Game> games, String pathToFile) {
		this.errors = errors;
		this.games = games;
		this.title = title;
		this.pathToFile = pathToFile;
	}

	public PgnParseResultsWindowItem(String title, List<PGNSyntaxError> chprsErrors,
			ArrayList<chesspresso.game.Game> chprsGames, String pathToFile) {
		this.chprsGames = chprsGames;
		this.title = title;
		this.pathToFile = pathToFile;
		this.chprsErrors = chprsErrors;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		if (errors != null) {
			errors.clear();
			errors = null;
		}
		if (games != null) {
			games.clear();
			games = null;
		}
		if (chprsGames != null) {
			chprsGames.clear();
			chprsGames = null;
		}
		if (composite != null && !composite.isDisposed()) {
			composite.dispose();
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed PgnParseResultsWindowItem"); 
		}

	}

	public Composite getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public String getPercentage(int count, int total) {
		if (total == 0 || count == 0) {
			return "0%"; 
		} else {
			return new BigDecimal((double) count / (double) total * 100.0)
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
					+ "%"; 
		}
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_PGN_RESULTS_QUADRANT);
	}

	public String getTitle() {
		return title;
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		int finishedGames = 0;
		int whiteWins = 0;
		int blackWins = 0;
		int draws = 0;

		if (games != null) { // used standard SimplePgnParser
			for (Game game : games) {
				if (game.getResult() == Result.WHITE_WON) {
					whiteWins++;
					finishedGames++;
				} else if (game.getResult() == Result.BLACK_WON) {
					blackWins++;
					finishedGames++;
				} else if (game.getResult() == Result.DRAW) {
					draws++;
					finishedGames++;
				}
			}
		}
		else { // Chesspresso was used during parsing			
			for (chesspresso.game.Game game: chprsGames) {
				if (game.getResult() == Chess.RES_WHITE_WINS) {
					whiteWins++;
					finishedGames++;
				} else if (game.getResult() == Chess.RES_BLACK_WINS) {
					blackWins++;
					finishedGames++;
				} else if (game.getResult() == Chess.RES_DRAW) {
					draws++;
					finishedGames++;
				}
			}
		}

		Label gamesTotalLabel = new Label(composite, SWT.LEFT);

		int size = (games == null) ? chprsGames.size() : games.size();
		gamesTotalLabel.setText(local.getString("pgnParseWI3") + size + local.getString("pgnParseWI4")
				+ getPercentage(whiteWins, finishedGames) + local.getString("pgnParseWI5")
				+ getPercentage(blackWins, finishedGames) + local.getString("pgnParseWI6")
				+ getPercentage(draws, finishedGames));
		gamesTotalLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		gamesTable = new RaptorTable(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		gamesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (games != null) { // used standard SimplePgnParser 
			gamesTable.addColumn(local.getString("pgnParseWI7"), SWT.LEFT, 3,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI8"), SWT.LEFT, 5,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI9"), SWT.LEFT, 10,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI10"), SWT.LEFT, 15,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI11"), SWT.LEFT, 15,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI12"), SWT.LEFT, 5,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI13"), SWT.LEFT, 15,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI14"), SWT.LEFT, 5,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI15"), SWT.LEFT, 3,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI16"), SWT.LEFT, 3,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI17"), SWT.LEFT, 21,
					true, null);
		}
		else { // Chesspresso was used during parsing
			gamesTable.addColumn(local.getString("pgnParseWI7"), SWT.LEFT, 3,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI8"), SWT.LEFT, 10,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI9"), SWT.LEFT, 10,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI10"), SWT.LEFT, 19,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI11"), SWT.LEFT, 19,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI12"), SWT.LEFT, 5,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI13"), SWT.LEFT, 15,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI14"), SWT.LEFT, 5,
					true, new IntegerComparator());
			gamesTable.addColumn(local.getString("pgnParseWI15"), SWT.LEFT, 7,
					true, null);
			gamesTable.addColumn(local.getString("pgnParseWI16"), SWT.LEFT, 7,
					true, null);
		}
		
			

		gamesTable.addRaptorTableListener(new RaptorTableListener() {

			@Override
			public void cursorMoved(int row, int column) {
			}

			@Override
			public void rowDoubleClicked(MouseEvent event, String[] rowData) {
				openGame(Integer.parseInt(rowData[0])-1);
			}

			@Override
			public void rowRightClicked(MouseEvent event, final String[] rowData) {
				Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
				MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
				deleteItem.setText(local.getString("pgnParseWI18") + rowData[0]);
				deleteItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						String id = rowData[0];
						int rowId = -1;
						for (int i = 0; i < gamesTable.getRowCount(); i++) {
							if (gamesTable.getText(i, 0).equals(id)) {
								rowId = i;
								break;
							}
						}
						gamesTable.removeRow(rowId);
					}
				});
				menu.setLocation(gamesTable.getTable().toDisplay(event.x,
						event.y));
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!gamesTable.getDisplay().readAndDispatch()) {
						gamesTable.getDisplay().sleep();
					}
				}
				menu.dispose();
			}

			@Override
			public void tableSorted() {
			}

			@Override
			public void tableUpdated() {
			}
		});

		populateGamesTable();

		Button saveButton = new Button(composite, SWT.PUSH);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		saveButton.setText(local.getString("pgnParseWI19"));
		saveButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (games == null || games.size() == 0) {
					Raptor.getInstance().alert(local.getString("pgnParseWI20"));
					return;
				}

				FileDialog fd = new FileDialog(composite.getShell(), SWT.SAVE);
				fd.setText(local.getString("pgnParseWI21"));
				File file = new File(pathToFile);
				fd.setFilterPath(file.getParent());
				fd.setFileName(file.getName());
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					pathToFile = selected;
					FileWriter fileWriter = null;
					try {
						fileWriter = new FileWriter(new File(pathToFile), false);
						for (int i = 0; i < gamesTable.getRowCount(); i++) {
							int id = Integer.parseInt(gamesTable.getText(0, 0));
							Game game = games.get(id);
							fileWriter.write(game.toPgn() + "\n\n"); 
							fileWriter.flush();
						}
						Raptor.getInstance().alert(
								local.getString("pgnParseWI23") + gamesTable.getRowCount()
										+ local.getString("pgnParseWI24") + pathToFile + "."); //$NON-NLS-2$
					} catch (Throwable t) {
						Raptor.getInstance().onError(
								local.getString("pgnParseWI26") + pathToFile);
					} finally {
						if (fileWriter != null) {
							try {
								fileWriter.close();
							} catch (Throwable t) {
							}
						}
					}
				}
			}
		});

		if (errors != null && !errors.isEmpty()) {
			Label errorsLabel = new Label(composite, SWT.LEFT);
			errorsLabel.setText(local.getString("pgnParseWI27") + errors.size());
			errorsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));

			errorsTable = new RaptorTable(composite, SWT.BORDER | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
			errorsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));
			errorsTable.addColumn(local.getString("pgnParseWI28"), SWT.LEFT, 50, true, null);
			errorsTable.addColumn(local.getString("pgnParseWI29"), SWT.LEFT, 40, true, null);
			errorsTable.addColumn(local.getString("pgnParseWI30"), SWT.LEFT, 10, true,
					new IntegerComparator());
			String[][] content = new String[errors.size()][3];
			for (int i = 0; i < errors.size(); i++) {
				PgnParserError error = errors.get(i);
				content[i][0] = error.getType().name();
				content[i][1] = error.getAction().name();
				content[i][2] = String.valueOf(error.getLineNumber());
			}
			errorsTable.refreshTable(content);
		}
		else if (chprsErrors != null && !chprsErrors.isEmpty() && chprsErrors.size() > 3) {
			Label errorsLabel = new Label(composite, SWT.LEFT);
			errorsLabel.setText(local.getString("pgnParseWI27") + chprsErrors.size());
			errorsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));

			errorsTable = new RaptorTable(composite, SWT.BORDER | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
			errorsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));
			errorsTable.addColumn(local.getString("pgnParseWI28"), SWT.LEFT, 50, true, null);
			errorsTable.addColumn(local.getString("pgnParseWI29"), SWT.LEFT, 40, true, null);
			errorsTable.addColumn(local.getString("pgnParseWI30"), SWT.LEFT, 10, true,
					new IntegerComparator());
			String[][] content = new String[chprsErrors.size()][3];
			for (int i = 0; i < chprsErrors.size(); i++) {
				PGNSyntaxError error = chprsErrors.get(i);
				content[i][0] = error.getMessage();
				content[i][1] = "";
				content[i][2] = String.valueOf(error.getLineNumber() == 0 ? "Und" : error.getLineNumber());
			}
			errorsTable.refreshTable(content);
		}
	}

	public void onActivate() {
		if (isPassive) {
			if (composite != null && !composite.isDisposed()) {
				composite.layout(true);
			}
			isPassive = false;
		}
	}

	public void onPassivate() {
		isPassive = true;
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

	protected void disposeAllItems(Table table) {
		TableItem[] items = table.getItems();
		for (TableItem item : items) {
			item.dispose();
		}
	}

	protected void openGame(int index) {
		if (games != null) { // used standard SimplePgnParser
			Game selectedGame = games.get(index);
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new ChessBoardWindowItem(new InactiveController(
							selectedGame, selectedGame
									.getHeader(PgnHeader.White)
									+ " vs "
									+ selectedGame.getHeader(PgnHeader.Black),
							false)));
		}
		else if (chprsGames != null) { // Chesspresso was used during parsing
			chesspresso.game.Game selGame = chprsGames.get(index);
			Game raptorGame = ChesspressoPgnParser.convertToRaptorGame(selGame);
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new ChessBoardWindowItem(new InactiveController(
							raptorGame, raptorGame
									.getHeader(PgnHeader.White)
									+ " vs "
									+ raptorGame.getHeader(PgnHeader.Black),
							false)));
		}
	}

	protected void populateGamesTable() {
		if (games == null) { // Chesspresso was used during parsing
			String[][] gamesData = new String[chprsGames.size()][10];
			for (int i = 0; i < chprsGames.size(); i++) {
				chesspresso.game.Game game = chprsGames.get(i);
				gamesData[i][0] = String.valueOf(i + 1);
				gamesData[i][1] = "Standard";
				gamesData[i][2] = game.getDate();
				gamesData[i][3] = game.getEvent();
				gamesData[i][4] = game.getWhite();
				gamesData[i][5] = game.getWhiteEloStr();
				gamesData[i][6] = game.getBlack();
				gamesData[i][7] = game.getBlackEloStr();
				gamesData[i][8] = game.getResultStr();
				gamesData[i][9] = game.getECO();
			}
			gamesTable.refreshTable(gamesData);
		} else { // used standard SimplePgnParser
			String[][] gamesData = new String[games.size()][11];
			for (int i = 0; i < games.size(); i++) {
				Game game = games.get(i);
				gamesData[i][0] = String.valueOf(i);
				gamesData[i][1] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Variant), "?");
				gamesData[i][2] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Date), "?");
				gamesData[i][3] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Event), "?");
				gamesData[i][4] = StringUtils.defaultString(game
						.getHeader(PgnHeader.White), "?");
				gamesData[i][5] = StringUtils.defaultString(game
						.getHeader(PgnHeader.WhiteElo), "?");
				gamesData[i][6] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Black), "?");
				gamesData[i][7] = StringUtils.defaultString(game
						.getHeader(PgnHeader.BlackElo), "?");
				gamesData[i][8] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Result), "?");
				gamesData[i][9] = StringUtils.defaultString(game
						.getHeader(PgnHeader.ECO), "");
				gamesData[i][10] = StringUtils.defaultString(game
						.getHeader(PgnHeader.Opening), "");
			}
			gamesTable.refreshTable(gamesData);
		}
	}

}

