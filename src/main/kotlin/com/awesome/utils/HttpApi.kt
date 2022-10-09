package com.awesome.utils

import com.alibaba.fastjson.JSONObject
import org.apache.commons.httpclient.URI
import toJSON

object HttpApi {

    fun translate(content: String, translateCode: String): String? {
        val api =
            "https://translate.google.com.hk/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=$translateCode&q=$content";
        val uri = URI(api, false, "utf-8")
        val response = HttpClient3.doGet(uri.toString())
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