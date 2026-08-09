import os
import random
import time
sentences = [
    "The quick brown fox jumps over the lazy dog.",
    "A watched pot never boils.",
    "Simplicity is the ultimate sophistication.",
    "She sells seashells by the seashore.",
    "Typing fast requires both accuracy and rhythm.",
    "Better late than never, but never late is better.",
    "Artificial intelligence is transforming the future.",
    "Curiosity killed the cat, but satisfaction brought it back.",
    "Life is what happens when you're busy making other plans.",
    "Great power comes with great responsibility.",
    "Every expert was once a beginner who never gave up.",
    "The early bird catches the worm, but the second mouse gets the cheese.",
    "Success is not final, failure is not fatal: it is the courage to continue that counts.",
    "In the middle of difficulty lies opportunity.",
    "Do not go where the path may lead, go instead where there is no path and leave a trail.",
    "The best way to predict the future is to invent it.",
    "Hard work beats talent when talent doesn't work hard.",
    "Programming is the art of teaching a computer how to think.",
    "All work and no play makes Jack a dull boy.",
    "Practice makes permanent, not just perfect.",
    "A journey of a thousand miles begins with a single step.",
    "Knowledge is power, but wisdom is knowing how to use it.",
    "If you want to go fast, go alone. If you want to go far, go together.",
    "Dreams don't work unless you do.",
    "Don't count the days, make the days count."
]

def main():
    os.system('clear')
    print("Welcome to the typing test")
    input("Press enter when you are ready...")
    time_limit = int(input("How many seconds do you want?"))
    os.system('clear')
    print("You have 30 seconds to type, start now!")
    for i in range(3):
        print(random.choice(sentences))
    typing = input()
    time.sleep(time_limit)
    print("Time is up!")
    time.sleep(3)
    words = typing.split(' ')
    num = 0
    wpm = num / (time_limit * 60)
    for i in words:
        num += 1
    print(f"You typed {num} words. Thats {wpm} words per minute")




if __name__ == "__main__":
    main()