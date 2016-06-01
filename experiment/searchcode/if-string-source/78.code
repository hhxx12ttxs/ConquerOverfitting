public DefaultsSource(ConfigSource source, ConfigSource fallback) {
this.source		= source;
this.fallback	= fallback;
}

public boolean has(String key) {
return source.has(key) || fallback.has(key);
}

public String get(String key) {
if (source.has(key))	return source.get(key);

