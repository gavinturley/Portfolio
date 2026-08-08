package ui;

import chess.*;
import static ui.EscapeSequences.*;
import java.util.Collection;

public class BoardPrinter {

    private static final int BOARD_SIZE = 8;

    public static String drawBoard(ChessBoard board, ChessGame.TeamColor perspective, Collection<ChessPosition> highlights) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);

        boolean whitePerspective = perspective != ChessGame.TeamColor.BLACK;

        stringBuilder.append(columnLabel(whitePerspective));

        int startRow = whitePerspective ? BOARD_SIZE : 1;
        int endRow = whitePerspective ? 1 : BOARD_SIZE;
        int rowStep = whitePerspective ? -1 : 1;

        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += rowStep){
            stringBuilder.append(rowLabel(row));

            int startCol = whitePerspective ? 1 : BOARD_SIZE;
            int endCol = whitePerspective ? BOARD_SIZE : 1;
            int colStep = whitePerspective ? 1 : -1;

            for (int col = startCol; whitePerspective ? col <= endCol: col >= endCol; col += colStep){
                ChessPosition here = new ChessPosition(row, col);
                boolean isWhite = (row + col) % 2 == 0;
                String backgroundColor = isWhite ? SET_BG_COLOR_DARK_BROWN : SET_BG_COLOR_LIGHT_BROWN;
                if (highlights != null && highlights.contains(here)) {
                    backgroundColor = SET_BG_COLOR_YELLOW;
                }
                stringBuilder.append(backgroundColor).append(pieceAt(board, row, col)).append(RESET_BG_COLOR);
            }

            stringBuilder.append(rowLabel(row)).append(RESET_TEXT_COLOR).append("\n");
        }

        stringBuilder.append(columnLabel(whitePerspective));
        return stringBuilder.toString();
    }

    private static String pieceAt(ChessBoard board, int row, int col){
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null){
            return EMPTY;
        }

        boolean isTan = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isTan ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        String pieceImg = switch (piece.getPieceType()){
            case KING -> isTan ? WHITE_KING : BLACK_KING;
            case QUEEN -> isTan ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> isTan ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> isTan ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> isTan ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> isTan ? WHITE_PAWN : BLACK_PAWN;
        };

        return color + pieceImg + RESET_TEXT_COLOR;
    }

    private static String columnLabel(boolean whitePerspective){
        StringBuilder stringBuilder = new StringBuilder(EMPTY);
        char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        if (whitePerspective){
            for (char letter : letters){
                stringBuilder.append(" ").append(letter).append(" ");
            }
        } else {
            for (int i = letters.length - 1; i >= 0; i--){
                stringBuilder.append(" ").append(letters[i]).append(" ");
            }
        }

        return stringBuilder.append("\n").toString();
    }

    private static String rowLabel(int row){
        return " " + row + " ";
    }
}