import java.util.Map;

public class Placement {
private int nrow;
private int ncol;
private short[] array;
private Placement() {
}

public static short[] doPlacement(int nrow, int ncol) {
int key = nrow * 1000 + ncol;

