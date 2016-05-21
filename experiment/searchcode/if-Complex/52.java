complex = true;
break;
if (complex) {
plan = new ComplexAccessPlan(clazz, propertyNames);
} else if (Map.class.isAssignableFrom(clazz)) {
plan = new MapAccessPlan(clazz, propertyNames);
boolean complex = false;
if (clazz == null || propertyNames == null) {
complex = true;
} else {
for (int i = 0; i < propertyNames.length; i++) {
if (propertyNames[i].indexOf('[') > -1 || propertyNames[i].indexOf('.') > -1) {

