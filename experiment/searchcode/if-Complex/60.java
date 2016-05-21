        CorbaTypeImpl corbatype = null;
        if (isLiteralArray(complex)) {
            corbatype = processLiteralArray(complex, defaultName, anonymous);
        } else if (WSDLTypes.isOMGUnion(complex)) {
            corbatype = processOMGUnion(complex, defaultName);
            // need to determine if its a primitive type.
            if (stype instanceof XmlSchemaComplexType) {
        if (stype instanceof XmlSchemaComplexType) {
            complex = (XmlSchemaComplexType)stype;
            if (!isLiteralArray(complex)
                && !WSDLTypes.isOMGUnion(complex)
        } else if (WSDLTypes.isUnion(complex)) {
            corbatype = processRegularUnion(complex, defaultName);

