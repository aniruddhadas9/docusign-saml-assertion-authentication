package com.etouch.auth;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.etouch.service.HtppResponseHelper;

public class TokenByUsername {

	public JSONObject post() {
		InputStream inputStream = null;

		try {
			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(HtppResponseHelper.authUrl);

			/*String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("phone", "4156509102");

			// 4. convert JSONObject to JSON to String
			json = jsonObject.toString();*/

			// 5. set json to StringEntity
			StringEntity se = new StringEntity(HtppResponseHelper.authReqString);

			// 6. set httpPost Entity
			httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			httpPost.addHeader("Accept", "application/json");
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

			// 8. Execute
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
			String response = HtppResponseHelper.getResponseBody(inputStream);
			
			System.out.println(response);

		} catch (ClientProtocolException e) {
			System.out.println("ClientProtocolException : " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println("IOException:" + e.getLocalizedMessage());
		} catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}

		return null;
	}
	
	public static void main(String[] args) {
		new TokenByUsername().post();
	}
	
}
