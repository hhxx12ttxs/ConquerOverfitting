import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

public class LnsExtraData implements Writable {

private static final Logger LOG = Logger.getLogger(LnsExtraData.class);

private int maxNeighborhoods = 50;

