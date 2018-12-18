jsonText = "{"
f = open("color.txt", "r")
t = f.readlines()

for (i, j) in zip(range(len(t)), t):
    jsonText = "{"
    filename = "FlatUI" + str(i)
    (r, g, b) = j.split(",")
    jsonText += ("\n\"segColor1"
                 + "\": [" + str(r) + ", " + str(g) + ", " + str(b) + "],")
    jsonText += "  \n\"frameColor\": [0,255,0],\n\"isBarFilled\": true,\n\"hasBarFrame\": false,\n\"frameSize\": 0.02\n}"
    f2 = open(filename + ".json", "w")
    f2.write(jsonText)
    f2.close()
f.close()
