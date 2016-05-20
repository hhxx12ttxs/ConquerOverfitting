package lib;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Writable;

public class VectorWritable implements Writable, Cloneable{

	@Override
	protected Object clone() throws CloneNotSupportedException {

		VectorWritable vectorWritable =  (VectorWritable) super.clone();
		vectorWritable.data = (HashMap<Integer, Double>) data.clone();
		
		return vectorWritable;
		
	}

	private HashMap<Integer,Double> data = new HashMap<Integer,Double>();
	private int columnNum = 0;
	public VectorWritable() {
		
	}
	public VectorWritable(int columNum) {
		this.columnNum = columNum;
	}
	
	public int getSize() {
		return data.size();
	}
	
	public void setData(List<Double> list) {
		for (int i=0; i<list.size(); i++) {
			if (list.get(i) !=0 ) {
				data.put(i, list.get(i));
			}
		}
	}
	
	public void setData(HashMap<Integer,Double> data) {
		this.data = data;
	}
	
	public Map<Integer,Double> getData() {
		return data;
	}
	
	public void setColumnNum(int columnNum) {
		this.columnNum = columnNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	@Override
	public String toString() {
		String result = "";
		for (Map.Entry<Integer, Double> entry : data.entrySet()) {
			result += entry.getKey()+":"+entry.getValue()+"\t";
		}
		return result.substring(0, result.lastIndexOf("\t"));
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		int mapSize = in.readInt();
		setColumnNum(in.readInt());
		data.clear();
		for (int i=0; i<mapSize; i++) {
			data.put(in.readInt(), in.readDouble());
		}
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(getSize());
		out.writeInt(getColumnNum());
		for (Map.Entry<Integer, Double> entry : data.entrySet()) {
//			out.write(entry.getKey().intValue());
			out.writeInt(entry.getKey().intValue());
			out.writeDouble(entry.getValue().doubleValue());
		}
		
		
	}
	
	public static void main (String[] args) {
		VectorWritable vector = new VectorWritable();
		List<Double> list = new ArrayList<Double>();
		list.add(1.1);
		list.add(1.2);
		list.add(1.3);
		vector.setData(list);
		System.out.println(vector);
	}

	@Override
	public int hashCode() {
		String codeString = "";
		for (Map.Entry<Integer, Double> entry : data.entrySet()) {
			codeString += entry.getKey() + entry.getValue();
		}
		
		return codeString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this.hashCode() == obj.hashCode()) {
			return true;
		}
		else
			return false;
	}
	


	
}

