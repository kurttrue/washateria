package com.washateria.www.wash;

public class WashException
{
	public WashException(String ahint, String aexception)
	{

	   hint = ahint;
	   exception = aexception;

	}

	public String getHint()
	{
		return hint;
	}

	public String getException()
	{
		return exception;
	}


	protected String hint;
	protected String exception;

}
