/*
 * Copyright (c) 2010 Dave Ray <daveray@gmail.com>
 *
 * Created on Jun 23, 2010
 */
package org.jsoar.kernel.smem;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jsoar.kernel.Decider;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.learning.Chunker;
import org.jsoar.kernel.lhs.Condition;
import org.jsoar.kernel.lhs.ConjunctiveTest;
import org.jsoar.kernel.lhs.EqualityTest;
import org.jsoar.kernel.lhs.PositiveCondition;
import org.jsoar.kernel.lhs.Test;
import org.jsoar.kernel.lhs.Tests;
import org.jsoar.kernel.memory.Instantiation;
import org.jsoar.kernel.memory.Preference;
import org.jsoar.kernel.memory.RecognitionMemory;
import org.jsoar.kernel.memory.Slot;
import org.jsoar.kernel.memory.WmeImpl;
import org.jsoar.kernel.memory.WorkingMemory;
import org.jsoar.kernel.modules.SoarModule;
import org.jsoar.kernel.parser.original.Lexeme;
import org.jsoar.kernel.parser.original.LexemeType;
import org.jsoar.kernel.parser.original.Lexer;
import org.jsoar.kernel.rhs.Action;
import org.jsoar.kernel.rhs.MakeAction;
import org.jsoar.kernel.rhs.RhsSymbolValue;
import org.jsoar.kernel.rhs.RhsValue;
import org.jsoar.kernel.smem.DefaultSemanticMemoryParams.Optimization;
import org.jsoar.kernel.symbols.IdentifierImpl;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.kernel.symbols.SymbolFactoryImpl;
import org.jsoar.kernel.symbols.SymbolImpl;
import org.jsoar.kernel.symbols.Symbols;
import org.jsoar.kernel.tracing.Printer;
import org.jsoar.util.ByRef;
import org.jsoar.util.JdbcTools;
import org.jsoar.util.adaptables.Adaptable;
import org.jsoar.util.adaptables.Adaptables;
import org.jsoar.util.markers.DefaultMarker;
import org.jsoar.util.markers.Marker;
import org.jsoar.util.properties.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SemanticMemory}
 * 
 * <h2>Variance from CSoar Implementation</h2>
 * <p>The smem_data_struct that was added to every identifier in CSoar is instead maintained 
 * in a map from id to {@link SemanticMemoryStateInfo} in this class. This structure is never
 * accessed outside of SMem or in a way that would make a map too slow.
 * 
 * <h2>Notes on soardb/sqlite to JDBC conversion</h2>
 * <ul>
 * <li>When retrieving column values (e.g. {@code sqlite3_column_int}), columns are 
 * 0-based. In JDBC, they are 1-based. So all column retrievals (in the initial port)
 * have the original index and {@code + 1}. For example, {@code rs.getLong(2 + 1)}.
 * <li>soardb tries to store ints as 32 or 64 bits depending on the platform. In this port,
 * we're just using long (64 bits) everywhere. So {@code column_int()} maps to 
 * {@code ResultSet.getLong()}.
 * </ul> 
 * <h2>Typedef mappings</h2>
 * <ul>
 * <li>uintptr_t == long
 * <li>intptr_t == long
 * <li>smem_hash_id == long
 * <li>smem_lti_id == long
 * <li>goal_stack_level == int
 * <li>smem_lti_set = {@code Set<Long>}
 * <li>smem_wme_list == {@code List<WmeImpl>}
 * <li>smem_slot == {@code List<smem_chunk_value> }
 * <li>smem_slot_map == {@code Map<SymbolImpl, List<smem_chunk_value>>}
 * <li>smem_str_to_chunk_map == {@code Map<String, smem_chunk_value>}
 * <li>smem_chunk_set == {@code Set<smem_chunk_value>}
 * <li>smem_sym_to_chunk_map = {@code Map<SymbolImpl, smem_chunk>}
 * <li>smem_lti_set = {@code Set<Long>}
 * <li>smem_weighted_cue_list = {@code LinkedList<WeightedCueElement>}
 * <li>smem_prioritized_activated_lti_queue = {@code PriorityQueue<ActivatedLti>}
 * <li>tc_number = {@code Marker}
 * </ul>
 * @author ray
 */
public class DefaultSemanticMemory implements SemanticMemory
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultSemanticMemory.class);

    /**
     * semantic_memory.h:232:smem_variable_key
     */
    private static enum smem_variable_key
    {
        var_max_cycle, var_num_nodes, var_num_edges, var_act_thresh
    };
    
    /**
     * semantic_memory.h:260:smem_storage_type
     */
    private static enum smem_storage_type { store_level, store_recursive };
    
    /**
     * semantic_memory.h:367:smem_query_levels
     */
    private static enum smem_query_levels { qry_search, qry_full };

    /**
     * semantic_memory.h:237:SMEM_ACT_MAX
     */
    private static final long SMEM_ACT_MAX = (0-1)/ 2; // TODO???

    private Adaptable context;
    private DefaultSemanticMemoryParams params;
    private DefaultSemanticMemoryStats stats;
    /*private*/ SymbolFactoryImpl symbols;
    private RecognitionMemory recMem;
    private Chunker chunker;
    private Decider decider;
    
    private SemanticMemoryDatabase db;
    
    /** agent.h:smem_validation */
    private /*uintptr_t*/ long smem_validation;
    /** agent.h:smem_first_switch */
    private boolean smem_first_switch = true;
    /** agent.h:smem_made_changes */
    private boolean smem_made_changes = false;
    /** agent.h:smem_max_cycle */
    private /*intptr_t*/ long smem_max_cycle;
    
    /*private*/ SemanticMemorySymbols predefinedSyms;
    
    private Map<IdentifierImpl, SemanticMemoryStateInfo> stateInfos = new HashMap<IdentifierImpl, SemanticMemoryStateInfo>();
    
    public DefaultSemanticMemory(Adaptable context)
    {
        this(context, null);
    }
    
    public DefaultSemanticMemory(Adaptable context, SemanticMemoryDatabase db)
    {
        this.context = context;
        this.db = db;
    }

    public void initialize()
    {
        this.symbols = Adaptables.require(DefaultSemanticMemory.class, context, SymbolFactoryImpl.class);
        this.predefinedSyms = new SemanticMemorySymbols(this.symbols);
        
        this.chunker = Adaptables.adapt(context, Chunker.class);
        this.decider = Adaptables.adapt(context, Decider.class);
        this.recMem = Adaptables.adapt(context, RecognitionMemory.class);
        
        final PropertyManager properties = Adaptables.require(DefaultSemanticMemory.class, context, PropertyManager.class);
        params = new DefaultSemanticMemoryParams(properties);
        stats = new DefaultSemanticMemoryStats(properties);
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#resetStatistics()
     */
    @Override
    public void resetStatistics()
    {
        stats.reset();
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#getStatistics()
     */
    @Override
    public SemanticMemoryStatistics getStatistics()
    {
    	return stats;
    }

    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#attachToNewContext(org.jsoar.kernel.symbols.IdentifierImpl)
     */
    @Override
    public void initializeNewContext(WorkingMemory wm, IdentifierImpl id)
    {
        stateInfos.put(id, new SemanticMemoryStateInfo(this, wm, id));
    }

    SemanticMemoryDatabase getDatabase()
    {
        return db;
    }
    
    DefaultSemanticMemoryParams getParams()
    {
        return params;
    }
    
    DefaultSemanticMemoryStats getStats()
    {
        return stats;
    }
    
    private SemanticMemoryStateInfo smem_info(IdentifierImpl state)
    {
        return stateInfos.get(state);
    }
    
    @Override
    public boolean smem_enabled()
    {
        return params.learning.get();
    }

    private List<WmeImpl> smem_get_direct_augs_of_id( SymbolImpl sym)
    {
        return smem_get_direct_augs_of_id(sym, null);
    }
    
    /**
     * semantic_memory.cpp:481:smem_get_direct_augs_of_id
     * 
     * @param sym
     * @param tc
     * @return
     */
    private List<WmeImpl> smem_get_direct_augs_of_id( SymbolImpl sym, Marker tc /*= NIL*/ )
    {
        final List<WmeImpl> return_val = new ArrayList<WmeImpl>();
        // augs only exist for identifiers
        final IdentifierImpl id = sym.asIdentifier();
        if ( id != null )
        {
            if ( tc != null )
            {
                if ( tc == id.tc_number )
                {
                    return return_val;
                }
                else
                {
                    id.tc_number = tc;
                }
            }

            // impasse wmes
            for (WmeImpl w=id.goalInfo != null ? id.goalInfo.getImpasseWmes() : null; w!=null; w=w.next )
            {
                if ( !w.acceptable )
                {
                    return_val.add( w );
                }
            }

            // input wmes
            for (WmeImpl w=id.getInputWmes(); w!=null; w=w.next )
            {
                return_val.add( w );
            }

            // regular wmes
            for (Slot s=id.slots; s!=null; s=s.next )
            {
                for (WmeImpl w=s.getWmes(); w!=null; w=w.next )
                {
                    if ( !w.acceptable )
                    {
                        return_val.add( w );
                    }
                }
            }
        }

        return return_val;
    } 
    
    /**
     * semantic_memory.cpp:481:smem_symbol_is_constant
     * 
     * @param sym
     * @return
     */
    private static boolean smem_symbol_is_constant( Symbol sym )
    {
        return sym.asIdentifier() == null;
//        return ( ( sym->common.symbol_type == SYM_CONSTANT_SYMBOL_TYPE ) ||
//                 ( sym->common.symbol_type == INT_CONSTANT_SYMBOL_TYPE ) ||
//                 ( sym->common.symbol_type == FLOAT_CONSTANT_SYMBOL_TYPE ) );
    }
    

    private long /*smem_hash_id*/ smem_temporal_hash_add(int sym_type ) throws SQLException
    {
        db.hash_add_type.setInt(1, sym_type );
        return JdbcTools.insertAndGetRowId(db.hash_add_type);
    }

    private long /*smem_hash_id*/ smem_temporal_hash_int(long val, boolean add_on_fail /*= true*/ ) throws SQLException
    {
        long /*smem_hash_id*/ return_val = 0;
        
        // search first
        db.hash_get_int.setLong( 1, val );
        final ResultSet rs = db.hash_get_int.executeQuery();
        try
        {
            if(rs.next())
            {
                return_val = rs.getLong(0 + 1);
            }
        }
        finally
        {
            rs.close();
        }

        // if fail and supposed to add
        if ( return_val == 0 && add_on_fail )
        {
            // type first       
            return_val = smem_temporal_hash_add( Symbols.INT_CONSTANT_SYMBOL_TYPE );

            // then content
            db.hash_add_int.setLong( 1, return_val );
            db.hash_add_int.setLong( 2, val );
            db.hash_add_int.executeUpdate(/*soar_module::op_reinit*/ );
        }

        return return_val;
    }

    private long /*smem_hash_id*/ smem_temporal_hash_float(double val, boolean add_on_fail /*= true*/ ) throws SQLException
    {
        long /*smem_hash_id*/ return_val = 0;
        
        // search first
        // search first
        db.hash_get_float.setDouble( 1, val );
        final ResultSet rs = db.hash_get_float.executeQuery();
        try
        {
            if(rs.next())
            {
                return_val = rs.getLong(0 + 1);
            }
        }
        finally
        {
            rs.close();
        }

        // if fail and supposed to add
        if ( return_val == 0 && add_on_fail )
        {
            // type first       
            return_val = smem_temporal_hash_add( Symbols.FLOAT_CONSTANT_SYMBOL_TYPE );

            // then content
            db.hash_add_float.setLong( 1, return_val );
            db.hash_add_float.setDouble( 2, val );
            db.hash_add_float.executeUpdate(/*soar_module::op_reinit*/ );
        }

        return return_val;
    }

    private long /*smem_hash_id*/ smem_temporal_hash_str(String val, boolean add_on_fail /*= true*/ ) throws SQLException
    {
        long /*smem_hash_id*/ return_val = 0;
        
        // search first
        // search first
        db.hash_get_str.setString( 1, val );
        final ResultSet rs = db.hash_get_str.executeQuery();
        try
        {
            if(rs.next())
            {
                return_val = rs.getLong(0 + 1);
            }
        }
        finally
        {
            rs.close();
        }

        // if fail and supposed to add
        if ( return_val == 0 && add_on_fail )
        {
            // type first       
            return_val = smem_temporal_hash_add( Symbols.SYM_CONSTANT_SYMBOL_TYPE );

            // then content
            db.hash_add_str.setLong( 1, return_val );
            db.hash_add_str.setString( 2, val );
            db.hash_add_str.executeUpdate(/*soar_module::op_reinit*/ );
        }

        return return_val;
    }

    
    /**
     * semantic_memory.cpp:589:_smem_add_wme
     * 
     * @param state
     * @param id
     * @param attr
     * @param value
     * @param meta
     */
    void _smem_add_wme(IdentifierImpl state, IdentifierImpl id, SymbolImpl attr, SymbolImpl value, boolean meta )
    {
        // this fake preference is just for this state.
        // it serves the purpose of simulating a completely
        // local production firing to provide backtracing
        // information, making the result wmes dependent
        // upon the cue wmes.
        final SemanticMemoryStateInfo smem_info = smem_info(state);
        final Preference pref = SoarModule.make_fake_preference( state, id, attr, value, smem_info.cue_wmes );

        // add the preference to temporary memory
        recMem.add_preference_to_tm( pref );

        // and add it to the list of preferences to be removed
        // when the goal is removed
        state.goalInfo.addGoalPreference(pref);
        pref.on_goal_list = true;


        if ( meta )
        {
            // if this is a meta wme, then it is completely local
            // to the state and thus we will manually remove it
            // (via preference removal) when the time comes
            smem_info.smem_wmes.push( pref );
        }
        else
        {
            // otherwise, we submit the fake instantiation to backtracing
            // such as to potentially produce justifications that can follow
            // it to future adventures (potentially on new states)

            final ByRef<Instantiation> my_justification_list = ByRef.create(null);
            chunker.chunk_instantiation( pref.inst, false, my_justification_list );

            // if any justifications are created, assert their preferences manually
            // (copied mainly from assert_new_preferences with respect to our circumstances)
            if ( my_justification_list.value != null)
            {
                Instantiation next_justification = null;
                
                for ( Instantiation my_justification=my_justification_list.value;
                      my_justification!=null;
                      my_justification=next_justification )
                {
                    next_justification = my_justification.nextInProdList;

                    if ( my_justification.in_ms )
                    {
                        my_justification.prod.instantiations = my_justification.insertAtHeadOfProdList(my_justification.prod.instantiations);
                    }

                    for (Preference just_pref=my_justification.preferences_generated; just_pref!=null; just_pref=just_pref.inst_next ) 
                    {
                        recMem.add_preference_to_tm( just_pref );                        
                        
                        // TODO SMEM WMA
//                        if ( wma_enabled( my_agent ) )
//                        {
//                            wma_activate_wmes_in_pref( my_agent, just_pref );
//                        }
                    }
                }
            }
        }
    }
    
    private void smem_add_retrieved_wme(IdentifierImpl state, IdentifierImpl id, SymbolImpl attr, SymbolImpl value )
    {
        _smem_add_wme( state, id, attr, value, false );
    }

    private void smem_add_meta_wme( IdentifierImpl state, IdentifierImpl id, SymbolImpl attr, SymbolImpl value )
    {
        _smem_add_wme( state, id, attr, value, true );
    }

    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
// Variable Functions (smem::var)
//
// Variables are key-value pairs stored in the database
// that are necessary to maintain a store between
// multiple runs of Soar.
//
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////

    /**
     * Gets an SMem variable from the database
     * 
     * <p>semantic_memory.cpp:682:smem_variable_get
     * 
     * @param variable_id
     * @param variable_value
     * @return
     * @throws SQLException
     */
    private boolean smem_variable_get(smem_variable_key variable_id, ByRef<Long> variable_value ) throws SQLException
    {
        final PreparedStatement var_get = db.var_get;
    
        var_get.setInt( 1, variable_id.ordinal() );
        final ResultSet rs = var_get.executeQuery();
        try
        {
            if(rs.next())
            {
                variable_value.value = rs.getLong(0 + 1);
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            rs.close();
        }
    }

    /**
     * Sets an SMem variable in the database
     * 
     * semantic_memory.cpp:705:smem_variable_set
     * 
     * @param variable_id
     * @param variable_value
     * @throws SQLException
     */
    private void smem_variable_set(smem_variable_key variable_id, long variable_value ) throws SQLException
    {
        final PreparedStatement var_set = db.var_set;
    
        var_set.setLong( 1, variable_value );
        var_set.setInt( 2, variable_id.ordinal() );
    
        var_set.execute();
    }
    
    /**
     * Create a new SMem variable in the database
     * 
     * semantic_memory.cpp:705:smem_variable_set
     * 
     * @param variable_id
     * @param variable_value
     * @throws SQLException
     */
    private void smem_variable_create(smem_variable_key variable_id, long variable_value ) throws SQLException
    {
        final PreparedStatement var_create = db.var_create;
    
        var_create.setInt( 1, variable_id.ordinal() );
        var_create.setLong( 2, variable_value );
    
        var_create.execute();
    }    
    
    /**
     * semantic_memory.cpp:735:smem_temporal_hash
     * 
     * @param sym
     * @return
     * @throws SQLException
     */
    private /*smem_hash_id*/ long smem_temporal_hash(SymbolImpl sym) throws SQLException
    {
        return smem_temporal_hash(sym, true);
    }
    
    /**
     * Returns a temporally unique integer representing a symbol constant
     * 
     * <p>semantic_memory.cpp:735:smem_temporal_hash
     * 
     * @param sym
     * @param add_on_fail
     * @return
     * @throws SQLException
     */
    private /*smem_hash_id*/ long smem_temporal_hash(SymbolImpl sym, boolean add_on_fail /*= true*/ ) throws SQLException
    {
        /*smem_hash_id*/ long return_val = 0;

        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM timers: my_agent->smem_timers->hash->start();
        ////////////////////////////////////////////////////////////////////////////

        if ( smem_symbol_is_constant( sym ) )
        {
            if ( ( sym.smem_hash == 0) || ( sym.common_smem_valid != smem_validation ) )
            {
                sym.smem_hash = 0;
                sym.common_smem_valid = smem_validation;

                // basic process:
                // - search
                // - if found, return
                // - else, add
                
                

                switch (Symbols.getSymbolType(sym))
                {
                case Symbols.SYM_CONSTANT_SYMBOL_TYPE:
                    return_val = smem_temporal_hash_str( sym.asString().getValue(), add_on_fail );
                    break;

                case Symbols.INT_CONSTANT_SYMBOL_TYPE:
                    return_val = smem_temporal_hash_int( sym.asInteger().getValue(), add_on_fail );
                    break;

                case Symbols.FLOAT_CONSTANT_SYMBOL_TYPE:
                    return_val = smem_temporal_hash_float( sym.asDouble().getValue(), add_on_fail );
                    break;
                }

                // cache results for later re-use
                sym.smem_hash = return_val;
                sym.common_smem_valid = smem_validation;
            }

            return_val = sym.smem_hash;
        }

        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->hash->stop();
        ////////////////////////////////////////////////////////////////////////////

        return return_val;
    }
    

    private int /*long?*/ smem_reverse_hash_int(long /*smem_hash_id*/ hash_value ) throws SQLException
    {
        db.hash_rev_int.setLong( 1, hash_value );
        final ResultSet rs = db.hash_rev_int.executeQuery();
        try
        {
            if(!rs.next()) { throw new IllegalStateException("Expected non-empty result"); }
            return rs.getInt(0 + 1);
        }
        finally
        {
            rs.close();
        }
    }

    private double smem_reverse_hash_float(long /*smem_hash_id*/ hash_value ) throws SQLException
    {
        db.hash_rev_float.setLong( 1, hash_value );
        final ResultSet rs = db.hash_rev_float.executeQuery();
        try
        {
            rs.next();
            return rs.getDouble(0 + 1);
        }
        finally
        {
            rs.close();
        }
    }

    private String smem_reverse_hash_str(long /*smem_hash_id*/ hash_value) throws SQLException
    {
        db.hash_rev_str.setLong( 1, hash_value );
        final ResultSet rs = db.hash_rev_str.executeQuery();
        try
        {
            rs.next();
            return rs.getString(0 + 1);
        }
        finally
        {
            rs.close();
        }
    }

    private SymbolImpl smem_reverse_hash(int sym_type, long /*smem_hash_id*/ hash_value ) throws SQLException
    {
        switch ( sym_type )
        {
        case Symbols.SYM_CONSTANT_SYMBOL_TYPE:  
            return symbols.createString(smem_reverse_hash_str(hash_value));

        case Symbols.INT_CONSTANT_SYMBOL_TYPE:
            return symbols.createInteger(smem_reverse_hash_int(hash_value));

        case Symbols.FLOAT_CONSTANT_SYMBOL_TYPE:
            return symbols.createDouble(smem_reverse_hash_float(hash_value));

        default:
            return null;
        }
    }

    /**
     * copied primarily from add_bound_variables_in_test
     * 
     * <p>semantic_memory.cpp:794:_smem_lti_from_test
     * 
     * @param t
     * @param valid_ltis
     */
    private static void _smem_lti_from_test( Test t, Set<IdentifierImpl> valid_ltis )
    {
        if(Tests.isBlank(t)) return;
        
        final EqualityTest eq = t.asEqualityTest();
        if (eq != null)
        {
            final IdentifierImpl referent = eq.getReferent().asIdentifier();
            if (referent != null && referent.smem_lti != 0)
            {
                valid_ltis.add( referent );
            }
          
            return;
        }

        {    
            final ConjunctiveTest ct = t.asConjunctiveTest();

            if (ct != null) 
            {
                for(Test c : ct.conjunct_list)
                {
                    _smem_lti_from_test(c, valid_ltis);
                }
            }
        }
    }
    
    /**
     * copied primarily from add_all_variables_in_rhs_value
     * 
     * <p>semantic_memory.cpp:823:_smem_lti_from_rhs_value
     * 
     * @param rv
     * @param valid_ltis
     */
    private static void _smem_lti_from_rhs_value( RhsValue rv, Set<IdentifierImpl> valid_ltis )
    {
        final RhsSymbolValue rsv = rv.asSymbolValue();
        if ( rsv != null )
        {
            final IdentifierImpl sym = rsv.getSym().asIdentifier();
            if (sym != null && sym.smem_lti != 0)
            {
                valid_ltis.add( sym );
            }
        }
        else
        {
            for(RhsValue c : rv.asFunctionCall().getArguments())
            {
                _smem_lti_from_rhs_value(c, valid_ltis );
            }
        }
    }
    
    /**
     * make sure ltis in actions are grounded
     * 
     * <p>semantic_memory.h:smem_valid_production
     * <p>semantic_memory.cpp:844:smem_valid_production
     */
    public static boolean smem_valid_production(Condition lhs_top, Action rhs_top)
    {
        final Set<IdentifierImpl> valid_ltis = new HashSet<IdentifierImpl>();
        
        // collect valid ltis
        for ( Condition c=lhs_top; c!=null; c=c.next )
        {
            final PositiveCondition pc = c.asPositiveCondition();
            if (pc != null)
            {
                _smem_lti_from_test( pc.attr_test, valid_ltis );
                _smem_lti_from_test( pc.value_test, valid_ltis );
            }
        }

        // validate ltis in actions
        // copied primarily from add_all_variables_in_action
        int action_counter = 0;

        for (Action a=rhs_top; a!=null; a=a.next )
        {       
            a.already_in_tc = false;
            action_counter++;
        }

        // good_pass detects infinite loops
        boolean good_pass = true;
        boolean good_action = true;
        while ( good_pass && action_counter != 0)
        {
            good_pass = false;
            
            for (Action a=rhs_top; a!=null; a=a.next )
            {
                if ( !a.already_in_tc )
                {
                    good_action = false;
                    
                    final MakeAction ma = a.asMakeAction();
                    if ( ma != null )
                    {
                        final IdentifierImpl id = ma.id.asSymbolValue().getSym().asIdentifier();

                        // non-identifiers are ok
                        if ( id == null )
                        {
                            good_action = true;
                        }
                        // short-term identifiers are ok
                        else if ( id.smem_lti == 0 )
                        {
                            good_action = true;
                        }
                        // valid long-term identifiers are ok
                        else if ( valid_ltis.contains( id ) )
                        {
                            good_action = true;
                        }
                    }
                    else
                    {                       
                        good_action = true;
                    }

                    // we've found a new good action
                    // mark as good, collect all goodies
                    if ( good_action )
                    {
                        a.already_in_tc = true;

                        if(ma != null)
                        {
                            _smem_lti_from_rhs_value( ma.value, valid_ltis );
                            _smem_lti_from_rhs_value( ma.attr, valid_ltis );
                        }
                        else
                        {
                            _smem_lti_from_rhs_value( a.asFunctionAction().getCall(), valid_ltis );
                        }

                        // note that we've dealt with another action
                        action_counter--;
                        good_pass = true;
                    }
                }
            }
        };

        return action_counter == 0;
    }
    
    /**
     * activates a new or existing long-term identifier
     * 
     * <p>semantic_memory.cpp:957:smem_lti_activate
     * 
     * @param lti
     * @throws SQLException
     */
    void smem_lti_activate(/*smem_lti_id*/ long lti ) throws SQLException
    {
        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->act->start();
        ////////////////////////////////////////////////////////////////////////////

        // First get the child count for the LTI
        long lti_child_ct = 0;
        db.act_lti_child_ct_get.setLong(1, lti);
        final ResultSet rs = db.act_lti_child_ct_get.executeQuery();
        try
        {
            rs.next();
            lti_child_ct = rs.getLong(0 + 1);
        }
        finally { rs.close(); }

        if ( lti_child_ct >= params.thresh.get())
        {
            // cycle=? WHERE lti=?
            db.act_lti_set.setLong( 1, smem_max_cycle++);
            db.act_lti_set.setLong( 2, lti );
            db.act_lti_set.execute( /*soar_module::op_reinit*/ );
        }
        else
        {
            // cycle=? WHERE lti=?
            db.act_set.setLong( 1, smem_max_cycle++);
            db.act_set.setLong( 2, lti );
            db.act_set.execute( /*soar_module::op_reinit*/ );
        }

        //db.act_lti_child_ct_get->reinitialize();

        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->act->stop();
        ////////////////////////////////////////////////////////////////////////////
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#smem_lti_get_id(char, long)
     */
    @Override
    public long /*smem_lti_id*/ smem_lti_get_id( char name_letter, long name_number ) throws SoarException
    {
        // semantic_memory.cpp:989:smem_lti_get_id
        
        /*smem_lti_id*/ long return_val = 0;

        // getting lti ids requires an open semantic database
        smem_attach();
        
        try
        {
            // letter=? AND number=?
            db.lti_get.setLong( 1, (long)( name_letter ) );
            db.lti_get.setLong( 2, (long)( name_number ) );

            final ResultSet rs = db.lti_get.executeQuery();
            try
            {
                if (rs.next())
                {
                    return_val = rs.getLong(0 + 1);
                }
            }
            finally
            {
                rs.close();
            }
        }
        catch (SQLException e)
        {
            throw new SoarException(e.getMessage(), e);
        }

        //db.lti_get->reinitialize();

        return return_val;
    }

    /**
     * adds a new lti id for a letter/number pair
     * 
     * <p>semantic_memory.cpp:1011:smem_lti_add_id
     * 
     * @param name_letter
     * @param name_number
     * @return
     * @throws SQLException
     */
    long /*smem_lti_id*/ smem_lti_add_id(char name_letter, long name_number) throws SQLException
    {
        /*smem_lti_id*/ long return_val = 0;

        // create lti: letter, number
        db.lti_add.setLong( 1, (long)( name_letter ) );
        db.lti_add.setLong( 2, (long)( name_number ) );
        db.lti_add.setLong( 3, 0 );
        db.lti_add.setLong( 4, 0 );

        return_val = JdbcTools.insertAndGetRowId(db.lti_add);

        // increment stat
        stats.nodes.set(stats.nodes.get() + 1); // smem_stats->chunks in CSoar

        return return_val;
    }
    
    /**
     * makes a non-long-term identifier into a long-term identifier
     * 
     * <p>semantic_memory.cpp:1031:smem_lti_soar_add
     * 
     * @param s
     * @return
     * @throws SoarException
     * @throws SQLException
     */
    private /*smem_lti_id*/ long smem_lti_soar_add(SymbolImpl s ) throws SoarException, SQLException
    {
        final IdentifierImpl id = s.asIdentifier();
        if ( ( id != null ) &&
             ( id.smem_lti == 0 ) )
        {
            // try to find existing lti
            id.smem_lti = smem_lti_get_id( id.getNameLetter(), id.getNameNumber() );

            // if doesn't exist, add
            if ( id.smem_lti == 0)
            {
                id.smem_lti = smem_lti_add_id( id.getNameLetter(), id.getNameNumber() );

                // TODO SMEM Uncomment and port these lines when epmem is working
                // id.smem_time_id = my_agent->epmem_stats->time->get_value();
                // id.id_smem_valid = my_agent->epmem_validation;
            }
        }

        return id.smem_lti;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#smem_lti_soar_make(long, char, long, int)
     */
    public IdentifierImpl smem_lti_soar_make(/*smem_lti_id*/ long lti, char name_letter, long name_number, /*goal_stack_level*/ int level )
    {
        // semantic_memory.cpp:1053:smem_lti_soar_make

        // try to find existing
        IdentifierImpl return_val = symbols.findIdentifier(name_letter, name_number);

        // otherwise create
        if ( return_val == null )
        {
            return_val = symbols.make_new_identifier(name_letter, level);
        }
        else
        {
            if ( ( return_val.level == LTI_UNKNOWN_LEVEL ) && ( level != LTI_UNKNOWN_LEVEL ) )
            {
                return_val.level = level;
                return_val.promotion_level = level;
            }
        }

        // set lti field irrespective
        return_val.smem_lti = lti;

        return return_val;
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.smem.SemanticMemory#smem_reset_id_counters()
     */
    @Override
    public void smem_reset_id_counters() throws SoarException
    {
        // semantic_memory.cpp:1082:smem_reset_id_counters
        
        if(db != null /*my_agent->smem_db->get_status() == soar_module::connected*/ )
        {
            try
            {
                final ResultSet rs = db.lti_max.executeQuery();
                try
                {
                    while (rs.next())
                    {
                        // letter, max
                        final long name_letter = rs.getLong(0 + 1);
                        final long letter_max = rs.getLong(1 + 1);
   
                        // shift to alphabet
                        // name_letter -= (long)( 'A' );
   
                        symbols.resetIdNumber((char) name_letter, letter_max);
                    }
                }
                finally
                {
                    rs.close();
                }
            }
            catch (SQLException e)
            {
                throw new SoarException(e.getMessage(), e);
            }

            // db.lti_max->reinitialize();
        }
    }
    
    /**
     * <p>semantic_memory.cpp:1128:smem_disconnect_chunk
     * 
     * @param parent_id
     * @throws SQLException
     */
    void smem_disconnect_chunk(/*smem_lti_id*/ long parent_id ) throws SQLException
    {
        // adjust attribute counts
        {
            long counter = 0;
            
            // get all old counts
            db.web_attr_ct.setLong( 1, parent_id );
            final ResultSet webAttrCounts = db.web_attr_ct.executeQuery();
            try
            {
                while (webAttrCounts.next())
                {
                    counter += webAttrCounts.getLong( 1 + 1);
                    
                    // adjust in opposite direction ( adjust, attribute )
                    db.ct_attr_update.setLong( 1, -( webAttrCounts.getLong( 1 + 1) ) );
                    db.ct_attr_update.setLong( 2, webAttrCounts.getLong( 0 + 1 ) );
                    db.ct_attr_update.executeUpdate( /*soar_module::op_reinit*/ );
                }
            }
            finally
            {
                webAttrCounts.close();
            }
            //db.web_attr_ct->reinitialize();

            stats.edges.set(stats.edges.get() - counter); // smem_stats->slots in CSoar
        }

        // adjust const counts
        {
            // get all old counts
            db.web_const_ct.setLong( 1, parent_id );
            final ResultSet webConstCounts = db.web_const_ct.executeQuery();
            try
            {
                while ( webConstCounts.next() )
                {
                    // adjust in opposite direction ( adjust, attribute, const )
                    db.ct_const_update.setLong( 1, -( webConstCounts.getLong( 2 + 1 ) ) );
                    db.ct_const_update.setLong( 2, webConstCounts.getLong( 0 + 1 ) );
                    db.ct_const_update.setLong( 3, webConstCounts.getLong( 1 + 1 ) );
                    db.ct_const_update.executeUpdate( /*soar_module::op_reinit*/ );
                }
            }
            finally
            {
                webConstCounts.close();
            }
            //db.web_const_ct->reinitialize();
        }

        // adjust lti counts
        {
            // get all old counts
            db.web_lti_ct.setLong( 1, parent_id );
            final ResultSet webLtiCounts = db.web_lti_ct.executeQuery();
            try
            {
                while ( webLtiCounts.next() )
                {
                    // adjust in opposite direction ( adjust, attribute, lti )
                    db.ct_lti_update.setLong( 1, -( webLtiCounts.getLong( 2 + 1 ) ) );
                    db.ct_lti_update.setLong( 2, webLtiCounts.getLong( 0 + 1) );
                    db.ct_lti_update.setLong( 3, webLtiCounts.getLong( 1 + 1) );
                    db.ct_lti_update.executeUpdate( /*soar_module::op_reinit*/ );
                }
            }
            finally
            {
                webLtiCounts.close();
            }

            //db.web_lti_ct->reinitialize();
        }

        // disconnect
        {
            db.web_truncate.setLong( 1, parent_id );
            db.web_truncate.executeUpdate( /*soar_module::op_reinit*/ );
        }
    }

    /**
     * <p>semantic_memory.cpp:1187:smem_store_chunk
     * 
     * @param parent_id
     * @param children
     * @throws SQLException
     */
    void smem_store_chunk(/*smem_lti_id*/ long parent_id, Map<SymbolImpl, List<Object>> children) throws SQLException
    {
        smem_store_chunk(parent_id, children, true);
    }
    
    /**
     * <p>semantic_memory.cpp:1187:smem_store_chunk
     * 
     * @param parent_id
     * @param children
     * @param remove_old_children
     * @throws SQLException
     */
    void smem_store_chunk(/*smem_lti_id*/ long parent_id, Map<SymbolImpl, List<Object>> children, boolean remove_old_children /*= true*/ ) throws SQLException
    {
        long /*smem_hash_id*/ attr_hash = 0;
        long /*smem_hash_id*/ value_hash = 0;
        long /*smem_lti_id*/ value_lti = 0;

        final Map</*smem_hash_id*/ Long, Long> attr_ct_adjust = new HashMap<Long, Long>();
        final Map</*smem_hash_id*/ Long, Map</*smem_hash_id*/ Long, Long> > const_ct_adjust = new HashMap<Long, Map<Long,Long>>();
        final Map</*smem_hash_id*/ Long, Map</*smem_lti_id*/ Long, Long> > lti_ct_adjust = new HashMap<Long, Map<Long,Long>>();

        final long next_act_cycle = smem_max_cycle++;
        
        // clear web, adjust counts
        long child_ct = 0;
        if ( remove_old_children )
        {
            smem_disconnect_chunk( parent_id );
        }
        else
        {
            child_ct = getChildCount(parent_id);
        }

        // already above threshold?
        final long thresh = params.thresh.get();
        final boolean before_above = ( child_ct >= thresh );

        // get final count
        {
            for(Map.Entry<SymbolImpl, List<Object>> s : children.entrySet())
            {
//                for(smem_chunk_value v : s.getValue())
//                {
//                    child_ct++; // TODO SMEM Just add size()?
//                }
                child_ct += s.getValue().size();
            }
        }

        // above threshold now?
        final boolean after_above = ( child_ct >= thresh );
        final long web_act_cycle = ( ( after_above )?( SMEM_ACT_MAX ):( next_act_cycle ) );

        // if didn't clear and wasn't already above, need to update kids
        if ( ( !remove_old_children ) && ( !before_above ) )
        {
            db.act_set.setLong( 1, web_act_cycle );
            db.act_set.setLong( 2, parent_id );
            db.act_set.executeUpdate( /*soar_module::op_reinit*/ );
        }

        // if above threshold, update parent activation
        if ( after_above )
        {
            db.act_lti_set.setLong( 1, next_act_cycle );
            db.act_lti_set.setLong( 2, parent_id );
            db.act_lti_set.executeUpdate( /*soar_module::op_reinit*/ );
        }

        long stat_adjust = 0;
        
        // for all slots
        for (Map.Entry<SymbolImpl, List<Object>> s : children.entrySet())
        {
            // get attribute hash and contribute to count adjustment
            attr_hash = smem_temporal_hash( s.getKey() );
            final Long countForAttrHash = attr_ct_adjust.get(attr_hash);
            attr_ct_adjust.put(attr_hash, countForAttrHash != null ? countForAttrHash + 1 : 0 + 1);
            stat_adjust++;

            // for all values in the slot
            for (Object v : s.getValue())
            {           
                // most handling is specific to constant vs. identifier
                final SymbolImpl constant = Adaptables.adapt(v, SymbolImpl.class);
                if ( constant != null )
                {
                    value_hash = smem_temporal_hash( constant );

                    // parent_id, attr, val_const, val_lti, act_cycle
                    db.web_add.setLong( 1, parent_id );
                    db.web_add.setLong( 2, attr_hash );
                    db.web_add.setLong( 3, value_hash );
                    db.web_add.setNull( 4, java.sql.Types.NULL); //db.web_add->bind_null( 4 );
                    db.web_add.setLong( 5, web_act_cycle );
                    db.web_add.executeUpdate( /*soar_module::op_reinit*/ );

                    // TODO SMEM clean this up
                    Map<Long, Long> forHash = const_ct_adjust.get(attr_hash);
                    if(forHash == null)
                    {
                        forHash = new HashMap<Long, Long>();
                        const_ct_adjust.put(attr_hash, forHash);
                    }
                    final Long countForValueHash = forHash.get(value_hash);
                    forHash.put(value_hash, countForValueHash != null ? countForValueHash + 1 : 0 + 1);
                    //const_ct_adjust[ attr_hash ][ value_hash ]++;
                }
                else
                {
                    final smem_chunk_lti vAsLti = (smem_chunk_lti) v;
                    value_lti = vAsLti.lti_id; // (*v)->val_lti.val_value->lti_id;
                    if ( value_lti == 0 )
                    {
                        value_lti = smem_lti_add_id( vAsLti.lti_letter, vAsLti.lti_number );
                        vAsLti.lti_id = value_lti;

                        if ( vAsLti.soar_id != null )
                        {
                            vAsLti.soar_id.smem_lti = value_lti;

                            // TODO SMEM uncomment and implement when epmem is implemented
                            // v.asLti().soar_id.smem_time_id = my_agent->epmem_stats->time->get_value();
                            // v.asLti().soar_id.smem_valid = my_agent->epmem_validation;
                        }
                    }

                    // parent_id, attr, val_const, val_lti, act_cycle
                    db.web_add.setLong( 1, parent_id );
                    db.web_add.setLong( 2, attr_hash );
                    db.web_add.setNull( 3, java.sql.Types.NULL ); // db.web_add->bind_null( 3 );
                    db.web_add.setLong( 4, value_lti );
                    db.web_add.setLong( 5, web_act_cycle );
                    db.web_add.executeUpdate( /*soar_module::op_reinit*/ );

                    // add to counts
                    // TODO SMEM clean this up
                    Map<Long, Long> forHash = lti_ct_adjust.get(attr_hash);
                    if(forHash == null)
                    {
                        forHash = new HashMap<Long, Long>();
                        lti_ct_adjust.put(attr_hash, forHash);
                    }
                    final Long countForValueHash = forHash.get(value_lti);
                    forHash.put(value_lti, countForValueHash != null ? countForValueHash + 1 : 0 + 1);
                    
                    //lti_ct_adjust[ attr_hash ][ value_lti ]++;
                }
            }
        }

        // update stat
        {
            stats.edges.set(stats.edges.get() + stat_adjust); // smem_stats->slots in CSoar
        }

        // update attribute counts
        {
            for(Map.Entry<Long, Long> p : attr_ct_adjust.entrySet())
            {
                // make sure counter exists (attr)
                // check if counter exists
                db.ct_attr_check.setLong(1, p.getKey());
                if(!JdbcTools.queryHasResults(db.ct_attr_check))
                {
                    db.ct_attr_add.setLong( 1, p.getKey() );
                    db.ct_attr_add.executeUpdate( /*soar_module::op_reinit*/ );
                }

                // adjust count (adjustment, attr)
                db.ct_attr_update.setLong( 1, p.getValue() );
                db.ct_attr_update.setLong( 2, p.getKey() );
                db.ct_attr_update.executeUpdate( /*soar_module::op_reinit*/ );
            }
        }

        // update constant counts
        {
            for(Map.Entry<Long, Map<Long, Long>> p1 : const_ct_adjust.entrySet())
            {
                for(Map.Entry<Long, Long> p2 : p1.getValue().entrySet())
                {
                    // make sure counter exists (attr, val)
                    db.ct_const_check.setLong( 1, p1.getKey() );
                    db.ct_const_check.setLong( 2, p2.getKey() );
                    if(!JdbcTools.queryHasResults(db.ct_const_check))
                    {
                        db.ct_const_add.setLong( 1, p1.getKey() );
                        db.ct_const_add.setLong( 2, p2.getKey() );
                        db.ct_const_add.executeUpdate( /*soar_module::op_reinit*/ );
                    }

                    // adjust count (adjustment, attr, val)
                    db.ct_const_update.setLong( 1, p2.getValue() );
                    db.ct_const_update.setLong( 2, p1.getKey() );
                    db.ct_const_update.setLong( 3, p2.getKey() );
                    db.ct_const_update.executeUpdate( /*soar_module::op_reinit*/ );
                }
            }
        }

        // update lti counts
        {
            for(Map.Entry<Long, Map<Long, Long>> p1 : lti_ct_adjust.entrySet())
            {
                for(Map.Entry<Long, Long> p2 : p1.getValue().entrySet())
                {
                    // make sure counter exists (attr, lti)
                    db.ct_lti_check.setLong( 1, p1.getKey() );
                    db.ct_lti_check.setLong( 2, p2.getKey() );
                    if(!JdbcTools.queryHasResults(db.ct_lti_check))
                    {
                    
                        db.ct_lti_add.setLong( 1, p1.getKey() );
                        db.ct_lti_add.setLong( 2, p2.getKey() );
                        db.ct_lti_add.executeUpdate( /*soar_module::op_reinit*/ );
                    }

                    // adjust count (adjustment, attr, lti)
                    db.ct_lti_update.setLong( 1, p2.getValue() );
                    db.ct_lti_update.setLong( 2, p1.getKey() );
                    db.ct_lti_update.setLong( 3, p2.getKey() );
                    db.ct_lti_update.executeUpdate( /*soar_module::op_reinit*/ );
                }
            }
        }

        // update child count
        {
            db.act_lti_child_ct_set.setLong( 1, child_ct );
            db.act_lti_child_ct_set.setLong( 2, parent_id );
            db.act_lti_child_ct_set.executeUpdate( /*soar_module::op_reinit*/ );
        }
    }

    /**
     * Get the child count for an id from the database. Extracted from smem_store_chunk().
     * 
     * <p>semantic_memory.cpp:1187:smem_store_chunk
     * 
     * @param parent_id the parent identifier
     * @return the child cound
     * @throws SQLException
     */
    private long getChildCount(long parent_id) throws SQLException
    {
        db.act_lti_child_ct_get.setLong( 1, parent_id );
        final ResultSet rs = db.act_lti_child_ct_get.executeQuery();
        try
        {
           return rs.next() ? rs.getLong(0 + 1) : 0L;
        }
        finally
        {
            rs.close();
        }

        // db.act_lti_child_ct_get->reinitialize();
    }

    /**
     * <p>semantic_memory.cpp:1387:smem_soar_store
     * 
     * @param id
     * @throws SQLException
     * @throws SoarException
     */
    void smem_soar_store(IdentifierImpl id) throws SQLException, SoarException
    {
        smem_soar_store(id, smem_storage_type.store_level);
    }
    
    /**
     * <p>semantic_memory.cpp:1387:smem_soar_store
     * 
     * @param id
     * @param store_type
     * @throws SQLException
     * @throws SoarException
     */
    void smem_soar_store(IdentifierImpl id, smem_storage_type store_type) throws SQLException, SoarException
    {
        smem_soar_store(id, store_type, null);
    }

    /**
     * <p>semantic_memory.cpp:1387:smem_soar_store
     * 
     * @param id
     * @param store_type
     * @param tc
     * @throws SQLException
     * @throws SoarException
     */
    void smem_soar_store(IdentifierImpl id, smem_storage_type store_type /*= store_level*/, /*tc_number*/ Marker tc /*= null*/) throws SQLException, SoarException
    {
        // transitive closure only matters for recursive storage
        if ( ( store_type == smem_storage_type.store_recursive ) && ( tc == null ) )
        {
            tc = DefaultMarker.create();
        }

        // get level
        final List<WmeImpl> children = smem_get_direct_augs_of_id( id, tc );

        // encode this level
        {
            final Map<IdentifierImpl, smem_chunk_lti> sym_to_chunk = new HashMap<IdentifierImpl, smem_chunk_lti>();

            final Map<SymbolImpl, List<Object>> slots = smem_chunk_lti.newSlotMap();

            for (WmeImpl w : children)
            {
                // get slot
                final List<Object> s = smem_chunk_lti.smem_make_slot( slots , w.attr );

                // create value, per type
                final Object v;
                if ( smem_symbol_is_constant( w.value ) )
                {
                    v = w.value;
                }
                else
                {
                    final IdentifierImpl valueId = w.value.asIdentifier();
                    assert valueId != null;
                    
                    // try to find existing chunk
                    smem_chunk_lti c = sym_to_chunk.get(valueId);
                    
                    // if doesn't exist, add; else use existing
                    if(c == null)
                    {
                        final smem_chunk_lti lti = new smem_chunk_lti();
                        lti.lti_id = valueId.smem_lti;
                        lti.lti_letter = valueId.getNameLetter();
                        lti.lti_number = valueId.getNameNumber();
                        lti.slots = null;
                        lti.soar_id = valueId;
                        
                        sym_to_chunk.put(valueId, lti);
                        
                        c = lti;
                    }
                    
                    v = c;
                }

                // add value to slot
                s.add( v );
            }

            smem_store_chunk( smem_lti_soar_add( id ), slots);

            // clean up
            // Nothing to do in JSoar
        }

        // recurse as necessary
        if ( store_type == smem_storage_type.store_recursive )
        {
            for (WmeImpl w : children)
            {
                if ( !smem_symbol_is_constant( w.value ) )
                {
                    smem_soar_store( w.value.asIdentifier(), store_type, tc );
                }
            }
        }

        // clean up child wme list
        //delete children;
    }
    
    /**
     * <p>semantic_memory.cpp:1494:smem_install_memory
     * 
     * @param state
     * @param parent_id
     * @throws SQLException
     */
    void smem_install_memory(IdentifierImpl state, long /*smem_lti_id*/ parent_id) throws SQLException
    {
        smem_install_memory(state, parent_id, null);
    }
    
    /**
     * <p>semantic_memory.cpp:1494:smem_install_memory
     * 
     * @param state
     * @param parent_id
     * @param lti
     * @throws SQLException
     */
    void smem_install_memory(IdentifierImpl state, long /*smem_lti_id*/ parent_id, IdentifierImpl lti /*= NIL*/ ) throws SQLException
    {
        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->ncb_retrieval->start();
        ////////////////////////////////////////////////////////////////////////////

        // get the ^result header for this state
        final SemanticMemoryStateInfo info = smem_info(state);
        final IdentifierImpl result_header = info.smem_result_header;

        // get identifier if not known
        boolean lti_created_here = false;
        if ( lti == null )
        {
            db.lti_letter_num.setLong(1, parent_id);
            final ResultSet rs = db.lti_letter_num.executeQuery();
            try
            {
                if(!rs.next()) { throw new IllegalStateException("Expected non-empty result"); };
                lti = smem_lti_soar_make( parent_id, 
                        (char) rs.getLong(0 + 1), 
                        rs.getLong(1 + 1), 
                        result_header.level );
            }
            finally
            {
                rs.close();
            }
            lti_created_here = true;
        }

        // activate lti
        smem_lti_activate(parent_id);

        // point retrieved to lti
        smem_add_meta_wme(state, result_header, predefinedSyms.smem_sym_retrieved, lti );
        if ( lti_created_here )
        {
            // if the identifier was created above we need to remove a single 
            // ref count AFTER the wme is added (such as to not deallocate the 
            // symbol prematurely)
            // Not needed in JSoar
            // symbol_remove_ref( my_agent, lti );
        }   

        // if no children, then retrieve children
        if ( ( lti.goalInfo == null || lti.goalInfo.getImpasseWmes() == null ) &&
             ( lti.getInputWmes() == null ) &&
             ( lti.slots == null ) )
        {
            // get direct children: attr_type, attr_hash, value_type, value_hash, value_letter, value_num, value_lti
            db.web_expand.setLong( 1, parent_id );
            final ResultSet rs = db.web_expand.executeQuery();
            try
            {
                while (rs.next())
                {
                    // make the identifier symbol irrespective of value type
                    final SymbolImpl attr_sym = smem_reverse_hash( rs.getInt(0 + 1) , rs.getLong(1 + 1));

                    // identifier vs. constant
                    final SymbolImpl value_sym;
                    final long lti_id = rs.getLong(6 + 1);
                    if(!rs.wasNull())
                    {
                        value_sym = smem_lti_soar_make(lti_id, (char) rs.getLong( 4 + 1 ), rs.getLong( 5 + 1 ), lti.level);
                    }
                    else
                    {
                        value_sym = smem_reverse_hash(rs.getInt(2 + 1), rs.getLong(3 + 1));
                    }
    
                    // add wme
                    smem_add_retrieved_wme( state, lti, attr_sym, value_sym );
    
                    // deal with ref counts - attribute/values are always created in this function
                    // (thus an extra ref count is set before adding a wme)
                    // Not needed in JSoar
                    //symbol_remove_ref( my_agent, attr_sym );
                    //symbol_remove_ref( my_agent, value_sym );
                }
            }
            finally
            {
                rs.close();
            }
            
        }

        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->ncb_retrieval->stop();
        ////////////////////////////////////////////////////////////////////////////
    }

    /**
     * <p>semantic_memory.cpp:1582:smem_process_query
     * 
     * @param state
     * @param query
     * @param prohibit
     * @return
     * @throws SQLException
     */
    long /*smem_lti_id*/ smem_process_query(IdentifierImpl state, IdentifierImpl query, 
            Set<Long> /*smem_lti_set*/ prohibit) throws SQLException
    {
        return smem_process_query(state, query, prohibit, smem_query_levels.qry_full);
    }

    /**
     * <p>semantic_memory.cpp:1582:smem_process_query
     * 
     * @param state
     * @param query
     * @param prohibit
     * @param query_level
     * @return
     * @throws SQLException
     */
    long /*smem_lti_id*/ smem_process_query(IdentifierImpl state, IdentifierImpl query, 
            Set<Long> /*smem_lti_set*/ prohibit, 
            smem_query_levels query_level /*= qry_full*/ ) throws SQLException
    {   
        final SemanticMemoryStateInfo smem_info = smem_info(state);
        final LinkedList<WeightedCueElement> weighted_cue = new LinkedList<WeightedCueElement>();    
        boolean good_cue = true;

        long /*smem_lti_id*/ king_id = 0;

        ////////////////////////////////////////////////////////////////////////////
        // TODO SMEM Timers: my_agent->smem_timers->query->start();
        ////////////////////////////////////////////////////////////////////////////

        // prepare query stats
        {
            final PriorityQueue<WeightedCueElement> weighted_pq = WeightedCueElement.newPriorityQueue();
            
            final List<WmeImpl> cue = smem_get_direct_augs_of_id( query );

            smem_cue_element_type element_type = smem_cue_element_type.attr_t;

            for (WmeImpl w : cue)
            {
                smem_info.cue_wmes.add( w );

                if ( good_cue )
                {
                    // we only have to do hard work if
                    final long attr_hash = smem_temporal_hash( w.attr, false );
                    if ( attr_hash != 0 )
                    {
                        long value_lti = 0;
                        long value_hash = 0;
                        PreparedStatement q = null;
                        if ( smem_symbol_is_constant( w.value ) )
                        {
                            value_lti = 0;
                            value_hash = smem_temporal_hash( w.value, false );

                            if ( value_hash != 0 )
                            {
                                q = db.ct_const_get;
                                q.setLong( 1, attr_hash );
                                q.setLong( 2, value_hash );

                                element_type = smem_cue_element_type.value_const_t;
                            }
                            else
                            {
                                good_cue = false;
                            }
                        }
                        else
                        {
                            value_lti = w.value.asIdentifier().smem_lti;
                            value_hash = 0;

                            if ( value_lti == 0 )
                            {
                                q = db.ct_attr_get;
                                q.setLong( 1, attr_hash );

                                element_type = smem_cue_element_type.attr_t;
                            }
                            else
                            {
                                q = db.ct_lti_get;
                                q.setLong( 1, attr_hash );
                                q.setLong( 2, value_lti );

                                element_type = smem_cue_element_type.value_lti_t;
                            }
                        }

                        if ( good_cue )
                        {
                            final ResultSet rs = q.executeQuery();
                            try
                            {
                                if ( rs.next() )
                                {
                                    final WeightedCueElement new_cue_element = new WeightedCueElement();
    
                                    new_cue_element.weight = rs.getLong( 0 + 1);
                                    new_cue_element.attr_hash = attr_hash;
                                    new_cue_element.value_hash = value_hash;
                                    new_cue_element.value_lti = value_lti;
                                    new_cue_element.cue_element = w;
    
                                    new_cue_element.element_type = element_type;
    
                                    weighted_pq.add( new_cue_element );
                                }
                                else
                                {
                                    good_cue = false;
                                }
                            }
                            finally
                            {
                                rs.close();
                            }

                            //q->reinitialize();
                        }
                    }
                    else
                    {
                        good_cue = false;
                    }
                }
            }

            // if valid cue, transfer priority queue to list
            if ( good_cue )
            {
                while ( !weighted_pq.isEmpty() )
                {
                    weighted_cue.add( weighted_pq.remove() ); // top()/pop()
                }
            }
            // else deallocate priority queue contents
            else
            {
                while ( !weighted_pq.isEmpty() )
                {
                    weighted_pq.remove(); // top()/pop()
                }
            }

            // clean cue irrespective of validity
            //delete cue;
        }

        // only search if the cue was valid
        if ( good_cue && !weighted_cue.isEmpty() )
        {
            final WeightedCueElement first_element = weighted_cue.iterator().next();

            PreparedStatement q = null;
            PreparedStatement q2 = null;

            long /*smem_lti_id*/ cand;
            boolean good_cand;
            
            // setup first query, which is sorted on activation already
            {
                if ( first_element.element_type == smem_cue_element_type.attr_t )
                {
                    // attr=?
                    q = db.web_attr_all;
     
