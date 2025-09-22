package top.zfmx.neokgbackend.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "SystemUser", description = "用户类")
public class SysUser{
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Boolean isEnable;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}