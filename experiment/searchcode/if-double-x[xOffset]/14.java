package infighter2.entity;

import infighter2.Graphics;
import infighter2.TextureID;
import infighter2.effect.CooldownEffect;
import infighter2.effect.DefendEffect;
import infighter2.effect.SpellEffect;
import infighter2.entity.spell.Spell;
import java.util.ArrayList;
import java.util.List;

public class Creature extends Entity {

    public Orientation orientation = Orientation.RIGHT;
    protected TextureID ltex, rtex;
    public List<SpellEffect> spellEffects = new ArrayList<SpellEffect>();
    public int mana = 50;
    protected int maxMana = 100;
    protected int manaRegenValue = 10;
    protected int manaRegenRate = 30;
    protected int attackPower = 5, attackSpeed = 60, attackTimer = 0;

    public enum Orientation {

        LEFT, RIGHT
    }

    public Creature(double x, double y) {
        super(x, y);
    }

    public void render() {
        TextureID renderTex = null;

        switch (orientation) {
            case LEFT:
                renderTex = ltex;
                break;
            case RIGHT:
                renderTex = rtex;
        }

        renderTex.render(x - renderTex.usedw / 2, y - renderTex.usedh);

        renderBars(renderTex.usedw / 2, renderTex.usedh);
    }

    private void renderBars(double xoffset, double yoffset) {
        Graphics.renderRect(x - 10 - xoffset, y - 7 - yoffset, 50, 2, 0xff4444);
        Graphics.renderRect(x - 10 - xoffset, y - 4 - yoffset, 50, 2, 0x4444ff);

        Graphics.renderRect(x - 10 - xoffset, y - 7 - yoffset, 50 * ((double) health / maxHealth), 2, 0xff0000);
        Graphics.renderRect(x - 10 - xoffset, y - 4 - yoffset, 50 * ((double) mana / maxMana), 2, 0x0000ff);
    }

    public void cast(Spell spell) {
        if (!canCast()) {
            return;
        }
        spell.register();
    }

    protected void move(double dx, double dy) {
        if (!canWalk()) {
            return;
        }
        x += dx;
        y += dy;
    }

    public boolean canCast() {
        for (SpellEffect s : spellEffects) {
            if (s.blocksCast() == true) {
                return false;
            }
        }

        return true;
    }

    public boolean canCast(int manaCost) {
        if(mana >= manaCost && canCast()){
            mana -= manaCost;
            return true;
        }
        
        return false;
    }

    protected boolean canWalk() {
        for (SpellEffect s : spellEffects) {
            if (s.blocksMovement() == true) {
                return false;
            }
        }

        return true;
    }

    public void tick() {
        super.tick();

        if (ticks % healthRegenRate == 0) {
            health += healthRegenValue;
            if (health > maxHealth) {
                health = maxHealth;
            }
        }

        if (ticks % manaRegenRate == 0) {
            mana += manaRegenValue;
            if (mana > maxMana) {
                mana = maxMana;
            }
        }
    }

    protected void attack() {
        if (attackTimer > 0) {
            return;
        }
        int xoffset = 0;

        switch (orientation) {
            case LEFT:
                xoffset = -20;
                break;
            case RIGHT:
                xoffset = 20;
                break;
        }

        List<Entity> attackedEntities = Handler.findEntities(this.x + xoffset, this.y);
        Handler.removeTeammates(attackedEntities, this);
        Handler.hurtGroup(attackedEntities, attackPower);

    }

    protected void remove() {
        super.remove();
        for (SpellEffect se : (List<SpellEffect>) ((ArrayList<SpellEffect>) spellEffects).clone()) {
            se.remove();
        }
    }

    protected void defend() {
        DefendEffect de = new DefendEffect(this);
        de.register();
        cooldown();
    }
    
    public void cooldown(){
        CooldownEffect ce = new CooldownEffect(this);
        ce.register();
    }

    public SpellEffect isDefending() {
        for (SpellEffect s : spellEffects) {
            if (s.defending() == true) {
                return s;
            }
        }
        return null;
    }

    void hurt(int damage) {
        SpellEffect s = isDefending();
        if (s != null) {
            s.remove();
            return;
        }
        
        super.hurt(damage);
    }
}

