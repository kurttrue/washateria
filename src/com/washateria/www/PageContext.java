package com.washateria.www.wash;

//this is a convenience class that keeps track of context values for a page.
//such as next, previous, last, first, total pages in pagefrom parent, path to root, and author.

import java.util.HashMap;
import java.util.ArrayList;

public class PageContext

{

  public PageContext()
  {

  }


  public void setProperty(String key, String value)
  {
	  properties.put(key, value);

	  if(key.equals(TOROOT))
	  {
		  this.makeToRootList(value);
	  }
  }

  public String getProperty(String key)
  {
	  StringBuffer returnBuffer = new StringBuffer("");

	  if(properties.containsKey(key))
	  {
		  returnBuffer.append(properties.get(key));
	  }

	  return returnBuffer.toString();

  }

  private void makeToRootList(String value)
  {

	  //the path to root is stored in the db as a tab-delimited list

	  String[] toRootArray = value.split(TOROOTDELIMITER);

	  toRootList = new ArrayList<String>();

	  for(String s : toRootArray)
	  {
		  toRootList.add(s);
	  }

  }

  public ArrayList<String> getToRootList()
  {

	  ArrayList<String> returnList;

	  //this condition should never evaluate to true.
	  if(toRootList == null)
	  {

		  returnList = new ArrayList<String>();
	  }
	  else
	  {
		  returnList = toRootList;
	  }

	  return returnList;
  }

  public HashMap<String, String> getProperties()
  {

	  return properties;
  }


public static final String PREVIOUS = "previous";
public static final String NEXT = "next";
public static final String TOTAL = "total";
public static final String FIRST = "first";
public static final String LAST = "last";
public static final String CURRENT = "current";
public static final String TOROOT = "toroot";
public static final String AUTHOR = "author";
public static final String UPDATED = "updated";
public static final String CREATED = "created";

public static final String TOROOTWRAPPER = "torootdetail";
public static final String TOROOTLEAF = "leaf";

private HashMap<String, String> properties = new HashMap<String, String>();

private ArrayList<String> toRootList;

private static final String TOROOTDELIMITER = "\t";

}

