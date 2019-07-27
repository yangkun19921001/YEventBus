package com.devyk.devykbus.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <pre>
 *     author  : devyk on 2019-07-27 18:05
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is YSubscribe
 * </pre>
 */

@Target(ElementType.METHOD) //target 描述此注解在哪里使用
@Retention(RetentionPolicy.RUNTIME) //retention 描述此注解保留的时长 这里是在运行时
public @interface YSubscribe {
    YThreadMode threadMode() default YThreadMode.POSTING; //默认是在 post 线程接收数据

    String tag() default "";//根据消息来接收事件
}
