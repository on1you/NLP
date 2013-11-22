package main.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JdomUtil {
	public static final String XMLPATHSEP = "/";

	protected static Element _findElement(Element eleParent, String[] astrPath, int nStartIndex) {
		if ((astrPath == null) || (nStartIndex > astrPath.length)) {
			return null;
		}
		Element eleChild = eleParent;
		for (int i = nStartIndex; i < astrPath.length; ++i) {
			eleChild = eleChild.getChild(astrPath[i]);
			if (eleChild == null)
				break;
		}
		return eleChild;
	}

	protected static Element _findElement(Element eleParent, String[] astrPath) {
		return _findElement(eleParent, astrPath, 0);
	}

	protected static Element _findElement(Element eleParent, String strPath) {
		String[] astrPath = StringUtil.toArray(strPath, "/");
		return _findElement(eleParent, astrPath);
	}

	protected static Element _findElement(Document document, String strPath) {
		String[] astrPath = StringUtil.toArray(strPath, "/");
		if ((astrPath == null) || (astrPath.length <= 0))
			return null;
		Element eleRoot = document.getRootElement();

		if (strPath.startsWith("/")) {
			return _findElement(eleRoot, astrPath, 1);
		}
		if ((astrPath.length > 0) && (eleRoot.getName().equals(astrPath[0]))) {
			return _findElement(eleRoot, astrPath, 1);
		}
		return _findElement(eleRoot, astrPath);
	}

	protected static List<Element> _findElements(Element element, String strPath) {
		ArrayList al = new ArrayList();
		if (strPath == null)
			return al;
		String strKey = null;
		String strValue = null;
		Pattern pattern = Pattern.compile("(.+)\\?(.+)=(.+)");
		Matcher m = pattern.matcher(strPath);
		if (m.find()) {
			strPath = m.group(1);
			strKey = m.group(2);
			strValue = m.group(3);
		}

		String[] astrPath = StringUtil.toArray(strPath, "/");
		if (element == null)
			return al;
		if (astrPath.length < 1) {
			return al;
		}
		if (element.getParentElement() != null) {
			element = element.getParentElement();
			List list = element.getChildren(astrPath[(astrPath.length - 1)]);
			if ((strKey == null) || (strValue == null))
				return list;
			for (int i = list.size() - 1; i >= 0; --i) {
				Element ele = (Element) list.get(i);
				if (strValue.equals(ele.getAttributeValue(strKey)))
					al.add(ele);
			}
		} else {
			al.add(element);
		}
		return al;
	}

	private static String getURI(String strPath) {
		Pattern pattern = Pattern.compile("(.+)\\?(.+)");
		Matcher m = pattern.matcher(strPath);
		if (m.find())
			return m.group(1);
		return strPath;
	}

	public static List<Element> findElements(Document document, String strPath) {
		Element element = _findElement(document, getURI(strPath));
		return _findElements(element, strPath);
	}

	public static List<Element> findElements(Element eleParent, String strPath) {
		Element element = _findElement(eleParent, getURI(strPath));
		return _findElements(element, strPath);
	}

	public static Element findElement(Element eleParent, String strPath) {
		List list = findElements(eleParent, strPath);
		if ((list == null) || (list.size() == 0))
			return null;
		return ((Element) list.get(0));
	}

	public static Element findElement(Document document, String strPath) {
		List list = findElements(document, strPath);
		if ((list == null) || (list.size() == 0))
			return null;
		return ((Element) list.get(0));
	}

	public static Properties populate(Element ele) {
		Properties props = new Properties();
		if (ele == null)
			return props;
		List list = ele.getAttributes();
		for (int i = 0; (list != null) && (i < list.size()); ++i) {
			Attribute attr = (Attribute) list.get(i);
			props.setProperty(attr.getName(), attr.getValue());
		}
		return props;
	}

	public static Document loadXML(URL url) {
		if (url == null) {
			return null;
		}
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document loadXML(InputStream is) {
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(is);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document loadXML(String strUrl) {
		try {
			return loadXML(new URL(strUrl));
		} catch (MalformedURLException e) {
			System.err.println(e);
		}
		return null;
	}

	public static Document loadXML(File file) {
		if (file == null) {
			return null;
		}
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document loadXMLFile(String strFile) {
		return loadXML(new File(strFile));
	}

}
