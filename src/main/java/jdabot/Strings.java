package jdabot;

import net.dv8tion.jda.core.entities.User;

public class Strings {
	
	public static final String[] disconnectFlavorText = {
			"sorry, we're closed",
			"shutting down",
			"I don't blame you",
			"I don't hate you",
			"no hard feelings",
			"whyy",
			"critical error",
			"good night",
			"sleep mode activated",
			"your business is appreciated",
			"hibernating",
			"resting",
			"nap time"
	};
	
	public static final String[] saltWords = {
			"fuck",
			"hax",
			"hack",
			"cheat",
			"noob",
			"nub",
			"n00b",
			"shit",
			"stupid",
			"scrub",
			"skrub",
			"scam",
			"gay"
	};
	
	public static final String[] sadWords = {
			"):", ":/", ":[", "]:", ")=",
			"sadness",
			"sad",
			"depression",
			"depressed",
			"sorrow"
	};
	
	public static final String youtubeUrl = "https://www.youtube.com/watch?v=";
	
	public static final String[] selfWords = { "i", "im", "ive", "ill", "me" };
	
	public static final String cuffTrigger = "cuffs";
	
	public static final String sadMsg = "Oh no! % is sad. Have a kitty!";
	
	public static final String[] saltFlavorText = {
			"Receiving a shipment from %",
			"Detected an incoming shipment from %",
			"% is sending a shipment",
			"Shipment detected arriving from %",
			"Shipment from % has been successfully delivered"
	};
	
	public static final String saltPath = "res/salt.png";
	public static final String cuffsPath = "res/cuffs.gif";
	public static final String kittyFolder = "res/kitty";
	
	public static boolean isStringSalty(String str)
	{
		String parts[] = str.split(" ");
		float n = 0;
		
		for (int i = 0; i < parts.length; i++)
		{
			for (int j = 0; j < saltWords.length; j++)
			{
				if (parts[i].toLowerCase().indexOf(saltWords[j]) != -1)
				{
					n++;
					if (parts[i].toUpperCase().equals(parts[i]))
					{
						// all caps = extra salt
						n++;
					}
				}
			}
		}

		return n / parts.length > 0.4;
	}
	
	public static boolean isStringSad(String str)
	{
		if (str.length() >= 100) return false;
		
		String[] parts = str.split(" ");
		boolean sad = false;
		boolean self = false;
		for (int i = 0; i < parts.length; i++)
		{
			for (int j = 0; j < sadWords.length; j++)
			{
				if (sad) break;
				if (parts[i].equals(sadWords[j]))
				{
					sad = true;
				}
			}
			
			for (int j = 0; j < selfWords.length; j++)
			{
				if (self) break;
				if (parts[i].replaceAll("'", "").toLowerCase().equals(selfWords[j]))
				{
					self = true;
				}
			}
			
			if (sad && self) return true;
		}
		return false;
	}
	
	public static String constructSaltShipment(User user)
	{
		int index = (int) Math.floor(Math.random() * saltFlavorText.length);
		String txt = saltFlavorText[index];
		int phIndex = txt.indexOf("%");
		txt = txt.substring(0, phIndex) + user.getAsMention() + txt.substring(phIndex+1);
		
		return txt;
	}
	
	public static String getDisconnectFlavorText()
	{
		int index = (int) Math.floor(Math.random() * disconnectFlavorText.length);
		return disconnectFlavorText[index];
	}
	
	public static boolean testCubeStr(String in)
	{
		String str = in.trim();
		if (str.length() <= 1 || str.length() % 2 == 0)
		{
			return false;
		}
		for (int i = 0; i < str.length(); i++)
		{
			if ((i % 2 == 0 && str.charAt(i) == ' ') ||
					(i % 2 == 1 && str.charAt(i) != ' '))
			{
				return false;
			}
		}
		
		return true;
	}

	public static String cubeString(String in)
	{
		String str = in.replace(" ", "");
		int len = str.length();
		if (len == 2)
		{
			return cubeString2Char(str);
		}
		String cube = "";
		cube += getBlanks(((len - 1) * 2) - 3);
		
		// back top edge
		for (int i = len - 1; i >= 0; i--)
		{
			cube += getBlanks(3);
			cube += str.charAt(i);
		}
		cube += "\n";
		
		int nextBackRightEdge = 0;
		int nextBottomRightEdge = len - 2;
		
		// left/right top edges
		for (int i = len - 2; i >= 1; i--)
		{
			cube += getBlanks(i * 2);
			cube += str.charAt(i);
			cube += getBlanks(((4 * (len - 1)) - 1));
			cube += str.charAt(len - 1 - i);
			
			if ((i + len) % 2 == 1 && ++nextBackRightEdge < len)
			{
				cube += getBlanks(((len - 1) * 2) - 1 - (i * 2));
				cube += str.charAt(nextBackRightEdge);
			}
			cube += "\n";
		}
		
		// front top edge
		for (int i = 0; i < len; i++)
		{
			cube += str.charAt(i);
			if (i != len - 1)
			{
				cube += getBlanks(3);
			}
			else if (len % 2 == 1 && ++nextBackRightEdge < len)
			{
				cube += getBlanks(((len - 1) * 2) - 1);
				cube += str.charAt(nextBackRightEdge);
			}
		}
		
		// front left/right edges
		
		for (int i = 1; i < len - 1; i++)
		{
			cube += "\n";
			if (len % 2 == 0)
			{
				if (++nextBackRightEdge < len)
				{
					cube += getBlanks((len - 1) * 6);
					cube += str.charAt(nextBackRightEdge);
				}
				else if (nextBottomRightEdge % 2 == 1)
				{
					cube += getBlanks((4 * (len - 1)) + (nextBottomRightEdge * 2));
					cube += str.charAt(nextBottomRightEdge--);
				}
			}
			else if (nextBackRightEdge >= len - 1 && nextBottomRightEdge % 2 == 1)
			{
				cube += getBlanks((4 * (len - 1)) + (nextBottomRightEdge * 2));
				cube += str.charAt(nextBottomRightEdge--);
			}
			cube += "\n";
			
			
			cube += str.charAt(i);
			cube += getBlanks((4 * (len - 1)) - 1);
			cube += str.charAt(len - 1 - i);
			
			if (len % 2 == 1)
			{
				if (++nextBackRightEdge < len)
				{
					cube += getBlanks(((len - 1) * 2) - 1);
					cube += str.charAt(nextBackRightEdge);
				}
				else if (nextBottomRightEdge % 2 == 0)
				{
					cube += getBlanks((nextBottomRightEdge * 2) - 1);
					cube += str.charAt(nextBottomRightEdge--);
				}
			}
			else if (nextBackRightEdge >= len - 1 && nextBottomRightEdge % 2 == 0)
			{
				cube += getBlanks((nextBottomRightEdge * 2) - 1);
				cube += str.charAt(nextBottomRightEdge--);
			}
		}
		cube += "\n";
		cube += getBlanks((4 * (len - 1)) + (nextBottomRightEdge * 2));
		cube += str.charAt(nextBottomRightEdge--);
		cube += "\n";
		
		// front bottom edge
		for (int i = len - 1; i >= 0; i--)
		{
			cube += str.charAt(i);
			if (i != 0)
			{
				cube += getBlanks(3);
			}
		}
		
		return cube;
	}
	
	private static String getBlanks(int n)
	{
		return (n > 1) ? " " + getBlanks(n - 1) : " ";
	}
	
	private static String cubeString2Char(String str)
	{
		String cube = "";
		cube += getBlanks(2) + str.charAt(0) + getBlanks(3) + str.charAt(1) + "\n";
		cube += str.charAt(0) + getBlanks(3) + str.charAt(1) + "\n";
		cube += getBlanks(6) + str.charAt(1) + "\n";
		cube += str.charAt(1) + getBlanks(3) + str.charAt(0);
		return cube;
	}
	
	public static String getYoutubeUrl(String id)
	{
		return youtubeUrl + id;
	}
}