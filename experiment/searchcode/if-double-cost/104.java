package project10;

/**
 * expense superclass
 * 
 * @author Hunter Morgan
 */
public abstract class Expense {
	/**
	 * cost associated with expense
	 */
	double	cost;

	/**
	 * expense type string for iteration logic
	 */
	String	type;

	/**
	 * basic constructor prototype
	 * 
	 * @param type
	 *            type to put in type field
	 */
	public Expense(String type) {
		this.type = type;
	}

	/**
	 * cost mutator
	 * 
	 * @param cost
	 *            cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * accessor for expense type string
	 * 
	 * @return expense type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * cost accessor
	 * 
	 * @return cost cost gotten
	 */
	public double getCost() {
		return this.cost;
	}

	/**
	 * get reimbursement amount for expense
	 * 
	 * @return reimbursement value
	 */
	@SuppressWarnings("static-method")
	public double getReimbursement() {
		return 0;
	}

	/**
	 * expense class for airfare expenses
	 */
	public static class AirfareExpense extends Expense {
		/**
		 * specifies expense type of airfare
		 */
		AirfareExpense() {
			super("airfare");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			return getCost();
		}
	}

	/**
	 * expense class for parking expenses
	 */
	public static class ParkingExpense extends Expense {
		/**
		 * specifies expense type of parking
		 */
		ParkingExpense() {
			super("parking");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			if (getCost() <= 10) return getCost();
			return 10.0;
		}
	}

	/**
	 * expense class for mileage expenses
	 */
	public static class MileageExpense extends Expense {
		/**
		 * specifies expense type of mileage
		 */
		MileageExpense() {
			super("mileage");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			return this.cost * .27;
		}

		// /*
		// * (non-Javadoc)
		// * @see project10.Expense#getCost()
		// */
		// @SuppressWarnings("javadoc")
		// @Override
		// public double getCost() {
		// return 0;
		// }
	}

	/**
	 * expense class for event expenses
	 */
	public static class EventExpense extends Expense {
		/**
		 * specifies expense type of event
		 */
		EventExpense() {
			super("event");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			return getCost();
		}
	}

	/**
	 * expense class for lodging expenses
	 */
	public static class LodgingExpense extends Expense {
		/**
		 * specifies expense type of lodging
		 */
		LodgingExpense() {
			super("lodging");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			if (getCost() <= 95) return getCost();
			return 95.0;
		}
	}

	/**
	 * expense class for car rental expenses
	 */
	public static class CarRentalExpense extends Expense {
		/**
		 * specifies expense type of carRental
		 */
		CarRentalExpense() {
			super("carRental");
		}
	}

	/**
	 * expense class for taxi expenses
	 */
	public static class TaxiExpense extends Expense {
		/**
		 * specifies expense type of taxi
		 */
		TaxiExpense() {
			super("taxi");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			if (getCost() <= 20) return getCost();
			return 20.0;
		}
	}

	/**
	 * expense class for food expenses
	 */
	public static class FoodExpense extends Expense {
		/**
		 * specifies expense type of food
		 */
		FoodExpense() {
			super("food");
		}

		/*
		 * (non-Javadoc)
		 * @see project10.Expense#getReimbursement()
		 */
		@SuppressWarnings("javadoc")
		@Override
		public double getReimbursement() {
			return 37.0;
		}
	}
}

