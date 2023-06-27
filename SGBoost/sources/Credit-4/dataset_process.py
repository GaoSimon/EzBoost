import numpy, os, csv
import pandas as pd
import sklearn.neural_network as nn
import matplotlib.pyplot as plt
import sklearn.metrics  as met
from sklearn.tree import DecisionTreeClassifier
from graphviz import Source
from sklearn.naive_bayes import GaussianNB
from sklearn.metrics import accuracy_score
from sklearn.svm import SVC
from sklearn.metrics import precision_recall_fscore_support
from sklearn import preprocessing, tree
from sklearn.preprocessing import RobustScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report,confusion_matrix
from sklearn.model_selection import StratifiedKFold

from sklearn.utils import shuffle




# Read data file

data = pd.read_csv("UCI_Credit_Card.csv", sep=",")
data = data.drop('ID', axis=1)
train_data, test_data = train_test_split(data,  test_size=0.2)
test_data.to_csv("test", index=False)

party1 = train_data[["LIMIT_BAL","SEX","EDUCATION","MARRIAGE","AGE","PAY_0"]]
party2 = train_data[["PAY_2","PAY_3","PAY_4","PAY_5","PAY_6","BILL_AMT1"]]
party3 = train_data[["BILL_AMT2", "BILL_AMT3","BILL_AMT4","BILL_AMT5","BILL_AMT6","PAY_AMT1"]]

organizeData = train_data[["PAY_AMT2","PAY_AMT3","PAY_AMT4","PAY_AMT5","PAY_AMT6","default_payment_next_month"]]


party1.to_csv("party1", index=False)
party2.to_csv("party2", index=False)
party3.to_csv("party3", index=False)

organizeData.to_csv("organizeData", index=False)
