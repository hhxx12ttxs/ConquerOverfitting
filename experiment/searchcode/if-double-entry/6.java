public boolean add(Object newEntry)
{
if (isArrayFull())
doubleArray();

// add new entry after last current entry
entry[length] = newEntry;
length++;

return true;
} // end add

