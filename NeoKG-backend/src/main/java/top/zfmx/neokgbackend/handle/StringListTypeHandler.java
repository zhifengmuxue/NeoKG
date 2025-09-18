package top.zfmx.neokgbackend.handle;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lima
 * @version 0.0.1
 **/
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.ARRAY);
        } else {
            // 转换成 PostgreSQL TEXT[] 类型
            Array array = ps.getConnection().createArrayOf("text", parameter.toArray());
            ps.setArray(i, array);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);
        if (array == null) return null;
        return Arrays.stream((Object[]) array.getArray())
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Array array = rs.getArray(columnIndex);
        if (array == null) return null;
        return Arrays.stream((Object[]) array.getArray())
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Array array = cs.getArray(columnIndex);
        if (array == null) return null;
        return Arrays.stream((Object[]) array.getArray())
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
