
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gradius.Util;

/** Classe responsável pelo delay do jogo
 * @author Eduardo
 */
public class Timer extends Thread {

    /**Construtor da classe Timer
     * @param listener -
     * @param millis - Tempo de delay do timer
     */
    public Timer(TimerListener listener, long millis)
    {
        this.listener = listener;
        this.millis = millis;
        this.paused = false;
        this.finalize = false;
    }

    /** Método que dispara o timer.
     */
    @Override
    public void run()
    {
        while (!finalize)
        {
            try
            {
                Thread.sleep(millis);
                if (!(paused))
                {
                    listener.update(millis);
                }
            }
            catch (InterruptedException ex)
            {
            }
        }
    }

    /** Método getter que retorna o tempo em milissegundos
     *
     * @return long - tempo em milissegundos
     */
    public long getMillis()
    {
        long i = this.millis;
        return i;
    }

    /** Método que redefine o tempo em milissegundos
     *
     * @param millis - tempo em milissegundos
     */
    public void reTime(long millis)
    {
        this.millis = millis;
    }

    /** Método que seta o jogo como pausado
     *
     */
    public void pause()
    {
        this.paused = true;
    }

    /** Método que seta o jogo como despausado
     *
     */
    public void unpause()
    {
        this.paused = false;
    }

    /** Método que retorna se o jogo está pausado ou năo
     *
     * @return boolean - Retorna se o jogo está pausado ou năo
     */
    public boolean paused()
    {
        boolean p = this.paused;
        return p;
    }

    /** Método que seta o jogo como finalizado
     *
     */
    public void finalizar()
    {
        this.finalize = true;
    }

    private long millis;
    private TimerListener listener;
    private boolean paused;
    private boolean finalize;
}











