public class AggColumnDistinctMapper implements Mapper<String, String, String> {

private int columnindex;

public AggColumnDistinctMapper(int columnindex) {
this.columnindex = columnindex;
}

public Pair<String, String> map(String line) {

