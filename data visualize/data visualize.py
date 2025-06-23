import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from mpl_toolkits.mplot3d import Axes3D

plt.rcParams["toolbar"] = "None"

file_path = "rubik's_cube_train_data.csv"
df = pd.read_csv(file_path, header=None, names=['R', 'G', 'B', 'Color'])

color_mapping = {
    "White": (255, 255, 255),
    "Yellow": (255, 255, 0),
    "Red": (255, 0, 0),
    "Orange": (255, 165, 0),
    "Blue": (0, 0, 255),
    "Green": (0, 255, 0)
}

df = df[df['Color'].isin(color_mapping.keys())]
df['MappedColor'] = df['Color'].map(color_mapping)
color_values = [tuple(v/255.0 for v in rgb) for rgb in df['MappedColor']]

fig = plt.figure(figsize=(8, 8))
ax = fig.add_subplot(111, projection='3d')
ax.set_facecolor((0, 0, 0.1))
ax.scatter(df['R'], df['G'], df['B'], c=color_values, marker='o', edgecolors='black')

ax.set_xticks([])
ax.set_yticks([])
ax.set_zticks([])
ax.grid(False)

for axis in [ax.xaxis, ax.yaxis, ax.zaxis]:
    axis.pane.fill = False
    axis.pane.set_edgecolor((0, 0, 0, 0))
    axis.line.set_color((0, 0, 0, 0))

r_min, r_max = df['R'].min(), df['R'].max()
g_min, g_max = df['G'].min(), df['G'].max()
b_min, b_max = df['B'].min(), df['B'].max()

ax.plot([r_min, r_max], [g_min, g_min], [b_min, b_min], 'w')
ax.plot([r_min, r_max], [g_max, g_max], [b_min, b_min], 'w')
ax.plot([r_min, r_min], [g_min, g_max], [b_min, b_min], 'w')
ax.plot([r_max, r_max], [g_min, g_max], [b_min, b_min], 'w')

ax.plot([r_min, r_max], [g_min, g_min], [b_max, b_max], 'w')
ax.plot([r_min, r_max], [g_max, g_max], [b_max, b_max], 'w')
ax.plot([r_min, r_min], [g_min, g_max], [b_max, b_max], 'w')
ax.plot([r_max, r_max], [g_min, g_max], [b_max, b_max], 'w')

ax.plot([r_min, r_min], [g_min, g_min], [b_min, b_max], 'w')
ax.plot([r_max, r_max], [g_min, g_min], [b_min, b_max], 'w')
ax.plot([r_min, r_min], [g_max, g_max], [b_min, b_max], 'w')
ax.plot([r_max, r_max], [g_max, g_max], [b_min, b_max], 'w')

def zoom(event):
    scale_factor = 1.2 if event.step > 0 else 1 / 1.2
    xlim = ax.get_xlim()
    ylim = ax.get_ylim()
    zlim = ax.get_zlim()
    xcenter = (xlim[0] + xlim[1]) / 2
    ycenter = (ylim[0] + ylim[1]) / 2
    zcenter = (zlim[0] + zlim[1]) / 2
    ax.set_xlim([(x - xcenter) * scale_factor + xcenter for x in xlim])
    ax.set_ylim([(y - ycenter) * scale_factor + ycenter for y in ylim])
    ax.set_zlim([(z - zcenter) * scale_factor + zcenter for z in zlim])
    plt.draw()

fig.canvas.mpl_connect('scroll_event', zoom)
plt.show()
