# Project H2G
This project has nothing right. 
This project has nothing left. 

### Lock 
| Class Name | Description | Status |
| --- | --- | --- |
| BarDrawingTutor | 这里面的代码已经快要成为意大利面了 | Locked |

### TODO LIST
| Contributor | Class Name | Content | DDL | Status |
| --- | --- | --- | --- | --- |
| Whexy | FrameCreator | ~~参考网上的图片，设计函数，绘制直方图的其他要素~~ | N/A | Abort |
| Whexy | FrameCreator | ~~思考旋转坐标系的方法~~ |  | Abort |
| Whexy | N/A | ~~直接提交Json生成器的代码~~ | N/A | Abort |
| Whexy | SigDraw | ~~添加图片高斯模糊函数，要求风格与StdDraw函数一致，并测试两图片叠加的情况~~ |  | Abort |
| Whexy | N/A | 修改BarFlatUI为Json | N/A | Working |
| Linyun | N/A | 搜集进度条素材，思考切割方式，编写一个继承BarGenerator的类，生成进度条图像 | N/A | Working |
| Linyun | N/A | 编写新的CanvasStyle的Json，尝试美化界面 | N/A | Working |

### Milestone
| Contributor | Class Name | Content | DoneDate | Status |
| --- | --- | --- | --- | --- |
| All members | N/A | 举办第一次组会 | 12.7 | Done |

# Data Structure
| Path | Class Name | Description |
| --- | --- | --- |
| \ | HistogramData | 画布的原始数据，用于存放相对动态的数据 |
| \ | CanvaStyle | 画布的样式，用于存放相对静态的数据 |
| \ | BarBasicSkinStyle | |

# Class
## FrameCreator
基于Reference的HistogramA的单帧生成器

### Update Log
#### Version 2 rev.A (2018.12.13)
1. 完成BarDrawingTutor接口的对接
2. 完成坐标系旋转相关代码

#### Version 1 rev.B+ (2018.12.1)

#### Version 1 rev.B (2018.11.30)
修复Bug
#### Version 1 rev.A (2018.11.29)

## RulerDrawingTutor
自动计算rulerGrade和rulerStep以指导FrameCreator绘制Ruler
| Primary API Name | Parameter | Description |
| --- | --- | --- |
| RulerDrawingTutor() | double[] yValue, int maxRulerGrade | 实例化，传入比例尺和最大刻度数量 |
| setYmaxValue() | double yMaxValue | 指定刻度的最大值 |
| getRulerStep() | | 自适应并计算rulerStep |
| getRulerGrade() | | 自适应并计算rulerGrade |

### Note:自适应只适应增大的情况，不适应缩小的情况


## BarDrawingTutor
指导FrameCreator绘制Bar

### Primary API Reference 
| Primary API Name | Parameter | Description |
| --- | --- | --- |
| BarDrawingTutor() | CanvaStyle c, double[][] rawData, double maxVelocity | 传入参数完成静态初始化(仅需初始化一次), maxVelocity:最大交换速率 |
| | int currentFrame | 实例化并切换到指定帧 |
| hasNext() || 判断当前帧是否还有下一个需要绘制的Bar |
| next() || 切换到下一个需要绘制的Bar |
| getLocation() || 获取Bar的坐标 |
| getBarImg() || 获得Bar的BufferedImage，已经预设好皮肤，透明度和长度 |
其余的方法请查看源码

### InnerClass
| InnerClass | Description |
| --- | --- |
| Interpolator | 生成数值和坐标的插值，调用BarLayoutDesigner生成坐标的插值 |
| BarLayoutDesigner | 队列化处理Bar交换事件，避免多重交换，生成Bar的横坐标 |
| BarSwaper | 利用非线性函数计算和平移Bar的坐标以实现Bar的交换 |
| BarSwapStatus | 含两个BarLocation和交换进度的数据结构 |
| BarLocation | 含Bar ID,所在图层和坐标的数据结构 |

### InnerClass Primary API Reference
| Class Name | API Name | Description |
| --- | --- | --- |
| Interplator | parseBarPattern | 解析BarPattern(在CanvaStyle里定义),分配Bar初始坐标 |
| | interpolateBarValue | 对值插值 |
| | interpolateBarLocation | 调用BarLocationDesigner对坐标插值 |
| | sortAndSwapBar | 优化的冒泡排序并产生交换事件传递给BarLocationDesigner |
| BarLayoutDesigner | swapBars | 设置交换事件 |
| | getTransparency | 根据交换进度计算透明度 |
| | getLayout | 调用BarSwaper获得当前帧所有Bar的坐标 |
| | nextFrame | 切换到下一帧 | 
| | checkWaitingQueue | 检查等待队列并激活等待态的事件 |
| BarSwaper | getDisplace | 计算Bar的偏移(更改插值函数同时需要调整构造器) |
| | getProgress | 获取Bar的交换进度 |
| | getCurrentLocation | 获得Bar的当前坐标(对调用顺序无要求) |


### Update Log
#### Version 2 rev.A (2018.12.9)
1. 重新设计交换事件冲突处理机制
#### Version 1 rev.C (2018.12.7)
#### Version 1 rev.B (2018.12.6)
#### Version 1 rev.A (2018.12.2)


## ThreadManager
线程管理器兼Launcher

| Primary Variable Name | Description |
| --- | --- |
| double[][] rawData | 第一维存储不同的Bar的信息，第二维存储不同帧的信息 |
| ConcurrentLinkedQueue<BufferedImage> buffer | 线程安全的FIFO缓冲区 |

| InnerClass | Description |
| --- | --- |
| ImagePlayer | 由StdDraw的JFrame部分分离出来的ImageBuffered播放器，并且借助Timer运行在其他线程上 |

#### InnerClass Primary API Reference
| API Name | Parameter | Description |
| --- | --- | --- |
| show() | BufferedImage offscreenImage | 显示BufferedImage的内容 |
| ImagePlayer() | ConcurrentLinkedQueue<BufferedImage> buffer, int[] bgSize | 传入FIFO缓冲区(线程安全)，初始化播放器 |

### Update Log
#### Version 1 rev.A (2018.12.8)
1. 完成基本处理流程的设计，基本实现动画化

## CoordProjecter
坐标转换工具

### API Reference
| API Name | Parameter | Description |
| --- | --- | --- |
| CoordProjecter() | SigDraw base, SigDraw img, double xCentreOfImg, double yCentreOfImg  | xCentreOfImg & yCentreOfImg 是img的图片中心在base参考系下base上的坐标 |
| getX() & getY() | double x/y | 将img上某点在img参考系下的坐标转换为在base参考系下base上的坐标 |


## BarGenerator
柱状图生成器抽象类

| SubClass | Description |
| --- | --- |
| BarBasicSkin | 基本样式 |
| ~~BarFlatUISkin~~ | ~~采用扁平化配色方案的测试样式~~ |

| Related Class | Description |
| --- | --- |
| BarBasicSkinStyle | BarBasicSkin的样式表 |
| DynamicLoader | 预读取Json文件，集中创建BarGenerator，降低IO开销 |
### API v2 Reference
| Class | API Name | Parameter | Description |
| --- | --- | --- | --- |
| BarGenerator | BarGenerator() | int[] barSize, double[] scale, boolean rotated | 构造器，传入bar尺寸，比例尺，旋转(true水平，false垂直) |
| | setScale() | double[] scale | 修改baseImg的比例尺，自动识别旋转方向 |
| | getBarChart() | int frame ,String text, double... val | 获取Bar的BufferedImage，传入帧序号，顶端显示文字，Bar对应值（可变长度，应产生叠加效果） |
| DynamicLoader | static{} | | 在静态初始化的时候读入并转化Json文件到内存 |
| | get() | String skinName, int[] barSize, double[] scale, boolean rotated | 创建并返回一个BarGenerator，skinName是Bar的皮肤名，与CanvaStyle里barSkin对应 |
### ~~API v1 Reference~~
1. ~~BarGenerator(int[] barSize, double[] scale) barSize:生成图像的尺寸 scale:柱状图的最小值和最大值~~
2. ~~BufferedImage getBarchart(int frame, double val1, double val2) 生成Bar图像~~
3. ~~void loadConfig(String filename) 载入预先配置文件~~

### Specification
1. 基于BasicSkin的配色方案，直接写成Json文件并由DynamicLoader作为BarBasicSkinStyle读入，同时在DynamicLoader里写入SkinName和实例化的方法
2. BarGenerator需要读入的静态文件需要在DynamicLoader里预读入到内存，降低IO开销
3. 可变参数的参数数量不做要求，带动画效果的BarGenerator可以只考虑一个参数的情况

### Update Log
#### Version 2 rev.A (2018.12.12)
0. 重新修订BarGenerator的设计规范
1. 增加使用扁平化配色方案的BarFlatUISkin样式
2. 通过透明化测试
3. 修复BarBasicSkin存在的一处bug
4. 完成DynamicLoader的设计
5. 完成BarBasicSkinStyle的设计

#### Version 1 rev.B (2018.12.1)

1. 增加了基本样式对json数据的支持
2. 修改了部分抽象类的定义以支持json读取

#### Version 1 rev.A (2018.11.30)
1. 定义抽象类
2. 完成基本样式(BarBasicSkin)的编写



## ConfigLoader

ConfigLoader基于Json库的辅助程序，简化了读取操作

### API Reference
1. ConfigLoader(String fileName) 构造器
2. get...(String path) 读取指定位置的值

### Syntax of path
| 符号 | 含义 | 适用类 |
| --- | --- | --- |
| . | 分隔符 | 无 |
| Digit | List的下标 | JsonArray |
| Token | Map的Key | JsonObject |

#### Example
ConfigLoader cL = new ConfigLoader("facebook.json"); 

String str = cL.getStr("data.1.message");  

Color color = cL.getColor("bar.color1");

### Update Log

#### Version 1 rev.B (2018.12.1)

增加了获取两种特殊类的方法

#### Version 1 rev.A (2018.11.28)



## SigDraw

SigDraw是基于StdDraw魔改的产物

### API Reference
| Extra API Name | Description |
| --- | --- |
| SigDraw() | 构造器 |
| enableAntialiasing() | 启动反锯齿(默认开启) |
| disableAntialiasing() | 关闭反锯齿 |
| getBuffImg() | 获取内存中的图片(BufferedImage类型) |
| getScaledImage() | 获取缩放后的图片(BufferedImage类型) |
| loadImage() | 从本地文件载入图片(基于getImage(),同时废除该函数) |
| setBuffImg() | 重设内存缓存的图片 |
| setScaleUnaltered() | 离散化坐标系 |
| getSubImage() | 截取图像 |
| enableTransparent() | 启动全局alpha通道绘图模式 |

| Modify API Name | Description |
| --- | --- |
| picture() | 修改为只能载入BufferedImage图像 |
| init() | 修改启动流程，优化嵌套渲染的情况 |
| setCanvasSize() | 修改执行流程 |

### Update Log

#### Version 2 (2018.12.12)

1. 精简代码
2. 增加全局alpha通道绘图模式开关

#### Version 1 rev.C (2018.11.30)
1. 移除高分辨率相关函数
2. 增加截取图像函数

#### Version 1 rev.B+ (2018.11.29)

1. 删除旧版本冗余JavaDoc，并新增了一些（冗余的JavaDoc）重要使用提示
2. 在  更  多  代  码  之  间  加  了  空  格
3. 调整了一些函数及参量的名称以符合使用者习惯

#### Version 1 rev.B (2018.11.27)
1. 添加四倍分辨率绘制支持API
2. 添加离散化坐标API
3. 修复BUG

#### Version 1 rev.A (2018.11.23)
1. 修改为动态类型，允许实例化
2. 剥离Event,Swing,双缓冲部分
3. 修改部分函数和启动流程，添加基础函数

## HistogramData
## CanvaStyle
### Update Log

#### Version 1 rev.B (2018.12.1)

1. 修复颜色自定义无效的bug
2. 代码架构优化
3. json模板结构优化，并统一命名规范

#### Version 1 rev.A (2018.11.30)
使用json获取数据
