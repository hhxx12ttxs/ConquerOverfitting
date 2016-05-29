@Override
public void actionClient(Connection connection, ClientLogic logic) {
if(inv == null) return;

HasInventory hasinv = GuiRegistry.instance().getInventory(inv.invId, logic.world, inv.invX, inv.invY);

