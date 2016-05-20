package data.idea;

public class BinaryString {

	//**************************************************************************//
	//	                        Public Final Statics                            //
	//**************************************************************************//

	//**************************************************************************//
	//	                        Members                                         //
	//**************************************************************************//
	String m_binStr = "";
	
	//**************************************************************************//
	//	                        Constructors                                    //
	//**************************************************************************//
	
	// TODO
	BinaryString(boolean b, String s){
		m_binStr = CryptoUtils.deleteSpaseSymbol(s);
	}
	
	BinaryString(BinaryString bs){
		m_binStr = bs.getBinaryString();
	}
	
	BinaryString(String s){
		byte[] arrByte = s.getBytes();
		for (int i = 0; i < arrByte.length; i++){
			m_binStr = m_binStr + byteToBinStr8x(arrByte[i]);
		}
	}

	BinaryString(char... chars){
		for (char c : chars)
			m_binStr = m_binStr + charToBinStr16x(c);
	}
	
	//**************************************************************************//
	//	                        Publics                                         //
	//**************************************************************************//
	public String getBinaryString(){
		return m_binStr;
	}
	
	public String toString(){
		String res = "";
		char c;
		for (int i = 0; i < m_binStr.length()/8; i++){
			c = (char)Integer.parseInt(get8bit(i).getBinaryString(), 2);
			res = res + c;
		}
		return res;
	}
	
	public String toString(boolean needSpase){
		String space = "";
		if (needSpase)
			space = " ";
		String res = "";
		int count = m_binStr.length()/8;
		int startIdx;
		int endIdx;
		for (int i = 0; i < count; i++){
			startIdx = i*8;
			endIdx = startIdx + 8;
			res = res + m_binStr.substring(startIdx, endIdx) + space;
		}
		return res;
	}
	
	public void leftShift(int shift){
		String buf = m_binStr.substring(shift);
		buf = buf + m_binStr.substring(0, shift);
		m_binStr = buf;
	}

	// char - 16 bit
	// numBlock - 0 .. 7
	public char getChar16bit(int numBlock){
		int startIdx = 16*numBlock;
		int endIdx = startIdx + 16;
		String buf = m_binStr.substring(startIdx, endIdx);
		return (char) Integer.parseInt(buf, 2);
	}
	
	public BinaryString get8bit(int numBlock){
		int startIdx = 8*numBlock;
		int endIdx = startIdx + 8;
		BinaryString bs = new BinaryString();
		bs.setBinaryString(m_binStr.substring(startIdx, endIdx));
		return bs;
	}
	
	public BinaryString get16bit(int numBlock){
		int startIdx = 16*numBlock;
		int endIdx = startIdx + 16;
		BinaryString bs = new BinaryString();
		bs.setBinaryString(m_binStr.substring(startIdx, endIdx));
		return bs;
	}
	
	public BinaryString get64bit(int numBlock){
		int startIdx = 64*numBlock;
		int endIdx = startIdx + 64;
		BinaryString bs = new BinaryString();
		bs.setBinaryString(m_binStr.substring(startIdx, endIdx));
		return bs;
	}
	
	//**************************************************************************//
	//	                        Abstracts                                       //
	//**************************************************************************//

	//**************************************************************************//
	//	                        Protected                                       //
	//**************************************************************************//

	//**************************************************************************//
	//	                        Privates                                        //
	//**************************************************************************//
	private String byteToBinStr8x(int val){
		String result = Integer.toBinaryString(val);
		int border =  8 - result.length();
		for (int i = 0; i < border; i++){
			result = "0" + result;
		}
		return result;
	}
	
	private String charToBinStr16x(int val){
		String result = Integer.toBinaryString(val);
		int border = 16 - result.length();
		for(int i = 0; i < border; i++){
			result = "0" + result;
		}
		return result;
	}
	
	private void setBinaryString(String s){
		m_binStr = s;
	}
	
	//**************************************************************************//
	//	                        Public Statics                                  //
	//**************************************************************************//

	//**************************************************************************//
	//	                        Private Statics                                 //
	//**************************************************************************//

	//**************************************************************************//
	//	                        Internal Classes                                //
	//**************************************************************************//
	
}
	
	
	

