arr[i] = Integer.parseInt(array[i]);
}
for (int i = 0; i < arr.length; i++) {
list.add(arr[i]);
}
int listSize = list.size();
System.out.println(listSize);
listSize =0;

Iterator iter = list.iterator();
int min = Collections.min(list);

