import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
from sklearn.neighbors import KNeighborsClassifier

df = pd.read_csv("rubik's_cube_train_data.csv", header=None)
df.columns = ['R', 'G', 'B', 'Label']

le = LabelEncoder()
y = le.fit_transform(df['Label'])
X = df[['R', 'G', 'B']]
label_names = le.classes_

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = KNeighborsClassifier(n_neighbors=3)
model.fit(X_train, y_train)
y_pred = model.predict(X_test)

accuracy = accuracy_score(y_test, y_pred)
precision = precision_score(y_test, y_pred, average='macro')
recall = recall_score(y_test, y_pred, average='macro')
f1 = f1_score(y_test, y_pred, average='macro')
conf_matrix = confusion_matrix(y_test, y_pred)

plt.style.use('dark_background')

fig, axes = plt.subplots(1, 2, figsize=(14, 6))
fig.suptitle("Rubik's Cube Color Classifier Performance", fontsize=16, fontweight='bold', color='white')

sns.heatmap(conf_matrix, annot=True, fmt='d', cmap='rocket', 
            xticklabels=label_names, yticklabels=label_names, ax=axes[0], cbar=False, linewidths=0.5, linecolor='gray')
axes[0].set_title('Confusion Matrix', fontsize=14, color='white')
axes[0].set_xlabel('Predicted Label', fontsize=12)
axes[0].set_ylabel('True Label', fontsize=12)

metrics = [accuracy, precision, recall, f1]
metric_names = ['Accuracy', 'Precision', 'Recall', 'F1 Score']
colors = ['#66c2a5', '#fc8d62', '#8da0cb', '#e78ac3']

sns.barplot(x=metric_names, y=metrics, palette=colors, ax=axes[1])
axes[1].set_ylim(0, 1.05)
axes[1].set_title("Performance Metrics", fontsize=14, color='white')
axes[1].set_ylabel("Score", fontsize=12)

for i, val in enumerate(metrics):
    axes[1].text(i, val + 0.02, f"{val:.2f}", ha='center', color='white', fontweight='bold')

plt.tight_layout()
plt.show()
