

## 1.概述

   下载火山引擎demo源码，根据其说明文档，替换授权文件及应用包名。

   下载HiFive音乐sdk。



## 2.接入准备

将音乐sdk的aar包放入libs文件夹下，并在gradle中引入。

gradle中还需引入部分三方库，以保证音乐sdk的正常使用，各版本可视情况自己修改：

```
//HiFive音乐sdk
implementation files('libs/hifivesdk-release.aar')
//以下是音乐sdk运行需要的库
implementation 'io.reactivex.rxjava2:rxjava:2.2.10'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.9.0'

implementation 'com.github.bumptech.glide:glide:4.3.0'
implementation 'com.google.android.material:material:1.3.0'
implementation 'androidx.appcompat:appcompat:1.3.1'

//以下是演示demo中用到的刷新库
implementation  'io.github.scwang90:refresh-layout-kernel:2.0.5' 
implementation  'io.github.scwang90:refresh-header-classics:2.0.5' 
implementation  'io.github.scwang90:refresh-footer-classics:2.0.5' 
```

​	

## 3.演示接入说明

### 3.1 接入音乐SDK入口

火山引擎源码中自带部分本地音频，接入音乐时，以下是接入入口：

```
//::editor-btmpanel
//接入音乐sdk的火山引擎源码类名
com.ss.ugc.android.editor.bottom.handler.impl.AudioHandler

 override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.TYPE_FUNCTION_ADD_AUDIO -> {
                ......
                //这是火山引擎演示代码自己的页面，看可以自己写个页面（fragment/activity),也可在其原页面上修改
                showFragment(AudioFragment())
            }

        }
    }
```



### 3.2 音乐SDK初始化和登录说明

使用音乐SDK时，需要配置相关参数，初始化后才能使用，使用前请参照音乐SDK的说明文档，配置appId、serverCode、clientId等参数进行初始化。

使用音乐SDK需要登录用户账号，可设置为用户ID等用户识别码。

登录接口调用成功后调用歌单列表接口获取歌单并获取收藏歌单的音乐列表数据临时保存。

接口调用步骤：

```
 //第一步，音乐SDK注册，配置参数
 HFOpenApi.setVersion().registerApp()
 //第二步，音乐SDK登录
 HFOpenApi.getInstance().baseLogin()
 //第三部，获取用户歌单列表，并获取收藏歌单ID（歌单type值为2时）
 HFOpenApi.getInstance().memberSheet()
 //第四部，根据收藏歌单ID，获取收藏歌单音乐列表（最大1000条），用来判断显示列表中歌曲是否收藏
HFOpenApi.getInstance().memberSheetMusic
```

第三、四步视需求决定是否使用。

### 3.3 音乐SDK api调用说明

演示代码中api对应功能：

```
//推荐歌单
HFOpenApi.getInstance().sheet()
//推荐音乐
 HFOpenApi.getInstance().baseHot()
//我的收藏使用已保存的歌单列表数据
//添加搜藏
HFOpenApi.getInstance().addMemberSheetMusic()
//取消收藏
HFOpenApi.getInstance().removeMemberSheetMusic()
//搜索歌曲
HFOpenApi.getInstance().searchMusic()
//获取歌单音乐列表
HFOpenApi.getInstance().sheetMusic()
//获取下载地址
HFOpenApi.getInstance().ugcHQListen()
//下载可使用音乐sdk提供的api，也可自己实现
HFOpenApi.getInstance().downLoadFile()
```

音乐SDK各api的参数，请参照音乐SDK的说明文档。