/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import fachada.HistSaidaProdutoMP;
import java.text.Normalizer;
import java.util.ArrayList;
import persistencia.HistSaidaMPMySQL;

/**
 *
 * @author Rafael
 */
public class HistSaidaProdutoMPController {

    private String[] colunasMes;
    private ArrayList<String[]> listaMes;
    private ArrayList<HistSaidaProdutoMP> listaDatas;
    private HistSaidaProdutoMP hist = new HistSaidaProdutoMP();
    private ArrayList<HistSaidaProdutoMP> lista = new ArrayList<HistSaidaProdutoMP>();

    public void buscar(int destino) {
        lista = new ArrayList<HistSaidaProdutoMP>();
        HistSaidaMPMySQL c = new HistSaidaMPMySQL();
        if (destino == 0) {
            lista = c.buscaHistorico();
        } else {
            lista = c.buscaHistoricoestino(destino);
        }
    }

    public void buscarHistoricoSomadoDia(String data) {
        HistSaidaMPMySQL c = new HistSaidaMPMySQL();
        lista = c.buscaHistorico();
        for (int i = 0; i < lista.size(); i++) {
            double total = 0;
            total = Double.parseDouble(lista.get(i).getQnt().replace(",", ".")) * Double.parseDouble(lista.get(i).getPreco().replace(",", "."));
            for (int j = i + 1; j < lista.size(); j++) {
                if (dataToInt(lista.get(i).getData()) == dataToInt(data) && dataToInt(lista.get(j).getData()) == dataToInt(data)) {
                    if (lista.get(i).getIdProd() == lista.get(j).getIdProd() && lista.get(i).getIdDest() == lista.get(j).getIdDest()) {
                        lista.get(i).setQnt(String.valueOf(Double.parseDouble(lista.get(i).getQnt().replace(",", ".")) + Double.parseDouble(lista.get(j).getQnt().replace(",", "."))));
                        total += Double.parseDouble(lista.get(j).getQnt().replace(",", ".")) * Double.parseDouble(lista.get(j).getPreco().replace(",", "."));
                        lista.remove(j);
                        j--;
                    }
                }
            }
            lista.get(i).setPreco(String.valueOf(total));
        }
    }

    public void getHistoricoDia(String dia, int categoria) {
        listaDatas = new ArrayList<HistSaidaProdutoMP>();
        if (categoria == 0) {//TODOS
            for (int i = 0; i < this.lista.size(); i++) {
                if (dataToInt(this.lista.get(i).getData()) == dataToInt(dia)) {
                    listaDatas.add(this.lista.get(i));
                }
            }
        } else if (categoria == 1) {//CS
            for (int i = 0; i < this.lista.size(); i++) {
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(this.lista.get(i).getData()) == dataToInt(dia)) {
                    if (c == 1) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        } else if (categoria == 2) {//CR
            for (int i = 0; i < this.lista.size(); i++) {
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(this.lista.get(i).getData()) == dataToInt(dia)) {
                    if (c == 2) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        } else if (categoria == 3) {//B
            for (int i = 0; i < this.lista.size(); i++) {
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(this.lista.get(i).getData()) == dataToInt(dia)) {
                    if (c == 3) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        }
    }

    public void buscarHistoricoSomadoPeriodo(String dataDe, String dataAte) {
        HistSaidaMPMySQL c = new HistSaidaMPMySQL();
        lista = c.buscaHistorico();
        for (int i = 0; i < lista.size(); i++) {
            double total = 0;
            total = Double.parseDouble(lista.get(i).getQnt().replace(",", ".")) * Double.parseDouble(lista.get(i).getPreco().replace(",", "."));
            for (int j = i + 1; j < lista.size(); j++) {
                if ((dataToInt(lista.get(i).getData()) >= dataToInt(dataDe)) && (dataToInt(lista.get(i).getData())) <= dataToInt(dataAte) && (dataToInt(lista.get(j).getData()) >= dataToInt(dataDe)) && (dataToInt(lista.get(j).getData())) <= dataToInt(dataAte)) {
                    if (lista.get(i).getIdProd() == lista.get(j).getIdProd() && lista.get(i).getIdDest() == lista.get(j).getIdDest()) {
                        lista.get(i).setQnt(String.valueOf(Double.parseDouble(lista.get(i).getQnt().replace(",", ".")) + Double.parseDouble(lista.get(j).getQnt().replace(",", "."))));
                        total += Double.parseDouble(lista.get(j).getQnt().replace(",", ".")) * Double.parseDouble(lista.get(j).getPreco().replace(",", "."));
                        lista.remove(j);
                        j--;
                    }
                }
            }
            lista.get(i).setPreco(String.valueOf(total));
        }
    }

    public void getHistoricoData(String dataDE, String dataATE, int categoria) {
        listaDatas = new ArrayList<HistSaidaProdutoMP>();
        if (categoria == 0) {//TODOS
            for (int i = 0; i < this.lista.size(); i++) {
                String d = this.lista.get(i).getData();
                if (dataToInt(d) >= dataToInt(dataDE) && dataToInt(d) <= dataToInt(dataATE)) {
                    listaDatas.add(this.lista.get(i));
                }
            }
        } else if (categoria == 1) {//CS
            for (int i = 0; i < this.lista.size(); i++) {
                String d = this.lista.get(i).getData();
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(d) >= dataToInt(dataDE) && dataToInt(d) <= dataToInt(dataATE)) {
                    if (c == 1) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        } else if (categoria == 2) {//CR
            for (int i = 0; i < this.lista.size(); i++) {
                String d = this.lista.get(i).getData();
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(d) >= dataToInt(dataDE) && dataToInt(d) <= dataToInt(dataATE)) {
                    if (c == 2) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        } else if (categoria == 3) {//B
            for (int i = 0; i < this.lista.size(); i++) {
                String d = this.lista.get(i).getData();
                int c = this.lista.get(i).getIdDest();
                if (dataToInt(d) >= dataToInt(dataDE) && dataToInt(d) <= dataToInt(dataATE)) {
                    if (c == 3) {
                        listaDatas.add(this.lista.get(i));
                    }
                }
            }
        }
    }

    public ArrayList<HistSaidaProdutoMP> buscaDimamicaData(String busca) {
        String desc2 = busca;
        desc2 = Normalizer.normalize(desc2, Normalizer.Form.NFD);
        desc2 = desc2.replaceAll("[^\\p{ASCII}]", "");
        desc2 = desc2.toUpperCase();
        ProdutoController p = new ProdutoController();
        p.buscarProdutosTotal();
        ArrayList<HistSaidaProdutoMP> retorno = new ArrayList<HistSaidaProdutoMP>();
        for (int i = 0; i < listaDatas.size(); i++) {
            p.getProduto(listaDatas.get(i).getIdProd());
            String comp = p.getProduto().getNome();
            comp = Normalizer.normalize(comp, Normalizer.Form.NFD);
            comp = comp.replaceAll("[^\\p{ASCII}]", "");
            comp = comp.toUpperCase();
            if (comp.contains(desc2)) {
                retorno.add(listaDatas.get(i));
            }
        }
        return retorno;
    }

    public void colunasHistoricoMes(int mes, int ano, int destino) {
        buscar(destino);
        double qnt = 0;
        ArrayList<String> r = new ArrayList<String>();
        String m = String.valueOf(mes);
        if (m.length() < 2) {
            m = "0" + m;
        }
        String inicio = ano + "" + m + "00";
        String fim = ano + "" + m + "32";
        int begin = Integer.parseInt(inicio);
        int end = Integer.parseInt(fim);
        for (int i = 0; i < lista.size(); i++) {
            int data = dataToInt(lista.get(i).getData());
            if (data > begin && data < end) {
                String aux = lista.get(i).getData().substring(0, 5);
                if (!r.contains(aux)) {
                    r.add(aux);
                }
            }
        }
        listaMes = new ArrayList<String[]>();
        colunasMes = new String[r.size() + 3];
        colunasMes[0] = "Produto";
        for (int i = 0; i < r.size(); i++) {
            colunasMes[i + 1] = r.get(i);
        }
        colunasMes[colunasMes.length - 2] = "Total";
        colunasMes[colunasMes.length - 1] = "Custo";
        if (r.size() > 0) {
            ProdutoController prod = new ProdutoController();
            prod.buscarProdutosSaida();
            for (int i = 0; i < prod.getListProdutos().size(); i++) {
                if (i == 15) {
                    System.out.println("");
                }
                String[] linha;
                linha = new String[colunasMes.length];
                linha[0] = prod.getListProdutos().get(i).getNome();
                double total = 0;
                double valor = 0;
                for (int j = 1; j < colunasMes.length; j++) {
                    qnt = 0;
                    for (int k = 0; k < lista.size(); k++) {
                        if (lista.get(k).getData().contains(colunasMes[j]) && lista.get(k).getIdProd() == prod.getListProdutos().get(i).getIdProduto()) {
                            qnt += Double.parseDouble(lista.get(k).getQnt().replace(",", "."));
                            valor += Double.parseDouble(lista.get(k).getPreco().replace(",", ".")) * Double.parseDouble(lista.get(k).getQnt().replace(",", "."));
                        }
                    }
                    total += qnt;
                    linha[j] = String.valueOf(qnt);
                }
                linha[colunasMes.length - 2] = String.valueOf(total);
                linha[colunasMes.length - 1] = String.valueOf(valor);
                if (total > 0) {
                    listaMes.add(linha);
                }
            }
        }
    }

    public ArrayList<String[]> buscaDinamicaMes(String busca) {
        String desc2 = busca;
        desc2 = Normalizer.normalize(desc2, Normalizer.Form.NFD);
        desc2 = desc2.replaceAll("[^\\p{ASCII}]", "");
        desc2 = desc2.toUpperCase();
        ArrayList<String[]> retorno = new ArrayList<String[]>();
        for (int i = 0; i < listaMes.size(); i++) {
            String comp = listaMes.get(i)[0];
            comp = Normalizer.normalize(comp, Normalizer.Form.NFD);
            comp = comp.replaceAll("[^\\p{ASCII}]", "");
            comp = comp.toUpperCase();
            if (comp.contains(desc2)) {
                retorno.add(listaMes.get(i));
            }
        }
        return retorno;
    }

    public int dataToInt(String data) {
        String aux[] = data.split("/");
        String d = String.valueOf(aux[0]);
        String m = String.valueOf(aux[1]);
        if (d.length() < 2) {
            d = "0" + d;
        }
        if (m.length() < 2) {
            m = "0" + m;
        }
        String date = aux[2] + m + d;
        return Integer.parseInt(date);
    }

    public void excluir(int i) {
        lista.remove(i);
    }

    public void editar(HistSaidaProdutoMP h, int row) {
        lista.set(row, h);
    }

    public HistSaidaProdutoMP getHist() {
        return hist;
    }

    public void setHist(HistSaidaProdutoMP hist) {
        this.hist = hist;
    }

    public ArrayList<HistSaidaProdutoMP> getLista() {
        return lista;
    }

    public void setLista(ArrayList<HistSaidaProdutoMP> lista) {
        this.lista = lista;
    }

    public String[] getColunasMes() {
        return colunasMes;
    }

    public void setColunasMes(String[] colunasMes) {
        this.colunasMes = colunasMes;
    }

    public ArrayList<String[]> getListaMes() {
        return listaMes;
    }

    public void setListaMes(ArrayList<String[]> listaMes) {
        this.listaMes = listaMes;
    }

    public ArrayList<HistSaidaProdutoMP> getListaDatas() {
        return listaDatas;
    }

    public void setListaDatas(ArrayList<HistSaidaProdutoMP> listaDatas) {
        this.listaDatas = listaDatas;
    }
}

