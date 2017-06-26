package com.springboot.dao;

import java.util.Map;

import org.springframework.stereotype.Component;
@Component
public interface QuestionDao {
	public Map<String, Object> getAnswerByIntent(String intent);
}
