import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.ibatis.type.BaseTypeHandler;
public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
throws SQLException {
if (parameter != null) {

