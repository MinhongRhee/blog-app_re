package com.cos.blogapp.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cos.blogapp.domain.user.User;
import com.cos.blogapp.handler.exception.MyAsyncNotFoundException;
import com.cos.blogapp.service.UserService;
import com.cos.blogapp.util.Script;
import com.cos.blogapp.web.dto.CMRespDto;
import com.cos.blogapp.web.dto.JoinReqDto;
import com.cos.blogapp.web.dto.LoginReqDto;
import com.cos.blogapp.web.dto.UserUpdateDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

	private final UserService userService;
	private final HttpSession session;
	
	@PutMapping("/user/{id}")
	public @ResponseBody CMRespDto<String> update(@PathVariable int id, @Valid @RequestBody UserUpdateDto dto, BindingResult bindingResult) {
		// 인증
		User principal = (User) session.getAttribute("principal");
		if (principal == null) { // 로그인 안됨
			throw new MyAsyncNotFoundException("인증이 되지 않았습니다.");
		}
		
		// 유효성 검사
		if (bindingResult.hasErrors()) {
			Map<String, String> errorMap = new HashMap<>();
			for (FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
			}
			throw new MyAsyncNotFoundException(errorMap.toString());
		}
		
		userService.회원수정(id, principal, dto);
		
		// 세션 동기화 해주는 부분
		principal.setEmail(dto.getEmail());		
		session.setAttribute("principal", principal); // 세션값 변경
		
		return new CMRespDto<>(1, "성공", null);
	}
	
	@GetMapping("/user/{id}")
	public String userinfo(@PathVariable int id) {
		// 기본은 userRepository.findById(id) DB에서 가져와야 함.
		// 편법은 세션값을 가져올 수 도 있다.
		
		return "user/updateForm";
	}
	
	@GetMapping("/logout")
	public String logout() {
		// return "board/list"; // 게시글 목록 화면에 데이터 x
		session.invalidate(); // 세션 무효화(jsessionid에 있는 값을 비우는 것)
		return "redirect:/";
	}
	
	@GetMapping("/loginForm")
	public String loginForm() {
		return "user/loginForm";
	}
	
	@GetMapping("/joinForm")
	public String joinForm() {
		return "user/joinForm";
	}
	
	@PostMapping("/login")
	public @ResponseBody String login(@Valid LoginReqDto dto, BindingResult bindingResult) {	
		System.out.println("에러사이즈:" + bindingResult.getFieldErrors().size());

		if( bindingResult.hasErrors() ) {
			Map<String, String> errorMap = new HashMap<>();
			for(FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
			}	
			return Script.back(errorMap.toString());
		} 

		if(userService.로그인(dto) == null) { 	// username, password 잘못 기입
			return Script.back("아이디 혹은 비밀번호를 잘못 입력하였습니다");
		} else { 
			// 세션이 날라가는 조건 : 1. session.invalidate(), 2. 브라우저를 닫으면 날라감
			session.setAttribute("principal", userService.로그인(dto));
			return Script.href("/","로그인 성공");
		}		
	}
	
	@PostMapping("/join")
	public @ResponseBody String join(@Valid JoinReqDto dto, BindingResult bindingResult) { 
		
		if( bindingResult.hasErrors() ) {
			Map<String, String> errorMap = new HashMap<>();
			for(FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
			}
			
			return Script.back(errorMap.toString());
		}
		
		userService.회원가입(dto);
		return Script.href( "/loginForm" ); // 리다이렉션 (300)
	}
	
}



