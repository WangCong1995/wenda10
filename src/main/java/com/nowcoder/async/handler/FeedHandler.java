package com.nowcoder.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 【事件监听器】
 * 当我关注问题或评论问题之后，都产生一条feed，然后将这条feed存入feed表中
 * Created by nowcoder on 2016/7/30.
 */
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;


    /**
     * 将事件的核心数据 转换成一个json串
     * @param model
     * @return 返回一个json串
     */
    private String buildFeedData(EventModel model) {
        Map<String, String> map = new HashMap<String ,String>();
        // 触发用户是通用的
        User actor = userService.getUser(model.getActorId());//触发用户。所有的新鲜事都会有个用户
        if (actor == null) {
            return null; /*如果是异常的，直接返回null */
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());

        /**
         * 关注可能要区分一下：
         * 关注的可能关注的是某一个人，这样的话question肯定就取不出来了。所以这里要加一个判断。
         */
        if (model.getType() == EventType.COMMENT ||
                (model.getType() == EventType.FOLLOW  && model.getEntityType() == EntityType.ENTITY_QUESTION)) {
            Question question = questionService.getById(model.getEntityId());
            if (question == null) {
                return null; /*如果是异常的，直接返回null */
            }
            map.put("questionId", String.valueOf(question.getId()));//问题id
            map.put("questionTitle", question.getTitle());//问题的标题。后面都是要拿出来显示的。
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel model) {

        /*1.构建一个新鲜事（feed）*/
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());/*feed里面的数据是从 EventModel 里取出来的*/
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedData(model)); /*将核心数据放到data里面。data存放的是一个json串*/
        if (feed.getData() == null) {
            return; /*如果data为null，说明核心数据出现了异常。应该直接return，这个事件我们就不处理了。 */
        }

        feedService.addFeed(feed);  /*2.将这条feed存入数据库*/

        /*【给事件的粉丝推】 */
        /*获取事件触发者的所有粉丝*/
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);

        followers.add(0);/*没登录的时候只能看系统队列。系统队列。0代表系统*/

        /*给所有粉丝 推一个feed。当我发生一个事件的时候，我所有的粉丝都能收到这样一个事件*/
        for (int follower : followers) { /*遍历所有粉丝的id*/
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);/*每个用户都有自己的timeline的key*/
            jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));/* 将这个feed的id，push到每个粉丝的timeline中*/
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW});//这个Handler会在评论和关注事件后执行。
    }
}
