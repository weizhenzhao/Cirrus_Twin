package com.springboot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BlobServiceUtil {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String upLoadUrl = "https://predix-blob.run.aws-usw02-pr.ice.predix.io/v1/blob";
	private String listNameUrl = "https://predix-blob.run.aws-usw02-pr.ice.predix.io/v1/bloblist/123123";
	private String downloadUrl = "https://predix-blob.run.aws-usw02-pr.ice.predix.io/v1";

	/**
	 * download file from blob service interface to call
	 * 
	 * @param fileNames
	 * @param indexFile 
	 */
	public void downLoadFile(List<String> fileNames, File indexFile) {
		for (String name : fileNames) {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(downloadUrl + "/" + name);
			HttpResponse httpResponse = null;
			HttpEntity entity = null;
			try {
				httpResponse = httpclient.execute(httpPost);
				entity = httpResponse.getEntity();
				OutputStream outputStream = new FileOutputStream(indexFile.getPath() + File.separator + name);
				entity.writeTo(outputStream);
				outputStream.close();
			} catch (ClientProtocolException e) {
				logger.info("protocol exception in uploadFile in BlobServiceUtil:" + e.getMessage());
			} catch (IOException e) {
				logger.info("ioexception in uploadFile in BlobServiceUtil" + e.getMessage());
			}finally {
				try {
					httpclient.close();
				} catch (IOException e) {
					logger.info("close httpclient error in downLoadFile" + e.getMessage());
				}
			}
		}
	}

	/**
	 * upload file to blob service interface to call
	 * 
	 * @return
	 */
	public Boolean uploadIndex(File indexFile) {
		boolean uploadResult = true;
		File[] files = indexFile.listFiles();
		for (File item : files) {
			boolean tempResult = uploadFile(item);
			if (tempResult == false) {
				uploadResult = false;
			}
		}
		return uploadResult;
	}

	/**
	 * upload file to blob service method to invoke
	 * 
	 * @param file
	 * @return
	 */
	public boolean uploadFile(File file) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(upLoadUrl);
		HttpResponse httpResponse = null;
		FileBody fileBody = new FileBody(file);
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.addPart("file", fileBody);
		HttpEntity httpEntity = multipartEntityBuilder.build();
		httpPost.setEntity(httpEntity);
		try {
			httpResponse = httpclient.execute(httpPost);
		} catch (ClientProtocolException e) {
			logger.info("protocol exception in uploadFile in BlobServiceUtil:" + e.getMessage());
			return false;
		} catch (IOException e) {
			logger.info("ioexception in uploadFile in BlobServiceUtil" + e.getMessage());
			return false;
		}finally{
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.info("close httpclient error in uploadFile" + e.getMessage());
			}
		}
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * get file list from blob service interface to call
	 * 
	 * @return
	 */
	public List<String> getFileList() {
		List<String> names = new ArrayList<String>();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(listNameUrl);
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpclient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity entity = httpResponse.getEntity();
			if (statusCode == HttpStatus.SC_OK) {
				String response_String = EntityUtils.toString(entity);
				logger.info(response_String);
				if (response_String != null) {
					try {
						JSONTokener jsonParser = new JSONTokener(response_String);
						JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
						JSONArray namesValue = jsonObject.getJSONArray("fileNames");
						for (int i = 0; i < namesValue.length(); i++) {
							names.add((String) namesValue.get(i));
						}
					} catch (Exception ex) {
						logger.info("JSON parse error in blob-service");
					}
				}
			}
		} catch (ClientProtocolException e) {
			logger.info("protocol exception in uploadFile in BlobServiceUtil:" + e.getMessage());
		} catch (IOException e) {
			logger.info("ioexception in uploadFile in BlobServiceUtil" + e.getMessage());
		}finally{
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.info("close httpclient error in getFileList" + e.getMessage());
			}
		}
		return names;
	}

}
