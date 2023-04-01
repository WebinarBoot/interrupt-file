# interrupt-file

致谢：青锋，vue-simple-uploader

 

## vue-simple-uploader介绍

vue-simple-uploader是基于 simple-uploader.js 封装的vue上传插件。它的优点包括且不限于以下几种：

●支持文件、多文件、文件夹上传；支持拖拽文件、文件夹上传

●可暂停、继续上传

●错误处理

●支持“秒传”，通过文件判断服务端是否已存在从而实现“秒传”

●分片上传

●支持进度、预估剩余时间、出错自动重试、重传等操作



### options参数分析：

参考 [simple-uploader.js 配置](https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#配置)。

## 分片大小设置：

如果需要改变分片大小，需要设置2个地方。

1、md5分片大小的计算。

![image.png](https://cdn.nlark.com/yuque/0/2022/png/12591990/1666538179235-d0109617-14af-493a-b2d8-6e8c6df30e84.png)

2、options中设置chunkSize

![image.png](https://cdn.nlark.com/yuque/0/2022/png/12591990/1666538243007-29ac1c05-509d-4b0f-b300-ffad9dd21abe.png)

### 断点续传及秒传的基础是要计算文件的MD5，这是文件的唯一标识，然后服务器根据MD5进行判断，是进行秒传还是断点续传。

在file-added事件之后，就计算MD5，我们最终的目的是将计算出来的MD5加到参数里传给后台，然后继续文件上传的操作，详细的思路步骤是：

1. 把uploader组件的autoStart设为false，即选择文件后不会自动开始上传
2. 先通过 file.pause()暂停文件，然后通过H5的FileReader接口读取文件
3. 将异步读取文件的结果进行MD5，这里我用的加密工具是spark-md5，你可以通过npm install spark-md5 --save来安装，也可以使用其他MD5加密工具。
4. file有个属性是uniqueIdentifier，代表文件唯一标示，我们把计算出来的MD5赋值给这个属性 file.uniqueIdentifier = md5，这就实现了我们最终的目的。
5. 通过file.resume()开始/继续文件上传。

## 秒传及断点续传

在计算完MD5后，我们就能谈断点续传及秒传的概念了。

服务器根据前端传过来的MD5去判断是否可以进行秒传或断点续传：

- a. 服务器发现文件已经完全上传成功，则直接返回**秒传**的标识。
- b. 服务器发现文件上传过分片信息，则返回这些分片信息，告诉前端继续上传，即**断点续传**。

在每次上传过程的最开始，vue-simple-uploader会发送一个get请求，来问服务器我哪些分片已经上传过了，这个请求返回的结果也有几种可能：

- a. 如果是**秒传**，在请求结果中会有相应的标识，比如我这里是skipUpload为true，且返回了url，代表服务器告诉我们这个文件已经有了，我直接把url给你，你不用再传了，这就是**秒传**。
- b. 如果后台返回了分片信息，这是**断点续传**。如图，返回的数据中有个uploaded的字段，代表这些分片是已经上传过的了，插件会自动跳过这些分片的上传。

## 前端做分片检验：checkChunkUploadedByResponse

插件自己是不会判断哪个需要跳过的，在代码中由options中的checkChunkUploadedByResponse控制，它会根据 XHR 响应内容检测每个块是否上传成功了，成功的分片直接跳过上传
你要在这个函数中进行处理，可以跳过的情况下返回true即可。