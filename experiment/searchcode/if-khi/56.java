package Strategy;

public class TestClass {

	/**_Mục đích: Chuyển đổi từ is-a sang has-a 
	 * _Ví dụ: Bạn có 1 cái Vehicle, nó có hàm go() để "chạy",
	 * Bạn cần tạo ra 1 cái Boat, nó cũng go(), vậy là extends Vehicle.
	 * Bạn cần tạo Boat2, cũng go(), lại extends Boat...
	 *..Có thể có quá nhiều lớp khác nhau liên quan cùng có hàm go(), chỉ khác ở cách xử lý..
	 * Vậy tách riêng bọn thuật toán đó ra, người dùng có thể tùy nghi lựa chọn thuật toán cần dùng
	 *
	 *_Vấn đề ko hiểu: Vì sao ko tạo nhiều class con rồi override hàm go(), như thế thì có sao? 
	 *
	 *Vì thực ra 2 vấn đề là hoàn toàn khác nhau, inheritance là is-a, tức là 1 thứ này là 1 thứ khác, rất lằng nhằng quan hệ cha con
	 *tuy nhiên với has-a, bất cứ ai cũng có thể có năng lực đó.
	 *Tùy theo cách suy nghĩ mà thôi
	 *ví dụ, tự nhiên VehicleCar và VehiclePlane có hàm runOnLand(),bạn ko thể vứt hàm này vào Vehicle được vì VehicleBoat is-a Vehicle, mà nó thì ko thể runOnLand()
	 *vậy nên bạn chỉ có thể tạo ra 1 class chứa hàm runOnLand(), và ai cũng có thể sở hữu runOnLand() đó.
	 *Mở rộng ra cho Strategy, thì runonLand() có thể được cài đặt khác nhau, và VehicleCar với VehiclePlane có thể cùng gọi runOnLand()
	 *nhưng cách thực thi sẽ khác nhau
	 *
	 *Áp dụng của Strategy thường là ở thời gian thực, khi save file thì cùng 1 hàm save(), nhưng  save file theo định dạng gì thì tùy chọn 
	 * @param args
	 */
	public static void main(String[] args) {
		/*This is for testing each child with specific go function
		 
		VehicleBoat boat=new VehicleBoat();
		boat.go();
		VehicleCar car=new VehicleCar();
		car.go();
		VehiclePlane plane =new VehiclePlane();
		plane.go();
		*/
		/*
		 * You can apply functions when running application(which could not be done using inheritance)
		 * So, you can see that the boat now can fly and surf
		 */
		Vehicle myVehicle=new Vehicle();
		myVehicle.setGoAlgorithm(new GoAlgorithmSurfing());
		myVehicle.go();
		//If you want it to fly, it turns to be a plane
		myVehicle.setGoAlgorithm(new GoAlgorithmFly());
		myVehicle.go();
	}

}

