package com.etouch.auth;

import java.io.IOException;
import java.util.Collections;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.AuthenticationStatementBean;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.apache.ws.security.saml.ext.builder.SAML2Constants;
import org.opensaml.common.SAMLVersion;

public class SamlBearerCallbackHandler implements CallbackHandler {
	private final String issuer;
	private final String username;
	
    public SamlBearerCallbackHandler(final String issuer, final String username) {
    	this.issuer = issuer;
    	this.username = username;
    }
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    	System.out.println("SamlBearerCallbackHandler: handle"+callbacks[0].getClass());
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
            	 System.out.println("SamlBearerCallbackHandler|handle|forloop|callbacks[i] instanceof SAMLCallback:inside if");
                SAMLCallback callback = (SAMLCallback) callbacks[i];
                
                callback.setIssuer(issuer);
                
                SAMLVersion samlVersion = callback.getSamlVersion();
                System.out.println("SamlBearerCallbackHandler|handle|forloop|samlVersion:"+samlVersion);
                org.apache.ws.security.saml.ext.bean.SubjectBean subjectBean = getSubjectBean(samlVersion);
                callback.setSubject(subjectBean);
                
                AuthenticationStatementBean authBean = new AuthenticationStatementBean();
                authBean.setAuthenticationMethod(getAuthenticationMethod(samlVersion));
                authBean.setSubject(subjectBean);
                callback.setAuthenticationStatementData(Collections.singletonList(authBean));
           }
        }
    }

    private String getAuthenticationMethod(SAMLVersion samlVersion) {
        if (samlVersion == SAMLVersion.VERSION_20) {
            return SAML2Constants.AUTH_CONTEXT_CLASS_REF_PASSWORD;
        } else {
            return SAML1Constants.AUTH_METHOD_PASSWORD;
        }
    }
    
    private SubjectBean getSubjectBean(SAMLVersion samlVersion) {
        if (samlVersion == SAMLVersion.VERSION_20) {
            return new SubjectBean(username, SAML2Constants.NAMEID_FORMAT_UNSPECIFIED, 
                    SAML2Constants.CONF_BEARER);
        } else {
            return new SubjectBean(username, SAML1Constants.NAMEID_FORMAT_UNSPECIFIED, 
                    SAML1Constants.CONF_BEARER);
        }
    }
}