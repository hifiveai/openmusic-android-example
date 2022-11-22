package com.vesdk.vebase.resource;

import android.content.Context;

import com.google.gson.Gson;
import com.vesdk.vebase.DemoApplication;
import com.vesdk.vebase.old.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 *  on 2020/7/30 14:26
 */
public class ResourceHelper {
    public static final String RESOURCE = "resource";
    public static final String LOCAL_RESOURCE = "LocalResource";
    private static final String LICENSE_NAME = "demo.licbag";

    private Context mContext;
    private Gson gson;

    private static ResourceHelper instance = new ResourceHelper();

    private ResourceHelper() {
        this.mContext = DemoApplication.context();
        gson = new Gson();
    }

    public static ResourceHelper getInstance() {
        return instance;
    }

    public String getLicensePath() {
        return new File(new File(getRecordResourcePath(), "LicenseBag.bundle"), LICENSE_NAME).getAbsolutePath();
    }

    /**
     * 拍摄部分 资源路径
     * @return
     */

    private String getRecordResourcePath() {
        return mContext.getExternalFilesDir("assets").getAbsolutePath() + File.separator + "resource";
    }
    public String getStickerPath(String sticker) {
        return new File(new File(new File(getRecordResourcePath()), "StickerResource.bundle"), sticker).getAbsolutePath();
    }
    public String getFilterPath() {
        return new File(new File(getRecordResourcePath(), "FilterResource.bundle"), "Filter").getAbsolutePath();
    }

    public String getFilterPath(String filter) {
        return new File(getFilterPath(), filter).getAbsolutePath();
    }
    public String getModelPath() {
        return new File(new File(getRecordResourcePath(), "ModelResource.bundle"), "").getAbsolutePath();
    }

    //  assets/resource/ComposeMakeup.bundle/ComposeMakeup/
    public String getComposePath() {
        return new File(new File(getRecordResourcePath(), "ComposeMakeup.bundle"), "ComposeMakeup").getAbsolutePath() + File.separator;
    }

    /**
     * 合拍资源路径
     * @return
     */
    public String getDuetPath() {
        return new File(getRecordResourcePath(), "duet.bundle").getAbsolutePath() + File.separator;
    }

    public List<ResourceItem> getDuetList() {
        return getResourceList("duet.bundle", "duet.json");
    }

    /**
     * 编辑部分 资源路径
     * @return
     */
    public String getResourceRootPath() {
        return mContext.getExternalFilesDir("assets").getAbsolutePath();
    }

    private String getResourcePath() {
        return getResourceRootPath() + File.separator + RESOURCE; // file/editor/EditorResource
    }

    public String getLocalResourcePath() {
        return getResourceRootPath() + File.separator + LOCAL_RESOURCE; // file/editor/EditorResource
    }

    public String getDefault() {
        return new File(getLocalResourcePath(), "default.bundle").getAbsolutePath() + File.separator; // file/editor/EditorResource/Filter/
    }
    public String getWaterMarkPath() {
        return new File(getLocalResourcePath(), "watermark.bundle").getAbsolutePath() + File.separator + "ve-watermark.png"; // file/editor/EditorResource/Filter/
    }

    private List<ResourceItem> getResourceList(String bundle, String resJson) {
        String resRootPath = new File(getResourcePath(), bundle).getAbsolutePath() + File.separator;
        String json = FileUtil.readJsonFile(resRootPath + resJson);
        EditorResBean animationBean = gson.fromJson(json, EditorResBean.class);
        List<ResourceItem> list = animationBean.getResource().getList();

        try {
            assert json != null;
            JSONObject jsonObject = new JSONObject(json);
            JSONObject resources = jsonObject.getJSONObject("resource");
            JSONArray itemList = resources.getJSONArray("list");

            for (int i = 0; i < itemList.length(); i++) {
                JSONObject item = itemList.getJSONObject(i);
                list.get(i).overlap = item.optBoolean("isOverlap",false);
                list.get(i).setIcon(resRootPath + list.get(i).getIcon());
                list.get(i).setPath(resRootPath + list.get(i).getPath());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}