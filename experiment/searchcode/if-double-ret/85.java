package com.asksunny.cli;

import java.util.HashMap;

public class CLIParameters 
{
		public static final String ARRAY_SEPERATORS  = "\\s*,\\s*|\\s*;\\s*|\\s*:\\s*";
		public static final String CLI_ARG_NAME_PREFIX_DOUBLE = "--";
		public static final String CLI_ARG_NAME_PREFIX_SINGLE = "-";
		
		
		HashMap<String, Object> parameters  = null;
		CLIParameters()
		{
			parameters = new HashMap<String, Object>();
		}
		
		void addParameter(String name, String value){
			this.parameters.put(name, value);
		}
		
		
		public boolean hasParameter(String name){
			return this.parameters.containsKey(name);
		}
		
		public boolean hasParameterValue(String name){
			return this.parameters.containsKey(name) && this.parameters.get(name)!=null;
		}
		
		public boolean getBooleanParameter(String name){
			boolean ret = false;
			Object obj = this.parameters.get(name);
			if(obj!=null){
				String boolVal = obj.toString();
				ret = boolVal.equalsIgnoreCase("YES") || boolVal.equalsIgnoreCase("Y")
						|| boolVal.equalsIgnoreCase("TRUE")
						|| isPositiveNumber(boolVal);						
			}
			return ret;
		}
		
		public String getStringParameter(String name)
		{
			Object obj = this.parameters.get(name);			
			return (obj!=null)?obj.toString():null;
		}
		
		
		
		public double getDoubleParameter(String name)
		{
			Object obj = this.parameters.get(name);
			double ret = Double.valueOf(obj.toString()).doubleValue();
			return ret;
		}
		
		public int getIntegerParameter(String name)
		{
			Object obj = this.parameters.get(name);
			int ret = Integer.valueOf(obj.toString()).intValue();
			return ret;
		}
		
		public long getLongParameter(String name)
		{
			Object obj = this.parameters.get(name);
			long ret = Long.valueOf(obj.toString()).longValue();
			return ret;
		}
		
		
		
		public String[] getStringArrayParameter(String name)
		{
			String strpval = getStringParameter(name);
			return (strpval==null) ? null : strpval.split(ARRAY_SEPERATORS);
		}
		
		public int[] getIntArrayParameter(String name)
		{
			String strpval = getStringParameter(name);
			if(strpval==null) return null;
			
			String[] parray = strpval.split(ARRAY_SEPERATORS);
			int[] pintArray = new int[parray.length];
			for (int i = 0; i < pintArray.length; i++) {
				pintArray[i] = Integer.valueOf(parray[i].trim()).intValue();
			}
			return pintArray;					
			
		}
		
		public long[] getLongArrayParameter(String name)
		{
			String strpval = getStringParameter(name);
			if(strpval==null) return null;			
			String[] parray = strpval.split(ARRAY_SEPERATORS);
			long[] pintArray = new long[parray.length];
			for (int i = 0; i < pintArray.length; i++) {
				pintArray[i] = Long.valueOf(parray[i].trim()).longValue();
			}
			return pintArray;					
			
		}
		
		
		public double[] getDoubleArrayParameter(String name)
		{
			String strpval = getStringParameter(name);
			if(strpval==null) return null;			
			String[] parray = strpval.split(ARRAY_SEPERATORS);
			double[] pintArray = new double[parray.length];
			for (int i = 0; i < pintArray.length; i++) {
				pintArray[i] = Double.valueOf(parray[i].trim()).doubleValue();
			}
			return pintArray;	
		}
		
		
		
		private static boolean isPositiveNumber(String d){
			boolean ret = false;
			try{
				ret = (Double.valueOf(d).doubleValue()>0);
			}catch(Exception ex){
				;
			}
			return ret;
		}
		
		
}

