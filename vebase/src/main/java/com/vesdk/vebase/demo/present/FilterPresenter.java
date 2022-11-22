package com.vesdk.vebase.demo.present;

import android.content.Context;


import com.vesdk.vebase.DemoApplication;
import com.vesdk.vebase.R;
import com.vesdk.vebase.demo.model.FilterItem;
import com.vesdk.vebase.demo.present.contract.FilterContract;
import com.vesdk.vebase.resource.ResourceHelper;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class FilterPresenter extends FilterContract.Presenter {
    private static final int[] IMAGES = new int[] {
            R.drawable.ck_filter_zhengchang,
            R.drawable.ck_filter_baixi,
            R.drawable.ck_filter_naiyou,
            R.drawable.ck_filter_yangqi,
            R.drawable.ck_filter_jugeng,
            R.drawable.ck_filter_luolita,
            R.drawable.ck_filter_mitao,
            R.drawable.ck_filter_makalong,
            R.drawable.ck_filter_paomo,
            R.drawable.ck_filter_yinhua,
            R.drawable.ck_filter_musi,
            R.drawable.ck_filter_wuyu,
            R.drawable.ck_filter_beihaidao,
            R.drawable.ck_filter_riza,
            R.drawable.ck_filter_xiyatu,
            R.drawable.ck_filter_jingmi,
            R.drawable.ck_filter_jiaopian,
            R.drawable.ck_filter_nuanyang,
            R.drawable.ck_filter_jiuri,
            R.drawable.ck_filter_hongchun,
            R.drawable.ck_filter_julandiao,
            R.drawable.ck_filter_tuise,
            R.drawable.ck_filter_heibai,
            R.drawable.ck_filter_wenrou,
            R.drawable.ck_filter_lianaichaotian,
            R.drawable.ck_filter_chujian,
            R.drawable.ck_filter_andiao,
            R.drawable.ck_filter_naicha,
            R.drawable.ck_filter_soft,
            R.drawable.ck_filter_xiyang,
            R.drawable.ck_filter_lengyang,
            R.drawable.ck_filter_haibianrenxiang,
            R.drawable.ck_filter_gaojihui,
            R.drawable.ck_filter_haidao,
            R.drawable.ck_filter_qianxia,
            R.drawable.ck_filter_yese,
            R.drawable.ck_filter_hongzong,
            R.drawable.ck_filter_qingtou,
            R.drawable.ck_filter_ziran2,
            R.drawable.ck_filter_suda,
            R.drawable.ck_filter_jiazhou,
            R.drawable.ck_filter_shise,
            R.drawable.ck_filter_chuanwei,
            R.drawable.ck_filter_meishijiaopian,
            R.drawable.ck_filter_hongsefugu,
            R.drawable.ck_filter_lutu,
            R.drawable.ck_filter_nuanhuang,
            R.drawable.ck_filter_landiaojiaopian

    };
    // todo wzz
//    private EffectInterface.EffectResourceProvider mResourceProvider;

    private List<FilterItem> mItems;

    @Override
    public List<FilterItem> getItems() {
        if (mItems != null) {
            return mItems;
        }
        mItems = new ArrayList<>();
        Context context = DemoApplication.context();
        String[] FILTER_TITLE = new String[]{
                context.getString(R.string.ck_filter_normal),
                context.getString(R.string.ck_filter_chalk),
                context.getString(R.string.ck_filter_cream),
                context.getString(R.string.ck_filter_oxgen),
                context.getString(R.string.ck_filter_campan),
                context.getString(R.string.ck_filter_lolita),
                context.getString(R.string.ck_filter_mitao),
                context.getString(R.string.ck_filter_makalong),
                context.getString(R.string.ck_filter_paomo),
                context.getString(R.string.ck_filter_yinhua),
                context.getString(R.string.ck_filter_musi),
                context.getString(R.string.ck_filter_wuyu),
                context.getString(R.string.ck_filter_beihaidao),
                context.getString(R.string.ck_filter_riza),
                context.getString(R.string.ck_filter_xiyatu),
                context.getString(R.string.ck_filter_jingmi),
                context.getString(R.string.ck_filter_jiaopian),
                context.getString(R.string.ck_filter_nuanyang),
                context.getString(R.string.ck_filter_jiuri),
                context.getString(R.string.ck_filter_hongchun),
                context.getString(R.string.ck_filter_julandiao),
                context.getString(R.string.ck_filter_tuise),
                context.getString(R.string.ck_filter_heibai),
                context.getString(R.string.ck_filter_wenrou),
                context.getString(R.string.ck_filter_lianaichaotian),
                context.getString(R.string.ck_filter_chujian),
                context.getString(R.string.ck_filter_andiao),
                context.getString(R.string.ck_filter_naicha),
                context.getString(R.string.ck_filter_soft),
                context.getString(R.string.ck_filter_xiyang),
                context.getString(R.string.ck_filter_lengyang),
                context.getString(R.string.ck_filter_haibianrenxiang),
                context.getString(R.string.ck_filter_gaojihui),
                context.getString(R.string.ck_filter_haidao),
                context.getString(R.string.ck_filter_qianxia),
                context.getString(R.string.ck_filter_yese),
                context.getString(R.string.ck_filter_hongzong),
                context.getString(R.string.ck_filter_qingtou),
                context.getString(R.string.ck_filter_ziran2),
                context.getString(R.string.ck_filter_suda),
                context.getString(R.string.ck_filter_jiazhou),
                context.getString(R.string.ck_filter_shise),
                context.getString(R.string.ck_filter_chuanwei),
                context.getString(R.string.ck_filter_meishijiaopian),
                context.getString(R.string.ck_filter_hongsefugu),
                context.getString(R.string.ck_filter_lvtu),
                context.getString(R.string.ck_filter_nuanhuang),
                context.getString(R.string.ck_filter_landiaojiaopian),

        };
        // todo wzz
//        if (mResourceProvider == null) {
//            mResourceProvider = new EffectResourceHelper(context);
//        }
//        File dir = new File(mResourceProvider.getFilterPath());

        File dir = new File(ResourceHelper.getInstance().getFilterPath());
        List<File> mFileList = new ArrayList<>();
        mFileList.add(null);  // normal 正常
        if (dir.exists() && dir.isDirectory()) {
            mFileList.addAll(Arrays.asList(dir.listFiles()));
        }
        Collections.sort(mFileList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                if (file == null)
                    return -1;
                if (t1 == null)
                    return 1;

                String s = "";
                String s1 = "";

                String[] arrays =  file.getName().split("/");
                String[] arrays1 =  t1.getName().split("/");

                arrays = arrays[arrays.length - 1 ].split("_");
                arrays1 = arrays1[arrays1.length - 1 ].split("_");

                s =  arrays[1];
                s1 =  arrays1[1];


                return Integer.valueOf(s) - Integer.valueOf(s1);
            }
        });

        for (int i = 0; i < mFileList.size(); i++) {
            int fileNum = mFileList.get(i) == null ? 0 : Integer.valueOf(mFileList.get(i).getName().split("_")[1]);
            mItems.add(new FilterItem(FILTER_TITLE[fileNum], IMAGES[fileNum], mFileList.get(i) == null ? "" : mFileList.get(i).getName()));
        }
        return mItems;
    }
}
