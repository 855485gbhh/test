package com.example.springbootredis.service.impl;

import com.example.springbootredis.dao.UserMapper;
import com.example.springbootredis.pojo.dto.FollowDto;
import com.example.springbootredis.pojo.po.Follow;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.FollowVo;
import com.example.springbootredis.pojo.vo.UserVo;
import com.example.springbootredis.utils.ResponseUtils;
import com.example.springbootredis.utils.UserHolder;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.example.springbootredis.dao.FollowMapper;
import com.example.springbootredis.service.FollowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.springbootredis.constant.RedisConstant.REDIS_CACHE_FOLLOW;
import static com.example.springbootredis.pojo.po.table.FollowTableDef.FOLLOW;
import static com.example.springbootredis.pojo.po.table.UserTableDef.USER;

/**
 * (Follow)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class FollowServiceImpl implements FollowService {
    @Resource
    private FollowMapper followMapper;
    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public JsonResponse<Object> follow(Long id, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        if (userId.equals(id)) {
            return ResponseUtils.error(1, "自己不能关注自己");
        }
        FollowVo followVo = followMapper.selectOneByQueryAs(QueryWrapper.create().
                        select().from(FOLLOW).
                        where(FOLLOW.USER_ID.eq(userId)).and(FOLLOW.FOLLOW_USER_ID.eq(id)),
                FollowVo.class);
        //关注
        if (Boolean.TRUE.equals(isFollow)) {
            if (followVo == null) {
                FollowDto followDto = new FollowDto();
                followDto.setUserId(userId);
                followDto.setFollowUserId(id);
                generateFollow(followDto);
                redisTemplate.opsForSet().add(REDIS_CACHE_FOLLOW + userId, id);
                return ResponseUtils.success("关注成功");
            }
        }
        //取关
        if (Boolean.FALSE.equals(isFollow)) {
            if (followVo != null) {
                removeFollow(followVo.getId());
                redisTemplate.opsForSet().remove(REDIS_CACHE_FOLLOW + userId, id);
                return ResponseUtils.success("取消关注成功");
            }
        }
        return ResponseUtils.success(null);
    }

    @Override
    public JsonResponse<Object> followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        Set intersectSet = redisTemplate.opsForSet().intersect(REDIS_CACHE_FOLLOW + userId, REDIS_CACHE_FOLLOW + id);
        if (intersectSet != null && intersectSet.size() != 0) {
            List<UserVo> list = new ArrayList<>();
            intersectSet.stream().forEach(o -> {
                UserVo userVo = userMapper.selectOneByQueryAs(QueryWrapper.create().
                                select().from(USER).where(USER.ID.eq(o)),
                        UserVo.class);
                list.add(userVo);
            });
            log.info("从redis查询到共同关注");
            return ResponseUtils.success(list);
        }
        List<Long> ids = followMapper.selectListByQueryAs(QueryWrapper.create()
                .select(FOLLOW.FOLLOW_USER_ID).from(FOLLOW)
                .where(FOLLOW.USER_ID.eq(userId)
                        .and(FOLLOW.FOLLOW_USER_ID.
                                in(QueryWrapper.create().select(FOLLOW.FOLLOW_USER_ID).from(FOLLOW)
                                        .where(FOLLOW.USER_ID.eq(id))))), Long.class);
        List<UserVo> userVos = userMapper.selectListByQueryAs(QueryWrapper.create().select()
                .from(USER).where(USER.ID.in(ids)), UserVo.class);
        if(userVos==null||userVos.size()==0){
            return ResponseUtils.success(null);
        }
        log.info("从mysql查询到共同关注");
        return ResponseUtils.success(userVos);

    }

    private void removeFollow(Long id) {
        if (id == null) {
            return;
        }
        followMapper.deleteById(id);
    }

    private void generateFollow(FollowDto followDto) {
        Follow follow = new Follow();
        BeanUtils.copyProperties(followDto, follow);
        followMapper.insertSelective(follow);
    }
}

