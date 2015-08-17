package com.washateria.www.wash;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import washateria.utils.EscapeStrings;

public class DBWriter
{

public DBWriter()
{

}


public DBWriter setDriver(String adriver)
{
	driver = adriver;

	return this;

}

public DBWriter setConnectionURL(String aconnectionURL)
{
	connectionURL = aconnectionURL;

	return this;
}

public DBWriter setConnectionUser(String aconnectionUser)
{
	connectionUser = aconnectionUser;

	return this;
}

public DBWriter setConnectionPW(String aconnectionPW)
{
	connectionPW = aconnectionPW;
	return this;
}


public void connect()
{
	try
	{

	   Class.forName(driver).newInstance();

	   conn=DriverManager.getConnection(connectionURL, connectionUser, connectionPW);
    }
    catch(Exception e)
    {
		exceptions.add(new WashException("Failed on connect", e.toString()));
	}
}


public void disconnect()
{
	try
	{
	   conn.close();
    }
    catch(Exception e)
    {
		exceptions.add(new WashException("Failed on disconnect", e.toString()));
	}
}

public String executeQuery(String sql, ArrayList<String> values, String delim)
{
	//this method executes a SELECT and returns a String delimited by delim with an end-of-line value of \n\
	//it can be invoked with the NOBINDS constant as the second argument if no substitution is needed.
	PreparedStatement statement = null;

	ResultSet rs;

	EscapeStrings escaper = new EscapeStrings();

	StringBuffer returnBuffer = new StringBuffer("");

	try
	{
	   statement = conn.prepareStatement(sql);

		//binding is one based.
		int counter = 1;

		for(String value : values)
		{
			 statement.setString(counter, value);

			 counter++;
		}

		rs = statement.executeQuery();

	    ArrayList<String> columns = this.getColumnNames(rs);

        int colcount;

		while(rs.next())
		{

			colcount = 0;

			for(String c : columns)
			{

				//usual check to see if delimter is required on this iteration.
				if(colcount>0)
				{
					returnBuffer.append(delim);
				}

				//SQL NULL results in a java null, so substitute a blank string for that.
				if(rs.getString(c)==null)
				{
				   	returnBuffer.append("");
				}
				else
				{
					returnBuffer.append(rs.getString(c));
					//returnBuffer.append(StringEscapeUtils.ESCAPE_XML.with( NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE) ););

				}

				colcount++;
			}

			//insert hard return after each record.
			returnBuffer.append(ENDOFLINE);
		}

    }
    catch(Exception e)
    {
		exceptions.add(new WashException("Failed on prepare. SQL: " + sql , e.toString()));
	}


	return returnBuffer.toString();


}

public HashMap<String, ArrayList<String>> executeQuery(String sql, ArrayList<String> values)
{

	//this method executes a SELECT and returns a HashMap of ArrayList<String> keyed on the column name.

	//first parse the column names from the sql statement.
	//getColumns() is unreliable.  using getColumnNames() instead.
	//ArrayList<String> columns = getColumns(sql);

    HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();

    EscapeStrings escaper = new EscapeStrings();


	PreparedStatement statement = null;

	ResultSet rs;


    try
    {

		statement = conn.prepareStatement(sql);

		//binding is one based.
		int counter = 1;

		for(String value : values)
		{
			 statement.setString(counter, value);

			 counter++;
		}

		rs = statement.executeQuery();


	ArrayList<String> columns = this.getColumnNames(rs);

    for(String cn : columns)
    {
		resultMap.put(cn, new ArrayList<String>());
	}


		while(rs.next())
		{
			for(String c : columns)
			{
				//SQL NULL results in a java null, so substitute a blank string for that.
				if(rs.getString(c)==null)
				{
				   	resultMap.get(c).add("");
				}
				else
				{
					resultMap.get(c).add(rs.getString(c));
				}
			}
		}

    }
    catch(Exception e)
    {
		exceptions.add(new WashException("Failed on prepare. SQL:" + sql , e.toString()));
	}

	return resultMap;


}



public ArrayList<String> getColumnNames(ResultSet rs) throws SQLException
{

	ArrayList<String> columns = new ArrayList<String>();

	ResultSetMetaData rsMetaData = rs.getMetaData();
	int numberOfColumns = rsMetaData.getColumnCount();


	// get the column names; column indexes start from 1
	for (int i = 1; i < numberOfColumns + 1; i++)
	{

	  columns.add(rsMetaData.getColumnLabel(i));
	  //String columnName = rsMetaData.getColumnName(i);
	  // Get the name of the column's table name
	  //String tableName = rsMetaData.getTableName(i);
	  //System.out.println("column name=" + columnName + " table=" + tableName + "");

	}

	return columns;
}

//this is not reliable for getting column names because it splits on the comma, which might be part of a function call.
//use getColumns(ResultSet rs) instead
public ArrayList<String> getColumns(String sql)
{
	//this method parses out the column names from the sql statement.

	ArrayList<String> columns = new ArrayList<String>();

    //first split on SELECT at the beginning of the statement.
    //using java RE
    String[] selectSplit = sql.split("^\\s*(?i)select\\s+");

    if(selectSplit.length>1)
    {

        //split on FROM preceded and followed by whitespace
        //using java RE
        String[] fromSplit = selectSplit[1].split("\\s+(?i)from\\s+");

        if(fromSplit.length>1)
        {

			//split on the comma separating the column names.
			String[] commaSplit = fromSplit[0].split(",");

			for(String s : commaSplit)
			{
				//look for a column alias
				String[] aliasSplit = s.split("\\s+(?i)as\\s+");

				//if there's a column alias, that is the column heading
				//otherwise it's just the column name.
				if(aliasSplit.length>1)
				{
					columns.add(aliasSplit[1].trim());
				}
				else
				{

				   //trim the column names.
				   columns.add(s.trim());

			    }
			}

		}


    }



	return columns;

}


public void execute(String sql, ArrayList<String> values)
{
	PreparedStatement statement = null;

	int result;

    try
    {

		statement = conn.prepareStatement(sql);

		//binding is one based.
		int counter = 1;

		for(String value : values)
		{
			 statement.setString(counter, value);

			 counter++;
		}

		result = statement.executeUpdate();

    }
    catch(Exception e)
    {
		exceptions.add(new WashException("Failed on action sql -> " + sql, e.toString()));
	}


}

public ArrayList<WashException> getExceptions()
{
	return exceptions;
}

public ArrayList<String> getFeedback()
{
	return feedback;
}

protected String driver;
protected String connectionURL;
protected String connectionUser;
protected String connectionPW;

protected ArrayList<WashException> exceptions = new ArrayList<WashException>();
protected ArrayList<String> feedback = new ArrayList<String>();

protected Connection conn;

//if calling routine has nothing to bind, it can use this constant instead of sending an empty ArrayList<String>.
public static final ArrayList<String> NOBINDS = new ArrayList<String>();

public static final String ENDOFLINE = "\n";

}


