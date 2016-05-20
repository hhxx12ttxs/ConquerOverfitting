package afl1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import afl1.helper.Logging;

public class Automaton {

	protected InitialState _initialState;

	protected List<FinalState> _finalStateList;

	protected List<Transition> _transitionList;

	public Automaton() {

		_initialState = new InitialState(0);
		_transitionList = new ArrayList<Transition>();
		_finalStateList = new ArrayList<FinalState>();
		_finalStateList.add(new FinalState(99));
	}

	public Automaton(State q_o, State q_f) {
		_initialState = new InitialState(q_o);
		_transitionList = new ArrayList<Transition>();
		_finalStateList = new ArrayList<FinalState>();
		_finalStateList.add(new FinalState(q_f));
	}

	public Automaton(State q_o, List<FinalState> finallist) {
		_initialState = new InitialState(q_o);
		_transitionList = new ArrayList<Transition>();
		_finalStateList = finallist;
	}

	@Override
	public String toString() {
		String str = "digraph G {" + System.lineSeparator();

		str = str.concat(_initialState.toString() + System.lineSeparator());
		for (FinalState fs : _finalStateList) {
			str = str.concat(fs.toString() + System.lineSeparator());
		}

		for (Transition t : getTransitions()) {
			str = str.concat(t.toString() + System.lineSeparator());
		}

		str = str.concat("}" + System.lineSeparator());
		return str;
	}

	public String toAutomatonString() {
		String str = "Automaton a = new Automaton(new InitialState(0), { ";
		String finStr = "";				
		for (FinalState fs : _finalStateList){
			if (!finStr.isEmpty())
				finStr += ",";
			finStr += "new FinalState(" + fs.getStateID() + ")";
			
		}
				
		str += finStr + "});"+ System.lineSeparator();

		for (Transition t : getTransitions()) {
			str += t.toAutomatonString() + System.lineSeparator();
		}

		return str;
	}

	public State newTransition(State from, String string, State to) {
		_transitionList.add(new Transition(from, string, to));
		// Logging.InfoStatus(from.getStateID() + "-" + to.getStateID() + ": "
		//		+ string);
		return to;
	}
	
	public void newTransition(int from, String string, int to) {
		_transitionList.add(new Transition(from, string, to));		
	}

	public State newTransition(State from, String string,
			List<FinalState> finalList) {
		if (finalList.size() == 1) {
			_transitionList.add(new Transition(from, string, finalList.get(0)));
			// Logging.InfoStatus(from.getStateID() + "-"
			// 		+ finalList.get(0).getStateID() + ": " + string);
			return finalList.get(0);
		} else if (finalList.size() == 0) {
			throw new NotImplementedException();
		} else {
			for (FinalState fs : finalList) {
				_transitionList.add(new Transition(from, string, fs));
			}

			throw new NotImplementedException();
		}
	}

	public void newEpsTransition(State q_o, State q_ox) {
		newTransition(q_o, Transition.EPSI, q_ox);
	}
	
	public void newEpsTransition(State q_o, List<FinalState> finalList) {
		newTransition(q_o, Transition.EPSI, finalList);
	}

	public State getInitialState() {
		return _initialState;
	}

	public List<FinalState> getFinalStatelist() {
		return _finalStateList;
	}
	
	public void addFinalState(int targetStateID) {
		if (!isFinalState(targetStateID))
			_finalStateList.add(new FinalState(targetStateID));
	}

	public List<Transition> getTransitions() {
		Collections.sort(_transitionList);
		return _transitionList;
	}

	private void addTransition(Transition t) {
		if (t != null && !this.contains(t))
			_transitionList.add(t);

	}

	public void addTransitionsFromFA(Automaton a) {
		for (Transition t : a.getTransitions()) {
			this.addTransition(t);
		}
	}

	public List<Transition> getTransitionsFromStateID(int id) {
		List<Transition> found = new ArrayList<Transition>();
		for (Transition t : _transitionList) {
			
			if (t.getSourceStateID() == id){
				found.add(t);
			}
		}
		return found;
	}
	
	private List<Transition> getUniqueTransitionsFromStateID(int id) {
		List<Transition> found = new ArrayList<Transition>();
		for (Transition t : _transitionList) {
			// if source and target are equal it's a star representation and shouldn't be added
			if (t.getSourceStateID() == id && t.getTargetStateID() != id) 
				found.add(t);			
		}
		return found;
	}
	
	public List<Transition> getTransitionsFromStateIDWithAlpha(int id, String alpha) {
		List<Transition> found = new ArrayList<Transition>();
		for (Transition t : _transitionList) {
			// if source and target are equal it's a star representation and shouldn't be added
			if (t.getSourceStateID() == id)
				if(t.getLabel().equals(alpha) ||t.getLabel().equals(Transition.ANY))
				found.add(t);					
		}
		return found;
	}
	
	private static boolean listContainsTransition(List<Transition> list, Transition element) {
		for (Transition t : list) {
			if (t.getSourceStateID() == element.getSourceStateID()
					&& t.getTargetStateID() == element.getTargetStateID()
					&& t.getLabel() == element.getLabel()) {
				return true;
			}
		}
		return false;
	}
	
	public List<Transition> getTransitionsToStateID(int id) {
		List<Transition> found = new ArrayList<Transition>();
		for (Transition t : _transitionList) {
			if (t.getTargetStateID() == id)
				found.add(t);
		}
		return found;
	}

	public boolean isFinalState(int targetStateID) {
		for(FinalState fs : _finalStateList)
			if (fs.getStateID() == targetStateID)
				return true;
		return false;
	}

	public boolean contains(Transition t) {
		List<Transition> tList = getTransitionsFromStateID(t.getSourceStateID());
		List<Transition> anyList = new ArrayList<Transition>();
		for (Transition ts : tList){
			if (t.getTargetStateID()== ts.getTargetStateID() 
					&& t.getLabel().equals(ts.getLabel())){
				anyList.add(t);
			}
		}
		//Logging.Debug("dfa contains "+  anyList.size() +" times [" + t.getSourceStateID() + " -" +t.getLabel() + "- " +t.getTargetStateID() + "]");
		
		return anyList.size() > 0;
		
		// return _transitionList.contains(t);
	}
	
	

	public List<State> getStateListWithoutFinal() {
		List<State> notFinalStateList = new ArrayList<State>();
		
		for (Transition t : _transitionList){
			
			if (!isFinalState(t.getSourceStateID()) )
				addUnique(notFinalStateList, new State(t.getSourceStateID()));
			if (!isFinalState(t.getTargetStateID()))
				addUnique(notFinalStateList, new State(t.getTargetStateID()));	
		}
		return notFinalStateList;
	}

	public List<State> getStateList() {
		List<State> stateList = new ArrayList<State>();
		boolean doNotAddSource = false;
		boolean doNotAddTarget = false;
		for (Transition t : _transitionList){
			doNotAddSource = false;
			doNotAddTarget = false;
			
			for (State s : stateList){
				if (s.getStateID() == t.getSourceStateID())
					doNotAddSource = true;
				if (s.getStateID() == t.getTargetStateID())
					doNotAddTarget = true;
			}
			if (doNotAddSource == false)
				stateList.add(new State(t.getSourceStateID()));
			if (doNotAddTarget == false)
				stateList.add(new State(t.getTargetStateID()));
		}
		return stateList;
	}

	public List<String> getUsedAlphabet() {
		List<String> alphabet = new ArrayList<String>();
		boolean found = false;
		for (Transition t : _transitionList){
/*			for (String s : alphabet){
				if (t.getLabel().equals(s))
					found = true;
			}*/
			if (!alphabet.contains(t.getLabel()))
				alphabet.add(t.getLabel());
				
		}
		
		return alphabet;
	}

	public void cleanupTransitions() {
		List<Transition> allCrapFound = new ArrayList<Transition>();
		for (Transition t : getTransitionsFromStateID(getInitialState().getStateID())){
			List<Transition> tempCrap = new ArrayList<Transition>();
			boolean isCrap = isCrapWithCrapList(t, tempCrap);
			allCrapFound.addAll(tempCrap);
		}
		Logging.Debug("cleanup->to remove: " + Logging.listToString(allCrapFound));
		
		for (Transition t : allCrapFound){
			if (listContainsTransition(_transitionList, t))
				_transitionList.remove(t);
		}
			
	}
	
	
	public boolean isCrapWithCrapList(Transition currentT, List<Transition> crapList) {
		crapList = new ArrayList<Transition>();
		
		// the target state is final
		if (isFinalState(currentT.getTargetStateID()))
				return false; // return empty list
		
		// target state not final and no succeeding transitions
		List<Transition> succeeding = getUniqueTransitionsFromStateID(currentT.getTargetStateID());
		 if (currentT.getTargetStateID() == 5)
			 Logging.Debug("5 is not final State and has " + succeeding.size()+ "succeeders");
		if (succeeding.size() <= 0 ){
			crapList.add(currentT); // transition itself is crap
			return true;
		}
		// target state not final 
		for (Transition t : succeeding){
			
			List<Transition> tempCrap = new ArrayList<Transition>();
			boolean isCrap = isCrapWithCrapList(t, tempCrap);
			if (currentT.getTargetStateID() == 5)
				Logging.Debug("5 is " + (isCrap ? " " : " not ") +
						" and has " +tempCrap.size() + " crappy succeeders from " + t.getTargetStateID());
			crapList.addAll(tempCrap);
			if (!isCrap){			
				return false;
			}
		}
		crapList.add(currentT); 
		return true; // and none of the succeeding has a final state return all the found crap
	}

	public void setInitialState(int stateID) {
		_initialState = new InitialState(stateID);		
	}

	

	public List<State> getTargetStates(List<Transition> transitions) {
		List<State> targets = new ArrayList<State>();
		for (Transition t : transitions) {
			targets.add(new State(t.getTargetStateID()));
		}
		return targets;
	}

	
	public List<Transition> getTransitionsFromStatesWithAlpha(
			HashSet<State> states, String alpha) {
		List<Transition> found = new ArrayList<Transition>();
		for (Transition t : _transitionList) {
			Iterator itr = states.iterator();
			while(itr.hasNext()){
			// if source and target are equal it's a star representation and shouldn't be added
			if (t.getSourceStateID() == ((State) itr.next()).getStateID())
				if(t.getLabel().equals(alpha) ||t.getLabel().equals(Transition.ANY))
				found.add(t);		
			}
		}
		return found;
	}

	public boolean isBullshit() {
		
		return isFinalState(_initialState.getStateID());
		
	}
	
	private static void addUnique(List<State> list, State state) {
		boolean found = false;
		for (State sl : list) {
			if (sl.getStateID() == state.getStateID())
				found = true;
		}
		if (found == false)
			list.add(state);
	}
}

