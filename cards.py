import random
import time
from Player import Player

# Set values #
MINIMUM_BET = 10
TIME_DELAY = 1
RESET = "\033[0m"
BOLD = "\033[1m"
RED = "\033[31m"
GREEN = "\033[32m"
BLUE = "\033[34m"
YELLOW = "\033[33m"
BLACK = "\033[30m"
CYAN = "\033[36m"

players = []

def main():
    clear_console()
    print(f"-- Welcome to the {BLACK}{BOLD}BLACKJACK{RESET} --")
    print("\n --    🂡    ♚    ♛    🂫    -- ")
    name = input("\nWhat is your name? ")
    player = Player(name)
    players.append(player)
    input("\nPress Enter to continue")
    loading(TIME_DELAY)
    menu(player)

def menu(player):
    start = True
    stats_for_nerds = False
    debug = False
    while start:
        clear_console()
        print('''Choose from the following options:
        1. How to play
        2. Check Player Information
        3. Play Game
        4. Toggle helpful stats
        5. Switch player
              ''')
        choice = input("")
        if choice == "saylor":
            player.money = player.money ** 2
            menu(player)
        elif choice == "debug":
            debug = True
        else:
            choice = int(choice)
        if choice == 1:
            clear_console()
            print("""
                🃏 HOW TO PLAY BLACKJACK

                Goal:
                Get as close to 21 as possible without going over.
                Beat the dealer's total to win!

                Card Values:
                2-10 = face value
                J, Q, K = 10
                A = 1 or 11

                Game Setup:
                - You get two cards face up.
                - Dealer gets one face up and one face down.

                Your Turn:
                - 'Hit' to take another card.
                - 'Stand' to keep your current hand.
                - Go over 21 and you 'bust'.

                Dealer's Turn:
                - Dealer reveals their hidden card.
                - Dealer must hit until 17 or more.

                Winning:
                - Closer to 21 than dealer → You win.
                - Same total → Push (tie).
                - Over 21 → Bust (lose).

                Blackjack:
                - Ace + 10-point card = Blackjack (best hand!)

                Quick Tips:
                - Stand on 18+
                - Hit on 11 or less
                - Treat dealer's hidden card like a 10
                """)
            input("Press Enter to continue")
        elif choice == 2:
            clear_console()
            print(player)
            input("Press Enter to continue")
        elif choice == 3:
            deck = shuffle()
            user_full_hand = []
            dealer_full_hand = []
            for i in range(2):
                new_card = card_chooser(deck)
                user_full_hand.append(new_card)
                deck.remove(new_card)
                new_card = card_chooser(deck)
                dealer_full_hand.append(new_card)
                deck.remove(new_card)            
            bet = betting(player)
            exit = False
            show = False
            while exit is not True:
                clear_console()
                print_title("Dealer's Hand")
                if show == False:
                    print_dealer_hand(dealer_full_hand)
                else:
                    print_hand(dealer_full_hand)
                dealer_score = calculate_score(dealer_full_hand)
                if debug:
                    print(dealer_full_hand[1][0])
                    print(f"Sum of cards: {dealer_score}")
                    print(f"Blackjack: {is_blackjack(dealer_full_hand)}")
                print_title("Your Hand")
                print_hand(user_full_hand)
                user_score = calculate_score(user_full_hand)
                if user_score > 21: 
                    show = True
                blackjack = is_blackjack(user_full_hand)
                if stats_for_nerds:
                    print(f"Sum of cards {user_score}")
                    print(f"Blackjack: {blackjack}")
                if not show:
                    hit_or_raise()
                    choice2 = int(input())
                else:
                    choice2 = 2
                #1 for hit, 2 for stay, 3 to exit
                if choice2 == 1:
                    new_card = card_chooser(deck)
                    user_full_hand.append(new_card)
                    deck.remove(new_card)
                if choice2 == 2 and show:
                    if user_score > 21:
                        dealer_win(bet, player)
                        menu(player)
                    if dealer_score > 21:
                        user_win(bet, player, blackjack)
                        menu(player)
                    if dealer_score == user_score:
                        if blackjack and not is_blackjack(dealer_full_hand):
                            user_win(bet, player, blackjack)
                            menu(player)
                        else:
                            print("\nIt's a tie! Nobody wins, nobody loses!")
                            input("\nPress Enter to continue")
                            menu(player)
                    if dealer_score > user_score:
                        dealer_win(bet, player)
                        menu(player)
                    if user_score > dealer_score:
                        if dealer_score < 17 or (dealer_score >= 17 and any(card[1] == "A" for card in dealer_full_hand)):
                            new_card = card_chooser(deck)
                            dealer_full_hand.append(new_card)
                            deck.remove(new_card)
                        else:
                            user_win(bet, player, blackjack)
                else:
                    show = True
                if choice2 == 3:
                    print(f"You quit! You lose ${bet}.")
                    player.money -= bet
                    player.add_loss()
                    menu(player)
                time.sleep(TIME_DELAY)
        elif choice == 4:
            if stats_for_nerds:
                stats_for_nerds = False
            else:
                stats_for_nerds = True
        elif choice == 5:
            i = 0
            for player in players:
                i += 1
                print(f"{i}. {player}")
            print(f"{i + 1}. New Player")
            switch = int(input("Which player do you want to choose?" ))
            if switch <= len(players):
                menu(players[switch - 1])
            else:
                print("Switching players, press Enter to continue")
                main()

def betting(player):
    if player.money < MINIMUM_BET:
        print(f"{BOLD}{RED}Come back when you get some money, buddy! You lose!{RESET}")
        time.sleep(TIME_DELAY)
        return
    bet = input(f'You have ${player.money} left, MINIMUM bet ${MINIMUM_BET}, {BLACK}{BOLD}HOW MUCH WOULD YOU LIKE TO BET?{RESET} ')
    try:
        bet = float(bet)
        if bet < 0:
            input("Please enter a positive number, press Enter to continue")
            menu(player)
        if bet < 10:
            print(f"The bet threshold is ${MINIMUM_BET}, press Enter to continue")
            bet = 10
        if bet > player.money:
            input(f"The max you can bet is ${player.money}, press Enter to confirm.")
            bet = player.money
        return bet
    except ValueError:
        input("Please input a valid number of dollars to bet, press Enter to try again.")
        menu(player)

def shuffle():
    deck = csv_to_list("full_deck.csv")
    return deck

def is_blackjack(hand):
    face_cards = ["10", "J", "Q", "K"]
    if (hand[0][1] == "A" and hand[1][1] in face_cards) or (hand[0][1] in face_cards and hand[1][1] == "A"):
        return True
    return False

def dealer_win(bet, player):
    print(f"\nDealer wins! You lose ${bet}.")
    player.add_loss()
    player.money -= bet
    input("\nPress Enter to continue")
    menu(player)

def user_win(bet, player, blackjack):
    if blackjack:
        print(f"\nYou win ${bet * (3/2)} with a {BOLD}{BLACK}BLACKJACK{RESET}!")
        player.add_win()
        player.money += bet + (bet * 3/2)
        input("\nPress Enter to continue")
        menu(player)
    else:
        print(f"\nYour hand is better than the dealers! You win ${bet}")
        player.add_win()
        player.money += bet
        input("\nPress Enter to continue")
        menu(player)

def calculate_score(hand):
    score = 0
    ace = False
    for card in hand:
        if card[1] == 'A':
            ace = True
    for i in hand:
        point_value = int(i[2])
        if point_value > 10:
            point_value = 10
        score += point_value
    if score > 21 and ace:
        score -= 9
    return score

def card_chooser(deck, n=None):
    card = random.choice(deck)
    return card

def build_card_front(number, suite):
    return [
        "┌─────────┐",
        f"│{number:<2}{suite}      │",
        "│         │",
        f"│    {suite}    │",
        "│         │",
        f"│      {suite}{number:>2}│",
        "└─────────┘"
    ]

def build_card_back():
    return [
        "┌─────────┐",
        "│░░░░░░░░░│",
        "│░░░░░░░░░│",
        "│░░░░░░░░░│",
        "│░░░░░░░░░│",
        "│░░░░░░░░░│",
        "└─────────┘"
    ]

def print_hand(cards):
    suite_number = get_suite_number(cards)
    visual_card = [build_card_front(number, suite) for number, suite in suite_number]
    for i in range(len(visual_card[0])):
        print("  ".join(card[i] for card in visual_card))

def print_dealer_hand(card):
    suite_number = get_suite_number(card)
    number, suite = suite_number[0]
    cards = [build_card_front(number, suite), build_card_back()]
    for i in range(len(cards[0])):
        print("  ".join(card[i] for card in cards))

def get_suite_number(cards):
    suite_number = []
    for i in cards:
        suite_number.append((i[1],i[4]))
    return suite_number

def hit_or_raise():
    print(f"\nType 1 to {BOLD}{CYAN}HIT{RESET}, 2 to {BOLD}{CYAN}STAY{RESET}, or 3 to {BOLD}{CYAN}EXIT{RESET}")

def print_title(string):
    print(f"\n      {BOLD}{CYAN}{string}{RESET}")

def loading(a):
    if a == 0:
        return
    while a > 0:
        clear_console()
        print(".")
        time.sleep(1)
        clear_console()
        print("..")
        time.sleep(1)
        clear_console()
        print("...")
        time.sleep(1)
        a -= 1
        clear_console()

def csv_to_list(filename):
    data = []
    with open(filename, 'r') as file:
        lines = file.readlines()[1:]
        for line in lines:
            values = line.strip().split(',')
            data.append(values)
    return data

def clear_console():
    print("\033c", end="")

if __name__ == "__main__":
    main()
