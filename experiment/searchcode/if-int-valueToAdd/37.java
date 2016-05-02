package com.przybylak.workshops.units.junit.executor;

import org.junit.runner.Result;

public class ExecutionResult 
{
	int classPassCount;
	int classFailCount;
	int methodPassCount;
	int methodFailCount;
	int methodIgnoreCount;
	
	public int getClassPassCount() 
	{
		return classPassCount;
	}
	
	public void setClassPassCount(int classPassCount) 
	{
		this.classPassCount = classPassCount;
	}
	
	public void addClassPassCount(int classPassValueToAdd)
	{
		this.classPassCount += classPassValueToAdd;
	}
	
	public int getClassFailCount()
	{
		return classFailCount;
	}
	
	public void setClassFailCount(int classFailCount) 
	{
		this.classFailCount = classFailCount;
	}
	
	public void addClassFailCount(int classFailValueToAdd)
	{
		this.classFailCount += classFailValueToAdd;
	}
	
	public int getMethodPassCount() 
	{
		return methodPassCount;
	}
	
	public void setMethodPassCount(int methodPassCount) 
	{
		this.methodPassCount = methodPassCount;
	}
	
	public void addMethodPassCount(int methodPassToAdd)
	{
		this.methodPassCount += methodPassToAdd;
	}
	
	public int getMethodFailCount() 
	{
		return methodFailCount;
	}
	
	public void setMethodFailCount(int methodFailCount) 
	{
		this.methodFailCount = methodFailCount;
	}
	
	public void addMethodFailCount(int methodFailToAdd)
	{
		this.methodFailCount += methodFailToAdd;
	}
	
	public int getMethodIgnoreCount()
	{
		return methodIgnoreCount;
	}
	
	public void setMethodIgnoreCount(int methodIgnoreCount) 
	{
		this.methodIgnoreCount = methodIgnoreCount;
	}
	
	public void addMethodIgnoreCount(int methodIgnoreToAdd)
	{
		this.methodIgnoreCount += methodIgnoreToAdd;
	}
	
	public void add(ExecutionResult valueToAdd)
	{
		addClassPassCount(valueToAdd.getClassPassCount());
		addClassFailCount(valueToAdd.getClassFailCount());
		addMethodPassCount(valueToAdd.getMethodPassCount());
		addMethodFailCount(valueToAdd.getMethodFailCount());
		addMethodIgnoreCount(valueToAdd.getMethodIgnoreCount());
	}

	public static ExecutionResult createFromTestResult(Result result) 
	{
		ExecutionResult executionResult = new ExecutionResult();
		
		if(result.getFailureCount() == 0)
		{
			executionResult.addClassPassCount(1);
		}
		else
		{
			executionResult.addClassFailCount(1);
		}
		
		executionResult.addMethodFailCount(result.getFailureCount());
		executionResult.addMethodPassCount(result.getRunCount() - (result.getFailureCount() + result.getIgnoreCount()));
		executionResult.addMethodIgnoreCount(result.getIgnoreCount());
		
		return executionResult;
	}
}

