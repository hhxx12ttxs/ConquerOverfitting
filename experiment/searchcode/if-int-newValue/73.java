/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of RainbowLibs project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.rainbowbreeze.libs.common;

/**
 * Class for result operation.
 * Similar to http reply, where there are a return code
 * and a reply
 *
 * @author Alfredo Morresi
 */
public class RainbowResultOperation<ResultType>
{
	//---------- Constructors
	public RainbowResultOperation() {
		mReturnCode = RETURNCODE_OK;
	}

	public RainbowResultOperation(int errorReturnCode) {
		mReturnCode = errorReturnCode;
	}

	public RainbowResultOperation(Exception ex, int errorReturnCode) {
		mReturnCode = errorReturnCode;
		mException = ex;
	}

	public RainbowResultOperation(ResultType value) {
		this(RETURNCODE_OK, value);
	}

	public RainbowResultOperation(int returnCode, ResultType value) {
		setReturnCode(returnCode);
		setResult(value);
	}


	
	//---------- Public fields
	/** All done! */
	public final static int RETURNCODE_OK = 200;
    /** First free value for custom operation (no error) code */
    public final static int RETURNCODE_OPERATION_FIRST_USER = 210;

	/** Errors code, all codes are over 400, please insert custum code from 450 to above */
	public final static int RETURNCODE_ERROR_GENERIC = 400;
	public static final int RETURNCODE_ERROR_APPLICATION_ARCHITECTURE = 401;
	public static final int RETURNCODE_ERROR_COMMUNICATION = 402;
	public static final int RETURNCODE_APP_EXPIRED = 403;
	/** First free value for custom error code */
	public static final int RETURNCODE_ERROR_FIRST_USER = 500;
	
	/** A good result, but without additional data to carry on */
	public static RainbowResultOperation OK = new RainbowResultOperation();
	

	
	
	//---------- Public properties

	ResultType mResult;
	public ResultType getResult()
	{return mResult; }
	public void setResult(ResultType newValue)
	{ mResult = newValue; }
	
	protected int mReturnCode;
	public int getReturnCode()
	{ return mReturnCode; }
	public void setReturnCode(int newValue)
	{ mReturnCode = newValue; }
	
	
	protected Exception mException;
	public Exception getException()
	{ return mException; }
	public void setException(Exception newValue, int returnCode)
	{
		mException = newValue;
		mReturnCode = returnCode;
	}
	
	
	
	
	//---------- Public methods
	/**
	 * Return if the object contains error
	 */
	public boolean hasErrors()
	{ return null != mException || mReturnCode >= 400; }
	
	/**
	 * Move Exception and return code to another {@link RainbowResultOperation}
	 * but with a different type.
	 * It's important to maintain same firm of this method in order to allow
	 * derived class to declare they own {@link #translateError(RainbowResultOperation)}
	 * method with a different parameters used by overload mechanism.
	 * 
	 * @param <NewResultType>
	 * @param newResultOperation
	 * @return
	 */
	public <NewResultType> RainbowResultOperation<NewResultType> translateError(RainbowResultOperation<NewResultType> newResultOperation) {
		if (null == newResultOperation) newResultOperation = new RainbowResultOperation<NewResultType>();
		newResultOperation.setException(mException, mReturnCode);
		return newResultOperation;
	}
	
	
	
	
	//---------- Private methods

}

