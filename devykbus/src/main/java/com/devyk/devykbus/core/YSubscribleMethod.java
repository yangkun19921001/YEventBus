package com.devyk.devykbus.core;

import java.lang.reflect.Method;

/**
 * <pre>
 *     author  : devyk on 2019-07-27 18:50
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is YSubscribleMethod
 * </pre>
 */
public class YSubscribleMethod {
    //注册方法
    private Method method;
    //线程类型
    private YThreadMode threadMode;

    //参数类型
    private Class<?> eventType;

    //接收参数的 TAG
    private String tag;

    public YSubscribleMethod(Method method, YThreadMode threadMode, Class<?> eventType) {
        this.method = method;
        this.threadMode = threadMode;
        this.eventType = eventType;
    }



    public YSubscribleMethod(String megTag, Method method, YThreadMode threadMode, Class<?> eventType) {
        this.method = method;
        this.threadMode = threadMode;
        this.eventType = eventType;
        this.tag = megTag;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public YThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(YThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public void setEventType(Class<?> eventType) {
        this.eventType = eventType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
