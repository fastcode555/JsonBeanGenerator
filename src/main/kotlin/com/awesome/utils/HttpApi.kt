package com.awesome.utils

import com.alibaba.fastjson.JSONObject
import toJSON
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HttpApi {

    fun translate(content: String, translateCode: String): String? {
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
            }
        }
        return null
    }

}
