package com.etouch.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

public class InPersonJson {
	
	public static void main(String[] args) throws Exception {
		
		String integratorKey = "CONN-87f5a379-fb57-49aa-ba27-e83c7e440ad5";
		String username = "rgoyal@etouch.net"; // account email (or your API //											// userId)
		String password = "test1234"; // account password
		
		/*String hostName =System.getProperty("hostName");
		String hostEmail =System.getProperty("hostEmail");
		String signerName1 =System.getProperty("signerName1");
		String signerName2 =System.getProperty("signerName2");
		String documentName =System.getProperty("documentName");	*/
		
		String docContentType = "application/pdf"; // content type for above

		// ------------------------------------------------------------------------------------

		// construct the DocuSign authentication header
		String authenticationHeader = "<DocuSignCredentials>" + "<Username>" + username + "</Username>" + "<Password>"
				+ password + "</Password>" + "<IntegratorKey>" + integratorKey + "</IntegratorKey>"
				+ "</DocuSignCredentials>";

		// additional variable declarations
		String baseURL = ""; // we will retrieve this through the Login API call
		String accountId = ""; // we will retrieve this through the Login API call
		String envelopeId = ""; // generated from signature request API call
		String url = ""; // end-point for each api call
		String body = ""; // request body
		String response = ""; // response body
		int status; // response status
		HttpURLConnection conn = null; // connection object used for each request

		// ============================================================================
		// STEP 1 - Make the Login API call to retrieve your baseUrl and
		// accountId
		// ============================================================================

		url = "https://demo.docusign.net/restapi/v2/login_information";
		body = ""; // no request body for the login call

		// create connection object, set request method, add request headers
		conn = InitializeRequest(url, "GET", body, authenticationHeader);

		// send the request
		System.out.println("STEP 1:  Sending Login request...\n");
		status = conn.getResponseCode();
		if (status != 200) {
			errorParse(conn, status);
			return;
		}
		
		// obtain baseUrl and accountId values from response body
		response = getResponseBody(conn);
		baseURL = parseXMLBody(response, "baseUrl");
		accountId = parseXMLBody(response, "accountId");
		System.out.println("-- Login Successful!!!!");
		System.out.println("BaseURL is: "+ baseURL);
		System.out.println("accountId: "+accountId);

		// ============================================================================
		// STEP 2 - Signature Request on Document API Call
		// ============================================================================
		
		String documentName = "C:\\eSignature\\DocuSign Interface TDD-orginal.pdf";
		String documentName1 = "C:\\eSignature\\DocuSign Interface TDD-orginal.pdf";

		url = baseURL + "/envelopes";
		
		body = readFile("C:\\aniruddh\\projects\\etouch\\eclipse\\docusign21mar\\WebContent\\inpersonsimple4-simple.json");
	
		conn = InitializeRequest(url, "POST", body, authenticationHeader);

		// read document content into byte array
		File file = new File(documentName);
		InputStream inputStream = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		inputStream.read(bytes);
		inputStream.close();
		
		// read document content into byte array
		File file1 = new File(documentName1);
		InputStream inputStream1 = new FileInputStream(file1);
		byte[] bytes1 = new byte[(int) file1.length()];
		inputStream1.read(bytes1);
		inputStream1.close();

		String requestBody = "\r\n\r\n--BOUNDARY\r\n" + 
				"Content-Type: application/json\r\n" + 
				"Content-Disposition: form-data\r\n" +
				//"Content-Length:"+ Integer.toString(body.length())+"\r\n" + 
				"\r\n" + 
				body + "\r\n--BOUNDARY\r\n" + 	// our xml formatted request body
				"Content-Disposition: form-data\r\n" +
				"Content-Type: multipart/mixed; boundary=BBB\r\n\r\n" +
				"--BBB\r\n" +
				"Content-Type: " + docContentType + "\r\n" + 
				"Content-Disposition: file; filename=\"" + documentName + "\"; documentId=1"+"\r\n\r\n";
			// we break this up into two string since the PDF doc bytes go here and are not in string format.
			// see further below where we write to the outputstream...
		
		String reqBody1 = "\r\n--BBB\r\n" +
				"Content-Type: " + docContentType + "\r\n" + 
				"Content-Disposition: file; filename=\"" + documentName + "\"; documentId=2" + "\r\n\r\n";
		String reqBody2 = "\r\n--BBB--\r\n" + "--BOUNDARY--\r\n\r\n";
		
		System.out.println("--||"+requestBody+bytes+reqBody1+bytes1+reqBody2);
		// write the body of the request...
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeBytes(requestBody.toString());
		dos.write(bytes);
		dos.writeBytes(reqBody1.toString());
		dos.write(bytes1);
		dos.writeBytes(reqBody2.toString());
		dos.flush();
		dos.close();

		System.out.println("STEP 2:  Sending signature request on document...\n");

		status = conn.getResponseCode(); // triggers the request
		if (status != 201) // 201 = Created
		{
			errorParse(conn, status);
			return;
		}

		// display the response body
		response = getResponseBody(conn);
		envelopeId = parseXMLBody(response, "envelopeId");
		System.out.println("Signature request has been sent!\nEnvelopeId is:  " + envelopeId + "\n");
		//*/
	}
	
	// ***********************************************************************************************
	// ***********************************************************************************************
	// --- HELPER FUNCTIONS ---
	// ***********************************************************************************************
	// ***********************************************************************************************
	public static HttpURLConnection InitializeRequest(String url, String method, String body, String httpAuthHeader) {
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
			
				conn.setRequestProperty("Content-Type", "application/xml");
			}
			return conn;

		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}
	public static HttpURLConnection InitializeRequest2(String url, String method, String body, String httpAuthHeader) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			
			conn.setRequestMethod(method);
			conn.setRequestProperty("X-DocuSign-Authentication", httpAuthHeader);
			conn.setRequestProperty("Content-Type", "application/xml");
			conn.setRequestProperty("Accept", "application/xml");
			if (method.equalsIgnoreCase("POST"))
			{
				conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
				conn.setDoOutput(true);
				// write body of the POST request 
				DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
				dos.writeBytes(body); dos.flush(); dos.close();
			}
			return conn;
			
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
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

	///////////////////////////////////////////////////////////////////////////////////////////////
	public static String getResponseBody(HttpURLConnection conn) {
		BufferedReader br = null;
		StringBuilder body = null;
		String line = "";
		try {
			// we use xPath to get the baseUrl and accountId from the XML
			// response body
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			body = new StringBuilder();
			while ((line = br.readLine()) != null)
				body.append(line);
			return body.toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
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
			System.out.println("\nError description:  \n" + prettyFormat(responseError.toString(), 2));
			return;
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	public static String prettyFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}
	
	public static String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

	
	
} // end class

