package com.washateria.www.wash;

import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.DeviceType;

public class DeviceTypeHandler
{


	public DeviceTypeHandler(String auserAgentString)
	{
         userAgentString = auserAgentString;
	}

	public String getDeviceType()
	{
		String returnString;

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
        OperatingSystem os = userAgent.getOperatingSystem();
        DeviceType dtype = os.getDeviceType();

        if(dtype == DeviceType.TABLET)
        {
			returnString = TABLET;
		}
		else if(dtype == DeviceType.MOBILE)
		{
			returnString = MOBILE;
		}
		else
		{
			returnString = DEFAULTTYPE;
		}


		return returnString;

	}



protected String userAgentString;

//these are the same constants that are in eu.bitwalker.useragentutils.DeviceType
//defining our own constants here in case the values of constnats in DeviceType change.
public static final String COMPUTER = "computer";

public static final String DMR = "dmr";

public static final String GAME_CONSOLE = "game";

public static final String TABLET = "tablet";

public static final String MOBILE = "mobile";

public static final String UNKNOWN = "unknown";

public static final String WEARABLE = "wearable";

//end of same constants in eu.bitwalker.useragentutils.DeviceType.

public static final String DEFAULTTYPE = COMPUTER;

}
