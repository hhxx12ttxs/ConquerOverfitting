// 表からデータを取得
ArrayList<Double> ranking = cn.select(tableName);

double score1 = ranking.get(0);
double score2 = ranking.get(1);
return cn.select(tableName);
}else if(score > score8){
// 9位よりも速いが、8位よりは遅かった場合
// score→9位、9位を10位
cn.update(tableName,score,9);

