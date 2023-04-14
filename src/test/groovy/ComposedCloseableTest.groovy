import org.junit.jupiter.api.Test

class ComposedCloseableTest {

    @Test
    void test() {
        def branches = ["development", "major.56.x.x"]
        def project = "BIP"
        def repos = ["mw", "mgsf"]
        
        Closure<Closeable> lock = { resource ->
            println "Locking resource"
            return { println "Releasing resource" } as Closeable
        }
        
        List<Closure<Closeable>> stashRepoLocks = [branches, repos].combinations().collect{ b, r -> return {
            println "Creating Stash restriction for branch [${b}] of repo [$r]"
            int restrictionId = (b + r).length()
            return { println "Removing restriction ${restrictionId}" } as Closeable }
        }
        
        ComposedCloseable allLocks = new ComposedCloseable([lock, *stashRepoLocks])
        
        allLocks.withCloseable { println "performing some action" }
    }
}
