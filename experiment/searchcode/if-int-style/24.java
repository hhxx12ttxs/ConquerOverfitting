package org.polaris.framework.report.excel.style;

import java.util.*;

/**
* 样式的集合,放在内存中
public Style getStyle(String styleName)
{
Style style = map.get(styleName);
if (style == null)
return new Style();
return (Style) style.clone();

