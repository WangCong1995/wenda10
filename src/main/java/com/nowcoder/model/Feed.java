package com.nowcoder.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * 新鲜事
 * Created by nowcoder on 2016/8/12.
 */
public class Feed {
    private int id;//新鲜事的id
    private int type;//新鲜事的类型
    private int userId;//由人来产生新鲜事
    private Date createdDate;//产生的时间
    private String data;//JSON串。新鲜事的核心数据
    private JSONObject dataJSON = null; /*为了快速把data变量里面的数据读取出来。我们加一个辅助变量*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        /*当把data设置进来的时候，我们把 辅助变量dataJSON也初始化*/
        dataJSON = JSONObject.parseObject(data);
    }

    /**
     * 有了这个函数，我们就可以在前端feeds.html页面中，直接使用 ${feed.userName} 取出data(Json串)里面的字段.
     * 因为我们不知道data里面可能会存什么样的字段。所有使用这种根据key来查的方式比较好。你传过来什么key，我就去查什么value
     * @param key
     * @return
     */
    public String get(String key) {
        return dataJSON == null ? null : dataJSON.getString(key);
    }
}
