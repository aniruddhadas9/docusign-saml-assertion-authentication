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

public class DocumentUsingSAML {
	
	
	public InputStream getDocument() {
		JSONObject samlTokenJson = null;
		samlTokenJson = new TokenBySAMLAsserertion().getToken();
		//samlTokenJson = new JSONObject("{\"access_token\": \"AAEAAGQZtDqUpWbyAJcbLbqrnEb6PEh8wrVBybDDrcFhkXey-_NEHd3AuZGdz5Wq_8upbwlHHY1Nbq2tll_zSaIJCCiWSYYCxJd2aP1rErisEaeKNVmZj7hOWyf-osZeRCvlu12S1NoRsHg4HwtiGd2TgQJjsPQdpNiy0hOJcCbyNSJgEV1VsfRYSLAYbzGFD9UzSBgB4WdU3vkQJnqhkiTlsoRyhl0jstzTttgmGoxQCjJwGoehgbuG-EZIkFqr28HcOuDQSN13Xv0wOo3n8mtFMwq2d_NGPpwAbALfw5UyeBfIyWJ6NK1_tTfVjDbliR0cGDxKQu-wADZWMkHYuyb0Kq7EAQAAAAEAAGN0-LlfT6DSq_LBZgULgbGF7WFVt9DCQnVZedFqkiMsMgvmdnSsGycWU2WLSdBotlU_raJMhPYSjK5fspz1i-nSsgV9LT4ss9_5LGIhgAGKwQeYFcoKpdrq1nzlt17VX3gfBDQ2wzlgLcE-t3hTS8a2PaAQ9r0TbpToQ5mhd_3xSG2XO3M6i2yjpL8T3sZMN7Ag5OtFDgZcV8Sl3lrhwrNwShSeIc2Hp67EnbCXurC9TiNYnYXyfJwGZGZN5erDdHDPyWrzb-dvZ51QO_vsNvQAKCJZa7ZCUg9ulDuGCc7HMVZL7JubEZvfnTay85yrrPI3XwhCdg8QrfIuJiZoAGLs8B9w6kS86xKIzLg8LL2KrxkY7oXgcaW9okjtQQObSZMNV0qo9I_PiZPP6ciKE3CQxO3jhU9VRzFSKj4RW5q1B-kfWErXUWUpva5GYSfsEchDThRVn1HiSGjCZnRVdiGGvtU-wtUUVLIlo4TZOtNRUFi2GdxbuC_Vp6WqAk0qTi82iPe6Wt00Zdoni97zbGhL05mKNw4_lpNLEGz45YnNNEMvvG3SbfzmY-L9d3dp-LeiiF7D_KtMcjTQYjc9SdM\",  \"token_type\": \"bearer\",  \"expires_in\": \"3600\",  \"scope\": \"api\"}");
		InputStream inputStream = null;

		try {
			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			
			String folder = "C:\\eSignature\\";
			String envId = "d03c0a5f-1ef7-42ef-b601-d22cdb2ad833";
			String filename = "Ranjna.pdf";
			int docId = 1;
			
			String url = HtppResponseHelper.baseUrl + "/envelopes/"+envId+"/documents/"+docId;

			// 2. make POST request to the given URL
			HttpGet httpGet = new HttpGet(url);

			String bodyJson = "";

			// 3. build jsonObject
			JSONObject bodyJsonObject = new JSONObject();
			bodyJsonObject.accumulate("phone", "4156509102");

			// 4. convert JSONObject to JSON to String
			bodyJson = bodyJsonObject.toString();

			// 5. set json to StringEntity
			StringEntity se = new StringEntity(bodyJson);

			// 6. set httpPost Entity
			//httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			httpGet.addHeader("Accept", "application/json");
			httpGet.addHeader("Content-Type", "application/json");
			httpGet.addHeader("Authorization", samlTokenJson.getString("token_type")+" "+samlTokenJson.getString("access_token"));
			
			// 8. Execute
			HttpResponse httpResponse = httpclient.execute(httpGet);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
			String response = HtppResponseHelper.getResponseBody(inputStream);
			
			System.out.println("response: "+response);

		} catch (ClientProtocolException e) {
			System.out.println("ClientProtocolException : " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println("IOException:" + e.getLocalizedMessage());
		} catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}

		return inputStream;
	}
	
	public static void main(String[] args) {
		InputStream jsonObject = new DocumentUsingSAML().getDocument();
		System.out.println("data:" + jsonObject.toString());
		
	}


}
