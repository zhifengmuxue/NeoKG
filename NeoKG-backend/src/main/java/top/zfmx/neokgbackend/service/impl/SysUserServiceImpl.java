package top.zfmx.neokgbackend.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.security.auth.message.AuthException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.SysUserMapper;
import top.zfmx.neokgbackend.pojo.dto.LoginDTO;
import top.zfmx.neokgbackend.pojo.entity.SysUser;
import top.zfmx.neokgbackend.service.SysUserService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Long login(LoginDTO loginDTO) throws AuthException {
        String password = loginDTO.getPassword();
        String username = loginDTO.getUsername();

        // 二次校验
        if (StringUtils.isAnyBlank(username, password)) {
            throw new AuthException("用户名和密码不能为空");
        }

        // 查询用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .oneOpt()
                .orElseThrow(() -> new AuthException("用户名或密码错误"));

        // 加密
        if (!SaSecureUtil.md5(password).equals(user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        // 检查账户状态
        if (!user.getIsEnable()) {
            throw new AuthException("账户已被禁用，请联系管理员");
        }
        return user.getId();
    }
}
