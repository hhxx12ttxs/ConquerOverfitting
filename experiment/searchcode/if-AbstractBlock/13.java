if (other == null) {
return false;
}

if (!(other instanceof AbstractBlock)) {
return false;
}

AbstractBlock otherBlock = (AbstractBlock)other;

if (otherBlock.row != this.row) {

