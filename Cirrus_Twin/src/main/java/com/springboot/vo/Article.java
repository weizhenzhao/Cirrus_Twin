package com.springboot.vo;

public class Article {
	private String answer;
	private String question;
	private String parameterId;
	private String newQuestion;
	
	public Article(String answer, String question, String parameterId, String newQuestion) {
		super();
		this.answer = answer;
		this.question = question;
		this.parameterId = parameterId;
		this.newQuestion = newQuestion;
	}

	public String getNewQuestion() {
		return newQuestion;
	}

	public void setNewQuestion(String newQuestion) {
		this.newQuestion = newQuestion;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Article() {
		super();
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

}
