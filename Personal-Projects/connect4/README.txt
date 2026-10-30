Connect 4 with Minimax AI (C++ / SFML 3)

Files:
  main.cpp     - SFML window, menu, game loop (Human vs Human / Human vs AI)
  Connect4.h   - board + win detection
  Player.h     - player name, piece, win/loss tracking
  AI.h         - minimax + alpha-beta + Zobrist transposition table

Build (example, adjust for your SFML install):
```
g++ -std=c++17 main.cpp -o connect4 \                    
  -I/opt/homebrew/include -L/opt/homebrew/lib \
  -lsfml-graphics -lsfml-window -lsfml-system
./connect4
```

Put a font file named arial.ttf next to the executable (or change the
filename in main.cpp) or text will not render.

Controls: click a column to drop, R = restart, ESC = menu.
