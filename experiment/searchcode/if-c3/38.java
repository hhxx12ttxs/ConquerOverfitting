
public class Yao{

Coin c1, c2, c3;

public Yao(Coin c1, Coin c2, Coin c3){
this.c1 = c1;
this.c2 = c2;
this.c3 = c3;
}

public int toss() {
int result1= c1.headOrTail();
int result2= c2.headOrTail();

