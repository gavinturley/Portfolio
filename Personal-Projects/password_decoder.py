import time
chars = [
    # Lowercase letters
    'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',

    # Uppercase letters
    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',

    # Numbers
    '0','1','2','3','4','5','6','7','8','9',

    # Special characters (ASCII)
    '!','"','#','$','%','&',"'",'(',')','*','+',
    ',','-','.','/',
    ':',';','<','=','>','?','@',
    '[','\\',']','^','_','`',
    '{','|','}','~'
]

def main():
    password = input("What is the password? ")
    index = 0
    answer = []
    for letter in password:
        if find_letter(letter, index) != None:
            answer.append(find_letter(letter, index))
        else:
            print("Error could not find answer.")
            return
        index += 1
        time.sleep(0.5)
        
    print(f"The password is:")
    for letter in answer:
        print(letter, end="", flush=True)
    print()

def find_letter(letter, index):
    for char in chars:
        if char == letter:
            print(char)
            return char
    return None

if __name__ == "__main__":
    main()