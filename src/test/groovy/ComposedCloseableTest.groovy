import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class ComposedCloseableTest {

    private static final String CLOSEABLE_EXCEPTION_MESSAGE = "Exception on closeable"
    private static final String EXCEPTION_ON_ACTION_MESSAGE = "Exception on action"

    private List<Boolean> closeablesOpened
    private List<Boolean> closeablesClosed
    private boolean actionPerformed

    private Closure action = { actionPerformed = true}
    
    private final opensOkClosesOk0 = opensOkClosesOk(0)
    private final opensOkClosesOk1 = opensOkClosesOk(1)

    private final failsOnOpen = { throw new RuntimeException(CLOSEABLE_EXCEPTION_MESSAGE) }
    
    private final opensOkButFailsOnClose = { closeablesOpened[1] = true
        return { throw new RuntimeException(CLOSEABLE_EXCEPTION_MESSAGE) } as Closeable
    }

    private opensOkClosesOk(int index) {
        return { closeablesOpened[index] = true
            return { closeablesClosed[index] = true } as Closeable
        }
    }
    
    @BeforeEach
    void beforeEach() {
        closeablesOpened = [false, false]
        closeablesClosed = [false, false]
        actionPerformed = false
    }

    @Test
    void testSuccess() {
        new ComposedCloseable([opensOkClosesOk0, opensOkClosesOk1]).withCloseable(action)

        assertEquals([true, true], closeablesOpened)
        assertEquals([true, true], closeablesClosed)
        assertTrue(actionPerformed)
    }

    @Test
    void testFailAtOpen() {
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new ComposedCloseable([opensOkClosesOk0, failsOnOpen]).withCloseable(action))
        assertEquals(CLOSEABLE_EXCEPTION_MESSAGE, thrown.getMessage())

        assertEquals([true, false], closeablesOpened)
        assertEquals([true, false], closeablesClosed)
        assertFalse(actionPerformed)
    }

    @Test
    void testFailAtClose() {
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new ComposedCloseable([opensOkClosesOk0, opensOkButFailsOnClose]).withCloseable(action))
        assertEquals(CLOSEABLE_EXCEPTION_MESSAGE, thrown.getMessage())

        assertEquals([true, true], closeablesOpened)
        assertEquals([true, false], closeablesClosed)
        assertTrue(actionPerformed)
    }

    @Test
    void testFailAtActionAndClose() {
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new ComposedCloseable([opensOkClosesOk0, opensOkButFailsOnClose]).withCloseable {throw new RuntimeException(EXCEPTION_ON_ACTION_MESSAGE)})
        assertEquals(EXCEPTION_ON_ACTION_MESSAGE, thrown.getMessage())
        assertEquals(1, thrown.getSuppressed().length)

        assertEquals([true, true], closeablesOpened)
        assertEquals([true, false], closeablesClosed)
    }
}
