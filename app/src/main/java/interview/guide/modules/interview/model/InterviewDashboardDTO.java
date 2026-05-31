package interview.guide.modules.interview.model;

import java.util.List;

/**
 * 面试看板数据 DTO
 */
public record InterviewDashboardDTO(
    List<TrendPoint> interviewCountTrend,
    List<SkillScore> skillScores,
    List<TrendPoint> scoreTrend,
    List<WeakCategory> weakCategories
) {

    /**
     * 趋势数据点（面试次数趋势 / 分数趋势共用）
     */
    public record TrendPoint(String label, int value) {}

    /**
     * 技能维度平均分（用于雷达图）
     */
    public record SkillScore(
        String skillId,
        String skillName,
        int avgScore,
        int interviewCount,
        int fullMark
    ) {}

    /**
     * 薄弱知识点
     */
    public record WeakCategory(
        String category,
        int avgScore,
        int questionCount,
        int interviewCount
    ) {}
}
