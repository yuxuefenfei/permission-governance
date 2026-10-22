package com.example.permissionhub.controller;

import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.CurrentUser;
import com.example.permissionhub.dto.LoginRequest;
import com.example.permissionhub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 登录、用户信息和 CSRF 初始化入口。注销交给 Spring Security LogoutFilter。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextRepository;
    private final AuthService authService;

    @GetMapping("/csrf")
    public ApiResponse<String> csrf(CsrfToken csrfToken) {
        return ApiResponse.ok(csrfToken.getToken());
    }

    @PostMapping("/login")
    public ApiResponse<CurrentUser> login(@Valid @RequestBody LoginRequest body,
                                          HttpServletRequest request, HttpServletResponse response) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        // 主动认证后轮换 session id，避免会话固定攻击。
        new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return ApiResponse.ok(authService.currentUser(authentication.getName()));
    }

    /**
     * 返回当前有效权限码，供前端控制按钮展示；并非最终的安全校验。
     */
    @GetMapping("/grants")
    public ApiResponse<List<String>> grants() {
        return ApiResponse.ok(authService.grants(currentUsername()));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUser> me() {
        return ApiResponse.ok(authService.currentUser(currentUsername()));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }
}
