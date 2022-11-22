package com.ss.ugc.android.editor.main;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ss.ugc.android.editor.base.resource.ResourceHelper;
import com.ss.ugc.android.editor.base.task.FileUtils;
import com.ss.ugc.android.editor.core.utils.DLog;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 */
// 部分手机不回调 onPostExecute,临时解决一下，后面不再使用 syncTask

public class EditorResCopyTask extends AsyncTask<String, Void, Boolean> {
    public static final String DIR = "resource";
    public static final String LOCAL_DIR = "LocalResource";

    public interface IUnzipViewCallback {
        void onStartTask();
        void onEndTask(boolean result);
    }

    private WeakReference<IUnzipViewCallback> mCallback;
    private Context mContext;

    public EditorResCopyTask(Context context, IUnzipViewCallback callback) {
        mCallback = new WeakReference<>(callback);
        this.mContext = context.getApplicationContext();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        IUnzipViewCallback callback = mCallback.get();
        if (callback == null) return false;

        boolean isSucRes = copyResource(strings[0]);
        boolean isSucLocalRes = copyResource(strings[1]);

        if (isSucRes && isSucLocalRes) {
            callback.onEndTask(true);
            return true;
        }

        DLog.d("isSucRes:" + isSucRes + "  isSucLocalRes:" + isSucLocalRes);
        callback.onEndTask(false);
        return false;

//        return FileUtils.unzipAssetFile(mCallback.get().getContext(), zipPath, dstFile);
    }

    private boolean copyResource(String path) {
        File dstFile = mContext.getExternalFilesDir("assets");
        FileUtils.clearDir(new File(dstFile, path));

        DLog.d("path:" + path + "  dstFile:" + dstFile.getAbsolutePath() ); //dstFile:/storage/emulated/0/Android/data/com.ss.android.vesdk.vedemo/files/assets
        try {
            initResource(mContext, path);
//            Log.d("copy ", "copy success ");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        IUnzipViewCallback callback = mCallback.get();
        if (callback == null) return;
        callback.onStartTask();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // 部分手机不回调 onPostExecute,临时解决一下，后面不再使用 syncTask
//        IUnzipViewCallback callback = mCallback.get();
//        if (callback == null) return;
//        callback.onEndTask(result);
    }

    private void initResource(Context context, String child) {
        String dstFile = ResourceHelper.getInstance().getResourceRootPath();

        FileUtils.clearDir(new File(dstFile, child));
        try {
            FileUtils.copyAssets(context.getAssets(), child, dstFile );
        } catch (IOException e) {
            Log.d("copy ", "catch exception");
            e.printStackTrace();
        }

    }

}
