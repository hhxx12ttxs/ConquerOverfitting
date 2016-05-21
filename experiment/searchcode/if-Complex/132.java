if (setDTO.getItems() != null && !setDTO.getItems().isEmpty()) {
final Map<String, ComplexConfigDTO> dtoMap = new HashMap<String, ComplexConfigDTO>();
for (final ConfigDTO item : setDTO.getItems()) {
final ComplexConfigDTO complexItem = (ComplexConfigDTO) item;
final ConfigSimpleValueDTO idProperty = complexItem.getSimpleValueProperty(idPropertyName);
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
if (complex && !polymorph) {
// merging is supported
}
dtoMap.put(idProperty.getValue(), complexItem);
}
if (idProperty == null) {
for (final ConfigSetDTO setDTO : originalDTOs) {

