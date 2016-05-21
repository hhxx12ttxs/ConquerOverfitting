for (final ConfigMapDTO mapDTO : originalDTOs) {
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
if (!polymorph && !complex && customValueConverter == null && !simpleTypeConverterRegistry.isTypeSupported(valueType)) {
throw new RuntimeException(\"Didn't find converter for simple value type: \" + valueType); //$NON-NLS-1$
final List<ComplexConfigDTO> result = new ArrayList<ComplexConfigDTO>();
if (mapDTO.getMap() != null) {
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
final ComplexConfigDTO valueDTO = (ComplexConfigDTO) mapDTO.getMap().get(key);
ConfigDTO valueDTO;
if (complex && !polymorph) {
// merging is supported

