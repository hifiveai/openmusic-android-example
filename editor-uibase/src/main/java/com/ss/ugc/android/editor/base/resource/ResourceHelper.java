package com.ss.ugc.android.editor.base.resource;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.ss.ugc.android.editor.base.utils.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ResourceHelper implements EffectInterface.EffectResourceProvider {
//    public static final String RESOURCE = "EditorResource";
    public static final String RESOURCE = "resource";
    public static final String LOCAL_RESOURCE = "LocalResource";
    private static final String LICENSE_NAME = "demo.licbag";

    private Context mContext;
    private Gson gson;

    private static ResourceHelper instance = new ResourceHelper();

    public void init(Context context) {
        this.mContext = context;
    }

    private ResourceHelper() {
        gson = new Gson();
    }

    public static ResourceHelper getInstance() {
        return instance;
    }

    public ResourceHelper(Context mContext) {
        this.mContext = mContext;
    }

    public String getResourceRootPath() {
        return mContext.getExternalFilesDir("assets").getAbsolutePath();
    }

    public String getResourcePath() {
        return getResourceRootPath() + File.separator + RESOURCE; // file/editor/EditorResource
    }

    public String getLocalResourcePath() {
        return getResourceRootPath() + File.separator + LOCAL_RESOURCE; // file/editor/EditorResource
    }

    @Override
    public String getLicensePath() {
        return new File(new File(getResourcePath(), "LicenseBag.bundle"), LICENSE_NAME).getAbsolutePath();
    }

    @Override
    public String getAnimationRootPath() {
        return new File(getResourcePath(), "video_animation.bundle").getAbsolutePath() + File.separator; // file/editor/EditorResource/Filter/
    }

    @Override
    public String getTextRootPath(String type) {
        return new File(getResourcePath(), type).getAbsolutePath() + File.separator; // file/editor/EditorResource/Filter/
    }

    @Override
    public String getAudioFilterRootPath() {
        return new File(getResourcePath(), "tone.bundle").getAbsolutePath() + File.separator; // file/editor/EditorResource/AudioFilter/
    }

    @Override
    public String getAdjustRootPath() {
        return new File(getResourcePath(), "adjust.bundle").getAbsolutePath() + File.separator; // file/editor/EditorResource/Filter/
    }

    public String getDefault() {
        return new File(getLocalResourcePath(), "default.bundle").getAbsolutePath() + File.separator; // file/editor/EditorResource/Filter/
    }

    private String getDefaultFontsPath() {
        return  new File(getLocalResourcePath(), "default_fonts.bundle").getAbsolutePath() + File.separator;
    }

    public String getCanvasStyleRootPath() {
        return new File(getResourcePath(), "CanvasStyle").getAbsolutePath() + File.separator; // file/editor/EditorResource/CanvasStyle/
    }

    private List<ResourceItem> mStickerItems;
    private List<ResourceItem> mFilterItems;

    public List<ResourceItem> getFilterList2() {
        if (mFilterItems != null) {
            return mFilterItems;
        }

        mFilterItems = getResourceList("ve_filter.bundle", "ve_filter.json");
        
        return mFilterItems;
    }

    public List<ResourceItem> getBlendModeList() {
        return getResourceList("mix.bundle", "mix.json");
    }

    public List<ResourceItem> getStickerList() {
        if (mStickerItems != null) {
            return mStickerItems;
        }
        mStickerItems = getResourceList("sticker.bundle", "sticker.json");
        return mStickerItems;
    }

    public List<ResourceItem> getAdjustList() {
        String json = FileUtil.readJsonFile(getAdjustRootPath() + "adjust.json");
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getAdjustRootPath() + item.getIcon());
            item.setPath(getAdjustRootPath() + item.getPath());
        }

        return list;
    }

    /**
     * @param type 1入场 2出场 0组合
     * @return
     */
    public List<ResourceItem> getAnimationList(int type) {

        String json = FileUtil.readJsonFile(getAnimationRootPath() + "video_animation.json");
        EditorResBean animationBean = gson.fromJson(json, EditorResBean.class);
        List<ResourceItem> list = new ArrayList<>();

        for (ResourceItem resourceItem : animationBean.getResource().getList()) {
            boolean isInAnimation = type == 1 && TextUtils.equals(resourceItem.getTags(), "入场");
            boolean isOutAnimation = type == 2 && TextUtils.equals(resourceItem.getTags(), "出场");
            boolean isComAnimation = type == 0 && TextUtils.equals(resourceItem.getTags(), "组合");
            if (isInAnimation || isOutAnimation || isComAnimation) {
                list.add(resourceItem);
            }
        }

        for (ResourceItem item : list) {
            item.setIcon(getAnimationRootPath() + item.getIcon());
            item.setPath(getAnimationRootPath() + item.getPath());
        }

        return list;
    }

    //alignType
    public List<ResourceItem> getTextAlignTypeList() {

        String json = FileUtil.readJsonFile(getTextRootPath("text_align.bundle") + "align.json");
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getTextRootPath("text_align.bundle") + item.getIcon());
            item.setPath(getTextRootPath("text_align.bundle") + item.getPath());
        }

        return list;
    }

    public List<ResourceItem> getTextStyleList() {

        String json = FileUtil.readJsonFile(getTextRootPath("text_style.bundle") + "style.json");
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getTextRootPath("text_style.bundle") + item.getIcon());
            item.setPath(getTextRootPath("text_style.bundle") + item.getPath());
        }

        return list;
    }

    public List<ResourceItem> getDefaultFontList() {
        try {
            String json = FileUtil.readJsonFile(getDefaultFontsPath() + "font.json");
            ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
            List<ResourceItem> list = resourceBean.getResource().getList();

            for (ResourceItem item : list) {
                item.setPath(getDefaultFontsPath() + item.getPath());
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public List<ResourceItem> getLocalTextFontList() {
        return getLocalResourceList("text_fonts.bundle", "font.json");
    }

    public List<ResourceItem> getTextFontList() {
        List<ResourceItem> localList = getLocalTextFontList();

        String json = FileUtil.readJsonFile(getTextRootPath("text_fonts.bundle") + "font.json");
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getTextRootPath("text_fonts.bundle") + item.getIcon());

            String path = getTextRootPath("text_fonts.bundle") + item.getPath();
            String ttfPah = FileUtil.findDirFirstFilePath(path);

            item.setPath(ttfPah);
        }

        localList.addAll(list);

        return localList;
    }

    public List<ResourceItem> getTextColorsList() {

        String json = FileUtil.readJsonFile(getTextRootPath("text_color.bundle") + "color.json");
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getTextRootPath("text_color.bundle") + item.getIcon());
            item.setPath(getTextRootPath("text_color.bundle") + item.getPath());
        }

        return list;
    }

    public List<ResourceItem> getTextFlowerList() {
        return getResourceList("flower.bundle", "flower.json");
    }

    public List<ResourceItem> getTextBubbleList() {
        return getResourceList("bubble.bundle", "bubble.json");
    }

    public List<ResourceItem> getAudioFilterList(){
        return getAudioFilterResourceList("tone.json");
    }

    private List<ResourceItem> getAudioFilterResourceList(String jsonName) {
        String json = FileUtil.readJsonFile(getAudioFilterRootPath() + jsonName);
        ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            if (!TextUtils.isEmpty(item.getIcon())) {
                item.setIcon(getAudioFilterRootPath() + item.getIcon());
            }
            if (!TextUtils.isEmpty(item.getPath())) {
                item.setPath(getAudioFilterRootPath() + item.getPath());
            }
        }
        return list;
    }

    public List<ResourceItem> getTextAnimationList(String type) {
        List<ResourceItem> textAnimationList = getResourceList("text_animation.bundle", "text_animation.json");
        List<ResourceItem> list = new ArrayList<>();

        for (ResourceItem resourceItem : textAnimationList) {
            boolean isInAnimation = TextUtils.equals("ruchang", type) && TextUtils.equals(resourceItem.getTags(), "入场");
            boolean isOutAnimation = TextUtils.equals("chuchang", type) && TextUtils.equals(resourceItem.getTags(), "出场");
            boolean isComAnimation = TextUtils.equals("xunhuan", type) && TextUtils.equals(resourceItem.getTags(), "循环");
            if (isInAnimation || isOutAnimation || isComAnimation) {
                list.add(resourceItem);
            }
        }

        return list;
    }

    public List<ResourceItem> getStickerAnimationList(String type) {
        List<ResourceItem> textAnimationList = getResourceList("sticker_animation.bundle", "sticker_animation.json");
        List<ResourceItem> list = new ArrayList<>();

        for (ResourceItem resourceItem : textAnimationList) {
            boolean isInAnimation = TextUtils.equals("ruchang", type) && TextUtils.equals(resourceItem.getTags(), "入场");
            boolean isOutAnimation = TextUtils.equals("chuchang", type) && TextUtils.equals(resourceItem.getTags(), "出场");
            boolean isComAnimation = TextUtils.equals("xunhuan", type) && TextUtils.equals(resourceItem.getTags(), "循环");
            if (isInAnimation || isOutAnimation || isComAnimation) {
                list.add(resourceItem);
            }
        }

        return list;
    }

    public List<ResourceItem> getTextResourceList(String file) {
        final String json = FileUtil.readJsonFile(getTextRootPath("text_template.bundle") + file);
        final ResourceBean resourceBean = gson.fromJson(json, ResourceBean.class);
        final List<ResourceItem> list = resourceBean.getResource().getList();

        for (ResourceItem item : list) {
            item.setIcon(getTextRootPath("text_template.bundle") + item.getIcon());
            item.setPath(getTextRootPath("text_template.bundle") + item.getPath());
        }

        return list;
    }

    /**
     *
     * @param type hot、env、basic，目前三个返回值是一样的
     * @return
     */
    public List<ResourceItem> getVideoEffectList(String type) {
        return getResourceList("ve_effect.bundle", "ve_effect.json");
    }

    public List<ResourceItem> getMusicList() {
        return getLocalResourceList("music.bundle", "music.json");
    }

    public List<ResourceItem> getTransitionList() {
        return getResourceList("transitions.bundle", "transitions.json");
    }

    /**
     * 获取视频蒙版的本地资源
     * @return
     */
    public List<ResourceItem> getVideoMaskList() {
        return getResourceList("video_mask.bundle", "videomask.json");
    }

    public List<ResourceItem> getCurveSpeedList() {
        return getResourceList("curve_speed.bundle", "curvespeed.json");
    }

    public List<ResourceItem> getSoundList() {
        return getLocalResourceList("sound.bundle", "sound.json");
    }

    private List<ResourceItem> getLocalResourceList(String bundle, String resJson) {
        String resRootPath = new File(getLocalResourcePath(), bundle).getAbsolutePath() + File.separator;
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

    /**
     * 获取画布样式的本地资源
     */
    public List<ResourceItem> getCanvasStyleList() {
        List<ResourceItem> list = getResourceList("canvas.bundle", "canvas.json");

        for (ResourceItem item : list) {
            item.setPath(item.getIcon());
        }

        return list;
    }
}