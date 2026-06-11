package com.oa.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.dto.AuthToken;
import com.oa.common.dto.LoginRequest;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.RegisterRequest;
import com.oa.common.entity.SysRole;
import com.oa.common.entity.SysUser;
import com.oa.common.entity.SysUserRole;
import com.oa.common.result.AjaxResult;
import com.oa.common.security.JwtProperties;
import com.oa.common.security.JwtUtil;
import com.oa.common.security.TokenConstants;
import com.oa.common.util.StringUtils;
import com.oa.user.mapper.RoleMapper;
import com.oa.user.mapper.UserMapper;
import com.oa.user.mapper.UserRoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;

    public AuthController(UserMapper userMapper,
                          RoleMapper roleMapper,
                          UserRoleMapper userRoleMapper,
                          BCryptPasswordEncoder passwordEncoder,
                          StringRedisTemplate redisTemplate,
                          ObjectMapper objectMapper,
                          JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public AjaxResult<AuthToken> login(@RequestBody LoginRequest request) throws Exception {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return AjaxResult.error("用户名和密码不能为空");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            return AjaxResult.error("用户不存在或已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AjaxResult.error("用户名或密码错误");
        }
        LoginUser loginUser = toLoginUser(user);
        String token = JwtUtil.generate(loginUser, jwtProperties.getSecret(), jwtProperties.getExpirationSeconds());
        redisTemplate.opsForValue().set(
                TokenConstants.redisKey(token),
                objectMapper.writeValueAsString(loginUser),
                Duration.ofSeconds(jwtProperties.getExpirationSeconds())
        );
        return AjaxResult.success(new AuthToken(token, loginUser, jwtProperties.getExpirationSeconds()));
    }

    @PostMapping("/register")
    public AjaxResult<Void> register(@RequestBody RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return AjaxResult.error("用户名和密码不能为空");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            return AjaxResult.error("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(StringUtils.hasText(request.getRealName()) ? request.getRealName() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "USER"));
        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
        return AjaxResult.success();
    }

    @GetMapping("/me")
    public AjaxResult<LoginUser> me(HttpServletRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token) || !Boolean.TRUE.equals(redisTemplate.hasKey(TokenConstants.redisKey(token)))) {
            return AjaxResult.error("登录状态已失效");
        }
        try {
            return AjaxResult.success(JwtUtil.parseLoginUser(token, jwtProperties.getSecret()));
        } catch (Exception ex) {
            return AjaxResult.error("登录状态已失效");
        }
    }

    @PostMapping("/logout")
    public AjaxResult<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            redisTemplate.delete(TokenConstants.redisKey(token));
        }
        return AjaxResult.success();
    }

    private LoginUser toLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoleCodes(findRoleCodes(user.getId()));
        return loginUser;
    }

    private List<String> findRoleCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        List<String> codes = new ArrayList<>();
        for (SysUserRole userRole : userRoles) {
            SysRole role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                codes.add(role.getRoleCode());
            }
        }
        return codes;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(TokenConstants.BEARER_PREFIX)) {
            return authorization.substring(TokenConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}

