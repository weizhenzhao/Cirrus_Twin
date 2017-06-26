package com.springboot.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.service.LuisService;
import com.springboot.service.QuestionService;
import com.springboot.util.BlobServiceUtil;
import com.springboot.util.ExtractKeyWords;
import com.springboot.util.Speech2Text;
import com.springboot.util.Text2Speech;

@RestController
@EnableAutoConfiguration
@RequestMapping("/CirrusTwin")
public class SoundController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String preQuestion = null;

	@Autowired
	private QuestionService questionService;
	
	@Autowired
	private LuisService luisService;
	
	@Autowired
	private ExtractKeyWords extrackKeyWords;

	@Autowired
	private Text2Speech text2Speech;

	@Autowired
	private Speech2Text speech2Text;
	
	@Autowired
	private BlobServiceUtil blobServiceUtil;

	/**
	 * in: question's voice 
	 * out:answer's text,
	 *     question's text, 
	 *     parameterId 
	 * or
	 * 	   errormessage
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @throws Exception
	 */
	@RequestMapping(value = "/SoundCommit", method = RequestMethod.POST)
	public String soundcommit(HttpServletRequest request, 
			  				  HttpServletResponse response) {
		String intent = null;
		Map<String,Object> answerMap =null;
		JSONObject jsonObject = new JSONObject();
		String speechResult = "";
		Part part = null;
		try {
			part = request.getPart("audioData");
			String uploadPath = request.getSession().getServletContext().getRealPath("/upload");
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}
			String realName = System.currentTimeMillis() + ".wav";
			String realPath = uploadPath + File.separator + realName;
			part.write(realPath);
			speechResult = speech2Text.getSpeechToTextResult(realPath);
			intent = luisService.getIntent(speechResult);
			answerMap = questionService.getAnswerByIntent(intent);
			if (answerMap.size()>0) {
				jsonObject.put("question", speechResult);
				if("None".equals(intent)|| null == intent||" ".equals(intent)){
					jsonObject.put("parameterId", answerMap.get("num"));
					jsonObject.put("answer", "None");
				}else{
					jsonObject.put("answer", intent);
				}
			}
		} catch (IOException e) {
			logger.info("file not find exception in SoundCommit" + e.getMessage());
		} catch (ServletException e) {
			logger.info("servlet exception in soundcommit:" + e.getMessage());
		} catch (JSONException e) {
			logger.info("json put error in soundcommit:" + e.getMessage());
		}
		return jsonObject.toString();
	}

	/**
	 * in:question's voice 
	 * out:answer's answervoice,
	 *     parameterId, 
	 *     answerText,
	 *     question, 
	 * or error message's voice
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/soundcommitvoice/{gender}")
	public String soundcommitvoice(@PathVariable String gender,
								   HttpServletRequest request, 
								   HttpServletResponse response) {
		String intent = null;
		Map<String,Object> answerMap = null;
		JSONObject jsonObject = new JSONObject();
		String speechResult = "";
		String value = "";
		String voicePath = "";
		File audioFile = null;
		Part part = null;
		try {
			part = request.getPart("audioData");
			String uploadPath = request.getSession().getServletContext().getRealPath("/upload");
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}
			String realName = System.currentTimeMillis() + ".wav";
			String realPath = uploadPath + File.separator + realName;
			part.write(realPath);
			speechResult = speech2Text.getSpeechToTextResult(realPath);
			intent = luisService.getIntent(speechResult);
			answerMap = questionService.getAnswerByIntent(intent);
			if (answerMap.size()>0) {
				value = (String) answerMap.get("answer");
				jsonObject.put("question", speechResult);
				if("None".equals(intent)|| null == intent||" ".equals(intent)){
					jsonObject.put("parameterId", answerMap.get("num"));
					jsonObject.put("answer", "None");
				}else{
					jsonObject.put("answer", intent);
				}
			} 
			String tempFilePath = request.getSession().getServletContext().getRealPath("/TEMP");
			File tempFile = new File(tempFilePath);
			if (!tempFile.exists()) {
				tempFile.mkdir();
			}
			voicePath = text2Speech.convertTextToSpeech(value, tempFilePath,gender);
			audioFile = new File(voicePath);
			jsonObject.put("answervoice", audioFile);
		} catch (IOException e) {
			logger.info("file not find exception in SoundCommit" + e.getMessage());
		} catch (ServletException e) {
			logger.info("servlet exception in soundcommit:" + e.getMessage());
		} catch (JSONException e) {
			logger.info("json put error in soundcommit:" + e.getMessage());
		}
		return jsonObject.toString();
	}

	/**
	 * in: question's text 
	 * out:answer's text, 
	 * 	   parameterId 
	 * or  errormessage's text
	 * 
	 * @param questionText
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getAnswerByText/{questionText}")
	public String getAnswerByText(@PathVariable String questionText,
			 					  HttpServletRequest request) {
		if("invoice".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to book invoice"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to delete the invoice";
			}
			else if("3".equals(questionText)){
				questionText = "how to query invoice";
			}
		}
		if("report".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to print the invoice report"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to run aging report";
			}
			else if("3".equals(questionText)){
				questionText = "how to run open invoice report ";
			}
			else if("4".equals(questionText)){
				questionText = "how to run invoice list report";
			}
		}
		if("customer".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to add customer"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to disable customer";
			}
			else if("3".equals(questionText)){
				questionText = "how to query customer";
			}
			else if("4".equals(questionText)){
				questionText = "how to reference to customer po number";
			}
			else if("5".equals(questionText)){
				questionText = "how to update customer";
			}
		}
		String intent = luisService.getIntent(questionText);
		Map<String,Object> answerMap = questionService.getAnswerByIntent(intent);
		JSONObject obj = new JSONObject();
		try {
			if (answerMap.size()>0) {
				obj.put("answer", intent);
				if("None".equals(intent)|| null == intent||" ".equals(intent)){
					obj.put("parameterId", answerMap.get("num"));
				}
			} else {
				obj.put("errormessage", "please correct your input");
			}
		} catch (JSONException e) {
			logger.info("json put error in Controller in getAnswerByText:" + e.getMessage());
		}
		this.preQuestion = questionText;
		return obj.toString();
	}

	/**
	 * in: question's text
	 * out:answer's voice,
	 *     parameterId 
	 * or errormessage's voice
	 * 
	 * @param questionText
	 * @param request
	 * @return answer's voice
	 * @throws Exception
	 */
	@RequestMapping("/getAnswerByText2Voice/{questionText}/{gender}")
	public String getAnswerByText2Voice(@PathVariable String questionText,
										HttpServletRequest request,
										@PathVariable String gender) {
		if("invoice".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to book invoice"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to delete the invoice";
			}
			else if("3".equals(questionText)){
				questionText = "how to query invoice";
			}
		}
		if("report".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to print the invoice report"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to run aging report";
			}
			else if("3".equals(questionText)){
				questionText = "how to run open invoice report ";
			}
			else if("4".equals(questionText)){
				questionText = "how to run invoice list report";
			}
		}
		if("customer".equals(this.preQuestion)){
			if("1".equals(questionText)){
				questionText = "how to add customer"; 
			}
			else if("2".equals(questionText)){
				questionText = "how to disable customer";
			}
			else if("3".equals(questionText)){
				questionText = "how to query customer";
			}
			else if("4".equals(questionText)){
				questionText = "how to reference to customer po number";
			}
			else if("5".equals(questionText)){
				questionText = "how to update customer";
			}
		}
		String answer = null;
		File audioFile = null;
		JSONObject obj = new JSONObject();
		String intent = luisService.getIntent(questionText);
		Map<String,Object> answerMap = questionService.getAnswerByIntent(intent);
		if (answerMap.size()>0) {
			answer = (String) answerMap.get("answer");
			try {
				if("None".equals(intent)|| null == intent||" ".equals(intent)){
					obj.put("parameterId", answerMap.get("num"));
				}
			} catch (JSONException e) {
				logger.info("json put error in Controller in getAnswerByText2Voice:" + e.getMessage());
			}
		} else {
			answer = "please correct your input";
		}
		String tempFilePath = request.getSession().getServletContext().getRealPath("/TEMP");
		File tempFile = new File(tempFilePath);
		if (!tempFile.exists()) {
			tempFile.mkdir();
		}
		String voicePath = text2Speech.convertTextToSpeech(answer, tempFilePath,gender);
		audioFile = new File(voicePath);
		try {
			obj.put("answer", intent);
			obj.put("answervoice", audioFile);
		} catch (JSONException e) {
			logger.info("json put error in Controller in getAnswerByText2Voice:" + e.getMessage());
		}
		this.preQuestion = questionText;
		return obj.toString();
	}

	/**
	 * only text to voice
	 * return voice's path
	 * 
	 * @param writtenText
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/textToSpeech/{writtenText}/{gender}")
	public String texttospeech(@PathVariable String gender,
			  				   @PathVariable String writtenText, 
			  				   HttpServletRequest request) {
		
		File audioFile = null;
		String audioPath = null;
		String tempFilePath = request.getSession().getServletContext().getRealPath("/TEMP");
		File tempFile = new File(tempFilePath);
		if (!tempFile.exists()) {
			tempFile.mkdir();
		}
		audioPath = text2Speech.convertTextToSpeech(writtenText, tempFilePath,gender);
		audioFile = new File(audioPath);
		JSONObject obj = new JSONObject();
		try {
			obj.put("filepath", audioFile);
		} catch (JSONException e) {
			logger.info("json put error in Controller in texttospeech:" + e.getMessage());
		}
		return obj.toString();
	}

	public SoundController() {
		
	}

	public ExtractKeyWords getExtrackKeyWords() {
		return extrackKeyWords;
	}

	public void setExtrackKeyWords(ExtractKeyWords extrackKeyWords) {
		this.extrackKeyWords = extrackKeyWords;
	}

	public Text2Speech getText2Speech() {
		return text2Speech;
	}

	public void setText2Speech(Text2Speech text2Speech) {
		this.text2Speech = text2Speech;
	}

	public Speech2Text getSpeech2Text() {
		return speech2Text;
	}

	public void setSpeech2Text(Speech2Text speech2Text) {
		this.speech2Text = speech2Text;
	}
	
	public BlobServiceUtil getBlobServiceUtil() {
		return blobServiceUtil;
	}

	public void setBlobServiceUtil(BlobServiceUtil blobServiceUtil) {
		this.blobServiceUtil = blobServiceUtil;
	}
	
	public String getPreviousQuestion() {
		return preQuestion;
	}

	public void setPreviousQuestion(String preQuestion) {
		this.preQuestion = preQuestion;
	}

}
