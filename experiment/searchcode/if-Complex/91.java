                //
                Class<?> clazz = complex.getClass();
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
                    for (Object pd: pds) {
                        if (BeansHelper.getPropertyName(pd).equals(element)) {
                            readMethod = BeansHelper.getReadMethod(pd);
 * You should have received a copy of the GNU General Public License version
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
                Method readMethod = null;
        try {
            if (complex.getClass().isArray() && element.equals(\"length\")) {
                return Array.getLength(complex);
            } else if (complex instanceof CompositeData) {
                return ((CompositeData) complex).get(element);

