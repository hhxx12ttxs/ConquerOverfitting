/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dao.ArvoreAjusteDao;
import dao.CoordenadaLocalDao;
import dao.EquacaoDao;
import dao.MunicipioLocalDao;
import dao.ParcelaDao;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 *
 * @author jaime
 */
public class Local extends Model  {
    
    
    private int id;
    private String descricao;
    private double area;
    private double areaParcela;
    private int idTipoEstimativa;
    private int idTipoFloresta;
    private int idDmTipoDistancia;
    private int idDmTipoPonderacao;    
    private int dmQtdeVizinhos;
    private boolean dmComLn;
    private double qtdeBiomassaEquacao;   
    private double qtdeBiomassaDm;   
    private double qtdeCarbonoEquacao;   
    private double qtdeCarbonoDm;   
    private double qtdeVolumeEquacao;   
    private double qtdeVolumeDm;
    private int idMunicipio;
    private double latitude;
    private double longitude;
    private int idFormacao;
    private int idEspacamento;
    private int idEspecie;
    private int idade;
    
    private double qtde;
    
    public ArrayList<Parcela> parcelas;
    public ArrayList<ArvoreAjuste> arvoresAjuste;
    
    public Local()
    {
        this.descricao = "";
        this.area = 0.0;
        this.areaParcela = 0.0;
        this.idTipoEstimativa = 0;
        this.idTipoFloresta = 0;
        this.idDmTipoDistancia = 0;
        this.idDmTipoPonderacao = 0;
        this.dmQtdeVizinhos = 0;
        this.dmComLn = false;
        this.qtdeBiomassaEquacao = 0.0;        
        this.qtdeBiomassaDm = 0.0;        
        this.qtdeCarbonoEquacao = 0.0;        
        this.qtdeCarbonoDm = 0.0;        
        this.qtdeVolumeEquacao = 0.0;        
        this.qtdeVolumeDm = 0.0;          
        this.idMunicipio = 0;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.idFormacao = 0;
        this.idEspacamento = 0;
        this.idEspecie = 0;
        this.idade = 0;

        this.qtde = 0.0;
    }
    
    public String getIdString()
    {
        return String.valueOf(this.getId());
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAreaParcela() {
        return areaParcela;
    }

    public void setAreaParcela(double areaParcela) {
        this.areaParcela = areaParcela;
    }

    public int getIdTipoEstimativa() {
        return idTipoEstimativa;
    }

    public void setIdTipoEstimativa(int idTipoEstimativa) {
        this.idTipoEstimativa = idTipoEstimativa;
    }

    public int getIdFormacao() {
        return idFormacao;
    }

    public void setIdFormacao(int idFormacao) {
        this.idFormacao = idFormacao;
    }

    public int getIdEspacamento() {
        return idEspacamento;
    }

    public void setIdEspacamento(int idEspacamento) {
        this.idEspacamento = idEspacamento;
    }

    public int getIdDmTipoDistancia() {
        return idDmTipoDistancia;
    }

    public void setIdDmTipoDistancia(int idDmTipoDistancia) {
        this.idDmTipoDistancia = idDmTipoDistancia;
    }

    public int getIdDmTipoPonderacao() {
        return idDmTipoPonderacao;
    }

    public void setIdDmTipoPonderacao(int idDmTipoPonderacao) {
        this.idDmTipoPonderacao = idDmTipoPonderacao;
    }

    public int getDmQtdeVizinhos() {
        return dmQtdeVizinhos;
    }

    public void setDmQtdeVizinhos(int dmQtdeVizinhos) {
        this.dmQtdeVizinhos = dmQtdeVizinhos;
    }

    public boolean isDmComLn() {
        return dmComLn;
    }

    public void setDmComLn(boolean dmComLn) {
        this.dmComLn = dmComLn;
    }

    public double getQtdeBiomassaEquacao() {
        return qtdeBiomassaEquacao;
    }

    public void setQtdeBiomassaEquacao(double qtdeBiomassaEquacao) {
        this.qtdeBiomassaEquacao = qtdeBiomassaEquacao;
    }

    public double getQtdeBiomassaDm() {
        return qtdeBiomassaDm;
    }

    public void setQtdeBiomassaDm(double qtdeBiomassaDm) {
        this.qtdeBiomassaDm = qtdeBiomassaDm;
    }

    public double getQtdeCarbonoEquacao() {
        return qtdeCarbonoEquacao;
    }

    public void setQtdeCarbonoEquacao(double qtdeCarbonoEquacao) {
        this.qtdeCarbonoEquacao = qtdeCarbonoEquacao;
    }

    public double getQtdeCarbonoDm() {
        return qtdeCarbonoDm;
    }

    public void setQtdeCarbonoDm(double qtdeCarbonoDm) {
        this.qtdeCarbonoDm = qtdeCarbonoDm;
    }

    public double getQtdeVolumeEquacao() {
        return qtdeVolumeEquacao;
    }

    public void setQtdeVolumeEquacao(double qtdeVolumeEquacao) {
        this.qtdeVolumeEquacao = qtdeVolumeEquacao;
    }

    public double getQtdeVolumeDm() {
        return qtdeVolumeDm;
    }

    public void setQtdeVolumeDm(double qtdeVolumeDm) {
        this.qtdeVolumeDm = qtdeVolumeDm;
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getIdTipoFloresta() {
        return idTipoFloresta;
    }

    public void setIdTipoFloresta(int idTipoFloresta) {
        this.idTipoFloresta = idTipoFloresta;
    }

    public int getIdEspecie() {
        return idEspecie;
    }

    public void setIdEspecie(int idEspecie) {
        this.idEspecie = idEspecie;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public double getQtde(int idVariavelInteresse, int idMetodoCalculo) {
        
        if (idMetodoCalculo==1) { //Equacao
            switch (idVariavelInteresse) {
                case 1:
                    this.qtde = this.qtdeBiomassaEquacao;
                    break;
                case 2:
                    this.qtde = this.qtdeCarbonoEquacao;
                    break;
                case 3:
                    this.qtde = this.qtdeVolumeEquacao;
                    break;
            }                            
        } else {// DM
            switch (idVariavelInteresse) {
                case 1:
                    this.qtde = this.qtdeBiomassaDm;
                    break;
                case 2:
                    this.qtde = this.qtdeCarbonoDm;
                    break;
                case 3:
                    this.qtde = this.qtdeVolumeDm;
                    break;
            }                            
        }
        
        return qtde;
    }

    public void setQtde(double qtde,int idVariavelInteresse, int idMetodoCalculo) {
        
        if (idMetodoCalculo==1) { //Equacao
            switch (idVariavelInteresse) {
                case 1:
                    this.qtdeBiomassaEquacao = qtde;
                    break;
                case 2:
                    this.qtdeCarbonoEquacao = qtde;
                    break;
                case 3:
                    this.qtdeVolumeEquacao = qtde;
                    break;
            }                            
        } else {// DM
            switch (idVariavelInteresse) {
                case 1:
                    this.qtdeBiomassaDm = qtde;
                    break;
                case 2:
                    this.qtdeCarbonoDm = qtde;
                    break;
                case 3:
                    this.qtdeVolumeDm = qtde;
                    break;
            }                            
        }         
    }

    public ArrayList<ArvoreAjuste> getArvoresAjuste() {
        return arvoresAjuste;
    }

    public void setArvoresAjuste(ArrayList<ArvoreAjuste> arvoresAjuste) {
        this.arvoresAjuste = arvoresAjuste;
    }
    
    public ArrayList<Equacao> getEquacoesLocal() throws SQLException, Exception {
        
        EquacaoDao equacaoDao = new EquacaoDao();
        ArrayList<Equacao> equacoesLocal = new ArrayList<Equacao>();
        equacoesLocal = equacaoDao.getEquacoesLocal(id);
        
        return equacoesLocal;
    }

    public ArrayList<Parcela> getParcelas() throws Exception {

        ParcelaDao parcelaDao = new ParcelaDao();
        parcelas = parcelaDao.listarParcelas(this.id);

        for (Parcela parcela: parcelas) {
            parcela.arvores = parcela.getArvores();
        }
        
        return parcelas;
    }

    public void setParcelas(ArrayList<Parcela> parcelas) {
        this.parcelas = parcelas;
    }

    public ArrayList<ArvoreAjuste> getArvoresAjuste(int idVariavelInteresse,int idMetodoCalculo) throws Exception {
        ArrayList<ArvoreAjuste> arvoresAjuste = new ArrayList<ArvoreAjuste>();
        ArvoreAjusteDao arvoreAjusteDao = new ArvoreAjusteDao();
        
        arvoresAjuste = arvoreAjusteDao.listarArvoresAjuste(this.id);

        return arvoresAjuste;
    }

    public void setArvoreAjuste(ArrayList<ArvoreAjuste> arvoresAjuste) {
        this.arvoresAjuste = arvoresAjuste;
    }
    
    
    public boolean eh_valido()
    {
        if(this.getDescricao() == null || this.getDescricao().isEmpty())
        {
            this.setErro("Descrição ", "não pode ficar em branco");
        }
        if(this.getArea() == 0)
        {
            this.setErro("Área ", "deve ser informada");
        }        
//        if(this.getAreaParcela() == 0)
//        {
//            this.setErro("Área da Parcela ", "deve ser informada");
//        }                
        if(this.getIdFormacao() == 0)
        {
            this.setErro("Formação ", "deve ser informada");
        }                
        if(this.getIdEspacamento() == 0)
        {
            this.setErro("Espaçamento ", "deve ser informado");
        }                
                        
        return (this.erros.isEmpty());
        
    }
    
    public ArrayList<Error> getErrors()
    {
        return this.erros;
    }

    
}

