package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int row;
    private int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public ChessPosition(int row, char col) {
        this.row = row;
        this.col = columnCharToInt(col);
    }

    public ChessPosition(String position) {
        
    }

    public static ChessPosition fromString(String position) throws IllegalArgumentException {
        var parts = position.split("");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
        var col = columnCharToInt(parts[0].charAt(0));
        var row = Integer.parseInt(parts[1]);
        return new ChessPosition(row, col);
    }

    private static int columnCharToInt(char col) {
        return col - 'a' + 1;
    }

    /**
     * @return which row this position is in
     *         1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     *         1 codes for the left row
     */
    public int getColumn() {
        return this.col;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + row;
        result = prime * result + col;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChessPosition other = (ChessPosition) obj;
        if (row != other.row) {
            return false;
        }
        if (col != other.col) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChessPosition [row=" + row + ", col=" + col + "]";
    }
}
