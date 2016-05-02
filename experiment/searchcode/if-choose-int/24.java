package management_user.account.view;

import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import management_user.account.controller.AccountUserController;
import management_user.bean.Course;
import management_user.bean.User;

public class AccountUserView {

	//显示选择列表
	public void displayMenu(String []aSelectList) {
		System.out.println("\n*****************");
		for (String select : aSelectList) {
			   System.out.println(select);
		}
		System.out.print("请选择操作对应的序号:");
	}
	
	//接收用户选择 
	public int receiveSelect(String []aSelectList) {
		Scanner in = new Scanner(System.in);
		int choose = 0;
		
		//接受用户选择
		while(true){
			boolean pass=true;
			try {
				choose = in.nextInt();
				if( !(choose > 0 && choose <= aSelectList.length) ) {
					pass=false;
					System.out.print("请输入1-"+aSelectList.length +"的正整数字:");
				}
			}
			catch (InputMismatchException e) {
				pass=false;
				System.out.print("请输入一个正整数字:");
				in.next();	
			}
			if(pass) {
				break;
			}
		}
		return choose;	
	}
	
	//接受用户名和密码
	public User receiveUser(String aPart) {
		
		/* 
		 * 规定: 1.  ALL 接受全部
		 * 		2.  NAME 只接受用户名
		 * 		3.  PASSWORD  只接受密码
		 * 
		 */
		if( !aPart.equals("ALL") && !aPart.equals("NAME") && !aPart.equals("PASSWORD") ) {
			System.out.println("参数错误!");
			return null;
		}
		
		User aUser = new User();
		Scanner in = new Scanner(System.in);
		
		if( aPart.equals("ALL") || aPart.equals("NAME") ) {
			String aUserName = new String();
			System.out.print("请输入用户名:");
			aUserName = in.nextLine();
			aUser.setUserName(aUserName);
		}
		
		if( aPart.equals("ALL") || aPart.equals("PASSWORD")) {
			String aPassword = new String();
			System.out.print("请输入密码:");
			aPassword = in.nextLine();
			aUser.setUserPwd(aPassword);
		}
		
		return aUser;
	}
	
	public void displayInfo(String aInfo) {
		System.out.println(aInfo);
	}
	
	public void displayFeedBack(int aFeedBackCode,String aUserName) {
		if( -5 == aFeedBackCode ) {
			System.out.println(aUserName + "用户(名)已存在!");
		}
		else if (5 == aFeedBackCode ) {
			System.out.println(aUserName + "用户(名)不存在!");
		}
		else if( -4 == aFeedBackCode ) {
			System.out.println(aUserName + "用户密码错误!");
		}
		else if( -3 == aFeedBackCode ) {
			System.out.println(aUserName + "用户已登录!");
		}
		else if( -2 == aFeedBackCode ) {
			System.out.println(aUserName + "注册失败!");
		}
		else if( 2 == aFeedBackCode ) {
			System.out.println(aUserName + "注册成功!");
		}
		else if( -1 == aFeedBackCode ) {
			System.out.println(aUserName + "登录失败!");
		}
		else if( 1 == aFeedBackCode ) {
			System.out.println(aUserName + "登录成功!");
		}
		else if( aFeedBackCode == -50 ) {
			System.out.println(aUserName + "登出成功,但好像有点问题!");
		}
		else if( aFeedBackCode == 50 ) {
			System.out.println(aUserName + "安全登出成功!");
		} 
	}
	
	//显示对课程的可用操作
	public int displayCourseMenu(String aUserName) {
		String selectList[] = new String[]{"1.显示所有课程","2.增加或更新课程","3.查询课程","4.删除课程","5.退出登录"};
		System.out.println("------------------");
		for (String select : selectList) {
			   System.out.println(select);
		}
		System.out.print("请选择操作对应的序号:");
		
		int courseChoose = receiveSelect(selectList);
		
		return courseChoose;
	}
	
	//显示课程信息,(所有，单科)
	public void displayCourseList(List<Course> aUserCourseList) {
		if( aUserCourseList == null ) {
			System.out.println("列表为空!");
			return;
		}
		for(Iterator<Course> i = aUserCourseList.iterator(); i.hasNext();) {
			Course courseRef = i.next();
			System.out.println(courseRef.getCourseName() + "\t" + courseRef.getCourseScore());
		}
		System.out.println("Over!");
	}
	
	//更新/修改成绩后的提示
	public void displayCourseFeedBack(int aFeedBackCode,String aUserName) {
		if( aFeedBackCode == -88) {
			System.out.println(aUserName + "用户未登录，无法修改成绩信息!");
		}
		else if( aFeedBackCode == 88 ) {
			System.out.println(aUserName + "更新课程成功!");
		}
		else if( aFeedBackCode == 89) {
			System.out.println(aUserName + "添加课程成功!");
		}
		else if( aFeedBackCode == -60 ) {
			System.out.println(aUserName + "删除改课程失败!");
		}
		else if( aFeedBackCode == 60 ) {
			System.out.println(aUserName + "删除改课程成功!");
		}
	
	}
	
	//接收一个课程信息
	public Course receiveCourse() {
		
		String aCourseName = new String();
		float aCourseScore = 0;
		Scanner in = new Scanner(System.in);
		
		System.out.print("请输入要添加/更新的课程名:");
		aCourseName = in.nextLine();
		
		System.out.print("请输入课程分数:");
		
		while(true){
			boolean pass=true;
			try {
				aCourseScore = in.nextFloat();
				if( !(aCourseScore >= 0 && aCourseScore <= 100) ) {
					pass=false;
					System.out.print("请输入1-100的数字:");
				}
			}
			catch (InputMismatchException e) {
				pass=false;
				System.out.print("请输入一个正数字:");
				in.next();	
			}
			if(pass) {
				break;
			}
		}
		return new Course(aCourseName,aCourseScore);
	}
	
	//接收一个课程名
	public String receiveCourseName() { 
		System.out.print("请输入课程名称:");
		
		Scanner in = new Scanner(System.in);
		
		//暂未进行课程名合法性检验
		return in.nextLine();
	}
	public static void main(String[] args) {
		String selectList[] = new String[]{"1.登录","2.注册","3.检查","4.退出"}; 
		
		AccountUserView aView = new AccountUserView();
		User aUser = new User();
		int choose = 0;
		
		AccountUserController aControl = new AccountUserController();
		
		while (true) {
			//打印选择列表
			aView.displayMenu(selectList);
			
			//判断用户是否主动要求退出
			if( selectList.length == (choose = aView.receiveSelect(selectList))) {
				break;
			}
						
			if( choose == 1 ) {
				//接受一个用户信息
				aUser = aView.receiveUser("ALL");
				
				//对登录用户的密码加密后传向C层
				aUser.md5AndKlForPwd();
				aControl.OperController(choose, aUser);
			}
			else if( choose == 2 ) {
				//接受一个用户信息
				aUser = aView.receiveUser("ALL");
				
				//对注册用户名和密码进行正则匹配
				if( !aUser.regexUserName() || !aUser.regexUserPwd() ) {
					System.out.println("注册失败!");
					continue;
				}
				
				//用户名和密码符合标准后对密码加密传向C层
				aUser.md5AndKlForPwd();
				aControl.OperController(choose, aUser);
			}
			else if( choose == 3 ) {
				//只接收一个用户名
				aUser = aView.receiveUser("NAME");
				
				//将检查用户（pwd==null)信息传向C层
				aControl.OperController(choose, aUser);
			}
		}
		
		System.out.println("谢谢使用!");
	}

}


