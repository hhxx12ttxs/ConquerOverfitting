private int total;

//开始条数
private int startRow;

//结束条数
private int endRow;

public PageUtil(int page, int rows, int total) {
if(page < 1){
page = 1;

