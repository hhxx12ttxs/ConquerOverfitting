public class Paintbrush implements IInstrument{

@Override
public void use(Icon icon, int i, int j, Color color, int dimension) {

if(dimension!=1){
dimension--;
for (int k = i-dimension; k < i+dimension; k++) {
for (int k2 = j-dimension; k2 < j+dimension; k2++) {

