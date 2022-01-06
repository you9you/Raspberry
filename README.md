# Raspberry
Android TV 投屏

## 简介

咕咕咕，咕咕咕咕咕？

## 特性

- 遥控器导航上下键对应进度条显示/隐藏
- 遥控器导航左右键对应后退/快进
- 部分视频无法后退或快进，但可以暂停(例如*央视频*)

## 未来计划

- [ ] 手动设置是否开启opensles、硬解
- [ ] strings.xml
- [ ] 控制端接口处理
- [ ] 手机控制暂停播放
- [ ] 支持调用其他播放器
- bugs:
  - [ ] 有的视频莫名其妙快进几秒(Logcat会显示已有DNS缓存但找不到路由)
  - [ ] 开始播放的前几秒掉帧并无声音(原因：opensles)

## 版本 / 架构

- API 21+
- `armeabi-v7a` `arm64-v8a` `x86` `x86_64`

## 开源许可 / 来源信息

| Repositories                                       | License                                                      |
| -------------------------------------------------- | ------------------------------------------------------------ |
| [ijkplayer](https://github.com/bilibili/ijkplayer) | [GPLv2](https://github.com/bilibili/ijkplayer/blob/master/COPYING.GPLv2) |
| [Cling](https://github.com/4thline/cling)          | [LGPL](http://www.gnu.org/licenses/lgpl-2.1.html)            |

> Icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [www.flaticon.com](http://www.flaticon.com/)

> - [kgplayer](https://github.com/JustForYouT/kgplayer) 
> - [Myijkplayer](https://github.com/979451341/Myijkplayer)
> - [View显示隐藏渐变动画](https://www.jianshu.com/p/d4b54d65fb89)