package com.etouch.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.docusign.esign.api.AuthenticationApi;
import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.model.LoginAccount;
import com.docusign.esign.model.LoginInformation;

public class DownloadPdfDocument {

    public void GetEnvelopeDocuments(final String envelopeId, final String documentId, final String fileName) {
        
        List<LoginAccount> loginAccounts = null;
        
        // initialize the api client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(HtppResponseHelper.baseUrl);
        
        // create JSON formatted auth header
        String creds = "{\"Username\":\"" +  HtppResponseHelper.username + "\",\"Password\":\"" +  HtppResponseHelper.password + "\",\"IntegratorKey\":\"" +  HtppResponseHelper.integratorKey + "\"}";
        apiClient.addDefaultHeader("X-DocuSign-Authentication", creds);
        
        // assign api client to the Configuration object
        Configuration.setDefaultApiClient(apiClient);
        
        //===============================================================================
        // Step 1:  Login() API
        //===============================================================================
        try {
        	// login call available off the AuthenticationApi
        	AuthenticationApi authApi = new AuthenticationApi();
        	
        	// login has some optional parameters we can set
            AuthenticationApi.LoginOptions loginOps = authApi.new LoginOptions();
            loginOps.setApiPassword("true");
            loginOps.setIncludeAccountIdGuid("true");
            LoginInformation loginInfo = authApi.login(loginOps);
         
            // note that a given user may be a member of multiple accounts
            loginAccounts = loginInfo.getLoginAccounts();
            
            System.out.println("LoginInformation: " + loginAccounts);
        }
        catch (com.docusign.esign.client.ApiException ex) {
            System.out.println("Exception: " + ex);
        }
        
        //===============================================================================
        // Step 2:  Get Document API 
        //===============================================================================
        try 
        {
            String accountId = loginAccounts.get(0).getAccountId();
            EnvelopesApi envelopesApi = new EnvelopesApi();
            
            byte[] document = envelopesApi.getDocument(accountId, envelopeId, documentId);
            OutputStream out = new FileOutputStream(HtppResponseHelper.filePath+fileName);
        	out.write(document);
        	out.close();
            System.out.println("Document " + documentId + " from envelope " + envelopeId);// + " has been downloaded to " + document.getAbsolutePath());
        } catch (com.docusign.esign.client.ApiException | IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }   
    
    public byte[] GetEnvelopeDocumentBytes(final String envelopeId, final String documentId, final String fileName) {
        
    	byte[] document = null;
        List<LoginAccount> loginAccounts = null;
        
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(HtppResponseHelper.baseUrl);
        
        String creds = "{\"Username\":\"" +  HtppResponseHelper.username + "\",\"Password\":\"" +  HtppResponseHelper.password + "\",\"IntegratorKey\":\"" +  HtppResponseHelper.integratorKey + "\"}";
        apiClient.addDefaultHeader("X-DocuSign-Authentication", creds);
        
        Configuration.setDefaultApiClient(apiClient);
        
        //===============================================================================
        // Step 1:  Login() API
        //===============================================================================
        try {
        	// login call available off the AuthenticationApi
        	AuthenticationApi authApi = new AuthenticationApi();
        	
        	// login has some optional parameters we can set
            AuthenticationApi.LoginOptions loginOps = authApi.new LoginOptions();
            loginOps.setApiPassword("true");
            loginOps.setIncludeAccountIdGuid("true");
            LoginInformation loginInfo = authApi.login(loginOps);
         
            // note that a given user may be a member of multiple accounts
            loginAccounts = loginInfo.getLoginAccounts();
            
            //System.out.println("LoginInformation: " + loginAccounts);
        }  catch (com.docusign.esign.client.ApiException ex) {
            System.out.println("Exception: " + ex);
        }
        
        //===============================================================================
        // Step 2:  Get Document API 
        //===============================================================================
        try  {
            String accountId = loginAccounts.get(0).getAccountId();
            EnvelopesApi envelopesApi = new EnvelopesApi();
            document = envelopesApi.getDocument(accountId, envelopeId, documentId);
            System.out.println("Document " + documentId + " from envelope " + envelopeId);// + " has been downloaded to " + document.getAbsolutePath());
        } catch (com.docusign.esign.client.ApiException ex) {
            System.out.println("Exception: " + ex);
        }
        
        return document;
    }
    
    
    public static void main(String args[]) {
    	DownloadPdfDocument downloadPdfDocument= new DownloadPdfDocument();
    	
		try {
			OutputStream out;
			out = new FileOutputStream("c:\\eSignature\\downloadedfiles\\testing1234.pdf");
			out.write(downloadPdfDocument.GetEnvelopeDocumentBytes(HtppResponseHelper.tmpEvenvelopId, "1", "testing1234.pdf"));
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    }


}
