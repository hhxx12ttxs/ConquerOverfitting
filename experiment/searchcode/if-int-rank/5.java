package gen;

import java.util.Vector;

public class Rank {
	private Vector<String> rankcap;
	
	public Rank(String rank)
	{
		this.rankcap = createRankcapVector(Integer.parseInt(rank));
	}

	private Vector<String> createRankcapVector(int rank) {
		Vector<String> rankcapVector = new Vector<String>();
		if (rank<4)
		{
			rankcapVector.add("11");
			rankcapVector.add("10");
			rankcapVector.add("9");
			rankcapVector.add("6");
			rankcapVector.add("3");
		}
		else if (rank<7)
		{
			rankcapVector.add("11");
			rankcapVector.add("10");
			rankcapVector.add("9");
			rankcapVector.add("6");
		}
		else if (rank<10)
		{
			rankcapVector.add("11");
			rankcapVector.add("10");
			rankcapVector.add("9");
		}	
		else if (rank<11)
		{
			rankcapVector.add("11");
			rankcapVector.add("10");
		}
		else if (rank<12)
		{
			rankcapVector.add("11");
		}
		else if (rank<13)
		{
			rankcapVector.add("12");
		}
		return rankcapVector;
	}
	public Vector<String> getRankcapVector()
	{
		return this.rankcap;
	}

}

