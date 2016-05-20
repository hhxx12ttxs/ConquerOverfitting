package at.edu.hti.shop.domain;

import java.util.ArrayList;

public class Order {

	private IPriceStrategy strategy = PriceStrategyFactory.getStrategy("Default");
	private ArrayList<OrderLine> lines = new ArrayList<>();

	public boolean add(OrderLine e) {
		if (e == null)
			return false;

		return lines.add(e);
	}

	public int size() {
		return lines.size();
	}

	public OrderLine get(int index) {
		return lines.get(index);
	}

	public void updateAmount(OrderLine line, int amount) throws OrderException {
		if (!lines.contains(line)) {
			throw new OrderException("Line:" + line + " not part of order");
		}

		if (amount < 0)
			throw new IllegalArgumentException("Amount not >=0");

		if (amount == 0) {
			lines.remove(line);
		} else {
			line.setAmount(amount);
		}
	}

	public double calcPrize() {
		return strategy.calcPrice(lines);
	}

	@Override
	public String toString() {

		return lines.toString() + " \n =>" + calcPrize();
	}
	
	public void setStrategy(IPriceStrategy strategy) {
		this.strategy = strategy;
	}
}

