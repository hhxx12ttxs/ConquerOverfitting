import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.joda.time.LocalDateTime;
private LocalDateTime fromSqlTimestamp(@Nullable final Timestamp sqlTimestamp) {
LocalDateTime localDateTime;
if (sqlTimestamp != null) {

