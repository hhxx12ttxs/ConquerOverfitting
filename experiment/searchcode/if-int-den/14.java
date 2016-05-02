package chess.logic;

import chess.Chess;
import chess.logic.figures.*;
import chess.persistance.IPersistance;
import java.util.ArrayList;

/**
 * Diese Klasse verwaltet die Figuren auf dem Schachfeld.
 * Benutzt das Singleton-Entwurfsmuster.
 */
public final class Gameboard {

    /**
     * Die Instanz des Spielfeldes.
     */
    private static Gameboard instance;
    /**
     * Enhaelt die Referenzen auf die Figuren.
     */
    private IGameFigure[][] gameFigures;
    /**
     * Referenz auf den weissen Koenig.
     */
    private King kingWhite;
    /**
     * Referenz auf den schwarzen Koenig.
     */
    private King kingBlack;

    /**
     * Erzeugt das Spielbrett.
     */
    private Gameboard() {
        gameFigures = new IGameFigure[8][8];
        reset();
    }

    /**
     * Liefert die Instanz der Klasse.
     */
    public static Gameboard getInstance() {
        if (instance == null) {
            instance = new Gameboard();
        }
        return instance;
    }

    /**
     * Prueft, ob ein Feld von einer Figur belegt wird.
     * @param f Das zu pr?fende Feld.
     * @return Feld belegt ? true : false;
     */
    public boolean isFieldOccupied(Field f) {
        if (gameFigures[f.getX()][f.getY()] == null) {
            return false;
        }
        return true;
    }

    /**
     * Prueft, ob ein Feld momentan von einer Figur der angegebenen Farbe bedroht wird.
     * @param field Das Feld.
     * @param color Die Farbe.
     * @param ignoreKing Wenn diese Variable true ist, wird der Koenig der anderen Farbe ignoriert. Damit kann der Wirkungsbereich
     * der Figuren ermittelt werden, ohne dass der Koenig moegliche Felder blockiert.
     * @return Wird das Feld bedroht ? true : false
     */
    public boolean isFieldUnderAttack(Field field, Color color, final boolean ignoreKing) {
        IGameFigure[] enemyFigures = getFiguresFromColor(color);
        Field[] temp;
        boolean attack = false;

        Color otherColor;
        if (color == Color.WHITE) {
            otherColor = Color.BLACK;
        } else {
            otherColor = Color.WHITE;
        }

        King otherKing = getKing(otherColor);
        int x = otherKing.getPosition().getX();
        int y = otherKing.getPosition().getY();
        if (ignoreKing) {
            /* Der Koenig wird hier kurz "vom Feld genommen", damit der Wirkungsbereich
             * der gegnerischen Figuren ermittelt werden kann, ohne dass der Koenig moegliche
             * Felder blockiert.
             */
            gameFigures[x][y] = null;
        }
        for (IGameFigure figure : enemyFigures) {

            temp = figure.getAttackFields();
            for (Field posField : temp) {
                if (posField.equals(field)) {
                    attack = true;
                }
            }
        }

        if (ignoreKing) {
            // Den Koenig wieder aufs Brett setzen
            gameFigures[x][y] = otherKing;
        }
        return attack;
    }

    /**
     * Liefert eine die Figur auf dem angegebenen Feld.
     * Liefert 'null', wenn das Feld leer ist.
     * @param x Die x-Koordinate des Feldes
     * @param y Die y-Koordinate des Feldes
     * @throws IllegalArgumentException Falls die Parameter kein legales Feld beschreiben.
     * @return Die Figur falls Feld belegt; 'null' falls Feld leer.
     */
    public IGameFigure getFigure(int x, int y) {
        if (!Field.exists(x, y)) {
            throw new IllegalArgumentException("Field (" + x + "," + y + ") doesn't exist");
        }
        return gameFigures[x][y];
    }

    /**
     *  Liefert eine die Figur auf dem angegebenen Feld.
     *  Liefert 'null', wenn das Feld leer ist.
     *  @param field Das Feld.
     *  @return Die Figur falls Feld belegt; 'null' falls Feld leer.
     */
    public IGameFigure getFigure(Field field) {
        return gameFigures[field.getX()][field.getY()];
    }

    /**
     * Bewegt eine Figur von einem Feld zu einem anderen Feld.
     * Falls auf dem Zielfeld eine Figur steht, wird diese geschlagen.
     * Das Ausgangsfeld ist nachher leer (<i>null</i>).
     *
     * @pre Das Ausgangsfeld ist nicht leer.
     * @param source Das Ausgangsfeld.
     * @param destination Das Zielfeld.
     */
    public synchronized void moveFigure(Field source, Field destination) {

        StringBuffer notation = new StringBuffer();

        handleSpecialMove(source, destination, notation);

        int destX = destination.getX();
        int destY = destination.getY();

        int srcX = source.getX();
        int srcY = source.getY();

        IPersistance persistance = Chess.getPersistance();

        // Namen abfragen
        String[] names = Chess.getLogic().getPlayerNames();
        String onTurnPlayer, otherPlayer;
        if (Chess.getLogic().getOnTurnColor().equals("white")) {
            onTurnPlayer = names[0];
            otherPlayer = names[1];
        } else {
            onTurnPlayer = names[1];
            otherPlayer = names[0];
        }

        // Statistik aendern
        if (gameFigures[destX][destY] != null) {
            String figureType = gameFigures[destX][destY].getClass().getSimpleName();

            if (figureType.equals("Pawn")) {
                persistance.logicInput(onTurnPlayer, "geschlagene Bauern");
                persistance.logicInput(otherPlayer, "verlorene Bauern");
            } else if (figureType.equals("Queen")) {
                persistance.logicInput(onTurnPlayer, "geschlagene Damen");
                persistance.logicInput(otherPlayer, "verlorene Damen");
            } else if (figureType.equals("Rook")) {
                persistance.logicInput(onTurnPlayer, "geschlagene Tuerme");
                persistance.logicInput(otherPlayer, "verlorene Tuerme");
            } else if (figureType.equals("Knight")) {
                persistance.logicInput(onTurnPlayer, "geschlagene Springer");
                persistance.logicInput(otherPlayer, "verlorene Springer");
            } else if (figureType.equals("Bishop")) {
                persistance.logicInput(onTurnPlayer, "geschlagene Laeufer");
                persistance.logicInput(otherPlayer, "verlorene Laeufer");
            }
        }

        persistance.logicInput(onTurnPlayer, "Anzahl Zuege");

        gameFigures[srcX][srcY].setPosition(destination);
        gameFigures[destX][destY] = gameFigures[srcX][srcY];
        gameFigures[srcX][srcY] = null;

        IGameFigure temp;

        Color onTurnColor, otherColor;
        String onTurnColorString = Chess.getLogic().getOnTurnColor();
        if (onTurnColorString.equals("white")) {
            onTurnColor = Color.WHITE;
            otherColor = Color.BLACK;
        } else {
            onTurnColor = Color.BLACK;
            otherColor = Color.WHITE;
        }

        /* Falls der eigene Koenig vor dem Zug im Schach stand, darf er jetzt nicht mehr im Schach stehen!
        Ob der Zug legal war, wird nicht hier geprueft. Es ergibt sich durch die markierten Felder, da jedes
        markierte Feld ein legales Feld ist. */

        this.getKing(onTurnColor).setCheck(false, null);

        // Pruefen, ob der gegnerische Koenig Schach gesetzt wurde
        King otherKing = this.getKing(otherColor);
        Chess.getPresentation().logicInputCheck(false);
        if ((temp = this.isCheck(otherColor)) != null) {
            otherKing.setCheck(true, temp);
            Chess.getPresentation().logicInputCheck(true);
            notation.append("+");
            persistance.logicInput(onTurnPlayer, "gegnerischer Koenig Schach gesetzt");
            persistance.logicInput(otherPlayer, "eigener Koenig Schach gesetzt");
        }

        if (this.isCheckMate(otherColor)) {
            Chess.getPresentation().logicInputCheck(false);
            Chess.getPresentation().logicInputCheckMate();
            notation.append("+");
            Chess.getPersistance().presentationInputEndgame(onTurnPlayer);
        }

        if (this.isRemis(otherColor)) {
            Chess.getPresentation().logicInputCheck(false);
            Chess.getPresentation().logicInputRemis();
            notation.append("=");
            Chess.getPersistance().presentationInputEndgame("");
        }
        Chess.getPersistance().logicInputLog(notation.toString());
    }

    /**
     * Wird vor dem Bewegen einer Figur aufgerufen.
     * Methode behandelt die besonderen Zuege: En-passant-Schlagen, Bauernumwandlung und teilweise die Rochade.
     * @param source Das Ausgangsfeld.
     * @param destination Das Zielfeld.
     * @param sb StringBuffer zur Erstellung der Schachnotation.
     */
    private void handleSpecialMove(Field source, Field destination, StringBuffer notation) {

        notation.append(getFigure(source).getClass().getSimpleName().substring(0, 1));
        notation.append(source.toString());
        if (isFieldOccupied(destination)) {
            notation.append("x");
        } else {
            notation.append("-");
        }
        notation.append(destination.toString());

        IGameFigure figure = getFigure(source);

        // Namen abfragen
        String[] names = Chess.getLogic().getPlayerNames();
        String onTurnPlayer, otherPlayer;
        if (Chess.getLogic().getOnTurnColor().equals("white")) {
            onTurnPlayer = names[0];
            otherPlayer = names[1];
        } else {
            onTurnPlayer = names[1];
            otherPlayer = names[0];
        }

        // Jede Runde muessen die gegnerischen EnPassant-Flags geloescht werden
        IGameFigure[] enemyFigures;
        Pawn pawnTemp;
        if (figure.getColor() == Color.WHITE) {
            enemyFigures = getFiguresFromColor(Color.BLACK);
        } else {
            enemyFigures = getFiguresFromColor(Color.WHITE);
        }
        for (IGameFigure enemyFigure : enemyFigures) {
            if (enemyFigure instanceof Pawn) {
                pawnTemp = (Pawn) enemyFigure;
                pawnTemp.setEnPassantField(null);
            }
        }

        if (figure instanceof Pawn) {
            Pawn pawn = (Pawn) figure;
            // Bauernumwandlung
            if (pawn.getColor() == Color.WHITE && destination.getY() == 7) { // -> weisser Bauer hat Brettende erreicht
                // TODO: Benutzereingabe aus Praesentation holen
                // evtl. public String logicInputGetPawnPromotion(){...} ?
                // Testweise wird eine Dame erzeugt
                int x = source.getX();
                int y = source.getY();  // Die neue Figur wird auf des Feld des Bauern gesetzt, noch NICHT auf das Zielfeld
                Queen queen = new Queen(Color.WHITE, new Field(x, y));
                putFigure(queen);
                Chess.getPresentation().logicInputPutFigure("queen", "white", x, y);
                Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Bauernumwandlungen");
            } else if (pawn.getColor() == Color.BLACK && destination.getY() == 0) { // -> schwarzer Bauer hat Brettende erreicht
                // Testweise wird eine Dame erzeugt
                int x = source.getX();
                int y = source.getY();
                Queen queen = new Queen(Color.BLACK, new Field(x, y));
                putFigure(queen);
                Chess.getPresentation().logicInputPutFigure("queen", "black", x, y);
                Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Bauernumwandlungen");
            }
            // En-passant Schlagen
            // Wurde der Doppelschritt gemacht?
            int delta = source.getY() - destination.getY();
            if (Math.abs(delta) == 2) {
                // Steht links oder rechts ein gegnerischer Bauer?
                int x = destination.getX();
                int y = destination.getY();
                Field left = null, right = null;
                IGameFigure temp;

                if (Field.exists(x - 1, y)) { // Feld links vom Bauern
                    left = new Field(x - 1, y);
                }
                if (Field.exists(x + 1, y)) { // Feld rechts vom Bauern
                    right = new Field(x + 1, y);
                }

                Field[] neigbours = {left, right};

                for (int i = 0; i < 2; i++) {
                    if (neigbours[i] != null) {
                        temp = getFigure(neigbours[i]);
                        if (temp != null) {
                            // Wenn links oder rechts ein gegnerischer Bauer steht, wird bei ihm das En-Passant-Feld registriert.
                            if (temp instanceof Pawn) {
                                if (temp.getColor() != pawn.getColor()) {
                                    Pawn tempPawn = (Pawn) temp;
                                    if (delta == 2) {   // schwarzer Bauer wurde gezogen
                                        tempPawn.setEnPassantField(new Field(source.getX(), source.getY() - 1));
                                    } else {  // delta ist somit -2 -> es wurde ein weisser Bauer gezogen
                                        tempPawn.setEnPassantField(new Field(source.getX(), source.getY() + 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Schlagen
            Field enPassField;
            if ((enPassField = pawn.getEnPassantField()) != null) {
                if (enPassField.equals(destination)) {
                    // Feld des gegnerischen Bauern ermitteln, um ihn entfernen zu koennen
                    int x = destination.getX();
                    int y;
                    if (pawn.getColor() == Color.WHITE) {
                        y = destination.getY() - 1;
                    } else {
                        y = destination.getY() + 1;
                    }
                    gameFigures[x][y] = null;
                    Chess.getPresentation().logicInputRemoveFigure(x, y);
                    notation.append(" e.p.");
                    Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl en-passants");
                    Chess.getPersistance().logicInput(onTurnPlayer, "geschlagene Bauern");
                    Chess.getPersistance().logicInput(otherPlayer, "verlorene Bauern");
                }
            }

        } else if (figure instanceof King) {
            // Rochade weiss
            if (source.equals(new Field('e', 1))) {
                if (destination.equals(new Field('c', 1))) {
                    // Turm bewegen
                    Chess.getPresentation().logicInputRemoveFigure(0, 0);
                    Chess.getPresentation().logicInputPutFigure("rook", "white", 3, 0);
                    moveFigure(new Field(0, 0), new Field(3, 0));
                    notation.setLength(0);
                    notation.append("0-0-0");
                    Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Rochaden");
                } else if (destination.equals(new Field('g', 1))) {
                    // Turm bewegen
                    Chess.getPresentation().logicInputRemoveFigure(7, 0);
                    Chess.getPresentation().logicInputPutFigure("rook", "white", 5, 0);
                    moveFigure(new Field(7, 0), new Field(5, 0));
                    notation.setLength(0);
                    notation.append("0-0");
                    Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Rochaden");
                }
                // Rochade schwarz
            } else if (source.equals(new Field('e', 8))) {
                if (destination.equals(new Field('c', 8))) {
                    // Turm bewegen
                    Chess.getPresentation().logicInputRemoveFigure(0, 7);
                    Chess.getPresentation().logicInputPutFigure("rook", "black", 3, 7);
                    moveFigure(new Field(0, 7), new Field(3, 7));
                    notation.setLength(0);
                    notation.append("0-0-0");
                    Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Rochaden");
                } else if (destination.equals(new Field('g', 8))) {
                    // Turm bewegen
                    Chess.getPresentation().logicInputRemoveFigure(7, 7);
                    Chess.getPresentation().logicInputPutFigure("rook", "black", 5, 7);
                    moveFigure(new Field(7, 7), new Field(5, 7));
                    notation.setLength(0);
                    notation.append("0-0");
                    Chess.getPersistance().logicInput(onTurnPlayer, "Anzahl Rochaden");
                }
            }
        }
    }

    /**
     * Diese Methode wird statt der getMoves()-Methode der Figur aufgerufen, falls der Koenig durch einen
     * "normalen" Zug der Figur im Schach stehen koennte. Ob diese Methode verwendet werden muss, muss vor dem Aufruf
     * mit der wouldBeCheckWithoutFigure() Methode geprueft werden.
     * @param figure Die Figur.
     * @return Alle Felder die erreicht werden koennen, ohne dass der Koenig nach dem Zug im Schach steht.
     */
    public Field[] getSpecialFields(IGameFigure figure) {

        Color enemyColor;
        if (figure.getColor() == Color.WHITE) {
            enemyColor = Color.BLACK;
        } else {
            enemyColor = Color.WHITE;
        }

        ArrayList<Field> specialFields = new ArrayList<Field>();

        IGameFigure[] enemyFigures = getFiguresFromColor(enemyColor);
        IGameFigure attackingFigure = null;

        Field[] possibleFields = figure.getMoves();
        Field[] temp = null;

        // Figur voruebergehend vom Brett nehmen
        Field position = figure.getPosition();
        int x = position.getX();
        int y = position.getY();
        gameFigures[x][y] = null;

        boolean flag = false;

        // Angriffslinie suchen
        for (IGameFigure enemyFigure : enemyFigures) {
            temp = enemyFigure.getAttackFields();
            for (Field field : temp) {
                if (getFigure(field) == getKing(figure.getColor())) { // '==' geht in diesem Fall
                    // Die Figur, die den Koenig bedrohen wuerde, ist gefunden
                    attackingFigure = enemyFigure;
                    flag = true;
                }
                if (flag) {
                    break;
                }
            }
            if (flag) {
                break;
            }
        }

        // Figur wieder aufs Brett setzen
        gameFigures[x][y] = figure;

        Field attackingFigurePosition = attackingFigure.getPosition(); // gegnerische Figur
        Field kingPosition = getKing(figure.getColor()).getPosition(); // eigener Koenig

        Field[] betweenFields = Field.getFieldsBetween(attackingFigurePosition, kingPosition);

        for (Field pField : possibleFields) {
            /* Von allen moeglichen Feldern nur die zwischen
            Koenig und gegnerischer Figur rausfiltern */
            for (Field bField : betweenFields) {
                if (pField.equals(bField)) {
                    specialFields.add(bField);
                }
            }
            // Schlagen der gegnerischen Figur moeglich?
            if (pField.equals(attackingFigurePosition)) {
                specialFields.add(pField);
            }
        }

        Field[] fields = new Field[specialFields.size()];
        return specialFields.toArray(fields);
    }

    /**
     * Diese Methode ermittelt, ob ein Spieler im Schach stehen wuerde,
     * wenn die angegebene Figur nicht auf dem Brett stehen wuerde.
     * @param figure Eine Figur.
     * @return Schachsituatoin ohne diese Figur? true : false
     */
    public boolean wouldBeCheckWithoutFigure(IGameFigure figure) {

        if (getKing(figure.getColor()).isCheck()) {
            // Koenig steht schon im Schach
            return false;
        }

        Color enemyColor;
        if (figure.getColor() == Color.WHITE) {
            enemyColor = Color.BLACK;
        } else {
            enemyColor = Color.WHITE;
        }

        int x = figure.getPosition().getX();
        int y = figure.getPosition().getY();

        // Figur vom Brett nehmen
        gameFigures[x][y] = null;

        Field kingPosition = getKing(figure.getColor()).getPosition();
        IGameFigure[] enemyFigures = getFiguresFromColor(enemyColor);
        for (IGameFigure f : enemyFigures) {
            Field[] fields = f.getAttackFields();
            for (Field field : fields) {
                if (field.equals(kingPosition)) {
                    gameFigures[x][y] = figure;
                    return true;
                }
            }
        }
        gameFigures[x][y] = figure;
        return false;
    }

    /**
     * Diese Methode prueft, ob ein Spieler im Schach steht.
     * @param color Die Farbe des Spielers.
     * @return Die Figur, die den Koenig bedroht, bzw. <i>null</i> wenn der Koenig nicht im Schach steht.
     */
    public IGameFigure isCheck(Color color) {
        Color enemyColor;
        if (color == Color.WHITE) {
            enemyColor = Color.BLACK;
        } else {
            enemyColor = Color.WHITE;
        }
        Field kingPosition = getKing(color).getPosition();
        IGameFigure[] enemyFigures = getFiguresFromColor(enemyColor);
        for (IGameFigure figure : enemyFigures) {
            Field[] fields = figure.getAttackFields();
            for (Field field : fields) {
                if (field.equals(kingPosition)) {
                    return figure;
                }
            }
        }
        return null;
    }

    /**
     * Diese Methode ermittelt, ob ein Spieler schachmatt ist.
     * @param color Die Farbe des Spielers.
     * @return Spieler schachmatt ? true : false
     */
    public boolean isCheckMate(Color color) {
        Color enemyColor;
        if (color == Color.WHITE) {
            enemyColor = Color.BLACK;
        } else {
            enemyColor = Color.WHITE;
        }

        King king = getKing(color);

        if (!king.isCheck()) {
            return false;
        }

        // Gibt es ein freies Feld, das nicht bedroht wird?
        Field[] possibleFields = king.getMoves();
        if (possibleFields.length != 0) {
            for (Field f : possibleFields) {
                if (!isFieldUnderAttack(f, enemyColor, true)) {
                    return false;
                }
            }
        }

        // Kann die bedrohende Figur geschlagen werden?
        IGameFigure attackingFigure = king.getAttackingFigure();
        assert attackingFigure != null;
        Field attackPosition = attackingFigure.getPosition();
        Field[] myFields;
        IGameFigure[] myFigures = getFiguresFromColor(color);
        // Es muss fuer jede eigene Figur geschaut werden, ob sie den Angreifer schlagen kann
        for (IGameFigure myFigure : myFigures) {
            myFields = myFigure.getMoves();
            for (Field myField : myFields) {
                if (myField.equals(attackPosition)) {
                    return false;
                }
            }
        }

        // Kann eine Figur dazwischengestellt werden?
        if ((attackingFigure instanceof Knight) || (attackingFigure instanceof Pawn)) {
            // Geht bei Bauern oder Springern nicht -> Schachmatt
            return true;
        }
        Field kingPosition = king.getPosition();
        Field[] betweenFields = Field.getFieldsBetween(kingPosition, attackPosition);

        // Sehr aufwendig: Fuer jede Figur muss muss fuer jedes moegliche Feld geprueft werden,
        // ob es mit einem der Felder zwischen Koenig und schach-bietender Figur uebereinstimmt
        for (IGameFigure myFigure : myFigures) {
            myFields = myFigure.getMoves();
            for (Field f : myFields) {
                for (Field b : betweenFields) {
                    if (f.equals(b)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Diese Methode prueft, ob ein Spiel Remis ist.
     * @param color Die Farbe des Spielers
     * @return Spiel ist Remis? true : false
     */
    public boolean isRemis(Color color) {
        // Die Partie ist Remis, wenn der Spieler nicht im Schach
        // steht, aber trotzdem keinen Zug mehr machen kann
        if (getKing(color).isCheck()) {
            return false;
        }
        IGameFigure[] figures = getFiguresFromColor(color);
        Field[] moves = null;
        for (IGameFigure figure : figures) {
            if (wouldBeCheckWithoutFigure(figure)) {
                moves = getSpecialFields(figure);
            } else {
                moves = figure.getMoves();
            }
            if (moves.length > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Liefert alle Figuren der angegebenen Farbe, die sich noch auf dem Feld befinden.
     * @param color Eine Farbe
     * @return Alle Figuren der Farbe.
     */
    public IGameFigure[] getFiguresFromColor(Color color) {
        ArrayList<IGameFigure> figures = new ArrayList<IGameFigure>();
        IGameFigure temp;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                temp = gameFigures[i][j];
                if (temp != null) {
                    if (temp.getColor() == color) {
                        figures.add(temp);
                    }
                }
            }
        }
        IGameFigure[] f = new IGameFigure[figures.size()];
        return figures.toArray(f);
    }

    /**
     * Liefert alle Figuren, die sich noch auf dem Feld befinden.
     * @return Ein Array mit allen Figuren.
     */
    public IGameFigure[] getAllFigures() {
        ArrayList<IGameFigure> figures = new ArrayList<IGameFigure>();
        IGameFigure temp;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                temp = gameFigures[i][j];
                if (temp != null) {
                    figures.add(temp);
                }
            }
        }
        IGameFigure[] f = new IGameFigure[figures.size()];
        return figures.toArray(f);
    }

    /**
     * Liefert den Koenig der angegebenen Farbe.
     * @param color Eine Spielerfarbe.
     * @return Den Koenig des Spielers.
     */
    public King getKing(Color color) {
        if (color == Color.WHITE) {
            return kingWhite;
        } else {
            return kingBlack;
        }
    }

    /**
     * Initialisiert das Spielbrett.
     * Alle Figuren befinden sich nach der Initialisierung auf Ausgangposition.
     */
    public synchronized void reset() {
        clear();
        // Weisse Figuren
        gameFigures[0][0] = new Rook(Color.WHITE, new Field('a', 1));
        gameFigures[1][0] = new Knight(Color.WHITE, new Field('b', 1));
        gameFigures[2][0] = new Bishop(Color.WHITE, new Field('c', 1));
        gameFigures[3][0] = new Queen(Color.WHITE, new Field('d', 1));
        gameFigures[4][0] = new King(Color.WHITE, new Field('e', 1));
        gameFigures[5][0] = new Bishop(Color.WHITE, new Field('f', 1));
        gameFigures[6][0] = new Knight(Color.WHITE, new Field('g', 1));
        gameFigures[7][0] = new Rook(Color.WHITE, new Field('h', 1));
        // Bauern
        for (int i = 0; i < 8; i++) {
            gameFigures[i][1] = new Pawn(Color.WHITE, new Field((char) ('a' + i), 2));
        }
        // Schwarze Figuren
        gameFigures[0][7] = new Rook(Color.BLACK, new Field('a', 8));
        gameFigures[1][7] = new Knight(Color.BLACK, new Field('b', 8));
        gameFigures[2][7] = new Bishop(Color.BLACK, new Field('c', 8));
        gameFigures[3][7] = new Queen(Color.BLACK, new Field('d', 8));
        gameFigures[4][7] = new King(Color.BLACK, new Field('e', 8));
        gameFigures[5][7] = new Bishop(Color.BLACK, new Field('f', 8));
        gameFigures[6][7] = new Knight(Color.BLACK, new Field('g', 8));
        gameFigures[7][7] = new Rook(Color.BLACK, new Field('h', 8));
        // Bauern
        for (int i = 0; i < 8; i++) {
            gameFigures[i][6] = new Pawn(Color.BLACK, new Field((char) ('a' + i), 7));
        }
        kingWhite = (King) gameFigures[4][0];
        kingBlack = (King) gameFigures[4][7];
    }

    /**
     * Leert des Spielfeld.
     */
    public void clear() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                gameFigures[i][j] = null;
            }
        }
    }

    /**
     * Setzt eine Figur auf das Spielfeld.
     * Falls auf dem Feld schon eine Figur steht, wird diese ersetzt.
     * @param figure Die neue Figur.
     */
    public void putFigure(IGameFigure figure) {
        Field pos = figure.getPosition();
        gameFigures[pos.getX()][pos.getY()] = figure;

        // Referenz auf den Koenig setzen.
        if (figure instanceof King) {
            King king = (King) figure;
            if (king.getColor() == Color.WHITE) {
                kingWhite = king;
            } else {
                kingBlack = king;
            }
        }
        // Evtl. das Schach-Flag setzen
        IGameFigure attackingFigure;
        if (kingWhite != null) {
            if ((attackingFigure = isCheck(Color.WHITE)) != null) {
                kingWhite.setCheck(true, attackingFigure);
            }
        }
        if (kingBlack != null) {
            if ((attackingFigure = isCheck(Color.BLACK)) != null) {
                kingBlack.setCheck(true, attackingFigure);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        IGameFigure figure;
        String figureType, color;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                figure = gameFigures[j][7 - i]; // '7-i' -> Reihe Nr 8 muss zuerst ausgegeben werden
                if (figure == null) {
                    sb.append("[   ]");
                    continue;
                }
                figureType = figure.getClass().getSimpleName().substring(0, 2);
                color = figure.getColor().toString().substring(0, 1);
                sb.append("[" + figureType + color + "]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

