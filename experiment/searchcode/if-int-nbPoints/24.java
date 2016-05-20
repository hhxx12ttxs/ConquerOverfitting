package packageTP2;

public class Client 
{

private String noCarteFidelite;
private String nom;
private int nbPointsAcc;

  // Constructeur d'un client
  public Client(String noCarteFidelite, String nom, int nbPointsAcc)
  {
  this.noCarteFidelite=noCarteFidelite;
  this.nbPointsAcc=nbPointsAcc;
  this.nom = nom;
  
  }

  public String getNoCarte()
  {
  return noCarteFidelite;
  }
  
  public String getNom()
  {
  return nom;
  }
  
  public int getNbPointsAcc()
  {
  return nbPointsAcc;
  }
  
  // Modifie les points aprčs le paiement d'une commande
  public void modifierPoints ( int nbPointsUtil )
  {
  this.nbPointsAcc+= nbPointsUtil;
  }

  // Valide si le montant reçu est suffissant pour payer la commande en cours
  public boolean assezArgent ( Commande c, double montant )
  {
   double total = c.calculerGrandTotal();
   if ( montant  >= total )
    return true;
  else 
    return false;
  }
  
  // Effectue le paiement et retour le montant du change s'il y a lieu
  public double paieCommande ( Commande c, double montant )
  {
  double total = c.calculerGrandTotal();
  double change = montant- total;
  int nbPoints = c.calculerPointsBonis();
  if ( change > 0)
      modifierPoints(nbPoints);

  return change;
  }
  
  public String toString(){
    return "NoClient :" +this.noCarteFidelite + " nom : "+ this.nom + " pointBonis :" + this.nbPointsAcc;
  }
}
