int invY = (int) Math.pow(2, tile.getZoom()) - 1 - tile.getYtile();
String sql = &quot;SELECT tile_data FROM tiles WHERE zoom_level=&quot;+tile.getZoom()+&quot; AND tile_column=&quot;+tile.getXtile()+&quot; AND tile_row=&quot;+invY+&quot; LIMIT 1&quot;;

ResultSet rs = stmt.executeQuery(sql);

