package com.brokerexpress.gate.micex.model;

import com.brokerexpress.gate.micex.MicexException;
import com.brokerexpress.gate.micex.impl.MicexFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: brokerexpress
 * Date: 26.04.11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
public class ExecutableTransaction{
	private Transaction transaction;
	private String[] params;
	private StringBuilder stringBuilder = new StringBuilder();
    private MicexFormat micexFormat = new MicexFormat();

	public ExecutableTransaction(Transaction transaction){
		this.transaction = transaction;
        clear();
	}

	public void clear(){
        params = new String[transaction.getInputFields().size()];
        for (int i = 0; i < params.length; i++)
            params[i] = transaction.getInputFields().get(i).getDefVal();
    }

	public String getName(){
		return transaction.getName();
	}

	public void setParam(String fieldName, Object value, Object metaData) throws MicexException {
		Field field = null;
		int fieldIndex = 0;
		//???? ????
		for (int i = 0; i < transaction.getInputFields().size(); i++)
			if (transaction.getInputFields().get(i).getName().equals(fieldName)){
				field = transaction.getInputFields().get(i);
				fieldIndex = i;
				break;
			}
		if (field == null)
			throw new MicexException("Can't find field " + fieldName + " for transaction " + transaction.getName() + ".");
		//??????????? ????????
		if (value != null){
			switch (field.getDataType()){
				case TCHAR:
					params[fieldIndex] = micexFormat.formatString((String)value, field.getSize());
					break;
				case TDATE:
					params[fieldIndex] = micexFormat.formatDate((Date) value);
					break;
				case TFIXED:
				case TFLOAT:
					params[fieldIndex] = value instanceof Double ?
							micexFormat.formatDouble((Double)value, field.getSize(), (Integer) metaData)
							: micexFormat.formatBigDecimal((BigDecimal)value, field.getSize());
					break;
				case TINTEGER:
					params[fieldIndex] = micexFormat.formatInt((Integer)value, field.getSize());
					break;
				case TLONG:
					params[fieldIndex] = micexFormat.formatLong((Long)value, field.getSize());
					break;
				case TTIME:
					params[fieldIndex] = micexFormat.formatTime((Date)value);
					break;
			}
		} else
			params[fieldIndex] = micexFormat.formatString("", field.getSize());
	}

	public String getParamsString(){
		stringBuilder.delete(0, stringBuilder.length());
		for (int i = 0; i < params.length; i++){
            String param = params[i];
            if (param == null){
                Field field = transaction.getInputFields().get(i);
                //???? ??? ?? ????????? ?? ???????? ?? ?????????
                if (field.getDefVal() == null)
                    return null;
                stringBuilder.append(field.getDefVal());
            } else
			    stringBuilder.append(param);
        }
		return stringBuilder.toString();
	}
}
