package queryevaluator;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.tree.TreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import inputprocessor.XMLNavigator;
import generatedparser.*;

/**
 * Evaluates XPath expressions and returns appropriate results
 * @author RaresMan
 * 
 */
public class XQueryEvaluator {
	
	Node xml_root;
	HashMap<String,List<Node>> context;
	enum oper{ AND, OR, NOT }
	public XQueryEvaluator()
	{
		// TODO: Add support for more than 1 XML root?
		xml_root = null;	
		context = new HashMap<String,List<Node>>();
	}

	
	 /** Main entry point. */
	public static void main(String args[]) {
    	
    	System.out.println("Reading from standard input...");
    	XPath t = new XPath(System.in);
    	
    	t.enable_tracing();
    	try {
    		ASTStart startNode = t.Start();
        	dumpWithTokens(startNode, " ");   
        		
      		XQueryEvaluator ev = new XQueryEvaluator();
      		ev.evaluate(startNode); 
      		//n.dump("");
      		//dumpWithTokens(n, " ");      		
      		System.out.println("Thank you.");
    	} catch (Exception e) {
      		System.out.println("Oops.");
      		System.out.println(e.getMessage());
      		e.printStackTrace();
    	}
  	}

	// XQUERY EVALUATOINS	
	public String evaluate(ASTStart ast_root) throws Exception {
		SimpleNode queryORjoin = (SimpleNode) ast_root.jjtGetChild(0);
		
		List<Node> finalResults = null;
		if(ast_root.jjtGetNumChildren() >= 1 && queryORjoin instanceof ASTXQuery) {			
			finalResults = evaluate_xquery( (ASTXQuery) ast_root.jjtGetChild(0), context);
		} else if ( queryORjoin instanceof ASTjoin) {
			finalResults = evaluate_join((ASTjoin) queryORjoin, context );
		} else {
			throw new Exception("Error: Child of ast_root was not of type XQuery");
		}
			
		List<Node> noDups  	= removeDuplicates(finalResults);
			
		System.out.println("-------------------------- Result Document -----------------------------------------\n");			
		String xmlDocumentString = createXMLDocFromNodeList(noDups);
		System.out.println(xmlDocumentString);
		System.out.println("------------------------------------------------------------------------------------");		
		
		return xmlDocumentString;
	}

	public List<Node> evaluate_join( ASTjoin join, HashMap<String,List<Node>> context) throws Exception
	{
		SimpleNode xquery1 = (SimpleNode) join.jjtGetChild(0);
		SimpleNode xquery2 = (SimpleNode) join.jjtGetChild(2);
		
		List<Node> xquery1_result = evaluate_xquery((ASTXQuery) xquery1,  context);
		List<Node> xquery2_result = evaluate_xquery((ASTXQuery) xquery2,  context);
		
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		
		int semicolon_index = 0;
		for ( int i = 4; i < join.jjtGetNumChildren(); i++)
		{
			SimpleNode tagi = (SimpleNode) join.jjtGetChild(i);
			if ( tagi instanceof ASTsemicolon)
			{
				semicolon_index = i;
				break;
			}
			else if ( tagi instanceof ASTcomma)
				continue;
			else
			{
				String token = tagi.jjtGetFirstToken().toString();
				list1.add(token);
			}
		}
		
		for ( int i = semicolon_index+1; i < join.jjtGetNumChildren(); i++)
		{
			SimpleNode tagi = (SimpleNode) join.jjtGetChild(i);
			if ( tagi instanceof ASTcomma)
				continue;
			else
			{
				String token = tagi.jjtGetFirstToken().toString();
				list2.add(token);
			}
		}
		
		//System.out.println(" --------LIST1 -------");
		for ( String child : list1)
		{
			 //System.out.println(child);
		}
		////System.out.println(" --------LIST2 -------");
		for ( String child : list2)
		{
			 //System.out.println(child);
		}
		//printResultList(xquery2_result);
		
		return evaluate_naive_join( xquery1_result, xquery2_result, list1, list2);		
	}
	
	
	
	public List<Node> evaluate_naive_join( List<Node> xquery1_result, List<Node> xquery2_result, List<String> tag1_list, List<String> tag2_list )
	{
		System.out.println("MATCHED RESULTS");
		
		List<Node> results = new ArrayList<Node>();
		
		for ( Node query1_node : xquery1_result)
		{
			for ( Node query2_node : xquery2_result)
			{
				if ( compareNodes( query1_node, query2_node, tag1_list, tag2_list))
				{					
//					System.out.println("-------------------------------------------------------------");
//					printNode(query1_node);
//					printNode(query2_node);
//					System.out.println("//");
					results.add(combine_tuples(query1_node, query2_node));
				}
			}						
		}
		
		return results;		
	}
	

	
	public List<Node> evaluate_hash_join( List<Node> xquery1_result, List<Node> xquery2_result, List<String> tag1_list, List<String> tag2_list )
	{
//		System.out.println("MATCHED RESULTS");
		
		List<Node> results = new ArrayList<Node>();
		
		for ( Node query1_node : xquery1_result)
		{
			for ( Node query2_node : xquery2_result)
			{
				if ( compareNodes( query1_node, query2_node, tag1_list, tag2_list))
				{					
//					System.out.println("-------------------------------------------------------------");
//					printNode(query1_node);
//					printNode(query2_node);
//					System.out.println("//");
					results.add(combine_tuples(query1_node, query2_node));
				}
			}						
		}
		
		return results;		
	}
	
	public Node combine_tuples(Node tuple1, Node tuple2) {
		
		Document xmlDoc = tuple1.getOwnerDocument();
		Node resultTuple = xmlDoc.createElement("tuple");
		
		NodeList tuple1Children = tuple1.getChildNodes();
		NodeList tuple2Children = tuple2.getChildNodes();

		for(int i=0; i < tuple1Children.getLength(); i++) {
			Node tuple1Child = tuple1Children.item(i);
			resultTuple.appendChild(xmlDoc.importNode(tuple1Child, true));
		}
		
		for(int i=0; i<tuple2Children.getLength();i++) {
			Node tuple2Child = tuple2Children.item(i);			
			resultTuple.appendChild(xmlDoc.importNode(tuple2Child, true));
		}
		
		return resultTuple;
	}
	
	public boolean compareNodes( Node a, Node b, List<String> tag1_list, List<String> tag2_list)
	{
		
		for ( int i = 0; i < tag1_list.size(); i++)
		{
			String tag1 = tag1_list.get(i);
			String tag2 = tag2_list.get(i);
			Node matched_node1 = get_child_with_tag( tag1, a);
			Node matched_node2 = get_child_with_tag( tag2, b);
			if (  matched_node1 == null || matched_node2 == null)
				return false;
			else {
				// Start the comparison at their children since the tags may be different
				NodeList node1Children = matched_node1.getChildNodes();
				NodeList node2Children = matched_node2.getChildNodes();
				
				if(node1Children.getLength() != node2Children.getLength()) {
					return false;
				} else {
					for(int j=0;j<node1Children.getLength();j++) {
						Node node1Child = node1Children.item(j);
						Node node2Child = node2Children.item(j);
						if(!node1Child.isEqualNode(node2Child)) {
							return false;
						}
					}
				}				
			}
			
		}
		return true;
	}
	public Node get_child_with_tag( String tagname, Node parent)
	{
		NodeList children = parent.getChildNodes();
		//System.out.println( "PARENT 	WITH TAGNAME : "  + tagname);
		//printNode( parent);
		for ( int i = 0; i < children.getLength(); i++)
		{
			
			Node child = children.item(i);
			if ( child.getNodeName().equals(tagname))
			{
				//System.out.println( "MATCHED  "  );
				return child;
			}
		}
		return null;
	}
	
	public List<Node> evaluate_var( ASTvar ast_var, HashMap<String,List<Node>> context)
	{
		String var_name = ast_var.jjtGetFirstToken().toString();
		
		List<Node> var_list = new ArrayList<Node>();		
		var_list.addAll(context.get(var_name));
		return var_list;
	}
	
	public List<Node> evaluate_stringConstant( ASTstringConstant ast_stringConstant, HashMap<String,List<Node>> context )
	{
		String stringConst = ast_stringConstant.jjtGetFirstToken().toString();
		//System.out.println("Before: " + stringConst);
		stringConst = stringConst.substring(1, stringConst.length() - 1 );
		//System.out.println("After: " + stringConst);
		Node text_node = XMLNavigator.makeText(stringConst);
		List<Node> textList = new ArrayList<Node>();
		textList.add(text_node);
		return textList;
	}
	

	
	public List<Node>  evaluate_letclause(ASTletclause ast_letclause, HashMap<String,List<Node>> context ) throws Exception
	{
		evaluate_empty_or_letclause( ast_letclause, context); // Evaluates the letclause part of the statement, extends context
		SimpleNode xquery = (SimpleNode) ast_letclause.jjtGetChild(ast_letclause.jjtGetNumChildren() -1);
		return evaluate_xquery( (ASTXQuery) xquery, context);
		
	}
	
	public Node createDependencyGraph(ASTforclause ast_forclause) {
		
		Document doc = XMLNavigator.getNewDocument();
		Node root = doc.createElement("root");
		
		Node newChild;
				
		
		
		for ( int i = 0; i < ast_forclause.jjtGetNumChildren(); i+=2)
		{
			ASTvar vari 	  =  (ASTvar) ast_forclause.jjtGetChild(i);
			ASTXQuery xquery1 =  (ASTXQuery) ast_forclause.jjtGetChild(i+1);
			
			
			
			SimpleNode xquery_identifier = (SimpleNode) xquery1.jjtGetChild(0);
			String vari_string = vari.jjtGetFirstToken().toString().substring(1);
			
			if ( xquery_identifier instanceof ASTxquery_identifier)
			{
				SimpleNode xquery_var = (SimpleNode) xquery_identifier.jjtGetChild(0);
				
				if ( xquery_var instanceof ASTvar)
				{
					
					String query_var_string =  ((ASTvar)xquery_var).jjtGetFirstToken().toString().substring(1);
					List<Node> root_list = XMLNavigator.descendantOrSelf(root);
					for ( Node root_element : root_list)
					{
						if ( root_element.getNodeName().equals(query_var_string))
						{
							newChild = doc.createElement(vari_string);
							newChild.setTextContent(reconstructInput(xquery1));
							root_element.appendChild(newChild);
							break;
						}
					}
				}
			
				else
				{
					newChild = doc.createElement(vari_string);
					newChild.setTextContent(reconstructInput(xquery1));
					root.appendChild(newChild);
				}
					
			}
		
		}
		
		return root;
		
	}
	
	public int getGraphID( Node root, String  varName)
	{
		
		NodeList root_children = root.getChildNodes();
		for ( int i = 0; i < root_children.getLength(); i++ )
		{
			Node root_child = root_children.item(i);
			List<Node> allChildrenOfChild = XMLNavigator.descendantOrSelf(root_child);
			for ( Node descendentChild : allChildrenOfChild)
			{
				if ( descendentChild.getNodeName().equals(varName.substring(1)))
				{
					return i;
				}
			}
			
		}
		return -1;
	}
	
	public String getReturnClauseFromGraph(Node root) {
		
		String tags = "";		
		if(root.getChildNodes().getLength() == 1) {
						
			String tagName = root.getNodeName();
			return tags += ",\n\t\t" + "<" + tagName + ">{$" + tagName + "}</" + tagName + ">";			
		} else if(root.getChildNodes().getLength() > 1)  {			
			String tagName = root.getNodeName();
			tags += ",\n\t\t" +  "<" + tagName + ">{$" + tagName + "}</" + tagName + ">";			
			
			NodeList rootChildren = root.getChildNodes();
			for(int i=0; i<rootChildren.getLength(); i++) {
				Node rootChild = rootChildren.item(i);
				if(rootChild.getNodeType() == Node.TEXT_NODE) {
					continue;
				}
				tags = tags + getReturnClauseFromGraph(rootChildren.item(i));
			}		
			
			
		}
		
		return tags;
		
	}
	
	public String getForClauseFromGraph(Node root) {
		
		String variables = "";

		if(root.getChildNodes().getLength() == 1) {			
			String tagName = root.getNodeName();
			
			tagName = "$" + tagName;
			String query   = root.getFirstChild().getNodeValue();			
			variables = variables + ";\n\t\t" + tagName + " in " + query;
		} else if(root.getChildNodes().getLength() > 1)  {
			
			String tagName = root.getNodeName();			
			tagName = "$" + tagName;
			String query   = root.getFirstChild().getNodeValue();			
			variables = variables + ";\n\t\t" + tagName + " in " + query;
						
			NodeList rootChildren = root.getChildNodes();
			for(int i=0; i<rootChildren.getLength(); i++) {
				Node rootChild = rootChildren.item(i);
				if(rootChild.getNodeType() == Node.TEXT_NODE) {
					continue;
				}
				variables = variables + getForClauseFromGraph(rootChildren.item(i));
			}			 	
		}
		
		return variables;
	}
	
	public String rewrite_forclause(ASTforclause ast_forclause, ASTwhereclause ast_whereclause ) throws Exception
	{
		if ( ast_whereclause.jjtGetNumChildren() == 0)
			return null;
		Node root = createDependencyGraph(ast_forclause);
		List<List<String>>  joinConditions = new ArrayList<List<String>>();
		for ( int i = 0; i <root.getChildNodes().getLength(); i++)
		{
			joinConditions.add(new ArrayList<String>());
		}
		
		List<Node> root_list = new ArrayList<Node>();
		root_list.add(root);
		Node root1 = root.getFirstChild();
		Node root2 = root1.getNextSibling();
		
		String xml_doc = createXMLDocFromNodeList(root_list);
		System.out.println( "DEPENDENCY GRAPHS:\n" + xml_doc);
		
		
		ASTCond ast_condition = (ASTCond) ast_whereclause.jjtGetChild(0);
		SimpleNode ast_cond_id = (SimpleNode) ast_condition.jjtGetChild(0);
		if(ast_cond_id instanceof ASTcond_identifier) {
			// get XQuery1
			SimpleNode xquery1 = (SimpleNode) ast_cond_id.jjtGetChild(0);
			// get eq_value or eq_id
			if(xquery1 instanceof ASTXQuery) {
				SimpleNode equality = (SimpleNode) ast_cond_id.jjtGetChild(1);
				// get XQuery2
				ASTXQuery xquery2 = (ASTXQuery) ast_cond_id.jjtGetChild(2);
				
				String var1 = getVarFromXQuery( (ASTXQuery) xquery1);
				String var2 = getVarFromXQuery( xquery2);
				if ( var1 != null && var2 != null)
				{
					int index_var1 = getGraphID(root, var1);
					int index_var2 = getGraphID(root, var2);
					if ( index_var1 != index_var2 && index_var1 != -1 && index_var2 != -1)
					{

						System.out.println("JOIN VARIABLES: ");
						System.out.println("\t" + var1 + " " + index_var1);
						System.out.println("\t" + var2 + " " + index_var2);
						joinConditions.get(index_var1).add(var1);
						joinConditions.get(index_var2).add(var2);
					}
				}				
			} else {
				return null;
			}
		}
		else if ( ast_cond_id instanceof ASTcond_prime)
		{
			// TODO : TEST FOR MULTIPLE CONCATINATED CONDITIONS
		}
		if ( joinConditions.get(0).isEmpty() || joinConditions.get(1).isEmpty())
			return null;
		
		String forClause1 = "for " + getForClauseFromGraph(root1).substring(3);
		String forClause2 = "for " + getForClauseFromGraph(root2).substring(3);
		String returnClause1 = "return " + "<tuple>{" + getReturnClauseFromGraph(root1).substring(3) + "}</tuple>";
		String returnClause2 = "return " + "<tuple>{" + getReturnClauseFromGraph(root2).substring(3) + "}</tuple>";
		
		
		System.out.println("FOR CLAUSE 1: " + forClause1);
		System.out.println("RTR CLAUSE 1: " + returnClause1);
		
		System.out.println("FOR CLAUSE 2: " + forClause2);
		System.out.println("RTR CLAUSE 2: " + returnClause2);
		
		String joinQuery = 
			"join(\t" + forClause1 + "\n\t" + returnClause1 + ";\n\t" + 
			          forClause2 + "\n\t" + returnClause2 + ";\n\t" +
			       "[" + joinConditions.get(0).get(0).substring(1) + "];" +
			       " [" + joinConditions.get(1).get(0).substring(1) + "]);\n";
		
		System.out.println(joinQuery);
		
		return joinQuery;
		// From evaluate_cond
		//ASTCond ast_condition = (ASTCond) ast_whereclause.jjtGetChild(0);
//		SimpleNode child = (SimpleNode) ast_cond.jjtGetChild(0);
//		
//		boolean firstCond_result;
//	    if ( child instanceof ASTcond_identifier)
//	    {
//	    	firstCond_result =  evaluate_cond_identifier((ASTcond_identifier) child, context);
//	    }
//	    else if ( child instanceof ASTCond_paranthesis)
//	    {
//	    	firstCond_result = evaluate_cond_paranthesis( (ASTCond_paranthesis) child, context);
//	    }
//	    else 
//	    	throw new Exception(" Wrong type at Cond, expecting cond_identifier or cond_paranthesis");
//	    
//	    if ( ast_cond.jjtGetNumChildren() > 1)
//	    {
//			for ( int i = ast_cond.jjtGetNumChildren()-1; i > 0 ; i--)
//			{ 
//				SimpleNode cond_prime =  (SimpleNode) ast_cond.jjtGetChild(i);
//				
//			}
//			
//			return current_boolean;
//	    }
//	    else
//	    	return firstCond_result;
		
		// Analyze the where clause
//		ASTCond ast_condition = (ASTCond) ast_whereclause.jjtGetChild(0);
//		SimpleNode ast_cond_id = (SimpleNode) ast_condition.jjtGetChild(0);
//		if(ast_cond_id instanceof ASTcond_identifier) {
//			ASTXQuery xquery1 = (ASTXQuery) ast_cond_id.jjtGetChild(0);
//			SimpleNode equality = (SimpleNode) ast_cond_id.jjtGetChild(1);
//			ASTXQuery xquery2 = (ASTXQuery) ast_cond_id.jjtGetChild(2);
//			
//			
//			
//			String var1 = getVarFromXQuery(xquery1).substring(1);
//			String var2 = getVarFromXQuery(xquery2).substring(1);
//			
//			if(var1 != null && var2 != null) {
//				List<Node> root1List = XMLNavigator.descendantOrSelf(root1);
//				List<Node> root2List = XMLNavigator.descendantOrSelf(root1);
//				
//				
//				boolean var1_1 = false;
//				boolean var1_2 = false;
//				boolean var2_1 = false;
//				boolean var2_2 = false;
//				
//				for(Node root1Node : root1List) {
//					if(root1Node.getNodeName().equals(var1)) {
//						var1_1 = true;
//					}
//					if(root1Node.getNodeName().equals(var2)) {
//						var2_1 = true;
//					}
//				}
//
//				for(Node root2Node : root2List) {
//					if(root2Node.getNodeName().equals(var1)) {
//						var1_2 = true;
//					}
//					if(root2Node.getNodeName().equals(var2)) {
//						var2_2 = true;
//					}
//				}
//				
//				if( var1_1 && var2_2 || var1_2 && var2_1) {
//					// TODO: rewrite for/where/return
//					
//					
//					
//				} else {
//					// Don't re-write
//				}				
//			}			
//		}		
	}
	
	public List<Node>  evaluate_xquery_flwr(ASTxquery_flwr ast_flwr, HashMap<String,List<Node>> context ) throws Exception
		{	
			// Get all the children nodes of ast_flwr
			ASTforclause forClause 			=  (ASTforclause) ast_flwr.jjtGetChild(0);
			ASTwhereclause whereChild 		= null;
			ASTempty_or_letclause letChild 	= null;
			ASTreturnclause returnChild 	= (ASTreturnclause) ast_flwr.jjtGetChild( ast_flwr.jjtGetNumChildren()-1);
	
			SimpleNode secondChild =  (SimpleNode) ast_flwr.jjtGetChild(1);
			
			if ( secondChild instanceof ASTempty_or_letclause)
			{
				SimpleNode thirdChild =  (SimpleNode) ast_flwr.jjtGetChild(2);
				letChild = (ASTempty_or_letclause) secondChild;
				if ( thirdChild instanceof ASTwhereclause)
					whereChild = (ASTwhereclause) thirdChild;
			}
			else if ( secondChild instanceof ASTwhereclause)
				whereChild = (ASTwhereclause) secondChild;
				
			HashMap<String, List<Node>> forEvalContext = new HashMap<String, List<Node>>();
			forEvalContext.putAll(context);
			
			// Evaluate for clause
			//evaluate_forclause( (ASTforclause) forclause, forEvalContext); // Just sets the context
			
			// Evaluate the let clause		
	//		if ( letChild != null && letChild.jjtGetNumChildren() > 0)
	//		{
	//			evaluate_empty_or_letclause( (ASTempty_or_letclause) letChild, forEvalContext);			
	//		}
	//								
	//		// Our context now has all of the variables from the for and the let clauses
	//		List<String> variableList = new ArrayList<String>();
	//		for(String variableName : forEvalContext.keySet()) {
	//			variableList.add(variableName);
	//		}
			
			// Attempt to rewrite to join
			String joinQuery = rewrite_forclause(forClause, whereChild);
			
			if ( joinQuery != null)
			{
				ByteArrayInputStream testStream = new ByteArrayInputStream(joinQuery.getBytes());
		    	XPath t 						= new XPath(testStream);

				ASTStart startNode;
				startNode = t.Start();
				XQueryEvaluator ev = new XQueryEvaluator();
				
				SimpleNode queryORjoin = (SimpleNode) startNode.jjtGetChild(0);
				
				List<Node> joinResults = null;
			    if ( queryORjoin instanceof ASTjoin) {
					joinResults = evaluate_join((ASTjoin) queryORjoin, context );
				} 
				
			    
			    List<String> variables = getAllVariablesFromXQuery((SimpleNode) returnChild.jjtGetChild(0));
			    
			    System.out.println("VARIABLES::");
			    for(String variable : variables) {
			    	System.out.println("\t" + variable);
			    }
			    
			    
			    
			    List<Node> finalResults = new ArrayList<Node>();
			    
			    for(Node tuple : joinResults) {
			    	HashMap<String, List<Node>> localContext = new HashMap<String, List<Node>>();
			    	NodeList tupleChildren = tuple.getChildNodes();
			    	for(int i=0; i<tupleChildren.getLength();i++) {
			    		Node tupleChild = tupleChildren.item(i);
			    		for(String variable : variables) {
			    			if(tupleChild.getNodeName().equals(variable)) {
			    				
			    				if(tupleChild.getNodeType() == Node.ELEMENT_NODE) {
			    					List<Node> listWrapper = new ArrayList<Node>();
			    					listWrapper.add(tupleChild.getFirstChild());
			    					localContext.put("$" + variable, listWrapper);			    					
			    				} else {
			    					List<Node> listWrapper = new ArrayList<Node>();
			    					listWrapper.add(tupleChild);
			    					localContext.put("$" + variable, listWrapper);
			    				}
			    			}
			    		}
			    	}
			    	
			    	finalResults.addAll(evaluate_xquery((ASTXQuery) returnChild.jjtGetChild(0) , localContext));
			    }
				
				return finalResults;
				
			}
			else
				return for_clause_dynamic_loop(forClause, forEvalContext, whereChild, returnChild, 0);	
			
			// TODO: If the string returned by rewrite_forclause is not null, evaluate join operator
			//       Else evaluate naively 
			
			// Evaluate where & return clauses and output the results
							
		}


	public String getVarFromXQuery(ASTXQuery xquery) {
		SimpleNode xquery_id = (SimpleNode) xquery.jjtGetChild(0);
		
		if(xquery_id instanceof ASTxquery_identifier) {
			SimpleNode ast_var = (SimpleNode) xquery_id.jjtGetChild(0);
			if(ast_var instanceof ASTvar) {
				return ((ASTvar) ast_var).jjtGetFirstToken().toString();
			}
		}
		
		return null;
	}
	
	public List<Node> evaluate_varQuery(ASTvar ast_var, ASTXQuery ast_xquery, HashMap<String,List<Node>> context ) throws Exception
	{
		List<Node> xquery_result = evaluate_xquery( ast_xquery, context);
		//String varName = ast_var.jjtGetFirstToken().toString();				
		//context.put(varName, xquery_result);
		
		return xquery_result;
	}
	
	public List<String> getAllVariablesFromXQuery(SimpleNode ast_xquery) {
		
		List<String> resultList = new ArrayList<String>();
		
		for(int i=0; i<ast_xquery.jjtGetNumChildren();i++) {
			SimpleNode child = (SimpleNode) ast_xquery.jjtGetChild(i);
			if(child instanceof ASTvar) {
				resultList.add(child.jjtGetFirstToken().toString().substring(1));
			}
			resultList.addAll(getAllVariablesFromXQuery(child));
		}
		
		return resultList;
		
	}
	
	public void evaluate_empty_or_letclause(SimpleNode ast_empty_or_letclause, HashMap<String,List<Node>> context ) throws Exception
	{
		if ( ast_empty_or_letclause.jjtGetNumChildren() == 0)
			return;
		else
		{
			int maxIter = ast_empty_or_letclause.jjtGetNumChildren() - (ast_empty_or_letclause.jjtGetNumChildren()%2);
			for ( int i = 0; i < maxIter; i+=2)
			{
				ASTvar var1 =  (ASTvar) ast_empty_or_letclause.jjtGetChild(i);
				ASTXQuery xquery1 =  (ASTXQuery) ast_empty_or_letclause.jjtGetChild(i+1);
				evaluate_varQuery(var1, xquery1, context);
				
			}
		}
		
		
	}
	
	public boolean evaluate_cond_paranthesis(ASTCond_paranthesis  ast_Cond_paranthesis, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode child = (ASTCond) ast_Cond_paranthesis.jjtGetChild(1);
		return evaluate_cond(   (ASTCond) child, context);
		
	}
	
	// Store result of match in matchingNodes
	public boolean evaluate_cond_identifier(ASTcond_identifier ast_cond_identifier, HashMap<String,List<Node>> context) throws Exception
	{		
		SimpleNode firstChild = ( SimpleNode) ast_cond_identifier.jjtGetChild(0);
		SimpleNode secondChild = (SimpleNode) ast_cond_identifier.jjtGetChild(1);
		SimpleNode thirdChild = (SimpleNode) ast_cond_identifier.jjtGetChild(2);
						
		if ( firstChild instanceof ASTXQuery)
		{
		
			if ( secondChild instanceof ASTeq_value)
			{
				boolean result = false;
				
				List<Node> query_result1 = evaluate_xquery( (ASTXQuery) firstChild, context);
				List<Node> query_result2 = evaluate_xquery( (ASTXQuery) thirdChild, context);
				
				for(Node query_result1_node : query_result1) {
					for(Node query_result2_node : query_result2) {
						if(query_result1_node.isEqualNode(query_result2_node)) {
							result = true;
						}
					}
				}
				
				return result;
			}
			else if ( secondChild instanceof ASTeq_id)
			{
				boolean result = false;
				
				List<Node> query_result1 = evaluate_xquery( (ASTXQuery) firstChild, context);
				List<Node> query_result2 = evaluate_xquery( (ASTXQuery) thirdChild, context);
				
				for(Node query_result1_node : query_result1) {
					for(Node query_result2_node : query_result2) {
						if(query_result1_node.isSameNode(query_result2_node)) {
							result = true;
						}
					}
				}
				
				return result;								
			}
			else
				throw new Exception(" expecting eq_id or eq_value!");
			
		}
		else if ( firstChild instanceof ASTempty_open)
		{
			if ( secondChild instanceof ASTXQuery)
			{
				return evaluate_xquery( (ASTXQuery) secondChild, context).isEmpty();
			}
			else
				throw new Exception( " Wrong type in cond_identifier, expecting CQuery");
		}
		else if ( firstChild instanceof ASTvar)
		{
			// TODO: double check this part
			int i;
			for ( i = 0; i < ast_cond_identifier.jjtGetNumChildren() / 2; i+=2)
			{
				if ( !(ast_cond_identifier.jjtGetChild(i) instanceof ASTCond)) 
				{
					ASTvar var1 = (ASTvar) ast_cond_identifier.jjtGetChild(i);
					ASTXQuery xquery1 = (ASTXQuery) ast_cond_identifier.jjtGetChild(i + 1);
					evaluate_varQuery(var1, xquery1, context);					
				} else {
					break;
				}
			}
				
			ASTCond cond = (ASTCond) ast_cond_identifier.jjtGetChild(i);
			return evaluate_cond( cond, context);			
		}
		else
			throw new Exception(" Child of cond_identifier was wrong type! ");				
	}
	
	public Object[]  evaluate_cond_logical(ASTcond_prime ast_cond_logical, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode firstChild = (SimpleNode) ast_cond_logical.jjtGetChild(0);
		SimpleNode secondChild = (SimpleNode) ast_cond_logical.jjtGetChild(1);
		Object[] result = new Object[2];
		
		if ( firstChild instanceof ASTand)
		{
			result[0] = oper.AND;
			result[1] = evaluate_cond((ASTCond) secondChild, context);
		}
		else if ( firstChild instanceof ASTor)
		{
			result[0] = oper.OR;
			result[1] = evaluate_cond((ASTCond) secondChild, context);
		}
		else if ( firstChild instanceof ASTnot)
		{
			result[0] = oper.NOT;
			result[1] = evaluate_cond((ASTCond) secondChild, context);
		}
		return result;
		
	}
	public Object[] evaluate_prime_list( List<Object[]> result_objects) throws Exception
	{
		oper first_enum = (oper) result_objects.get(0)[0];
		boolean first_boolean = (Boolean) result_objects.get(0)[1];
		
		oper second_enum = (oper) result_objects.get(1)[0];
		boolean second_boolean = (Boolean) result_objects.get(1)[1];
		
		oper current_enum = second_enum;
		boolean current_boolean =  evaluate_enum( first_boolean, second_boolean, first_enum);
		
		for ( int i = 2; i < result_objects.size(); i++)
		{
			oper ith_enum = (oper) result_objects.get(i)[0];
			boolean ith_boolean = (Boolean) result_objects.get(i)[1];
			current_boolean = evaluate_enum( current_boolean, ith_boolean, current_enum);
			current_enum = ith_enum;
		}
		Object[] returned_result = new Object [2];
		returned_result[0] = current_enum;
		returned_result[1] = current_boolean;
		return returned_result;
	}
	public Object[]  evaluate_cond_prime(ASTcond_prime ast_cond_prime, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode firstChild = (SimpleNode) ast_cond_prime.jjtGetChild(0);
		if ( firstChild instanceof ASTcond_logical)
		{
			Object[] result = evaluate_cond_logical( (ASTcond_prime) firstChild, context);
			boolean firstCond_result = (Boolean) result[1];
			oper firstCond_enum = (oper) result[0];
			if ( ast_cond_prime.jjtGetNumChildren() > 1 )
			{
				List<Object[]> result_objects = new ArrayList<Object[]>();
				for ( int i = ast_cond_prime.jjtGetNumChildren()-1; i > 0 ; i--)
				{
					Object[] for_result; 
					SimpleNode cond_prime =  (SimpleNode) ast_cond_prime.jjtGetChild(i);
					 for_result = evaluate_cond_prime((ASTcond_prime) cond_prime, context);
					 result_objects.add(for_result);
				}
				Object[] current_result = evaluate_prime_list(result_objects);
				boolean current_boolean = (Boolean) current_result[1];
				oper current_enum = (oper) current_result[0];
				
				current_boolean = evaluate_enum( current_boolean, firstCond_result, current_enum);
				Object[] result_returned = new Object[2];
				result_returned[0] = firstCond_enum;
				result_returned[1] = current_boolean;
				return result_returned;
			}
			else
				return result;
			
		}
		else
			throw new Exception(" Missing AST_cond_logical!!!");
		
	}
	
	public boolean evaluate_enum( boolean first_boolean, boolean second_boolean, oper first_enum) throws Exception
	{
		if ( first_enum.equals(oper.AND))
		{
			return first_boolean && second_boolean;
		}
		else if ( first_enum.equals(oper.OR))
		{
			return first_boolean || second_boolean;
		}
		else
			throw new Exception(" wrong operator, expecting and | or");
	}
	
	public boolean evaluate_cond(ASTCond ast_cond, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode child = (SimpleNode) ast_cond.jjtGetChild(0);
		boolean firstCond_result;
	    if ( child instanceof ASTcond_identifier)
	    {
	    	firstCond_result =  evaluate_cond_identifier((ASTcond_identifier) child, context);
	    }
	    else if ( child instanceof ASTCond_paranthesis)
	    {
	    	firstCond_result = evaluate_cond_paranthesis( (ASTCond_paranthesis) child, context);
	    }
	    else 
	    	throw new Exception(" Wrong type at Cond, expecting cond_identifier or cond_paranthesis");
	    
	    if ( ast_cond.jjtGetNumChildren() > 1)
	    {
	    	List<Object[]> result_objects = new ArrayList<Object[]>();
			for ( int i = ast_cond.jjtGetNumChildren()-1; i > 0 ; i--)
			{
				Object[] for_result; 
				SimpleNode cond_prime =  (SimpleNode) ast_cond.jjtGetChild(i);
				 for_result = evaluate_cond_prime((ASTcond_prime) cond_prime, context);
				 result_objects.add(for_result);
			}
			Object[] current_result = evaluate_prime_list(result_objects);
			boolean current_boolean = (Boolean) current_result[1];
			oper current_enum = (oper) current_result[0];
			
			current_boolean = evaluate_enum( current_boolean, firstCond_result, current_enum);
			
			return current_boolean;
	    }
	    else
	    	return firstCond_result;
		
	}
	
	public boolean evaluate_whereclause(ASTwhereclause ast_whereclause, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode cond = (SimpleNode) ast_whereclause.jjtGetChild(0);
	    return evaluate_cond( (ASTCond) cond, context) ;
	}
		
	/**
	 * Expects a context where each variable is bound to a single Node. It then evaluates the
	 * where and return clauses with respect to this context and returns the results
	 * 
	 * @param context
	 * @param whereClause
	 * @param returnClause
	 * @return
	 * @throws Exception
	 */
	List<Node> evaluate_xquery_where_return(HashMap<String, List<Node>> context, 
			ASTwhereclause whereClause, ASTreturnclause returnClause) throws Exception {
		
//		// Debug printouts
//		System.out.println("Reached dyanmicLoop stopping condition:");
//		for(String key : context.keySet()) {
//			System.out.print("Variable: " + key + " ");
//			//printResultList(context.get(key));
//			for(Node resultNode : context.get(key)) {
//				System.out.println(resultNode.getNodeName() + " ");
////				printNode(resultNode);
//			}
//			System.out.println();				
//		}
//		System.out.println("Done printing stopping point context");
//		// End debug printouts
				
		// Evaluate where clause in current context
		// current context only has one Node bound to each variable (simulating for loop) 
		if ( whereClause != null && whereClause.jjtGetNumChildren() > 0)
		{			
			// We have a where clause, check if it holds and output the results
			if ( evaluate_whereclause( whereClause, context))
			{				
				// Output results
				return evaluate_xquery( (ASTXQuery) returnClause.jjtGetChild(0), context);				
			}
			else
			{
				// Do not output any results
//				System.out.println("EMPTY/FALSE WHERE CLAUSE - not outputting results");
				return new ArrayList<Node>();									
			}		
		}
		else
		{
			// No where clause, just output results
			return evaluate_xquery( (ASTXQuery) returnClause.jjtGetChild(0), context);
		}			
	}
	
	// List of variable names, each one bound to a list of Nodes
	// for each one, get the list, set the 
	
	/**
	 * clauseVariables  - a list of all the variable names in the for clause
	 * context			- binding of variable names to lists of XML nodes
	 * depth 			- used to keep track internally, always starts at 0
	 * @throws Exception 
	 */
	public List<Node> for_clause_dynamic_loop(
			ASTforclause forClause, HashMap<String, List<Node>> context, 
			ASTwhereclause whereClause, ASTreturnclause returnClause, int counter) throws Exception {									
				
		List<Node> resultsToOutput = new ArrayList<Node>();
		
		if(counter >= forClause.jjtGetNumChildren()) {
			// Base case - evaluate the where/return clauses
			List<Node> partialResults = evaluate_xquery_where_return(context, whereClause, returnClause);
//			System.out.println("-------------------------- WHERE/RETURN PATIAL RESULTS ---------------------------");
//			printResultList(partialResults);
//			System.out.println("----------------------------------------------------------------------------------");
			resultsToOutput.addAll(partialResults);															
		} else {
			// Recursive case - keep building up contexts
			ASTvar var1 	  		= (ASTvar) forClause.jjtGetChild(counter);
			ASTXQuery xquery1 		= (ASTXQuery) forClause.jjtGetChild(counter + 1);							
			List<Node> innerNodes 	= evaluate_varQuery(var1, xquery1, context);
			
			String currentVariable	=  var1.jjtGetFirstToken().toString();
			HashMap<String, List<Node>> extendedContext = new HashMap<String,List<Node>>();
			extendedContext.putAll(context);
			
			for(Node innerNode : innerNodes) {
				List<Node> listWrapper = new ArrayList<Node>();
				listWrapper.add(innerNode);				
				extendedContext.put(currentVariable, listWrapper);								
				resultsToOutput.addAll(for_clause_dynamic_loop(forClause, extendedContext, whereClause, returnClause, counter + 2));				
			}			
		}
		
		return resultsToOutput;						
	}
	
	public List<Node>  evaluate_xquery_comma(SimpleNode ast_xquery_comma, HashMap<String,List<Node>> context , List<Node> results) throws Exception
	{
		SimpleNode firstChild = (SimpleNode) ast_xquery_comma.jjtGetChild(0);
		SimpleNode secondChild = (SimpleNode) ast_xquery_comma.jjtGetChild(1);
		if ( firstChild instanceof ASTcomma)
		{
			List<Node> return_list = new ArrayList<Node>();
			return_list.addAll(evaluate_xquery( (ASTXQuery) secondChild, context));
	        //System.out.println(" --------------------------SCENE SCENE SCNE");
			//printResultList(return_list); 
			results.addAll(return_list);
			return results;
		}
		else if ( firstChild instanceof ASTslash )
		{
			List<Node> all_rp_results = new ArrayList<Node>();
			for ( Node result : results)
			{
				List<Node> rp_results = new ArrayList<Node>();
				evaluate_rp( result, secondChild, rp_results, false);
				all_rp_results.addAll(rp_results);
			}
			return all_rp_results;
			// TODO : EVALUATE RP
			//evaluate_rp( secondChild, context);
		}
		else if (  firstChild instanceof ASTslashslash)
		{
			List<Node> all_rp_results = new ArrayList<Node>();
			for ( Node result : results)
			{
				List<Node> descendants = new ArrayList<Node>();
				descendants = XMLNavigator.descendantOrSelf(result);
				
				for ( Node descendant : descendants)
				{
					List<Node> descendant_results = new ArrayList<Node>();
					evaluate_rp( descendant, secondChild, descendant_results, false);
					all_rp_results.addAll( descendant_results);
				}	
				
			}
			return all_rp_results;
		}
		return null;
	}


	public List<Node>  evaluate_xquery_tagname(ASTxquery_tagname ast_tagname, HashMap<String,List<Node>> context ) throws Exception
	{
		SimpleNode start_tag =  (SimpleNode) ast_tagname.jjtGetChild(0);
		SimpleNode end_tag =  (SimpleNode) ast_tagname.jjtGetChild(2);
		SimpleNode xquery =  (SimpleNode) ast_tagname.jjtGetChild(1);
		
		String startTag = start_tag.jjtGetFirstToken().toString();
		String endTag = end_tag.jjtGetFirstToken().toString();
		
		
		if ( !startTag.equals(endTag))
			throw new Exception(" Mismatched tags : " + startTag + " - " + endTag);
		else
		{
			List<Node> xquery_list = evaluate_xquery((ASTXQuery) xquery, context);
			Node element_node = XMLNavigator.makeElem( startTag, xquery_list);
			List<Node> results = new ArrayList<Node>();
			results.add(element_node);
			return results;
		}
		
	}
	
	public List<Node>  evaluate_xquery_paranthesis(ASTxquery_paranthesis ast_paranthesis, HashMap<String,List<Node>> context ) throws Exception
	{
		ASTXQuery child = (ASTXQuery) ast_paranthesis.jjtGetChild(1);
		return evaluate_xquery(   child, context);
		
	}
	
	
	
	public List<Node> evaluate_xquery_identifier(ASTxquery_identifier ast_xquery_identifier, HashMap<String,List<Node>> context) throws Exception
	{
		SimpleNode child = (SimpleNode) ast_xquery_identifier.jjtGetChild(0);
		
		if (child instanceof ASTvar )
				return evaluate_var( (ASTvar) child, context);
		else if ( child instanceof ASTstringConstant)
				return evaluate_stringConstant( (ASTstringConstant)child, context);
		else if ( child instanceof ASTap)
				return evaluate_ap((ASTap) child);
		return null;
		
	}
	
	public List<Node>  evaluate_xquery_prime(ASTxquery_prime ast_xquery_prime, HashMap<String,List<Node>> context , List<Node> results) throws Exception
	{
		SimpleNode child = (SimpleNode) ast_xquery_prime.jjtGetChild(0);
		if ( child instanceof ASTxquery_comma)
		{
			List<Node> prime_results = new ArrayList<Node>();
			prime_results.addAll(evaluate_xquery_comma( (ASTxquery_comma) child, context, results));
			if ( ast_xquery_prime.jjtGetNumChildren() > 1 )
			{
				
				for ( int i = 1; i < ast_xquery_prime.jjtGetNumChildren(); i++)
				{
					SimpleNode xquery_prime =  (SimpleNode) ast_xquery_prime.jjtGetChild(i);
					prime_results.addAll(evaluate_xquery_prime((ASTxquery_prime) xquery_prime, context, results));
				}
				
				return prime_results;
			}
			else
			{
				return prime_results;
			}				
		}
		else if (  child instanceof ASTxquery_slash  )
		{
			List<Node> prime_results = new ArrayList<Node>();
			List<Node> deleted_results = new ArrayList<Node>();
			deleted_results.addAll(results);
			prime_results.addAll(evaluate_xquery_comma(  child, context, results));
			prime_results.removeAll(deleted_results);
			if ( ast_xquery_prime.jjtGetNumChildren() > 1 )
			{
				
				for ( int i = 1; i < ast_xquery_prime.jjtGetNumChildren(); i++)
				{
					SimpleNode xquery_prime =  (SimpleNode) ast_xquery_prime.jjtGetChild(i);
					prime_results.addAll(evaluate_xquery_prime((ASTxquery_prime) xquery_prime, context, results));
				}
				
				return prime_results;
			}
			else
			{
				return prime_results;
			}
		}
		else
			throw new Exception(" Child of XQuery_prime was not comma");
	}


	public List<Node> evaluate_xquery(ASTXQuery ast_xquery, HashMap<String,List<Node>> context) throws Exception
	{
		List<Node> results 	= new ArrayList<Node>();		
		SimpleNode child 	= (SimpleNode) ast_xquery.jjtGetChild(0);
		
		if (child instanceof ASTxquery_identifier )
			results.addAll(evaluate_xquery_identifier( (ASTxquery_identifier) child, context));
		else if ( child instanceof ASTxquery_paranthesis)
			results.addAll(evaluate_xquery_paranthesis( (ASTxquery_paranthesis)child, context));
		else if ( child instanceof ASTxquery_tagname)
			results.addAll(evaluate_xquery_tagname((ASTxquery_tagname) child, context));
		else if ( child instanceof ASTxquery_flwr)
			results.addAll(evaluate_xquery_flwr((ASTxquery_flwr) child, context));
		else if ( child instanceof ASTletclause)
			results.addAll(evaluate_letclause((ASTletclause) child, context));
		
		if (  ast_xquery.jjtGetNumChildren() > 1 )
		{
			SimpleNode secondChild = (SimpleNode) ast_xquery.jjtGetChild(1);
			if ( secondChild instanceof ASTxquery_prime)
			{
				List<Node> deleted_list = new ArrayList<Node>();
				deleted_list.addAll(results);
//				System.out.println(" DELETED LIST ");
//				printResultList(deleted_list);
				for ( int i = 1; i < ast_xquery.jjtGetNumChildren(); i++)
				{
					SimpleNode xquery_prime =  (SimpleNode) ast_xquery.jjtGetChild(i);					
					results.addAll(evaluate_xquery_prime((ASTxquery_prime) xquery_prime, context, results));					
				}
//				System.out.println(" RESULT LIST ");
//				printResultList(results);
				 
				SimpleNode slashChild = (SimpleNode) ast_xquery.jjtGetChild(1).jjtGetChild(0);
				
				if(slashChild instanceof ASTxquery_slash) {
					results.removeAll(deleted_list);
				}
			}
			else throw new Exception(" child of ASTXQuery is wrong type");
		}
		//else System.out.println(" simple xquery ");
		return removeDuplicates(results);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// XPATH EVALUATIONS
	
//	public void evaluate_XQuery(ASTXQuery xqNode) throws Exception {
//		if(xqNode.jjtGetNumChildren() >= 1) {
//			SimpleNode xqChild = (SimpleNode) xqNode.jjtGetChild(0);
//			if(xqChild instanceof ASTxquery_identifier) {
//				evaluate_xquery_identifier( (ASTxquery_identifier) xqChild);
//			}			
//		}
//	}
	
//	public void evaluate_xquery_identifier(ASTxquery_identifier xqId) throws Exception {
//		if(xqId.jjtGetNumChildren() >= 1) {
//			SimpleNode child = (SimpleNode) xqId.jjtGetChild(0);
//			if(child instanceof ASTap) {
//				evaluate_ap((ASTap) child);
//			}
//			
//		}
//	}
	
	/**
	 * The input for this function contains a complete XPath within its subnodes
	 * @throws Exception
	 */
	public List<Node> evaluate_ap(ASTap ap_node) throws Exception
	{		
		List<Node> result = new ArrayList<Node>();
		
		if (ap_node.jjtGetNumChildren() != 0) {
			SimpleNode child = (SimpleNode) ap_node.jjtGetChild(0);
			if (child != null && child instanceof ASTap_doc ) {				
				xml_root = evaluate_ap_doc( (ASTap_doc) child);
			}
			child = (SimpleNode) ap_node.jjtGetChild(1);
			if ( child.toString().equals("slash") )
			{
				List<Node> result_list = new ArrayList<Node>();
				//System.out.println(xml_root.getNodeName());
				evaluate_rp( xml_root, (SimpleNode) ap_node.jjtGetChild(2),result_list, true);
				result.addAll( result_list);
			} 
			else if (child.toString().equals("slashslash"))
			{
				List<Node> descendants = new ArrayList<Node>();
				System.out.println(xml_root.getNodeName());
				descendants = XMLNavigator.descendantOrSelf(xml_root);
				for ( Node descendant : descendants)
				{
					List<Node> descendant_results = new ArrayList<Node>();
					evaluate_rp( descendant, (SimpleNode) ap_node.jjtGetChild(2),descendant_results, true);
					result.addAll( descendant_results);
				}								
			}
			else
				throw new Exception(" no slashy after doc, something went wrong");			
		}
				
		//printResultList(result);
		
		return result;
	}

	public Node evaluate_ap_doc(ASTap_doc ap_doc) throws Exception
	{
		for (int i = 0; i < ap_doc.jjtGetNumChildren(); i++) {
			SimpleNode child = (SimpleNode) ap_doc.jjtGetChild(i);
			if (child != null && child.toString().equals("filename") ) {
				String fileName = child.jjtGetFirstToken().toString();
				fileName = fileName.substring(1, fileName.length() - 1);
				return XMLNavigator.root(fileName).getParentNode();				 
			}
		}
		throw new Exception("no filename, something went wrong");

	}
	
	public List<Node> evaluate_rp( Node xml_node, SimpleNode ast_rp, List<Node> result_list, boolean isRoot) throws Exception
	{
		List<Node> new_result_list = new ArrayList<Node>();
		if (  ast_rp.jjtGetChild(0).toString().equals("rp_identifier"))
		{
			SimpleNode rp_identifier = (SimpleNode)ast_rp.jjtGetChild(0);
		
			if ( rp_identifier.jjtGetNumChildren() >= 1 &&   rp_identifier.jjtGetChild(0).toString().equals("tagname"))
			{
				List<Node> matching_rp_list = tag_rp(xml_node, rp_identifier, isRoot);
				if (ast_rp.jjtGetNumChildren() >= 2) {
					SimpleNode rp_prime = (SimpleNode) ast_rp.jjtGetChild(1);
					
					if (rp_prime.toString().equals("rp_prime")) {
						for (Node matching_rp : matching_rp_list) {
							SimpleNode rp_slash = (SimpleNode) rp_prime
									.jjtGetChild(0);
							String slash_token = rp_slash.jjtGetChild(0).toString();
							if ( slash_token.equals("filter"))
							{
								System.out.println("FILTER");
								
							}
							else {
								SimpleNode rp_child = (SimpleNode) rp_slash
										.jjtGetChild(1);

								if (slash_token.equals("slash")) {
									evaluate_rp(matching_rp, rp_child,
											result_list, false);

								} else if (slash_token.equals("slashslash")) {
									System.out.println(" slashslash");
									List<Node> descendants = XMLNavigator
											.descendantOrSelf(matching_rp);
									for (Node descendant : descendants) {
										List<Node> descendant_results = new ArrayList<Node>();
										evaluate_rp(descendant, rp_child,
												descendant_results, false);
										result_list.addAll(descendant_results);

									}
								} else if (slash_token.equals("comma")) {
									List<Node> new_resultList = new ArrayList<Node>();
									evaluate_rp(xml_node, rp_child,
											new_resultList, true);
									if (!result_list
											.containsAll(new_resultList))
										result_list.addAll(new_resultList);
									if (!result_list
											.containsAll(matching_rp_list))
										result_list.addAll(matching_rp_list);
								}
							}
						}
					}
				}
				else
				{
					result_list.addAll(matching_rp_list);
					
				}
			}
			else if ( rp_identifier.jjtGetChild(0).toString().equals("star") )
				result_list.addAll(star_rp( xml_node, rp_identifier));
			else if ( rp_identifier.jjtGetChild(0).toString().equals("dot") )
				result_list.addAll( dot_rp( xml_node, rp_identifier));
			else if ( rp_identifier.jjtGetChild(0).toString().equals("dotdot") )
			{
				List<Node> parent_list = dotdot_rp( xml_node, rp_identifier);
				if( !result_list.contains(parent_list.get(0)))
					result_list.addAll( parent_list);
			}
			else if ( rp_identifier.jjtGetChild(0).toString().equals("text"))
			{			
				result_list.add(XMLNavigator.text(xml_node));
			}
			
			//System.out.println("evaluate_rp: rp_identifier: result list size: " +result_list.size());
		}
		else if ( ast_rp.jjtGetChild(0).toString().equals("rp_paranthesis"))
		{
			System.out.println(" rp " + ast_rp.jjtGetChild(0).jjtGetChild(1).toString());
			evaluate_rp( xml_node, (SimpleNode) ast_rp.jjtGetChild(0).jjtGetChild(1), result_list, isRoot);
			if (ast_rp.jjtGetNumChildren() >= 2) {
				SimpleNode rp_prime = (SimpleNode) ast_rp.jjtGetChild(1);
				if (rp_prime.toString().equals("rp_prime")) {
					SimpleNode rp_slash = (SimpleNode) rp_prime
					.jjtGetChild(0);
					SimpleNode rp_child = (SimpleNode) rp_slash
					.jjtGetChild(1);
					List<Node> old_results = new ArrayList<Node>();
					if ( result_list != null)
					{
						for (Node partial_result : result_list) {
							
							old_results.add( partial_result);
							evaluate_rp((Node)partial_result, rp_child, new_result_list,	false);
						}
						for (Object old_result : old_results) {
							result_list.remove(old_result);
							
						}
					}
				}
			}
		}
		
		result_list.addAll( new_result_list);
		
		return removeDuplicates(result_list);
	}
	
	public List<Node> tag_rp( Node xml_node, SimpleNode rp_identifier, boolean isRoot) throws Exception
	{		
		SimpleNode rp_tagname = (SimpleNode) rp_identifier.jjtGetChild(0);
		List<Node> tag_list = new ArrayList<Node>();
		String tagname = rp_tagname.jjtGetFirstToken().toString();

		List<Node> childrenOFxml = XMLNavigator.children(xml_node);
		for (Node child : childrenOFxml) {
			if (XMLNavigator.tag(child).equals(tagname))
				tag_list.add(child);
		}
			
		return tag_list;
	}
	
	public List<Node> star_rp( Node xml_node, SimpleNode rp_identifier) throws Exception
	{
		return XMLNavigator.children(xml_node);	
	}
	
	public List<Node> dot_rp( Node xml_node, SimpleNode rp_identifier) throws Exception
	{
		List<Node> singleton_node = new ArrayList<Node>();
		singleton_node.add(xml_node);
		return  singleton_node;	
	}
	
	public List<Node> dotdot_rp( Node xml_node, SimpleNode rp_identifier) throws Exception
	{
		return XMLNavigator.parent(xml_node);
	}
	
	/**************************************************************************
	 * 
	 *	Utility Functions
	 * 
	 *************************************************************************/
   	
	/**
  	 * Dumps the parse tree rooted at the specified node along with the tokens at each node.
  	 * @param n - node to start at
  	 * @param prefix - string to indicate depth of the current node in the tree
  	 */
	public static void dumpWithTokens(SimpleNode n, String prefix) {		
		System.out.println(n.toString(prefix) + " :" + n.jjtGetFirstToken().toString());
		if (n.jjtGetNumChildren() != 0) {
			for (int i = 0; i < n.jjtGetNumChildren(); ++i) {
				SimpleNode child = (SimpleNode) n.jjtGetChild(i);
				if (child != null) {
					dumpWithTokens(child, prefix + " ");
				}
			}
		}
	}
	
	/*
	 * Print out the result list
	 */
	public static void printResultList(List<Node> resultList) {
		
		System.out.println("Result list size: " + resultList.size());
		
		// Print out result list
		for(Node resultNode : resultList) {
			if ( resultNode instanceof Node) {
				printNode(resultNode);
			}			
			else
				System.out.println("Not a node: " + resultNode.toString());
		}				
	}
	
	public static void printNode(Node resultNode) {
		Node resultNodeN = (Node) resultNode;
		if( resultNodeN.getNodeType() == Node.ELEMENT_NODE  )
		{
			System.out.println(resultNodeN.getNodeName());
			XMLNavigator.printNonTextChildren( resultNode, " ");
		}
		else if(resultNodeN.getNodeType() == Node.TEXT_NODE) {
			System.out.println("\t" + resultNodeN.getNodeValue());
		}				

	}
	
	/**
	 * Removes duplicate nodes from the list. uses isSameNode as elimintation criteria
	 * @param resultList
	 * @return
	 */
	public List<Node> removeDuplicates(List<Node> resultList) {
		List<Node> cleanList = new ArrayList<Node>();
		for(Node dupListNode : resultList) {
			if(!cleanList.contains(dupListNode)) {
				cleanList.add(dupListNode);
			}
		}
		
		return cleanList;
	}
	
	public String createXMLDocFromNodeList(List<Node> nodeList)
	      throws javax.xml.parsers.ParserConfigurationException,
	             javax.xml.transform.TransformerException,
	             javax.xml.transform.TransformerConfigurationException{

	        DocumentBuilderFactory factory
	          = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        DOMImplementation impl = builder.getDOMImplementation();

	        Document doc = impl.createDocument(null,null,null);
	        
	        // Create the XML document
	        if(nodeList.size() == 1) {
	        	doc.appendChild(doc.importNode(nodeList.get(0), true));
	        } else {
	        	Element root = doc.createElementNS(null, "root");
		        for(Node childNode : nodeList) {
		        	root.appendChild(doc.importNode(childNode, true));
		        }	     
		        doc.appendChild(root);
	        }
	        	        
	        // transform the Document into a String
	        DOMSource domSource = new DOMSource(doc);
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        //transformer.setOutputProperty
	        //    (OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
	        transformer.setOutputProperty
	            ("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        java.io.StringWriter sw = new java.io.StringWriter();
	        StreamResult sr = new StreamResult(sw);
	        transformer.transform(domSource, sr);
	        String xml = sw.toString();
	        return xml;
	    }

	public String reconstructInput(SimpleNode node) {
		
		String output = "";
		Token token = node.jjtGetFirstToken();
		Token last_token = node.jjtGetLastToken();
		while(token != null && !token.equals(last_token)) {
			output += token.toString();
			token = token.next;
		}
		output = output + last_token.toString();
		return output;
	}	
}

