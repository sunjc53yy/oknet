# oknet
### 介绍 <br />
一个快捷、高效的android kotlin版本的网络请求框架 <br />
1、支持RxJava格式操作返回结果 <br />
2、支持HttpUrlConnection和OkHttp的底层请求框架，还可以很方便扩展为其他的底层请求框架 <br />
3、支持统一model解析，还可以自动公共model的格式 <br />
4、支持Gson、ProtoBuf解析，可以扩展其他的解析方式 <br />
5、支持本地不同的缓存策略， <br />
6、支持https认证 <br />

### 使用
1.初始化
```
fun init(context : Context) {
	//ca证书
	val ca = HttpsCA()
	ca.serverCrtPath = "server.crt"
	//初始化
	OkNet.instance.init(this)
	  .addConverterFactory(NetGsonConverterFactory.create()) //添加响应结果gson解析器
	  .addConverterFactory(NetProtobufConverterFactory.create()) //添加响应结果protobuf解析器
	  .addWorkerFactory(OkHttpWorkerFactory.create()) //添加okhttp作为请求框架
	  .setBaseUrl("https://10.0.2.2:8085") //设置baseurl
	  .setHttpCA(ca) //设置ca证书信息
		//.setBaseParserModel() //设置公共解析model的class
	  //设置全局拦截器，1、用来设置一些公共参数和头部信息 2、全局监听请求的路径(开始-成功/失败) 3、自定义参数加密
	  .setNetInterceptor(object : INetInterceptor.INetInterceptorAdapter() { 
		  override fun onInterceptHeader(url: String, header: MutableMap<String, String>) {
			  header["token"] = "abcdefg"
		  }

		  override fun onInterceptParams(url: String, params: MutableMap<String, Any?>) {
			  params["id"] = "11111"
		  }

		  override fun <T> onRequestStart(request: NetRequest<T>) {
			  Log.d("TAG", "request start url=${request.getUrl()}")
		  }

		  override fun <T> onRequestSuccess(request: NetRequest<T>, response: RawResponse) {
			  Log.d("TAG", "request end url=${request.getUrl()}")
		  }
	  })
}
```
2、具体使用
```
fun get() {
	//设置请求body
	val body = NetRequestBody.body()
			  .param("name", "zhangsan")
			  .param("age", 1)
	//new一个请求对象
	val request = OkNet.newListRequest(People::class.java) //new一个结果为list格式的请求
	  .setUrl("go") //设置接口地址
	  .setBody(body) //设置请求body
	  .setUseBaseParser(true) //是否使用公共model解析
	  .setCache(NetCache.FAIL_AND_GET_CACHE) //设置缓存模式
	request.adapter(RxWorkerAdapterFactory.createObservableAdapter()).subscribe {
	  it.data?.let { list-> //结果解析
		  for (people in list) {
			  Log.d("TAG", "it=${people.name}")
		  }
	  }
	}
}
```
