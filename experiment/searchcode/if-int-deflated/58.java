import org.jin.util.io._RandomAccessFile;

public class DeflatedDataSourceMaker {

private static int FRACTIONSIZE = 0x800;
private void writeDeflatedData(DataOutput out, byte[] data, int len) throws IOException{
if(data != null){
int toWrite = baos.size() + len;

