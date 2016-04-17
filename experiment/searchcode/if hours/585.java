package DefiningClasses;

/**
 * Created by Xwz on 30.12.2015 г..
 */
public  class Battery{
    //variables
    String model;
    float idleHours;
    float talkHours;

    //Constructor
    public Battery(String model) {
        if (model == "Nogo Gotina"){
            this.idleHours=5;
            this.talkHours = 2;
            this.model = "Nogo Gotina";
        }
        else if (model == null || model == ""){
            this.model = ("Unknow characteristic of this battery model");
        }else {

            this.model = model;
        }

    }
    //Getters and Setters
    public String getModel() {
        return model;
    }

    private void setModel(String model) {
        this.model = model;
    }

    public float getIdleHours() {
        return idleHours;
    }

    private void setIdleHours(float idleHours) {
        this.idleHours = idleHours;
    }

    public float getTalkHours() {
        return talkHours;
    }

    private void setTalkHours(float talkHours) {
        this.talkHours = talkHours;
    }
}

