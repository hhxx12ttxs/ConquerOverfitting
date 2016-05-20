/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.jsensors.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import dev.jsensors.sensores.RedeSensores;
import dev.jsensors.sensores.Sensor;

/**
 *
 * @author Ismael
 */
public class MapaVisualizacao extends MapaSensores {

    private boolean exibirSensores, exibirRCobertura, exibirRComunicacao, exibirConexao;

    public MapaVisualizacao(RedeSensores redeSensores, boolean exibirSensores, boolean exibirRCobertura, boolean exibirRComunicacao, boolean exibirConexao) {
        super(redeSensores);
        this.exibirSensores = exibirSensores;
        this.exibirRCobertura = exibirRCobertura;
        this.exibirRComunicacao = exibirRComunicacao;
        this.exibirConexao = exibirConexao;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(COR_AREA);
        g2d.fillRect(0, 0, SIZE_REAL, SIZE_REAL);
        if (exibirRComunicacao) {
            desenharRaiosComunicacao(g2d);
        }
        if (exibirRCobertura) {
            desenharRaiosCobertura(g2d);
        }
        if (exibirConexao) {
            desenharLinksComunicacao(g2d);
        }
        if (exibirSensores) {
            desenharSensores(g2d);
        }
        desenharSink(g2d);
    }

    private void desenhaCirculo(Graphics2D g2d, double centro_x, double centro_y, double largura, double altura, Color c) {
        g2d.setColor(c);
        double real_x = centro_x - (largura / 2);
        double real_y = centro_y - (altura / 2);
        g2d.fill(new Ellipse2D.Double(real_x, real_y, largura, altura));
    }

    private void desenharRaiosComunicacao(Graphics2D g2d) {
        for (int i = 0; i < redeSensores.getSensores().size(); i++) {
            Sensor s = redeSensores.getSensores().get(i);
            if (s.getStatus() == Sensor.ATIVO) {
                Point2D p = pontoDesenho(s.getX(), s.getY());
                desenhaCirculo(g2d, p.getX(), p.getY(), tamanhoRaioDesenhar(s.getRaio_comunicacao()), tamanhoRaioDesenhar(s.getRaio_comunicacao()), COR_RAIO_COMUNICACAO);
            }
        }
    }

    private void desenharRaiosCobertura(Graphics2D g2d) {
        for (int i = 0; i < redeSensores.getSensores().size(); i++) {
            Sensor s = redeSensores.getSensores().get(i);
            if (s.getStatus() == Sensor.ATIVO) {
                Point2D p = pontoDesenho(s.getX(), s.getY());
                if (s.isConexo()) {
                    desenhaCirculo(g2d, p.getX(), p.getY(), tamanhoRaioDesenhar(s.getRaio_cobertura()), tamanhoRaioDesenhar(s.getRaio_cobertura()), COR_RAIO_COBERTURA_CONEXA);
                }
            }
        }
    }

    private void desenharLinksComunicacao(Graphics2D g2d) {
        g2d.setColor(COR_LINK_COMUNICACAO);
        for (int i = 0; i < redeSensores.getSensores().size(); i++) {
            Sensor s = redeSensores.getSensores().get(i);
            List<Integer> conectados = s.getSConectados();
            for (int con : conectados) {
                if (con < 0 || con > redeSensores.getSensores().size()) {
                    continue;
                }
                Sensor sc = (con == redeSensores.getSensores().size()) ? redeSensores.getSink() : redeSensores.getSensores().get(con);
                Point2D p1 = pontoDesenho(s.getX(), s.getY());
                Point2D p2 = pontoDesenho(sc.getX(), sc.getY());
                g2d.draw(new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY()));

            }

        }

    }

    private void desenharSensores(Graphics2D g2d) {
        for (int i = 0; i < redeSensores.getSensores().size(); i++) {
            Sensor s = redeSensores.getSensores().get(i);
            Color c = s.getStatus() == Sensor.FALHO ? COR_SENSOR_FALHA : (s.getStatus() == Sensor.ATIVO ? COR_SENSOR_ATIVO : COR_SENSOR_INATIVO);
            Point2D p1 = pontoDesenho(s.getX(), s.getY());
            desenhaCirculo(g2d, p1.getX(), p1.getY(), TAM_SENSORES, TAM_SENSORES, c);
            desenhaIdSensor(g2d, s);
        }
    }

    private void desenhaIdSensor(Graphics2D g2d, Sensor s) {
        g2d.setColor(COR_NUMERO);
        Point2D p1 = pontoDesenho(s.getX(), s.getY());
        double x = p1.getX() - TAM_SENSORES / 2;
        double y = p1.getY() - TAM_SENSORES;
        g2d.drawString("" + s.getId(), (int) x, (int) y);
    }

    private void desenharSink(Graphics2D g2d) {
        Point2D p1 = pontoDesenho(redeSensores.getSink().getX(), redeSensores.getSink().getY());
        desenhaCirculo(g2d, p1.getX(), p1.getY(), TAM_SENSORES, TAM_SENSORES, COR_SINK);
    }

    private double tamanhoRaioDesenhar(double raio) {
        return ((raio * 2 * SIZE_REAL) / redeSensores.getTamanho());
    }
}

