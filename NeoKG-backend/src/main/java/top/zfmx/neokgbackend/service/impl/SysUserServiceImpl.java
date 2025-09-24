package top.zfmx.neokgbackend.service.impl;


import org.mindrot.jbcrypt.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.SysUserMapper;
import top.zfmx.neokgbackend.pojo.dto.LoginDTO;
import top.zfmx.neokgbackend.pojo.dto.RegisterDTO;
import top.zfmx.neokgbackend.pojo.entity.SysUser;
import top.zfmx.neokgbackend.service.SysUserService;

import java.time.LocalDateTime;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;


    @Override
    public Long login(LoginDTO loginDTO) throws AuthException {
        String password = loginDTO.getPassword();
        String username = loginDTO.getUsername();

        if (StringUtils.isAnyBlank(username, password)) {
            throw new AuthException("用户名和密码不能为空");
        }

        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .oneOpt()
                .orElseThrow(() -> new AuthException("用户名或密码错误"));

        // 使用 BCrypt 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        if (!user.getIsEnable()) {
            throw new AuthException("账户已被禁用，请联系管理员");
        }
        return user.getId();
    }

    @Override
    public Long register(RegisterDTO dto) {
        SysUser exist = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .one();
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }

        // jBCrypt 加密
        String encodedPwd = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

        SysUser user = SysUser.builder()
                .username(dto.getUsername())
                .password(encodedPwd)
                .isEnable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sysUserMapper.insert(user);
        return user.getId();
    }

}
