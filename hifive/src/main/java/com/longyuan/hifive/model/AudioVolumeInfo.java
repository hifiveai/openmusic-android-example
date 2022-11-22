package com.longyuan.hifive.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * #duxiaxing
 * #date: 2022/7/29
 */
public class AudioVolumeInfo {
    public String id;
    public int mNumSamples;
    public int mNumFrames;
    public int[] mFrameGains;
    //
    public int mNumZoomLevels;
    public int mZoomLevel;
    public double[] mHeightsAtThisZoomLevel;

    public int getNumFrames() {
        return mNumFrames;
    }

    public int[] getFrameGains() {
        return mFrameGains;
    }

//    // Should be removed when the app will use directly the samples instead of the frames.
//    public int getSamplesPerFrame() {
//        return 16000 / 50;  // just a fixed value here...
////        return 1024/2;  // just a fixed value here...
//    }


    @Override
    public String toString() {
        return "AudioVolumeInfo{" + "id=" + id +
                ", mNumSamples=" + mNumSamples +
                ", mNumFrames=" + mNumFrames +
                ", mFrameGains length=" + mFrameGains.length +
                ", mNumZoomLevels=" + mNumZoomLevels +
                ", mZoomLevel=" + mZoomLevel +
                ", mHeightsAtThisZoomLevel length=" + mHeightsAtThisZoomLevel.length +
                '}';
    }

    public String toJsonString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id",id);
            jsonObject.put("mNumSamples",mNumSamples);
            jsonObject.put("mNumFrames",mNumFrames);

            JSONArray array_gain = new JSONArray();
            for (int gain : mFrameGains){
                array_gain.put(gain);
            }
            jsonObject.put("mFrameGains",array_gain);

            jsonObject.put("mNumZoomLevels",mNumZoomLevels);
            jsonObject.put("mZoomLevel",mZoomLevel);

            JSONArray array_height = new JSONArray();
            for (double height : mHeightsAtThisZoomLevel){
                array_height.put(height);
            }
            jsonObject.put("mHeightsAtThisZoomLevel",array_height);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void formatJson(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            id = jsonObject.optString("id");
            mNumSamples = jsonObject.optInt("mNumSamples");
            mNumFrames = jsonObject.optInt("mNumFrames");
            mNumZoomLevels = jsonObject.optInt("mNumZoomLevels");
            mZoomLevel = jsonObject.optInt("mZoomLevel");

            JSONArray array_gain = jsonObject.optJSONArray("mFrameGains");
            if (array_gain != null) {
                mFrameGains = new int[array_gain.length()];
                for (int i = 0; i < array_gain.length(); i++) {
                    mFrameGains[i] = array_gain.optInt(i);
                }
            }

            JSONArray array_height = jsonObject.optJSONArray("mHeightsAtThisZoomLevel");
            if (array_height != null){
                mHeightsAtThisZoomLevel = new double[array_height.length()];
                for (int i = 0; i < array_height.length(); i++){
                    mHeightsAtThisZoomLevel[i] = array_height.optDouble(i);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
