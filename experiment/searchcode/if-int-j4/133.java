// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   castle.java

import jagex.*;

import java.awt.*;
import java.io.IOException;
import java.net.*;

public class castle extends GameApplet
{

    public static void main(String as[])
    {
        appletMode = false;
        castle castle1 = new castle();
        castle1.startApplication(512, 384, "Castle GamesDomain - By Andrew Gower", false);
    }

    public void _mth010B()
    {
        D0 = true;
        if(E3)
            appletGraphics.drawImage(gameImage, 0, 0, this);
    }

    public void startGame()
    {
        label0:
        {
            if(!appletMode)
                break label0;
            String host = getDocumentBase().getHost().toLowerCase();
            if(host.equals("") || host.equals("127.0.0.1") || host.endsWith("jagex.com"))
                break label0;
            try
            {
                Stream stream = new Stream("hosts.txt");
                stream.skipToEqual();
                int hostcount = stream.getPropInt();
                for(int k1 = 0; k1 < hostcount; k1++)
                {
                    stream.skipToEqual();
                    String s2 = stream.getPropStr();
                    if(!host.endsWith(s2))
                        continue;
                    stream.closeStream();
                    break label0;
                }

                stream.closeStream();
            }
            catch(IOException ioexception)
            {
                GameDialog.showError("Error in hosts.txt");
                ioexception.printStackTrace();
            }
            try
            {
                getAppletContext().showDocument(new URL("http://www.gamesdomain.co.uk/GamesArena/castle/index.html"));
                return;
            }
            catch(IOException _ex)
            {
                GameDialog.showError("Illegal host! To play visit: www.gamesdomain.com");
            }
            return;
        }
        if(appletMode)
        {
            gameName = getParameter("longid");
            if(gameName != "" && gameName != null)
            {
                paramTextures = getParameter("textures");
                paramTitleModel = getParameter("title");
                paramMap = getParameter("map");
                paramQuit = getParameter("quit");
                paramLogo = getParameter("logo");
                paramAmbient = Integer.parseInt(getParameter("ambient"));
                paramContrast = Integer.parseInt(getParameter("contrast"));
                paramFog = Integer.parseInt(getParameter("fog"));
                int i1 = Integer.parseInt(getParameter("ground"));
                paramGround = new Color(i1 / 0xf4240, (i1 / 1000) % 1000, i1 % 1000);
                paramLogid = Integer.parseInt(getParameter("logid"));
                if(paramLogid == 2)// assuming logid=2 when playing from the happypuppy domains (see hosts.txt)
                    happyPuppy = true;
            } else
            {
                gameName = "Castle GamesDomain";
            }
        }
        gameImage = createImage(512, 384);
        gameGraphics = gameImage.getGraphics();
        // TODO (this is to skip the quality selection)
        //gameStatus = -1;
        interlace = false;
        loadSoundEffects();
        gameStatus = 0;
        // TODO
        appletGraphics = getGraphics();
        setFPS(40);
        surface2mby = new Surface2mby(500, 290, 350, this);
        scene = new Scene(surface2mby._fld0407, 100, 5000, 50);
        GameDialog.setLoadingProgress(10);
        scene.loadTextures(paramTextures, null, 10, 60);// ? mmmaaayyybbeee
        GameDialog.setLoadingProgress(60);
        if(happyPuppy)
            gamePanelGamesImage = GameDialog.loadImage("menu/puppypanel.gif");
        else
            gamePanelGamesImage = GameDialog.loadImage("menu/panel.gif");
        GameDialog.setLoadingProgress(70);
        if(happyPuppy)
            gamePanelQuitImage = GameDialog.loadImage("menu/puppyquit2.gif");
        else
            gamePanelQuitImage = GameDialog.loadImage(paramQuit);
        GameDialog.setLoadingProgress(80);
        loadTitleModel();
        GameDialog.setLoadingProgress(90);
        loadMap();
        GameDialog.setLoadingProgress(100);
    }

    public synchronized void draw()
    {
        try
        {
            initCurrentScreen();
            if(drawScreenNewAccount)
                handleNewAccInput();
            else
            if(gameStatus == -1)
                handleGameQualityInput();
            else
            if(gameStatus == 0)
                handleLoginScreenInput();
            else
            if(gameStatus == 1)
                p();
            else
            if(gameStatus == 2)
                x();
            else
            if(gameStatus >= 3)
                games[gameStatus - 3]._mth0190();
            if(packetError)
            {
                packetError = false;
                sendLogout();
                gameStatus = 0;
                initCurrentScreen();
                login();
                readLoginResponse();
            }
            if(!drawScreenNewAccount)
            {
                super.lastMouseButtonDown = 0;
                super.lastKeyDown = 0;
                return;
            }
        }
        catch(Throwable throwable)
        {
            if(gameStatus != 0 && stream != null && _fld0106)
            {
                _fld0106 = false;
                stream.newPacket(17);
                stream.putString("p: " + throwable + " s:" + gameStatus + " a:" + _fld0146 + " cl:" + _fld0107);
                stream.sendPacket();
                return;
            }
            System.out.println(String.valueOf(throwable));
            throwable.printStackTrace();
        }
    }

    public synchronized void drawScreens()
    {
        initCurrentScreen();
        if(drawScreenNewAccount)
        {
            drawScreenNewAccount();
            return;
        }
        if(gameStatus == -1)
        {
            drawGameQualityScreen();
            return;
        }
        if(gameStatus == 0)
        {
            drawLoginScreenViewport();
            return;
        }
        if(gameStatus == 1)
        {
            drawGameScreen();
            return;
        }
        if(gameStatus == 2)
        {
            drawChallengeScreen();
            return;
        }
        if(gameStatus >= 3)
            games[gameStatus - 3]._mth018E();
    }

    public void initCurrentScreen()
    {
        try
        {
            if(gameStatus != E7)
            {
                E7 = gameStatus;
                super.lastMouseButtonDown = 0;
                if(gameStatus == 0)
                {
                    resetLoginScreenVars();
                    return;
                }
                if(gameStatus == 1)
                {
                    C1();
                    return;
                }
                if(gameStatus == 2)
                {
                    loadGame();
                    return;
                }
                if(gameStatus >= 3)
                {
                    games[gameStatus - 3].setCastle(this, castleId);
                    return;
                }
            }
        }
        catch(Throwable throwable)
        {
            if(gameStatus != 0 && stream != null && _fld0106)
            {
                _fld0106 = false;
                stream.newPacket(17);
                stream.putString("init: " + throwable + " screen:" + gameStatus);
                stream.sendPacket();
            }
        }
    }

    public void _mth010F()
    {
        sendLogout();
        if(surface2mby != null)
            surface2mby._mth01CF();
        if(_fld0113 != null)
        {
            _fld0113._mth0277();
            _fld0113 = null;
        }
    }

    public void f()
    {
        try
        {
            F1++;
            if(F1 > F2)
            {
                F1 = 0;
                stream.newPacket(4);
                stream._mth02A8();
                return;
            }
        }
        catch(IOException _ex)
        {
            packetError = true;
        }
    }

    public void sendLogout()
    {
        if(stream != null)
        {
            stream.newPacket(1);
            stream.sendPacket();
        }
        gameStatus = 0;
        loginScreen = 0;
    }

    public void c(int i1, int j1)
    {
        if(_fld0113 == null)
        {
            return;
        } else
        {
            _fld0113._mth0278(6 + _fld0114, i1, 8000, j1);
            _fld0114 = 1 - _fld0114;
            return;
        }
    }

    public void a(int i1, int j1, int k1)
    {
        if(_fld0113 == null)
        {
            return;
        } else
        {
            _fld0113._mth0278(6 + _fld0114, i1, k1, j1);
            _fld0114 = 1 - _fld0114;
            return;
        }
    }

    public void Y()
    {
        if(_fld0113 == null)
        {
            return;
        } else
        {
            _fld0113._mth0280(6);
            _fld0113._mth0280(7);
            return;
        }
    }

    public void loadSoundEffects()
    {
        byte abyte0[] = new byte[0x124f8];
        GameDialog.ED("sfx.xm", abyte0, 0x124f8, 0, 100);
        _fld0113 = new v(abyte0);
        _fld0113._mth027D();
    }

    public void createScreenNewAccount()
    {
        surface = new Surface(512, 384, 10, this);
        surface._mth01E8("scrollbar.gif", 0, true, 2, 11, 12);
        surface._mth01E8("corners.gif", 2, true, 4, 7, 7);
        panelNewAccount = new Panel(surface, 50);
        panelNewAccount.addButtonBackground(256, 30, 350, 35);
        controlNewAccStatus1 = panelNewAccount.addText(256, 22, "To play please fill in the details below", "H14b", false);
        controlNewAccStatus2 = panelNewAccount.addText(256, 38, "You will only be asked for this information once!", "H14b", false);
        panelNewAccount.addButtonBackground(143, 80, 200, 40);
        panelNewAccount.addText(143, 70, "First name", "H14b", false);
        controlNewAccFirstName = panelNewAccount.addTextInput(143, 90, 200, 40, "H14b", 20, false, false);
        panelNewAccount.addButtonBackground(143, 130, 200, 40);
        panelNewAccount.addText(143, 120, "Surname", "H14b", false);
        controlNewAccSurname = panelNewAccount.addTextInput(143, 140, 200, 40, "H14b", 20, false, false);
        panelNewAccount.addButtonBackground(143, 180, 200, 40);
        panelNewAccount.addText(143, 170, "Age", "H14b", false);
        controlNewAccAge = panelNewAccount.addTextInput(143, 190, 200, 40, "H14b", 20, false, false);
        panelNewAccount.addButtonBackground(143, 230, 200, 40);
        panelNewAccount.addText(143, 220, "Sex", "H14b", false);
        String as[] = {
            "Male", "Female"
        };
        controlNewAccSex = panelNewAccount.addOption(143, 240, as, "H14b", false);
        panelNewAccount._mth016D(269, 60, 200, 190);
        controlNewAccCountry = panelNewAccount._mth014A(279, 60, 200, 190, "H14b", 50, true);
        panelNewAccount.addButtonBackground(256, 280, 400, 40);
        panelNewAccount.addText(256, 270, "E-mail Address", "H14b", false);
        controlNewAccEmail = panelNewAccount.addTextInput(256, 290, 400, 40, "H14b", 40, false, false);
        panelNewAccount.addButtonBackground(256, 330, 100, 40);
        panelNewAccount.addText(256, 330, "Submit", "H16b", false);
        controlNewAccSubmit = panelNewAccount.addButton(256, 330, 100, 40);
        String as1[] = {
            "UK", "US", "Africa", "Asia", "Australia", "Austria", "Canada", "Finland", "France", "Germany", 
            "Italy", "Norway", "Poland", "Portugal", "South America", "Spain", "Sweden", "Switzerland", "Other European", "Other"
        };
        for(int i1 = 0; i1 < as1.length; i1++)
            panelNewAccount.addOptionList(controlNewAccCountry, i1, as1[i1]);

        panelNewAccount.setFocus(controlNewAccFirstName);
        panelNewAccount.drawPanel(0, 0, 512, 384);
    }

    public void handleNewAccInput()
    {
        if(panelNewAccount == null)
            createScreenNewAccount();
        f();
        if(super.lastKeyDown != 0)
        {
            panelNewAccount._mth0155(super.lastKeyDown);
            super.lastKeyDown = 0;
        }
        if(panelNewAccount.isClicked(controlNewAccFirstName))
            panelNewAccount.setFocus(controlNewAccSurname);
        if(panelNewAccount.isClicked(controlNewAccSurname))
            panelNewAccount.setFocus(controlNewAccAge);
        if(panelNewAccount.isClicked(controlNewAccAge))
            panelNewAccount.setFocus(controlNewAccEmail);
        if(panelNewAccount.isClicked(controlNewAccEmail))
            panelNewAccount.setFocus(controlNewAccFirstName);
        if(panelNewAccount.isClicked(controlNewAccSubmit))
        {
            if(panelNewAccount.getText(controlNewAccFirstName) != null && panelNewAccount.getText(controlNewAccFirstName).length() == 0 || panelNewAccount.getText(controlNewAccSurname) != null && panelNewAccount.getText(controlNewAccSurname).length() == 0 || panelNewAccount.getText(controlNewAccAge) != null && panelNewAccount.getText(controlNewAccAge).length() == 0 || panelNewAccount.getText(controlNewAccEmail) != null && panelNewAccount.getText(controlNewAccEmail).length() == 0 || panelNewAccount.getOption(controlNewAccCountry) == -1)
            {
                panelNewAccount.updateText(controlNewAccStatus1, "Please fill in ALL information to continue!");
                panelNewAccount.updateText(controlNewAccStatus2, "");
                panelNewAccount.drawPanel(0, 0, 512, 384);
                return;
            }
            String firstname = panelNewAccount.getText(controlNewAccFirstName);
            String surname = panelNewAccount.getText(controlNewAccSurname);
            String email = panelNewAccount.getText(controlNewAccEmail);
            int country = panelNewAccount.getOption(controlNewAccCountry);
            int sex = panelNewAccount.getOption(controlNewAccSex);
            String age = panelNewAccount.getText(controlNewAccAge);
            int ageage = 0;
            try
            {
                ageage = Integer.parseInt(age);
            }
            catch(Exception _ex) { }
            drawScreenNewAccount = false;
            super.inputTextCurrent = "";
            super.inputTextFinal = "";
            super.inputPmCurrent = "";
            super.inputPmFinal = "";
            surface = null;
            panelNewAccount = null;
            stream.newPacket(25);
            for(; firstname.length() < 20; firstname = firstname + " ");
            stream.putString(firstname);
            for(; surname.length() < 20; surname = surname + " ");
            stream.putString(surname);
            for(; email.length() < 40; email = email + " ");
            stream.putString(email);
            stream.putInt(country);
            stream.putInt(ageage);
            stream.putInt(sex);
            stream.sendPacket();
        }
    }

    public void drawScreenNewAccount()
    {
        if(panelNewAccount == null)
            createScreenNewAccount();
        panelNewAccount.handleMouse(super.mouseX, super.mouseY, super.lastMouseButtonDown, super.mouseButtonDown);
        super.lastMouseButtonDown = 0;
        panelNewAccount.drawPanel();
        surface.draw(appletGraphics, 0, 0);
    }

    public void handleGameQualityInput()
    {
        if(super.lastMouseButtonDown != 0)
        {
            if(super.mouseY >= 112 && super.mouseY < 160)
            {
                interlace = true;
                gameStatus = 0;
                super.lastMouseButtonDown = 0;
                return;
            }
            if(super.mouseY >= 160 && super.mouseY < 208)
            {
                interlace = false;
                gameStatus = 0;
                super.lastMouseButtonDown = 0;
                return;
            }
            if(super.mouseY >= 208 && super.mouseY < 256)
            {
                interlace = true;
                loadSoundEffects();
                gameStatus = 0;
                super.lastMouseButtonDown = 0;
                return;
            }
            if(super.mouseY >= 256 && super.mouseY < 304)
            {
                interlace = false;
                loadSoundEffects();
                gameStatus = 0;
                super.lastMouseButtonDown = 0;
                return;
            }
        }
    }

    public void drawGameQualityScreen()
    {
        gameGraphics.setColor(Color.black);
        if(happyPuppy)
            gameGraphics.setColor(new Color(253, 253, 205));
        gameGraphics.fillRect(0, 0, 512, 384);
        gameGraphics.setColor(new Color(0, 32, 64));
        if(happyPuppy)
            gameGraphics.setColor(new Color(75, 122, 171));
        gameGraphics.fillRect(0, 64, 512, 256);
        gameGraphics.setColor(new Color(0, 48, 80));
        gameGraphics.fillRect(0, 112, 512, 48);
        gameGraphics.setColor(new Color(0, 48, 80));
        gameGraphics.fillRect(0, 208, 512, 48);
        gameGraphics.setColor(Color.white);
        GameDialog.drawstringCenter(gameGraphics, "Select options:", fontH30B, 256, 88);
        GameDialog.drawstringCenter(gameGraphics, "Recommended for Pentium\256 Computers", fontH15, 256, 144);
        GameDialog.drawstringCenter(gameGraphics, "Recommended for Pentium-II\256 or better", fontH15, 256, 192);
        GameDialog.drawstringCenter(gameGraphics, "Sounds may take 20-40 secs to load on modems", fontH15, 256, 240);
        GameDialog.drawstringCenter(gameGraphics, "Sounds may take 20-40 secs to load on modems", fontH15, 256, 288);
        gameGraphics.setColor(Color.red);
        GameDialog.drawstringCenter(gameGraphics, "Low detail", fontH18B, 201, 128);
        GameDialog.drawstringCenter(gameGraphics, "Low detail", fontH18B, 201, 224);
        GameDialog.drawstringCenter(gameGraphics, "Sound off", fontH18B, 311, 128);
        GameDialog.drawstringCenter(gameGraphics, "Sound off", fontH18B, 311, 176);
        gameGraphics.setColor(Color.green);
        GameDialog.drawstringCenter(gameGraphics, "High detail", fontH18B, 201, 176);
        GameDialog.drawstringCenter(gameGraphics, "High detail", fontH18B, 201, 272);
        GameDialog.drawstringCenter(gameGraphics, "Sound on", fontH18B, 311, 224);
        GameDialog.drawstringCenter(gameGraphics, "Sound on", fontH18B, 311, 272);
        appletGraphics.drawImage(gameImage, 0, 0, this);
    }

    public void loadTitleModel()
    {
        modelTitleScreen = new GameModel(paramTitleModel);
    }

    public void resetLoginScreenVars()
    {
        loginUserDesc = "Please enter a username:";
        _fld0129 = "";
        loginUserDisp = "*" + loginUser + "*";
        _fld012A = "";
        sceneCleared = false;
        scene.clear();
        scene.addModel(modelTitleScreen);
        scene._fld04A1 = 5;
        scene._fld04A3 = 5000;
        scene._mth0275(0, 0, 0, 0, 0, 0);
        scene._mth0270(250, 145, 250, 145, 500);
        scene.fog = 20;
        scene._mth025F(32, 0, 0);
        gameGraphics.setColor(Color.black);
        gameGraphics.fillRect(0, 0, 512, 384);
        D0 = true;
        loginScreen = 0;
        super.inputTextCurrent = "";
        super.inputTextFinal = "";
    }

    public void handleLoginScreenInput()
    {
        if(gameStatus == 0)
        {
            loginCameraRotation = loginCameraRotation + 2 & 0xff;
            if(loginScreen == 0)
            {
                if(super.inputTextCurrent != loginUser)
                {
                    loginUser = super.inputTextCurrent;
                    loginUserDisp = "*" + loginUser + "*";
                }
                if(super.inputTextFinal != "")
                {
                    loginUser = super.inputTextFinal;
                    super.inputTextCurrent = "";
                    super.inputTextFinal = "";
                    loginScreen = 1;
                    loginUserDesc = "Please enter password";
                    loginUserDisp = "*";
                    for(int i1 = 0; i1 < loginPass.length(); i1++)
                        loginUserDisp += "+";

                    loginUserDisp += "*";
                    return;
                }
            } else
            if(loginScreen == 1)
            {
                if(super.inputTextCurrent != loginUser)
                {
                    loginPass = super.inputTextCurrent;
                    loginUserDisp = "*";
                    for(int j1 = 0; j1 < loginPass.length(); j1++)
                        loginUserDisp += "+";

                    loginUserDisp += "*";
                }
                if(super.inputTextFinal != "")
                {
                    loginPass = super.inputTextFinal;
                    super.inputTextCurrent = "";
                    super.inputTextFinal = "";
                    login();
                    readLoginResponse();
                    return;
                }
            } else
            {
                if(loginScreen == 2 && super.lastKeyDown != 0)
                {
                    f();
                    if(super.lastKeyDown == 121 || super.lastKeyDown == 89)
                    {
                        stream.newPacket(2);
                        stream.sendPacket();
                        readLoginResponse();
                        return;
                    } else
                    {
                        super.inputTextCurrent = "";
                        super.inputTextFinal = "";
                        loginScreen = 0;
                        loginUserDesc = "Please Enter a username";
                        loginUserDisp = "*" + loginUser + "*";
                        return;
                    }
                }
                if(loginScreen == 3 && super.lastKeyDown != 0)
                {
                    login();
                    readLoginResponse();
                    return;
                }
                if(loginScreen == 4 && super.lastKeyDown != 0)
                {
                    super.inputTextCurrent = "";
                    super.inputTextFinal = "";
                    loginScreen = 0;
                    loginUserDesc = "Please Enter a username";
                    loginUserDisp = "*" + loginUser + "*";
                }
            }
        }
    }

    public void drawLoginScreenViewport()
    {
        if(gameStatus == 0)
        {
            surface2mby._mth01BD(interlace);
            scene._mth025C(0, 0, 0, 235, loginCameraRotation, 0, 500);// TODO 235 = "z-axis", 500 = zoom
            scene._mth025A(interlace);
            surface2mby._mth01C2(gameGraphics, 6, 47, interlace);
            gameGraphics.setColor(Color.white);
            GameDialog.drawstringCenter(gameGraphics, "Welcome to", fontH40, 256, 50);
            GameDialog.drawstringCenter(gameGraphics, gameName, fontH40, 256, 85);
            GameDialog.drawstringCenter(gameGraphics, "Lead Programmer: Andrew Gower - Graphics by: Ian Gower", fronH13B, 256, 110);
            GameDialog.drawstringCenter(gameGraphics, loginUserDesc, fontH23, 256, 200);
            GameDialog.drawstringCenter(gameGraphics, loginUserDisp, fontH23, 256, 240);
            appletGraphics.drawImage(gameImage, 0, 0, this);
            D0 = false;
        }
    }

    public void login()
    {
        try
        {
            loginUserDesc = "Please wait...";
            loginUserDisp = "Connecting to server";
            drawScreens();
            resetTimings();
            stream = null;
            Socket socket;
            if(appletMode)
                socket = new Socket(InetAddress.getByName(getCodeBase().getHost()), 4600);
            else
                socket = new Socket(InetAddress.getByName(server), 4600);
            socket.setSoTimeout(10000);
            socket.setTcpNoDelay(true);
            stream = new Stream(socket);
            System.out.println("asa");
            stream.newPacket(19);
            stream.putUnsignedShort(paramLogid);
            stream._mth02A8();
            stream.newPacket(0);
            System.out.println("sas");
            String s1 = GameDialog.DD(loginUser, 12);
            stream.putString(s1);
            String s2 = GameDialog.DD(loginPass, 12);
            stream.putString(s2);
            stream._mth02A8();
            return;
        }
        catch(Exception exception)
        {
            //server = "javaserv.theglobe.com";
            System.out.println(String.valueOf(exception));
            System.out.println(exception.getMessage());
            loginScreen = 5;
            loginUserDesc = "Sorry! Unable to connect to server";
            loginUserDisp = "Please try later, or press any key to retry";
            return;
        }
    }

    public void readLoginResponse()
    {
        try
        {
            System.out.println("short: " + stream.getShort());
            int i1 = stream.readStream();
            System.out.println("Got response: " + i1);
            if(i1 == 0)
            {
                gameStatus = 1;
                return;
            }
            if(i1 == 1)
            {
                loginScreen = 2;
                loginUserDesc = "New user detected";
                loginUserDisp = "Did you enter your name correctly? (Y or N)";
                return;
            }
            if(i1 == 2)
            {
                loginScreen = 3;
                loginUserDesc = "Sorry! The server is currently full";
                loginUserDisp = "Please try later, or press any key to retry";
                return;
            }
            if(i1 == 3)
            {
                loginScreen = 4;
                loginUserDesc = "Wrong password - try again!";
                loginUserDisp = "New players: That name is taken/invalid";
                return;
            } else
            {
                loginScreen = 3;
                loginUserDesc = "Sorry! Unable to connect to server";
                loginUserDisp = "Please try later, or press any key to retry";
                return;
            }
        }
        catch(Exception _ex)
        {                     _ex.printStackTrace();
            loginScreen = 3;
        }
        loginUserDesc = "Sorry! Unable to connect to server";
        loginUserDisp = "Please try later, or press any key to retry";
    }

    public void loadMap()
    {
        try
        {
            Stream w1 = new Stream(paramMap);
            w1.skipToEqual();
            _fld012F = w1.getPropInt();
            _fld0130 = w1.getPropInt();
            _fld0137 = _fld012F * 64;
            _fld0138 = _fld0130 * 64;
            _fld0136 = 0;
            _fld0131 = new int[_fld012F][_fld0130];
            _fld0134 = new int[_fld012F][_fld0130];
            _fld0147 = new int[_fld012F * 3][_fld0130 * 3];
            _fld0142 = new int[_fld012F][_fld0130];
            _fld0143 = new int[_fld012F][_fld0130];
            for(int i1 = 0; i1 < _fld0130; i1++)
            {
                w1.skipToEqual();
                String s1 = w1.getPropStr();
                for(int k1 = 0; k1 < _fld012F; k1++)
                {
                    char c1 = s1.charAt(k1);
                    if(c1 == ' ')
                        c1 = '\0';
                    _fld0134[k1][i1] = _fld0131[k1][i1] = c1;
                }

            }

            _fld0132 = new int[_fld012F][_fld0130];
            for(int j1 = 0; j1 < _fld0130; j1++)
            {
                w1.skipToEqual();
                String s2 = w1.getPropStr();
                for(int i2 = 0; i2 < _fld012F; i2++)
                {
                    int k2 = s2.charAt(i2);
                    if(k2 >= 48 && k2 <= 57)
                        k2 -= 48;
                    else
                    if(k2 >= 65 && k2 <= 90)
                        k2 = (k2 - 65) + 100;
                    else
                    if(k2 == 32)
                        k2 = 0;
                    _fld0132[i2][j1] = k2;
                }

            }

            _fld0133 = new int[_fld012F][_fld0130];
            _fld0135 = new int[_fld012F + 1][_fld0130 + 1];
            for(int l1 = 0; l1 <= _fld012F; l1++)
            {
                _fld0135[l1][0] = 1000;
                _fld0135[l1][_fld0130] = 1000;
            }

            for(int j2 = 0; j2 <= _fld0130; j2++)
            {
                _fld0135[0][j2] = 1000;
                _fld0135[_fld012F][j2] = 1000;
            }

            for(int l2 = 0; l2 < _fld0130; l2++)
            {
                w1.skipToEqual();
                String s3 = w1.getPropStr();
                for(int j3 = 0; j3 < _fld012F; j3++)
                {
                    int l3 = s3.charAt(j3);
                    if(l3 >= 48 && l3 <= 57)
                        l3 -= 48;
                    else
                        l3 = 0;
                    if(l3 == 0 && _fld0131[j3][l2] != 0)
                        l3 = 3;
                    _fld0133[j3][l2] = l3;
                    if(_fld0131[j3][l2] == 0)
                        l3 += 1000;
                    if(l3 > _fld0135[j3][l2])
                        _fld0135[j3][l2] = l3;
                    if(l3 > _fld0135[j3 + 1][l2])
                        _fld0135[j3 + 1][l2] = l3;
                    if(l3 > _fld0135[j3][l2 + 1])
                        _fld0135[j3][l2 + 1] = l3;
                    if(l3 > _fld0135[j3 + 1][l2 + 1])
                        _fld0135[j3 + 1][l2 + 1] = l3;
                }

            }

            for(int i3 = 0; i3 < _fld012F; i3++)
            {
                for(int k3 = 0; k3 < _fld0130; k3++)
                    if(_fld0135[i3][k3] >= 1000)
                        _fld0135[i3][k3] -= 1000;

            }

            w1.closeStream();
            _fld0107 = 1;
        }
        catch(IOException _ex)
        {
            _fld0107 = 0;
            System.out.println("Fatal error loading map!");
        }
        modelTable = new GameModel("menu/table.ob2");
        modelTrapdoor = new GameModel("menu/trapdoor.ob2");
    }

    public void C1()
    {
        if(!sceneCleared)
        {
            scene.clear();
            sceneCleared = true;
            X();
        }
        scene._fld04A1 = 5;
        scene._fld04A3 = 800;
        scene.fog = paramFog;
        scene._mth0275(_fld0137, _fld013E, _fld0138, _fld013D, _fld0136, 0);
        scene._mth0270(250, 145, 250, 145, 500);
        scene._mth025F(-64, -32, 32);
        if(!interlace)
            _fld013E = -80;
        else
            _fld013E = -65;
        gameGraphics.setColor(new Color(0, 104, 130));
        gameGraphics.fillRect(0, 0, 512, 384);
        gameGraphics.setColor(Color.black);
        gameGraphics.fillRect(5, 5, 502, 293);
        gameGraphics.drawImage(gamePanelGamesImage, 0, 298, this);
        D0 = true;
        super.inputPmCurrent = "";
        super.inputPmFinal = "";
        _fld0144 = -1;
        _fld0145 = -1;
        surface2mby._mth01BD(false);
        surface2mby._mth01C2(gameGraphics, 6, 6, false);
        for(int i1 = 0; i1 < 3; i1++)
            chatMessages[i1] = "";

        stream.newPacket(16);
        stream.putByte(0);
        stream.sendPacket();
    }

    public void w(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
    {
        if(k2 == 0)
            return;
        if((i1 & 1) == 0 && (j1 & 1) == 0)
            _fld0135[i1 / 2][j1 / 2] = k2;
        if((k1 & 1) == 0 && (l1 & 1) == 0)
            _fld0135[k1 / 2][l1 / 2] = k2;
        if(_fld0139[i1][j1] == 0)
        {
            _fld0139[i1][j1] = _fld013A++;
            _fld012B._mth012D(i1 * 64, 0, j1 * 64);
            _fld012B._mth012D(i1 * 64, -k2 * 42, j1 * 64);
            _fld013B[i1][j1] = _fld013C++;
            _fld012C._mth012D(i1 * 64, -k2 * 42, j1 * 64);
        }
        if(_fld0139[k1][l1] == 0)
        {
            _fld0139[k1][l1] = _fld013A++;
            _fld012B._mth012D(k1 * 64, 0, l1 * 64);
            _fld012B._mth012D(k1 * 64, -k2 * 42, l1 * 64);
            _fld013B[k1][l1] = _fld013C++;
            _fld012C._mth012D(k1 * 64, -k2 * 42, l1 * 64);
        }
        int ai[] = new int[4];
        ai[0] = (_fld0139[i1][j1] - 1) * 2;
        ai[1] = (_fld0139[i1][j1] - 1) * 2 + 1;
        ai[2] = (_fld0139[k1][l1] - 1) * 2 + 1;
        ai[3] = (_fld0139[k1][l1] - 1) * 2;
        _fld012B._mth012E(4, ai, i2, j2);
    }

    public void o(int i1, int j1, int k1, int l1, int i2, int j2)
    {
        if(i1 >= _fld012F * 2 || k1 >= _fld012F * 2 || i2 >= _fld012F * 2 || j1 >= _fld0130 * 2 || l1 >= _fld0130 * 2 || j2 >= _fld0130 * 2)
            return;
        int ai[] = new int[3];
        if(_fld013B[i1][j1] == 0)
        {
            int k2 = _fld0135[i1 / 2][j1 / 2];
            if(k2 == 0)
                return;
            _fld013B[i1][j1] = _fld013C++;
            _fld012C._mth012D(i1 * 64, -k2 * 42, j1 * 64);
        }
        if(_fld013B[k1][l1] == 0)
        {
            int l2 = _fld0135[k1 / 2][l1 / 2];
            if(l2 == 0)
                return;
            _fld013B[k1][l1] = _fld013C++;
            _fld012C._mth012D(k1 * 64, -l2 * 42, l1 * 64);
        }
        if(_fld013B[i2][j2] == 0)
        {
            int i3 = _fld0135[i2 / 2][j2 / 2];
            if(i3 == 0)
                return;
            _fld013B[i2][j2] = _fld013C++;
            _fld012C._mth012D(i2 * 64, -i3 * 42, j2 * 64);
        }
        ai[0] = _fld013B[i1][j1] - 1;
        ai[1] = _fld013B[k1][l1] - 1;
        ai[2] = _fld013B[i2][j2] - 1;
        _fld012C._mth012E(3, ai, E2, 0);
    }

    public void u(int i1, int j1, int k1, int l1, int i2, int j2, int k2, 
            int l2)
    {
        if(i1 >= _fld012F * 2 || k1 >= _fld012F * 2 || i2 >= _fld012F * 2 || k2 >= _fld012F * 2 || j1 >= _fld0130 * 2 || l1 >= _fld0130 * 2 || j2 >= _fld0130 * 2 || l2 >= _fld0130 * 2)
            return;
        int ai[] = new int[4];
        if(_fld013B[i1][j1] == 0)
        {
            int i3 = _fld0135[i1 / 2][j1 / 2];
            if(i3 == 0)
                return;
            _fld013B[i1][j1] = _fld013C++;
            _fld012C._mth012D(i1 * 64, -i3 * 42, j1 * 64);
        }
        if(_fld013B[k1][l1] == 0)
        {
            int j3 = _fld0135[k1 / 2][l1 / 2];
            if(j3 == 0)
                return;
            _fld013B[k1][l1] = _fld013C++;
            _fld012C._mth012D(k1 * 64, -j3 * 42, l1 * 64);
        }
        if(_fld013B[i2][j2] == 0)
        {
            int k3 = _fld0135[i2 / 2][j2 / 2];
            if(k3 == 0)
                return;
            _fld013B[i2][j2] = _fld013C++;
            _fld012C._mth012D(i2 * 64, -k3 * 42, j2 * 64);
        }
        if(_fld013B[k2][l2] == 0)
        {
            int l3 = _fld0135[k2 / 2][l2 / 2];
            if(l3 == 0)
                return;
            _fld013B[k2][l2] = _fld013C++;
            _fld012C._mth012D(k2 * 64, -l3 * 42, l2 * 64);
        }
        ai[0] = _fld013B[i1][j1] - 1;
        ai[1] = _fld013B[k1][l1] - 1;
        ai[2] = _fld013B[i2][j2] - 1;
        ai[3] = _fld013B[k2][l2] - 1;
        _fld012C._mth012E(4, ai, E2, 0);
    }

    public void X()
    {
        _fld012B = new GameModel((_fld012F + 1) * (_fld0130 + 1) * 6, _fld012F * _fld0130 * 2);
        _fld012C = new GameModel((_fld012F + 1) * (_fld0130 + 1) * 4, _fld012F * _fld0130 * 4);
        _fld0139 = new int[_fld012F * 2 + 1][_fld0130 * 2 + 1];
        _fld013A = 1;
        _fld013B = new int[_fld012F * 2 + 1][_fld0130 * 2 + 1];
        _fld013C = 1;
        for(int i1 = 0; i1 < _fld0130; i1++)
        {
            for(int j1 = 0; j1 < _fld012F; j1++)
            {
                char c1 = (char)_fld0131[j1][i1];
                if(c1 == '0')
                {
                    GameModel k2 = modelTable._mth013F();
                    k2._mth012C(j1 * 128 + 64, 0, i1 * 128 + 64);
                    scene.addModel(k2);
                } else
                if(c1 == '1' || c1 == '2')
                {
                    GameModel k3 = modelTrapdoor._mth013F();
                    k3._mth012C(j1 * 128 + 64, 0, i1 * 128 + 64);
                    scene.addModel(k3);
                }
                if(c1 == '-')
                {
                    int j2 = j1 * 2;
                    int k5 = i1 * 2 + 1;
                    int j8 = j1 * 2 + 2;
                    int l11 = i1 * 2 + 1;
                    w(j2, k5, j8, l11, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                } else
                if(c1 == '|')
                {
                    int l2 = j1 * 2 + 1;
                    int l5 = i1 * 2;
                    int k8 = j1 * 2 + 1;
                    int i12 = i1 * 2 + 2;
                    w(l2, l5, k8, i12, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                } else
                if(c1 == '\\')
                {
                    int i3 = j1 * 2;
                    int i6 = i1 * 2;
                    int l8 = j1 * 2 + 2;
                    int j12 = i1 * 2 + 2;
                    w(i3, i6, l8, j12, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                } else
                if(c1 == '/')
                {
                    int j3 = j1 * 2 + 2;
                    int j6 = i1 * 2;
                    int i9 = j1 * 2;
                    int k12 = i1 * 2 + 2;
                    w(j3, j6, i9, k12, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                } else
                if(c1 == '+')
                {
                    int l3 = j1 * 2 + 1;
                    int k6 = i1 * 2 + 1;
                    _fld0134[j1][i1] = 16;
                    if(q(j1, i1 - 1))
                    {
                        int j9 = j1 * 2 + 1;
                        int l12 = i1 * 2;
                        w(j9, l12, l3, k6, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                        _fld0134[j1][i1]++;
                    }
                    if(q(j1, i1 + 1))
                    {
                        int k9 = j1 * 2 + 1;
                        int i13 = i1 * 2 + 2;
                        w(l3, k6, k9, i13, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                        _fld0134[j1][i1] += 2;
                    }
                    if(q(j1 - 1, i1))
                    {
                        int l9 = j1 * 2;
                        int j13 = i1 * 2 + 1;
                        w(l3, k6, l9, j13, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                        _fld0134[j1][i1] += 4;
                    }
                    if(q(j1 + 1, i1))
                    {
                        int i10 = j1 * 2 + 2;
                        int k13 = i1 * 2 + 1;
                        w(l3, k6, i10, k13, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                        _fld0134[j1][i1] += 8;
                    }
                } else
                if(c1 == '*' || c1 == 'o')
                {
                    byte byte0 = 0;
                    byte byte1 = 0;
                    byte byte2 = 0;
                    byte byte3 = 0;
                    if(q(j1, i1 - 1))
                    {
                        byte0 = 0;
                        byte1 = -1;
                    }
                    if(q(j1, i1 + 1))
                    {
                        byte2 = 0;
                        byte3 = 1;
                    }
                    if(q(j1 - 1, i1) && byte0 == 0 && byte1 == 0)
                        byte0 = -1;
                    else
                    if(q(j1 - 1, i1) && byte2 == 0 && byte3 == 0)
                        byte2 = -1;
                    if(q(j1 + 1, i1) && byte0 == 0 && byte1 == 0)
                        byte0 = 1;
                    else
                    if(q(j1 + 1, i1) && byte2 == 0 && byte3 == 0)
                        byte2 = 1;
                    if(C4(j1 - 1, i1 - 1, c1) && byte0 == 0 && byte1 == 0)
                    {
                        byte0 = -1;
                        byte1 = -1;
                    } else
                    if(C4(j1 - 1, i1 - 1, c1) && byte2 == 0 && byte3 == 0)
                    {
                        byte2 = -1;
                        byte3 = -1;
                    }
                    if(C4(j1 + 1, i1 + 1, c1) && byte0 == 0 && byte1 == 0)
                    {
                        byte0 = 1;
                        byte1 = 1;
                    } else
                    if(C4(j1 + 1, i1 + 1, c1) && byte2 == 0 && byte3 == 0)
                    {
                        byte2 = 1;
                        byte3 = 1;
                    }
                    if(C4(j1 + 1, i1 - 1, c1) && byte0 == 0 && byte1 == 0)
                    {
                        byte0 = 1;
                        byte1 = -1;
                    } else
                    if(C4(j1 + 1, i1 - 1, c1) && byte2 == 0 && byte3 == 0)
                    {
                        byte2 = 1;
                        byte3 = -1;
                    }
                    if(C4(j1 - 1, i1 + 1, c1) && byte0 == 0 && byte1 == 0)
                    {
                        byte0 = -1;
                        byte1 = 1;
                    } else
                    if(C4(j1 - 1, i1 + 1, c1) && byte2 == 0 && byte3 == 0)
                    {
                        byte2 = -1;
                        byte3 = 1;
                    }
                    int j15 = j1 * 2 + byte0 + 1;
                    int i16 = i1 * 2 + byte1 + 1;
                    int k16 = j1 * 2 + byte2 + 1;
                    int i17 = i1 * 2 + byte3 + 1;
                    w(j15, i16, k16, i17, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                    _fld0134[j1][i1] = 0;
                    if(byte0 == -1 && byte1 == 0 && byte3 == 0 && byte2 == 1)
                        _fld0134[j1][i1] = 45;
                    else
                    if(byte1 == -1 && byte0 == 0 && byte2 == 0 && byte3 == 1)
                        _fld0134[j1][i1] = 124;
                    else
                    if(byte0 == -1 && byte1 == -1 && byte2 == 1 && byte3 == 1)
                        _fld0134[j1][i1] = 92;
                    else
                    if(byte0 == 1 && byte3 == 1 && byte1 == -1 && byte2 == -1)
                        _fld0134[j1][i1] = 47;
                    else
                    if(byte0 == 0 && byte1 == -1 && byte3 == 1 && byte2 == -1)
                        _fld0134[j1][i1] = 5;
                    else
                    if(byte0 == 0 && byte1 == -1 && byte3 == 1 && byte2 == 1)
                        _fld0134[j1][i1] = 9;
                    else
                    if(byte0 == 1 && byte1 == 0 && byte2 == -1 && byte3 == -1)
                        _fld0134[j1][i1] = 6;
                    else
                    if(byte0 == 1 && byte1 == 0 && byte2 == -1 && byte3 == 1)
                        _fld0134[j1][i1] = 10;
                    else
                    if(byte2 == 0 && byte3 == 1 && byte1 == -1 && byte0 == -1)
                        _fld0134[j1][i1] = 7;
                    else
                    if(byte2 == 0 && byte3 == 1 && byte1 == -1 && byte0 == 1)
                        _fld0134[j1][i1] = 11;
                    else
                    if(byte0 == -1 && byte1 == 0 && byte2 == 1 && byte3 == -1)
                        _fld0134[j1][i1] = 8;
                    else
                    if(byte0 == -1 && byte1 == 0 && byte2 == 1 && byte3 == 1)
                        _fld0134[j1][i1] = 12;
                    else
                    if(byte0 == 0 && byte1 == -1 && byte2 == -1 && byte3 == 0)
                        _fld0134[j1][i1] = 1;
                    else
                    if(byte0 == 0 && byte1 == -1 && byte2 == 1 && byte3 == 0)
                        _fld0134[j1][i1] = 2;
                    else
                    if(byte2 == 0 && byte3 == 1 && byte0 == 1 && byte1 == 0)
                        _fld0134[j1][i1] = 3;
                    else
                    if(byte2 == 0 && byte3 == 1 && byte0 == -1 && byte1 == 0)
                        _fld0134[j1][i1] = 4;
                } else
                if(_fld0131[j1][i1] == 88)
                {
                    if(j1 > 0 && _fld0131[j1 - 1][i1] != 88)
                    {
                        int i4 = j1 * 2;
                        int l6 = i1 * 2;
                        int j10 = j1 * 2;
                        int l13 = i1 * 2 + 2;
                        w(i4, l6, j10, l13, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                    }
                    if(j1 < _fld012F - 1 && _fld0131[j1 + 1][i1] != 88)
                    {
                        int j4 = j1 * 2 + 2;
                        int i7 = i1 * 2;
                        int k10 = j1 * 2 + 2;
                        int i14 = i1 * 2 + 2;
                        w(j4, i7, k10, i14, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                    }
                    if(i1 > 0 && _fld0131[j1][i1 - 1] != 88)
                    {
                        int k4 = j1 * 2;
                        int j7 = i1 * 2;
                        int l10 = j1 * 2 + 2;
                        int j14 = i1 * 2;
                        w(k4, j7, l10, j14, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                    }
                    if(i1 < _fld0130 - 1 && _fld0131[j1][i1 + 1] != 88)
                    {
                        int l4 = j1 * 2;
                        int k7 = i1 * 2 + 2;
                        int i11 = j1 * 2 + 2;
                        int k14 = i1 * 2 + 2;
                        w(l4, k7, i11, k14, _fld0132[j1][i1], _fld0132[j1][i1], _fld0133[j1][i1]);
                    }
                }
            }

        }

        _fld012B._mth0121(true, paramAmbient, paramContrast);
        scene.addModel(_fld012B);
        for(int k1 = 0; k1 < _fld0130; k1++)
        {
            for(int l1 = 0; l1 < _fld012F; l1++)
            {
                int i5 = _fld0134[l1][k1];
                int l7 = l1 * 2;
                int j11 = k1 * 2;
                if(i5 != 88)
                {
                    int l14 = _fld0135[l7 / 2][j11 / 2];
                    int k15 = _fld0135[l7 / 2 + 1][j11 / 2];
                    int j16 = _fld0135[l7 / 2 + 1][j11 / 2 + 1];
                    int l16 = _fld0135[l7 / 2][j11 / 2 + 1];
                    if(l14 == k15 && k15 == j16 && j16 == l16)
                        u(l7, j11, l7 + 2, j11, l7 + 2, j11 + 2, l7, j11 + 2);
                    else
                    if(i5 == 0 || i5 == 32)
                    {
                        if(l14 == k15 && j16 == l16 || l14 == l16 && k15 == j16)
                        {
                            u(l7, j11, l7 + 2, j11, l7 + 2, j11 + 2, l7, j11 + 2);
                        } else
                        {
                            o(l7, j11, l7 + 2, j11, l7, j11 + 2);
                            o(l7 + 2, j11 + 2, l7, j11 + 2, l7 + 2, j11);
                        }
                    } else
                    if(i5 == 47)
                    {
                        o(l7, j11, l7 + 2, j11, l7, j11 + 2);
                        o(l7 + 2, j11 + 2, l7, j11 + 2, l7 + 2, j11);
                    } else
                    if(i5 == 92)
                    {
                        o(l7 + 2, j11, l7 + 2, j11 + 2, l7, j11);
                        o(l7, j11 + 2, l7, j11, l7 + 2, j11 + 2);
                    } else
                    if(i5 == 45)
                    {
                        o(l7, j11, l7 + 2, j11, l7, j11 + 1);
                        o(l7 + 2, j11 + 1, l7, j11 + 1, l7 + 2, j11);
                        o(l7, j11 + 1, l7 + 2, j11 + 1, l7, j11 + 2);
                        o(l7 + 2, j11 + 2, l7, j11 + 2, l7 + 2, j11 + 1);
                    } else
                    if(i5 == 124)
                    {
                        o(l7, j11, l7 + 1, j11, l7, j11 + 2);
                        o(l7 + 1, j11 + 2, l7, j11 + 2, l7 + 1, j11);
                        o(l7 + 1, j11, l7 + 2, j11, l7 + 1, j11 + 2);
                        o(l7 + 2, j11 + 2, l7 + 1, j11 + 2, l7 + 2, j11);
                    } else
                    if(i5 >= 16 && i5 < 32)
                    {
                        if(_fld0139[l7 + 1][j11] == 0)
                        {
                            o(l7, j11, l7 + 2, j11, l7 + 1, j11 + 1);
                        } else
                        {
                            o(l7 + 1, j11, l7 + 1, j11 + 1, l7, j11);
                            o(l7 + 1, j11, l7 + 2, j11, l7 + 1, j11 + 1);
                        }
                        if(_fld0139[l7][j11 + 1] == 0)
                        {
                            o(l7, j11, l7 + 1, j11 + 1, l7, j11 + 2);
                        } else
                        {
                            o(l7, j11 + 1, l7, j11, l7 + 1, j11 + 1);
                            o(l7, j11 + 1, l7 + 1, j11 + 1, l7, j11 + 2);
                        }
                        if(_fld0139[l7 + 2][j11 + 1] == 0)
                        {
                            o(l7 + 2, j11, l7 + 2, j11 + 2, l7 + 1, j11 + 1);
                        } else
                        {
                            o(l7 + 2, j11 + 1, l7 + 1, j11 + 1, l7 + 2, j11);
                            o(l7 + 2, j11 + 1, l7 + 2, j11 + 2, l7 + 1, j11 + 1);
                        }
                        if(_fld0139[l7 + 1][j11 + 2] == 0)
                        {
                            o(l7, j11 + 2, l7 + 1, j11 + 1, l7 + 2, j11 + 2);
                        } else
                        {
                            o(l7 + 1, j11 + 2, l7, j11 + 2, l7 + 1, j11 + 1);
                            o(l7 + 1, j11 + 2, l7 + 1, j11 + 1, l7 + 2, j11 + 2);
                        }
                    } else
                    if(i5 == 1)
                    {
                        o(l7, j11, l7 + 1, j11, l7, j11 + 1);
                        o(l7 + 2, j11, l7 + 2, j11 + 2, l7 + 1, j11);
                        o(l7, j11 + 2, l7, j11 + 1, l7 + 2, j11 + 2);
                        o(l7 + 2, j11 + 2, l7, j11 + 1, l7 + 1, j11);
                    } else
                    if(i5 == 2)
                    {
                        o(l7 + 2, j11, l7 + 2, j11 + 1, l7 + 1, j11);
                        o(l7, j11, l7 + 1, j11, l7, j11 + 2);
                        o(l7 + 2, j11 + 2, l7, j11 + 2, l7 + 2, j11 + 1);
                        o(l7, j11 + 2, l7 + 1, j11, l7 + 2, j11 + 1);
                    } else
                    if(i5 == 3)
                    {
                        o(l7 + 2, j11, l7 + 2, j11 + 1, l7, j11);
                        o(l7, j11 + 2, l7, j11, l7 + 1, j11 + 2);
                        o(l7, j11, l7 + 2, j11 + 1, l7 + 1, j11 + 2);
                        o(l7 + 2, j11 + 2, l7 + 1, j11 + 2, l7 + 2, j11 + 1);
                    } else
                    if(i5 == 4)
                    {
                        o(l7, j11, l7 + 2, j11, l7, j11 + 1);
                        o(l7, j11 + 2, l7, j11 + 1, l7 + 1, j11 + 2);
                        o(l7 + 2, j11 + 2, l7 + 1, j11 + 2, l7 + 2, j11);
                        o(l7 + 2, j11, l7 + 1, j11 + 2, l7, j11 + 1);
                    } else
                    if(i5 == 5 || i5 == 9)
                    {
                        o(l7, j11, l7 + 1, j11, l7, j11 + 2);
                        o(l7 + 2, j11, l7 + 2, j11 + 2, l7 + 1, j11);
                        o(l7 + 1, j11, l7 + 2, j11 + 2, l7, j11 + 2);
                    } else
                    if(i5 == 6 || i5 == 10)
                    {
                        o(l7 + 2, j11, l7 + 2, j11 + 1, l7, j11);
                        o(l7 + 2, j11 + 2, l7, j11 + 2, l7 + 2, j11 + 1);
                        o(l7 + 2, j11 + 1, l7, j11 + 2, l7, j11);
                    } else
                    if(i5 == 7 || i5 == 11)
                    {
                        o(l7, j11 + 2, l7, j11, l7 + 1, j11 + 2);
                        o(l7 + 2, j11 + 2, l7 + 1, j11 + 2, l7 + 2, j11);
                        o(l7 + 1, j11 + 2, l7, j11, l7 + 2, j11);
                    } else
                    if(i5 == 8 || i5 == 12)
                    {
                        o(l7, j11, l7 + 2, j11, l7, j11 + 1);
                        o(l7, j11 + 2, l7, j11 + 1, l7 + 2, j11 + 2);
                        o(l7, j11 + 1, l7 + 2, j11, l7 + 2, j11 + 2);
                    }
                }
            }

        }

        _fld012C._mth0121(true, 75, 24);
        if(!interlace)
            scene.addModel(_fld012C);
        _fld0141 = 0;
        for(int i2 = 0; i2 < _fld0130; i2++)
        {
            for(int j5 = 0; j5 < _fld012F; j5++)
                if(_fld0132[j5][i2] == 1)
                {
                    int i8 = 0;
                    if(_fld0132[j5][i2 - 1] >= 100)
                        i8 = _fld0132[j5][i2 - 1] - 100;
                    else
                    if(_fld0132[j5][i2 + 1] >= 100)
                        i8 = _fld0132[j5][i2 + 1] - 100;
                    else
                    if(_fld0132[j5 + 1][i2] >= 100)
                        i8 = _fld0132[j5 + 1][i2] - 100;
                    else
                    if(_fld0132[j5 - 1][i2] >= 100)
                        i8 = _fld0132[j5 - 1][i2] - 100;
                    _fld0142[j5][i2] = _fld0141;
                    _fld0143[j5][i2] = 0;
                    GameModel k11 = _fld0140[_fld0141++] = new GameModel(8, 2);
                    int i15;
                    int l15;
                    byte byte6;
                    byte byte8;
                    int j17;
                    int k17;
                    if(_fld0131[j5][i2] == 45)
                    {
                        i15 = 0;
                        l15 = 0;
                        byte6 = 96;
                        byte8 = 0;
                        j17 = j5 * 128 + 17;
                        k17 = i2 * 128 + 64;
                    } else
                    {
                        i15 = 0;
                        l15 = 0;
                        byte6 = 0;
                        byte8 = 96;
                        j17 = j5 * 128 + 64;
                        k17 = i2 * 128 + 17;
                    }
                    int l17 = -_fld0133[j5][i2] * 42;
                    k11._mth012D(i15, 0, l15);
                    k11._mth012D(i15, l17, l15);
                    k11._mth012D(byte6, l17, byte8);
                    k11._mth012D(byte6, 0, byte8);
                    int ai[] = {
                        0, 1, 2, 3
                    };
                    if(i8 == 5)
                    {
                        k11._mth012E(4, ai, 7, 7);
                    } else
                    {
                        k11._mth012E(4, ai, 5, 5);
                        byte byte4;
                        byte byte5;
                        byte byte7;
                        byte byte9;
                        if(_fld0131[j5][i2] == 45)
                        {
                            byte4 = 11;
                            byte5 = 0;
                            byte7 = 59;
                            byte9 = 0;
                        } else
                        {
                            byte4 = 0;
                            byte5 = 11;
                            byte7 = 0;
                            byte9 = 59;
                        }
                        k11._mth012D(byte4, (l17 * 28) / 128, byte5);
                        k11._mth012D(byte4, (l17 * 94) / 128, byte5);
                        k11._mth012D(byte7, (l17 * 94) / 128, byte9);
                        k11._mth012D(byte7, (l17 * 28) / 128, byte9);
                        int ai1[] = {
                            8, 9, 10, 11, 12, 12, 13, 14, 15, 16, 
                            17
                        };
                        int ai2[] = {
                            4, 5, 6, 7
                        };
                        k11._mth012E(4, ai2, ai1[i8], ai1[i8]);
                    }
                    k11._mth011B(j17, 0, k17);
                    k11._mth0121(true, 75, 24);
                    scene.addModel(k11);
                }

        }

    }

    public boolean C4(int i1, int j1, char c1)
    {
        if(i1 < 0 || j1 < 0 || i1 >= _fld012F || j1 >= _fld0130)
            return false;
        char c2 = (char)_fld0131[i1][j1];
        if(c1 == '*' && c2 != '*')
            return false;
        if(c1 == 'o' && c2 != 'o')
            return false;
        return c2 > 0;
    }

    public boolean q(int i1, int j1)
    {
        if(i1 < 0 || j1 < 0 || i1 >= _fld012F || j1 >= _fld0130)
            return false;
        char c1 = (char)_fld0131[i1][j1];
        if(c1 >= '0' && c1 <= '9')
            return false;
        return c1 > 0;
    }

    public void r()
    {
        try
        {
            for(int i1 = 0; i1 < _fld012F * 3; i1++)
            {
                for(int j1 = 0; j1 < _fld0130 * 3; j1++)
                    if(_fld0132[i1 / 3][j1 / 3] - 100 == _fld0146)
                        _fld0147[i1][j1] = 0;
                    else
                    if(_fld0132[i1 / 3][j1 / 3] >= 100)
                        _fld0147[i1][j1] = 100;
                    else
                    if(_fld0132[i1 / 3][j1 / 3] == 1)
                        _fld0147[i1][j1] = 0;
                    else
                        _fld0147[i1][j1] = m(22 + (i1 * 128) / 3, 22 + (j1 * 128) / 3) ? 100 : 0;

            }

            int k1 = (_fld0137 * 3) / 128;
            int l1 = (_fld0138 * 3) / 128;
            int i2 = _fld0137 * 3 & 0x7f;
            int j2 = _fld0138 * 3 & 0x7f;
            if(_fld0147[k1][l1] != 0)
                if(i2 < 64 && k1 > 0 && _fld0147[k1 - 1][l1] == 0)
                    k1--;
                else
                if(i2 >= 64 && k1 < _fld012F - 1 && _fld0147[k1 + 1][l1] == 0)
                    k1++;
                else
                if(j2 < 64 && l1 > 0 && _fld0147[k1][l1 - 1] == 0)
                    l1--;
                else
                if(j2 >= 64 && l1 > _fld0130 - 1 && _fld0147[k1][l1 + 1] == 0)
                    l1++;
            _fld014A = 0;
            _fld0147[k1][l1] = 100;
            int ai[] = new int[_fld012F * _fld0130 * 9];
            int ai1[] = new int[_fld012F * _fld0130 * 9];
            int i3 = 0;
            int j3 = 1;
            ai[0] = k1;
            ai1[0] = l1;
            int k2;
            int l2;
            do
            {
                int k3 = ai[i3];
                int l3 = ai1[i3];
                i3++;
                _fld014A++;
                if(_fld0132[k3 / 3][l3 / 3] == _fld0146 + 100 && k3 % 3 == 1 && l3 % 3 == 1)
                {
                    k2 = k3;
                    l2 = l3;
                    break;
                }
                if(_fld0147[k3][l3] == 1 || _fld0147[k3][l3] == 3)
                {
                    if(k3 > 0 && _fld0147[k3 - 1][l3] == 0)
                    {
                        ai[j3] = k3 - 1;
                        ai1[j3] = l3;
                        _fld0147[k3 - 1][l3] = 1;
                        j3++;
                    }
                    if(k3 < _fld012F * 3 - 1 && _fld0147[k3 + 1][l3] == 0)
                    {
                        ai[j3] = k3 + 1;
                        ai1[j3] = l3;
                        _fld0147[k3 + 1][l3] = 3;
                        j3++;
                    }
                    if(l3 > 0 && _fld0147[k3][l3 - 1] == 0)
                    {
                        ai[j3] = k3;
                        ai1[j3] = l3 - 1;
                        _fld0147[k3][l3 - 1] = 2;
                        j3++;
                    }
                    if(l3 < _fld0130 * 3 - 1 && _fld0147[k3][l3 + 1] == 0)
                    {
                        ai[j3] = k3;
                        ai1[j3] = l3 + 1;
                        _fld0147[k3][l3 + 1] = 4;
                        j3++;
                    }
                } else
                {
                    if(l3 > 0 && _fld0147[k3][l3 - 1] == 0)
                    {
                        ai[j3] = k3;
                        ai1[j3] = l3 - 1;
                        _fld0147[k3][l3 - 1] = 2;
                        j3++;
                    }
                    if(l3 < _fld0130 * 3 - 1 && _fld0147[k3][l3 + 1] == 0)
                    {
                        ai[j3] = k3;
                        ai1[j3] = l3 + 1;
                        _fld0147[k3][l3 + 1] = 4;
                        j3++;
                    }
                    if(k3 > 0 && _fld0147[k3 - 1][l3] == 0)
                    {
                        ai[j3] = k3 - 1;
                        ai1[j3] = l3;
                        _fld0147[k3 - 1][l3] = 1;
                        j3++;
                    }
                    if(k3 < _fld012F * 3 - 1 && _fld0147[k3 + 1][l3] == 0)
                    {
                        ai[j3] = k3 + 1;
                        ai1[j3] = l3;
                        _fld0147[k3 + 1][l3] = 3;
                        j3++;
                    }
                }
            } while(true);
            _fld0148 = new int[_fld014A + 1];
            _fld0149 = new int[_fld014A + 1];
            _fld0148[0] = k2;
            _fld0149[0] = l2;
            j3 = 1;
            do
            {
                _fld0148[j3] = k2;
                _fld0149[j3] = l2;
                j3++;
                if(k2 == k1 && l2 == l1)
                    break;
                if(_fld0147[k2][l2] == 1)
                    k2++;
                else
                if(_fld0147[k2][l2] == 2)
                    l2++;
                else
                if(_fld0147[k2][l2] == 3)
                    k2--;
                else
                if(_fld0147[k2][l2] == 4)
                    l2--;
            } while(true);
            _fld014A = j3;
            _fld0146 = 100;
            return;
        }
        catch(Exception _ex)
        {
            gameStatus = 2;
        }
        currentGame = _fld0146;
        _fld0146 = -1;
    }

    public void h()
    {
        try
        {
            if(super.inputPmFinal != "")
            {
                if(GameDialog.EE(super.inputPmFinal) == 0)
                    super.inputPmFinal = "censorme99";
                stream.newPacket(18);
                stream.putString(super.inputPmFinal);
                stream.sendPacket();
                F1 = 0;
                super.inputPmCurrent = "";
                super.inputPmFinal = "";
            }
            if(lastpsize == 0 && stream.available() >= 2)
                lastpsize = stream.getShort();
            if(lastpsize > 0 && stream.available() >= lastpsize)
            {
                stream.readStreamBytes(lastpsize, lastpdata);
                lastptype = stream.byte2int(lastpdata[0]);
                if(lastptype == 8)
                {
                    D0 = true;
                    for(int i1 = 2; i1 >= 1; i1--)
                        chatMessages[i1] = chatMessages[i1 - 1];

                    chatMessages[0] = new String(lastpdata, 1, lastpsize - 1);
                } else
                if(lastptype == 25)
                {
                    createScreenNewAccount();
                    drawScreenNewAccount = true;
                } else
                if(lastptype == 255)
                {
                    EB = true;
                    D0 = true;
                    for(int j1 = 0; j1 < 11; j1++)
                        EC[j1] = stream.getShort(lastpdata, 1 + j1 * 2);

                }
                lastpsize = 0;
                return;
            }
        }
        catch(IOException _ex)
        {
            packetError = true;
        }
    }

    public void p()
    {
        f();
        h();
        if(super.lastMouseButtonDown != 0 && super.mouseY > 351)
        {
            int i1 = (super.mouseX - 3) / 46;
            if(i1 < 0)
                i1 = 0;
            else
            if(i1 > 10)
                i1 = 10;
            _fld0146 = i1;
            super.lastMouseButtonDown = 0;
        }
        if(_fld0144 == -1 || _fld0145 == -1)
        {
            if(_fld013D > 0)
                _fld013D--;
            int j1 = 0;
            int l1 = 0;
            if(_fld0146 == -1)
            {
                if(super.keyLeft)
                    _fld0136 = _fld0136 + 2 & 0xff;
                if(super.keyRight)
                    _fld0136 = _fld0136 + 253 & 0xff;
                if(super.keyUp)
                {
                    j1 = -(int)(Math.sin(((double)_fld0136 * 6.2800000000000002D) / 256D) * 10D);
                    l1 = (int)(Math.cos(((double)_fld0136 * 6.2800000000000002D) / 256D) * 10D);
                }
                if(super.keyDown)
                {
                    j1 = (int)(Math.sin(((double)_fld0136 * 6.2800000000000002D) / 256D) * 10D);
                    l1 = -(int)(Math.cos(((double)_fld0136 * 6.2800000000000002D) / 256D) * 10D);
                }
            } else
            {
                if(_fld0146 < 100)
                    r();
                if(_fld0146 != -1)
                {
                    int j2 = _fld0148[_fld014A - (_fld0146 - 99)];
                    int i3 = _fld0149[_fld014A - (_fld0146 - 99)];
                    int l3 = _fld0149[_fld014A - (_fld0146 - 99) - 1];
                    int j4 = (j2 * 128) / 3 + 22;
                    int l4 = (i3 * 128) / 3 + 22;
                    int i5 = (j2 * 128) / 3 + 22;
                    int j5 = (l3 * 128) / 3 + 22;
                    int k5 = (int)((Math.atan2(_fld0137 - j4, l4 - _fld0138) * 4D) / 6.2800000000000002D) * 64 & 0xff;
                    int l5 = (int)((Math.atan2(_fld0137 - i5, j5 - _fld0138) * 4D) / 6.2800000000000002D) * 64 & 0xff;
                    if(_fld0137 == j4 && _fld0138 == l4)
                        k5 = _fld0136;
                    if(l5 != k5)
                        k5 = _fld0136;
                    int i6 = k5 - _fld0136;
                    if(i6 < 4 && i6 > -4)
                        _fld0136 = k5;
                    else
                    if(i6 < -128 || i6 > 0 && i6 < 128)
                        _fld0136 = _fld0136 + 3 & 0xff;
                    else
                        _fld0136 = _fld0136 - 3 & 0xff;
                    if(_fld0136 == k5)
                    {
                        if(_fld0137 - j4 > -15 && _fld0137 - j4 < 15)
                            j1 = j4 - _fld0137;
                        else
                        if(_fld0137 < j4)
                            j1 = 15;
                        else
                        if(_fld0137 > j4)
                            j1 = -15;
                        if(_fld0138 - l4 > -15 && _fld0138 - l4 < 15)
                            l1 = l4 - _fld0138;
                        else
                        if(_fld0138 < l4)
                            l1 = 15;
                        else
                        if(_fld0138 > l4)
                            l1 -= 15;
                        if(_fld0137 - j4 > -15 && _fld0137 - j4 < 15 && _fld0138 - l4 > -15 && _fld0138 - l4 < 15 && _fld0146 < _fld014A + 100)
                            _fld0146++;
                    }
                }
            }
            while(j1 != 0 || l1 != 0) 
            {
                _fld0137 += j1;
                if(m(_fld0137 - 10, _fld0138 - 10) || m(_fld0137 + 10, _fld0138 - 10) || m(_fld0137 - 10, _fld0138 + 10) || m(_fld0137 + 10, _fld0138 + 10))
                    _fld0137 -= j1;
                else
                    j1 = 0;
                _fld0138 += l1;
                if(m(_fld0137 - 10, _fld0138 - 10) || m(_fld0137 + 10, _fld0138 - 10) || m(_fld0137 - 10, _fld0138 + 10) || m(_fld0137 + 10, _fld0138 + 10))
                    _fld0138 -= l1;
                else
                    l1 = 0;
                if(j1 > 0)
                    j1--;
                else
                if(j1 < 0)
                    j1++;
                if(l1 > 0)
                    l1--;
                else
                if(l1 < 0)
                    l1++;
            }
            for(int k2 = 0; k2 < _fld012F; k2++)
            {
                for(int j3 = 0; j3 < _fld0130; j3++)
                {
                    if(_fld0143[k2][j3] >= 100 && _fld0143[k2][j3] < 200)
                    {
                        _fld0143[k2][j3] += 2;
                        _fld0140[_fld0142[k2][j3]]._mth011F(0, _fld0143[k2][j3] - 100, 0);
                        if(_fld0143[k2][j3] >= 166)
                            _fld0143[k2][j3] = 300;
                    }
                    if(_fld0143[k2][j3] >= 200 && _fld0143[k2][j3] < 300)
                    {
                        _fld0143[k2][j3] += 2;
                        _fld0140[_fld0142[k2][j3]]._mth011F(0, 256 - (_fld0143[k2][j3] - 200) & 0xff, 0);
                        if(_fld0143[k2][j3] >= 266)
                            _fld0143[k2][j3] = 400;
                    }
                    if(_fld0143[k2][j3] == 300 || _fld0143[k2][j3] == 400)
                    {
                        int i4 = k2 * 128 + 64;
                        int k4 = j3 * 128 + 64;
                        if(i4 - _fld0137 < -256 || i4 - _fld0137 > 256 || k4 - _fld0138 < -256 || k4 - _fld0138 > 256)
                            _fld0143[k2][j3] += 2;
                    }
                    if(_fld0143[k2][j3] > 300 && _fld0143[k2][j3] < 400)
                    {
                        _fld0143[k2][j3] += 2;
                        _fld0140[_fld0142[k2][j3]]._mth011F(0, 66 - (_fld0143[k2][j3] - 300), 0);
                        if(_fld0143[k2][j3] >= 366)
                            _fld0143[k2][j3] = 0;
                    }
                    if(_fld0143[k2][j3] > 400 && _fld0143[k2][j3] < 500)
                    {
                        _fld0143[k2][j3] += 2;
                        _fld0140[_fld0142[k2][j3]]._mth011F(0, (190 + _fld0143[k2][j3]) - 400 & 0xff, 0);
                        if(_fld0143[k2][j3] >= 466)
                            _fld0143[k2][j3] = 0;
                    }
                }

            }

            return;
        }
        int k1 = _fld0144 * 128 + 64;
        int i2 = _fld0145 * 128 + 64;
        int l2 = (int)((Math.atan2(_fld0137 - k1, i2 - _fld0138) * 256D) / 6.2800000000000002D) & 0xff;
        int k3 = l2 - _fld0136;
        if(k3 < -128 || k3 > 0 && k3 < 128)
            _fld0136 = _fld0136 + 2 & 0xff;
        else
            _fld0136 = _fld0136 - 2 & 0xff;
        if(k3 < 4 && k3 > -4)
            _fld0136 = l2;
        if(_fld0136 == l2 && _fld013D < 20)
            _fld013D++;
        if(_fld013D == 20)
        {
            gameStatus = 2;
            currentGame = _fld0132[_fld0144][_fld0145] - 100;
        }
    }

    public boolean m(int i1, int j1)
    {
        int k1 = i1 >> 7;
        int l1 = j1 >> 7;
        int i2 = i1 & 0x7f;
        int j2 = j1 & 0x7f;
        int k2 = _fld0134[k1][l1];
        if(k2 == 0)
            return false;
        if(_fld0132[k1][l1] == 1)
        {
            if(_fld0143[k1][l1] < 100)
                if(k2 == 45)
                {
                    if(j2 > 64)
                        _fld0143[k1][l1] += 100;
                    else
                        _fld0143[k1][l1] += 200;
                } else
                if(k2 == 124)
                    if(i2 > 64)
                        _fld0143[k1][l1] += 200;
                    else
                        _fld0143[k1][l1] += 100;
            if(_fld0143[k1][l1] == 300 || _fld0143[k1][l1] == 400)
                return false;
        }
        if(_fld0131[k1][l1] == 48 || _fld0131[k1][l1] == 49 || _fld0131[k1][l1] == 50)
        {
            _fld0144 = k1;
            _fld0145 = l1;
            return true;
        }
        if(k2 == 45 && (j2 < 54 || j2 > 74))
            return false;
        if(k2 == 124 && (i2 < 54 || i2 > 74))
            return false;
        if(k2 == 1 && (i2 > 74 || j2 > 74))
            return false;
        if(k2 == 2 && (i2 < 54 || j2 > 74))
            return false;
        if(k2 == 3 && (i2 < 54 || j2 < 54))
            return false;
        if(k2 == 4 && (i2 > 74 || j2 < 54))
            return false;
        if(k2 == 5 && i2 > 74)
            return false;
        if(k2 == 9 && i2 < 54)
            return false;
        if(k2 == 6 && j2 > 74)
            return false;
        if(k2 == 10 && j2 < 54)
            return false;
        if(k2 == 7 && i2 > 74)
            return false;
        if(k2 == 11 && i2 < 54)
            return false;
        if(k2 == 8 && j2 > 74)
            return false;
        if(k2 == 12 && j2 < 54)
            return false;
        if(k2 == 92 && (i2 - j2 < -10 || i2 - j2 > 10))
            return false;
        if(k2 == 47 && (128 - i2 - j2 < -10 || 128 - i2 - j2 > 10))
            return false;
        if(k2 >= 16 && k2 < 32)
        {
            if(((k2 -= 16) & 1) == 1 && i2 >= 54 && i2 <= 74 && j2 <= 74)
                return true;
            if((k2 & 2) == 2 && i2 >= 54 && i2 <= 74 && j2 >= 54)
                return true;
            if((k2 & 4) == 4 && j2 >= 54 && j2 <= 74 && i2 <= 74)
                return true;
            return (k2 & 8) == 8 && j2 >= 54 && j2 <= 74 && i2 >= 54;
        } else
        {
            return true;
        }
    }

    public void drawGameScreen()
    {
        if(gameStatus == 1)
        {
            if(!interlace)
            {
                surface2mby._mth01C7(0, 0, 500, 145, Color.black);
                surface2mby._mth01CA(0, 145, 500, 145, Color.black, paramGround, interlace);
            } else
            {
                surface2mby._mth01CA(0, 0, 500, 145, paramGround, Color.black, interlace);
                surface2mby._mth01CA(0, 145, 500, 145, Color.black, paramGround, interlace);
            }
            scene._mth0275(_fld0137, _fld013E, _fld0138, _fld013D, _fld0136, 0);
            scene._mth025A(interlace);
            if(_fld014B != super.inputPmCurrent)
            {
                D0 = true;
                _fld014B = super.inputPmCurrent;
            }
            if(!D0)
            {
                surface2mby._mth01C2(appletGraphics, 6, 6, interlace);
                return;
            }
            surface2mby._mth01C2(gameGraphics, 6, 6, interlace);
            gameGraphics.drawImage(gamePanelGamesImage, 0, 298, this);
            gameGraphics.setColor(Color.white);
            gameGraphics.setFont(fontH13);
            if(super.inputPmCurrent != null)
                gameGraphics.drawString(super.inputPmCurrent, 8, 349);
            for(int i1 = 0; i1 < 3; i1++)
                if(chatMessages[i1] != null)
                    gameGraphics.drawString(chatMessages[i1], 8, 336 - i1 * 12);

            if(EB)
            {
                gameGraphics.setColor(Color.black);
                for(int j1 = 0; j1 < 11; j1++)
                    GameDialog.D8(gameGraphics, String.valueOf(EC[j1]), fontH10B, 46 + j1 * 46, 362);

            }
            appletGraphics.drawImage(gameImage, 0, 0, this);
            D0 = false;
        }
    }

    public void loadGame()
    {
        scene._mth025A(interlace);
        surface2mby._mth01B3(96, 105, 320, 40, colorGrey, 140, interlace);
        surface2mby._mth01C2(gameGraphics, 6, 6, interlace);
        gameGraphics.setColor(Color.black);
        GameDialog.drawstringCenter(gameGraphics, "Please Wait... Loading Game", fontH23, 256, 130);
        appletGraphics.drawImage(gameImage, 0, 0, this);
        E3 = true;
        int i1 = 0;
        try
        {
            stream.newPacket(15);
            stream.sendPacket();
            if(games[currentGame] == null)
                if(currentGame == 0)
                    games[currentGame] = new Checkers();
                else
                if(currentGame == 1)
                    games[currentGame] = new Chess();
                else
                if(currentGame == 2)
                    games[currentGame] = new Battleships();
                else
                if(currentGame == 3)
                    games[currentGame] = new Quadlink();
                else
                if(currentGame == 4)
                    games[currentGame] = new Reversi();
                else
                if(currentGame == 5)
                    games[currentGame] = new Monster();
                else
                if(currentGame == 6)
                    games[currentGame] = new Mahjong();
                else
                if(currentGame == 7)
                    games[currentGame] = new Pairs();
                else
                if(currentGame == 8)
                    games[currentGame] = new Crypt();
                else
                if(currentGame == 9)
                    games[currentGame] = new Dungeon();
                else
                if(currentGame == 10)
                    games[currentGame] = new Cyber();
            i1 = 1;
            games[currentGame].setCastle(this);
            i1 = 0;
        }
        catch(Throwable throwable)
        {
            if(gameStatus != 0 && stream != null && _fld0106)
            {
                _fld0106 = false;
                stream.newPacket(17);
                stream.putString("l: " + throwable + " s:" + gameStatus + " g:" + currentGame + " dl:" + i1);
                stream.sendPacket();
            }
        }
        E3 = false;
        _fld0153 = 0;
        _fld014F = 0;
        for(int j1 = 0; j1 < 3; j1++)
            chatMessages[j1] = "";

        if(games[currentGame]._fld03C3 == 2)
            _fld0156 = true;
        else
            _fld0156 = false;
        stream.newPacket(16);
        stream.putByte(currentGame + 1);
        stream.sendPacket();
        _fld0160 = -330;
        _fld0104 = 0;
        _fld0105 = 0;
    }

    public void l()
    {
        try
        {
            if(super.inputPmFinal != "")
            {
                if(GameDialog.EE(super.inputPmFinal) == 0)
                    super.inputPmFinal = "censorme99";
                stream.newPacket(18);
                stream.putString(super.inputPmFinal);
                stream.sendPacket();
                F1 = 0;
                super.inputPmCurrent = "";
                super.inputPmFinal = "";
            }
            if(lastpsize == 0 && stream.available() >= 2)
                lastpsize = stream.getShort();
            if(lastpsize > 0 && stream.available() >= lastpsize)
            {
                stream.readStreamBytes(lastpsize, lastpdata);
                lastptype = stream.byte2int(lastpdata[0]);
                if(lastptype == 5)
                {
                    playerCount = stream.getShort(lastpdata, 1);
                    castleId = stream.getShort(lastpdata, 3);
                    int i1 = (lastpsize - 5) / 18;
                    F8 = 0;
                    for(int k2 = 0; k2 < maxPlayerCount; k2++)
                        playerNames[k2] = null;

                    for(int j3 = 0; j3 < i1; j3++)
                    {
                        int l3 = stream.getShort(lastpdata, 5 + j3 * 18);
                        playerUnknown[l3] = stream.getShort(lastpdata, 7 + j3 * 18);
                        playerPoints[l3] = stream.getShort(lastpdata, 9 + j3 * 18);
                        playerNames[l3] = (new String(lastpdata, 11 + j3 * 18, 12)).trim();
                        if(l3 + 1 > F8)
                            F8 = l3 + 1;
                    }

                } else
                if(lastptype == 6)
                {
                    int j1 = (lastpsize - 1) / 18;
                    _fld0157 = 0;
                    for(int l2 = 0; l2 < _fld0159; l2++)
                        _fld015A[l2] = null;

                    for(int k3 = 0; k3 < j1; k3++)
                    {
                        int i4 = stream.getShort(lastpdata, 1 + k3 * 18);
                        _fld015A[i4] = (new String(lastpdata, 3 + k3 * 18, 12)).trim();
                        _fld015B[i4] = stream.getShort(lastpdata, 15 + k3 * 18);
                        _fld015C[i4] = stream.getShort(lastpdata, 17 + k3 * 18);
                        if(i4 + 1 > _fld0157)
                            _fld0157 = i4 + 1;
                    }

                } else
                if(lastptype == 7)
                {
                    points = stream.getInt(lastpdata, 1);
                    for(int k1 = 0; k1 < 50; k1++)
                    {
                        FD[k1] = (new String(lastpdata, 5 + k1 * 20, 12)).trim();
                        FE[k1] = stream.getInt(lastpdata, 17 + k1 * 20);
                        FF[k1] = stream.getShort(lastpdata, 21 + k1 * 20);
                        _fld0100[k1] = stream.getShort(lastpdata, 23 + k1 * 20);
                    }

                    _fld0101 = true;
                } else
                if(lastptype == 8)
                {
                    D0 = true;
                    for(int l1 = 2; l1 >= 1; l1--)
                        chatMessages[l1] = chatMessages[l1 - 1];

                    chatMessages[0] = new String(lastpdata, 1, lastpsize - 1);
                } else
                if(lastptype == 9 && _fld0156)
                {
                    _fld0154 = playerNames[stream.getShort(lastpdata, 1)] + " has challenged you!";
                    currentGameTime = stream.getShort(lastpdata, 3);
                    currentGameType = stream.getShort(lastpdata, 5);
                    _fld0153 = 1;
                } else
                if(lastptype == 10 && _fld0156)
                {
                    _fld0154 = "Challenging: " + playerNames[stream.getShort(lastpdata, 1)];
                    currentGameTime = stream.getShort(lastpdata, 3);
                    currentGameType = stream.getShort(lastpdata, 5);
                    _fld0153 = 2;
                } else
                if(lastptype == 11 && _fld0156)
                {
                    _fld0154 = "Player busy with another challenge";
                    _fld0155 = "";
                    _fld0153 = 3;
                } else
                if(lastptype == 12 && _fld0153 == 2 && _fld0156)
                {
                    _fld0154 = "Other player declined challenge";
                    _fld0155 = "";
                    _fld0153 = 3;
                } else
                if(lastptype == 12 && _fld0153 == 1 && _fld0156)
                {
                    _fld0154 = "Other player aborted challenge";
                    _fld0155 = "";
                    _fld0153 = 3;
                } else
                if(lastptype == 13 && !_fld0156)
                {
                    int i2 = stream.getShort(lastpdata, 3);
                    int i3 = stream.getShort(lastpdata, 5);
                    currentGameTime = stream.getShort(lastpdata, 7);
                    currentGameType = stream.getShort(lastpdata, 9);
                    _fld0163 = stream.getShort(lastpdata, 11);
                    if(i2 == 1)
                        _fld0154 = playerNames[0] + "'s game - Currently 1 player";
                    else
                        _fld0154 = playerNames[0] + "'s game - Currently " + i2 + " players";
                    if(i3 > 0)
                        _fld0155 = "Game starts in: " + i3;
                    else
                        _fld0155 = "";
                    _fld0153 = 4;
                } else
                if(lastptype == 14 && !_fld0156)
                {
                    _fld0154 = "Game Aborted by owner";
                    _fld0155 = "";
                    _fld0153 = 3;
                } else
                if(lastptype == 15 && !_fld0156)
                {
                    _fld0154 = "Game is not available for joining";
                    _fld0155 = "";
                    _fld0153 = 3;
                } else
                if(lastptype == 16)
                {
                    castleId = stream.getShort(lastpdata, 1);
                    gameStatus = currentGame + 3;
                    for(int j2 = 0; j2 < 3; j2++)
                        chatMessages[j2] = "";

                } else
                if(lastptype == 25)
                {
                    createScreenNewAccount();
                    drawScreenNewAccount = true;
                }
                lastpsize = 0;
                return;
            }
        }
        catch(IOException _ex)
        {
            packetError = true;
        }
    }

    public void x()
    {
        f();
        l();
        if(_fld014F < 140)
        {
            _fld014F += 3;
            if(_fld014F > 140)
                _fld014F = 140;
        }
        if(_fld014F >= 200 && _fld0101)
        {
            _fld0160 += 2;
            if(_fld0160 >= 2450)
                _fld0160 = -330;
        }
        if(super.lastMouseButtonDown != 0 && super.mouseY > 351)
        {
            int i1 = (super.mouseX - 3) / 46;
            if(i1 < 0)
                i1 = 0;
            else
            if(i1 > 10)
                i1 = 10;
            gameStatus = 1;
            _fld0144 = -1;
            _fld0145 = -1;
            super.lastMouseButtonDown = 0;
            if(currentGame == i1)
                _fld0146 = -1;
            else
                _fld0146 = i1;
        }
        _fld0166++;
        if(super.mouseButtonDown == 1 && _fld0166 % 4 == 0)
        {
            if(super.mouseX > 18 && super.mouseY > 10 && super.mouseX < 138 && super.mouseY < 20)
                do
                {
                    _fld0104--;
                    if(_fld0104 < 0)
                        _fld0104 = 0;
                } while(_fld0104 != 0 && playerNames[_fld0104] == null);
            if(super.mouseX > 18 && super.mouseY > 280 && super.mouseX < 138 && super.mouseY < 290 && _fld0156)
                do
                {
                    _fld0104++;
                    if(_fld0104 > F8 - 7)
                        _fld0104 = F8 - 7;
                } while(_fld0104 != F8 - 7 && playerNames[_fld0104] == null);
            if(super.mouseX > 18 && super.mouseY > 128 && super.mouseX < 138 && super.mouseY < 138 && !_fld0156)
                do
                {
                    _fld0104++;
                    if(_fld0104 > F8 - 7)
                        _fld0104 = F8 - 7;
                } while(_fld0104 != F8 - 7 && playerNames[_fld0104] == null);
            if(super.mouseX > 18 && super.mouseY > 140 && super.mouseX < 138 && super.mouseY < 150)
                do
                {
                    _fld0105--;
                    if(_fld0105 < 0)
                        _fld0105 = 0;
                } while(_fld0105 != 0 && _fld015A[_fld0105] == null);
            if(super.mouseX > 18 && super.mouseY > 280 && super.mouseX < 138 && super.mouseY < 290)
                do
                {
                    _fld0105++;
                    if(_fld0105 > _fld0157 - 7)
                        _fld0105 = _fld0157 - 7;
                } while(_fld0105 != _fld0157 - 7 && _fld015A[_fld0105] == null);
        }
        if(_fld0105 > _fld0157 - 7)
            _fld0105 = _fld0157 - 7;
        if(_fld0104 > F8 - 7)
            _fld0104 = F8 - 7;
        if(_fld0105 < 0)
            _fld0105 = 0;
        if(_fld0104 < 0)
            _fld0104 = 0;
        _fld0158 = -1;
        _fld0152 = -1;
        if(super.mouseX > 20 && super.mouseX < 140 && super.mouseY > 47 && super.mouseY < 272)
            if(_fld0156)
            {
                int j1 = 57;
                int l1 = 0;
                for(int k2 = _fld0104; k2 < F8; k2++)
                {
                    if(playerNames[k2] == null)
                        continue;
                    if(super.mouseY >= j1 - 10 && super.mouseY <= j1 + 2)
                        _fld0152 = k2;
                    j1 += 12;
                    if(++l1 >= 19 || l1 >= 7 && !_fld0156)
                        break;
                }

                if(super.lastMouseButtonDown != 0 && _fld0152 != -1)
                {
                    stream.newPacket(6);
                    stream.putUnsignedShort(_fld0152);
                    stream.sendPacket();
                    super.lastMouseButtonDown = 0;
                }
            } else
            {
                int k1 = 187;
                int i2 = 0;
                for(int l2 = _fld0105; l2 <= _fld0157; l2++)
                {
                    if(_fld015A[l2] == null && l2 != _fld0157)
                        continue;
                    if(super.mouseY >= k1 - 10 && super.mouseY <= k1 + 2)
                        _fld0158 = l2;
                    k1 += 12;
                    if(++i2 >= 7)
                        break;
                }

                if(super.lastMouseButtonDown != 0 && _fld0158 != -1)
                    if(_fld0158 < _fld0157)
                    {
                        stream.newPacket(13);
                        stream.putUnsignedShort(_fld0158);
                        stream.sendPacket();
                    } else
                    {
                        stream.newPacket(10);
                        stream.sendPacket();
                    }
            }
        if(_fld0153 == 1 && super.lastMouseButtonDown != 0)
        {
            if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 125 && super.mouseY < 145)
            {
                stream.newPacket(7);
                stream.sendPacket();
            }
            if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
            {
                stream.newPacket(8);
                stream.sendPacket();
                _fld0153 = 0;
            }
            super.lastMouseButtonDown = 0;
        } else
        if(_fld0153 == 2 && super.lastMouseButtonDown != 0)
        {
            if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
            {
                stream.newPacket(8);
                stream.sendPacket();
                _fld0153 = 0;
            }
            super.lastMouseButtonDown = 0;
        } else
        if((_fld0153 == 3 || _fld0153 == 5) && super.lastMouseButtonDown != 0)
        {
            _fld0153 = 0;
            super.lastMouseButtonDown = 0;
        } else
        if(_fld0153 == 4 && super.lastMouseButtonDown != 0)
        {
            if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 125 && super.mouseY < 145)
            {
                stream.newPacket(11);
                stream.sendPacket();
                super.lastMouseButtonDown = 0;
            }
            if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
            {
                stream.newPacket(12);
                stream.sendPacket();
                _fld0153 = 0;
                super.lastMouseButtonDown = 0;
            }
        }
        if(super.lastMouseButtonDown != 0)
        {
            boolean flag = false;
            if(super.mouseY > 188 && super.mouseY <= 198)
            {
                if(super.mouseX >= 250 && super.mouseX < 293)
                {
                    _fld015D = 0;
                    flag = true;
                }
                if(super.mouseX >= 298 && super.mouseX < 336)
                {
                    _fld015D = 1;
                    flag = true;
                }
                if(super.mouseX >= 341 && super.mouseX < 379)
                {
                    _fld015D = 2;
                    flag = true;
                }
                if(super.mouseX >= 384 && super.mouseX < 422)
                {
                    _fld015D = 3;
                    flag = true;
                }
                if(super.mouseX >= 427 && super.mouseX < 477)
                {
                    _fld015D = 4;
                    flag = true;
                }
            }
            if(super.mouseY > 201 && super.mouseY <= 211)
            {
                if(super.mouseX >= 236 && super.mouseX < 266)
                {
                    _fld015E = true;
                    flag = true;
                }
                if(super.mouseX >= 270 && super.mouseX < 300)
                {
                    _fld015E = false;
                    flag = true;
                }
            }
            if(super.mouseY >= 246 && super.mouseY <= 255)
            {
                if(super.mouseX >= 149 && super.mouseX <= 158)
                {
                    _fld015F[0] = !_fld015F[0];
                    flag = true;
                }
                if(super.mouseX >= 224 && super.mouseX <= 233)
                {
                    _fld015F[2] = !_fld015F[2];
                    flag = true;
                }
                if(super.mouseX >= 319 && super.mouseX <= 328)
                {
                    _fld015F[4] = !_fld015F[4];
                    flag = true;
                }
                if(super.mouseX >= 415 && super.mouseX <= 424)
                {
                    _fld015F[6] = !_fld015F[6];
                    flag = true;
                }
            }
            if(super.mouseY >= 261 && super.mouseY <= 270)
            {
                if(super.mouseX >= 149 && super.mouseX <= 158)
                {
                    _fld015F[1] = !_fld015F[1];
                    flag = true;
                }
                if(super.mouseX >= 224 && super.mouseX <= 233)
                {
                    _fld015F[3] = !_fld015F[3];
                    flag = true;
                }
                if(super.mouseX >= 319 && super.mouseX <= 328)
                {
                    _fld015F[5] = !_fld015F[5];
                    flag = true;
                }
                if(super.mouseX >= 415 && super.mouseX <= 424)
                {
                    _fld015F[7] = !_fld015F[7];
                    flag = true;
                }
            }
            if(flag)
            {
                stream.newPacket(5);
                stream.putUnsignedShort(_fld015D);
                stream.putUnsignedShort(_fld015E ? 1 : 0);
                int j2 = 0;
                if(!_fld015F[0])
                    j2++;
                if(!_fld015F[1])
                    j2 += 2;
                if(!_fld015F[2])
                    j2 += 4;
                if(!_fld015F[3])
                    j2 += 8;
                if(!_fld015F[4])
                    j2 += 16;
                if(!_fld015F[5])
                    j2 += 32;
                if(!_fld015F[6])
                    j2 += 64;
                if(!_fld015F[7])
                    j2 += 128;
                stream.putUnsignedShort(j2);
                stream.sendPacket();
            }
        }
        super.lastMouseButtonDown = 0;
    }

    public String getRankTitle(int i1)
    {
        if(i1 <= 100)
            return "Beginner";
        if(i1 <= 120)
            return "Novice";
        if(i1 <= 140)
            return "Apprentice";
        if(i1 <= 170)
            return "Intermediate";
        if(i1 <= 200)
            return "Experienced";
        if(i1 <= 250)
            return "Expert";
        if(i1 <= 300)
            return "Master";
        else
            return "GrandMaster";
    }

    public boolean g(int i1)
    {
        if(points <= 100 && (i1 & 1) == 0)
            return true;
        if(points > 100 && points <= 120 && (i1 & 2) == 0)
            return true;
        if(points > 120 && points <= 140 && (i1 & 4) == 0)
            return true;
        if(points > 140 && points <= 170 && (i1 & 8) == 0)
            return true;
        if(points > 170 && points <= 200 && (i1 & 0x10) == 0)
            return true;
        if(points > 200 && points <= 250 && (i1 & 0x20) == 0)
            return true;
        if(points > 250 && points <= 300 && (i1 & 0x40) == 0)
            return true;
        return points > 300 && (i1 & 0x80) == 0;
    }

    public void drawChallengeScreen()
    {
        if(_fld014F < 200)
        {
            boolean flag = interlace;
            if(_fld014F == 140)
                flag = false;
            if(!flag)
            {
                surface2mby._mth01C7(0, 0, 500, 145, Color.black);
                surface2mby._mth01CA(0, 145, 500, 145, Color.black, paramGround, flag);
            } else
            {
                surface2mby._mth01CA(0, 0, 500, 145, paramGround, Color.black, flag);
                surface2mby._mth01CA(0, 145, 500, 145, Color.black, paramGround, flag);
            }
            scene._mth025A(flag);
            if(_fld0156)
            {
                surface2mby._mth01B3(12, 4, 120, 10, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 16, 120, 256, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 274, 120, 10, colorGrey, _fld014F, flag);
            } else
            {
                surface2mby._mth01B3(12, 4, 120, 10, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 16, 120, 104, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 122, 120, 10, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 134, 120, 10, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 146, 120, 126, colorGrey, _fld014F, flag);
                surface2mby._mth01B3(12, 274, 120, 10, colorGrey, _fld014F, flag);
            }
            surface2mby._mth01B3(138, 4, 352, 20, colorGrey, _fld014F, flag);
            surface2mby._mth01B3(138, 30, 352, 144, colorGrey, _fld014F, flag);
            surface2mby._mth01B3(138, 180, 352, 104, colorGrey, _fld014F, flag);
            if(_fld014F == 140)
                D0 = true;
            if(!D0)
            {
                surface2mby._mth01C2(appletGraphics, 6, 6, flag);
            } else
            {
                surface2mby._mth01C2(gameGraphics, 6, 6, flag);
                appletGraphics.drawImage(gameImage, 0, 0, this);
                D0 = false;
            }
            if(_fld014F == 140)
                _fld014F = 200;
        }
        if(_fld014F >= 200)
        {
            surface2mby._mth01C2(gameGraphics, 6, 6, false);
            gameGraphics.setColor(Color.black);
            gameGraphics.setFont(fontH13B);
            if(playerCount == 1)
                gameGraphics.drawString(gamesList[currentGame] + " - Currently " + playerCount + " player", 148, 24);
            else
                gameGraphics.drawString(gamesList[currentGame] + " - Currently " + playerCount + " players", 148, 24);
            if(_fld0153 == 0)
            {
                gameGraphics.drawLine(144, 52, 495, 52);
                gameGraphics.setFont(fontH16B);
                gameGraphics.drawString("Top Players", 146, 49);
                gameGraphics.setFont(fontH13B);
                gameGraphics.drawString("Name:", 148, 63);
                gameGraphics.drawString("Rating:", 263, 63);
                gameGraphics.drawString("Score:", 355, 63);
                gameGraphics.drawString("Played:", 403, 63);
                gameGraphics.drawString("Won:", 457, 63);
                gameGraphics.drawLine(144, 66, 495, 66);
                gameGraphics.setFont(fontH13);
                if(_fld0101)
                {
                    Graphics g1 = gameGraphics.create();
                    g1.clipRect(138, 70, 352, 105);
                    for(int k1 = 0; k1 < 50; k1++)
                    {
                        int k2 = (78 + k1 * 16) - _fld0160 / 3;
                        if(k2 >= 30 && k2 <= 190)
                        {
                            g1.drawString((k1 + 1) + ": " + FD[k1], 148, k2);
                            g1.drawString(getRankTitle(FE[k1]), 263, k2);
                            g1.drawString(String.valueOf(FE[k1]), 355, k2);
                            g1.drawString(String.valueOf(FF[k1]), 403, k2);
                            g1.drawString(String.valueOf(_fld0100[k1]), 457, k2);
                        }
                    }

                }
            } else
            if(_fld0153 == 1)
            {
                GameDialog.drawstringCenter(gameGraphics, _fld0154, fontH21, 320, 90);
                GameDialog.drawstringCenter(gameGraphics, gameTimes[currentGameTime] + " per move - " + gameTypes[currentGameType], fontH16B, 320, 110);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 125 && super.mouseY < 145)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                GameDialog.drawstringCenter(gameGraphics, "Accept Challenge", fontH16B, 320, 140);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                GameDialog.drawstringCenter(gameGraphics, "Decline Challenge", fontH16B, 320, 160);
            } else
            if(_fld0153 == 2)
            {
                GameDialog.drawstringCenter(gameGraphics, _fld0154, fontH21, 320, 80);
                GameDialog.drawstringCenter(gameGraphics, gameTimes[currentGameTime] + " per move - " + gameTypes[currentGameType], fontH16B, 320, 100);
                GameDialog.drawstringCenter(gameGraphics, "Waiting for a response", fontH16B, 320, 120);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                GameDialog.drawstringCenter(gameGraphics, "Abort Challenge", fontH16B, 320, 160);
            } else
            if(_fld0153 == 3)
            {
                GameDialog.drawstringCenter(gameGraphics, _fld0154, fontH21, 320, 90);
                if(_fld0155.length() > 55)
                    GameDialog.drawstringCenter(gameGraphics, _fld0155, fontH10B, 320, 120);
                else
                if(_fld0155.length() > 40)
                    GameDialog.drawstringCenter(gameGraphics, _fld0155, fontH13, 320, 120);
                else
                    GameDialog.drawstringCenter(gameGraphics, _fld0155, fontH16B, 320, 120);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                GameDialog.drawstringCenter(gameGraphics, "Click mouse to continue", fontH16B, 320, 160);
            } else
            if(_fld0153 == 4)
            {
                GameDialog.drawstringCenter(gameGraphics, _fld0154, fontH21, 320, 70);
                GameDialog.drawstringCenter(gameGraphics, _fld0155, fontH16B, 320, 90);
                GameDialog.drawstringCenter(gameGraphics, gameTimes[currentGameTime] + " per move - " + gameTypes[currentGameType], fontH13B, 320, 105);
                if(_fld0163 == 0)
                    GameDialog.drawstringCenter(gameGraphics, "All player-levels allowed", fontH13B, 320, 120);
                else
                    GameDialog.drawstringCenter(gameGraphics, "Restricted player-levels allowed", fontH13B, 320, 120);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 125 && super.mouseY < 145)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                if(castleId == 0)
                    GameDialog.drawstringCenter(gameGraphics, "Start game", fontH16B, 320, 140);
                if(super.mouseX > 214 && super.mouseX < 414 && super.mouseY > 145 && super.mouseY < 165)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                if(castleId == 0)
                    GameDialog.drawstringCenter(gameGraphics, "Abort game", fontH16B, 320, 160);
                else
                    GameDialog.drawstringCenter(gameGraphics, "Leave game", fontH16B, 320, 160);
            } else
            if(_fld0153 == 5)
            {
                String as[] = new String[50];
                int l1 = 0;
                int l2 = 0;
                String s1 = "";
                for(; l2 < _fld0154.length(); l2++)
                {
                    char c1 = _fld0154.charAt(l2);
                    if(c1 == '*')
                    {
                        as[l1++] = s1;
                        s1 = "";
                    } else
                    {
                        s1 = s1 + c1;
                    }
                }

                as[l1++] = s1;
                Font font = fontH13;
                byte byte0 = 6;
                if(l1 > 9)
                {
                    font = fontH10B;
                    byte0 = 4;
                }
                gameGraphics.setFont(fontH13);
                gameGraphics.setColor(Color.black);
                for(int i4 = 0; i4 < l1; i4++)
                    GameDialog.drawstringCenter(gameGraphics, as[i4], font, 320, (110 - l1 * byte0) + i4 * byte0 * 2);

                GameDialog.drawstringCenter(gameGraphics, "You won! Score breakdown is:", fontH16B, 320, 45);
                GameDialog.drawstringCenter(gameGraphics, "Click mouse to continue", fontH13, 320, 170);
            }
            gameGraphics.setFont(fontH13B);
            gameGraphics.setColor(Color.black);
            gameGraphics.drawString("Time per move:", 147, 198);
            if(_fld015D == 0)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("30-sec", 251, 198);
            if(_fld015D == 1)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("1-min", 298, 198);
            if(_fld015D == 2)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("2-min", 341, 198);
            if(_fld015D == 3)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("4-min", 384, 198);
            if(_fld015D == 4)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("10-min", 427, 198);
            gameGraphics.setColor(Color.black);
            gameGraphics.drawString("Rated game:", 147, 211);
            if(_fld015E)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("Yes", 236, 211);
            if(!_fld015E)
                gameGraphics.setColor(colorLightRed);
            else
                gameGraphics.setColor(Color.black);
            gameGraphics.drawString("No", 270, 211);
            gameGraphics.setColor(Color.black);
            gameGraphics.drawLine(144, 230, 495, 230);
            gameGraphics.drawString("Will play against following players:", 147, 242);
            gameGraphics.setFont(fontH13);
            gameGraphics.drawString("- Beginner", 161, 255);
            Z(gameGraphics, 149, 246, 9, 9, _fld015F[0]);
            gameGraphics.drawString("- Novice", 161, 270);
            Z(gameGraphics, 149, 261, 9, 9, _fld015F[1]);
            gameGraphics.drawString("- Apprentice", 236, 255);
            Z(gameGraphics, 224, 246, 9, 9, _fld015F[2]);
            gameGraphics.drawString("- Intermediate", 236, 270);
            Z(gameGraphics, 224, 261, 9, 9, _fld015F[3]);
            gameGraphics.drawString("- Experienced", 331, 255);
            Z(gameGraphics, 319, 246, 9, 9, _fld015F[4]);
            gameGraphics.drawString("- Expert", 331, 270);
            Z(gameGraphics, 319, 261, 9, 9, _fld015F[5]);
            gameGraphics.drawString("- Master", 427, 255);
            Z(gameGraphics, 415, 246, 9, 9, _fld015F[6]);
            gameGraphics.drawString("- GM", 427, 270);
            Z(gameGraphics, 415, 261, 9, 9, _fld015F[7]);
            gameGraphics.drawLine(144, 274, 495, 274);
            gameGraphics.setFont(fontH13B);
            gameGraphics.drawString("Your rating is: " + points + " (" + getRankTitle(points) + ")", 149, 286);
            gameGraphics.setFont(fontH10B);
            gameGraphics.setColor(colorLightRed);
            if(_fld0153 != 4)
                gameGraphics.drawString("Waiting players", 20, 31);
            else
                gameGraphics.drawString("Players in game", 20, 31);
            if(_fld0156)
            {
                gameGraphics.drawString("Click name to challenge", 20, 44);
                gameGraphics.setColor(Color.black);
                gameGraphics.drawLine(18, 46, 137, 46);
            } else
            {
                gameGraphics.setColor(Color.black);
                gameGraphics.drawLine(18, 33, 137, 33);
            }
            int i1 = 0;
            int i2 = 0;
            int i3 = 57;
            if(!_fld0156)
                i3 -= 13;
            for(int j3 = _fld0104; j3 < F8; j3++)
            {
                if(playerNames[j3] == null)
                    continue;
                if(g(playerPoints[j3]))
                    gameGraphics.setColor(Color.black);
                else
                    gameGraphics.setColor(colorLighterRed);
                if(_fld0152 == j3 && _fld0156)
                    gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString(playerNames[j3] + " (" + playerUnknown[j3] + ")", 20, i3 + i1);
                i1 += 12;
                if(++i2 >= 19 || i2 >= 7 && !_fld0156)
                    break;
            }

            gameGraphics.setColor(Color.black);
            if(super.mouseX > 18 && super.mouseY > 10 && super.mouseX < 138 && super.mouseY < 20)
                gameGraphics.setColor(colorLightRed);
            gameGraphics.drawString("Up", 71, 19);
            gameGraphics.setColor(Color.black);
            if(_fld0156)
            {
                if(super.mouseX > 18 && super.mouseY > 280 && super.mouseX < 138 && super.mouseY < 290)
                    gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString("Down", 65, 288);
            } else
            {
                if(super.mouseX > 18 && super.mouseY > 128 && super.mouseX < 138 && super.mouseY < 138)
                    gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString("Down", 65, 136);
            }
            if(!_fld0156 && _fld0153 != 4)
            {
                gameGraphics.setFont(fontH10B);
                gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString("Available games", 20, 161);
                gameGraphics.drawString("Click game to join", 20, 174);
                gameGraphics.setColor(Color.black);
                gameGraphics.drawLine(18, 176, 137, 176);
                int j1 = 0;
                int j2 = 0;
                for(int k3 = _fld0105; k3 < _fld0157; k3++)
                {
                    if(_fld015A[k3] == null)
                        continue;
                    if(g(_fld015C[k3]))
                        gameGraphics.setColor(Color.black);
                    else
                        gameGraphics.setColor(colorLighterRed);
                    if(_fld0158 == k3)
                        gameGraphics.setColor(colorLightRed);
                    gameGraphics.drawString(_fld015A[k3] + "'s game (" + _fld015B[k3] + "/" + games[currentGame]._fld03C3 + ")", 20, j1 + 57 + 130);
                    j1 += 12;
                    if(++j2 >= 7)
                        break;
                }

                if(_fld0158 == _fld0157)
                    gameGraphics.setColor(colorLightRed);
                else
                    gameGraphics.setColor(Color.black);
                gameGraphics.drawString("Create new game", 20, j1 + 57 + 130);
                gameGraphics.setColor(Color.black);
                if(super.mouseX > 18 && super.mouseY > 140 && super.mouseX < 138 && super.mouseY < 150)
                    gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString("Up", 71, 149);
                gameGraphics.setColor(Color.black);
                if(super.mouseX > 18 && super.mouseY > 280 && super.mouseX < 138 && super.mouseY < 290)
                    gameGraphics.setColor(colorLightRed);
                gameGraphics.drawString("Down", 65, 288);
            }
            gameGraphics.drawImage(gamePanelGamesImage, 0, 298, this);
            gameGraphics.drawImage(gamePanelQuitImage, currentGame * 46 + 3, 352, this);
            gameGraphics.setColor(Color.white);
            gameGraphics.setFont(fontH13);
            if(super.inputPmCurrent != null)
                gameGraphics.drawString(super.inputPmCurrent, 8, 349);
            for(int l3 = 0; l3 < 3; l3++)
                if(chatMessages[l3] != null)
                    gameGraphics.drawString(chatMessages[l3], 8, 336 - l3 * 12);

            appletGraphics.drawImage(gameImage, 0, 0, this);
        }
    }

    public void Z(Graphics g1, int i1, int j1, int k1, int l1, boolean flag)
    {
        g1.drawRect(i1, j1, k1, l1);
        if(flag)
        {
            g1.drawLine(i1, j1, i1 + k1, j1 + l1);
            g1.drawLine(i1 + k1, j1, i1, j1 + l1);
        }
    }

    public castle()
    {
        happyPuppy = false;
        D0 = true;
        fontH40 = new Font("Helvetica", 0, 40);
        fontH23 = new Font("Helvetica", 0, 23);
        fontH21 = new Font("Helvetica", 0, 21);
        fontH16B = new Font("Helvetica", 1, 16);
        fronH13B = new Font("Helvetica", 0, 16);
        fontH13B = new Font("Helvetica", 1, 13);
        fontH13 = new Font("Helvetica", 0, 13);
        fontH10B = new Font("Helvetica", 1, 10);
        fontH30B = new Font("Helvetica", 1, 30);
        fontH18B = new Font("Helvetica", 1, 18);
        fontH15 = new Font("Helvetica", 0, 15);
        E2 = 0xbc614e;
        E3 = false;
        interlace = false;
        E5 = false;
        gameStatus = -1;
        E7 = -2;
        games = new Game[11];
        EB = false;
        EC = new int[11];
        lastpdata = new byte[5000];
        F2 = 75;
        packetError = false;
        server = "127.0.0.1";
        chatMessages = new String[3];
        maxPlayerCount = 500;
        playerNames = new String[maxPlayerCount];
        playerUnknown = new int[maxPlayerCount];
        playerPoints = new int[maxPlayerCount];
        FD = new String[50];
        FE = new int[50];
        FF = new int[50];
        _fld0100 = new int[50];
        _fld0101 = false;
        _fld0106 = true;
        gameName = "Castle GamesDomain";
        paramTextures = "menu/textures1.gif";
        paramTitleModel = "menu/castle.ob2";
        paramMap = "menu/castle.dat";
        paramQuit = "menu/quit1.gif";
        paramLogo = "menu/gd.gif";
        paramAmbient = 75;
        paramContrast = 24;
        paramFog = 4;
        paramGround = new Color(136, 136, 136);
        drawScreenNewAccount = false;
        loginUser = "";
        loginPass = "";
        _fld013E = -80;
        sceneCleared = false;
        _fld0140 = new GameModel[20];
        _fld0144 = -1;
        _fld0145 = -1;
        _fld0146 = -1;
        _fld014B = "";
        colorLightRed = new Color(160, 0, 0);
        colorGrey = new Color(192, 192, 192);
        colorLighterRed = new Color(128, 0, 0);
        _fld0152 = -1;
        _fld0154 = "";
        _fld0155 = "";
        _fld0156 = true;
        _fld0158 = -1;
        _fld0159 = 500;
        _fld015A = new String[_fld0159];
        _fld015B = new int[_fld0159];
        _fld015C = new int[_fld0159];
        _fld015D = 1;
        _fld015E = true;
        _fld0160 = -330;
        currentGameTime = 1;
        currentGameType = 1;
    }

    boolean happyPuppy;
    Graphics gameGraphics;
    Image gameImage;
    boolean D0;
    Image gamePanelGamesImage;
    Image gamePanelQuitImage;
    Font fontH40;
    Font fontH23;
    Font fontH21;
    Font fontH16B;
    Font fronH13B;
    Font fontH13B;
    Font fontH13;
    Font fontH10B;
    Font fontH30B;
    Font fontH18B;
    Font fontH15;
    Graphics appletGraphics;
    Scene scene;
    Surface2mby surface2mby;
    int E2;
    boolean E3;
    boolean interlace;
    boolean E5;
    int gameStatus;
    int E7;
    int E8;
    static boolean appletMode = true;
    Game games[];
    boolean EB;
    int EC[];
    Stream stream;
    byte lastpdata[];
    int lastpsize;
    int lastptype;
    int F1;
    int F2;
    boolean packetError;
    String server;
    String chatMessages[];
    int maxPlayerCount;
    int F8;
    int playerCount;
    String playerNames[];
    int playerUnknown[];
    int playerPoints[];
    String FD[];
    int FE[];
    int FF[];
    int _fld0100[];
    boolean _fld0101;
    public static int points;
    public static int castleId;
    int _fld0104;
    int _fld0105;
    boolean _fld0106;
    int _fld0107;
    String gameName;
    String paramTextures;
    String paramTitleModel;
    String paramMap;
    String paramQuit;
    String paramLogo;
    int paramAmbient;
    int paramContrast;
    int paramFog;
    int paramLogid;
    Color paramGround;
    v _fld0113;
    int _fld0114;
    Panel panelNewAccount;
    int controlNewAccStatus1;
    int controlNewAccStatus2;
    int controlNewAccFirstName;
    int controlNewAccSurname;
    int controlNewAccAge;
    int controlNewAccSex;
    int controlNewAccCountry;
    int controlNewAccEmail;
    int controlNewAccSubmit;
    int _fld011F;
    boolean drawScreenNewAccount;
    Surface surface;
    GameModel modelTitleScreen;
    int loginCameraRotation;
    String loginUser;
    String loginPass;
    int loginScreen;
    String loginUserDesc;
    String loginUserDisp;
    String _fld0129;
    String _fld012A;
    GameModel _fld012B;
    GameModel _fld012C;
    GameModel modelTable;
    GameModel modelTrapdoor;
    int _fld012F;
    int _fld0130;
    int _fld0131[][];
    int _fld0132[][];
    int _fld0133[][];
    int _fld0134[][];
    int _fld0135[][];
    int _fld0136;
    int _fld0137;
    int _fld0138;
    int _fld0139[][];
    int _fld013A;
    int _fld013B[][];
    int _fld013C;
    int _fld013D;
    int _fld013E;
    boolean sceneCleared;
    GameModel _fld0140[];
    int _fld0141;
    int _fld0142[][];
    int _fld0143[][];
    int _fld0144;
    int _fld0145;
    int _fld0146;
    int _fld0147[][];
    int _fld0148[];
    int _fld0149[];
    int _fld014A;
    String _fld014B;
    Color colorLightRed;
    Color colorGrey;
    Color colorLighterRed;
    int _fld014F;
    String gamesList[] = {
        "Checkers", "Chess", "Battle-Cruisers", "Quad-Link", "Reversi", "Mystery Monster", "Mahjong", "Pairs", "Treasure Crypt", "Castle Dungeon", 
        "Cyber wars"
    };
    int currentGame;
    int _fld0152;
    int _fld0153;
    String _fld0154;
    String _fld0155;
    boolean _fld0156;
    int _fld0157;
    int _fld0158;
    int _fld0159;
    String _fld015A[];
    int _fld015B[];
    int _fld015C[];
    int _fld015D;
    boolean _fld015E;
    boolean _fld015F[] = {
        true, true, true, true, true, true, true, true
    };
    int _fld0160;
    int currentGameTime;
    int currentGameType;
    int _fld0163;
    String gameTimes[] = {
        "30-secs", "1-min", "2-mins", "4-mins", "10-mins"
    };
    String gameTypes[] = {
        "Non rated game", "Rated Game"
    };
    int _fld0166;

}

