public int resolveType(int cacheId, Element el, TypeResult result) {
Element element = el;
if (expression != null) {
numEval++;
if (!expression.eval(cacheId, element))
return cacheId;

