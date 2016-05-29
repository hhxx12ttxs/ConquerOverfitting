score += pairs(c1,c2,c3,c4,c5);
score += runs(c1,c2,c3,c4,c5);

if(crib)
score += cribSuit(c1,c2,c3,c4,c5);
ArrayList<Card[]> invalid = new ArrayList<Card[]>();

int score = 0;

if(sortRuns(c1,c2,c3,c4,c5)){
score += 5;
invalid.add(new Card[] {c1,c2,c3,c4,c5});

