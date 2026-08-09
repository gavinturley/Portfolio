import os
import time

snake = "@"
blank = " "
wall_v = "_"
wall_h = "|"
apple = "$"
grid_start = """
________________
|              |
|  Welcome to  |
|      the     |
|  Snake  Game |
|______________|
"""
grid_blank = """
________________
|              |
|              |
|              |
|              |
|______________|
"""

def main():
    os.system("Clear")
    print(grid_start)
    print("Press enter to start")
    input("")
    loading(3)







def loading(a):
    while a > 0:
        os.system("Clear")
        print(".")
        time.sleep(1)
        os.system("Clear")
        print("..")
        time.sleep(1)
        os.system("Clear")
        print("...")
        time.sleep(1)
        a -= 1
        os.system("Clear")

if __name__ == "__main__":
    main()
