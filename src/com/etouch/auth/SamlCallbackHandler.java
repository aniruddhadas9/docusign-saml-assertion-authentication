/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.etouch.auth;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.rs.security.common.CryptoLoader;
import org.apache.cxf.rs.security.common.SecurityUtils;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.ActionBean;
import org.apache.ws.security.saml.ext.bean.AttributeBean;
import org.apache.ws.security.saml.ext.bean.AttributeStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthDecisionStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthDecisionStatementBean.Decision;
import org.apache.ws.security.saml.ext.bean.AuthenticationStatementBean;
import org.apache.ws.security.saml.ext.bean.ConditionsBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.bean.SubjectConfirmationDataBean;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.apache.ws.security.saml.ext.builder.SAML2Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.common.SAMLVersion;

import com.etouch.service.HtppResponseHelper;


/**
 * A CallbackHandler instance that is used by the STS to mock up a SAML Attribute Assertion.
 */
public class SamlCallbackHandler implements CallbackHandler {
    private boolean saml2 = true;
    private String confirmationMethod = SAML2Constants.CONF_BEARER;
    
    public SamlCallbackHandler() {
    	
    }
    
    public SamlCallbackHandler(boolean saml2) {
        this.saml2 = saml2;
    }
    
    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        Message m = PhaseInterceptorChain.getCurrentMessage();
        
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                SAMLCallback callback = (SAMLCallback) callbacks[i];
                
                if (saml2) {
                    callback.setSamlVersion(SAMLVersion.VERSION_20);
                } else {
                    callback.setSamlVersion(SAMLVersion.VERSION_11);
                }
                
                callback.setIssuer(HtppResponseHelper.integratorKey);
                
                /*String subjectName = (String)m.getContextualProperty("saml.subject.name");
                if (subjectName == null) {
                    subjectName = "uid=sts-client,o=mock-sts.com";
                }*/
                
                String subjectQualifier = "www.mock-sts.com";
                
                if (!saml2 && SAML2Constants.CONF_SENDER_VOUCHES.equals(confirmationMethod)) {
                    confirmationMethod = SAML1Constants.CONF_SENDER_VOUCHES;
                }
                
                DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                DateTime notAfter = formatter.parseDateTime(HtppResponseHelper.samlExpireDate);
                
                SubjectBean subjectBean = new SubjectBean(HtppResponseHelper.username, SAML2Constants.NAMEID_FORMAT_EMAIL_ADDRESS, confirmationMethod);
                subjectBean.setSubjectNameIDFormat(SAML2Constants.NAMEID_FORMAT_EMAIL_ADDRESS);
                SubjectConfirmationDataBean subjectConfirmationData = new SubjectConfirmationDataBean();
                subjectConfirmationData.setRecipient(HtppResponseHelper.samlRecipient);
                subjectConfirmationData.setNotAfter(notAfter);
				subjectBean.setSubjectConfirmationData(subjectConfirmationData);
                                
                if (SAML2Constants.CONF_HOLDER_KEY.equals(confirmationMethod)) {
                    
                    try {
                        CryptoLoader loader = new CryptoLoader();
                        Crypto crypto = (Crypto) loader.getCrypto(m, SecurityConstants.SIGNATURE_CRYPTO, SecurityConstants.SIGNATURE_PROPERTIES);
                        X509Certificate cert = 
                            SecurityUtils.getCertificates((org.apache.ws.security.components.crypto.Crypto) crypto, 
                                SecurityUtils.getUserName(m, (org.apache.ws.security.components.crypto.Crypto) crypto, "ws-security.signature.username"))[0];
                        
                        KeyInfoBean keyInfo = new KeyInfoBean();
                        keyInfo.setCertificate(cert);
                        subjectBean.setKeyInfo(keyInfo);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                callback.setSubject(subjectBean);
                
                //SubjectConfirmationData SubjectConfirmationData
                
                ConditionsBean conditions = new ConditionsBean();
                
                //System.out.println("notAfter:"+notAfter);
                conditions.setNotAfter(notAfter);
                conditions.setAudienceURI(HtppResponseHelper.samlAudienceURI);
                callback.setConditions(conditions);
                
                /*AuthDecisionStatementBean authDecBean = new AuthDecisionStatementBean();
                authDecBean.setDecision(Decision.INDETERMINATE);
                authDecBean.setResource("https://sp.example.com/SAML2");
                ActionBean actionBean = new ActionBean();
                actionBean.setContents("Read");
                authDecBean.setActions(Collections.singletonList(actionBean));
                callback.setAuthDecisionStatementData(Collections.singletonList(authDecBean));*/
                
                AuthenticationStatementBean authBean = new AuthenticationStatementBean();
                authBean.setSubject(subjectBean);
                authBean.setAuthenticationInstant(new DateTime());
                authBean.setSessionIndex("123456");
                // AuthnContextClassRef is not set
                authBean.setAuthenticationMethod("urn:oasis:names:tc:SAML:2.0:ac:classes:X509");
                callback.setAuthenticationStatementData(
                    Collections.singletonList(authBean));
                
                AttributeStatementBean attrBean = new AttributeStatementBean();
                attrBean.setSubject(subjectBean);
                
                /*List<String> roles = CastUtils.cast((List<?>)m.getContextualProperty("saml.roles"));
                if (roles == null) {
                    roles = Collections.singletonList("user");
                }
                List<AttributeBean> claims = new ArrayList<AttributeBean>();
                AttributeBean roleClaim = new AttributeBean();
                roleClaim.setSimpleName("subject-role");
                roleClaim.setQualifiedName(Claim.DEFAULT_ROLE_NAME);
                roleClaim.setNameFormat(Claim.DEFAULT_NAME_FORMAT);
                roleClaim.setCustomAttributeValues(new ArrayList<Object>(roles));
                claims.add(roleClaim);
                
                List<String> authMethods = CastUtils.cast((List<?>)m.getContextualProperty("saml.auth"));
                if (authMethods == null) {
                    authMethods = Collections.singletonList("password");
                }
                */
                
                AttributeBean authClaim = new AttributeBean();
                authClaim.setSimpleName("http://claims/authentication");
                authClaim.setQualifiedName("http://claims/authentication");
                authClaim.setNameFormat("http://claims/authentication-format");
                //authClaim.setCustomAttributeValues(new ArrayList<Object>(authMethods));
                //claims.add(authClaim);
                
                //attrBean.setSamlAttributes(claims);
                callback.setAttributeStatementData(Collections.singletonList(attrBean));
            }
        }
    }
    
}
