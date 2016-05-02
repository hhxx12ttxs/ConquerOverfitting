package Controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.jws.WebParam.Mode;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.TableView.TableCell;

import org.eclipse.persistence.exceptions.EclipseLinkException;

import com.mysql.jdbc.TimeUtil;

import Model.EventHolder;
import Model.Model;
import Model.MyUser;
import View.Config;
import View.Friend;
import View.ChooserPanel;
import View.IntroSplash;
import View.Login;
import View.MainContainer;
import View.MyCalendar;
import View.Register;
import View.UserEvent;
import WEB.User;

public class Controller {

	private MainContainer frame;
	private MyCalendar calendar;
	private Login login;
	private UserEvent event;
	private Register register;
	private ChooserPanel choose;
	private Friend friend;
	private Date dateWhen;
	private IntroSplash intro;

	public Controller(MainContainer main) {
		frame = main;
		intro = new IntroSplash();
		login = new Login();

		System.out.println("Before Show:");
		frame.showPane(intro);

		Thread t = new Thread(intro);
		t.start();
		try {
			TimeUnit.SECONDS.sleep(4);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("After Timer:");
		frame.showPane(login);
		login.addListener(new LoginListener());

	}

	// ////LISTENER CLASSES////////////

	private class LoginListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source == login.getMyButton_Enter()) {
				class MyWorker extends SwingWorker<Object, Object> {

					@Override
					protected Object doInBackground() throws Exception {

						String name = login.getLogin();
						String pass = login.getPass();

						if (name.isEmpty() || pass.isEmpty()) {

							System.out.println("Please, enter data in filds");
							InfoBox.BOX
									.alert(frame,
											"Please, enter email and passowrd in filds");
							login.getTxt().showError();
							login.getPin().showError();
						} else {
							if (!name.contains("@")) {
								System.out.println("Incorect email");
								InfoBox.BOX.alert(frame,
										"Please, enter CORECT email  fild");
								login.getTxt().showError();
							} else {

								login.getProgressBar().setVisible(true);
								login.getProgressBar().setIndeterminate(true);

								if (Model.MODEL.doLogIn(name, pass)) {

									initChoose();
									frame.showPane(choose);

								} else {
									login.getProgressBar().setVisible(false);
									login.getTxt().showError();
									login.getPin().showError();
								}

							}
						}
						return null;
					}

					@Override
					protected void done() {
						login.clearFields();
						login.getProgressBar().setVisible(false);
					}
				}
				new MyWorker().execute();
			}

			if (source == login.getMyButton_Cancel()) {
				if (InfoBox.BOX.exit(frame, "Are you shure") == 0) {
					Model.MODEL.doLogOut();
					Model.MODEL.doCloseConnection();
					System.exit(0);
				}
			}
			if (source == login.getBtnRegister()) {
				login.clearFields();
				initRegister();
				frame.showPane(register);

			}

		}
	}// end of LoginListener

	private class CalendarListner implements MouseListener, ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();

			if (source == calendar.getBtnBack()) {
				initChoose();
				frame.showPane(choose);

			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() == 2) {

				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				int column = target.getSelectedColumn();

				System.out.println("Found, column: " + column + " row: " + row);
				// need to rennder chosen cell

				dateWhen = new java.util.Date(0);

				Integer day = (Integer) target.getValueAt(row, column);
				// System.out.println("Day"+day);
				dateWhen.setDate(day);

				int month = Config.getIndex(Config.MONTHS, calendar
						.getLblMonth().getText());
				dateWhen.setMonth(month);
				// System.out.println("month"+month);
				// and now time for magic number
				Integer magic = 1900;
				Integer year = Integer.parseInt((String) calendar.getCmbYear()
						.getSelectedItem());
				year = year - magic;
				// System.out.println("year "+year);
				dateWhen.setYear(year);
				// END of MAGIC

				System.out.println("Choosen date: " + dateWhen);

				initEvent(dateWhen);
				frame.showPane(event);

			}

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

	}// END CalendarListner

	private class UserEventListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			event.getProgressBar().setVisible(true);
			if (source == event.getBtnCancel()) {
				event.clearFileds();
				frame.showPane(calendar);
			}

			if (source == event.getBtnSave()) {
				class MyWorker extends SwingWorker<Object, Object> {

					@Override
					protected Object doInBackground() throws Exception {

						event.getProgressBar().setVisible(true);
						event.getProgressBar().setIndeterminate(true);

						EventHolder eh = getDataToSend();
						Model.MODEL.doCreateEvent(eh);
						initCalendar();
						frame.showPane(calendar);
						return null;
					}

					protected void done() {
						event.clearFileds();
						event.getProgressBar().setVisible(false);
					}

				}
				new MyWorker().execute();

			}

		}

		private EventHolder getDataToSend() {

			String title = event.getTxtName().getText();
			String where = event.getTxtWhere().getText();
			int when = event.getCmbbxWhen().getSelectedIndex();
			System.out.println(when+"S1");
			String desc = event.getTxtDescription().getText();
			boolean isEmail = event.getChckbxEmail().isSelected();
			boolean isSms = event.getChckbxSms().isSelected();
			Date date = event.getDateEvent();
			System.out.println(date+" EventHoleder");
			int remind = event.getComboBox().getSelectedIndex();

			ArrayList<User> users = (ArrayList<User>) Model.MODEL
					.doGetListPeolpleByEmail(event.getSelected());

			EventHolder eh = new EventHolder(title, when, where, desc, isEmail,
					isSms, users, date, remind, Model.MODEL.getCURRENT_LOGIN());

			return eh;

		}

	}// END UserEventListener

	private class RegisterListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();

			if (source == register.getBtnCancel()) {

				register.clearFilds();
				if (Model.MODEL.isLoginIn()) {
					Model.MODEL.doLogOut();
				}

				initLogin();
				frame.showPane(login);

			}

			if (source == register.getBtnSave()) {

				if (register.isAllowToRegister()) {

					class MyWorker extends SwingWorker<Object, Object> {
						@Override
						protected Object doInBackground() throws Exception {
							register.getProgressBar().setVisible(true);
							register.getProgressBar().setIndeterminate(true);

							String name = register.getTxtLogin().getText();
							String pass = register.getTxtConfirmPass()
									.getText();

							String mail = register.getTxtEmail().getText();
							String phone = register.getTxtPhone().getText();
							MyUser user = new MyUser(name, pass, mail, phone);

							if (Model.MODEL.doRegisterNewOne(user)) {

								initLogin();

								frame.showPane(login);

							} else {
								System.out.println("Already exists");
							}
							return null;
						}

						@Override
						protected void done() {
							register.clearFilds();
							register.getProgressBar().setVisible(false);
						}
					}
					new MyWorker().execute();

				} else {

					System.out.println("You edit incorect information");
				}
			}

		}
	}// END RegisterListener

	private class ChooseListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();

			if (source == choose.getBtnEvent()) {

				initCalendar();
				frame.showPane(calendar);
			}

			if (source == choose.getBtnBack()) {
				Model.MODEL.doLogOut();
				initLogin();
				frame.showPane(login);
			}

			if (source == choose.getBtnFriends()) {
				class MyWorker extends SwingWorker<Object, Object> {

					@Override
					protected Object doInBackground() throws Exception {

						choose.getProgressBar().setVisible(true);
						choose.getProgressBar().setIndeterminate(true);

						initFriend();
						frame.showPane(friend);

						return null;
					}

					protected void done() {
						choose.getProgressBar().setVisible(false);
					}

				}
				new MyWorker().execute();
			}

		}

	}// END ChooseListener

	private class FriendsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();

			if (source == friend.getBtnBack()) {
				frame.showPane(choose);
			}

			if (source == friend.getBtnMakeFriendship()) {

				class MyWorker extends SwingWorker<Object, Object> {
					@Override
					protected Object doInBackground() throws Exception {
						friend.getProgressBar().setVisible(true);
						friend.getProgressBar().setIndeterminate(true);

						if (friend.getList().getSelectedIndices().length != 0) {
							Model.MODEL.doMakeFriendShip(friend
									.getSelectedMails());
						}

						return null;
					}

					@Override
					protected void done() {
						friend.getProgressBar().setVisible(false);

					}
				}
				new MyWorker().execute();
				InfoBox.BOX.info(frame, "You add friend");
				initCalendar();
				frame.showPane(calendar);
			}

		}

	}// END FriendsListener

	// /// INITIALIZE /////////
	private void initLogin() {
		if (login == null) {
			login = new Login();
			login.addListener(new LoginListener());
		}
	}

	private void initChoose() {

		if (choose == null) {
			choose = new ChooserPanel();
			choose.addListener(new ChooseListener());
		}
	}

	private void initFriend() {

		if (friend == null) {
			friend = new Friend();
			friend.addListener(new FriendsListener());
		}
		friend.setListModel();

	}

	private void initCalendar() {
		if (calendar == null) {
			calendar = new MyCalendar();
			calendar.addListener(new CalendarListner());
			calendar.addMyActionListener(new CalendarListner());
		}
	}

	private void initEvent(Date when) {

		// System.out.println("Choosen 1 date: " + when);
		if (event == null) {
			// System.out.println("Choosen 2  date: " + when);
			event = new UserEvent(when);
			event.addListener(new UserEventListener());
		}
		event.setListModel();

	}

	private void initRegister() {

		if (register == null) {
			register = new Register();
			register.addListener(new RegisterListener());
		}
	}

}

