Character ret;
if(element == null)
ret = Character.valueOf(Character.MIN_VALUE);
else
synchronized(this.map) {
ret = this.map.get(element);
if(ret == null) {
if(this.max == Character.MAX_VALUE)

