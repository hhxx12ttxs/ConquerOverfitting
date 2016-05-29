public SparseFieldMatrix(final Field<T> field,
final int rowDimension, final int columnDimension) {
this.rows = rowDimension;
this.columns = columnDimension;
entries = new OpenIntToFieldHashMap<T>(field);

