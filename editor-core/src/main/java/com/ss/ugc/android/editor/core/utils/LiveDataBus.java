package com.ss.ugc.android.editor.core.utils;




import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 粘性事件：
 * LiveDataBus有bug，就是当second界面开启后，会自动收到上一个界面来的消息
 *
 * 此LiveDataBus修复了这个bug
 */
public class LiveDataBus {

    private Map<String, BusMutableLiveData> bus;
    private LiveDataBus(){
        bus = new HashMap<>();
    }
    private static LiveDataBus liveDataBus = new LiveDataBus();
    public static LiveDataBus getInstance(){
        return liveDataBus;
    }


    public <T> MutableLiveData<T> with(String key , Class<T > type){
        if ( !bus.containsKey(key) ){
            bus.put( key , new BusMutableLiveData<T>());
        }
        return bus.get(key);
    }


    public static class BusMutableLiveData<T> extends MutableLiveData<T>{

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            super.observe(owner, observer);
            hook(observer);
        }

        /**
         * 通过反射hook 改变mVersion的值
         * @param observer
         */
        private void hook(Observer<? super T> observer) {
            try{
                //1.得到mLastVersion
                Class<LiveData> liveDataClass=LiveData.class;
                Field mObserversField = liveDataClass.getDeclaredField("mObservers");
                mObserversField.setAccessible(true);
                //获取到这个成员变量对应的对象
                Object mObserversObject = mObserversField.get(this);
                //得到map
                Class<?> mObserversObjectClass = mObserversObject.getClass();
                //获取到mObservers对象的get方法
                Method get=mObserversObjectClass.getDeclaredMethod("get",Object.class);
                get.setAccessible(true);
                //执行get方法
                Object invokeEntry=get.invoke(mObserversObject,observer);
                //取到map中的value
                Object observerWraper=null;
                if(invokeEntry!=null && invokeEntry instanceof Map.Entry){
                    observerWraper=((Map.Entry)invokeEntry).getValue();
                }
                if(observerWraper==null){
                    throw new NullPointerException("observerWraper is null");
                }
                //得到ObserverWrapper的类对象
                Class<?> superclass=observerWraper.getClass().getSuperclass();
                Field mLastVersion = superclass.getDeclaredField("mLastVersion");
                mLastVersion.setAccessible(true);


                //2.得到mVersion
                Field mVersion = liveDataClass.getDeclaredField("mVersion");
                mVersion.setAccessible(true);

                //3.把mVersion的值填入到mLastVersion中
                Object mVersionValue=mVersion.get(this);
                mLastVersion.set(observerWraper,mVersionValue);

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
