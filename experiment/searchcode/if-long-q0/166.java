/*
   Copyright 2011 Paulo Augusto Dacach Bichara

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.paulobichara.afdafn.view;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.paulobichara.afdafn.controller.AutomatoAFN;
import org.paulobichara.afdafn.controller.TransicaoAFD;
import org.paulobichara.afdafn.controller.TransicaoAFN;
import org.paulobichara.afdafn.model.Estado;
import org.paulobichara.afdafn.model.Simbolo;

public class InterfaceAFNAFD extends JFrame implements ActionListener {
  /**
	 * 
	 */
	private static final long serialVersionUID = -9056122448661316706L;
JTabbedPane jTabbedPane1 = new JTabbedPane();
  JPanel jPanelCriarAFN = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextField jTextFieldNumEsts = new JTextField();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JTextField jTextFieldSimbolo = new JTextField();
  JButton jButtonOK = new JButton();
  JButton jButtonConcluirSim = new JButton();
  JButton jButtonIncluirSim = new JButton();
  JLabel jLabel4 = new JLabel();
  JComboBox jComboBoxOrigem = new JComboBox();
  JLabel jLabel5 = new JLabel();
  JComboBox jComboBoxDestino = new JComboBox();
  JComboBox jComboBoxString = new JComboBox();
  JLabel jLabel6 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel jLabel8 = new JLabel();
  JButton jButtonIncluirTrans = new JButton();
  JButton jButtonConcluirTrans = new JButton();
  JLabel jLabel9 = new JLabel();
  JComboBox jComboBoxFinais = new JComboBox();
  JButton jButtonDefinirFinal = new JButton();
  JLabel jLabel10 = new JLabel();
  JPanel jPanelMostrarAFN = new JPanel();
  JLabel jLabel11 = new JLabel();
  JLabel jLabel13 = new JLabel();
  JLabel jLabel14 = new JLabel();
  JLabel jLabelEstInicAFN = new JLabel();
  JLabel jLabel17 = new JLabel();
  TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  TitledBorder titledBorder3;
  TitledBorder titledBorder4;
  JButton jButtonAFNAFD = new JButton();
  JLabel jLabel19 = new JLabel();
  TitledBorder titledBorder5;
  TitledBorder titledBorder6;
  JLabel jLabel116 = new JLabel();
  JLabel jLabelEstInicAFD = new JLabel();
  JLabel jLabel115 = new JLabel();
  JLabel jLabel110 = new JLabel();
  JLabel jLabel114 = new JLabel();
  JLabel jLabel112 = new JLabel();
  JPanel jPanelMostrarAFD = new JPanel();
  JButton jButtonFinalizar = new JButton();
  AutomatoAFN automatoAFN;
  JLabel jLabel12 = new JLabel();
  JButton jButtonConcluirFinais = new JButton();
  JTextArea jTextArea1 = new JTextArea();
  JComboBox jComboBoxTransAFN = new JComboBox();
  JComboBox jComboBoxTransAFD = new JComboBox();
  JComboBox jComboBoxEstsAFN = new JComboBox();
  JComboBox jComboBoxAlfAFN = new JComboBox();
  JComboBox jComboBoxEstsFinaisAFN = new JComboBox();
  JComboBox jComboBoxEstsAFD = new JComboBox();
  JComboBox jComboBoxEstsFinaisAFD = new JComboBox();

  public void actionPerformed(ActionEvent aeEvento){
    Object obSource = aeEvento.getSource();

    if (obSource == jButtonOK) {    // CONFIRMAКAO DO NUMERO DE ESTADOS
      int i = 0;
      ArrayList estados;
      String rotulo, num = jTextFieldNumEsts.getText();

      if ((num.equals("") == false) && (num.equals("0") == false)) {
        jTextFieldNumEsts.setEnabled(false);
        jButtonOK.setEnabled(false);
        int numEsts = Integer.valueOf(jTextFieldNumEsts.getText()).intValue();
        automatoAFN.criarEstados(numEsts);
        estados = automatoAFN.getEstados();
        do {
          rotulo = ((Estado)estados.get(i)).getRotulo();
          jComboBoxFinais.addItem(rotulo);
          jComboBoxOrigem.addItem(rotulo);
          jComboBoxDestino.addItem(rotulo);
          i++;
        } while (i < estados.size());
      } else {
        JOptionPane.showMessageDialog(null, "Entrada invрlida --> Tente Novamente!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
        jTextFieldNumEsts.setText("");
      }
    } else {
      if (obSource == jButtonConcluirTrans) { // CONCLUI INCLUSAO DE TRANSICOES
        int i = 0;                            // ENCERRA CRIACAO DO AFN
        Estado orig, dest;                    // EXIBE O AFN NO PANEL MOSTRAR AFN
        Simbolo simb;
        TransicaoAFN transicao;
        ArrayList estados, alfabeto, transicoes;
        String rotulo, simbolo = new String(), tipo, ests = new String();
        String trans = new String();

        jTabbedPane1.setSelectedComponent(jPanelMostrarAFN);
        estados = automatoAFN.getEstados();
        trans = "  ";
        transicoes = automatoAFN.getTransicoes();
        alfabeto = automatoAFN.getAlfabeto();
        rotulo = " " + ((Estado)estados.get(0)).getRotulo();
        jLabelEstInicAFN.setText(rotulo);
        rotulo = "";
        do {
          rotulo = ((Estado)estados.get(i)).getRotulo();
          tipo = ((Estado)estados.get(i)).getTipo();
          if (tipo.equals("Final")) {
            jComboBoxEstsFinaisAFN.addItem(rotulo);
          }
          jComboBoxEstsAFN.addItem(rotulo);
          rotulo = "";
          i++;
        } while (i < estados.size());
        i = 0;
        do {
          simbolo = ((Simbolo)alfabeto.get(i)).getSimbolo() + " ";
          jComboBoxAlfAFN.addItem(simbolo);
          simbolo = "";
          i++;
        } while (i < alfabeto.size());
        i = 0;
        for (i = 0; i < transicoes.size(); i++) {
          transicao = (TransicaoAFN)transicoes.get(i);
          orig = transicao.getOrigem();
          dest = transicao.getDestino();
          simb = transicao.getSimbolo();
          trans = orig.getRotulo() + " --> " + dest.getRotulo() + "  { " + simb.getSimbolo() + " }";
          jComboBoxTransAFN.addItem(trans);
        }
      } else {
        if (obSource == jButtonDefinirFinal) {  // BOTAO PARA DEFINIR O ESTADO COMO FINAL
          String rotulo = (String)jComboBoxFinais.getSelectedItem();
          if (rotulo != null) {
            automatoAFN.setEstadoFinal(rotulo);
            JOptionPane.showMessageDialog(null, rotulo + " foi definido como final!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
          } else {
            JOptionPane.showMessageDialog(null, "Nenhum estado foi escolhido! Tente outra vez!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
          }
        } else {
          if (obSource == jButtonConcluirFinais) {  // BOTAO PARA ENCERRAR DEFINICAO DE FINAIS
            int i = 0;
            ArrayList estados;
            String tipo;
            boolean existeFinal = false;
            estados = automatoAFN.getEstados();
            for (i = 0; i < estados.size(); i++) {
              tipo = ((Estado)estados.get(i)).getTipo();
              if (tipo.equals("Final")) {
                existeFinal = true;
              }
            }
            if (existeFinal == true) {
              jComboBoxFinais.setEnabled(false);
              jButtonDefinirFinal.setEnabled(false);
              jButtonConcluirFinais.setEnabled(false);
            } else {
              JOptionPane.showMessageDialog(null, "Nenhum estado final foi definido! Tente outra vez!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
            }
          } else {
            if (obSource == jButtonIncluirSim) {   // DEFINE O ALFABETO SIMBOLO A SIMBOLO
              String simbolo = jTextFieldSimbolo.getText();
              if (simbolo.equals("") == false) {
                if (automatoAFN.checaSimbolo(simbolo) == false) {
                  automatoAFN.addSimbolo(simbolo);
                  jComboBoxString.addItem(simbolo);
                  JOptionPane.showMessageDialog(null, "O sьmbolo " + simbolo + " foi inserido!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
                  jTextFieldSimbolo.setText("");
                } else {
                  JOptionPane.showMessageDialog(null, "Sьmbolo jр inserido!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
                  jTextFieldSimbolo.setText("");
                }
              } else {
                  JOptionPane.showMessageDialog(null,
                      "Entrada invрlida --> Tente Novamente!", "AVISO",
                                                JOptionPane.INFORMATION_MESSAGE);
                  jTextFieldSimbolo.setText("");
              }
            } else {
              if (obSource == jButtonConcluirSim) {  // CONCLUI A DEFINICAO DO ALFABETO
                int i = 0;
                ArrayList simbolos = automatoAFN.getAlfabeto();
                if (simbolos.size() > 0) {
                  jTextFieldSimbolo.setEditable(false);
                  jButtonIncluirSim.setEnabled(false);
                  jButtonConcluirSim.setEnabled(false);
                  jComboBoxOrigem.setEnabled(true);
                  jComboBoxDestino.setEnabled(true);
                  jComboBoxString.setEnabled(true);
                  jButtonIncluirTrans.setEnabled(true);
                  jButtonConcluirTrans.setEnabled(true);
                } else {
                  JOptionPane.showMessageDialog(null, "Nenhum sьmbolo foi definido! Tente outra vez!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
                }
              } else {
                if (obSource == jButtonIncluirTrans) { // INCLUI NO AFN A TRANSICAO ESPECIFICADA
                  String estOrigem, estDestino, simbolo, mensagem = new String();
                  boolean incluiu;

                  estOrigem = (String)jComboBoxOrigem.getSelectedItem();
                  estDestino = (String)jComboBoxDestino.getSelectedItem();
                  simbolo = (String)jComboBoxString.getSelectedItem();
                  incluiu = automatoAFN.addTransicao(estOrigem, estDestino, simbolo);
                  if (incluiu == true) {
                    mensagem = "Transiусo " + estOrigem + " --> " + estDestino + " {" + simbolo + " } inserida!";
                    JOptionPane.showMessageDialog(null, mensagem, "AVISO", JOptionPane.INFORMATION_MESSAGE);
                  } else {
                    JOptionPane.showMessageDialog(null, "Transiуao jр inserida!", "AVISO", JOptionPane.INFORMATION_MESSAGE);
                  }
                } else {
                  if (obSource == jButtonAFNAFD) {  // TRANFORMA AFN EM AFD E MOSTRA O AFD
                    String rotulo = new String(), msgTransicao = new String(), origem = new String(), destino = new String(), simbolo = new String();
                    ArrayList estadosAFD, estadoAFD, estOrig, estDest, transicoes;
                    Estado estadoAFN;
                    TransicaoAFD trans;
                    boolean ehFinal = false;

                    automatoAFN.transformarAFD();
                    estadosAFD = automatoAFN.getEstadosAFD();
                    transicoes = automatoAFN.getTransicoesAFD();
                    for (int i = 0; i < estadosAFD.size(); i++) {
                      estadoAFD = (ArrayList)estadosAFD.get(i);
                      for (int j = 0; j < estadoAFD.size(); j++) {
                        estadoAFN = (Estado)estadoAFD.get(j);
                        rotulo = rotulo + estadoAFN.getRotulo();
                        if (estadoAFN.getTipo() == "Final") {
                          ehFinal = true;
                        }
                      }
                      if (ehFinal == true) {
                        jComboBoxEstsFinaisAFD.addItem(rotulo);
                      }
                      jComboBoxEstsAFD.addItem(rotulo);
                      rotulo = "";
                      ehFinal = false;
                    }

                    for (int i = 0; i < transicoes.size(); i++) {
                      trans = (TransicaoAFD)transicoes.get(i);
                      estOrig = trans.getOrigem();
                      estDest = trans.getDestino();
                      for (int j = 0; j < estOrig.size(); j++) {
                        estadoAFN = (Estado)estOrig.get(j);
                        origem = origem + estadoAFN.getRotulo();
                      }
                      for (int j = 0; j < estDest.size(); j++) {
                        estadoAFN = (Estado)estDest.get(j);
                        destino = destino + estadoAFN.getRotulo();
                      }
                      simbolo = trans.getSimbolo().getSimbolo();
                      msgTransicao = origem + " --> " + destino + "  { " + simbolo + " }";
                      origem = "";
                      destino = "";
                      simbolo = "";
                      jComboBoxTransAFD.addItem(msgTransicao);
                    }

                    jTabbedPane1.setSelectedComponent(jPanelMostrarAFD);
                    jLabelEstInicAFD.setText(((Estado)((ArrayList)((ArrayList)estadosAFD.get(0))).get(0)).getRotulo());
                  } else {
                    if (obSource == jButtonFinalizar) { // ENCERRA O PROGRAMA
                      System.exit(0);
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


  public InterfaceAFNAFD() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    jLabel116.setBounds(new Rectangle(192, 13, 116, 34));
    jLabel116.setText("MOSTRAR AFD");
    jLabel116.setBorder(titledBorder2);
    jLabel116.setForeground(Color.blue);
    jLabel116.setFont(new java.awt.Font("Arial", 1, 14));
    jLabelEstInicAFD.setBounds(new Rectangle(143, 169, 55, 27));
    jLabelEstInicAFD.setText("");
    jLabelEstInicAFD.setFont(new java.awt.Font("Arial", 1, 12));
    jLabelEstInicAFD.setForeground(Color.blue);
    jLabelEstInicAFD.setBorder(BorderFactory.createEtchedBorder());
    jLabel115.setBounds(new Rectangle(15, 171, 94, 27));
    jLabel115.setText("Estado Inicial:");
    jLabel115.setForeground(Color.blue);
    jLabel115.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel110.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel110.setForeground(Color.blue);
    jLabel110.setText("Transiушes:");
    jLabel110.setBounds(new Rectangle(15, 295, 94, 27));
    jLabel114.setBounds(new Rectangle(17, 116, 63, 24));
    jLabel114.setText("Estados:");
    jLabel114.setForeground(Color.blue);
    jLabel114.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel112.setBounds(new Rectangle(15, 225, 116, 27));
    jLabel112.setText("Estado(s) Final(is):");
    jLabel112.setForeground(Color.blue);
    jLabel112.setFont(new java.awt.Font("Arial", 0, 13));
    jPanelMostrarAFD.setLayout(null);
    jPanelMostrarAFD.setAlignmentX((float) 0.5);
    jPanelMostrarAFD.setDebugGraphicsOptions(0);
    jPanelMostrarAFD.setBackground(Color.lightGray);
    jPanelMostrarAFD.setAlignmentY((float) 0.5);
    jButtonFinalizar.setBackground(Color.lightGray);
    jButtonFinalizar.setBounds(new Rectangle(168, 375, 187, 47));
    jButtonFinalizar.setFont(new java.awt.Font("Dialog", 1, 13));
    jButtonFinalizar.setForeground(Color.blue);
    jButtonFinalizar.addActionListener(this);
    jButtonFinalizar.setText("Finalizar");
    jLabel12.setBounds(new Rectangle(15, 124, 63, 24));
    jLabel12.setText("Alfabeto:");
    jLabel12.setForeground(Color.blue);
    jLabel12.setFont(new java.awt.Font("Arial", 0, 13));
    jButtonConcluirFinais.addActionListener(this);
    jButtonConcluirFinais.setBounds(new Rectangle(404, 135, 89, 28));
    jButtonConcluirFinais.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonConcluirFinais.setForeground(Color.blue);
    jButtonConcluirFinais.setDebugGraphicsOptions(0);
    jButtonConcluirFinais.setMaximumSize(new Dimension(47, 25));
    jButtonConcluirFinais.setMinimumSize(new Dimension(51, 27));
    jButtonConcluirFinais.setBorderPainted(true);
    jButtonConcluirFinais.setText("Concluir");
    jLabelEstInicAFN.setFont(new java.awt.Font("Arial", 1, 12));
    jLabelEstInicAFN.setForeground(Color.blue);
    jComboBoxTransAFN.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxTransAFN.setForeground(Color.blue);
    jComboBoxTransAFN.setAutoscrolls(false);
    jComboBoxTransAFN.setBounds(new Rectangle(125, 273, 257, 29));
    jComboBoxTransAFD.setBounds(new Rectangle(144, 294, 257, 29));
    jComboBoxTransAFD.setAutoscrolls(false);
    jComboBoxTransAFD.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxTransAFD.setForeground(Color.blue);
    jComboBoxEstsAFN.setBounds(new Rectangle(128, 77, 176, 26));
    jComboBoxEstsAFN.setAutoscrolls(false);
    jComboBoxEstsAFN.setForeground(Color.blue);
    jComboBoxEstsAFN.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxAlfAFN.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxAlfAFN.setForeground(Color.blue);
    jComboBoxAlfAFN.setAutoscrolls(false);
    jComboBoxAlfAFN.setBounds(new Rectangle(127, 123, 79, 26));
    jComboBoxEstsFinaisAFN.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxEstsFinaisAFN.setForeground(Color.blue);
    jComboBoxEstsFinaisAFN.setAutoscrolls(false);
    jComboBoxEstsFinaisAFN.setBounds(new Rectangle(127, 220, 176, 26));
    jComboBoxEstsAFD.setFont(new java.awt.Font("Arial", 1, 12));
    jComboBoxEstsAFD.setForeground(Color.blue);
    jComboBoxEstsAFD.setAutoscrolls(false);
    jComboBoxEstsAFD.setBounds(new Rectangle(143, 118, 176, 26));
    jComboBoxEstsFinaisAFD.setBounds(new Rectangle(143, 225, 176, 26));
    jComboBoxEstsFinaisAFD.setAutoscrolls(false);
    jComboBoxEstsFinaisAFD.setForeground(Color.blue);
    jComboBoxEstsFinaisAFD.setFont(new java.awt.Font("Arial", 1, 12));
    jPanelMostrarAFN.add(jLabel17, null);
    jPanelMostrarAFN.add(jLabel13, null);
    jPanelMostrarAFN.add(jLabelEstInicAFN, null);
    jPanelMostrarAFN.add(jLabel14, null);
    jPanelMostrarAFN.add(jLabel11, null);
    jPanelMostrarAFN.add(jLabel12, null);
    jPanelMostrarAFN.add(jLabel19, null);
    jPanelMostrarAFN.add(jComboBoxTransAFN, null);
    jPanelMostrarAFN.add(jButtonAFNAFD, null);
    jPanelMostrarAFN.add(jComboBoxEstsAFN, null);
    jPanelMostrarAFD.add(jLabel112, null);
    jPanelMostrarAFD.add(jLabel114, null);
    jPanelMostrarAFD.add(jLabel116, null);
    jPanelMostrarAFD.add(jLabel115, null);
    jPanelMostrarAFD.add(jLabelEstInicAFD, null);
    jPanelMostrarAFD.add(jLabel110, null);
    jPanelMostrarAFD.add(jComboBoxTransAFD, null);
    jPanelMostrarAFD.add(jButtonFinalizar, null);
    jPanelMostrarAFD.add(jComboBoxEstsAFD, null);
    titledBorder5 = new TitledBorder("");
    titledBorder6 = new TitledBorder("");
    jLabel19.setBounds(new Rectangle(10, 274, 94, 27));
    jLabel19.setText("Transiушes:");
    jLabel19.setForeground(Color.blue);
    jLabel19.setFont(new java.awt.Font("Arial", 0, 13));
    titledBorder1 = new TitledBorder("");
    titledBorder2 = new TitledBorder("");
    titledBorder3 = new TitledBorder("");
    titledBorder4 = new TitledBorder("");
    this.getContentPane().setLayout(null);
    jTabbedPane1.setBackground(Color.lightGray);
    jTabbedPane1.setEnabled(true);
    jTabbedPane1.setFont(new java.awt.Font("Dialog", 1, 12));
    jTabbedPane1.setForeground(Color.blue);
    jTabbedPane1.setBorder(BorderFactory.createLineBorder(Color.black));
    jTabbedPane1.setDebugGraphicsOptions(0);
    jTabbedPane1.setPreferredSize(new Dimension(62, 27));
    jTabbedPane1.setBounds(new Rectangle(0, 3, 508, 479));
    jPanelCriarAFN.setLayout(null);
    jPanelCriarAFN.setBackground(Color.lightGray);
    jPanelCriarAFN.setFont(new java.awt.Font("Arial Black", 0, 14));
    jPanelCriarAFN.setForeground(Color.blue);
    jPanelCriarAFN.setAlignmentX((float) 0.5);
    jPanelCriarAFN.setBorder(BorderFactory.createEtchedBorder());
    jPanelCriarAFN.setDebugGraphicsOptions(0);
    jPanelCriarAFN.setDoubleBuffered(true);
    jLabel1.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel1.setForeground(Color.blue);
    jLabel1.setText("Digite o nЩmero de estados:");
    jLabel1.setBounds(new Rectangle(16, 57, 191, 34));
    jTextFieldNumEsts.setFont(new java.awt.Font("Dialog", 0, 12));
    jTextFieldNumEsts.setText("");
    jTextFieldNumEsts.setBounds(new Rectangle(236, 61, 66, 27));
    jLabel2.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel2.setForeground(Color.blue);
    jLabel2.setText("Digite o alfabeto :");
    jLabel2.setBounds(new Rectangle(10, 218, 115, 26));
    jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel3.setForeground(Color.blue);
    jLabel3.setText("(Por padrao o estado inicial serр o Q0)");
    jLabel3.setBounds(new Rectangle(5, 86, 214, 28));
    jTextFieldSimbolo.setBounds(new Rectangle(129, 216, 53, 27));
    jTextFieldSimbolo.setText("");
    jTextFieldSimbolo.setFont(new java.awt.Font("Dialog", 0, 12));
    jButtonOK.setBounds(new Rectangle(331, 60, 63, 28));
    jButtonOK.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonOK.setForeground(Color.blue);
    jButtonOK.setDebugGraphicsOptions(0);
    jButtonOK.setMaximumSize(new Dimension(47, 25));
    jButtonOK.setMinimumSize(new Dimension(51, 27));
    jButtonOK.setBorderPainted(true);
    jButtonOK.addActionListener(this);
    jButtonOK.setText("Ok");
    jButtonConcluirSim.setText("Concluir");
    jButtonConcluirSim.setBorderPainted(true);
    jButtonConcluirSim.setMinimumSize(new Dimension(51, 27));
    jButtonConcluirSim.setMaximumSize(new Dimension(47, 25));
    jButtonConcluirSim.setDebugGraphicsOptions(0);
    jButtonConcluirSim.setForeground(Color.blue);
    jButtonConcluirSim.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonConcluirSim.setBounds(new Rectangle(378, 215, 98, 28));
    jButtonConcluirSim.addActionListener(this);
    jButtonIncluirSim.setText("Incluir simbolo");
    jButtonIncluirSim.setBorderPainted(true);
    jButtonIncluirSim.setMinimumSize(new Dimension(51, 27));
    jButtonIncluirSim.setMaximumSize(new Dimension(47, 25));
    jButtonIncluirSim.setDebugGraphicsOptions(0);
    jButtonIncluirSim.setForeground(Color.blue);
    jButtonIncluirSim.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonIncluirSim.addActionListener(this);
    jButtonIncluirSim.setBounds(new Rectangle(217, 215, 134, 28));
    jLabel4.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel4.setForeground(Color.blue);
    jLabel4.setText("Incluir Transiушes :");
    jLabel4.setBounds(new Rectangle(8, 281, 121, 24));
    jComboBoxOrigem.setBounds(new Rectangle(131, 281, 58, 24));
    jComboBoxOrigem.setEnabled(false);
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabel5.setForeground(Color.blue);
    jLabel5.setText("--->");
    jLabel5.setBounds(new Rectangle(200, 281, 30, 23));
    jComboBoxDestino.setBounds(new Rectangle(234, 281, 58, 24));
    jComboBoxDestino.setEnabled(false);
    jComboBoxString.setBounds(new Rectangle(345, 282, 58, 24));
    jComboBoxString.setEnabled(false);
    jLabel6.setForeground(Color.blue);
    jLabel6.setText("Origem");
    jLabel6.setBounds(new Rectangle(135, 308, 55, 21));
    jLabel7.setBounds(new Rectangle(240, 310, 57, 21));
    jLabel7.setForeground(Color.blue);
    jLabel7.setText("Destino");
    jLabel8.setForeground(Color.blue);
    jLabel8.setText("String");
    jLabel8.setBounds(new Rectangle(355, 308, 47, 25));
    jButtonIncluirTrans.setBounds(new Rectangle(88, 373, 144, 34));
    jButtonIncluirTrans.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonIncluirTrans.setForeground(Color.blue);
    jButtonIncluirTrans.setText("Incluir Transiусo");
    jButtonIncluirTrans.addActionListener(this);
    jButtonIncluirTrans.setEnabled(false);
    jButtonConcluirTrans.setText("Concluir");
    jButtonConcluirTrans.setForeground(Color.blue);
    jButtonConcluirTrans.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonConcluirTrans.setBounds(new Rectangle(260, 372, 144, 34));
    jButtonConcluirTrans.addActionListener(this);
    jButtonConcluirTrans.setEnabled(false);
    jLabel9.setFont(new java.awt.Font("Dialog", 0, 13));
    jLabel9.setForeground(Color.blue);
    jLabel9.setText("Defina o(s) estado(s) final(is):");
    jLabel9.setBounds(new Rectangle(8, 132, 203, 33));
    jComboBoxFinais.setBounds(new Rectangle(214, 136, 58, 24));
    jButtonDefinirFinal.setBounds(new Rectangle(278, 135, 116, 28));
    jButtonDefinirFinal.setFont(new java.awt.Font("Arial Black", 0, 11));
    jButtonDefinirFinal.setForeground(Color.blue);
    jButtonDefinirFinal.setDebugGraphicsOptions(0);
    jButtonDefinirFinal.setMaximumSize(new Dimension(47, 25));
    jButtonDefinirFinal.setMinimumSize(new Dimension(51, 27));
    jButtonDefinirFinal.setRequestFocusEnabled(true);
    jButtonDefinirFinal.setBorderPainted(true);
    jButtonDefinirFinal.setText("Definir final");
    jButtonDefinirFinal.addActionListener(this);
    jLabel10.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabel10.setForeground(Color.blue);
    jLabel10.setBorder(titledBorder4);
    jLabel10.setVerifyInputWhenFocusTarget(true);
    jLabel10.setText("CRIAК├O DO AFN");
    jLabel10.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
    jLabel10.setBounds(new Rectangle(186, 9, 143, 30));
    this.getContentPane().setBackground(SystemColor.scrollbar);
    this.setForeground(Color.black);
    jPanelMostrarAFN.setBackground(Color.lightGray);
    jPanelMostrarAFN.setAlignmentY((float) 0.5);
    jPanelMostrarAFN.setBorder(BorderFactory.createEtchedBorder());
    jPanelMostrarAFN.setLayout(null);
    jLabel11.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel11.setForeground(Color.blue);
    jLabel11.setText("Estados:");
    jLabel11.setBounds(new Rectangle(14, 78, 63, 24));
    jLabel13.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel13.setForeground(Color.blue);
    jLabel13.setText("Estado Inicial:");
    jLabel13.setBounds(new Rectangle(13, 170, 94, 27));
    jLabel14.setFont(new java.awt.Font("Arial", 0, 13));
    jLabel14.setForeground(Color.blue);
    jLabel14.setText("Estado(s) Final(is):");
    jLabel14.setBounds(new Rectangle(12, 219, 116, 27));
    jLabelEstInicAFN.setBorder(BorderFactory.createEtchedBorder());
    jLabelEstInicAFN.setText("");
    jLabelEstInicAFN.setBounds(new Rectangle(127, 168, 94, 27));
    jLabel17.setFont(new java.awt.Font("Arial", 1, 14));
    jLabel17.setForeground(Color.blue);
    jLabel17.setBorder(titledBorder2);
    jLabel17.setText("MOSTRAR AFN");
    jLabel17.setBounds(new Rectangle(192, 12, 116, 34));
    jButtonAFNAFD.setBackground(Color.lightGray);
    jButtonAFNAFD.setBounds(new Rectangle(134, 363, 235, 43));
    jButtonAFNAFD.setFont(new java.awt.Font("Arial", 1, 13));
    jButtonAFNAFD.setForeground(Color.blue);
    jButtonAFNAFD.setText("Transformaусo AFN - AFD");
    jButtonAFNAFD.addActionListener(this);
    jPanelCriarAFN.add(jLabel4, null);
    jPanelCriarAFN.add(jButtonIncluirTrans, null);
    jPanelCriarAFN.add(jButtonConcluirTrans, null);
    jPanelCriarAFN.add(jComboBoxString, null);
    jPanelCriarAFN.add(jLabel8, null);
    jPanelCriarAFN.add(jLabel7, null);
    jPanelCriarAFN.add(jComboBoxDestino, null);
    jPanelCriarAFN.add(jLabel5, null);
    jPanelCriarAFN.add(jComboBoxOrigem, null);
    jPanelCriarAFN.add(jLabel6, null);
    jPanelCriarAFN.add(jLabel2, null);
    jPanelCriarAFN.add(jButtonIncluirSim, null);
    jPanelCriarAFN.add(jButtonConcluirSim, null);
    jPanelCriarAFN.add(jTextFieldSimbolo, null);
    jPanelCriarAFN.add(jLabel1, null);
    jPanelCriarAFN.add(jLabel3, null);
    jPanelCriarAFN.add(jTextFieldNumEsts, null);
    jPanelCriarAFN.add(jButtonOK, null);
    jPanelCriarAFN.add(jLabel9, null);
    jPanelCriarAFN.add(jLabel10, null);
    jPanelCriarAFN.add(jButtonDefinirFinal, null);
    jPanelCriarAFN.add(jButtonConcluirFinais, null);
    jPanelCriarAFN.add(jComboBoxFinais, null);
    jTabbedPane1.add(jPanelCriarAFN, "Criar AFN");
    this.getContentPane().add(jTabbedPane1, null);
    jTabbedPane1.add(jPanelMostrarAFN,  "MostrarAFN");
    jTabbedPane1.add(jPanelMostrarAFD,  "MostrarAFD");
    jPanelMostrarAFN.add(jComboBoxAlfAFN, null);
    jPanelMostrarAFN.add(jComboBoxEstsFinaisAFN, null);
    jPanelMostrarAFD.add(jComboBoxEstsFinaisAFD, null);
    automatoAFN = new AutomatoAFN();
  }

  public static void main(String[] args) {
    InterfaceAFNAFD interfaceAFNAFD = new InterfaceAFNAFD();
    interfaceAFNAFD.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    interfaceAFNAFD.pack();
    interfaceAFNAFD.setVisible(true);
    interfaceAFNAFD.setBounds(new Rectangle(0, 3, 516, 508));
    interfaceAFNAFD.setLocation(250,100);
  }
}

