public AreaNode getPreviousSibling() {
NonLeafAreaNode p = getParent();
if (p != null) {
AreaNode cPrev = null;
AreaNode cPrev = null;
for (AreaNode c : p.getChildren()) {

