package at.kurumi;

/**
 * Describes a log source.
 */
public class Source {

    private final String name;
    private final String path;
    private final String hierarchy;

    public Source(String name, String path, String hierarchy) {
        this.name = name;
        this.path = path;
        this.hierarchy = hierarchy;
    }

    public String getName() {
        return name;
    }

    /**
     * The absolute path to the log file.
     *
     * @return absolute path
     */
    public String getPath() {
        return path;
    }

    /**
     * The hierarchy string describes where the source should be positioned within the sources-tree structure on the
     * client. A closing angle bracket (>) is used as delimiter between hierarchy-levels. If the string is empty,
     * the source will be positioned at the root level.
     *
     * @return the hierarchy description
     */
    public String getHierarchy() {
        return hierarchy;
    }
}
