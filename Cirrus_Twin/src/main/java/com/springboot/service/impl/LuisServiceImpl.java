package com.springboot.service.impl;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.springboot.service.LuisService;

@Component
public class LuisServiceImpl implements LuisService{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static String LuisUrl = "https://westcentralus.api.cognitive.microsoft.com/luis/v2.0/apps/"
			+ "eaa9df3b-3417-4e04-a3ee-af8c49fd0976?" + "subscription-key=706ca914d14c483f8c287c23dd76fec9"
			+ "&timezoneOffset=0" + "&verbose=true" + "&q=";

	public String getIntent(String question) {
		HttpClient httpclient = new DefaultHttpClient();
		String intentUrl = LuisUrl + URLEncoder.encode(question);
		HttpGet httpGet = new HttpGet(intentUrl);
		try {
			HttpResponse response = httpclient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				String response_String = EntityUtils.toString(response.getEntity());
				if (response_String != null) {
					try {
						JSONTokener jsonParser = new JSONTokener(response_String);
						JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
						JSONObject hearValue = jsonObject.getJSONObject("topScoringIntent");
						return (String) hearValue.getString("intent");
					} catch (Exception ex) {
						logger.info("json parse error in LuisService getIntent:" + ex.getMessage());
					}
				}
			}
		} catch (ClientProtocolException e) {
			logger.info("ClientProtocolException in LuisService getIntent:" + e.getMessage());
		} catch (IOException e) {
			logger.info("IOException in LuisService getIntent:" + e.getMessage());
		}
		return null;
	}

}
