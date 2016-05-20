package maicliant.reifnsk.minimap;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import org.lwjgl.input.Keyboard;

public enum KeyInput
{
    MENU_KEY(50),
    TOGGLE_ENABLE(0),
    TOGGLE_RENDER_TYPE(0),
    TOGGLE_ZOOM(44),
    TOGGLE_LARGE_MAP(45),
    TOGGLE_LARGE_MAP_LABEL(0),
    TOGGLE_WAYPOINTS_VISIBLE(0),
    TOGGLE_WAYPOINTS_MARKER(0),
    TOGGLE_WAYPOINTS_DIMENSION(0),
    TOGGLE_ENTITIES_RADAR(0),
    SET_WAYPOINT(46),
    WAYPOINT_LIST(0),
    ZOOM_IN(0),
    ZOOM_OUT(0);
    private static File configFile = new File(ReiMinimap.directory, "keyconfig.txt");
    private final int defaultKeyIndex;
    private String label;
    private int keyIndex;
    private boolean keyDown;
    private boolean oldKeyDown;

    private KeyInput(int var3)
    {
        this.defaultKeyIndex = var3;
        this.keyIndex = var3;
        this.label = ReiMinimap.capitalize(this.name());
    }

    private KeyInput(String var3, int var4)
    {
        this.label = var3;
        this.defaultKeyIndex = var4;
        this.keyIndex = var4;
    }

    public void setKey(int var1)
    {
        if (var1 == 1)
        {
            var1 = 0;
        }

        if (var1 != 0 || this != MENU_KEY)
        {
            if (var1 != 0)
            {
                KeyInput[] var2 = values();
                int var3 = var2.length;

                for (int var4 = 0; var4 < var3; ++var4)
                {
                    KeyInput var5 = var2[var4];

                    if (var5.keyIndex == var1)
                    {
                        if (var5 == MENU_KEY && this.keyIndex == 0)
                        {
                            return;
                        }

                        var5.keyIndex = this.keyIndex;
                        var5.keyDown = false;
                        var5.oldKeyDown = false;
                        break;
                    }
                }
            }

            this.keyIndex = var1;
            this.keyDown = false;
            this.oldKeyDown = false;
        }
    }

    public int getKey()
    {
        return this.keyIndex;
    }

    public String label()
    {
        return this.label;
    }

    public String getKeyName()
    {
        String var1 = Keyboard.getKeyName(this.keyIndex);
        return var1 == null ? String.format("#%02X", new Object[] {Integer.valueOf(this.keyIndex)}): ReiMinimap.capitalize(var1);
    }

    public void setKey(String var1)
    {
        int var2 = Keyboard.getKeyIndex(var1);

        if (var1.startsWith("#"))
        {
            try
            {
                var2 = Integer.parseInt(var1.substring(1), 16);
            }
            catch (Exception var4)
            {
                ;
            }
        }

        this.setKey(var2);
    }

    public boolean isKeyDown()
    {
        return this.keyDown;
    }

    public boolean isKeyPush()
    {
        return this.keyDown && !this.oldKeyDown;
    }

    public boolean isKeyPushUp()
    {
        return !this.keyDown && this.oldKeyDown;
    }

    public static void update()
    {
        KeyInput[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2)
        {
            KeyInput var3 = var0[var2];
            var3.oldKeyDown = var3.keyDown;
            var3.keyDown = var3.keyIndex != 0 && Keyboard.isKeyDown(var3.keyIndex);
        }
    }

    public static boolean saveKeyConfig()
    {
        PrintWriter var0 = null;
        boolean var2;

        try
        {
            var0 = new PrintWriter(configFile);
            KeyInput[] var1 = values();
            int var10 = var1.length;

            for (int var3 = 0; var3 < var10; ++var3)
            {
                KeyInput var4 = var1[var3];
                var0.println(var4.toString());
            }

            return true;
        }
        catch (Exception var8)
        {
            var2 = false;
        }
        finally
        {
            if (var0 != null)
            {
                var0.flush();
                var0.close();
            }
        }

        return var2;
    }

    public static void loadKeyConfig()
    {
        Scanner var0 = null;

        try
        {
            var0 = new Scanner(configFile);

            while (var0.hasNextLine())
            {
                try
                {
                    String[] var1 = var0.nextLine().split(":");
                    valueOf(ReiMinimap.toUpperCase(var1[0].trim())).setKey(ReiMinimap.toUpperCase(var1[1].trim()));
                }
                catch (Exception var6)
                {
                    ;
                }
            }
        }
        catch (Exception var7)
        {
            ;
        }
        finally
        {
            if (var0 != null)
            {
                var0.close();
            }
        }
    }

    public void setDefault()
    {
        this.keyIndex = this.defaultKeyIndex;
    }

    public boolean isDefault()
    {
        return this.keyIndex == this.defaultKeyIndex;
    }

    public String toString()
    {
        return ReiMinimap.capitalize(this.name()) + ": " + this.getKeyName();
    }

    static {
        loadKeyConfig();
        saveKeyConfig();
    }
}

