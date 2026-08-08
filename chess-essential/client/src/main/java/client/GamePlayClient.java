package client;

import chess.*;
import ui.BoardPrinter;
import websocket.messages.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GamePlayClient {
    private final ServerFacade serverFacade;
    private final Repl repl;

    private WebSocketFacade webSocketFacade;
    private ChessGame currentGame;
    private ChessGame.TeamColor playerColor;
    private Integer gameID;
    private boolean gameOver = false;

    public GamePlayClient(ServerFacade serverFacade, Repl repl) {
        this.serverFacade = serverFacade;
        this.repl = repl;
    }

    public void start(int gameID, ChessGame.TeamColor playerColor) {
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.gameOver = false;
        try {
            webSocketFacade = new WebSocketFacade(serverFacade.getServerUrl(), this::handleServerMessage);
            webSocketFacade.connect(repl.getAuthToken(), gameID);
        } catch (ResponseException e) {
            System.out.println("Error: unable to connect - " + e.getMessage());
            repl.setState(State.SIGNED_IN);
        }
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame();
                System.out.print("\n" + redrawChessBoardQuiet());
                repl.printPrompt();
            }
            case NOTIFICATION -> {
                System.out.print("\n" + ((NotificationMessage) message).getMessage());
                repl.printPrompt();
            }
            case ERROR -> {
                System.out.print("\n" + ((ErrorMessage) message).getErrorMessage());
                repl.printPrompt();
            }
        }
    }

    public String eval(String input) {
        try {
            var tokens = input.trim().split("\\s+");
            var command = tokens.length > 0 ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "redraw" -> redrawChessBoard();
                case "leave" -> leave();
                case "move" -> makeMove(params);
                case "resign" -> resign();
                case "highlight" -> highlightLegalMoves(params);
                case "help" -> help();
                default -> "Unknown command.\n" + help();
            };
        } catch (ResponseException exception) {
            return exception.getMessage();
        } catch (Exception exception) {
            return "Error: unable to process command.";
        }
    }

    private String redrawChessBoard() throws ResponseException {
        return redrawChessBoardQuiet();
    }

    private String redrawChessBoardQuiet() {
        if (currentGame ==  null) {
            return "Board not loaded yet.\n";
        }
        ChessGame.TeamColor perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
        return BoardPrinter.drawBoard(currentGame.getBoard(), perspective, null);
    }

    private String leave() throws ResponseException {
        webSocketFacade.leave(repl.getAuthToken(), gameID);
        gameID = null;
        currentGame = null;
        playerColor = null;
        repl.setState(State.SIGNED_IN);
        return repl.getUsername() + " left the game.\n";
    }

    private String makeMove(String... params) throws ResponseException {
        if (gameOver) {
            return "The game is over. No moves can be made.\n";
        }
        if (playerColor == null) {
            return "Observers cannot make moves.\n";
        }
        if (params.length != 2 && params.length != 3) {
            return "Expected move <FROM> <TO> [PROMOTION] (e.g. move e2 e4 or move e7 e8 queen)\n";
        }

        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);

        if (start == null || end == null) {
            return "Invalid square. Use chess notation like b3.\n";
        }

        ChessPiece.PieceType promotion = null;
        if (params.length == 3) {
            promotion = parsePromotion(params[2]);
            if (promotion == null) {
                return "Invalid promotion piece. Use queen, rook, bishop, or knight.\n";
            }
        }

        ChessBoard boardCopy = currentGame.getBoard().copy();
        webSocketFacade.makeMove(repl.getAuthToken(), gameID, new ChessMove(start, end, promotion));
        if (currentGame.getBoard().equals(boardCopy)){
            return "Invalid move, board not changed.\n";
        }

        return "";
    }
    
    private String resign() throws ResponseException {
        if (playerColor == null) {
            return "Observers can't resign.\n";
        }
        if (gameOver) {
            return "The game is already over.\n";
        }
        
        System.out.print("Are you sure you want to resign? (y,n) ");
        String confirm = new java.util.Scanner(System.in).nextLine();
        if (!confirm.trim().equalsIgnoreCase("y")) {
            return "Resignation cancelled.\n";
        }
        webSocketFacade.resign(repl.getAuthToken(), gameID);
        gameOver = true;
        return "You resigned.\n";
    }

    private String highlightLegalMoves(String... params) throws ResponseException {
        if (currentGame == null) {
            return "Board not loaded yet.\n";
        }
        if (params.length != 1) {
            return "Expected: highlight <SQUARE> (e.g. highlight b3\n";
        }

        ChessPosition position = parsePosition(params[0]);
        if (position == null) {
            return "Invalid square. Use chess notation like b3.\n";
        }

        ChessGame.TeamColor perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;

        var validMoves = currentGame.validMoves(position);
        if (validMoves == null || validMoves.isEmpty()) {
            return "No legal moves for that square.\n" + BoardPrinter.drawBoard(currentGame.getBoard(), perspective, null);
        }

        List<ChessPosition> highlights = new ArrayList<>();
        highlights.add(position);
        for (ChessMove move : validMoves) {
            highlights.add(move.getEndPosition());
        }

        return BoardPrinter.drawBoard(currentGame.getBoard(), perspective, highlights);
    }

    private ChessPosition parsePosition(String tile) {
        if (tile.length() != 2) {
            return null;
        }
        char colChar = Character.toLowerCase(tile.charAt(0));
        char rowChar = tile.charAt(1);
        if (isOutOfBounds(colChar, rowChar)){
            return null;
        }
        int col = colChar - 'a' + 1;
        int row = rowChar - '0';
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String text) {
        return switch (text.toLowerCase()) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    private boolean isOutOfBounds(char colChar, char rowChar) {
        return colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8';
    }

    private String help() {
        return """
                Commands:
                  redraw - redraws the chess board
                  leave - removes the user from the game
                  move <FROM> <TO> [PROMOTION] - make a move (e.g. move e2 e4)
                  resign - confirms input then forfeits the game
                  highlight <CHESS PIECE POSITION> - highlights legal moves for a piece (e.g. highlight b3)
                  help - display this help text
                """;
    }
}