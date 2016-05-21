        } else if (itemInfo.isAny() || itemInfo.getType() instanceof XmlSchemaComplexType) {
            // even for required complex elements, we leave them null.
            XmlSchemaType type = e.getValue();
            if (type instanceof XmlSchemaComplexType) {
                try {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                    if (!JavascriptUtils.notVeryComplexType(complexType) && complexType.getName() != null) {
                        complexTypeConstructorAndAccessors(complexType.getQName(), complexType);
                }
                if (!(type instanceof XmlSchemaComplexType)) {
                    // we never make classes for simple type.
                // element.
                if (!JavascriptUtils.notVeryComplexType(complexType) && complexType.getName() == null) {
                    complexTypeConstructorAndAccessors(element.getQName(), complexType);
            utils.appendLine(\"this._\" + itemInfo.getJavascriptName() + \" = [];\");

