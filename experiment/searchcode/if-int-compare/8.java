public int compareNote(NoteName compare) {
int compareVal = 0;

if(this.index > compare.index)
compareVal = 1;
else if(this.index < compare.index)
compareVal = -1;

return compareVal;
}
}

