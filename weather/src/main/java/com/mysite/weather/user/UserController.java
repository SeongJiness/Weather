package com.mysite.weather.user;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}
	
	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return "signup_form";
		}
		
		if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
			return "signup_form";
		}
		
		try{
        	userService.create(userCreateForm.getUsername(), 
            userCreateForm.getEmail(), userCreateForm.getPassword1());
        }catch(DataIntegrityViolationException e) {
        	e.printStackTrace();
        	bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
        	return "signup_form";
        }catch(Exception e) {
        	e.printStackTrace();
        	bindingResult.reject("signupFailed" , e.getMessage());
        	return "signup_form";
        }
		return "redirect:/";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login_form";
	}
	
	@GetMapping("/profile")
	public String profile(Principal principal, Model model, RedirectAttributes redirectAttributes) {
	    // 인증되지 않은 사용자 처리
	    if (principal == null) {
	        return "redirect:/user/login";  // 로그인 페이지로 리다이렉트
	    }

	    // Principal 객체에서 로그인된 사용자의 username을 가져옴
	    String username = principal.getName();

	    // UserService를 통해 사용자 정보를 조회
	    SiteUser user = userService.findByUsername(username);

	    // 사용자가 존재하지 않는 경우 처리
	    if (user == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
	        return "redirect:/user/signup"; 
	    }

	    // 모델에 사용자 정보를 추가하여 뷰로 전달
	    model.addAttribute("user", user);

	    return "user_profile";  // 사용자 프로필 화면으로 이동
	}
	
	@GetMapping("/profile/edit")
	public String profile_edit(Principal principal, Model model) {
	    if (principal == null) {
	        return "redirect:/user/login";
	    }

	    String username = principal.getName();
	    SiteUser user = userService.findByUsername(username);

	    if (user == null) {
	        return "redirect:/user/signup";
	    }

	    model.addAttribute("user", user);
	    

	    return "user_profile_edit"; // 수정된 프로필 페이지
	}
	
	
	@PostMapping("/profile/edit")
	public String updateProfile(
	        Principal principal,
	        @RequestParam("email") String email,
	        @RequestParam("currentPassword") String currentPassword,
	        @RequestParam(value = "password1", required = false) String password1,
	        @RequestParam(value = "password2", required = false) String password2,
	        RedirectAttributes redirectAttributes) {

	    if (principal == null) {
	        return "redirect:/user/login";
	    }

	    String username = principal.getName();
	    SiteUser user = userService.findByUsername(username);

	    if (user == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
	        return "redirect:/user/signup";
	    }

	    // 1️. 현재 비밀번호가 맞는지 검증
	    if (!userService.checkPassword(user, currentPassword)) {
	        redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
	        return "redirect:/user/profile/edit";
	    }

	    // 2️. 이메일 수정
	    user.setEmail(email);

	    // 3️. 새 비밀번호 변경 요청이 있을 경우
	    if (password1 != null && !password1.isEmpty()) {
	        if (!password1.equals(password2)) {
	            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
	            return "redirect:/user/profile/edit";
	        }
	        userService.updatePassword(user, password1);
	    }

	    // 4️. 저장
	    userService.save(user);
	    redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
	    return "redirect:/user/profile";
	}
	
	@PostMapping("/delete")
	public String deleteUser(Principal principal, RedirectAttributes redirectAttributes,
	                         HttpServletRequest request, HttpServletResponse response) {
	    if (principal == null) {
	        return "redirect:/user/login";
	    }

	    String username = principal.getName();

	    SiteUser user;
	    try {
	        user = userService.findByUsername(username);
	    } catch (RuntimeException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
	        return "redirect:/user/signup";
	    }

	    userService.deleteUser(user);

	    // 로그아웃 처리
	    new SecurityContextLogoutHandler().logout(request, response, null);

	    redirectAttributes.addFlashAttribute("message", "탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
	    return "redirect:/";
	}
}
