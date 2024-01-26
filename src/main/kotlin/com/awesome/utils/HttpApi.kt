package com.awesome.utils

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

}
