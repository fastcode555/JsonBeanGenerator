import com.intellij.psi.PsiDirectory;

/**
 * 仅供调试 runner 使用。Kotlin 的 `null as PsiDirectory` 在运行时会做 null 检查，
 * 这里通过 Java 的 platform type 绕过：Kotlin 拿到的是 `PsiDirectory!`，赋给非空
 * 字段不触发检查，只要后续不解引用就不抛。Kt 三件套的 toString() 路径不解引用
 * psiDir，所以可以用 null 占位。
 */
public final class TestHelpers {
    private TestHelpers() {}

    public static PsiDirectory nullPsiDir() {
        return null;
    }
}
