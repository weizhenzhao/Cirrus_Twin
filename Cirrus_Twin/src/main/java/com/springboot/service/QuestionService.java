package com.springboot.service;

import java.util.Map;

import org.springframework.stereotype.Component;
@Component
public interface QuestionService {
	public Map<String, Object> getAnswerByIntent(String intent);
}
