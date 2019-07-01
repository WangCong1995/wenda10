package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by nowcoder on 2016/7/15.
 */
@Controller
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;

    /*当使用push模式*/
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model) {
        /*如果没登录，那当前用户的id就是系统0。*/
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;/*获取当前用户的id*/
        /*从自己的Redis的List(即timelinekey)中，取出10个feed的id*/
        List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 10);
        List<Feed> feeds = new ArrayList<Feed>();
        /*一个一个地根据id，把feed取出来，之后传到前端页面*/
        for (String feedId : feedIds) { /* 把所有别人推给我的新鲜事的id取出来*/
            Feed feed = feedService.getById(Integer.parseInt(feedId));
            if (feed == null) { /*feed可能会被拥有者突然删掉，所以要进行非空判断*/
                continue;
            }
            feeds.add(feed);
        }
        model.addAttribute("feeds", feeds);/*将feed流传到前端页面*/
        return "feeds";
    }

    /*拉取feed流。直接把数据库里面的取出来即可*/
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPullFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) { /*处于登录状态*/
            /*取出 当前用户关注的所有的人的id*/
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);/*从最新的（即从maxId往下找）开始找，找10个*/
        model.addAttribute("feeds", feeds); /*将feed流 传到 feeds.html页面去渲染*/
        return "feeds";
    }
}
