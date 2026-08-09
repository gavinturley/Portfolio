import pandas as pd
from sklearn.linear_model import LinearRegression
import numpy as np

#Linear Regression

df = pd.read_excel("practice_data.xlsx")

# Assume Month is 1,2,3,... and Cost is your target
X = df["Month"].values.reshape(-1, 1)
y = df["Total_Cost"].values

model = LinearRegression().fit(X, y)

# Predict months 13,14,15
future_months = np.array([13,14,15]).reshape(-1, 1)
predictions = model.predict(future_months)

print(predictions)


#Forest Regression

from sklearn.ensemble import RandomForestRegressor

features = df[["Labor_Hours", "Material_Cost", "Travel_Cost", "Month"]]
target = df["Total_Cost"]

model = RandomForestRegressor()
model.fit(features, target)

print("Next month:", model.predict([[1300, 17000, 4200, 13]]))


