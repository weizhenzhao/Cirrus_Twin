package com.springboot.util;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Speech2Text {

	private static String accessToken = null;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public String getSpeechToTextResult(String filePath) throws JSONException{
		String result = null;
		if (isAccessable()) {
			result = speechToText(filePath);
		}
		return result;
	}

	public String speechToText(String filePath) throws JSONException {
		String url = "https://speech.platform.bing.com/recognize?" + "version=3.0"
				+ "&requestid=b2c95ede-97eb-4c88-81e4-80f32d6aee54" + "&appID=D4D52672-91D7-4C74-8AD8-42B1D98141A5"
				+ "&format=json" + "&locale=en-Us" + "&device.os=Android" + "&scenarios=ulm"
				+ "&instanceid=b2c95ede-97eb-4c88-81e4-80f32d6aee54";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		try {
			File tempFile = new File(filePath);
			logger.info(tempFile.getAbsolutePath());
			AudioInputStream cin = AudioSystem.getAudioInputStream(tempFile);
			byte[] bytes = new byte[(int) tempFile.length()];
			cin.read(bytes);
			cin.close();
			httpPost.setHeader(new BasicHeader("Authorization", "Bearer " + accessToken));
			HttpEntity body = new FileEntity(tempFile, "audio/wav; samplerate=44100");
			httpPost.setEntity(body);
			HttpResponse response = httpclient.execute(httpPost);
			logger.info("response:" + response.toString());
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("status code:" + statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				String response_String = EntityUtils.toString(response.getEntity());
				logger.info(response_String);
				try {
					JSONTokener jsonParser = new JSONTokener(response_String);
					JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
					JSONObject hearValue = jsonObject.getJSONObject("header");
					return (String) hearValue.getString("name");
				} catch (Exception ex) {
					logger.info("json parse error in Speech2Text speechToText");
					return " ";
				}
			}else{
				logger.info("can't connect to microsoft web service");
				return "sorry there's a net work error";
			}
		} catch (Exception exp) {
			logger.info("error in speech to text");
			return "sorry there's a net work error";
		}
	}

	public boolean isAccessable() {
		String url = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Ocp-Apim-Subscription-Key","fd0590e32036418db5df8c36e5d7cde3");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException e) {
			logger.info("ClientProtocolException in isAccessable in speech2text");
		} catch (IOException e) {
			logger.info("IOException in isAccessable in speech2text");
		}
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			logger.info("Error authentication to microsoft status:" + statusCode);
			return false;
		}
		String getResult = null;
		try {
			getResult = EntityUtils.toString(response.getEntity());
			accessToken = getResult;
			logger.info("access token" + getResult);
			return true;
		} catch (Exception e) {
			logger.info("error in parse access result");
			return false;
		}
	}

}
