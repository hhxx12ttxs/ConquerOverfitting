import org.springframework.util.StringUtils;

public class HttpClientUtil {

public static String buildUrl(String domain, String prefix, String route) {
if (StringUtils.isEmpty(domain)) {
return null;
}
if (StringUtils.isEmpty(prefix)) {

