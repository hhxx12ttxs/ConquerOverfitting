package pl.edu.amu.wmi.daut.base;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Klasa abstrakcyjna reprezentująca specyfikację (opis) automatu
 * (jakie są stany, przejścia, który stan jest stanem początkowym,
 * które stany są stanami akceptującymi).
 *
 * Uwaga: klasa ta nie reprezentuje działającego automatu (nie ma tu funkcji
 * odpowiadających na pytanie, czy automat akceptuje napis, czy nie),
 * tylko "zawartość" automatu.
 */
abstract class AutomatonSpecification implements Cloneable  {

    // metody "budujące" automat
    /**
     * Dodaje nowy stan do automatu.
     *
     * Zwraca dodany stan.
     */
    public abstract State addState();

    /**
     * Dodaje przejście od stanu 'from' do stanu 'to' etykietowane etykietą transitionLabel.
     */
    public abstract void addTransition(State from, State to, TransitionLabel transitionLabel);
    /**
     * Dodaje przejście od stanu 'from' do nowo utworzonego stanu 'to' etykietowane etykietą
     * transitionLabel, a następnie zwraca utworzony stan.
     */
    public State addTransition(State from, TransitionLabel transitionLabel) {

        State to = addState();
        addTransition(from, to, transitionLabel);

        return to;
    }

    /**
     * Dla zadanego słowa dodaje stany i przejścia.
     *
     * Zwraca stan końcowy.
     */
    public State addTransitionSequence(State from, String text) {
        State prev = from;
        State next = prev;

        for (int i = 1; i <= text.length(); i++) {
            prev = addTransition(next,
                    new CharTransitionLabel(text.charAt(i - 1)));
            next = prev;
        }
       return prev;
    }

    /**
     * Tworzy "gałąź" w automacie.
     * Metoda dodaje ciąg przejść od stanu początkowego automatu,
     * dla podanej listy etykiet przejść.
     * Metoda zwraca (nowo utworzony) stan docelowy ostatniego przejścia.
     */
    public State addBranch(State from, List<TransitionLabel> oTransition) {
        State prev = from;
        State next = prev;

         for (TransitionLabel transition : oTransition) {
             prev = addTransition(next, transition);
             next = prev;
         }
        return prev;
    }

    /**
     * Oznacza stan jako początkowy.
     */
    public abstract void markAsInitial(State state);

    /**
     * Metoda budująca 2-stanowy automat z jednym przejściem.
     */
    public AutomatonSpecification makeOneTransitionAutomaton(char c) {
        AutomatonSpecification spec1 = new NaiveAutomatonSpecification();
        State q0 = spec1.addState();
        State q1 = spec1.addState();
        spec1.addTransition(q0, q1, new CharTransitionLabel(c));
        spec1.markAsInitial(q0);
        spec1.markAsFinal(q1);
        return spec1;
    }

    public AutomatonSpecification makeMultiples(char c, int n){
    	AutomatonSpecification spec1 = new NaiveAutomatonSpecification();
    	State q0 = spec1.addState();
    	spec1.markAsInitial(q0);
    	spec1.markAsFinal(q0);
    	spec1.addLoop(q0, new CharTransitionLabel(c));    	
    	return spec1;
    }
    
    /**
     * Oznacza stan jako końcowy (akceptujący).
     */
    public abstract void markAsFinal(State state);

    // metody zwracające informacje o automacie
    /**
     * Zwraca listę wszystkich stanów.
     *
     * Stany niekoniecznie muszą być zwrócone w identycznej
     * kolejności jak były dodane.
     */
    public abstract List<State> allStates();

    /**
     * Zwraca listę wszystkich przejść wychodzących ze stanu 'from'.
     *
     * Przejścia niekoniecznie muszą być zwrócone w identycznej
     * kolejności jak były dodane.
     */
    public abstract List<OutgoingTransition> allOutgoingTransitions(State from);

    /**
     * Zwraca stan początkowy.
     */
    public abstract State getInitialState();

    /**
     * Zwraca true wgdy stan jest stanem końcowym.
     */
    public abstract boolean isFinal(State state);

    /**
     * Metoda sprawdza czy automat jest pusty.
     */
    public boolean isEmpty() {

        List<State> states = allStates();
        if (states.isEmpty())
            return true;
        return false;
    }

    /**
     * Zwraca zawartość automatu w czytelnej dla człowieka postaci String'a.
     * @return String
     */
    @Override
    public String toString() {
        StringBuffer retString = new StringBuffer("Automaton:\n-States: ");

        //Dopisanie stanów z automatu
        List<State> listOfStates = allStates();
        for (int i = 0; i < listOfStates.size(); i++) {
            retString.append("q" + i + " ");
        }

        //Dopisanie przejść z automatu
        retString.append("\n-Transitions:\n");
        for (int i = 0; i < listOfStates.size(); i++) {
            List<OutgoingTransition> listOfTrans = allOutgoingTransitions(listOfStates.get(i));
            for (int j = 0; j < listOfTrans.size(); j++) {
                retString.append("  q" + i + " -" + listOfTrans.get(j).getTransitionLabel()
                        + "-> q");
                State target = listOfTrans.get(j).getTargetState();
                for (int m = 0; m < listOfStates.size(); m++) {
                    if (target == listOfStates.get(m)) {
                        retString.append(m);
                        break;
                    }
                }
                retString.append("\n");
            }
        }

        //Dopisanie stanu początkowego automatu
        retString.append("-Initial state: ");
        for (int i = 0; i < listOfStates.size(); i++) {
            if (listOfStates.get(i) == getInitialState()) {
                retString.append("q" + i);
                break;
            }
        }

        //Dopisanie stanów końcowych automatu
        retString.append("\n-Final states: ");
        for (int i = 0; i < listOfStates.size(); i++) {
            if (isFinal(listOfStates.get(i))) {
                retString.append("q" + i + " ");
            }
        }

        //Zwrócenie wyniku
        return retString.toString();
    }

   /**
     * Sprawdza, czy automat jest deterministyczny (to znaczy, czy ma
     * przynajmniej jeden stan, czy nie zawiera epsilon-przejść (za wyjątkiem
     * sytuacji, gdy epsilon-przejście jest jedynym sposobem wyjścia ze stanu)
     * oraz czy przejścia z danego stanu do innych stanów odbywają się po
     * różnych znakach).
     */
    public boolean isDeterministic() {
        List<State> states = allStates();

        if (states.isEmpty())
            return false;

        for (State state : states) {
            List<OutgoingTransition> transitions = allOutgoingTransitions(state);

            if (transitions.size() <= 1)
                continue;

            for (int i = 0; i < transitions.size(); ++i) {
                TransitionLabel label = transitions.get(i).getTransitionLabel();

                if (label.canBeEpsilon())
                    return false;

                for (int j = i + 1; j < transitions.size(); ++j) {
                    TransitionLabel label2 = transitions.get(j).getTransitionLabel();
                    if (!label2.intersect(label).isEmpty())
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * Dodaje przejście od stanu state z powrotem do tego samego stanu
     * po etykiecie transitionLabel.
     */
    public void addLoop(State state, TransitionLabel transitionLabel) {

        addTransition(state, state, transitionLabel);
    }

    /**
     * Zwraca obiekt typu String, który zawiera gotowy kod w języku DOT służący do
     * przedstawienia automatu w formie graficznej, (w ubuntu pakiet
     * graphviz). Z konsoli wywołuje się przykładowo w następujący sposób: dot
     * -Tpng -O plik_zkodem.dot który tworzy plik-schemat zapisany w formacie
     * png. Więcej w: man dot.
     *
     * @return Kod źródłowy schematu w języku DOT.
     */
    public String getDotGraph() {

        class DotGraph {
            private StringBuffer dotCode;
            private List<State> states;

            public DotGraph() {
                dotCode = new StringBuffer();
                states = allStates();
            }

            private void getDotGraphIntro() {
                dotCode.append(
                        "digraph finite_state_machine {\n"
                         + "    rankdir=LR;\n"
                         + "    size=\"8,5\"\n"
                         + "    node [style=filled fillcolor=\"#00ff005f\" shape = ");
                if (isFinal(getInitialState())) dotCode.append("double");
                dotCode.append("circle];\n"
                               + "    \"State #" + states.indexOf(getInitialState()) + "\";\n"
                               + "    node [shape = doublecircle style=filled "
                               + "fillcolor=\"#00000000\"];\n    ");
            }

            private void getDotGraphFinalStates() {
                for (State it : states) {
                    if (isFinal(it)) {
                        dotCode.append("\"State #" + states.indexOf(it) + "\" ");
                    }
                }
            }

            private void getEdgeLabel(State state, int target, String label) {
                if (label.length() != 0) {
                    dotCode.append("    \"State #");
                    dotCode.append(states.indexOf(state) + "\"");
                    dotCode.append(" -> ");
                    dotCode.append("\"State #");
                    dotCode.append(target + "\"");
                    dotCode.append(" [ label = \"" + label
                            + "\" ]");
                    dotCode.append(";\n");
                }

            }

            private void getDotGraphEdges() {
                for (State it : states) {
                    final StringBuffer[] labelList = new StringBuffer[states.size()];
                    for (int i = 0; i < labelList.length; ++i) {
                        labelList[i] = new StringBuffer();
                    }

                    final List<OutgoingTransition> edges = allOutgoingTransitions(it);

                    for (OutgoingTransition edgeIt : edges) {
                        if (labelList[states.indexOf(edgeIt.getTargetState())].length() == 0) {
                            labelList[states.indexOf(edgeIt.getTargetState())]
                                    .append(edgeIt.getTransitionLabel());
                        } else {
                            labelList[states.indexOf(edgeIt.getTargetState())]
                                    .append(", " + edgeIt.getTransitionLabel());
                        }
                    }

                    for (int i = 0; i < labelList.length; ++i) {
                        getEdgeLabel(it, i, labelList[i].toString());
                    }
                }
            }

            public String  getDotGraph() {
                getDotGraphIntro();
                getDotGraphFinalStates();
                dotCode.append(";\n" + "    node [shape = circle];\n" + "");
                getDotGraphEdges();
                dotCode.append("\n}\n");
                return dotCode.toString();
            }
        }

        DotGraph tmp = new DotGraph();
        return tmp.getDotGraph();
    }

    public int countStates() {
        return allStates().size();
    }

    public int countTransitions() {
        int sum = 0;
        for (State state : allStates()) {
            sum += allOutgoingTransitions(state).size();
        }
        return sum;
    }

    /**
     * Wstawia począwszy od stanu state kopię automatu automaton.
     * Stan state będzie utożsamiony ze stanem
     * początkowym automatu automaton.
     */
    void insert(State state, AutomatonSpecification automaton) {
        List<State> loadedStates = automaton.allStates();
        HashMap<State, State> connectedStates = new HashMap<State, State>();
        State automatonInitialState = automaton.getInitialState();
        for (State currentState : loadedStates) {
            if (currentState == automatonInitialState)
                connectedStates.put(currentState, state);
            else
                connectedStates.put(currentState, this.addState());
        }
        for (State currentState : loadedStates) {
            if (automaton.isFinal(currentState))
                markAsFinal(connectedStates.get(currentState));
            List<OutgoingTransition> list = automaton
                    .allOutgoingTransitions(currentState);
            for (OutgoingTransition transition : list) {
                this.addTransition(connectedStates.get(currentState),
                        connectedStates.get(transition.getTargetState()),
                        transition.getTransitionLabel());
            }
        }
    }

    /**
     * Funkcja zmieniająca pusty automat na automat akceptujący wyłącznie napis
     * pusty.
     */
    public void makeEmptyStringAutomaton() {
        State emptyState = this.addState();
        this.markAsInitial(emptyState);
        this.markAsFinal(emptyState);
    }

    public boolean isFull(String alphabet) {
        int index;
        if (allStates().isEmpty())
            return false;
        for (State state : allStates()) {
            if (allOutgoingTransitions(state).isEmpty())
                    return false;
            for (int i = 0; i < alphabet.length(); i++) {
                index = 0;
                for (OutgoingTransition transition : allOutgoingTransitions(state)) {
                    if (transition.getTransitionLabel().canAcceptCharacter(alphabet.charAt(i)))
                        break;
                    else if ((index == allOutgoingTransitions(state).size() - 1)
                            && !transition.getTransitionLabel()
                            .canAcceptCharacter(alphabet.charAt(i)))
                        return false;
                    else index++;
                }
            }
        }
        return true;
    }

    public void makeFull(String alphabet) {
        State trash = addState();
        int indeks;
        for (State state : allStates()) {
            for (int i = 0; i < alphabet.length(); i++) {
                indeks = 0;
                if (allOutgoingTransitions(state).isEmpty())
                    addTransition(state, trash, new CharTransitionLabel(
                            alphabet.charAt(i)));
                for (OutgoingTransition transition1 : allOutgoingTransitions(state)) {
                    if (transition1.getTransitionLabel().canAcceptCharacter(
                            alphabet.charAt(i)))
                        break;
                    else if ((indeks == allOutgoingTransitions(state).size() - 1)
                            && !transition1.getTransitionLabel()
                                    .canAcceptCharacter(alphabet.charAt(i)))
                        addTransition(state, trash, new CharTransitionLabel(
                                alphabet.charAt(i)));
                    else
                        indeks++;
                }
            }
        }
    }

    public boolean prefixChecker(State state) {

        if (isFinal(state)) {
            return true;
        }

        List<State> checkedStates = new ArrayList<State>();
        List<OutgoingTransition> outgoing = new ArrayList<OutgoingTransition>();
        State currentState;

        checkedStates.add(state);
        int limit = checkedStates.size();

        for (int i = 0; i < limit; i++) {
            outgoing.clear();
            outgoing = allOutgoingTransitions(checkedStates.get(i));

            for (int j = 0; j < outgoing.size(); j++) {

                currentState = outgoing.get(j).getTargetState();

                if (isFinal(currentState)) {
                        return true;
                }

                if (!checkedStates.contains(currentState)) {
                    checkedStates.add(currentState);
                    limit++;
                }
            }
        }
        return false;
    }

    /**
     * Funkcja tworzaca zawartość automatu ze Stringa.
     */

    void fromString(String automatonDescription) throws StructureException {
        MakeAutomatonFromString graph = new MakeAutomatonFromString(this, automatonDescription);
        graph.make();
    }

    /**
     * Zwraca true, gdy automat akceptuje napis pusty.
     */
    public boolean acceptEmptyWord() {

        List<State> tocheck = new ArrayList<State>();
        List<OutgoingTransition> transitions = new ArrayList<OutgoingTransition>();
        TransitionLabel label;
        State state;

        if (isFinal(getInitialState())) {
            return true;
        }

        tocheck.add(getInitialState());
        int iterator = tocheck.size();

        for (int i = 0; i < iterator; ++i) {
            transitions.clear();
            transitions = allOutgoingTransitions(tocheck.get(i));

            for (int j = 0; j < transitions.size(); ++j) {
                label = transitions.get(j).getTransitionLabel();
                state = transitions.get(j).getTargetState();

                if (label.canBeEpsilon() && !tocheck.contains(state)) {
                    tocheck.add(state);
                    iterator++;

                    if (isFinal(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //true-istnieją stany zbędne
    public boolean uselessStates() {
        boolean flag1 = true;
        boolean flag2 = false;
        State q = getInitialState();
        List<State> stack = new ArrayList<State>();
        List<State> used;
        used = allStates();
        int x = 0;
        while (true) {
            if (flag1) {
                for (int i = 1; i <= allOutgoingTransitions(q).size(); i++) {
                    stack.add(allOutgoingTransitions(q).get(i).getTargetState());
                }
            }
            if (!stack.isEmpty()) {
                flag1 = true;
                q = stack.get(stack.size());
                for (int i = 1; i <= used.size(); i++) {
                    if (used.get(i) == q) {
                        flag2 = true;
                        x = i;
                        break;
                    }
                }
                if (flag2) {
                    used.remove(x);
                    flag2 = false;
                    continue;
                } else {
                    flag1 = false;
                }
            } else {
                break;
            }
        }
        for (int i = 1; i <= used.size(); i++) {
            if (used.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    public void makeAllStringsAutomaton(String alphabet) {
        State state = addState();
        markAsInitial(state);
        markAsFinal(state);
        for (int i = 0; i < alphabet.length(); i++)
            addLoop(state, new CharTransitionLabel(alphabet.charAt(i)));
    }

    public void makeAllNonEmptyStringsAutomaton(String alphabet) {
        State s0 = addState();
        State s1 = addState();
        markAsInitial(s0);
        markAsFinal(s1);
        for (int i = 0; i < alphabet.length(); i++) {
            addTransition(s0, s1, new CharTransitionLabel(alphabet.charAt(i)));
            addLoop(s1, new CharTransitionLabel(alphabet.charAt(i)));
        }
    }

    public boolean checkPrefix(String word) {

        List<State> finalStates = new ArrayList<State>();
        List<State> nowChecking = new ArrayList<State>();
        List<OutgoingTransition> outgoing = new ArrayList<OutgoingTransition>();
        TransitionLabel label;
        State state;

        finalStates.add(getInitialState());

        for (int i = 0; i < word.length(); i++) {
            nowChecking.clear();
            nowChecking.addAll(finalStates);
            finalStates.clear();
            int size = nowChecking.size();

            for (int j = 0; j < size; j++) {
                outgoing.clear();
                outgoing = allOutgoingTransitions(nowChecking.get(j));
                for (int k = 0; k < outgoing.size(); k++) {
                    label = outgoing.get(k).getTransitionLabel();
                    state = outgoing.get(k).getTargetState();

                    if (label.canBeEpsilon() && !nowChecking.contains(state)) {
                        nowChecking.add(state);
                        size++;
                    }
                    boolean term = label.canAcceptCharacter(word.charAt(i));
                    if (term && !finalStates.contains(state)) {
                        finalStates.add(state);
                    }
                }
            }
        }

        int size = finalStates.size();

        for (int j = 0; j < size; j++) {
            outgoing.clear();
            outgoing = allOutgoingTransitions(finalStates.get(j));
            for (int k = 0; k < outgoing.size(); k++) {
                label = outgoing.get(k).getTransitionLabel();
                state = outgoing.get(k).getTargetState();

                if (label.canBeEpsilon() && !finalStates.contains(state)) {
                    finalStates.add(state);
                    size++;
                }
            }
        }

        for (int i = 0; i < finalStates.size(); i++) {
            boolean istrue = prefixChecker(finalStates.get(i));

            if (istrue)
                return true;
        }
        return false;
    }

    @Override
    public AutomatonSpecification clone() {
        AutomatonSpecification mini = new NaiveAutomatonSpecification();
        State q5 = mini.addState();
        mini.insert(q5, this);
        return mini;
    }
    public void makeOneLoopAutomaton(char c) {
        State q0 = addState();
        addLoop(q0, new CharTransitionLabel(c));
        markAsInitial(q0);
        markAsFinal(q0);
    }

    /**
     * Metoda zwracającą wszystkie napisy akceptowane przez automat.
     */
    public AllAcceptedWords returnAllAcceptedWords() {
        AllAcceptedWords words = new AllAcceptedWords(this);
        return words;
    }

    /**
     * Sprawdza, czy akceptowany język jest nieskończony.
     */
    public boolean isInfinite() {
        return findFinals(getInitialState(), new ArrayList<State>());
    }

    private boolean findFinals(State state, List<State> history) {
        boolean result = false;

        if (isFinal(state))
            return checkForLoop(state, new ArrayList<State>());

        if (allOutgoingTransitions(state).size() == 0)
            return false;

        for (State his : history)
            if (his == state)
                return false;
        history.add(state);

        for (OutgoingTransition child : allOutgoingTransitions(state)) {
            result = result || findFinals(child.getTargetState(), history);
            if (result)
                break;
            }
        return result;
    }

    private boolean checkForLoop(State state, List<State> history) {
        for (State his : history)
            if (his == state)
                return isFinal(state);

        if (allOutgoingTransitions(state).size() == 0)
            return false;
        history.add(state);
        boolean result = false;
        for (OutgoingTransition child : allOutgoingTransitions(state)) {
            result = result || checkForLoop(child.getTargetState(), history);
            if (result)
                break;
        }
        return result;
    }

    /**
     * Metoda zwracająca pierwszy według kolejności alfabetycznej napis,
     * akceptowany przez automat.
     */
    public String firstAcceptedWord() {
        AllAcceptedWords words = new AllAcceptedWords(this);
        String tmp;
        if (words.hasNext()) {
            String min = words.next();
            while (words.hasNext()) {
                tmp = words.next();
                if (tmp.compareTo(min) > 0)
                    min = tmp;
            }
            return min;
        } else
            throw new RuntimeException("error");
    }
};

class StructureException extends Exception {
}

