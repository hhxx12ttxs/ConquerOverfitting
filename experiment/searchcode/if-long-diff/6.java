private String getTimeDiff() {

long diff = new Date().getTime() - postedTime;

long diffSeconds = diff / 1000;
long diffMinutes = diff / (60 * 1000);
long diffHours   = diff / (60 * 60 * 1000);

