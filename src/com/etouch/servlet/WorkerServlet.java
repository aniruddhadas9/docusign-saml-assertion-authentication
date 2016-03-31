package com.etouch.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;


import com.etouch.beans.DocusignResults;
import com.etouch.model.StoreData;
import com.etouch.service.DownloadPdfDocument;
import com.etouch.util.HeaderInfo;


public class WorkerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private String status;
    private String envelopeID;
    private String fullJson = "";
    private String header = "";

    private String filePath = "c:\\eSignature\\downloadedfiles\\";
	private JSONObject pdfObj = null;
	private JSONObject envStatusObj = null;
	private JSONObject xmlJSONObj = null;


    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    	header = HeaderInfo.getHeaderInfoJson(request);

    	DocusignResults res =  new DocusignResults();
    	StoreData storeData = new StoreData();
    	String pdfObject = "";

        try {
	        InputStream sis = request.getInputStream();
	        String reqString = IOUtils.toString(sis, request.getCharacterEncoding());

	    	xmlJSONObj = XML.toJSONObject(reqString);
	    	fullJson = xmlJSONObj.toString(4);
	    	//System.out.println("fullJson: "+fullJson);

	    	xmlJSONObj = xmlJSONObj.getJSONObject("DocuSignEnvelopeInformation");
	    	envStatusObj = xmlJSONObj.getJSONObject("EnvelopeStatus");
	    	pdfObj = xmlJSONObj.optJSONObject("DocumentPDFs");

	    	status = envStatusObj.getString("Status");
	    	envelopeID = envStatusObj.getString("EnvelopeID");

	    	if (pdfObj != null) {
	    		pdfObject = pdfObj.toString(4);
	    		storePDFJsonInFile(pdfObj);
	    	}
	    	System.out.println("DocumentStatuses>>>>"+envStatusObj.getJSONObject("DocumentStatuses").toString(4));
	    	downloadSinglePDF(envStatusObj.getJSONObject("DocumentStatuses"));

	    	//System.out.println("EnvelopeStatus>>>>"+envStatusObj.toString(4));
	    	//System.out.println("envoloped: "+envStatusObj.getString("EnvelopeID"));
	    	//System.out.println("status: "+envStatusObj.getString("Status"));
	    	//System.out.println("Document: "+pdfObj.toString(4));
                
        } catch (IOException ioe) {
           ioe.printStackTrace();
           System.out.println("request data received as json:"+fullJson);
        } catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("request data received as json:"+fullJson);
		}
    	
        res.setStatus(status);
        res.setEnvelopeId(envelopeID);
        res.setDocument(pdfObject);
        res.setFullJson(fullJson);
        res.setHeader(header);
    	storeData.store(res);
    }
    
    private String storePDFJsonInFile(JSONObject  pdfObj) {
    	StringBuilder returnData = new StringBuilder();
    	try {
        	if (pdfObj.has("DocumentPDF")) {
        	    JSONObject dataObject = pdfObj.optJSONObject("DocumentPDF");
        	    //System.out.println("dataObject: "+dataObject);
        	    if (dataObject != null) {
        	    	System.out.println(dataObject.getString("Name")+" (single) file found...");
        	        //Do things with object.
        	    	byte[] encodedBytes = Base64.decodeBase64(dataObject.getString("PDFBytes").getBytes());
            		OutputStream out = new FileOutputStream(filePath+dataObject.getString("Name"));
                	out.write(encodedBytes);
                	out.close();
                	//System.IO.File.WriteAllBytes(filePath+"new//"+pdfObj.getString("Name").toString(), filebyt);
                	returnData.append("{one pdf file stored successfully}");
                	
        	    } else {
        	        JSONArray pdfDocArray = pdfObj.optJSONArray("DocumentPDF");
        	        //System.out.println("Multiple file found..."+pdfDocArray.toString(0));
        	        String filename = envStatusObj.getString("Subject")+envStatusObj.getString("Created")+".pdf";
        	        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        	        //System.out.println("filename:"+filePath+filename);
        	        OutputStream out = new FileOutputStream(filePath+filename);
        	        for(int i=0; i< pdfDocArray.length() ; i++) {
        	        	
        	        	JSONObject pdf = pdfDocArray.getJSONObject(i);
        	        	byte[] encodedBytes = Base64.decodeBase64(pdf.getString("PDFBytes").getBytes());
        	        	out.write(encodedBytes);
        	        	returnData.append("{one pdf file stored successfully}");
        	        }
        	        out.close();
        	    }
        	}
    	} catch (JSONException je) {
    		System.out.println("JSONException|request data received as json:"+fullJson);
    		je.printStackTrace();
    	} catch (Exception e) {
    		System.out.println("request data received as json:"+fullJson);
    		e.printStackTrace();
    	}
    	
    	return returnData.toString();
    }
    
    private String downloadPDF(JSONObject DocumentStatuses){
    	StringBuilder returnData = new StringBuilder();
    	DownloadPdfDocument downloadPdfDocument= new DownloadPdfDocument();
    	
    	try {
        	if (DocumentStatuses.has("DocumentStatus")) {
        	    JSONObject dataObject = DocumentStatuses.optJSONObject("DocumentStatus");
        	    System.out.println("dataObject: "+dataObject);
        	    if (dataObject != null) {
        	    	String filename = envStatusObj.getString("Subject")+"-"+dataObject.getString("Name");
        	        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        	        String documentId = ""+dataObject.getInt("ID");
        	        downloadPdfDocument.GetEnvelopeDocuments(envelopeID, documentId, filename);
                	//System.IO.File.WriteAllBytes(filePath+"new//"+pdfObj.getString("Name").toString(), filebyt);
                	returnData.append("{pdf file downloaded and stored by using DocumentStatuses}");
                	
        	    } else {
        	        JSONArray DocumentStatusArray = DocumentStatuses.optJSONArray("DocumentStatus");
        	        for(int i=0; i< DocumentStatusArray.length() ; i++) {
        	        	JSONObject documentStatus = DocumentStatusArray.getJSONObject(i);
        	        	String filename = envStatusObj.getString("Subject")+documentStatus.getString("Name");
        		        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        		        String documentId = ""+documentStatus.getInt("ID");
        		        System.out.println("envelopeID:"+envelopeID+"|documentId:"+documentId+"|filename:"+filename);
        		        downloadPdfDocument.GetEnvelopeDocuments(envelopeID, documentId, filename);
        	        	returnData.append("{pdf file downloaded and stored by using DocumentStatuses}");
        	        }
        	    }
        	}
	        
    	} catch (JSONException je) {
    		System.out.println("JSONException|request data received as json:"+fullJson);
    		je.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return returnData.toString();
    }

    private String downloadSinglePDF(JSONObject DocumentStatuses){
    	StringBuilder returnData = new StringBuilder();
    	DownloadPdfDocument downloadPdfDocument= new DownloadPdfDocument();
    	
    	try {
        	if (DocumentStatuses.has("DocumentStatus")) {
        	    JSONObject dataObject = DocumentStatuses.optJSONObject("DocumentStatus");
        	    System.out.println("dataObject: "+dataObject);
        	    if (dataObject != null) {
        	    	String filename = envStatusObj.getString("Subject")+"-"+dataObject.getString("Name");
        	        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        	        String documentId = ""+dataObject.getInt("ID");
        	        downloadPdfDocument.GetEnvelopeDocuments(envelopeID, documentId, filename);
                	//System.IO.File.WriteAllBytes(filePath+"new//"+pdfObj.getString("Name").toString(), filebyt);
                	returnData.append("{pdf file downloaded and stored by using DocumentStatuses}");
                	
        	    } else {
        	        JSONArray DocumentStatusArray = DocumentStatuses.optJSONArray("DocumentStatus");
    	        	String filename = envStatusObj.getString("Subject")+envStatusObj.getString("Created")+".pdf";
    		        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        	        OutputStream out = new FileOutputStream(filePath+filename);
        	        for(int i=0; i< DocumentStatusArray.length() ; i++) {
        	        	JSONObject documentStatus = DocumentStatusArray.getJSONObject(i);
        		        String documentId = ""+documentStatus.getInt("ID");
        		        System.out.println("envelopeID:"+envelopeID+"|documentId:"+documentId+"|filename:"+filename);
        		        out.write(downloadPdfDocument.GetEnvelopeDocumentBytes(envelopeID, documentId, filename));
        	        	returnData.append("{pdf file downloaded and stored by using DocumentStatuses}");
        	        }
        	        out.close();
        	    }
        	}
	        
    	} catch (JSONException je) {
    		System.out.println("JSONException|request data received as json:"+fullJson);
    		je.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return returnData.toString();
    }

}
