package Adapter;

/**
 * Adapter là mẫu tạo ra 1 bộ phận trung gian giữa cái cũ và cái mới, để cái cũ cũng lắp vào cái mới và dùng dc
 * Ví dụ khi bạn đang có 1 hệ thống chạy ổn(frontend-backend) và bạn phải thay backend mới
 * khi đó, bạn phải dùng adapter (frontend-adapter-new backend)
 * 
 * Adapter pattern lets you fix the interface between objects and class without having to modify
 * the objects or classes directly. By converting the current interface to another interface
 * 
 * We use this pattern if some parts of software is set to be unchanged, and other parts always change.
 * 
 * @author HP Elitebook
 *
 */
public class TestClass {

	public static void main(String args[]){
		AceClass aceObj=new AceClass();
		aceObj.setName("Adam Lambert");
		AceToAcmeAdapter adapter=new AceToAcmeAdapter(aceObj);
		//Here we can use the Acme function, which is connected to ace object
		System.out.println("Customer’s first name: " +
				adapter.getFirstName());
		System.out.println("Customer’s last name: " +
				adapter.getLastName());
	}
}

