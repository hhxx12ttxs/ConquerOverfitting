int userid;
Intent intentr;
PayDAO payDAO;
int defaultMonth;
int defaultYear;
Time time;// 获取当前时间
for (int i = 0; i <= 10; i++) {
yearlist.add(String.valueOf(defaultYear - i));
}
adapter = new ArrayAdapter<String>(PayDataActivity.this,

