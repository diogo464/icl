package icl.ast;

public class Location {
	public final int beginLine;
	public final int endLine;
	public final int beginColumn;
	public final int endColumn;

	Location(Token token) {
		this(token.beginLine, token.endLine, token.beginColumn, token.endColumn);
	}

	Location(int beginLine, int endLine, int beginColumn, int endColumn) {
		this.beginLine = beginLine;
		this.endLine = endLine;
		this.beginColumn = beginColumn;
		this.endColumn = endColumn;
	}
}
