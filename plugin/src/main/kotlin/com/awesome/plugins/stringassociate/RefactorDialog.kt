package com.awesome.plugins.stringassociate

import com.awesome.utils.regex
import com.awesome.utils.runWriteCmd
import com.awesome.utils.searchFile
import com.awesome.utils.travse
import com.intellij.psi.PsiDirectory
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

//正则匹配src对应的文件内容
const val srcRegex = " src=\".*?\""

//匹配获取到的文件名
const val iconNameRegex = "(?<=import) .*? from \\'\\@\\/.*?\\'"

class RefactorDialog(private val directory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null
    var buttonCancel: JButton? = null
    var btnRefactor: JButton? = null

    init {
        setContentPane(contentPane)
        setModal(true)
        getRootPane().setDefaultButton(buttonCancel)
        buttonCancel!!.addActionListener { dispose() }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        contentPane!!.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        btnRefactor!!.addActionListener { directory.runWriteCmd(::handleRefactorResource) }
    }

    /**
     * 重构资源文件
     **/
    private fun handleRefactorResource() {
        val resFile = directory.searchFile("res/r.ts") ?: return
        val fileContent = resFile.readText()
        val keys = HashMap<String, String>()
        fileContent.regex(iconNameRegex) {
            val results = it.split(" from ")
            val name = results[0].trim()
            var path = results[1].trim()
            path = path.substring(1, path.length - 1)
            keys[path] = name
        }
        directory.travse { file ->
            if (file.extension != "vue") return@travse
            println(file.path)
            var content = file.readText()
            val text = content
            text.regex(srcRegex) {
                val line = it.replace("src=", "").trim()
                val path = line.substring(1, line.length - 1)
                val name = keys[path] ?: return@regex
                content = content.replace(it, " :src=\"R.${name}\"")
            }
            file.writeText(content)
        }
        dispose()
    }

    fun showDialog(): RefactorDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }


}
