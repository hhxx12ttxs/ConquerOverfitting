public class Virus {

private static int virusCount = 0;

private int newSeconds;

public Virus(){
public void setSeconds(int seconds){
if(seconds >= 60 &amp;&amp; seconds <= 100)
this.newSeconds = seconds;
}

public int getSeconds(){
return this.newSeconds;
}

}

