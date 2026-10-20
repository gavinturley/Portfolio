# Gavin Turley: Projects

Electrical engineering student at Brigham Young University (minors in Mathematics and Computer Science), working on embedded systems, firmware, PCB design, and software. This repository collects my projects.

**Contact:** [77turley@gmail.com](mailto:77turley@gmail.com) | [LinkedIn](https://www.linkedin.com/in/gavin-turley-21481432b)

## Projects

| Project | Area | Stack | Status |
| --- | --- | --- | --- |
| [FSAE ECU](#fsae-ecu-byu-racing) | Embedded | Teensy, STM32, CAN, RTOS | In progress |
| [Multiplayer Chess Server and Client](#multiplayer-chess-server-and-client) | Software | Java, Maven, HTTP, WebSockets, SQL | In progress |
| [Four in a Row AI](#four-in-a-row-ai) | Software | C++, SFML | Complete |
| [Sound-reactive Camera System](#sound-reactive-camera-system) | Embedded | STM32, analog microphones, SG90 servo | Complete |

---

### FSAE ECU (BYU Racing)

*December 2025 - Present*

Electronic Control Unit for a Formula SAE race car, built with my BYU Racing teammates.

- Reads brake, throttle, and start-switch sensors, calculates motor torque, and sends it to the motor over CAN
- Runs safety checks and triggers an automatic shutdown when any sensor flags a fault
- Migrating from a Teensy to an STM32 (faster clock, second core), merging with the Data Collector Front (DCF), and converting from polling to an RTOS design

**Tech:** Teensy, STM32, CAN, RTOS

**Link:** [add link if the code is public]

---

### Multiplayer Chess Server and Client

*May 2026 - Present*

A client-server chess application written in Java for BYU CS 240, structured as a Maven multi-module project.

- **Server:** manages users and games over HTTP and WebSockets, with SQL database storage
- **Client:** command-line client for playing over the network
- **Shared:** chess rules and game-state tracking used by both sides
- **In progress:** a graphical UI and a minimax-based AI opponent, building on the Four in a Row AI below

**Tech:** Java, Maven, HTTP, WebSockets, SQL

**Link:** [add repository link]

---

### Four in a Row AI

*February 2026 - May 2026*

A Four in a Row game engine in C++ with an AI opponent.

- Minimax search with alpha-beta pruning and a Zobrist-hashed transposition table
- Custom heuristic for scoring board positions, searching up to 7 moves deep
- SFML interface (built with AI assistance) with a menu system and human vs. human and human vs. AI modes

**Tech:** C++, SFML

**Link:** [add repository link]

---

### Sound-reactive Camera System

*June 2025 - July 2025*

A directional audio tracker that points toward the source of a sound in real time.

- Two analog microphones detect the direction of incoming sound
- An STM32 microcontroller processes the signals and drives an SG90 servo to rotate toward the source

**Tech:** STM32, analog microphones, SG90 servo

**Link:** [add repository link]

---

## Skills

- **Languages:** Python, C, C++, C#, Java, SQL, ARM Assembly, x86-64 Assembly
- **Embedded:** STM32, ESP32, Teensy microcontrollers; RTOS; CAN, SPI, I2C, UART; ADC, PWM, GPIO; firmware debugging
- **Digital design:** Verilog, FPGA design, Logisim (CPU design)
- **Circuits and PCB:** KiCad, Altium Designer; Qucs, SPICE; through-hole and surface-mount soldering
- **Lab equipment:** Oscilloscopes, function generators, Vector Network Analyzer
- **RF:** Antenna matching, Smith charts
- **Software:** Linux, Git, MATLAB, networking and sockets
