public class PacketBlink implements IMessage, IMessageHandler<PacketBlink, IMessage>
{

private double targetX, targetY, targetZ;

public PacketBlink()
{
}

public PacketBlink(double targetX, double targetY, double targetZ)
{
this.targetX = targetX;
this.targetY = targetY;

