package top.zfmx.neokgbackend.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录表单
 *
 * @author lima
 * @version 0.0.1
 **/
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private Boolean rememberMe;
}
