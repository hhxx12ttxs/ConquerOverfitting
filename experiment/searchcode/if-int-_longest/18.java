private final int shortest;
private final int longest;

/**
* Define the lookup table to be used in translation
lookupMap = new HashMap<CharSequence, CharSequence>();
int _shortest = Integer.MAX_VALUE;
int _longest = 0;
for(CharSequence[] seq : lookup) {

