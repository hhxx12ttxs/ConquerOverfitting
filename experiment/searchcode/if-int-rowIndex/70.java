public List<Integer> getRow(int rowIndex) {

rowIndex++; // 第rowIndex的实质数据数目

if (rowIndex < 0) {
return null;
}

List<Integer> result = new ArrayList<>(rowIndex);

if (rowIndex >= 1) {

