labels[k][m].setText(String.valueOf(S.table[k][m].value));
}
S=S.phase1();
if(S.Unknown_values()==0){ Sudoku=S; post_sudoku(Sudoku); completed=true; lblState.setForeground(new Color(0,153,0)); lblState.setText(&quot;New Sudoku has been generated for you!&quot;);
//<editor-fold defaultstate=&quot;collapsed&quot; desc=&quot; Look and feel setting code (optional) &quot;>
/*
* If Nimbus (introduced in Java SE 6) is not available, stay with the

