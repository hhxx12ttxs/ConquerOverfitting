package filefortest;

public class FileForTestMethodCollect {
	public boolean test11(){
		return false;
	}
	
	
	private String test12(){
		return "";
	}

	public static boolean test13(int a){
		return true;
	}

	public void test14(){
		return;
	}

	private void test15(){
		Out anonyInter=new Out(){// 获取匿名内部类实例

			void show(){//重写父类的方法
				System.out.println("this is Anonymous InterClass showing.");
			}
		};
		anonyInter.show();// 调用其方法
	}
}

class Out{
	void show(){
		System.out.println("this is Out showing.");
	}
}

