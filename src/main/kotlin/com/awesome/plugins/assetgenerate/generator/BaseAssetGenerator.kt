package com.awesome.plugins.assetgenerate.generator

import com.intellij.psi.PsiDirectory

abstract class BaseAssetGenerator(mDirectory: PsiDirectory, ignoreDirs: String?, targetDir: String?) {

    abstract fun generate()
}
