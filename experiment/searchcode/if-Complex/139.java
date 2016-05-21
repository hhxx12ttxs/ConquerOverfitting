            // we want complex layout even if the text is all left to right.
            Object d = getProperty(TextAttribute.RUN_DIRECTION);
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
                    putProperty( I18NProperty, Boolean.TRUE);
                char[] chars = str.toCharArray();
 * You should have received a copy of the GNU General Public License version
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
                if (SwingUtilities2.isComplexLayout(chars, 0, chars.length)) {
        // see if complex glyph layout support is needed
        if( getProperty(I18NProperty).equals( Boolean.FALSE ) ) {

