// Model.java

package gov.nasa.jpf.hmi.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.jvm.AnnotationInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.NativeStateMachine;
import gov.nasa.jpf.jvm.choice.sc.SCEvent;
import gov.nasa.jpf.jvm.serialize.Abstraction;

/**
 * Class representing a system or mental model of a machine
 *
 * @author Sebastien Combefis (UCLouvain)
 * @version August 7, 2011
 */
public abstract class Model extends NativeStateMachine
{
	// Class variables
	private static final List<String> fieldsType = Arrays.asList ("boolean", "byte", "short", "int", "long", "float", "double", "java.lang.String");
	private static final Action tau = new Action ("tau", ActionType.TAU);
	
	// Instance variables
	private final Map<String,State> states;
	private final Map<String,Action> actions;
	private final Set<String> visibleVariables;
	private final Map<String,List<FieldInfo>> fieldsMap;
	private String fromState, from, to, fromMode, toMode;
	private Map<String,String> fromMap, toMap;
	private boolean triggered;
	
	/**
	 * Constructor
	 * 
	 * @pre -
	 * @post An instance of this is created
	 */
	public Model()
	{
		states = new HashMap<String,State>();
		actions = new HashMap<String,Action>();
		visibleVariables = getVisibleVariables();
		triggered = false;
		for (Action a : getActions())
		{
			actions.put (a.toString(), a);
		}
		fieldsMap = new HashMap<String,List<FieldInfo>>();
		fromMap = new HashMap<String,String>();
		toMap = new HashMap<String,String>();
		
		Models.lts.put (getClassName(), new LTS<State,Transition>());
	}
	
	/**
	 * Get the behaviour class name
	 * 
	 * @pre -
	 * @post The returned value contains the name of the class
	 *       containing the behaviour of this model
	 */
	public final String getClassName()
	{
		return getClass().getName();
	}
	
	/**
	 * Get the actions of the model
	 * 
	 * @pre -
	 * @post The returned value contains the list
	 *       of actions of this model
	 */
	public abstract List<Action> getActions();
	
	/**
	 * Get the visible variables of the model
	 * 
	 * @pre -
	 * @post The returned value contains the set
	 *       of visible variable of this model
	 */
	public Set<String> getVisibleVariables()
	{
		return new HashSet<String>();
	}
	
	/**
	 * Get the states map of the model
	 * 
	 * @pre -
	 * @post The returned value contains a map giving for each
	 *       super-state the corresponding initial state
	 */
	public Map<String,String> getStatesMap()
	{
		return new HashMap<String,String>();
	}
	
	@Override
	protected final void logEventProcessed (SCEvent e)
	{
		super.logEventProcessed (e);
		
		String action = e.toString();

		// If no signal fired
		if (! triggered || from == null || to == null)
		{
			return;
		}

		Map<String,String> statesMap = getStatesMap();
		LTS<State,Transition> lts = Models.lts.get (getClassName());
		
//		System.out.printf ("\n\t%s --%s--> %s\n\n", from, action, to);
		
		if (statesMap.containsKey (to))
		{
			to = statesMap.get (to);
		}
		if (! states.containsKey (from))
		{
			states.put (from, new State (from));
			lts.addState (states.get (from));
			if (fromMode != null)
			{
				String modeName = "_mode(" + fromMode + ")";
				if (actions.get (modeName) == null)
				{
					actions.put (modeName, new Action (modeName, ActionType.COMMAND));
				}
				lts.addTransition (new Transition (actions.get (modeName)), states.get (from), states.get (from));
			}
			System.out.println ("states " + states.size());
		}
		if (! states.containsKey (to))
		{
			states.put (to, new State (to));
			lts.addState (states.get (to));
			if (toMode != null)
			{
				String modeName = "_mode(" + toMode + ")";
				if (actions.get (modeName) == null)
				{
					actions.put (modeName, new Action (modeName, ActionType.COMMAND));
				}
				lts.addTransition (new Transition (actions.get (modeName)), states.get (to), states.get (to));
			}
			System.out.println ("states " + states.size());
		}
		
		Transition t = new Transition (actions.get (action) != null ? actions.get (action) : tau);
		if (actions.containsKey (action))
		{
			lts.addTransition (t, states.get (from), states.get (to));
		}
		else
		{
			// Checking if from and to differs by some visible variables change
			if (! visibleVariables.isEmpty())
			{
				String actionName = "";
//				for (String variable : visibleVariables)
//				{
//					if (! fromMap.get (variable).equals (toMap.get (variable)))
//					{
//						actionName += String.format ("%s:%s->%s,", variable, fromMap.get (variable), toMap.get (variable));
//					}
//				}
				if ("".equals (actionName))
				{
					lts.addTauTransition (t, states.get (from), states.get (to));
				}
				else
				{
					actionName = actionName.substring (0, actionName.length() - 1);
					if (actions.get (actionName) == null)
					{
						actions.put (actionName, new Action (actionName, ActionType.OBSERVATION));
					}
					lts.addTransition (new Transition (actions.get (actionName)), states.get (from), states.get (to));
				}
			}
			else
			{
				lts.addTauTransition (t, states.get (from), states.get (to));
			}
		}
		
		triggered = false;
	}
	
	@Override
	protected final void logTrigger (MJIEnv env, int stateRef, MethodInfo mi)
	{
		super.logTrigger (env, stateRef, mi);
		
		fromState = genStateName (env, stateRef, fromMap);
		AnnotationInfo ai = env.getClassInfo (stateRef).getAnnotation ("gov.nasa.jpf.hmi.models.Mode");
		fromMode = ai == null ? null : ai.getValueAsString ("value");
		triggered = false;
	}
	
	@Override
	public void triggerFired (MJIEnv env, int objRef, int srcStateRef, int tgtStateRef)
	{
		from = env.getStringObject (env.getReferenceField (srcStateRef, "fieldName")) + fromState;
		to = env.getStringObject (env.getReferenceField (tgtStateRef, "fieldName")) + genStateName (env, tgtStateRef, toMap);
		AnnotationInfo ai = env.getClassInfo (tgtStateRef).getAnnotation ("gov.nasa.jpf.hmi.models.Mode");
		toMode = ai == null ? null : ai.getValueAsString ("value");
		triggered = true;
	}
	
	private final String genStateName (MJIEnv env, int objRef, Map<String,String> values)
	{
		StringBuilder name = new StringBuilder();
		values.clear();
		
		// Create fields map if not yet done
		if (! fieldsMap.containsKey (env.getClassInfo (objRef).getName()))
		{
			List<FieldInfo> fields = new LinkedList<FieldInfo>();
			FieldInfo[] info = env.getClassInfo (objRef).getDeclaredInstanceFields();
			for (FieldInfo fi : info)
			{
				if (fi.getAnnotation ("gov.nasa.jpf.annotation.FilterField") == null && (fieldsType.contains (fi.getType()) || fi.getTypeClassInfo().isEnum()))
				{
					fields.add (fi);
				}
			}
			fieldsMap.put (env.getClassInfo (objRef).getName(), fields);
		}
		
		// If there are fields, must get their values to get the state
		List<FieldInfo> fields = fieldsMap.get (env.getClassInfo (objRef).getName());
		if (fields.size() > 0)
		{
			name.append ("[");
			for (FieldInfo fi : fields)
			{
				name.append (fi.getName()).append ("=");
				Abstraction a = fi.getAttr (Abstraction.class);
				String type = fi.getType();
				String value = "";
				if (type.equals ("boolean")) { value += env.getBooleanField (objRef, fi.getName()); }
				else if (type.equals ("byte")) { value += env.getByteField (objRef, fi.getName()); }
				else if (type.equals ("short")) { value += env.getShortField (objRef, fi.getName()); }
				else if (type.equals ("int"))
				{
					if (a != null)
					{
						int val = env.getIntField (objRef, fi.getName());
						try
						{
							value += a.getClass().getMethod ("getName", int.class).invoke (a, val);
						}
						catch (Exception e)
						{
							value += env.getIntField (objRef, fi.getName());
						}
					}
					else
					{
						value += env.getIntField (objRef, fi.getName());
					}
				}
				else if (type.equals ("long")) { value += env.getLongField (objRef, fi.getName()); }
				else if (type.equals ("float")) { value += String.format ("%.9f", env.getFloatField (objRef, fi.getName())); }
				else if (type.equals ("double")) { value += String.format ("%.12f", env.getDoubleField (objRef, fi.getName())); }
				else if (type.equals ("java.lang.String")) { value += env.getStringField (objRef, fi.getName()); }
				else if (fi.getTypeClassInfo().isEnum())
				{
					int refId = env.getReferenceField (objRef, fi.getName());
					value += (refId == -1 ? "" : env.getElementInfo (refId).getStringField ("name"));
				}
				name.append (value).append (",\\n");
				values.put (fi.getName(), value);
			}
			name.delete (name.length() - 3, name.length()).append ("]");
		}
		
		return name.toString();
	}
}
