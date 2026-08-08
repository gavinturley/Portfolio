package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece.PieceType chessPiece = board.getPiece(myPosition).getPieceType();
        if (chessPiece == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }
        else if (chessPiece == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        }
        else if (chessPiece == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        }
        else if (chessPiece == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }
        else if (chessPiece == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        }
        else if (chessPiece == PieceType.KING) {
            return kingMoves(board, myPosition);
        }
        else {
            return null;
        }
    }

    private boolean checkBounds(ChessPosition position){
        int row = position.getRow();
        int col = position.getColumn();
        return row <= 8 && row >= 1 && col <= 8 && col >= 1;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor pawnColor = board.getPiece(myPosition).getTeamColor();
        int direction;
        int startingRow;
        if (pawnColor == ChessGame.TeamColor.BLACK) {
            direction = -1;
            startingRow = 7;
        }
        else {
            direction = 1;
            startingRow = 2;
        }


        // moving forward
        ChessPosition oneSpaceForward = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (checkBounds(oneSpaceForward)){
            if (board.getPiece(oneSpaceForward) == null) {
                moves.addAll(addPawnMove(board, myPosition, oneSpaceForward, pawnColor));

                //starting double move
                ChessPosition twoSpaceForward = new ChessPosition(oneSpaceForward.getRow() + direction, oneSpaceForward.getColumn());
                if (checkBounds(twoSpaceForward)){
                    if (board.getPiece(twoSpaceForward) == null && startingRow == myPosition.getRow()) {
                        moves.addAll(addPawnMove(board, myPosition, twoSpaceForward, pawnColor));
                    }
                }
            }
        }

        //capturing
        int[] diagonals = {-1, 1};
        for (int diagonal : diagonals){
            ChessPosition diagonalCapture = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + diagonal);
            if (checkBounds(diagonalCapture)) {
                ChessPiece capturePiece = board.getPiece(diagonalCapture);
                if (capturePiece != null && capturePiece.getTeamColor() != pawnColor) {
                    moves.addAll(addPawnMove(board, myPosition, diagonalCapture, pawnColor));
                }
            }
        }

        ChessMove enPassantMove = checkEnPassant(board, myPosition);
        if (enPassantMove != null){
            moves.add(enPassantMove);
        }

        return moves;
    }

    private ChessMove checkEnPassant(ChessBoard board, ChessPosition myPosition){
        ChessPosition target = board.getEnPassantTarget();
        if (target == null) {
            return null;
        }

        ChessGame.TeamColor teamColor = board.getPiece(myPosition).getTeamColor();
        int direction = (teamColor == ChessGame.TeamColor.BLACK) ? -1 : 1;

        int dx = target.getRow() - myPosition.getRow();
        int dy = target.getColumn() - myPosition.getColumn();

        if (dx == direction && Math.abs(dy) == 1){
            return new ChessMove(myPosition, target, null);
        }

        return null;
    }

    private Collection<ChessMove> addPawnMove(ChessBoard board, ChessPosition myPosition, ChessPosition endPosition, ChessGame.TeamColor pawnColor){
        int promotionRow = ChessGame.TeamColor.BLACK == pawnColor ? 1 : 8;
        Collection<ChessMove> moves = new ArrayList<>();

        if (endPosition.getRow() == promotionRow){
            moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(myPosition, endPosition, null));
        }

        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        moves.addAll(bishopMoves(board, myPosition));
        moves.addAll(rookMoves(board, myPosition));
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};
        return bishopRookMoves(board, myPosition, dx, dy);
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        int[] dx = {-1, -1, 1, 1};
        int[] dy = {-1, 1, -1, 1};
        return bishopRookMoves(board, myPosition, dx, dy);
    }

    private Collection<ChessMove> bishopRookMoves(ChessBoard board, ChessPosition myPosition, int[] dx, int[] dy){
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor thisColor = board.getPiece(myPosition).getTeamColor();

        for (int i = 0; i < 4; i++){
            for (int j = 1; j < 8; j++){
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() + (dx[i] * j),myPosition.getColumn() + (dy[i] * j));
                if (!checkBounds(newPosition)) {
                    break;
                }

                ChessPiece pieceAtSquare = board.getPiece(newPosition);
                if (pieceAtSquare == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    continue;
                } else if (thisColor == pieceAtSquare.getTeamColor()) {
                    break;
                } else {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[] dx = {2, 2, -2, -2, 1, -1, 1, -1};
        int[] dy = {1, -1, 1, -1, 2, 2, -2, -2};
        return specificMoves(board, myPosition, dx, dy);
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[] dx = {0, 0, -1, 1, 1, 1, -1, -1};
        int[] dy = {-1, 1, 0, 0, 1, -1, 1, -1};
        return specificMoves(board, myPosition, dx, dy);
    }

    private Collection<ChessMove> specificMoves(ChessBoard board, ChessPosition myPosition, int[] dx, int[] dy) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor thisColor = board.getPiece(myPosition).getTeamColor();

        for (int i = 0; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + dx[i],myPosition.getColumn() + dy[i]);
            if (checkBounds(newPosition)) {
                if (board.getPiece(newPosition) == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    ChessGame.TeamColor thatColor = board.getPiece(newPosition).getTeamColor();
                    if (thisColor != thatColor) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return moves;
    }
}