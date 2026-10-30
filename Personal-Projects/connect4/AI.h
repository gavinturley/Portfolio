#pragma once

#include <vector>
#include <climits>
#include <cstdint>
#include <unordered_map>
#include <random>

using namespace std;

const int ROWS = 6;
const int COLS = 7;

class AI {
private:
    /* Private variables for number of rows, columns, and the value for an empty cell */
    char emptyCell;
    char aiPiece;
    char humanPiece;

    /* How deep the AI searches */
    int aiDepth;

    /* Zobrist table to get rid of most repeats */
    enum TTFlag { EXACT, LOWER_BOUND, UPPER_BOUND };
    struct TTEntry {
        int score;
        int depth;
        TTFlag flag;
    };
    unordered_map<uint64_t, TTEntry> transpositionTable;
    uint64_t zobristTable[ROWS][COLS][2];

    /* Adds turn into hash calculation */
    uint64_t sideToMoveKey;

    /* Function to initialize Zobrist table */
    void initZobrist(){
        /* Random seed for RNG */        
        mt19937_64 rng(561081837);

        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                zobristTable[i][j][0] = rng();
                zobristTable[i][j][1] = rng();
            }
        }

        sideToMoveKey = rng();
    }

    /* Function to create a hash value for the board */
    uint64_t hashBoard(const vector<vector<char>>& board, bool maximizing) const {
        uint64_t hash = 0;
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                if (board[i][j] == humanPiece){
                    hash ^= zobristTable[i][j][0];
                } else if (board[i][j] == aiPiece){
                    hash ^= zobristTable[i][j][1];
                }
            }
        }
        if (maximizing){
            hash ^= sideToMoveKey;
        }

        return hash;
    }

    /* Returns true if wanted cell is empty */
    bool isValidMove(const vector<vector<char>>& board, int column) const {
        if (column < 0 || column >= COLS) {
            return false;
        }
        return board[0][column] == emptyCell;
    }

    /* Returns true if there are four in a row of a certain piece */
    bool checkForWin(const vector<vector<char>>& board, char piece) const {
        return checkHorizontalWin(board, piece) || checkVerticalWin(board, piece) || checkDiagonalWin(board, piece);
    }

    /* Checks for horizontal wins*/
    bool checkHorizontalWin(const vector<vector<char>>& board, char piece) const {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                if (board[i][j] == piece &&
                    board[i][j + 1] == piece &&
                    board[i][j + 2] == piece &&
                    board[i][j + 3] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    /* Checks for vertical wins */
    bool checkVerticalWin(const vector<vector<char>>& board, char piece) const {
        for (int j = 0; j < COLS; j++) {
            for (int i = 0; i <= ROWS - 4; i++) {
                if (board[i][j] == piece &&
                    board[i + 1][j] == piece &&
                    board[i + 2][j] == piece &&
                    board[i + 3][j] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    /* Checks for diagonal wins */
    bool checkDiagonalWin(const vector<vector<char>>& board, char piece) const {
        /* top left to botton right */
        for (int i = 0; i <= ROWS - 4; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                if (board[i][j] == piece &&
                    board[i + 1][j + 1] == piece &&
                    board[i + 2][j + 2] == piece &&
                    board[i + 3][j + 3] == piece) {
                    return true;
                }
            }
        }

        /* top right to bottom left */
        for (int i = 3; i < ROWS; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                if (board[i][j] == piece &&
                    board[i - 1][j + 1] == piece &&
                    board[i - 2][j + 2] == piece &&
                    board[i - 3][j + 3] == piece) {
                    return true;
                }
            }
        }

        return false;
    }

    /* Returns score based on number of values in a window of four */
    int scoreWindow(int aiCount, int humanCount, int emptyCount) const {
        if (aiCount == 3 && emptyCount == 1){
            return 50;
        } else if (aiCount == 2 && emptyCount == 2){
            return 5;
        } else if (humanCount == 3 && emptyCount == 1){
            return -80;
        } else if (humanCount == 2 && emptyCount == 2){
            return -10;
        }
        return 0;
    }

    /* Returns true if board is full */
    bool checkFullBoard(const vector<vector<char>>& board) const {
        for (int j = 0; j < COLS; j++) {
            if (board[0][j] == emptyCell) {
                return false;
            }
        }
        return true;
    }

    /* Evaluates general score of board (used when deep recursing and testing moves to check overall game position) */
    int evaluateBoard(const vector<vector<char>>& board) const {
        int aiCount = 0, humanCount = 0, emptyCount = 0, score = 0;

        /* Diagonal ↘ */
        for (int i = 0; i <= ROWS - 4; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                for (int k = 0; k < 4; k++){
                    char cell = board[i + k][j + k];
                    if (cell == aiPiece){
                        aiCount++;
                    } else if (cell == humanPiece){
                        humanCount++;
                    } else {
                        emptyCount++;
                    }
                }
                score += scoreWindow(aiCount, humanCount, emptyCount);
                aiCount = humanCount = emptyCount = 0;
            }
        }

        /* Diagonal ↙ */
        for (int i = 3; i < ROWS; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                for (int k = 0; k < 4; k++){
                    char cell = board[i - k][j + k];
                    if (cell == aiPiece){
                        aiCount++;
                    } else if (cell == humanPiece){
                        humanCount++;
                    } else {
                        emptyCount++;
                    }
                }
                score += scoreWindow(aiCount, humanCount, emptyCount);
                aiCount = humanCount = emptyCount = 0;
            }
        }

        /* Vertical */
        for (int j = 0; j < COLS; j++) {
            for (int i = 0; i <= ROWS - 4; i++) {
                for (int k = 0; k < 4; k++){
                    char cell = board[i + k][j];
                    if (cell == aiPiece){
                        aiCount++;
                    } else if (cell == humanPiece){
                        humanCount++;
                    } else {
                        emptyCount++;
                    }
                }
                score += scoreWindow(aiCount, humanCount, emptyCount);
                aiCount = humanCount = emptyCount = 0;
            }
        }

        /* Horizontal */
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j <= COLS - 4; j++) {
                for (int k = 0; k < 4; k++){
                    char cell = board[i][j + k];
                    if (cell == aiPiece){
                        aiCount++;
                    } else if (cell == humanPiece){
                        humanCount++;
                    } else {
                        emptyCount++;
                    }
                }
                score += scoreWindow(aiCount, humanCount, emptyCount);
                aiCount = humanCount = emptyCount = 0;
            }
        }

        /* Adds three for each AI middle piece (cause that means good position) */
        for (int i = 0; i < ROWS; i++){
            if (board[i][COLS / 2] == aiPiece){
                score += 3;
            }
        }

        return score;
    }

    /* Drops piece like normal connect four, returns the row it landed in (-1 if invalid) */
    int dropPiece(vector<vector<char>>& board, int column, char piece) {
        if (!isValidMove(board, column)) {
            return -1;
        }

        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == emptyCell) {
                board[row][column] = piece;
                return row;
            }
        }
        return -1;
    }

    /* Clears the cell at the given row and column */
    void undoPiece(vector<vector<char>>& board, int row, int col) {
        board[row][col] = emptyCell;
    }

    // ---------- minimax function ----------

    /* AI recursion, goes to specific depth and tracks the best score */
    int minimax(vector<vector<char>>& board, int depth, bool maximizing, int alpha, int beta, uint64_t hash) {
        /* Hashes board to avoid repeats */
        auto it = transpositionTable.find(hash);
        if (it != transpositionTable.end()) {
            const TTEntry& entry = it->second;
            if (entry.depth >= depth){
                if (entry.flag == EXACT){
                    return entry.score;
                } else if (entry.flag == LOWER_BOUND){
                    alpha = max(alpha, entry.score);
                } else if (entry.flag == UPPER_BOUND){
                    beta = min(beta, entry.score);
                }
                if (alpha >= beta){
                    return entry.score;
                }
            }
        }

        /* If AI win */
        if (checkForWin(board, aiPiece)) {
            return 100000 + depth;
        }

        /* If human win */
        if (checkForWin(board, humanPiece)) {
            return -100000 - depth;
        }

        /* If full board */
        if (checkFullBoard(board)) {
            return 0;
        }

        /* If 0 depth, evaluate board */
        if (depth == 0) {
            return evaluateBoard(board);
        }

        /* Set variables and save alpha and beta before branches */
        int originalAlpha = alpha;

        int finalScore;

        if (maximizing){
            /* Checks AI move (positive) */
            int bestScore = INT_MIN;

            for (int col = 0; col < COLS; col++){
                if (isValidMove(board, col)){
                    int row = dropPiece(board, col, aiPiece);

                    uint64_t childHash = hash;
                    childHash ^= zobristTable[row][col][1];
                    childHash ^= sideToMoveKey;

                    int score = minimax(board, depth - 1, false, alpha, beta, childHash);

                    undoPiece(board, row, col);

                    /* Update best score and alpha */
                    bestScore = max(bestScore, score);
                    alpha = max(alpha, bestScore);

                    /* Checks if best score for human is worse than the best score for AI */
                    if (beta <= alpha){
                        break;
                    }
                }
            }
            finalScore = bestScore;
        } else {
            /* Checks human move (negative) */
            int worstScore = INT_MAX;

            for (int col = 0; col < COLS; col++){
                if (isValidMove(board, col)){
                    int row = dropPiece(board, col, humanPiece);

                    uint64_t childHash = hash;
                    childHash ^= zobristTable[row][col][0];
                    childHash ^= sideToMoveKey;

                    int score = minimax(board, depth - 1, true, alpha, beta, childHash);

                    undoPiece(board, row, col);

                    /* Update best move for human and beta */
                    worstScore = min(worstScore, score);
                    beta = min(beta, worstScore);

                    /* Checks if best score for human is worse than the best score for AI */
                    if (beta <= alpha){
                        break;
                    }
                }
            }
            finalScore = worstScore;
        }

        TTEntry entry;
        entry.score = finalScore;
        entry.depth = depth;

        /* Saves flag and hash values */
        if (finalScore <= originalAlpha) {
            entry.flag = UPPER_BOUND;
        }
        else if (finalScore >= beta) {
            entry.flag = LOWER_BOUND;
        }
        else {
            entry.flag = EXACT;
        }

        transpositionTable[hash] = entry; 

        return finalScore;
    }

    /* Finds first valid column */
    int getBestCol(const vector<vector<char>>& board) const {
        for (int i = 0; i < COLS; i++)
            if (isValidMove(board, i)){
                return i;
            }
        return -1;
    }

// ---------- public ----------

public:
    /* Initializes AI, aiDepth being AI difficulty */
    AI(char empty, char aiPiece, char humanPiece, int aiDepth) {
        this->emptyCell = empty;
        this->aiPiece = aiPiece;
        this->humanPiece = humanPiece;
        this->aiDepth = aiDepth;

        initZobrist();
    }

    int getBestMove(vector<vector<char>>& board){
        /* Clear transposition table */
        transpositionTable.clear();

        /* Makes sure board isn't full */
        if (checkFullBoard(board)){
            return -1;
        }

        /* Gets starting valid column */
        int bestCol = getBestCol(board);
        uint64_t hash = hashBoard(board, true);

        /* Sets best scores as max values */
        int bestScore = INT_MIN;
        int alpha = INT_MIN;
        int beta = INT_MAX;

        /* Loops through each column */
        for (int col = 0; col < COLS; col++){
            if (isValidMove(board, col)){
                int row = dropPiece(board, col, aiPiece);

                uint64_t childHash = hash;
                childHash ^= zobristTable[row][col][1];  
                childHash ^= sideToMoveKey;

                int score = minimax(board, aiDepth, false, alpha, beta, childHash);
                undoPiece(board, row, col);

                /* Update best score and column */
                if (score > bestScore){
                    bestScore = score;
                    bestCol = col;
                }

                /* Updates best overall score */
                if (bestScore > alpha){
                    alpha = bestScore;
                }

                /* Checks if best score for human is worse than the best score for AI */
                if (beta <= alpha){
                    break;
                }
            }
        }
        return bestCol;
    }
};
