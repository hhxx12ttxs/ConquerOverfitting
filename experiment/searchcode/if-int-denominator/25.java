<<<<<<< HEAD
package org.drools.guvnor.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PercentageBar extends Composite
    implements
    HasValue<Integer> {

    public static final String FAILURE          = "#CC0000";
    public static final String COMPLETE_SUCCESS = "GREEN";
    public static final String INCOMPLETE       = "YELLOW";

    interface PercentageBarBinder
        extends
        UiBinder<Widget, PercentageBar> {
    }

    private static PercentageBarBinder uiBinder           = GWT.create( PercentageBarBinder.class );

    @UiField
    Label                              percentage;

    @UiField
    DivElement                         wrapper;

    @UiField
    DivElement                         text;

    @UiField
    DivElement                         bar;

    private int                        percent            = 0;
    private String                     inCompleteBarColor = FAILURE;

    public PercentageBar() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public PercentageBar(String color,
                         int width,
                         float percent) {
        this();
        setColor( color );
        setWidth( width );
        setPercent( (int) percent );
    }

    public PercentageBar(String color,
                         int width,
                         int numerator,
                         int denominator) {
        this( color,
              width,
              calculatePercent( numerator,
                                denominator ) );
    }

    private static int calculatePercent(int numerator,
                                        int denominator) {
        int percent = 0;

        if ( denominator != 0 ) {
            percent = (int) ((((float) denominator - (float) numerator) / (float) denominator) * 100);
        }

        return percent;
    }

    private void setColor(String color) {
        bar.getStyle().setBackgroundColor( color );
    }

    public void setWidth(String width) {
        setWidth( Integer.parseInt( width ) );
    }

    public void setWidth(int width) {
        text.getStyle().setWidth( width,
                                  Unit.PX );
        wrapper.getStyle().setWidth( width,
                                     Unit.PX );
    }

    public void setPercent(int percent) {
        setValue( percent );
    }

    public void setPercent(int numerator,
                           int denominator) {
        setPercent( calculatePercent( numerator,
                                      denominator ) );
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
        return addHandler( handler,
                           ValueChangeEvent.getType() );
    }

    public Integer getValue() {
        return percent;
    }

    public void setValue(Integer value) {
        setValue( value,
                  false );
    }

    public void setValue(Integer value,
                         boolean fireEvents) {

        percent = value;

        setColor();

        percentage.setText( Integer.toString( value ) + " %" );
        bar.getStyle().setWidth( value,
                                 Unit.PCT );

        if ( fireEvents ) {
            ValueChangeEvent.fire( this,
                                   value );
        }

    }

    private void setColor() {
        if ( percent < 100 ) {
            setColor( inCompleteBarColor );
        } else {
            setColor( COMPLETE_SUCCESS );
        }
    }

    public void setInCompleteBarColor(String color) {
        this.inCompleteBarColor = color;
    }
}

=======
package mobstats;

import java.util.ArrayList;
import java.util.Random;

import mobstats.entities.StatsEntity;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item that should be dropped for certain mobs in certain zones.
 * 
 * @author Justin Stauch
 * @since May 21, 2012
 * 
 * copyright 2012ÂŠ Justin Stauch, All Rights Reserved
 */
public class Drop {
    private ArrayList<ItemStack> drops;
    private int startZone, endZone, numerator, denominator;
    private ArrayList<EntityType> mobs;
    
    MobStats plugin;
    
    /**
     * Creates a new Drop with the given items, zones, and mobs.
     * 
     * @param drops List of ItemStacks to drop.
     * @param startZone Zone to start dropping the items in.
     * @param endZone Zone to be the last zone to drop the items in.
     * @param numerator The numerator to use in deciding if the items are dropped.
     * @param denominator The denominator used in deciding if the items are dropped.
     * @param mobs List of the mobs to drop for.
     */
    public Drop(ArrayList<ItemStack> drops, int startZone, int endZone, int numerator, int denominator, ArrayList<EntityType> mobs, MobStats plugin) {
        this.drops = new ArrayList<ItemStack>();
        this.mobs = new ArrayList<EntityType>();
        this.drops.addAll(drops);
        this.mobs.addAll(mobs);
        this.startZone = startZone;
        this.endZone = endZone;
        this.numerator = numerator;
        this.denominator = denominator;
        this.plugin = plugin;
    }
    
    /**
     * Drops the items if it is supposed to and the random generator tells it to.
     * 
     * @param event The event that was thrown to be used to find the required information.
     */
    public void drop(LivingEntity entity) {
        if (!(((CraftEntity) entity).getHandle() instanceof StatsEntity)) {
            throw new IllegalArgumentException("The entity wasn't changed to a proper entity.");
        }
        int level = ((StatsEntity) ((CraftEntity) entity).getHandle()).getLevel();
        if (level >= startZone && level <= endZone) {
            if (!mobs.isEmpty() && !mobs.contains(entity.getType())) {
                return;
            }
            Random random = new Random();
            int chosen = random.nextInt(denominator);
            if (chosen < numerator) {
                for (ItemStack x : drops) {
                    entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), x);
                }
            }
        }
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
