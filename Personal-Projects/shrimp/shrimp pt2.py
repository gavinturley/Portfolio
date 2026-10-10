import os
import tkinter as tk
from tkinter import messagebox
from PIL import Image

os.system('Clear')

def ask_question():
    response = messagebox.askquestion("Question", "Are you a shrimp?")
    if response == 'yes':
        print("You are a shrimp!")
    else:
        print("You are not a shrimp!")

root = tk.Tk()

def open_image_and_play_audio():
    # Open image
    image_path = "whale.jpg"
    image = Image.open(image_path)
    image.show()

    # Play audio
    audio_path = "cat-laugh-meme-1.mp3"
    os.system("afplay " + audio_path)

def ask_question():
    response = messagebox.askquestion("Question", "Are you a shrimp?")
    if response == 'yes':
        print("You are a shrimp!")
        open_image_and_play_audio()
        os.system("osascript -e 'set volume output volume 50'")
    else:
        print("You are not a shrimp!")

ask_question()
root.mainloop()
