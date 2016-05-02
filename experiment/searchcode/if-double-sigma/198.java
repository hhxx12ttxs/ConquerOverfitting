// (c) MIT 2003.  All rights reserved.

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// AUTHOR:      Tevfik Metin Sezgin                                           //
//              Massachusetts Institute of Technology                         //
//              Department of Electrical Engineering and Computer Science     //
//              Artificial Intelligence Laboratory                            //
//                                                                            //
// E-MAIL:        mtsezgin@ai.mit.edu, mtsezgin@mit.edu                       //
//                                                                            //
// COPYRIGHT:   Tevfik Metin Sezgin                                           //
//              All rights reserved. This code can not be copied, modified,   //
//              or distributed in whole or partially without the written      //
//              permission of the author. Also see the COPYRIGHT file.        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
package edu.mit.sketch.ui;

  /**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: moltmans $
  * $Date: 2003/11/05 01:42:02 $
  * $Revision: 1.10 $
  * $Headers$
  * $Id: Tablet.java,v 1.10 2003/11/05 01:42:02 moltmans Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/ui/Tablet.java,v $
  *
  **/


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.PopupMenu;
import java.awt.MenuItem;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;

import java.awt.geom.AffineTransform;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.SpatialDatabase;
import edu.mit.sketch.geom.Segment;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.GraphicsUtil;
import edu.mit.sketch.util.Util;
import edu.mit.sketch.util.LinearFit;
import edu.mit.sketch.util.LoadStoreModule;
import edu.mit.sketch.toolkit.*;

import edu.mit.sketch.ddlcompiler.Segmentation;
import edu.mit.sketch.ddlcompiler.HMMAnalyzer;
import edu.mit.sketch.ddlcompiler.VariableLengthHMMAnalyzer;
/**
  * Acronyms: sdr -> StrokeDataReader, sd -> StrokeData
  *
  * This class handles the updates to various frames.
  *
  **/
public
class      Tablet
extends    TabletGUIBehavior
implements KeyListener,
           ActionListener
{
    /**
    *
    * The debugger
    *
    **/
    public TabletDebugger tablet_debugger;

    /**
    *
    * For painting vertices closest to the cursor.
    *
    **/
    public boolean vertex_identification = false;

    /**
    *
    * Be quiet, don't print junk..
    *
    **/
    public static boolean doing_batch_processing = false;

    /**
    *
    * Be quiet, don't print junk..
    *
    **/
    public static boolean very_quiet = true;

    /**
    *
    * false if jmatlink is launched and the rechmm is initialized
    *
    **/
    public static boolean hmm_initialized = false;

    /**
    *
    * jmatlink communicator
    *
    **/
    public static JMCommunicator communicator;

    /**
    *
    * Check this to see if we want to launch the sp graphs
    *
    **/
    public static boolean launch_shortest_path_graphs = false;
    
    /**
    *
    * The segmentation
    *
    **/
    public static int segmentation[];
    
    /**
    *
    * Popup for classes
    *
    **/
    public PopupMenu popup_menu;
    
    /**
    *
    * DataManager for all the data we have
    *
    **/
    public DataManager all_data;
    
    /**
    *
    * DataManager for all the variable_length_all_data we have
    *
    **/
    public VariableLengthDataManager variable_length_all_data;

    /**
    *
    * Name of the last hmm loaded in ML
    *
    **/
    public String last_hmm_file = "";

    /**
    *
    * HMM analyzer that computes segmentation etc. 
    * This analyzer uses variable length training data.
    *
    **/
    VariableLengthHMMAnalyzer vlhmm_analyzer;

    /**
    *
    * HMM analyzer that computes segmentation etc.
    *
    **/
    HMMAnalyzer analyzer;

    /**
    *
    * The constructor.
    *
    **/
    public Tablet( int     max_data_points,
                   String  cursor_image_name,
                   boolean platform_is_windows9x )
    {
        super( max_data_points,
               cursor_image_name,
               platform_is_windows9x );

        tablet_debugger = new TabletDebugger( this );


        popup_menu       = new PopupMenu( "Select Class" );
        
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            MenuItem menu_item = new MenuItem( object_manager.hmm_classes[i] );
            popup_menu.add( menu_item );
            menu_item.addActionListener( this );
        }
        
        add( popup_menu );
        addKeyListener( this );
        addAllComponentsAsKeyListeners();
        filename_field.removeKeyListener( this );
    }


    /**
    *
    * Add Components As Key Listeners
    *
    **/
    public void
    addAllComponentsAsKeyListeners()
    {
        Component components[] = AWTUtil.getAllComponentsRecursively( this );

        for ( int i=0; i<components.length; i++ ) {
            if ( ! ( components[i] instanceof Panel ) ) {
                components[i].addKeyListener( this );
            }
        }
        System.out.println( components.length + " components detected..." );
    }


    /**
    *
    * Recompute the training data.
    *
    **/
    public void 
    recomputeDataSet( String file_name ) 
    {
        loadCurrentDataSet( "g:\\research\\hmm_models\\training_data_file_names" );
        saveCurrentDataSetInMatlab( file_name );
    }


    /**
    *
    * Recompute the training data.
    *
    **/
    public void 
    recomputeVariableLengthDataSet( String file_name ) 
    {
        loadCurrentVariableLengthDataSet( "g:\\research\\hmm_models\\training_data_file_names" );
        saveCurrentVariableDataSetInMatlab( file_name );
    }


    /**
    *
    * expand data set with the input in the data file named string, and its 
    * segmentation
    *
    **/
    public void 
    expandDataSet( String file_name ) 
    {
        String current_data_files[]  = LoadStoreModule.loadStrings( "g:\\research\\hmm_models\\training_data_file_names" );
        
        String extended_data_files[] = new String[current_data_files.length + 1];
        
        for ( int i=0; i<current_data_files.length; i++ ) {
            extended_data_files[i] = current_data_files[i];
        }
        extended_data_files[extended_data_files.length-1] = file_name;
        
        LoadStoreModule.saveStrings( "g:\\research\\hmm_models\\training_data_file_names", extended_data_files );
        
        loadCurrentDataSet( "g:\\research\\hmm_models\\training_data_file_names" );
        saveCurrentDataSetInMatlab( "g:\\research\\hmm_models\\trained_sketch_hmm_model2_expanded" );
    }


    /**
    *
    * save current data in the all_data as a Matlab .m file along with the
    * trained HMMs
    *
    **/
    private void 
    saveCurrentDataSetInMatlab( String file_name ) 
    {
        ArrayList[] reorganized_training_data =
            all_data.getReorganizedTrainingData();
    
    
        communicator.evalString( "eval('clear data')" );
        communicator.evalString( "eval('O = 14')" );

        for ( int i=0; i<reorganized_training_data.length; i++ ) {
            communicator.evalString( "eval('data{" + (i+1) + "} = " +
                Util.array2DToMLMatrix(
                    Util.arrayList2DtoArray( reorganized_training_data[i] )
                    ) + "')");
        }
    
        communicator.evalString( "eval('rechmm_train')" );
    
        String new_input_names[] = new String[all_data.class_ranges[all_data.class_ranges.length-1]];
        int    new_input_ids[]   = new int[all_data.class_ranges[all_data.class_ranges.length-1]];
        for ( int i=0; i<all_data.class_ranges.length; i++ ) {
            for ( int j = (i==0) ? 0 : all_data.class_ranges[i-1]; j<all_data.class_ranges[i]; j++ ) {
                new_input_ids[j] = i;
            }
        }
    
        communicator.putIntArrayOriginal( "new_input_ids", new_input_ids );
        communicator.putIntArrayOriginal( "class_ranges",  all_data.class_ranges );
        communicator.putIntArrayOriginal( "input_lengths", all_data.input_lengths );

        communicator.evalString( "eval('save " + file_name + "')" );
    
        String date_string = ((Calendar.getInstance()).getTime()).toString();
        String buffer      = "";
    
        for ( int i=0; i<date_string.length(); i++ ) {
            if ( ! date_string.substring( i, i+1 ).equals( " " ) && ! date_string.substring( i, i+1 ).equals( ":" )  )
                buffer += date_string.substring( i, i+1 );
            if ( date_string.substring( i, i+1 ).equals( ":" ) )
                buffer += "_";
        }
        communicator.evalString( "eval('save g:\\research\\hmm_models\\backup\\trained_sketch_hmm_model" + buffer + "')" );

        doing_batch_processing = false;

    }


    /**
    *
    * save current data in the all_data as a Matlab .m file along with the
    * trained HMMs. The for each hmm, data elements may be of different 
    * lengths.
    *
    **/
    private void 
    saveCurrentVariableDataSetInMatlab( String file_name )
    {
        int input_lengths[][] = variable_length_all_data.input_lengths;
        doing_batch_processing = true;
        ArrayList[] reorganized_training_data =
            variable_length_all_data.getReorganizedTrainingData();
    
    
        communicator.evalString( "eval('clear data')" );
        communicator.evalString( "eval('O = 14')" );

        for ( int i=0; i<reorganized_training_data.length; i++ ) {
            communicator.evalString( "eval('data{" + (i+1) + "} = " +
                Util.arrayListToMLCellMatrix( reorganized_training_data[i] ) + "')");
        }

        communicator.evalString( "eval('rechmm_train')" );
    
        for ( int i=0; i<input_lengths.length; i++ ) {
            communicator.evalString( "eval('variable_input_lengths{" + (i+1) + "} = " +
                Util.arrayToMLMatrix( input_lengths[i] ) + "')");
        }

        String new_input_names[] = new String[input_lengths.length];
        int    new_input_ids[]   = new int[input_lengths.length];
        int    class_ranges[]    = new int[input_lengths.length];
        for ( int i=0; i<new_input_ids.length; i++ ) {
            new_input_ids[i] = i;
            class_ranges[i]  = i+1;
        }
    
        communicator.putIntArrayOriginal( "new_input_ids", new_input_ids );
        communicator.putIntArrayOriginal( "class_ranges",  class_ranges );

        communicator.evalString( "eval('save " + file_name + "')" );
    
        String date_string = ((Calendar.getInstance()).getTime()).toString();
        String buffer      = "";
    
        for ( int i=0; i<date_string.length(); i++ ) {
            if ( ! date_string.substring( i, i+1 ).equals( " " ) && ! date_string.substring( i, i+1 ).equals( ":" )  )
                buffer += date_string.substring( i, i+1 );
            if ( date_string.substring( i, i+1 ).equals( ":" ) )
                buffer += "_";
        }
        communicator.evalString( "eval('save g:\\research\\hmm_models\\backup\\trained_sketch_hmm_model" + buffer + "')" );

        doing_batch_processing = false;
    }
    
    
    /**
    *
    * load the files in the data_set_file_names file, process them,
    * save the results in files with the extension _processed
    *
    **/
    public void 
    processAndSaveRawData( String data_set_file_names ) 
    {
        doing_batch_processing = true;

        String training_data_files[] = LoadStoreModule.loadStrings( data_set_file_names );

        for ( int examples=0; examples<training_data_files.length; examples++ ) {
            object_manager.reset();
            object_manager.openRawVertices( training_data_files[examples] );
            object_manager.saveData( training_data_files[examples] + "_processed" );
        }
        doing_batch_processing = false;
    }    
    
    /**
    *
    * load the current available training data
    *
    **/
    public void 
    loadCurrentVariableLengthDataSet( String data_set_file_names ) 
    {
        doing_batch_processing = true;

        int    class_ranges[]        = new int[10];
        String training_data_files[] = LoadStoreModule.loadStrings( data_set_file_names );
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            class_ranges[i]        = i+1;
        }
    
        for ( int i=0; i<training_data_files.length; i++ ) {
            training_data_files[i] = training_data_files[i] + "_processed";
        }
    
        String input_names[] = object_manager.hmm_classes;
    
        VariableLengthDataManager data_manager = null;
    
        for ( int examples=0; examples<training_data_files.length; examples++ ) {
            object_manager.reset();
            object_manager.openAndAppendData( training_data_files[examples] );
    
            ArrayList packed_segmentation =
                LoadStoreModule.loadSegmentationForClass( training_data_files[examples] + "_segmentation" );
    
            ArrayList unpacked_segmentation = new ArrayList();
    
            int object_ids[]     = new int[packed_segmentation.size()];
            int ending_indices[] = new int[packed_segmentation.size()];
            for ( int i=0; i<packed_segmentation.size(); i++ ) {
                int pair[] = (int[])(packed_segmentation.get(i));
                for ( int j=0; j<pair[1]; j++ ) {
                    unpacked_segmentation.add( new Integer( pair[0] ) );
                }
                if ( i == 0 ) {
                    ending_indices[i] = pair[1];
                } else {
                    ending_indices[i] = ending_indices[i-1] + pair[1];
                }
                object_ids[i] = pair[0];
            }
            segmentation = Util.arrayListToIntArray( unpacked_segmentation );
            
            if ( data_manager == null ) {
                data_manager = new VariableLengthDataManager( new ArrayList( object_manager.objects ),
                                                              ending_indices,
                                                              object_ids,
                                                              input_names.length );
            } else {
                VariableLengthDataManager tmp_vldm = new VariableLengthDataManager( new ArrayList( object_manager.objects ),
                                                                           ending_indices,
                                                                           object_ids,
                                                                           input_names.length );
                data_manager.expandDataSet( tmp_vldm ); 
            }
        }
    
        variable_length_all_data = data_manager;
            
        doing_batch_processing = false;
    }
    
    /**
    *
    * load the current available training data
    *
    **/
    public void 
    loadCurrentDataSet( String data_set_file_names ) 
    {
        doing_batch_processing = true;

        int    class_ranges[]        = new int[10];
        String training_data_files[] = LoadStoreModule.loadStrings( data_set_file_names );
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            class_ranges[i]        = i+1;
        }
    
        for ( int i=0; i<training_data_files.length; i++ ) {
            training_data_files[i] = training_data_files[i] + "_processed";
        }
    
        String input_names[] = object_manager.hmm_classes;
    
        DataManager data_manager = null;
    
        for ( int examples=0; examples<training_data_files.length; examples++ ) {
            object_manager.reset();
            object_manager.openAndAppendData( training_data_files[examples] );
    
            ArrayList packed_segmentation =
                LoadStoreModule.loadSegmentationForClass( training_data_files[examples] + "_segmentation" );
    
            ArrayList unpacked_segmentation = new ArrayList();
    
            int object_ids[]     = new int[packed_segmentation.size()];
            int ending_indices[] = new int[packed_segmentation.size()];
            for ( int i=0; i<packed_segmentation.size(); i++ ) {
                int pair[] = (int[])(packed_segmentation.get(i));
                for ( int j=0; j<pair[1]; j++ ) {
                    unpacked_segmentation.add( new Integer( pair[0] ) );
                }
                if ( i == 0 ) {
                    ending_indices[i] = pair[1];
                } else {
                    ending_indices[i] = ending_indices[i-1] + pair[1];
                }
                object_ids[i] = pair[0];
            }
            segmentation = Util.arrayListToIntArray( unpacked_segmentation );
    

            doing_batch_processing = false;
            object_manager.paint( debug_graphics, segmentation, input_names );
            doing_batch_processing = true;
    
            DataManager tmp_data_manager = new DataManager( new ArrayList( object_manager.objects ),
                                                            ending_indices,
                                                            object_ids,
                                                            input_names.length );
            System.out.println( "\n\nTraining Data for " + training_data_files[examples] );
            Util.printTrainingData( tmp_data_manager.getReorganizedTrainingData() );
            Util.printArray( tmp_data_manager.class_ranges,  training_data_files[examples] + ".class_ranges"  );
            Util.printArray( tmp_data_manager.input_lengths, training_data_files[examples] + ".input_lengths" );
    
            System.out.println( "Trying to merge" );
            if ( data_manager == null ) {
                data_manager = tmp_data_manager;
            } else {
                data_manager = new DataManager( data_manager,
                                                tmp_data_manager );
            }
            System.out.println( "\n\nMerged Training Data objects" + training_data_files[examples]);
            Util.printTrainingData( data_manager.getReorganizedTrainingData() );
            Util.printArray( data_manager.class_ranges,  training_data_files[examples] + ".class_ranges"  );
            Util.printArray( data_manager.input_lengths, training_data_files[examples] + ".input_lengths" );
        }
    
        all_data = data_manager;
            
        doing_batch_processing = false;
    }


    /**
    *
    * actionPerformed. Handle popup menu events
    *
    **/
    public void 
    actionPerformed( ActionEvent e ) 
    {
        String command = e.getActionCommand();
        
        for ( int i=0; i<ObjectManager.hmm_classes.length; i++ ) {
            if ( command.equals( ObjectManager.hmm_classes[i] ) ) {
                System.out.println( ObjectManager.hmm_classes[i] + " selected" );
                object_manager.modifySegmentationForObject( 
                    object_manager.getBeginningIndex( object_manager.getSelectedObjects() ), 
                    object_manager.getEndingIndex( object_manager.getSelectedObjects() ), 
                    filename_field.getText(),
                    i );
            }
        }
        
        super.actionPerformed( e );
    }





    /**
    *
    * processMouseEvent
    *
    **/
    public void 
    processMouseEvent( MouseEvent e ) 
    {
        if ( e.isPopupTrigger() ) { 
            popup_menu.show( e.getComponent(), e.getX(), e.getY() );
        }
        super.processMouseEvent( e );
    }

    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyPressed( KeyEvent k )
    {
    }


    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyReleased( KeyEvent k )
    {
    }


    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyTyped( KeyEvent ke )
    {
        char key = ke.getKeyChar();

        if ( key == 'E' ) {
            //dd_dt_scale_space_viewer.printData( "dd_dt_scale_space_data_" +
            //                dd_dt_scale_space_viewer.interesting_scale );
            v_scale_space_viewer.printData( "v_scale_space_data_" +
                            v_scale_space_viewer.interesting_scale );
            return;
        }

        Vertex final_fit[]     = null;
        Vertex speed_fit[]     = null;
        Vertex direction_fit[] = null;
        Vertex points[]           = null;

        if ( classifier != null ) {
            final_fit        = classifier.final_fit;
            speed_fit        = classifier.speed_fit;
            direction_fit    = classifier.direction_fit;
            points           = classifier.points;
        }

        if ( key == 'V' ) {
            vertex_identification = !vertex_identification;
        }

        if ( key == 'c' ) {
            repaint();
            return;
        }

	if ( key == 'n' ) {
	  // perhaps this should clear too.
	  object_manager.openRawVertices_drg( filename_field.getText() );
	  return;
	}

        if ( key == '1' ) {
            debug = !debug;
            return;
        }

	
        if ( key == '2' ) {
            debug2 = !debug2;
            return;
        }

        if ( key == '3' ) {
            SpatialDatabase database = new SpatialDatabase();
            
            for ( int i=0; i<10000; i++ ) {
                database.addPoint( new Point( (int)(Math.random()*500),
                                              (int)(Math.random()*500) ) );
            }
            

            ArrayList query_results;
            
            debug_graphics.setColor( Color.red );
            for ( int k=0; k<10; k++ ){
            debug_graphics.setColor( Color.red );
                query_results = database.pointsInside( 100, 50, 200, 250 );

                for ( int i=0; i<query_results.size(); i++ ) {
                    Point p = (Point) query_results.get( i );
                    debug_graphics.drawOval( p.x-1, p.y-1, 2, 2 );
                }

                query_results = database.pointsInside( 150, 200, 300, 300 );
    
                debug_graphics.setColor( Color.green );
                for ( int i=0; i<query_results.size(); i++ ) {
                    Point p = (Point) query_results.get( i );
                    debug_graphics.drawOval( p.x-1, p.y-1, 2, 2 );
                }

                query_results = database.pointsInside( 200, 250, 350, 350 );
    
                debug_graphics.setColor( Color.blue );
                for ( int i=0; i<query_results.size(); i++ ) {
                    Point p = (Point) query_results.get( i );
                    debug_graphics.drawOval( p.x-1, p.y-1, 2, 2 );
                }
            }
            return;
        }

        if ( key == 'r' ) {
            object_manager.reset();
            GraphicsUtil.clearComponent( this );
            return;
        }

        if ( key == 't' ) {
            toggle();
        }

	if ( key == 'O' ) {
	    System.out.println( "Setting observations" );
	    LoadStoreModule.storeStringInFile( "c:\\matlab6p1\\work\\observations.sym",
					       DataManager.getEncoding2( new ArrayList( object_manager.objects ) ) );
	}

        if ( key == 'S' ) {
            deriving_scale_space = !deriving_scale_space;
            System.out.println( "Scale space = " + deriving_scale_space );
        }

        if ( key == '#' ) {
            setStatusMessage(
                "The general path has " +
                Blackboard.general_path.getSegmentCount() +
                " segments " );
            Blackboard.paintGeneralPathInSegments( Blackboard.general_path );
            Blackboard.paintGeneralPathInSegments( Blackboard.general_path );
        }

        if ( key == '$' ) {
            setStatusMessage(
                "The general path has " +
                Blackboard.general_path.getSegmentCount() +
                " segments " );
            Blackboard.paintGeneralPathConvexHulls( Blackboard.general_path );
        }

        if ( key == '%' ) {
            Blackboard.adjustGeneralPath( Blackboard.general_path, data );
        }

        if ( key == 'B' ) {
            GraphicsUtil.clearComponent( this );

            for ( int i=0; i<object_manager.stroke_vertices.size(); i++ ) {
                displayVerticesAsDots(
                    (Vertex[])object_manager.stroke_vertices.elementAt( i ) );
            }
            System.out.println( "Displaying stroke_vertices." );
        }

	/*  M13918 15000c-6,-514 14,-1513 -181,-2524 -69,-359 -321,-1049 -528,-1426 -202,-370 -409,-632 -631,-923 -206,-271 -493,-557 -854,-811 -757,-535 -932,-572 -1379,-745 -824,-319 -2137,-446 -2770,-449 -633,-4 -1512,83 -2192,220 -680,137 -1569,556 -2063,855 -1348,815 -2470,1971 -2975,3491 -353,1064 -345,2313 -345,2313
*/      
        if ( key == 'Y' ) {
	    float control_points[][] = {
	        { -6,-514,   14,-1513,   -181,-2524 },
		{ -69,-359,  -321,-1049, -528,-1426 },
		{ -202,-370, -409,-632,  -631,-923  },
		{ -206,-271, -493,-557,  -854,-811  },
		{ -757,-535, -932,-572,  -1379,-745 },
		{ -824,-319, -2137,-446, -2770,-449 },
		{ -633,-4,   -1512,83,   -2192,220  },
		{ -680,137,  -1569,556,  -2063,855  },
		{ -1348,815, -2470,1971, -2975,3491 },
		{ -353,1064, -345,2313,  -345,2313  }};

	    GeneralPath curve = new GeneralPath();
	    float start_x = 13.918f;
	    float start_y = 0.0f;

	    curve.moveTo( start_x, start_y );

	    // curve.moveTo( 13918, 15000 );
	    /*for ( int i=1; i<control_points.length; i++ ) {
		control_points[i][0] += control_points[i-1][4];
		control_points[i][1] += control_points[i-1][5];
		control_points[i][2] += control_points[i-1][4];
		control_points[i][3] += control_points[i-1][5];
		control_points[i][4] += control_points[i-1][4];
		control_points[i][5] += control_points[i-1][5];
	    }*/

	    float k = 1000.0f;
	    for ( int i=0; i<control_points.length; i++ ) {
		control_points[i][0] = control_points[i][0]/k;
		control_points[i][1] = control_points[i][1]/k;
		control_points[i][2] = control_points[i][2]/k;
		control_points[i][3] = control_points[i][3]/k;
		control_points[i][4] = control_points[i][4]/k;
		control_points[i][5] = control_points[i][5]/k;
	    }

	    for ( int i=1; i<control_points.length; i++ ) {
		control_points[i][0] += control_points[i-1][4];
		control_points[i][1] += control_points[i-1][5];
		control_points[i][2] += control_points[i-1][4];
		control_points[i][3] += control_points[i-1][5];
		control_points[i][4] += control_points[i-1][4];
		control_points[i][5] += control_points[i-1][5];
	    }


	    for ( int i=0; i<control_points.length; i++ ) {
		control_points[i][0] += start_x;
		control_points[i][1] += start_y;
		control_points[i][2] += start_x;
		control_points[i][3] += start_y;
		control_points[i][4] += start_x;
		control_points[i][5] += start_y;
	    }

	    for ( int i=0; i<control_points.length; i++ ) {		
		System.out.println( control_points[i][0] + ", " + control_points[i][1] + " " +
				    control_points[i][2] + ", " + control_points[i][3] + " " +
				    control_points[i][4] + ", " + control_points[i][5] );
	    }

	    for ( int i=0; i<control_points.length; i++ ) {
		curve.curveTo( control_points[i][0], control_points[i][1], 
			       control_points[i][2], control_points[i][3], 
			       control_points[i][4], control_points[i][5] );
		
	    }
	    
	    Line start = new Line( start_x, start_y, start_x, start_y);

	    Line line = new Line( start_x, start_y,
				  control_points[control_points.length-1][4], 
				  control_points[control_points.length-1][5] ); 

	    System.out.println( "Painting the curve" );
	    curve.paint( debug_graphics );
	    debug_graphics.setColor( Color.blue );
	    curve.paint( debug_graphics );
	    debug_graphics.setColor( Color.black );
	    start.paint( debug_graphics );
	    debug_graphics.setColor( Color.red );
	    line.paint( debug_graphics );
	    
	    // Set precision to .001 inches
	    double coordinates[][] = curve.flatten( .001 );

	    double curve_length = 0.0;
	    for ( int i=1; i<coordinates.length; i++ ) {
		curve_length += Math.sqrt(
		    (coordinates[i][0] - coordinates[i-1][0])* 
		    (coordinates[i][0] - coordinates[i-1][0]) + 

		    (coordinates[i][1] - coordinates[i-1][1]) *
		    (coordinates[i][1] - coordinates[i-1][1]) );
	    }	    
	    /*
	    double large_stave_theta = 5.0/8.0/6.879;
	    
	    
	    curve_length = 0.0;
	    System.out.print( "<path d=M 0 0 L 0 " + (2.43/2.54)/21.0/2.0 + " ");

	    for ( int i=1; i<coordinates.length; i++ ) {
		curve_length += Math.sqrt(
		    (coordinates[i][0] - coordinates[i-1][0])* 
		    (coordinates[i][0] - coordinates[i-1][0]) + 

		    (coordinates[i][1] - coordinates[i-1][1]) *
		    (coordinates[i][1] - coordinates[i-1][1]) );
		    

		System.out.print( "L " + curve_length + " " +
				  ( Math.abs(coordinates[i][1])*
				    (Math.PI-2*large_stave_theta) + 2.43/2.54 )/21.0/2.0 + " " );

	    }	    
	    System.out.println("/>");
	    */
	    curve_length = 0.0;

	    
	    double new_positions[][] = new double[coordinates.length][2];


	    for ( int factor = 0; factor<=45; factor++ ) {
	    
		new_positions[0][0] = coordinates[0][0];
		new_positions[0][1] = coordinates[0][1];
		
		double accumulated_angle = Math.PI/2;
		
		for ( int i=2; i<coordinates.length; i++ ) {
		    double angle0 = Math.atan2( coordinates[i-1][1] - coordinates[i-2][1],
						coordinates[i-1][0] - coordinates[i-2][0]);
		    double angle1 = Math.atan2( coordinates[i][1]   - coordinates[i-1][1],
						coordinates[i][0]   - coordinates[i-1][0]);
		    
		    double d_theta = angle1-angle0;
		    
		    		    
		    double delta_length = Math.sqrt(
		        (coordinates[i-1][0] - coordinates[i-2][0])* 
			(coordinates[i-1][0] - coordinates[i-2][0]) + 
			(coordinates[i-1][1] - coordinates[i-2][1]) *
			(coordinates[i-1][1] - coordinates[i-2][1]) );

		    curve_length += delta_length;
		    
		    accumulated_angle += d_theta - Math.PI/45.0/coordinates.length*factor;
		    new_positions[i-1][0] = new_positions[i-2][0] + 
			delta_length*Math.cos( accumulated_angle );
		    new_positions[i-1][1] = new_positions[i-2][1] + 
			delta_length*Math.sin( accumulated_angle );
		}	   


		System.out.print( "<path class=\"fill str0\"d=\"M " + coordinates[0][0] + " " + coordinates[0][1] + " ");
		for ( int i=1; i<new_positions.length-1; i++ ) {
		    System.out.print( "L " + new_positions[i][0] + " " +
				      new_positions[i][1] + " " );
		}
		System.out.println("\"/>");
	    } 

	    System.out.print( "<path class=\"fill str0\"d=\"M " + coordinates[0][0] + " " + coordinates[0][1] + " " );
	    for ( int i=1; i<coordinates.length; i++ ) {
		System.out.print( "L " + coordinates[i][0] + " " +
				  coordinates[i][1] + " " );
	    }
	    System.out.println("/>");
	}

        if ( key == 'i' ) {
            debug_graphics.setColor( Color.red );

            for ( int i=0; i<direction_fit.length-1; i++ ) {
                debug_graphics.drawLine( direction_fit[i].x,
                                         direction_fit[i].y,
                                         direction_fit[i+1].x,
                                         direction_fit[i+1].y );
            }

            for ( int i=0; i<direction_fit.length; i++ ) {
                debug_graphics.fillOval(
                    direction_fit[i].x-
                    (int)(direction_fit[i].certainty*20)/2,
                    direction_fit[i].y-
                    (int)(direction_fit[i].certainty*20)/2,
                    (int)(direction_fit[i].certainty*20),
                    (int)(direction_fit[i].certainty*20) );
            }
            debug_graphics.setColor( Color.white );
            for ( int i=0; i<direction_fit.length; i++ ) {
                debug_graphics.drawString(
                    direction_fit[i].index + "",
                    direction_fit[i].x,
                    direction_fit[i].y );
            }
            setStatusMessage( "Output obtained by direction information... " +
                              direction_fit.length );
            Util.printArray( direction_fit, "direction_fit" );
        }

        if ( key == 'k' ) {
            debug_graphics.setColor( Color.blue );

            for ( int i=0; i<speed_fit.length-1; i++ ) {
                debug_graphics.drawLine( speed_fit[i].x,
                                         speed_fit[i].y,
                                         speed_fit[i+1].x,
                                         speed_fit[i+1].y );
            }

            for ( int i=0; i<speed_fit.length; i++ ) {
                debug_graphics.fillOval(
                    speed_fit[i].x -
                        (int)(speed_fit[i].certainty*20)/2,
                    speed_fit[i].y -
                        (int)(speed_fit[i].certainty*20)/2,
                    (int)(speed_fit[i].certainty*20),
                    (int)(speed_fit[i].certainty*20) );
            }
            debug_graphics.setColor( Color.black );
            for ( int i=0; i<speed_fit.length; i++ ) {
                debug_graphics.drawString(
                    speed_fit[i].index + "",
                    speed_fit[i].x,
                    speed_fit[i].y );
            }
            setStatusMessage( "Output obtained by speed information... " +
                                speed_fit.length );
            Util.printArray( speed_fit, "speed_fit" );
        }

        if ( key == '\\' ) {
            launch_shortest_path_graphs = !launch_shortest_path_graphs;
            System.out.println( "launch_shortest_path_graphs = " + 
                launch_shortest_path_graphs );
        }

        // Load the raw input, (indicated by the textbox int the TabletGUI)
        // perform segmentation, save their segmentation 
        if ( key == '&' ) {
            doing_batch_processing = true;
            if ( ! hmm_initialized ) {  
                System.out.println( "Initializing JMC" );
                  communicator = new JMCommunicator();


                System.out.println( "Preparing the HMM" );
                communicator.evalString( "eval('startup')" );
                communicator.evalString( "eval('load g:\\research\\hmm_models\\trained_sketch_hmm_model')" );
                System.out.println( "HMM ready" );
                hmm_initialized = true;
            }

            // new_input_ids are the object numbers for each hmm
            // class ranges indicate the ending indices for each HMM
            int input_names_int[] = communicator.getIntArrayOriginal( "new_input_ids" );
            int class_ranges[]    = communicator.getIntArrayOriginal( "class_ranges"  );
            int input_lengths[]   = communicator.getIntArrayOriginal( "input_lengths" );

            String input_names[] = new String[input_names_int.length];
            for ( int i=0; i<input_names_int.length; i++ ) {
                input_names[i] = "_" + input_names_int[i];
            }
            String training_data_files[] = new String[1];
            training_data_files[0]       = filename_field.getText();
            
            for ( int examples=0; examples<training_data_files.length; examples++ ) {
                object_manager.reset();
                object_manager.openAndAppendData( training_data_files[examples] ); 
                
                System.out.println( "Setting observations for " + training_data_files[examples] );
                LoadStoreModule.storeStringInFile( "c:\\matlab6p1\\work\\observations.sym",
                                                    DataManager.getEncoding2( new ArrayList( object_manager.objects ) ) );
    
                System.out.println( "computing final_sums" );
                communicator.evalString( "eval('compute_final_sums')" );

                System.out.println( "getting final_sums " );
                double final_sums[][] = communicator.getArrayOriginal( "final_sums" );
    
                analyzer = new HMMAnalyzer( input_lengths, final_sums );
    
                segmentation = analyzer.getSegmentation();

                doing_batch_processing = false;
                object_manager.paint( debug_graphics, segmentation, input_names );
                doing_batch_processing = true;

                int ending_indices[] = DataManager.getEndingIndices( segmentation, input_lengths, new ArrayList( object_manager.objects ) );
                int object_ids[]     = DataManager.getObjectIds( segmentation, input_lengths, class_ranges );
                
                LoadStoreModule.saveSegmentationForClass( training_data_files[examples] + "_segmentation", ending_indices, object_ids );
            }
            doing_batch_processing = false;
            return;
        }
        
        // Load the input processed by the toolkit, load their segmentation and 
        // setup new training data with the new encoding, train hmms, save the
        // ml environment in trained_sketch_hmm_model2
        if ( key == '*' ) {
            recomputeDataSet( "g:\\research\\hmm_models\\trained_sketch_hmm_model2" );
        }

        // Check for arc
        if ( key == '(' ) {
            System.out.println( "creating classifier");
            SimpleClassifier2 cl = new SimpleClassifier2( data );
            System.out.println( "created classifier");
            
            System.out.println( "calling classifier");
            System.out.println( "classification = " + cl.classify() );
            System.out.println( "called classifier");
            
            if ( cl.classify() == cl.ARC ) {
                System.out.println( "classifier got an arc");
                System.out.println( "painting");
                cl.getArcApproximation().paint( debug_graphics );
                                
                System.out.println( "painted");
                return;
            }
            System.out.println( "didn't get an arc");
            return;
        }
	/*
        // Simply compute and show the segmentation
        if ( key == '`' ) {
            RibbonITBuilder builder = new RibbonITBuilder(5);
	    for ( int i=0; i<object_manager.objects.size(); i++ ) {
		if ( object_manager.objects.get(i) instanceof Line )
		    builder.addLine( (Line) object_manager.objects.get(i) );
	    }

	    String file_name = "c:\\graph" + object_manager.objects.size();
	    LoadStoreModule.storeStringInFile( file_name + ".dot",
					       builder.it.toDot(this, object_manager) );

	    try {
		String command = "dot " + file_name + ".dot -Tgif > " + file_name + ".gif";
		System.out.println( "Executing " + command );
		Runtime.getRuntime().exec( command );//.waitFor();
		System.out.println( "Executed " + command );
	    } catch( Exception e ) {
		System.out.println( e );
	    }

	    System.out.println( "Final tree " + builder.it );
	    }*/

        // Simply compute and show the segmentation
        if ( key == '|' ) {
            computeAndShowSegmentationWithModel( "g:\\research\\hmm_models\\trained_sketch_hmm_model2" );
        }

        // Simply compute and show the segmentation with the expanded model
        if ( key == ')' ) {
            computeAndShowSegmentationWithModel( "g:\\research\\hmm_models\\trained_sketch_hmm_model2_expanded" );
        }
        
        if ( key == 'K' ) {
            recomputeVariableLengthDataSet( "g:\\research\\hmm_models\\trained_sketch_variable_length_hmm_model" );
        }
        
        // Simply compute and show the segmentation with the expanded model
        if ( key == 'L' ) {
            computeAndShowSegmentationWithVariableLengthModel( "g:\\research\\hmm_models\\trained_sketch_variable_length_hmm_model" );
        }
        
        // Simply compute and show the segmentation with the expanded model
        if ( key == '9' ) {
            processAndSaveRawData( "g:\\research\\hmm_models\\training_data_file_names" );
        }
        
        // Paint segmentation
        if ( key == '^' ) {
            String training_data_files[] = new String[object_manager.hmm_classes.length];
            
            for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
                training_data_files[i] = object_manager.hmm_classes[i] + "_processed";
            }
            
            int input_ranges[]   = communicator.getIntArrayOriginal( "class_ranges"  );
            int input_lengths[]  = communicator.getIntArrayOriginal( "input_lengths" );
            int ending_indices[] = DataManager.getEndingIndices( segmentation, input_lengths, new ArrayList( object_manager.objects ) );
            int object_ids[]     = DataManager.getObjectIds( segmentation, input_lengths, input_ranges );

            object_manager.paint( debug_graphics, ending_indices, object_ids, training_data_files );
            return;
        }
        
        if ( key == 'o' ) {
            debug_graphics.setColor( Color.red );

            for ( int i=0; i<direction_fit.length-1; i++ ) {
                debug_graphics.drawLine( direction_fit[i].x,
                                         direction_fit[i].y,
                                         direction_fit[i+1].x,
                                         direction_fit[i+1].y );
            }

            setStatusMessage( "Output obtained by direction information... " +
                              direction_fit.length );
            Util.printArray( direction_fit, "direction_fit" );


        }

        if ( key == 'l' ) {
            debug_graphics.setColor( Color.blue );

            for ( int i=0; i<speed_fit.length-1; i++ ) {
                debug_graphics.drawLine( speed_fit[i].x,
                                         speed_fit[i].y,
                                         speed_fit[i+1].x,
                                         speed_fit[i+1].y );
            }

            setStatusMessage( "Output obtained by speed information... " +
                                speed_fit.length);
            Util.printArray( speed_fit, "speed_fit" );
        }

        if ( key == '+' ) {
            Color  colors[] = { Color.red, Color.green, Color.blue };
            for ( int i=0; i<data.speed_scale_space.length;i ++ ) {
                data.speed = data.speed_scale_space[i];
                paintVelocity( data, colors[i%3] );
            }
        }

        if ( key == 'p' ) {
            for ( int i=0; i<data.vertices.length; i++ ) {
                System.out.println( data.vertices[i] );
            }
        }

        if ( key == 'w' ) {
            bar_statistics_module.setVisible(
                !bar_statistics_module.isVisible() );

            dsw_statistics_module.setVisible(
                !dsw_statistics_module.isVisible() );

            ddsw_statistics_module.setVisible(
                !ddsw_statistics_module.isVisible() );

            sliding_window_statistics_module.setVisible(
                !sliding_window_statistics_module.isVisible() );

            System.out.println( "Statistics window." );
            return;
        }

        if ( key == 'W' ) {
            v_window.setVisible(
                !v_window.isVisible() );
            dd_dt_window.setVisible(
                !dd_dt_window.isVisible() );

            System.out.println( "Scale space veiwers." );
            return;
        }


        if ( key == '0' ) {
            for ( int i=0; i<object_manager.stroke_vertices.size(); i++ ) {
                displayVertices(
                    (Vertex[])object_manager.stroke_vertices.elementAt( i ) );
            }
            System.out.println( "Displaying stroke_vertices." );
            return;
        }

        if ( key == 'e' ) {
            dsw_statistics_module.setVisible(
                !dsw_statistics_module.isVisible() );
            ddsw_statistics_module.setVisible(
                !ddsw_statistics_module.isVisible() );

            System.out.println( "Statistics window." );
            return;
        }

        if ( key == 'b' ) {
            double d[] = data.d;
            setStatusMessage( "Direction obtained by " +
                             LinearFit.fitMethodToString( data.fit_method ) );
            debug_graphics.setColor( Color.green );

            for ( int i=0; i<d.length; i++ ) {
                double a = d[i];
                int mid_point_x = data.vertices[i].x;
                int mid_point_y = data.vertices[i].y;
                int dx          = (int)(Math.cos(a)*100);
                int dy          = (int)(Math.sin(a)*100);
                debug_graphics.drawLine( mid_point_x - dx,
                                          mid_point_y - dy,
                                          mid_point_x + dx,
                                          mid_point_y + dy );
            }
        }

        if ( key == 'v' ) {
            Vector paths = object_manager.paths;

            if ( paths == null )
                System.out.println( "paths is null" );
            for ( int i=0; i<paths.size(); i++ ) {
                ((GeneralPath) paths.elementAt(i)).paint(
                    (Graphics2D)debug_graphics );
                if ( paths.elementAt(i) == null )
                    System.out.println( "paths is null" );
            }

            return;
        }

        if ( key == 'z' ) {
            object_manager.undo();
            return;
        }

        if ( key == 'A' ) {
            System.out.println("calling the thinning routine");
            //CloudThinner.thinCloud( data.vertices );
            System.out.println("thinned??");
            return;
        }

        if ( key == 'Z' ) {
            classifier.reclassify();
        }
	/*
        if ( key == '`' ) {
            very_quiet = !very_quiet;
            System.out.println("very_quiet = " + very_quiet );
            return;
	    }*/

        if ( key == 'q' ) {
            System.exit( 0 );
            return;
        }
    }

    public void
    computeAndShowSegmentationWithVariableLengthModel( String model_name )
    {
        String training_data_files[] = new String[object_manager.hmm_classes.length];
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            training_data_files[i] = object_manager.hmm_classes[i] + "_processed";
        }
    
        doing_batch_processing = true;

        if ( !hmm_initialized || !last_hmm_file.equals( model_name ) ) {
            System.out.println( "Initializing JMC" );
              communicator = new JMCommunicator();

            System.out.println( "Preparing the HMM" );
            communicator.evalString( "eval('startup')" );
            communicator.setDebug( true );
            communicator.evalString( "eval('load " + model_name + "')" );
            System.out.println( "HMM ready" );
            hmm_initialized = true;
            last_hmm_file   = model_name;
        }

        int input_lengths[][] = new int[object_manager.hmm_classes.length][1];

        for ( int i=0; i<input_lengths.length; i++ ) {
            input_lengths[i] = communicator.getIntCellArray( "variable_input_lengths{" + (i+1) + "}" );
        }

        System.out.println( "Setting observations" );
        LoadStoreModule.storeStringInFile( "c:\\matlab6p1\\work\\observations.sym",
                                            DataManager.getEncoding2( new ArrayList( object_manager.objects ) ) );
    
        System.out.println( "computing final_sums" );
        communicator.evalString( "eval('compute_vlhmm_final_sums')" );

        System.out.println( "getting final_sums " );
        double final_sums[][][] = communicator.get3DArray( "scores" ); 

        System.out.println( "getting ending state probabilities " );
        double qend_probabilities[][][] = communicator.get3DArray( "final_state_probabilities" ); 

        System.out.println( "getting ending state probabilities " );
        int viterbi_paths[][][][] = communicator.get3DCellWithIntArrayElements( "obs_viterbi_path" ); 
        
        double scores[][][]    = new double[final_sums.length][final_sums[0].length][1];
        
        for ( int i=0; i<final_sums.length; i++ ) {
            for ( int j=0; j<final_sums[i].length; j++ ) {
                scores[i][j] = new double[input_lengths[i].length];
                for ( int k=0; k<input_lengths[i].length; k++ ) {
                    scores[i][j][k] = final_sums[i][j][k];
                }
            }
        }
        
	
        vlhmm_analyzer = new VariableLengthHMMAnalyzer( input_lengths, scores, qend_probabilities, viterbi_paths );
    
        segmentation = vlhmm_analyzer.getSegmentation();
    
        PathBrowser segmentation_browser = new PathBrowser( this, vlhmm_analyzer.getPaths(), true );
    
        Util.printArrayConcisely( segmentation, "segmentation" );
        doing_batch_processing = false;


	int result[][] = VariableLengthDataManager.getEndingIndicesAndObjectIDs2( segmentation, (int[])(vlhmm_analyzer.getPaths().get(0)), input_lengths, new ArrayList( object_manager.objects ) );

        int ending_indices[] = result[0];
	int object_ids[]     = result[1];
        Util.printArrayConcisely( ending_indices, "ending_indices" );
        Util.printArrayConcisely( object_ids, "object_ids" );

        object_manager.paint( debug_graphics, ending_indices, object_ids, training_data_files );
    }

    public void
    computeAndShowSegmentationWithModel( String model_name )
    {
        String training_data_files[] = new String[object_manager.hmm_classes.length];
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            training_data_files[i] = object_manager.hmm_classes[i] + "_processed";
        }
        doing_batch_processing = true;

        if ( !hmm_initialized || !last_hmm_file.equals( model_name ) ) {
            System.out.println( "Initializing JMC" );
              communicator = new JMCommunicator();

            System.out.println( "Preparing the HMM" );
            communicator.evalString( "eval('startup')" );
            communicator.setDebug( true );
            communicator.evalString( "eval('load " + model_name + "')" );
            System.out.println( "HMM ready" );
            hmm_initialized = true;
            last_hmm_file   = model_name;
        }
    
        int input_names_int[] = communicator.getIntArrayOriginal( "new_input_ids" );
        int input_ranges[]    = communicator.getIntArrayOriginal( "class_ranges"  );
        int input_lengths[]   = communicator.getIntArrayOriginal( "input_lengths" );

        String input_names[] = new String[input_names_int.length];
        for ( int i=0; i<input_names_int.length; i++ ) {
            input_names[i] = "_" + input_names_int[i];
        }

        System.out.println( "Setting observations" );
        LoadStoreModule.storeStringInFile( "c:\\matlab6p1\\work\\observations.sym",
                                            DataManager.getEncoding2( new ArrayList( object_manager.objects ) ) );
    
        System.out.println( "computing final_sums" );
        communicator.evalString( "eval('compute_final_sums')" );

        System.out.println( "getting final_sums " );
        double final_sums[][] = communicator.getArrayOriginal( "final_sums" );
    
        analyzer = new HMMAnalyzer( input_lengths, final_sums );
    
        segmentation = analyzer.getSegmentation();
    
        //SegmentationBrowser segmentation_browser = new SegmentationBrowser( this, final_sums, input_lengths );
        PathBrowser segmentation_browser = new PathBrowser( this, analyzer.getPaths(), false );
    
        Util.printArrayConcisely( segmentation, "segmentation" );
        doing_batch_processing = false;

        int ending_indices[] = DataManager.getEndingIndices( segmentation, input_lengths, new ArrayList( object_manager.objects ) );
        int object_ids[]     = DataManager.getObjectIds( segmentation, input_lengths, input_ranges );
    
        Util.printArrayConcisely( ending_indices, "ending_indices" );
        Util.printArrayConcisely( object_ids, "object_ids" );

        object_manager.paint( debug_graphics, ending_indices, object_ids, training_data_files );
    }



    public double
    gauss( double x, double sigma )
    {
        return Math.pow( Math.E, -( (x*x) / (2*sigma*sigma) ) );
    }


    public void
    paintVLHMMSegmentation( int shortest_path[] )
    {
        int input_lengths[][]        = new int[object_manager.hmm_classes.length][1];
        String training_data_files[] = new String[object_manager.hmm_classes.length];
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            training_data_files[i] = object_manager.hmm_classes[i] + "_processed";
        }

        for ( int i=0; i<input_lengths.length; i++ ) {
            input_lengths[i] = communicator.getIntCellArray( "variable_input_lengths{" + (i+1) + "}" );
        }
    
        segmentation = vlhmm_analyzer.getSegmentation( shortest_path );
        Util.printArrayConcisely( segmentation, "segmentation" );
    
	int result[][] = VariableLengthDataManager.getEndingIndicesAndObjectIDs2( segmentation, shortest_path, input_lengths, new ArrayList( object_manager.objects ) );
        int ending_indices[] = result[0];
	int object_ids[]     = result[1];

        Util.printArrayConcisely( ending_indices, "ending_indices" );
        Util.printArrayConcisely( object_ids, "object_ids" );

        object_manager.paint( debug_graphics, ending_indices, object_ids, training_data_files );
    }


    public void
    paintSegmentation( int shortest_path[] )
    {
        String training_data_files[] = new String[object_manager.hmm_classes.length];
    
        for ( int i=0; i<object_manager.hmm_classes.length; i++ ) {
            training_data_files[i] = object_manager.hmm_classes[i] + "_processed";
        }
    
        int input_ranges[]    = communicator.getIntArrayOriginal( "class_ranges"  );
        int input_lengths[]   = communicator.getIntArrayOriginal( "input_lengths" );

        int segmentation[] = analyzer.getSegmentation( shortest_path );
        Util.printArrayConcisely( segmentation, "segmentation" );

        int ending_indices[] = DataManager.getEndingIndices( segmentation, input_lengths, new ArrayList( object_manager.objects ) );
        int object_ids[]     = DataManager.getObjectIds( segmentation, input_lengths, input_ranges );
    
        Util.printArrayConcisely( ending_indices, "ending_indices" );
        Util.printArrayConcisely( object_ids, "object_ids" );

        object_manager.paint( debug_graphics, ending_indices, object_ids, training_data_files );
    }
}

/**
  *
  * $Log: Tablet.java,v $
  * Revision 1.10  2003/11/05 01:42:02  moltmans
  * Found more ^M's  They should all be gone now... Again...  For good?
  *
  * Revision 1.9  2003/10/13 03:10:50  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.8  2003/06/26 19:57:15  calvarad
  * Lots of bug fixes
  *
  * Revision 1.7  2003/06/17 21:20:53  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2003/06/16 17:14:37  mtsezgin
  *
  * Changes to the GUI.
  *
  * Revision 1.5  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.4  2002/07/22 21:00:40  mtsezgin
  * Modified to make it work with the new classifier: SimpleClassifier2
  *
  * Revision 1.3  2001/11/24 20:59:33  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.2  2001/11/24 20:58:15  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1  2001/11/23 03:24:13  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.4  2001/10/12 23:32:29  mtsezgin
  * Turned off printing...
  *
  * Revision 1.3  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
  *
  * Revision 1.2  2001/04/05 19:02:52  mtsezgin
  * The files needed to make the dll.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.26  1000/09/20 20:07:36  mtsezgin
  * This is a working version with curve recognition and curve
  * refinement. The GeneralPath approximation is refined if needed
  * to result in a better fit.
  *
  * Revision 1.25  1000/09/06 22:40:59  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  *
  *
  **/

