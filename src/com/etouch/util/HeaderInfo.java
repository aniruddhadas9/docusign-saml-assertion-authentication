package com.etouch.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class HeaderInfo {

	public Map<String, String> getHeaderInfo(final HttpServletRequest request) {

		Map<String, String> map = new HashMap<String, String>();
		Enumeration<?> headerNames = request.getHeaderNames();
		
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		return map;
	  }
	
	
	public static String getHeaderInfoJson(final HttpServletRequest request) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		for(Map.Entry<String, String> entry : new HeaderInfo().getHeaderInfo(request).entrySet()) {
			json.append("{"+entry.getKey()+":"+entry.getValue()+"}");
		}
		json.append("}");
		return json.toString();
	}

}
