/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.jsensors.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import dev.jsensors.exceptions.RSSFException;
import dev.jsensors.sensores.Periodo;
import dev.jsensors.sensores.RedeSensores;
import dev.jsensors.sensores.Sensor;
import dev.jsensors.sensores.Sink;

/**
 *
 * @author Ismael
 */
public class RedeHandler {

    private final int QTD_DADOS_MINIMO_SENSORES = 6;
    private File arquivoEstadoInicial, arquivoPeriodos;

    public RedeHandler(File arquivoEstadoInicial, File arquivoPeriodos) {
        this.arquivoEstadoInicial = arquivoEstadoInicial;
        this.arquivoPeriodos = arquivoPeriodos;
    }

    public static void iniciarRede(RedeSensores rede) throws RSSFException {
        rede.calculaEnergia_Inicial();
        /*
         * É preciso simular todos os periodos para que as coberturas de cada um seja calculada.
         * Sem isso se o usuário pular vários periodos na simulação o gráfico de cobertura não
         * será preenchido corretamente.
         */
        /*for (int i = 0; i < rede.getPeriodos().size(); i++) {
        rede.andarPeriodos(1);
        rede.getPeriodos().get(rede.getPeriodo_atual()).atualizarCoberturas();
        }
        rede.setPeriodo_atual(-1);
        rede.andarPeriodos(1);*/
    }

    public RedeSensores construirRede() throws RSSFException, FileNotFoundException {
        RedeSensores rede = lerDadosIniciais(arquivoEstadoInicial);
        rede.calcularPontosCobertura();
        rede.setPeriodos(lerDadosPeriodos(arquivoPeriodos, rede));
        rede.getPeriodos().add(0, gerarPeriodoInicial(rede));
        iniciarRede(rede);
        return rede;
    }

    private RedeSensores lerDadosIniciais(File file) throws FileNotFoundException, RSSFException, NumberFormatException {
        Scanner scan = new Scanner(file);
        RedeSensores redeSensores = new RedeSensores();
        redeSensores.setTamanho(Integer.parseInt(scan.nextLine()));
        StringTokenizer st = new StringTokenizer(scan.nextLine());
        redeSensores.setSink(new Sink(redeSensores, Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken())));
        for (int i = 0; scan.hasNext(); i++) {
            String linha = scan.nextLine();
            st = new StringTokenizer(linha);
            if (st.countTokens() < QTD_DADOS_MINIMO_SENSORES) {
                Mensagens.erro("Os dados dos sensores devem ser:\nPOS_X POS_Y BATERIA_INICIAL RAIO_COBERTURA RAIO_COMUNICACAO STATUS");
                System.exit(-1);
            }
            Sensor s = criarSensor(redeSensores, st);
            s.setId(i);
            redeSensores.addSensor(s);
        }
        scan.close();
        return redeSensores;
    }

    public static Sensor criarSensor(RedeSensores rede, StringTokenizer st) throws RSSFException, NumberFormatException {
        Sensor s = new Sensor(rede);
        s.setX(Double.parseDouble(st.nextToken()));
        s.setY(Double.parseDouble(st.nextToken()));
        s.setBateria(Double.parseDouble(st.nextToken()));
        s.setRaio_cobertura(Double.parseDouble(st.nextToken()));
        s.setRaio_comunicacao(Double.parseDouble(st.nextToken()));
        s.setStatus(Byte.parseByte(st.nextToken()));
        while (st.hasMoreTokens()) {
            int id = Integer.parseInt(st.nextToken());
            if (id >= 0) {
                s.getSConectados().add(id);
            }
        }
        return s;
    }

    private List<Periodo> lerDadosPeriodos(File file, RedeSensores rede) throws FileNotFoundException, NumberFormatException {
        Scanner scan = new Scanner(file);
        List<Periodo> periodos = new ArrayList<Periodo>();
        while (scan.hasNextLine()) {
            String linha = scan.nextLine();
            int estagio = Integer.parseInt(linha);
            linha = scan.nextLine();
            double cobertura = Double.parseDouble(linha);
            List<List<Integer>> conectados = new ArrayList<List<Integer>>();
            double[][] dadosPeriodo = gerarDadosPeriodo(scan, conectados, rede.getSensores().size());
            Periodo novo = new Periodo(rede, estagio, dadosPeriodo, conectados, cobertura);
            periodos.add(novo);
        }
        scan.close();
        return periodos;
    }

    private double[][] gerarDadosPeriodo(Scanner scan, List<List<Integer>> conectados, int qtdSensores) {
        String linha = scan.nextLine();
        int cont = 0;
        double[][] dados = new double[qtdSensores][2];
        while (!linha.equals("")) {
            StringTokenizer st = new StringTokenizer(linha);
            dados[cont][0] = Double.parseDouble(st.nextToken());
            dados[cont][1] = Double.parseDouble(st.nextToken());
            List<Integer> conects = new ArrayList<Integer>();
            while (st.hasMoreTokens()) {
                int id = Integer.parseInt(st.nextToken());
                if (id >= 0) {
                    conects.add(id);
                }
            }
            conectados.add(conects);
            if (scan.hasNextLine()) {
                linha = scan.nextLine();
                cont++;
            } else {
                break;
            }
        }
        return dados;
    }

    private Periodo gerarPeriodoInicial(RedeSensores rede) {
        double[][] dados = new double[rede.getSensores().size()][2];
        List<List<Integer>> conectados = new ArrayList<List<Integer>>();
        int i = 0;
        for (Sensor s : rede.getSensores()) {
            dados[i][0] = s.getStatus();
            dados[i][1] = s.getBateria();
            conectados.add(s.getSConectados());
            i++;
        }
        Periodo novo = new Periodo(rede, 0, dados, conectados, 0);
        //novo.atualizarCoberturas();
        return novo;
    }
}

