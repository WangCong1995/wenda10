package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import com.nowcoder.model.Feed;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Mapper
public interface FeedDAO {
    String TABLE_NAME = " feed ";
    String INSERT_FIELDS = " user_id, data, created_date, type ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    /* 插入一个feed */
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{data},#{createdDate},#{type})"})
    int addFeed(Feed feed);

    /**
     * “推”模式。拿feed的 id去查一个feed
     * @param id
     * @return
     */
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Feed getFeedById(int id);


    /**
     * 增量地“拉“的模式去获取我关注的人 或全体用户的 feed。（查一批的feed）
     * 如果我是一个没登录的状态，userIds这个变量就用不上了。只需要用maxId和count就可以了。
     * 如果是的登录状态就需要把这个userId用起来。
     * @param maxId 最新的微博在最上面，然后往下拉。当我翻页的时候，下一页的最上面的微博（feed）的id，要小于MaxId
     * @param userIds 我关注的好友们的id（feed流的来源用户们）
     * @param count 分页
     * @return
     */
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
                               @Param("userIds") List<Integer> userIds,
                               @Param("count") int count);
}
