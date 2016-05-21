                singleUnicodeGlyphVector = null; // Don't need this anymore
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
            GlyphVector gv = singleUnicodeGlyphVector;
        but if we encounter complex text and/or unicode sequences we
        don't understand, we can render them using the
            boolean complex = (fullRunGlyphVector.getLayoutFlags() != 0);
            if (complex || DISABLE_GLYPH_CACHE) {
        // If this TextData represents a single glyph, this is its
        // unicode ID
                // Punt to the robust version of the renderer
            if (gv != null) {

