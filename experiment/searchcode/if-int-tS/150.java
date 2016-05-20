package example;

public class newFetchState{
	Processor p = new Processor();
	String data;
	char colon = ':';
	char closeBracket = '}';
	char comma = ',';
	int colonCounter;
	String refined;
	int Pivot;
	
	String temp = "{\"status\":1,\"data\":{\"weather\":{\"condition\":\"Sunny\",\"last_change\":1365668640},\"icetizen\":{\"1794\":{\"user\":{\"username\":\"Frankybeast\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(7,18)\",\"timestamp\":\"1365665965\"}},\"1796\":{\"user\":{\"username\":\"Curryhulktough\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(11,23)\",\"timestamp\":\"1365666921\"}},\"1797\":{\"user\":{\"username\":\"Walletdustmech\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(2,6)\",\"timestamp\":\"1365667170\"}},\"1798\":{\"user\":{\"username\":\"Holosixty\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(12,18)\",\"timestamp\":\"1365667309\"}},\"1799\":{\"user\":{\"username\":\"Gonavenger\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(1,6)\",\"timestamp\":\"1365667387\"}},\"1800\":{\"user\":{\"username\":\"Bullytokyo\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(6,12)\",\"timestamp\":\"1365667457\"}},\"1801\":{\"user\":{\"username\":\"Shopwolfchic\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(16,17)\",\"timestamp\":\"1365667533\"}},\"1802\":{\"user\":{\"username\":\"Lunarbeta\",\"type\":0,\"ip\":\"126.112.214.10\",\"port\":\"0\",\"pid\":\"246\"},\"last_known_destination\":{\"position\":\"(5,12)\",\"timestamp\":\"1365667557\"}},\"66\":{\"user\":{\"username\":\"SivaGod\",\"type\":1,\"ip\":\"Heavenly IP\",\"port\":0,\"pid\":0},\"last_known_destination\":{\"position\":\"(48,48)\",\"timestamp\":1365668688}},\"77\":{\"user\":{\"username\":\"EtherealProgrammer\",\"type\":1,\"ip\":\"Heavenly IP\",\"port\":0,\"pid\":0},\"last_known_destination\":{\"position\":\"(14,40)\",\"timestamp\":1365668690}}}}}";
	
	public WeatherCon getWeather(){
		String wth = null;
		int ts = 0;
		colonCounter = 0;
		Pivot = 0;
		for (int i=0;i<temp.length();i++){
			if (colonCounter == 5){
				for (int j=i;j<temp.length();j++){
					if (temp.charAt(j)==closeBracket){
						refined = temp.substring(0,j);
						break;
					}
				}
				break;
			}
			if (temp.charAt(i)==colon){
				colonCounter += 1;
			}
		}
		colonCounter = 0;
		for (int i=0;i<refined.length();i++){
			if(refined.charAt(i)==colon){
				colonCounter += 1;
				switch (colonCounter){
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					for (int j=i;j<refined.length();j++){
						if(refined.charAt(j)==comma){
							wth = p.removeQuotationMark((refined.substring(i+1,j)));
							break;
						}
					}
					break;
				case 5:
					ts = Integer.parseInt(refined.substring(i+1,refined.length()));
					break;
				default:
					break;
			}
			}
			
		}
		
		return new WeatherCon(wth,ts);
	}
}


