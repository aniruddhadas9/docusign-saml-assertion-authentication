package com.etouch.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.opensaml.xml.util.Base64;

import com.etouch.service.HtppResponseHelper;

public class TokenBySAMLAsserertion {
	
	private SAMLAssertion tokenGenerator = new SAMLAssertion();
	
	public JSONObject getToken() {
		JSONObject responseJson = null;
		InputStream inputStream = null;

		try {
			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(HtppResponseHelper.authUrl);

			
			String assertion = tokenGenerator.getAssertion();
			//String base64Assertion = Base64.encodeBytes(assertion.getBytes());
			Map<String, List<String>> grant = (Map<String, List<String>>) tokenGenerator.getGrantMap(assertion);
			System.out.println("grant_type: "+grant.get("grant_type").get(0));
			System.out.println("assertion: "+grant.get("assertion").get(0));
			String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("grant_type", grant.get("grant_type").get(0));
			jsonObject.accumulate("assertion", grant.get("assertion").get(0));

			// 4. convert JSONObject to JSON to String
			json = jsonObject.toString();
			
			json = "grant_type="+grant.get("grant_type").get(0);
			json += "&assertion="+ grant.get("assertion").get(0);
			
			
			
			// 5. set json to StringEntity
			StringEntity se = new StringEntity(json);

			// 6. set httpPost Entity
			httpPost.setEntity(se);
			
			//System.out.println(HtppResponseHelper.getResponseBody(httpPost.getEntity().getContent()));
			// 7. Set some headers to inform server about the type of the
			// content
			httpPost.addHeader("Accept", "application/json");
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

			
			/*for(int i=0; i < httpPost.getAllHeaders().length; i++){
				System.out.println(httpPost.getAllHeaders()[i].getName()+"-->"+httpPost.getAllHeaders()[i].getValue());
			}*/

			// 8. Execute
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
			String response = HtppResponseHelper.getResponseBody(inputStream);
			
			//System.out.println("token: "+response);
			
			responseJson = (JSONObject) new JSONObject(response);
			
			

		} catch (ClientProtocolException e) {
			System.out.println("ClientProtocolException : " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println("IOException:" + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception:" + e.getLocalizedMessage());
		}
		
		return responseJson;
	}
	
	public static void main(String[] args) {
		JSONObject jsonObject = new TokenBySAMLAsserertion().getToken();
		System.out.println("access_token:" + jsonObject.toString());
		System.out.println("access_token:" + jsonObject.getString("access_token"));
		System.out.println("token_type:" + jsonObject.getString("token_type"));
		System.out.println("scope:" + jsonObject.getString("scope"));
	}


}
