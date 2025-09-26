import sympy as sp
import tkinter as tk
from tkinter import Frame, Label, Button, Entry
import math

def main():
    # Creates the main window for the GUI
    root = tk.Tk()

    frm_main = Frame(root)
    frm_main.master.title("Derivative and Integral Calculator")
    frm_main.pack(padx=10, pady=10, fill=tk.BOTH, expand=1)

    populate_main_window(frm_main)

    root.mainloop()


def populate_main_window(frm_main):
    # Creates widgets
    lbl_title = Label(frm_main, text="Calculus Calculator")
    
    # Integral button switches calculations to integral calculator
    btn_integral = Button(frm_main, text="Integral Calculator", command=lambda: populate_integral_window(frm_main))
    
    # Derivative button switches calculations to derivative calculator
    btn_derivative = Button(frm_main, text="Derivative Calculator", command=lambda: populate_derivative_window(frm_main))
    
    # Puts the widgets in the grid layout
    lbl_title.grid(row=0, column=0, padx=3, pady=3, columnspan=3, sticky="nsew")
    btn_integral.grid(row=1, column=0, padx=3, pady=3, sticky="ew")
    btn_derivative.grid(row=1, column=1, padx=3, pady=3, sticky="ew")

def populate_derivative_window(frm_main):
        # Adds derivative calculator widgets to the main frame
        lbl_formula_entry = Label(frm_main, text="Derivative Calculator")
        ent_formula = Entry(frm_main, width=20)

        # Clear button to reset the input and output
        btn_clear = Button(frm_main, text="Clear")
        lbl_formula_bd = Label(frm_main, width=3)
        lbl_answer = Label(frm_main, width=3)

        # Place the widgets in the grid layout
        lbl_formula_entry.grid(row=2, column=0, padx=3, pady=3)
        ent_formula.grid(row=2, column=1, padx=3, pady=3, columnspan=2)
        btn_clear.grid(row=3, column=0, padx=3, pady=3, sticky="ew")
        lbl_answer.grid(row=5, column=0, padx=3, pady=3, columnspan=2, sticky="ew")

        def calc_derivative(event=None):
            # Calculates the derivative of the input equation
            equation_input = ent_formula.get()  
            x = sp.Symbol('x')
            try:
                equation = sp.sympify(equation_input)
                derivative = sp.diff(equation, x)
                lbl_answer.config(text=f"Derivative: {derivative}")
            except sp.SympifyError:
                lbl_answer.config(text="Invalid equation. Please try again.")
        
        def clear():
            # Clear the input and output fields
            ent_formula.delete(0, tk.END)  
            lbl_formula_bd.config(text="")
            lbl_answer.config(text="")    
        
        # Sets binding event as key release
        ent_formula.bind("<KeyRelease>", calc_derivative)
        btn_clear.config(command=clear)

def populate_integral_window(frm_main):
        # Adds integral calculator widgets to the main frame
        lbl_formula_entry = Label(frm_main, text="Integral Calculator")
        ent_formula = Entry(frm_main, width=20) 

        # Clear button to reset the input and output
        btn_clear = Button(frm_main, text="Clear")
        lbl_answer = Label(frm_main, width=3)
        
        # Place the widgets in the grid layout
        lbl_formula_entry.grid(row=2, column=0, padx=3, pady=3)
        ent_formula.grid(row=2, column=1, padx=3, pady=3, columnspan=2)
        btn_clear.grid(row=3, column=0, padx=3, pady=3, sticky="ew")
        lbl_answer.grid(row=4, column=0, padx=3, pady=3, columnspan=2, sticky="ew")

        def calc_integral(event=None):
            # Calculates the integral of the input equation
            equation_input = ent_formula.get()  
            x = sp.Symbol('x')
            try:
                equation = sp.sympify(equation_input)
                integral = sp.integrate(equation, x)
                lbl_answer.config(text=f"Integral: {integral}")
            except sp.SympifyError:
                lbl_answer.config(text="Invalid equation. Please try again.")
        
        def clear():
            # Clear the input and output fields
            ent_formula.delete(0, tk.END)  
            lbl_answer.config(text="")

        # Sets binding event as key release
        ent_formula.bind("<KeyRelease>", calc_integral)
        btn_clear.config(command=clear)



if __name__ == "__main__":
    main()