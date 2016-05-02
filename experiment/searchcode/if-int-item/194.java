package nz.uberutils.methods;

import com.rsbuddy.script.methods.Keyboard;
import com.rsbuddy.script.methods.Mouse;
import com.rsbuddy.script.task.Task;
import com.rsbuddy.script.util.Random;
import com.rsbuddy.script.util.Timer;
import com.rsbuddy.script.wrappers.Component;
import com.rsbuddy.script.wrappers.Item;
import nz.uberutils.helpers.Utils;
import nz.uberutils.wrappers.BankItem;
import org.rsbuddy.tabs.Inventory;
import org.rsbuddy.widgets.Bank;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Taylor
 * Date: 5/17/11
 * Time: 6:38 PM
 * Package: nz.uberutils.methods;
 */
public class UberBanking
{
    public static boolean makeInventoryCount(final Item i, final int count) {
        if (!Bank.isOpen() || i == null)
            return false;
        if (count != 0) {
            int invCount = Inventory.getCount(true, i.getId());
            if (invCount != count) {
                if (invCount < count)
                    return withdraw(i, count - invCount);
                else
                    return deposit(UberInventory.getItem(i.getId()), invCount - count);
            }
            else
                return true;
        }
        else {
            if (Bank.getCount(i.getId()) > 0)
                return withdraw(i.getId(), 0);
            else
                return Inventory.getCount(i.getId()) > 0;
        }
    }

    public static boolean makeInventoryCount(final int id, final int count) {
        return makeInventoryCount(Bank.getItem(id), count);
    }

    public static boolean makeInventoryCount(final String name, final int count) {
        return makeInventoryCount(getItem(name), count);
    }

    /**
     * If bank is open, deposits specified amount of an item into the bank.
     *
     * @param id     The Name of the item.
     * @param number The amount to deposit. 0 deposits All. 1,5,10 deposit
     *               corresponding amount while other numbers deposit X.
     * @return <tt>true</tt> if successful; otherwise <tt>false</tt>.
     */
    public static boolean deposit(int id, int number) {
        return deposit(Inventory.getItem(id), number);
    }

    /**
     * If bank is open, deposits specified amount of an item into the bank.
     *
     * @param name   The Name of the item.
     * @param number The amount to deposit. 0 deposits All. 1,5,10 deposit
     *               corresponding amount while other numbers deposit X.
     * @return <tt>true</tt> if successful; otherwise <tt>false</tt>.
     */
    public static boolean deposit(String name, int number) {
        return deposit(UberInventory.getItem(name), number);
    }

    /**
     * If bank is open, deposits specified amount of an item into the bank.
     *
     * @param iItem  The Item.
     * @param number The amount to deposit. 0 deposits All. 1,5,10 deposit
     *               corresponding amount while other numbers deposit X.
     * @return <tt>true</tt> if successful; otherwise <tt>false</tt>.
     */
    public static boolean deposit(Item iItem, int number) {
        if (Bank.isOpen()) {
            if (number < 0) {
                throw new IllegalArgumentException("number < 0 (" + number + ")");
            }
            int invCount = Inventory.getCount(true);
            if (iItem == null)
                return false;
            Component item = iItem.getComponent();
            int itemCount = Inventory.getCount(true, iItem.getId());
            if (item == null) {
                return true;
            }
            if (number == Inventory.getCount(true, iItem.getId()))
                number = 0;
            switch (number) {
                case 0: // Deposit All
                    if (item.interact(itemCount > 1 ? "Deposit-All" : "Deposit"))
                        break;
                    else
                        return false;
                case 1:
                    if (item.interact("Deposit"))
                        break;
                    else
                        return false;
                case 5:
                    if (item.interact("Deposit-" + number))
                        break;
                    else
                        return false;
                default: // Deposit x
                    if (!item.interact("Deposit-" + number)) {
                        if (item.interact("Deposit-X")) {
                            Task.sleep(Random.nextInt(1000, 1300));
                            Keyboard.sendText(String.valueOf(number), true);
                        }
                        else
                            return false;
                    }
                    break;
            }
            for (int i = 0; i < 1500; i += 20) {
                Task.sleep(20);
                int cInvCount = Inventory.getCount(true);
                if (cInvCount < invCount || cInvCount == 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Tries to withdraw an item. 0 is All. 1,5,10 use Withdraw 1,5,10 while
     * other numbers Withdraw X.
     *
     * @param itemId The ID of the item.
     * @param count  The number to withdraw.
     * @return <tt>true</tt> on success.
     */
    public static boolean withdraw(final int itemId, final int count) {
        return withdraw(Bank.getItem(itemId), count);

    }

    /**
     * Tries to withdraw an item. 0 is All. 1,5,10 use Withdraw 1,5,10 while
     * other numbers Withdraw X.
     *
     * @param name  The name of the item.
     * @param count The number to withdraw.
     * @return <tt>true</tt> on success.
     */
    public static boolean withdraw(final String name, final int count) {
        return withdraw(getItem(name), count);
    }

    /**
     * Tries to withdraw an item. 0 is All. 1,5,10 use Withdraw 1,5,10 while
     * other numbers Withdraw X.
     *
     * @param rsi   The Item to withdraw.
     * @param count The number to withdraw.
     * @return <tt>true</tt> on success.
     */
    public static boolean withdraw(final Item rsi, final int count) {
        if (Bank.isOpen()) {
            if (count < 0)
                throw new IllegalArgumentException("count (" + count + ") < 0");
            if (rsi == null)
                return false;
            Component item = rsi.getComponent();
            if (item == null)
                return false;
            if (item.getRelLocation().equals(new Point(0, 0))) {
                Bank.getWidget().getComponent(Bank.COMPONENT_BANK_TABS[0]).click();
                Task.sleep(1000, 1300);
            }
            Component container = Bank.getWidget().getComponent(93);
            if (!container.getViewportRect().contains(item.getBoundingRect())) {
                Point p = container.getAbsLocation();
                Rectangle r = container.getViewportRect();
                Mouse.move(Random.nextGaussian(p.x, p.y + r.width, r.width / 2),
                           Random.nextGaussian(p.y, p.y + r.height, r.height / 2));
                Timer limit = new Timer(5000);
                while (!container.getViewportRect().contains(item.getBoundingRect()) && limit.isRunning()) {
                    Mouse.scroll(item.getAbsLocation().y < container.getAbsLocation().y);
                    Task.sleep(20, 150);
                }
            }
            if (!container.getBoundingRect().contains(item.getBoundingRect()))
                return false;
            int invCount = Inventory.getCount(true);
            switch (count) {
                case 0:
                    item.interact("Withdraw-All");
                    break;
                case 1:
                    item.click(true);
                    break;
                case 5:
                case 10:
                    item.interact("Withdraw-" + count);
                    break;
                default:
                    String exactAction = "Withdraw-" + count;
                    boolean hasAction = false;
                    for (final String action : rsi.getComponent().getActions()) {
                        if (action != null && action.equalsIgnoreCase(exactAction)) {
                            hasAction = true;
                        }
                    }
                    if (!hasAction || !item.interact("Withdraw-" + count)) {
                        if (item.interact("Withdraw-X")) {
                            Task.sleep(Random.nextInt(1000, 1300));
                            Keyboard.sendText(String.valueOf(count), true);
                        }
                    }
            }
            for (int i = 0; i < 1500; i += 20) {
                Task.sleep(20);
                int newInvCount = Inventory.getCount(true);
                if (newInvCount > invCount || Inventory.isFull())
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets the first item with the provided Name in the bank.
     *
     * @param name Name of the item to get.
     * @return The component of the item; otherwise null.
     */
    public static Item getItem(final String name) {
        if (Bank.isOpen()) {
            final Item[] items = Bank.getItems();
            if (items != null) {
                for (final Item item : items) {
                    if (item.getName().equalsIgnoreCase(name) ||
                        item.getName().toLowerCase().contains(name.toLowerCase()))
                        return item;
                }
            }
        }
        return null;
    }

    public static void depositAllExcept(BankItem... bis) {
        if (!Bank.isOpen())
            return;
        boolean stop = false;
        while (!stop && Bank.isOpen()) {
            stop = true;
            for (Item i : UberInventory.getItems()) {
                if (!Utils.arrayContains(bis, i.getName()) && !Utils.arrayContains(bis, i.getId())) {
                    deposit(i.getId(), 0);
                    stop = false;
                }
            }
        }
    }

    public static boolean doBanking(BankItem... bis) {
        if (!Bank.isOpen())
            Bank.open();
        UberBanking.depositAllExcept(bis);
        int count = 0;
        for(BankItem bi : bis)
         count += bi.getQuantity();
        for (BankItem bi : bis) {
            if ((!UberInventory.contains(bi.getId()) && !UberInventory.contains(bi.getName())) || bi.inventoryCount() != bi.getQuantity()) {
                if (bi.getId() != -1)
                    UberBanking.makeInventoryCount(bi.getId(), bi.getQuantity());
                else
                    UberBanking.makeInventoryCount(bi.getName(), bi.getQuantity());
            }
        }
        boolean close = true;
        for (BankItem bi : bis) {
            if (bi.getId() != -1) {
                if (!UberInventory.contains(bi.getId()))
                    close = false;
            }
            else if (!UberInventory.contains(bi.getName()))
                close = false;
            if(bi.inventoryCount() != bi.getQuantity())
                close = false;
        }
        if (Inventory.getCount(true) != count)
            close = false;
        if (close) {
            Bank.close();
            return true;
        }
        return false;
    }

}

