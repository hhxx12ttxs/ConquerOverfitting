mParams.put(&quot;gb_longitude&quot;, gb_longitude);
mParams.put(&quot;gb_channel&quot;, gb_channel);
mParams.put(&quot;gb_address&quot;, gb_address);

double distan = 0.000003524459795033278;

// default 반경 3km
distan *= 3000;
mParams.put(&quot;gb_lat_start&quot;, (Double.parseDouble(gb_latitude) - distan)

