package com.etouch.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

public class HtppResponseHelper {
	
	public static final String CRYPTO_RESOURCE_PROPERTIES = "alice.properties";
	public static final String oauthClientUsername = "your username";
	public static final String oauthClientPassword = "your password";
	public static final String grantType = "urn:ietf:params:oauth:grant-type:saml2-bearer";
	public static final String samlRecipient = "https://demo.docusign.net/restapi/v2/oauth2/token";
	public static final String samlAudienceURI = "https://www.docusign.net";
	public static final String samlExpireDate = "29/03/2016 03:09:00";
	
	public static final String mysqlUrl = "jdbc:mysql://localhost:3306/dbname";
	public static final String mysqlUsername = "mysqlusername";
	public static final String mysqlPassword = "mysqldbpassword";
			
	
	public static final String authUrl = "https://demo.docusign.net/restapi/v2/oauth2/token";
	public static final String loginUrl = "https://demo.docusign.net/restapi/v2/login_information";
	public static final String baseUrl = "https://demo.docusign.net/restapi/v2/accounts/[youraccountid-fordocusign]/";
	public static final String authReqString = "grant_type=password&client_id=[yourclientid for docusign]"+
												"&username=[your email id used in docusign]&password=[yourpassworddocusign]&scope=api";
	
	public static final String integratorKey = "docusign integratorKey";
	public static final String issuer = "docusign issuer";
	public static final String username = "docusign username";
	public static final String password = "docusign password";
	
	public static final String filePath = "c:\\eSignature\\downloadedfiles\\";
	
	public static final String tmpEvenvelopId = "one of the env id you want to test";

	
	public static String parseXMLBody(String body, String searchToken) {
		String xPathExpression;
		try {
			// we use xPath to parse the XML formatted response body
			xPathExpression = String.format("//*[1]/*[local-name()='%s']", searchToken);
			XPath xPath = XPathFactory.newInstance().newXPath();
			return (xPath.evaluate(xPathExpression, new InputSource(new StringReader(body))));
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}
	
	public static void errorParse(HttpURLConnection conn, int status) {
		BufferedReader br;
		String line;
		StringBuilder responseError;
		try {
			System.out.print("API call failed, status returned was: " + status);
			InputStreamReader isr = new InputStreamReader(conn.getErrorStream());
			br = new BufferedReader(isr);
			responseError = new StringBuilder();
			line = null;
			while ((line = br.readLine()) != null)
				responseError.append(line);
			System.out.println("\nError description:  \n" + responseError.toString());
			return;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getResponseBody(HttpURLConnection conn) {
		BufferedReader br = null;
		StringBuilder body = null;
		String line = "";
		try {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			body = new StringBuilder();
			while ((line = br.readLine()) != null)
				body.append(line);
			return body.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getResponseBody(InputStream is) {
		BufferedReader br = null;
		StringBuilder body = null;
		String line = "";
		try {
			// we use xPath to get the baseUrl and accountId from the XML
			// response body
			br = new BufferedReader(new InputStreamReader(is));
			body = new StringBuilder();
			while ((line = br.readLine()) != null)
				body.append(line);
			return body.toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}

	
	public static HttpURLConnection InitializeLoginRequest(String url, String method, String body, String httpAuthHeader) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setRequestMethod(method);
			conn.setRequestProperty("X-DocuSign-Authentication", httpAuthHeader);
			conn.setRequestProperty("Accept", "application/xml");
			if (method.equalsIgnoreCase("POST")) {
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=BOUNDARY");
				conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
				conn.setDoOutput(true);
			} else {
				conn.setRequestProperty("Content-Type", "application/josn");
			}
			return conn;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	public static HttpURLConnection initHttpConnection(String url, String method, String body, String httpAuthHeader) {

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setRequestMethod(method);
			conn.setRequestProperty("Authentication", "bearer 11111");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			return conn;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static HttpURLConnection InitializeRequest2(String url, String method, String body, String httpAuthHeader) {
		HttpURLConnection conn = null;
		
		System.out.println("InitializeRequest2|Integer.toString(body.getBytes().length):"+Integer.toString(body.getBytes().length));
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setRequestMethod(method);
			//conn.setRequestProperty("X-DocuSign-Authentication", httpAuthHeader);
			//conn.setRequestProperty("Authentication", "bearer");
			conn.setRequestProperty("Accept", "application/xml");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(1024));
			conn.setDoOutput(true);
			
			return conn;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



}
