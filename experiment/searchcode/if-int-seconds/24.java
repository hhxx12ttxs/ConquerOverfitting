public int inSeconds() {
return inSeconds;
}

public int in(Period p) {
if (this.inSeconds < p.inSeconds) {
return this.inSeconds / p.inSeconds;
}
}

public static Period fromSeconds(int seconds) {
for (Period p : values()) {
if (p.inSeconds == seconds) {

