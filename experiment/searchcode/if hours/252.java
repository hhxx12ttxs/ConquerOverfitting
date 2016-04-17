
public class Employee {
		private String name;
		private Task currentTask;
		private double hoursLeft;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			if (name != null) {
			this.name = name;
			}
		}

		public Task getCurrentTask() {
			return currentTask;
		}

		public void setCurrentTask(Task currentTask) {
			if (currentTask != null) {
			this.currentTask = currentTask;
			}
		}

		public double getHoursLeft() {
			return hoursLeft;
		}

		public void setHoursLeft(double hoursLeft) {
			if (hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
			}
		}

		Employee(String name) {
			if (name != null) {
				this.name = name;
			}
		}
		public void work () {
			if (this.currentTask != null) {
				if (this.hoursLeft > this.getCurrentTask().getWorkingHours()) {
					this.hoursLeft = this.hoursLeft - this.getCurrentTask().getWorkingHours();
					this.getCurrentTask().setWorkingHours(0);
				}
				if (this.hoursLeft < this.getCurrentTask().getWorkingHours()) {
					this.getCurrentTask().setWorkingHours(this.getCurrentTask().getWorkingHours()
							- this.hoursLeft);
					this.hoursLeft = 0;		
				}
				if (this.hoursLeft == this.getCurrentTask().getWorkingHours()) {
					this.getCurrentTask().setWorkingHours(0);
					this.hoursLeft = 0;		
				}
				showReport ();
			}
		}
		public void showReport () {
			System.out.println("Name: " + this.name);
			System.out.println("Task: " + this.currentTask.getName());
			System.out.println("Employee working hours left: " + this.hoursLeft);
			System.out.println("Time left to finish task " + this.getCurrentTask().getWorkingHours());
		}
}

