
/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
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
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* THIS FILE IS AUTO-GENERATED FROM soot_options.xml. DO NOT MODIFY. */

package soot.options;
import soot.*;
import java.util.*;
import soot.PackManager;

/** Soot command-line options parser.
 * @author Ondrej Lhotak
 */

public class Options extends OptionsBase {
    public Options(Singletons.Global g) { }
    public static Options v() { return G.v().soot_options_Options(); }


    public static final int src_prec_c = 1;
    public static final int src_prec_class = 1;
    public static final int src_prec_only_class = 2;
    public static final int src_prec_J = 3;
    public static final int src_prec_jimple = 3;
    public static final int src_prec_java = 4;
    public static final int src_prec_apk = 5;
    public static final int output_format_J = 1;
    public static final int output_format_jimple = 1;
    public static final int output_format_j = 2;
    public static final int output_format_jimp = 2;
    public static final int output_format_S = 3;
    public static final int output_format_shimple = 3;
    public static final int output_format_s = 4;
    public static final int output_format_shimp = 4;
    public static final int output_format_B = 5;
    public static final int output_format_baf = 5;
    public static final int output_format_b = 6;
    public static final int output_format_G = 7;
    public static final int output_format_grimple = 7;
    public static final int output_format_g = 8;
    public static final int output_format_grimp = 8;
    public static final int output_format_X = 9;
    public static final int output_format_xml = 9;
    public static final int output_format_n = 10;
    public static final int output_format_none = 10;
    public static final int output_format_jasmin = 11;
    public static final int output_format_c = 12;
    public static final int output_format_class = 12;
    public static final int output_format_d = 13;
    public static final int output_format_dava = 13;
    public static final int output_format_t = 14;
    public static final int output_format_template = 14;
    public static final int throw_analysis_pedantic = 1;
    public static final int throw_analysis_unit = 2;

    public boolean parse( String[] argv ) {
        LinkedList phaseOptions = new LinkedList();

        for( int i = argv.length; i > 0; i-- ) {
            pushOptions( argv[i-1] );
        }
        while( hasMoreOptions() ) {
            String option = nextOption();
            if( option.charAt(0) != '-' ) {
                classes.add( option );
                continue;
            }
            while( option.charAt(0) == '-' ) {
                option = option.substring(1);
            }
            if( false );

            else if( false 
            || option.equals( "h" )
            || option.equals( "help" )
            )
                help = true;
  
            else if( false 
            || option.equals( "pl" )
            || option.equals( "phase-list" )
            )
                phase_list = true;
  
            else if( false
            || option.equals( "ph" )
            || option.equals( "phase-help" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( phase_help == null )
                    phase_help = new LinkedList();

                phase_help.add( value );
            }
  
            else if( false 
            || option.equals( "version" )
            )
                version = true;
  
            else if( false 
            || option.equals( "v" )
            || option.equals( "verbose" )
            )
                verbose = true;
  
            else if( false 
            || option.equals( "interactive-mode" )
            )
                interactive_mode = true;
  
            else if( false 
            || option.equals( "unfriendly-mode" )
            )
                unfriendly_mode = true;
  
            else if( false 
            || option.equals( "app" )
            )
                app = true;
  
            else if( false 
            || option.equals( "w" )
            || option.equals( "whole-program" )
            )
                whole_program = true;
  
            else if( false 
            || option.equals( "ws" )
            || option.equals( "whole-shimple" )
            )
                whole_shimple = true;
  
            else if( false 
            || option.equals( "validate" )
            )
                validate = true;
  
            else if( false 
            || option.equals( "debug" )
            )
                debug = true;
  
            else if( false 
            || option.equals( "debug-resolver" )
            )
                debug_resolver = true;
  
            else if( false
            || option.equals( "cp" )
            || option.equals( "soot-class-path" )
            || option.equals( "soot-classpath" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( soot_classpath.length() == 0 )
                    soot_classpath = value;
                else {
                    G.v().out.println( "Duplicate values "+soot_classpath+" and "+value+" for option -"+option );
                    return false;
                }
            }
  
            else if( false 
            || option.equals( "pp" )
            || option.equals( "prepend-classpath" )
            )
                prepend_classpath = true;
  
            else if( false
            || option.equals( "process-path" )
            || option.equals( "process-dir" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( process_dir == null )
                    process_dir = new LinkedList();

                process_dir.add( value );
            }
  
            else if( false 
            || option.equals( "oaat" )
            )
                oaat = true;
  
            else if( false 
            || option.equals( "ast-metrics" )
            )
                ast_metrics = true;
  
            else if( false
            || option.equals( "src-prec" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( false );
    
                else if( false
                || value.equals( "c" )
                || value.equals( "class" )
                ) {
                    if( src_prec != 0
                    && src_prec != src_prec_class ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    src_prec = src_prec_class;
                }
    
                else if( false
                || value.equals( "only-class" )
                ) {
                    if( src_prec != 0
                    && src_prec != src_prec_only_class ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    src_prec = src_prec_only_class;
                }
    
                else if( false
                || value.equals( "J" )
                || value.equals( "jimple" )
                ) {
                    if( src_prec != 0
                    && src_prec != src_prec_jimple ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    src_prec = src_prec_jimple;
                }
    
                else if( false
                || value.equals( "java" )
                ) {
                    if( src_prec != 0
                    && src_prec != src_prec_java ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    src_prec = src_prec_java;
                }
    
                else if( false
                || value.equals( "apk" )
                ) {
                    if( src_prec != 0
                    && src_prec != src_prec_apk ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    src_prec = src_prec_apk;
                }
    
                else {
                    G.v().out.println( "Invalid value "+value+" given for option -"+option );
                    return false;
                }
           }
  
            else if( false 
            || option.equals( "full-resolver" )
            )
                full_resolver = true;
  
            else if( false 
            || option.equals( "allow-phantom-refs" )
            )
                allow_phantom_refs = true;
  
            else if( false 
            || option.equals( "no-bodies-for-excluded" )
            )
                no_bodies_for_excluded = true;
  
            else if( false 
            || option.equals( "j2me" )
            )
                j2me = true;
  
            else if( false
            || option.equals( "main-class" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( main_class.length() == 0 )
                    main_class = value;
                else {
                    G.v().out.println( "Duplicate values "+main_class+" and "+value+" for option -"+option );
                    return false;
                }
            }
  
            else if( false 
            || option.equals( "polyglot" )
            )
                polyglot = true;
  
            else if( false
            || option.equals( "d" )
            || option.equals( "output-dir" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( output_dir.length() == 0 )
                    output_dir = value;
                else {
                    G.v().out.println( "Duplicate values "+output_dir+" and "+value+" for option -"+option );
                    return false;
                }
            }
  
            else if( false
            || option.equals( "f" )
            || option.equals( "output-format" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( false );
    
                else if( false
                || value.equals( "J" )
                || value.equals( "jimple" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_jimple ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_jimple;
                }
    
                else if( false
                || value.equals( "j" )
                || value.equals( "jimp" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_jimp ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_jimp;
                }
    
                else if( false
                || value.equals( "S" )
                || value.equals( "shimple" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_shimple ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_shimple;
                }
    
                else if( false
                || value.equals( "s" )
                || value.equals( "shimp" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_shimp ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_shimp;
                }
    
                else if( false
                || value.equals( "B" )
                || value.equals( "baf" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_baf ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_baf;
                }
    
                else if( false
                || value.equals( "b" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_b ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_b;
                }
    
                else if( false
                || value.equals( "G" )
                || value.equals( "grimple" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_grimple ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_grimple;
                }
    
                else if( false
                || value.equals( "g" )
                || value.equals( "grimp" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_grimp ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_grimp;
                }
    
                else if( false
                || value.equals( "X" )
                || value.equals( "xml" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_xml ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_xml;
                }
    
                else if( false
                || value.equals( "n" )
                || value.equals( "none" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_none ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_none;
                }
    
                else if( false
                || value.equals( "jasmin" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_jasmin ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_jasmin;
                }
    
                else if( false
                || value.equals( "c" )
                || value.equals( "class" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_class ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_class;
                }
    
                else if( false
                || value.equals( "d" )
                || value.equals( "dava" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_dava ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_dava;
                }
    
                else if( false
                || value.equals( "t" )
                || value.equals( "template" )
                ) {
                    if( output_format != 0
                    && output_format != output_format_template ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    output_format = output_format_template;
                }
    
                else {
                    G.v().out.println( "Invalid value "+value+" given for option -"+option );
                    return false;
                }
           }
  
            else if( false 
            || option.equals( "outjar" )
            || option.equals( "output-jar" )
            )
                output_jar = true;
  
            else if( false 
            || option.equals( "xml-attributes" )
            )
                xml_attributes = true;
  
            else if( false 
            || option.equals( "print-tags" )
            || option.equals( "print-tags-in-output" )
            )
                print_tags_in_output = true;
  
            else if( false 
            || option.equals( "no-output-source-file-attribute" )
            )
                no_output_source_file_attribute = true;
  
            else if( false 
            || option.equals( "no-output-inner-classes-attribute" )
            )
                no_output_inner_classes_attribute = true;
  
            else if( false
            || option.equals( "dump-body" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( dump_body == null )
                    dump_body = new LinkedList();

                dump_body.add( value );
            }
  
            else if( false
            || option.equals( "dump-cfg" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( dump_cfg == null )
                    dump_cfg = new LinkedList();

                dump_cfg.add( value );
            }
  
            else if( false 
            || option.equals( "show-exception-dests" )
            )
                show_exception_dests = true;
  
            else if( false 
            || option.equals( "gzip" )
            )
                gzip = true;
  
            else if( false
            || option.equals( "p" )
            || option.equals( "phase-option" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No phase name given for option -"+option );
                    return false;
                }
                String phaseName = nextOption();
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No phase option given for option -"+option+" "+phaseName );
                    return false;
                }
                String phaseOption = nextOption();
    
                phaseOptions.add( phaseName );
                phaseOptions.add( phaseOption );
            }
  
            else if( false
            || option.equals( "O" )
            || option.equals( "optimize" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "sop" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "jop" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "gop" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "bop" );
                pushOptions( "-p" );
                pushOptions( "only-stack-locals:false" );
                pushOptions( "gb.a2" );
                pushOptions( "-p" );
                pushOptions( "only-stack-locals:false" );
                pushOptions( "gb.a1" );
                pushOptions( "-p" );
            }
  
            else if( false
            || option.equals( "W" )
            || option.equals( "whole-optimize" )
            ) {
                
                pushOptions( "-O" );
                pushOptions( "-w" );
                pushOptions( "enabled:true" );
                pushOptions( "wsop" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "wjop" );
                pushOptions( "-p" );
            }
  
            else if( false 
            || option.equals( "via-grimp" )
            )
                via_grimp = true;
  
            else if( false 
            || option.equals( "via-shimple" )
            )
                via_shimple = true;
  
            else if( false
            || option.equals( "throw-analysis" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( false );
    
                else if( false
                || value.equals( "pedantic" )
                ) {
                    if( throw_analysis != 0
                    && throw_analysis != throw_analysis_pedantic ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    throw_analysis = throw_analysis_pedantic;
                }
    
                else if( false
                || value.equals( "unit" )
                ) {
                    if( throw_analysis != 0
                    && throw_analysis != throw_analysis_unit ) {
                        G.v().out.println( "Multiple values given for option "+option );
                        return false;
                    }
                    throw_analysis = throw_analysis_unit;
                }
    
                else {
                    G.v().out.println( "Invalid value "+value+" given for option -"+option );
                    return false;
                }
           }
  
            else if( false 
            || option.equals( "omit-excepting-unit-edges" )
            )
                omit_excepting_unit_edges = true;
  
            else if( false
            || option.equals( "trim-cfgs" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "jb.tt" );
                pushOptions( "-p" );
                pushOptions( "-omit-excepting-unit-edges" );
                pushOptions( "unit" );
                pushOptions( "-throw-analysis" );
            }
  
            else if( false
            || option.equals( "i" )
            || option.equals( "include" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( include == null )
                    include = new LinkedList();

                include.add( value );
            }
  
            else if( false
            || option.equals( "x" )
            || option.equals( "exclude" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( exclude == null )
                    exclude = new LinkedList();

                exclude.add( value );
            }
  
            else if( false 
            || option.equals( "include-all" )
            )
                include_all = true;
  
            else if( false
            || option.equals( "dynamic-class" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( dynamic_class == null )
                    dynamic_class = new LinkedList();

                dynamic_class.add( value );
            }
  
            else if( false
            || option.equals( "dynamic-dir" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( dynamic_dir == null )
                    dynamic_dir = new LinkedList();

                dynamic_dir.add( value );
            }
  
            else if( false
            || option.equals( "dynamic-package" )
            ) {
                if( !hasMoreOptions() ) {
                    G.v().out.println( "No value given for option -"+option );
                    return false;
                }
                String value = nextOption();
    
                if( dynamic_package == null )
                    dynamic_package = new LinkedList();

                dynamic_package.add( value );
            }
  
            else if( false 
            || option.equals( "keep-line-number" )
            )
                keep_line_number = true;
  
            else if( false 
            || option.equals( "keep-bytecode-offset" )
            || option.equals( "keep-offset" )
            )
                keep_offset = true;
  
            else if( false
            || option.equals( "annot-purity" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "wjap.purity" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "cg.spark" );
                pushOptions( "-p" );
                pushOptions( "-w" );
            }
  
            else if( false
            || option.equals( "annot-nullpointer" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "tag.an" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "jap.npc" );
                pushOptions( "-p" );
            }
  
            else if( false
            || option.equals( "annot-arraybounds" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "tag.an" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "jap.abc" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "wjap.ra" );
                pushOptions( "-p" );
            }
  
            else if( false
            || option.equals( "annot-side-effect" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "tag.dep" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "jap.sea" );
                pushOptions( "-p" );
                pushOptions( "-w" );
            }
  
            else if( false
            || option.equals( "annot-fieldrw" )
            ) {
                
                pushOptions( "enabled:true" );
                pushOptions( "tag.fieldrw" );
                pushOptions( "-p" );
                pushOptions( "enabled:true" );
                pushOptions( "jap.fieldrw" );
                pushOptions( "-p" );
                pushOptions( "-w" );
            }
  
            else if( false 
            || option.equals( "time" )
            )
                time = true;
  
            else if( false 
            || option.equals( "subtract-gc" )
            )
                subtract_gc = true;
  
            else {
                G.v().out.println( "Invalid option -"+option );
                return false;
            }
        }

        Iterator it = phaseOptions.iterator();
        while( it.hasNext() ) {
            String phaseName = (String) it.next();
            String phaseOption = (String) it.next();
            if( !setPhaseOption( phaseName, "enabled:true" ) ) return false;
        }

        it = phaseOptions.iterator();
        while( it.hasNext() ) {
            String phaseName = (String) it.next();
            String phaseOption = (String) it.next();
            if( !setPhaseOption( phaseName, phaseOption ) ) return false;
        }

        return true;
    }


    public boolean help() { return help; }
    private boolean help = false;
    public void set_help( boolean setting ) { help = setting; }
  
    public boolean phase_list() { return phase_list; }
    private boolean phase_list = false;
    public void set_phase_list( boolean setting ) { phase_list = setting; }
  
    public List phase_help() { 
        if( phase_help == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return phase_help;
    }
    public void set_phase_help( List setting ) { phase_help = setting; }
    private List phase_help = null;
    public boolean version() { return version; }
    private boolean version = false;
    public void set_version( boolean setting ) { version = setting; }
  
    public boolean verbose() { return verbose; }
    private boolean verbose = false;
    public void set_verbose( boolean setting ) { verbose = setting; }
  
    public boolean interactive_mode() { return interactive_mode; }
    private boolean interactive_mode = false;
    public void set_interactive_mode( boolean setting ) { interactive_mode = setting; }
  
    public boolean unfriendly_mode() { return unfriendly_mode; }
    private boolean unfriendly_mode = false;
    public void set_unfriendly_mode( boolean setting ) { unfriendly_mode = setting; }
  
    public boolean app() { return app; }
    private boolean app = false;
    public void set_app( boolean setting ) { app = setting; }
  
    public boolean whole_program() { return whole_program; }
    private boolean whole_program = false;
    public void set_whole_program( boolean setting ) { whole_program = setting; }
  
    public boolean whole_shimple() { return whole_shimple; }
    private boolean whole_shimple = false;
    public void set_whole_shimple( boolean setting ) { whole_shimple = setting; }
  
    public boolean validate() { return validate; }
    private boolean validate = false;
    public void set_validate( boolean setting ) { validate = setting; }
  
    public boolean debug() { return debug; }
    private boolean debug = false;
    public void set_debug( boolean setting ) { debug = setting; }
  
    public boolean debug_resolver() { return debug_resolver; }
    private boolean debug_resolver = false;
    public void set_debug_resolver( boolean setting ) { debug_resolver = setting; }
  
    public String soot_classpath() { return soot_classpath; }
    public void set_soot_classpath( String setting ) { soot_classpath = setting; }
    private String soot_classpath = "";
    public boolean prepend_classpath() { return prepend_classpath; }
    private boolean prepend_classpath = false;
    public void set_prepend_classpath( boolean setting ) { prepend_classpath = setting; }
  
    public List process_dir() { 
        if( process_dir == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return process_dir;
    }
    public void set_process_dir( List setting ) { process_dir = setting; }
    private List process_dir = null;
    public boolean oaat() { return oaat; }
    private boolean oaat = false;
    public void set_oaat( boolean setting ) { oaat = setting; }
  
    public boolean ast_metrics() { return ast_metrics; }
    private boolean ast_metrics = false;
    public void set_ast_metrics( boolean setting ) { ast_metrics = setting; }
  
    public int src_prec() {
        if( src_prec == 0 ) return src_prec_class;
        return src_prec; 
    }
    public void set_src_prec( int setting ) { src_prec = setting; }
    private int src_prec = 0;
    public boolean full_resolver() { return full_resolver; }
    private boolean full_resolver = false;
    public void set_full_resolver( boolean setting ) { full_resolver = setting; }
  
    public boolean allow_phantom_refs() { return allow_phantom_refs; }
    private boolean allow_phantom_refs = false;
    public void set_allow_phantom_refs( boolean setting ) { allow_phantom_refs = setting; }
  
    public boolean no_bodies_for_excluded() { return no_bodies_for_excluded; }
    private boolean no_bodies_for_excluded = false;
    public void set_no_bodies_for_excluded( boolean setting ) { no_bodies_for_excluded = setting; }
  
    public boolean j2me() { return j2me; }
    private boolean j2me = false;
    public void set_j2me( boolean setting ) { j2me = setting; }
  
    public String main_class() { return main_class; }
    public void set_main_class( String setting ) { main_class = setting; }
    private String main_class = "";
    public boolean polyglot() { return polyglot; }
    private boolean polyglot = false;
    public void set_polyglot( boolean setting ) { polyglot = setting; }
  
    public String output_dir() { return output_dir; }
    public void set_output_dir( String setting ) { output_dir = setting; }
    private String output_dir = "";
    public int output_format() {
        if( output_format == 0 ) return output_format_class;
        return output_format; 
    }
    public void set_output_format( int setting ) { output_format = setting; }
    private int output_format = 0;
    public boolean output_jar() { return output_jar; }
    private boolean output_jar = false;
    public void set_output_jar( boolean setting ) { output_jar = setting; }
  
    public boolean xml_attributes() { return xml_attributes; }
    private boolean xml_attributes = false;
    public void set_xml_attributes( boolean setting ) { xml_attributes = setting; }
  
    public boolean print_tags_in_output() { return print_tags_in_output; }
    private boolean print_tags_in_output = false;
    public void set_print_tags_in_output( boolean setting ) { print_tags_in_output = setting; }
  
    public boolean no_output_source_file_attribute() { return no_output_source_file_attribute; }
    private boolean no_output_source_file_attribute = false;
    public void set_no_output_source_file_attribute( boolean setting ) { no_output_source_file_attribute = setting; }
  
    public boolean no_output_inner_classes_attribute() { return no_output_inner_classes_attribute; }
    private boolean no_output_inner_classes_attribute = false;
    public void set_no_output_inner_classes_attribute( boolean setting ) { no_output_inner_classes_attribute = setting; }
  
    public List dump_body() { 
        if( dump_body == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return dump_body;
    }
    public void set_dump_body( List setting ) { dump_body = setting; }
    private List dump_body = null;
    public List dump_cfg() { 
        if( dump_cfg == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return dump_cfg;
    }
    public void set_dump_cfg( List setting ) { dump_cfg = setting; }
    private List dump_cfg = null;
    public boolean show_exception_dests() { return show_exception_dests; }
    private boolean show_exception_dests = false;
    public void set_show_exception_dests( boolean setting ) { show_exception_dests = setting; }
  
    public boolean gzip() { return gzip; }
    private boolean gzip = false;
    public void set_gzip( boolean setting ) { gzip = setting; }
  
    public boolean via_grimp() { return via_grimp; }
    private boolean via_grimp = false;
    public void set_via_grimp( boolean setting ) { via_grimp = setting; }
  
    public boolean via_shimple() { return via_shimple; }
    private boolean via_shimple = false;
    public void set_via_shimple( boolean setting ) { via_shimple = setting; }
  
    public int throw_analysis() {
        if( throw_analysis == 0 ) return throw_analysis_pedantic;
        return throw_analysis; 
    }
    public void set_throw_analysis( int setting ) { throw_analysis = setting; }
    private int throw_analysis = 0;
    public boolean omit_excepting_unit_edges() { return omit_excepting_unit_edges; }
    private boolean omit_excepting_unit_edges = false;
    public void set_omit_excepting_unit_edges( boolean setting ) { omit_excepting_unit_edges = setting; }
  
    public List include() { 
        if( include == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return include;
    }
    public void set_include( List setting ) { include = setting; }
    private List include = null;
    public List exclude() { 
        if( exclude == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return exclude;
    }
    public void set_exclude( List setting ) { exclude = setting; }
    private List exclude = null;
    public boolean include_all() { return include_all; }
    private boolean include_all = false;
    public void set_include_all( boolean setting ) { include_all = setting; }
  
    public List dynamic_class() { 
        if( dynamic_class == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return dynamic_class;
    }
    public void set_dynamic_class( List setting ) { dynamic_class = setting; }
    private List dynamic_class = null;
    public List dynamic_dir() { 
        if( dynamic_dir == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return dynamic_dir;
    }
    public void set_dynamic_dir( List setting ) { dynamic_dir = setting; }
    private List dynamic_dir = null;
    public List dynamic_package() { 
        if( dynamic_package == null )
            return java.util.Collections.EMPTY_LIST;
        else
            return dynamic_package;
    }
    public void set_dynamic_package( List setting ) { dynamic_package = setting; }
    private List dynamic_package = null;
    public boolean keep_line_number() { return keep_line_number; }
    private boolean keep_line_number = false;
    public void set_keep_line_number( boolean setting ) { keep_line_number = setting; }
  
    public boolean keep_offset() { return keep_offset; }
    private boolean keep_offset = false;
    public void set_keep_offset( boolean setting ) { keep_offset = setting; }
  
    public boolean time() { return time; }
    private boolean time = false;
    public void set_time( boolean setting ) { time = setting; }
  
    public boolean subtract_gc() { return subtract_gc; }
    private boolean subtract_gc = false;
    public void set_subtract_gc( boolean setting ) { subtract_gc = setting; }
  

    public String getUsage() {
        return ""

+"\nGeneral Options:\n"
      
+padOpt(" -h -help", "Display help and exit" )
+padOpt(" -pl -phase-list", "Print list of available phases" )
+padOpt(" -ph PHASE -phase-help PHASE", "Print help for specified PHASE" )
+padOpt(" -version", "Display version information and exit" )
+padOpt(" -v -verbose", "Verbose mode" )
+padOpt(" -interactive-mode", "Run in interactive mode" )
+padOpt(" -unfriendly-mode", "Allow Soot to run with no command-line options" )
+padOpt(" -app", "Run in application mode" )
+padOpt(" -w -whole-program", "Run in whole-program mode" )
+padOpt(" -ws -whole-shimple", "Run in whole-shimple mode" )
+padOpt(" -validate", "Run internal validation on bodies" )
+padOpt(" -debug", "Print various Soot debugging info" )
+padOpt(" -debug-resolver", "Print debugging info from SootResolver" )
+"\nInput Options:\n"
      
+padOpt(" -cp PATH -soot-class-path PATH -soot-classpath PATH", "Use PATH as the classpath for finding classes." )
+padOpt(" -pp -prepend-classpath", "Prepend the given soot classpath to the default classpath." )
+padOpt(" -process-path DIR -process-dir DIR", "Process all classes found in DIR" )
+padOpt(" -oaat", "From the process-dir, processes one class at a time." )
+padOpt(" -ast-metrics", "Compute AST Metrics if performing java to jimple" )
+padOpt(" -src-prec FORMAT", "Sets source precedence to FORMAT files" )
+padVal(" c class (default)", "Favour class files as Soot source" )
+padVal(" only-class", "Use only class files as Soot source" )
+padVal(" J jimple", "Favour Jimple files as Soot source" )
+padVal(" java", "Favour Java files as Soot source" )
+padVal(" apk", "Favour APK files as Soot source" )
+padOpt(" -full-resolver", "Force transitive resolving of referenced classes" )
+padOpt(" -allow-phantom-refs", "Allow unresolved classes; may cause errors" )
+padOpt(" -no-bodies-for-excluded", "Do not load bodies for excluded classes" )
+padOpt(" -j2me", "Use J2ME mode; changes assignment of types" )
+padOpt(" -main-class CLASS", "Sets the main class for whole-program analysis." )
+padOpt(" -polyglot", "Use Java 1.4 Polyglot frontend instead of JastAdd" )
+"\nOutput Options:\n"
      
+padOpt(" -d DIR -output-dir DIR", "Store output files in DIR" )
+padOpt(" -f FORMAT -output-format FORMAT", "Set output format for Soot" )
+padVal(" J jimple", "Produce .jimple Files" )
+padVal(" j jimp", "Produce .jimp (abbreviated Jimple) files" )
+padVal(" S shimple", "Produce .shimple files" )
+padVal(" s shimp", "Produce .shimp (abbreviated Shimple) files" )
+padVal(" B baf", "Produce .baf files" )
+padVal(" b", "Produce .b (abbreviated Baf) files" )
+padVal(" G grimple", "Produce .grimple files" )
+padVal(" g grimp", "Produce .grimp (abbreviated Grimp) files" )
+padVal(" X xml", "Produce .xml Files" )
+padVal(" n none", "Produce no output" )
+padVal(" jasmin", "Produce .jasmin files" )
+padVal(" c class (default)", "Produce .class Files" )
+padVal(" d dava", "Produce dava-decompiled .java files" )
+padVal(" t template", "Produce .java files with Jimple templates." )
+padOpt(" -outjar -output-jar", "Make output dir a Jar file instead of dir" )
+padOpt(" -xml-attributes", "Save tags to XML attributes for Eclipse" )
+padOpt(" -print-tags -print-tags-in-output", "Print tags in output files after stmt" )
+padOpt(" -no-output-source-file-attribute", "Don't output Source File Attribute when producing class files" )
+padOpt(" -no-output-inner-classes-attribute", "Don't output inner classes attribute in class files" )
+padOpt(" -dump-body PHASENAME", "Dump the internal representation of each method before and after phase PHASENAME" )
+padOpt(" -dump-cfg PHASENAME", "Dump the internal representation of each CFG constructed during phase PHASENAME" )
+padOpt(" -show-exception-dests", "Include exception destination edges as well as CFG edges in dumped CFGs" )
+padOpt(" -gzip", "GZip IR output files" )
+"\nProcessing Options:\n"
      
+padOpt(" -p PHASE OPT:VAL -phase-option PHASE OPT:VAL", "Set PHASE's OPT option to VALUE" )
+padOpt(" -O -optimize", "Perform intraprocedural optimizations" )
+padOpt(" -W -whole-optimize", "Perform whole program optimizations" )
+padOpt(" -via-grimp", "Convert to bytecode via Grimp instead of via Baf" )
+padOpt(" -via-shimple", "Enable Shimple SSA representation" )
+padOpt(" -throw-analysis ARG", "" )
+padVal(" pedantic (default)", "Pedantically conservative throw analysis" )
+padVal(" unit", "Unit Throw Analysis" )
+padOpt(" -omit-excepting-unit-edges", "Omit CFG edges to handlers from excepting units which lack side effects" )
+padOpt(" -trim-cfgs", "Trim unrealizable exceptional edges from CFGs" )
+"\nApplication Mode Options:\n"
      
+padOpt(" -i PKG -include PKG", "Include classes in PKG as application classes" )
+padOpt(" -x PKG -exclude PKG", "Exclude classes in PKG from application classes" )
+padOpt(" -include-all", "Set default excluded packages to empty list" )
+padOpt(" -dynamic-class CLASS", "Note that CLASS may be loaded dynamically" )
+padOpt(" -dynamic-dir DIR", "Mark all classes in DIR as potentially dynamic" )
+padOpt(" -dynamic-package PKG", "Marks classes in PKG as potentially dynamic" )
+"\nInput Attribute Options:\n"
      
+padOpt(" -keep-line-number", "Keep line number tables" )
+padOpt(" -keep-bytecode-offset -keep-offset", "Attach bytecode offset to IR" )
+"\nAnnotation Options:\n"
      
+padOpt(" -annot-purity", "Emit purity attributes" )
+padOpt(" -annot-nullpointer", "Emit null pointer attributes" )
+padOpt(" -annot-arraybounds", "Emit array bounds check attributes" )
+padOpt(" -annot-side-effect", "Emit side-effect attributes" )
+padOpt(" -annot-fieldrw", "Emit field read/write attributes" )
+"\nMiscellaneous Options:\n"
      
+padOpt(" -time", "Report time required for transformations" )
+padOpt(" -subtract-gc", "Subtract gc from time" );
    }


    public String getPhaseList() {
        return ""
    
        +padOpt("jb", "Creates a JimpleBody for each method")
        +padVal("jb.ls", "Local splitter: one local per DU-UD web")
        +padVal("jb.a", "Aggregator: removes some unnecessary copies")
        +padVal("jb.ule", "Unused local eliminator")
        +padVal("jb.tr", "Assigns types to locals")
        +padVal("jb.ulp", "Local packer: minimizes number of locals")
        +padVal("jb.lns", "Local name standardizer")
        +padVal("jb.cp", "Copy propagator")
        +padVal("jb.dae", "Dead assignment eliminator")
        +padVal("jb.cp-ule", "Post-copy propagation unused local eliminator")
        +padVal("jb.lp", "Local packer: minimizes number of locals")
        +padVal("jb.ne", "Nop eliminator")
        +padVal("jb.uce", "Unreachable code eliminator")
        +padVal("jb.tt", "Trap Tightener")
        +padOpt("jj", "Creates a JimpleBody for each method directly from source")
        +padVal("jj.ls", "Local splitter: one local per DU-UD web")
        +padVal("jj.a", "Aggregator: removes some unnecessary copies")
        +padVal("jj.ule", "Unused local eliminator")
        +padVal("jj.tr", "Assigns types to locals")
        +padVal("jj.ulp", "Local packer: minimizes number of locals")
        +padVal("jj.lns", "Local name standardizer")
        +padVal("jj.cp", "Copy propagator")
        +padVal("jj.dae", "Dead assignment eliminator")
        +padVal("jj.cp-ule", "Post-copy propagation unused local eliminator")
        +padVal("jj.lp", "Local packer: minimizes number of locals")
        +padVal("jj.ne", "Nop eliminator")
        +padVal("jj.uce", "Unreachable code eliminator")
        +padOpt("wjpp", "Whole Jimple Pre-processing Pack")
        +padOpt("wspp", "Whole Shimple Pre-processing Pack")
        +padOpt("cg", "Call graph constructor")
        +padVal("cg.cha", "Builds call graph using Class Hierarchy Analysis")
        +padVal("cg.spark", "Spark points-to analysis framework")
        +padVal("cg.paddle", "Paddle points-to analysis framework")
        +padOpt("wstp", "Whole-shimple transformation pack")
        +padOpt("wsop", "Whole-shimple optimization pack")
        +padOpt("wjtp", "Whole-jimple transformation pack")
        +padVal("wjtp.mhp", "Determines what statements may be run concurrently")
        +padVal("wjtp.tn", "Finds critical sections, allocates locks")
        +padOpt("wjop", "Whole-jimple optimization pack")
        +padVal("wjop.smb", "Static method binder: Devirtualizes monomorphic calls")
        +padVal("wjop.si", "Static inliner: inlines monomorphic calls")
        +padOpt("wjap", "Whole-jimple annotation pack: adds interprocedural tags")
        +padVal("wjap.ra", "Rectangular array finder")
        +padVal("wjap.umt", "Tags all unreachable methods")
        +padVal("wjap.uft", "Tags all unreachable fields")
        +padVal("wjap.tqt", "Tags all qualifiers that could be tighter")
        +padVal("wjap.cgg", "Creates graphical call graph.")
        +padVal("wjap.purity", "Emit purity attributes")
        +padOpt("shimple", "Sets parameters for Shimple SSA form")
        +padOpt("stp", "Shimple transformation pack")
        +padOpt("sop", "Shimple optimization pack")
        +padVal("sop.cpf", "Shimple constant propagator and folder")
        +padOpt("jtp", "Jimple transformation pack: intraprocedural analyses added to Soot")
        +padOpt("jop", "Jimple optimization pack (intraprocedural)")
        +padVal("jop.cse", "Common subexpression eliminator")
        +padVal("jop.bcm", "Busy code motion: unaggressive partial redundancy elimination")
        +padVal("jop.lcm", "Lazy code motion: aggressive partial redundancy elimination")
        +padVal("jop.cp", "Copy propagator")
        +padVal("jop.cpf", "Constant propagator and folder")
        +padVal("jop.cbf", "Conditional branch folder")
        +padVal("jop.dae", "Dead assignment eliminator")
        +padVal("jop.nce", "Null Check Eliminator")
        +padVal("jop.uce1", "Unreachable code eliminator, pass 1")
        +padVal("jop.ubf1", "Unconditional branch folder, pass 1")
        +padVal("jop.uce2", "Unreachable code eliminator, pass 2")
        +padVal("jop.ubf2", "Unconditional branch folder, pass 2")
        +padVal("jop.ule", "Unused local eliminator")
        +padOpt("jap", "Jimple annotation pack: adds intraprocedural tags")
        +padVal("jap.npc", "Null pointer checker")
        +padVal("jap.npcolorer", "Null pointer colourer: tags references for eclipse")
        +padVal("jap.abc", "Array bound checker")
        +padVal("jap.profiling", "Instruments null pointer and array checks")
        +padVal("jap.sea", "Side effect tagger")
        +padVal("jap.fieldrw", "Field read/write tagger")
        +padVal("jap.cgtagger", "Call graph tagger")
        +padVal("jap.parity", "Parity tagger")
        +padVal("jap.pat", "Colour-codes method parameters that may be aliased")
        +padVal("jap.lvtagger", "Creates color tags for live variables")
        +padVal("jap.rdtagger", "Creates link tags for reaching defs")
        +padVal("jap.che", "Indicates whether cast checks can be eliminated")
        +padVal("jap.umt", "Inserts assertions into unreachable methods")
        +padVal("jap.lit", "Tags loop invariants")
        +padVal("jap.aet", "Tags statements with sets of available expressions")
        +padVal("jap.dmt", "Tags dominators of statement")
        +padOpt("gb", "Creates a GrimpBody for each method")
        +padVal("gb.a1", "Aggregator: removes some copies, pre-folding")
        +padVal("gb.cf", "Constructor folder")
        +padVal("gb.a2", "Aggregator: removes some copies, post-folding")
        +padVal("gb.ule", "Unused local eliminator")
        +padOpt("gop", "Grimp optimization pack")
        +padOpt("bb", "Creates Baf bodies")
        +padVal("bb.lso", "Load store optimizer")
        +padVal("bb.pho", "Peephole optimizer")
        +padVal("bb.ule", "Unused local eliminator")
        +padVal("bb.lp", "Local packer: minimizes number of locals")
        +padOpt("bop", "Baf optimization pack")
        +padOpt("tag", "Tag aggregator: turns tags into attributes")
        +padVal("tag.ln", "Line number aggregator")
        +padVal("tag.an", "Array bounds and null pointer check aggregator")
        +padVal("tag.dep", "Dependence aggregator")
        +padVal("tag.fieldrw", "Field read/write aggregator")
        +padOpt("db", "Dummy phase to store options for Dava")
        +padVal("db.transformations", "The Dava back-end with all its transformations")
        +padVal("db.renamer", "Apply heuristics based naming of local variables")
        +padVal("db.deobfuscate", " Apply de-obfuscation analyses")
        +padVal("db.force-recompile", " Try to get recompilable code.");
    }

    public String getPhaseHelp( String phaseName ) {
    
        if( phaseName.equals( "jb" ) )
            return "Phase "+phaseName+":\n"+
                "\nJimple Body Creation creates a JimpleBody for each input \nmethod, using either coffi, to read .class files, or the jimple \nparser, to read .jimple files. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "use-original-names (false)", "" )
                +padOpt( "preserve-source-annotations (false)", "" );
    
        if( phaseName.equals( "jb.ls" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Local Splitter identifies DU-UD webs for local variables \nand introduces new variables so that each disjoint web is \nassociated with a single local. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" );
    
        if( phaseName.equals( "jb.a" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Jimple Local Aggregator removes some unnecessary copies by \ncombining local variables. Essentially, it finds definitions \nwhich have only a single use and, if it is safe to do so, \nremoves the original definition after replacing the use with the \ndefinition's right-hand side. At this stage in JimpleBody \nconstruction, local aggregation serves largely to remove the \ncopies to and from stack variables which simulate load and store \ninstructions in the original bytecode."
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-stack-locals (true)", "" );
    
        if( phaseName.equals( "jb.ule" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Unused Local Eliminator removes any unused locals from the \nmethod. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" );
    
        if( phaseName.equals( "jb.tr" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Type Assigner gives local variables types which will \naccommodate the values stored in them over the course of the \nmethod. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "ignore-wrong-staticness (false)", "Ignores errors due to wrong staticness" )
                +padOpt( "use-older-type-assigner (false)", "Enables the older type assigner" )
                +padOpt( "compare-type-assigners (false)", "Compares Ben Bellamy's and the older type assigner" );
    
        if( phaseName.equals( "jb.ulp" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Unsplit-originals Local Packer executes only when the \n`use-original-names' option is chosen for the `jb' phase. The \nLocal Packer attempts to minimize the number of local variables \nrequired in a method by reusing the same variable for disjoint \nDU-UD webs. Conceptually, it is the inverse of the Local \nSplitter. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "unsplit-original-locals (true)", "" );
    
        if( phaseName.equals( "jb.lns" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Local Name Standardizer assigns generic names to local \nvariables. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-stack-locals (false)", "" );
    
        if( phaseName.equals( "jb.cp" ) )
            return "Phase "+phaseName+":\n"+
                "\nThis phase performs cascaded copy propagation. If the \npropagator encounters situations of the form: A: a = ...; \n... B: x = a; ... C: ... = ... x; where a and x are \neach defined only once (at A and B, respectively), then it can \npropagate immediately without checking between B and C for \nredefinitions of a. In this case the propagator is global. \nOtherwise, if a has multiple definitions then the propagator \nchecks for redefinitions and propagates copies only within \nextended basic blocks. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-regular-locals (false)", "" )
                +padOpt( "only-stack-locals (true)", "" );
    
        if( phaseName.equals( "jb.dae" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Dead Assignment Eliminator eliminates assignment statements \nto locals whose values are not subsequently used, unless \nevaluating the right-hand side of the assignment may cause \nside-effects. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-stack-locals (true)", "" );
    
        if( phaseName.equals( "jb.cp-ule" ) )
            return "Phase "+phaseName+":\n"+
                "\nThis phase removes any locals that are unused after copy \npropagation. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" );
    
        if( phaseName.equals( "jb.lp" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Local Packer attempts to minimize the number of local \nvariables required in a method by reusing the same variable for \ndisjoint DU-UD webs. Conceptually, it is the inverse of the \nLocal Splitter. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (false)", "" )
                +padOpt( "unsplit-original-locals (false)", "" );
    
        if( phaseName.equals( "jb.ne" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Nop Eliminator removes nop statements from the method. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" );
    
        if( phaseName.equals( "jb.uce" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Unreachable Code Eliminator removes unreachable code and \ntraps whose catch blocks are empty. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "remove-unreachable-traps (false)", "" );
    
        if( phaseName.equals( "jb.tt" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Trap Tightener changes the area protected by each exception \nhandler, so that it begins with the first instruction in the old \nprotected area which is actually capable of throwing an \nexception caught by the handler, and ends just after the last \ninstruction in the old protected area which can throw an \nexception caught by the handler. This reduces the chance of \nproducing unverifiable code as a byproduct of pruning \nexceptional control flow within CFGs. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (false)", "" );
    
        if( phaseName.equals( "jj" ) )
            return "Phase "+phaseName+":\n"+
                "\nJimple Body Creation creates a JimpleBody for each input \nmethod, using polyglot, to read .java files. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "use-original-names (true)", "" );
    
        if( phaseName.equals( "jj.ls" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Local Splitter identifies DU-UD webs for local variables \nand introduces new variables so that each disjoint web is \nassociated with a single local. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (false)", "" );
    
        if( phaseName.equals( "jj.a" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Jimple Local Aggregator removes some unnecessary copies by \ncombining local variables. Essentially, it finds definitions \nwhich have only a single use and, if it is safe to do so, \nremoves the original definition after replacing the use with the \ndefinition's right-hand side. At this stage in JimpleBody \nconstruction, local aggregation serves largely to remove the \ncopies to and from stack variables which simulate load and store \ninstructions in the original bytecode."
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-stack-locals (true)", "" );
    
        if( phaseName.equals( "jj.ule" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Unused Local Eliminator removes any unused locals from the \nmethod. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" );
    
        if( phaseName.equals( "jj.tr" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Type Assigner gives local variables types which will \naccommodate the values stored in them over the course of the \nmethod. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (false)", "" );
    
        if( phaseName.equals( "jj.ulp" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Unsplit-originals Local Packer executes only when the \n`use-original-names' option is chosen for the `jb' phase. The \nLocal Packer attempts to minimize the number of local variables \nrequired in a method by reusing the same variable for disjoint \nDU-UD webs. Conceptually, it is the inverse of the Local \nSplitter. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (false)", "" )
                +padOpt( "unsplit-original-locals (false)", "" );
    
        if( phaseName.equals( "jj.lns" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Local Name Standardizer assigns generic names to local \nvariables. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-stack-locals (false)", "" );
    
        if( phaseName.equals( "jj.cp" ) )
            return "Phase "+phaseName+":\n"+
                "\nThis phase performs cascaded copy propagation. If the \npropagator encounters situations of the form: A: a = ...; \n... B: x = a; ... C: ... = ... x; where a and x are \neach defined only once (at A and B, respectively), then it can \npropagate immediately without checking between B and C for \nredefinitions of a. In this case the propagator is global. \nOtherwise, if a has multiple definitions then the propagator \nchecks for redefinitions and propagates copies only within \nextended basic blocks. "
                +"\n\nRecognized options (with default values):\n"
                +padOpt( "enabled (true)", "" )
                +padOpt( "only-regular-locals (false)", "" )
                +padOpt( "only-stack-locals (true)", "" );
    
        if( phaseName.equals( "jj.dae" ) )
            return "Phase "+phaseName+":\n"+
                "\nThe Dead Assignment Eliminator eliminates assignment statements \nto locals whose values are not subsequently used, unless \ne
