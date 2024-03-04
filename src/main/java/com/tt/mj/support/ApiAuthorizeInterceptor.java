package com.tt.mj.support;

import com.tt.mj.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@RequiredArgsConstructor
public class ApiAuthorizeInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String apiSecret = request.getHeader(Constants.API_SECRET_HEADER_NAME);

		//TODO 用户验证

		return true;
	}

}
