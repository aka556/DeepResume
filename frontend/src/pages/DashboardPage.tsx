import { useCallback, useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { BarChart3, Loader2, TrendingUp } from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, LineChart, Line
} from 'recharts';
import RadarChart from '../components/RadarChart';
import { dashboardApi, type InterviewDashboardData, type TrendPeriod } from '../api/dashboard';
import { getScoreProgressColor, getScoreTextColor } from '../utils/score';

export default function DashboardPage() {
  const [data, setData] = useState<InterviewDashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<TrendPeriod>('week');

  const isDark = typeof window !== 'undefined' && document.documentElement.classList.contains('dark');
  const gridColor = isDark ? '#334155' : '#e2e8f0';
  const tickColor = isDark ? '#94a3b8' : '#64748b';
  const tooltipBg = isDark ? '#1e293b' : '#fff';
  const tooltipBorder = isDark ? '#334155' : '#e2e8f0';

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await dashboardApi.getDashboard(period);
      setData(result);
    } catch (err) {
      console.error('加载看板数据失败', err);
    } finally {
      setLoading(false);
    }
  }, [period]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <Loader2 className="w-8 h-8 text-primary-500 animate-spin" />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="text-center">
          <BarChart3 className="w-12 h-12 text-slate-300 dark:text-slate-600 mx-auto mb-4" />
          <p className="text-slate-500 dark:text-slate-400">加载看板数据失败</p>
        </div>
      </div>
    );
  }

  const hasData = data.interviewCountTrend.length > 0
    || data.skillScores.length > 0
    || data.scoreTrend.length > 0;

  const radarData = data.skillScores.map(s => ({
    subject: s.skillName,
    score: s.avgScore,
    fullMark: s.fullMark,
  }));

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* 页头 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary-100 dark:bg-primary-900/30 rounded-xl flex items-center justify-center">
            <BarChart3 className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white">面试看板</h1>
            <p className="text-sm text-slate-500 dark:text-slate-400">数据分析与趋势追踪</p>
          </div>
        </div>

        {/* 周期切换 */}
        <div className="flex bg-slate-100 dark:bg-slate-800 rounded-xl p-1">
          {(['week', 'month'] as TrendPeriod[]).map(p => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`px-4 py-2 text-sm font-medium rounded-lg transition-all ${
                period === p
                  ? 'bg-white dark:bg-slate-700 text-primary-600 dark:text-primary-400 shadow-sm'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-300'
              }`}
            >
              {p === 'week' ? '按周' : '按月'}
            </button>
          ))}
        </div>
      </div>

      {!hasData ? (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col items-center justify-center py-20"
        >
          <TrendingUp className="w-16 h-16 text-slate-200 dark:text-slate-700 mb-4" />
          <p className="text-lg text-slate-500 dark:text-slate-400 mb-2">暂无面试数据</p>
          <p className="text-sm text-slate-400 dark:text-slate-500">完成面试后即可查看统计</p>
        </motion.div>
      ) : (
        <>
          {/* 面试次数趋势 */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-100 dark:border-slate-700"
          >
            <h2 className="text-lg font-semibold text-slate-800 dark:text-white mb-4">面试次数趋势</h2>
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={data.interviewCountTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
                <XAxis dataKey="label" tick={{ fill: tickColor, fontSize: 12 }} />
                <YAxis tick={{ fill: tickColor, fontSize: 12 }} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: tooltipBg,
                    border: `1px solid ${tooltipBorder}`,
                    borderRadius: '12px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="value"
                  stroke="#6366f1"
                  fill="#6366f1"
                  fillOpacity={0.3}
                  name="面试次数"
                />
              </AreaChart>
            </ResponsiveContainer>
          </motion.div>

          {/* 双列：雷达图 + 分数趋势 */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* 技能雷达图 */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-100 dark:border-slate-700"
            >
              <h2 className="text-lg font-semibold text-slate-800 dark:text-white mb-4">技能雷达图</h2>
              {radarData.length > 0 ? (
                <RadarChart data={radarData} height={320} />
              ) : (
                <div className="flex items-center justify-center h-[320px] text-slate-400">
                  暂无技能数据
                </div>
              )}
            </motion.div>

            {/* 分数趋势 */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              className="bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-100 dark:border-slate-700"
            >
              <h2 className="text-lg font-semibold text-slate-800 dark:text-white mb-4">分数趋势</h2>
              {data.scoreTrend.length > 0 ? (
                <ResponsiveContainer width="100%" height={320}>
                  <LineChart data={data.scoreTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
                    <XAxis dataKey="label" tick={{ fill: tickColor, fontSize: 12 }} />
                    <YAxis tick={{ fill: tickColor, fontSize: 12 }} domain={[0, 100]} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: tooltipBg,
                        border: `1px solid ${tooltipBorder}`,
                        borderRadius: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                      }}
                      formatter={(value: number) => [`${value} 分`, '平均分']}
                    />
                    <Line
                      type="monotone"
                      dataKey="value"
                      stroke="#6366f1"
                      strokeWidth={2}
                      dot={{ fill: '#6366f1', r: 4 }}
                      activeDot={{ r: 6 }}
                      name="平均分"
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center h-[320px] text-slate-400">
                  暂无分数数据
                </div>
              )}
            </motion.div>
          </div>

          {/* 薄弱知识点 */}
          {data.weakCategories.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-100 dark:border-slate-700"
            >
              <h2 className="text-lg font-semibold text-slate-800 dark:text-white mb-4">
                薄弱知识点 Top {data.weakCategories.length}
              </h2>
              <div className="space-y-3">
                {data.weakCategories.map((item, index) => (
                  <div
                    key={item.category}
                    className="flex items-center gap-4 p-4 rounded-xl bg-slate-50 dark:bg-slate-700/50"
                  >
                    <span className="text-lg font-bold text-slate-400 dark:text-slate-500 w-6 text-right">
                      {index + 1}
                    </span>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-medium text-slate-800 dark:text-white truncate">
                          {item.category}
                        </span>
                        <span className={`font-bold ${getScoreTextColor(item.avgScore)}`}>
                          {item.avgScore}分
                        </span>
                      </div>
                      <div className="w-full h-2 bg-slate-200 dark:bg-slate-600 rounded-full overflow-hidden">
                        <motion.div
                          className={`h-full ${getScoreProgressColor(item.avgScore)} rounded-full`}
                          initial={{ width: 0 }}
                          animate={{ width: `${item.avgScore}%` }}
                          transition={{ duration: 0.8, delay: index * 0.05 }}
                        />
                      </div>
                      <span className="text-xs text-slate-400 dark:text-slate-500 mt-1 block">
                        {item.questionCount} 题 · {item.interviewCount} 次面试
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </motion.div>
          )}
        </>
      )}
    </div>
  );
}
