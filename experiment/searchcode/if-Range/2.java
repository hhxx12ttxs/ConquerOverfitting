package com.clinkworks.gameengine.datatypes;

public enum Range {
MELEE(0),
SHORT(1),
MEDIUM(2),
for(Range range : Range.values()){
if(range.getRangeValue() == value){
return range;
}
}
return null;
}
}

