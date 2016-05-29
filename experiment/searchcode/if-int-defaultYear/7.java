if(ConditionsProperty.YEAR_FILTER_URL_MAP.get(url) != null) {
int currentYear = (Integer) session.get(&quot;defaultYear&quot;);
int defaultYear = Integer.parseInt(((SystemConfig) dao.query(SystemConfig.class, &quot;sysconfig00014&quot;)).getValue());

