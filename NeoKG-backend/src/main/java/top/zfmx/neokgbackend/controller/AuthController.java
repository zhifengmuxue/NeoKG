package top.zfmx.neokgbackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.pojo.dto.LoginDTO;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.SysUserService;

/**
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Tag(name = "鉴权接口组")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "登录接口")
    public Result<String> doLogin(@Validated @RequestBody LoginDTO loginDTO) throws AuthException {
        log.info("用户登录：{}, 是否记录:{}",loginDTO.getUsername(),loginDTO.getRememberMe());
        Long userId = sysUserService.login(loginDTO);
        if (userId != null) {
            StpUtil.login(userId, loginDTO.getRememberMe());
        }
        return Result.ok("登录成功",StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出接口")
    public Result<String> doLogout(){
        log.info("是否登录：{}", StpUtil.isLogin());
        StpUtil.logout();
        return Result.ok("注销成功");
    }
}
