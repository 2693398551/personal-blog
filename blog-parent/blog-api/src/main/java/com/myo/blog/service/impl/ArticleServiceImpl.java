package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.controller.UploadController;
import com.myo.blog.dao.dos.Archives;
import com.myo.blog.dao.mapper.ArticleBodyMapper;
import com.myo.blog.dao.mapper.ArticleMapper;
import com.myo.blog.dao.mapper.ArticleTagMapper;
import com.myo.blog.dao.pojo.Article;
import com.myo.blog.dao.pojo.ArticleBody;
import com.myo.blog.dao.pojo.ArticleTag;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.service.*;

import com.myo.blog.entity.ArticleBodyVo;
import com.myo.blog.entity.ArticleVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.TagVo;
import com.myo.blog.entity.params.ArticleParam;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.utils.*;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private QiniuUtils qiniuUtils;
    //查询文章列表
    @Override
    public Result listArticle(PageParams pageParams,String token) {
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());

        boolean isToken=!"undefined".equals(token);
        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth(),
                isToken
        );

        List<Article> records = articleIPage.getRecords();



        return Result.success(copyList(records,true,true,false,false,isToken));
    }
    //查询一个多少文章数量
    public Result listArticleCount(String token){
        boolean isToken=!"undefined".equals(token);
        Integer count=articleMapper.listArticleCount(isToken);
        return Result.success(count);
    };
    /*查询mac*/
    @Override
    public Result queryMAC() {
        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip=IpUtils.getIpAddr(request);
        //String mac=LocalMACUtil.getLocalMac();//
        String addr=IpUtils.getCityInfo(ip);

        return Result.success(addr);
    }

//    @Override
//    public Result listArticle(PageParams pageParams) {
//        /**
//         * 1. 分页查询 article数据库表
//         */
//        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        if (pageParams.getCategoryId() != null){
//            // and category_id=#{categoryId}
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if (pageParams.getTagId() != null){
//            //加入标签 条件查询
//            //article表中 并没有tag字段 一篇文章 有多个标签
//            //article_tag  article_id 1 : n tag_id
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if (articleIdList.size() > 0){
//                // and id in(1,2,3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//        //是否置顶进行排序
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //能直接返回吗？ 很明显不能
//        List<ArticleVo> articleVoList = copyList(records,true,true);
//        return Result.success(articleVoList);
//    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 1. 根据id查询 文章信息
         * 2. 根据bodyId和categoryid 去做关联查询
         */



        Article article = this.articleMapper.selectById(articleId);

        ArticleVo articleVo = copy(article, true, true,true,true);
        //查看完文章了，新增阅读数，有没有问题呢？
        //查看完文章之后，本应该直接返回数据了，这时候做了一个更新操作，更新时加写锁，阻塞其他的读操作，性能就会比较低
        // 更新 增加了此次接口的 耗时 如果一旦更新出问题，不能影响 查看文章的操作
        //线程池  可以把更新操作 扔到线程池中去执行，和主线程就不相关了
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        //此接口 要加入到登录拦截当中
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 1. 发布文章 目的 构建Article对象
         * 2. 作者id  当前的登录用户
         * 3. 标签  要将标签加入到 关联列表当中
         * 4. body 内容存储 article bodyId
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));


        //获取最新文章的id   articlesId
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Article::getId);
        queryWrapper.last("limit 1");
        queryWrapper.orderByDesc(Article::getCreateDate);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        int id=0;
        if(articles.size()!=0){
            id=Integer.parseInt(String.valueOf(articles.get(0).getId()));
        }

        //封面cover
        String cover=articleParam.getCover();


        article.setCover(cover);
        //获取最新文章的位置id然后加169

        int articlesId=id+1;
        //将文章id添加
        article.setId(Long.parseLong(String.valueOf(articlesId)));
        //没有article.setId的活会自动添加
        this.articleMapper.insert(article);

        //tag
        List<TagVo> tags = articleParam.getTags();
        if (tags != null){
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody  = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        //

        //获取最新文章的body中id   articlesBodyId
        LambdaQueryWrapper<ArticleBody> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.select(ArticleBody::getId);
        queryWrapper2.last("limit 1");
        queryWrapper2.orderByDesc(ArticleBody::getId);
        List<ArticleBody> articleBodyList = articleBodyMapper.selectList(queryWrapper2);
        long id2=0;
        if(articleBodyList.size()!=0){
            id2=articleBodyList.get(0).getId();
        }
        int articlesBodyId=Integer.parseInt(String.valueOf(id2))+1;
        //将id添加到获取最新文章的body中id
        articleBody.setId( Long.parseLong(String.valueOf(articlesBodyId)));
        //没有articleBody.setId的活会自动添加
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String,String> map = new HashMap<>();
        map.put("id",article.getId().toString());
        return Result.success(map);
    }


    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory,boolean isToken) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            System.out.println("record:"+record);
            System.out.println("是否登录:"+isToken);
            /*if(record.getViewKeys()==1&&isToken==false){
                int titleLength=record.getTitle().length();
                int summaryLength=record.getSummary().length();
                String title=ArticleUtils.keys(titleLength);
                String summary=ArticleUtils.keys(summaryLength);
                //给标题马赛克■
                record.setTitle(title);
                //给文章简介马赛克■
                record.setSummary(summary);
                //
                record.setCategoryId(Long.parseLong(0+""));
            }*/
            System.out.println("record2:"+record);
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));

        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory){

        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));

        BeanUtils.copyProperties(article,articleVo);
        articleVo.setViewKeys(String.valueOf(article.getViewKeys()));
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        //并不是所有的接口 都需要标签 ，作者信息
        if (isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if (isBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if (isCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }


        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }

}
