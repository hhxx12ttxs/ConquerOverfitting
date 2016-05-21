         * @return the name, null if none
         */
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
            // see if complex glyph layout support is needed
            if(HTMLDocument.this.getProperty(I18NProperty).equals( Boolean.FALSE ) ) {
                // we want complex layout even if the text is all left to right.
                Object d = getProperty(TextAttribute.RUN_DIRECTION);
                } else {
                    if (SwingUtilities2.isComplexLayout(data, 0, data.length)) {
                        HTMLDocument.this.putProperty( I18NProperty, Boolean.TRUE);
         *

