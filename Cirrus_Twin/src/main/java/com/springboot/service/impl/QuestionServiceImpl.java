package com.springboot.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.springboot.dao.QuestionDao;
import com.springboot.service.QuestionService;
@Component
public class QuestionServiceImpl implements QuestionService{
	@Autowired
	private QuestionDao questionDao;

	@Override
	public Map<String,Object> getAnswerByIntent(String intent) {
		return questionDao.getAnswerByIntent(intent);
	}
}
