List<List> sublistgroup = new ArrayList<List>();
int listsize = list.size();
if (listsize < sublistsize) {
sublistgroup.add(list);
} else {
for (int i = 0; i < listsize; i += sublistsize) {
int j = i + sublistsize;

