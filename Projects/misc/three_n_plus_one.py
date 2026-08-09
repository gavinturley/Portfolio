import matplotlib.pyplot as plt
import os
def main():
    os.system("clear")
    start = 1
    y = []
    while start == 1:
        number = int(input("Enter a number: "))
        init_number = number
        while number != 1:
            print(round(number,0))
            y.append(number)
            if number % 2 == 0:
                number = even(number)
            else:
                number = odd(number)
        print(number)
        y.append(number)
        print(y)
        plt.xlabel('Index')
        plt.ylabel('Value')
        plt.title(f'Plot of 3n + 1 for {init_number}')
        plt.plot(y, color ='blue', linestyle='--')
        plt.show()


def odd(number):
    return (3 * number) + 1

def even(number):
    return number / 2



if __name__ == "__main__":
    main()