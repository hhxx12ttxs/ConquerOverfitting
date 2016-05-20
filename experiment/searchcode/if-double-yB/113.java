/*
 * Created on 16/11/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.erkobridee.PID.processamento;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * @author Erko Bridee de Almeida Cabrera
 *
 * 16/11/2005 <br>
 * <b>Descri??o:</b><br>
 * Classe que implementa os calculos do filtro de Prewitt
 */
public class Prewitt {
	
//  -----------------------------------------------------------
//   Atributos da classe
//  -----------------------------------------------------------
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
//  -----------------------------------------------------------
//   Fim dos atributos da classe	
//  -----------------------------------------------------------	

//  -----------------------------------------------------------
//   Construtores da classe
//  -----------------------------------------------------------
  	/**
  	 * Construtor sem parametros da classe
  	 */
    public Prewitt() { this.init(); }
//  -----------------------------------------------------------
//   Fim dos construtores da classe
//  -----------------------------------------------------------	
  	
//  -----------------------------------------------------------
//   M?todos de acesso aos atributos da classe
//  -----------------------------------------------------------
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
//  -----------------------------------------------------------
//   Fim dos m?todos de acesso ao atributos da classe
//  -----------------------------------------------------------	
  	
//  -----------------------------------------------------------
//   M?todos de servi?os da classe
//  -----------------------------------------------------------	
  	/**
  	 * M?todo que calcula a mediana para a imagem
  	 * 
  	 * @return BufferedImage - imagem
  	 */
	public BufferedImage doWork() {
	    // criando um novo buffer para saida do resultado
	    this.setBiResultado( new BufferedImage(this.getBiOriginal().getWidth(), this.getBiOriginal().getHeight(), this.getBiOriginal().getType()) );
	    
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
	    
		// retorna a imagem resultado
		return this.getBiResultado();
	}
//  -----------------------------------------------------------
//   Fim dos m?todos de servi?os da classe
//  -----------------------------------------------------------	
  	
//  -----------------------------------------------------------
//   M?todos auxiliares da classe
//  -----------------------------------------------------------
  	/**
  	 * M?todo de inicializa??o dos atributos da classe em uma
  	 * nova int?ncia??o de um objeto
  	 */
    private void init() {
  	    this.setFlagEscalaCinza( false );
  	    this.setBiOriginal( null );
  	    this.setBiResultado( null );
  	}
    
    /**
     * M?todo que realiza o calculo da nova cor do pixel
     * 
     * @param int x
     * @param int y
     * @return Color - nova cor do pixel calculado
     */
    /*
	private Color calcula( int x, int y ) {
	    
	    // sa?da do calculo
	    Color resultado = null;
	    
	    // matriz de pixel
	    Color[] pixel = new Color[9];
	    
	    // recuperando os pixels
	    pixel[0] = new Color(this.getBiOriginal().getRGB( x-1, y-1 ));
	    pixel[1] = new Color(this.getBiOriginal().getRGB( x-1, y ));
	    pixel[2] = new Color(this.getBiOriginal().getRGB( x-1, y+1 ));
	    pixel[3] = new Color(this.getBiOriginal().getRGB( x, y-1 ));
	    pixel[4] = new Color(this.getBiOriginal().getRGB( x, y ));
	    pixel[5] = new Color(this.getBiOriginal().getRGB( x, y+1 ));
	    pixel[6] = new Color(this.getBiOriginal().getRGB( x+1, y-1 ));
	    pixel[7] = new Color(this.getBiOriginal().getRGB( x+1, y ));
	    pixel[8] = new Color(this.getBiOriginal().getRGB( x+1, y+1 ));         
	    
        // verifica se a imagem est? em escala de cinza
        if( this.isFlagEscalaCinza() ) {
            
            int xCinza,yCinza, resultadoCinza;
            
            int[] cinza = new int[9];
            for( int i = 0; i < 9; i++ ) {
                cinza[i] = pixel[i].getRed();
            }
            
            xCinza = ( cinza[6] + cinza[7] + cinza[8] ) - ( cinza[0] + cinza[1] + cinza[2] );
            yCinza = ( cinza[2] + cinza[5] + cinza[8] ) - ( cinza[0] + cinza[3] + cinza[6] );
            
            if( xCinza < 0 ) { xCinza = Math.abs( xCinza ); } 
            if( yCinza < 0 ) { yCinza = Math.abs( yCinza ); }
            
            resultadoCinza = (int)( xCinza + yCinza );
            
            // setando o objeto de sa?da
            resultado = new Color( resultadoCinza, resultadoCinza, resultadoCinza );
            
        // imagem RGB    
        } else {
            
            // calculando canal R
            
            int xR,yR, resultadoR;
            
            int[] r = new int[9];
            for( int i = 0; i < 9; i++ ) {
                r[i] = pixel[i].getRed();
            }
            
            xR = ( r[6] + r[7] + r[8] ) - ( r[0] + r[1] + r[2] );
            yR = ( r[2] + r[5] + r[8] ) - ( r[0] + r[3] + r[6] );
            
            if( xR < 0 ) { xR *= -1; }
            if( yR < 0 ) { yR *= -1; }
            
            resultadoR = (int)( xR + yR );
            
            // calculando canal G
            
            int xG,yG, resultadoG;
            
            int[] g = new int[9];
            for( int i = 0; i < 9; i++ ) {
                g[i] = pixel[i].getGreen();
            }
            
            xG = ( g[6] + g[7] + g[8] ) - ( g[0] + g[1] + g[2] );
            yG = ( g[2] + g[5] + g[8] ) - ( g[0] + g[3] + g[6] );
            
            if( xG < 0 ) { xG *= -1; }
            if( yG < 0 ) { yG *= -1; }
            
            resultadoG = (int)( xG + yG );
            
            // calculando canal B
            
            int xB,yB, resultadoB;
            
            int[] b = new int[9];
            for( int i = 0; i < 9; i++ ) {
                b[i] = pixel[i].getBlue();
            }
            
            xB = ( b[6] + b[7] + b[8] ) - ( b[0] + b[1] + b[2] );
            yB = ( b[2] + b[5] + b[8] ) - ( b[0] + b[3] + b[6] );
            
            if( xB < 0 ) { xB *= -1; }
            if( yB < 0 ) { yB *= -1; }
            
            resultadoB = (int)( xB + yB );
            
            
            // setando o objeto de sa?da
            resultado = new Color( resultadoR, resultadoG, resultadoB );             
            
        }
	    
        // resultado do processamento
	    return resultado;
	}*/
	
    /**
     * M?todo que realiza o calculo da nova cor do pixel
     * 
     * @param int x
     * @param int y
     * @return Color - nova cor do pixel calculado
     */
	private Color calcula( int x, int y ) {

	    return this.calculoFuncao( x, y );
	    
	}
	
	/**
	 * M?todo que calcula da fun??o de Sobel
	 * 
	 * F: |Gx| + |Gy|
	 * 
	 * @param int x
	 * @param int y
	 * @return Color
	 */
	private Color calculoFuncao( int x, int y ) {
	    Color color = null;
	    
	    Color Gx = this.Gx( x, y );
	    Color Gy = this.Gy( x, y );
	    
	    // caso a imagem esteja em escala de cinza
	    if( this.isFlagEscalaCinza() ) {	
	        
	        int cinzaX = Gx.getRed();
	        int cinzaY = Gy.getRed();
	        // resultado
	        int cinzaR = cinzaX + cinzaY;
	        
	        if( cinzaR > 255 ) {
	            cinzaR = 255;
	        } else if( cinzaR < 0 ) {
	            cinzaR = 0;
	        }
	        
	        color = new Color( cinzaR, cinzaR, cinzaR );
	        
	    // caso a imagem esteja em RGB    
	    } else {
	        
	        int rX, gX, bX, rY, gY, bY, rR, gR, bR;
	        
	        rX = Gx.getRed(); 
	        gX = Gx.getGreen();
	        bX = Gx.getBlue();  
	        rY = Gy.getRed();
	    	gY = Gy.getGreen();
	    	bY = Gy.getBlue();
	    	rR = rX + rY;
	    	gR = gX + gY;
	    	bR = bX + bY;
	    	
	    	if( rR > 255 ) {
	    	    rR = 255;
	    	} else if( rR < 0 ) {
	    	    rR = 0;
	    	}
	    	
	    	if( gR > 255 ) {
	    	    gR = 255;
	    	} else if( gR < 0 ) {
	    	    gR = 0;
	    	}
	    	
	    	if( bR > 255 ) {
	    	    bR = 255;
	    	} else if( bR < 0 ) {
	    	    bR = 0;
	    	}
	    	color = new Color( rR, gR, bR );
	    	
	    }
	    
	    return color;
	}
	
	/**
	 * M?todo que calcula o componente do eixo x do calculo
	 * 
	 * @param int x
	 * @param int y
	 * @return Color
	 */
	private Color Gx( int x, int y ) {
	    Color color = null;
	    
	    double[][] mascara = {{-1,-1,-1},{0,0,0},{1,1,1}};
	    
	    color = this.calculaMascara( x, y, mascara ); 
	    
	    return color;
	}
	
	/**
	 * M?todo que calcula o componente do eixo y do calculo
	 * 
	 * @param int x
	 * @param int y
	 * @return Color
	 */
	private Color Gy( int x, int y ) {
	    Color color = null;
	    
	    double[][] mascara = {{-1,0,1},{-1,0,1},{-1,0,1}};
	    
	    color = this.calculaMascara( x, y, mascara ); 
	    
	    return color;
	}
	
	/**
	 * M?todo que calcula uma determinda mascara em uma regi?o especifica da
	 * da imagem
	 *  
	 * @param int x
	 * @param int y
	 * @param double[][] mascara
	 * @return Color
	 */
	private Color calculaMascara( int x, int y, double[][] mascara ) {
    	
		// objeto referente a imagem que ser? manipulada
		BufferedImage imagem = this.getBiOriginal();
    	
   		// o tamanho da ?rea considerada depender? das dimens?es da mascara
    	// recupera a quantidade de linhas da mascara
    	int linhas = mascara.length;
    	// recupera a quantidade de colunas da mascara
    	int colunas = mascara[0].length;
		
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
		
		for(int i = 0; i < linhas; i++) { 
			for(int j = 0; j < colunas; j++) {
				try {
    				
					Color color = new Color(imagem.getRGB( px, py ));
					// calculo para imagem em escala de cinza
					if( this.isFlagEscalaCinza() ) {						

					    pixel += (int)( color.getRed() * mascara[i][j] );
					    
					// calculo para imagem em RGB	
					} else {

					    pixelR += (int)( color.getRed() * mascara[i][j] );
					    pixelG += (int)( color.getGreen() * mascara[i][j] );
					    pixelB += (int)( color.getBlue() * mascara[i][j] );

					}
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
			// verifica se o valor est? acima do m?ximo aceito
			if( pixel > 255 ) { pixel = 255; }	
			// verifica se o valor est? abaixo do aceito
			if( pixel < 0 ) { pixel = 0; }	
			
			// carrega um objeto cor com a nova cor
			color = new Color( pixel, pixel, pixel );
		} else {
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
//  -----------------------------------------------------------
//   Fim dos m?todos auxiliares de classe
//  -----------------------------------------------------------	 
}

