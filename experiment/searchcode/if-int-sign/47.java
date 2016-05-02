package com.aholacraft.lololmaker.aholatrade;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class PlayerSignListener implements Listener {
    Main pl;
    String WrongFormat = ChatColor.DARK_RED + "The layout of AholaTrade sign is wrong !";
    String CorruptSign = ChatColor.DARK_RED + "The AholaTrade sign is corrupted, inform admin !";
    String NoCreatePerm = ChatColor.DARK_RED + "You are not allowed to create AholaTrade signs !";
    String NoUsePerm = ChatColor.DARK_RED + "You are not allowed to use AholaTrade signs !";
    String NoItem = ChatColor.DARK_RED + "You must be holding \"selling\" item in hand !";
    String NoInvPlace = ChatColor.DARK_RED + "You don't have any slots left in inventory !";
    String NoAmount = ChatColor.DARK_RED + "You don't have enough of selling items in hand !";
    String traded = ChatColor.DARK_GREEN + "Successfully traded ";
    public PlayerSignListener(Main main) {
        pl = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent e){
        //Check if created sign is AholaTrade
        if(!e.getLine(0).equalsIgnoreCase("[AholaTrade]")) return;


        //Check if player has permission to create AholaTrade signs
        if(!e.getPlayer().hasPermission("aholatrade.create"))
        {e.getPlayer().sendMessage(NoCreatePerm); e.getBlock().breakNaturally(); return;}


        //Check if layout is correct
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        int length1 = line1.split(":").length;
        int length2 = line2.split(":").length;
        if(line1 == "" || (length1 != 2 && length1 != 3) || (length2 != 2 && length2 != 3)) {
            e.getPlayer().sendMessage(WrongFormat);
            e.getBlock().breakNaturally();
            return;
        }

        int i1, i2, a1, a2;
        Short dmg1, dmg2;

        ItemStack item1;
        ItemStack item2;

        if(length1 == 3){
            i1 = Integer.parseInt(line1.split(":")[0]);
            dmg1 = Short.parseShort(line1.split(":")[1]);
            a1 = Integer.parseInt(line1.split(":")[2]);

            item1 = new ItemStack(i1, a1, dmg1);
        }else{
            i1 = Integer.parseInt(line1.split(":")[0]);
            a1 = Integer.parseInt(line1.split(":")[1]);

            item1 = new ItemStack(i1, a1);
        }

        if(length2 == 3){
            i2 = Integer.parseInt(line2.split(":")[0]);
            dmg2 = Short.parseShort(line2.split(":")[1]);
            a2 = Integer.parseInt(line2.split(":")[2]);

            item2 = new ItemStack(i2, a2, dmg2);
        }else{
            i2 = Integer.parseInt(line2.split(":")[0]);
            a2 = Integer.parseInt(line2.split(":")[1]);

            item2 = new ItemStack(i2, a2);
        }

        e.setLine(0, ChatColor.DARK_GREEN + "[AholaTrade]");

        Location infoLoc;

        switch(pl.getInfoMode()){
            case 1:
                infoLoc = e.getBlock().getLocation().add(0D, 1D, 0D);
                break;
            case 2:
                infoLoc = e.getBlock().getLocation().subtract(0D, 1D, 0D);
                break;
            default:
                return;
        }


        Block infoBlock = infoLoc.getBlock();
        infoBlock.setType(Material.WALL_SIGN);
        Sign infoSign = (Sign) infoBlock.getState();

        MaterialData face = new MaterialData(e.getBlock().getType(), e.getBlock().getData());
        infoSign.setData(face);

        infoSign.setLine(0, a1 + "x ");
        infoSign.setLine(1, item1.getType().name());
        infoSign.setLine(2, a2 + "x ");
        infoSign.setLine(3, item2.getType().name());

        infoSign.update();
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        //Define player for later use
        Player p = e.getPlayer();


        //Check if block is left mouse clicked
        if(!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;


        //Check if interacted with a sign
        if(!(e.getClickedBlock().getState() instanceof Sign)) return;


        //Check if sign is AholaTrade
        Sign sign = (Sign) e.getClickedBlock().getState();
        if(!(sign.getLine(0).contains("[AholaTrade]"))) return;
        e.setCancelled(true);

        //Check if player has permissions to trade
        if(!p.hasPermission("aholatrade.trade")) {p.sendMessage(NoUsePerm); return;}

        int i1, i2, a1, a2;
        Short dmg1, dmg2;

        if(sign.getLine(1).split(":").length == 3){
            i1 = Integer.parseInt(sign.getLine(1).split(":")[0]);
            dmg1 = Short.parseShort(sign.getLine(1).split(":")[1]);
            a1 = Integer.parseInt(sign.getLine(1).split(":")[2]);
        }else if(sign.getLine(1).split(":").length == 2){
            i1 = Integer.parseInt(sign.getLine(1).split(":")[0]);
            a1 = Integer.parseInt(sign.getLine(1).split(":")[1]);
            dmg1 = -1;
        }else{
            p.sendMessage(CorruptSign);
            return;
        }

        if(sign.getLine(2).split(":").length == 3){
            i2 = Integer.parseInt(sign.getLine(2).split(":")[0]);
            dmg2 = Short.parseShort(sign.getLine(2).split(":")[1]);
            a2 = Integer.parseInt(sign.getLine(2).split(":")[2]);
        }else if(sign.getLine(2).split(":").length == 2){
            i2 = Integer.parseInt(sign.getLine(2).split(":")[0]);
            a2 = Integer.parseInt(sign.getLine(2).split(":")[1]);
            dmg2 = -1;
        }else{
            p.sendMessage(CorruptSign);
            return;
        }

        ItemStack i1stack;
        ItemStack i2stack;

        if(dmg1 != -1){
            i1stack = new ItemStack(i1, 1, dmg1);
        }else{
            i1stack = new ItemStack(i1, 1);
        }

        if(dmg2 != -1){
            i2stack = new ItemStack(i2, 1, dmg2);
        }else{
            i2stack = new ItemStack(i2, 1);
        }

        Byte inHandData = p.getItemInHand().getData().getData();
        Byte i1stackData = i1stack.getData().getData();
        Byte i2stackData = i2stack.getData().getData();

        //Check which item is in hand for trade
        if(inHandData == i1stackData && p.getItemInHand().getType() == i1stack.getType()){
            tryTrade(p, i2, dmg2, a1, a2);
        }else if(inHandData == i2stackData && p.getItemInHand().getType() == i2stack.getType()){
            tryTrade(p, i1, dmg1, a2, a1);
        }else{
            p.sendMessage(NoItem);
        }
    }

    @SuppressWarnings("deprecation")
    public void tryTrade (Player p, int item, int dataValue, int take, int give){
        //Check if player has got free inventory space
        if(!CheckSpace(p,item, give, dataValue)) {p.sendMessage(NoInvPlace); return;}

        ItemStack inHand = p.getItemInHand();

        //TRADE !!
        if(p.getItemInHand().getAmount() > take){

            if(!(dataValue > -2 && dataValue < 1)){
                Short data = new Short(dataValue + "");
                p.getInventory().addItem(new ItemStack(Material.getMaterial(item), give, data));

                p.sendMessage(traded + take + " " + inHand.getType().name() + " for " +
                        give + " " + Material.getMaterial(item).name() + ":" + dataValue);
            }else{
                p.getInventory().addItem(new ItemStack(Material.getMaterial(item), give));

                p.sendMessage(traded + take + " " + inHand.getType().name() + " for " +
                        give + " " + Material.getMaterial(item).name());
            }
            ItemStack NewStack = inHand.getData().toItemStack(inHand.getAmount() - take);
            p.setItemInHand(NewStack);
        }else if(p.getItemInHand().getAmount() < take){

            p.sendMessage(NoAmount);

        }else{

            if(dataValue != -1){
                Short data = new Short(dataValue + "");
                p.getInventory().addItem(new ItemStack(Material.getMaterial(item), give, data));
            }else{
                p.getInventory().addItem(new ItemStack(Material.getMaterial(item), give));
            }

            p.setItemInHand(null);
        }
        p.updateInventory();
    }


    public boolean CheckSpace(Player TestSubject, int TestItem, int Amount, int dataValue){
        Inventory inv = TestSubject.getInventory();

        ItemStack item;

        if(dataValue != -1){
            Short datavalue = new Short(Amount + "");
            item = new ItemStack(Material.getMaterial(TestItem), Amount, datavalue);
        }else{
            item = new ItemStack(Material.getMaterial(TestItem), Amount);
        }



        for(ItemStack is : inv){
            if(is == null){
                return true;
            }else if(is.getType() == item.getType() && is.getMaxStackSize() >= (is.getAmount() + item.getAmount())){
                return true;
            }
        }

        return false;
    }
}

