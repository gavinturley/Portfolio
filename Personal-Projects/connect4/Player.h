#pragma once

#include <iostream>
#include <string>
using namespace std;

class Player {
private:
    string name;
    char pieceColor;
    int wins;
    int losses;


public:
    Player() {
        name = "Unknown";
        pieceColor = 'X';
        wins = 0;
        losses = 0;
    }

    Player(string n, char p) {
        name = n;
        pieceColor = p;
        wins = 0;
        losses = 0;
    }

    void setName(string n) {
        name = n;
    }

    void setPieceColor(char p) {
        pieceColor = p;
    }

    void addWin() {
        wins++;
    }

    void addLoss(){
        losses++;
    }

    string getName() const {
        return name;
    }

    char getPieceColor() const {
        return pieceColor;
    }

    int getWins() const {
        return wins;
    }

    int getLosses() const {
        return losses;
    }

    void displayPlayer() const {
        cout << "Player: " << name
             << " | Piece: " << pieceColor
             << " | Wins: " << wins 
             << " | Losses: " << losses << endl;
    }

    void clear() {
        name = "Unknown";
        pieceColor = 'X';
        wins = 0;
        losses = 0;
    }
};
