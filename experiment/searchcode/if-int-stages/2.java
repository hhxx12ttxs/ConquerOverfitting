public BaseStage getStage(int index)   {
if (index < stages.size()) {
return stages.get(index);
public Stage removeStage(String tag)   {
for (int x = 0; x < stages.size(); x++)    {
if (stages.get(x).tag.equals(tag))    {

