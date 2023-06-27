import os
import pandas as pd
import numpy as np

df = pd.read_csv("src/data/Credit/UCI_Credit_Card.csv")

rows = df.shape[0]
train_prop = 0.8
df_train = df.loc[:rows*train_prop, :]
df_test = df.loc[rows*train_prop:, :]
df_test.to_csv("src/data/Credit/use_dataset/data_test.csv",index=None)


columns = df_train.columns.tolist()
column_set = [columns[:6],[columns[0]]+columns[6:12],[columns[0]]+columns[12:18],[columns[0]]+columns[18:25]]
print(column_set)
for i in range(3):
    d = df_train.loc[:, column_set[i]]
    d.to_csv("src/data/Credit/use_dataset/PPdata_train_{}.csv".format(i),index=None)
d = df_train.loc[:, column_set[3]]
d.to_csv("src/data/Credit/use_dataset/APdata_train.csv".format(i),index=None)