public void removeEntryAt(int index) {
Entry e = (Entry) entries.elementAt(index);
if (e instanceof DoubleEntry) ((DoubleEntry) e).removeOther();
Entry oldEntry = (Entry) entries.elementAt(index);
if (oldEntry instanceof DoubleEntry) ((DoubleEntry) oldEntry).removeOther();

