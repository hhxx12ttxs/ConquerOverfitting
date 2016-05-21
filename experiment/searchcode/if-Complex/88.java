            GlyphVector gv = singleUnicodeGlyphVector;
                // Punt to the robust version of the renderer
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
            boolean complex = (fullRunGlyphVector.getLayoutFlags() != 0);
        but if we encounter complex text and/or unicode sequences we
        don't understand, we can render them using the
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
            if (gv != null) {
            if (complex || DISABLE_GLYPH_CACHE) {
                singleUnicodeGlyphVector = null; // Don't need this anymore

