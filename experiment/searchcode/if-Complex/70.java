 * Redistribution and use of this software and associated documentation
 * (\"Software\"), with or without modification, are permitted provided
 * that the following conditions are met:
        //-- both simple types
        if (e1Type.isSimpleType() && e2Type.isSimpleType()) {
            if (!e1Type.getName().equals(e2Type.getName())) {
                if (element != null) {
                    //-- if complex...merge definition
                    if (sInfo.complex) {
                        try {
                //XMLType type = sInfo.element.getType();
                //if ((type != null) && (type.isComplexType())) {
                //    if (type.getName() == null) {
        
        if (sInfo.complex) {
            sInfo.mixed = true;    

