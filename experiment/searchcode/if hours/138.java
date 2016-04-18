

	public class Task {
		private String name;
		private int workingHours;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			if (name != null)
				this.name = name;
		}

		public int getWorkingHours() {
			return this.workingHours;
		}

		public void setWorkingHours(int workingHours) {
			if (workingHours >= 0)
				this.workingHours = workingHours;
		}

		public Task(String name,int workingHours) {
			if(name != null && name != "") {
				this.name = name;
			}
			else {
				this.name="Undefined Task";
			}
			this.setWorkingHours((int) (Math.random() * 20));
		}

	}


