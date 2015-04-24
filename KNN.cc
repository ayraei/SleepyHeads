/**
 * Fast KNN
 */
#include <iostream>
#include <fstream>
#include <unordered_map>
#include <cmath.h>
#include <list>
#include <string>
#include <thread>

#define NUM_MOVIES 17770
#define K 20

using namespace std;

int sim[NUM_MOVIES][NUM_MOVIES] = {0};
unsigned char vCount[5][NUM_MOVIES][5] = {0}; 
unsigned short overflows[5][NUM_MOVIES][5] = {0};
unordered_map< int, unordered_map<int, int> > movieHashMap = {};
unordered_map< int, unordered_map<int, int> > userHashMap = {};
PearsonIntermediate array[MOVIE_CNT];

/** Location of training file **/
const string TRAIN_FILE_LOC =
        "/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";

/** Location of output files **/
const string OUTPUT_SIM_LOC =
        "/Users/debbie1/Documents/NetflixData/output/sim.dta";

// Movie n as neighbor of movie m
struct MovieNeighbor {
    unsigned int commonViewers;
    float m1Avg;   //average rating of movie m
    float m2Avg;   //average rating of movie n

    float nRating; //current viewer's rating for movie n

    float rRaw;    //raw Pearson's r value
    float rLower;
    float weight;
};

// From all viewers who rated both movie m1 and m2
struct PearsonIntermediate {
    float s1;    //sum of ratings for movie X
    float s2;    //sum of ratings for movie Y
    float p12;   //sum of product of ratings for movies X and Y
    float p11;   //sum of square of ratings for movie X
    float p22;   //sum of square of ratings for movie Y
    unsigned int numIntersect; //number of viewers who rated both movies
};

/* Calculate similarity using Person coefficient */
float calcPersonR(PearsonIntermediate p)
{
    float num = (p->numIntersect * p->p12) - (p->s1)(p->s2);
    float denom = sqrt ((p->numIntersect * p->p11 - pow(p->s1, 2)) * 
                        (p->numIntersect * p->p22 - pow(p->s2, 2));
    return num / denom;
}

/* Fill up the array to indicate the overlapping users for each movie pair */
void findInsersections(int m1)
{
    unsigned char r1, r2;
    unordered_map< int, int > m1History = movieHashMap.find(m1);

     // For each user who rated movie X
    for (auto viewer : m1History) {
        r1 = m1History.find(viewer);                   // The rating viewer gave to m1

        // For each movie Y rated by the user
        for (auto m2 : userHashMap.find(viewer)) {
            r2 = userHashMap.find(viewer).find(m2);    // The rating viewer gave to m2

            // Increment the rating
            vCount[r1][m2][r2]++;

            // Catch overflow beyond 255
            if (0 == vCount[r1][m2][r2]) {
                overflows[r1][m2][r2]++;
            }
        }
    }
}

/* Fill up the structs to later calculate the Pearson coefficient */
void getPearsonIntermediate(int m1, int m2)
{

}

/* Calculate the weight predicted from Pearson's coefficient */
float calculatePearsonWeight(MovieNeighbor n)
{
    // Incomplete, just a place holder
    return n->rLower * n->rLower * log(n->commonViewers);
}

int main ()
{
    int count, userID, movieID, date;
    unsigned char rating;
    unordered_map< int, int > movieRating;
    unordered_map< int, int > userRating;

    // Read in the data from input file
    ifstream infile(TRAIN_FILE_LOC);
    if(!infile) {
        cout << endl << "Failed to open file " << filename;
        return 1;
    }

    /*
     * Load training set into memory
     * Each movie has a list of <user, rating>
     */
    count = 0;
    while (infile >> userID >> movieID >> date >> rating)
    {
        // Display progress
        if (count % 10000000 == 0) 
        {
            cout << count;
        }

        // Check if movie already already exists in movie hashmap
        if (movieHashMap.count(movieID) != 0) 
        {
            movieRating = movieHashMap.find(movieID);
        }

        else
        {
            movieRating = new unordered_map<int, int>;
        }

        // Put new <user, ratings> in
        movieRating.insert(userID, rating);
        movieHashMap[movieID] = movieRating;


        // Check if userID already already exists in user hashmap
        if (userHashMap.count(userID) != 0) 
        {
            userRating = userHashMap.find(userID);
        }

        else
        {
            userRating = new unordered_map<int, int>;
        }

        // Put new <user, ratings> in
        userRating.insert(movieID, rating);
        userHashMap[userID] = userRating;

        count++;
    }
    
    // Use multi-threading to calculate similarities
    thread t1(PearsonTask());
    t1.join;

    // Output similarities to text file


    return 0;
}