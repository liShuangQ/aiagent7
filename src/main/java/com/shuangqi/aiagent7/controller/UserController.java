package com.shuangqi.aiagent7.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.dto.UserLoginDTO;
import com.shuangqi.aiagent7.service.UserService;
import com.shuangqi.aiagent7.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/user", produces = "application/json;charset=utf-8")
public class UserController {
    private JwtUtil jwtUtil;

    private final UserService userService;

    public UserController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Validated UserLoginDTO userLoginDTO, HttpServletResponse response) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        TUser user = userService.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, userLoginDTO.getUsername()));
        if (user == null) {
            return Result.error("用户名不存在");
        }

        if (!user.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(username);
        response.setHeader(JwtUtil.HEADER, token);
        response.setHeader("Access-control-Expost-Headers", JwtUtil.HEADER);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);

        return Result.success(map);
    }

    //@PreAuthorize配合@EnableGlobalMethodSecurity(prePostEnabled = true)使用
    //@PreAuthorize("hasAuthority('/user/logout')")
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 退出登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //清除认证
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return Result.success("操作成功");
    }
}