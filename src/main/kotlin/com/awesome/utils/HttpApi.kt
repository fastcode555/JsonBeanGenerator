package com.awesome.utils

import ImageUtil
import com.alibaba.fastjson.JSONObject
import toJSON
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

object HttpApi {

    fun translate(content: String, translateCode: String): String? {
        return googleTranslate(content, translateCode)
    }

    private fun baiduTranslate(content: String, translateCode: String): String? {
        val queryParameters = URLEncoder.encode(content, StandardCharsets.UTF_8.toString())
        val appId = "20221014001391840"
        val key = "2spMBzfXVvNChb44p9zX"
        val salt = "${translateCode}${Date().time}"
        val value = "${appId}${content}${salt}${key}"
        val sign = Md5Util.encode(value)
        val api =
            "http://api.fanyi.baidu.com/api/trans/vip/translate?q=${queryParameters}&from=auto&to=${translateCode}&appid=${appId}&salt=${salt}&sign=${sign}"
        val response = HttpClient3.doGet(api)
        val json = response?.toJSON()
        if (json != null) {
            try {
                val sentences = (json as JSONObject).getJSONArray("trans_result")
                val builder = StringBuilder()
                for (sen in sentences) {
                    val jsonObject = sen as JSONObject
                    val sentence = jsonObject.getString("dst")
                    builder.append(sentence)
                }
                return builder.toString()
            } catch (e: Exception) {
                print(e)
                NotifyUtil.showTipDialog("${e.toString()},${json}", title = "Error Msg")
            }
        }
        return null
    }

    private fun googleTranslate(content: String, translateCode: String): String? {
        val queryParameters = URLEncoder.encode(content, StandardCharsets.UTF_8.toString())
        val api =
            "https://translate.google.com.hk/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=$translateCode&q=$queryParameters"
        val response = HttpClient3.doGet(api)
        val json = response?.toJSON()
        if (json != null) {
            try {
                val sentences = (json as JSONObject).getJSONArray("sentences")
                val builder = StringBuilder()
                for (sen in sentences) {
                    val jsonObject = sen as JSONObject
                    val sentence = jsonObject.getString("trans")
                    builder.append(sentence)
                }
                return builder.toString()
            } catch (e: Exception) {
                print(e)
                NotifyUtil.showTipDialog("${e.toString()}", title = "Error Msg")
            }
        }
        return null
    }


    /**
     *https://docs.tenapi.cn/random/yiyan.html#%E6%8E%A5%E5%8F%A3%E5%9C%B0%E5%9D%80
     * 获取随机的一句话，来自一个免费的api接口
     **/
    fun randomString(): String {
        return HttpClient3.doGet("https://tenapi.cn/v2/yiyan") ?: "There has an error"
    }

}
