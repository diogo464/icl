package icl.ast;

public class AnnotationKey<T> {
    private final String key;

    public AnnotationKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AnnotationKey))
            return false;
        @SuppressWarnings("unchecked")
        var other = (AnnotationKey<T>) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AnnotationKey [key=" + key + "]";
    }
}
