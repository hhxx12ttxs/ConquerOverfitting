package spade;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/** Selects an option on menus. relays to game the menu selection. Also handles the displayed text
 * @author Patrick
 */
public class MenuSelector extends AnimEntity {
    
    int index = 0;
    int maxindex = 1;
    int yoffset = 0;
    Menu menu;
    Menu previous;
    TextEntity[] selections;
    ArrayList<TextEntity> others = new ArrayList<>();
    Color positive = new Color(94,212,255);
    Color negative = new Color(255,255,255);
    
    public enum Menu { TITLE, PAUSE, PAUSE_LEVEL, HELP_1, HELP_2, OPTIONS, CREDITS_1, CREDITS_2, CREDITS_3, STATS, STATS_2 }
    
    public MenuSelector(Game game) {
        super(0,0,Type.MENUSELECTOR,game);
    }
    
    public void tick() {
        super.tick();
        int lastIndex = index;
        if (game.inputhandler.getTyped(KeyEvent.VK_DOWN)) index++;
        if (game.inputhandler.getTyped(KeyEvent.VK_UP)) index--;
        if (index>maxindex) index = 0;
        if (index<0) index = maxindex;
        if (index != lastIndex) {
            selections[lastIndex].setColor(negative);
            selections[index].setColor(positive);
            Sound.blip.play();
        }
        ypos = yoffset+index*16;
        if (game.inputhandler.getTyped(KeyEvent.VK_ENTER)) game.menuSelection(index);
    }
    
    public void setState(Menu menu) {
        if (game.HUDentities.contains(this)) removeAll();
        index = 0;
        String[] titles = {"HI"};
        if (menu==Menu.PAUSE || menu==Menu.PAUSE_LEVEL || menu==Menu.TITLE) previous = menu;
        this.menu = menu;
        others.clear();
        if (menu == Menu.TITLE) {
            xpos = 24;
            yoffset = 40;
            titles = new String[]{"START @","OPTIONS","STATS","CREDITS","QUIT %"};
            others.add(new TextEntity(8,8,"SPADE",TextEntity.STATIC,game));
            others.add(new TextEntity(120,120,"PRE-ALPHA",TextEntity.STATICSMALL,game));
        }
        else if (menu == Menu.PAUSE) {
            xpos = 24;
            yoffset = 32;
            titles = new String[]{"RESUME","OPTIONS","HELP","TITLE"};
            String text = Integer.toString(SaveData.getDeaths());
            while (text.length()<5) {
                text = "0"+text;
            }
            others.add(new TextEntity(8,104,"^:"+SaveData.num_diodes+"nLEVEL HUB",TextEntity.STATICSMALL,game));
            others.add(new TextEntity(128,104,"ROBOT#n"+text,TextEntity.STATICSMALL,game));
        }
        else if (menu == Menu.PAUSE_LEVEL) {
            xpos = 24;
            yoffset = 32;
            titles = new String[]{"RESUME","OPTIONS","HELP","EXIT LEVEL"};
            others.add(new TextEntity(8,104,"^:"+SaveData.num_diodes+"nLEVEL "+game.levelindex,TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.HELP_1) {
            xpos = 32;
            yoffset = 96;
            titles = new String[]{"NEXT"};
            others.add(new TextEntity(32,8,"HELP - 1/2nnA,D - WALKnnW   - JUMPnnS   - ENTER DOORnnARROW KEYS(u,d,l,r)n    - DIG",TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.HELP_2) {
            xpos = 32;
            yoffset = 96;
            titles = new String[]{"DONE"};
            others.add(new TextEntity(32,8,"HELP - 2/2nnTHE QUICK BROWNnFOX JUMPS OVERnTHE LAZY DOG",TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.OPTIONS) {
            xpos = 16;
            yoffset = 48;
            String sound, music;
            if (Sound.mute) sound = "OFF";
            else sound = "ON";
            if (Sound.musicmute) music = "OFF";
            else music = "ON";
            titles = new String[]{"SCREEN: X"+game.canvas.scaleX,"SOUND: "+sound,"MUSIC: "+music,"DONE"};
            others.add(new TextEntity(32,16,"OPTIONS",TextEntity.STATIC,game));
        }
        else if (menu==Menu.CREDITS_1) {
            xpos = 16;
            yoffset = 96;
            titles = new String[]{"NEXT"};
            others.add(new TextEntity(8,8,"CREDITSnnPROGRAMMINGnGRAPHICS    : PATRICKnDESIGNnnSOUNDS: SFXR (DRPETTER)nnMADE INnNETBEANS IDE 7.1.2",TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.CREDITS_2) {
            xpos = 16;
            yoffset = 96;
            titles = new String[]{"NEXT"};
            others.add(new TextEntity(8,8,"TODO: MUSIC CREDITS",TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.CREDITS_3) {
            xpos = 16;
            yoffset = 96;
            titles = new String[]{"DONE"};
            others.add(new TextEntity(8,8,"SPECIAL THANKSnnWILLnCHRISnNOTCHnREDDIT GAMEDEV",TextEntity.STATICSMALL,game));   
        }
        else if (menu==Menu.STATS) {
           xpos = 16;
           yoffset = 88; 
           titles = new String[]{"DONE","ERASE DATA"};
           others.add(new TextEntity(8,8,"STATISTICS"+
                   "nn# JUMPS:     "+TextEntity.padZeroes(Integer.toString(SaveData.getJumps()),5)+
                   "nn# CREATED:   "+TextEntity.padZeroes(Integer.toString(SaveData.getCreated()),5)+
                   "nn# DESTROYED: "+TextEntity.padZeroes(Integer.toString(SaveData.getDestroyed()),5)+
                   "nn# DEATHS:    "+TextEntity.padZeroes(Integer.toString(SaveData.getDeaths()),5)+" %"
                   ,TextEntity.STATICSMALL,game));
        }
        else if (menu==Menu.STATS_2) {
            xpos = 16;
            yoffset = 80;
            titles = new String[]{"NO","YES"};
            others.add(new TextEntity(24,8,"nARE YOU SURE YOUnWANT TO ERASEnYOUR SAVE FILE? %",TextEntity.STATICSMALL,game));
            index = 0;
        }
        
        maxindex = titles.length-1;
        selections = new TextEntity[maxindex+1];
        for (int n=0;n<selections.length;n++) {
            selections[n] = new TextEntity(xpos,yoffset+16*n,titles[n],TextEntity.STATIC,game);
            selections[n].setColor(negative);
            game.HUDentities.add(selections[n]);
        }
        for (int n=0;n<others.size();n++) {
            game.HUDentities.add(others.get(n));
        }
        game.HUDentities.add(this);
        xpos-=16;
        selections[index].setColor(positive);
    }
    
    public void removeAll() {
        game.HUDentities.remove(this);
        for (int n=0;n<selections.length;n++) {
            game.HUDentities.remove(selections[n]);
        }
        for (int n=0;n<others.size();n++) {
            game.HUDentities.remove(others.get(n));
        }
        others.clear();
    }
    
    // update the text on the options menu, such as screen resolution number, etc. right after you change it.
    public void updateOptions() {
        String sound, music;
        if (Sound.mute) sound = "OFF";
        else sound = "ON";
        if (Sound.musicmute) music = "OFF";
        else music = "ON";
        String[] titles = new String[]{"SCREEN: X"+game.canvas.scaleX,"SOUND: "+sound,"MUSIC: "+music,"DONE"};
        game.HUDentities.remove(selections[index]);
        selections[index] = new TextEntity(xpos+16,yoffset+16*index,titles[index],TextEntity.STATIC,game);
        selections[index].setColor(positive);
        game.HUDentities.add(selections[index]);
    }
    
    public Menu getMenu() {
        return menu;
    }
    
}

