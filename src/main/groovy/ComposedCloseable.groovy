class ComposedCloseable implements Closeable{

    private List<Closeable> closeables
    
    ComposedCloseable(List<Closure<Closeable>> closeablesBuilders) {
        closeables = new ArrayList<>();
        try {
            closeablesBuilders.each {closeables.add(it())}
        } catch (Exception e) {
            close();
            throw e;
        }
    }
    
    @Override
    void close() throws IOException {
        Exception firstThrown = null
        closeables.reverse().each {
            try {
                it.close();
            } catch (Exception e) {
                if (firstThrown == null) {
                    firstThrown = e
                } else {
                    firstThrown.addSuppressed(e)
                }
            }
        }
        if (firstThrown != null) {
            throw firstThrown
        }
    }
}
