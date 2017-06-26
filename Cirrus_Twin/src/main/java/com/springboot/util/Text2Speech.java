package com.springboot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Text2Speech {

	private static String accessToken = null;
	private static UUID appId = UUID.randomUUID();
	private static UUID clientId = UUID.randomUUID();

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public String convertTextToSpeech(String writtenText, String tempFilePath, String gender) {
		String result = null;
		if (isAccessable()) {
			result = textToSpeech(writtenText, tempFilePath, gender);
		}
		return result;
	}

	private String textToSpeech(String textForSpeech, String tempFilePath, String gender) {
		String speakerName = "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)";
		if ("male".equals(gender)) {
			speakerName = "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)";
		}
		String url = "https://speech.platform.bing.com/synthesize";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setHeader(new BasicHeader("Authorization", "Bearer " + accessToken));
			httpPost.setHeader("Content-Type", "application/ssml+xml");
			httpPost.setHeader("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3");
			httpPost.setHeader("X-Search-AppId", appId.toString().replace("-", ""));
			httpPost.setHeader("X-Search-ClientID", clientId.toString().replace("-", ""));
			httpPost.setHeader("User-Agent", "Predix_text_to_speech");
			HttpEntity body = new StringEntity("<speak version='1.0' xml:lang='en-US'>" + "<voice xml:lang='en-US'"
					+ " xml:gender='" + gender + "'" + " OutputFormat='Audio16khz32kbitrateMonoMp3'" + " name='"
					+ speakerName + "'>" + textForSpeech + "</voice>" + "</speak>");
			httpPost.setEntity(body);
			HttpResponse response = httpclient.execute(httpPost);
			System.out.println("response:" + response.toString());
			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println("status code:" + statusCode);
			System.out.println("temp file path:" + tempFilePath);
			HttpEntity entity = response.getEntity();
			String tempDir = tempFilePath;
			File dir = new File(tempDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String path = dir + File.separator + System.currentTimeMillis() + ".wav";
			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.createNewFile();
			}
			OutputStream outputStream = new FileOutputStream(tempFile);
			entity.writeTo(outputStream);
			outputStream.close();
			return path;
		} catch (Exception exp) {
			logger.info("texttospeech parse error in call mircosoft cognitive service");
		}
		return null;
	}

	private boolean isAccessable() {
		String url = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Ocp-Apim-Subscription-Key", "fd0590e32036418db5df8c36e5d7cde3");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException e) {
			logger.info("clientProtocol error in isAccessable in Ocp-Apim-Subscription-Key execute");
		} catch (IOException e) {
			logger.info("IOException error in isAccessable in Ocp-Apim-Subscription-Key execute");
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
			logger.info("Access true");
			return true;
		} catch (Exception e) {
			logger.info("Error parse result from isAccessable");
			return false;
		}
	}

	public Text2Speech() {
		super();
	}

}
