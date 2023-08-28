package com.awesome.plugins.json2bean.utils

import com.awesome.plugins.json2bean.generators.DartJsonGenerator
import com.awesome.plugins.json2bean.generators.PythonJsonGenerator
import com.awesome.plugins.json2bean.generators.TsJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtFastJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtGsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.MapKtJsonGenerator
import com.intellij.psi.PsiDirectory

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
        primaryKey: String,
        depType: String,
        psiDirectory: PsiDirectory,
        needClone: Boolean,
    ): String {
        if (fileType == ".dart") {
            return DartJsonGenerator(
                content,
                className,
                extendName,
                impName,
                isSqliteEnable,
                primaryKey,
                needClone,
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
            needClone,
        ).toString()
    }

    fun json2KtOrJava(
        fileType: String,
        fileName: String,
        content: String,
        extendName: String,
        impName: String,
        depType: String,
        psiDirectory: PsiDirectory
    ) {
        if (fileType == ".kt") {
            if (depType == "none") {
                MapKtJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "gson") {
                KtGsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "fastjson") {
                KtFastJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            }
        } else if (fileType == ".java") {

        }
    }

}
