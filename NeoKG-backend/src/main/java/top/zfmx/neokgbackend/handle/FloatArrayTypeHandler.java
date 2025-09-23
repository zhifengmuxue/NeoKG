package top.zfmx.neokgbackend.handle;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * @author li ma
 * @version 0.0.1
 **/
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(float[].class)
public class FloatArrayTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
            throws SQLException {
        // pgvector 的存储格式是类似于 {0.1, 0.2, 0.3}
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int j = 0; j < parameter.length; j++) {
            if (j > 0) sb.append(',');
            sb.append(parameter[j]);
        }
        sb.append(']');
        ps.setObject(i, sb.toString(), Types.OTHER);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String val = rs.getString(columnName);
        return parseVector(val);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return parseVector(val);
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String val = cs.getString(columnIndex);
        return parseVector(val);
    }

    private float[] parseVector(String val) {
        if (val == null) return null;
        val = val.replaceAll("[\\[\\]]", ""); // 去掉 []
        String[] parts = val.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}

