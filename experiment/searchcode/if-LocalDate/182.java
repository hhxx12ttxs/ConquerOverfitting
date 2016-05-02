package controller;

import helpers.CalendarHelper;
import helpers.Settings;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import model.TaskModel;
import model.XmlTaskModel;

import org.joda.time.LocalDate;

import resource.theme.ThemeResources;
import view.CalendarView;
import view.Menu;




public class MainController {
	
	JFrame mainFrame = new JFrame();
	
	private Menu menu;
	private CalendarView view;
	private XmlTaskModel model;

	public MainController() {
		menu = new Menu();
		mainFrame.setJMenuBar(menu.getMenubar());
		
		//BUGG nr man skapar fler n ett event card och flyttar dem d lggs  de p varandra
		
		new ThemeResources();
		model = new XmlTaskModel();

		LocalDate date = new LocalDate();
		
		int week = date.getWeekOfWeekyear();		
		CalendarHelper.setThisWeek(week);
		int year =  date.getWeekyear(); 

		
		CalendarHelper.setDate(date);

		//System.out.println("week num:"+week + "  year: "+year);
		//System.out.println("date:"+CalendarHelper.getDate().toString());
		//System.out.println("day:"+CalendarHelper.getDate().getDayOfMonth());

		/*for(int i = 0;  i < 5; i++){
			TaskModel tmp = new TaskModel("title"+i, "content"+i,"location"+i,"category"+i,CalendarHelper.getDate().getDayOfMonth()+i,week,"month"+i,year,1800+i,1830+i);
			model.addTask(tmp);
		}*/


		ArrayList<TaskModel> list =  model.getTasks(year,week);

		//System.out.println("num tasks:"+list.size());

		Iterator<TaskModel> iter = list.iterator();
		while(iter.hasNext()) {
			TaskModel tmp = iter.next();
			//System.out.println("Year:"+tmp.getYear());
			//System.out.println("week:"+tmp.getWeekNum());
			//System.out.println("day:"+tmp.getDay());
		}

		view = new CalendarView(list,model.getId());

		
		/*
		 * - get current week number
		 * - CalendarHelper.setWeekNumber(number); , CalendarHelper.getWeekNumber();
		 * 
		 * - get all tasks corresponding to current week number, pseudo: ArrayList tasks = model->getTasks(weekNumber);
		 * - send arraylist with task to CalenderView(), pseudo: view = new CalendarView(tasks);	
		 *
		 *
		 */
		new Actions(view,model,menu,mainFrame);
		
		mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	System.out.println("Windows is closing");
            	//XmlTaskModel.storeIdToFile(view.getId());
            }
        });
		
	
		
		
		mainFrame.add(view);
		
		mainFrame.setMinimumSize(new Dimension(640,480));
		mainFrame.setPreferredSize(new Dimension(Settings.getFrameWidth(),Settings.getFrameHeight()));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();		
		mainFrame.setVisible(true);
	}

	
	public static void main(String[] args) {

		// If Mac OS, set the menu accordingly.
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JEEh-Calendar");
		} 
		
		try {
			// Set the look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
		} catch (IllegalAccessException ex) {
		} catch (InstantiationException ex) {
		} catch (ClassNotFoundException ex) {
		}
		
		//Only english in this application
		//Locale.setDefault(Locale.ENGLISH);
		JComponent.setDefaultLocale(Locale.ENGLISH);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new MainController();
			}
		});



	}
}

