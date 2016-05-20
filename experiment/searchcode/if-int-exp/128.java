// Written by self, unfinished non-GT-specific interpreter now

/*
 * with help from:
 * http://stackoverflow.com/questions/15610183/if-else-statements-in-antlr-using-listeners
 * http://stackoverflow.com/questions/182636/how-to-determine-the-class-of-a-generic-type
 * not anymore - http://stackoverflow.com/questions/1901164/get-type-of-a-generic-parameter-in-java-with-reflection
 */

package eel;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;
import java.lang.reflect.*;
import structures.*;

public class ActiveVisitor extends EelBaseVisitor<Object> {
	private HashMap<String, Object> env = new HashMap<String, Object>();
	private int depth = 0;
	private final int MAXRECURSIONDEPTH = 10000;
	boolean returned = false;
	Object ret;

	@Override
	public Object visit(ParseTree t){
//		System.out.println(t.getText());
		if(!returned)
			return super.visit(t);
		return null;
	}
	
	@Override
	public Object visitMapAssignment(EelParser.MapAssignmentContext ctx){
		String name = ctx.ID().getText();
		HMap m;
		Object k = visit(ctx.exp(0));
		Object v = visit(ctx.exp(1));
		if(env.containsKey(name)){
			Object map = env.get(name);
			if(!(map instanceof HMap))
				throw new RuntimeException("\"" + name + "\" is not defined as a map in \n" + ctx.getText());
			m = (HMap)map;
			if(m.isEmpty())
				env.put(name, (m = mapOfType(k.getClass(), v.getClass())));
			else if(m.kClass() != k.getClass())
				throw new RuntimeException("The type of the parameter passed to \"" + name + "\" is not equal to the type of that map's domain in \n" + ctx.getText());
			else if(m.vClass() != v.getClass())	
				throw new RuntimeException("The type of the value given to " + name + "[" + ctx.exp(0).getText() + "] is not equal to the type of that map's domain in \n" + ctx.getText());
		}
		else
			env.put(name, (m = mapOfType(k.getClass(), v.getClass())));
		m.put(k, v);
		return null;
	}

	@Override
	public Object visitWeightE(EelParser.WeightEContext ctx){
		Object o = visit(ctx.exp());
		if(o instanceof Edge)
			return ((Edge)o).get();
		else
			throw new RuntimeException("Weight requested for non-edge object in \n" + ctx.getText());
	}

	@Override
	public Object visitNot(EelParser.NotContext ctx){
		Object o = visit(ctx.exp());
		if(!(o instanceof Boolean))
			throw new RuntimeException("The given expression does not evaluate to a boolean in \n" + ctx.getText());
		return (((Boolean)o).equals(true)? Boolean.FALSE : Boolean.TRUE);
	}

	@Override
	public Object visitContainment(EelParser.ContainmentContext ctx){
		Object x = visit(ctx.exp(0));
		Object s = visit(ctx.exp(1));
		if(!(s instanceof HSet))
			throw new RuntimeException("The second expression does not evaluate to a set in \n" + ctx.getText());
		return ((HSet)s).contains(x);
	}

	@Override
	public Object visitData(EelParser.DataContext ctx){
		Object v = visit(ctx.exp());
		if(!(v instanceof Vertex))
			throw new RuntimeException("Data requested from non-vertex object in \n" + ctx.getText());
		return ((Vertex)v).get();
	}
	
	@Override
	public Object visitAdjacent(EelParser.AdjacentContext ctx){
		Object g = visit(ctx.graphArg());
		boolean isdefault = false;
		if(g == null){
			if(!(env.containsKey("default")))
				throw new RuntimeException("Default graph undefined, so adjacency cannot be checked in \n" + ctx.getText());
			isdefault = true;
			g = env.get("default");
		}
		Object v1 = visit(ctx.exp(0));
		Object v2 = visit(ctx.exp(1));
		if(!(v1 instanceof Vertex))
			throw new RuntimeException("Argument " + (isdefault? 1 : 2) + " is not a vertex in \n" + ctx.getText());
		else if(!(v2 instanceof Vertex))
			throw new RuntimeException("Argument " + (isdefault? 2 : 3) + " is not a vertex in \n" + ctx.getText());
		else if(!(g instanceof Graph))
			throw new RuntimeException("Argument 1 is not an graph in \n" + ctx.getText());
		else if(!(((Graph)g).getV().contains((Vertex)v1)))
			throw new RuntimeException("The graph does not contain argument " + (isdefault? "1" : "2") + " in \n" + ctx.getText());
		else if(!(((Graph)g).getV().contains((Vertex)v2)))
			throw new RuntimeException("The graph does not contain argument " + (isdefault? "2" : "3") + " in \n" + ctx.getText());
		return ((Graph)g).adjacent((Vertex)v1, (Vertex)v2);
	}

	@Override
	public Object visitGraphArg(EelParser.GraphArgContext ctx){
		if(ctx.exp() == null)
			return null;
		return visit(ctx.exp());
	}

	@Override
	public Object visitFunctionDef(EelParser.FunctionDefContext ctx){
		env.put(ctx.ID().getText(), new Function(ctx.argsID(), ctx.block().program()));
		return null;
	}

	@Override
	public Object visitRange(EelParser.RangeContext ctx){
		Object o = visit(ctx.exp());
		if(o instanceof Integer){
			HSet<Integer> h = new HSet<Integer>(((Integer)o), Integer.class);
			for(int i=0; i<((Integer)o); i++)
				h.add(i);
			return h;
		}
		else
			throw new RuntimeException("Range requested for non-integral object in \n" + ctx.getText());
	}

	@Override
	public Object visitVertices(EelParser.VerticesContext ctx){
		Object o = visit(ctx.exp());
		if(o instanceof Graph)
			return ((Graph)o).getV();
		else
			throw new RuntimeException("Vertex set of non-graph object requested in \n" + ctx.getText());
	}

	@Override
	public Object visitInteger(EelParser.IntegerContext ctx){
		return Integer.parseInt(ctx.INT().getText());
	}

	@Override
	public Object visitReturn(EelParser.ReturnContext ctx){
		ret = visit(ctx.exp());
		returned = true;
		return null;
	}

	@Override
	public Object visitBlock(EelParser.BlockContext ctx){
		HashMap<String, Object> old = (HashMap<String, Object>)env.clone();
		visit(ctx.program());
		for(String key: env.keySet())
			if(old.containsKey(key) && !old.get(key).equals(env.get(key)))
				old.put(key, env.get(key));
		env = old;
		return null;
	}

	@Override
	public Object visitObject(EelParser.ObjectContext ctx){
		String type = ctx.type().getText();
		if(type.equals("vertex"))
			return new Vertex<Integer>((int)(Integer.MAX_VALUE*Math.random()), Integer.class);
//		else if(type.equals("edge"))
//			return new Edge<Integer>(new Vertex<Integer>((int)(Integer.MAX_VALUE*Math.random()), Integer.class), new Vertex<Integer>((int)(Integer.MAX_VALUE*Math.random()), Integer.class), 1);
		else if(type.equals("set"))
			return setOfType(Integer.class);
		else if(type.equals("map"))
			return mapOfType(Integer.class, Integer.class);
		else
			return graphOfType(Integer.class, Integer.class);
	}

	@Override
	public Object visitBoolexp(EelParser.BoolexpContext ctx){
		Object o1 = visit(ctx.exp(0));
		Object o2;
		String error = "Non-boolean argument passed to boolean expression in \n" + ctx.getText();
		if(!(o1 instanceof Boolean))
			throw new RuntimeException(error);
		String op = ctx.BOOLOP().getText();
		if(op.equals("and")){
			if(((Boolean)o1).equals(Boolean.FALSE))
				return o1;
			if((o2 = visit(ctx.exp(1))) instanceof Boolean)
				return o2;
			else
				throw new RuntimeException(error);
		}
		else if(op.equals("or")){
			if(((Boolean)o1).equals(Boolean.TRUE))
				return o1;
			if((o2 = visit(ctx.exp(1))) instanceof Boolean)
				return o2;
			else
				throw new RuntimeException(error);
		}
		else if(op.equals("xor")){
			if((o2 = visit(ctx.exp(1))) instanceof Boolean)
				return !(o1.equals(o2));
			else
				throw new RuntimeException(error);
		}
		return null;
	}

	@Override
	public Object visitMultiply(EelParser.MultiplyContext ctx){
		Object o1 = visit(ctx.exp(0));
		Object o2 = visit(ctx.exp(1));
		if(o1 instanceof Integer && o2 instanceof Integer){
			Integer i1 = (Integer)o1;
			Integer i2 = (Integer)o2;
			if(ctx.MULTOP().getText().equals("*"))
				return i1 * i2;
			else
				return i1 / i2;
		}
		else
			throw new RuntimeException("Non-integral argument passed to arithmetic operation in \n" + ctx.getText());
	}

	@Override
	public Object visitParens(EelParser.ParensContext ctx){
		return visit(ctx.exp());
	}

	@Override
	public Object visitAdd(EelParser.AddContext ctx){
		Object o1 = visit(ctx.exp(0));
		Object o2 = visit(ctx.exp(1));
		boolean isplus = ctx.ADDOP().getText().equals("+");
		if(o1 instanceof Integer && o2 instanceof Integer){
			Integer i1 = (Integer)o1;
			Integer i2 = (Integer)o2;
			if(isplus)
				return i1 + i2;
			else
				return i1 - i2;
		}
		else if(o1 instanceof HSet){
			HSet h = ((HSet)o1).clone();
			if(isplus){
				if(h.isEmpty())
					h = setOfType(o2.getClass());
				else if(o2.getClass() != h.hClass())
					throw new RuntimeException("Element added to set containing elements of a different type in \n" + ctx.getText());
				h.add(o2.getClass().cast(o2));
			}
			else
				h.remove(o2);
			return h;
		}
		else if(o1 instanceof Graph){
			Graph g = ((Graph)o1).clone();
			boolean isv = (o2 instanceof Vertex);
			boolean ise = (o2 instanceof Edge);
			if(!ise && !isv)
				throw new RuntimeException("Non-vertex, non-edge object added to graph in \n" + ctx.getText());
			if(isplus){
				if(g.isEmpty()){
					if(isv)
						g = graphOfType(((Vertex)o2).vClass(), Integer.class);
					else 
						g = graphOfType(((Edge)o2).vClass(), ((Edge)o2).eClass());
				}
				else {
					if(isv && g.vClass() != ((Vertex)o2).vClass())
						throw new RuntimeException("The vertex added to the graph is not of the same type as the graph's vertices in \n" + ctx.getText());
					else if(ise){
						if(g.eClass() != ((Edge)o2).eClass())
							throw new RuntimeException("The edge added to the graph is not of the same type as the graph's edges in \n" + ctx.getText());
						if(g.vClass() != ((Edge)o2).vClass())
							throw new RuntimeException("The edge added to the graph has vertices that are not of the same type as the graph's vertices in \n" + ctx.getText());
					}
				}
				if(isv)
					g.add((Vertex)o2);
				else
					g.add((Edge)o2);
			}
			else {
				if(isv)
					g.remove((Vertex)o2);
				else
					g.remove((Edge)o2);
			}
			return g;
		}
		else
			throw new RuntimeException("Non-integral argument passed to arithmetic operation in \n" + ctx.getText());
	}

	@Override
	public Object visitInfinity(EelParser.InfinityContext ctx){
		return Integer.MAX_VALUE;
	}

	@Override
	public Object visitEdges(EelParser.EdgesContext ctx){
		Object o = visit(ctx.exp());
		if(o instanceof Graph)
			return ((Graph)o).getE();
		else
			throw new RuntimeException("Edge set of non-graph object requested in \n" + ctx.getText());
	}

	@Override
	public Object visitMap(EelParser.MapContext ctx){
		String name = ctx.ID().getText();
		Object key = visit(ctx.exp());
		if(env.containsKey(name)){
			if(!(env.get(name) instanceof HMap))
				throw new RuntimeException("\"" + name + "\" is not defined as a map in \n" + ctx.getText());
			else if(((HMap)env.get(name)).containsKey(key))
				return ((HMap)env.get(name)).get(key);
			else
				throw new RuntimeException(ctx.exp().getText() + " evaluates to " + key + ", which is not part of the domain of \"" + name + "\" in \n" + ctx.getText());
		}
		else
			throw new RuntimeException("Call to undefined map \"" + name + "\" in \n" + ctx.getText());
	}

	@Override
	public Object visitComparison(EelParser.ComparisonContext ctx){
		Object o1 = visit(ctx.exp(0));
		Object o2 = visit(ctx.exp(1));
		String comp = ctx.COMP().getText();
		if(comp.equals("=="))
			return visit(ctx.exp(0)).equals(visit(ctx.exp(1)));
		if(comp.equals("!="))
			return !(visit(ctx.exp(0)).equals(visit(ctx.exp(1))));
		/*
		 * to be revised to include other types
		 */
		if(o1 instanceof Integer && o2 instanceof Integer){
			Integer i1 = (Integer)o1;
			Integer i2 = (Integer)o2;
			if(comp.equals(">"))
				return i1 > i2;
			if(comp.equals("<"))
				return i1 < i2;
			if(comp.equals(">="))
				return i1 >= i2;
			else
				return i1 <= i2;
		}
		else
			throw new RuntimeException("Non-comparable argument passed to a comparison in \n" + ctx.getText());
	}

	@Override
	public Object visitPrint(EelParser.PrintContext ctx){
		System.out.println(visit(ctx.exp()));
		return null;
	}

	@Override
	public Object visitAssignment(EelParser.AssignmentContext ctx){
		Object o = visit(ctx.exp());
		env.put(ctx.ID().getText(), o);
		return null;
	}

	@Override
	public Object visitNeighbors(EelParser.NeighborsContext ctx){
		HSet h;
		Object v = visit(ctx.exp());
		if(!(v instanceof Vertex))
			throw new RuntimeException("Cannot findneighbors of non-vertex argument in \n"  + ctx.getText());
		if(ctx.graphArg().exp() == null){
			if(!env.containsKey("default"))
				throw new RuntimeException("Default graph undefined, so neighbors cannot be found in \n" + ctx.getText());
			h = ((Graph)env.get("default")).getNeighbors((Vertex)v);
			if(h == null)
				throw new RuntimeException("The given vertex is not contained in the default graph in \n" + ctx.getText());
		}
		else {
			Object g = visit(ctx.graphArg());
			if(!(g instanceof Graph))
				throw new RuntimeException("\"" + ctx.graphArg().exp().getText() + "\" is not defined as a graph in \n" + ctx.getText());
			h = ((Graph)g).getNeighbors((Vertex)v);
			if(h == null)
				throw new RuntimeException("The given vertex is not contained in the default graph in \n" + ctx.getText());
		}
		return h;
	}

	@Override
	public Object visitDefaultAssignment(EelParser.DefaultAssignmentContext ctx){
		Object o = visit(ctx.exp());
		if(o instanceof Graph)
			env.put("default", (Graph)o);
		else
			throw new RuntimeException("Non-graph object passed to default graph assignment in \n" + ctx.getText());
		return null;
	}

	@Override
	public Object visitSetexp(EelParser.SetexpContext ctx){
		Object o1 = visit(ctx.exp(0));
		Object o2 = visit(ctx.exp(1));
		String op = ctx.SETOP().getText();
		if(!(o1 instanceof HSet))
			throw new RuntimeException("Argument 1 does not evaluate to a set in \n" + ctx.getText());
		if(!(o2 instanceof HSet))
			throw new RuntimeException("Argument 2 does not evaluate to a set in \n" + ctx.getText());
		if(!(((HSet)o1).hClass() != ((HSet)o2).hClass()))
			throw new RuntimeException("Arguments 1 and 2 are sets containing different types in \n" + ctx.getText());
		if(op.equals("union"))
			return ((HSet)o1).setUnion((HSet)o2);
		if(op.equals("intersect"))
			return ((HSet)o1).setIntersection((HSet)o2);
		else
			return ((HSet)o1).setComplement((HSet)o2);
	}

	@Override
	public Object visitEdge(EelParser.EdgeContext ctx){
		Object v1 = visit(ctx.exp(0));
		Object v2 = visit(ctx.exp(1));
		Object w = (ctx.exp(2) == null? new Integer(1) : visit(ctx.exp(2)));
		if(!(v1 instanceof Vertex))
			throw new RuntimeException("Argument 1 is not a vertex in \n" + ctx.getText());
		else if(!(v2 instanceof Vertex))
			throw new RuntimeException("Argument 2 is not a vertex in \n" + ctx.getText());
		else if(((Vertex)v1).vClass() != ((Vertex)v2).vClass())
			throw new RuntimeException("Vertex arguments are of different types in \n" + ctx.getText());
		return edgeOfType((Vertex)v1, (Vertex)v2, w, v1.getClass(), w.getClass());
	}
	@Override
	public Object visitEdgeAt(EelParser.EdgeAtContext ctx){
		Object g = visit(ctx.graphArg());
		boolean isdefault = false;
		if(g == null){
			if(!(env.containsKey("default")))
				throw new RuntimeException("Default graph undefined, so edge cannot be found in \n" + ctx.getText());
			isdefault = true;
			g = env.get("default");
		}
		Object v1 = visit(ctx.exp(0));
		Object v2 = visit(ctx.exp(1));
		if(!(v1 instanceof Vertex))
			throw new RuntimeException("Argument " + (isdefault? 1 : 2) + " is not a vertex in \n" + ctx.getText());
		else if(!(v2 instanceof Vertex))
			throw new RuntimeException("Argument " + (isdefault? 2 : 3) + " is not a vertex in \n" + ctx.getText());
		else if(!(g instanceof Graph))
			throw new RuntimeException("Argument 1 is not an graph in \n" + ctx.getText());
		else if(!(((Graph)g).getV().contains((Vertex)v1)))
			throw new RuntimeException("The graph does not contain argument " + (isdefault? "1" : "2") + " in \n" + ctx.getText());
		else if(!(((Graph)g).getV().contains((Vertex)v2)))
			throw new RuntimeException("The graph does not contain argument " + (isdefault? "2" : "3") + " in \n" + ctx.getText());
		Edge e = ((Graph)g).getEdge((Vertex)v1, (Vertex)v2);
		if(e == null)
			throw new RuntimeException("There is no edge between the two vertices in \n" + ctx.getText());
		return e;
	}

	@Override
	public Object visitFunctionCall(EelParser.FunctionCallContext ctx){
		if(env.containsKey(ctx.ID().getText())){
			Function f = (Function)env.get(ctx.ID().getText());
			if(f.actx.ID().size() == ctx.argsExp().exp().size()) {
				depth++;
				if(depth > MAXRECURSIONDEPTH)
					throw new RuntimeException("Max recursion depth exceeded; last call was to \"" + ctx.ID().getText() + "\" in \n" + ctx.getText());
				HashMap<String, Object> old = (HashMap<String, Object>)env.clone();
				for(int i=0; i<ctx.argsExp().exp().size(); i++)
					env.put(f.actx.ID(i).getText(), visit(ctx.argsExp().exp(i)));
				visit(f.pctx);
				depth--;
				env = old;
				if(returned){
					returned = false;
					return ret;
				}
			}
			else
				throw new RuntimeException("Function " + ctx.ID().getText() + " takes " + f.actx.getChildCount() + " arguments, given " + ctx.argsExp().getChildCount() + " arguments in \n" + ctx.getText());
		}
		else
			throw new RuntimeException("Call to undefined function in \n" + ctx.getText());
		return null;
	}

	@Override
	public Object visitConditional(EelParser.ConditionalContext ctx){
		int i = 0;
		boolean elsecase = true;
		for(EelParser.ExpContext e : ctx.exp()){
			Object o = visit(e);
			if(o instanceof Boolean){
				if((Boolean)visit(e)){
					visit(ctx.block(i));
					elsecase = false;
					break;
				}
			}
			else
				throw new RuntimeException("Non-boolean return value of \n" + e.getText() + "\nin\n" + ctx.getText());
			i++;
		}
		if(elsecase && ctx.block(i) != null)
			visit(ctx.block(i));
		return null;
	}

	@Override
	public Object visitBool(EelParser.BoolContext ctx){
		return (ctx.BOOL().getText().equals("true")? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public Object visitLabeledV(EelParser.LabeledVContext ctx){
		Object o = visit(ctx.exp());
		return vertexOfType(o.getClass(), o);
	}

	@Override
	public Object visitWhileLoop(EelParser.WhileLoopContext ctx){
		while((Boolean)visit(ctx.exp()))
			visit(ctx.block());
		return null;
	}

	@Override
	public Object visitIdentifier(EelParser.IdentifierContext ctx){
		String name = ctx.ID().getText();
		if(env.containsKey(name))
			return env.get(name);
		else
			throw new RuntimeException("Undefined identifier \"" + name + "\" in \n" + (ctx.getParent() != null ? ctx.getParent().getText() : ""));
	}

	@Override
	public Object visitForLoop(EelParser.ForLoopContext ctx){
		Object set = visit(ctx.exp());
		String name = ctx.ID().getText();
		if(env.containsKey(name))
			throw new RuntimeException("\"" + name + "\" is already defined prior to the for loop: \n" + ctx.getText()); 
		if(set instanceof Iterable){
			for(Object o : (Iterable)set){
				env.put(name, o);
				visit(ctx.block());
			}
			env.remove(name);
		}
		else
			throw new RuntimeException("Non-iterable object passed to for loop in \n" + ctx.exp().getText() + "\nin\n" + ctx.getText());
		return null;
	}

	@Override
	public Object visitLoneExp(EelParser.LoneExpContext ctx){
		visit(ctx.exp());
		return null;
	}

	private <T> HSet<T> setOfType(Class<T> c){
		return new HSet<T>(c);
	}

	private <V, E> Graph<V, E> graphOfType(Class<V> cv, Class<E> ce){
		return new UndirectedGraph<V, E>(cv, ce);
	}

	private <T> Vertex<T> vertexOfType(Class<T> c, Object o){
		return new Vertex<T>((T)o, c);
	}

	private <V, E> Edge<V, E> edgeOfType(Object v1, Object v2, Object w, Class<V> cv, Class<E> ce){
		return new Edge<V, E>((Vertex<V>)v1, (Vertex<V>)v2, (E)w, ce);
	}

	private <K, V> HMap<K, V> mapOfType(Class<K> ck, Class<V> cv){
		return new HMap<K, V>(ck, cv);
	}

	private class Function {
		public EelParser.ArgsIDContext actx;
		public EelParser.ProgramContext pctx;
		public Function(EelParser.ArgsIDContext a, EelParser.ProgramContext p){
			actx = a;
			pctx = p;
		}
	}
}

