/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.query.resolver.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.teiid.api.exception.query.InvalidFunctionException;
import org.teiid.api.exception.query.QueryMetadataException;
import org.teiid.api.exception.query.QueryResolverException;
import org.teiid.api.exception.query.UnresolvedSymbolDescription;
import org.teiid.core.CoreConstants;
import org.teiid.core.TeiidComponentException;
import org.teiid.core.types.DataTypeManager;
import org.teiid.core.types.DataTypeManager.DefaultDataClasses;
import org.teiid.core.util.StringUtil;
import org.teiid.query.QueryPlugin;
import org.teiid.query.function.FunctionDescriptor;
import org.teiid.query.function.FunctionForm;
import org.teiid.query.function.FunctionLibrary;
import org.teiid.query.metadata.GroupInfo;
import org.teiid.query.metadata.QueryMetadataInterface;
import org.teiid.query.metadata.TempMetadataID;
import org.teiid.query.sql.LanguageObject;
import org.teiid.query.sql.LanguageVisitor;
import org.teiid.query.sql.lang.*;
import org.teiid.query.sql.navigator.PostOrderNavigator;
import org.teiid.query.sql.proc.ExceptionExpression;
import org.teiid.query.sql.symbol.*;
import org.teiid.query.sql.symbol.AggregateSymbol.Type;
import org.teiid.query.sql.symbol.ElementSymbol.DisplayMode;


public class ResolverVisitor extends LanguageVisitor {
    
    public static final String TEIID_PASS_THROUGH_TYPE = "teiid:pass-through-type"; //$NON-NLS-1$
	private static final String SYS_PREFIX = CoreConstants.SYSTEM_MODEL + '.';

    private static ThreadLocal<Boolean> determinePartialName = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
    public static void setFindShortName(boolean enabled) {
    	determinePartialName.set(enabled);
    }
    
    private Collection<GroupSymbol> groups;
    private GroupContext externalContext;
    protected QueryMetadataInterface metadata;
    private TeiidComponentException componentException;
    private QueryResolverException resolverException;
    private Map<Function, QueryResolverException> unresolvedFunctions;
    private boolean findShortName;
    private List<ElementSymbol> matches = new ArrayList<ElementSymbol>(2);
    private List<GroupSymbol> groupMatches = new ArrayList<GroupSymbol>(2);
    
    /**
     * Constructor for ResolveElementsVisitor.
     * 
     * External groups are ordered from inner to outer most
     */
    public ResolverVisitor(QueryMetadataInterface metadata, Collection<GroupSymbol> internalGroups, GroupContext externalContext) {
        this.groups = internalGroups;
        this.externalContext = externalContext;
        this.metadata = metadata;
        this.findShortName = determinePartialName.get();
    }

	public void setGroups(Collection<GroupSymbol> groups) {
		this.groups = groups;
	}

    public void visit(ElementSymbol obj) {
        try {
            resolveElementSymbol(obj);
        } catch(QueryMetadataException e) {
            handleException(handleUnresolvedElement(obj, e.getMessage()));
        } catch(TeiidComponentException e) {
            handleException(e);
        } catch (QueryResolverException e) {
			handleException(e);
		} 
    }

    private QueryResolverException handleUnresolvedElement(ElementSymbol symbol, String description) {
    	UnresolvedSymbolDescription usd = new UnresolvedSymbolDescription(symbol.toString(), description);
        QueryResolverException e = new QueryResolverException(usd.getDescription());
        e.setUnresolvedSymbols(Arrays.asList(usd));
        return e;
    }

    private void resolveElementSymbol(ElementSymbol elementSymbol)
        throws TeiidComponentException, QueryResolverException {

        // already resolved
        if(elementSymbol.getMetadataID() != null) {
        	return;
        }
        
        // look up group and element parts of the potentialID
        String groupContext = null;
        if (elementSymbol.getGroupSymbol() != null) {
        	groupContext = elementSymbol.getGroupSymbol().getName();
        }
        String elementShortName = elementSymbol.getShortName();
        if (groupContext != null) {
            groupContext = elementSymbol.getGroupSymbol().getName();
        	try {
				if (findShortName && internalResolveElementSymbol(elementSymbol, null, elementShortName, groupContext)) {
		    		elementSymbol.setDisplayMode(DisplayMode.SHORT_OUTPUT_NAME);
		    		return;
				}
			} catch (QueryResolverException e) {
				//ignore
			} catch (QueryMetadataException e) {
				//ignore
			}
        }
        
        internalResolveElementSymbol(elementSymbol, groupContext, elementShortName, null);
   }

	private boolean internalResolveElementSymbol(ElementSymbol elementSymbol,
			String groupContext, String shortCanonicalName, String expectedGroupContext)
			throws TeiidComponentException, QueryResolverException {
		boolean isExternal = false;
        boolean groupMatched = false;
        
        GroupContext root = null;
        
        if (groups != null || externalContext != null) {
            if (groups != null) {
                root = new GroupContext(externalContext, groups);
            }
            if (root == null) {
                isExternal = true;
                root = externalContext;
            }
        } else {
            try {
                LinkedList<GroupSymbol> matchedGroups = new LinkedList<GroupSymbol>();
                
                if (groupContext != null) {
                    //assume that this is fully qualified
                    Object groupID = this.metadata.getGroupID(groupContext);
                    // No groups specified, so any group is valid
                    GroupSymbol groupSymbol = new GroupSymbol(groupContext);
                    groupSymbol.setMetadataID(groupID);
                    matchedGroups.add(groupSymbol);
                }
                
                root = new GroupContext(null, matchedGroups);
            } catch(QueryMetadataException e) {
                // ignore 
            }
        }
        
        matches.clear();
        groupMatches.clear();
        while (root != null) {
            Collection<GroupSymbol> matchedGroups = ResolverUtil.findMatchingGroups(groupContext, root.getGroups(), metadata);
            if (matchedGroups != null && !matchedGroups.isEmpty()) {
                groupMatched = true;
                    
                resolveAgainstGroups(shortCanonicalName, matchedGroups);
                
                if (matches.size() > 1) {
            	    throw handleUnresolvedElement(elementSymbol, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31117, elementSymbol, groupMatches));
                }
                
                if (matches.size() == 1) {
                    break;
                }
            }
            
            root = root.getParent();
            isExternal = true;
        }
        
        if (matches.isEmpty()) {
            if (groupMatched) {
                throw handleUnresolvedElement(elementSymbol, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31118, elementSymbol)); 
            }
            throw handleUnresolvedElement(elementSymbol, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31119, elementSymbol)); 
        }
        //copy the match information
        ElementSymbol resolvedSymbol = matches.get(0);
        GroupSymbol resolvedGroup = groupMatches.get(0);
        String oldName = elementSymbol.getOutputName();
        if (expectedGroupContext != null && !ResolverUtil.nameMatchesGroup(expectedGroupContext, resolvedGroup.getName())) {
        	return false;
        }
        elementSymbol.setIsExternalReference(isExternal);
        elementSymbol.setType(resolvedSymbol.getType());
        elementSymbol.setMetadataID(resolvedSymbol.getMetadataID());
        elementSymbol.setGroupSymbol(resolvedGroup);
        elementSymbol.setShortName(resolvedSymbol.getShortName());
        elementSymbol.setOutputName(oldName);
        return true;
	}
    
    private void resolveAgainstGroups(String elementShortName,
                                      Collection<GroupSymbol> matchedGroups) throws QueryMetadataException,
                                                         TeiidComponentException {
    	for (GroupSymbol group : matchedGroups) {
            GroupInfo groupInfo = ResolverUtil.getGroupInfo(group, metadata);
            
            ElementSymbol result = groupInfo.getSymbol(elementShortName);
            if (result != null) {
            	matches.add(result);
            	groupMatches.add(group);
            }
        }
    }
        
    public void visit(BetweenCriteria obj) {
        try {
            resolveBetweenCriteria(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        } catch(TeiidComponentException e) {
            handleException(e);
        }
    }

    public void visit(CompareCriteria obj) {
        try {
            resolveCompareCriteria(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }

    public void visit(MatchCriteria obj) {
        try {
            resolveMatchCriteria(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }

    public void visit(SetCriteria obj) {
        try {
            resolveSetCriteria(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }

    public void visit(SubqueryCompareCriteria obj) {
        try {
            obj.setLeftExpression(ResolverUtil.resolveSubqueryPredicateCriteria(obj.getLeftExpression(), obj, metadata));
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }

    public void visit(SubquerySetCriteria obj) {
        try {
            obj.setExpression(ResolverUtil.resolveSubqueryPredicateCriteria(obj.getExpression(), obj, metadata));
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }

    public void visit(IsNullCriteria obj) {
        try {
        	setDesiredType(obj.getExpression(), DefaultDataClasses.OBJECT, obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }
    
    public void visit(Function obj) {
        try {
            resolveFunction(obj, this.metadata.getFunctionLibrary());
        } catch(QueryResolverException e) {
        	if (QueryPlugin.Event.TEIID30069.name().equals(e.getCode()) || QueryPlugin.Event.TEIID30067.name().equals(e.getCode())) {
	        	if (unresolvedFunctions == null) {
	        		unresolvedFunctions = new LinkedHashMap<Function, QueryResolverException>();
	        	}
	        	unresolvedFunctions.put(obj, e);
        	} else {
        		handleException(e);
        	}
        } catch(TeiidComponentException e) {
            handleException(e);
        }
    }
    
    @Override
    public void visit(Array array) {
    	try {
	    	if (array.getComponentType() != null) {
	    		String type = DataTypeManager.getDataTypeName(array.getComponentType());
	    		for (int i = 0; i < array.getExpressions().size(); i++) {
	    			Expression expr = array.getExpressions().get(i);
	    			setDesiredType(expr, array.getComponentType(), array);
	    			array.getExpressions().set(i, ResolverUtil.convertExpression(expr, type, metadata));
	    		}
	    	} else {
	    		String[] types = new String[array.getExpressions().size()];
	    		for (int i = 0; i < array.getExpressions().size(); i++) {
	    			Expression expr = array.getExpressions().get(i);
	    			types[i] = DataTypeManager.getDataTypeName(expr.getType());
	    		}
	    		String commonType = ResolverUtil.getCommonType(types);
	    		array.setComponentType(DataTypeManager.getDataTypeClass(commonType));
	    	}
    	} catch (QueryResolverException e) {
    		handleException(e);
    	}
    }

    public void visit(CaseExpression obj) {
        try {
            resolveCaseExpression(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }
    
    public void visit(SearchedCaseExpression obj) {
        try {
            resolveSearchedCaseExpression(obj);
        } catch(QueryResolverException e) {
            handleException(e);
        }
    }
    
    public void visit(SetClause obj) {
    	String type = DataTypeManager.getDataTypeName(obj.getSymbol().getType());
    	try {
    		setDesiredType(obj.getValue(), obj.getSymbol().getType(), obj);
            obj.setValue(ResolverUtil.convertExpression(obj.getValue(), type, metadata));                    
        } catch(QueryResolverException e) {
            handleException(new QueryResolverException(e, QueryPlugin.Util.getString("SetClause.resolvingError", new Object[] {obj.getValue(), obj.getSymbol(), type}))); //$NON-NLS-1$
        } 
    }
    
    @Override
    public void visit(XMLSerialize obj) {
    	try {
			obj.setExpression(ResolverUtil.convertExpression(obj.getExpression(), DataTypeManager.DefaultDataTypes.XML, metadata));
		} catch (QueryResolverException e) {
			handleException(new QueryResolverException(e, QueryPlugin.Util.getString("XMLSerialize.resolvingError", obj))); //$NON-NLS-1$
		}
    }
    
    @Override
    public void visit(XMLQuery obj) {
    	try {
	    	ResolverUtil.setDesiredType(obj.getPassing(), obj);
			obj.compileXqueryExpression();
		} catch (QueryResolverException e) {
			handleException(e); 
		}
    }
    
    @Override
    public void visit(QueryString obj) {
    	try {
			obj.setPath(ResolverUtil.convertExpression(obj.getPath(), DataTypeManager.DefaultDataTypes.STRING, metadata));
			for (DerivedColumn col : obj.getArgs()) {
				col.setExpression(ResolverUtil.convertExpression(col.getExpression(), DataTypeManager.DefaultDataTypes.STRING, metadata));
			}
		} catch (QueryResolverException e) {
			handleException(new QueryResolverException(e, QueryPlugin.Util.getString("XMLQuery.resolvingError", obj))); //$NON-NLS-1$
		}
    }
    
    @Override
    public void visit(ExpressionCriteria obj) {
		try {
			obj.setExpression(ResolverUtil.convertExpression(obj.getExpression(), DataTypeManager.DefaultDataTypes.BOOLEAN, metadata));
		} catch (QueryResolverException e) {
			handleException(e);
		}
    }
    
    @Override
    public void visit(ExceptionExpression obj) {
    	try {
    		if (obj.getErrorCode() != null) {
    			obj.setErrorCode(ResolverUtil.convertExpression(obj.getErrorCode(), DataTypeManager.DefaultDataTypes.INTEGER, metadata));
    		}
			obj.setMessage(ResolverUtil.convertExpression(obj.getMessage(), DataTypeManager.DefaultDataTypes.STRING, metadata));
			if (obj.getSqlState() != null) {
				obj.setSqlState(ResolverUtil.convertExpression(obj.getSqlState(), DataTypeManager.DefaultDataTypes.STRING, metadata));
			}
			checkException(obj.getParent());
		} catch (QueryResolverException e) {
			handleException(e);
		}
    }

	public static void checkException(Expression obj)
			throws QueryResolverException {
		if (obj == null || obj instanceof ExceptionExpression) {
			return;
		}
		if (obj instanceof ElementSymbol) {
			ElementSymbol es = (ElementSymbol)obj;
			if (!(es.getMetadataID() instanceof TempMetadataID)) {
				throw new QueryResolverException(QueryPlugin.Event.TEIID31120, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31120, obj));
			}
			TempMetadataID tid = (TempMetadataID)es.getMetadataID();
			if (tid.getType() != Exception.class) {
				throw new QueryResolverException(QueryPlugin.Event.TEIID31120, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31120, obj));
			}
		} else if (obj instanceof Constant) {
			Constant c = (Constant)obj;
			if (!(c.getValue() instanceof Exception)) {
				throw new QueryResolverException(QueryPlugin.Event.TEIID31120, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31120, obj));
			}
		} else {
			throw new QueryResolverException(QueryPlugin.Event.TEIID31120, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID31120, obj));
		}
	}
    
    @Override
    public void visit(AggregateSymbol obj) {
    	if (obj.getCondition() != null) {
			try {
				obj.setCondition(ResolverUtil.convertExpression(obj.getCondition(), DataTypeManager.DefaultDataTypes.BOOLEAN, metadata));
			} catch (QueryResolverException e) {
				handleException(e);
			}
    	}
    	if (obj.getAggregateFunction() == Type.USER_DEFINED) {
    		visit((Function)obj);
    	}
    }

    public TeiidComponentException getComponentException() {
        return this.componentException;
    }

    public QueryResolverException getResolverException() {
        return this.resolverException;
    }

    void handleException(TeiidComponentException e) {
        this.componentException = e;

        // Abort the validation process
        setAbort(true);
    }

    void handleException(QueryResolverException e) {
        this.resolverException = e;

        // Abort the validation process
        setAbort(true);
    }

	public void throwException(boolean includeUnresolvedFunctions)
			throws TeiidComponentException, QueryResolverException {
		if(getComponentException() != null) {
            throw getComponentException();
        }

        if(getResolverException() != null) {
            throw getResolverException();
        }
        
        if (includeUnresolvedFunctions 
        		&& unresolvedFunctions != null && !unresolvedFunctions.isEmpty()) {
        	throw unresolvedFunctions.values().iterator().next();
        }
	}

	/**
	 * Resolve function such that all functions are resolved and type-safe.
	 */
	void resolveFunction(Function function, FunctionLibrary library)
	    throws QueryResolverException, TeiidComponentException {
	
	    // Check whether this function is already resolved
	    if(function.getFunctionDescriptor() != null) {
	        return;
	    }
	
	    // Look up types for all args
	    boolean hasArgWithoutType = false;
	    Expression[] args = function.getArgs();
	    Class<?>[] types = new Class[args.length];
	    for(int i=0; i<args.length; i++) {
	        types[i] = args[i].getType();
	        if(types[i] == null) {
	        	if(!(args[i] instanceof Reference)){
	                 throw new QueryResolverException(QueryPlugin.Event.TEIID30067, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30067, new Object[] {args[i], function}));
	        	}
	            hasArgWithoutType = true;
	        }
	    }
	
	    //special case handling for convert of an untyped reference
	    if (FunctionLibrary.isConvert(function) && hasArgWithoutType) {
	        Constant constant = (Constant)function.getArg(1);
	        Class<?> type = DataTypeManager.getDataTypeClass((String)constant.getValue());
	
	        setDesiredType(function.getArg(0), type, function);
	        types[0] = type;
	        hasArgWithoutType = false;
	    }
	
	    // Attempt to get exact match of function for this signature
	    FunctionDescriptor fd = findWithImplicitConversions(library, function, args, types, hasArgWithoutType);
	    
	    // Function did not resolve - determine reason and throw exception
	    if(fd == null) {
	        FunctionForm form = library.findFunctionForm(function.getName(), args.length);
	        if(form == null) {
	            // Unknown function form
	             throw new QueryResolverException(QueryPlugin.Event.TEIID30068, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30068, function));
	        }
	        // Known function form - but without type information
	        if (hasArgWithoutType) {
	             throw new QueryResolverException(QueryPlugin.Event.TEIID30069, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30069, function));
	        }
	        // Known function form - unable to find implicit conversions
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30070, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30070, function));
	    }
	    
	    if (fd.getMethod().isVarArgs() 
	    		&& fd.getTypes().length == types.length 
	    		&& library.isVarArgArrayParam(fd.getMethod(), types, types.length - 1, fd.getTypes()[types.length - 1])) {
	    	fd = fd.clone();
	    	fd.setCalledWithVarArgArrayParam(true);
	    }
	    
	    if(fd.isSystemFunction(FunctionLibrary.CONVERT) || fd.isSystemFunction(FunctionLibrary.CAST)) {
	        String dataType = (String) ((Constant)args[1]).getValue();
	        Class<?> dataTypeClass = DataTypeManager.getDataTypeClass(dataType);
	        fd = library.findTypedConversionFunction(args[0].getType(), dataTypeClass);
	
	        // Verify that the type conversion from src to type is even valid
	        Class<?> srcTypeClass = args[0].getType();
	        if(srcTypeClass != null && dataTypeClass != null &&
	           !srcTypeClass.equals(dataTypeClass) &&
	           !DataTypeManager.isTransformable(srcTypeClass, dataTypeClass)) {
	
	             throw new QueryResolverException(QueryPlugin.Event.TEIID30071, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30071, new Object[] {DataTypeManager.getDataTypeName(srcTypeClass), dataType}));
	        }
	    } else if(fd.isSystemFunction(FunctionLibrary.LOOKUP)) {
			ResolverUtil.ResolvedLookup lookup = ResolverUtil.resolveLookup(function, metadata);
			fd = library.copyFunctionChangeReturnType(fd, lookup.getReturnElement().getType());
	    } else if (fd.isSystemFunction(FunctionLibrary.ARRAY_GET) && args[0].getType().isArray()) {
	    	//hack to use typed array values
			fd = library.copyFunctionChangeReturnType(fd, args[0].getType().getComponentType());
	    } else if (Boolean.valueOf(fd.getMethod().getProperty(TEIID_PASS_THROUGH_TYPE, false))) {
	    	//hack largely to support pg
	    	fd = library.copyFunctionChangeReturnType(fd, args[0].getType());
	    }
	
	    function.setFunctionDescriptor(fd);
	    function.setType(fd.getReturnType());
	    if (CoreConstants.SYSTEM_MODEL.equals(fd.getSchema()) && StringUtil.startsWithIgnoreCase(function.getName(), SYS_PREFIX)) {
	    	function.setName(function.getName().substring(SYS_PREFIX.length()));
	    }
	}

	/**
	 * Find possible matches based on implicit conversions of the arguments.
	 * NOTE: This method has the side-effect of explicitly inserting conversions into the function arguments,
	 * and thereby changing the structure of the function call.
	 * @param library
	 * @param function
	 * @param types
	 * @return
	 * @throws TeiidComponentException 
	 * @since 4.3
	 */
	private FunctionDescriptor findWithImplicitConversions(FunctionLibrary library, Function function, Expression[] args, Class<?>[] types, boolean hasArgWithoutType) throws QueryResolverException, TeiidComponentException {
	    
	    // Try to find implicit conversion path to still perform this function
	    FunctionDescriptor[] conversions;
		try {
			conversions = library.determineNecessaryConversions(function.getName(), function.getType(), args, types, hasArgWithoutType);
		} catch (InvalidFunctionException e) {
			return null;
		}
		Class<?>[] newSignature = types;
	    
	    if(conversions != null) {
		    newSignature = new Class[conversions.length];
		    // Insert new conversion functions as necessary, while building new signature
		    for(int i=0; i<conversions.length; i++) {
		        
		        Class<?> newType = types[i];
		        
		        if(conversions[i] != null) {
		            newType = conversions[i].getReturnType();
		            
		            setDesiredType(args[i], newType, function);
		                                
		            //only currently typed expressions need conversions
		            if (types[i] != null && newType != DataTypeManager.DefaultDataClasses.OBJECT) {
		                function.insertConversion(i, conversions[i]);
		            }
		        } 
		                    
		        newSignature[i] = newType;
		    }
	    }
	
	    // Now resolve using the new signature to get the function's descriptor
	    return library.findFunction(function.getName(), newSignature);
	}

	/**
	 * Resolves criteria "a BETWEEN b AND c". If type conversions are necessary,
	 * this method attempts the following implicit conversions:
	 * <br/>
	 * <ol type="1" start="1">
	 *   <li>convert the lower and upper expressions to the criteria expression's type, or</li>
	 *   <li>convert the criteria and upper expressions to the lower expression's type, or</li>
	 *   <li>convert the criteria and lower expressions to the upper expression's type, or</li>
	 *   <li>convert all expressions to a common type to which all three expressions' types can be implicitly converted.</li>
	 * </ol>
	 * @param criteria
	 * @throws QueryResolverException
	 * @throws TeiidComponentException 
	 * @throws TeiidComponentException
	 */
	void resolveBetweenCriteria(BetweenCriteria criteria)
	    throws QueryResolverException, TeiidComponentException {
	
	    Expression exp = criteria.getExpression();
	    Expression lower = criteria.getLowerExpression();
	    Expression upper = criteria.getUpperExpression();
	
	    // invariants: none of the expressions is an aggregate symbol
	    setDesiredType(exp,
	                                   (lower.getType() == null)
	                                        ? upper.getType()
	                                        : lower.getType(), criteria);
	    // invariants: exp.getType() != null
	    setDesiredType(lower, exp.getType(), criteria);
	    setDesiredType(upper, exp.getType(), criteria);
	    // invariants: none of the types is null
	
	    String expTypeName = DataTypeManager.getDataTypeName(exp.getType());
	    String lowerTypeName = DataTypeManager.getDataTypeName(lower.getType());
	    String upperTypeName = DataTypeManager.getDataTypeName(upper.getType());
	    if (exp.getType().equals(lower.getType()) && exp.getType().equals(upper.getType())) {
	        return;
	    }
	
	    String commonType = ResolverUtil.getCommonType(new String[] {expTypeName, lowerTypeName, upperTypeName});
	    if (commonType != null) {
	        criteria.setExpression(ResolverUtil.convertExpression(exp, expTypeName, commonType, metadata));
	        criteria.setLowerExpression(ResolverUtil.convertExpression(lower, lowerTypeName, commonType, metadata));
	        criteria.setUpperExpression(ResolverUtil.convertExpression(upper, upperTypeName, commonType, metadata));
	    } else {
	        // Couldn't find a common type to implicitly convert to
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30072, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30072, expTypeName, lowerTypeName, criteria));
	    }
	    // invariants: exp.getType() == lower.getType() == upper.getType()
	}

	void resolveCompareCriteria(CompareCriteria ccrit)
		throws QueryResolverException {
	
		Expression leftExpression = ccrit.getLeftExpression();
		Expression rightExpression = ccrit.getRightExpression();
	
		// Check typing between expressions
	    setDesiredType(leftExpression, rightExpression.getType(), ccrit);
	    setDesiredType(rightExpression, leftExpression.getType(), ccrit);
	
		if(leftExpression.getType().equals(rightExpression.getType()) ) {
			return;
		}
	
		// Try to apply an implicit conversion from one side to the other
		String leftTypeName = DataTypeManager.getDataTypeName(leftExpression.getType());
		String rightTypeName = DataTypeManager.getDataTypeName(rightExpression.getType());
	
	    // Special cases when right expression is a constant
	    if(rightExpression instanceof Constant) {
	        // Auto-convert constant string on right to expected type on left
	        try {
	            ccrit.setRightExpression(ResolverUtil.convertExpression(rightExpression, rightTypeName, leftTypeName, metadata));
	            return;
	        } catch (QueryResolverException qre) {
	            //ignore
	        }
	    } 
	    
	    // Special cases when left expression is a constant
	    if(leftExpression instanceof Constant) {
	        // Auto-convert constant string on left to expected type on right
	        try {
	            ccrit.setLeftExpression(ResolverUtil.convertExpression(leftExpression, leftTypeName, rightTypeName, metadata));
	            return;                                           
	        } catch (QueryResolverException qre) {
	            //ignore
	        }
	    }
	
	    // Try to apply a conversion generically
		
	    if(ResolverUtil.canImplicitlyConvert(leftTypeName, rightTypeName)) {
			ccrit.setLeftExpression(ResolverUtil.convertExpression(leftExpression, leftTypeName, rightTypeName, metadata) );
			return;
		}
	
		if(ResolverUtil.canImplicitlyConvert(rightTypeName, leftTypeName)) {
			ccrit.setRightExpression(ResolverUtil.convertExpression(rightExpression, rightTypeName, leftTypeName, metadata) );
			return;
	    }
	
		String commonType = ResolverUtil.getCommonType(new String[] {leftTypeName, rightTypeName});
		
		if (commonType == null) {
	        // Neither are aggs, but types can't be reconciled
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30072, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30072, new Object[] { leftTypeName, rightTypeName, ccrit }));
		}
		ccrit.setLeftExpression(ResolverUtil.convertExpression(leftExpression, leftTypeName, commonType, metadata) );
		ccrit.setRightExpression(ResolverUtil.convertExpression(rightExpression, rightTypeName, commonType, metadata) );
	}

	void resolveMatchCriteria(MatchCriteria mcrit)
	    throws QueryResolverException {
	
	    setDesiredType(mcrit.getLeftExpression(), mcrit.getRightExpression().getType(), mcrit);
	    mcrit.setLeftExpression(resolveMatchCriteriaExpression(mcrit, mcrit.getLeftExpression()));
	
	    setDesiredType(mcrit.getRightExpression(), mcrit.getLeftExpression().getType(), mcrit);
	    mcrit.setRightExpression(resolveMatchCriteriaExpression(mcrit, mcrit.getRightExpression()));
	}

	/**
	 * Checks one side of a LIKE Criteria; implicitly converts to a String or CLOB if necessary.
	 * @param mcrit the Match Criteria
	 * @param expr either left or right expression
	 * @return either 'expr' itself, or a new implicit type conversion wrapping expr
	 * @throws QueryResolverException if no implicit type conversion is available
	 */
	Expression resolveMatchCriteriaExpression(MatchCriteria mcrit, Expression expr)
	throws QueryResolverException {
	    // Check left expression == string or CLOB
	    String type = DataTypeManager.getDataTypeName(expr.getType());
	    Expression result = expr;
	    if(type != null) {
	        if (! type.equals(DataTypeManager.DefaultDataTypes.STRING) &&
	            ! type.equals(DataTypeManager.DefaultDataTypes.CLOB)) {
	                
	            if(ResolverUtil.canImplicitlyConvert(type, DataTypeManager.DefaultDataTypes.STRING)) {
	
	                result = ResolverUtil.convertExpression(expr, type, DataTypeManager.DefaultDataTypes.STRING, metadata);
	                
	            } else if (ResolverUtil.canImplicitlyConvert(type, DataTypeManager.DefaultDataTypes.CLOB)){
	                    
	                result = ResolverUtil.convertExpression(expr, type, DataTypeManager.DefaultDataTypes.CLOB, metadata);
	
	            } else {
	                 throw new QueryResolverException(QueryPlugin.Event.TEIID30074, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30074, mcrit));
	            }
	        }
	    }
	    return result;
	}

	void resolveSetCriteria(SetCriteria scrit)
	    throws QueryResolverException {
	
	    // Check that each of the values are the same type as expression
	    Class<?> exprType = scrit.getExpression().getType();
	    if(exprType == null) {
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30075, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30075, scrit.getExpression()));
	    }
	
	    String exprTypeName = DataTypeManager.getDataTypeName(exprType);
	    boolean changed = false;
	    List<Expression> newVals = new ArrayList<Expression>();
	
	    boolean convertLeft = false;
	    Class<?> setType = null;
	
	    Iterator valIter = scrit.getValues().iterator();
	    while(valIter.hasNext()) {
	        Expression value = (Expression) valIter.next();
	        setDesiredType(value, exprType, scrit);
	        if(! value.getType().equals(exprType)) {
	            // try to apply cast
	            String valTypeName = DataTypeManager.getDataTypeName(value.getType());
	            if(ResolverUtil.canImplicitlyConvert(valTypeName, exprTypeName)) {
	                // Apply cast and replace current value
	                newVals.add(ResolverUtil.convertExpression(value, valTypeName, exprTypeName, metadata) );
	                changed = true;
	            } else {
	                convertLeft = true;
	                setType = value.getType();
	                break;
	            }
	        } else {
	            newVals.add(value);
	        }
	    }
	
	    // If no convert found for first element, check whether everything in the
	    // set is the same and the convert can be placed on the left side
	    if(convertLeft) {
	        // Is there a possible conversion from left to right?
	        String setTypeName = DataTypeManager.getDataTypeName(setType);
	        if(ResolverUtil.canImplicitlyConvert(exprTypeName, setTypeName)) {
	            valIter = scrit.getValues().iterator();
	            while(valIter.hasNext()) {
	                Expression value = (Expression) valIter.next();
	                if(value.getType() == null) {
	                     throw new QueryResolverException(QueryPlugin.Event.TEIID30075, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30075, value));
	                } else if(! value.getType().equals(setType)) {
	                     throw new QueryResolverException(QueryPlugin.Event.TEIID30077, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30077, scrit));
	                }
	            }
	
	            // Convert left expression to type of values in the set
	            scrit.setExpression(ResolverUtil.convertExpression(scrit.getExpression(), exprTypeName, setTypeName, metadata));
	
	        } else {
	             throw new QueryResolverException(QueryPlugin.Event.TEIID30077, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30077, scrit));
	        }
	    }
	
	    if(changed) {
	        scrit.setValues(newVals);
	    }
	}

	void resolveCaseExpression(CaseExpression obj) throws QueryResolverException {
	    // If already resolved, do nothing
	    if (obj.getType() != null) {
	        return;
	    }
	    final int whenCount = obj.getWhenCount();
	    Expression expr = obj.getExpression();
	
	    Class<?> whenType = null;
	    Class<?> thenType = null;
	    // Get the WHEN and THEN types, and get a candidate type for each (for the next step)
	    for (int i = 0; i < whenCount; i++) {
	        if (whenType == null) {
	            whenType = obj.getWhenExpression(i).getType();
	        }
	        if (thenType == null) {
	            thenType = obj.getThenExpression(i).getType();
	        }
	    }
	
	    Expression elseExpr = obj.getElseExpression();
	    if (elseExpr != null) {
	        if (thenType == null) {
	            thenType = elseExpr.getType();
	        }
	    }
	    // Invariant: All the expressions contained in the obj are resolved (except References)
	
	    // 2. Attempt to set the target types of all contained expressions,
	    //    and collect their type names for the next step
	    ArrayList<String> whenTypeNames = new ArrayList<String>(whenCount + 1);
	    ArrayList<String> thenTypeNames = new ArrayList<String>(whenCount + 1);
	    setDesiredType(expr, whenType, obj);
	    // Add the expression's type to the WHEN types
	    whenTypeNames.add(DataTypeManager.getDataTypeName(expr.getType()));
	    Expression when = null;
	    Expression then = null;
	    // Set the types of the WHEN and THEN parts
	    for (int i = 0; i < whenCount; i++) {
	        when = obj.getWhenExpression(i);
	        then = obj.getThenExpression(i);
	
	        setDesiredType(when, expr.getType(), obj);
	        setDesiredType(then, thenType, obj);
	
	        if (!whenTypeNames.contains(DataTypeManager.getDataTypeName(when.getType()))) {
	            whenTypeNames.add(DataTypeManager.getDataTypeName(when.getType()));
	        }
	        if (!thenTypeNames.contains(DataTypeManager.getDataTypeName(then.getType()))) {
	            thenTypeNames.add(DataTypeManager.getDataTypeName(then.getType()));
	        }
	    }
	    // Set the type of the else expression
	    if (elseExpr != null) {
	        setDesiredType(elseExpr, thenType, obj);
	        if (!thenTypeNames.contains(DataTypeManager.getDataTypeName(elseExpr.getType()))) {
	            thenTypeNames.add(DataTypeManager.getDataTypeName(elseExpr.getType()));
	        }
	    }
	
	    // Invariants: all the expressions' types are non-null
	
	    // 3. Perform implicit type conversions
	    String whenTypeName = ResolverUtil.getCommonType(whenTypeNames.toArray(new String[whenTypeNames.size()]));
	    if (whenTypeName == null) {
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30079, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30079, "WHEN", obj));//$NON-NLS-1$
	    }
	    String thenTypeName = ResolverUtil.getCommonType(thenTypeNames.toArray(new String[thenTypeNames.size()]));
	    if (thenTypeName == null) {
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30079, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30079, "THEN/ELSE", obj));//$NON-NLS-1$
	    }
	    obj.setExpression(ResolverUtil.convertExpression(obj.getExpression(), whenTypeName, metadata));
	    ArrayList<Expression> whens = new ArrayList<Expression>(whenCount);
	    ArrayList<Expression> thens = new ArrayList<Expression>(whenCount);
	    for (int i = 0; i < whenCount; i++) {
	        whens.add(ResolverUtil.convertExpression(obj.getWhenExpression(i), whenTypeName, metadata));
	        thens.add(ResolverUtil.convertExpression(obj.getThenExpression(i), thenTypeName, metadata));
	    }
	    obj.setWhen(whens, thens);
	    if (elseExpr != null) {
	        obj.setElseExpression(ResolverUtil.convertExpression(elseExpr, thenTypeName, metadata));
	    }
	    // Set this CASE expression's type to the common THEN type, and we're done.
	    obj.setType(DataTypeManager.getDataTypeClass(thenTypeName));
	}

	private void setDesiredType(Expression obj, Class<?> type, LanguageObject surrounding) throws QueryResolverException {
		ResolverUtil.setDesiredType(obj, type, surrounding);
		//second pass resolving for functions
		if (!(obj instanceof Function)) {
			return;
		}
		if (unresolvedFunctions != null) {
			Function f = (Function)obj;
			if (f.getFunctionDescriptor() != null) {
				return;
			}
        	unresolvedFunctions.remove(obj);
			obj.acceptVisitor(this);
			QueryResolverException e = unresolvedFunctions.get(obj);
			if (e != null) {
				throw e;
			}
		}
	}

	void resolveSearchedCaseExpression(SearchedCaseExpression obj) throws QueryResolverException {
	    // If already resolved, do nothing
	    if (obj.getType() != null) {
	        return;
	    }
	    final int whenCount = obj.getWhenCount();
	    // 1. Call recursively to resolve any contained CASE expressions
	
	    Class<?> thenType = null;
	    // Get the WHEN and THEN types, and get a candidate type for each (for the next step)
	    for (int i = 0; i < whenCount; i++) {
	        if (thenType == null) {
	            thenType = obj.getThenExpression(i).getType();
	        }
	    }
	
	    Expression elseExpr = obj.getElseExpression();
	    if (elseExpr != null) {
	        if (thenType == null) {
	            thenType = elseExpr.getType();
	        }
	    }
	    // Invariant: All the expressions contained in the obj are resolved (except References)
	
	    // 2. Attempt to set the target types of all contained expressions,
	    //    and collect their type names for the next step
	    ArrayList<String> thenTypeNames = new ArrayList<String>(whenCount + 1);
	    Expression then = null;
	    // Set the types of the WHEN and THEN parts
	    for (int i = 0; i < whenCount; i++) {
	        then = obj.getThenExpression(i);
	        setDesiredType(then, thenType, obj);
            thenTypeNames.add(DataTypeManager.getDataTypeName(then.getType()));
	    }
	    // Set the type of the else expression
	    if (elseExpr != null) {
	        setDesiredType(elseExpr, thenType, obj);
            thenTypeNames.add(DataTypeManager.getDataTypeName(elseExpr.getType()));
	    }
	
	    // Invariants: all the expressions' types are non-null
	
	    // 3. Perform implicit type conversions
	    String thenTypeName = ResolverUtil.getCommonType(thenTypeNames.toArray(new String[thenTypeNames.size()]));
	    if (thenTypeName == null) {
	         throw new QueryResolverException(QueryPlugin.Event.TEIID30079, QueryPlugin.Util.gs(QueryPlugin.Event.TEIID30079, "THEN/ELSE", obj)); //$NON-NLS-1$
	    }
	    ArrayList<Expression> thens = new ArrayList<Expression>(whenCount);
	    for (int i = 0; i < whenCount; i++) {
	        thens.add(ResolverUtil.convertExpression(obj.getThenExpression(i), thenTypeName, metadata));
	    }
	    obj.setWhen(obj.getWhen(), thens);
	    if (elseExpr != null) {
	        obj.setElseExpression(ResolverUtil.convertExpression(elseExpr, thenTypeName, metadata));
	    }
	    // Set this CASE expression's type to the common THEN type, and we're done.
	    obj.setType(DataTypeManager.getDataTypeClass(thenTypeName));
	}
	
    public static void resolveLanguageObject(LanguageObject obj, QueryMetadataInterface metadata)
    throws TeiidComponentException, QueryResolverException {
	    ResolverVisitor.resolveLanguageObject(obj, null, metadata);
	}
	
	public static void resolveLanguageObject(LanguageObject obj, Collection<GroupSymbol> groups, QueryMetadataInterface metadata)
	    throws TeiidComponentException, QueryResolverException {
	    ResolverVisitor.resolveLanguageObject(obj, groups, null, metadata);
	}
	
	public static void resolveLanguageObject(LanguageObject obj, Collection<GroupSymbol> groups, GroupContext externalContext, QueryMetadataInterface metadata)
	    throws TeiidComponentException, QueryResolverException {
	
	    if(obj == null) {
	        return;
	    }
	
	    // Resolve elements, deal with errors
	    ResolverVisitor elementsVisitor = new ResolverVisitor(metadata, groups, externalContext);
	    PostOrderNavigator.doVisit(obj, elementsVisitor);
	    elementsVisitor.throwException(true);
	}
    
}

