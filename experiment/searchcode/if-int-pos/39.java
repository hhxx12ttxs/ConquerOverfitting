import eu.janinko.Andaria.logparser.MessageType;
import java.util.Calendar;

public class LocatedMessage extends Message {
int posX, posY, posZ;


public LocatedMessage(Calendar c, CharSequence m, int posX, int posY, int posZ, MessageType t){

