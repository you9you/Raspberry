## Raspberry

Android TV 投屏

### 说明

- 遥控器导航上下键对应进度条显示/隐藏
- 遥控器导航左右键对应后退/快进
- 部分视频无法后退或快进，但可以暂停（例如*央视频*）
- 暂不支持手机控制暂停播放（能力有限-_-）
- 一些*View*为`status`，可能会内存溢出（原因同上）
- ~~开始播放的前几秒掉帧并无声音（原因：opensles）~~
- ~~有的视频莫名其妙快进几秒~~
- 优酷部分视频不支持IPv6, 播放时会造成自动快进的假象（Logcat会显示已有DNS缓存但找不到路由）
- `values/strings.xml`暂未修改（下次一定）
- 如有`License`不规范等情况请`Issues`（本人对GitHub等不是特别熟悉）

### 更新计划

- 手动设置是否开启opensles、硬解
- strings.xml
- 控制端接口处理

### Requirement

- Android 5.0+
- `armeabi-v7a` , `arm64-v8a`, `x86` or `x86_64` Architecture

### Build Environment

- Android Studio 4.0
- NDK 21.3.6528147
- Gradle 4.0.0

### Icon

Icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [www.flaticon.com](http://www.flaticon.com/)

### License

|                                                    |                                                              |
| -------------------------------------------------- | ------------------------------------------------------------ |
| [ijkplayer](https://github.com/bilibili/ijkplayer) | [GPLv2](https://github.com/bilibili/ijkplayer/blob/master/COPYING.GPLv2) |
| [Cling](https://github.com/4thline/cling)          | [LGPL](http://www.gnu.org/licenses/lgpl-2.1.html)            |

### Reference

- [kgplayer](https://github.com/JustForYouT/kgplayer)
- [Myijkplayer](https://github.com/979451341/Myijkplayer)
- [View显示隐藏渐变动画](https://www.jianshu.com/p/d4b54d65fb89)