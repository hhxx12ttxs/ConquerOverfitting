private LocalDateTime whenIsItTaken;
private LocalDateTime whenIsItBroughtBack;

public History(LocalDateTime time) {
if(time!=null)
this.whenIsItTaken= time;
}
public void addTimeWhenItISBroughtBack(LocalDateTime time){
if(time!=null ){

