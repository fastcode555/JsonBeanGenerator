package com.awesome.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.gson.JsonObject
import org.apache.commons.httpclient.URI
import toJSON

object HttpApi {

    fun translate(content: String, translateCode: String): String? {
        val api =
            "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=$translateCode&q=$content";
        val uri = URI(api, false, "utf-8")
        val response = HttpClient3.doGet(uri.toString())
        val json = response?.toJSON()
        if (json != null) {
            try {
                val result = ((json as JSONObject).getJSONArray("sentences")[0] as JSONObject).getString("trans")
                return result
            } catch (e: Exception) {
                print(e)
            }
        }
        return null
    }

}