package com.ximba.jarvis;

// Java
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

// SWT
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// JFreeChart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.experimental.chart.swt.ChartComposite;

// Jarvis
import com.ximba.common.*;

/**
 * <p> 
 * Front end to UI thread.
 * </p>
 * @author Michael J. Hammel
 * @since 1.0
 */

public class UI implements Runnable {

    /* lo4j logger */
    private static final Logger log = Logger.getLogger("com.ximba.jarvis.UI");

    // Inbound messages, one for each display area.
    private static final ConcurrentLinkedQueue<Message> audioMsgs = new ConcurrentLinkedQueue<Message>();
    private static final ConcurrentLinkedQueue<Message> apiMsgs   = new ConcurrentLinkedQueue<Message>();
    private static final ConcurrentLinkedQueue<Message> nlpMsgs   = new ConcurrentLinkedQueue<Message>();
    private static final ConcurrentLinkedQueue<Message> iotMsgs   = new ConcurrentLinkedQueue<Message>();

    // Stats
    private static final ConcurrentLinkedQueue<Timing> timings = new ConcurrentLinkedQueue<Timing>();

    /*
     * --------------------------------------------
     * Thread Managment
     * --------------------------------------------
     */

    /* This thread. */
    private Thread thread = null;
    private Thread audioThread = null;
    private Thread apiThread = null;
    private Thread nlpThread = null;
    private Thread iotThread = null;

    /* When true, the thread is running. */
    private boolean isRunning = false;
    private boolean audioIsRunning = false;
    private boolean apiIsRunning = false;
    private boolean nlpIsRunning = false;
    private boolean iotIsRunning = false;

    /* When true, the thread has completed and is ready to change state to "not running". */
    private boolean runComplete = false;

    /* When shutting down, disable exception hanlding in run() method. */
    private boolean doShutdown = false;

    /*
     * --------------------------------------------
     * Fonts and Colors
     * --------------------------------------------
     */
    Font  boldFont   = null;
    Font  mediumFont = null;
    Color red        = null;

    /*
     * --------------------------------------------
     * Numerics
     * --------------------------------------------
     */
    private static final int N_SP  = 1;
    private static final int N_TT  = 2;
    private static final int N_JS  = 3;
    private static final int N_NLP = 4;

    /*
     * --------------------------------------------
     * Strings
     * --------------------------------------------
     */
    private static final String S_TIMINGS = "Message Timings";
    private static final String[] S_TIMING_SERIES = {"Speech","ToText","JSON","NLP","Total"};

    /*
     * --------------------------------------------
     * Datasets
     * --------------------------------------------
     */
    private XYSeriesCollection timingDataset = new XYSeriesCollection();
    private XYSeries[] timingSeries = new XYSeries[5];
    private long timingSeriesIdx = 1;

    /*
     * --------------------------------------------
     * UI Components
     * --------------------------------------------
     */
    Display display = null;
    Shell shell = null;
    ChartComposite timingChartComposite = null;
    JFreeChart timingChart = null;

    // Audio frame
    Label reccntLabel = null;
    Label whispersLabel = null;
    Canvas audioGraph = null;
    Canvas audioLevelGraph = null;

    /* Menus indices */
    private static final int M_FILE_QUIT            = 0;

    private static final int M_EDIT_AUDIO           = 0;
    private static final int M_EDIT_NLP             = 1;
    private static final int M_EDIT_CMDS            = 2;
    private static final int M_EDIT_IoT             = 3;

    private static final int M_HELP_ABOUT           = 0;

    /*
     * Menus in Menu Bar
     */
    private static String[] FileMenuItems = {
        "&Quit"
    };
    private static String[] EditMenuItems = {
        "&Audio Config",
        "&NLP Config",
        "&Commands",
        "&IoT Devices"
    };
    private static String[] HelpMenuItems = {
        "&About",
    };

    /*
     * --------------------------------------------
     * Audio stats
     * --------------------------------------------
     */
    float audiolevel= 0;       // Audio level, 0 to 1.
    long reccnt     = 0;       // Number of recordings made, even if its just noise
    long whispers   = 0;       // Number of recordings that were not noise but we couldn't identify as speech.

    /*
     * =======================================================
     * Constructor
     * =======================================================
     */

    void UI() { }

    /*
     * =======================================================
     * Inner classes
     * =======================================================
     */

    /* Timings for a single message. */
    class Timing 
    {
        private long SPIN   = 0;
        private long SPOUT  = 0;
        private long TTIN   = 0;
        private long TTOUT  = 0;
        private long JSIN   = 0;
        private long JSOUT  = 0;
        private long NLPIN  = 0;
        private long NLPOUT = 0;
        private long total  = 0;

        public Timing( Message msg )
        {
            String str; 
            str = msg.getDataItem( Message.S_TS_SPIN );
            try { SPIN = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_SPOUT );
            try { SPOUT = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_TTIN );
            try { TTIN = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_TTOUT );
            try { TTOUT = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_JSIN );
            try { JSIN = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_JSOUT );
            try { JSOUT = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_NLPIN );
            try { NLPIN = Long.parseLong( str ); }
            catch(Exception e){};

            str = msg.getDataItem( Message.S_TS_NLPOUT );
            try { NLPOUT = Long.parseLong( str ); }
            catch(Exception e){};

            total = (SPOUT-SPIN) + (TTOUT-TTIN) + (JSOUT-JSIN) + (NLPOUT-NLPIN);
        }

        public long getTime(int idx) { 
            switch(idx)
            {
                case N_SP:  return (SPOUT-SPIN);
                case N_TT:  return (TTOUT-TTIN);
                case N_JS:  return (JSOUT-JSIN);
                case N_NLP: return (NLPOUT-NLPIN);
                default: return 0;
            }
        }
        public long getTotal() { return total; }
        public String toString() { 
            StringBuilder str = new StringBuilder("Timings: \n");
            str.append("\tSpeech: " + (SPOUT-SPIN) + "ms \n" );
            str.append("\tToText: " + (TTOUT-TTIN) + "ms \n" );
            str.append("\tJSON  : " + (JSOUT-JSIN) + "ms \n" );
            str.append("\tNLP   : " + (NLPOUT-NLPIN) + "ms \n" );
            str.append("\tTotal : " + total + "ms \n" );
            return str.toString(); 
        }
    }

    /* Process Audio messages. */
    class AudioHandler implements Runnable
    {
        public void run(){
            audioIsRunning = true;
            while ( audioIsRunning )
            {
                Message msg = audioMsgs.poll();
                if ( msg == null )
                    continue;

                // Handle Raw data
                final byte[] rawData = msg.getByteDataItem(Message.S_RAW);
                if ( rawData != null )
                {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            drawSignal(rawData);
                        }
                    });
                }

                // Update audio level
                String cntStr = msg.getDataItem(Message.S_AUDIOLEVEL);
                if ( cntStr != null )
                {
                    try { audiolevel = Float.parseFloat(cntStr); }
                    catch(Exception e){ log.error("Malformed AUDIOLEVEL: " + cntStr); }
                }

                // Update recordings count
                cntStr = msg.getDataItem(Message.S_RECCNT);
                if ( cntStr != null )
                {
                    try { reccnt += Long.parseLong(cntStr); }
                    catch(Exception e){ log.error("Malformed RECCNT: " + cntStr); }
                }

                // Update whispers count
                cntStr = msg.getDataItem(Message.S_WHISPERS);
                if ( cntStr != null )
                {
                    try { whispers += Long.parseLong(cntStr); }
                    catch(Exception e){ log.error("Malformed WHISPERS: " + cntStr); }
                }

                // Update the display.
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        updateAudio();
                    }
                });
            }
        }
    }

    /* Process API messages. */
    class APIHandler implements Runnable
    {
        public void run(){
            apiIsRunning = true;
            while ( apiIsRunning )
            {
                Message msg = apiMsgs.poll();
                if ( msg == null )
                    continue;
            }
        }
    }

    /* Process NLP messages. */
    class NLPHandler implements Runnable
    {
        public void run(){
            nlpIsRunning = true;
            while ( nlpIsRunning )
            {
                Message msg = nlpMsgs.poll();
                if ( msg == null )
                    continue;

                // Process timings.
                final Timing timing = new Timing(msg);
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        timingSeries[0].add(timingSeriesIdx, timing.getTime(N_SP)/1000);
                        timingSeries[1].add(timingSeriesIdx, timing.getTime(N_TT)/1000);
                        timingSeries[2].add(timingSeriesIdx, timing.getTime(N_JS)/1000);
                        timingSeries[3].add(timingSeriesIdx, timing.getTime(N_NLP)/1000);
                        timingSeries[4].add(timingSeriesIdx, timing.getTotal()/1000);
                        timingSeriesIdx++;
                        timingChartComposite.forceRedraw( );
                    }
                });
                log.info( timing.toString() );
            }
        }
    }

    /* Process IOT messages. */
    class IOTHandler implements Runnable
    {
        public void run(){
            iotIsRunning = true;
            while ( iotIsRunning )
            {
                Message msg = iotMsgs.poll();
                if ( msg == null )
                    continue;
            }
        }
    }

    /*
     * =======================================================
     * Private UI methods
     * =======================================================
     */

    /*
     * Update Confidence level from spinner
     */
    private void confidenceHandler(Spinner spinner) 
    {
        double val = (spinner.getSelection()/100);
        try {
            GAPI gapi = new GAPI();
            gapi.setMinConfidence( val );
        }
        catch(Exception e){}
    }

    /*
     * Update Minimum audio level from spinner
     */
    private void audioLevelHandler(Spinner spinner) 
    {
        log.info( String.format("spinner = %s", spinner.getText()) );
        try {
            float val = Float.parseFloat( spinner.getText() );
            log.info( String.format("minLevel = %f", val) );
            Speech speech = new Speech();
            speech.setMinAudioLevel( val );
        }
        catch(Exception e){}
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                updateAudio();
            }
        });
    }

    /*
     * File menu handler
     */
    private void fileMenuHandler( int idx ) 
    {
        switch(idx)
        {
            case M_FILE_QUIT:   
                Cli cli = new Cli();
                cli.disable();
                break;
        }
    }

    /* Update the audio display region. */
    private void updateAudio() 
    {
        reccntLabel.setText( "" + reccnt );
        whispersLabel.setText( "" + whispers );

        // Clear audio level background
        GC gc = new GC(audioLevelGraph);
        gc.setBackground( display.getSystemColor(SWT.COLOR_WHITE) );
        audioLevelGraph.drawBackground(gc, 0, 0, audioLevelGraph.getSize().x, audioLevelGraph.getSize().y);

        // Draw new level
        gc.setBackground( display.getSystemColor(SWT.COLOR_RED) );
        audioLevelGraph.drawBackground(
                        gc, 
                        0, 
                        (int)(audioLevelGraph.getSize().y*(1-audiolevel)), 
                        audioLevelGraph.getSize().x, 
                        audioLevelGraph.getSize().y);

        // Draw the minimum level for speech
        Speech speech = new Speech();
        gc.setBackground( display.getSystemColor(SWT.COLOR_DARK_CYAN) );
        audioLevelGraph.drawBackground(
                        gc, 
                        0,
                        (int)(audioLevelGraph.getSize().y*(1-speech.getMinAudioLevel())), 
                        audioLevelGraph.getSize().x, 
                        1);
    }

    /* Draw a graph based on the raw audio data. */
    private void drawSignal( byte[] rawData ) 
    {
        // Clear audio wave background
        GC gc = new GC(audioGraph);
        gc.setBackground( display.getSystemColor(SWT.COLOR_BLACK) );
        audioLevelGraph.drawBackground(gc, 0, 0, audioGraph.getSize().x, audioGraph.getSize().y);

        if ( rawData == null )
            return;

        // Convert byte data to integers, which is what the were before packing in the inbound message.
        IntBuffer intBuffer = ByteBuffer.wrap(rawData).asIntBuffer();
        int[] data = new int[intBuffer.remaining()];
        intBuffer.get( data );

        int m_nWidth = audioGraph.getSize().x;
        float[] m_anDisplayData = new float[m_nWidth];

        int nSamplesPerPixel = data.length / m_nWidth;
        for (int i = 0; i < m_nWidth; i++)
        {
            float nValue = 0.0f;
            for (int j = 0; j < nSamplesPerPixel; j++)
            {
                nValue += (float) (Math.abs(data[i * nSamplesPerPixel + j]) / 65536.0f);
            }
            nValue /= nSamplesPerPixel;
            m_anDisplayData[i] = nValue;
        }

        gc.setForeground( display.getSystemColor(SWT.COLOR_YELLOW) );
        int nHeight = audioGraph.getSize().y;
        for (int i = 0; i < m_nWidth; i++)
        {
            int value = (int) (m_anDisplayData[i] * nHeight);
            int y1 = (nHeight - 2 * value) / 2;
            int y2 = y1 + 2 * value;
            gc.drawLine(i, y1, i, y2);
        }
    }

    /* The Audio Frame */
    private void buildAudioFrame() 
    {
        GridData gd = new GridData(GridData.FILL_BOTH);
        Label label = null;
        Group group = new Group(shell, SWT.NONE);
        group.setText("Audio Analysis");
        group.setFont(boldFont);
        group.setLayoutData(gd);

        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 2;
        gl.numColumns = 3;
        gl.makeColumnsEqualWidth = false;
        group.setLayout(gl);

        // Canvas for Audio level
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint  = 15;
        gd.verticalSpan = 5;
        audioLevelGraph = new Canvas(group, SWT.BORDER);
        audioLevelGraph.setLayoutData(gd);
        audioLevelGraph.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent e){
                updateAudio();
            }
        }); 

        // Canvas for audio graph
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        label = new Label(group, SWT.NONE);
        label.setText("Voice Signal");
        label.setLayoutData(gd);
        label.setFont(mediumFont);

        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint  = 150;
        gd.heightHint = 150;
        gd.horizontalSpan = 2;
        audioGraph = new Canvas(group, SWT.NONE);
        audioGraph.setLayoutData(gd);
        audioGraph.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent e){
                drawSignal(null);
            }
        }); 

        // reccnt field
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label = new Label(group, SWT.RIGHT);
        label.setText("Recordings");
        label.setLayoutData(gd);
        reccntLabel = new Label(group, SWT.LEFT);
        reccntLabel.setText("0");
        reccntLabel.setLayoutData(gd);

        // whispers field
        label = new Label(group, SWT.RIGHT);
        label.setText("Whispers");
        label.setLayoutData(gd);
        whispersLabel = new Label(group, SWT.LEFT);
        whispersLabel.setText("0");
        whispersLabel.setLayoutData(gd);

        // Audio Level
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.CENTER;
        label = new Label(group, SWT.RIGHT);
        label.setText("Audio Level");
        label.setLayoutData(gd);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 14;
        final Spinner spinner = new Spinner(group, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(100);
        spinner.setIncrement(1);
        spinner.setPageIncrement(10);
        spinner.setDigits(2);
        spinner.setLayoutData(gd);
        spinner.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
                audioLevelHandler(spinner);
            }
        });

        Speech speech = new Speech();
        spinner.setSelection((int)(speech.getMinAudioLevel()*100.0f));
    }

    /* The API Frame */
    private void buildAPIFrame() 
    {
        GridData gd = new GridData(GridData.FILL_BOTH);
        Label label = null;
        Group group = new Group(shell, SWT.NONE);
        group.setText("Google API Performance");
        group.setFont(boldFont);
        group.setLayoutData(gd);

        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 2;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = true;
        group.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gd.grabExcessVerticalSpace = true; 
        label = new Label(group, SWT.RIGHT);
        label.setText("Confidence Level");
        label.setLayoutData(gd);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 14;
        final Spinner spinner = new Spinner(group, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(100);
        spinner.setIncrement(1);
        spinner.setPageIncrement(10);
        spinner.setDigits(2);
        spinner.setLayoutData(gd);
        spinner.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
                confidenceHandler(spinner);
            }
        });

        try { 
            GAPI gapi = new GAPI();
            spinner.setSelection((int)(gapi.getMinConfidence()*100));
        }
        catch(Exception e){}

        // A Time Series charting 
        timingChartComposite = new ChartComposite(group, SWT.BORDER, null, true);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 300;
        gd.heightHint = 300;
        gd.horizontalSpan = 2;
        timingChartComposite.setLayoutData(gd);
        timingChartComposite.pack();

        /* Create a line chart to plot the new data */
        timingChart = ChartFactory.createXYLineChart(
            S_TIMINGS,                // chart title
            "Message",                // domain axis label
            "Time in Sec",            // range axis label
            timingDataset,            // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            false,                    // tooltips?
            false                     // URLs?
        );
        timingChartComposite.setChart( timingChart );

        // Create 5 timing series
        for(int i=0; i<5; i++)
        {
            timingSeries[i] = new XYSeries(S_TIMING_SERIES[i]);
            timingDataset.addSeries(timingSeries[i]);
        }
    }

    /* The NLP Frame */
    private void buildNLPFrame() 
    {
        GridData gd = new GridData(GridData.FILL_BOTH);
        Label label = null;
        Group group = new Group(shell, SWT.NONE);
        group.setText("NL Processing");
        group.setFont(boldFont);
        group.setLayoutData(gd);

        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = true;
        group.setLayout(gl);
    }

    /* The IOT Frame */
    private void buildIOTFrame() 
    {
        GridData gd = new GridData(GridData.FILL_BOTH);
        Label label = null;
        Group group = new Group(shell, SWT.NONE);
        group.setText("IoT Devices");
        group.setFont(boldFont);
        group.setLayoutData(gd);

        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = true;
        group.setLayout(gl);
    }

    /*
     * Initialize the display components
     */
    private void buildUI() 
    {
        display = new Display();
        shell = new Shell(display);

        boldFont = new Font(Display.getDefault(), "Tahoma", 10, SWT.BOLD);
        mediumFont = new Font(Display.getDefault(), "Tahoma", 9, SWT.ITALIC);
        red = new Color(Display.getDefault(), 250, 150, 150);

        // Add a menu bar across the top
        Menu menuBar = new Menu (shell, SWT.BAR);
        shell.setMenuBar ( menuBar );

        /* Add File Menu */
        MenuItem menuBar_File= new MenuItem (menuBar, SWT.CASCADE);
        menuBar_File.setText ("&File");
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        menuBar_File.setMenu( fileMenu );
        for (int i=0; i<FileMenuItems.length; i++)
        {
            final int idx = i;
            MenuItem item = new MenuItem (fileMenu, SWT.CASCADE);
            item.setText( FileMenuItems[i] );
            item.addListener (SWT.Selection, new Listener () {
                public void handleEvent (Event e) {
                    fileMenuHandler(idx);
                }
            });
        }

        /* Add Edit Menu */
        MenuItem menuBar_Edit= new MenuItem (menuBar, SWT.CASCADE);
        menuBar_Edit.setText ("&Edit");
        Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        menuBar_Edit.setMenu( editMenu );
        for (int i=0; i<EditMenuItems.length; i++)
        {
            final int idx = i;
            MenuItem item = new MenuItem (editMenu, SWT.CASCADE);
            item.setText( EditMenuItems[i] );
            item.addListener (SWT.Selection, new Listener () {
                public void handleEvent (Event e) {
                    // editMenuHandler(idx);
                }
            });
        }

        /* Add Help Menu */
        MenuItem menuBar_Help= new MenuItem (menuBar, SWT.CASCADE);
        menuBar_Help.setText ("&Help");
        Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
        menuBar_Help.setMenu( helpMenu );
        for (int i=0; i<HelpMenuItems.length; i++)
        {
            final int idx = i;
            MenuItem item = new MenuItem (helpMenu, SWT.CASCADE);
            item.setText( HelpMenuItems[i] );
            item.addListener (SWT.Selection, new Listener () {
                public void handleEvent (Event e) {
                    // aboutMenuHandler(idx);
                }
            });
        }

        // Create a grid below the menu bar
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = true;
        shell.setLayout(gl);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);

        // The Audio Frame
        buildAudioFrame();

        // The API Frame
        buildAPIFrame();

        // The NLP Frame
        buildNLPFrame();

        // The IOT Frame
        buildIOTFrame();

        shell.pack();
    }

    /*
     * =======================================================
     * Public Methods - Thread Managmenet
     * =======================================================
     */

    /** 
     * Submit a message to this thread.
     * The queues are thread-safe.
     */
    public static void submit(Message msg) 
    {
        if ( msg == null )
            return;

        // Route to the appropriate queue.
        if ( msg.getDataItem(Message.S_AUDIOMSG) != null )
        {
            log.info("UI inbound message for AUDIO");
            audioMsgs.add( msg );
        }
        else if ( msg.getDataItem(Message.S_APIMSG) != null )
        {
            log.info("UI inbound message for API");
            apiMsgs.add( msg );
        }
        else if ( msg.getDataItem(Message.S_NLPMSG) != null )
        {
            log.info("UI inbound message for NLP");
            nlpMsgs.add( msg );
        }
        else if ( msg.getDataItem(Message.S_IOTMSG) != null )
        {
            log.info("UI inbound message for IOT");
            iotMsgs.add( msg );
        }
    }
    /** 
     * Setup specific setup items.
     */
    boolean setup() 
    {
        if ( !isRunning )
        {
            return true;
        }
        return false;
    }

    /** 
     * Start the thread.
     * @return True if the thread is started, false if it is already running.
     * @throws IOException if the thread cannot start.
     */
    synchronized boolean start() throws IOException
    {
        /*
         * Create and start the thread.
         */
        if ( !isRunning )
        {
            // Start UI thread
            this.thread = new Thread(this, "JARVUI");
            thread.start();

            // Start message handling sub-threads.
            this.audioThread = new Thread(new AudioHandler(), "UIAUDIO");
            audioThread.start();
            this.apiThread = new Thread(new APIHandler(), "UIAPI");
            apiThread.start();
            this.nlpThread = new Thread(new NLPHandler(), "UINLP");
            nlpThread.start();
            this.iotThread = new Thread(new IOTHandler(), "UIIOT");
            iotThread.start();

            /* Wait for thread to start. */
            Calendar startWait = Calendar.getInstance();
            while ( !isRunning )
            {
                Calendar now = Calendar.getInstance();
                now.add(Calendar.SECOND, (-1*5));
                if ( now.after(startWait) )
                    break;
            }
            if ( !isRunning )
            {
                thread.interrupt();
                audioThread.interrupt();
                apiThread.interrupt();
                nlpThread.interrupt();
                iotThread.interrupt();

                thread = null;
                audioThread = null;
                apiThread = null;
                nlpThread = null;
                iotThread = null;

                runComplete = false;
                throw new IOException("Timed out waiting for server thread to start.");
            }
            return true;
        }
        else
            return false;
    }

    /** 
     * Stop the thread.  This always succeeds and can be used to make
     * sure the thread is ready to be started again.
     */
    synchronized void shutdown()
    {
        doShutdown = true;
        if ( isRunning )
        {
            isRunning = false;
            audioIsRunning = false;
            apiIsRunning = false;
            nlpIsRunning = false;
            iotIsRunning = false;

            thread.interrupt();
            audioThread.interrupt();
            apiThread.interrupt();
            nlpThread.interrupt();
            iotThread.interrupt();

            /* Wait for the thread to stop. */
            Calendar startWait = Calendar.getInstance();
            while ( !runComplete )
            {
                Calendar now = Calendar.getInstance();
                now.add(Calendar.SECOND, (-1*5));
                if ( now.after(startWait) )
                    break;
            }
            log.info("UI shutdown complete.");
        }
        doShutdown = false;
    }

    /** 
     * Handle inbound messages.
     */
    public void run()
    {
        if ( isRunning )
            return;
        runComplete = false;

        // Build the UI 
        buildUI();

        /*
         *----------------------------------------------------------------
         * Spin, waiting on shutdown.
         *----------------------------------------------------------------
         */
        log.info("Starting UI Thread.");
        isRunning = true;
        shell.open();
        while ( isRunning && !shell.isDisposed() )
        {
            if ( !display.readAndDispatch() )
                display.sleep();
        }
        if ( !shell.isDisposed() )
            shell.dispose();
        display.dispose();
        runComplete = true;
        log.info("UI thread exiting.");
    }
}

