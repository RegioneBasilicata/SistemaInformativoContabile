package it.latraccia.utility;







import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;







public class StringFormat {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy", Locale.ITALIAN);

	public static String NULLToString(String valore) {
		if(valore != null) {
			return valore;
		} else {
			return "";
		}
	}
	public static String getDayOfWeek(String date) {
		String aMesi[] = new String[12];
		Calendar cal = new GregorianCalendar(Locale.ITALIAN);
		aMesi[1] = "Domenica";
		aMesi[2] = "Lunedì";
		aMesi[3] = "Martedì";
		aMesi[4] = "Mercoledì";
		aMesi[5] = "Giovedì";
		aMesi[6] = "Venerdi";
		aMesi[7] = "Sabato";
		cal.set(Integer.parseInt(date.substring(6)), Integer.parseInt(date.substring(3, 5)) - 1, Integer.parseInt(date.substring(0, 2)));
		return aMesi[cal.get(Calendar.DAY_OF_WEEK)];
	}
	public static Date toDate(String dateString, String dateFormatPattern) throws Exception {
		Date date = null;
		if (dateFormatPattern == null) {
			dateFormatPattern = "yyyy-MM-dd";
		}
		if (dateString != null && dateString.length() > 0) {
			synchronized (dateFormat) {
				dateFormat.applyPattern(dateFormatPattern);
				dateFormat.setLenient(false);
				try {
					date = dateFormat.parse(dateString);
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new Exception();
				}

			}
		}
		return date;
	}
	public static String DateNowItaliana() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALY);
		return sdf.format(new Date());

	} 
	public static String DateNowItaliana(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ITALY);
		return sdf.format(new Date());

	} 

	public static String getLastDayOfMonth(String mese_anno) {
		String day = "";
		Calendar cal = GregorianCalendar.getInstance();
		try {
			cal.setTime(toDate(mese_anno,"MM/yyyy"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		day = Integer.toString(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return day;
	}
	public static String dateToString(java.util.Date date) {
		if (date != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(date);
		} else
			return null;

	}
	
	public static String dateToString(java.util.Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(date);
        } else
            return null;

    }
	 public static boolean isValidDate(String dateString, String dateFormatPattern) {
	        Date validDate = null;
	        synchronized (dateFormat) {
	            try {
	                dateFormat.applyPattern(dateFormatPattern);
	                dateFormat.setLenient(false);
	                validDate = dateFormat.parse(dateString);
	            } catch (ParseException e) {
	               
	                return false;
	            }
	        }
	        return validDate != null;
	    }
	 
	 public static Timestamp toTimestamp(String dateString, String pattern) {
	        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ITALY);
	        Date greg = null;
	        try {
	        	greg = dateFormat.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			} 
	        Timestamp jsqlD = new Timestamp(greg.getTime());
	        return jsqlD;
	    }
	 
	 public static String replaceInString(String in, String from, String to) {
	        if (in == null || from == null || to == null) {
	            return in;
	        }

	        StringBuffer newValue = new StringBuffer();
	        char[] inChars = in.toCharArray();
	        int inLen = inChars.length;
	        char[] fromChars = from.toCharArray();
	        int fromLen = fromChars.length;

	        for (int i = 0; i < inLen; i++) {
	            if (inChars[i] == fromChars[0] && (i + fromLen) <= inLen) {
	                boolean isEqual = true;
	                for (int j = 1; j < fromLen; j++) {
	                    if (inChars[i + j] != fromChars[j]) {
	                        isEqual = false;
	                        break;
	                    }
	                }
	                if (isEqual) {
	                    newValue.append(to);
	                    i += fromLen - 1;
	                } else {
	                    newValue.append(inChars[i]);
	                }
	            } else {
	                newValue.append(inChars[i]);
	            }
	        }
	        return newValue.toString();
	    }
	 public static int confrontaDate(String data1, String data2)  {
	        int ret = -1000; //?
	        SimpleDateFormat sdf;
	        data1 = replace(data1, "/", "-");
	        data2 = replace(data2, "/", "-");

	        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALY);
	        Date myDate = null;
	        Date myDate2 = null;
	        try {
	        	myDate = sdf.parse(data1);
	            myDate2 = sdf.parse(data2);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	        if (!(myDate.after(myDate2) || myDate.before(myDate2))) {
	            ret = 0;
	        }
	        if (myDate.after(myDate2)) {
	            ret = 1;
	        }
	        if (myDate.before(myDate2)) {
	            ret = -1;
	        }

	        return ret;
	    }
	 
	 public static String replace(String s, String f, String r) {
	        if ("".equalsIgnoreCase(f))
	            return s;
	        if (s == null)
	            return s;
	        if (f == null)
	            return s;
	        if (r == null)
	            r = "";

	        int index01 = s.indexOf(f);
	        while (index01 != -1) {
	            s = s.substring(0, index01) + r + s.substring(index01 + f.length());
	            index01 += r.length();
	            index01 = s.indexOf(f, index01);
	        }
	        return s;
	    }
	 
	 /**
		 * 
		 * @return il numero del mese attuale
		 * @author c.roncio
		 */
	 	public static int getNumberMonth(){
	 	    Calendar calendar = new GregorianCalendar();
	 	    return calendar.get(Calendar.MONTH)+1;
	 	}
	 	
		/**
		 *  	
		 * @return Restituisce l'anno mese corrente
		  * @author c.roncio
		*/
	 	public static int getNumberYear(){
	 	    Calendar calendar = new GregorianCalendar();
	 	    return calendar.get(Calendar.YEAR);
	 	}

	 	/**
	 	 * Data una data restituisce il suo anno
	 	 * @param date
	 	 * @return
	 	 */
	 	public static int getNumberYear(String date){
		     Calendar cal = new GregorianCalendar(Locale.ITALIAN);
		     cal.set(Integer.parseInt(date.substring(6)), Integer.parseInt(date.substring(3, 5)) - 1, Integer.parseInt(date.substring(0, 2)));
		     return cal.get(Calendar.YEAR);

	 	}
	 	
	 	 /**
		  * 
		  * @param date: la data nel formato "gg/mm/yyyy"
		  * @return il numero del mese
		  * 
		  */
		 public static int getNumberMonth(String date){
		     Calendar cal = new GregorianCalendar(Locale.ITALIAN);
		     cal.set(Integer.parseInt(date.substring(6)), Integer.parseInt(date.substring(3, 5)) - 1, Integer.parseInt(date.substring(0, 2)));
		     return cal.get(Calendar.MONTH)+1;
		 }
}
