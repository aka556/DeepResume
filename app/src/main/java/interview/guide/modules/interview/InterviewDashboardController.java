package interview.guide.modules.interview;

import interview.guide.common.result.Result;
import interview.guide.modules.interview.model.InterviewDashboardDTO;
import interview.guide.modules.interview.service.InterviewDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面试看板控制器
 * 提供面试数据统计与可视化接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "面试看板", description = "面试数据统计与可视化")
public class InterviewDashboardController {

    private final InterviewDashboardService dashboardService;

    /**
     * 获取面试看板数据
     */
    @GetMapping("/api/interview/dashboard")
    @Operation(summary = "获取面试看板数据", description = "返回面试次数趋势、技能雷达图、分数趋势、薄弱知识点")
    public Result<InterviewDashboardDTO> getDashboard(
            @RequestParam(defaultValue = "week") String period) {
        log.info("获取面试看板数据: period={}", period);
        return Result.success(dashboardService.getDashboardData(period));
    }
}
