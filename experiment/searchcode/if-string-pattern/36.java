import bma.common.langutil.core.StringUtil;
import bma.common.netty.webserver.Matcher;

public class MatcherCommonPattern implements Matcher {
this.pattern = StringUtil.commonPatternToRegexPattern(v);
}

@Override
public boolean match(String v) {
if (this.pattern == null)

