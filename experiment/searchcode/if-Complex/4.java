                complexAliasMember.makeAliasColName(colNameBuffer, fieldTypeBuffer, modelViewEntity, modelReader);
                String nodeName = subElement.getNodeName();
                if (\"complex-alias\".equals(nodeName)) {
                    this.addComplexAliasMember(new ComplexAlias(subElement));
            ModelViewEntity.ModelAlias alias = it.next();
            if (alias.isComplexAlias()) {
                // TODO: conversion for complex-alias needs to be implemented for cache and in-memory eval stuff to work correctly
                complexAliasMember = new ComplexAlias(complexAliasElement);
            Element complexAliasElement = UtilXml.firstChildElement(aliasElement, \"complex-alias\");
            if (complexAliasElement != null) {
            if (alias.isComplexAlias()) {
                // if this is a complex alias, make a complex column name...
                StringBuilder colNameBuffer = new StringBuilder();
        public void makeAliasColName(StringBuilder colNameBuffer, StringBuilder fieldTypeBuffer, ModelViewEntity modelViewEntity, ModelReader modelReader) {
            if (complexAliasMember != null) {

