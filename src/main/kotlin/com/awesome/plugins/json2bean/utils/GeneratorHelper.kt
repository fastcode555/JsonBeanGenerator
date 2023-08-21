package com.awesome.plugins.json2bean.utils

import com.awesome.plugins.json2bean.generators.DartJsonGenerator
import com.awesome.plugins.json2bean.generators.PythonJsonGenerator
import com.awesome.plugins.json2bean.generators.TsJsonGenerator

object GeneratorHelper {

    /**
     * json转成bean的方法
     **/
    fun json2Bean(
        fileType: String, content: String,
        className: String,
        extendName: String,
        impName: String,
        isSqliteEnable: Boolean,
        primaryKey: String
    ): String {
        if (fileType == ".dart") {
            return DartJsonGenerator(
                content,
                className,
                extendName,
                impName,
                isSqliteEnable,
                primaryKey,
            ).toString()
        } else if (fileType == ".ts") {
            return TsJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        } else if (fileType == ".py") {
            return PythonJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        }
        return DartJsonGenerator(
            content,
            className,
            extendName,
            impName,
            isSqliteEnable,
            primaryKey,
        ).toString()
    }
}
