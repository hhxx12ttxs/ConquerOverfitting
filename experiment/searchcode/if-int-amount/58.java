package A;

import java.io.PrintWriter;

public class Treasure extends Item {

private int amount = 0;
return amount;
}

public void setAmount(int amount) {
if(amount > -1)	this.amount = amount;
}

@Override
public void save(PrintWriter pw) {

}

}

