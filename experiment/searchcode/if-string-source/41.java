/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.demo.fsm;

import java.util.HashMap;
import javax.slee.SbbLocalObject;
import org.apache.log4j.Logger;


/**
 * Represents call finite state machine.
 * 
 * @author kulikov
 */
public class FSM {

    private SbbLocalObject root;
    private State state;

    private HashMap<String, State> states = new HashMap();
    private Logger logger;
    
    private HashMap attributes = new HashMap();
    
    public FSM(String name) {
        logger = Logger.getLogger(name);
    }

    public State createState(String name) {
        State s = new State(this, name);
        states.put(name, s);
        return s;
    }

    /**
     * Creates transition between two states.
     * 
     * @param name the name of transition.
     * @param  source the name of the initial state
     * @param destination the name of the final state
     * @return transition descriptor object.
     */ 
    public Transition createTransition(String name, String source, String destination) {
        State start = states.get(source);
        State finish = states.get(destination);
        
        return start.createTransition(name, finish);
    }
    
    public void setStart(State s) {
        this.state = s;
    }
    
    public void start() {
    }
    
    public synchronized void signal(Signal s) {
        State next = this.state.process(s);

        logger.info("State=" + state.getName() + ", Signal=" + s.getName() + ", transitioned to state=" + next.getName());
        this.state = next;
    }
    
    public State getState() {
        return state;
    }

    public void setAttribute(String name, Object attribute) {
        attributes.put(name, attribute);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}

