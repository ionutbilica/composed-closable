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
        closeables.reverse().each {
            try {
                it.close();
            } catch (Exception ignored) {
                println "Ignored exception during closing closeable [${it}]: [${ignored}]"
            }
        }
    }
}
