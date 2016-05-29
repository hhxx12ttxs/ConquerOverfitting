public class IPv4UnicastNLRI
{
@Getter
private NetworkLayerReachabilityInformation address;

public IPv4UnicastNLRI(int pfxlen, byte[] data)
{
this.address = new NetworkLayerReachabilityInformation(pfxlen, NLRIHelper.trimNLRI(pfxlen, data));

