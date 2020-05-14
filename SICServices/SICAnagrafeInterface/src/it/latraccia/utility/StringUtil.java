package it.latraccia.utility;

public class StringUtil {
	public static String escapeApici(String s) {
		if  (s!=null)
			return s.replace("'", "''");
		else
			return "";
	}
}
