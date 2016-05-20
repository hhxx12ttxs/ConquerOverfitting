package afl1.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import afl1.helper.Logging;
import afl1.model.Automaton;
import afl1.model.FinalState;
import afl1.model.State;
import afl1.model.Transition;

public class NFAetoNFA {
	/**
	 * This algorithm transforms the input NFA that still contains epsilon-transitions to an NFA without epsilon-transitions.
	 * It was built according to the algorithm in the script of "Automata and Formal Languages"
	 */

	private Automaton _nfa;
	private Automaton _nfae;

	public NFAetoNFA(Automaton nfae){
		List<FinalState> finallist = new ArrayList<FinalState>(); 
		if (nfae.getFinalStatelist().contains(nfae.getInitialState()))
			finallist.add(new FinalState(nfae.getInitialState()));
		_nfa = new Automaton(nfae.getInitialState(), finallist);
		_nfae = nfae;
		process();
	}

	public Automaton getAutomaton() {
		return _nfa;
	}
	
	private void process() {

		List<Transition> w = new ArrayList<Transition>();
		w.addAll(_nfae.getTransitionsFromStateID(_nfae.getInitialState().getStateID()));
		
		Transition currentT = w.get(0);
		
		int q_id = _nfae.getInitialState().getStateID();
		int f_id = currentT.getTargetStateID(); 
		List<Transition> epsiList = new ArrayList<Transition>();
		
		Logging.Debug("worklist: " + Logging.listToString(w));
		// TODO q_id in OR situations? use foreach and recursion
		while (!w.isEmpty()) {
			currentT = w.get(0);
			w.remove(0);
			
			if (currentT.hasEpsilon()){
				epsiList.add(currentT);
				if (_nfae.isFinalState(currentT.getTargetStateID())){
					//Logging.Debug(currentT.getTargetStateID() + " is final in enfa");
					_nfa.addFinalState(currentT.getSourceStateID());
					//Logging.Debug("add " + currentT.getSourceStateID() +  " from eps trans to nfa fslist");
				}
				else{
					//Logging.Debug(currentT.getTargetStateID() + " is not final in enfa");
				}
				for(Transition ts : _nfae.getTransitionsFromStateID(currentT.getTargetStateID())){
					if (!_nfa.contains(ts) || !epsiList.contains(ts))
						w.add(new Transition(currentT.getSourceStateID(), ts.getLabel(), ts.getTargetStateID()));
					
				}
				
			}
			else{ // no epsilon
				
				f_id = currentT.getTargetStateID();
				_nfa.newTransition(new State(currentT.getSourceStateID()), currentT.getLabel(), new State(currentT.getTargetStateID()));
				
				/**** update FinalStates ****/
				if (_nfae.isFinalState(f_id)){
					_nfa.addFinalState(f_id);
					//Logging.Debug(f_id + " is final in enfa");
					//Logging.Debug("add " + f_id +  " from " +currentT.getLabel() + " trans to nfa fslist");
				}
				else{
					//Logging.Debug(f_id + " is not final in enfa");
				}
				/**** get followers from the labeled transition ****/
				for(Transition ts : _nfae.getTransitionsFromStateID(f_id)){

					/**** if a follower is an epsilon, concat transitions and add to worklist****/ 
					if (ts.hasEpsilon() && !_nfa.contains(ts)){
						//List<Transition> tList = _nfa.getTransitionsToStateID(ts.getSourceStateID());						
						
						w.add(new Transition(currentT.getSourceStateID(), currentT.getLabel(), ts.getTargetStateID()));
					}
					else if (!ts.hasEpsilon() && !_nfa.contains(ts)) // TODO: check if it works
						w.add(ts);
					/****  ****/
				}
				
				// how to connect transitions in the nfa? 
			}
			Logging.Debug("nfa: " +  Logging.listToString(_nfa.getTransitions()));
			Logging.Debug("worklist: " + Logging.listToString(w));
		}	
		
		// cleanup the nfa from unfinished paths
		//_nfa.cleanupTransitions();
	}
	
	
}

