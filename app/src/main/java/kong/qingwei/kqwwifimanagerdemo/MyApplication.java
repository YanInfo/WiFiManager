package kong.qingwei.kqwwifimanagerdemo;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

/**
 * @Author: zhangyan
 * @Date: 2019/4/9 15:48
 * @Description: 全局Application
 * @Version: 1.0
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    public static Context getContext() {
        return context;
    }

    private static Context context;

}
