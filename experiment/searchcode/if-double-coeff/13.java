package testimage;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public abstract class TraitImage {

	    //Bufferiser une image

	    static BufferedImage toBufferedImage(Image image) {
	        
	        /* On test si l'image n'est pas déja une instance de BufferedImage */
	        if( image instanceof BufferedImage ){
	        		/* cool, rien ŕ faire */
	                return( (BufferedImage)image );
	            } 

	        else {
	                /* On s'assure que l'image est complčtement chargée */
	                image = new ImageIcon(image).getImage();

	                /* On crée la nouvelle image */
	                BufferedImage bufferedImage = new BufferedImage( image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_BYTE_GRAY );
	                Graphics g = bufferedImage.createGraphics();
	                g.drawImage(image,0,0,null);
	                g.dispose();

	                return( bufferedImage );
	        }
	      }


        /**
         * Appliquer filtrage de l'image avec fenetrage
         * @param imOri Image a filtrer
         * @param coeff Coefficients de la fenetre
         * @return Image filtrée
         */

        public static BufferedImage applicMasque(BufferedImage imOri, float[] coeff, int w, int h){

            //Masque
            Kernel kern=new Kernel(w,h,coeff);

            //type : convolution
            ConvolveOp op=new ConvolveOp(kern,ConvolveOp.EDGE_NO_OP,null);

            //Traitement de l'image
            BufferedImage nouvelleImage = op.filter(imOri, null);

          	return nouvelleImage;
        }


        public static double[][] getPixelTab(BufferedImage im){

            //Raster pour modifier l'image;
            WritableRaster raster = im.getRaster() ;
            int w=raster.getWidth();
            int h=raster.getHeight();
            double[][] donnee = new double[w][h];

            for(int i=0;i<w;i++)
              for(int j=0;j<h;j++)
                donnee[i][j]=raster.getSample(i,j,0);

            return donnee;
        }


        public static BufferedImage setPixelTab(double[][] donnee){

            //Buffer de la nouvelle image
            BufferedImage modifIm = new BufferedImage(donnee.length,donnee[donnee.length-1].length,BufferedImage.TYPE_BYTE_GRAY);

            //Raster pour modifier l'image;
            WritableRaster raster = modifIm.getRaster() ;
            int w = raster.getWidth();
            int h = raster.getHeight();

            for(int i=0;i<w;i++)
              for(int j=0;j<h;j++)
                raster.setSample(i,j,0,donnee[i][j]);

            return modifIm;
        }


		public static double calculMoyenne(BufferedImage aCentrer) {

			double[][] centrage = getPixelTab(aCentrer);
			double somme = 0;

			for (int x=0;x<centrage[0].length;x++)
				for (int y=0;y<centrage.length;y++)
					somme+=centrage[y][x];
					
			return somme/((double)centrage[0].length*(double)centrage.length);
		}
	
		public static double calculMoyenne(double[][] centrage) {
			double somme = 0;

			for (int x=0;x<centrage[0].length;x++)
				for (int y=0;y<centrage.length;y++)
					somme+=centrage[y][x];
					
			return somme/((double)centrage[0].length*(double)centrage.length);
		}

		public static double[][] centrageImage(BufferedImage aCentrer,double moyenne){
			double[][] centrage = getPixelTab(aCentrer);

			for (int x=0;x<centrage[0].length;x++)
				for (int y=0;y<centrage.length;y++)
					centrage[y][x]-=(double)moyenne;
					
			return centrage;
		}

		public static double[][] centrageImage(double[][] centrage,double moyenne){
			for (int x=0;x<centrage[0].length;x++)
				for (int y=0;y<centrage.length;y++)
					centrage[y][x]-=(double)moyenne;
							
			return centrage;
		}


		public static void decentrageImage(int[][] centrage,double moyenne){
			for (int x=0;x<centrage[0].length;x++)
				for (int y=0;y<centrage.length;y++)
					centrage[y][x]+=(int)moyenne;
					
		}

        public static double quantification(double valeur, double pas){
			int tempo=0;
			tempo=Math.round((float)(valeur/pas));
			return (double)(tempo*pas);
        }


		public static void predictionAR2d(double[][] x,double[][] err, double[][] coeffs, double pas){

			double error=0;
			double predit=0;
			//err = new double[x.length][x[0].length];

			//System.out.println("Calcul prediction boucle 1");
			for (int i=0;i<x.length;i++)
			 for (int j=0;j<coeffs[0].length;j++)
			  {err[i][j]=quantification(x[i][j],pas); x[i][j]=err[i][j];}
			  //err[i][j]=x[i][j];

			//System.out.println("Calcul prediction boucle 2");
			for (int i=0;i<coeffs.length;i++)
			 for (int j=0;j<x[0].length;j++)
			  {err[i][j]=quantification(x[i][j],pas);x[i][j]=err[i][j];}
			  //err[i][j]=x[i][j];

			//System.out.println("Calcul prediction boucle 3");
			for (int i=coeffs.length-1;i<x.length;i++)
			 for (int j=coeffs[0].length-1;j<x[0].length;j++)
			 {
				predit=0;

				for (int k=0;k<coeffs.length;k++)
				 	for (int l=0;l<coeffs[0].length;l++)
					  {predit += (coeffs[k][l]*x[i-coeffs.length+1+k][j-coeffs[0].length+1+l]);}

				predit = Math.round(predit);

				error=x[i][j]-predit;
				err[i][j]=quantification(error, pas);
				x[i][j]=predit+err[i][j];
				//if(x[i][j]<0) x[i][j] = 0; 
				//if(x[i][j]>255) x[i][j] = 255; 

			 }
		}


	public static void predictionAR2d_inv(double[][] err,double[][] x_rec, double[][] coeffs, double pas,double moyenne)
	{
	double error=0;
	double predit=0;
	for (int i=0;i<err.length;i++)
	 for (int j=0;j<coeffs[0].length;j++)
	  x_rec[i][j]=err[i][j];

	for (int i=0;i<coeffs.length;i++)
	 for (int j=0;j<err[0].length;j++)
	  x_rec[i][j]=err[i][j];

	for (int i=coeffs.length-1;i<err.length;i++)
	 for (int j=coeffs[0].length-1;j<err[0].length;j++)
	 {

		predit=0;
		for (int k=0;k<coeffs.length;k++)
		 for (int l=0;l<coeffs[0].length;l++)
		  {
		  predit += (coeffs[k][l]*x_rec[i-coeffs.length+1+k][j-coeffs[0].length+1+l]);
		  }
		predit = Math.round(predit);
		x_rec[i][j]=err[i][j]+predit;
		
	 }

	for (int i=0;i<err.length;i++)
	 for (int j=0;j<err[0].length;j++)
	  x_rec[i][j]+=moyenne;

	}



        public static int[][] moyenneur(int[][] donnee,float[] fenetre){
          int[][] modif=new int[donnee.length][donnee[0].length];



          return modif;
        }


        /**
         * Calcul l'histogramme d'une image
         * @param im Image bufferidée
         * @return Tableau contenant les valeurs de l'histogramme
         */
         public static int[] calculHisto(BufferedImage im){
            int[] histo=new int[256];
            double[][] donnee= getPixelTab(im);

            for(int i=0;i<donnee.length;i++)
              for(int j=0;j<donnee[i].length;j++)
              {
                histo[(int)donnee[i][j]]++;
              }

            return histo;
          }

          /**
           * Calcul de l'entropie d'une image ŕ partir de son histogramme
           * @param histogramme Tableau contenant les valeurs de l'histogramme
           * @return Valeur de l'entropie
           */
          public static double calculEntropie(int[] histogramme, int taille){
            double entropie =0 ;

            for(int j=0;j<histogramme.length;j++)
		{
		    if(histogramme[j]>0)
			entropie-=((double)histogramme[j]/(double)taille) * Math.log((double)histogramme[j]/(double)taille) / Math.log(2);
		}
	    
            return entropie ;
	  }
         

    /*    
   public static double[][] Autocorelation(double[][] donnee,int w,int h){
       double[][] auto=new double[w][h];
       
       for(int k=0;k<w;k++)
	   for(int l=0;l<h;l++)
	       {
		   int count=0;
		   double temp=0;
		   
		   for(int i=0;i<donnee.length;i++)
		       for(int j=0;j<donnee[0].length;j++)
			   {
			       
			       try{
				   temp+=donnee[i][j]*donnee[i-k][j-l];
				   count++;
			       }
			       catch(ArrayIndexOutOfBoundsException e){
				   continue;
			       }
			   }
		   
		   auto[k][l]=temp/count;
		   auto[l][k]=temp/count; // MODIF TITUS
	       }



       return auto; 
   }  
*/ 
    //VERSION TITUS 
   public static double[][] Autocorelation(double[][] donnee,int w,int h){
       double[][] auto=new double[2*w-1][2*h-1];
       
       for(int k=0;k<2*w-1;k++)
	   for(int l=0;l<2*h-1;l++)
	       {
		   int count=0;
		   double temp=0;
		   
		   for(int i=0;i<donnee.length;i++)
		       for(int j=0;j<donnee[0].length;j++)
			   {
			       
			       try{
				   temp+=donnee[i][j]*donnee[i-k+w-1][j-l+h-1];
				   count++;
			       }
			       catch(ArrayIndexOutOfBoundsException e){
				   continue;
			       }
			   }
		   
		   auto[/*w-1-*/k][/*h-1-*/l]=temp/count;
		   //auto[/*w-1-*/l][/*h-1-*/k]=temp/count; // MODIF TITUS 
	       }



       return auto;
   }


    // VERSION TITUS 
public static double[][] getR(double[][] r){
    int w, h, W, H; 
    w = (r.length+1)/2;  h = (r.length+1)/2; 
    W = (r.length+1)*(r[0].length+1); W = W/4; W = W-1; 
    H = (r.length+1)*(r[0].length+1); H = H/4; H = H-1;

    double[][] R=new double[W][H];
    
    System.out.println("w = "+w+" h = "+h+" W = "+W+" H = "+H); 
    int cont = 0; int contp = 0; 
    for(int i=0;i<w;i++)
	for(int j=0;j<h;j++)
	    {
		if(! ( (i==w-1) && (j==h-1) ))
		    {
			contp = 0; 
			for(int k=0;k<w;k++)
			    for(int l=0;l<h;l++)
				{
				    if(! ( (k==w-1) && (l==h-1) ))
					{
					    R[cont][contp] = r[i-k+w-1][j-l+h-1];
					    contp++; 		
					}	
				}
			cont++; 
		    }
	    }
    System.out.println("cont = "+cont+" contp = "+contp);

    return R;
}


    // VERSION TITUS 
public static double[] getV(double[][] r){
    int w, h, W, H; 
    w = (r.length+1)/2;  h = (r.length+1)/2; 
    W = (r.length+1)*(r[0].length+1); W = W/4; W = W-1; 
    H = (r.length+1)*(r[0].length+1); H = H/4; H = H-1;

    double[] V=new double[H];
    int cont = 0; 

    for(int i=0;i<w;i++)
	for(int j=0;j<h;j++)
	    {
	    	if (!( (i==w-1) && (j==h-1) ))
		    {
			V[cont]=r[i][j];
			cont++; 
		    }
	    }
    System.out.println("getV : cont = "+cont); 
    return V;
}

private static void getCoeff2(double[] V,double[][] coeff){

      for(int i=0;i<coeff.length;i++)
	for(int j=0;j<coeff[0].length;j++)

	    {
	    	if (!( (i==coeff.length-1) && (j==coeff[0].length-1) ))
		    {
			//			coeff[i][j]*=V[(V.length-1)-(j+i*coeff[0].length)];
			coeff[i][j]*=V[(j+i*coeff[0].length)]; // MODIF TITUS 

		    }
	    }
      
      /*
      double somme=0; 
      for(int i=0;i<coeff.length;i++) 
       for(int j=0;j<coeff[0].length;j++) 
        somme+=Math.abs(coeff[i][j]); 

      if (somme==0.0) 
        somme=1.0; 

       for(int i=0;i<coeff.length;i++)
       	for(int j=0;j<coeff[0].length;j++)
          coeff[i][j]/=somme;
      */ // MODIF TITUS 
}

public static void getCoeff(double[][] donnee, double[][] coeff){

	double[][] auto=TraitImage.Autocorelation(donnee,coeff.length,coeff[0].length);
	
	System.out.println("TraitImage.getR r:"+auto.length+"*"+auto[0].length);

	for (int i = 0; i < auto.length; i++)
	{
		System.out.println("");
		for (int j = 0; j < auto[0].length; j++)
			 System.out.print(auto[i][j] + " ");
	}

	System.out.println("");
	
	double[][] R=getR(auto);
	
	System.out.println("getV  R:"+R.length+"*"+R[0].length);

	for (int i = 0; i < R.length; i++)
	{
		System.out.println("");
		for (int j = 0; j < R[0].length; j++)
			 System.out.print(R[i][j] + " ");
	}

	System.out.println("");
	System.out.println("----------------------");
	
	double[] V=getV(auto);

	for (int i = 0; i < V.length; i++)
			 System.out.println("V[" + i + "] = " + V[i]);

	System.out.println("syst  V:"+V.length);

	SystemeLineaire sys=new SystemeLineaire(R,V);

	//System.out.println("sol :");

	double[] solu=sys.solution();

	if (solu == null)
			{
			 System.out.println("solution impossible mise ŕ 1");
			 solu = new double[V.length];
			 for (int i=0;i<V.length;i++)
			 	solu[i]=1.0;
			}
			 /*else
			 for (int i = 0; i < solu.length; i++)
			 System.out.println("solu[" + i + "] = " + sys.fmt.format(solu[i]));*/

	/*System.out.println("coeff  "+solu.length+"  "+coeff.length+"*"+coeff[0].length);

	for(int i=0;i<coeff.length;i++)
	{
	  System.out.print("\n");
	  for(int j=0;j<coeff[0].length;j++)
	  	System.out.print(coeff[i][j]+" ");
	}
	*/
	getCoeff2(solu,coeff); // MODIF TITUS 
	/*
	for(int i=0;i<coeff.length;i++)
	{
	  System.out.print("\n");
	  for(int j=0;j<coeff[0].length;j++)
	  	System.out.print(coeff[i][j]+" ");
	}*/
}

}

