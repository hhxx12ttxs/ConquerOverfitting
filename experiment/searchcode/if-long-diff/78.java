/*
 * Lisans bilgisi icin lutfen proje ana dizinindeki zemberek2-lisans.txt dosyasini okuyunuz.
 */

package net.zemberek.araclar;

import java.util.HashMap;

/**
 * Hassas kronometre ihtiyaçlar? için tasarlanm??t?r.
 * <p/>
 * Kullanmak için timeTracker.startClock(isim) dedikten sonra
 * TimeTracker.stopClock(isim)'un döndürdü?ü String'i geçen süreyi göstermek 
 * için kullanabilirsiniz. Stop'tan önce ara ad?mlar? izlemek istiyorsan?z 
 * TimeTracker.getElapsedTimeString(isim) veya getElapsedTimeStringAsMillis
 * metodlarini kullanabilirsiniz. Start ile ba?latt???n?z saatleri isiniz 
 * bittigindemutlaka stop ile durdurman?z gerekiyor, çünkü ancak stop ile register
 * olmu? bir saat nesnesini unregistr edebilirsiniz.
 * <p/>
 * Olusan saatler globaldir, yani programin icinde istediginiz her yerde
 * kullanabilirsiniz.
 *
 * @author M.D.A
 */
public class TimeTracker {
    public static int MAX_TIMETRACKER_USERS = 500;
    private static HashMap<String, TimerElement> users = new HashMap<String, TimerElement>();

    /**
     * Yeni bir saat olu?turur ve listeye register eder.
     * @param name : saat ad?
     */
    public static void startClock(String name) {
        if (users.size() > MAX_TIMETRACKER_USERS) {
            System.err.println("Max Saat izleyici say?s? a??ld?. (" + MAX_TIMETRACKER_USERS + ")");
            return;
        }
        if (users.get(name) != null) {
            System.err.println(name + " isminde bir zaman izleyici zaten var.");
            return;
        }
        TimerElement timer = new TimerElement(name);
        users.put(name, timer);
    }

    /**
     * ismi verilen saat için ba?lang?çtan bu yana bu yana ne kadar zaman 
     * geçti?ini milisaniye cinsinden döndürür.
     *
     * @param name : saatin ad?
     * @return :Bir önceki tick'ten bu yana geçen süre (milisaniye cinsinden)
     */
    public static long getElapsedTime(String name) {
        TimerElement timer = users.get(name);
        if (timer == null)
            return -1;
        timer.refresh();
        return timer.getElapsedTime();
    }

    /**
     * ismi verilen saatin en son kontrolünden bu yana ne kadar zaman geçti?ini
     * milisaniye cinsinden döndürür.
     *
     * @param name :  saatin ad?
     * @return :Bir önceki tick'ten bu yana geçen süre (milisaniye cinsinden)
     */
    public static long getTimeDelta(String name) {
        TimerElement timer = users.get(name);
        if (timer == null)
            return -1;
        timer.refresh();
        return timer.getDiff()/1000L;
    }

    /**
     * ismi verilen saatin en son kontrolunden (baslangic veya bir onceki tick) 
     * bu yana ne kadar zaman gecti?ini ve ba?lang?çtan bu yana geçen süreyi 
     * virgülden sonra 3 basamakl? saniyeyi ifade eden String cinsinden döndürür.
     *
     * @param name : saatin ad?
     * @return : Bir önceki tick'ten bu yana geçen süre (Binde bir hassasiyetli saniye cinsinden cinsinden)
     */
    public static String getElapsedTimeString(String name) {
        TimerElement timer = users.get(name);
        if (timer == null)
            return "Geçersiz Kronometre: " + name;
        timer.refresh();
        return "Delta: " + (double) timer.getDiff()/1000L  + " s. Elapsed: " + (double) timer.getElapsedTime()/1000L + " s.";
    }

    /**
     * @param name : saatin ad?
     * @return : Bir önceki tick'ten bu yana geçen süre (milisaniye cinsinden)
     */
    public static String getElapsedTimeStringAsMillis(String name) {
        TimerElement timer =users.get(name);
        if (timer == null)
            return "Geçersiz Kronometre: " + name;
        timer.refresh();
        return "Delta: " + timer.getDiff()/1000L + "ms. Elapsed: " + timer.getElapsedTime()/1000L + "ms.";
    }

    /**
     * @param name      : saatin ad?
     * @param itemCount : sure zarf?nda islenen nesne sayisi
     * @return : baslangictan bu yana islenen saniyedeki eleman sayisi
     */
    public static long getItemsPerSecond(String name, long itemCount) {
        TimerElement timer = users.get(name);
        if (timer == null)
            return -1;
        timer.refresh();
        long items = 0;
        if (timer.getElapsedTime() > 0)
            items = (itemCount)* 1000L / timer.getElapsedTime();
        return items;
    }

    /**
     * Saati durdurur ve ba?lang?çtan bu yana geçen süreyi saniye ve ms 
     * cinsinden döndürür. Ayr?ca saati listeden siler. 
     *
     * @param name Saat ismi
     * @return ba?lang?çtan bu yana geçen süre
     */
    public static String stopClock(String name) {
        TimerElement timer = users.get(name);
        if (timer == null)
            return name + " : Geçersiz Kronometre";
        timer.refresh();
        users.remove(name);
        return "" + (float) timer.elapsedTime + "sn."
               + "(" + timer.elapsedTime + " ms.)";
    }
}

/**
 * isimlendirilmi? Zaman bilgisi ta??y?c?.
 *
 * @author MDA
 */
class TimerElement {
    String name;
    long startTime = 0;
    long stopTime = 0;
    long lastTime = 0;
    long creationTime = 0;
    long elapsedTime = 0;
    long diff = 0;

    private static long getMilis() {
       return System.nanoTime()/ 1000000L;
    }

    public TimerElement(String name) {
        creationTime = getMilis();
        startTime = creationTime;
        lastTime = creationTime;
        this.name = name;
    }

    public void refresh() {
        diff =getMilis() - lastTime;
        lastTime = getMilis();
        elapsedTime = lastTime - startTime;
    }

    public long getDiff() {
        return diff;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }
}
