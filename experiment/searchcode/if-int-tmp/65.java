tmp += sudoku[i][j];
}
if (tmp != sum)
status=1;

}
//		檢查每個column
for (int i = 0; i < 9; i++) {
int tmp = 0;
for (int j = 0; j < 9; j++) {
tmp += sudoku[j][i];
}
if (tmp != sum)

