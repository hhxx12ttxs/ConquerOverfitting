XMLAdapter.writeElement( writer, gns, &quot;CRS&quot;, crs );
if ( !( Double.isInfinite( l.minscale ) &amp;&amp; Double.isInfinite( l.maxscale ) ) ) {
writer.writeStartElement( lns, &quot;ScaleDenominators&quot; );

