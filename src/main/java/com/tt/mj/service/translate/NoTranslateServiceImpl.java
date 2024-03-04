package com.tt.mj.service.translate;


import com.tt.mj.service.TranslateService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoTranslateServiceImpl implements TranslateService {

	@Override
	public String translateToEnglish(String prompt) {
		return prompt;
	}
}
