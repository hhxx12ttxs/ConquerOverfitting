/* Copyright (c) 2009 Ricardo Menotti, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its 
 * documentation for NON-COMERCIAL purposes and without fee is hereby granted 
 * provided that this copyright notice appears in all copies.
 *
 * RICARDO MENOTTI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR 
 * NON-INFRINGEMENT. RICARDO MENOTTI SHALL NOT BE LIABLE FOR ANY DAMAGES 
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS 
 * SOFTWARE OR ITS DERIVATIVES. 
 */

package lalp.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.impl.SparseGraph;

/**
 * A hardware design which consists of an <code>Component</code> set and an <code>Signal</code> set.
 * 
 * @author <a href="http://menotti.pro.br/">Ricardo Menotti</a>


 * @author <a href="http://www.dc.ufscar.br/">DC/UFSCar</a>
 * @version January, 2009
 * 
 * @see Component
 * @see Signal
 */
public class Design extends SparseGraph {
	protected int addressWidth = Parameters.getDefaultAddressWidth();
	protected int dataWidth = Parameters.getDefaultDataWidth();
	protected Map<String,Component> globalComponents = new TreeMap<String,Component>();
	protected Map<String,Component> components = new TreeMap<String,Component>();
	protected int iterations;
	protected String name;
	protected int stages;
	protected int maxSchedulingTime = 0;
	protected Component initialComponent;
	protected boolean sync = false;
	
	public boolean isSync() {
		return sync;
	}
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	public Design() {
		super();
	}
	public Design(int dataWidth) {
		this();
		this.dataWidth = dataWidth;
	}
	public Design(String name) {
		this();
		this.name = name;
	}
	
	public Design(String name, int dataWidth) {
		this();
		this.dataWidth = dataWidth;
		this.name = name;
	}
	
	public Component addComponent(Component comp) {
		Component c;
		c = (Component) super.addVertex(comp);
		components.put(comp.getName(), comp);
		if(!isSync() && comp.getSync())
			setSync(true);
    	return c;
    }
	
	
	public Component getComponent(String name) {
		return components.get(name);
	}
	
	public void removeComponent(Component comp) throws Exception {
		if (comp.degree() != 0) {
			throw new Exception("This is a connected component and can't be removed");
		}
		super.removeVertex(comp);
	}
	/**
	 * Include the Component in the globalComponents Map. 
	 * This components are not included into graph representation and are used to links like clk signals 
	 * @param comp
	 * @return A reference to the added component
	 */
	public Component addGlobalComponent(Component comp) {
		return globalComponents.put(comp.getName(), comp);
	}
	public Signal addSignal(Signal signal) {
    	// TODO: check something else
    	return (Signal) super.addEdge(signal);
    }
	public void removeSignal(Signal signal) {
		// TODO: check port references DONE!
		if (signal.getSourcePort().getSignal() == signal) {
			if (signal.getSignal() == null) {
				signal.getSourcePort().clearSignal();			
			}
			else {
				signal.getSourcePort().signal = signal.getSignal();
			}
		}
		else {
			Signal pred = signal.getSourcePort().getSignal();
			while (pred.getSignal() != signal)
				pred = pred.getSignal();
			if (signal.getSignal() == null) {
				pred.alias = null;
			}
			else {
				pred.alias = signal.alias;
			}
		}
		signal.getDestPort().clearSignal();
		super.removeEdge(signal);
	}
	public int getAddressWidth() {
		return addressWidth;
	}
	@SuppressWarnings("unchecked")
	public Set<Component> getComponents() {
        return (Set<Component>)super.getVertices();
    }
	public int getDataWidth() {
		return dataWidth;
	}
	public Component getGlobalComponent(String name) {
    	return globalComponents.get(name);
    }
	public Map<String,Component> getGlobalComponents() {
		return globalComponents;
	}
	public int getIterations() {
		return iterations;
	}
	public String getName() {
		return name;
	}
	@SuppressWarnings("unchecked")
	public Set<Signal> getSignals() {
        return (Set<Signal>) super.getEdges();
    }
	public int getStages() {
		return stages;
	}
	/**
	 * Attribute an automatic name to unnamed components
	 * @deprecated
	 */
	public void organizeComponents() {
		int count = 0;
		for (Component c : getComponents()) {
			if (c.name == null) {
				c.setName("c" + ++count);
			}
		}
	}
	/**
	 * Attribute an automatic name to unnamed signals
	 * @deprecated
	 */
	public void organizeSignals() {
		int count = 0;
		for (Signal c : getSignals()) {
			if (c.getName() == null) {
				c.setName("s" + ++count);
			}
		}
	}
	public void setAddressWidth(int addressWidth) {
		this.addressWidth = addressWidth;
	}
	public void setDataWidth(int dataWidth) {
		this.dataWidth = dataWidth;
	}
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setStages(int stages) {
		this.stages = stages;
	}
	/**
	 * Generate debug outputs for each component in all output ports
	 * @throws Exception
	 */
	public void generateDebugOutputs() throws Exception {
		System.out.print("\nGenerating Debug Output Pins...");
		Set<Component> compsxpto = new HashSet<Component>(); 
		for (Component c : getComponents()) {
			if (c.isIComponent()) {
				compsxpto.add(c);
			}
		}
		for (Component c : compsxpto) {
				for (Port p : ((IComponent)c).getPortsByName().values()) {
					if (p.getType() == PortType.OUTPUT)
						c.debugOutput(p.getName());
			}
		}
		System.out.println("Ok!");
	}
	public void setInit(Component init) {
		this.initialComponent = init;
	}
	public Component getInit() {
		if (initialComponent != null)
			return initialComponent;
		else
			return this.getComponents().iterator().next();
	}
	public void resetComponentDistances() {
		for (Component c : getComponents())
			c.resetDistance();
	}
//	public void resetPortVisiting() {
//		for (Component c : getComponents())
//			for (Port p : c.getPorts())
//				p.setVisited(false);
//	}
	public void resetComponentVisiting() {
		for (Component c : getComponents())
			c.setVisited(false);
	}
	public void resetSchedulingTimes() {
		for (Component c : getComponents())
			c.resetSchedulingTimes();		
	}
	public void copySchedulingTimes() {
		for (Component c : getComponents())
			c.copySchedulingTimes();		
	}
	public int getMaxSchedulingTime() {
		return maxSchedulingTime;
	}
	public void setMaxSchedulingTime(int maxSchedulingTime) {
		this.maxSchedulingTime = maxSchedulingTime;
	}
}


