List<List<Data>> rows = dataset.getRows(rowDimension);
//TODO: maybe this should really be part of dataset.getRows()...
int i = 0;
List<Data> row = rows.get(i);
int j = 0;
row.add(j, new Data(rowDimension.getCategory().getLabel(s).getOrElse(s), Optional.<String>none()));

