final DimensionLink other = (DimensionLink) obj;
if (this.parentDimension != other.parentDimension &amp;&amp; (this.parentDimension == null || !this.parentDimension.equals(other.parentDimension))) {
return false;
}
if (this.childDimension != other.childDimension &amp;&amp; (this.childDimension == null || !this.childDimension.equals(other.childDimension))) {

