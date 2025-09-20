package top.zfmx.neokgbackend.utils;

import top.zfmx.neokgbackend.model.Keyword;

import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
public class KeywordMatcher {
    /**
     * 基于字符匹配查找
     */
    public static Keyword findByName(List<Keyword> allKeywords, Keyword kw) {
        if (kw == null || kw.getName() == null) return null;
        return allKeywords.stream()
                .filter(dbKw -> dbKw.getName() != null && dbKw.getName().equalsIgnoreCase(kw.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 基于 embedding 相似度查找
     */
    public static Keyword findMostSimilar(List<Keyword> allKeywords,
                                          Keyword kw,
                                          Vector<Float> kwVec,
                                          double threshold) {
        if (kwVec == null || allKeywords == null || allKeywords.isEmpty()) return null;

        double maxSim = 0.0;
        Keyword mostSimilar = null;

        for (Keyword dbKw : allKeywords) {
            if (dbKw.getVec() == null) continue;
            double sim = cosineSimilarity(dbKw.getVec(), kwVec);
            if (sim > threshold && sim > maxSim) {
                maxSim = sim;
                mostSimilar = dbKw;
            }
        }
        return mostSimilar;
    }

    /**
     * 计算余弦相似度
     */
    public static double cosineSimilarity(Vector<Float> vec1, Vector<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) return 0.0;

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vec1.size(); i++) {
            double a = vec1.get(i);
            double b = vec2.get(i);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}
