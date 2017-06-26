package com.springboot.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.springboot.dao.QuestionDao;

@Component
public class QuestionDaoImpl implements QuestionDao {

	private Random random;

	public QuestionDaoImpl() {
		random = new Random();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Map<String, Object> getAnswerByIntent(String intent) {
		List<String> customAnswer = new ArrayList<String>();
		customAnswer.add("I didn't get that.");
		customAnswer.add("I'm not sure what you just said.");
		customAnswer.add("Sorry,I didn't get what you just said.");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if ("None".equals(intent) || null == intent||" ".equals(intent)) {
			random = new Random();
			int num = random.nextInt(3);
			resultMap.put("answer", customAnswer.get(num));
			resultMap.put("num", num);
			return resultMap;
		} else {
			try {
				String sql = "SELECT T.ANSWER,T.QUESTION FROM ANSWER T WHERE T.QUESTION = '" + intent + "'";
				resultMap = jdbcTemplate.queryForMap(sql);
				return resultMap;
			} catch (Exception e) {
				return resultMap;
			}
		}
	}

}
