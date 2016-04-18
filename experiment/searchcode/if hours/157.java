package models;

import java.util.List;

/**
 * Created by Yoan D on 12/05/2015.
 */
public class CalculateHoursTeacher {

    private Integer hours;            // Heures due

    private Integer extraHours;       // Heures supplémentaire

    private double hours_to_doing_CM; // Nombre d'heures de CM validés par le responsable de filière pour une période donnée
    private double hours_to_doing_TD; // Nombre d'heures de TD validés par le responsable de filière pour une période donnée

    private String statut;            // Si hours_to_doing < à hours statut = "HEURES_A_PRENDRE",
                                      // si hours_to_doing > à hours et hours_to_doing < à extraHours statut = "HEURES_SUPPLEMENTAIRES",
                                      // si hours_to_doing > à hours et > extraHours statut = "DEPASSEMENT_D_HEURES"

    private double total;             // Total = hours_to_doing_CM + hours_to_doing_TD

    /**
     * Constructeur par defaut
     */
    public CalculateHoursTeacher()
    {
        this.hours = 0;
        this.extraHours = 0;
        this.hours_to_doing_CM = 0.0;
        this.statut = "A_PRENDRE";
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(Integer extraHours) {
        this.extraHours = extraHours;
    }

    public double getHours_to_doing_CM() {
        return hours_to_doing_CM;
    }

    public void setHours_to_doing_CM(double hours_to_doing_CM) {
        this.hours_to_doing_CM = hours_to_doing_CM;
    }

    public double getHours_to_doing_TD() {
        return hours_to_doing_TD;
    }

    public void setHours_to_doing_TD(double hours_to_doing_TD) {
        this.hours_to_doing_TD = hours_to_doing_TD;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Calcul les heures statutaires
     * @param login
     * @param periodeOuverte
     */
    public void CalculateHours (String login, int periodeOuverte)
    {
        Double calcule_heures_CM = 0.0;
        Double calcule_heure_TD = 0.0;

        Teacher enseignant = Teacher.find.where().eq("USER_ID",login).findUnique(); // On récupére le nombre d'heures dues et le nombre d'heures supplémentaires

        if(enseignant != null)
        {
            this.setHours(enseignant.getHours());
            this.setExtraHours(enseignant.getExtraHours());

            List<Voeux> listeVoeuxAnneeCourant = Voeux.find.where().eq("period.id",periodeOuverte)
                                                                   .eq("ref_utilisateurs",login)
                                                                   .eq("VALIDE","VALIDE")
                                                                   .findList();

            for(Voeux resultat : listeVoeuxAnneeCourant)
            {
                calcule_heures_CM += resultat.getVolume_horaire_cours_assure() * 1.5;

                calcule_heure_TD += resultat.getVolume_horaire_TD_assure();
            }

            this.setHours_to_doing_CM(calcule_heures_CM); // CM
            this.setHours_to_doing_TD(calcule_heure_TD);  // TD

            this.total = this.getHours_to_doing_CM() + this.getHours_to_doing_TD(); // Total

            if(this.total < this.getHours())
            {
                this.statut = "HEURES_A_PRENDRE";
            }
            else
            {
                if((this.total > this.getHours()) && (this.total < this.getExtraHours() + this.getHours()))
                {
                    this.statut = "HEURES_SUPPLEMENTAIRES";
                }
                else
                {
                    this.statut = "DEPASSEMENT_D_HEURES";
                }
            }
        }
    }
}

