package chess;

import java.util.Arrays;
import java.util.Objects;

//done
/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] chessBoard;
    private ChessPosition enPassantTarget;
    private boolean whiteKingSideCastle = true;
    private boolean whiteQueenSideCastle = true;
    private boolean blackKingSideCastle = true;
    private boolean blackQueenSideCastle = true;

    public ChessBoard() {
        chessBoard = new ChessPiece[8][8];
        this.enPassantTarget = null;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        chessBoard[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return chessBoard[position.getRow() - 1][position.getColumn() - 1];
    }

    public ChessPosition getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(ChessPosition target){
        this.enPassantTarget = target;
    }

    public boolean isWhiteKingSideCastle() {
        return whiteKingSideCastle;
    }

    public void setWhiteKingSideCastle(boolean whiteKingSideCastle) {
        this.whiteKingSideCastle = whiteKingSideCastle;
    }

    public boolean isWhiteQueenSideCastle() {
        return whiteQueenSideCastle;
    }

    public void setWhiteQueenSideCastle(boolean whiteQueenSideCastle) {
        this.whiteQueenSideCastle = whiteQueenSideCastle;
    }

    public boolean isBlackKingSideCastle() {
        return blackKingSideCastle;
    }

    public void setBlackKingSideCastle(boolean blackKingSideCastle) {
        this.blackKingSideCastle = blackKingSideCastle;
    }

    public boolean isBlackQueenSideCastle() {
        return blackQueenSideCastle;
    }

    public void setBlackQueenSideCastle(boolean blackQueenSideCastle) {
        this.blackQueenSideCastle = blackQueenSideCastle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(chessBoard, that.chessBoard);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(chessBoard);
    }

    public ChessBoard copy(){
        ChessBoard newBoard = new ChessBoard();
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                newBoard.addPiece(position, chessBoard[row][col]);
            }
        }
        newBoard.setEnPassantTarget(this.enPassantTarget);
        newBoard.setBlackKingSideCastle(this.blackKingSideCastle);
        newBoard.setBlackQueenSideCastle(this.blackQueenSideCastle);
        newBoard.setWhiteKingSideCastle(this.whiteKingSideCastle);
        newBoard.setWhiteQueenSideCastle(this.whiteQueenSideCastle);

        return newBoard;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] backRank = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoard[row][col] = null;
            }
        }

        for (int col = 0; col < 8; col++) {
            chessBoard[0][col] = new ChessPiece(ChessGame.TeamColor.WHITE, backRank[col]);
            chessBoard[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            chessBoard[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            chessBoard[7][col] = new ChessPiece(ChessGame.TeamColor.BLACK, backRank[col]);
        }

        this.enPassantTarget = null;
        this.blackKingSideCastle = true;
        this.blackQueenSideCastle = true;
        this.whiteKingSideCastle = true;
        this.whiteQueenSideCastle = true;
    }
}
