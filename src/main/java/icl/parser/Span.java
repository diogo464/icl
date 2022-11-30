package icl.parser;

public class Span {
    public final int startLine;
    public final int startColumn;
    public final int endLine;
    public final int endColumn;

    public Span(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public static Span join(Span left, Span right) {
        return new Span(left.startLine, left.startColumn, right.endLine, right.endColumn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + startLine;
        result = prime * result + startColumn;
        result = prime * result + endLine;
        result = prime * result + endColumn;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Span other = (Span) obj;
        if (startLine != other.startLine)
            return false;
        if (startColumn != other.startColumn)
            return false;
        if (endLine != other.endLine)
            return false;
        if (endColumn != other.endColumn)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Span [startLine=" + startLine + ", startColumn=" + startColumn + ", endLine=" + endLine + ", endColumn="
                + endColumn + "]";
    }
}
