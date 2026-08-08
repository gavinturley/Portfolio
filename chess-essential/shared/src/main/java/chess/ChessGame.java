package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard gameBoard;
    TeamColor teamTurn;

    public ChessGame() {
        this.gameBoard = new ChessBoard();
        this.gameBoard.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // valid if doesn't leave king in check or move king into check
        if (gameBoard.getPiece(startPosition) == null) {
            return null;
        }

        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece currentPiece = gameBoard.getPiece(startPosition);
        for (ChessMove move : currentPiece.pieceMoves(gameBoard, startPosition)){
            if (!leavesKingInCheck(gameBoard, move)){
                moves.add(move);
            }
        }

        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(startPosition, moves);
        }

        return moves;
    }

    private void addCastlingMoves(ChessPosition kingPosition, Collection<ChessMove> moves) {
        ChessPiece king = gameBoard.getPiece(kingPosition);
        TeamColor color = king.getTeamColor();
        int row = kingPosition.getRow();

        if (isInCheck(color)){
            return;
        }

        boolean kingSideAllowed = (color == TeamColor.WHITE) ? gameBoard.isWhiteKingSideCastle() : gameBoard.isBlackKingSideCastle();
        boolean queenSideAllowed = (color == TeamColor.WHITE) ? gameBoard.isWhiteQueenSideCastle() : gameBoard.isBlackQueenSideCastle();

        if (kingSideAllowed && hasRookAt(row, 8, color) && squaresEmpty(row, 6, 7) && anySquareAttacked(color, row, 6, 7)) {
            moves.add(new ChessMove(kingPosition, new ChessPosition(row, 7), null));
        }
        if (queenSideAllowed && hasRookAt(row, 1, color) && squaresEmpty(row, 2, 3, 4) && anySquareAttacked(color, row, 3, 4)) {
            moves.add(new ChessMove(kingPosition, new ChessPosition(row, 3), null));
        }
    }

    private boolean hasRookAt(int row, int col, TeamColor color) {
        ChessPiece piece = gameBoard.getPiece(new ChessPosition(row, col));
        return piece != null && piece.getPieceType() == ChessPiece.PieceType.ROOK && piece.getTeamColor() == color;
    }

    private boolean squaresEmpty(int row, int... cols){
        for (int col : cols) {
            if (gameBoard.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean anySquareAttacked(TeamColor defendingColor, int row, int... cols) {
        for (int col : cols) {
            if (isSquareAttacked(new ChessPosition(row, col), defendingColor)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSquareAttacked(ChessPosition square, TeamColor defendingColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition tile = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(tile);
                if (piece == null || piece.getTeamColor() == defendingColor) {
                    continue;
                }
                for (ChessMove attackerMove : piece.pieceMoves(gameBoard, tile)) {
                    if (attackerMove.getEndPosition().equals(square)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean leavesKingInCheck(ChessBoard board, ChessMove move){
        // if move happens and king is in check, return false
        ChessGame.TeamColor teamColor = board.getPiece(move.getStartPosition()).getTeamColor();
        ChessBoard tempBoard = board.copy();
        tempBoard.addPiece(move.getStartPosition(), null);
        tempBoard.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));

        return isInCheck(teamColor, tempBoard);
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameBoard.getPiece(move.getStartPosition()) == null){
            throw new InvalidMoveException("No piece in specified location.");
        }

        boolean isValid = false;
        for (ChessMove validMove : validMoves(move.getStartPosition())){
            if (validMove.equals(move)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new InvalidMoveException("Not a valid move.");
        }

        ChessPiece movingPiece = gameBoard.getPiece(move.getStartPosition());
        ChessGame.TeamColor pieceColor = gameBoard.getPiece(move.getStartPosition()).getTeamColor();
        if (teamTurn != pieceColor) {
            throw new InvalidMoveException("Not your turn");
        }

        boolean isEnPassant = movingPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getEndPosition().equals(gameBoard.getEnPassantTarget())
                && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                && gameBoard.getPiece(move.getEndPosition()) == null;

        boolean isCastle = movingPiece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2;

        updateCastlingAllowance(move, movingPiece);

        ChessBoard newBoard = gameBoard.copy();
        newBoard.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() == null){
            newBoard.addPiece(move.getEndPosition(), gameBoard.getPiece(move.getStartPosition()));
        } else {
            ChessPiece promotionPiece = new ChessPiece(pieceColor, move.getPromotionPiece());
            newBoard.addPiece(move.getEndPosition(), promotionPiece);
        }

        if (isEnPassant){
            ChessPosition capturedPawn = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn());
            newBoard.addPiece(capturedPawn, null);
        }

        if (isCastle) {
            int row = move.getStartPosition().getRow();
            boolean kingSide = move.getEndPosition().getColumn() == 7;
            ChessPosition rookStart = new ChessPosition(row, kingSide ? 8 : 1);
            ChessPosition rookEnd = new ChessPosition(row, kingSide ? 6 : 4);
            newBoard.addPiece(rookStart, null);
            newBoard.addPiece(rookEnd, new ChessPiece(pieceColor, ChessPiece.PieceType.ROOK));
        }

        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN
            && Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) == 2) {
            int passedRow = (move.getStartPosition().getRow() + move.getEndPosition().getRow()) / 2;
            newBoard.setEnPassantTarget(new ChessPosition(passedRow, move.getStartPosition().getColumn()));
        } else {
            newBoard.setEnPassantTarget(null);
        }

        setBoard(newBoard);
        setTeamTurn(teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    private void updateCastlingAllowance(ChessMove move, ChessPiece movingPiece){
        ChessPosition start = move.getStartPosition();
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING){
            if (movingPiece.getTeamColor() == TeamColor.WHITE){
                gameBoard.setWhiteKingSideCastle(false);
                gameBoard.setWhiteQueenSideCastle(false);
            } else {
                gameBoard.setBlackKingSideCastle(false);
                gameBoard.setBlackQueenSideCastle(false);
            }
        }

        if (movingPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (start.equals(new ChessPosition(1, 1))) {
                gameBoard.setWhiteQueenSideCastle(false);
            }
            if (start.equals(new ChessPosition(1, 8))) {
                gameBoard.setWhiteKingSideCastle(false);
            }
            if (start.equals(new ChessPosition(8, 1))) {
                gameBoard.setBlackQueenSideCastle(false);
            }
            if (start.equals(new ChessPosition(8, 8))) {
                gameBoard.setBlackKingSideCastle(false);
            }
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, gameBoard);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard board){
        ChessPosition kingPosition = findKing(teamColor, board);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition tile = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = board.getPiece(tile);
                if (piece == null) {
                    continue;
                }
                for (ChessMove move : piece.pieceMoves(board, tile)){
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(TeamColor teamColor, ChessBoard board){
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                ChessPosition tile = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = board.getPiece(tile);
                if (piece == null) {
                    continue;
                }
                if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                    return tile;
                }
            }
        }
        return null;
    }

    private boolean hasNoValidMoves(TeamColor teamColor){
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition tile = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = gameBoard.getPiece(tile);
                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }
                Collection <ChessMove> moves = validMoves(tile);
                if (moves != null && !moves.isEmpty()) {
                    return false;
                }

            }
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //in checkmate if isInCheck() and no valid moves
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if not in check but no valid moves
        return !isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.gameBoard;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) object;
        return Objects.equals(gameBoard, chessGame.gameBoard) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, teamTurn);
    }
}
