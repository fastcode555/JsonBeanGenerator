package com.awesome.utils

import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.*
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


object HttpClient3 {

    fun doGet(url: String?): String? {
        var result: String? = null
        // 创建httpClient实例
        val httpClient = HttpClients.createDefault()
        // 设置http连接主机服务超时时间：15000毫秒
        // 先获取连接管理器对象，再获取参数对象,再进行参数的赋值
        // 创建一个Get方法实例对象
        val getMethod = HttpGet(url)
        addHeader(getMethod)
        // 设置get请求超时为60000毫秒
//        getMethod.params.setParameter(HttpMethodParams.SO_TIMEOUT, 60000)
//        getMethod.params.contentCharset = "utf-8"
        // 设置请求重试机制，默认重试次数：3次，参数设置为true，重试机制可用，false相反
//        getMethod.params.setParameter(HttpMethodParams.RETRY_HANDLER, DefaultHttpMethodRetryHandler(3, true))
        try {
            // 执行Get方法
            val response = httpClient.execute(getMethod)
            val statusCode = response.statusLine.statusCode
            // 判断返回码
            if (statusCode != HttpStatus.SC_OK) {
                // 如果状态码返回的不是ok,说明失败了,打印错误信息
                System.err.println("Method faild: " + response.statusLine)
            } else {
                result = EntityUtils.toString(response.entity)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // 释放连接
            getMethod.releaseConnection()
        }
        return result
    }

    ///需要增加随机的请求头
    var index = 0
    private fun addHeader(method: HttpRequestBase) {
        method.addHeader("User-Agent", UserAgents.randomUserAgent(index))
        index++
    }
}
