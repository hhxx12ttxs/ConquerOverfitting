public static <T> Double getTotalRatio(List<ItemRatioBox<T>> boxes){
Double tatalRatio = 0.0;
if(boxes != null &amp;&amp; !boxes.isEmpty()){
for(ItemRatioBox<T> box : boxes){
public static <T> ItemRatioBox<T> randomOne(List<ItemRatioBox<T>> ratioBoxes, Double totalRatio){
Double randomNum = Math.random()*totalRatio;
ItemRatioBox<T> randomBox = null;
if(ratioBoxes != null &amp;&amp; !ratioBoxes.isEmpty()){

