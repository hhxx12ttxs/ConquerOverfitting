public class LocationAuxItem extends AuxItem {
double longitude;
double latitude;
double accuracy;
public LocationAuxItem(int tag, double latitude, double longitude, double accuracy) {
longitude = Double.parseDouble(longstr);
if (accstr != null) accuracy = Double.parseDouble(accstr);
} catch (NumberFormatException ex1) {

