package com.etouch.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.rs.security.common.CryptoLoader;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils.Consumer;
import org.apache.cxf.rs.security.oauth2.auth.saml.Saml2BearerAuthOutInterceptor;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils;
import org.apache.cxf.rs.security.oauth2.common.AccessTokenGrant;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.grants.saml.Saml2BearerGrant;
import org.apache.cxf.rs.security.saml.SAMLUtils;
import org.apache.cxf.rs.security.saml.SAMLUtils.SelfSignInfo;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.util.DOM2Writer;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.etouch.auth.SamlCallbackHandler;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.etouch.service.HtppResponseHelper;


public class SAMLAssertion {
	
	
	public AccessTokenGrant getGrant(String assertion) {
		AccessTokenGrant grant = new Saml2BearerGrant(assertion);
		
		//System.out.println(grant.toMap().get("grant_type").get(0));
		//System.out.println(grant.toMap().get("assertion").get(0));
		
		/*Iterator<?> it = grant.toMap().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + ": " + pair.getValue());
	        it.remove();
	    }*/
	    
	    return grant;
	}
	
	public Map<String, List<String>> getGrantMap(String assertion) {
		return getGrant(assertion).toMap();
	}


	public String getAssertion() {
		Crypto crypto = null;
		try {
			crypto = new CryptoLoader().loadCrypto(HtppResponseHelper.CRYPTO_RESOURCE_PROPERTIES);
			crypto.setCryptoProvider("Provide Name");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WSSecurityException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SelfSignInfo signInfo = new SelfSignInfo(crypto, HtppResponseHelper.oauthClientUsername, HtppResponseHelper.oauthClientPassword);
		//System.out.println("signInfo.getCrypto():"+signInfo.getCrypto());

		String assertion = null;
		try {
			AssertionWrapper assertionW = SAMLUtils.createAssertion(new SamlCallbackHandler(), signInfo);
			//assertionW.s
			assertion = assertionW.assertionToString();
			//String subjectSert = SAMLUtils.getSubject(message, assertionW);
		} catch (Fault e) {
			e.printStackTrace();
		} catch (WSSecurityException e) {
			e.printStackTrace();
		}
		return assertion;
	}
	
	private WebClient createWebClientWithProps(String address) { 
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean(); 
        bean.setAddress(address); 
         
        
        Map<String, Object> properties = new HashMap<String, Object>(); 
        properties.put("security.callback-handler",  
                       "org.apache.cxf.systest.jaxrs.security.saml.KeystorePasswordCallback"); 
        properties.put("security.saml-callback-handler",  
                       "org.apache.cxf.systest.jaxrs.security.oauth2.SamlCallbackHandler2"); 
        properties.put("security.signature.username", "alice"); 
        properties.put("security.signature.properties", HtppResponseHelper.CRYPTO_RESOURCE_PROPERTIES); 
        bean.setProperties(properties); 
         
        bean.getOutInterceptors().add(new Saml2BearerAuthOutInterceptor()); 
         
        WebClient wc = bean.createWebClient(); 
        wc.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON); 
        return wc; 
    } 
	
	public void getTokenDocusign() throws Exception {

		String authenticationHeader = "<DocuSignCredentials>" + "<Username>" + HtppResponseHelper.username + "</Username>" + "<Password>"
				+ HtppResponseHelper.password + "</Password>" + "<IntegratorKey>" + HtppResponseHelper.integratorKey + "</IntegratorKey>"
				+ "</DocuSignCredentials>";
		
		int status;
		HttpURLConnection conn = null;
		
		System.out.println("----------Assertion start--------------");
		String assertion = getAssertion();
		System.out.println(assertion);
		System.out.println("-----------Assertion end-------------");
		System.out.println("-----------Base64 Assertion start-------------");
		String encoded = Base64.encodeBytes(assertion.getBytes());
		System.out.println(encoded);
		System.out.println("-----------Base64 Assertion end-------------");
		
		
		
		StringBuilder body = new StringBuilder();
		body.append(HtppResponseHelper.authReqString);
		/*body.append("grant_type=");
		body.append(grantMap.get("grant_type").get(0));
		body.append("&assertion=");
		body.append(grantMap.get("assertion").get(0));*/
		//body.append("&Content-Length="+Integer.toString(body.length()));
		//System.out.println("body:"+body.toString());
		
		conn = HtppResponseHelper.InitializeRequest2(HtppResponseHelper.authUrl, "POST", body.toString(), authenticationHeader);

		status = conn.getResponseCode();
		if (status != 200) { //// 200 = OK 
			HtppResponseHelper.errorParse(conn, status);
			return;
		}
		
		InputStream is = conn.getInputStream();
	}
	
	
	public void testSAML2BearerGrant() throws Exception {
	    WebClient wc = createWebClientWithProps(HtppResponseHelper.authUrl);
	    
	    Crypto crypto = new CryptoLoader().loadCrypto(HtppResponseHelper.CRYPTO_RESOURCE_PROPERTIES);
	    SelfSignInfo signInfo = new SelfSignInfo(crypto, HtppResponseHelper.oauthClientUsername, HtppResponseHelper.oauthClientPassword); 
	    
	    AssertionWrapper assertionWrapper = SAMLUtils.createAssertion(new SamlCallbackHandler(), signInfo);
	    Document doc = DOMUtils.newDocument();
	    Element assertionElement = assertionWrapper.toDOM(doc);
	    String assertion = DOM2Writer.nodeToString(assertionElement);
	    
	    Saml2BearerGrant grant = new Saml2BearerGrant(assertion);
	    ClientAccessToken at = OAuthClientUtils.getAccessToken(wc, 
	                                    new Consumer(HtppResponseHelper.oauthClientUsername, HtppResponseHelper.oauthClientUsername), 
	                                    grant,
	                                    false);
	    System.out.println(at.getTokenKey());
	}
	
	public ClientAccessToken getOAuth2Token() {
		
		WebClient wc = createWebClientWithProps(HtppResponseHelper.authUrl);
		ClientAccessToken at = null;
		 
		String base64Assertion = Base64.encodeBytes(getAssertion().getBytes());
		System.out.println(base64Assertion);
		
		try{
			Consumer consumer = new OAuthClientUtils.Consumer(HtppResponseHelper.oauthClientUsername, HtppResponseHelper.oauthClientPassword);
			System.out.println("key: "+consumer.getKey());
			System.out.println("secret: "+consumer.getSecret());
			at = OAuthClientUtils.getAccessToken(wc, consumer, getGrant(getAssertion()), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return at;
	}

	
	
	public static void main(String[] args) {
		SAMLAssertion samlAssertion = new SAMLAssertion();
		
		try {
			//tokenGenerator.getTokenDocusign();
			samlAssertion.getOAuth2Token();
			System.out.println("--------------------------------------------------------------------");
			samlAssertion.testSAML2BearerGrant();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
