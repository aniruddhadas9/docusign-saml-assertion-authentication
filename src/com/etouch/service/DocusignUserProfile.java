package com.etouch.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.etouch.auth.TokenBySAMLAsserertion;

public class DocusignUserProfile {
	
	public static JSONObject getUserProfile() {
		return userProfile(HtppResponseHelper.username);
	}
	
	public static void main(String args[]) {
		getUserProfile();
	}
	
	public static JSONObject userProfile(String userId) {
		JSONObject responseJson = null;
		JSONObject samlTokenJson = new TokenBySAMLAsserertion().getToken();
		InputStream inputStream = null;

		try {
			HttpClient httpclient = new DefaultHttpClient();
			String url = HtppResponseHelper.baseUrl + "/users/"+userId+"/profile";
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept", "application/json");
			httpGet.addHeader("Content-Type", "application/json");
			httpGet.addHeader("Authorization", samlTokenJson.getString("token_type")+" "+samlTokenJson.getString("access_token"));
			HttpResponse httpResponse = httpclient.execute(httpGet);
			
			System.out.println("----------HTTP Request headers start--------------");
			for(int i = 0; i < httpGet.getAllHeaders().length; i++){
				System.out.println(httpGet.getAllHeaders()[i].getName()+"-->"+httpGet.getAllHeaders()[i].getValue());
			}
			System.out.println("----------HTTP Request headers end--------------");
			System.out.println("----------HTTP Response headers start--------------");
			for(int i = 0; i < httpResponse.getAllHeaders().length; i++){
				System.out.println(httpResponse.getAllHeaders()[i].getName()+"-->"+httpResponse.getAllHeaders()[i].getValue());
			}
			System.out.println("----------HTTP Response headers end--------------");

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
			String response = HtppResponseHelper.getResponseBody(inputStream);
			System.out.println("response: "+response);
			
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
	

}
