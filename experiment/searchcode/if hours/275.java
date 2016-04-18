package ca.ulaval.glo4003.model.stats;

public class WorkStatDTO {
	double hours;
	double cost;

	public WorkStatDTO() {
		super();
		hours = 0.0;
		cost = 0.0;
	}

	public double getHours() {
		return hours;
	}

	public double getCost() {
		return cost;
	}

	public void addWork(double hours, double cost) {
		this.hours += hours;
		this.cost += cost;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		WorkStatDTO ws = (WorkStatDTO) obj;
		return this.hours == ws.hours && this.cost == ws.cost;
	}
}

