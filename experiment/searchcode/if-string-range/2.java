package ipfilter;

public class IpRange {
private String range;
public IpRange(String range) {
this.range = range;
}

public boolean ipIn(String ip) {
return false;

