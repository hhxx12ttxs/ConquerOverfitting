this.descending = descending;
}

@Override
public int compare(Question q1, Question q2) {

Long timestampQ1 = q1.getQuestionTimestamp();
Long timestampQ2 = q2.getQuestionTimestamp();

if(descending) {

