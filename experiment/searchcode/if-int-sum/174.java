/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesImpressoes;

import aplicacaoimpresodexo.AplicacaoImpreSodexo;
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

import java.util.Timer;  
import java.util.TimerTask;  
import java.util.logging.*;  
/**
 *
 * @author Progamador
 */
public class Gerar_Registro_Por_Lote {
    
 
  public void GerarRegistroPorLote () throws DocumentException, IOException {
  File dirPath = new File("C:\\Sigma\\Clientes\\INI");
        File fListPath[] = dirPath.listFiles();
        File sPath = null; 
        int linenumberPath = 0;
        String arqIni = null;
        ArrayList arqPathList = new ArrayList();
        
        for (int i = 0; i < fListPath.length; i++) {
               arqIni = fListPath[i].getName();
               arqPathList.add(arqIni);
           File f = fListPath[i];  
           sPath = f;
          }
       String fileNamePath = "C:\\SIGMA\\Clientes\\INI\\"+arqIni;
       BufferedReader brPath = new BufferedReader(new  FileReader(fileNamePath));
       
       
       Date date1 = new Date(System.currentTimeMillis());
       DateFormat data_atual1 = new SimpleDateFormat("dd/MM/yyyy");
       
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
     
        String clienteCadastrado = null;      
        ArrayList relacaoCliente = new ArrayList();
        File diretorio = new File(caminhoArq13);
        File fList[] = diretorio.listFiles();
        File s = null;
       
        int contadorarquivo = 0;
        if (fList!=null)
        {
        for (int i = 0; i < fList.length; i++) {
               clienteCadastrado = fList[i].getName();
               relacaoCliente.add(clienteCadastrado);
           File f = fList[i];  
           s = f;
           contadorarquivo = contadorarquivo + 1;
          }
        if ((clienteCadastrado == null) || (clienteCadastrado == ""))
        {
            System.out.println("Não foi encontrado nenhum arquivo de Embossing para querbrar por lote os registros de cartão.");
        }
        else
            {
              //AQUI LOCALIZA QUAIS E QUANTAS SÃO AS TRANSPORTADORAS EXISTENTES DENTRO DO ARQUIVO DE EMBOSSING
	       int IndiceTransportadora = 1;
	       int ContTransportadora = 0;
	       String CodigoTransportadoraAuxiliar = null;
	       String Status = null;
               String OArquivo1 = null;
               String fileName = caminhoArq13+clienteCadastrado; 
               BufferedReader brTransportadora = new BufferedReader(new  FileReader(fileName)); 
               String TipoPlastico1  = null;
               BufferedReader br = new BufferedReader(new  FileReader(fileName)); 
               int linenumber = 0;
               
               while (br.readLine()!= null) {
    	        	 linenumber ++;                     
    	             }
    	        br.close();
                System.out.println("Total de Linhas no arquivo é de: " + linenumber);
                
                String [] CodigoTransportadora  = new String[linenumber];
	        String [] CodigoRegiao = new String [linenumber];
                String [] ContadorDeRegioes = new String [linenumber];
                String [] EstadoREgiao = new String [linenumber];
                String [] CodigoTransportadoraAUX = new String [linenumber];
                String [] NomeTransportadoraAUX = new String [linenumber];
                String TipoCartaBerco = "";
                String CartaBerco = "";
                String NomeDoArquivoNovo = null;
                int I = 0; 
                
	        while ((OArquivo1 = brTransportadora.readLine())!= null) 
	        { 
	            System.out.println("ESTOU NA LINHA: " + IndiceTransportadora);
	               if (IndiceTransportadora == 1)
	               {
	                 TipoPlastico1 = OArquivo1.substring(125,129);
                         NomeDoArquivoNovo = OArquivo1.substring(50, 56);
	               }
	               else if (IndiceTransportadora < linenumber)
	               {
	                   if (IndiceTransportadora == 2)
	                   {
	                   CodigoTransportadoraAuxiliar = OArquivo1.substring(1068,1078);
	                   CodigoTransportadora[ContTransportadora]=OArquivo1.substring(1068,1078);
	                   ContTransportadora = ContTransportadora + 1;
                           TipoCartaBerco = OArquivo1.substring(902,903);
                           if (TipoCartaBerco.equals("1"))
                            {
                                CartaBerco = "DESBLOQ"   ;
                            }
                            else if (TipoCartaBerco.equals("0"))
                                {
                                    CartaBerco = "Bloq";
                                }
	                   }
	                   else
	                   { 
	                      if (IndiceTransportadora < linenumber)
	                      {
	                       I = 0; 
	                           while (I < ContTransportadora)   
	                                  {
	                                   if  (CodigoTransportadora[I].equals(OArquivo1.substring(1068,1078)) )
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
            quantidadePorRegiao = 0;
             int sum = 0;
	        ContTrans1 = 0;
	        LinhaContCodRegiao = 1;
	       
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
                                    CodigoTransportadoraAUX[quantidadePorRegiao]=OArquivoTrans.substring(1068,1078);
                                    if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000636397"))
                                    {
                                        NomeTransportadoraAUX[quantidadePorRegiao] = "MBC";
                                    }
                                     else
                                    if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000687392"))
                                    {
                                         NomeTransportadoraAUX[quantidadePorRegiao]  = "RGV TRANSP";
                                    }
                                    else
                                        if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000687575"))
                                        {
                                             NomeTransportadoraAUX[quantidadePorRegiao]  = "RGV INT SP";
                                        }
                                        else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000795636"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "WIHUS";
                                            }
                                            else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001168209"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "ADVLOG";
                                            }
                                            else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001216785"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "RODOBAN";
                                            }
                                           else
                                               if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000670864"))
                                                {
                                                     NomeTransportadoraAUX[quantidadePorRegiao]  = "SEDEX C";
                                                }
                                               else
                                                  if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000389255"))
                                                    {
                                                         NomeTransportadoraAUX[quantidadePorRegiao]  = "SEDEX T";
                                                    }
                                                    else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001411552"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao]  = "ILOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001419578"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao]  = "INTERLOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001419579"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao] ="TEXTLOG";
                                                        }
	                            ContadorDeRegioes[quantidadePorRegiao] = "1";
	                            somacontRegiao=1;
	                            System.out.println("A soma atual desta região é: " + ContadorDeRegioes[quantidadePorRegiao]);
	                           // ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1;
	                            quantidadePorRegiao = quantidadePorRegiao + 1;
	                            ContTrans1 = ContTrans1 + 1;
                                    sum = sum + 1;
	                        }
	                        else
	                            {
	                                int ContInternoCodRegiao = 0;
	                                while (ContInternoCodRegiao < quantidadePorRegiao)
	                                    {
	                                        System.out.println("Codigo da Região na Array: " + (CodigoRegiao[ContInternoCodRegiao]));
	                                        System.out.println("Codigo da Região no Arquivo: " + (OArquivoTrans.substring(651,661)));
	                                        if (EstadoREgiao[ContInternoCodRegiao].equals(OArquivoTrans.substring(610,612)))
	                                        {
	                                            Status2 = "S";                                            
	                                            /*somacontRegiao = somacontRegiao + 1;
	                                            ContadorDeRegioes[ContInternoCodRegiao]= Integer.toString(somacontRegiao);
	                                            System.out.println("A soma atual desta região é: " + ContadorDeRegioes[ContInternoCodRegiao]);*/
	                                            ContInternoCodRegiao = quantidadePorRegiao+1;
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
                                            CodigoTransportadoraAUX[quantidadePorRegiao]=OArquivoTrans.substring(1068,1078);
                                        if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000636397"))
                                        {
                                            NomeTransportadoraAUX[quantidadePorRegiao] = "MBC";
                                        }
                                        else
                                        if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000687392"))
                                        {
                                            NomeTransportadoraAUX[quantidadePorRegiao]  = "RGV TRANSP";
                                        }
                                        else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000687575"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "RGV INT SP";
                                            }
                                            else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000795636"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "WIHUS";
                                            }
                                            else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001168209"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "ADVLOG";
                                            }
                                            else
                                            if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001216785"))
                                            {
                                                 NomeTransportadoraAUX[quantidadePorRegiao]  = "RODOBAN";
                                            }
                                           else
                                               if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000670864"))
                                                {
                                                     NomeTransportadoraAUX[quantidadePorRegiao]  = "SEDEX C";
                                                }
                                               else
                                                  if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0000389255"))
                                                    {
                                                         NomeTransportadoraAUX[quantidadePorRegiao]  = "SEDEX T";
                                                    }
                                                    else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001411552"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao]  = "ILOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001419578"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao]  = "INTERLOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[quantidadePorRegiao].equals("0001419579"))
                                                        {
                                                             NomeTransportadoraAUX[quantidadePorRegiao] ="TEXTLOG";
                                                        }
	                                    ContadorDeRegioes[quantidadePorRegiao] = "1";
	                                    System.out.println("A soma atual desta região é: " + ContadorDeRegioes[quantidadePorRegiao]);
	                                   // ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1; 
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
	        //System.out.println("ESTA TRANSPORTADORA: " +  CodigoTransportadora[ContTrans] + " DESTA REGIÃO: " + CodigoRegiao[ContCodigoTrans_Regiao-1] + " POSSUI ESSA QUANTIDADE DE ENTREGA DIFERENTES: " + quantidadePorRegiao  );  
	  
   	
   	
   	    int regiaoQtde = 0;
	    while (regiaoQtde < quantidadePorRegiao )
	    {
	        System.out.println("Indice: " + regiaoQtde);
	        System.out.println("Região: " + CodigoRegiao[regiaoQtde] + " quantidade é: " + ContadorDeRegioes[regiaoQtde] + " o estado é: " + EstadoREgiao[regiaoQtde]);
	        regiaoQtde = regiaoQtde + 1;
	    }
    	  System.out.println("QUANTIDADE DE REGIÕES DIFERENTES SÃO: " + sum);   
          
          int CriarArquivo = 0;
          String TipoPlastico = null;
          String Arq = "N";
          PrintWriter pw = null;
         //while (CriarArquivo < ContTransportadora)
               //{
                    int ContarRegiao = 0;
                    
                    while (ContarRegiao < quantidadePorRegiao)
                        {
                            
                            
                            String LerArquivoEmbossing = null;
                            BufferedReader LerArqEmb = new BufferedReader(new  FileReader(fileName)); 
                            int LinhaAtual = 1;
                            String InicioNomeArq = null;
                            String FimNomeArq = null;
                            String Migracao = null;
                            String Cabecalho = null;
                            String CabecalhoAux = "N";
                            
                            while ((LerArquivoEmbossing  = LerArqEmb.readLine())!= null)
                            {
                                System.out.println("Linha atual: " + LinhaAtual);
                                if (LinhaAtual == 1)
                                {
                                    InicioNomeArq = LerArquivoEmbossing.substring(50,60);
                                    FimNomeArq = LerArquivoEmbossing.substring(68,74);
                                    TipoPlastico = LerArquivoEmbossing.substring(125, 129);
                                    
                                    //Cabecalho = LerArquivoEmbossing;
                                              
                                }
                                else 
                                      if (LinhaAtual < linenumber)
                                      {
                                         //if ((EstadoREgiao[ContarRegiao].equals(LerArquivoEmbossing.substring(610,612)))&&(CodigoTransportadora[CriarArquivo].equals(LerArquivoEmbossing.substring(1068,1078))))
                                          if ((EstadoREgiao[ContarRegiao].equals(LerArquivoEmbossing.substring(610,612))))
                                          {
                                             
                                              if (Arq.equals("N"))
                                              {
                                                  Arq = "S";
                                                  if ((TipoPlastico.equals("0097"))||(TipoPlastico.equals("0098")))
                                                  {
                                                      
                                                      Migracao = "_MIGRACAO_";
                                                  }
                                                  else
                                                  {
                                                      Migracao = "_";
                                                  }
                                                  pw = new PrintWriter(new File(caminhoArq2+InicioNomeArq.substring(0,6)+Migracao+EstadoREgiao[ContarRegiao]+"_"+CartaBerco+"_"+InicioNomeArq.substring(6,10)+"_"+FimNomeArq+".txt"));
                                              }
                                              if (CabecalhoAux.equals("N"))
                                              {
                                                 //pw.println(Cabecalho);
                                                 pw.println(LerArquivoEmbossing); 
                                                 CabecalhoAux = "S";
                                              }
                                              else
                                              {
                                                 pw.println(LerArquivoEmbossing);  
                                              }
                                              
                                          }
                                              
                                       }   
                                      else 
                                          if (LinhaAtual == linenumber)
                                          {
                                              //pw.println(LerArquivoEmbossing);
                                          }
                             LinhaAtual = LinhaAtual + 1;  
                             
                            }
                          CabecalhoAux = "N";
                          Arq = "N";
                          LerArqEmb.close();
                          pw.close();
                          ContarRegiao = ContarRegiao + 1;  
                          
                            File ArqLog = new File ("C:\\Sodexo\\Log\\DtHrRecebimento.txt");
                            if (ArqLog.exists())
                            {
                            String path = "C:\\Sodexo\\Log\\DtHrRecebimento.txt";
                        
                            File ArquivoLido = new File(path);
                            FileOutputStream novaLinha = new FileOutputStream(ArquivoLido, true);
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date dataProcessamento = new Date();
                                                
                            String texto1 = "O Arquivo: " + InicioNomeArq.substring(0,6)+Migracao+EstadoREgiao[ContarRegiao]+"_"+CartaBerco+"_"+InicioNomeArq.substring(6,10)+"_"+FimNomeArq+".txt" + ", foi gerado com sucesso."+"\t"+"Data e Hora do processo de geracao deste arquivo: "+" "+dataProcessamento+"\n"; 
                            novaLinha.write(texto1.getBytes());
                            novaLinha.close();
                            }
                          //quantidadePorRegiao = quantidadePorRegiao + 1;
                        }
                  //CriarArquivo = CriarArquivo + 1;
                //}   
 
            }
  }
  }  
}

