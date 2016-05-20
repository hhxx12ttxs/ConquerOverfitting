/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesImpressoes;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeInter25;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Progamador
 */
public class SODEXO_EMITIR_CSV_DWS {
    
    
  public void EmitirCSV_DWS () throws DocumentException, IOException {

      
        File dirPath = new File("C:\\Sigma\\Clientes\\INI");
        File fListPath[] = dirPath.listFiles();
        File sPath = null; 
        int linenumberPath = 0;
        String arqIni = null;
        ArrayList arqPathList = new ArrayList();
        
         int IndicePath = 1; 
       String ArquivoPath = null;
       String caminhoArq1 = null;
       String caminhoArq2 = null;
       String caminhoArq3 = null;
       String caminhoArq4 = null;
       String caminhoArq5 = null;
       String caminhoArq6 = null;
       String caminhoArq7 = null;
       String caminhoArq8 = null;
       String caminhoArq9 = null;
       String caminhoArq10 = null;
       String caminhoArq11 = null;
       String caminhoArq12 = null;
       
       
    
       
        
        for (int i = 0; i < fListPath.length; i++) {
               arqIni = fListPath[i].getName();
               arqPathList.add(arqIni);
           File f = fListPath[i];  
           sPath = f;
        }
        
        
        String clienteCadastrado = null;      
        ArrayList relacaoCliente = new ArrayList();
        
        
        File s = null;
     
       
        
        
           
       String fileNamePath = "C:\\SIGMA\\Clientes\\INI\\"+arqIni;
       BufferedReader brPath = new BufferedReader(new  FileReader(fileNamePath));
       
       String caminhoArq13 = null;
       String caminhoArq14 = null;
    
       
      while ((ArquivoPath = brPath.readLine())!= null) { 
           if (IndicePath == 1 ){
           //IndicePath = 2;
            }
           else
           if (IndicePath < 16){
               if (IndicePath == 2){
                caminhoArq1 = ArquivoPath.substring(15,29);   
               }
               else if (IndicePath == 3){ 
                caminhoArq2 = ArquivoPath.substring(15,29); 
               }
           else 
               if (IndicePath == 4){
               caminhoArq3 = ArquivoPath.substring(21,56);    
               }
           else
                if (IndicePath == 5){
                caminhoArq4 = ArquivoPath.substring(21, 51);   
               }
               else
               if (IndicePath == 6){
                caminhoArq5 = ArquivoPath.substring(21, 51);   
               } 
                else
               if (IndicePath == 7){
                caminhoArq6 = ArquivoPath.substring(22, 43);    
               }
                else
               if (IndicePath == 8){
                caminhoArq7 = ArquivoPath.substring(16, 27);    
               }
                else
               if (IndicePath == 9){
                caminhoArq8 = ArquivoPath.substring(10, 19);    
               }
                else
               if (IndicePath == 10){
                caminhoArq9 = ArquivoPath.substring(17, 30);    
               }
                else
               if (IndicePath == 11){
                caminhoArq10 = ArquivoPath.substring(8, 12);    
               }
                else
               if (IndicePath == 12){
                caminhoArq11 = ArquivoPath.substring(6, 8);    
               }
               else
               if (IndicePath == 13){
                caminhoArq12 = ArquivoPath.substring(17, 22);    
               }
               else 
                   if (IndicePath == 14) {
                       caminhoArq13 = ArquivoPath.substring(22,43);
                   }
               else 
                   if (IndicePath == 15) {
                       caminhoArq14 = ArquivoPath.substring(30,52);
                   }
       }
        IndicePath = IndicePath + 1; 
       }  
            brPath.close();
            
            File diretorio = new File(caminhoArq4);
            File fList[] = diretorio.listFiles();
            File pathName = new File(caminhoArq4);
               int linenumber = 0;
            
            int contadorarquivo = 0;
        for (int i = 0; i < fList.length; i++) {
               clienteCadastrado = fList[i].getName();
               relacaoCliente.add(clienteCadastrado);
           File f = fList[i];  
           s = f;
           contadorarquivo = contadorarquivo + 1;
        }
         if ((clienteCadastrado == null) || (clienteCadastrado == ""))
        {
        }
        else
        {
        int icontadorarquivo =0;
        while (icontadorarquivo < contadorarquivo)
        {
            System.out.println(fList[icontadorarquivo]);
            
            if (fList[icontadorarquivo].getName().substring(0,5).equals("D_EMB"))
            {
                clienteCadastrado = fList[icontadorarquivo].getName();
                System.out.println("Achei o arquivo da Sodexo: " + clienteCadastrado);
            }
           icontadorarquivo = icontadorarquivo + 1;         
        }
         
        String fileName = caminhoArq4+clienteCadastrado; 
        BufferedReader brLinha = new BufferedReader(new  FileReader(fileName)); 
               
               
               while (brLinha.readLine()!= null) {
    	        	 linenumber ++;                     
    	             }
    	        brLinha.close();
                System.out.println("Total de Linhas no arquivo é de: " + linenumber);
                
       
        
                String [] CodigoRegiao = new String [linenumber];
                String [] ContadorDeRegioes = new String [linenumber];
                String [] EstadoREgiao = new String [linenumber];	                           
                String [] CodigoTransportadora  = new String[linenumber];
                String TipoPlastico = null;
                String OArquivo1 = null;
                 int ContTransportadora = 0;
                  String Status = null;
                String CodigoTransportadoraAuxiliar = null;
               BufferedReader brTransportadora = new BufferedReader(new  FileReader(fileName)); 
               int IndiceTransportadora = 1; 
               while ((OArquivo1 = brTransportadora.readLine())!= null) 
	        { 
	            //System.out.println("ESTOU NA LINHA: " + IndiceTransportadora);
	               if (IndiceTransportadora == 1)
	               {
	                 TipoPlastico = OArquivo1.substring(125,129);
	               }
	               else if (IndiceTransportadora < linenumber)
	               {
	                   if (IndiceTransportadora == 2)
	                   {
	                   CodigoTransportadoraAuxiliar = OArquivo1.substring(1068,1078);
	                   CodigoTransportadora[ContTransportadora]=OArquivo1.substring(1068,1078);
	                   ContTransportadora = ContTransportadora + 1;
	                   }
	                   else
	                   { 
	                      if (IndiceTransportadora < linenumber)
	                      {
	                       int I = 0; 
	                           while (I < ContTransportadora)   
	                                  {
	                                   if  ( OArquivo1.substring(1068,1078).equals(CodigoTransportadora[I]) )
	                                         {
	                                            Status = "S";
	                                            //I =  ContTransportadora;
	                                            break;
	                                         }
	                                           else
	                                               {
	                                                  Status = "N";                                                   
	                                               }
	                                      I = I + 1;
	                                   }
	                           
	                           if (Status.equals("N"))
	                              {
	                                CodigoTransportadoraAuxiliar = OArquivo1.substring(1068,1078);
	                                CodigoTransportadora[ContTransportadora]=OArquivo1.substring(1068,1078);
	                                ContTransportadora = ContTransportadora + 1;
	                              }         
	                       } 
	                    }
	                 }
	                  else if (IndiceTransportadora == linenumber)
	                  {
	                   
	                  }
	               IndiceTransportadora = IndiceTransportadora + 1;
	        }
	        
	        brTransportadora.close();
	        System.out.println("A QUANTIDADE DE TRANPORTADORA DIFERENTE NO ARQUIVO DE EMBOSSING É: " + ContTransportadora);   
                
                
                
            //AQUI LOCALIZA QUANTAS REGIÕES EXISTEM
                int ContTrans = 0;
	    int ContTrans1 = 0;
	    int ContCodigoTrans_Regiao = 0;
	    int LinhaContCodRegiao = 1;
	    int quantidadePorRegiao = 0;
    	    int somacontRegiao = 0;
            String OArquivoTrans = null;
            
             int sum = 0;
	        ContTrans1 = 0;
	        LinhaContCodRegiao = 1;
	       while (ContTrans < ContTransportadora)
	     {
	        String Status2 = null;
	        BufferedReader brTrans = new BufferedReader(new  FileReader(fileName));
                
	        while ((OArquivoTrans = brTrans.readLine())!= null)
	        {
	            if (LinhaContCodRegiao == 1)
	            {
	                
	            }
	            else if (LinhaContCodRegiao < linenumber)
	            {
	                
	                        if (ContTrans1 == 0)
	                        {
	                            CodigoRegiao[quantidadePorRegiao]=OArquivoTrans.substring(651,661);
	                            EstadoREgiao[quantidadePorRegiao]=OArquivoTrans.substring(610,612);
	                            ContadorDeRegioes[quantidadePorRegiao] = "1";
	                            somacontRegiao=1;
	                            //System.out.println("A soma atual desta região é: " + ContadorDeRegioes[quantidadePorRegiao]);
	                             ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1;
	                            quantidadePorRegiao = quantidadePorRegiao + 1;
	                            ContTrans1 = ContTrans1 + 1;
                                    sum = sum + 1;
	                        }
	                        else
	                            {
	                                int ContInternoCodRegiao = 0;
	                                while (ContInternoCodRegiao < quantidadePorRegiao)
	                                    {
	                                        //System.out.println("Codigo da Região na Array: " + (CodigoRegiao[ContInternoCodRegiao]));
	                                       // System.out.println("Codigo da Região no Arquivo: " + (OArquivoTrans.substring(651,661)));
	                                        if (EstadoREgiao[ContInternoCodRegiao].equals(OArquivoTrans.substring(610,612)))
	                                        {
	                                            Status2 = "S";                                            
	                                            /*somacontRegiao = somacontRegiao + 1;
	                                            ContadorDeRegioes[ContInternoCodRegiao]= Integer.toString(somacontRegiao);
	                                            System.out.println("A soma atual desta região é: " + ContadorDeRegioes[ContInternoCodRegiao]);*/
	                                            //ContInternoCodRegiao = quantidadePorRegiao+1;
	                                            break;
	                                        }
	                                        else
	                                        {
	                                         Status2 = "N";
	                                        }
	                                       ContInternoCodRegiao = ContInternoCodRegiao + 1; 
	                                    }
                                        if (Status2.equals("N"))
	                                {
                                            Status2 = "S";
	                                    somacontRegiao=1;
	                                    CodigoRegiao[quantidadePorRegiao]=OArquivoTrans.substring(651,661);
	                                    EstadoREgiao[quantidadePorRegiao]=OArquivoTrans.substring(610,612);
	                                    ContadorDeRegioes[quantidadePorRegiao] = "1";
	                                    //System.out.println("A soma atual desta região é: " + ContadorDeRegioes[quantidadePorRegiao]);
	                                    ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1; 
	                                    quantidadePorRegiao = quantidadePorRegiao + 1;
                                            sum = sum + 1;
	                                }    

	                            }
                                
	            }
	            else if (LinhaContCodRegiao == linenumber)
	            {
	                
	            }
	            LinhaContCodRegiao = LinhaContCodRegiao + 1;     
	        }
	        brTrans.close();
                ContTrans = ContTrans + 1;
             }
            System.out.println("QUANTIDADE DE REGIÕES DIFERENTES SÃO: " + quantidadePorRegiao); 
            BufferedReader br = new BufferedReader(new  FileReader(fileName)); 
            BufferedReader br1 = new BufferedReader(new  FileReader(fileName));
            String NumArquivo = null;
            String NomeProduto = null;
            int Ind = 1;
            String text = null;
            int linenumber1 = 0;
            while ((text = br.readLine())!= null) {    	      	 
                if (Ind == 1) 
                {
                    NumArquivo = text.substring(2,11);
                    NomeProduto = text.substring(50,56);
                    
                }
                linenumber1 ++;
                Ind = Ind + 1;
                         }
           System.out.println("Total de Linhas neste arquivo: " + linenumber1);
          
           
               int ContCartaBercoPorTransportadora = 0;
               PrintWriter pw = null;
               String SequenciaInternaTexto = null;
              // while (ContCartaBercoPorTransportadora < ContTransportadora)
	        //{
	         int regiao = 0;
                 String Cabecalho = "N";            
              	 int ContarRegiao = 0;
              
                 while (regiao < quantidadePorRegiao)
                        {
                            
                           	  int Indice = 1; 
			          Cabecalho = "N";
			                   
			          String text1 = null;
			           
			          String Codigo = null;
			          String Codigo1 = null;
			          String Codigo2 = null;
			          String Codigo3 = null;
			          String Codigo4 = null;
                	          int SequenciaInterna =1;
                                  SequenciaInternaTexto = "";
                            
                            
                            String LerArquivoEmbossing = null;
                            BufferedReader LerArqEmb = new BufferedReader(new  FileReader(fileName)); 
                            int LinhaAtual = 1;
                            String InicioNomeArq = null;
                            String CabecalhoAux = "N";
                            
                           
            		    
                            while ((LerArquivoEmbossing  = LerArqEmb.readLine())!= null)
                            {
                                System.out.println("Linha atual: " + LinhaAtual);
                                if (LinhaAtual == 1)
                                {
                                    InicioNomeArq = LerArquivoEmbossing.substring(50,60);                                  
                                    
                                              
                                }
                                else 
                                      if (LinhaAtual < linenumber)
                                      {
                                          System.out.println("SEQUÊNCIA INTERNA: " + SequenciaInterna);
                                            if (SequenciaInterna < 10 ){
                                            SequenciaInternaTexto = "00000" + SequenciaInterna;
                                            }
                                            else if (SequenciaInterna < 100){
                                            SequenciaInternaTexto = "0000" + SequenciaInterna;
                                            }
                                            else if (SequenciaInterna < 1000){
                                            SequenciaInternaTexto = "000"+ SequenciaInterna;
                                            }
                                            else if (SequenciaInterna < 10000) {
                                            SequenciaInternaTexto = "00" + SequenciaInterna;
                                            }
                                            else if (SequenciaInterna < 100000){
                                            SequenciaInternaTexto = "0"+SequenciaInterna;
                                            }
                                            else {
                                             SequenciaInternaTexto = ""+SequenciaInterna;
                                                }
                                         //if ((EstadoREgiao[regiao].equals(LerArquivoEmbossing.substring(610,612)))&&(CodigoTransportadora[ContCartaBercoPorTransportadora].equals(LerArquivoEmbossing.substring(1068,1078))))
                                          if ((EstadoREgiao[regiao].equals(LerArquivoEmbossing.substring(610,612))))
                                          {                    
                                              	if (CabecalhoAux.equals("N"))
                                              	{
                                              	   String Migracao = null;
						        if ((!TipoPlastico.equals("0097")) && (!TipoPlastico.equals("0098")))
						              {
						          
						               Migracao="";
						                                      
						              }
						                else
						                 {
						                                          
				   	                           Migracao="MIG_"; 
                                                                 } 
                                              	   /*"caminho+nome do arquivo inicial+identificação do arquivo ALI ou REF + Estado + Sequencia do Arquivo + Codigo do Plastico"*/
                                              	   pw = new PrintWriter(new File(caminhoArq2+"DWS_Sodexo_"+InicioNomeArq.substring(0,6)+"_"+EstadoREgiao[regiao]+NumArquivo+"_"+InicioNomeArq.substring(6,10)+"_"+CodigoTransportadora[ContCartaBercoPorTransportadora]+"_"+CodigoRegiao[regiao]+".csv"));					        
					           pw.println("CodCartao;CodAR;QtdCartao");
					           CabecalhoAux = "S";
                                              	}
                                              	
                                              	 Codigo  = (LerArquivoEmbossing.substring(100, 116));
						 Codigo1 = (LerArquivoEmbossing.substring(634,651));
						 Codigo2 = (LerArquivoEmbossing.substring(2,11));
						 //Codigo3 = (LerArquivoEmbossing.substring(94,100));
                                                 Codigo3 = SequenciaInternaTexto;
						 //Codigo4 = (text1.substring(701,711));
						 pw.println(Codigo+";"+Codigo1+Codigo2+Codigo3+";1");
                                                 SequenciaInterna = SequenciaInterna + 1;
                                          }
                                              
                                       }   
                                      else 
                                          if (LinhaAtual == linenumber)
                                          {
                                              
                                          }
                             LinhaAtual = LinhaAtual + 1;     
                            }
                          pw.close();
	                  br1.close(); 
          		  regiao = regiao + 1;
                          CabecalhoAux = "N";
                          LerArqEmb.close();
                          
                          File ArqLog = new File ("C:\\Sodexo\\Log\\DtHrRecebimento.txt");
                        if (ArqLog.exists())
                        {
                            String path = "C:\\Sodexo\\Log\\DtHrRecebimento.txt";
                    
                            File ArquivoLido = new File(path);
                            FileOutputStream novaLinha = new FileOutputStream(ArquivoLido, true);
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date dataProcessamento = new Date();
                                                
                            String texto1 = "O Arquivo alimentar a DWS - DWS_Sodexo_: " + InicioNomeArq.substring(0,6)+"_"+EstadoREgiao[regiao]+NumArquivo+"_"+InicioNomeArq.substring(6,10)+"_"+CodigoTransportadora[ContCartaBercoPorTransportadora]+"_"+CodigoRegiao[regiao]+".csv" + ", foi criado corretamente."+"\t"+"Data e Hora do processo de criacao deste arquivo CSV: "+" "+dataProcessamento+"\n"; 
                            novaLinha.write(texto1.getBytes());
                            novaLinha.close();
                        }
                          
                        }            
         //ContCartaBercoPorTransportadora = ContCartaBercoPorTransportadora + 1;
        //}   
   } 
        
}
    
}

