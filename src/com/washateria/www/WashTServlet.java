package com.washateria.www.wash;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Map;


import com.washateria.www.builder.XMLInput;
import com.washateria.www.builder.XMLRow;

public class WashTServlet extends HttpServlet
{

  public void doGet(HttpServletRequest req,
                    HttpServletResponse res)
    throws ServletException, IOException
  {

     String realSettingsPath = getServletContext().getRealPath(SETTINGSDIR);
     String realSQLPath = getServletContext().getRealPath(SQLPATH);


     File settingsDir = new File(realSettingsPath);

	 if(!settingsDir.isDirectory())
	 {
			throw new ServletException(SETTINGSDIR +" is not a directory.");
	 }

     File settingsFile = new File(realSettingsPath + "/" + SETTINGSFILE);

     XMLInput xmlinput = new XMLInput();

     xmlinput.setFile(settingsFile);

     xmlinput.parse();

     String wdriver = xmlinput.getValue("settings", "connection", "driver");

     String wurl = xmlinput.getValue("settings", "connection", "url");

     String wuser = xmlinput.getValue("settings", "connection", "credentials", "read", "user");

     String wpw = xmlinput.getValue("settings", "connection", "credentials", "read", "pw");


     DBWriter writer = new DBWriter();

     writer.setDriver(wdriver);

     writer.setConnectionURL(wurl);

     writer.setConnectionUser(wuser);

     writer.setConnectionPW(wpw);

     writer.connect();


     String block = xmlinput.getValue("settings", "text", "tags", "block", "name");

     String xslpath = xmlinput.getValue("settings", "local", "xslpath");

     String xsldefault = xmlinput.getValue("settings", "local", "xsldefault");

     PrintWriter out = res.getWriter();

     /*

     out.println("Hello from the wash directory, everybody.");

     out.println("block tag: " + block);

     out.println("path: " + realSettingsPath);

     */

	 Map<String, String> env = System.getenv();

   /*
   out.println("env test");

   out.println("req uri-> " + req.getRequestURI());

   out.println("req url-> " + req.getRequestURL());

   out.println("host-> " + req.getRemoteHost());

   out.println("addr-> " + req.getRemoteAddr());

   out.println("server-> " + req.getServerName());

   out.println("xslpath-> " + xslpath);

   out.println("defaultxsl-> " + xsldefault);

   out.println("sql path-> " + realSQLPath);

   out.println("time to expand.");
   */

     reqHash = (HashMap<String, String[]>) req.getParameterMap();

     /*
     for(String k : reqHash.keySet())
     {
		 out.println(k + "->" + reqHash.get(k)[0]);
	 }

	 */

	 if(reqHash.containsKey("zero"))
	 {
		 zero=reqHash.get("zero")[0];
	 }
	 else
	 {
		 zero=LeafHandler.NOZERO;
	 }

/*
     UserAgent userAgent = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
     OperatingSystem os = userAgent.getOperatingSystem();
     DeviceType dtype = os.getDeviceType();
     //String dname = dtype.getName();

     if(dtype==DeviceType.COMPUTER)
     {

         out.println("device is computer.");

     }
     else
     {
		 out.println("device is not computer.");
	 }
*/

     DeviceTypeHandler dthandler = new DeviceTypeHandler(req.getHeader("User-Agent"));

     String dtName = dthandler.getDeviceType();


	 //default to page view if there's a zero.
	 //default to browse view if no zero.
	 if(reqHash.containsKey("duty"))
	 {
		 duty = reqHash.get("duty")[0];
	 }
	 else
	 {
		 if(!zero.equals(LeafHandler.NOZERO))
		 {
		    duty = LeafHandler.PAGE;
	     }
	     else
	     {
			duty = LeafHandler.BROWSE;
		 }
	 }

       if(duty.equals(LeafHandler.BROWSE))
       {
		    handler = new LeafHandler(writer).setRootTag("browse").setServer("www.sudsopolis.com").setRealPath("/data/wash").setXSLPath("./xsl").setDefaultXSL("wash.xsl").setDeviceType("mobile");

	   }
	   else
	   {
	        handler = new LeafHandler(writer).setZero(zero).setRootTag("page").setServer("www.sudsopolis.com").setRealPath("/data/wash").setXSLPath("//www.sudsopolis.com/wash/xsl").setDefaultXSL("wash.xsl").setDeviceType("tablet");
	   }

	   //populate collections in LeafHandler
	   handler.expand();

	   //close db connection.
	   writer.disconnect();

       String output = handler.getDocumentAsString();

       //out.println("output...");

       out.println(output);


     out.close();

  }

  public void handleRequest(HttpServletRequest req)
  {
  //this method works as long as each param has a single value.

	        Enumeration<String> headerNames = req.getHeaderNames();

	        while (headerNames.hasMoreElements())
            {

	            String headerName = headerNames.nextElement();

	            Enumeration<String> headers = req.getHeaders(headerName);
	            while (headers.hasMoreElements())
                {
	                String headerValue = headers.nextElement();
                        headerHash.put(headerName, headerValue);

	            }

	        }




   }


  public void doPost (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
    {
         doGet(req, res);
    }


public static final String SETTINGSDIR = "../builder/WEB-INF/conf";
public static final String SETTINGSFILE = "settings.xml";
public static final String SQLPATH = "WEB-INF/sql";

private String duty;
private String zero;

private HashMap<String, String[]> reqHash = new HashMap<String, String[]>();
private HashMap<String, String> headerHash = new HashMap<String, String>();

private LeafHandler handler;

}
