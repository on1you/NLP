package main.common;

import java.util.StringTokenizer;

import org.htmlparser.Parser;
import org.htmlparser.Text;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.util.NodeList;

public class StringUtil {
	public static String[] toArray(String __str, String __delim) {
		if ((__str == null) || (__delim == null)) {
			return null;
		}

		StringTokenizer st = new StringTokenizer(__str, __delim);
		int nCount = st.countTokens();
		if ((st == null) || (nCount <= 0)) {
			return null;
		}
		String[] astrToken = new String[nCount];
		for (int i = 0; i < nCount; ++i) {
			astrToken[i] = st.nextToken().trim();
		}
		return astrToken;
	}

	public static String toUtf8String(String s) {
		if (s == null) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if ((c >= 0) && (c <= 255)) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = Character.toString(c).getBytes("utf-8");
				} catch (Exception ex) {
					System.out.println(ex);
					b = new byte[0];
				}
				for (int j = 0; j < b.length; ++j) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}
	
	public  static String   html2Text( String s )
    {
        try
        {
            Parser  parser  =   Parser.createParser( s, "UTF-8" );
            NodeList  nodes   =   parser.extractAllNodesThatMatch( new NodeClassFilter(Text.class) );
            StringBuffer    sb  =   new StringBuffer();
            for( int i=0; null!=nodes && i<nodes.size(); i++ )
                sb.append( nodes.elementAt(i).getText() );
            return sb.toString().replaceAll( "&nbsp;", " ")
            		.replaceAll( "　", " " )
            		.replaceAll( "\\s+", " " ).replaceAll( "^\\s+", "" );
        }
        catch( Exception e )
        {
            System.err.println( e );
        }
        return  s;
    }
	
	public static boolean isEmpty(String value) {
		return ((value == null) || (value.trim().equals("")));
	}
	public static boolean isString(String s) {
		 return ((s != null) && (s.length() > 0));
	}
}
