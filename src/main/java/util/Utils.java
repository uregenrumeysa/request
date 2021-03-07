package util;


import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class Utils
{
  public static String encriyptMD5(String value) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(value.getBytes());
    byte[] digest = md.digest();
    String hash = DatatypeConverter
            .printHexBinary(digest);
    return hash.toLowerCase();
  }
  public static String getDateStringAsISO8601(Date date)
  {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    String dateAsISO = df.format(date);
    return dateAsISO;
  }
  
  public static Date getDateFromString(String dateAsISO)
  {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    
    ParsePosition pp = new ParsePosition(0);
    Date date = df.parse(dateAsISO, pp);
    if ((pp.getIndex() < dateAsISO.length()) || (date == null)) {
      date = new Date();
    }
    return date;
  }
  
  public static Calendar toCalendar(String iso8601string)
  {
    Calendar calendar = GregorianCalendar.getInstance();
    try
    {
      String s = iso8601string.replace("Z", "+00:00");
      s = s.substring(0, 22) + s.substring(23);
      
      Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
      calendar.setTime(date);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return calendar;
  }
}
