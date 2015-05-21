import numpy as np
import datetime as dt
import math

# Constants
NUM_USERS    = 458293
NUM_MOVIES   = 17770
NUM_FEATURES = 20

# Run parameters
LEARNING_RATE = 0.0002
REG_PENALTY   = 0.04
NUM_EPOCHS    = 2

# Location of input files 
TRAIN_FILE_LOC = '/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta'
TEST_FILE_LOC  = '/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta'

#Location of output file
OUTPUT_PREDICT_LOC = '/Users/debbie1/Documents/NetflixData/output/SVDPP_predictions_probe.dta'
OUTPUT_PERF_LOC    = '/Users/debbie1/Documents/NetflixData/output/SVDPP_performance.dta'

# RateUnit
class RateUnit:
    def __init__(self, ID, rating):
        self.getID = ID
        self.getRating = rating

# ArrayManager
class ArrayManager:
    def __init__(self):
        self.sums = np.zeros(NUM_USERS)
        self.Ns   = np.empty(NUM_USERS)
        self.map  = {}

    def add(self, userID, movieID, rating):
        ru = RateUnit(movieID, rating)

        if userID in self.map:
            self.map[userID].append(ru)
        else:
            self.map[userID] = [ru]
    
    def getUserHistory_N(self, userID):
        return self.map.get(userID)

    def getSum(self, userID):
        return sums[userID]

    def getN(self, userID):
        return Ns[userID]

    def getOriginalRating(self, userHistory, movieID):
        for ru in userHistory:
            if ru.getID == movieID:
                return ru.getRating
        return 0.0

    def initConstants(self):
        for i in range(0, NUM_USERS):
            userHistory = self.getUserHistory_N(i);
            self.Ns[i] = math.pow(len(userHistory), -0.5)

            for ru in userHistory:
                self.sums[i] += ru.getRating

# Initialization
def init(arrayManager):
    f_train = open(TRAIN_FILE_LOC, 'r');
    for count, line in enumerate(f_train):
        if count % 10000000 == 0:
            print count

        lineArray = line.split()
        
        userID  = int(lineArray[0]) - 1
        movieID = int(lineArray[1]) - 1
        rating  = int(lineArray[3])

        arrayManager.add(userID, movieID, rating)
    f_train.close()

    f_test  = open(TEST_FILE_LOC, 'r');
    for count, line in enumerate(f_test):
        if count % 500000 == 0:
            print count

        lineArray = line.split()
        
        userID  = int(lineArray[0]) - 1
        movieID = int(lineArray[1]) - 1

        arrayManager.add(userID, movieID, 0)
    f_test.close()

    arrayManager.initConstants()

def predictedRating(arrayManager, userID, movieID):
    maxIndex = NUM_FEATURES - 1
    N = arrayManager.getN(userID)
    N_list = arrayManager.getUserHistory_N(userID)
    q_i = q[movieID, 0:]
    p_u = p[userID, 0:]
    y_sum = np.zeros(NUM_FEATURES)

    # Calculate y_sum (userTestRatings of all the ratings from a user)
    for ru in N_list:
        movie = ru.getID

        for f in range(0, maxIndex):
            y_sum[f] += y[movieID][f]
    y_sum *= N

    # r hat = q[movie] * (pu + N * sum(yj))
    return (q_i * (y_sum + p_u))[0];


# Program entry point
def main():

    # Initialize q, p, y
    q = 0.001 * np.random.randn(NUM_MOVIES, NUM_FEATURES)
    p = 0.001 * np.random.randn(NUM_USERS,  NUM_FEATURES)
    y = 0.001 * np.random.randn(NUM_MOVIES, NUM_FEATURES)

    # Initialize everything else
    arrayManager = ArrayManager()
    init(arrayManager)
    print "Initialization complete!\n"

    # File prep
    f_perf  = open(OUTPUT_PERF_LOC, 'w')
    f_train = open(TRAIN_FILE_LOC,  'r')

    # Constants
    maxIndex = NUM_FEATURES - 1
    LRtRP = LEARNING_RATE * REG_PENALTY

    # SGD
    for e in range(0, NUM_EPOCHS):
        print "Epoch %d start: %s \n" % (e, str(dt.datetime.now().time()))

        for count, line in enumerate(f_train):

            # Print progress through file
            if count % 500000 == 0:
                print count

            # Get training data
            lineArray = line.split()
            userID  = int(lineArray[0]) - 1
            movieID = int(lineArray[1]) - 1
            rating  = int(lineArray[3])

            # Set up
            N = arrayManager.getN(userID)
            N_list = arrayManager.getUserHistory_N(userID)
            q_i = q[movieID, 0:]
            p_u = p[userID, 0:]
            y_sum = np.zeros(NUM_FEATURES)

            # Calculate y_sum (sum of all the ratings from a user)
            for ru in N_list:
                movie = ru.getID

                for f in range(0, maxIndex):
                    y_sum[f] += y[movieID][f]
            y_sum *= N

            # Calculate error
            rHat = (q_i * (y_sum + p_u))[0]
            err = rating - rHat
            LRtE = LEARNING_RATE * err

            # Print out performance
            f_perf.write("%f %f %f %f \n" % (userID, movieID, rating, rating))

            # Update q
            # q_i = q_i + LEARNING_RATE * err * (p_u + y_sum) - LEARNING_RATE * REG_PENALTY * q_i
            q_temp = q_i + LRtE * (p_u + y_sum) - LRtRP * q_i
            q[movieID, 0:] = q_temp

            # Update p
            # p_u = p_u + LEARNING_RATE * err * q_i - LEARNING_RATE * REG_PENALTY * p_u
            p_u += LRtE * q_i - LRtRP * p_u
            p[userID, 0:] = p_u

            # Update y
            # y_j = y_j + q_i * LEARNING_RATE * err * N - LEARNING_RATE * REG_PENALTY * y_j
            y[movieID, 0:] += LRtE * N * q_i - LRtRP * y[movieID, 0:]

            print "Epoch %d ended: %s \n" % (e, str(dt.datetime.now().time()))

    f_perf.close()
    f_train.close()

    # Write out predictions
    f_test = open(OUTPUT_PREDICT_LOC, 'w')
    f_out = open(OUTPUT_PREDICT_LOC, 'w')

    for count, line in enumerate(f_train):
        # Print progress through file
        if count % 500000 == 0:
            print count

        # Get test data
        lineArray = line.split()
        userID  = int(lineArray[0]) - 1
        movieID = int(lineArray[1]) - 1
        prediction = predictedRating(arrayManager, userID, movieID)

        f_out("%.3f \n" % prediction)

    f_test.close()
    f_out.close()

if __name__ == "__main__":
    main()



