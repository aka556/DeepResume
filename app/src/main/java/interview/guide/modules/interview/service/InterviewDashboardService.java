package interview.guide.modules.interview.service;

import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.InterviewDashboardDTO;
import interview.guide.modules.interview.model.InterviewDashboardDTO.SkillScore;
import interview.guide.modules.interview.model.InterviewDashboardDTO.TrendPoint;
import interview.guide.modules.interview.model.InterviewDashboardDTO.WeakCategory;
import interview.guide.modules.interview.model.InterviewSessionEntity;
import interview.guide.modules.interview.model.InterviewSessionEntity.SessionStatus;
import interview.guide.modules.interview.repository.InterviewAnswerRepository;
import interview.guide.modules.interview.repository.InterviewSessionRepository;
import interview.guide.modules.interview.skill.InterviewSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 面试看板数据聚合服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewDashboardService {

    private static final List<SessionStatus> EVALUATED_STATUSES =
        List.of(SessionStatus.COMPLETED, SessionStatus.EVALUATED);

    private static final int MAX_WEAK_CATEGORIES = 10;

    private final InterviewSessionRepository sessionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final InterviewSkillService skillService;

    /**
     * 获取看板数据
     *
     * @param period "week" 或 "month"
     */
    public InterviewDashboardDTO getDashboardData(String period) {
        boolean isMonthly = "month".equalsIgnoreCase(period);

        List<InterviewSessionEntity> sessions =
            sessionRepository.findByStatusInAndOverallScoreIsNotNullOrderByCreatedAtAsc(EVALUATED_STATUSES);

        List<TrendPoint> countTrend = buildCountTrend(sessions, isMonthly);
        List<SkillScore> skillScores = buildSkillScores(sessions);
        List<TrendPoint> scoreTrend = buildScoreTrend(sessions, isMonthly);
        List<WeakCategory> weakCategories = buildWeakCategories();

        return new InterviewDashboardDTO(countTrend, skillScores, scoreTrend, weakCategories);
    }

    /**
     * 面试次数趋势：按周/月分组计数
     */
    private List<TrendPoint> buildCountTrend(List<InterviewSessionEntity> sessions, boolean isMonthly) {
        Map<String, Long> grouped = sessions.stream()
            .collect(Collectors.groupingBy(
                s -> bucketKey(s.getCreatedAt().toLocalDate(), isMonthly),
                LinkedHashMap::new,
                Collectors.counting()
            ));

        return grouped.entrySet().stream()
            .map(e -> new TrendPoint(e.getKey(), e.getValue().intValue()))
            .toList();
    }

    /**
     * 技能雷达图：按 skillId 分组求平均分
     */
    private List<SkillScore> buildSkillScores(List<InterviewSessionEntity> sessions) {
        Map<String, String> skillNameMap = skillService.getAllSkills().stream()
            .collect(Collectors.toMap(
                InterviewSkillService.SkillDTO::id,
                InterviewSkillService.SkillDTO::name,
                (a, b) -> a
            ));

        Map<String, List<InterviewSessionEntity>> grouped = sessions.stream()
            .collect(Collectors.groupingBy(InterviewSessionEntity::getSkillId));

        return grouped.entrySet().stream()
            .map(entry -> {
                String skillId = entry.getKey();
                List<InterviewSessionEntity> group = entry.getValue();
                int avgScore = (int) Math.round(
                    group.stream()
                        .mapToInt(InterviewSessionEntity::getOverallScore)
                        .average()
                        .orElse(0)
                );
                String skillName = skillNameMap.getOrDefault(skillId, skillId);
                return new SkillScore(skillId, skillName, avgScore, group.size(), 100);
            })
            .sorted(Comparator.comparing(SkillScore::skillName))
            .toList();
    }

    /**
     * 分数趋势：按周/月分组求平均分
     */
    private List<TrendPoint> buildScoreTrend(List<InterviewSessionEntity> sessions, boolean isMonthly) {
        Map<String, List<InterviewSessionEntity>> grouped = sessions.stream()
            .collect(Collectors.groupingBy(
                s -> bucketKey(s.getCreatedAt().toLocalDate(), isMonthly),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        return grouped.entrySet().stream()
            .map(entry -> {
                int avgScore = (int) Math.round(
                    entry.getValue().stream()
                        .mapToInt(InterviewSessionEntity::getOverallScore)
                        .average()
                        .orElse(0)
                );
                return new TrendPoint(entry.getKey(), avgScore);
            })
            .toList();
    }

    /**
     * 薄弱知识点：按答案 category 分组求均分，取最低 Top N
     */
    private List<WeakCategory> buildWeakCategories() {
        List<InterviewAnswerEntity> answers = answerRepository.findAllWithScore();

        Map<String, List<InterviewAnswerEntity>> grouped = answers.stream()
            .filter(a -> a.getCategory() != null && !a.getCategory().isBlank())
            .collect(Collectors.groupingBy(InterviewAnswerEntity::getCategory));

        return grouped.entrySet().stream()
            .map(entry -> {
                String category = entry.getKey();
                List<InterviewAnswerEntity> group = entry.getValue();
                int avgScore = (int) Math.round(
                    group.stream().mapToInt(InterviewAnswerEntity::getScore).average().orElse(0)
                );
                int questionCount = group.size();
                long interviewCount = group.stream()
                    .map(a -> a.getSession().getId())
                    .distinct()
                    .count();
                return new WeakCategory(category, avgScore, questionCount, (int) interviewCount);
            })
            .sorted(Comparator.comparingInt(WeakCategory::avgScore))
            .limit(MAX_WEAK_CATEGORIES)
            .toList();
    }

    /**
     * 按周或月生成分组 key
     */
    private String bucketKey(LocalDate date, boolean isMonthly) {
        if (isMonthly) {
            return YearMonth.from(date).toString();
        }
        WeekFields weekFields = WeekFields.ISO;
        int weekYear = date.get(weekFields.weekBasedYear());
        int weekNum = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", weekYear, weekNum);
    }
}
