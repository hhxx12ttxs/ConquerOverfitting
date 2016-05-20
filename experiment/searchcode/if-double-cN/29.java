package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.login.zidingyi.excel.LDAPPeopleUtil;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;

public class SendGZmessage extends DOAbstractAction {

	

	public String excute() {
		
		// ??????????
		String manager_email = "uii2008@sohu.com";// ???????;
		String emailTo = null;// ????? ;
		StringBuffer noEmail = new StringBuffer();// ???????;
		String name = null; // ??????;
		String emailName = null; // ?????;
		String eamilSelf = null;//???????;
		int countAll = 0;
		int countSend = 0;
		int countFail = 0;
		// ?????
		Date resmonth = null;
		String resname = null;
		
		Connection conn = MySqlOperation.getConnection();
		List users = new ArrayList();

		try {
			users = service.invokeSelect();
			// String user = service.invokeSelectGetAValue();
			// users.add(user);

		} catch (Exception e) {
			this.setEchoValue("??????????error" + e.toString());
			return "notpass";
		}

		// ????????
		if (users != null && users.size() > 0) {
			String s = users.get(0).toString();

			String st = s.substring(s.indexOf("{") + 1, s.lastIndexOf("}"));
			String[] sarray = st.split(",");
			ResultSet rs = null;

			// ?????????????????
			for (int i = 0; i < sarray.length; i++) {
				String temp = sarray[i];
				String[] nv = temp.split("=");
				if (nv.length == 2 && "month".equals(nv[0].trim())) {
					Date month = Date.valueOf(nv[1]);
					resmonth = month;
				}
				if (nv.length == 2 && "name".equals(nv[0].trim())) {
					resname = nv[1];
				}

			}
			
			System.out.println("resmonth===================="
					+ resmonth);
			System.out.println("resname===================="
					+ resname);

			// ????
			try {
				if (resmonth != null && (resname == null || resname.length() <= 0)) {
					rs = MySqlOperation.SMfindByDate(conn, resmonth);
				} else if (resmonth == null && resname != null
						&& resname.length() > 0) {
					rs = MySqlOperation.SMfindByName(conn, resname);
				} else if (resmonth != null && resname != null
						&& resname.length() > 0) {
					rs = MySqlOperation.SMfindByNameAndDate(conn, resname,
							resmonth);
				} else if (resmonth == null
						&& (resname == null || resname.length() <= 0)) {
					this.setEchoValue("??????");
					return "notpass";
				}

				// ????????
				if (rs != null) {
					// month, name, basesalary, buckshee, rentdeduct,
					// leavededuct, 6
					// factsalary, payyanglaoinsure, payshiyeinsure,
					// payyilaioinsure, 4
					// payshebaofee, payhousingsurplus, taxbefore, tax,
					// taxafter, remark 6

					
					while (rs.next()) {

						SalaryMessage sm = new SalaryMessage();
						sm.setObjuid(rs.getString("objuid"));
						sm.setMonth(rs.getDate("month"));
						sm.setName(rs.getString("name"));
						sm.setBasesalary(rs.getDouble("basesalary"));
						sm.setBuckshee(rs.getDouble("buckshee"));
						sm.setRentdeduct(rs.getDouble("rentdeduct"));
						sm.setLeavededuct(rs.getDouble("leavededuct"));
						sm.setFactsalary(rs.getDouble("factsalary"));
						sm.setPayyanglaoinsure(rs
								.getDouble("payyanglaoinsure"));
						sm.setPayshiyeinsure(rs.getDouble("payshiyeinsure"));
						sm.setPayyilaioinsure(rs.getDouble("payyilaioinsure"));
						sm.setPayshebaofee(rs.getDouble("payshebaofee"));
						sm.setPayhousingsurplus(rs
								.getDouble("payhousingsurplus"));
						sm.setTaxbefore(rs.getDouble("taxbefore"));
						
						sm.setTaxget(rs.getDouble("taxget"));
						sm.setTaxlv(rs.getString("taxlv"));
						sm.setTaxrm(rs.getDouble("taxrm"));

						sm.setTax(rs.getDouble("tax"));
						sm.setTaxafter(rs.getDouble("taxafter"));
						sm.setRemark(rs.getString("remark"));

						/**
						 * ??????
						 */
						
						
						// ????
						StringBuffer content = new StringBuffer();
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy?MM?");
						String stMonth = format.format(sm.getMonth());
						
						String title = sm.getName()+ " " + stMonth + " ?????";
						// ????
						
						content.append("??:\t\t\t" + stMonth + "\n??:\t\t\t"
								+ sm.getName() + "\n");
						content.append("???:\t\t" + sm.getBasesalary()
								+ "\n??:\t\t\t" + sm.getBuckshee() + "\n");
						content.append("????:\t\t" + sm.getRentdeduct()
								+ "\n???????:\t" + sm.getLeavededuct() + "\n");
						content.append("???:\t\t" + sm.getFactsalary()
								+ "\n????????:\t" + sm.getPayyanglaoinsure()
								+ "\n");
						content.append("????????:\t" + sm.getPayshiyeinsure()
								+ "\n????????:\t" + sm.getPayyilaioinsure()
								+ "\n");
						content.append("????????:\t" + sm.getPayshebaofee()
								+ "\n?????????:\t" + sm.getPayhousingsurplus()
								+ "\n");
						content.append("????:\t\t" + sm.getTaxbefore()
								+ "\n????G=F-2000:\t" + sm.getTaxget() + "\n");
						
						content.append("??H:\t\t" + sm.getTaxlv()
								+ "\n????\t\t" + sm.getTaxrm()
								+ "\n?:\t\t\t" + sm.getTax() + "\n");
						
						
						content.append("????:\t\t" + sm.getTaxafter()
								+ "\n??:\t\t\t" + sm.getRemark() + "\n");

						content
								.append("\n\t????????(???????)?\nhttp://127.0.0.1:8080/yiyi/allsm?uid="
										+ sm.getObjuid());
						String contentText = content.toString();
						System.out.println(contentText);
						/**
						 * // ??????;
						 */

						// ???????????????
						name = sm.getName();
						
						try {
							eamilSelf = MySqlOperation.findEmailByName(conn, name);
							
							//???????
							if (eamilSelf != null && eamilSelf.length() > 0) {
								emailTo = eamilSelf.trim();
							} else {//?????????
								emailName = MySqlOperation.findTonameByName(conn, name);
								if (emailName != null && emailName.length() > 0) {
									emailTo = LDAPPeopleUtil
											.getLDAPEmailBySN(emailName);
								} else {
									//????????
									emailTo = LDAPPeopleUtil.getLDAPEmailByCN(name);
								}
							}
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// month, name, basesalary, buckshee, rentdeduct,
						// leavededuct, 6
						// factsalary, payyanglaoinsure, payshiyeinsure,
						// payyilaioinsure, 4
						// payshebaofee, payhousingsurplus, taxbefore, tax,
						// taxafter, remark 6
						// /????
						countAll++;
						if (emailTo == null || emailTo.trim().length() <= 0) {
							countFail++;
							String tsname = "";
							String addname = "[" + name + "]";
							if(noEmail != null)
								tsname = noEmail.toString();
							if(tsname.contains(addname)){
								//????????
							}else if(noEmail == null || noEmail.length() <= 0)
								noEmail.append("??[" + name + "], ");
							 else
								noEmail.append("[" + name + "],");
						} else {
							try {
								String password = "yyfxyxx2008";
								System.out.println("$$$$$??????????$$$$$$");
								System.out.println("????======================"
										+ manager_email);
								System.out.println("?????===================="
										+ name);
								System.out.println("?????===================="
										+ emailName);
								System.out.println("????======================"
										+ emailTo);
								System.out.println("??????=================="
										+ "yuanxx@zephyr.com.cn");
								System.out
										.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
								countSend++;
								sendEmail(manager_email, password,
										"yuanxx@zephyr.com.cn", title,
										contentText);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			this.setEchoValue("??????");
			return "notpass";
		}

		try {
			if (conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (noEmail != null && noEmail.length() > 0) {
			noEmail.append("??????????????????");
			if(countSend > 0)
				this.setEchoValue("??????????" + countSend + "????\n" + "??" + countFail + "?" + noEmail);
			else
				this.setEchoValue("???????\n" + "?" + countFail + "?" + noEmail);
			
			return "notpass";
		} else {
			this.setEchoValue("??????????" + countSend + "????\n");
			return "notpass";
		}
	}

	// ????
	public static void sendEmail(String from, String password, String to,
			String title, String text) throws AddressException,
			MessagingException {

		// **************************************************8
		// ???
		//to = "yuanxx@zephyr.com.cn";
		// *****************************************************8

		String smtpHost = "smtp." + from.substring(from.lastIndexOf("@") + 1);

		// System.out.println("$$$$$$$$$$LoginActionLDAP()$$$$$$$$$$$$" + from +
		// "===" + password + "$$$$$$$$$$$$$$$$$$$$$$$4");
		// System.out.println("$$$$$$$$$$$$$$$????????$$$$$$$$$$$$$$4");
		// System.out.println("???????==========================" + from);
		// System.out.println("???????==========================" + to);
		// System.out.println("???????????==========================" +
		// smtpHost);
		// System.out.println("????==========================" + title);
		// System.out.println("????==========================" + text);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4");

		final Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");

		Session myMailSession = Session.getInstance(props);
		myMailSession.setDebug(true); // ??DEBUG??
		Message msg = new MimeMessage(myMailSession);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		msg.setContent("I have a email!", "text/plain");
		msg.setSentDate(new java.util.Date());
		msg.setSubject(title);
		msg.setText(text);
		System.out.println("1.Please wait for sending two...");

		// ????
		Transport myTransport = myMailSession.getTransport("smtp");
		myTransport.connect(smtpHost, from, password);
		myTransport.sendMessage(msg, msg
				.getRecipients(Message.RecipientType.TO));
		myTransport.close();
		// javax.mail.Transport.send(msg); // ???????
		System.out.println("2.Your message had send!");
	}

	public static Double castDouble(String value) {
		Double number = 0.00;
		if (value != null && value.trim().length() > 0
				&& value.matches("^\\d+.\\d+|\\d+$")) {
			number = Double.parseDouble(value);
		}
		System.out.println(number);
		return number;
	}

	public static void main(String[] args) {
		SendGZmessage sg = new SendGZmessage();
		try {
			sg.sendEmail("uii2008@sohu.com", "yyfxyxx2008", "yuanxxasdfasdf@zephyr.com.cn", "test", "testast");
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

