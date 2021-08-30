package com.awesome.utils

import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.params.HttpMethodParams
import java.io.*
import java.nio.charset.Charset

object HttpClient3 {
    fun doGet(url: String?): String? {

        // 输入流
        var `is`: InputStream? = null
        var br: BufferedReader? = null
        var result: String? = null
        // 创建httpClient实例
        val httpClient = HttpClient()
        // 设置http连接主机服务超时时间：15000毫秒
        // 先获取连接管理器对象，再获取参数对象,再进行参数的赋值
        httpClient.httpConnectionManager.params.connectionTimeout = 15000
        // 创建一个Get方法实例对象
        val getMethod = GetMethod(url)
        // 设置get请求超时为60000毫秒
        getMethod.params.setParameter(HttpMethodParams.SO_TIMEOUT, 60000)
        // 设置请求重试机制，默认重试次数：3次，参数设置为true，重试机制可用，false相反
        getMethod.params.setParameter(HttpMethodParams.RETRY_HANDLER, DefaultHttpMethodRetryHandler(3, true))
        try {
            // 执行Get方法
            val statusCode = httpClient.executeMethod(getMethod)
            // 判断返回码
            if (statusCode != HttpStatus.SC_OK) {
                // 如果状态码返回的不是ok,说明失败了,打印错误信息
                System.err.println("Method faild: " + getMethod.statusLine)
            } else {
                // 通过getMethod实例，获取远程的一个输入流
                `is` = getMethod.responseBodyAsStream
                // 包装输入流
                br = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                val sbf = StringBuffer()
                // 读取封装的输入流
                var temp: String? = null
                while (br.readLine().also { temp = it } != null) {
                    sbf.append(temp).append("\r\n")
                }
                result = sbf.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (null != `is`) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            // 释放连接
            getMethod.releaseConnection()
        }
        return result
    }

    fun doPost(url: String?, paramMap: Map<String, Any>?): String? {
        // 获取输入流
        var `is`: InputStream? = null
        var br: BufferedReader? = null
        var result: String? = null
        // 创建httpClient实例对象
        val httpClient = HttpClient()
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.httpConnectionManager.params.connectionTimeout = 15000
        // 创建post请求方法实例对象
        val postMethod = PostMethod(url)
        // 设置post请求超时时间
        postMethod.params.setParameter(HttpMethodParams.SO_TIMEOUT, 60000)
        var nvp: Array<NameValuePair?>? = null
        // 判断参数map集合paramMap是否为空
        if (null != paramMap && paramMap.size > 0) { // 不为空
            // 创建键值参数对象数组，大小为参数的个数
            nvp = arrayOfNulls(paramMap.size)
            // 循环遍历参数集合map
            val entrySet = paramMap.entries
            // 获取迭代器
            val iterator = entrySet.iterator()
            var index = 0
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                // 从mapEntry中获取key和value创建键值对象存放到数组中
                try {
                    nvp[index] = NameValuePair(
                        key,
                        String(value.toString().toByteArray(charset("UTF-8")), Charset.defaultCharset())
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                index++
            }
        }
        // 判断nvp数组是否为空
        if (null != nvp && nvp.size > 0) {
            // 将参数存放到requestBody对象中
            postMethod.setRequestBody(nvp)
        }
        // 执行POST方法
        try {
            val statusCode = httpClient.executeMethod(postMethod)
            // 判断是否成功
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method faild: " + postMethod.statusLine)
            }
            // 获取远程返回的数据
            `is` = postMethod.responseBodyAsStream
            // 封装输入流
            br = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            val sbf = StringBuffer()
            var temp: String? = null
            while (br.readLine().also { temp = it } != null) {
                sbf.append(temp).append("\r\n")
            }
            result = sbf.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (null != `is`) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            // 释放连接
            postMethod.releaseConnection()
        }
        return result
    }
}