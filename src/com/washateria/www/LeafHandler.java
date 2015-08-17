package com.washateria.www.wash;


import java.io.File;
import java.io.StringReader;

import eu.bitwalker.useragentutils.DeviceType;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


//this is the class that creates the XML output for the public pages.

public class LeafHandler
{
	public LeafHandler(DBWriter adbreader)
	{
       dbreader = adbreader;

	}

	public LeafHandler setZero(String azero)
	{
		zero = azero;

		return this;
	}

	public LeafHandler setDuty(String aduty)
	{
		duty=aduty;

		return this;
	}

	public LeafHandler setRootTag(String arootTag)
	{

		rootTag = arootTag;

		return this;
	}

	public LeafHandler setIncludeSSHREF(boolean aincludeSSHREF)
	{
		includeSSHREF = aincludeSSHREF;

		return this;
	}

	public LeafHandler setXSLPath(String axslPath)
	{

		xslPath = axslPath;

		return this;
	}

	public LeafHandler setDefaultXSL(String adefaultXSL)
	{
		defaultXSL = adefaultXSL;

		return this;
	}

	public LeafHandler setDeviceType(String adeviceType)
	{

		//device type as defined in eu.bitwalker.useragentutils.DeviceType
		//http://bitwalker.eu/user-agent-utils-snapshot/javadoc/index.html
		deviceType = adeviceType;

		return this;
	}

	public void connect()
	{
		dbreader.connect();
	}

	public void disconnect()
	{
		dbreader.disconnect();
	}


    public void expand()
    {
		//if there's a zero and no duty, assume duty is single-page view.
		if(duty==null  && zero != null)
		{
			duty = PAGE;
		}
		//if no duty and no zero, browse most recent entries.
		else if(duty==null && zero == null)
		{
			duty = BROWSE;
			zero = NOZERO;
		}

		if(!zero.equals(NOZERO) && duty==PAGE)
		{
			pagecontext = new PageContext();

			this.expandFromIdx(zero);
		}
		//a request for page or TOC view without a zero will result in a browse view.
		else
	    {
			this.expandBrowse();
		}
	}


    public void setPathToRoot()
    {
		//you have to call expandFromIdx() or setZero()
		//before you call this method.
		if(zero != null)
		{
		     String topleft = displayMap.get(zero).getValue("lf_left");
		     String topright = displayMap.get(zero).getValue("lf_right");
		     String totem = displayMap.get(zero).getValue("lf_lf_as_top_idx");

		     ArrayList<String> binds = new ArrayList<String>();

		     binds.add(topleft);
		     binds.add(topright);
		     binds.add(totem);



		     String select = "SELECT lf_idx, lf_text FROM leaf " +
                              "WHERE " +
                              "lf_left<=?" +
                              " AND " +
                              "lf_right>=?" +
                              " AND " +
                              "lf_lf_as_top_idx=?" +
                              " ORDER BY lf_idx";

             /*

		     String select = "SELECT lf_idx, lf_text FROM leaf " +
                              "WHERE " +
                              "lf_left<=" + topleft +
                              " AND " +
                              "lf_right>=" + topright +
                              " AND " +
                              "lf_lf_as_top_idx=" + totem +
                              " ORDER BY lf_idx";

            */



//String pathToRoot = dbreader.executeQuery(select, binds, "\t");

//System.out.println("pathtorootstring->" + pathToRoot);

            HashMap<String, ArrayList<String>> pathToRoot = dbreader.executeQuery(select, binds);


            int i = 0;

            ArrayList<String> keys = pathToRoot.get("lf_idx");
            ArrayList<String> values = pathToRoot.get("lf_text");
/*

      if(keys == null)
      {

		  System.out.println("keys list is null.");
		  System.out.println("lf_left-> " + displayMap.get(zero).getValue("lf_left"));
		  System.out.println("lf_right-> " + displayMap.get(zero).getValue("lf_right"));
		  System.out.println("lf_top-> " + displayMap.get(zero).getValue("lf_lf_as_top_idx"));
		  System.out.println("select-> " + select);
	  }

*/


            //path back to root isn't stored in Leaf because it fits in a sorted HashMap.
            for(String k : keys)
            {

				pathToRootList.add(k);

				pathToRootMap.put(k, values.get(i));

				i++;

			}




		}
	}

	public void expandBrowse()
    {

		/*
		String select = "SELECT " +
		"c.lf_idx, pti_author, pti_lf_as_aut_idx, pti_img_url, pti_img_alt, " +
		"pti_img_width, pti_img_height, pti_to_root, pti_heading, pti_tease, tg_teaserule, " +
		"st_sshref, st_sstype, st_xmlencoding, st_xmlversion, " +
		"c.lf_created, c.lf_published " +
		"FROM ptindex, leaf c, leaf p, universe, tag, settings " +
		"WHERE c.lf_idx = pti_lf_idx " +
		"AND c.lf_lf_as_top_idx = p.lf_idx " +
		"AND p.lf_uni_idx = uni_idx " +
		"AND c.lf_tg_idx = tg_idx " +
		"AND c.lf_published ='yes' " +
		"AND st_idx = uni_st_idx " +
		"AND uni_url = ? " +
		"ORDER BY lf_created DESC ";
		*/

		//replacements is string replacements for the sql string.
		//It has nothing to do with binds.
		HashMap<String, String> replacements = NOREPLACEMENTS;

        String select = getSQLFromFile("browse.sql", replacements);

        ArrayList<String> binds = new ArrayList<String>();

		binds.add(server);

        HashMap<String, ArrayList<String>> pageMap = dbreader.executeQuery(select, binds);

		sshref = pageMap.get("st_sshref").get(0);

		sstype = pageMap.get("st_sstype").get(0);

		xmlencoding = pageMap.get("st_xmlencoding").get(0);

        xmlversion = pageMap.get("st_xmlversion").get(0);



        int i=0;

        //create the root leaf
        displayMap.put(NOZERO, new Leaf());

        Leaf zeroLeaf = displayMap.get(NOZERO);

        if(pageMap.containsKey("lf_idx"))
        {

			//create a hash map of all the leafs.
			for(String lf_idx : pageMap.get("lf_idx"))
			{


				displayList.add(lf_idx);

				displayMap.put(lf_idx, new Leaf());

				Leaf leaf = displayMap.get(lf_idx);

				//pagecontext.setProperty(PageContext.TOROOT, displayMap.get(zero).getValue("pti_to_root"));

				//put the values from the db into each leaf.
				for(String handle : pageMap.keySet())
				{
					String value = pageMap.get(handle).get(i);

					leaf.setValue(handle, value);

					//Leaf's PageContext will parse the value of pti_to_root
					//so it can be output in XML as sibling elements.
					leaf.setPathToRoot(pageMap.get("pti_to_root").get(i));


				}


				zeroLeaf.addChild(leaf);



				i++;

			}

	    }



    }

	public void expandFromIdx(String zlf_idx)
	{

		//you only want to assign the xsl at the first time through this loop.
		//not subsequent calls for foreign keys.
		expandCount++;

		//System.out.println("calling expandFromIdx on " + zlf_idx);

		//zero is the first lf_idx to pass through this method.
		if(zero == null)
		{
			zero = zlf_idx;
		}

		ArrayList<String> localForeignList = new ArrayList<String>();

		String keyOn;

		//foreign leaf lf_text value can be an integer (referring to lf_idx)
		//or a String (referring to lf_nickname).
		if(this.isInteger(zlf_idx))
		{
			keyOn="lf_idx";
	    }
	    else
	    {
			keyOn="lf_nickname";

		}

		/*
		String select =
			"SELECT " +
			"ll.lf_idx, ll.lf_lf_as_parent_idx, ll.lf_lf_as_top_idx, tg_name, tg_datatype, \n" +
			"ll.lf_text, ll.lf_created, ll.lf_updated, ll.lf_left, ll.lf_right, ll.lf_lf_as_top_idx, \n" +
			"st_xmlversion, st_xmlencoding, st_sstype, st_sshref, u.uni_url\n" +
			"FROM leaf l \n" +
			"LEFT JOIN universe u on l.lf_uni_idx = u.uni_idx, \n" +
			"leaf ll, tag t\n" +
			"LEFT JOIN settings on st_idx = tg_st_idx \n" +
			"WHERE ll.lf_tg_idx = tg_idx \n" +
			"AND ll.lf_right<=l.lf_right \n" +
			"AND ll.lf_left>= l.lf_left \n" +
			"AND ll.lf_lf_as_top_idx = l.lf_lf_as_top_idx \n" +
			"AND l." + keyOn + " =? \n" +
			"ORDER BY ll.lf_left";
		*/

        //adjusted select to include pageindex table on 26ja15.
		/*String select =
			"SELECT " +
			"ll.lf_idx, ll.lf_lf_as_parent_idx, ll.lf_lf_as_top_idx, tg_name, tg_datatype, \n" +
			"ll.lf_text, ll.lf_created, ll.lf_updated, ll.lf_left, ll.lf_right, ll.lf_lf_as_top_idx, \n" +
			"st_xmlversion, st_xmlencoding, st_sstype, st_sshref, u.uni_url, \n" +
			"pgi_lf_as_next_idx, pgi_lf_as_previous_idx, pgi_lf_as_first_idx, pgi_lf_as_last_idx, pgi_total, pgi_page \n" +
			"FROM leaf l \n" +
			"LEFT JOIN universe u on l.lf_uni_idx = u.uni_idx \n" +
			"LEFT JOIN pageindex p ON p.pgi_lf_idx = l.lf_idx, \n" +
			"leaf ll, tag t\n" +
			"LEFT JOIN settings on st_idx = tg_st_idx \n" +
			"WHERE ll.lf_tg_idx = tg_idx \n" +
			"AND ll.lf_right<=l.lf_right \n" +
			"AND ll.lf_left>= l.lf_left \n" +
			"AND ll.lf_lf_as_top_idx = l.lf_lf_as_top_idx \n" +
			"AND l." + keyOn + " =? \n" +
			"ORDER BY ll.lf_left";

		*/


        //map for doing a simple string replacement in getSQLFromFile.
        HashMap<String, String> replacements = new HashMap<String, String>();

        replacements.put("KEYON", keyOn);

        String select = getSQLFromFile("page.sql", replacements);

        //System.out.println("select -> "  + select);

        ArrayList<String> binds = new ArrayList<String>();

        binds.add(zlf_idx);


        HashMap<String, ArrayList<String>> pageMap = dbreader.executeQuery(select, binds);

        //System.out.println("select...");
        //System.out.println(select);

        int i=0;

        //create a hash map of all the leafs.
        for(String lf_idx : pageMap.get("lf_idx"))
        {

			//System.out.println("adding " + lf_idx + " i->" + i);

			displayList.add(lf_idx);

			displayMap.put(lf_idx, new Leaf());

			//put the values from the db into each leaf.
			for(String handle : pageMap.keySet())
			{
				String value = pageMap.get(handle).get(i);



				//add foreign keys to global and local foreignKey lists
				//if this foreign key hasn't been encountered yet.
				//don't set a value for foreign key.
				//instead of a value, the leaf will have the foreign key's decendants.
				if(handle.equals("tg_datatype") && value.equals("foreign"))
				{

                      String foreignKey = pageMap.get("lf_text").get(i);
                      //don't allow a foreign leaf to refer to itself or its zero ancestor.
                      if(!foreignList.contains(foreignKey) && !foreignKey.equals(zlf_idx) && !foreignKey.equals(lf_idx))
                      {

						  foreignList.add(foreignKey);
						  localForeignList.add(foreignKey);
						  foreignToRef.put(foreignKey, lf_idx);
					  }
				}
				else
				{

	 				    //foreign values look untidy, so omit them from the xml output.
	 				    if(!(pageMap.get("tg_datatype").get(i).equals("foreign")&& handle.equals("lf_text")))
	 					{
	 					  //values from the db are stored as a HashMap in Leaf class.
						  displayMap.get(lf_idx).setValue(handle, value);



					    }

				}

			}

			i++;


		}



		if(expandCount==1)
		{
			//assign values for stylesheet tag from the root node.
			//these will all be blank strings if the root node has no reference to settings table.
			sshref = displayMap.get(zero).getValue("st_sshref");

			sstype = displayMap.get(zero).getValue("st_sstype");

			xmlencoding = displayMap.get(zero).getValue("st_xmlencoding");

			xmlversion = displayMap.get(zero).getValue("st_xmlversion");

			//pgi_lf_as_next_idx, pgi_lf_as_previous_idx, pgi_lf_as_first_idx, pgi_lf_as_last_idx, pgi_total, pgi_page,
            //pti_to_root, pti_author

			//send the page context values from the first row of the return to the PageContext convenience class.
			pagecontext.setProperty(PageContext.NEXT, displayMap.get(zero).getValue("pgi_lf_as_next_idx"));
			pagecontext.setProperty(PageContext.PREVIOUS, displayMap.get(zero).getValue("pgi_lf_as_previous_idx"));
			pagecontext.setProperty(PageContext.FIRST, displayMap.get(zero).getValue("pgi_lf_as_first_idx"));
			pagecontext.setProperty(PageContext.LAST, displayMap.get(zero).getValue("pgi_lf_as_last_idx"));
			pagecontext.setProperty(PageContext.TOTAL, displayMap.get(zero).getValue("pgi_total"));
			pagecontext.setProperty(PageContext.CURRENT, displayMap.get(zero).getValue("pgi_page"));
			pagecontext.setProperty(PageContext.TOROOT, displayMap.get(zero).getValue("pti_to_root"));
			pagecontext.setProperty(PageContext.AUTHOR, displayMap.get(zero).getValue("pti_author"));
			pagecontext.setProperty(PageContext.UPDATED, displayMap.get(zero).getValue("lupdated"));
			pagecontext.setProperty(PageContext.CREATED, displayMap.get(zero).getValue("lcreated"));

			//System.out.println("sshref -> " + sshref);

	    }

			//recursive call.  put foreign keys in displayMap;
			for(String flf_idx : localForeignList)
			{
				//reset the row counter.
				i=0;
				expandFromIdx(flf_idx);
			}



        //if calling routine hasn't called setServer(), you can set it here.
        if(server == null)
        {
			server = displayMap.get(zero).getValue("uni_url");
		}

		//everything but the zero leaf will be the child of some other leaf in displayMap.
		for(String child : displayMap.keySet())
		{

			String parent = displayMap.get(child).getValue("lf_lf_as_parent_idx");
			if(!parent.equals("0") && !parent.equals(""))
			{
				if(displayMap.containsKey(parent))
				{
					displayMap.get(parent).addChild(displayMap.get(child));
				}
			}

		}
        /*

        for(String handle : pageMap.keySet())
        {
			System.out.println(handle);
		}
		*/

	}

 public String getDocumentAsHTML()
 {
	 StringBuffer returnBuffer = new StringBuffer("");

	 StringBuffer stylePath = new StringBuffer(realPath);
	 stylePath.append("/xsl/");
	 stylePath.append(sshref);

	 //returnBuffer.append("stylePath-> " + stylePath.toString());

	 //don't include the stylesheet data in the document.
	 includeSSHREF = false;

	 Document document;

	 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	 if(duty.equals(PAGE))
	 {
	    document = this.getDocument();
	 }
	 else
	 {
	    document = this.getBrowseDocument();
	 }

        try
        {
            File stylesheet = new File(stylePath.toString());
            //File datafile = new File(argv[1]);

            DocumentBuilder builder = factory.newDocumentBuilder();
            //document = builder.parse(datafile);

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(document);
            StringWriter outWriter = new StringWriter();
            StreamResult result = new StreamResult(outWriter);
            transformer.transform(source, result);
            returnBuffer.append(outWriter.getBuffer().toString());
        }
        catch (TransformerConfigurationException tce)
        {
            // Error generated by the parser
            System.out.println("\n** Transformer Factory error");
            System.out.println("   " + tce.getMessage());

            // Use the contained exception, if any
            Throwable x = tce;

            if (tce.getException() != null) {
                x = tce.getException();
            }

            x.printStackTrace();
        }
        catch (TransformerException te)
        {
            // Error generated by the parser
            System.out.println("\n** Transformation error");
            System.out.println("   " + te.getMessage());

            // Use the contained exception, if any
            Throwable x = te;

            if (te.getException() != null) {
                x = te.getException();
            }

            x.printStackTrace();
        }
        /*
        catch (SAXException sxe)
        {
            // Error generated by this application
            // (or a parser-initialization error)
            Exception x = sxe;

            if (sxe.getException() != null) {
                x = sxe.getException();
            }

            x.printStackTrace();
        }
        */
        catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
        /*catch (IOException ioe)
        {
            // I/O error
            ioe.printStackTrace();
        }
        */

	 return returnBuffer.toString();

 }

 public String getDocumentAsString()
 {

        Document doc;

        if(duty.equals(PAGE))
        {
           doc = this.getDocument();
	    }
	    else
	    {
		   doc = this.getBrowseDocument();
		}

        StringBuffer returnBuffer = new StringBuffer("");

	    try
	    {


	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       if(includeSSHREF)
	       {
             transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	       }

	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }

        /*

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;

        try
        {
            //if(!sshref.equals("") && includeSSHREF)
            //{
            //    String stylesheetPathname = sshref;
            //    Source stylesheetSource = new StreamSource(new File(stylesheetPathname).getAbsoluteFile());

            //    transformer = tf.newTransformer(stylesheetSource);
		    //}
		    //else
		    //{

			   transformer = tf.newTransformer();

	           //for now let's just write the wrappers manually.
	           transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	           transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	           transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	           transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			//}
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            returnBuffer.append(output);
        }
        catch (TransformerException e)
        {
            exceptions.add("transformer: " + e.toString());
        }

        */

        //return returnBuffer.toString();
    }
  public Document getBrowseDocument()
  {


	  String rootStr;

	  //if calling routine didn't define a wrapper, use default wrapper.
	  if(rootTag == null)
	  {
		  rootStr = BROWSE;
	  }
	  else
	  {
		  rootStr = rootTag;
	  }


	  try
	  {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		doc = docBuilder.newDocument();
		//this tells you how to create a DOM tree.
		//http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
		Element rootElement = doc.createElement(rootStr);

		rootElement.appendChild(getDeviceTypeElement());

		doc.appendChild(rootElement);

		this.setXSLInstruction();

        for(String elf_idx : displayList)
        {


			Leaf leaf = displayMap.get(elf_idx);

			Element myElement = doc.createElement("leaf");

			leaf.setElement(myElement);

			elementMap.put(elf_idx, myElement);

			rootElement.appendChild(myElement);

			Element contextElement = doc.createElement("context");

			Element trwe = doc.createElement(PageContext.TOROOTWRAPPER);

			//the toRoot value needs special handling on account of it's a tab-delimited list.
			for(String tr : leaf.getPageContext().getToRootList())
			{


				//create an element for each leaf heading back to root.
				Element tre = doc.createElement(PageContext.TOROOTLEAF);

				tre.appendChild(doc.createTextNode(tr));

				//append the to root leaf element to the to root wrapper element.
				trwe.appendChild(tre);
			}

			contextElement.appendChild(trwe);

			myElement.appendChild(contextElement);




			for(String k : leaf.getValues().keySet())
			{

					Element browselet = doc.createElement(k);

					browselet.appendChild(doc.createTextNode(leaf.getValue(k)));

					myElement.appendChild(browselet);

			}





	    }

	    this.addBrand();

      }
      catch(Exception pce)
      {

	        exceptions.add("getBrowseDocument:" + pce.toString());

	  		doc = null;

	  }

	  return doc;



  }

  protected void addBrand()
  {

        try
        {
			//add branding information from the universe xml file to the root element.
			Document brand = this.getBrand();

			if(brand !=null)
			{
				//rootElement.appendChild((Element) brand.getFirstChild());

				//String xyz = "123";

				Element be = (Element) brand.getFirstChild();

				Node dup = doc.importNode(be, true);

				doc.getDocumentElement().appendChild(dup);
			}
	    }
	    catch(Exception e)
	    {
			exceptions.add("add brand error: " + e.toString());
		}

  }

  protected void setUniverse()
  {
	  int uint = 0;

	  String column = "uni_lf_idx";

	  String sql = "SELECT " + column + " FROM universe WHERE uni_url = ?";

	  ArrayList<String> binds = new ArrayList<String>();


	  if(server != null)
	  {
		 binds.add(server);

		 HashMap<String, ArrayList<String>> uMap = dbreader.executeQuery(sql, binds);

		 //using a for loop to guard against the possibility of a null pointer.
		 for(String k : uMap.keySet())
		 {
			 if(k.equals(column))
			 {
				 //the universe root leaf is the first entry returned from universe.uni_lf_idx.
				 uint = Integer.parseInt(uMap.get(column).get(0));
			 }
		 }
	  }

	  universe = uint;
  }

  protected String getSQLFromFile(String filestr, HashMap<String, String> replacements)
  {
	  StringBuffer returnBuffer = new StringBuffer("");

	  try
	  {

		  StringBuffer pathBuffer = new StringBuffer(realPath);

	      pathBuffer.append("/");

		  pathBuffer.append(WashServlet.SQLPATH);

		  pathBuffer.append("/");

		  pathBuffer.append(filestr);

		  //System.out.println("opening -> " + pathBuffer.toString());

			File file = new File(pathBuffer.toString());
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null)
			{
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			fileReader.close();


			String sql = this.doReplacements(stringBuffer.toString(), replacements);


			returnBuffer.append(sql);
	  }
	  catch(Exception e)
	  {
		  exceptions.add("couldn't open " + filestr + " -> java reports " + e.toString());
	  }

	  return returnBuffer.toString();
  }

  protected String doReplacements(String astring, HashMap<String, String> replacements)
  {

	  ArrayList<String> replaceList = new ArrayList<String>();

	  int i = 0;

	  replaceList.add(astring);

	  for(String r : replacements.keySet())
	  {
	  		//sql.replace(r, replacements.get(r));

	  		replaceList.add(replaceList.get(i).replaceAll(r, replacements.get(r)));

	  		i++;
	  }

	  //return the last entry in the replacement array.
	  //if no replacements, returns the unaltered sql string.
	  return replaceList.get(i);

  }

  protected Document getBrand()
  {
    //this method tries to open the xml file related to the current universe
    //and return the contents so they can be appended to the XML output that comes from the db.
    StringBuffer pathBuffer = new StringBuffer(realPath);

    String xmlString = getErrorXMLString();

    pathBuffer.append("/xml/");

    //the brand document is in a folder with the same name as the server (e.g., www.sudsopolis.com)
    pathBuffer.append(server);

    pathBuffer.append("/brand.xml");

    File file;

    Document doc;

    try
    {
       file = new File(pathBuffer.toString());


		try
		{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();




		if(file != null)
		{
			doc = dBuilder.parse(new File(pathBuffer.toString()));
		}
		else
		{
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlString));

			doc = dBuilder.parse(is);

		}


		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();



		}
		catch(Exception e)
		{
			exceptions.add(e.toString());
			doc = null;
		}


    }
    catch(Exception ie)
    {

		exceptions.add("brand file io exception: " + ie.toString());
		doc = null;
	}

    return doc;

  }

  protected String getErrorXMLString()
  {
	  StringBuffer returnBuffer = new StringBuffer("");

	  returnBuffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
	  returnBuffer.append("<brand>\n");
	  returnBuffer.append("</brand>\n");

	  return returnBuffer.toString();

  }

  protected Element simpleElement(String handle, String value)
  {
	  Element e = doc.createElement(handle);
	  e.appendChild(doc.createTextNode(value));
	  return e;
  }

  protected Element getDeviceTypeElement()
  {
	  if(deviceType==null)
	  {
		  deviceType=DeviceType.COMPUTER.getName();
	  }

	  return this.simpleElement(DEVICETAG, deviceType);
  }


  public Document getDocument()
  {

	  String rootStr;


	  //if calling routine didn't define a wrapper, use default wrapper.
	  if(rootTag == null)
	  {
		  rootStr = DEFAULTROOTTAG;
	  }
	  else
	  {
		  rootStr = rootTag;
	  }

	  //System.out.println("root tag: " + rootStr);

	  try
	  {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		doc = docBuilder.newDocument();
		//this tells you how to create a DOM tree.
		//http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
		Element rootElement = doc.createElement(rootStr);

		rootElement.appendChild(getDeviceTypeElement());

		doc.appendChild(rootElement);

		this.setXSLInstruction();

		//append the page context values from the PageContext convenience class.
		if(pagecontext != null)
		{

			Element contextElement = doc.createElement("context");

			HashMap<String, String> contextMap = pagecontext.getProperties();

			for(String pk : contextMap.keySet())
			{

				Element pce = doc.createElement(pk);

				pce.appendChild(doc.createTextNode(contextMap.get(pk)));

				contextElement.appendChild(pce);
			}

			Element trwe = doc.createElement(PageContext.TOROOTWRAPPER);

			//the toRoot value needs special handling on account of it's a tab-delimited list.
			for(String tr : pagecontext.getToRootList())
			{


				//create an element for each leaf heading back to root.
				Element tre = doc.createElement(PageContext.TOROOTLEAF);

				tre.appendChild(doc.createTextNode(tr));

				//append the to root leaf element to the to root wrapper element.
				trwe.appendChild(tre);
			}

			contextElement.appendChild(trwe);

			rootElement.appendChild(contextElement);
		}


		//System.out.println("created root element.");

        //call displayList rather than displayMap.keySet()
        //so that you process the leafs in the correct sort order.
        for(String elf_idx : displayList)
        {
			//System.out.println("elf-> " + elf_idx);

			//System.out.println("call to tg_name... ");

			//System.out.println(displayMap.get(elf_idx).getValue("tg_name"));


			Leaf leaf = displayMap.get(elf_idx);

			Element myElement = doc.createElement(displayMap.get(elf_idx).getValue("tg_name"));

			//debugging is easier if you add the lf_idx as an attribute
			//(but the stylesheet doesn't need it.)
			myElement.setAttribute("lf_idx", elf_idx);

			leaf.setElement(myElement);

			elementMap.put(elf_idx, myElement);



			//System.out.println(displayMap.get(elf_idx).getValue("tg_name"));



			if(leaf.hasChildren())
			{
				if(!leaf.getValue("lf_text").equals(""))
				{

					Element heading = doc.createElement("heading");

					heading.appendChild(doc.createTextNode(leaf.getValue("lf_text")));

					myElement.appendChild(heading);

					//System.out.println("heading-> " + leaf.getValue("lf_text"));

			    }
			}


			else
			{
				myElement.appendChild(doc.createTextNode(leaf.getValue("lf_text")));
			}




			//child of check.
			String parent = leaf.getValue("lf_lf_as_parent_idx");



			/*
			System.out.println("parent-> " + leaf.getValue("lf_lf_as_parent_idx"));

			if(displayMap.containsKey(parent))
			{
				System.out.println("parent found.");
		    }
		    else
		    {
				System.out.println("PARENT NOT FOUND.");
			}
			*/



			if(elementMap.containsKey(parent))
			{
				//System.out.println("inserting childjjj.");

				elementMap.get(parent).appendChild(myElement);

				/*

				Element parentElement = displayMap.get(parent).getElement();

				Element childElement = myElement;

				try
				{

				   parentElement.appendChild(childElement);
			    }
			    catch(Exception ce)
			    {
					System.out.println("appendChild() failed -> " + ce.toString());
				}

				*/

				//displayMap.get(parent).getElement().appendChild(myElement);

				//System.out.println("inserted child.");

			}




		}


		//now set append foreign children.
		for(String fidx : foreignList)
		{



			String ridx = foreignToRef.get(fidx);



			//this condition is here to avoid a null pointer value.
			//it should never evaluate to false.
			if(elementMap.containsKey(ridx) && elementMap.containsKey(fidx))
			{
			    	elementMap.get(ridx).appendChild(elementMap.get(fidx));

			}

		}



		System.out.println("made it through for loop.");

		//append the zero element to the root element.
		//the for loop above already attached all decendants to the zero element.
		rootElement.appendChild(elementMap.get(zero));

		//if pathToRoot has been set, use the pathToRoot HashMap<String, String> to make those tags.
		if(pathToRootList.size()>0)
		{

			//Element heading = doc.createElement("heading");

			//heading.appendChild(doc.createTextNode(leaf.getValue("lf_text")));

			//myElement.appendChild(heading);

			Element pathToRootElement = doc.createElement("pathtoroot");

			for(String k : pathToRootList)
			{
				Element leafNode = doc.createElement("leaf");

				Element idxp = doc.createElement("idxp");

				idxp.appendChild(doc.createTextNode(k));

				Element heading = doc.createElement("heading");

				heading.appendChild(doc.createTextNode(pathToRootMap.get(k)));

				leafNode.appendChild(idxp);

				leafNode.appendChild(heading);

				pathToRootElement.appendChild(leafNode);


			}

			rootElement.appendChild(pathToRootElement);
		}

		this.addBrand();

		//add branding information from the universe xml file to the root element.
		//Document brand = this.getBrand();

		//if(brand !=null)
		//{
			//rootElement.appendChild(null);
		//	String xyz = "123";
		//}


	  }
	  catch (Exception pce)
	  {
	  		exceptions.add("getDocument:" + pce.toString());

	  		doc = null;
	  }

	  return doc;

  }

    protected void setXSLInstruction()
    {

		String asshref;

		//if no stylesheet assiged from the db call, use the default
		if(sshref != null)
		{
			asshref = sshref;
		}
		else
		{
			if(defaultXSL != null)
			{
		 	   asshref = defaultXSL;
		    }
		    //precaution against a null pointer exception.
		    else
		    {
				asshref = "";
			}
		}

		if(includeSSHREF)
		{


		  //path to xsl is tomcat's path, not the real path.
		  if(defaultXSL != null && xslPath != null)
		  {

		    Element root = doc.getDocumentElement();

		    Node pi = doc.createProcessingInstruction("xml-stylesheet","type=\"text/xsl\" href=\"" + xslPath + "/" + asshref + "\"");
		    doc.insertBefore(pi, root);
	      }

		}


	}



    public LeafHandler setServer(String aserver)
    {
		//server is the root url from the http request.
		server = aserver;

		return this;

	}

	public LeafHandler setRealPath(String arealPath)
	{
		realPath = arealPath;

		return this;
	}

	public boolean isInteger( String input )
	{

	   //expandFromIdx calls this method to see if it's expanding from a numeric value (lf_idx)
	   //or a String value (lf_nickname)
	   boolean returnBoolean;

	   try
	   {
		  Integer.parseInt( input );
		  returnBoolean = true;
	   }
	   catch( Exception e)
	   {
		  returnBoolean = false;
	   }

	   return returnBoolean;
	}

  public ArrayList<String> getExceptions()
  {
	  return exceptions;
  }



protected Document doc;

HashMap<String, Leaf> displayMap = new HashMap<String, Leaf>();

HashMap<String, Leaf> universeMap = new HashMap<String, Leaf>();

protected HashMap<String, Element> elementMap = new HashMap<String, Element>();

//preserve the leaf sort order.
ArrayList<String> displayList = new ArrayList<String>();

ArrayList<String> universeList = new ArrayList<String>();

protected String zero;

protected String rootTag;

protected static final String DEFAULTROOTTAG = "page";

//these three constants represent duty
public static final String PAGE = "page";
public static final String TOC = "toc";
public static final String BROWSE = "browse";

//a zero can't be < 1, so use this constant to indicate the absence of a zero value.
public static final String NOZERO = "-1";

//tells expandFromIdx method if you're expanding from the zero lf_idx or the universe lf_idx
public static final int DISPLAY = 0;
public static final int UNIVERSE = 1;

protected ArrayList<String> foreignList = new ArrayList<String>();

//stylesheet values from settings table...

protected String sshref;

protected String sstype;

protected String xmlencoding;

protected String xmlversion;

protected boolean includeSSHREF = true;

protected String server;

protected String realPath;

protected String xslPath;

protected String defaultXSL;

protected int universe = 0;

protected int expandCount = 0;

protected PageContext pagecontext;

//end of stylesheet values from settings table.

DBWriter dbreader;

ArrayList<String> exceptions = new ArrayList<String>();

HashMap<String, String> foreignToRef = new HashMap<String, String>();

//list preserves sort order of pathToRootMap.
ArrayList<String> pathToRootList = new ArrayList<String>();

HashMap<String, String> pathToRootMap = new HashMap<String, String>();

//empty HashMap for doReplacements() method.
protected static final HashMap<String, String> NOREPLACEMENTS = new HashMap<String, String>();

protected String duty;

protected String deviceType;

public static final String DEVICETAG = "device";


}
