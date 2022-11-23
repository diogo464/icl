package icl.hir;

public class Location {
	public final int beginLine;
	public final int endLine;
	public final int beginColumn;
	public final int endColumn;

	public Location() {
		this(0, 0, 0, 0);
	}

	public Location(int beginLine, int endLine, int beginColumn, int endColumn) {
		this.beginLine = beginLine;
		this.endLine = endLine;
		this.beginColumn = beginColumn;
		this.endColumn = endColumn;
	}

}
