private static HarmonyStrategyFactory instance = null;

public static HarmonyStrategyFactory getInstance(){
if (instance == null) {
public HarmonyStrategy getHarmonyStategy(String strategy){
if (strategy.equals(&quot;PitchSet&quot;)) {
return new PitchSetStrategy();

