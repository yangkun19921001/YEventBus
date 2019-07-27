package com.devyk.devykbus.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.devyk.devykbus.Constants;
import com.devyk.devykbus.utils.ThreadUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author  : devyk on 2019-07-27 18:03
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is YEventBus
 * </pre>
 */
public class YEventBus {

    /**
     * Log tag, apps may override it.
     */
    public static String TAG = "EventBus";

    /**
     * 当前实例
     */
    static volatile YEventBus defaultInstance;

    /**
     * 定义一个 Handler ,为了在主线程中执行任务
     */
    private Handler mHandler;

    /**
     * 定义一个用来装当前 class 的 订阅方法
     */
    private Map<Object, List<YSubscribleMethod>> mChacheSubscribleMethod = null;

    /**
     * 拿到 YEventBus 实例
     */
    public static YEventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (YEventBus.class) { //这里为了多线程中使用，加同步块
                if (defaultInstance == null) {
                    defaultInstance = new YEventBus();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * init
     */
    public YEventBus() {
        //init mChacheSubscribleMethod
        mChacheSubscribleMethod = new HashMap<>();
        //init Handler
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 注册方法
     */
    public void register(Object subscriber) {
        //拿到当前注册的所有的订阅者
        List<YSubscribleMethod> ySubscribleMethods = mChacheSubscribleMethod.get(subscriber);
        //如果订阅者已经注册了 就不需要再注册了
        if (ySubscribleMethods == null) {
            //开始反射找到当前类的订阅者
            ySubscribleMethods = getSubscribleMethods(subscriber);
            //注册了就存在缓存中，避免多次注册
            mChacheSubscribleMethod.put(subscriber, ySubscribleMethods);
        }
    }

    /**
     * 拿到当前注册的所有订阅者
     *
     * @param subscriber
     * @return
     */
    private List<YSubscribleMethod> getSubscribleMethods(Object subscriber) {
        //拿到注册的 class
        Class<?> subClass = subscriber.getClass();
        //定义一个容器，用来装订阅者
        List<YSubscribleMethod> ySubscribleMethodList = new ArrayList<>();
        //开始循环找到
        while (subClass != null) {
            //1. 开始进行筛选，如果是系统的就不需要进行下去
            String subClassName = subClass.getName();
            if (subClassName.startsWith(Constants.JAVA) ||
                    subClassName.startsWith(Constants.JAVA_X) ||
                    subClassName.startsWith(Constants.ANDROID) ||
                    subClassName.startsWith(Constants.ANDROID_X)
            ) {
                break;
            }
            //2. 遍历拿到当前 class
            Method[] declaredMethods = subClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                //3. 检测当前方法中是否有 我们的 订阅者 注解也就是 YSubscribe
                YSubscribe annotation = declaredMethod.getAnnotation(YSubscribe.class);
                //如果没有直接跳出查找
                if (annotation == null)
                    continue;

                // check 这个方法的参数是否有多个
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                if (parameterTypes.length > 1) {
                    throw new RuntimeException("YEventBus 只能接收一个参数");
                }

                //4. 符合要求，最后添加到容器中
                //4.1 拿到需要在哪个线程中接收事件
                YThreadMode yThreadMode = annotation.threadMode();
                //只能在当前 tag 相同下才能接收事件
                String tag = annotation.tag();
                YSubscribleMethod subscribleMethod = new YSubscribleMethod(tag, declaredMethod, yThreadMode, parameterTypes[0]);
                ySubscribleMethodList.add(subscribleMethod);

            }
            //去父类找订阅者
            subClass =   subClass.getSuperclass();
        }
        return ySubscribleMethodList;
    }

    /**
     * post 方法
     */
    public void post(String tag, Object object) {
        //拿到当前所有订阅者持有的类
        Set<Object> subscriberClass = mChacheSubscribleMethod.keySet();
        //拿到迭代器，
        Iterator<Object> iterator = subscriberClass.iterator();
        //进行循环遍历
        while (iterator.hasNext()) {
            //拿到注册 class
            Object subscribleClas = iterator.next();
            //获取类中所有添加订阅者的注解
            List<YSubscribleMethod> ySubscribleMethodList = mChacheSubscribleMethod.get(subscribleClas);
            for (YSubscribleMethod subscribleMethod : ySubscribleMethodList) {
                //判断这个方法是否接收事件
                if (!TextUtils.isEmpty(tag) && subscribleMethod.getTag().equals(tag) //注解上面的 tag 是否跟发送者的 tag 相同，相同就接收
                        && subscribleMethod.getEventType().isAssignableFrom(object.getClass() //判断类型
                )
                ) {
                    //根据注解上面的线程类型来进行切换接收消息
                    postMessage(subscribleClas, subscribleMethod, object);
                }

            }

        }

    }

    private void postMessage(final Object subscribleClas, final YSubscribleMethod subscribleMethod, final Object message) {

        //根据需要的线程来进行切换
        switch (subscribleMethod.getThreadMode()) {
            case MAIN:
                //如果接收的是主线程，那么直接进行反射，执行订阅者的方法
                if (isMainThread()) {
                    postInvoke(subscribleClas, subscribleMethod, message);
                } else {//如果接收消息在主线程，发送线程在子线程那么进行线程切换
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            postInvoke(subscribleClas, subscribleMethod, message);
                        }
                    });
                }
                break;
            case ASYNC://需要在子线程中接收
                if (isMainThread())
                    //如果当前 post  是在主线程中，那么切换为子线程
                    ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
                        @Nullable
                        @Override
                        public Boolean doInBackground() throws Throwable {
                            postInvoke(subscribleClas, subscribleMethod, message);
                            return true;
                        }

                        @Override
                        public void onSuccess(@Nullable Boolean result) {
                            Log.i(TAG, "执行成功");
                        }

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFail(Throwable t) {

                        }
                    });
                else
                    postInvoke(subscribleClas, subscribleMethod, message);
                break;
            case POSTING:
            case BACKGROUND:
            case MAIN_ORDERED:
                postInvoke(subscribleClas, subscribleMethod, message);
                break;
            default:
                break;
        }
    }

    /**
     * 反射调用订阅者
     *
     * @param subscribleClas
     * @param subscribleMethod
     * @param message
     */
    private void postInvoke(Object subscribleClas, YSubscribleMethod subscribleMethod, Object message) {
        Log.i(TAG, "post message: " + "TAG:" + subscribleMethod.getTag() + " 消息体：" + message);
        Method method = subscribleMethod.getMethod();
        //执行
        try {
            method.invoke(subscribleClas, message);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否是主线程
     *
     * @return
     */
    public boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 取消注册订阅者
     */
    public void unRegister(Object subscriber) {
        Log.i(TAG, "unRegister start：当前注册个数" + mChacheSubscribleMethod.size());
        Class<?> subClas = subscriber.getClass();
        List<YSubscribleMethod> ySubscribleMethodList = mChacheSubscribleMethod.get(subClas);
        if (ySubscribleMethodList != null)
            mChacheSubscribleMethod.remove(subscriber);

        Log.i(TAG, "unRegister success：当前注册个数" + mChacheSubscribleMethod.size());
    }
}
