//Отработка и закрытие незаконченных комбинаций компьютера.
//По строкам.
if(!moveComplete) {
for (int i = 0; i < Field.FIELD_SIZE; i++) {
if(Field.field[i][0] == Field.COMPUTER_SYMBOL &amp;&amp; Field.field[i][1] == Field.COMPUTER_SYMBOL &amp;&amp; Field.field[i][2] == Field.DEFAULT_SYMBOL) {

