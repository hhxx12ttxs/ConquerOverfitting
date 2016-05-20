// CatfoOD 2010-1-10 下午06:33:02

package trayicon;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Log extends Frame {
	
	private static final long serialVersionUID = 4339591968023632926L;
	
	private static final int W = 280;
	private static final int H = 350;
	private static final String NULLTEXT = "";
	private static final Log thiz = new Log();
	
	public static void display() {
		thiz.setVisible(true);
	}
	
	public static void p(Object o) {
		thiz.text.append(o + "");
	}
	
	public static void pl(Object o) {
		p(o + "\n");
	}
	
	private TextArea text;
	private Button close;
	private Button clear;
	
	private Log() {
		super("日志");
		setSize(W, H);
		move();
		setVisible(false);
		setLayout(new BorderLayout());
		addWindowListener(WAP);
		
		add(getText(),		BorderLayout.CENTER);
		add(getButtons(),	BorderLayout.SOUTH);
	}
	
	private void move() {
		Dimension dim = getToolkit().getScreenSize();
		int x = dim.width - W;
		int y = dim.height - H - 50;
		this.setLocation(x, y);
	}
	
	private TextArea getText() {
		if (text==null) {
			text = new TextArea();
			text.setEditable(false);
		}
		return text;
	}
	
	private Button getClose() {
		if (close==null) {
			close = new Button("关闭");
			close.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return close;
	}
	
	private Panel getButtons() {
		Panel p = new Panel();
		p.setLayout(new GridLayout(1, 2));
		p.add(getClear());
		p.add(getClose());
		return p;
	}
	
	private Button getClear() {
		if (clear==null) {
			clear = new Button("清除");
			clear.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					text.setText(NULLTEXT);
				}
			});
		}
		return clear;
	}
	
	private WindowAdapter WAP = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			setVisible(false);
		}
	};
}

