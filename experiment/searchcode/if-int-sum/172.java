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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.xml.crypto.Data;

/**
 *
 * @author Progamador
 */
public class CartaBerco {
  
    
    public void CartaBerco() throws FileNotFoundException, DocumentException, IOException, SQLException, ClassNotFoundException {
        GeradorLog gerador = GeradorLog.getInstance();
        gerador.setLevel(Level.INFO);
        gerador.log(Level.INFO, "Iniciando a conexão com o java.security.provider"); 
        AplicacaoImpreSodexo cartasenha = new AplicacaoImpreSodexo();
        
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
       if (arqIni != null)
       {
       gerador.setLevel(Level.INFO);
       gerador.log(Level.INFO, "Arquivo INI encontrado");    
       }
       else
       {
       gerador.setLevel(Level.INFO);
       gerador.log(Level.INFO, "Arquivo INI NÃO encontrado");    
       }
       String fileNamePath = "C:\\SIGMA\\Clientes\\INI\\"+arqIni;
       BufferedReader brPath = new BufferedReader(new  FileReader(fileNamePath));
 
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
       
      /*String driver = caminhoArq6; //Classe do driver JDBC                                        
      String banco = caminhoArq7; //Nome do Banco criado                                          
      String host = caminhoArq8; //Maquina onde está o banco                                           
      String str_conn = caminhoArq9 + host + ":3306/" + banco; //URL de conexão                                             
      String usuario = caminhoArq10; //Usuário do banco                                       
      String senha = ""; //Senha de conexão                        
      Connection conn = null;                                   
      Class.forName(driver);                            
      conn = (Connection) DriverManager.getConnection(str_conn, usuario, senha);
      System.out.println("A conexão foi bem sucedida\n");*/
      
      
        File s = null;
        int Indice = 1;
        
        ArrayList relacaoCliente = new ArrayList();
        File diretorio = new File(caminhoArq4);
        File fList[] = diretorio.listFiles();
        String clienteCadastrado = null;
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
        String ArqEncontrado = null;
        while (icontadorarquivo < contadorarquivo)
        {
            System.out.println(fList[icontadorarquivo]);
            
            if (fList[icontadorarquivo].getName().substring(0,5).equals("D_EMB"))
            {
                clienteCadastrado = fList[icontadorarquivo].getName();
                System.out.println("Achei o arquivo da Sodexo: " + clienteCadastrado);
                ArqEncontrado = "D_EMB";
            }
           icontadorarquivo = icontadorarquivo + 1;         
        }
        if (ArqEncontrado==(null)){
          System.out.println("NAO EXISTE ARQUIVO PARA PRODUCAO DA FOLHETARIA.");
           
          gerador.setLevel(Level.INFO);
          gerador.log(Level.INFO, "Arquivo de Embossing NÃO encontrado");    
        }

        else
        {
        System.out.println("Existe. " + clienteCadastrado);
        gerador.setLevel(Level.INFO);
        gerador.log(Level.INFO, "Arquivo de Embossing  encontrado");
        DateFormat data_atual = new SimpleDateFormat("ddMMMyyyy");  
        Date date = new Date(System.currentTimeMillis());
        
        
        Image img = null; 
        Image imgMigracao = null;
        Image imgQuadroBranco = null;
        img = Image.getInstance("C:\\SIGMA\\Clientes\\Sodexo\\Imagens\\SODEXO_CARTA_BERCO_FRENTE_NOVA.WMF");
        imgMigracao = Image.getInstance("C:\\SIGMA\\Clientes\\Sodexo\\Imagens\\ALI_MIGRACAO.WMF");
        imgQuadroBranco = Image.getInstance("C:\\SIGMA\\Clientes\\Sodexo\\Imagens\\Quadro_Branco.WMF");
        
       
       
       
       
       String fileName = caminhoArq4+clienteCadastrado;
       System.out.println(clienteCadastrado.toString().substring(0,46));
       BufferedReader br = new BufferedReader(new  FileReader(fileName)); 
       BufferedReader brCArtaBerco = new BufferedReader(new  FileReader(fileName)); 
       BufferedReader brCArtaBerco1 = new BufferedReader(new  FileReader(fileName)); 
       BufferedReader brTransportadora = new BufferedReader(new  FileReader(fileName));        
           while (br.readLine()!= null) {
	    	        	 linenumber ++;                     
	    	             }
	     
	        br.close();
	        System.out.println("Total de Linhas existentes no arquivo é: " + linenumber); 
	         
	        String [] NumeroHawb = new String [linenumber];
	        String [] Endereco = new String [linenumber];
	        String [] Complemento = new String [linenumber];
	        String [] CEP = new String [linenumber];
	        String [] Bairro = new String [linenumber];
	        String [] Cidade = new String [linenumber];
	        String [] Estado = new String [linenumber];
	        String[] HAWB = new String[linenumber];
	        String[] NumeroBordero = new String[linenumber];
	        String NumeroSeqDoArquivo = null;
	        String [] CodBarra = new String [linenumber];
	        String [] Matricula = new String [linenumber];
	        String [] CodDepto = new String [linenumber];
	        String [] NomeDepto = new String [linenumber];
	        String [] NomeBeneficiario = new String[linenumber];
	        String [] NomeCliente = new  String [linenumber];
	        String [] CodigoCliente = new String [linenumber];
	        String [] Cartao = new String [linenumber];
	        String [] seqobjeto = new String [linenumber];
	        String [] CodigoTransportadora = new String [linenumber];
                String [] CodigoTransportadoraAUX = new String [linenumber];
	        String [] CodigoRegiao = new String [linenumber];
	        String [] EstadoREgiao = new String [linenumber];
	        String [] ContadorDeRegioes = new String [linenumber];
                String[] senha = new String[linenumber];
                String TipoPlastico  = null;
	       
	        String OArquivo = null;
	        String OArquivo1 = null;
	        String OArquivo2 = null;
	        String OArquivoAux = null;
	        String OArquivoTrans = null;
	        String TipoCartaBerco = "";
                String CartaBerco = "";
                String MigracaoLog = null;
                String TransportadoraLog = null;
        
        	String SequenciaTexto = null;
        	int Cont = 0;
        
                int IndiceTransportadora = 1;
	        int ContTransportadora = 0;
	        String CodigoTransportadoraAuxiliar = null;
                String Status = null;
                String NomeTransportadora = null;
        
        while ((OArquivo1 = brTransportadora.readLine())!= null) 
	        { 
	            System.out.println("ESTOU NA LINHA: " + IndiceTransportadora);
	               if (IndiceTransportadora == 1)
	               {
	                 
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
	                                            I =  ContTransportadora;
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
        
            int ContTrans = 0;
	    int ContTrans1 = 0;
	    int ContCodigoTrans_Regiao = 0;
	    int LinhaContCodRegiao = 1;
	    int quantidadePorRegiao = 0;
    	    int somacontRegiao = 0;
            String Carta = null;
        
        /*while (ContTrans < ContTransportadora)
	    {   
	        ContTrans1 = 0;
	        LinhaContCodRegiao = 1;
	        quantidadePorRegiao = 0;
	        String Status2 = null;
	        BufferedReader brTrans = new BufferedReader(new  FileReader(fileName));
	        while ((OArquivoTrans = brTrans.readLine())!= null)
	        {
	            if (LinhaContCodRegiao == 1)
	            {
	                
	            }
	            else if (LinhaContCodRegiao < linenumber)
	            {
	                if (CodigoTransportadora[ContTrans].equals(OArquivoTrans.substring(1068,1078)))
	                    {
	                        if (ContTrans1 == 0)
	                        {
	                            CodigoRegiao[ContCodigoTrans_Regiao]=OArquivoTrans.substring(651,658);
	                            EstadoREgiao[ContCodigoTrans_Regiao]=OArquivoTrans.substring(610,612);
	                            ContadorDeRegioes[ContCodigoTrans_Regiao] = "1";
	                            somacontRegiao=1;
	                            System.out.println("A soma atual desta região é: " + ContadorDeRegioes[ContCodigoTrans_Regiao]);
	                            ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1;
	                            quantidadePorRegiao = quantidadePorRegiao + 1;
	                            ContTrans1 = ContTrans1 + 1;
	                        }
	                        else
	                            {
	                                int ContInternoCodRegiao = 0;
	                                while (ContInternoCodRegiao < ContCodigoTrans_Regiao)
	                                    {
	                                        System.out.println("Codigo da Região na Array: " + (CodigoRegiao[ContInternoCodRegiao]));
	                                        System.out.println("Codigo da Região no Arquivo: " + (OArquivoTrans.substring(651,658)));
	                                        if (EstadoREgiao[ContInternoCodRegiao].equals(OArquivoTrans.substring(610,612)))
	                                        {
	                                            Status2 = "S";                                            
	                                            somacontRegiao = somacontRegiao + 1;
	                                            ContadorDeRegioes[ContInternoCodRegiao]= Integer.toString(somacontRegiao);
	                                            System.out.println("A soma atual desta região é: " + ContadorDeRegioes[ContInternoCodRegiao]);
	                                            ContInternoCodRegiao = ContCodigoTrans_Regiao;
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
	                                    somacontRegiao=1;
	                                    CodigoRegiao[ContCodigoTrans_Regiao]=OArquivoTrans.substring(651,658);
	                                    EstadoREgiao[ContCodigoTrans_Regiao]=OArquivoTrans.substring(610,612);
	                                    ContadorDeRegioes[ContCodigoTrans_Regiao] = "1";
	                                    System.out.println("A soma atual desta região é: " + ContadorDeRegioes[ContCodigoTrans_Regiao]);
	                                    ContCodigoTrans_Regiao = ContCodigoTrans_Regiao + 1; 
	                                    quantidadePorRegiao = quantidadePorRegiao + 1;
	                                }                                
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
	        ContTrans = ContTrans + 1;
   	}*/
           String Status2 = null;
           int sum = 0;
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
            
            
            int regiaoQtde = 0;
	    while (regiaoQtde < quantidadePorRegiao )
	    {
	        System.out.println("Indice: " + regiaoQtde);
	        System.out.println("Região: " + CodigoRegiao[regiaoQtde] + " quantidade é: " + ContadorDeRegioes[regiaoQtde] + " o estado é: " + EstadoREgiao[regiaoQtde]);
	        regiaoQtde = regiaoQtde + 1;
	    }
    	System.out.println("QUANTIDADE DE REGIÕES DIFERENTES SÃO: " + ContCodigoTrans_Regiao);   
        
        
        
        int Indice1 = 1;
        
        String SequenciaInternaTexto = null;
        while ((OArquivo1 = brCArtaBerco.readLine())!= null) 
        { 
               if (Indice1 == 2)
               {
               TipoCartaBerco = OArquivo1.substring(902,903);    
               }
               Indice1 = Indice1 + 1;
        }
       if (TipoCartaBerco.equals("1"))
       {
        CartaBerco = "Des"   ;
       }
       else if (TipoCartaBerco.equals("0"))
       {
           CartaBerco = "Bloq";
       }
       brCArtaBerco.close();
       
       System.out.println("Tipo de Carta Berço é: " + CartaBerco);
       
       int ContCartaBercoPorTransportadora = 0;
       String TipoArquivo = null;
       boolean TemPaginas = false;
       
       //while (ContCartaBercoPorTransportadora < ContTransportadora)
       //{
          int regiao = 0;
            
          while (regiao < regiaoQtde )
           { 
              int Sequencia = 0;
              Indice = 1;
              int SequenciaInterna =1;
              SequenciaInternaTexto = "";
              BufferedReader br1 = new BufferedReader(new  FileReader(fileName)); 
              Document document = new Document(PageSize.A4);
              PdfWriter writer = null;
              PdfContentByte cb = null; 
              PrintWriter pw = null;
              TemPaginas = false;
       
       
       int ContTxt = 0;
       while ((OArquivo = br1.readLine())!= null) { 
       
       
               if (Indice == 1){
                NumeroSeqDoArquivo = OArquivo.substring(2,11);  
                Indice = Indice + 1;
                TipoPlastico = OArquivo.substring(125,129);
                //System.out.println("Passei pela primiera linha!");
                TipoArquivo = OArquivo.substring(50,53);
               }
               else
                   
               if (Indice <  linenumber){ 
                   
                    Sequencia = Sequencia + 1;
                    if (Sequencia < 10 ){
                        SequenciaTexto = "00000" + Sequencia;
                    }
                    else if (Sequencia < 99){
                        SequenciaTexto = "0000" + Sequencia;
                    }
                    else if (Sequencia < 999){
                        SequenciaTexto = "000"+ Sequencia;
                    }
                    else if (Sequencia < 9999) {
                        SequenciaTexto = "00" + Sequencia;
                    }
                    else if (Sequencia < 99999){
                        SequenciaTexto = "0"+Sequencia;
                    }
                    else {
                        SequenciaTexto = ""+Sequencia;
                    }
                    
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
                    System.out.println("SEQUÊNCIA INTERNA TEXTO: " + SequenciaInternaTexto);
                    System.out.println("Estou escrevendo a carta berço");
                    System.out.println("Linha: " + Indice);
                    NomeCliente [Cont] = OArquivo.substring(165,192);
                    CodigoCliente [Cont] = OArquivo.substring(36, 46);
                    HAWB[Cont] =  OArquivo.substring(703,711); 
                    Endereco[Cont] = OArquivo.substring(440,520); 
                    Complemento[Cont] = OArquivo.substring(520,550); 
                    Bairro[Cont] =  OArquivo.substring(550,580); 
                    Cidade[Cont] = OArquivo.substring(580,610); 
                    CEP[Cont] = OArquivo.substring(612,621);
                    Estado[Cont] = OArquivo.substring(610,612); 
                    NumeroBordero[Cont] = OArquivo.substring(634,651); 
                    Matricula[Cont] = OArquivo.substring(775,785); 
                    CodDepto[Cont] = OArquivo.substring(785,803); 
                    NomeDepto[Cont] = OArquivo.substring(803,833);
                    NomeBeneficiario [Cont] = OArquivo.substring(138,165);
                    seqobjeto [Cont] = OArquivo.substring(94, 100);
                    CodigoTransportadoraAUX[Cont]=OArquivo.substring(1068,1078);
                    //CodBarra[Cont] = "*"+NumeroBordero[Cont]+NumeroSeqDoArquivo+"."+seqobjeto[Cont]+"*"; antigo, buscar a sequencia do objeto direto no arquivo de embossing
                    //String CodBarraS = NumeroBordero[Cont]+NumeroSeqDoArquivo+"."+seqobjeto[Cont];
                    
                    CodBarra[Cont] = "*"+NumeroBordero[Cont]+NumeroSeqDoArquivo+"."+SequenciaInternaTexto+"*";
                    String CodBarraS = NumeroBordero[Cont]+NumeroSeqDoArquivo+"."+SequenciaInternaTexto;
                    
                    Cartao [Cont] = OArquivo.substring(111,116);
                    senha[Cont] = OArquivo.substring(246, 262);
                    
                    
                    if (CodigoTransportadoraAUX[Cont].equals("0000636397"))
                      {
                          NomeTransportadora = "MBC";
                      }
                      else
                          if (CodigoTransportadoraAUX[Cont].equals("0000687392"))
                            {
                                NomeTransportadora = "RGV TRANSP";
                            }
                      else
                               if (CodigoTransportadoraAUX[Cont].equals("0000687575"))
                                 {
                                        NomeTransportadora = "RGV INT SP";
                                 }
                                else
                                   if (CodigoTransportadoraAUX[Cont].equals("0000795636"))
                                    {
                                        NomeTransportadora = "WIHUS";
                                    }
                                    else
                                       if (CodigoTransportadoraAUX[Cont].equals("0001168209"))
                                        {
                                            NomeTransportadora = "ADVLOG";
                                        }
                                        else
                                           if (CodigoTransportadoraAUX[Cont].equals("0001216785"))
                                            {
                                                NomeTransportadora = "RODOBAN";
                                            }
                                           else
                                               if (CodigoTransportadoraAUX[Cont].equals("0000670864"))
                                                {
                                                    NomeTransportadora = "SEDEX C";
                                                }
                                               else
                                                  if (CodigoTransportadoraAUX[Cont].equals("0000389255"))
                                                    {
                                                        NomeTransportadora = "SEDEX T";
                                                    }
                                                    else
                                                       if (CodigoTransportadoraAUX[Cont].equals("0001411552"))
                                                        {
                                                            NomeTransportadora = "ILOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[Cont].equals("0001419578"))
                                                        {
                                                            NomeTransportadora = "INTERLOG";
                                                        }  
                                                        else
                                                       if (CodigoTransportadoraAUX[Cont].equals("0001419579"))
                                                        {
                                                            NomeTransportadora="TEXTLOG";
                                                        }
                    TransportadoraLog = NomeTransportadora;
                    
                    //if (CodigoTransportadora[ContCartaBercoPorTransportadora].equals(OArquivo.substring(1068,1078)) && (EstadoREgiao[regiao].equals(OArquivo.substring(610,612))))
                    if ((EstadoREgiao[regiao].equals(OArquivo.substring(610,612))))
		    {
		       System.out.println("Estou escrevendo a carta berço na linha: " + Indice);
		       if (TemPaginas == false)
		    { 
                         String Migracao = null;
                            if ((!TipoPlastico.equals("0097")) && (!TipoPlastico.equals("0098")) && (!TipoPlastico.equals("0087")))
                                {
           
                                    Migracao="";
                                    Carta = "CARTA_BERCO_";
                                    CartaBerco="Desbloq";
                                }
                            else
                                {
                                    
                           if (TipoPlastico.equals("0087"))
                                    {
                                        Migracao="";
                                        Carta = "CARTA_BERCO_";
                                         CartaBerco="Bloq"; 
                                    }
                                else
                                    if (CartaBerco.equals("Bloq"))
                                    {
                                        Migracao="MIG_"; 
                                        Carta = "CARTBERC_";
                                    }
                                    else
                                        if (CartaBerco.equals("Des"))
                                        {
                                            Migracao = "MIG_";
                                            Carta = "CartBerc_";
                                        }      
                                    
                                }
                      
                      
                      pw = new PrintWriter( new File(caminhoArq2+Carta+Migracao+CartaBerco+"_"+TipoArquivo+"_"+EstadoREgiao[regiao]+clienteCadastrado.toString().substring(42,46)+"_"+data_atual.format(date)+"_"+CodigoTransportadora[ContCartaBercoPorTransportadora]+/*"_"+NomeTransportadora+*/".txt"));
		      writer = PdfWriter.getInstance(document, new FileOutputStream(caminhoArq1+Carta+Migracao+CartaBerco+"_"+TipoArquivo+"_"+EstadoREgiao[regiao]+clienteCadastrado.toString().substring(42,46)+"_"+data_atual.format(date)+"_"+CodigoTransportadora[ContCartaBercoPorTransportadora]+/*"_"+NomeTransportadora+*/".pdf"));    
		      MigracaoLog = Migracao;
                      
                      document.open(); 
		      cb = writer.getDirectContent();
		      TemPaginas = true;
                    }
                    
                    document.newPage(); 
                    
                   
                                               
                    
                    
                    cb.beginText();
                    if ((!TipoPlastico.equals("0097"))&& (!TipoPlastico.equals("0098"))) //TESTA SE É ARQUIVO DE ALIMENTAÇÃO DE MIGRAÇÃO
                    { 
                    img.setAbsolutePosition (1F, 1f);
                    //document.add(img);
                    
                    float y = 0.0001f;
                    BarcodeInter25 cod25i = new BarcodeInter25(); 
                    cod25i.setSize(y);
                    cod25i.setBarHeight(25f);
                    cod25i.setX(0.68f);
                    
                    cod25i.setFont(null);
                    cod25i.setCodeType(cod25i.CODE128);
                    cod25i.setCode(CodBarraS);//NumeroBordero+NumeroSeqDoArquivo+"."+SequenciaTexto;
                    Image  imageCod25i = null; 
                    imageCod25i = cod25i.createImageWithBarcode(cb, null, null); 
                        
                    imageCod25i.setAbsolutePosition(330f, 704);
                    document.add(imageCod25i);
                    imgQuadroBranco.setAbsolutePosition (375F, 695f);
                    //document.add(imgQuadroBranco); 
                    
                    BaseFont bfArq2 = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    cb.setFontAndSize(bfArq2, 6);
                    //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, seqobjeto[Cont]+ "", 330,698, 0);        ANTIGO COM A SEQEUANCI DO OBJETO NO ARQUIVO DE EMBOSSING
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, SequenciaInternaTexto+ "", 330,698, 0);        
                    BaseFont bfArq3 = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    cb.setFontAndSize(bfArq3, 6); 
                    //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, " - SODEXO PASS DO BRASIL"+ "", 349,690, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, CodigoCliente[Cont]+" - "+NomeCliente[Cont]+"", 330,685, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "HAWB: "+ "", 330,675, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, HAWB[Cont]+ "", 370,675, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, NomeBeneficiario[Cont]+ "", 330,665, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Matrícula: "+ "", 330,655, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Matricula[Cont]+ "", 370,655, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Depto: "+ "",420,655, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, CodDepto[Cont]+"",441,655, 0);
                    cb.setFontAndSize(bfArq3, 5); 
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "   " + NomeDepto[Cont] +"",495,655, 0);
                    cb.setFontAndSize(bfArq3, 6); 
                    //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "ALAMEDA ARAGUAIA Nº 1142"+ "", 330,635, 0);
                    //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "ALPHAVILLE INDUSTRIA CEP: 06455-000  BARUERI - SP "+ "", 330,625, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Endereco[Cont]+" ", 330,645, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Complemento[Cont]+ "", 500,645, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Bairro[Cont]+ "                 "+Cidade[Cont]+ " "+Estado[Cont]+ "", 330,635, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "CEP: " +CEP[Cont]+ "", 330,625, 0);
                    
                    BaseFont bfArqCodigoTransportadora = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		    cb.setFontAndSize(bfArq3, 4); 
		    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, NomeTransportadora+"_"+CodigoRegiao[regiao]+"_"+EstadoREgiao[regiao] + "", 500,8, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT,  TipoArquivo+"_"+EstadoREgiao[regiao]+clienteCadastrado.toString().substring(42,46)+" /"+"",543,138, 0);                   
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, SequenciaInternaTexto+"",570,138, 0);
                    }
                    else
                    /*if ((TipoPlastico.equals("0097")) || (TipoPlastico.equals("0098"))) //TESTA SE É ARQUIVO DE ALIMENTAÇÃO DE MIGRAÇÃO*/
                    {    
                       imgMigracao.setAbsolutePosition (1F, 1f);
                        //document.add(imgMigracao);                        
                        
                        float y = 0.0001f;
                        BarcodeInter25 cod25i = new BarcodeInter25(); 
                        cod25i.setSize(y);
                        cod25i.setBarHeight(25f);
                        cod25i.setX(0.68f);
                    
                        cod25i.setFont(null);
                        cod25i.setCodeType(cod25i.CODE128);
                        cod25i.setCode(CodBarraS);//NumeroBordero+NumeroSeqDoArquivo+"."+SequenciaTexto;
                        Image  imageCod25i = null; 
                        imageCod25i = cod25i.createImageWithBarcode(cb, null, null); 
                        
                        imageCod25i.setAbsolutePosition(330f, 690);
                        document.add(imageCod25i);
                        imgQuadroBranco.setAbsolutePosition (375F, 695f);
                        //document.add(imgQuadroBranco); 
                        BaseFont bfArq2 = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                        cb.setFontAndSize(bfArq2, 6);
                        //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, seqobjeto[Cont]+ "", 330,683, 0);        ANTIGO COM A SEQUENCIA DO OBJETO DO ARQUIVO DE EMBOSSING
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, SequenciaInternaTexto+ "", 330,683, 0);        
                        BaseFont bfArq3 = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                        cb.setFontAndSize(bfArq3, 6); 
                        //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, " - SODEXO PASS DO BRASIL"+ "", 349,690, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, CodigoCliente[Cont]+" - "+NomeCliente[Cont]+"", 330,675, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "HAWB: "+ "", 330,665, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, HAWB[Cont]+ "", 370,665, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, NomeBeneficiario[Cont]+ "", 330,655, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Matrícula: "+ "", 330,645, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Matricula[Cont]+ "", 370,645, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Depto: "+ "",420,645, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, CodDepto[Cont]+"",441,645, 0);
                        cb.setFontAndSize(bfArq3, 5); 
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "   " + NomeDepto[Cont] +"",495,645, 0);
                        //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "ALAMEDA ARAGUAIA Nº 1142"+ "", 330,635, 0);
                        //cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "ALPHAVILLE INDUSTRIA CEP: 06455-000  BARUERI - SP "+ "", 330,625, 0);
                        cb.setFontAndSize(bfArq3, 6); 
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Endereco[Cont]+" ", 330,635, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Complemento[Cont]+ "", 500,635, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, Bairro[Cont]+ "                 "+Cidade[Cont]+ " "+Estado[Cont]+ "", 330,625, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "CEP: " +CEP[Cont]+ "", 330,615, 0);
                    
                        BaseFont bfArqCodigoTransportadora = BaseFont.createFont("C:\\Windows\\Fonts\\ARLRDBD.TTF", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                        cb.setFontAndSize(bfArq3, 4); 
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, NomeTransportadora+"_"+CodigoRegiao[regiao]+"_"+EstadoREgiao[regiao] + "", 500,8, 0);
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,  TipoArquivo+"_"+EstadoREgiao[regiao]+clienteCadastrado.toString().substring(42,46)+" /"+"",543,138, 0);                   
                        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, SequenciaInternaTexto+"",570,138, 0);
                        if (TipoPlastico.equals("0097"))
                        {
                            //BLOQUEADO
                             
                             cb.setFontAndSize(bfArq3,11);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"ATENÇÃO!"+"",55, 445, 0);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"ESTE CARTÃO ESTÁ BLOQUEADO!"+"",55, 435, 0);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"Para desbloqueá-lo, ligue agora para a"+"",55, 425, 0);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"Central de Atendimento Sodexo:"+"",55, 415, 0);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"0800 728 4429"+"",55, 405, 0);
                             cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"SENHA: " + senha[Cont] +"",55, 395, 0);
                        }
                        else 
                            if (TipoPlastico.equals("0098"))
                            {
                                //DESBLOQUEADO
                                
                                cb.setFontAndSize(bfArq3,11);
                                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"ATENÇÃO! "+"",55, 435, 0);
                                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"Para sua comodidade, o seu cartão"+"",55, 425, 0);
                                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"está desbloqueado e pronto para uso."+"",55, 415, 0);
                                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"SENHA: " + senha[Cont] +"",55, 405, 0);                                
                            }
                    }
                    cb.endText();  
                    
                     
                    
                     pw.println(ContTxt + "\t" + ContTxt);  
		     ContTxt = ContTxt + 1;
		     System.out.println("Estou na " + Indice + "ª linha.");
                     SequenciaInterna = SequenciaInterna + 1; 
		     }
		      else
		         {
		         }
		    Indice = Indice + 1;
		   }
		     else if (Indice == linenumber){
		     Indice = Indice + 1;
		     ContTxt = ContTxt + 1;
		     System.out.println("Estou na última linha. Rodapé do arquivo. " + + Indice);
		     //System.out.println("Estou na linha: " + Indice);
                     } 
              
                   
      }
       br1.close();
       if (TemPaginas == true)
                {
                  document.close();  
                  pw.close(); 
                  TemPaginas = false;
                  
                  File ArqLog = new File ("C:\\Sodexo\\Log\\DtHrRecebimento.txt");
                  if (ArqLog.exists())
                        {
                            String path = "C:\\Sodexo\\Log\\DtHrRecebimento.txt";
                    
                            File ArquivoLido = new File(path);
                            FileOutputStream novaLinha = new FileOutputStream(ArquivoLido, true);
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date dataProcessamento = new Date();
                                                
                            String texto = "O arquivo de carta berço: "  + Carta+MigracaoLog+CartaBerco+"_"+TipoArquivo+"_"+EstadoREgiao[regiao]+clienteCadastrado.toString().substring(42,46)+"_"+data_atual.format(date)+"_"+CodigoTransportadora[ContCartaBercoPorTransportadora]+"_"+TransportadoraLog+".pdf"+"\t" +", foi criado com sucesso."+"\t"+"Data e Hora do processo de criação: "+" "+dataProcessamento+"\n"; 
                            novaLinha.write(texto.getBytes());
                            novaLinha.close();
                        }
                  
               }
        
         regiao = regiao + 1;
          //System.out.println("Valor do indice da região: " + regiao);     
         }
        
       // ContCartaBercoPorTransportadora = ContCartaBercoPorTransportadora + 1;
        
        //}
          
       }
       
   }
     }  
   
}

