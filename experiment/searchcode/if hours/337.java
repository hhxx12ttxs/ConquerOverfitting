class Time {
    public updateMinutes(){
        for (int i=0;i<12;i++){
            minutes++;
        }
    }
    public int hours;
    public int minutes;
    public int seconds;
    public Time() {
        hours=1;
        minutes=12;
        seconds=45;
    }
    public Midnight() {
        if(hours != 0) hours=0;
        if(minutes != 0) minutes=0;
        if(seconds != 0) seconds=0;
    }
    public updateHoursTodo (){
        do hours++;
        while(hours<12);
    }
    public updateSeconds() {
        while(seconds<12) {
            seconds++;
        }
    }
    private fgt   (){
        hours*minutes/seconds+1-2;
    }
    private secretFunction(){
        double r = 0.0;
        for(int i = 0; i < 3; i++){
            for(int j=0; j <5;j++) {
                while (r < 5) {
                    r++;
                }
            }
        }
    }
    public func(int r, double t, float s){
        if(hours==1){
            hours++;
        }
        else{
            hours++;
        }
    }
    public func4(int u){
        try{
            u;
        }
        catch(Ecxeption e) {
            u = 3;
        }
        switch() {
            default: {
                break;
            }
        }
        if(u==3) {
            u+1;
        }
        else if (u == 4) {
            u--;
        }
    }
}

