    package cnslab.cnsnetwork;

    import java.io.*;
    import java.util.*;

    import jpvm.*;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import cnslab.cnsmath.*;
    import edu.jhu.mb.ernst.model.ModelFactory;
    import edu.jhu.mb.ernst.model.Synapse;
    import edu.jhu.mb.ernst.model.MutableSynapse;

    /***********************************************************************
    * Organize layers of neurons. 
    * 
    * @version
    *   $Date: 2012-08-04 20:43:22 +0200 (Sat, 04 Aug 2012) $
    *   $Rev: 104 $
    *   $Author: croft $
    * @author
    *   Yi Dong
    * @author
    *   David Wallace Croft
    ***********************************************************************/
    public final class  LayerStructure
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {
      
    private static final Class<LayerStructure>
      CLASS = LayerStructure.class;

    private static final Logger
      LOGGER = LoggerFactory.getLogger ( CLASS );
    
    //
  
    public static final int
      LINEMAP = 0,
      ABMAP   = 1;

    public static int
      sectionsInAxisX, // previously named A
      sectionsInAxisY; // previously named B
    
    //

    public Map<String, Integer>  prefixSuffixToLayerIndexMap;
    
    /** for slow cellmap */
    public Map<String, Integer>  indexmap;
    
    /** Remove maps after setup core!!! */
    @Deprecated
    public String [ ]  celltype;

    @Deprecated
    public Map<String, TreeSet<Synapse>> [ ]  branchmaps;

    /** Scaffold is used to help to build up the network structure,
    which should be private */
    public Map<Integer, Map<String, SizeID>>  scaffold;

    public Map<Integer, Axon>  axons;
    
    public List<Layer> layerList;
    
    public int
      edgeLengthX,
      edgeLengthY;
    
    public int
      numberOfNeurons;
    
    /** end index in neuron for each node; .length=number parallel nodes */
    public int [ ]
      nodeEndIndices;
    
    private int
      nodeCount; // formerly named nodeNumber

    /** holds reference to neuron array after sent to network */
    public Neuron [ ]
      neurons;

    public int
      base,
      neuron_end,
      back_idum2,
      back_iy;      
    
    public int [ ]
      back_iv = new int [ 32 ];
    
    public int
      backseed;
    
    public Seed
      idum;

    public int
      numSYN;
    
    // private final instance variables
    
    private final ModelFactory  modelFactory;

    ////////////////////////////////////////////////////////////////////////
    // constructor methods
    ////////////////////////////////////////////////////////////////////////

    public  LayerStructure (
      final ModelFactory  modelFactory,
      final int           nodeCount,
      final int           edgeLengthX,
      final int           edgeLengthY )
    ////////////////////////////////////////////////////////////////////////
    {
      this.modelFactory = modelFactory;
      
      numSYN = 0;
      
      base = 0;
      
      // cellmap=new TreeMap<String, Integer>();
      
      prefixSuffixToLayerIndexMap = new TreeMap<String, Integer> ( );        
      
      indexmap = new TreeMap<String, Integer> ( );
      
      layerList = new LinkedList<Layer> ( );    

      this.edgeLengthX = edgeLengthX;
      
      this.edgeLengthY = edgeLengthY;

      this.nodeCount = nodeCount;

      //numberOfNeurons=nodeIndices[nodeIndices.length-1] + 1;
      //nodeEndIndices=nodeIndices;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    /***********************************************************************
    * set up the base neuron index for NetHost hostId
    ***********************************************************************/
    public void  neuronIndex ( final int  hostId )
    ////////////////////////////////////////////////////////////////////////
    {
      neuron_end = nodeEndIndices [ hostId ];
      
      base = 0;
      
      if ( hostId != 0 )
      {
        base = nodeEndIndices [ hostId - 1 ] + 1;
      }
    }

    /***********************************************************************
    * Add one layer of neurons into the structure.
    * 
    * @param pre
    * @param suf
    * @param mult
    * @param type
    ***********************************************************************/
    public void  addLayer (
      final String  pre,
      final String  suf,
      final int     mult,
      final String  type )
    ////////////////////////////////////////////////////////////////////////
    {
      final int  xMul = mult;
      
      final int  yMul = mult;
      
      if ( mult == 0 )
      {
        throw new RuntimeException ( "No zero multiplier is allowed" );
      }
      
      if( xMul < 0 )
      {
        if ( edgeLengthX % xMul != 0 )
        {
          throw new RuntimeException (
            xMul + "is not a multiplier of x-edge length" + edgeLengthX );
        }
      }
      
      if ( yMul < 0 )
      {
        if ( edgeLengthY % yMul != 0 )
        {
          throw new RuntimeException (
            yMul + "is not a multiplier of y-edge length" + edgeLengthY );
        }
      }

      final Layer  tmp_layer = new Layer ( pre, suf, mult, type );
      
      layerList.add ( tmp_layer );
      
      prefixSuffixToLayerIndexMap.put (
        pre + "," + suf,
        Integer.valueOf ( layerList.size ( ) - 1 ) );
    }

    public void  addLayer (
      final String  pre,
      final String  suf,
      final int     xmult,
      final int     ymult,
      final String  type )
    ////////////////////////////////////////////////////////////////////////
    {
      final int  xMul = xmult;
      
      final int  yMul = ymult;
      
      if ( xmult == 0 )
      {
        throw new RuntimeException ( "No zero multiplier is allowed" );
      }
      
      if ( xMul < 0 )
      {
        if ( edgeLengthX % xMul != 0 )
        {
          throw new RuntimeException (
            xMul + "is not a multiplier of x-edge length" + edgeLengthX );
        }
      }
      
      if( yMul < 0 )
      {
        if ( edgeLengthY % yMul !=0 )
        {
          throw new RuntimeException (
            yMul + "is not a multiplier of y-edge length" + edgeLengthY );
        }
      }

      final Layer  tmp_layer = new Layer ( pre, suf, xmult, ymult, type );
      
      layerList.add ( tmp_layer );
      
      prefixSuffixToLayerIndexMap.put (
        pre + "," + suf,
        Integer.valueOf ( layerList.size ( ) - 1 ) );
    }

    /***********************************************************************
    * Map the cells in linear fashion.
    ***********************************************************************/
    public void  mapcells ( )
    ////////////////////////////////////////////////////////////////////////
    {
      int
        xstep,
        ystep,
        xresosize,
        yresosize;
      
      numberOfNeurons = totalNumNeurons ( );
      
      nodeEndIndices = new int [ nodeCount ];

      // celltype = new String [ numberOfNeurons ];

      int  n_neuron = 0;
      
      // String  str;
      
      final int  layerListSize = layerList.size ( );

      for ( int  layerIndex = 0; layerIndex < layerListSize; layerIndex++ )
      {
        final Layer  layer = layerList.get ( layerIndex );
        
        final int  layerMultiplierX = layer.getMultiplierX ( );
        
        if ( layerMultiplierX > 0 )
        {
          xstep = 1;
          
          xresosize = layerMultiplierX * edgeLengthX;
        }
        else
        {
          xstep = -layerMultiplierX;
          
          xresosize = edgeLengthX;
        }
        
        final int  layerMultiplierY = layer.getMultiplierY ( );
        
        if ( layerMultiplierY > 0 )
        {
          ystep = 1;
          
          yresosize = layerMultiplierY * edgeLengthY;
        }
        else
        {
          ystep = -layerMultiplierY;
          
          yresosize = edgeLengthY;
        }
        
        for ( int  x = 0; x < xresosize; x += xstep )
        {
          for ( int  y = 0; y < yresosize; y += ystep )
          {
            // str=(layer_structure.get(iter2)).getPrefix()+"," + iter
            // + "," + iter1 + ","+(layer_structure.get(iter2)).getSuffix();
            
            //          celltype[n_neuron]= new String(str);
            //          cellmap.put(str,n_neuron);
            
            n_neuron++;
          }
        }
      }
      
      if ( n_neuron != numberOfNeurons )
      {
        throw new  RuntimeException (
          "number doesn't match" + n_neuron + " - " + numberOfNeurons );
      }
      
      for ( int  i = 0; i < nodeCount; i++ )
      {
        nodeEndIndices [ i ] = numberOfNeurons / nodeCount * ( i + 1 );
        
        if ( i == nodeCount - 1 )
        {
          nodeEndIndices [ i ] = numberOfNeurons - 1;
        }
      }
      
      //    branchmaps=new TreeMap[numberOfNeurons];
      //    for(int a=0;a<numberOfNeurons;a++)
      //      branchmaps[a]=new TreeMap<String, TreeSet<Synapse>>();
    }

    @Deprecated
    public void  qmapcells()
    ////////////////////////////////////////////////////////////////////////
    {
      boolean sqCheck=false;
      
      int sect;
      
      for(sect=0;sect<10;sect++)
      {
        if(nodeCount == sect*sect)
        {
          sqCheck=true;
          
          break;
        }
      }
      
      if(!sqCheck)
      {
        throw new RuntimeException (
          "Now only sqaure nodeNubmers are supported" );
      }

      nodeEndIndices = new int[nodeCount];

      int iter,iter1,iter2,n_neuron;
      
      int xstep,ystep,xresosize,yresosize;
      
      int xOri,yOri;
      
      int hostId=0;
      
      numberOfNeurons = totalNumNeurons() ;

      //    celltype=new String[numberOfNeurons];

      n_neuron=0;
      
      String str;

      for(int secX=0;secX<sect;secX++) // x axis section 
      {
        for(int secY=0;secY<sect;secY++) //y axis section
        {
          // go through all layers
          
          for(iter2=0;iter2<layerList.size();iter2++)
          {
            if((layerList.get(iter2)).getMultiplierX()>0)
            {
              xstep=1;
              
              if(secX!=0)
              {
                xOri=(layerList.get(iter2)).getMultiplierX()
                  *edgeLengthX/sect*(secX);
              }
              else
              {
                xOri=0;
              }
              
              if(secX!=sect-1)
              {
                xresosize=(layerList.get(iter2)).getMultiplierX()
                  *edgeLengthX/sect*(secX+1);
              }
              else
              {
                xresosize=(layerList.get(iter2)).getMultiplierX()
                  *edgeLengthX;
              }
            }
            else
            {
              xstep=-((layerList.get(iter2)).getMultiplierX());
              if(secX!=0)
              {
                xOri=edgeLengthX/sect*(secX);
                xOri=xOri + xOri % xstep;
              }
              else
              {
                xOri=0;
              }
              
              if(secX!=sect-1)
              {
                xresosize=edgeLengthX/sect*(secX+1);
              }
              else
              {
                xresosize=edgeLengthX;
              }
            }
            
            if((layerList.get(iter2)).getMultiplierY()>0)
            {
              ystep=1;
              
              if(secY!=0)
              {
                yOri=(layerList.get(iter2)).getMultiplierY()
                  *edgeLengthY/sect*(secY);
              }
              else
              {
                yOri=0;
              }
              
              if(secY!=sect-1)
              {
                yresosize=(layerList.get(iter2)).getMultiplierY()
                  *edgeLengthY/sect*(secY+1);
              }
              else
              {
                yresosize=(layerList.get(iter2)).getMultiplierY()
                  *edgeLengthY;
              }
            }
            else
            {
              ystep=-((layerList.get(iter2)).getMultiplierY());
              if(secY!=0)
              {
                yOri=edgeLengthY/sect*(secY);
                yOri=yOri + yOri % ystep;
              }
              else
              {
                yOri=0;
              }
              if(secY!=sect-1)
              {
                yresosize=edgeLengthY/sect*(secY+1);
              }
              else
              {
                yresosize=edgeLengthY;
              }
            }
            for(iter=xOri;iter<xresosize;iter+=xstep)
            {
              for(iter1=yOri;iter1<yresosize;iter1+=ystep)
              {
                // System.out.println( "x"+iter+" y"+iter1+" layer"+iter2
                //   +" host"+(secX*sect+secY));
                
                str=(layerList.get(iter2)).getPrefix() + ","+iter
                  + "," + iter1 +","
                  + (layerList.get(iter2)).getSuffix();
                
                //              celltype[n_neuron]= new String(str);
                
                //              cellmap.put(str,n_neuron);
                
                n_neuron++;
              }
            }
          } // END: for layer iteration
          
          nodeEndIndices[hostId]=n_neuron-1;
          
          hostId++;
        }
      }
      
      if(n_neuron!=numberOfNeurons)
      {
        throw new  RuntimeException(
          "number doesn't match"+n_neuron+" - "+numberOfNeurons);
      }

      //    branchmaps=new TreeMap[numberOfNeurons];
      //    for(int a=0;a<numberOfNeurons;a++)
      //      branchmaps[a]=new TreeMap<String, TreeSet<Synapse>>();
    }

    /***********************************************************************
    * Used in main Host, which is fast and used only to get numbers.
    * neurons are not really initialized to save memory
    * 
    * @param a sections in x axis
    * @param b sections in y axis
    ***********************************************************************/
    public void  abmapcells_Main (
      final int  a,
      final int  b )
    ////////////////////////////////////////////////////////////////////////
    {
      if ( a * b != nodeCount )
      {
        throw new RuntimeException (
          "a times b should equal to " + nodeCount );
      }
      
      sectionsInAxisX = a;
      
      sectionsInAxisY = b;

      nodeEndIndices = new int [ nodeCount ];

      int  iter, iter1, n_neuron;
      
      int  xstep, ystep, xresosize, yresosize;
      
      int  xOri, yOri;
      
      int  hostId = 0;
      
      numberOfNeurons = totalNumNeurons ( );

      // celltype=new String[numberOfNeurons];

      n_neuron = 0;
      
      String str;

      //    System.out.println(a+" "+b);

      for ( int  secX = 0; secX < a; secX++ ) // x axis section 
      {
        for ( int  secY = 0; secY < b; secY++ ) // y axis section
        {
          // go through all layers
          
          final int  layerListSize = layerList.size ( );
          
          for (
            int  layerIndex = 0; layerIndex < layerListSize; layerIndex++ )
          {
            final Layer  layer = layerList.get ( layerIndex );
            
            final int  layerMultiplierX = layer.getMultiplierX ( );
            
            if ( layerMultiplierX > 0 )
            {
              xstep = 1;
              
              if ( secX != 0 )
              {
                xOri= ( int ) ( ( double ) layerMultiplierX
                  * ( double ) edgeLengthX / ( double ) a
                  * ( double ) ( secX ) );
              }
              else
              {
                xOri = 0;
              }
              
              if ( secX != a - 1 )
              {
                xresosize= ( int ) ( ( double ) layerMultiplierX
                  * ( double ) edgeLengthX / ( double ) a
                  * ( double ) ( secX + 1 ) );
              }
              else
              {
                xresosize = layerMultiplierX * edgeLengthX;
              }
            }
            else
            {
              xstep = -layerMultiplierX;
              
              if ( secX != 0 )
              {
                xOri = ( int ) ( ( double ) edgeLengthX
                  / ( double ) a * ( double ) ( secX ) );
                
                xOri = xOri + xstep - xOri % xstep;
              }
              else
              {
                xOri = 0;
              }
              
              if ( secX != a - 1 )
              {
                xresosize = ( int ) ( ( double ) edgeLengthX
                  / ( double ) a * ( double ) ( secX + 1 ) );
                
                xresosize = xresosize + xstep -  xresosize % xstep;
                
                if ( xresosize > edgeLengthX )
                {
                  xresosize = edgeLengthX;
                }
              }
              else
              {
                xresosize = edgeLengthX;
              }
            }
            
            final int  layerMultiplierY = layer.getMultiplierY ( );
            
            if ( layerMultiplierY > 0 )
            {
              ystep = 1;
              
              if ( secY != 0 )
              {
                yOri = ( int ) ( ( double ) layerMultiplierY
                  * ( double ) edgeLengthY / ( double ) b
                  * ( double ) ( secY ) );
              }
              else
              {
                yOri = 0;
              }
              
              if ( secY != b - 1 )
              {
                yresosize = ( int ) ( ( double ) layerMultiplierY
                  * ( double ) edgeLengthY / ( double ) b
                  * ( double ) ( secY + 1 ) );
              }
              else
              {
                yresosize = layerMultiplierY * edgeLengthY;
              }
            }
            else
            {
              ystep = -layer.getMultiplierY ( );
              
              if ( secY != 0 )
              {
                yOri = ( int ) ( ( double ) edgeLengthY
                  / ( double ) b * ( double ) ( secY ) );
                
                yOri = yOri + ystep - yOri % ystep;
              }
              else
              {
                yOri = 0;
              }
              
              if ( secY != b - 1 )
              {
                yresosize = ( int ) ( ( double ) edgeLengthY / ( double ) b
                  * ( double ) ( secY + 1 ) );
                
                yresosize = yresosize + ystep -  yresosize % ystep;
                
                if ( yresosize > edgeLengthY )
                {
                  yresosize = edgeLengthY;
                }
              }
              else
              {
                yresosize = edgeLengthY;
              }
            }

            indexmap.put (
              secX + "," + secY + "," + layerIndex,
              n_neuron);
            
            for ( iter = xOri; iter < xresosize; iter += xstep )
            {
              for ( iter1 = yOri; iter1 < yresosize; iter1 += ystep )
              {
                // System.out.println("x"+iter+" y"+iter1+" layer"+iter2
                // +" host"+(secX*b+secY));
                
                str = layer.getPrefix ( ) + ","+ iter + "," + iter1 + ","
                  + layer.getSuffix ( );
                
                // celltype[n_neuron]= new String(str);
                
                // cellmap.put(str,n_neuron);
                
                n_neuron++;
              }
            }
          } // END: for layer iteration
          
          nodeEndIndices [ hostId ] = n_neuron - 1;
          
          hostId++;
        } // END: for y axis
      } // END: for x axis
      
      if ( n_neuron != numberOfNeurons )
      {
        throw new RuntimeException (
          "number doesn't match" + n_neuron + " - " + numberOfNeurons );
      }

      // branchmaps=new TreeMap[numberOfNeurons];
      // for(int i=0;i<numberOfNeurons;i++)
      //   branchmaps[i]=new TreeMap<String, TreeSet<Synapse>>();
      
      LOGGER.trace ( "Total Neuron:" + numberOfNeurons );
    }

    /***********************************************************************
    * Calculates the neuron index from the layer x,y coordinates.
    ***********************************************************************/ 
    @Deprecated
    public int  cellmap_slow (
      final String  pre,
      final String  suf,
      final int     x,
      final int     y )
    ////////////////////////////////////////////////////////////////////////
    {
      int
        stepX     = 0,
        stepY     = 0,
        xresosize = 0,
        yresosize = 0;
      
      int
        xOri = 0,
        yOri = 0;
      
      boolean
        foundX = false,
        foundY = false;

      int  layerIndex;

      try
      {
        layerIndex = prefixSuffixToLayerIndexMap.get ( pre + "," + suf );
      }
      catch ( final Exception  e )
      {
        //TODO: Add Exception handler here
        
        throw new RuntimeException ( "pre" + pre + " suf" + suf );
      }
      
      final Layer  layer = layerList.get ( layerIndex );
      
      final int
        layerMultiplierX = layer.getMultiplierX ( ),
        layerMultiplierY = layer.getMultiplierY ( );      
      
      final double
        neuronsPerSectionX
          = ( layerMultiplierX * edgeLengthX ) / ( double ) sectionsInAxisX,
        neuronsPerSectionY
          = ( layerMultiplierY * edgeLengthY ) / ( double ) sectionsInAxisY;  
      
      int  secX;
      
      for ( secX = 0; secX < sectionsInAxisX; secX++ ) 
      {
        if ( layerMultiplierX > 0 )
        {
          stepX = 1;
          
          if ( secX != 0 )
          {
            // TODO:  maybe multiply first and then divide
            
            xOri = ( int ) ( neuronsPerSectionX * secX );
          }
          else
          {
            xOri = 0;
          }
          
          if ( secX != sectionsInAxisX - 1 )
          {
            xresosize = ( int ) ( neuronsPerSectionX * ( secX + 1 ) );
          }
          else
          {
            xresosize = layerMultiplierX * edgeLengthX;
          }
        }
        else
        {
          stepX = -layerMultiplierX;
          
          if ( secX != 0 )
          {
            xOri = ( int ) ( ( double ) edgeLengthX
              / ( double ) sectionsInAxisX * ( double ) ( secX ) );
            
            xOri = xOri + stepX - xOri % stepX;
          }
          else
          {
            xOri = 0;
          }
          
          if ( secX != sectionsInAxisX - 1 )
          {
            xresosize = ( int ) ( ( double ) edgeLengthX
              / ( double ) sectionsInAxisX * ( double ) ( secX + 1 ) );
            
            xresosize = xresosize + stepX - xresosize % stepX;
            
            if ( xresosize > edgeLengthX )
            {
              xresosize=edgeLengthX;
            }
          }
          else
          {
            xresosize = edgeLengthX;
          }
        }
        
        if ( x >= xOri
          && x < xresosize )
        {
          foundX = true;
          
          break;
        }
      }
      
      int  secY;

      for ( secY = 0; secY < sectionsInAxisY; secY++ )
      {
        if ( layerMultiplierY > 0 )
        {
          stepY = 1;
          
          if ( secY != 0 )
          {
            yOri = ( int ) ( neuronsPerSectionY * secY );
          }
          else
          {
            yOri = 0;
          }
          
          if ( secY != sectionsInAxisY - 1 )
          {
            yresosize = ( int ) ( neuronsPerSectionY * ( secY + 1 ) );
          }
          else
          {
            yresosize = layerMultiplierY * edgeLengthY;
          }
        }
        else
        {
          stepY = -layerMultiplierY;
          
          if ( secY != 0 )
          {
            yOri = ( int ) ( ( double ) edgeLengthY
              / ( double ) sectionsInAxisY * ( double ) ( secY ) );
            
            yOri = yOri + stepY - yOri % stepY;
          }
          else
          {
            yOri = 0;
          }
          
          if ( secY != sectionsInAxisY - 1 )
          {
            yresosize = ( int ) ( ( double ) edgeLengthY
              / ( double ) sectionsInAxisY * ( double ) ( secY + 1 ) );
            
            yresosize = yresosize + stepY - yresosize % stepY;
            
            if ( yresosize > edgeLengthY )
            {
              yresosize = edgeLengthY;
            }
          }
          else
          {
            yresosize = edgeLengthY;
          }
        }
        
        if( y >= yOri
          && y < yresosize )
        {
          foundY = true;
          
          break;
        }
      } // END: for y axis sections
      
      if ( foundX
        && foundY )
      {
        int
          n_neuron = indexmap.get ( secX + "," + secY + "," + layerIndex );

//      int xall = 0;
        
        int yall = 0;
        
        //      for(iter=xOri;iter<xresosize;iter+=xstep) xall++;
        
        for ( int  indexY = yOri; indexY < yresosize; indexY += stepY )
        {
          yall++;
        }
        
        n_neuron = n_neuron + yall * ( x - xOri ) / stepX;
        
        n_neuron = n_neuron + ( y - yOri ) / stepY;
        
        if ( ( x - xOri ) % stepX != 0
          || ( y - yOri ) % stepY != 0 )
        {
          return -1;
        }
        
        return n_neuron;
        /*
        for(iter=xOri;iter<xresosize;iter+=xstep)
        {
          for(iter1=yOri;iter1<yresosize;iter1+=ystep)
          {
            if(x==iter && y==iter1)return n_neuron;
            n_neuron++;
          }
        }
        */
      }
      else
      {
        throw new RuntimeException (
          "neuron was not found in cellmap_slow"
          + " pre:" + pre + "suf:" + suf + "x:" + x + "y:" + y );
      }
      //  throw new RuntimeException("neuron was not found in cellmap_slow");
    }

    @Deprecated
    public boolean celllayer_test(int index,String pre, String suf) // a sections in x axis , b sections in y axis;
    ////////////////////////////////////////////////////////////////////////
    {
      int iter,iter1,iter2,n_neuron;
      int xstep=0,ystep=0,xresosize=0,yresosize=0;
      int xOri=0,yOri=0;
      int hostId=0;
      int secX,secY;
      boolean xfound=false,yfound=false;

      n_neuron=0;
      String str;

      //iter2=layermap.get(pre+","+suf);

      //Iterator<Integer> iter = indexmap.entrySet(
      //    indexmap.put(secX+","+secY+","+iter2,n_neuron);
      int bsecX=-1,bsecY=-1,bindex=-1;
      boolean exit=false;


      for(secX=0;secX<sectionsInAxisX;secX++)
      {
        for(secY=0;secY<sectionsInAxisX;secY++) // x axis section 
        {
          for(iter2 = 0 ; iter2< prefixSuffixToLayerIndexMap.size(); iter2++)
          {
            if(index>=indexmap.get(secX+","+secY+","+iter2))
            {
              bsecX=secX;
              bsecY=secY;
              bindex=iter2;
            }
            if(index<indexmap.get(secX+","+secY+","+iter2))
            {  
              exit=true;
              break;
            }
          }
          if(exit)break;
        }
        if(exit)break;
      }

      iter2=bindex;
      return (layerList.get(iter2).getPrefix().equals(pre) && layerList.get(iter2).getSuffix().equals(suf));
    }

    @Deprecated
    public int [] celllayer_cordinate(int index,String pre, String suf)
    ////////////////////////////////////////////////////////////////////////
    {
      int iter,iter1,iter2,n_neuron;
      int xstep=0,ystep=0,xresosize=0,yresosize=0;
      int xOri=0,yOri=0;
      int hostId=0;
      int secX,secY;
      boolean xfound=false,yfound=false;

      n_neuron=0;
      String str;

      //iter2=layermap.get(pre+","+suf);

      //Iterator<Integer> iter = indexmap.entrySet(
      //    indexmap.put(secX+","+secY+","+iter2,n_neuron);
      int bsecX=-1,bsecY=-1,bindex=-1;
      boolean exit=false;

      //    iter2 = layermap.get(pre+","+suf);


      for(secX=0;secX<sectionsInAxisX;secX++)
      {
        for(secY=0;secY<sectionsInAxisX;secY++) // x axis section 
        {
          for(iter2 = 0 ; iter2< prefixSuffixToLayerIndexMap.size(); iter2++)
          {
            if(index>=indexmap.get(secX+","+secY+","+iter2))
            {
              bsecX=secX;
              bsecY=secY;
              bindex=iter2;
            }
            if(index<indexmap.get(secX+","+secY+","+iter2))
            {  
              exit=true;
              break;
            }
          }
          if(exit)break;
        }
        if(exit)break;
      }

      iter2=bindex;


      for(secX=0;secX<sectionsInAxisX;secX++) // x axis section 
      {
        if((layerList.get(iter2)).getMultiplierX()>0)
        {
          xstep=1;
          if(secX!=0)
          {
            xOri=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          }
          else
          {
            xOri=0;
          }
          if(secX!=sectionsInAxisX-1)
          {
            xresosize=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          }
          else
          {
            xresosize=(layerList.get(iter2)).getMultiplierX()*edgeLengthX;
          }
        }
        else
        {
          xstep=-((layerList.get(iter2)).getMultiplierX());
          if(secX!=0)
          {
            xOri=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
            xOri=xOri + xstep - xOri %xstep;

          }
          else
          {
            xOri=0;
          }
          if(secX!=sectionsInAxisX-1)
          {
            xresosize=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
            xresosize=xresosize + xstep -  xresosize % xstep;
            if(xresosize>edgeLengthX)xresosize=edgeLengthX;
          }
          else
          {
            xresosize=edgeLengthX;
          }
        }
      }


      for(secY=0;secY<sectionsInAxisY;secY++) //y axis section
      {
        if((layerList.get(iter2)).getMultiplierY()>0)
        {
          ystep=1;
          if(secY!=0)
          {
            yOri=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          }
          else
          {
            yOri=0;
          }
          if(secY!=sectionsInAxisY-1)
          {
            yresosize=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          }
          else
          {
            yresosize=(layerList.get(iter2)).getMultiplierY()*edgeLengthY;
          }
        }
        else
        {
          ystep=-((layerList.get(iter2)).getMultiplierY());
          if(secY!=0)
          {
            yOri=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
            yOri=yOri + ystep - yOri % ystep;
          }
          else
          {
            yOri=0;
          }
          if(secY!=sectionsInAxisY-1)
          {
            yresosize=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
            yresosize=yresosize + ystep -  yresosize % ystep;
            if(yresosize>edgeLengthY)yresosize=edgeLengthY;
          }
          else
          {
            yresosize=edgeLengthY;
          }
        }
      } // END: for y axis sections

      secX=bsecX;
      secY=bsecY;
      //    iter2=bindex;


      //    try {
      n_neuron = indexmap.get(secX+","+secY+","+iter2);
      //    }
      //    catch(Exception ex) {
      //      throw new RuntimeException("secX"+secX+" secY"+secY+", iter2"+iter2+" indexmap"+indexmap.size()+" layermap:"+layermap.size()+" pre:"+pre+" suf:"+suf);
      //    }
      int yall = 0;
      //      for(iter=xOri;iter<xresosize;iter+=xstep) xall++;
      for(iter1=yOri;iter1<yresosize;iter1+=ystep) yall++;


      int x = (index - n_neuron)/yall*xstep + xOri;
      int y = ((index - n_neuron)%(yall))*ystep + yOri;
      int [] out = new int [] {x,y};
      return out;
    }

    /***********************************************************************
    *  Give the nethost id, map the cells in a times b,  slow method.
    * 
    * @param  a parts in X dimension
    * @param  b parts in X dimension
    * @param  host_id
    ***********************************************************************/
    @Deprecated
    public String celltype_slow(int index) // a sections in x axis , b sections in y axis;
    ////////////////////////////////////////////////////////////////////////
    {
      int iter,iter1,iter2,n_neuron;
      int xstep=0,ystep=0,xresosize=0,yresosize=0;
      int xOri=0,yOri=0;
      int hostId=0;
      int secX,secY;
      boolean xfound=false,yfound=false;

      n_neuron=0;
      String str;

      //iter2=layermap.get(pre+","+suf);

      //Iterator<Integer> iter = indexmap.entrySet(
      //    indexmap.put(secX+","+secY+","+iter2,n_neuron);
      int bsecX=-1,bsecY=-1,bindex=-1;
      boolean exit=false;


      for(secX=0;secX<sectionsInAxisX;secX++)
      {
        for(secY=0;secY<sectionsInAxisX;secY++) // x axis section 
        {
          for(iter2 = 0 ; iter2< prefixSuffixToLayerIndexMap.size(); iter2++)
          {
            if(index>=indexmap.get(secX+","+secY+","+iter2))
            {
              bsecX=secX;
              bsecY=secY;
              bindex=iter2;
            }
            if(index<indexmap.get(secX+","+secY+","+iter2))
            {  
              exit=true;
              break;
            }
          }
          if(exit)break;
        }
        if(exit)break;
      }

      iter2=bindex;

      for(secX=0;secX<sectionsInAxisX;secX++) // x axis section 
      {
        if((layerList.get(iter2)).getMultiplierX()>0)
        {
          xstep=1;
          if(secX!=0)
          {
            xOri=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          }
          else
          {
            xOri=0;
          }
          if(secX!=sectionsInAxisX-1)
          {
            xresosize=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          }
          else
          {
            xresosize=(layerList.get(iter2)).getMultiplierX()*edgeLengthX;
          }
        }
        else
        {
          xstep=-((layerList.get(iter2)).getMultiplierX());
          if(secX!=0)
          {
            xOri=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
            xOri=xOri + xstep - xOri %xstep;

          }
          else
          {
            xOri=0;
          }
          if(secX!=sectionsInAxisX-1)
          {
            xresosize=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
            xresosize=xresosize + xstep -  xresosize % xstep;
            if(xresosize>edgeLengthX)xresosize=edgeLengthX;
          }
          else
          {
            xresosize=edgeLengthX;
          }
        }
      }


      for(secY=0;secY<sectionsInAxisY;secY++) //y axis section
      {
        if((layerList.get(iter2)).getMultiplierY()>0)
        {
          ystep=1;
          if(secY!=0)
          {
            yOri=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          }
          else
          {
            yOri=0;
          }
          if(secY!=sectionsInAxisY-1)
          {
            yresosize=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          }
          else
          {
            yresosize=(layerList.get(iter2)).getMultiplierY()*edgeLengthY;
          }
        }
        else
        {
          ystep=-((layerList.get(iter2)).getMultiplierY());
          if(secY!=0)
          {
            yOri=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
            yOri=yOri + ystep - yOri % ystep;
          }
          else
          {
            yOri=0;
          }
          if(secY!=sectionsInAxisY-1)
          {
            yresosize=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
            yresosize=yresosize + ystep -  yresosize % ystep;
            if(yresosize>edgeLengthY)yresosize=edgeLengthY;
          }
          else
          {
            yresosize=edgeLengthY;
          }
        }
      } // END: for y axis sections

      secX=bsecX;
      secY=bsecY;
      iter2=bindex;


      n_neuron = indexmap.get(secX+","+secY+","+iter2);
      int yall = 0;
      //      for(iter=xOri;iter<xresosize;iter+=xstep) xall++;
      for(iter1=yOri;iter1<yresosize;iter1+=ystep) yall++;


      int x = (index - n_neuron)/yall*xstep + xOri;
      int y = ((index - n_neuron)%(yall))*ystep + yOri;

      return layerList.get(iter2).getPrefix()+","+x+","+y+","+layerList.get(iter2).getSuffix();
    }

    /***********************************************************************
    *  Give the nethost id, map the cells in a times b,  fast method.
    * 
    * @param  a parts in X dimension
    * @param  b parts in X dimension
    * @param  host_id
    ***********************************************************************/
    public void  abmapcells (
      final int  a,
      final int  b,
      final int  host_id ) // a sections in x axis , b sections in y axis;
    ////////////////////////////////////////////////////////////////////////
    {
      if ( a * b != nodeCount )
      {
        throw new  RuntimeException (
          "a times b should equal to " + nodeCount );
      }
      
      sectionsInAxisX = a;
      
      sectionsInAxisY = b;

      nodeEndIndices = new int[nodeCount];

      int iter,iter1,iter2,n_neuron;
      int xstep,ystep,xresosize,yresosize;
      int xOri,yOri;
      int hostId=0;
      numberOfNeurons = totalNumNeurons() ;

      //    celltype=new String[numberOfNeurons];

      int hostCell=0;
      n_neuron=0;
      String str;
      int cc=0;

      //    System.out.println(a+" "+b);

      for(int secX=0;secX<a;secX++) // x axis section 
      {
        for(int secY=0;secY<b;secY++) //y axis section
        {

          for(iter2=0;iter2<layerList.size();iter2++) // go through all layers
          {
            if((layerList.get(iter2)).getMultiplierX()>0)
            {
              xstep=1;
              if(secX!=0)
              {
                xOri=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)a*(double)(secX));
              }
              else
              {
                xOri=0;
              }
              if(secX!=a-1)
              {
                xresosize=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)a*(double)(secX+1));
              }
              else
              {
                xresosize=(layerList.get(iter2)).getMultiplierX()*edgeLengthX;
              }
            }
            else
            {
              xstep=-((layerList.get(iter2)).getMultiplierX());
              if(secX!=0)
              {
                xOri=(int)((double)edgeLengthX/(double)a*(double)(secX));
                xOri=xOri + xstep - xOri %xstep;

              }
              else
              {
                xOri=0;
              }
              if(secX!=a-1)
              {
                xresosize=(int)((double)edgeLengthX/(double)a*(double)(secX+1));
                xresosize=xresosize + xstep -  xresosize % xstep;
                if(xresosize>edgeLengthX)xresosize=edgeLengthX;
              }
              else
              {
                xresosize=edgeLengthX;
              }
            }
            if((layerList.get(iter2)).getMultiplierY()>0)
            {
              ystep=1;
              if(secY!=0)
              {
                yOri=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)b*(double)(secY));
              }
              else
              {
                yOri=0;
              }
              if(secY!=b-1)
              {
                yresosize=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)b*(double)(secY+1));
              }
              else
              {
                yresosize=(layerList.get(iter2)).getMultiplierY()*edgeLengthY;
              }
            }
            else
            {
              ystep=-((layerList.get(iter2)).getMultiplierY());
              if(secY!=0)
              {
                yOri=(int)((double)edgeLengthY/(double)b*(double)(secY));
                yOri=yOri + ystep - yOri % ystep;
              }
              else
              {
                yOri=0;
              }
              if(secY!=b-1)
              {
                yresosize=(int)((double)edgeLengthY/(double)b*(double)(secY+1));
                yresosize=yresosize + ystep -  yresosize % ystep;
                if(yresosize>edgeLengthY)yresosize=edgeLengthY;
              }
              else
              {
                yresosize=edgeLengthY;
              }
            }

            indexmap.put(secX+","+secY+","+iter2,n_neuron);

            for(iter=xOri;iter<xresosize;iter+=xstep)
            {
              for(iter1=yOri;iter1<yresosize;iter1+=ystep)
              {
                //              System.out.println("x"+iter+" y"+iter1+" layer"+iter2+" host"+(secX*b+secY));

                if(hostId == host_id)
                {
                  hostCell++;
                }
                //          celltype[n_neuron]= new String(str);
                //                cellmap.put(str,n_neuron);
                n_neuron++;
              }
            }
          } // END: for layer iteration
          nodeEndIndices[hostId]=n_neuron-1;
          hostId++;
        } // END: for y axis
      } // END: for x axis

      hostId=0;
      celltype = new String[hostCell];
      for(int secX=0;secX<a;secX++) // x axis section 
      {
        for(int secY=0;secY<b;secY++) //y axis section
        {

          for(iter2=0;iter2<layerList.size();iter2++) // go through all layers
          {
            if((layerList.get(iter2)).getMultiplierX()>0)
            {
              xstep=1;
              if(secX!=0)
              {
                xOri=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)a*(double)(secX));
              }
              else
              {
                xOri=0;
              }
              if(secX!=a-1)
              {
                xresosize=(int)((double)(layerList.get(iter2)).getMultiplierX()*(double)edgeLengthX/(double)a*(double)(secX+1));
              }
              else
              {
                xresosize=(layerList.get(iter2)).getMultiplierX()*edgeLengthX;
              }
            }
            else
            {
              xstep=-((layerList.get(iter2)).getMultiplierX());
              if(secX!=0)
              {
                xOri=(int)((double)edgeLengthX/(double)a*(double)(secX));
                xOri=xOri + xstep - xOri %xstep;

              }
              else
              {
                xOri=0;
              }
              if(secX!=a-1)
              {
                xresosize=(int)((double)edgeLengthX/(double)a*(double)(secX+1));
                xresosize=xresosize + xstep -  xresosize % xstep;
                if(xresosize>edgeLengthX)xresosize=edgeLengthX;
              }
              else
              {
                xresosize=edgeLengthX;
              }
            }
            if((layerList.get(iter2)).getMultiplierY()>0)
            {
              ystep=1;
              if(secY!=0)
              {
                yOri=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)b*(double)(secY));
              }
              else
              {
                yOri=0;
              }
              if(secY!=b-1)
              {
                yresosize=(int)((double)(layerList.get(iter2)).getMultiplierY()*(double)edgeLengthY/(double)b*(double)(secY+1));
              }
              else
              {
                yresosize=(layerList.get(iter2)).getMultiplierY()*edgeLengthY;
              }
            }
            else
            {
              ystep=-((layerList.get(iter2)).getMultiplierY());
              if(secY!=0)
              {
                yOri=(int)((double)edgeLengthY/(double)b*(double)(secY));
                yOri=yOri + ystep - yOri % ystep;
              }
              else
              {
                yOri=0;
              }
              if(secY!=b-1)
              {
                yresosize=(int)((double)edgeLengthY/(double)b*(double)(secY+1));
                yresosize=yresosize + ystep -  yresosize % ystep;
                if(yresosize>edgeLengthY)yresosize=edgeLengthY;
              }
              else
              {
                yresosize=edgeLengthY;
              }
            }
            for(iter=xOri;iter<xresosize;iter+=xstep)
            {
              for(iter1=yOri;iter1<yresosize;iter1+=ystep)
              {
                if(hostId == host_id)
                {

                  str = (layerList.get(iter2)).getPrefix() +","+ iter + "," + iter1 +","+ (layerList.get(iter2)).getSuffix();
                  celltype[cc] = new String(str);
                  cc++;
                  hostCell++;
                }
              }
            }
          } // END: for layer iteration
          hostId++;
        } // END: for y axis
      }
      if(n_neuron!=numberOfNeurons) throw new  RuntimeException("number doesn't match"+n_neuron+" - "+numberOfNeurons);
      //    branchmaps=new TreeMap[numberOfNeurons];
      //    for(int i=0;i<numberOfNeurons;i++)
      //      branchmaps[i]=new TreeMap<String, TreeSet<Synapse>>();
      scaffold = new HashMap<Integer, Map<String, SizeID> >();
      /*
    for(int i=0;i<numberOfNeurons;i++) 
    {
      scaffold[i]=new TreeMap<String, SizeID>(); 
    }
       */
      //  axons = new Axon[numberOfNeurons];
    }

    /***********************************************************************
    * Compute the total number of neurons
    *  
    * @return number of neurons
    ***********************************************************************/
    public int  totalNumNeurons ( )
    ////////////////////////////////////////////////////////////////////////
    {
      int  num = 0;
      
      int xMul, yMul;
      
      for ( final Layer  layer : layerList )
      {
        xMul = layer.getMultiplierX ( );
        
        yMul = layer.getMultiplierY ( );

        if ( xMul < 0 )
        {
          if ( edgeLengthX % xMul != 0 )
          {
            throw new RuntimeException (
              xMul + "is not a multiplier of x-edge length" + edgeLengthX );
          }
        }
        
        if ( yMul < 0 )
        {
          if ( edgeLengthY % yMul !=0 )
          {
            throw new RuntimeException (
              yMul + "is not a multiplier of y-edge length" + edgeLengthY );
          }
        }

        num += ( xMul > 0 ? edgeLengthX * xMul : edgeLengthX / -xMul )
             * ( yMul > 0 ? edgeLengthY * yMul : edgeLengthY / -yMul );
      }
      
      return num;
    }

    /***********************************************************************
    * Find out the number of neurons of a layer, given the layer name
    * 
    * @param prefix string
    * @param suffix string
    * @return number of neurons
    ***********************************************************************/
    public int  layerNumNeurons (
      final String  prefix,
      final String  suffix )
    ////////////////////////////////////////////////////////////////////////
    {
      final int
        index = prefixSuffixToLayerIndexMap.get ( prefix + "," + suffix );
      
      // while(!( layer_structure.get(iter)).getPrefix().equals(prefix)
      //    || !( layer_structure.get(iter)).getSuffix().equals(suffix))
      // {
      //   iter++;
      // }
      
      final Layer  layer = layerList.get ( index );
      
      final int
        xMultiplier = layer.getMultiplierX ( ),
        yMultiplier = layer.getMultiplierY ( );
      
      int
        xstep,
        ystep,
        xresosize,
        yresosize;
      
      if ( xMultiplier > 0
        && yMultiplier > 0 )
      {
        xstep = 1;
        
        ystep = 1;
        
        xresosize = xMultiplier * edgeLengthX;
        
        yresosize = yMultiplier * edgeLengthY;
      }
      else if ( xMultiplier > 0
             && yMultiplier < 0 )
      {
        xstep = 1;
        
        ystep = -yMultiplier;
        
        xresosize = xMultiplier * edgeLengthX;
        
        yresosize = edgeLengthY;
      }
      else if ( xMultiplier < 0
             && yMultiplier > 0 )
      {
        xstep = -xMultiplier;
        
        ystep = 1;
        
        xresosize = edgeLengthX;
        
        yresosize = yMultiplier * edgeLengthY;
      }
      else
      {
        xstep = -xMultiplier;
        
        ystep = -yMultiplier;
        
        xresosize = edgeLengthX;
        
        yresosize = edgeLengthY;
      }
      
      return xresosize * yresosize / ( xstep * ystep );
    }

    public int  getXmultiplier (
      final String  prefix,
      final String  suffix )
    ////////////////////////////////////////////////////////////////////////
    {
      final String  key = prefix + "," + suffix;
      
      if ( prefixSuffixToLayerIndexMap.containsKey ( key ) )
      {
        final int  iter = prefixSuffixToLayerIndexMap.get ( key );
        
        final Layer  layer = layerList.get ( iter ); 
        
        return layer.getMultiplierX ( );
      }
      
      return 0;
    }

    public int  getYmultiplier (
      final String  prefix,
      final String  suffix )
    ////////////////////////////////////////////////////////////////////////
    {
      final String  key = prefix + "," + suffix;
      
      if ( prefixSuffixToLayerIndexMap.containsKey ( key ) )
      {
        final int  iter = prefixSuffixToLayerIndexMap.get ( key );
        
        final Layer  layer = layerList.get ( iter );
        
        return layer.getMultiplierY ( );
      }
      
      return 0;
    }

    /***********************************************************************
    * Find out the number of layers starting with prefix string
    * 
    * @param prefix string
    * @return number of layers.
    ***********************************************************************/
    public int  getLayersize ( final String  prefix )
    ////////////////////////////////////////////////////////////////////////
    {
      int  sum = 0;
      
      for ( final Layer  layer : layerList )
      {
        if ( layer.getPrefix ( ).equals ( prefix ) )
        {
          sum++;
        }
      }
      
      return sum;
    }

    public Layer  getLayer (
      final String  prefix,
      final String  suffix )
    ////////////////////////////////////////////////////////////////////////
    {
      final String  key = prefix + "," + suffix;
      
      if ( prefixSuffixToLayerIndexMap.containsKey ( key ) )
      {
        final int  index = prefixSuffixToLayerIndexMap.get ( key );
        
        return layerList.get ( index );
      }
      
      return null;
    }

    /***********************************************************************
    * Returns the suffix of the Nth layer with given prefix. 
    ***********************************************************************/
    public String  getLayerSuffix (
      final String  prefix,
      final int     index )
    ////////////////////////////////////////////////////////////////////////
    {
      int  matchingPrefixIndex = 0;
      
      for ( final Layer  layer : layerList )
      {
        final String  layerPrefix = layer.getPrefix ( );
        
        if ( layerPrefix.equals ( prefix ) )
        {
          if ( matchingPrefixIndex == index )
          {
            return layer.getSuffix ( );
          }
          
          matchingPrefixIndex++;
        }
      }
      
      System.err.println ( "no layer matched" );
      
      return "no layer matched";
    }

    @Deprecated
    public void  connect (
      String f_pre,
      String f_suf,
      String t_pre,
      String t_suf,
      boolean convergent,
      double stre,
      byte type,
      double delay,
      int idum)
    ////////////////////////////////////////////////////////////////////////
    {
      //ONLY MAKE ONCE, THEN COPY AND PASTE AND ADD FILE RETRIEVE FUNCTION.
      /*
    FileReader fr=null;
    try{
      fr=new FileReader(filename);
    }
    catch(FileNotFoundException e)
    {
      System.err.println("Couldn't get File: " + filename);
      System.exit(0);
    }
    BufferedReader br=new BufferedReader(fr);
    String currentLine=br.readline();
       */
    }

    /***********************************************************************
    * Build up the Scaffold for later building up network structure.
    * 
    * L connection
    * 
    * This method is called from class SimulatorParser
    * method parseScaffoldWithPeriodic(int,Element).
    ***********************************************************************/
    public void  popScaffold (
      final String          fromLayerPrefix,
      final String          fromLayerSuffix,
      final String          toLayerPrefix,
      final String          toLayerSuffix,
      final boolean         convergent,
      final double          strength,
      final int             type,
      final double          delay,
      final double [ ] [ ]  trans,
      final int             hostId,
      final boolean         periodic )
    ////////////////////////////////////////////////////////////////////////
    {
      // double  maxOutput = -1.0;
      
      final int
        fromLayerMultiplierX
          = getXmultiplier ( fromLayerPrefix, fromLayerSuffix ),
        fromLayerMultiplierY
          = getYmultiplier ( fromLayerPrefix, fromLayerSuffix ),
        toLayerMultiplierX
          = getXmultiplier ( toLayerPrefix,   toLayerSuffix ),
        toLayerMultiplierY
          = getYmultiplier ( toLayerPrefix,   toLayerSuffix );
      
      int
        fromLayerStepX, // previously named f_xstep
        fromLayerStepY,
        fromLayerMultipliedEdgeLengthX, // previously named f_xresosize
        fromLayerMultipliedEdgeLengthY;
      
      int
        toLayerStepX, // previously named t_xstep
        toLayerStepY,
        toLayerMultipliedEdgeLengthX, // previously named t_xresosize
        toLayerMultipliedEdgeLengthY;
      
      int
        denomX = 0,
        numX   = 0;
      
      int
        denomY = 0,
        numY   = 0;
      
      int
        xmagratio = 0,
        ymagratio = 0;
      
      int
        xborder,
        yborder;
      
      int
        newx = 0,
        newy = 0;
      
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      
      int [ ]
        xy = new int [ 2 ];

      final int
        secX = hostId / sectionsInAxisY,
        secY = hostId % sectionsInAxisY;

      int
        fromLayerStepX2, // previously named xstep_ff
        fromLayerStepY2,
        fromLayerSectionEndX, // previously named xresosize_ff
        fromLayerSectionEndY;
      
      int
        toLayerStepX2, // previously named xstep_tt
        toLayerStepY2,
        toLayerSectionEndX, // previously named xresosize_tt
        toLayerSectionEndY;
      
      int
        fromLayerSectionOriginX, // previously named xOri_ff
        fromLayerSectionOriginY;
      
      int
        toLayerSectionOriginX, // previously named xOri_tt
        toLayerSectionOriginY;

      if ( fromLayerMultiplierX > 0 )
      {
        fromLayerStepX2 = 1;
        
        final double  fromLayerNeuronsPerSectionX
          = fromLayerMultiplierX * edgeLengthX / ( double ) sectionsInAxisX;
        
        if ( secX != 0 )
        {
          fromLayerSectionOriginX
            = ( int ) ( fromLayerNeuronsPerSectionX * secX );
        }
        else
        {
          fromLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          fromLayerSectionEndX
            = ( int ) ( fromLayerNeuronsPerSectionX * ( secX + 1 ) );
        }
        else
        {
          fromLayerSectionEndX = fromLayerMultiplierX * edgeLengthX;
        }
      }
      else // fromLayerMultiplierX <= 0
      {
        fromLayerStepX2 = -fromLayerMultiplierX;
        
        if ( secX != 0 )
        {
          fromLayerSectionOriginX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX
            * ( double ) ( secX ) );
          
          //  if(xstep_ff==0)System.out.println("pre:"+f_pre+" suf:"+f_suf);
          //          try{
          
          fromLayerSectionOriginX = fromLayerSectionOriginX
            + fromLayerStepX2 - fromLayerSectionOriginX % fromLayerStepX2;
          
          //          }
          //          catch(Exception ex)
          //          {
          //            throw new RuntimeException("pre:"+f_pre+" suf:"+f_suf);
          //          }
        }
        else
        {
          fromLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          fromLayerSectionEndX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX
            * ( double ) ( secX + 1 ) );
          
          fromLayerSectionEndX = fromLayerSectionEndX
            + fromLayerStepX2 -  fromLayerSectionEndX % fromLayerStepX2;
          
          if ( fromLayerSectionEndX > edgeLengthX )
          {
            fromLayerSectionEndX=edgeLengthX;
          }
        }
        else
        {
          fromLayerSectionEndX = edgeLengthX;
        }
      }
      
      if ( fromLayerMultiplierY > 0 )
      {
        fromLayerStepY2 = 1;
        
        final double  fromLayerNeuronsPerSectionY
          = fromLayerMultiplierY * edgeLengthY / ( double ) sectionsInAxisY;
        
        if ( secY != 0 )
        {
          fromLayerSectionOriginY
            = ( int ) ( fromLayerNeuronsPerSectionY * secY );
        }
        else
        {
          fromLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          fromLayerSectionEndY = ( int )
            ( fromLayerNeuronsPerSectionY * ( double ) ( secY + 1 ) );
        }
        else
        {
          fromLayerSectionEndY = fromLayerMultiplierY * edgeLengthY;
        }
      }
      else  // fromLayerMultiplierY <= 0
      {
        fromLayerStepY2 = -fromLayerMultiplierY;
        
        if ( secY != 0 )
        {
          fromLayerSectionOriginY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY ) );
          
          fromLayerSectionOriginY = fromLayerSectionOriginY
            + fromLayerStepY2 - fromLayerSectionOriginY % fromLayerStepY2;
        }
        else
        {
          fromLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          fromLayerSectionEndY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY + 1 ) );
          
          fromLayerSectionEndY = fromLayerSectionEndY
            + fromLayerStepY2 -  fromLayerSectionEndY % fromLayerStepY2;
          
          if ( fromLayerSectionEndY > edgeLengthY )
          {
            fromLayerSectionEndY = edgeLengthY;
          }
        }
        else
        {
          fromLayerSectionEndY = edgeLengthY;
        }
      }

      if ( toLayerMultiplierX > 0 )
      {
        toLayerStepX2 = 1;
        
        final double  toLayerNeuronsPerSectionX
          = toLayerMultiplierX * edgeLengthX / ( double ) sectionsInAxisX;        
        
        if ( secX != 0 )
        {
          toLayerSectionOriginX
            = ( int ) ( toLayerNeuronsPerSectionX * secX );
        }
        else
        {
          toLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          toLayerSectionEndX
            = ( int ) ( toLayerNeuronsPerSectionX * ( secX + 1 ) );
        }
        else
        {
          toLayerSectionEndX = toLayerMultiplierX * edgeLengthX;
        }
      }
      else
      {
        toLayerStepX2 = -toLayerMultiplierX;
        
        if ( secX != 0 )
        {
          toLayerSectionOriginX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX ) );
          
          toLayerSectionOriginX = toLayerSectionOriginX
            + toLayerStepX2 - toLayerSectionOriginX % toLayerStepX2;
        }
        else
        {
          toLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          toLayerSectionEndX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX + 1 ) );
          
          toLayerSectionEndX = toLayerSectionEndX
            + toLayerStepX2 -  toLayerSectionEndX % toLayerStepX2;
          
          if ( toLayerSectionEndX > edgeLengthX )
          {
            toLayerSectionEndX = edgeLengthX;
          }
        }
        else
        {
          toLayerSectionEndX = edgeLengthX;
        }
      }
      
      if ( toLayerMultiplierY > 0 )
      {
        toLayerStepY2 = 1;
        
        final double  toLayerNeuronsPerSectionY
          = toLayerMultiplierY * edgeLengthY / ( double ) sectionsInAxisY;        
      
        if ( secY != 0 )
        {
          toLayerSectionOriginY
            = ( int ) ( toLayerNeuronsPerSectionY * secY );
        }
        else
        {
          toLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          toLayerSectionEndY
            = ( int ) ( toLayerNeuronsPerSectionY * ( secY + 1 ) );
        }
        else
        {
          toLayerSectionEndY = toLayerMultiplierY * edgeLengthY;
        }
      }
      else
      {
        toLayerStepY2 = -toLayerMultiplierY;
        
        if ( secY != 0 )
        {
          toLayerSectionOriginY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY ) );
          
          toLayerSectionOriginY = toLayerSectionOriginY
            + toLayerStepY2 - toLayerSectionOriginY % toLayerStepY2;
        }
        else
        {
          toLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          toLayerSectionEndY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY + 1 ) );
          
          toLayerSectionEndY = toLayerSectionEndY
            + toLayerStepY2 -  toLayerSectionEndY % toLayerStepY2;
          
          if ( toLayerSectionEndY > edgeLengthY )
          {
            toLayerSectionEndY = edgeLengthY;
          }
        }
        else
        {
          toLayerSectionEndY = edgeLengthY;
        }
      }

      // these may not be useful for new neuronal model
      
      if ( type == 1
        && strength > 0 )
      {
        throw new RuntimeException ( "sign of connection is wrong" );
      }
      
      if ( type == 0
        && strength < 0 )
      {
        throw new RuntimeException ( "sign of connection is wrong" );
      }

      if ( strength != 0 )
      {
        if ( fromLayerMultiplierX > 0 )
        {
          fromLayerStepX = 1;
          
          fromLayerMultipliedEdgeLengthX
            = fromLayerMultiplierX * edgeLengthX;
        }
        else
        {
          fromLayerStepX = -fromLayerMultiplierX;
          
          fromLayerMultipliedEdgeLengthX = edgeLengthX;
        }
        
        if ( fromLayerMultiplierY > 0 )
        {
          fromLayerStepY = 1;
          
          fromLayerMultipliedEdgeLengthY
            = fromLayerMultiplierY * edgeLengthY;
        }
        else
        {
          fromLayerStepY = -fromLayerMultiplierY;
          
          fromLayerMultipliedEdgeLengthY = edgeLengthY;
        }
        
        if ( toLayerMultiplierX > 0 )
        {
          toLayerStepX = 1;
          
          toLayerMultipliedEdgeLengthX = toLayerMultiplierX * edgeLengthX;
        }
        else
        {
          toLayerStepX = -toLayerMultiplierX;
          
          toLayerMultipliedEdgeLengthX = edgeLengthX;
        }
        
        if ( toLayerMultiplierY > 0 )
        {
          toLayerStepY = 1;
          
          toLayerMultipliedEdgeLengthY = toLayerMultiplierY * edgeLengthY;
        }
        else
        {
          toLayerStepY = -toLayerMultiplierY;
          
          toLayerMultipliedEdgeLengthY = edgeLengthY;
        }

        double [ ] [ ]  conn;

        //find out the post synaptic neurons belong to which host?
          /*
      if(convergent) //convergence connections from many to 1, first iterate to side
      {
        conn = FunUtil.lRotate90(trans);

        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }

        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
            xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

            newx=xy[0];
            newy=xy[1];
            //              if(neuronID == 18400 && t_pre.equals("SE") && t_suf.equals("U3") ) throw new RuntimeException("18400  has a target at host 6\n"+"x:"+(newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep)))+1)*(t_xstep))+"x:"+(newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep))))*(t_xstep))+" y:"+(newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep)))+1)*(t_ystep))+" y:"+(newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep))))*(t_ystep))+" \n center"+newx+","+newy);
            for(int itxx=newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep)))+1)*(t_xstep);itxx<=newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep))))*(t_xstep);itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
            {
              for(int ityy=newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep)))+1)*(t_ystep);ityy<=newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep))))*(t_ystep);ityy+=t_ystep)  
              {
                if(periodic)
                {
                  xborder = itxx % t_xresosize;
                  yborder = ityy % t_yresosize;
                  if(xborder<0) xborder+=t_xresosize;
                  if(yborder<0) yborder+=t_yresosize;
                }
                else
                {
                  if(itxx >= t_xresosize || itxx < 0 || ityy >= t_yresosize || ityy<0) // out of boundary
                  {
                    continue;
                  }
                  else //within boundary
                  {
                    xborder = itxx;
                    yborder = ityy;
                  }
                }




                int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/f_xstep+(((conn.length%2)==0?conn.length-1:conn.length)/2);
                int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/f_ystep+(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2);
                if(maX>=0 && maX < conn.length && maY>=0 && maY < conn[0].length )
                {
                  if(Math.abs(conn[maX][maY])!=0)
                  {

                    int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    //  if(neuronID == 18400 && node == 6 ) throw new RuntimeException("18400 "+f_pre+","+f_suf+","+iterx+","+itery+" has a target at host 6\n"+18400+" connects to"+toIndex+" "+t_pre+","+t_suf+","+xborder+","+yborder+" ,which is at 6");
                    if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                    {
                      neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                    }
                  }
                }


              }
            }

          }
        }
      }
      else //divergent
      {
        conn = FunUtil.rRotate90(trans);
        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }
        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
            if( neuronID <= neuron_end && neuronID >= base  )
            {
              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

              newx=xy[0];
              newy=xy[1];

              for(int itxx=newx-conn.length/2*t_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*t_xstep;itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center

              {
                for(int ityy=newy-conn[0].length/2*t_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*t_ystep;ityy+=t_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % t_xresosize;
                    yborder = ityy % t_yresosize;
                    if(xborder<0) xborder+=t_xresosize;
                    if(yborder<0) yborder+=t_yresosize;
                  }
                  else
                  {
                    if(itxx >= t_xresosize || itxx < 0 || ityy >= t_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


//                  synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses;                
                  if(Math.abs(conn[(itxx-newx+conn.length/2*t_xstep)/t_xstep][(ityy-newy+conn[0].length/2*t_ystep)/t_ystep])!=0)
                  {
                      int node=FunUtil.hostId(nodeEndIndices,toIndex);
                      if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                      {
                        neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                      }

                  }


                }
              }
            }

          }
        }

      }
           */


        //  make connections here.
        
        if ( convergent )
        {
          // convergence connections from many to 1, first iterate to side

          conn = FunUtil.rRotate90 ( trans );
          
          if ( toLayerMultiplierX   < 0
            && fromLayerMultiplierX > 0 )
          {
            numX = fromLayerMultiplierX;
            
            denomX = 1;
          }
          else if ( toLayerMultiplierX < 0
            && fromLayerMultiplierX    < 0 )
          {
            numX = 1;
            
            denomX = 1;
          }
          else if ( toLayerMultiplierX > 0
            && fromLayerMultiplierX    > 0 )
          {
            numX = fromLayerMultiplierX;
            
            denomX = toLayerMultiplierX;
          }
          else if ( toLayerMultiplierX > 0
            && fromLayerMultiplierX    < 0 )
          {
            numX = 1;
            
            denomX = toLayerMultiplierX;
          }

          if ( toLayerMultiplierY   < 0
            && fromLayerMultiplierY > 0 )
          {
            numY = fromLayerMultiplierY;
            
            denomY = 1;
          }
          else if ( toLayerMultiplierY < 0
            && fromLayerMultiplierY    < 0 )
          {
            numY = 1;
            
            denomY = 1;
          }
          else if ( toLayerMultiplierY > 0
            && fromLayerMultiplierY    > 0 )
          {
            numY = fromLayerMultiplierY;
            
            denomY = toLayerMultiplierY;
          }
          else if ( toLayerMultiplierY > 0
            && fromLayerMultiplierY    < 0 )
          {
            numY = 1;
            
            denomY = toLayerMultiplierY;
          }

          for ( int  toLayerX = toLayerSectionOriginX;
            toLayerX < toLayerSectionEndX;
            toLayerX += toLayerStepX2 )
          {
            for ( int  toLayerY = toLayerSectionOriginY;
              toLayerY < toLayerSectionEndY;
              toLayerY += toLayerStepY2 )
            {
              final int  neuronID = cellmap_slow (
                toLayerPrefix,
                toLayerSuffix,
                toLayerX,
                toLayerY );

              xy = closePoint (
                ( int ) ( ( double ) numX / ( double ) denomX * toLayerX ),
                ( int ) ( ( double ) numY / ( double ) denomY * toLayerY ),
                fromLayerPrefix,
                fromLayerSuffix );
              
              newx = xy [ 0 ];
              
              newy = xy [ 1 ];       
              
// TODO:  left off cleaning up source code here              

              // if(neuronID == 59404
              //   && f_pre.equals("ONLGN")
              //   && f_suf.equals("E"))
              //   throw new RuntimeException(
              //     "find to and from neuron\n"
              //     +"x:"+(newx-conn.length/2*f_xstep)
              //     +"x:"+(newx+((conn.length%2)==0
              //     ?conn.length:conn.length+1)/2*f_xstep)
              //     +" y:"+(newy-conn[0].length/2*f_ystep)
              //     +" y:"+(newy+((conn[0].length%2)==0
              //     ?conn[0].length:conn[0].length+1)/2*f_ystep)
              //     +" \n center"+newx+","+newy);

              for(int itxx=newx-conn.length/2*fromLayerStepX;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*fromLayerStepX;itxx+=fromLayerStepX) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-conn[0].length/2*fromLayerStepY;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*fromLayerStepY;ityy+=fromLayerStepY)
                {
                  if(periodic)
                  {
                    xborder = itxx % fromLayerMultipliedEdgeLengthX;
                    yborder = ityy % fromLayerMultipliedEdgeLengthY;
                    if(xborder<0) xborder+=fromLayerMultipliedEdgeLengthX;
                    if(yborder<0) yborder+=fromLayerMultipliedEdgeLengthY;
                  }
                  else
                  {
                    if(itxx >= fromLayerMultipliedEdgeLengthX || itxx < 0 || ityy >= fromLayerMultipliedEdgeLengthY || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex =  cellmap_slow(fromLayerPrefix,fromLayerSuffix,xborder,yborder);

                  //    if(neuronID == 59404 && fromIndex == 18400) throw new RuntimeException("find to and from neuron");
                  if(Math.abs(conn[(itxx-newx+conn.length/2*fromLayerStepX)/fromLayerStepX][(ityy-newy+conn[0].length/2*fromLayerStepY)/fromLayerStepY])!=0)
                  {
                    //  if(fromIndex == 18400 && hostId == 6 ) throw new RuntimeException("couting 18400 now");

                    //  try {
                    Map<String, SizeID> content;
                    if(!scaffold.containsKey(fromIndex))  
                    {
                      scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                    }
                    else
                    {
                      content = scaffold.get(fromIndex);
                    }

                    if(!content.containsKey(String.valueOf(delay)))
                    {
                      int totSize=content.size(); // number of branches 
                      content.put(String.valueOf(delay), new SizeID(totSize,1));
                    }
                    else
                    {
                      (content.get(String.valueOf(delay)).size)++;
                    }
                    //  }

                    //  catch(Exception ex) {
                    //    throw new RuntimeException("size"+scaffold.length+" fromIndex"+fromIndex+" total"+totalNumNeurons());
                    //      }
                  }

                }
              }

            }
          }


        }
        else //divergent // change it into convergent
        {
          conn = FunUtil.lRotate90(trans);
          
          if(toLayerMultiplierX<0&&fromLayerMultiplierX>0)
          {
            numX = fromLayerMultiplierX;
            denomX = 1;
          }
          else if(toLayerMultiplierX<0&&fromLayerMultiplierX<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(toLayerMultiplierX>0&&fromLayerMultiplierX>0)
          {
            numX = fromLayerMultiplierX;
            denomX = toLayerMultiplierX;
          }
          else if(toLayerMultiplierX>0&&fromLayerMultiplierX<0)
          {
            numX = 1;
            denomX = toLayerMultiplierX;
          }


          if(toLayerMultiplierY<0&&fromLayerMultiplierY>0)
          {
            numY = fromLayerMultiplierY;
            denomY = 1;
          }
          else if(toLayerMultiplierY<0&&fromLayerMultiplierY<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(toLayerMultiplierY>0&&fromLayerMultiplierY>0)
          {
            numY = fromLayerMultiplierY;
            denomY = toLayerMultiplierY;
          }
          else if(toLayerMultiplierY>0&&fromLayerMultiplierY<0)
          {
            numY = 1;
            denomY = toLayerMultiplierY;
          }

          for(int iterx=toLayerSectionOriginX;iterx<toLayerSectionEndX;iterx+=toLayerStepX2)
          {
            for(int itery=toLayerSectionOriginY;itery<toLayerSectionEndY;itery+=toLayerStepY2)
            {
              int neuronID=cellmap_slow(toLayerPrefix,toLayerSuffix,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), fromLayerPrefix, fromLayerSuffix);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)toLayerStepX/(double)fromLayerStepX)))+1)*(fromLayerStepX);itxx<=newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)toLayerStepX/(double)fromLayerStepX))))*(fromLayerStepX);itxx+=fromLayerStepX) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)toLayerStepY/(double)denomY/(double)fromLayerStepY)))+1)*(fromLayerStepY);ityy<=newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)toLayerStepY/(double)denomY/(double)fromLayerStepY))))*(fromLayerStepY);ityy+=fromLayerStepY)  
                {
                  if(periodic)
                  {
                    xborder = itxx % fromLayerMultipliedEdgeLengthX;
                    yborder = ityy % fromLayerMultipliedEdgeLengthY;
                    if(xborder<0) xborder+=fromLayerMultipliedEdgeLengthX;
                    if(yborder<0) yborder+=fromLayerMultipliedEdgeLengthY;
                  }
                  else
                  {
                    if(itxx >= fromLayerMultipliedEdgeLengthX || itxx < 0 || ityy >= fromLayerMultipliedEdgeLengthY || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex = cellmap_slow(fromLayerPrefix,fromLayerSuffix,xborder,yborder);

                  int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/toLayerStepX+(((conn.length%2)==0?conn.length-1:conn.length)/2);
                  int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/toLayerStepY+(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2);

                  if(maX>=0 && maX < conn.length && maY>=0 && maY < conn[0].length )
                  {
                    if(Math.abs(conn[maX][maY])!=0)
                    {
                      Map<String, SizeID> content;
                      if(!scaffold.containsKey(fromIndex))  
                      {
                        scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                      }
                      else
                      {
                        content = scaffold.get(fromIndex);
                      }

                      if(!content.containsKey(String.valueOf(delay)))
                      {
                        int totSize=content.size(); // number of branches 
                        content.put(String.valueOf(delay), new SizeID(totSize,1));
                      }
                      else
                      {
                        (content.get(String.valueOf(delay)).size)++;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    /***********************************************************************
    *  Build up the Scaffold for later buiding up network structure. L connection probability connection
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    ***********************************************************************/
    public void popScaffold(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] proma, double [][] strma, int hostId, boolean periodic) // probability connections
    ////////////////////////////////////////////////////////////////////////
    {
      if(strma!=null && (proma.length != strma.length  || proma[0].length != strma[0].length)) throw new RuntimeException("matrix size doesn't match for probability connection");

      double maxOutput=-1.0;
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      throw new RuntimeException("sign of connection is wrong");
      if(type==0&&stre<0)
        throw new RuntimeException("sign of connection is wrong");



      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }

        double [][] conn=null;
        double [][] pconn;
        //find out the post synaptic neurons belong to which host?
          /*
      if(convergent) //convergence connections from many to 1, first iterate to side
      {
        if(strma!=null) conn = FunUtil.lRotate90(strma);
        pconn = FunUtil.lRotate90(proma);

        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }

        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
            xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

            newx=xy[0];
            newy=xy[1];
            //              if(neuronID == 18400 && t_pre.equals("SE") && t_suf.equals("U3") ) throw new RuntimeException("18400  has a target at host 6\n"+"x:"+(newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep)))+1)*(t_xstep))+"x:"+(newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep))))*(t_xstep))+" y:"+(newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep)))+1)*(t_ystep))+" y:"+(newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep))))*(t_ystep))+" \n center"+newx+","+newy);
            for(int itxx=newx-((int)((double)(pconn.length/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep)))+1)*(t_xstep);itxx<=newx+((int)((double)(((pconn.length%2)==0?pconn.length-1:pconn.length)/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep))))*(t_xstep);itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
            {
              for(int ityy=newy-((int)((double)(pconn[0].length/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep)))+1)*(t_ystep);ityy<=newy+((int)((double)(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep))))*(t_ystep);ityy+=t_ystep)  
              {
                if(periodic)
                {
                  xborder = itxx % t_xresosize;
                  yborder = ityy % t_yresosize;
                  if(xborder<0) xborder+=t_xresosize;
                  if(yborder<0) yborder+=t_yresosize;
                }
                else
                {
                  if(itxx >= t_xresosize || itxx < 0 || ityy >= t_yresosize || ityy<0) // out of boundary
                  {
                    continue;
                  }
                  else //within boundary
                  {
                    xborder = itxx;
                    yborder = ityy;
                  }
                }




                int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/f_xstep+(((pconn.length%2)==0?pconn.length-1:pconn.length)/2);
                int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/f_ystep+(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2);
                if(maX>=0 && maX < pconn.length && maY>=0 && maY < pconn[0].length )
                {
                  if(strma != null)
                  {
                    if(Math.abs(conn[maX][maY])!=0)
                    {

                      int node=FunUtil.hostId(nodeEndIndices,toIndex);
                      if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                      {
                        neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                      }
                    }
                  }
                  else
                  {
                    int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                    {
                      neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                    }

                  }
                }


              }
            }

          }
        }

      }
      else //divergent
      {
        if(strma!=null) conn = FunUtil.rRotate90(strma);
        pconn = FunUtil.rRotate90(proma);
        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }
        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
            if( neuronID <= neuron_end && neuronID >= base  )
            {
              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

              newx=xy[0];
              newy=xy[1];

              for(int itxx=newx-pconn.length/2*t_xstep;itxx<newx+((pconn.length%2)==0?pconn.length:pconn.length+1)/2*t_xstep;itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center

              {
                for(int ityy=newy-pconn[0].length/2*t_ystep;ityy<newy+((pconn[0].length%2)==0?pconn[0].length:pconn[0].length+1)/2*t_ystep;ityy+=t_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % t_xresosize;
                    yborder = ityy % t_yresosize;
                    if(xborder<0) xborder+=t_xresosize;
                    if(yborder<0) yborder+=t_yresosize;
                  }
                  else
                  {
                    if(itxx >= t_xresosize || itxx < 0 || ityy >= t_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                  //                  synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses;                
                  if(strma != null)
                  {
                    if(Math.abs(pconn[(itxx-newx+pconn.length/2*t_xstep)/t_xstep][(ityy-newy+pconn[0].length/2*t_ystep)/t_ystep])!=0)
                    {
                      int node=FunUtil.hostId(nodeEndIndices,toIndex);
                      if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                      {
                        neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                      }
                    }
                  }
                  else
                  {
                    int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                    {
                      neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                    }
                  }
                }
              }
            }

          }
        }

      }
           */


        //  make connections here.
        if(convergent) //convergence connections from many to 1, first iterate to side
        {

          if(strma!=null) conn = FunUtil.rRotate90(strma);
          pconn = FunUtil.rRotate90(proma);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            


              for(int itxx=newx-pconn.length/2*f_xstep;itxx<newx+((pconn.length%2)==0?pconn.length:pconn.length+1)/2*f_xstep;itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-pconn[0].length/2*f_ystep;ityy<newy+((pconn[0].length%2)==0?pconn[0].length:pconn[0].length+1)/2*f_ystep;ityy+=f_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex =  cellmap_slow(f_pre,f_suf,xborder,yborder);

                  //    if(neuronID == 59404 && fromIndex == 18400) throw new RuntimeException("find to and from neuron");
                  if(pconn[(itxx-newx+pconn.length/2*f_xstep)/f_xstep][(ityy-newy+pconn[0].length/2*f_ystep)/f_ystep]> Cnsran.ran2(idum))
                  {
                    if(strma != null)
                    {
                      if(Math.abs(conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep])!=0)
                      {
                        //  if(fromIndex == 18400 && hostId == 6 ) throw new RuntimeException("couting 18400 now");

                        //  try {
                        Map<String, SizeID> content;
                        if(!scaffold.containsKey(fromIndex))  
                        {
                          scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                        }
                        else
                        {
                          content = scaffold.get(fromIndex);
                        }

                        if(!content.containsKey(String.valueOf(delay)))
                        {
                          int totSize=content.size(); // number of branches 
                          content.put(String.valueOf(delay), new SizeID(totSize,1));
                        }
                        else
                        {
                          (content.get(String.valueOf(delay)).size)++;
                        }
                        //  }

                        //  catch(Exception ex) {
                        //    throw new RuntimeException("size"+scaffold.length+" fromIndex"+fromIndex+" total"+totalNumNeurons());
                        //      }
                      }
                    }
                    else
                    {
                      Map<String, SizeID> content;
                      if(!scaffold.containsKey(fromIndex))  
                      {
                        scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                      }
                      else
                      {
                        content = scaffold.get(fromIndex);
                      }

                      if(!content.containsKey(String.valueOf(delay)))
                      {
                        int totSize=content.size(); // number of branches 
                        content.put(String.valueOf(delay), new SizeID(totSize,1));
                      }
                      else
                      {
                        (content.get(String.valueOf(delay)).size)++;
                      }

                    }
                  }

                }
              }

            }
          }


        }
        else //divergent // change it into convergent
        {
          if(strma!=null) conn = FunUtil.lRotate90(strma);
          pconn = FunUtil.lRotate90(proma);

          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            
              for(int itxx=newx-((int)((double)(pconn.length/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep)))+1)*(f_xstep);itxx<=newx+((int)((double)(((pconn.length%2)==0?pconn.length-1:pconn.length)/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep))))*(f_xstep);itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-((int)((double)(pconn[0].length/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep)))+1)*(f_ystep);ityy<=newy+((int)((double)(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep))))*(f_ystep);ityy+=f_ystep)
                {

                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex = cellmap_slow(f_pre,f_suf,xborder,yborder);

                  int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/t_xstep+(((pconn.length%2)==0?pconn.length-1:pconn.length)/2);
                  int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/t_ystep+(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2);

                  if(maX>=0 && maX < pconn.length && maY>=0 && maY < pconn[0].length )
                  {
                    if(pconn[maX][maY] > Cnsran.ran2(idum))
                    {
                      if(strma != null)
                      {
                        if(Math.abs(conn[maX][maY])!=0)
                        {
                          Map<String, SizeID> content;
                          if(!scaffold.containsKey(fromIndex))  
                          {
                            scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                          }
                          else
                          {
                            content = scaffold.get(fromIndex);
                          }

                          if(!content.containsKey(String.valueOf(delay)))
                          {
                            int totSize=content.size(); // number of branches 
                            content.put(String.valueOf(delay), new SizeID(totSize,1));
                          }
                          else
                          {
                            (content.get(String.valueOf(delay)).size)++;
                          }
                        }
                      }
                      else
                      {
                        Map<String, SizeID> content;
                        if(!scaffold.containsKey(fromIndex))  
                        {
                          scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                        }
                        else
                        {
                          content = scaffold.get(fromIndex);
                        }

                        if(!content.containsKey(String.valueOf(delay)))
                        {
                          int totSize=content.size(); // number of branches 
                          content.put(String.valueOf(delay), new SizeID(totSize,1));
                        }
                        else
                        {
                          (content.get(String.valueOf(delay)).size)++;
                        }

                      }
                    }
                  }

                }
              }

            }
          }


        }

      }
    }

    /***********************************************************************
    * Build up the Scaffold for later building up network structure.
    * General connection
    ***********************************************************************/
    public void  popScaffold (
      final String          fromLayerPrefix,
      final String          fromLayerSuffix,
      final String          toLayerPrefix,
      final String          toLayerSuffix,
      final double          strength,
      final int             type,
      final double          delay,
      final double [ ] [ ]  trans,
      final int             hostId )
    ////////////////////////////////////////////////////////////////////////
    {
      // general pop up the scaffold
      
      // double maxOutput=-1.0;
      
      final int
        fromLayerMultiplierX
          = getXmultiplier ( fromLayerPrefix, fromLayerSuffix ),
        fromLayerMultiplierY
          = getYmultiplier ( fromLayerPrefix, fromLayerSuffix ),
        toLayerMultiplierX
          = getXmultiplier ( toLayerPrefix,   toLayerSuffix   ),
        toLayerMultiplierY
          = getYmultiplier ( toLayerPrefix,   toLayerSuffix   );
      
      int
        fromLayerStepX, // previously named f_xstep
        fromLayerStepY,
        fromLayerMultipliedEdgeLengthX, // previously named f_xresosize
        fromLayerMultipliedEdgeLengthY;
      
      int
        toLayerStepX,
        toLayerStepY,
        toLayerMultipliedEdgeLengthX,
        toLayerMultipliedEdgeLengthY;
      
      int
        denomX,
        numX;
      
      int
        denomY,
        numY;
      
//      int xmagratio=0;
//      int ymagratio=0;
//      int xborder,yborder;
//      int newx=0;
//      int newy=0;
//      int[] xy=new int[2];

      final int
        secX = hostId / sectionsInAxisY,
        secY = hostId % sectionsInAxisY;

      int
        fromLayerStepX2, // previously named xstep_ff
        fromLayerStepY2,
        fromLayerSectionEndX, // previously named xresosize_ff
        fromLayerSectionEndY;
      
      int
        toLayerStepX2,
        toLayerStepY2,
        toLayerSectionEndX,
        toLayerSectionEndY;
      
      int
        fromLayerSectionOriginX, // previously named xOri_ff
        fromLayerSectionOriginY;
      
      int
        toLayerSectionOriginX,
        toLayerSectionOriginY;

      if ( fromLayerMultiplierX > 0 )
      {
        fromLayerStepX2 = 1;
        
        final double  layerNeuronsPerSectionX
          = fromLayerMultiplierX * edgeLengthX / ( double ) sectionsInAxisX;
        
        if ( secX != 0 )
        {
          fromLayerSectionOriginX
            = ( int ) ( layerNeuronsPerSectionX * secX );
        }
        else
        {
          fromLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          fromLayerSectionEndX
            = ( int ) ( layerNeuronsPerSectionX * ( secX + 1 ) );
        }
        else
        {
          fromLayerSectionEndX = fromLayerMultiplierX * edgeLengthX;
        }
      }
      else // f_xmulti <= 0
      {
        fromLayerStepX2 = -fromLayerMultiplierX;
        
        if ( secX != 0 )
        {
          fromLayerSectionOriginX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX
            * ( double ) ( secX ) );
          
          fromLayerSectionOriginX = fromLayerSectionOriginX
            + fromLayerStepX2 - fromLayerSectionOriginX % fromLayerStepX2;
        }
        else
        {
          fromLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          fromLayerSectionEndX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX
            * ( double ) ( secX + 1 ) );
          
          fromLayerSectionEndX = fromLayerSectionEndX
            + fromLayerStepX2 -  fromLayerSectionEndX % fromLayerStepX2;
          
          if ( fromLayerSectionEndX > edgeLengthX )
          {
            fromLayerSectionEndX = edgeLengthX;
          }
        }
        else
        {
          fromLayerSectionEndX = edgeLengthX;
        }
      }
      
      if ( fromLayerMultiplierY > 0 )
      {
        fromLayerStepY2 = 1;
        
        final double  layerNeuronsPerSectionY
          = fromLayerMultiplierY * edgeLengthY / ( double ) sectionsInAxisY;
        
        if ( secY != 0 )
        {          
          fromLayerSectionOriginY
            = ( int ) ( layerNeuronsPerSectionY * secY );
        }
        else
        {
          fromLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          fromLayerSectionEndY
            = ( int ) ( layerNeuronsPerSectionY * ( secY + 1 ) );
        }
        else
        {
          fromLayerSectionEndY = fromLayerMultiplierY * edgeLengthY;
        }
      }
      else
      {
        fromLayerStepY2 = -fromLayerMultiplierY;
        
        if ( secY != 0 )
        {
          fromLayerSectionOriginY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY ) );
          
          fromLayerSectionOriginY = fromLayerSectionOriginY
            + fromLayerStepY2 - fromLayerSectionOriginY % fromLayerStepY2;
        }
        else
        {
          fromLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          fromLayerSectionEndY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY + 1 ) );
          
          fromLayerSectionEndY = fromLayerSectionEndY
            + fromLayerStepY2 - fromLayerSectionEndY % fromLayerStepY2;
          
          if ( fromLayerSectionEndY > edgeLengthY )
          {
            fromLayerSectionEndY = edgeLengthY;
          }
        }
        else
        {
          fromLayerSectionEndY = edgeLengthY;
        }
      }

      if ( toLayerMultiplierX > 0 )
      {
        toLayerStepX2 = 1;
        
        if ( secX != 0 )
        {
          toLayerSectionOriginX = ( int ) ( ( double ) toLayerMultiplierX
            * ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX ) );
        }
        else
        {
          toLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          toLayerSectionEndX = ( int ) ( ( double ) toLayerMultiplierX
            * ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX + 1 ) );
        }
        else
        {
          toLayerSectionEndX = toLayerMultiplierX * edgeLengthX;
        }
      }
      else
      {
        toLayerStepX2 = -toLayerMultiplierX;
        
        if ( secX != 0 )
        {
          toLayerSectionOriginX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX ) );
          
          toLayerSectionOriginX = toLayerSectionOriginX
            + toLayerStepX2 - toLayerSectionOriginX % toLayerStepX2;
        }
        else
        {
          toLayerSectionOriginX = 0;
        }
        
        if ( secX != sectionsInAxisX - 1 )
        {
          toLayerSectionEndX = ( int ) ( ( double ) edgeLengthX
            / ( double ) sectionsInAxisX * ( double ) ( secX + 1 ) );
          
          toLayerSectionEndX = toLayerSectionEndX
            + toLayerStepX2 - toLayerSectionEndX % toLayerStepX2;
          
          if ( toLayerSectionEndX > edgeLengthX )
          {
            toLayerSectionEndX = edgeLengthX;
          }
        }
        else
        {
          toLayerSectionEndX = edgeLengthX;
        }
      }
      
      if ( toLayerMultiplierY > 0 )
      {
        toLayerStepY2 = 1;
        
        if ( secY != 0 )
        {
          toLayerSectionOriginY = ( int ) ( ( double ) toLayerMultiplierY
            * ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY ) );
        }
        else
        {
          toLayerSectionOriginY = 0;
        }
        
        if ( secY != sectionsInAxisY - 1 )
        {
          toLayerSectionEndY = ( int ) ( ( double ) toLayerMultiplierY
            * ( double ) edgeLengthY / ( double ) sectionsInAxisY
            * ( double ) ( secY + 1 ) );
        }
        else
        {
          toLayerSectionEndY = toLayerMultiplierY * edgeLengthY;
        }
      }
      else
      {
        toLayerStepY2 = -toLayerMultiplierY;
        
        if ( secY != 0 )
        {
          toLayerSectionOriginY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY ) );
          
          toLayerSectionOriginY = toLayerSectionOriginY
            + toLayerStepY2 - toLayerSectionOriginY % toLayerStepY2;
        }
        else
        {
          toLayerSectionOriginY=0;
        }
        
        if ( secY != sectionsInAxisY-1 )
        {
          toLayerSectionEndY = ( int ) ( ( double ) edgeLengthY
            / ( double ) sectionsInAxisY * ( double ) ( secY + 1 ) );
          
          toLayerSectionEndY = toLayerSectionEndY
            + toLayerStepY2 - toLayerSectionEndY % toLayerStepY2;
          
          if ( toLayerSectionEndY > edgeLengthY )
          {
            toLayerSectionEndY = edgeLengthY;
          }
        }
        else
        {
          toLayerSectionEndY = edgeLengthY;
        }
      }
      
      // these may not be useful for new neuronal model

      if ( type == 1
        && strength > 0 )
      {        
        throw new RuntimeException ( "sign of connection is wrong" );
      }
      
      if ( type == 0
        && strength < 0 )
      {
        throw new RuntimeException ( "sign of connection is wrong" );
      }

      if ( strength != 0 )
      {
        if ( fromLayerMultiplierX > 0 )
        {
          fromLayerStepX = 1;
          
          fromLayerMultipliedEdgeLengthX
            = fromLayerMultiplierX * edgeLengthX;
        }
        else
        {
          fromLayerStepX = -fromLayerMultiplierX;
          
          fromLayerMultipliedEdgeLengthX = edgeLengthX;
        }
        
        if ( fromLayerMultiplierY > 0 )
        {
          fromLayerStepY = 1;
          
          fromLayerMultipliedEdgeLengthY
            = fromLayerMultiplierY * edgeLengthY;
        }
        else
        {
          fromLayerStepY = -fromLayerMultiplierY;
          
          fromLayerMultipliedEdgeLengthY = edgeLengthY;
        }
        
        if ( toLayerMultiplierX > 0 )
        {
          toLayerStepX = 1;
          
          toLayerMultipliedEdgeLengthX = toLayerMultiplierX * edgeLengthX;
        }
        else
        {
          toLayerStepX = -toLayerMultiplierX;
          
          toLayerMultipliedEdgeLengthX = edgeLengthX;
        }
        
        if ( toLayerMultiplierY > 0 )
        {
          toLayerStepY = 1;
          
          toLayerMultipliedEdgeLengthY = toLayerMultiplierY * edgeLengthY;
        }
        else
        {
          toLayerStepY = -toLayerMultiplierY;
          
          toLayerMultipliedEdgeLengthY = edgeLengthY;
        }

        //find out the post synaptic neurons belong to which host?
        
        /*
      {

        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }

        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);

            int fIndex = (iterx / f_xstep)
              * (f_yresosize / f_ystep) + itery / f_ystep;
             
            for(int itxx=0;itxx<t_xresosize;itxx+=t_xstep)
            {
              for(int ityy=0;ityy<t_yresosize;ityy+=t_ystep)  
              {
                int toIndex = cellmap_slow(t_pre,t_suf,itxx,ityy);
                int tIndex = (itxx / t_xstep)*(t_yresosize / t_ystep)
                  + ityy / t_ystep;
                if(Math.abs(trans[fIndex % trans.length]
                  [tIndex % trans[0].length])!=0)
                {

                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                  // if the neuron doen'st have a target synapse at node
                  // then set it.
                  if((neurons[neuronID - base].getTHost() & (1L<<node) )==0)
                  {
                    neurons[neuronID - base].setTHost(
                      (neurons[neuronID - base].getTHost() | (1L<< node)));
                  }
                }
              }
            }

          }
        }

      }
         */

        {
          if (   toLayerMultiplierX < 0
            && fromLayerMultiplierX > 0 )
          {
            numX = fromLayerMultiplierX;
            
            denomX = 1;
          }
          else if (   toLayerMultiplierX < 0
                 && fromLayerMultiplierX < 0 )
          {
            numX = 1;
            
            denomX = 1;
          }
          else if (   toLayerMultiplierX > 0
                 && fromLayerMultiplierX > 0 )
          {
            numX = fromLayerMultiplierX;
            
            denomX = toLayerMultiplierX;
          }
          else if (   toLayerMultiplierX > 0
                 && fromLayerMultiplierX < 0 )
          {
            numX = 1;
            
            denomX = toLayerMultiplierX;
          }

          if (   toLayerMultiplierY < 0
            && fromLayerMultiplierY > 0 )
          {
            numY = fromLayerMultiplierY;
            
            denomY = 1;
          }
          else if (   toLayerMultiplierY < 0
                 && fromLayerMultiplierY < 0 )
          {
            numY = 1;
            
            denomY = 1;
          }
          else if (   toLayerMultiplierY > 0
                 && fromLayerMultiplierY > 0 )
          {
            numY = fromLayerMultiplierY;
            
            denomY = toLayerMultiplierY;
          }
          else if (   toLayerMultiplierY > 0
                 && fromLayerMultiplierY < 0 )
          {
            numY = 1;
            
            denomY = toLayerMultiplierY;
          }

          for ( int  toLayerX = toLayerSectionOriginX;
            toLayerX < toLayerSectionEndX;
            toLayerX += toLayerStepX2 )
          {
            for ( int  toLayerY = toLayerSectionOriginY;
              toLayerY < toLayerSectionEndY;
              toLayerY += toLayerStepY2 )
            {
              final int  neuronID = cellmap_slow (
                toLayerPrefix,
                toLayerSuffix,
                toLayerX,
                toLayerY );
              
              final int  tIndex = ( toLayerX / toLayerStepX )
                * ( toLayerMultipliedEdgeLengthY / toLayerStepY )
                + toLayerY / toLayerStepY;
              
              for ( int  itxx = 0;
                itxx < fromLayerMultipliedEdgeLengthX;
                itxx += fromLayerStepX )
              {
                for ( int  ityy = 0;
                  ityy < fromLayerMultipliedEdgeLengthY;
                  ityy += fromLayerStepY )
                {
                  final int  fromIndex = cellmap_slow (
                    fromLayerPrefix,
                    fromLayerSuffix,
                    itxx,
                    ityy );
                  
                  final int  fIndex = ( itxx / fromLayerStepX )
                    * ( fromLayerMultipliedEdgeLengthY / fromLayerStepY )
                    + ityy / fromLayerStepY;
                  
                  if ( Math.abs (
                    trans [ fIndex % trans.length ]
                          [ tIndex % trans [ 0 ].length ] ) != 0 )
                  {
                    Map<String, SizeID>  content;
                    
                    if ( !scaffold.containsKey ( fromIndex ) )  
                    {
                      scaffold.put (
                        fromIndex,
                        content = ( new TreeMap<String, SizeID> ( ) ) );
                    }
                    else
                    {
                      content = scaffold.get ( fromIndex );
                    }

                    if ( !content.containsKey ( String.valueOf ( delay ) ) )
                    {
                      // number of branches
                      
                      final int  totSize = content.size ( );
                      
                      content.put (
                        String.valueOf ( delay ),
                        new SizeID ( totSize, 1 ) );
                    }
                    else
                    {
                      ( content.get ( String.valueOf ( delay ) ).size )++;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    /***********************************************************************
    * Build up the Scaffold for later buiding up network structure.
    * General population connection
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    ***********************************************************************/
    public void popScaffold(String f_pre, String f_suf, String t_pre, String t_suf, double stre, int type, double delay, double [][] proma, double[][] strma, int hostId) //general probability pop up the scaffold
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      throw new RuntimeException("sign of connection is wrong");
      if(type==0&&stre<0)
        throw new RuntimeException("sign of connection is wrong");



      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }


        //find out the post synaptic neurons belong to which host?
        /*
      {

        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }

        for(int iterx=xOri_ff;iterx<xresosize_ff;iterx+=xstep_ff)
        {
          for(int itery=yOri_ff;itery<yresosize_ff;itery+=ystep_ff)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);

            int fIndex = (iterx / f_xstep) * (f_yresosize / f_ystep) + itery / f_ystep;
            for(int itxx=0;itxx<t_xresosize;itxx+=t_xstep)
            {
              for(int ityy=0;ityy<t_yresosize;ityy+=t_ystep)  
              {
                int toIndex = cellmap_slow(t_pre,t_suf,itxx,ityy);
                int tIndex = (itxx / t_xstep)*(t_yresosize / t_ystep) + ityy / t_ystep;
                if(strma != null)
                {
                  if(Math.abs(strma[fIndex % strma.length][tIndex % strma[0].length])!=0)
                  {

                    int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                    {
                      neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                    }
                  }
                }
                else
                {
                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                  if((neurons[neuronID - base].getTHost() & (1L<<node) )==0) // if the neuron doen'st have a target synapse at node then set it.
                  {
                    neurons[neuronID - base].setTHost((neurons[neuronID - base].getTHost() | (1L<< node)));

                  }

                }
              }
            }

          }
        }

      }
         */

        {
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);
              int tIndex = (iterx / xstep_tt) * (yresosize_tt / ystep_tt) + itery / ystep_tt;
              for(int itxx=0;itxx<f_xresosize;itxx+=f_xstep)
              {
                for(int ityy=0;ityy<f_yresosize;ityy+=f_ystep)
                {
                  int fromIndex =  cellmap_slow(f_pre,f_suf,itxx,ityy);
                  int fIndex = (itxx / f_xstep)*(f_yresosize / f_ystep) + ityy / f_ystep;
                  if(Math.abs(proma[fIndex % proma.length][tIndex % proma[0].length])> Cnsran.ran2(idum))
                  {
                    if(strma != null)
                    {
                      if(Math.abs(strma[fIndex % strma.length][tIndex % strma[0].length])!=0)
                      {
                        Map<String, SizeID> content;
                        if(!scaffold.containsKey(fromIndex))  
                        {
                          scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                        }
                        else
                        {
                          content = scaffold.get(fromIndex);
                        }

                        if(!content.containsKey(String.valueOf(delay)))
                        {
                          int totSize=content.size(); // number of branches 
                          content.put(String.valueOf(delay), new SizeID(totSize,1));
                        }
                        else
                        {
                          (content.get(String.valueOf(delay)).size)++;
                        }
                      }
                    }
                    else
                    {
                      Map<String, SizeID> content;
                      if(!scaffold.containsKey(fromIndex))  
                      {
                        scaffold.put(fromIndex,content = (new TreeMap<String, SizeID>()) );
                      }
                      else
                      {
                        content = scaffold.get(fromIndex);
                      }

                      if(!content.containsKey(String.valueOf(delay)))
                      {
                        int totSize=content.size(); // number of branches 
                        content.put(String.valueOf(delay), new SizeID(totSize,1));
                      }
                      else
                      {
                        (content.get(String.valueOf(delay)).size)++;
                      }

                    }
                  }
                }
              }

            }
          }


        }
      }

    }

    /***********************************************************************
    *  For nethost with id Hostid, build up the structure by using the scaffold build before.
    * 
    * @param hostId
    ***********************************************************************/
    public void buildStructure(int hostId)
    {
      //    axons = new HashMap<Integer, Axon>((int)((double)scaffold.size()/0.75));
      axons = new HashMap<Integer, Axon>(scaffold.size());
      Set entries = scaffold.entrySet();
      Iterator entryIter = entries.iterator();
      while(entryIter.hasNext())
      {
        Map.Entry<Integer, Map<String, SizeID> > entry = (Map.Entry<Integer, Map<String, SizeID> >)entryIter.next();          
        Integer key = entry.getKey();
        Map<String, SizeID> value = entry.getValue(); 

        Iterator<String> iter = value.keySet().iterator();

        Axon caxon;

        if(!axons.containsKey(key))
        {
          axons.put(key, caxon=(new Axon()));
        }
        else
        {
          caxon = axons.get(key);
        }

        caxon.branches = new Branch[value.size()];
        //      Branch[] mybranches=new Branch[scaffold[a].size()];
        //      int currBranch=0;
        while(iter.hasNext()) 
        {
          String branchKey = iter.next();
          SizeID sid = value.get(branchKey);
          Synapse[] syns=new Synapse[(int)(sid.size)];
          sid.size=0;
          //        double delay=Double.parseDouble(branchKey.substring(0,branchKey.lastIndexOf(" ")));
          double delay=Double.parseDouble(branchKey);
          //        mybranches[currBranch]=new Branch(syns,delay);
          caxon.branches[(int)(sid.id)]=new Branch(syns,delay);
          //        currBranch++;
        }
      }
    }

    public void sortSynapses(int hostId)
    ////////////////////////////////////////////////////////////////////////
    {
      Iterator<Axon> values = axons.values().iterator();
      while(values.hasNext())
      {
        Axon caxon = values.next();
        for(int i=0;i<caxon.branches.length; i++)
        {
          Arrays.sort(caxon.branches[i].synapses);
        }
      }
    }

    /***********************************************************************
    *  Makeing connections between from and to layer of neurons, L connection.
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    * @return Largest weight
    ***********************************************************************/
    public double connect(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] trans, int hostId, boolean periodic)
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }

        double [][] conn;
        //  make connections here.
        if(convergent) //convergence connections from many to 1, first iterate to side
        {

          conn = FunUtil.rRotate90(trans);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-conn.length/2*f_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*f_xstep;itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-conn[0].length/2*f_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*f_ystep;ityy+=f_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex =  cellmap_slow(f_pre,f_suf,xborder,yborder);
                  if(Math.abs(conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep])!=0)
                  {
                    double synWei = stre*conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep];
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }

                    //                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                    //if(fromIndex == 38036 && hostId == 1) throw new RuntimeException("this has a syn"+axons[fromIndex].branches.length);
                    
                    axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                      = modelFactory.createSynapse (
                        neuronID,
                        ( byte ) type,
                        ( float ) synWei );
                    
                    numSYN++;


                    //  neurons[neuronID-base].getAxon().branches[(int)(sid.id)].synapses[(int)(sid.size)] = new Synapse(toIndex,synWei,type);
                    sid.size++;
                  }

                }
              }

            }
          }


        }
        else //divergent // change it into convergent
        {
          conn = FunUtil.lRotate90(trans);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep)))+1)*(f_xstep);itxx<=newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep))))*(f_xstep);itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep)))+1)*(f_ystep);ityy<=newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep))))*(f_ystep);ityy+=f_ystep)  
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }
                  int fromIndex = cellmap_slow(f_pre,f_suf,xborder,yborder);

                  int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/t_xstep+(((conn.length%2)==0?conn.length-1:conn.length)/2);
                  int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/t_ystep+(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2);
                  if(maX>=0 && maX < conn.length && maY>=0 && maY < conn[0].length )
                  {
                    if(Math.abs(conn[maX][maY])!=0)
                    {
                      double synWei = stre*conn[maX][maY];
                      if(Math.abs(synWei) > SIFNeuron.maxWeight)
                      {
                        if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                      }

                      SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                      
                      axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                        = modelFactory.createSynapse (
                          neuronID,
                          ( byte ) type,
                          ( float ) synWei );
                      
                      numSYN++;


                      sid.size++;
                    }
                  }

                }
              }

            }
          }


        }

      }
      return maxOutput;  
    }

    public void setSeed(Seed i)
    ////////////////////////////////////////////////////////////////////////
    {
      backseed = i.seed;
      System.arraycopy(Cnsran.iv,0,back_iv,0,32);
      back_iy = Cnsran.iy;
      back_idum2=Cnsran.idum2;
      this.idum = new Seed(backseed);
    }

    public void reSetSeed()
    ////////////////////////////////////////////////////////////////////////
    {
      this.idum = new Seed(backseed);
      System.arraycopy(back_iv,0,Cnsran.iv,0,32);
      Cnsran.iy = back_iy;
      Cnsran.idum2 = back_idum2;
    }

    /***********************************************************************
    *  Makeing connections between from and to layer of neurons, probability connection.
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    * @return Largest weight
    ***********************************************************************/
    public double connect(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] proma, double [][] strma, int hostId, boolean periodic) // probability connections
    ////////////////////////////////////////////////////////////////////////
    {
      if(strma!=null && (proma.length != strma.length  || proma[0].length != strma[0].length)) throw new RuntimeException("matrix size doesn't match for probability connection");


      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }

        double [][] conn=null;
        double [][] pconn;
        //  make connections here.
        if(convergent) //convergence connections from many to 1, first iterate to side
        {

          if(strma!=null) conn = FunUtil.rRotate90(strma);
          pconn = FunUtil.rRotate90(proma);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-pconn.length/2*f_xstep;itxx<newx+((pconn.length%2)==0?pconn.length:pconn.length+1)/2*f_xstep;itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-pconn[0].length/2*f_ystep;ityy<newy+((pconn[0].length%2)==0?pconn[0].length:pconn[0].length+1)/2*f_ystep;ityy+=f_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex =  cellmap_slow(f_pre,f_suf,xborder,yborder);
                  if(pconn[(itxx-newx+pconn.length/2*f_xstep)/f_xstep][(ityy-newy+pconn[0].length/2*f_ystep)/f_ystep]> Cnsran.ran2(idum))
                  {
                    if(strma != null)
                    {
                      if(Math.abs(conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep])!=0)
                      {
                        double synWei = stre*conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep];
                        if(Math.abs(synWei) > SIFNeuron.maxWeight)
                        {
                          if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                        }

                        //                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                        SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                        //if(fromIndex == 38036 && hostId == 1) throw new RuntimeException("this has a syn"+axons[fromIndex].branches.length);
                        
                        axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                          = modelFactory.createSynapse (
                            neuronID,
                            ( byte ) type,
                            ( float ) synWei );
                        
                        numSYN++;


                        //  neurons[neuronID-base].getAxon().branches[(int)(sid.id)].synapses[(int)(sid.size)] = new Synapse(toIndex,synWei,type);
                        sid.size++;
                      }
                    }
                    else
                    {
                      double synWei = stre;
                      if(Math.abs(synWei) > SIFNeuron.maxWeight)
                      {
                        if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                      }

                      //                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                      SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                      //if(fromIndex == 38036 && hostId == 1) throw new RuntimeException("this has a syn"+axons[fromIndex].branches.length);
                      
                      axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                        = modelFactory.createSynapse (
                          neuronID,
                          ( byte ) type,
                          ( float ) synWei );
                      
                      numSYN++;

                      //  neurons[neuronID-base].getAxon().branches[(int)(sid.id)].synapses[(int)(sid.size)] = new Synapse(toIndex,synWei,type);
                      
                      sid.size++;
                    }
                  }

                }
              }

            }
          }


        }
        else //divergent // change it into convergent
        {
          if(strma!=null) conn = FunUtil.lRotate90(strma);
          pconn = FunUtil.lRotate90(proma);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-((int)((double)(pconn.length/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep)))+1)*(f_xstep);itxx<=newx+((int)((double)(((pconn.length%2)==0?pconn.length-1:pconn.length)/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep))))*(f_xstep);itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-((int)((double)(pconn[0].length/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep)))+1)*(f_ystep);ityy<=newy+((int)((double)(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep))))*(f_ystep);ityy+=f_ystep)  
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }
                  int fromIndex = cellmap_slow(f_pre,f_suf,xborder,yborder);

                  int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/t_xstep+(((pconn.length%2)==0?pconn.length-1:pconn.length)/2);
                  int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/t_ystep+(((pconn[0].length%2)==0?pconn[0].length-1:pconn[0].length)/2);
                  if(maX>=0 && maX < pconn.length && maY>=0 && maY < pconn[0].length )
                  {
                    if(pconn[maX][maY] > Cnsran.ran2(idum))
                    {
                      if(strma != null)
                      {
                        if(Math.abs(conn[maX][maY])!=0)
                        {
                          double synWei = stre*conn[maX][maY];
                          if(Math.abs(synWei) > SIFNeuron.maxWeight)
                          {
                            if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                          }

                          SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                          
                          axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                            = modelFactory.createSynapse (
                              neuronID,
                              ( byte ) type,
                              ( float ) synWei );
                          
                          numSYN++;
                          sid.size++;
                        }
                      }
                      else
                      {
                        double synWei = stre;
                        if(Math.abs(synWei) > SIFNeuron.maxWeight)
                        {
                          if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                        }

                        SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                        
                        axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                          = modelFactory.createSynapse (
                            neuronID,
                            ( byte ) type,
                            ( float ) synWei );
                        
                        numSYN++;

                        sid.size++;

                      }
                    }
                  }

                }
              }

            }
          }


        }

      }
      return maxOutput;  
    }

    /***********************************************************************
    *  Makeing connections between from and to layer of neurons, general connection.
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    * @return Largest weight
    ***********************************************************************/
    public double connect(String f_pre, String f_suf, String t_pre, String t_suf, double stre, int type, double delay, double[][] trans, int hostId) //general connection
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      TreeSet<Synapse> synapses;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }


        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = 1;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = t_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = 1;
          denomX = t_xmulti;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = 1;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = t_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = 1;
          denomY = t_ymulti;
        }

        for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
        {
          for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
          {
            int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);
            int tIndex = (iterx / t_xstep) * (t_yresosize / t_ystep) + itery / t_ystep;

            for(int itxx=0;itxx<f_xresosize;itxx+=f_xstep)
            {
              for(int ityy=0;ityy<f_yresosize;ityy+=f_ystep)
              {
                int fIndex = (itxx / f_xstep)*(f_yresosize / f_ystep) + ityy / f_ystep;

                int fromIndex =  cellmap_slow(f_pre,f_suf,itxx,ityy);
                if(Math.abs(trans[fIndex % trans.length][tIndex % trans[0].length])!=0)
                {
                  double synWei = stre*trans[fIndex % trans.length][tIndex % trans[0].length];
                  if(Math.abs(synWei) > SIFNeuron.maxWeight)
                  {
                    if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                  }

                  SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                  
                  axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                    = modelFactory.createSynapse (
                      neuronID,
                      ( byte ) type,
                      ( float ) synWei );
                  
                  numSYN++;


                  sid.size++;
                }
              }
            }

          }
        }
      }
      return maxOutput;  
    }

    /***********************************************************************
    *  Makeing connections between from and to layer of neurons, general probability connection.
    * 
    * @param f_pre
    * @param f_suf
    * @param t_pre
    * @param t_suf
    * @param convergent
    * @param stre
    * @param type
    * @param delay
    * @param trans
    * @param hostId
    * @param periodic
    * @return Largest weight
    ***********************************************************************/
    public double connectgp(String f_pre, String f_suf, String t_pre, String t_suf, double stre, int type, double delay, double[][] proma, double [][] strma, int hostId) //general probability connection
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      TreeSet<Synapse> synapses;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }


        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = 1;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = t_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = 1;
          denomX = t_xmulti;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = 1;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = t_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = 1;
          denomY = t_ymulti;
        }

        for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
        {
          for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
          {
            int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);
            int tIndex = (iterx / t_xstep) * (t_yresosize / t_ystep) + itery / t_ystep;

            for(int itxx=0;itxx<f_xresosize;itxx+=f_xstep)
            {
              for(int ityy=0;ityy<f_yresosize;ityy+=f_ystep)
              {
                int fIndex = (itxx / f_xstep)*(f_yresosize / f_ystep) + ityy / f_ystep;

                int fromIndex =  cellmap_slow(f_pre,f_suf,itxx,ityy);
                if(Math.abs(proma[fIndex % proma.length][tIndex % proma[0].length])> Cnsran.ran2(idum))
                {
                  if(strma != null)
                  {
                    if(Math.abs(strma[fIndex % strma.length][tIndex % strma[0].length])!=0)
                    {
                      double synWei = stre*strma[fIndex % strma.length][tIndex % strma[0].length];
                      if(Math.abs(synWei) > SIFNeuron.maxWeight)
                      {
                        if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                      }

                      SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                      
                      axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                        = modelFactory.createSynapse (
                          neuronID,
                          ( byte ) type,
                          ( float ) synWei );
                      
                      numSYN++;

                      sid.size++;
                    }
                  }
                  else
                  {
                    double synWei = stre;
                    
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }

                    SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));
                    
                    axons.get(fromIndex).branches[(int)(sid.id)].synapses[(int)(sid.size)]
                      = modelFactory.createSynapse (
                        neuronID,
                        ( byte ) type,
                        ( float ) synWei );
                    
                    numSYN++;
                    
                    sid.size++;
                  }
                }

              }
            }

          }
        }
      }
      return maxOutput;  
    }

    @Deprecated
    public double changeConnection(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] trans, int hostId, boolean periodic)
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }

        double [][] conn;
        //  make connections here.
        if(convergent) //convergence connections from many to 1, first iterate to side
        {

          conn = FunUtil.rRotate90(trans);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-conn.length/2*f_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*f_xstep;itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-conn[0].length/2*f_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*f_ystep;ityy+=f_ystep)
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }

                  int fromIndex =  cellmap_slow(f_pre,f_suf,xborder,yborder);
                  if(Math.abs(conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep])!=0)
                  {
                    double synWei = stre*conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep];
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }

                    //                  int node=FunUtil.hostId(nodeEndIndices,toIndex);
                    SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));

                    final int index = Arrays.binarySearch (
                      axons.get ( fromIndex ).branches [ ( int ) ( sid.id ) ].synapses,
                      modelFactory.createSynapse (
                        neuronID,
                        ( byte ) type,
                        ( float ) synWei ) );

                    if(index>=0)
                    {
                      final MutableSynapse  synapseMut = ( MutableSynapse )
                        axons.get(fromIndex).branches[(int)(sid.id)].synapses[index];
                      
                      synapseMut.setWeight ( ( float ) synWei );
                    }
                    else
                    {
                      throw new RuntimeException("modify none existing synapse");
                    }
                  }

                }
              }

            }
          }


        }
        else //divergent // change it into convergent
        {
          conn = FunUtil.lRotate90(trans);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = 1;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = f_xmulti;
            denomX = t_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = 1;
            denomX = t_xmulti;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = 1;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = f_ymulti;
            denomY = t_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = 1;
            denomY = t_ymulti;
          }

          for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
          {
            for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
            {
              int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);

              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
              newx=xy[0];
              newy=xy[1];            

              for(int itxx=newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep)))+1)*(f_xstep);itxx<=newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)t_xstep/(double)f_xstep))))*(f_xstep);itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
              {
                for(int ityy=newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep)))+1)*(f_ystep);ityy<=newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)t_ystep/(double)denomY/(double)f_ystep))))*(f_ystep);ityy+=f_ystep)  
                {
                  if(periodic)
                  {
                    xborder = itxx % f_xresosize;
                    yborder = ityy % f_yresosize;
                    if(xborder<0) xborder+=f_xresosize;
                    if(yborder<0) yborder+=f_yresosize;
                  }
                  else
                  {
                    if(itxx >= f_xresosize || itxx < 0 || ityy >= f_yresosize || ityy<0) // out of boundary
                    {
                      continue;
                    }
                    else //within boundary
                    {
                      xborder = itxx;
                      yborder = ityy;
                    }
                  }
                  int fromIndex = cellmap_slow(f_pre,f_suf,xborder,yborder);


                  int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/t_xstep+(((conn.length%2)==0?conn.length-1:conn.length)/2);
                  int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/t_ystep+(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2);

                  if(maX>=0 && maX < conn.length && maY>=0 && maY < conn[0].length )
                  {
                    if(Math.abs(conn[maX][maY])!=0)
                    {
                      double synWei = stre*conn[maX][maY];
                      if(Math.abs(synWei) > SIFNeuron.maxWeight)
                      {
                        if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                      }

                      SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));

                      int index = Arrays.binarySearch (
                        axons.get(fromIndex).branches[(int)(sid.id)].synapses,
                        modelFactory.createSynapse (
                          neuronID,
                          ( byte ) type,
                          ( float ) synWei ) );

                      if(index>=0)
                      {
                        final MutableSynapse  synapseMut = ( MutableSynapse )
                          axons.get(fromIndex).branches[(int)(sid.id)].synapses[index];
                        
                        synapseMut.setWeight ( ( float ) synWei );
                      }
                      else
                      {
                        throw new RuntimeException("modify none existing synapse");
                      }

                    }
                  }

                }
              }

            }
          }


        }

      }
      return maxOutput;  
    }

    @Deprecated
    public double changeConnection(String f_pre, String f_suf, String t_pre, String t_suf, double stre, int type, double delay, double[][] trans, int hostId) //general change connection
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput=-1.0;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      //    double[] curr=new double[2];
      //    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
      //    String fromNeur,toNeur;
      TreeSet<Synapse> synapses;
      int[] xy=new int[2];

      int secX = hostId/sectionsInAxisY;
      int secY = hostId%sectionsInAxisY;

      int xstep_ff,ystep_ff,xresosize_ff,yresosize_ff;
      int xstep_tt,ystep_tt,xresosize_tt,yresosize_tt;
      int xOri_ff,yOri_ff;
      int xOri_tt,yOri_tt;


      if(f_xmulti>0)
      {
        xstep_ff=1;
        if(secX!=0)
        {
          xOri_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)f_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_ff=f_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_ff=-f_xmulti;
        if(secX!=0)
        {
          xOri_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_ff=xOri_ff + xstep_ff - xOri_ff %xstep_ff;
        }
        else
        {
          xOri_ff=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_ff=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_ff=xresosize_ff + xstep_ff -  xresosize_ff % xstep_ff;
          if(xresosize_ff>edgeLengthX)xresosize_ff=edgeLengthX;
        }
        else
        {
          xresosize_ff=edgeLengthX;
        }
      }
      if(f_ymulti>0)
      {
        ystep_ff=1;
        if(secY!=0)
        {
          yOri_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)f_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_ff=f_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_ff=-f_ymulti;
        if(secY!=0)
        {
          yOri_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_ff=yOri_ff + ystep_ff - yOri_ff % ystep_ff;
        }
        else
        {
          yOri_ff=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_ff=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_ff=yresosize_ff + ystep_ff -  yresosize_ff % ystep_ff;
          if(yresosize_ff>edgeLengthY)yresosize_ff=edgeLengthY;
        }
        else
        {
          yresosize_ff=edgeLengthY;
        }
      }

      if(t_xmulti>0)
      {
        xstep_tt=1;
        if(secX!=0)
        {
          xOri_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)t_xmulti*(double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
        }
        else
        {
          xresosize_tt=t_xmulti*edgeLengthX;
        }
      }
      else
      {
        xstep_tt=-t_xmulti;
        if(secX!=0)
        {
          xOri_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX));
          xOri_tt=xOri_tt + xstep_tt - xOri_tt %xstep_tt;
        }
        else
        {
          xOri_tt=0;
        }
        if(secX!=sectionsInAxisX-1)
        {
          xresosize_tt=(int)((double)edgeLengthX/(double)sectionsInAxisX*(double)(secX+1));
          xresosize_tt=xresosize_tt + xstep_tt -  xresosize_tt % xstep_tt;
          if(xresosize_tt>edgeLengthX)xresosize_tt=edgeLengthX;
        }
        else
        {
          xresosize_tt=edgeLengthX;
        }
      }
      if(t_ymulti>0)
      {
        ystep_tt=1;
        if(secY!=0)
        {
          yOri_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)t_ymulti*(double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
        }
        else
        {
          yresosize_tt=t_ymulti*edgeLengthY;
        }
      }
      else
      {
        ystep_tt=-t_ymulti;
        if(secY!=0)
        {
          yOri_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY));
          yOri_tt=yOri_tt + ystep_tt - yOri_tt % ystep_tt;
        }
        else
        {
          yOri_tt=0;
        }
        if(secY!=sectionsInAxisY-1)
        {
          yresosize_tt=(int)((double)edgeLengthY/(double)sectionsInAxisY*(double)(secY+1));
          yresosize_tt=yresosize_tt + ystep_tt -  yresosize_tt % ystep_tt;
          if(yresosize_tt>edgeLengthY)yresosize_tt=edgeLengthY;
        }
        else
        {
          yresosize_tt=edgeLengthY;
        }
      }

      if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");


      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }


        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = 1;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = t_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = 1;
          denomX = t_xmulti;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = 1;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = t_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = 1;
          denomY = t_ymulti;
        }

        for(int iterx=xOri_tt;iterx<xresosize_tt;iterx+=xstep_tt)
        {
          for(int itery=yOri_tt;itery<yresosize_tt;itery+=ystep_tt)
          {
            int neuronID=cellmap_slow(t_pre,t_suf,iterx,itery);
            int tIndex = (iterx / t_xstep) * (t_yresosize / t_ystep) + itery / t_ystep;

            for(int itxx=0;itxx<f_xresosize;itxx+=f_xstep)
            {
              for(int ityy=0;ityy<f_yresosize;ityy+=f_ystep)
              {
                int fIndex = (itxx / f_xstep)*(f_yresosize / f_ystep) + ityy / f_ystep;

                int fromIndex =  cellmap_slow(f_pre,f_suf,itxx,ityy);
                if(Math.abs(trans[fIndex % trans.length][tIndex % trans[0].length])!=0)
                {
                  double synWei = stre*trans[fIndex % trans.length][tIndex % trans[0].length];
                  if(Math.abs(synWei) > SIFNeuron.maxWeight)
                  {
                    if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                  }


                  SizeID sid = scaffold.get(fromIndex).get(String.valueOf(delay));

                  int index = Arrays.binarySearch (
                    axons.get(fromIndex).branches[(int)(sid.id)].synapses,
                    modelFactory.createSynapse (
                      neuronID,
                      ( byte ) type,
                      ( float ) synWei ) );

                  if(index>=0)
                  {
                    final MutableSynapse  synapseMut = ( MutableSynapse )
                      axons.get(fromIndex).branches[(int)(sid.id)].synapses[index];
                    
                    synapseMut.setWeight ( ( float ) synWei );
                  }
                  else
                  {
                    throw new RuntimeException("modify none existing synapse");
                  }
                }

              }
            }

          }
        }
      }
      return maxOutput;  
    }

    /*
  public double connect(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] trans, int hostId)
    ////////////////////////////////////////////////////////////////////////
  {
    double maxOutput=-1.0;
    double [][] conn = FunUtil.rRotate90(trans);
//    System.out.println("connect");
    int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
    f_xmulti=getXmultiplier(f_pre,f_suf);
    f_ymulti=getYmultiplier(f_pre,f_suf);
    t_xmulti=getXmultiplier(t_pre,t_suf);
    t_ymulti=getYmultiplier(t_pre,t_suf);
    int f_xstep,f_ystep,f_xresosize,f_yresosize;
    int t_xstep,t_ystep,t_xresosize,t_yresosize;
    int denomX=0,numX=0;
    int denomY=0,numY=0;
    int xmagratio=0;
    int ymagratio=0;
    int xborder,yborder;
    int newx=0;
    int newy=0;
    double[] curr=new double[2];
    curr[0]=0.0;curr[1]=0.0;//no initial currents in neurons
//    String fromNeur,toNeur;
    TreeSet<Synapse> synapses;
    int[] xy=new int[2];


    if(type==1&&stre>0) // these maynot be useful for new neuronal model
      System.err.println("sign of connection is wrong");
    if(type==0&&stre<0)
      System.err.println("sign of connection is wrong");



    if(stre!=0)
    {
      if(f_xmulti>0)
      {
        f_xstep=1;
        f_xresosize=f_xmulti*xedgeLength;
      }
      else
      {
        f_xstep=-f_xmulti;
        f_xresosize=xedgeLength;
      }
      if(f_ymulti>0)
                        {
                                f_ystep=1;
                                f_yresosize=f_ymulti*yedgeLength;
                        }
                        else
                        {
                                f_ystep=-f_ymulti;
                                f_yresosize=yedgeLength;
                        }
      if(t_xmulti>0)
      {
        t_xstep=1;
        t_xresosize=t_xmulti*xedgeLength;
      }
      else
      {
        t_xstep=-t_xmulti;
        t_xresosize=xedgeLength;
      }
      if(t_ymulti>0)
                        {
                                t_ystep=1;
                                t_yresosize=t_ymulti*yedgeLength;
                        }
                        else
                        {
                                t_ystep=-t_ymulti;
                                t_yresosize=yedgeLength;
                        }
      if(convergent) //convergence connections from many to 1, first iterate to side
      {
        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = 1;
//          xmagratio=f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
//          xmagratio=1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = f_xmulti;
          denomX = t_xmulti;
//          xmagratio=(f_xmulti/t_xmulti);
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = 1;
          denomX = t_xmulti;
//          xmagratio=1/t_xmulti;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = 1;
//                                       ymagratio=(int)f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
//          ymagratio=1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = f_ymulti;
          denomY = t_ymulti;
//          ymagratio=(int)(f_ymulti/t_ymulti);
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = 1;
          denomY = t_ymulti;
        }

        for(int iterx=0;iterx<t_xresosize;iterx+=t_xstep)
        {
          for(int itery=0;itery<t_yresosize;itery+=t_ystep)
          {
            int toIndex = cellmap_slow(t_pre,t_suf,iterx,itery);
            //get the closest point to the from layer in the to neuron layer.
            xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), f_pre, f_suf);
            newx=xy[0];
            newy=xy[1];            

            for(int itxx=newx-conn.length/2*f_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*f_xstep;itxx+=f_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
            {
              for(int ityy=newy-conn[0].length/2*f_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*f_ystep;ityy+=f_ystep)
              {
                xborder = itxx % f_xresosize;
                yborder = ityy % f_yresosize;

                if(xborder<0) xborder+=f_xresosize;
                if(yborder<0) yborder+=f_yresosize;

                int neuronID;
                neuronID=cellmap_slow(f_pre,f_suf,xborder,yborder);

                if( neuronID <= neuron_end && neuronID >= base )
                {
                  if(Math.abs(conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep])!=0)
                  {
                    synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses
                    double synWei = stre*conn[(itxx-newx+conn.length/2*f_xstep)/f_xstep][(ityy-newy+conn[0].length/2*f_ystep)/f_ystep];
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }
                    syn= new Synapse(toIndex,synWei,type);
                    synapses.add(syn);
                  }
                }

              }
            }

          }
        }


      }
      else //divergent
      {
        if(t_xmulti<0&&f_xmulti>0)
        {
          numX = 1;
          denomX = f_xmulti;
        }
        else if(t_xmulti<0&&f_xmulti<0)
        {
          numX = 1;
          denomX = 1;
        }
        else if(t_xmulti>0&&f_xmulti>0)
        {
          numX = t_xmulti;
          denomX = f_xmulti;
        }
        else if(t_xmulti>0&&f_xmulti<0)
        {
          numX = t_xmulti;
          denomX = 1;
        }


        if(t_ymulti<0&&f_ymulti>0)
        {
          numY = 1;
          denomY = f_ymulti;
        }
        else if(t_ymulti<0&&f_ymulti<0)
        {
          numY = 1;
          denomY = 1;
        }
        else if(t_ymulti>0&&f_ymulti>0)
        {
          numY = t_ymulti;
          denomY = f_ymulti;
        }
        else if(t_ymulti>0&&f_ymulti<0)
        {
          numY = t_ymulti;
          denomY = 1;
        }

        for(int iterx=0;iterx<f_xresosize;iterx+=f_xstep)
        {
          for(int itery=0;itery<f_yresosize;itery+=f_ystep)
          {
            int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
            if( neuronID <= neuron_end && neuronID >= base  )
            {
              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

              newx=xy[0];
              newy=xy[1];

              for(int itxx=newx-conn.length/2*t_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*t_xstep;itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center

              {
                for(int ityy=newy-conn[0].length/2*t_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*t_ystep;ityy+=t_ystep)
                {
                  xborder = itxx % t_xresosize;
                  yborder = ityy % t_yresosize;

                  if(xborder<0) xborder+=t_xresosize;
                  if(yborder<0) yborder+=t_yresosize;



                  int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                  synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses;                
                  if(Math.abs(conn[(itxx-newx+conn.length/2*t_xstep)/t_xstep][(ityy-newy+conn[0].length/2*t_ystep)/t_ystep])!=0)
                  {
                    double synWei = stre*conn[(itxx-newx+conn.length/2*t_xstep)/t_xstep][(ityy-newy+conn[0].length/2*t_ystep)/t_ystep];
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }

                    syn=new Synapse(toIndex,synWei,type);
                    synapses.add(syn);
                  }


                }
              }
            }

          }
        }

      }
    }

    return maxOutput;  
  }
     */
    @Deprecated
    public double connect(String f_pre, String f_suf, String t_pre, String t_suf, boolean convergent, double stre, int type, double delay, double[][] trans)
    ////////////////////////////////////////////////////////////////////////
    {
      double maxOutput = -1.0;
      double [][] conn;
      //    System.out.println("connect");
      int f_xmulti,f_ymulti,t_xmulti,t_ymulti;
      f_xmulti=getXmultiplier(f_pre,f_suf);
      f_ymulti=getYmultiplier(f_pre,f_suf);
      t_xmulti=getXmultiplier(t_pre,t_suf);
      t_ymulti=getYmultiplier(t_pre,t_suf);
      int f_xstep,f_ystep,f_xresosize,f_yresosize;
      int t_xstep,t_ystep,t_xresosize,t_yresosize;
      int denomX=0,numX=0;
      int denomY=0,numY=0;
      int xmagratio=0;
      int ymagratio=0;
      int xborder,yborder;
      int newx=0;
      int newy=0;
      String fromNeur,toNeur;
      Synapse syn;
      TreeSet<Synapse> synapses;
      int[] xy=new int[2];


      if(type==1&&stre>0) // these maynot be useful for new neuronal model
        System.err.println("sign of connection is wrong");
      if(type==0&&stre<0)
        System.err.println("sign of connection is wrong");



      if(stre!=0)
      {
        if(f_xmulti>0)
        {
          f_xstep=1;
          f_xresosize=f_xmulti*edgeLengthX;
        }
        else
        {
          f_xstep=-f_xmulti;
          f_xresosize=edgeLengthX;
        }
        if(f_ymulti>0)
        {
          f_ystep=1;
          f_yresosize=f_ymulti*edgeLengthY;
        }
        else
        {
          f_ystep=-f_ymulti;
          f_yresosize=edgeLengthY;
        }
        if(t_xmulti>0)
        {
          t_xstep=1;
          t_xresosize=t_xmulti*edgeLengthX;
        }
        else
        {
          t_xstep=-t_xmulti;
          t_xresosize=edgeLengthX;
        }
        if(t_ymulti>0)
        {
          t_ystep=1;
          t_yresosize=t_ymulti*edgeLengthY;
        }
        else
        {
          t_ystep=-t_ymulti;
          t_yresosize=edgeLengthY;
        }

        //      System.gc();
        if(convergent) //convergence connections from many to 1, first iterate to side
        {
          conn = FunUtil.lRotate90(trans);

          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = 1;
            denomX = f_xmulti;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = t_xmulti;
            denomX = f_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = t_xmulti;
            denomX = 1;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = 1;
            denomY = f_ymulti;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = t_ymulti;
            denomY = f_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = t_ymulti;
            denomY = 1;
          }
          for(int iterx=0;iterx<f_xresosize;iterx+=f_xstep)
          {
            for(int itery=0;itery<f_yresosize;itery+=f_ystep)
            {

              int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
              //            if( neuronID <= neuron_end && neuronID >= base  )
              {

                xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

                newx=xy[0];
                newy=xy[1];

                for(int itxx=newx-((int)((double)(conn.length/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep)))+1)*(t_xstep);itxx<=newx+((int)((double)(((conn.length%2)==0?conn.length-1:conn.length)/2)*(((double)numX/(double)denomX*(double)f_xstep/(double)t_xstep))))*(t_xstep);itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center
                {
                  for(int ityy=newy-((int)((double)(conn[0].length/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep)))+1)*(t_ystep);ityy<=newy+((int)((double)(((conn[0].length%2)==0?conn[0].length-1:conn[0].length)/2)*(((double)numY*(double)f_ystep/(double)denomY/(double)t_ystep))))*(t_ystep);ityy+=t_ystep)  
                  {
                    xborder = itxx % t_xresosize;
                    yborder = ityy % t_yresosize;

                    if(xborder<0) xborder+=t_xresosize;
                    if(yborder<0) yborder+=t_yresosize;



                    int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                    synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses;                
                    int maX = (int)((((double)itxx)/(double)numX*(double)denomX)-iterx)/f_xstep+conn.length/2;
                    int maY = (int)((((double)ityy)/(double)numY*(double)denomY)-itery)/f_ystep+conn[0].length/2;
                    if(maX>=0 && maX < conn.length && maY>=0 && maY < conn[0].length )
                    {
                      if(Math.abs(conn[maX][maY])!=0)
                      {
                        double synWei = stre*conn[maX][maY];
                        if(Math.abs(synWei) > SIFNeuron.maxWeight)
                        {
                          if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                        }

                        syn = modelFactory.createSynapse (
                          toIndex,
                          ( byte ) type,
                          ( float ) synWei );
                        
                        synapses.add(syn);
                      }
                    }


                  }
                }
              }

            }
          }
        }
        else //divergent
        {
          conn = FunUtil.rRotate90(trans);
          if(t_xmulti<0&&f_xmulti>0)
          {
            numX = 1;
            denomX = f_xmulti;
          }
          else if(t_xmulti<0&&f_xmulti<0)
          {
            numX = 1;
            denomX = 1;
          }
          else if(t_xmulti>0&&f_xmulti>0)
          {
            numX = t_xmulti;
            denomX = f_xmulti;
          }
          else if(t_xmulti>0&&f_xmulti<0)
          {
            numX = t_xmulti;
            denomX = 1;
          }


          if(t_ymulti<0&&f_ymulti>0)
          {
            numY = 1;
            denomY = f_ymulti;
          }
          else if(t_ymulti<0&&f_ymulti<0)
          {
            numY = 1;
            denomY = 1;
          }
          else if(t_ymulti>0&&f_ymulti>0)
          {
            numY = t_ymulti;
            denomY = f_ymulti;
          }
          else if(t_ymulti>0&&f_ymulti<0)
          {
            numY = t_ymulti;
            denomY = 1;
          }
          for(int iterx=0;iterx<f_xresosize;iterx+=f_xstep)
          {
            for(int itery=0;itery<f_yresosize;itery+=f_ystep)
            {
              int neuronID=cellmap_slow(f_pre,f_suf,iterx,itery);
              //            if( neuronID <= neuron_end && neuronID >= base  )
                //            {
              xy=closePoint((int)((double)numX/(double)denomX*iterx),(int)((double)numY/(double)denomY*itery), t_pre, t_suf);

              newx=xy[0];
              newy=xy[1];

              for(int itxx=newx-conn.length/2*t_xstep;itxx<newx+((conn.length%2)==0?conn.length:conn.length+1)/2*t_xstep;itxx+=t_xstep) // if matrix is even, the center is center+0.5, if matrix is odd, center is center

              {
                for(int ityy=newy-conn[0].length/2*t_ystep;ityy<newy+((conn[0].length%2)==0?conn[0].length:conn[0].length+1)/2*t_ystep;ityy+=t_ystep)
                {
                  xborder = itxx % t_xresosize;
                  yborder = ityy % t_yresosize;

                  if(xborder<0) xborder+=t_xresosize;
                  if(yborder<0) yborder+=t_yresosize;



                  int toIndex = cellmap_slow(t_pre,t_suf,xborder,yborder);


                  synapses=getSynapses(neuronID,delay,toIndex); //read the old synapses;                
                  if(Math.abs(conn[(itxx-newx+conn.length/2*t_xstep)/t_xstep][(ityy-newy+conn[0].length/2*t_ystep)/t_ystep])!=0)
                  {
                    double synWei = stre*conn[(itxx-newx+conn.length/2*t_xstep)/t_xstep][(ityy-newy+conn[0].length/2*t_ystep)/t_ystep];
                    if(Math.abs(synWei) > SIFNeuron.maxWeight)
                    {
                      if( Math.abs(synWei) / SIFNeuron.maxWeight > maxOutput) maxOutput = Math.abs(synWei) / SIFNeuron.maxWeight;
                    }

                    syn = modelFactory.createSynapse (
                      toIndex,
                      ( byte ) type,
                      ( float ) synWei );
                    
                    synapses.add(syn);
                  }
                }
              }
              //            }

            }
          }

        }
      }

      return maxOutput;    
    }

    /*  private TreeSet<Synapse> getSynapses(String from, double delay, String to)
  {

    if(!branchmaps[( cellmap.get(from)).intValue()- base].containsKey(delay+" "+node))
      branchmaps[( cellmap.get(from)).intValue()-base].put(delay+" "+node,new TreeSet<Synapse>());

    return branchmaps[(cellmap.get(from)).intValue()-base].get(delay+" "+node);    
  //return branchmaps[( cellmap.get(from)).intValue()].get(delay+" "+node);    
  }
     */

    private TreeSet<Synapse> getSynapses(int from, double delay, int to)
    ////////////////////////////////////////////////////////////////////////
    {
      int node=FunUtil.hostId(nodeEndIndices,to);    

      if(!branchmaps[from- base].containsKey(delay+" "+node))
        branchmaps[from-base].put(delay+" "+node,new TreeSet<Synapse>());
      return branchmaps[from-base].get(delay+" "+node);    
      //return branchmaps[( cellmap.get(from)).intValue()].get(delay+" "+node);    
    }
    /*

  public void populateNeurons()
    ////////////////////////////////////////////////////////////////////////
  {

    neurons= new Neuron[numberOfNeurons];
    int xTotal=0;
    int yTotal=0;
    int xstep=0;
    int ystep=0;
    int xMul,yMul;
    String neu;
    for(int iter=0;iter<layer_structure.size();iter++)
    {
      xMul = layer_structure.get(iter).getXmultiplier();
      yMul = layer_structure.get(iter).getYmultiplier();

      if(xMul < 0 )
      {
        xTotal=xedgeLength;
        xstep=-xMul;
      }
      else
      {
        xTotal=xedgeLength*xMul;
        xstep=1;
      }
      if(yMul < 0 )
      {
        yTotal=yedgeLength;
        ystep=-yMul;
      }
      else
      {
        yTotal=yedgeLength*yMul;
        ystep=1;
      }

      for(int x = 0 ; x < xTotal; x+=xstep)
      {
        for(int y = 0 ; y < yTotal; y+=ystep)
        {
          neu=layer_structure.get(iter).getPrefix()+","+x+","+y+","+layer_structure.get(iter).getSuffix();
        //  System.out.println(neu+cellmap.get(neu));
          neurons[cellmap.get(neu)] = new SIFNeuron(-0.07,new Axon(null));
        }
      }

    }

  }

  public void populateNeurons(int hostID)
    ////////////////////////////////////////////////////////////////////////
  {
    neurons= new Neuron[numberOfNeurons];
    int xTotal=0;
    int yTotal=0;
    int xstep=0;
    int ystep=0;
    int xMul,yMul;
    String neu;
    for(int iter=0;iter<layer_structure.size();iter++)
    {
      xMul = layer_structure.get(iter).getXmultiplier();
      yMul = layer_structure.get(iter).getYmultiplier();

      if(xMul < 0 )
      {
        xTotal=xedgeLength;
        xstep=-xMul;
      }
      else
      {
        xTotal=xedgeLength*xMul;
        xstep=1;
      }
      if(yMul < 0 )
      {
        yTotal=yedgeLength;
        ystep=-yMul;
      }
      else
      {
        yTotal=yedgeLength*yMul;
        ystep=1;
      }

      for(int x = 0 ; x < xTotal; x+=xstep)
      {
        for(int y = 0 ; y < yTotal; y+=ystep)
        {
          neu=layer_structure.get(iter).getPrefix()+","+x+","+y+","+layer_structure.get(iter).getSuffix();
        //  System.out.println(neu+cellmap.get(neu));
          neurons[cellmap.get(neu)] = new SIFNeuron(-0.07,new Axon(null));
        }
      }

    }

  }
     */

    /***********************************************************************
    * Given tolayer of neuron and iterx, itery coordinates,
    * return the closest point.
    * 
    * Called from popScaffold().
    * 
    * @param iterx
    * @param itery
    * @param tpre
    * @param tsuf
    * @return
    ***********************************************************************/
    public int [ ]  closePoint (
      final int     iterx,
      final int     itery,
      final String  tpre,
      final String  tsuf )
    ////////////////////////////////////////////////////////////////////////
    {
      final int
        txmulti = getXmultiplier ( tpre, tsuf ),
        tymulti = getYmultiplier ( tpre, tsuf );
      
      // fxmulti=getXmultiplier(fpre,fsuf);
      // fymulti=getYmultiplier(fpre,fsuf);
      
      String thisSpot;
      
      int txstep,tystep;
      
      int
        fxmult,
        fymult,
        txmult,
        tymult;
      
      if ( txmulti > 0 )
      {
        txmult = txmulti;
      }
      else
      {
        txmult = 1;
      }
      
      if ( tymulti > 0 )
      {
        tymult = tymulti;
      }
      else
      {
        tymult = 1;
      }
      
      /*
      if(fxmulti>0)
      {
        fxmult=fxmulti;
      }
      else
      {
        fxmult=1;
      }
      if(fymulti>0)
      {
        fymult=fymulti;
      }
      else
      {
        fymult=1;
      }
      */

      int
        newx = iterx,
        newy = itery;    

      thisSpot = tpre + "," + newx + "," + newy + "," + tsuf;
      
      if ( cellmap_slow ( tpre, tsuf, newx, newy ) == -1 )
      {
        // the neuron is not in the grid
        
        if ( txmulti > 0 )
        {
          txstep = 1;
        }
        else
        {
          txstep = -txmulti;
        }
        
        if ( tymulti > 0 )
        {
          tystep = 1;
        }
        else
        {
          tystep = -tymulti;
        }
        
        final int
          modx = iterx % txstep,
          mody = itery % tystep;
        
        // System.out.println("x"+iterx+" y"+itery+" modx"
        //   +modx+" mody"+mody+" stepx"+txstep+" stepy"+tystep);
        
        if ( txstep > 2 * modx )
        {
          newx = iterx - modx;
        }
        else
        {
          newx = iterx + ( txstep ) - modx;
        }
        
        if ( tystep > 2 * mody )
        {
          newy = itery - mody;
        }
        else
        {
          newy = itery + ( tystep ) - mody;                        
        }

        /*
        newx = newx % (txmult*xedgeLength);

        newy = newy % (tymult*yedgeLength); 

        if(newx<0) newx+=(txmult*xedgeLength);
        if(newy<0) newy+=(tymult*yedgeLength);
        */

        /*
        if(newx>txmult*xedgeLength-1)
        {
          newx-=txmult*xedgeLength;
        }
        
        if(newx<0)
        {
          newx+=txmult*xedgeLength;
        }

        if(newy>tymult*yedgeLength-1)
        {
          newy-=tymult*yedgeLength;
        }
        
        if(newy<0)
        {
          newy+=tymult*yedgeLength;
        }
        */
      }
      
      final int [ ]  answer = new int [ ] { newx, newy }; 

      //    thisSpot=tpre + ","+newx + "," + newy + ","+tsuf;
      //    if(cellmap.containsKey(thisSpot))
      //    {
      
      return answer;
      
      //    }
      //    else
      //    {
      //      answer[0]=-1;
      //      answer[1]=-1;
      //      return answer;
      //      throw new RuntimeException("the layers length is not well defined");
      //    }
    }

    /**
    * For debugging, pring out the connections connects from neuron with String id .
    * The format is Prefix,x,y,Suffix
    * 
    * @param Neuron string
    * @param  print stream p
    * @return Information.
    ***********************************************************************/
    public String connectFrom(String id, PrintStream p)
    ////////////////////////////////////////////////////////////////////////
    {

      p.println("finding connections from"+id);

      String [] items = id.split(",");
      int index = cellmap_slow(items[0],items[3],Integer.parseInt(items[1]),Integer.parseInt(items[2])) ;

      Iterator entryIter = axons.entrySet().iterator();
      while(entryIter.hasNext())
      {
        Map.Entry<Integer, Axon> entry = (Map.Entry<Integer, Axon>)entryIter.next(); 
        Integer sourceId = entry.getKey();
        Axon axon = entry.getValue();
        for(int i =0; i< axon.branches.length; i++)
        {
          for(int j=0;j<axon.branches[i].synapses.length;j++)
          {
            if ( axon.branches[i].synapses[j].getTargetNeuronIndex ( ) == index )
            {
              p.println (
                id + " has " + axon.branches[i].synapses[j].getType ( )
                + " with strength "
                + axon.branches[i].synapses[j].getWeight ( )
                + " synapse From: "
                + sourceId
                + " celltype "
                + celltype_slow(sourceId)
                +" base:"
                +base);
            }
          }
        }
      }
      p.println("End");

      /*
    int iter;
    int iter1;
    int iter2;
    String [] items = id.split(",");
    int index = cellmap_slow(items[0],items[3],Integer.parseInt(items[1]),Integer.parseInt(items[2])) ;
    if(index!=-1)
    {
      p.println("id"+index);
      String synType="";
      for(iter=0;iter<numberOfNeurons;iter++)
      {

        for(iter1=0;iter1<neurons[iter].getAxon().branches.length;iter1++)
        {
          for(iter2=0;iter2<neurons[iter].getAxon().branches[iter1].synapses.length;iter2++)
          {
            if(neurons[iter].getAxon().branches[iter1].synapses[iter2].to==index)
            {
              if(neurons[iter].getAxon().branches[iter1].synapses[iter2].type==0)
                synType="Glutamate";
              if(neurons[iter].getAxon().branches[iter1].synapses[iter2].type==1)
                synType="GABA";
              p.println(id + " has " + synType + " with strength " + neurons[iter].getAxon().branches[iter1].synapses[iter2].weight + " synapse From: " + (celltype_slow(iter)));
            }
          }
        }
      }
      p.println("End");
    }
    else
    {
      return "none";
    }

       */
      return "yes";
    }
    /*
  public String connectFrom(String id)
    ////////////////////////////////////////////////////////////////////////
  {

    int iter;
    int iter1;
    int iter2;
    int index;
    if(cellmap.containsKey(id))
    {       index=cellmap.get(id);
      String synType="";
      for(iter=0;iter<numberOfNeurons;iter++)
      {

        for(iter1=0;iter1<neurons[iter].getAxon().branches.length;iter1++)
        {
          for(iter2=0;iter2<neurons[iter].getAxon().branches[iter1].synapses.length;iter2++)
          {
            if(neurons[iter].getAxon().branches[iter1].synapses[iter2].to==index)
            {
              if(neurons[iter].getAxon().branches[iter1].synapses[iter2].type==0)
                synType="Glutamate";
              if(neurons[iter].getAxon().branches[iter1].synapses[iter2].type==1)
                synType="GABA";
              System.out.println(id + " has " + synType + " with strength " + neurons[iter].getAxon().branches[iter1].synapses[iter2].weight + " synapse From: " + (celltype[iter]));
            }
          }
        }
      }
    }
    else
    {
      return "none";
    }
  return "yes";
  }
     */
    /*
  public String connectTo(String id)
    ////////////////////////////////////////////////////////////////////////
  {

    int iter;
    int iter1;
    int iter2;
    int index;
    if(cellmap.containsKey(id))
    { 
      index=cellmap.get(id);
      String synType="";
      System.out.println( id + " connects to"); 
      for(iter1=0;iter1<neurons[index].getAxon().branches.length;iter1++)
      {
        for(iter2=0;iter2<neurons[index].getAxon().branches[iter1].synapses.length;iter2++)
        {
          if(neurons[index].getAxon().branches[iter1].synapses[iter2].type==0)
            synType="Glutamate";
          if(neurons[index].getAxon().branches[iter1].synapses[iter2].type==1)
            synType="GABA";
          System.out.println("      "+(celltype[neurons[index].getAxon().branches[iter1].synapses[iter2].to])+ "--------"+ synType + " with strength " + neurons[index].getAxon().branches[iter1].synapses[iter2].weight);
        }
      }
    }
    else
    {
      return "none";
    }
    return "yes";
  }
     */
    /*
  public int[] getLayerIndices(String prefix, String suffix)
    ////////////////////////////////////////////////////////////////////////
  {
    int xstep,ystep,xresosize,yresosize;
    if(getXmultiplier(prefix,suffix)>=1.0)
    {
      xstep=1;
      xresosize=(int) getXmultiplier(prefix,suffix)*xedgeLength;
    }
    else
    {
      xstep=(int)(1.0/getXmultiplier(prefix,suffix));
      xresosize=xedgeLength;
    }
    if(getYmultiplier(prefix,suffix)>=1.0)
    {
      ystep=1;
      yresosize=(int) getYmultiplier(prefix,suffix)*yedgeLength;
    }
    else
    {
      ystep=(int)(1.0/getYmultiplier(prefix,suffix));
      yresosize=yedgeLength;
    }
    int[] indices=new int[layerNumNeurons(prefix,suffix)];
    int curr=0;
    for(int a=0;a<xresosize;a+=xstep)
    {
      for(int b=0;b<yresosize;b+=ystep)
      {
        indices[curr]=( cellmap.get(prefix + ","+a + "," + b + ","+suffix)).intValue();
        curr++;
      }
    }
    return indices;
  }
  public int[] namesToIndices(String[] names)
    ////////////////////////////////////////////////////////////////////////
  {
    int[] indices=new int[names.length];
    for(int a=0;a<names.length;a++)
    {
      indices[a]=( cellmap.get(names[a])).intValue();
    }
    return indices;
  }

     */
    public void killMaps()
    ////////////////////////////////////////////////////////////////////////
    {
      //    cellmap=null;
      //    celltype=null;
      //    branchmaps=null;
    }

    public int getXEdgeLength()
    ////////////////////////////////////////////////////////////////////////
    {

      return edgeLengthX;

    }

    public int getYEdgeLength()
    ////////////////////////////////////////////////////////////////////////
    {
      return edgeLengthY;
    }

    /***********************************************************************
    * Set up the target host (binary long type) information through network (JPVM). 
    * @param info
    * @param pas
    * 
    * @throws jpvmException
    ***********************************************************************/
    public void parseTarget(JpvmInfo info, SimulatorParser pas) throws jpvmException
    ////////////////////////////////////////////////////////////////////////
    {
      TargetID [] targetID = new TargetID[info.numTasks];

      for(int i = 0 ; i < info.numTasks; i++)
      {
        if(i != info.idIndex)
        {
          targetID[i] = new TargetID(info.idIndex);
        }
      }

      Iterator<Integer> keyIter = pas.layerStructure.axons.keySet().iterator();
      long self = (1L<<info.idIndex);
      while(keyIter.hasNext())
      {
        int target = keyIter.next();
        int hostDD = FunUtil.hostId(pas.layerStructure.nodeEndIndices,target);
        if(hostDD != info.idIndex)
        {
          targetID[hostDD].target.add(target);
        }
        else if(hostDD == info.idIndex)
        {
          if((pas.layerStructure.neurons[target - pas.layerStructure.base].getTHost() & self )==0) // if the neuron doen'st have a target synapse at node then set it.
          {
            pas.layerStructure.neurons[target - pas.layerStructure.base].setTHost((pas.layerStructure.neurons[target - pas.layerStructure.base].getTHost() | self));
          }
        }
      }

      for(int i = 0 ; i < info.numTasks; i++)
      {
        if(i != info.idIndex)
        {
          jpvmBuffer buf2 = new jpvmBuffer();

          buf2.pack(targetID[i]); //pack the info

          info.jpvm.pvm_send(buf2, info.tids[i] ,NetMessageTag.assignTargets);
        }
      }
      //receive all the target info
      jpvmMessage m;
      for(int i = 0 ; i < info.numTasks-1; i++)
      {
        m = info.jpvm.pvm_recv(NetMessageTag.assignTargets); //receive info from others
        TargetID reTID =  (TargetID)m.buffer.upkcnsobj();                                     
        long setID = (1L<<(reTID.target.get(0)));
        int neuronID;
        for(int ii=1; ii< reTID.target.size(); ii++)
        {
          neuronID = reTID.target.get(ii);
          if((pas.layerStructure.neurons[neuronID - pas.layerStructure.base].getTHost() & setID )==0) // if the neuron doen'st have a target synapse at node then set it.
          {
            pas.layerStructure.neurons[neuronID - pas.layerStructure.base].setTHost((pas.layerStructure.neurons[neuronID - pas.layerStructure.base].getTHost() | (setID)));

          }

        }
      }
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }
