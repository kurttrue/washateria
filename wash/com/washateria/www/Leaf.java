package com.washateria.www.wash;

import java.util.HashMap;
import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Leaf
{


	public Leaf()
	{

	}

	public Leaf(String tag)
	{
		values.put(TAGHANDLE, tag);
	}


    public void setValue(String handle, String value)
    {
		values.put(handle, value);
	}

	public boolean hasChildren()
	{
		return children.size()>0;
	}


    public void addChild(Leaf leaf)
    {
		children.add(leaf);
	}

	public String getValue(String handle)
	{
		StringBuffer returnBuffer = new StringBuffer("");

		String out;

		if(values.containsKey(handle))
		{
			try
			{
		    	 out = new String(values.get(handle).getBytes("UTF-8"), "ISO-8859-1");
		    }
		    catch(Exception e)
		    {
		         out = "CONVERTERROR";
			}

			//returnBuffer.append(values.get(handle));
			returnBuffer.append(out);
		}

		return returnBuffer.toString();
	}

	//calling routine creates a Document and creates an element by getting tag, value and attributes from Leaf.
	public void setElement(Element aelement)
	{
		Element element = aelement;
	}

	public Element getElement()
	{
		return element;
	}

	public HashMap<String, String> getValues()
	{
		return values;
	}

	public void setPathToRoot(String pathtoroot)
	{
		//pathtoroot is a tab delimited string.
		//PageContext knows how to parse it.
		pagecontext.setProperty(PageContext.TOROOT, pathtoroot);
	}

	public PageContext getPageContext()
	{

		return pagecontext;
	}




//values from the db, or set from the calling routine.
//for example the calling routine can set the tag for the foreign wrapper this way...
//[Leaf].setValue("tag", "foreignkeys");
HashMap<String, String> values = new HashMap<String, String>();
ArrayList<Leaf> children = new ArrayList<Leaf>();
public static final String TAGHANDLE = "tg_name";
protected Element element;
protected PageContext pagecontext = new PageContext();

}
