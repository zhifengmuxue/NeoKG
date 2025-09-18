package top.zfmx.neokgbackend.handle;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
public class VectorTypeHandler extends BaseTypeHandler<Vector<Float>> {

    /**
     * 设置非空参数
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter 参数
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Vector<Float> parameter, JdbcType jdbcType) throws SQLException {
        StringBuilder sb = new StringBuilder("[");
        for (int j = 0; j < parameter.size(); j++) {
            sb.append(parameter.get(j));
            if (j < parameter.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        ps.setObject(i, sb.toString(), Types.OTHER);
    }

    /**
     * 获取结果
     * @param rs 结果集
     * @param columnName 列名
     * @return 向量
     * @throws SQLException SQL异常
     */
    @Override
    public Vector<Float> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    /**
     * 获取结果
     * @param rs 结果集
     * @param columnIndex 列索引
     * @return 向量
     * @throws SQLException SQL异常
     */
    @Override
    public Vector<Float> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    /**
     * 获取结果
     * @param cs CallableStatement
     * @param columnIndex 列索引
     * @return 向量
     * @throws SQLException SQL异常
     */
    @Override
    public Vector<Float> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    /**
     * 解析向量字符串
     * @param value 向量字符串
     * @return 向量
     */
    private Vector<Float> parseVector(String value) {
        if (value == null) {
            return null;
        }
        // 去掉大括号和方括号
        value = value.replaceAll("[\\[\\]\\{\\}]", "");
        String[] parts = value.split(",");
        Vector<Float> vector = new Vector<>();
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                vector.add(Float.parseFloat(part.trim()));
            }
        }
        return vector;
    }

}
