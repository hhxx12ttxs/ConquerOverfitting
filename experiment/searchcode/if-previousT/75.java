groupedList.get(0).add(t);
} else if (previousT != null
&amp;&amp; getDate_Absolute(getCreationDate(t)).equals(previousT)) {
Date creationDate = getCreationDate(t);
if (creationDate != null) {
previousT = getDate_Absolute(creationDate);

