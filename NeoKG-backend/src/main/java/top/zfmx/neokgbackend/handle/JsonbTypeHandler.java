package top.zfmx.neokgbackend.handle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setObject(i, mapper.writeValueAsString(parameter), Types.OTHER); // PostgreSQL jsonb
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json == null) return null;
        try {
            return mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json == null) return null;
        try {
            return mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json == null) return null;
        try {
            return mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Map", e);
        }
    }
}

