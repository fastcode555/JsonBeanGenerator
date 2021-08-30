package com.awesome.utils

import com.awesome.utils.PsiFileUtils
import com.intellij.psi.PsiElement
import org.apache.http.util.TextUtils
import java.io.*
import java.lang.Exception
import java.util.*

/**
 * PropertiesUtil.java
 *
 * @author Guoxp
 * @desc properties 资源文件解析工具
 * @datatime Apr 7, 2013 3:58:45 PM
 */
class PropertiesHelper(element: PsiElement) {
    private var properties: Properties? = null
    private var propertiestFilePath = ""

    /**
     * 获取某个属性
     */
    fun getProperty(key: String?): String? {
        val value = properties?.getProperty(key)
        return if (TextUtils.isEmpty(value)) "" else value
    }

    /**
     * 获取所有属性，返回一个map,不常用
     * 可以试试props.putAll(t)
     */
    val allProperty: Map<*, *>
        get() {
            val map: HashMap<Any?, Any?> = HashMap<Any?, Any?>()
            val enu = properties?.propertyNames()
            while (enu?.hasMoreElements() ?: false) {
                val key = enu?.nextElement() as String
                val value = properties?.getProperty(key)
                map[key] = value
            }
            return map
        }

    /**
     * 在控制台上打印出所有属性，调试时用。
     */
    fun printProperties() {
        properties?.list(System.out)
    }


    /**
     * 写入properties信息
     */
    fun setProperty(key: String, value: String?) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return
        }
        try {
            val fos: OutputStream = FileOutputStream(File(propertiestFilePath))
            properties?.setProperty(key, value)
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流  
            properties?.store(fos, "『comments』Update key：$key")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        val mFile = PsiFileUtils.getFileByName(element, "plugins.properties")
        if (mFile != null && mFile.virtualFile.exists()) {
            propertiestFilePath = mFile!!.virtualFile.path
            properties = Properties()
            var bufferedReader: BufferedReader? = null
            // 使用InPutStream流读取properties文件
            try {
                bufferedReader = BufferedReader(FileReader(propertiestFilePath))
                properties?.load(bufferedReader)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {

        }

    }
}