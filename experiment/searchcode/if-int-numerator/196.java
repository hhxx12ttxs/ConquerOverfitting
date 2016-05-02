<<<<<<< HEAD
package org.herac.tuxguitar.gui.editors.matrix;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.actions.ActionLock;
import org.herac.tuxguitar.gui.actions.caret.GoLeftAction;
import org.herac.tuxguitar.gui.actions.caret.GoRightAction;
import org.herac.tuxguitar.gui.actions.duration.DecrementDurationAction;
import org.herac.tuxguitar.gui.actions.duration.IncrementDurationAction;
import org.herac.tuxguitar.gui.editors.TGPainter;
import org.herac.tuxguitar.gui.editors.TGRedrawListener;
import org.herac.tuxguitar.gui.editors.tab.Caret;
import org.herac.tuxguitar.gui.editors.tab.TGNoteImpl;
import org.herac.tuxguitar.gui.system.config.TGConfigKeys;
import org.herac.tuxguitar.gui.system.icons.IconLoader;
import org.herac.tuxguitar.gui.system.language.LanguageLoader;
import org.herac.tuxguitar.gui.undo.undoables.measure.UndoableMeasureGeneric;
import org.herac.tuxguitar.gui.util.DialogUtils;
import org.herac.tuxguitar.gui.util.TGMusicKeyUtils;
import org.herac.tuxguitar.player.base.MidiPercussion;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGChannel;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.song.models.TGVelocities;
import org.herac.tuxguitar.song.models.TGVoice;

public class MatrixEditor implements TGRedrawListener,IconLoader,LanguageLoader{
	
	private static final int BORDER_HEIGHT = 20;
	private static final int SCROLL_INCREMENT = 50;
	private static final String[] NOTE_NAMES = TGMusicKeyUtils.getSharpKeyNames(TGMusicKeyUtils.PREFIX_MATRIX);
	private static final MidiPercussion[] PERCUSSIONS = TuxGuitar.instance().getPlayer().getPercussions();
	protected static final int[] DIVISIONS = new int[] {1,2,3,4,6,8,16};
	
	private MatrixConfig config;
	private MatrixListener listener;
	private Shell dialog;
	private Composite composite;
	private Composite toolbar;
	private Composite editor;
	private Rectangle clientArea;
	private Image buffer;
	private BufferDisposer bufferDisposer;
	private Label durationLabel;
	private Label gridsLabel;
	private Button settings;
	private float width;
	private float height;
	private float bufferWidth;
	private float bufferHeight;
	private float timeWidth;
	private float lineHeight;
	private int leftSpacing;
	private int minNote;
	private int maxNote;
	private int duration;
	private int selection;
	private int grids;
	private int playedTrack;
	private int playedMeasure;
	private TGBeat playedBeat;
	
	private Image selectionBackBuffer;
	private int selectionX;
	private int selectionY;
	
	private boolean selectionPaintDisabled;
	
	public MatrixEditor(){
		this.grids = this.loadGrids();
		this.listener = new MatrixListener();
	}
	
	public void show(){
		this.config = new MatrixConfig();
		this.config.load();
		
		this.dialog = DialogUtils.newDialog(TuxGuitar.instance().getShell(),SWT.DIALOG_TRIM | SWT.RESIZE);
		this.dialog.setText(TuxGuitar.getProperty("matrix.editor"));
		this.dialog.setImage(TuxGuitar.instance().getIconManager().getAppIcon());
		this.dialog.setLayout(new GridLayout());
		this.dialog.addDisposeListener(new DisposeListenerImpl());
		this.bufferDisposer = new BufferDisposer();
		
		this.composite = new Composite(this.dialog,SWT.NONE);
		this.composite.setLayout(new GridLayout());
		this.composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		this.initToolBar();
		this.initEditor();
		this.loadIcons();
		
		this.addListeners();
		this.dialog.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				removeListeners();
				TuxGuitar.instance().updateCache(true);
			}
		});
		DialogUtils.openDialog(this.dialog,DialogUtils.OPEN_STYLE_CENTER);
	}
	
	public void addListeners(){
		TuxGuitar.instance().getkeyBindingManager().appendListenersTo(this.toolbar);
		TuxGuitar.instance().getkeyBindingManager().appendListenersTo(this.editor);
		TuxGuitar.instance().getIconManager().addLoader(this);
		TuxGuitar.instance().getLanguageManager().addLoader(this);
		TuxGuitar.instance().getEditorManager().addRedrawListener( this );
	}
	
	public void removeListeners(){
		TuxGuitar.instance().getIconManager().removeLoader(this);
		TuxGuitar.instance().getLanguageManager().removeLoader(this);
		TuxGuitar.instance().getEditorManager().removeRedrawListener( this );
	}
	
	private void initToolBar() {
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		
		this.toolbar = new Composite(this.composite, SWT.NONE);
		
		// position
		layout.numColumns ++;
		Button goLeft = new Button(this.toolbar, SWT.ARROW | SWT.LEFT);
		goLeft.addSelectionListener(TuxGuitar.instance().getAction(GoLeftAction.NAME));
		
		layout.numColumns ++;
		Button goRight = new Button(this.toolbar, SWT.ARROW | SWT.RIGHT);
		goRight.addSelectionListener(TuxGuitar.instance().getAction(GoRightAction.NAME));
		
		// separator
		layout.numColumns ++;
		makeToolSeparator(this.toolbar);
		
		// duration
		layout.numColumns ++;
		Button decrement = new Button(this.toolbar, SWT.ARROW | SWT.MIN);
		decrement.addSelectionListener(TuxGuitar.instance().getAction(DecrementDurationAction.NAME));
		
		layout.numColumns ++;
		this.durationLabel = new Label(this.toolbar, SWT.BORDER);
		
		layout.numColumns ++;
		Button increment = new Button(this.toolbar, SWT.ARROW | SWT.MAX);
		increment.addSelectionListener(TuxGuitar.instance().getAction(IncrementDurationAction.NAME));
		
		// separator
		layout.numColumns ++;
		makeToolSeparator(this.toolbar);
		
		// grids
		layout.numColumns ++;
		this.gridsLabel = new Label(this.toolbar,SWT.NONE);
		this.gridsLabel.setText(TuxGuitar.getProperty("matrix.grids"));
		
		layout.numColumns ++;
		final Combo divisionsCombo = new Combo(this.toolbar, SWT.DROP_DOWN | SWT.READ_ONLY);
		divisionsCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false, true));
		for(int i = 0; i < DIVISIONS.length; i ++){
			divisionsCombo.add(Integer.toString(DIVISIONS[i]));
			if(this.grids == DIVISIONS[i]){
				divisionsCombo.select(i);
			}
		}
		if(this.grids == 0){
			divisionsCombo.select(0);
		}
		divisionsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = divisionsCombo.getSelectionIndex();
				if(index >= 0 && index < DIVISIONS.length){
					setGrids(DIVISIONS[index]);
				}
			}
		});
		
		// settings
		layout.numColumns ++;
		this.settings = new Button(this.toolbar, SWT.PUSH);
		this.settings.setImage(TuxGuitar.instance().getIconManager().getSettings());
		this.settings.setToolTipText(TuxGuitar.getProperty("settings"));
		this.settings.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,true,true));
		this.settings.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configure();
			}
		});
		
		this.toolbar.setLayout(layout);
		this.toolbar.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
	}
	
	private void makeToolSeparator(Composite parent){
		Label separator = new Label(parent,SWT.SEPARATOR);
		separator.setLayoutData(new GridData(20,20));
	}
	
	private void loadDurationImage(boolean force) {
		int duration = TuxGuitar.instance().getTablatureEditor().getTablature().getCaret().getDuration().getValue();
		if(force || this.duration != duration){
			this.duration = duration;
			this.durationLabel.setImage(TuxGuitar.instance().getIconManager().getDuration(this.duration));
		}
	}
	
	public void initEditor(){
		this.selection = -1;
		this.editor = new Composite(this.composite,SWT.DOUBLE_BUFFERED | SWT.BORDER  | SWT.H_SCROLL | SWT.V_SCROLL);
		this.editor.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		this.editor.setLayout(new FillLayout());
		this.editor.setFocus();
		this.editor.addPaintListener(this.listener);
		this.editor.addMouseListener(this.listener);
		this.editor.addMouseMoveListener(this.listener);
		this.editor.addMouseTrackListener(this.listener);
		this.editor.getHorizontalBar().setIncrement(SCROLL_INCREMENT);
		this.editor.getHorizontalBar().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				redrawLocked();
			}
		});
		this.editor.getVerticalBar().setIncrement(SCROLL_INCREMENT);
		this.editor.getVerticalBar().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				redrawLocked();
			}
		});
	}
	
	protected void updateScroll(){
		if( this.clientArea != null ){
			int borderWidth = this.editor.getBorderWidth();
			ScrollBar vBar = this.editor.getVerticalBar();
			ScrollBar hBar = this.editor.getHorizontalBar();
			vBar.setMaximum(Math.round(this.height + (borderWidth * 2)));
			vBar.setThumb(Math.round(Math.min(this.height + (borderWidth * 2), this.clientArea.height)));
			hBar.setMaximum(Math.round(this.width + (borderWidth * 2)));
			hBar.setThumb(Math.round(Math.min(this.width + (borderWidth * 2), this.clientArea.width)));
		}
	}
	
	protected int getValueAt(float y){
		if(this.clientArea == null || (y - BORDER_HEIGHT) < 0 || y + BORDER_HEIGHT > this.clientArea.height){
			return -1;
		}
		int scroll = this.editor.getVerticalBar().getSelection();
		int value = (this.maxNote -  ((int)(  (y + scroll - BORDER_HEIGHT)  / this.lineHeight)) );
		return value;
	}
	
	protected long getStartAt(float x){
		TGMeasure measure = getMeasure();
		float posX = (x + this.editor.getHorizontalBar().getSelection());
		long start =(long) (measure.getStart() + (((posX - this.leftSpacing) * measure.getLength()) / (this.timeWidth * measure.getTimeSignature().getNumerator())));
		return start;
	}
	
	protected void paintEditor(TGPainter painter){
		if(!TuxGuitar.instance().getPlayer().isRunning()){
			this.resetPlayed();
		}
		
		this.disposeSelectionBuffer();
		this.clientArea = this.editor.getClientArea();
		
		if( this.clientArea != null ){
			Image buffer = getBuffer();
			
			this.width = this.bufferWidth;
			this.height = (this.bufferHeight + (BORDER_HEIGHT *2));
			
			this.updateScroll();
			int scrollX = this.editor.getHorizontalBar().getSelection();
			int scrollY = this.editor.getVerticalBar().getSelection();
			
			painter.drawImage(buffer,-scrollX,(BORDER_HEIGHT - scrollY));
			this.paintMeasure(painter,(-scrollX), (BORDER_HEIGHT - scrollY) );
			this.paintBorders(painter,(-scrollX),0);
			this.paintPosition(painter,(-scrollX),0);
			
			this.paintSelection(painter, (-scrollX), (BORDER_HEIGHT - scrollY) );
		}
	}
	
	protected Image getBuffer(){
		if( this.clientArea != null ){
			this.bufferDisposer.update(this.clientArea.width, this.clientArea.height);
			if(this.buffer == null || this.buffer.isDisposed()){
				String[] names = null;
				TGMeasure measure = getMeasure();
				boolean percussion = measure.getTrack().isPercussionTrack();
				this.maxNote = 0;
				this.minNote = 127;
				if(percussion){
					names = new String[PERCUSSIONS.length];
					for(int i = 0; i < names.length;i ++){
						this.minNote = Math.min(this.minNote,PERCUSSIONS[i].getValue());
						this.maxNote = Math.max(this.maxNote,PERCUSSIONS[i].getValue());
						names[i] = PERCUSSIONS[names.length - i -1].getName();
					}
				}else{
					for(int sNumber = 1; sNumber <= measure.getTrack().stringCount();sNumber ++){
						TGString string = measure.getTrack().getString(sNumber);
						this.minNote = Math.min(this.minNote,string.getValue());
						this.maxNote = Math.max(this.maxNote,(string.getValue() + 20));
					}
					names = new String[this.maxNote - this.minNote + 1];
					for(int i = 0; i < names.length;i ++){
						names[i] = (NOTE_NAMES[ (this.maxNote - i) % 12] + ((this.maxNote - i) / 12 ) );
					}
				}
				
				int minimumNameWidth = 110;
				int minimumNameHeight = 0;
				TGPainter painter = new TGPainter(new GC(this.dialog.getDisplay()));
				painter.setFont(this.config.getFont());
				for(int i = 0; i < names.length;i ++){
					Point size = painter.getStringExtent(names[i]);
					if( size.x > minimumNameWidth ){
						minimumNameWidth = size.x;
					}
					if( size.y  > minimumNameHeight ){
						minimumNameHeight = size.y ;
					}
				}
				painter.dispose();
				
				int cols = measure.getTimeSignature().getNumerator();
				int rows = (this.maxNote - this.minNote);
				
				this.leftSpacing = minimumNameWidth + 10;
				this.lineHeight = Math.max(minimumNameHeight,( (this.clientArea.height - (BORDER_HEIGHT * 2.0f))/ (rows + 1.0f)));
				this.timeWidth = Math.max((10 * (TGDuration.SIXTY_FOURTH / measure.getTimeSignature().getDenominator().getValue())),( (this.clientArea.width-this.leftSpacing) / cols)  );
				this.bufferWidth = this.leftSpacing + (this.timeWidth * cols);
				this.bufferHeight = (this.lineHeight * (rows + 1));
				this.buffer = new Image(this.editor.getDisplay(),Math.round( this.bufferWidth),Math.round(this.bufferHeight));
				
				painter = new TGPainter(new GC(this.buffer));
				painter.setFont(this.config.getFont());
				painter.setForeground(this.config.getColorForeground());
				for(int i = 0; i <= rows; i++){
					painter.setBackground(this.config.getColorLine( i % 2 ) );
					painter.initPath(TGPainter.PATH_FILL);
					painter.setAntialias(false);
					painter.addRectangle(0 ,(i * this.lineHeight),this.bufferWidth ,this.lineHeight);
					painter.closePath();
					painter.drawString(names[i],5,( Math.round( (i * this.lineHeight) ) +  Math.round(  (this.lineHeight - minimumNameHeight) / 2 )  ) );
				}
				for(int i = 0; i < cols; i ++){
					float colX = this.leftSpacing + (i * this.timeWidth);
					float divisionWidth = ( this.timeWidth / this.grids );
					for( int j = 0; j < this.grids; j ++ ){
						painter.setLineStyle( j == 0 ? SWT.LINE_SOLID : SWT.LINE_DOT);
						painter.initPath();
						painter.setAntialias(false);
						painter.moveTo(Math.round( colX + (j * divisionWidth) ),0);
						painter.lineTo(Math.round( colX + (j * divisionWidth) ),this.bufferHeight);
						painter.closePath();
					}
				}
				painter.dispose();
			}
		}
		return this.buffer;
	}
	
	protected void paintMeasure(TGPainter painter,float fromX, float fromY){
		if( this.clientArea != null ){
			TGMeasure measure = getMeasure();
			if(measure != null){
				Iterator it = measure.getBeats().iterator();
				while(it.hasNext()){
					TGBeat beat = (TGBeat)it.next();
					paintBeat(painter, measure, beat, fromX, fromY);
				}
			}
		}
	}
	
	protected void paintBeat(TGPainter painter,TGMeasure measure,TGBeat beat,float fromX, float fromY){
		if( this.clientArea != null ){
			int minimumY = BORDER_HEIGHT;
			int maximumY = (this.clientArea.height - BORDER_HEIGHT);
			
			for( int v = 0; v < beat.countVoices(); v ++ ){
				TGVoice voice = beat.getVoice(v);
				for( int i = 0 ; i < voice.countNotes() ; i ++){
					TGNoteImpl note = (TGNoteImpl)voice.getNote(i);
					float x1 = (fromX + this.leftSpacing + (((beat.getStart() - measure.getStart()) * (this.timeWidth * measure.getTimeSignature().getNumerator())) / measure.getLength()) + 1);
					float y1 = (fromY + (((this.maxNote - this.minNote) - (note.getRealValue() - this.minNote)) * this.lineHeight) + 1 );
					float x2 = (x1 + ((voice.getDuration().getTime() * this.timeWidth) / measure.getTimeSignature().getDenominator().getTime()) - 2 );
					float y2 = (y1 + this.lineHeight - 2 );
					
					if( y1 >= maximumY || y2 <= minimumY){
						continue;
					}
					
					y1 = ( y1 < minimumY ? minimumY : y1 );
					y2 = ( y2 > maximumY ? maximumY : y2 );
					
					if((x2 - x1) > 0 && (y2 - y1) > 0){
						painter.setBackground( (note.getBeatImpl().isPlaying(TuxGuitar.instance().getTablatureEditor().getTablature().getViewLayout()) ? this.config.getColorPlay():this.config.getColorNote() ) );
						painter.initPath(TGPainter.PATH_FILL);
						painter.setAntialias(false);
						painter.addRectangle(x1,y1, (x2 - x1), (y2 - y1));
						painter.closePath();
					}
				}
			}
		}
	}
	
	protected void paintBorders(TGPainter painter,float fromX, float fromY){
		if( this.clientArea != null ){
			painter.setBackground(this.config.getColorBorder());
			painter.initPath(TGPainter.PATH_FILL);
			painter.setAntialias(false);
			painter.addRectangle(fromX,fromY,this.bufferWidth ,BORDER_HEIGHT);
			painter.addRectangle(fromX,fromY + (this.clientArea.height - BORDER_HEIGHT),this.bufferWidth ,BORDER_HEIGHT);
			painter.closePath();
			
			painter.initPath();
			painter.setAntialias(false);
			painter.addRectangle(fromX,fromY,this.width,this.clientArea.height);
			painter.closePath();
		}
	}
	
	protected void paintPosition(TGPainter painter,float fromX, float fromY){
		if( this.clientArea != null && !TuxGuitar.instance().getPlayer().isRunning()){
			Caret caret = getCaret();
			TGMeasure measure = getMeasure();
			TGBeat beat = caret.getSelectedBeat();
			if(beat != null){
				float x = (((beat.getStart() - measure.getStart()) * (this.timeWidth * measure.getTimeSignature().getNumerator())) / measure.getLength());
				float width = ((beat.getVoice(caret.getVoice()).getDuration().getTime() * this.timeWidth) / measure.getTimeSignature().getDenominator().getTime());
				painter.setBackground(this.config.getColorPosition());
				painter.initPath(TGPainter.PATH_FILL);
				painter.setAntialias(false);
				painter.addRectangle(fromX + (this.leftSpacing + x),fromY , width,BORDER_HEIGHT);
				painter.closePath();
				
				painter.initPath(TGPainter.PATH_FILL);
				painter.setAntialias(false);
				painter.addRectangle(fromX + (this.leftSpacing + x),fromY + (this.clientArea.height - BORDER_HEIGHT), width,BORDER_HEIGHT);
				painter.closePath();
			}
		}
	}
	
	protected void paintSelection(TGPainter painter, float fromX, float fromY){
		if( !this.selectionPaintDisabled && this.clientArea != null && !TuxGuitar.instance().getPlayer().isRunning()){
			selectionFinish();
			if(this.selection >= 0){
				this.selectionPaintDisabled = true;
				
				int x = Math.round( fromX );
				int y = Math.round( fromY + ((this.maxNote - this.selection) * this.lineHeight)  );
				int width = Math.round( this.bufferWidth );
				int height = Math.round( this.lineHeight );
				
				Image selectionArea = new Image(this.editor.getDisplay(),width,height);
				painter.copyArea(selectionArea,x,y);
				painter.setAlpha(100);
				painter.setBackground(this.config.getColorLine(2));
				painter.initPath(TGPainter.PATH_FILL);
				painter.setAntialias(false);
				painter.addRectangle(x,y,width,height);
				painter.closePath();
				
				this.selectionX = x;
				this.selectionY = y;
				this.selectionBackBuffer = selectionArea;
				this.selectionPaintDisabled = false;
			}
		}
	}
	
	protected void updateSelection(float y){
		if(!TuxGuitar.instance().getPlayer().isRunning()){
			int selection = getValueAt(y);
			
			if(this.selection != selection){
				this.selection = selection;
				
				int scrollX = this.editor.getHorizontalBar().getSelection();
				int scrollY = this.editor.getVerticalBar().getSelection();
				
				TGPainter painter = new TGPainter(new GC(this.editor));
				this.paintSelection(painter, (-scrollX), (BORDER_HEIGHT - scrollY) );
				painter.dispose();
			}
		}
	}
	
	public void selectionFinish(){
		if(this.selectionBackBuffer != null && !this.selectionBackBuffer.isDisposed()){
			TGPainter painter = new TGPainter(new GC(this.editor));
			painter.drawImage(this.selectionBackBuffer,this.selectionX, this.selectionY);
			painter.dispose();
		}
		disposeSelectionBuffer();
	}
	
	protected void disposeSelectionBuffer(){
		if(this.selectionBackBuffer != null && !this.selectionBackBuffer.isDisposed()){
			this.selectionBackBuffer.dispose();
			this.selectionBackBuffer = null;
		}
	}
	
	protected void hit(float x, float y){
		if(!TuxGuitar.instance().getPlayer().isRunning()){
			TGMeasure measure = getMeasure();
			Caret caret = getCaret();
			int value = getValueAt(y);
			long start = getStartAt(x);
			
			if(start >= measure.getStart() && start < (measure.getStart() + measure.getLength())){
				caret.update(caret.getTrack().getNumber(),start,caret.getStringNumber());
				TuxGuitar.instance().updateCache(true);
			}
			if(value >= this.minNote || value <= this.maxNote){
				if(start >= measure.getStart()){
					TGVoice voice = TuxGuitar.instance().getSongManager().getMeasureManager().getVoiceIn(measure, start, caret.getVoice());
					if( voice != null ){
						if(!removeNote(voice.getBeat(), value)){
							addNote(voice.getBeat(), start, value);
						}
					}
				}else{
					play(value);
				}
			}
		}
	}
	
	private boolean removeNote(TGBeat beat,int value) {
		Caret caret = TuxGuitar.instance().getTablatureEditor().getTablature().getCaret();
		TGMeasure measure = getMeasure();
		
		for(int v = 0; v < beat.countVoices(); v ++){
			TGVoice voice = beat.getVoice( v );
			Iterator it = voice.getNotes().iterator();
			while (it.hasNext()) {
				TGNoteImpl note = (TGNoteImpl) it.next();
				if (note.getRealValue() == value) {
					caret.update(measure.getTrack().getNumber(),beat.getStart(),note.getString());
					
					//comienza el undoable
					UndoableMeasureGeneric undoable = UndoableMeasureGeneric.startUndo();
					
					TGSongManager manager = TuxGuitar.instance().getSongManager();
					manager.getMeasureManager().removeNote(note);
					
					//termia el undoable
					TuxGuitar.instance().getUndoableManager().addEdit(undoable.endUndo());
					TuxGuitar.instance().getFileHistory().setUnsavedFile();
					
					this.afterAction();
					
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean addNote(TGBeat beat,long start, int value) {
		if(beat != null){
			TGMeasure measure = getMeasure();
			Caret caret = TuxGuitar.instance().getTablatureEditor().getTablature().getCaret();
			
			List strings = measure.getTrack().getStrings();
			for(int i = 0;i < strings.size();i ++){
				TGString string = (TGString)strings.get(i);
				if(value >= string.getValue()){
					boolean emptyString = true;
					
					for(int v = 0; v < beat.countVoices(); v ++){
						TGVoice voice = beat.getVoice( v );
						Iterator it = voice.getNotes().iterator();
						while (it.hasNext()) {
							TGNoteImpl note = (TGNoteImpl) it.next();
							if (note.getString() == string.getNumber()) {
								emptyString = false;
								break;
							}
						}
					}
					if(emptyString){
						TGSongManager manager = TuxGuitar.instance().getSongManager();
						
						//comienza el undoable
						UndoableMeasureGeneric undoable = UndoableMeasureGeneric.startUndo();
						
						TGNote note = manager.getFactory().newNote();
						note.setValue((value - string.getValue()));
						note.setVelocity(caret.getVelocity());
						note.setString(string.getNumber());
						
						TGDuration duration = manager.getFactory().newDuration();
						caret.getDuration().copy(duration);
						
						manager.getMeasureManager().addNote(beat,note,duration,start,caret.getVoice());
						
						caret.moveTo(caret.getTrack(),caret.getMeasure(),note.getVoice().getBeat(),note.getString());
						
						//termia el undoable
						TuxGuitar.instance().getUndoableManager().addEdit(undoable.endUndo());
						TuxGuitar.instance().getFileHistory().setUnsavedFile();
						
						//reprodusco las notas en el pulso
						caret.getSelectedBeat().play();
						
						this.afterAction();
						
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected void afterAction() {
		TuxGuitar.instance().getTablatureEditor().getTablature().getViewLayout().fireUpdate(getMeasure().getNumber());
		TuxGuitar.instance().updateCache(true);
		this.editor.redraw();
	}
	
	protected void play(final int value){
		new Thread(new Runnable() {
			public void run() {
				TGTrack track = getMeasure().getTrack();
				int volume = TGChannel.DEFAULT_VOLUME;
				int balance = TGChannel.DEFAULT_BALANCE;
				int chorus = track.getChannel().getChorus();
				int reverb = track.getChannel().getReverb();
				int phaser = track.getChannel().getPhaser();
				int tremolo = track.getChannel().getTremolo();
				int channel = track.getChannel().getChannel();
				int program = track.getChannel().getInstrument();
				int[][] beat = new int[][]{ new int[]{ (track.getOffset() + value) , TGVelocities.DEFAULT } };
				TuxGuitar.instance().getPlayer().playBeat(channel,program, volume, balance,chorus,reverb,phaser,tremolo,beat);
			}
		}).start();
	}
	
	protected int loadGrids(){
		int grids = TuxGuitar.instance().getConfig().getIntConfigValue(TGConfigKeys.MATRIX_GRIDS);
		// check if is valid value
		for(int i = 0 ; i < DIVISIONS.length ; i ++ ){
			if(grids == DIVISIONS[i]){
				return grids;
			}
		}
		return DIVISIONS[1];
	}
	
	protected void setGrids(int grids){
		this.grids = grids;
		this.disposeBuffer();
		this.redrawLocked();
	}
	
	public int getGrids(){
		return this.grids;
	}
	
	protected TGMeasure getMeasure(){
		if(TuxGuitar.instance().getPlayer().isRunning()){
			TGMeasure measure = TuxGuitar.instance().getEditorCache().getPlayMeasure();
			if(measure != null){
				return measure;
			}
		}
		return TuxGuitar.instance().getTablatureEditor().getTablature().getCaret().getMeasure();
	}
	
	protected Caret getCaret(){
		return TuxGuitar.instance().getTablatureEditor().getTablature().getCaret();
	}
	
	public boolean isDisposed(){
		return (this.dialog == null || this.dialog.isDisposed());
	}
	
	protected void resetPlayed(){
		this.playedBeat = null;
		this.playedMeasure = -1;
		this.playedTrack = -1;
	}
	
	public void redrawLocked(){
		if(!TuxGuitar.instance().isLocked()){
			TuxGuitar.instance().lock();
			this.redraw();
			TuxGuitar.instance().unlock();
		}
	}
	
	public void redraw(){
		if(!isDisposed() && !TuxGuitar.instance().isLocked()){
			//TuxGuitar.instance().lock();
			
			this.editor.redraw();
			this.loadDurationImage(false);
			
			//TuxGuitar.instance().unlock();
		}
	}
	
	public void redrawPlayingMode(){
		if(!isDisposed() && !TuxGuitar.instance().isLocked() && TuxGuitar.instance().getPlayer().isRunning()){
			//TuxGuitar.instance().lock();
			
			TGMeasure measure = TuxGuitar.instance().getEditorCache().getPlayMeasure();
			TGBeat beat = TuxGuitar.instance().getEditorCache().getPlayBeat();
			if(measure != null && beat != null){
				int currentMeasure = measure.getNumber();
				int currentTrack = measure.getTrack().getNumber();
				boolean changed = (currentMeasure != this.playedMeasure || currentTrack != this.playedTrack);
				if(changed){
					this.resetPlayed();
					this.editor.redraw();
				}
				else{
					TGPainter painter = new TGPainter(new GC(this.editor));
					int scrollX = this.editor.getHorizontalBar().getSelection();
					int scrollY = this.editor.getVerticalBar().getSelection();
					if(this.playedBeat != null){
						this.paintBeat(painter,measure,this.playedBeat,(-scrollX), (BORDER_HEIGHT - scrollY));
					}
					this.paintBeat(painter,measure,beat,(-scrollX), (BORDER_HEIGHT - scrollY));
					painter.dispose();
				}
				this.playedMeasure = currentMeasure;
				this.playedTrack = currentTrack;
				this.playedBeat = beat;
			}
			//TuxGuitar.instance().unlock();
		}
	}
	
	protected void configure(){
		this.config.configure(this.dialog);
		this.disposeBuffer();
		this.redrawLocked();
	}
	
	private void layout(){
		if( !isDisposed() ){
			this.toolbar.layout();
			this.editor.layout();
			this.composite.layout(true,true);
		}
	}
	
	public void loadIcons(){
		if( !isDisposed() ){
			this.dialog.setImage(TuxGuitar.instance().getIconManager().getAppIcon());
			this.settings.setImage(TuxGuitar.instance().getIconManager().getSettings());
			this.loadDurationImage(true);
			this.layout();
			this.redraw();
		}
	}
	
	public void loadProperties() {
		if( !isDisposed() ){
			this.dialog.setText(TuxGuitar.getProperty("matrix.editor"));
			this.gridsLabel.setText(TuxGuitar.getProperty("matrix.grids"));
			this.settings.setToolTipText(TuxGuitar.getProperty("settings"));
			this.disposeBuffer();
			this.layout();
			this.redraw();
		}
	}
	
	public void dispose(){
		if(!isDisposed()){
			this.dialog.dispose();
		}
	}
	
	protected void disposeBuffer(){
		if(this.buffer != null && !this.buffer.isDisposed()){
			this.buffer.dispose();
			this.buffer = null;
		}
	}
	
	protected void dispose(Resource[] resources){
		if(resources != null){
			for(int i = 0; i < resources.length; i ++){
				dispose(resources[i]);
			}
		}
	}
	
	protected void dispose(Resource resource){
		if(resource != null){
			resource.dispose();
		}
	}
	
	protected void disposeAll(){
		this.disposeBuffer();
		this.disposeSelectionBuffer();
		this.config.dispose();
	}
	
	protected Composite getEditor(){
		return this.editor;
	}
	
	protected class BufferDisposer{
		private int numerator;
		private int denominator;
		private int track;
		private boolean percussion;
		
		private int width;
		private int height;
		
		public void update(int width, int height){
			TGMeasure measure = getMeasure();
			int track = measure.getTrack().getNumber();
			int numerator = measure.getTimeSignature().getNumerator();
			int denominator = measure.getTimeSignature().getDenominator().getValue();
			boolean percussion = measure.getTrack().isPercussionTrack();
			if(width != this.width || height != this.height || this.track != track || this.numerator != numerator || this.denominator != denominator || this.percussion != percussion){
				disposeBuffer();
			}
			this.track = track;
			this.numerator = numerator;
			this.denominator = denominator;
			this.percussion = percussion;
			this.width = width;
			this.height = height;
		}
	}
	
	protected class DisposeListenerImpl implements DisposeListener{
		public void widgetDisposed(DisposeEvent e) {
			disposeAll();
		}
	}
	
	protected class MatrixListener implements PaintListener,MouseListener,MouseMoveListener,MouseTrackListener {
		
		public MatrixListener(){
			super();
		}
		
		public void paintControl(PaintEvent e) {
			if(!TuxGuitar.instance().isLocked()){
				TuxGuitar.instance().lock();
				TGPainter painter = new TGPainter(e.gc);
				paintEditor(painter);
				TuxGuitar.instance().unlock();
			}
		}
		
		public void mouseUp(MouseEvent e) {
			getEditor().setFocus();
			if(e.button == 1){
				if(!TuxGuitar.instance().isLocked() && !ActionLock.isLocked()){
					ActionLock.lock();
					hit(e.x,e.y);
					ActionLock.unlock();
				}
			}
		}
		
		public void mouseMove(MouseEvent e) {
			if(!TuxGuitar.instance().isLocked() && !ActionLock.isLocked()){
				updateSelection(e.y);
			}
		}
		
		public void mouseExit(MouseEvent e) {
			if(!TuxGuitar.instance().isLocked() && !ActionLock.isLocked()){
				updateSelection(-1);
			}
		}
		
		public void mouseEnter(MouseEvent e) {
			if(!TuxGuitar.instance().isLocked() && !ActionLock.isLocked()){
				redrawLocked();
			}
		}
		
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
		}
		
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
		}
		
		public void mouseHover(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	}
	
	public void doRedraw(int type) {
		if( type == TGRedrawListener.NORMAL ){
			this.redraw();
		}else if( type == TGRedrawListener.PLAYING_NEW_BEAT ){
			this.redrawPlayingMode();
		}
	}
=======
/*
 * Copyright (c) 2009 Levente Farkas
 * Copyright (C) 2009 Tamas Korodi <kotyo@zamba.fm> 
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 2003 David A. Schleef <ds@schleef.org>
 * 
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gstreamer;

import org.gstreamer.lowlevel.GType;
import org.gstreamer.lowlevel.GstNative;
import org.gstreamer.lowlevel.GstStructureAPI;
import org.gstreamer.lowlevel.GstValueAPI;
import org.gstreamer.lowlevel.NativeObject;
import org.gstreamer.lowlevel.GValueAPI.GValue;
import org.gstreamer.lowlevel.annotations.CallerOwnsReturn;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Generic structure containing fields of names and values.
 * <p>
 * A Structure is a collection of key/value pairs. The keys are expressed
 * as GQuarks and the values can be of any GType.
 * <p>
 * In addition to the key/value pairs, a Structure also has a name. The name
 * starts with a letter and can be followed by letters, numbers and any of "/-_.:".
 * <p>
 * Structure is used by various GStreamer subsystems to store information
 * in a flexible and extensible way. 
 * <p>
 * A Structure can be created with new {@link #Structure(String)} or 
 * {@link #Structure(String, String, Object...)}, which both take a name and an
 * optional set of key/value pairs along with the types of the values.
 * <p>
 * Field values can be changed with set{Integer,String}() etc functions.
 * <p>
 * Field values can be retrieved with get{Integer,String}() etc functions.
 * <p>
 * Fields can be removed with {@link #removeField} or {@link #removeFields}
 * @see Caps
 * @see Event
 */
public class Structure extends NativeObject {
    private static interface API extends GstStructureAPI, GstValueAPI {
        @CallerOwnsReturn Pointer ptr_gst_structure_from_string(String data, PointerByReference end);
        @CallerOwnsReturn Pointer ptr_gst_structure_empty_new(String name);
        //Pointer gst_structure_id_empty_new(GQuark quark);
        @CallerOwnsReturn Pointer ptr_gst_structure_new(String name, String firstField, Object... args);
        void gst_structure_free(Pointer ptr);
    }
    private static final API gst = GstNative.load(API.class);
    
    /**
     * Creates a new instance of Structure
     */
    public Structure(Initializer init) {
        super(init);
    }

    private Structure(Pointer ptr) {
        this(initializer(ptr));
    }
    
    /**
     * Creates a new, empty #GstStructure with the given name.
     *
     * @param name The name of new structure.
     */
    public Structure(String name) {
        this(gst.ptr_gst_structure_empty_new(name));
    }

    /**
     * Creates a new Structure with the given name.  Parses the
     * list of variable arguments and sets fields to the values listed.
     * Variable arguments should be passed as field name, field type,
     * and value.
     *
     * @param name The name of new structure.
     * @param firstFieldName The name of first field to set
     * @param data Additional arguments.
     */
    public Structure(String name, String firstFieldName, Object... data) {
        this(gst.ptr_gst_structure_new(name, firstFieldName, data));
    }

    /**
     * Creates a Structure from a string representation.
     *
     * @param data A string representation of a Structure.
     * @return A new Structure or null when the string could not be parsed.
     */
    public static Structure fromString(String data) {
        return new Structure(gst.ptr_gst_structure_from_string(data, new PointerByReference()));
    }

    public Structure copy() {
        return gst.gst_structure_copy(this);
    }

    public class InvalidFieldException extends RuntimeException {

        private static final long serialVersionUID = 864118748304334069L;

        public InvalidFieldException(String type, String fieldName) {
            super(String.format("Structure does not contain %s field '%s'", type, fieldName));
        }
    }

    /**
     * Gets ValueList field representation
     * @param fieldName The name of the field.
     * @return field as ValueList
     */
    public ValueList getValueList(String fieldName) {
    	GValue val = gst.gst_structure_get_value(this, fieldName);
    	if (val == null) {
    		throw new InvalidFieldException("ValueList", fieldName);        	
    	}
    	return new ValueList(val);
	}
    
    public Object getValue(String fieldName) {
    	GValue val = gst.gst_structure_get_value(this, fieldName);
    	
    	if (val == null) {
    		throw new InvalidFieldException("Object", fieldName);        	
    	}

	return val.getValue();
    }
    
    public int getInteger(String fieldName) {
        int[] val = { 0 };
        if (!gst.gst_structure_get_int(this, fieldName, val)) {
            throw new InvalidFieldException("integer", fieldName);
        }
        return val[0];
    }
    public int getInteger(String fieldName, int i) {
    	return getValueList(fieldName).getInteger(i);
    }
    public double getDouble(String fieldName) {
        double[] val = { 0d };
        if (!gst.gst_structure_get_double(this, fieldName, val)) {
            throw new InvalidFieldException("double", fieldName);
        }
        return val[0];
    }
    public double getDouble(String fieldName, int i) {
    	return getValueList(fieldName).getDouble(i);
    }
    public String getString(String fieldName) {
        return gst.gst_structure_get_string(this, fieldName);
    }
    public String getString(String fieldName, int i) {
    	return getValueList(fieldName).getString(i);
    }
    /**
     * 
     * @param fieldName
     * @return The boolean value for fieldName
     */
    public boolean getBoolean(String fieldName) {
        int[] val = { 0 };
        if (!gst.gst_structure_get_boolean(this, fieldName, val)) {
            throw new InvalidFieldException("boolean", fieldName);
        }
        return val[0] != 0;
    }
    public boolean getBoolean(String fieldName, int i) {
    	return getValueList(fieldName).getBoolean(i);
    }
    public Fraction getFraction(String fieldName) {
        int[] numerator = { 0 };
        int[] denominator = { 0 };
        if (!gst.gst_structure_get_fraction(this, fieldName, numerator, denominator)) {
            throw new InvalidFieldException("fraction", fieldName);
        }
        return new Fraction(numerator[0], denominator[0]);
    }    
    /**
     * Gets FOURCC field int representation
     * @param fieldName The name of the field.
     * @return FOURCC field as a 4 byte integer
     */
    public int getFourcc(String fieldName) {
    	int[] val = { 0 };
        if (!gst.gst_structure_get_fourcc(this, fieldName, val)) {
            throw new InvalidFieldException("FOURCC", fieldName);
        }
        return val[0];    	
    }
    /**
     * Gets FOURCC field String representation
     * @param fieldName The name of the field.
     * @return FOURCC field as a String
     */
    public String getFourccString(String fieldName) {
    	int f = getFourcc(fieldName);
    	byte[] b = {(byte)((f>>0)&0xff),(byte)((f>>8)&0xff),
    			    (byte)((f>>16)&0xff),(byte)((f>>24)&0xff)};
    	return new String(b);
    }
    /**
     * Gets Range field representation
     * @param fieldName The name of the field.
     * @return field as Range
     */
    public Range getRange(String fieldName) {
    	GValue val = gst.gst_structure_get_value(this, fieldName);
        if (val == null) {
            throw new InvalidFieldException("Range", fieldName);        	
        }
        return new Range(val);
    }

    public boolean fixateNearestInteger(String field, Integer value) {
        return gst.gst_structure_fixate_field_nearest_int(this, field, value);
    }
    
    /**
     * Sets an integer field in the structure.
     * 
     * @param field the name of the field to set.
     * @param value the value to set for the field.
     */
    public void setInteger(String field, Integer value) {
        gst.gst_structure_set(this, field, GType.INT, value);
    }
        
    public void setValue(String field, GType type, Object value) {
    	gst.gst_structure_set(this, field, type, value);
    }
        
    public void setDouble(String field, Double value) {
        gst.gst_structure_set(this, field, GType.DOUBLE, value);
    }

    public void setPointer(String field, Pointer value) {
        gst.gst_structure_set(this, field, GType.POINTER, value);
    }

    public void setIntegerRange(String field, Integer min, Integer max) {
        gst.gst_structure_set(this, field, 
                gst.gst_int_range_get_type(), min, max);
    }
    public void setDoubleRange(String field, Double min, Double max) {
        gst.gst_structure_set(this, field, 
                gst.gst_double_range_get_type(), min, max);
    }
    
    /**
     * Get the name of @structure as a string.
     *
     * @return The name of the structure.
     */
    public String getName() {
        return gst.gst_structure_get_name(this);
    }
    
    /**
     * Sets the name of the structure to the given name.
     * 
     * The name must not be empty, must start with a letter and can be followed 
     * by letters, numbers and any of "/-_.:".
     * 
     * @param name The new name of the structure.
     */
    public void setName(String name) {
        gst.gst_structure_set_name(this, name);
    }
    
    /**
     * Checks if the structure has the given name.
     * 
     * @param name structure name to check for
     * @return true if @name matches the name of the structure.
     */
    public boolean hasName(String name) {
        return gst.gst_structure_has_name(this, name);
    }
    /**
     * Check if the {@link Structure} contains a field named fieldName.
     *
     * @param fieldName The name of the field to check.
     * @return true if the structure contains a field with the given name.
     */
    public boolean hasField(String fieldName) {
        return gst.gst_structure_has_field(this, fieldName);
    }

    /**
     * Get the number of fields in the {@link Structure}.
     *
     * @return the structure's filed number.
     */
    public int getFields() {
        return gst.gst_structure_n_fields(this);
    }
    
    /**
     * Check if the {@link Structure} contains a field named fieldName.
     *
     * @param fieldName The name of the field to check.
     * @param fieldType The type of the field.
     * @return true if the structure contains a field named fieldName and of type fieldType
     */
    public boolean hasField(String fieldName, GType fieldType) {
        return gst.gst_structure_has_field_typed(this, fieldName, fieldType);
    }
    
    /**
     * Check if the {@link Structure} contains a field named fieldName.
     *
     * @param fieldName The name of the field to check.
     * @param fieldType The type of the field.
     * @return true if the structure contains a field named fieldName and of type fieldType
     */
    public boolean hasField(String fieldName, Class<?> fieldType) {
        return gst.gst_structure_has_field_typed(this, fieldName, GType.valueOf(fieldType));
    }
    
    /**
     * Check if the {@link Structure} contains an integer field named fieldName.
     *
     * @param fieldName The name of the field to check.
     * @return true if the structure contains an integer field named fieldName
     */
    public boolean hasIntField(String fieldName) {
        return hasField(fieldName, GType.INT);
    }
    
    /**
     * Check if the {@link Structure} contains a double field named fieldName.
     *
     * @param fieldName The name of the field to check.
     * @return true if the structure contains a double field named fieldName
     */
    public boolean hasDoubleField(String fieldName) {
        return hasField(fieldName, GType.DOUBLE);
    }
    
    /**
     * Removes the field with the given name from the structure.
     * If the field with the given name does not exist, the structure is unchanged.
     * @param fieldName The name of the field to remove.
     */
    public void removeField(String fieldName) {
        gst.gst_structure_remove_field(this, fieldName);
    }
    
    /**
     * Removes the fields with the given names. 
     * If a field does not exist, the argument is ignored.
     * 
     * @param fieldNames A list of field names to remove.
     */
    public void removeFields(String... fieldNames) {
        gst.gst_structure_remove_fields(this, fieldNames);
    }
    
    /**
     * Get the @structure's ith field name as a string.
     * @param i the requested filed number
     * @return The name of the structure.
     */
    public String getName(int i) {
        return gst.gst_structure_nth_field_name(this, i);
    }
    
    @Override
    public String toString() {
        return gst.gst_structure_to_string(this);
    }
    public static Structure objectFor(Pointer ptr, boolean needRef, boolean ownsHandle) {
        return NativeObject.objectFor(ptr, Structure.class, needRef, ownsHandle);
    }
    //--------------------------------------------------------------------------
    protected void disposeNativeHandle(Pointer ptr) {
        gst.gst_structure_free(ptr);
    }
    
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

