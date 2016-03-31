package com.etouch.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;

import com.etouch.beans.DocusignResults;
import com.etouch.model.StoreData;
import com.etouch.util.HeaderInfo;

public class WorkerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private String status;
    private String envelopeID;
    private String fullJson = "";
    private String header = "";

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    	header = HeaderInfo.getHeaderInfoJson(request);
    	DocusignResults res =  new DocusignResults();
    	StoreData storeData = new StoreData();
		
		String fullJson = "";
		JSONObject envStatusObj = null;
		JSONObject documentList = null;
		
        try {

                InputStream sis = request.getInputStream();
                String reqString = IOUtils.toString(sis, request.getCharacterEncoding());
                
                try {
                	
                	JSONObject xmlJSONObj = XML.toJSONObject(reqString);
                	fullJson = xmlJSONObj.toString(4);
                	
                	xmlJSONObj = xmlJSONObj.getJSONObject("DocuSignEnvelopeInformation");
                	envStatusObj = xmlJSONObj.getJSONObject("EnvelopeStatus");
                	documentList = envStatusObj.getJSONObject("DocumentStatuses");
                	
                	downloadSinglePDF(documentList, envStatusObj);
                	
                    res.setStatus(envStatusObj.getString("Status"));
                    res.setEnvelopeId(envStatusObj.getString("EnvelopeID"));
                    res.setDocument(documentList.toString(4));
                    res.setFullJson(fullJson);
                    res.setHeader(header);
                    
                    storeData.store(res);

                	
                } catch (Exception e) {
                	System.out.println("Error while parsing xml to json. request xml:"+reqString);
                	e.printStackTrace();
                }
                
                
                
        } catch (IOException ioe) {
           ioe.printStackTrace();
           System.out.println("request data received as json:"+fullJson);
        } catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("request data received as json:"+fullJson);
		}

    }
    
    private void downloadSinglePDF(JSONObject DocumentStatuses, JSONObject envStatusObj){
    	
    	try {
        	if (DocumentStatuses.has("DocumentStatus")) {
        	    JSONObject dataObject = DocumentStatuses.optJSONObject("DocumentStatus");
        	    System.out.println("dataObject: "+dataObject);
        	    if (dataObject != null) {
        	    	String documentId = ""+dataObject.getInt("ID");
        	    	String filename = envStatusObj.getString("Subject")+documentId+"-"+dataObject.getString("Name");
        	        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        	        this.downloadDocument(envStatusObj.getString("EnvelopeID"), documentId, filename);
        	    } else {
        	        JSONArray DocumentStatusArray = DocumentStatuses.optJSONArray("DocumentStatus");
        	        for(int i=0; i< DocumentStatusArray.length() ; i++) {
        	        	JSONObject documentStatus = DocumentStatusArray.getJSONObject(i);
        	        	String documentId = ""+documentStatus.getInt("ID");
        	        	String filename = envStatusObj.getString("Subject")+documentId+envStatusObj.getString("Created")+".pdf";
        		        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        		        this.downloadDocument(envStatusObj.getString("EnvelopeID"), documentId, filename);
        		        System.out.println("envelopeID:"+envStatusObj.getString("EnvelopeID")+"|documentId:"+documentId+"|filename:"+filename);
        	        }
        	    }
        	}
	        
    	} catch (JSONException je) {
    		System.out.println("JSONException|request data received as json");
    		je.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }

	public void downloadDocument(String envId, String docID, String filename) throws Exception {
		
		String integratorKey = "CONN-87f5a379-fb57-49aa-ba27-e83c7e440ad5";
		String username = "rgoyal@etouch.net"; // account email (or your API //											// userId)
		String password = "test1234"; // account password
				
		String docContentType = "application/pdf"; // content type for above
													
		// ------------------------------------------------------------------------------------

		// construct the DocuSign authentication header
		String authenticationHeader = "<DocuSignCredentials>" + "<Username>" + username + "</Username>" + "<Password>"
				+ password + "</Password>" + "<IntegratorKey>" + integratorKey + "</IntegratorKey>"
				+ "</DocuSignCredentials>";

		// additional variable declarations
		String baseURL = "https://demo.docusign.net/restapi/v2/accounts/1460443/"; // we will retrieve this through the Login API call
		String url = ""; // end-point for each api call
		String response = ""; // response body
		int status; // response status
		HttpURLConnection conn = null; // connection object used for each request
		
		String folder = "C:\\eSignature\\";
		
		url = baseURL + "/envelopes/"+envId+"/documents/"+docID; 
		conn = InitializeRequest2(url, "GET", "", authenticationHeader);
		System.out.println("STEP 2:  getting the document...\n");
		status = conn.getResponseCode(); // triggers the request
		if (status != 200) {
			System.out.println("got some error not 200");
			return;
		}

		File file = new File(folder+filename);
        FileOutputStream fileOutput = new FileOutputStream(file);
		
		InputStream is = conn.getInputStream();
        try {
              byte[] buffer = new byte[1024];
              int bufferLength = 0; // used to store a temporary size of the
                                                  // buffer
              while ((bufferLength = is.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
              }
              fileOutput.close();
        } finally {
              is.close();
        }

		System.out.println("Status: "+status);

	}
	
	
	public static HttpURLConnection InitializeRequest2(String url, String method, String body, String httpAuthHeader) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setRequestMethod(method);
			conn.setRequestProperty("X-DocuSign-Authentication", httpAuthHeader);
			conn.setRequestProperty("Accept", "application/pdf");
			conn.setRequestProperty("Content-Type", "application/pdf");
			return conn;

		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}
	


}
