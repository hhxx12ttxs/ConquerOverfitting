showEndTitle();
}

// 西暦を和暦に変換する
void convertYear() {
int year;
String japaneseYear;

year = inputYear();
showResult(year, japaneseYear);
}

// 西暦を入力する
int inputYear() {
int year;
System.out.println(&quot;西暦を入力してください&quot;);
year = Input.getInt();

