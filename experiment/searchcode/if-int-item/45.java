public static int chargeAmount(ItemStack item, int get, boolean isSimulate) {
if (item == null)
return 0;

if (item.getItem() instanceof IEnergyContainerItem) {
public static int dischargeAmount(ItemStack item, int ret, boolean isSimulate) {
if (item == null)
return 0;

if (item.getItem() instanceof IEnergyContainerItem) {

