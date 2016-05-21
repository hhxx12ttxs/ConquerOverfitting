            Property property = complex.getProperty(toTypeName(name));
            if (property != null && !(property instanceof ComplexAttribute)) {
                return property.getValue();
            if (\"id\".equals(name.getLocalPart())) {
                return complex.getIdentifier();
            }
                complexAtts = (Collection) object;
            } else if (object instanceof ComplexAttribute) {
                // get collection of features from this attribute
            for (Object complex : complexAtts) {
                if (complex instanceof ComplexAttribute) {
                    PropertyDescriptor descriptor = ((Attribute) complex).getDescriptor();
    public Object getProperty(Object object, QName name) throws Exception {
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;

