import java.util.Random;

/**
*
* @author Zlati
*/
public class minExclusive extends restriction {

private double minval = 0;
public minExclusive(String field, String restrictionType, double minval){
super(field, restrictionType);
this.minval = minval;

