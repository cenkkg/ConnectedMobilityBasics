import matplotlib.pyplot as plt

# Sample data
x_values = [1, 2, 3]
y_values = [55, 7718, 105062]

# Create a line graph
plt.plot(x_values, y_values, marker='o', linestyle='-')


# Add labels and title
plt.xlabel('Configration Index')
plt.ylabel('Total Number of SmallVirus Messages')
plt.title('Analysis of SmallVirus Messages')
plt.xticks(x_values)

# Show the plot
plt.show()
