#pragma once

#include <iostream>
#include <vector>
#include <string>
#include <fstream>
using namespace std;

class Connect4 {
private:
    int rows;
    int cols;
    char emptyCell;
    vector<vector<char>> grid;
    char winner;

public:
    Connect4(int r = 6, int c = 7, char empty = '.') {
        rows = r;
        cols = c;
        emptyCell = empty;
        grid.resize(rows, vector<char>(cols, emptyCell));
        winner = emptyCell;
    }

    void createGrid() {
        grid.clear();
        grid.resize(rows, vector<char>(cols, emptyCell));
        winner = emptyCell;
    }

    void reset() {
        createGrid();
    }

    void displayGrid() const {
        cout << endl;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cout << grid[i][j] << " ";
            }
            cout << endl;
        }

        for (int j = 0; j < cols; j++) {
            cout << j << " ";
        }
        cout << endl;
    }

    bool isValidMove(int column) const {
        if (column < 0 || column >= cols) {
            return false;
        }
        return grid[0][column] == emptyCell;
    }

    bool dropPiece(int column, char piece) {
        if (!isValidMove(column)) {
            return false;
        }

        for (int i = rows - 1; i >= 0; i--) {
            if (grid[i][column] == emptyCell) {
                grid[i][column] = piece;
                return true;
            }
        }

        return false;
    }

    char getCell(int row, int col) const {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return '?';
    }

    void setCell(int row, int col, char value) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            grid[row][col] = value;
        }
    }

    int getRows() const {
        return rows;
    }

    int getCols() const {
        return cols;
    }

    char getWinner() const {
        return winner;
    }

    char getEmptyCell() const {
        return emptyCell;
    }

    bool checkForWin(char piece) {
        bool won = checkHorizontalWin(piece) || checkVerticalWin(piece) || checkDiagonalWin(piece);
        if (won) {
            winner = piece;
        }
        return won;
    }

    bool checkHorizontalWin(char piece) const {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= cols - 4; j++) {
                if (grid[i][j] == piece &&
                    grid[i][j + 1] == piece &&
                    grid[i][j + 2] == piece &&
                    grid[i][j + 3] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    bool checkVerticalWin(char piece) const {
        for (int j = 0; j < cols; j++) {
            for (int i = 0; i <= rows - 4; i++) {
                if (grid[i][j] == piece &&
                    grid[i + 1][j] == piece &&
                    grid[i + 2][j] == piece &&
                    grid[i + 3][j] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    bool checkDiagonalWin(char piece) const {
        for (int i = 0; i <= rows - 4; i++) {
            for (int j = 0; j <= cols - 4; j++) {
                if (grid[i][j] == piece &&
                    grid[i + 1][j + 1] == piece &&
                    grid[i + 2][j + 2] == piece &&
                    grid[i + 3][j + 3] == piece) {
                    return true;
                }
            }
        }

        for (int i = 3; i < rows; i++) {
            for (int j = 0; j <= cols - 4; j++) {
                if (grid[i][j] == piece &&
                    grid[i - 1][j + 1] == piece &&
                    grid[i - 2][j + 2] == piece &&
                    grid[i - 3][j + 3] == piece) {
                    return true;
                }
            }
        }

        return false;
    }

    bool checkFullBoard() const {
        for (int j = 0; j < cols; j++) {
            if (grid[0][j] == emptyCell) {
                return false;
            }
        }
        return true;
    }

    void saveGridToFile(const string& filename) const {
        ofstream outFile(filename);

        if (!outFile) {
            cout << "Error opening file for writing.\n";
            return;
        }

        outFile << rows << " " << cols << endl;
        outFile << emptyCell << endl;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                outFile << grid[i][j];
            }
            outFile << endl;
        }

        outFile.close();
        cout << "Grid saved to " << filename << endl;
    }

    void loadGridFromFile(const string& filename) {
        ifstream inFile(filename);

        if (!inFile) {
            cout << "Error opening file for reading.\n";
            return;
        }

        inFile >> rows >> cols;
        inFile >> emptyCell;

        grid.clear();
        grid.resize(rows, vector<char>(cols, emptyCell));

        string line;
        for (int i = 0; i < rows; i++) {
            inFile >> line;
            for (int j = 0; j < cols && j < (int)line.size(); j++) {
                grid[i][j] = line[j];
            }
        }

        winner = emptyCell;
        inFile.close();
        cout << "Grid loaded from " << filename << endl;
    }
};
