int waterOrFoodInQuadrant = GridSearchFramework.isWaterOrFoodInQuadrant(entityQuadrantPosition, attachedEntity.worldObj);
if(waterOrFoodInQuadrant == 0 || waterOrFoodInQuadrant == 2){
Long readdangerRating = (Long) quadrantJSONData.get(&quot;waterandfood&quot;);
if(readdangerRating == 1 || readdangerRating == 3){

