private Long xmId;//ÏûÏ¢id
private Long kuId;//ÊÕ¼þÈËid

// Constructors

/** default constructor */
public int hashCode() {
int result = 17;

result = 37 * result
+ (getXmId() == null ? 0 : this.getXmId().hashCode());

