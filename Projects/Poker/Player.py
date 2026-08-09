class Player:
    def __init__(self, name):
        self.name = name.capitalize()
        self.money = 100.0
        self.hands_won = 0
        self.hands_lost = 0
        self.money_lost = 100 - self.money
        self.bet = 0
 
    def __str__(self):
        return f"{self.name}: Money: ${self.money}, Hands Won: {self.hands_won}, Hands Lost: {self.hands_lost}, Money Lost: ${self.money_lost}"

    def __repr__(self):
        return f"Player({self.name}, {self.money}, {self.hands_won}, {self.hands_lost}, {self.money_lost})"

    def add_win(self):
        self.hands_won += 1

    def add_loss(self):
        self.hands_lost += 1