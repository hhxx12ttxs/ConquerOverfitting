public class ParkFeeIf {
public static void main(String[] argv) {
int hours = 0;
int fee = 0;

// 轉換為 int
hours = Integer.parseInt(argv[0]);

if(hours > 6) { // 先計算超過6小時的部分

