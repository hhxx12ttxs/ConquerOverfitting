/*
 * Created on 27/09/2005
 */
package com.erkobridee.PID.processamento;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * @author Erko Bridee de Almeida Cabrera
 * 
 * <br><br>
 * Classe respons?vel pelo c?digo de aplica??o de mascara 
 * em uma determinada imagem repassada
 */
public class AplicaMascara {
	
//-----------------------------------------------------------
// Atributos da classe
//-----------------------------------------------------------
	/**
	 * Mascara a ser aplicada na imagem
	 */    
    private double[][] mascara;
    /**
     * Imagem original enviada para a classe
     */
    private BufferedImage biOriginal;
    /**
     * Imagem resultado da aplica??o da mascara
     */
    private BufferedImage biResultado;
	/**
	 * flag que indica se a imagem em manipula??o atual ? a RGB ou a em escala 
	 * de cinza
	 */
	private boolean flagEscalaCinza;
	/**
	 * flag para o m?todo de aplica??o da mascara que indica se divide ou n?o
	 * assim calculando um valor resultado
	 */
	private boolean flagDivide = true;
    
//-----------------------------------------------------------
// Fim dos atributos da classe	
//-----------------------------------------------------------	

//-----------------------------------------------------------
// Construtores da classe
//-----------------------------------------------------------

	/**
	 * Construtor default da classe sem parametros
	 */
	public AplicaMascara() {
	    /*
	     * chama o m?todo de inicializa??o dos atributos
	     * da classe
	     */ 
	    this.init();
	}
	
//-----------------------------------------------------------
// Fim dos construtores da classe
//-----------------------------------------------------------	
	
//-----------------------------------------------------------
// M?todos de acesso aos atributos da classe
//-----------------------------------------------------------
    /**
     * @return BufferedImage biOriginal.
     */
    public BufferedImage getBiOriginal() {
        return biOriginal;
    }
    /**
     * @param BufferedImage biOriginal
     */
    public void setBiOriginal(BufferedImage biOriginal) {
        this.biOriginal = biOriginal;
    }
    /**
     * @return BufferedImage biResultado.
     */
    public BufferedImage getBiResultado() {
        return biResultado;
    }
    /**
     * @param BufferedImage biResultado
     */
    public void setBiResultado(BufferedImage biResultado) {
        this.biResultado = biResultado;
    }
    /**
     * @return boolean flagDivide.
     */
    public boolean isFlagDivide() {
        return flagDivide;
    }
    /**
     * @param boolean flagDivide
     */
    public void setFlagDivide(boolean flagDivide) {
        this.flagDivide = flagDivide;
    }
    /**
     * @return boolean flagEscalaCinza.
     */
    public boolean isFlagEscalaCinza() {
        return flagEscalaCinza;
    }
    /**
     * @param boolean flagEscalaCinza
     */
    public void setFlagEscalaCinza(boolean flagEscalaCinza) {
        this.flagEscalaCinza = flagEscalaCinza;
    }
	/**
	 * @return double[][] mascara.
	 */
	public double[][] getMascara() {
	    return mascara;
	}
	/**
	 * @param double[][] mascara
	 */
	public void setMascara(double[][] mascara) {
	    this.mascara = mascara;
	}
//-----------------------------------------------------------
// Fim dos m?todos de acesso ao atributos da classe
//-----------------------------------------------------------	
	
//-----------------------------------------------------------
// M?todos de servi?os da classe
//-----------------------------------------------------------	
	/**
	 * M?todo que aplica a mascara na imagem
	 * 
	 * @return BufferedImage - imagem com a mascara aplicada
	 */
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
	    long iniTime = System.currentTimeMillis();
	    
	    if( this.isFlagDivide() ) {
			for( int y = 0; y < this.getBiOriginal().getHeight(null); y++ ) {
			    for( int x = 0; x < this.getBiOriginal().getWidth(null); x++ ) {
			        //	calcula a nova cor do pixel
					Color colorSE = this.calcula( x, y );
					// seta a nova cor do pixel para a imagem
					this.getBiResultado().setRGB( x, y, colorSE.getRGB() );
			    }
			}
	    } else {
		    float[] elements = this.getFloatMasck();
		    
	    	// recupera a quantidade de linhas da mascara
	    	int linhas = this.getMascara().length;
	    	// recupera a quantidade de colunas da mascara
	    	int colunas = this.getMascara()[0].length;
		    
	        Kernel kernel = new Kernel(linhas, colunas, elements);
	        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	        cop.filter(this.getBiOriginal(),this.getBiResultado());
	    }
	    
        long endTime = System.currentTimeMillis();
		System.out.println( "tempo de processamento milis: " + ( endTime - iniTime ) );
        
		// retorna a imagem resultado
		return this.getBiResultado();
	}
	// M?TODO SEQUENCIAL
	/*
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
	    long iniTime = System.currentTimeMillis();
	    
		for( int y = 0; y < this.getBiOriginal().getHeight(null); y++ ) {
		    for( int x = 0; x < this.getBiOriginal().getWidth(null); x++ ) {
		        //	calcula a nova cor do pixel
				Color colorSE = this.calcula( x, y );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( x, y, colorSE.getRGB() );
		    }
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println( "tempo de processamento milis: " + ( endTime - iniTime ) );
		
		// retorna a imagem resultado
		return this.getBiResultado();
	}
	*/
	// M?TODO 4 PIXELS POR INTERA??O
	/*
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
	    long iniTime = System.currentTimeMillis();
	    
	    int yIni = 0;
		int yEnd = this.getBiOriginal().getHeight(null) - 1;		
		while( true ) {
		    if( yEnd < yIni ) {
		        break;
		    }
		    int xIni = 0;
			int xEnd = this.getBiOriginal().getWidth(null) - 1;			    
		    while( true ) {
		        if( xEnd < xIni ) {
			        break;
			    }
		        //	calcula a nova cor do pixel
				Color colorSE = this.calcula( xIni, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yIni, colorSE.getRGB() );
		        //	calcula a nova cor do pixel
				Color colorSD = this.calcula( xIni, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yEnd, colorSD.getRGB());
		        //	calcula a nova cor do pixel
				Color colorIE = this.calcula( xEnd, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yIni, colorIE.getRGB() );
		        //	calcula a nova cor do pixel
				Color colorID = this.calcula( xEnd, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yEnd, colorID.getRGB());
				xIni++;
		        xEnd--;
		    }
		    yIni++;
		    yEnd--;
		}
	    
		long endTime = System.currentTimeMillis();
		System.out.println( "tempo de processamento milis: " + ( endTime - iniTime ) );
		
		// retorna a imagem resultado
		return this.getBiResultado();
	}
	*/
	// M?TODO 16 PIXELS POR INTERA??O
	/*
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
	    long iniTime = System.currentTimeMillis();
	    	    
	    int yIni = 0;
		int yEnd = this.getBiOriginal().getHeight(null) - 1;
		
		int yM = (int)( this.getBiOriginal().getHeight(null) / 2 );
		int yMtoIni = yM + 1;
		int yMtoEnd = yM - 1;
		
		boolean fyIni = false;
		boolean fyEnd = false;
		
		while( true ) {
		    
		    // verificando condi??o de parada
		    if( fyIni && fyEnd ) {
		        break;
		    }
		    
		    // muda valor do flag de parada
		    if( yMtoIni < yIni ) {
		        fyIni = true;
		    }
		    
		    // muda valor do flag de parada
		    if( yMtoEnd > yEnd  ) {
		        fyEnd = true;
		    }
		    

		    int xIni = 0;
			int xEnd = this.getBiOriginal().getWidth(null) - 1;
			
			int xM = (int)( this.getBiOriginal().getWidth(null) / 2 );
			int xMtoIni = xM + 1;
			int xMtoEnd = xM - 1;
			
			boolean fxIni = false;
			boolean fxEnd = false;
			
		    while( true ) {
		        
		        // verificando condi??o de parada
		        if( fxIni && fxEnd ) {
		            break;
		        }
		        
			    // muda valor do flag de parada
			    if( xMtoIni < xIni ) {
			        fxIni = true;
			    }
			    
			    // muda valor do flag de parada
			    if( xMtoEnd > xEnd  ) {
			        fxEnd = true;
			    }
		        
			    //----------
			    // processamento dos pixels
			    //----------
			    
		        //	calcula a nova cor do pixel
				Color colorSEa = this.calcula( xIni, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yIni, colorSEa.getRGB() );	        
				
				// calcula a nova cor do pixel
				Color colorSMtoEa = this.calcula( xMtoIni, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoIni, yIni, colorSMtoEa.getRGB() );			
				
				// calcula a nova cor do pixel
				Color colorSMtoDa = this.calcula( xMtoEnd, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoEnd, yIni, colorSMtoDa.getRGB() );
								
				//	calcula a nova cor do pixel
				Color colorSDa = this.calcula( xEnd, yIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yIni, colorSDa.getRGB());		
				
				//----------
				
		        //	calcula a nova cor do pixel
				Color colorSEb = this.calcula( xIni, yMtoIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yMtoIni, colorSEb.getRGB() );	        
				
				// calcula a nova cor do pixel
				Color colorSMtoEb = this.calcula( xMtoIni, yMtoIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoIni, yMtoIni, colorSMtoEb.getRGB() );			
				
				// calcula a nova cor do pixel
				Color colorSMtoDb = this.calcula( xMtoEnd, yMtoIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoEnd, yMtoIni, colorSMtoDb.getRGB() );
								
				//	calcula a nova cor do pixel
				Color colorSDb = this.calcula( xEnd, yMtoIni );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yMtoIni, colorSDb.getRGB());
								
				//----------
				
				//	calcula a nova cor do pixel
				Color colorIEa = this.calcula( xIni, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yEnd, colorIEa.getRGB() );		        
				
				//	calcula a nova cor do pixel
				Color colorIMtoEa = this.calcula( xMtoIni, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoIni, yEnd, colorIMtoEa.getRGB() );
				
				//	calcula a nova cor do pixel
				Color colorIMtoDa = this.calcula( xMtoEnd, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoEnd, yEnd, colorIMtoDa.getRGB() );
				
				//	calcula a nova cor do pixel
				Color colorIDa = this.calcula( xEnd, yEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yEnd, colorIDa.getRGB());
				
				//----------
				
				//	calcula a nova cor do pixel
				Color colorIEb = this.calcula( xIni, yMtoEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xIni, yMtoEnd, colorIEb.getRGB() );		        
				
				//	calcula a nova cor do pixel
				Color colorIMtoEb = this.calcula( xMtoIni, yMtoEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoIni, yMtoEnd, colorIMtoEb.getRGB() );
				
				//	calcula a nova cor do pixel
				Color colorIMtoDb = this.calcula( xMtoEnd, yMtoEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xMtoEnd, yMtoEnd, colorIMtoDb.getRGB() );
				
				//	calcula a nova cor do pixel
				Color colorIDb = this.calcula( xEnd, yMtoEnd );
				// seta a nova cor do pixel para a imagem
				this.getBiResultado().setRGB( xEnd, yMtoEnd, colorIDb.getRGB());
				
				//-----------
				// atualizando indicadores de percurso
				//-----------
			    xIni++;
			    xMtoIni--;
			    
			    xEnd--;
			    xMtoEnd++;
		        
		    }
		    yIni++;
		    yMtoIni--;
		    
		    yEnd--;
		    yMtoEnd++;
		}
	    
		long endTime = System.currentTimeMillis();
		System.out.println( "tempo de processamento milis: " + ( endTime - iniTime ) );
		
		// retorna a imagem resultado
		return this.getBiResultado();
	}
	*/
	// M?TODO COM 16 THREADS, infelizmente foi mais lento =(
	/*
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
	    long iniTime = System.currentTimeMillis();
	    	   
	    int xIni = 0;
		int xEnd = this.getBiOriginal().getWidth(null) - 1;
		
		int xM = (int)( this.getBiOriginal().getWidth(null) / 2 );
		int xMtoIni = xM + 1;
		int xMtoEnd = xM - 1;
	    
	    int yIni = 0;
		int yEnd = this.getBiOriginal().getHeight(null) - 1;
		
		int yM = (int)( this.getBiOriginal().getHeight(null) / 2 );
		int yMtoIni = yM + 1;
		int yMtoEnd = yM - 1;
		
		//----- q1 -------
		int q1a_xIni, q1a_yIni, q1a_xEnd, q1a_yEnd;
		int q1b_xIni, q1b_yIni, q1b_xEnd, q1b_yEnd;
		int q1c_xIni, q1c_yIni, q1c_xEnd, q1c_yEnd;
		int q1d_xIni, q1d_yIni, q1d_xEnd, q1d_yEnd;		
		
		int q1_xM = (int)( xMtoIni / 2 );
		int q1_xMtoIni = q1_xM + 1;
		int q1_xMtoEnd = q1_xM - 1;
		
		int q1_yM = (int)( yMtoIni / 2 );
		int q1_yMtoIni = q1_yM + 1;
		int q1_yMtoEnd = q1_yM - 1;
		
		q1a_xIni = 0;
		q1a_yIni = 0;
		q1a_xEnd = q1_xMtoIni;
		q1a_yEnd = q1_yMtoIni;
		//--		
		q1b_xIni = q1_xMtoEnd;
		q1b_yIni = 0;
		q1b_xEnd = xMtoIni;
		q1b_yEnd = q1_yMtoIni;
		//--
		q1c_xIni = 0;
		q1c_yIni = q1_yMtoEnd;
		q1c_xEnd = q1_xMtoIni;
		q1c_yEnd = yMtoIni;
		//--
		q1d_xIni = q1_xMtoEnd;
		q1d_yIni = q1_yMtoEnd;
		q1d_xEnd = xMtoIni;
		q1d_yEnd = yMtoIni;		
		//----- q2 -------
		int q2a_xIni, q2a_yIni, q2a_xEnd, q2a_yEnd;
		int q2b_xIni, q2b_yIni, q2b_xEnd, q2b_yEnd;
		int q2c_xIni, q2c_yIni, q2c_xEnd, q2c_yEnd;
		int q2d_xIni, q2d_yIni, q2d_xEnd, q2d_yEnd;		
		
		int q2_xM = (int)( ( xEnd - xMtoEnd ) / 2 );
		int q2_xMtoIni = q1_xM + 1;
		int q2_xMtoEnd = q1_xM - 1;
		
		int q2_yM = (int)( yMtoIni / 2 );
		int q2_yMtoIni = q1_yM + 1;
		int q2_yMtoEnd = q1_yM - 1;
		
		q2a_xIni = xMtoEnd;
		q2a_yIni = 0;
		q2a_xEnd = q2_xMtoIni;
		q2a_yEnd = q2_yMtoIni;
		//--		
		q2b_xIni = q2_xMtoEnd;
		q2b_yIni = 0;
		q2b_xEnd = xEnd;
		q2b_yEnd = q2_yMtoIni;
		//--
		q2c_xIni = xMtoEnd;
		q2c_yIni = q2_yMtoEnd;
		q2c_xEnd = q2_xMtoIni;
		q2c_yEnd = yMtoIni;
		//--
		q2d_xIni = q2_xMtoEnd;
		q2d_yIni = q2_yMtoEnd;
		q2d_xEnd = xEnd;
		q2d_yEnd = yMtoIni;	
		//----- q3 -------
		int q3a_xIni, q3a_yIni, q3a_xEnd, q3a_yEnd;
		int q3b_xIni, q3b_yIni, q3b_xEnd, q3b_yEnd;
		int q3c_xIni, q3c_yIni, q3c_xEnd, q3c_yEnd;
		int q3d_xIni, q3d_yIni, q3d_xEnd, q3d_yEnd;		
		
		int q3_xM = (int)( xMtoIni / 2 );
		int q3_xMtoIni = q3_xM + 1;
		int q3_xMtoEnd = q3_xM - 1;
		
		int q3_yM = (int)( ( yEnd - yMtoEnd ) / 2 );
		int q3_yMtoIni = q3_yM + 1;
		int q3_yMtoEnd = q3_yM - 1;
		
		q3a_xIni = 0;
		q3a_yIni = yMtoEnd;
		q3a_xEnd = q3_xMtoIni;
		q3a_yEnd = q3_yMtoIni;
		//--		
		q3b_xIni = q1_xMtoEnd;
		q3b_yIni = yMtoEnd;
		q3b_xEnd = xMtoIni;
		q3b_yEnd = q3_yMtoIni;
		//--
		q3c_xIni = 0;
		q3c_yIni = q3_yMtoEnd;
		q3c_xEnd = q3_xMtoIni;
		q3c_yEnd = yEnd;
		//--
		q3d_xIni = q3_xMtoEnd;
		q3d_yIni = q3_yMtoEnd;
		q3d_xEnd = xMtoIni;
		q3d_yEnd = yEnd;		
		//----- q4 -------
		int q4a_xIni, q4a_yIni, q4a_xEnd, q4a_yEnd;
		int q4b_xIni, q4b_yIni, q4b_xEnd, q4b_yEnd;
		int q4c_xIni, q4c_yIni, q4c_xEnd, q4c_yEnd;
		int q4d_xIni, q4d_yIni, q4d_xEnd, q4d_yEnd;		
		
		int q4_xM = (int)( ( xEnd - xMtoEnd ) / 2 );
		int q4_xMtoIni = q4_xM + 1;
		int q4_xMtoEnd = q4_xM - 1;
		
		int q4_yM = (int)( ( yEnd - yMtoEnd ) / 2 );
		int q4_yMtoIni = q4_yM + 1;
		int q4_yMtoEnd = q4_yM - 1;
		
		q4a_xIni = xMtoEnd;
		q4a_yIni = yMtoEnd;
		q4a_xEnd = q4_xMtoIni;
		q4a_yEnd = q4_yMtoIni;
		//--		
		q4b_xIni = q4_xMtoEnd;
		q4b_yIni = yMtoEnd;
		q4b_xEnd = xEnd;
		q4b_yEnd = q4_yMtoIni;
		//--
		q4c_xIni = xMtoEnd;
		q4c_yIni = q4_yMtoEnd;
		q4c_xEnd = q4_xMtoIni;
		q4c_yEnd = yMtoIni;
		//--
		q4d_xIni = q4_xMtoEnd;
		q4d_yIni = q4_yMtoEnd;
		q4d_xEnd = xEnd;
		q4d_yEnd = yEnd;	
		//--------------------

		// inst?nciando objetos de processamento de ?reas da imagem
		Quadro quadro = new Quadro();
		
		// carregando os objetos
			// enviando objeto da imagem original
		quadro.setBiOriginal( this.getBiOriginal() );
			// enviando objeto para a imagem resultado
		quadro.setBiResultado( this.getBiResultado() );
			// enviando a mascara
		quadro.setMascara( this.getMascara() );
			// informa se deve ser realizada a divis?o
		quadro.setFlagDivide( this.isFlagDivide() );
			// informa se a imagem est? em escala de cinza
		quadro.setFlagEscalaCinza( this.isFlagEscalaCinza() );
			
		// recupera os clones
		Quadro q1a = quadro.cloneThis();
		Quadro q1b = quadro.cloneThis();
		Quadro q1c = quadro.cloneThis();
		Quadro q1d = quadro.cloneThis();
		
		Quadro q2a = quadro.cloneThis();
		Quadro q2b = quadro.cloneThis();
		Quadro q2c = quadro.cloneThis();
		Quadro q2d = quadro.cloneThis();
		
		Quadro q3a = quadro.cloneThis();
		Quadro q3b = quadro.cloneThis();
		Quadro q3c = quadro.cloneThis();
		Quadro q3d = quadro.cloneThis();
		
		Quadro q4a = quadro.cloneThis();
		Quadro q4b = quadro.cloneThis();
		Quadro q4c = quadro.cloneThis();
		Quadro q4d = quadro.cloneThis();
		
		// setando o nome do quadro de processamento
		q1a.setName( "q1a" );
		q1b.setName( "q1b" );
		q1c.setName( "q1c" );
		q1d.setName( "q1d" );
		
		q2a.setName( "q2a" );
		q2b.setName( "q2b" );
		q2c.setName( "q2c" );
		q2d.setName( "q2d" );
		
		q3a.setName( "q3a" );
		q3b.setName( "q3b" );
		q3c.setName( "q3c" );
		q3d.setName( "q3d" );
		
		q4a.setName( "q4a" );
		q4b.setName( "q4b" );
		q4c.setName( "q4c" );
		q4d.setName( "q4d" );
		
		// determina a ?rea a ser processada por cada quadro
		q1a.setArea( q1a_xIni, q1a_yIni, q1a_xEnd, q1a_yEnd );
		q1b.setArea( q1b_xIni, q1b_yIni, q1b_xEnd, q1b_yEnd );
		q1c.setArea( q1c_xIni, q1c_yIni, q1c_xEnd, q1c_yEnd );
		q1d.setArea( q1d_xIni, q1d_yIni, q1d_xEnd, q1d_yEnd );
		
		q2a.setArea( q2a_xIni, q2a_yIni, q2a_xEnd, q2a_yEnd );
		q2b.setArea( q2b_xIni, q2b_yIni, q2b_xEnd, q2b_yEnd );
		q2c.setArea( q2c_xIni, q2c_yIni, q2c_xEnd, q2c_yEnd );
		q2d.setArea( q2d_xIni, q2d_yIni, q2d_xEnd, q2d_yEnd );
		
		q3a.setArea( q3a_xIni, q3a_yIni, q3a_xEnd, q3a_yEnd );
		q3b.setArea( q3b_xIni, q3b_yIni, q3b_xEnd, q3b_yEnd );
		q3c.setArea( q3c_xIni, q3c_yIni, q3c_xEnd, q3c_yEnd );
		q3d.setArea( q3d_xIni, q3d_yIni, q3d_xEnd, q3d_yEnd );
		
		q4a.setArea( q4a_xIni, q4a_yIni, q4a_xEnd, q4a_yEnd );
		q4b.setArea( q4b_xIni, q4b_yIni, q4b_xEnd, q4b_yEnd );
		q4c.setArea( q4c_xIni, q4c_yIni, q4c_xEnd, q4c_yEnd );
		q4d.setArea( q4d_xIni, q4d_yIni, q4d_xEnd, q4d_yEnd );
		
		// associando as threads
		Thread t1a = new Thread( q1a, "q1a" );
		Thread t1b = new Thread( q1b, "q1b" );
		Thread t1c = new Thread( q1c, "q1c" );
		Thread t1d = new Thread( q1d, "q1d" );

		Thread t2a = new Thread( q2a, "q2a" );
		Thread t2b = new Thread( q2b, "q2b" );
		Thread t2c = new Thread( q2c, "q2c" );
		Thread t2d = new Thread( q2d, "q2d" );

		Thread t3a = new Thread( q3a, "q3a" );
		Thread t3b = new Thread( q3b, "q3b" );
		Thread t3c = new Thread( q3c, "q3c" );
		Thread t3d = new Thread( q3d, "q3d" );

		Thread t4a = new Thread( q4a, "q4a" );
		Thread t4b = new Thread( q4b, "q4b" );
		Thread t4c = new Thread( q4c, "q4c" );
		Thread t4d = new Thread( q4d, "q4d" );
		
		// inicializando as threads				
		t1a.start();
		t1b.start();
		t1c.start();
		t1d.start();
		
		t2a.start();
		t2b.start();
		t2c.start();
		t2d.start();
		
		t3a.start();
		t3b.start();
		t3c.start();
		t3d.start();
		
		t4a.start();
		t4b.start();
		t4c.start();
		t4d.start();
		
		long endTime = System.currentTimeMillis();
		System.out.println( "tempo de processamento milis: " + ( endTime - iniTime ) );
		
		// retorna a imagem resultado
		return this.getBiResultado();
	}
	*/
	
//-----------------------------------------------------------
// Fim dos m?todos de servi?os da classe
//-----------------------------------------------------------	
	
//-----------------------------------------------------------
// M?todos auxiliares da classe
//-----------------------------------------------------------
	/**
	 * M?todo de inicializa??o dos atributos da classe
	 * configura??es default para um novo objeto 
	 * da classe que ? instanciado
	 */
	private void init() {
		this.setFlagEscalaCinza( false );
		this.setFlagDivide( true );
		this.setBiOriginal( null );
		this.setBiResultado( null );
		// mascara default, Passa Baixa - m?dia
		double[][] mascaraIni = {{1,1,1},{1,1,1},{1,1,1}};
		this.setMascara( mascaraIni );
	}
	/**
	 * M?todo que calcula a aplica??o da mascara para uma regi?o x,y da imagem informada
	 * 
	 * @param int x - pixel posi??o linha
	 * @param int y - pixel posi??o coluna
	 */
	private Color calcula( int x, int y ) {
    	
		// objeto referente a imagem que ser? manipulada
		BufferedImage imagem = this.getBiOriginal();
    	
   		// o tamanho da ?rea considerada depender? das dimens?es da mascara
    	// recupera a quantidade de linhas da mascara
    	int linhas = this.getMascara().length;
    	// recupera a quantidade de colunas da mascara
    	int colunas = this.getMascara()[0].length;
		
		/*
		 *  ---->
		 * +-----+-----+-----+ |
		 * |  *  |     |     | |
		 * +-----+-----+-----+ v
		 * |     | x,y |     |
		 * +-----+-----+-----+
		 * |     |     |     |
		 * +-----+-----+-----+
		 * 
		 *  * - referente a posi??o onde come?a a analise da regi?o aonde ser? aplicado o filtro no caso: px, py
		 */		
    	int px = x - (linhas - 2);
   		int py = y - (colunas - 2);
        
		int pixel = 0; // para uso no calculo da m?dia da imagem em escala de cinza
		
		int pixelR = 0; // calculo da m?dia do canal R
		int pixelG = 0; // calculo da m?dia do canal G
		int pixelB = 0; // calculo da m?dia do canal B		 
		
		int divisor = 0; // total pelo qual dever? ser dividido
		
		for(int i = 0; i < linhas; i++) { 
			for(int j = 0; j < colunas; j++) {
				try {
    				
					Color color = new Color(imagem.getRGB( px, py ));
					// calculo para imagem em escala de cinza
					if( this.isFlagEscalaCinza() ) {						

					    pixel += (int)( color.getRed() * this.getMascara()[i][j] );
					    
					// calculo para imagem em RGB	
					} else {

					    pixelR += (int)( color.getRed() * this.getMascara()[i][j] );
					    pixelG += (int)( color.getGreen() * this.getMascara()[i][j] );
					    pixelB += (int)( color.getBlue() * this.getMascara()[i][j] );

					}
					// incrementando divisor
					divisor++;
				// caso seja um pixel de borda trata essa situa??o	
				}catch(Exception e ) {
					//System.out.println("Aplicando mascara: Borda Detectada.");
				}
				py++;
			}		
			px++;
			py = y - 1;;
		}
				
		Color color = null;
		if( this.isFlagEscalaCinza() ) {
			
			// verifica se deve ser dividido ou n?o
			if( this.isFlagDivide() ) {
				pixel = Math.abs(pixel/divisor);
			}
			
			// verifica se o valor est? acima do m?ximo aceito
			if( pixel > 255 ) { pixel = 255; }	
			// verifica se o valor est? abaixo do aceito
			if( pixel < 0 ) { pixel = 0; }	
			
			// carrega um objeto cor com a nova cor
			color = new Color( pixel, pixel, pixel );
		} else {
			// verifica se deve ser dividido ou n?o
			if( this.isFlagDivide() ) {	
				pixelR = Math.abs( pixelR/divisor );
				pixelG = Math.abs( pixelG/divisor );
				pixelB = Math.abs( pixelB/divisor );
			}
			
			// verifica se o valor est? acima do m?ximo aceito
			if( pixelR > 255 ) { pixelR = 255; }
			if( pixelG > 255 ) { pixelG = 255; }
			if( pixelB > 255 ) { pixelB = 255; }
			// verifica se o valor est? abaixo do aceito
			if( pixelR < 0 ) { pixelR = 0; }
			if( pixelG < 0 ) { pixelG = 0; }
			if( pixelB < 0 ) { pixelB = 0; }
			
			// carrega um objeto cor com a nova cor
			color = new Color( pixelR, pixelG, pixelB );
		}
		// retorna a nova cor do pixel
		return color;
	}
	
	/**
	 * M?todo que converte a mascara de double para um vetor float
	 * @param mascara
	 * @return
	 */
	private float[] getFloatMasck() {
	    
    	// recupera a quantidade de linhas da mascara
    	int linhas = this.getMascara().length;
    	// recupera a quantidade de colunas da mascara
    	int colunas = this.getMascara()[0].length;
	    // montando mascara em formato float
	    float[] out = new float[ linhas * colunas ];
	    
	    int index = 0;
	    for( int j = 0; j < linhas; j++ ) {
	        for( int i = 0; i < colunas; i++ ) {
	            // convertendo para float
	            out[index++] = Float.parseFloat( Double.toString( this.getMascara()[i][j] ) );
	        }
	    }
	    
	    // retornando o vetor do filtro
	    return out;
	}
//-----------------------------------------------------------
// 	Fim dos m?todos auxiliares de classe
//-----------------------------------------------------------	
	
}

