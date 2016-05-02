package BLL;

import java.util.*;

import DTO.*;

/**
 * Esta classe ж responsрvel pelo gerenciamento do comportamento genжrico dos
 * elementos de jogo.
 *
 * @author (R. Soares)
 * @version (1.0 - 18.05.2010)
 */
public class ElementoJogo
{
    // Variрveis de Instancia ------------------------------------------ *
    // nсo hр

    /**
     * Construtor da Classe ElementoJogo
     */
    public ElementoJogo()
    {

    }

    // Mжtodo ------------------------------------------------------- *

    /**
     * Mжtodo que realiza a movimentaусo de um Elemento. Recebe como parРmetro o
     * elemento que deve ser movido. get o elemento com sua posicao atualizada.
     *
     * @param  ElementoDTO - elemento : Elemento que deve ser movido.
     * @return   ElementoDTO : Elemento com sua posicao atualizada.
     */
    public ElementoDTO mover(ElementoDTO elemento)
    {
        // Coordenadas de inьcio e fim do elemento
        double pxDestino   = elemento.getDestino().getCoordenadaX();
        double pyDestino   = elemento.getDestino().getCoordenadaY();

        // velocidade de deslocamento
        double pxVelocidade = elemento.getPxVelocidade();
        double pyVelocidade = elemento.getPyVelocidade();

        // Atualiza a nova localizaусo com a posiусo Final atual, deslocando o elemento
        elemento.setLocalizacaoAtual(new Ponto(pxDestino, pyDestino) );

        // Atualiza a destino para o prзximo movimento.
        elemento.setDestino(new Ponto (pxDestino + pxVelocidade, pyDestino + pyVelocidade) );
        return elemento;
    }


    /**
     * Mжtodo que realiza a movimentaусo de um elemento Satжlite de forma Circular.
     * Recebe como parРmetros o elemento que deve ser movido e a direусo do
     * deslocamento .
     *
     * @param  ElementoDTO - elemento : Elemento que deve ser deslocado
     * @param  int - direcao : Direусo do deslocamento (1: Sentido Horрrio;
     *                                                                                 2: Sentido Ant-Horрrio)
     * @return  SateliteDTO : Elemento SateliteDTO com nova posiусo.
     */
    public SateliteDTO moverCircular(SateliteDTO satelite, int direcao)
    {
        double anguloAtual = satelite.getAngulo();
        float velocidade = satelite.getVelocidade();
        double distanciaCentro = satelite.getDistanciaCentro();
        double centroX = satelite.getCentroX();
        double centroY = satelite.getCentroY();

        Ponto novaPosicao = null;

        double coordenadaX = 0;
        double coordenadaY = 0;
        double anguloNovo = 0;

        // sentido horario
        if(direcao == 1)
        {
            anguloNovo = anguloAtual - velocidade;

            if(anguloNovo < 0)
            {
                anguloNovo = (anguloAtual + 360) - velocidade;
            }

        }

        // sentido anti-horрrio
        if(direcao == 2)
        {
             anguloNovo = anguloAtual + velocidade;

             if(anguloNovo > 360)
             {
                 anguloNovo = (anguloAtual - 360) + velocidade;
             }
         }


       // cateto adjacente = hipotenusa * cosseno A
       coordenadaX = centroX + ( Math.cos( Math.toRadians(anguloNovo) ) * distanciaCentro );

       // cateto oposto = hipotenusa * Seno A
       coordenadaY = centroY - ( Math.sin( Math.toRadians(anguloNovo) )  * distanciaCentro );

       satelite.setAngulo(anguloNovo);
       satelite.setLocalizacaoAtual( new Ponto(coordenadaX, coordenadaY) );

       return satelite;
    }

    /**
     * Mжtodo que determina as compoentes X e Y de um vetor de velocidade. Recebe como
     * parametro um elemento de jogo e get o elemento com as componentes
     * determinadas
     *
     * @param  ElementoDTO - elemento : Elemento ao qual serсo determinados os compoentes
     * @return  ElementoDTO : Elemento com as compoenetes X e Y da velocidade determinadas
     */
    public ElementoDTO determinarComponentesVelocidade(ElementoDTO elemento)
    {
        // Coeficiente angular da reta
        double k = 0;

        // Angulo da posicao;
        double angulo;

        // Velocidade do elemento
        float velocidade = elemento.getVelocidade();

        // Coordenadas atuais de inьcio e fim de movimento do elemento
        double pxInicio = elemento.getLocalizacaoAtual().getCoordenadaX();
        double pyInicio = elemento.getLocalizacaoAtual().getCoordenadaY();
        double pxFim = elemento.getDestino().getCoordenadaX();
        double pyFim = elemento.getDestino().getCoordenadaY();

        //Componentes da velocidade
        double pxVelocidade = 0;
        double pyVelocidade = 0;


        // Determina os vetores para coordenadas em X de inьcio e fim diferentes
        if(pxInicio != pxFim)
        {
            k = ( pyFim - pyInicio ) / ( pxFim - pxInicio );

            angulo = Math.atan( k );

            // cateto adjacente = hipotenusa * cosseno A
            pxVelocidade = Math.cos( angulo ) * velocidade;

            // cateto oposto = hipotenusa * Seno A
            pyVelocidade = Math.sin( angulo ) * velocidade;
        }

        // Determina os vetores para coordenadas de inьcio X e fim X iguais.
        else
        {
            pxVelocidade = 0;
            pyVelocidade =  velocidade;
        }

        elemento.setPxVelocidade( pxVelocidade );
        elemento.setPyVelocidade( pyVelocidade );

       return elemento;
    }

    /**
     * Determina o movimento inicial de um elemento, recebe como parametro o elemento a ser
     * movido e get o elemento apзs deslocamento.
     *
     * @param  ElementoDTO - elemento : Elemento que serр movido
     * @return  ElementoDTO : Elemento com movimento inicial
     */
    public ElementoDTO determinarMovimentoInicial(ElementoDTO elemento)
    {
            elemento = determinarComponentesVelocidade(elemento);

            // ╔ obtido o valor dos componentes em mзdulo
            double pxVelocidade = Math.abs( elemento.getPxVelocidade() );
            double pyVelocidade = Math.abs( elemento.getPyVelocidade() );

            // Caso o elemento se desloque para a direita (X inicio > X fim)
            // a velocidade em x deve ser decrementada
            if(elemento.getLocalizacaoAtual().getCoordenadaX() > elemento.getDestino().getCoordenadaX() )
                pxVelocidade = - pxVelocidade;

             // Caso o elemento se desloque para a cima (Y inicio > Y fim)
             // a velocidade em Y deve ser decrementada
             if(elemento.getLocalizacaoAtual().getCoordenadaY() > elemento.getDestino().getCoordenadaY() )
                 pyVelocidade =  - pyVelocidade;

             elemento.setPxVelocidade( pxVelocidade );
             elemento.setPyVelocidade( pyVelocidade );

             return elemento;
    }

    /**
     * Mжtodo que identifica se dois Elementos colidiram.
     *
     * @param  ElementoDTO - elementoA : Primeiro elemento
     * @param  ElementoDTO - elementoB : Segundo elemento
     * @return   boolean : TRUE - Houve colisсo; FALSE - Nсo houve colisсo
     */
    public boolean detectarColisaoElementos(ElementoDTO elementoA, ElementoDTO elementoB)
    {
        double distancia = 0;

        double raioA = elementoA.getDiametro() / 2;
        double raioB = elementoB.getDiametro() / 2;

        double xA = elementoA.getLocalizacaoAtual().getCoordenadaX();
        double yA = elementoA.getLocalizacaoAtual().getCoordenadaY();
        double xB = elementoB.getLocalizacaoAtual().getCoordenadaX();
        double yB = elementoB.getLocalizacaoAtual().getCoordenadaY();

        // Cрlculo da distРncia entre os dois elementos
        distancia = Math.sqrt( (xA - xB) * (xA -xB) + (yA - yB) * (yA - yB) );

        //verifica colisсo
        if ( distancia < (raioA + raioB) )
        {
            return true;
        }

        return false;
    }

    /**
     * Mжtodo que cрlcula os novos componentes de velocidade (vX e Vy) para dois elementos
     * apзs o impрcto.
     *
     * @param  ElementoDTO - elementoA : Primeiro elemento a ser redirecionado.
     * @param  ElementoDTO - elementoB : Segundo elemento a ser redirecionado
     * @return   double[] : Vetor de 4 posiушes com os valores dos novos componentes de velocidade
     *                                 dos dois elementos  -  [0] = vxA;  [1] = vyA;  [2] = vxB;  [3] = vyB;
     */
    public double[] redirecionarElementos(ElementoDTO elementoA, ElementoDTO elementoB)
    {
        double raioA = elementoA.getDiametro() / 2;
        double raioB = elementoB.getDiametro() / 2;
        double vA = elementoA.getVelocidade();
        double vB = elementoB.getVelocidade();
        double massaA = elementoA.getMassa();
        double massaB = elementoB.getMassa();

        double xA = elementoA.getLocalizacaoAtual().getCoordenadaX();
        double yA = elementoA.getLocalizacaoAtual().getCoordenadaY();
        double xB = elementoB.getLocalizacaoAtual().getCoordenadaX();
        double yB = elementoB.getLocalizacaoAtual().getCoordenadaY();

        double vxA = elementoA.getPxVelocidade();
        double vyA = elementoA.getPyVelocidade();
        double vxB = elementoB.getPxVelocidade();
        double vyB = elementoB.getPyVelocidade();

        // Cрlculo da distРncia entre as coordenadas X
        double dX = xA - xB;

        // Cрlculo da distРncia entre as coordenadas Y
        double dY = yA - yB;

        // Vetor unitрrio na direусo da colisсo
        double d = Math.sqrt( dX * dX + dY * dY );

        // Projeусo das Velocidades nos eixos
        double pX = dX / d;
        double pY = dY / d;

        // Cрlculo das novas velocidades nos eixos apзs colisсo
        double vA_x = (vxA * pX + vyA * pY);
        double vA_y = (-vxA * pY + vyA * pX);
        double vB_x = (vxB * pX + vyB * pY);
        double vB_y = (-vxB * pY + vyB * pX);

        // Cрlculo da compoente de velocidade para a direусo (dx,dy)
        int ed = 1; //para a colisсo elрstica ed = 1

        double vpA = vA_x + (1 + ed) * (vB_x - vA_x) / (1 + massaA / massaB);
        double vpB = vB_x + (1 + ed) * (vA_x - vB_x) / (1 + massaA / massaB);

        // Velocidade no eixo de cada esfera apзs colisсo
        vxA = vpA * pX - vA_y  * pY;
        vyA = vpA * pY + vA_y  * pX;
        vxB = vpB * pX - vB_y  * pY;
        vyB = vpB * pY + vB_y  * pX;

        double[] novasCoordenadas = new double[4];

        novasCoordenadas[0] = vxA; //novo_vxA;
        novasCoordenadas[1] = vyA; //novo_vyA;
        novasCoordenadas[2] = vxB; //novo_vxB;
        novasCoordenadas[3] = vyB; //novo_vyB;

        return novasCoordenadas;
    }

}

