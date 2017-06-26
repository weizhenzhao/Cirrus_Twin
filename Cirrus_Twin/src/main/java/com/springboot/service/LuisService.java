package com.springboot.service;

import org.springframework.stereotype.Component;

@Component
public interface LuisService {
	public String getIntent(String question);
}
