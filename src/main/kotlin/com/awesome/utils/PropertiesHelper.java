package com.awesome.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.http.util.TextUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesUtil.java
 *
 * @author Guoxp
 * @desc properties 资源文件解析工具
 * @datatime Apr 7, 2013 3:58:45 PM
 */
public class PropertiesHelper {

    private Properties properties;
    private String propertiestFilePath = "";

    public PropertiesHelper(PsiElement element) {
        PsiFile mFile = PsiFileUtils.getFileByName(element, "local.properties");
        propertiestFilePath = mFile.getVirtualFile().getPath();
        properties = new Properties();
        BufferedReader bufferedReader = null;
        // 使用InPutStream流读取properties文件
        try {
            bufferedReader = new BufferedReader(new FileReader(propertiestFilePath));
            properties.load(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取某个属性
     */
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        return TextUtils.isEmpty(value) ? "" : value;
    }

    /**
     * 获取所有属性，返回一个map,不常用
     * 可以试试props.putAll(t)
     */
    public Map getAllProperty() {
        Map map = new HashMap();
        Enumeration enu = properties.propertyNames();
        while (enu.hasMoreElements()) {
            String key = (String) enu.nextElement();
            String value = properties.getProperty(key);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 在控制台上打印出所有属性，调试时用。
     */
    public void printProperties() {
        properties.list(System.out);
    }

    public String getPluginUser() {
        return getProperty("pluginUser");
    }

    /**
     * 写入properties信息
     */
    public void setProperty(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        try {
            OutputStream fos = new FileOutputStream(new File(propertiestFilePath));
            properties.setProperty(key, value);
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流  
            properties.store(fos, "『comments』Update key：" + key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 