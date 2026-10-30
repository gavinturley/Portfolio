#include <SFML/Graphics.hpp>
#include <string>
#include "Player.h"
#include "Connect4.h"
#include "AI.h"
#include <iostream>
#include <filesystem>

#ifdef __APPLE__
    #include <mach-o/dyld.h>
#elif defined(_WIN32)
    #define NOMINMAX        // stops Windows from breaking std::min / std::max in AI.h
    #include <windows.h>
#endif

std::filesystem::path exeDir() {
#ifdef __APPLE__
    char buf[4096];
    uint32_t size = sizeof(buf);
    if (_NSGetExecutablePath(buf, &size) == 0)
        return std::filesystem::canonical(buf).parent_path();
#elif defined(_WIN32)
    char buf[MAX_PATH];
    if (GetModuleFileNameA(nullptr, buf, MAX_PATH) > 0)
        return std::filesystem::path(buf).parent_path();
#endif
    return std::filesystem::current_path();
}

std::string getResourcePath(const std::string& name) {
    auto dir = exeDir();
    auto bundle = dir.parent_path() / "Resources" / name;   // macOS .app
    if (std::filesystem::exists(bundle)) return bundle.string();
    auto beside = dir / name;                               // next to the exe
    if (std::filesystem::exists(beside)) return beside.string();
    return name;
}

// ============================================================
//  Game mode enum
// ============================================================
enum GameMode { MENU, HUMAN_VS_HUMAN, HUMAN_VS_AI };

// ============================================================
//  Helper — check if a mouse click hit a rectangle
// ============================================================
bool buttonClicked(const sf::RectangleShape& btn, sf::Vector2i mousePos) {
    sf::Vector2f pos  = btn.getPosition();
    sf::Vector2f size = btn.getSize();
    return mousePos.x >= pos.x && mousePos.x <= pos.x + size.x &&
           mousePos.y >= pos.y && mousePos.y <= pos.y + size.y;
}

// ============================================================
//  Helper — draw centered text inside a rectangle button
// ============================================================
void drawButton(sf::RenderWindow& window,
                const sf::RectangleShape& btn,
                const std::string& label,
                const sf::Font& font,
                bool hovered)
{
    sf::RectangleShape hover = btn;
    hover.setFillColor(hovered ? sf::Color(70, 130, 220) : sf::Color(40, 90, 170));
    hover.setOutlineThickness(2.f);
    hover.setOutlineColor(sf::Color(120, 180, 255));
    window.draw(hover);

    sf::Text text(font, label, 22);
    text.setFillColor(sf::Color::White);

    sf::FloatRect tb = text.getLocalBounds();
    sf::Vector2f pos = btn.getPosition();
    sf::Vector2f sz  = btn.getSize();
    text.setPosition({
        pos.x + (sz.x - tb.size.x) / 2.f - tb.position.x,
        pos.y + (sz.y - tb.size.y) / 2.f - tb.position.y
    });
    window.draw(text);
}

int main() {
    // --------------------------------------------------------
    //  Window / layout constants
    // --------------------------------------------------------
    constexpr float        CELL_SIZE      = 90.f;
    constexpr float        PADDING        = 7.f;
    constexpr float        BOARD_X        = 55.f;
    constexpr float        BOARD_Y        = 110.f;
    constexpr unsigned int WINDOW_WIDTH   = 750;
    constexpr unsigned int WINDOW_HEIGHT  = 720;

    sf::RenderWindow window(
        sf::VideoMode({WINDOW_WIDTH, WINDOW_HEIGHT}),
        "Connect 4"
    );
    window.setFramerateLimit(60);

    // --------------------------------------------------------
    //  Font — fallback to default if file not found
    // --------------------------------------------------------
    sf::Font font;
    if (!font.openFromFile(getResourcePath("font.ttf"))) {
        std::cerr << "Could not load font.ttf\n";
        return 1;
    }

    // --------------------------------------------------------
    //  Game objects
    // --------------------------------------------------------
    Connect4 game;
    Player player1("Player 1", 'R');
    Player player2("Player 2", 'Y');
    AI ai('.', 'Y', 'R', 8);   // AI plays Yellow against Red

    Player* currentPlayer = &player1;
    GameMode mode  = MENU;
    bool gameOver  = false;
    bool draw      = false;
    bool aiThinking = false;   // flag so AI move fires once per turn

    // --------------------------------------------------------
    //  Menu buttons
    // --------------------------------------------------------
    sf::RectangleShape hvhButton, hvaButton, restartButton;

    hvhButton.setSize({300.f, 60.f});
    hvhButton.setPosition({(WINDOW_WIDTH - 300.f) / 2.f, 300.f});

    hvaButton.setSize({300.f, 60.f});
    hvaButton.setPosition({(WINDOW_WIDTH - 300.f) / 2.f, 390.f});

    restartButton.setSize({200.f, 45.f});
    restartButton.setPosition({(WINDOW_WIDTH - 200.f) / 2.f, 650.f});

    // --------------------------------------------------------
    //  Lambda — reset everything for a new game
    // --------------------------------------------------------
    auto resetGame = [&](GameMode newMode) {
        mode          = newMode;
        gameOver      = false;
        draw          = false;
        aiThinking    = false;
        currentPlayer = &player1;
        game.reset();
    };

    // --------------------------------------------------------
    //  Main loop
    // --------------------------------------------------------
    while (window.isOpen()) {

        sf::Vector2i mousePos = sf::Mouse::getPosition(window);

        // ---- Events ----------------------------------------
        while (auto event = window.pollEvent()) {
            if (event->is<sf::Event::Closed>()) {
                window.close();
            }

            // Global key: Escape → back to menu
            if (const auto* key = event->getIf<sf::Event::KeyPressed>()) {
                if (key->code == sf::Keyboard::Key::Escape) {
                    mode = MENU;
                    resetGame(MENU);
                }
                // R key restarts current mode
                if (key->code == sf::Keyboard::Key::R && mode != MENU) {
                    resetGame(mode);
                }
            }

            if (const auto* mouse = event->getIf<sf::Event::MouseButtonPressed>()) {
                if (mouse->button != sf::Mouse::Button::Left) continue;

                // ---- MENU clicks ---------------------------
                if (mode == MENU) {
                    if (buttonClicked(hvhButton, mousePos)) resetGame(HUMAN_VS_HUMAN);
                    if (buttonClicked(hvaButton,  mousePos)) resetGame(HUMAN_VS_AI);
                }

                // ---- In-game restart button ----------------
                else if (buttonClicked(restartButton, mousePos)) {
                    resetGame(mode);
                }

                // ---- HUMAN_VS_HUMAN ------------------------
                else if (mode == HUMAN_VS_HUMAN && !gameOver && !draw) {
                    float bx = BOARD_X, by = BOARD_Y;
                    bool inX = mousePos.x >= bx && mousePos.x < bx + game.getCols() * CELL_SIZE;
                    bool inY = mousePos.y >= by && mousePos.y < by + game.getRows() * CELL_SIZE;

                    if (inX && inY) {
                        int col = static_cast<int>((mousePos.x - bx) / CELL_SIZE);

                        if (game.dropPiece(col, currentPlayer->getPieceColor())) {
                            if (game.checkForWin(currentPlayer->getPieceColor())) {
                                gameOver = true;
                                currentPlayer->addWin();
                                Player* other = (currentPlayer == &player1) ? &player2 : &player1;
                                other->addLoss();
                            } else if (game.checkFullBoard()) {
                                draw = true;
                            } else {
                                currentPlayer = (currentPlayer == &player1) ? &player2 : &player1;
                            }
                        }
                    }
                }

                // ---- HUMAN_VS_AI — human turn (Player 1 = Red) ----
                else if (mode == HUMAN_VS_AI && !gameOver && !draw && currentPlayer == &player1) {
                    float bx = BOARD_X, by = BOARD_Y;
                    bool inX = mousePos.x >= bx && mousePos.x < bx + game.getCols() * CELL_SIZE;
                    bool inY = mousePos.y >= by && mousePos.y < by + game.getRows() * CELL_SIZE;

                    if (inX && inY) {
                        int col = static_cast<int>((mousePos.x - bx) / CELL_SIZE);

                        if (game.dropPiece(col, player1.getPieceColor())) {
                            if (game.checkForWin(player1.getPieceColor())) {
                                gameOver = true;
                                player1.addWin();
                                player2.addLoss();
                            } else if (game.checkFullBoard()) {
                                draw = true;
                            } else {
                                // Switch to AI turn
                                currentPlayer = &player2;
                                aiThinking    = true;
                            }
                        }
                    }
                }
            }
        }

        // ---- AI move (fires once after human moves) --------
        if (mode == HUMAN_VS_AI && aiThinking && !gameOver && !draw) {
            aiThinking = false;

            // Get the grid from Connect4 and ask AI for best column
            vector<vector<char>> grid(game.getRows(), vector<char>(game.getCols()));
            for (int r = 0; r < game.getRows(); r++)
                for (int c = 0; c < game.getCols(); c++)
                    grid[r][c] = game.getCell(r, c);

            int aiCol = ai.getBestMove(grid);

            if (aiCol >= 0 && game.dropPiece(aiCol, player2.getPieceColor())) {
                if (game.checkForWin(player2.getPieceColor())) {
                    gameOver = true;
                    player2.addWin();
                    player1.addLoss();
                } else if (game.checkFullBoard()) {
                    draw = true;
                } else {
                    currentPlayer = &player1;
                }
            }
        }

        // ====================================================
        //  DRAW
        // ====================================================
        window.clear(sf::Color(18, 18, 28));

        // ---- MENU ------------------------------------------
        if (mode == MENU) {
            sf::Text title(font, "CONNECT 4", 52);
            title.setFillColor(sf::Color(80, 160, 255));
            title.setStyle(sf::Text::Bold);
            sf::FloatRect tb = title.getLocalBounds();
            title.setPosition({(WINDOW_WIDTH - tb.size.x) / 2.f - tb.position.x, 160.f});
            window.draw(title);

            sf::Text sub(font, "Choose a game mode", 20);
            sub.setFillColor(sf::Color(160, 160, 180));
            sf::FloatRect sb = sub.getLocalBounds();
            sub.setPosition({(WINDOW_WIDTH - sb.size.x) / 2.f - sb.position.x, 250.f});
            window.draw(sub);

            bool hvhHover = buttonClicked(hvhButton, mousePos);
            bool hvaHover = buttonClicked(hvaButton,  mousePos);
            drawButton(window, hvhButton, "Human vs Human", font, hvhHover);
            drawButton(window, hvaButton,  "Human vs AI",   font, hvaHover);
        }

        // ---- IN GAME ---------------------------------------
        else {
            // Board background
            sf::RectangleShape boardRect;
            boardRect.setSize({game.getCols() * CELL_SIZE, game.getRows() * CELL_SIZE});
            boardRect.setPosition({BOARD_X, BOARD_Y});
            boardRect.setFillColor(sf::Color(30, 80, 200));
            window.draw(boardRect);

            // Cells
            for (int row = 0; row < game.getRows(); ++row) {
                for (int col = 0; col < game.getCols(); ++col) {
                    float x = BOARD_X + col * CELL_SIZE + PADDING;
                    float y = BOARD_Y + row * CELL_SIZE + PADDING;

                    sf::CircleShape piece(CELL_SIZE / 2.f - PADDING);
                    piece.setPosition({x, y});

                    char cell = game.getCell(row, col);
                    if      (cell == 'R') piece.setFillColor(sf::Color(220, 50,  50));
                    else if (cell == 'Y') piece.setFillColor(sf::Color(240, 210, 40));
                    else                  piece.setFillColor(sf::Color(20,  20,  35));

                    window.draw(piece);
                }
            }

            // Hover preview (human turn only)
            bool humanTurn = (mode == HUMAN_VS_HUMAN) ||
                             (mode == HUMAN_VS_AI && currentPlayer == &player1);

            if (!gameOver && !draw && humanTurn) {
                float bx = BOARD_X;
                if (mousePos.x >= bx && mousePos.x < bx + game.getCols() * CELL_SIZE) {
                    int hoverCol = static_cast<int>((mousePos.x - bx) / CELL_SIZE);
                    sf::CircleShape preview(CELL_SIZE / 2.f - PADDING);
                    preview.setPosition({bx + hoverCol * CELL_SIZE + PADDING, 15.f});

                    sf::Color col = (currentPlayer->getPieceColor() == 'R')
                        ? sf::Color(220, 50, 50, 160)
                        : sf::Color(240, 210, 40, 160);
                    preview.setFillColor(col);
                    window.draw(preview);
                }
            }

            // Status text
            std::string statusStr;
            if (gameOver) {
                statusStr = (mode == HUMAN_VS_AI && currentPlayer == &player2)
                    ? "AI wins!  Press R to restart"
                    : currentPlayer->getName() + " wins!  Press R to restart";
            } else if (draw) {
                statusStr = "Draw!  Press R to restart";
            } else if (mode == HUMAN_VS_AI && currentPlayer == &player2) {
                statusStr = "AI is thinking...";
            } else {
                statusStr = currentPlayer->getName() + "'s turn";
            }

            sf::Text status(font, statusStr, 22);
            status.setFillColor(sf::Color(200, 200, 220));
            sf::FloatRect stb = status.getLocalBounds();
            status.setPosition({(WINDOW_WIDTH - stb.size.x) / 2.f - stb.position.x, 68.f});
            window.draw(status);

            // Win/loss counters
            auto drawStats = [&](const Player& p, float x, float y) {
                std::string s = p.getName() + "  W:" + std::to_string(p.getWins())
                              + "  L:" + std::to_string(p.getLosses());
                sf::Text t(font, s, 17);
                t.setFillColor(p.getPieceColor() == 'R'
                    ? sf::Color(220, 80, 80) : sf::Color(240, 210, 40));
                t.setPosition({x, y});
                window.draw(t);
            };
            drawStats(player1, BOARD_X, BOARD_Y + game.getRows() * CELL_SIZE + 12.f);
            drawStats(player2, BOARD_X + 360.f, BOARD_Y + game.getRows() * CELL_SIZE + 12.f);

            // Restart button
            bool rHover = buttonClicked(restartButton, mousePos);
            drawButton(window, restartButton, "Restart  (R)", font, rHover);

            // ESC hint
            sf::Text esc(font, "ESC = menu", 15);
            esc.setFillColor(sf::Color(90, 90, 110));
            esc.setPosition({BOARD_X, 82.f});
            window.draw(esc);
        }

        window.display();
    }

    return 0;
}
