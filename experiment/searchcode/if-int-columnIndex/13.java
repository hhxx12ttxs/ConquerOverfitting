import java.sql.ResultSet;


public class ColumnWrapper {

protected int columnIndex;

public ColumnWrapper(int columnIndex){
this.columnIndex = columnIndex;
}

public String getText(ResultSet rs) throws Exception{

