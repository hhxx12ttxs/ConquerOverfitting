package mw.server.phases;

import java.io.Serializable;

import mw.mtgforge.CommandList;
import mw.server.pattern.Command;

/**
 * Handles "until end of turn" and "at end of turn" commands from cards
 * "until end of turn" and "at end of turn" are removed when executed, "at end of each turn" - not.
 */
public class EndOfTurn implements Serializable {

	private CommandList at = new CommandList();
    private CommandList until = new CommandList();
    private CommandList atEachTurn = new CommandList();

    public void addAt(Command c) {
        at.add(c);
    }

    public void addAtEachTurn(Command c) {
        atEachTurn.add(c);
    }
    
    public void removeAtEachTurn(Command c) {
        int length = atEachTurn.size();
        
        for (int i = 0; i < length; i++) {
        	if (atEachTurn.get(i).equals(c)) {
        		atEachTurn.remove(i);
        		break;
        	}
        }
    }

    public void addUntil(Command c) {
        until.add(c);
    }

    public void executeAt() {        
        execute(at);
    }

    public void executeUntil() {
        execute(until);
    }
    
    public void executeAtEachTurn() {
        executeWithOutRemove(atEachTurn);
    }

    public int sizeAt() {
        return at.size();
    }

    public int sizeUntil() {
        return until.size();
    }
    
    public int sizeAtEachTurn() {
        return atEachTurn.size();
    }

    private void execute(CommandList c) {
        int length = c.size();

        for (int i = 0; i < length; i++) {
            c.remove(0).execute();
        }
    }
    
    private void executeWithOutRemove(CommandList c) {
        int length = c.size();

        for (int i = 0; i < length; i++) {
            c.get(i).execute();
        }
    }
    
    private static final long serialVersionUID = 1L;
}

