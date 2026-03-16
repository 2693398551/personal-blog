package com.myo.blog.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.*;
import com.myo.blog.dao.pojo.*;
import com.myo.blog.entity.DashboardVo;
import com.myo.blog.entity.Result;
import com.myo.blog.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;
    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_KEY    = "dashboard:data";

    private static final long   CACHE_MINUTES = 60;

    @Override
    public Result getDashboardData() {
        // 1. 先查 Redis
        try {
            String cached = stringRedisTemplate.opsForValue().get(CACHE_KEY);
            if (StringUtils.isNotBlank(cached)) {
                log.debug("[仪表盘] 命中缓存，直接返回");
                return Result.success(JSON.parseObject(cached, DashboardVo.class));
            }
        } catch (Exception e) {
            log.warn("[仪表盘] Redis 读取失败，降级查数据库: {}", e.getMessage());
        }

        // 2. 缓存未命中，查数据库聚合
        DashboardVo vo = buildDashboardVo();

        // 3. 写入 Redis，60 分钟过期
        try {
            stringRedisTemplate.opsForValue().set(
                    CACHE_KEY, JSON.toJSONString(vo), CACHE_MINUTES, TimeUnit.MINUTES);
            log.debug("[仪表盘] 数据已写入缓存，过期时间 {} 分钟", CACHE_MINUTES);
        } catch (Exception e) {
            log.warn("[仪表盘] Redis 写入失败: {}", e.getMessage());
        }

        return Result.success(vo);
    }

    /**
     * 主动刷新缓存（删除 Key，下次请求时重新聚合）
     * 供发布文章、新增/删除评论等操作后调用
     */
    @Override
    public void refreshCache() {
        try {
            stringRedisTemplate.delete(CACHE_KEY);
            log.info("[仪表盘] 缓存已主动清除");
        } catch (Exception e) {
            log.warn("[仪表盘] 缓存清除失败: {}", e.getMessage());
        }
    }

    // ===== 数据聚合入口 =====

    private DashboardVo buildDashboardVo() {
        DashboardVo vo = new DashboardVo();

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long monthStart = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 顶部卡片
        vo.setArticleCount(articleMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1)));
        vo.setUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)));
        vo.setTotalViewCount(calcTotalViewCount());

        // 本月新增
        vo.setMonthArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().ge(Article::getCreateDate, monthStart)));
        vo.setMonthCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getStatus, 1)
                        .ge(Comment::getCreateDate, monthStart)));
        vo.setMonthUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .ge(SysUser::getCreateDate, monthStart)));

        // 图表 + 列表
        vo.setTrendData(buildTrendData());
        vo.setHotArticles(buildHotArticles());
        vo.setCategoryStats(buildCategoryStats());
        vo.setRecentComments(buildRecentComments());

        return vo;
    }

    // ===== 各模块私有方法（逻辑不变） =====

    private long calcTotalViewCount() {
        try {
            Map<Object, Object> viewMap = stringRedisTemplate.opsForHash()
                    .entries("blog:article:viewCount");
            if (!viewMap.isEmpty()) {
                return viewMap.values().stream()
                        .mapToLong(v -> Long.parseLong(v.toString()))
                        .sum();
            }
        } catch (Exception e) {
            log.warn("[仪表盘] Redis浏览量读取失败，降级查数据库: {}", e.getMessage());
        }
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().select(Article::getViewCounts));
        return articles.stream()
                .mapToLong(a -> a.getViewCounts() == null ? 0L : a.getViewCounts())
                .sum();
    }

    private List<DashboardVo.DayStats> buildTrendData() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        ZoneId zone = ZoneId.systemDefault();

        Map<String, DashboardVo.DayStats> buckets = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.format(fmt);
            DashboardVo.DayStats s = new DashboardVo.DayStats();
            s.setDate(label);
            s.setViews(0L);
            s.setComments(0L);
            buckets.put(label, s);
        }

        long start30 = today.minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli();

        List<Comment> recentComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getStatus, 1)
                        .ge(Comment::getCreateDate, start30)
                        .select(Comment::getCreateDate));
        for (Comment c : recentComments) {
            String label = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(c.getCreateDate()), zone).format(fmt);
            if (buckets.containsKey(label)) {
                buckets.get(label).setComments(buckets.get(label).getComments() + 1);
            }
        }

        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .ge(Article::getCreateDate, start30)
                        .select(Article::getCreateDate, Article::getViewCounts));
        for (Article a : articles) {
            if (a.getCreateDate() == null) continue;
            String label = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(a.getCreateDate()), zone).format(fmt);
            if (buckets.containsKey(label)) {
                long v = a.getViewCounts() == null ? 0L : a.getViewCounts();
                buckets.get(label).setViews(buckets.get(label).getViews() + v);
            }
        }

        return new ArrayList<>(buckets.values());
    }

    private List<DashboardVo.HotArticle> buildHotArticles() {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getViewCounts)
                .orderByDesc(Article::getViewCounts)
                .last("LIMIT 5");
        return articleMapper.selectList(qw).stream().map(a -> {
            DashboardVo.HotArticle ha = new DashboardVo.HotArticle();
            ha.setId(a.getId());
            ha.setTitle(a.getTitle());
            ha.setViewCounts(a.getViewCounts() == null ? 0 : a.getViewCounts());
            return ha;
        }).collect(Collectors.toList());
    }

    private List<DashboardVo.CategoryStat> buildCategoryStats() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().select(Article::getCategoryId));

        Map<String, Long> countMap = articles.stream()
                .filter(a -> a.getCategoryId() != null)
                .collect(Collectors.groupingBy(Article::getCategoryId, Collectors.counting()));

        if (countMap.isEmpty()) return Collections.emptyList();

        List<Category> categories = categoryMapper.selectBatchIds(countMap.keySet());
        Map<String, String> nameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        return countMap.entrySet().stream()
                .map(e -> {
                    DashboardVo.CategoryStat cs = new DashboardVo.CategoryStat();
                    cs.setCategoryName(nameMap.getOrDefault(e.getKey(), "未知分类"));
                    cs.setCount(e.getValue());
                    return cs;
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private List<DashboardVo.RecentComment> buildRecentComments() {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateDate)
                .last("LIMIT 5");
        List<Comment> comments = commentMapper.selectList(qw);
        if (comments.isEmpty()) return Collections.emptyList();

        Set<String> authorIds = comments.stream()
                .map(Comment::getAuthorId).collect(Collectors.toSet());
        List<SysUser> users = sysUserMapper.selectBatchIds(authorIds);
        Map<String, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        return comments.stream().map(c -> {
            DashboardVo.RecentComment rc = new DashboardVo.RecentComment();
            rc.setId(c.getId());
            rc.setContent(c.getContent());
            rc.setCreateDate(c.getCreateDate());
            SysUser u = userMap.get(c.getAuthorId());
            rc.setAuthorNickname(u != null ? u.getNickname() : "匿名");
            rc.setAuthorAvatar(u != null ? u.getAvatar() : null);
            return rc;
        }).collect(Collectors.toList());
    }
}