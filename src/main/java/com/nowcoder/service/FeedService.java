package com.nowcoder.service;

import com.nowcoder.dao.FeedDAO;
import com.nowcoder.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用来读取feed
 * Created by nowcoder on 2016/8/12.
 */
@Service
public class FeedService {
    @Autowired
    FeedDAO feedDAO;

    /**
     * 取出所有与我相关联的人的feed
     * @param maxId
     * @param userIds
     * @param count
     * @return
     */
    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, int count) {
        return feedDAO.selectUserFeeds(maxId, userIds, count);
    }

    /**
     * 发生事件的时候，增加一个feed
     * @param feed
     * @return
     */
    public boolean addFeed(Feed feed) {
        feedDAO.addFeed(feed);
        return feed.getId() > 0;//如果加成功的话，则getId()大于0
    }

    /**
     * 推模式。查询一个feed
     * @param id
     * @return
     */
    public Feed getById(int id) {
        return feedDAO.getFeedById(id);
    }
}
