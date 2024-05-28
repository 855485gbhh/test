package com.example.springbootredis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.example.springbootredis.dao.FollowMapper;
import com.example.springbootredis.dao.UserMapper;
import com.example.springbootredis.pojo.dto.BlogDto;
import com.example.springbootredis.pojo.dto.UserDto;
import com.example.springbootredis.pojo.po.Blog;
import com.example.springbootredis.pojo.po.User;
import com.example.springbootredis.pojo.po.ValueScore;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.BlogVo;
import com.example.springbootredis.pojo.vo.ScrollBlogVo;
import com.example.springbootredis.pojo.vo.UserVo;
import com.example.springbootredis.service.UserService;
import com.example.springbootredis.utils.ResponseUtils;
import com.example.springbootredis.utils.UserHolder;
import com.github.yitter.idgen.YitIdHelper;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.example.springbootredis.dao.BlogMapper;
import com.example.springbootredis.service.BlogService;
import com.mybatisflex.core.row.Db;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.springbootredis.constant.RedisConstant.*;
import static com.example.springbootredis.pojo.po.table.BlogTableDef.BLOG;
import static com.example.springbootredis.pojo.po.table.FollowTableDef.FOLLOW;
import static com.example.springbootredis.pojo.po.table.UserTableDef.USER;

/**
 * (Blog)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class BlogServiceImpl implements BlogService {
    @Resource
    private BlogMapper blogMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FollowMapper followMapper;

    @Override
    public BlogVo getById(Serializable id) {
//        QueryWrapper queryWrapper = QueryWrapper.create().select().from(BLOG).where(BLOG.ID.eq(id));
//        List<BlogVo> blogVos = blogMapper.selectListByQueryAs(queryWrapper,
//                BlogVo.class
//                , c -> c.field(BlogVo::getUserVo).prevent().queryWrapper(blogVo -> QueryWrapper.create().select().from(USER)
//                        .where(USER.ID.eq(blogVo.getUserId()))));
//
//        if (blogVos == null || blogVos.size() == 0) {
//            return null;
//        }
//        return blogVos.get(0);
        QueryWrapper queryWrapper = QueryWrapper.create().select().from(BLOG).where(BLOG.ID.eq(id));
        BlogVo blogVo = blogMapper.selectOneByQueryAs(queryWrapper, BlogVo.class);
        User user = QueryChain.of(userMapper).select().from(USER).where(USER.ID.eq(blogVo.getUserId())).one();
        blogVo.setIcon(user.getIcon());
        blogVo.setNickName(user.getNickName());
        return blogVo;
    }

    @Override
    public JsonResponse<Object> queryById(Serializable id) {
        Long userId = UserHolder.getUser().getId();
        log.info("尝试从redis读取数据");
        Map entries = redisTemplate.opsForHash().entries(REDIS_CACHE_BLOG + id);
        BlogVo blogVo = BeanUtil.fillBeanWithMap(entries, new BlogVo(), false);
        Double score = redisTemplate.opsForZSet().score(REDIS_CACHE_BLOG_LIKE + id, userId);
        boolean isMember = false;
        if (score != null) {
            isMember = true;
        }

        if (blogVo != null && blogVo.getId() != null) {
            log.info("从redis读取到数据：{}", blogVo.getId());
            blogVo.setIsLike(Boolean.TRUE.equals(isMember));
            return ResponseUtils.success(blogVo);
        }
        blogVo = getById(id);
        log.info("从mysql读取到数据：{}", blogVo.getId());
        if (blogVo == null) {
            redisTemplate.opsForHash().putAll(REDIS_CACHE_BLOG + id, null);
            redisTemplate.expire(REDIS_CACHE_BLOG + id, 1, TimeUnit.MINUTES);
            return ResponseUtils.error(1, "该笔记不存在");
        }
        Map<String, Object> map = BeanUtil.beanToMap(blogVo, new HashMap<>(), CopyOptions.create()
                .setIgnoreNullValue(true));
        log.info("将blogVo:{}写入redis", blogVo.getId());
        redisTemplate.opsForHash().putAll(REDIS_CACHE_BLOG + id, map);
        blogVo.setIsLike(Boolean.TRUE.equals(isMember));
        return ResponseUtils.success(blogVo);
    }

    @Override
    public JsonResponse<Object> like(Long id) {
        UserDto user = UserHolder.getUser();
        Long userId = user.getId();
        Double score = redisTemplate.opsForZSet().score(REDIS_CACHE_BLOG_LIKE + id, userId);
        boolean isMember = false;
        if ((score != null)) {
            isMember = true;
        }
        if (Boolean.TRUE.equals(isMember)) {
            //用户已点赞，则取消点赞
            log.info("用户已点赞，取消点赞");
            //redisTemplate.opsForSet().remove(REDIS_CACHE_BLOG_LIKE + id, userId);
            redisTemplate.opsForZSet().remove(REDIS_CACHE_BLOG_LIKE + id, userId);
            redisTemplate.opsForHash().increment(REDIS_CACHE_BLOG + id, "liked", -1);
            return queryById(id);
            //  redisTemplate.opsForHash().put();
        }
        //点赞
        log.info("用户点赞");
        redisTemplate.opsForZSet().add(REDIS_CACHE_BLOG_LIKE + id, userId, System.currentTimeMillis());
        redisTemplate.opsForHash().increment(REDIS_CACHE_BLOG + id, "liked", 1);
        return queryById(id);
    }

    @Override
    public JsonResponse<Object> list() {
        UserDto user = UserHolder.getUser();
        Long userId = user.getId();
        List<BlogVo> blogVos = blogMapper.selectListByQueryAs(QueryWrapper.create().select().from(BLOG).where(BLOG.USER_ID.eq(userId)),
                BlogVo.class);
        if (blogVos == null || blogVos.size() == 0) {
            return ResponseUtils.error(1, "尚未发布记录");
        }
        return ResponseUtils.success(blogVos);
    }

    @Override
    public JsonResponse<Object> queryBlogLikes(Long id) {

        Set rangeSet = redisTemplate.opsForZSet().range(REDIS_CACHE_BLOG_LIKE + id, 0, 4);
        List<UserVo> list = new ArrayList<>();
        for (Object o : rangeSet) {
            if (o != null) {
                UserVo userVo = userMapper.selectOneByQueryAs(QueryWrapper.create().select().from(USER).where(USER.ID.eq(o)),
                        UserVo.class);
                list.add(userVo);
            }
        }
        if (list.size() == 0 || list == null) {
            return ResponseUtils.error(1, "该笔记尚未用户点赞");
        }
        return ResponseUtils.success(list);

    }

    @Override
    public JsonResponse<Object> add(BlogDto blogDto) {
        Long userId = UserHolder.getUser().getId();
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogDto, blog);
        blog.setId(YitIdHelper.nextId());
        blog.setUserId(userId);
        int inserted = blogMapper.insertSelective(blog);
        if (inserted != 1) {
            return ResponseUtils.error(2, "笔记添加到数据库失败");
        }
        //查询出发布者的所有粉丝
        List<Long> ids = followMapper.selectListByQueryAs(QueryWrapper.create().select(FOLLOW.USER_ID).from(FOLLOW)
                .where(FOLLOW.FOLLOW_USER_ID.eq(userId)), Long.class);

        //推送笔记给所有粉丝
        if (ids == null || ids.size() == 0) {
            log.info("该用户无粉丝,不用推送笔记");
            return ResponseUtils.success("该用户无粉丝,不用推送笔记");
        }
        for (Long id : ids) {
            log.info("将笔记推送给粉丝，userId：{}", id);
            redisTemplate.opsForZSet().add(REDIS_FEED_BLOG + id, blog.getId(), System.currentTimeMillis());
        }
        return ResponseUtils.success(getById(blog.getId()));
    }

    @Override
    public JsonResponse<Object> followed(Long time, Integer offSet) {
        Long userId = UserHolder.getUser().getId();
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(REDIS_FEED_BLOG + userId, 0, time, offSet, 10);
        if (tuples == null || tuples.size() == 0) {
            return ResponseUtils.success(null);
        }
        int count = 0;
        List<BlogVo> list = new ArrayList<>();
        Double minScore = Collections.min(tuples, (o1, o2) -> (int) (o1.getScore() - o2.getScore())).getScore();
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            Double score = tuple.getScore();
            if (minScore.equals(score)) {
                count++;
            }
            Object blogId = tuple.getValue();
//  Long id = Long.getLong(blogId.toString());
            log.info("开始查询笔记：{}", blogId);
            BlogVo blogVo = getById(((Serializable) blogId));

            if (blogVo != null) {
                log.info("查询到用户:{}发布的笔记：{}", blogVo.getUserId(), blogVo.getId());
                list.add(blogVo);
            }
        }


        ScrollBlogVo scrollBlogVo = new ScrollBlogVo();
        scrollBlogVo.setBlogVos(list);
        scrollBlogVo.setMinTime(minScore.longValue());
        scrollBlogVo.setOffSet(count);
        return ResponseUtils.success(scrollBlogVo);
    }
}

