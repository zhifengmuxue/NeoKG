package top.zfmx.neokgbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.security.auth.message.AuthException;
import top.zfmx.neokgbackend.pojo.dto.LoginDTO;
import top.zfmx.neokgbackend.pojo.dto.RegisterDTO;
import top.zfmx.neokgbackend.pojo.entity.SysUser;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface SysUserService extends IService<SysUser> {
    Long login(LoginDTO loginDTO) throws AuthException;

    Long register(RegisterDTO registerDTO);
}
