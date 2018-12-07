# Project H2G
This project has nothing right. 
This project has nothing left. 

### Lock 
| Class Name | Description | Status |
| --- | --- | --- |
| FrameCreator | 不要修改plotKeys和plotBars部分 | Locked |

### TODO LIST
| Contributor | Class Name | Content | DDL | Status |
| --- | --- | --- | --- | --- |
| Whexy | CanvasStyle | 完成loadConfig()，从Json从读取配置文件 | 12.2 | Done |
| Whexy | BarBasicSkin | 完成loadConfig()，从Json从读取配置文件 | 12.2 | Done |
| Whexy | FrameCreator | 参考网上的图片，设计函数，绘制直方图的其他要素 | N/A | Working |
| Linyun | N/A | 搜集进度条素材，思考切割方式，编写一个继承BarGenerator的类，生成进度条图像 | N/A | Working |
| Linyun | N/A | 编写新的CanvasStyle的Json，尝试美化界面 | N/A | Not Available |
| Whexy | SigDraw | 添加图片全局半透明函数，要求风格与StdDraw函数一致，并测试两图片叠加的情况 | 12.5 | Working |
| Whexy | FrameCreator | 思考旋转坐标系的方法 | 12.8 | Working |
| Whexy | N/A | 直接提交Json生成器的代码 | N/A | Working |
| Whexy | SigDraw | 添加图片高斯模糊函数，要求风格与StdDraw函数一致，并测试两图片叠加的情况 | 12.10 | Working |

## FrameCreator
基于Reference的HistogramA的单帧生成器

### Update Log
#### Version 1 rev.B+ (2018.12.1)

#### Version 1 rev.B (2018.11.30)

修复Bug
#### Version 1 rev.A (2018.11.29)



## HistogramData

画布的原始数据 （存放相对动态的数据）



## CanvaStyle

画布的样式（存放相对静态的数据）

### Update Log

#### Version 1 rev.B (2018.12.1)

1. 修复颜色自定义无效的bug
2. 代码架构优化
3. json模板结构优化，并统一命名规范

#### Version 1 rev.A (2018.11.30)
使用json获取数据



## BarDrawingTutor
生成插值图像

### Note
本类线程非安全

### API Reference 
| API Name | Description |
| --- | --- |
| hasNext() | 判断当前帧是否还有下一个需要绘制的Bar |
| next() | 切换到下一个需要绘制的Bar |
| getLocation() | 获取Bar的坐标 |
| getTransparency() | 获得Bar的透明度 |
| getBarID() | 获得Bar的ID |
| getValue() | 获得Bar的值 |


### InnerClass
| InnerClass | Description |
| --- | --- |
| Interpolator | 生成数值的插值，调用BarLayoutDesigner生成坐标的插值 |
| BarLayoutDesigner | 队列化处理Bar交换事件，避免多重交换，生成Bar的横坐标 |
| BarSwaper | 利用非线性函数计算和平移Bar的坐标以实现Bar的交换 |
| BarSwapStatus | 含两个BarLocation和交换进度的数据结构 |
| BarLocation | 含Bar ID,所在图层和坐标的数据结构 |

### Update Log
#### Version 1 rev.C (2018.12.7)
#### Version 1 rev.B (2018.12.6)
#### Version 1 rev.A (2018.12.2)


## ThreadManager (Incomplete)

线程管理器



## CoordProjecter

坐标转换工具

### API Reference

1. CoordProjecter(SigDraw base, SigDraw img, double xCentreOfImg, double yCentreOfImg)
xCentreOfImg & yCentreOfImg 是img的图片中心在base参考系下base上的坐标
2. getX(double x) & getY(double y)
将img上某点在img参考系下的坐标转换为在base参考系下base上的坐标



## BarGenerator

柱状图生成器抽象类

### SubClass
| SubClass | Description |
| --- | --- |
| BarBasicSkin | 基本样式 |

### API Reference
1. BarGenerator(int[] barSize, double[] scale) barSize:生成图像的尺寸 scale:柱状图的最小值和最大值
2. BufferedImage getBarchart(int frame, double val1, double val2) 生成Bar图像
3. void loadConfig(String filename) 载入预先配置文件

### Update Log

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
##### Extra API
1. SigDraw() 构造器
2. enableAntialiasing() 启动反锯齿(默认开启)
3. disableAntialiasing() 关闭反锯齿
4. getBuffImg() 获取内存中的图片(BufferedImage类型)
5. getScaledImage() 获取缩放后的图片(BufferedImage类型)
6. loadImage() 从本地文件载入图片(基于getImage(),同时废除该函数)
7. setBuffImg() 重设内存缓存的图片
8. setScaleUnaltered() 离散化坐标系
9. getSubImage() 截取图像

##### Modified API
1. picture() 修改为只能载入BufferedImage图像
2. init() 修改启动流程，优化嵌套渲染的情况
3. setCanvasSize() 修改执行流程

### Update Log

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



### Peformance Caution

单线程缩放速度参考(Core i7 3630QM)  

（AA: 反锯齿 HR: 2k分辨率缩放）  

| 模式 | 帧率(FPS) |
| --- | --- |
| AA_HR_UQ | 1.488316713796696 |
| AA_HR_Q | 10.463899546564353 |
| AA_HR_AUTO | 23.059185242121448 |
| AA_HR_SPD | 37.59398496240601 |
| AA_HR_BL | 23.752969121140143 |
| AA_UQ | 5.512679162072767 |
| AA_Q | 25.40220152413209 |
| AA_AUTO | 24.97918401332223 |
| AA_SPD | 44.11764705882353 |
| AA_BL | 37.40648379052369 |

单线程大压力缩放速度参考2(Core i7 8750H 节电模式) 

| 模式               | 帧率(FPS)          |
| ------------------ | ------------------ |
| AA_HR_UQ | 0.7746533426291735 |
|AA_HR_Q|4.365541327124563|
|AA_HR_AUTO| 26.619343389529725|
|AA_HR_SPD |29.615004935834158|
|AA_HR_BL| 13.071895424836601|
|AA_UQ| 2.1216407355021216|
|AA_Q| 13.507429085997298|
|AA_AUTO| 37.08281829419035|
|AA_SPD| 43.10344827586207|
|AA_BL |33.63228699551569 |