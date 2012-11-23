#! /usr/bin/python
#  Name: Sravan Bhamidipati
#  Date: 15th November, 2012
#  Purpose: To draw the hexagonal grid map of 6 adjacent monochromatic grids.


def print_line(colors, counts):
	line = ""
	for i in range(0, len(colors)):
		line += (colors[i]+",") * counts[i]
	print line.strip(",")


def print_block1(colors):
	for i in range(11, 6, -1):
		counts = [i, 12-i]
		print_line(colors, counts)


def print_block2(colors):
	for i in range(6, 0, -1):
		counts = [i, 6-i, i, 6-i]
		print_line(colors, counts)


def print_block3(colors):
	for i in range(6, 0, -1):
		counts = [6-i, i, 6-i, i]
		print_line(colors, counts)


def print_block4(colors):
	for i in range(6, 12):
		counts = [i, 12-i]
		print_line(colors, counts)


# R - 1, G - 2, B - 3, P - 4, Y - 5, O - 6
print_block1(["X", "1"])
print_block2(["X", "2", "1", "3"])
print_block3(["P", "2", "5", "3"])
print_block3(["X", "4", "6", "5"])
print_block4(["X", "6"])
