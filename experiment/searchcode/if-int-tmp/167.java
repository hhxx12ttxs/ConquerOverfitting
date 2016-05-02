/* Auto-generated by genmsg_java.py for file /opt/ros/fuerte/share/geometry_msgs/msg/Pose2D.msg */

package ros.pkg.geometry_msgs.msg;

import java.nio.ByteBuffer;

public class Pose2D extends ros.communication.Message {

  public double x;
  public double y;
  public double theta;

  public Pose2D() {
  }

  public static java.lang.String __s_getDataType() { return "geometry_msgs/Pose2D"; }
  public java.lang.String getDataType() { return __s_getDataType(); }
  public static java.lang.String __s_getMD5Sum() { return "938fa65709584ad8e77d238529be13b8"; }
  public java.lang.String getMD5Sum() { return __s_getMD5Sum(); }
  public static java.lang.String __s_getMessageDefinition() { return "# This expresses a position and orientation on a 2D manifold.\n" +
"\n" +
"float64 x\n" +
"float64 y\n" +
"float64 theta\n" +
""; }
  public java.lang.String getMessageDefinition() { return __s_getMessageDefinition(); }

  public Pose2D clone() {
    Pose2D c = new Pose2D();
    c.deserialize(serialize(0));
    return c;
  }

  public void setTo(ros.communication.Message m) {
    deserialize(m.serialize(0));
  }

  public int serializationLength() {
    int __l = 0;
    __l += 8; // x
    __l += 8; // y
    __l += 8; // theta
    return __l;
  }

  public void serialize(ByteBuffer bb, int seq) {
    bb.putDouble(x);
    bb.putDouble(y);
    bb.putDouble(theta);
  }

  public void deserialize(ByteBuffer bb) {
    x = bb.getDouble();
    y = bb.getDouble();
    theta = bb.getDouble();
  }

  @SuppressWarnings("all")
  public boolean equals(Object o) {
    if(!(o instanceof Pose2D))
      return false;
    Pose2D other = (Pose2D) o;
    return
      x == other.x &&
      y == other.y &&
      theta == other.theta &&
      true;
  }

  @SuppressWarnings("all")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long tmp;
    result = prime * result + (int)((tmp = Double.doubleToLongBits(this.x)) ^ (tmp >>> 32));
    result = prime * result + (int)((tmp = Double.doubleToLongBits(this.y)) ^ (tmp >>> 32));
    result = prime * result + (int)((tmp = Double.doubleToLongBits(this.theta)) ^ (tmp >>> 32));
    return result;
  }
} // class Pose2D


