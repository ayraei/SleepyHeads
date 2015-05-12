#include <iostream>
#include <fstream>
#include <unordered_map>
//#include <cmath.h>
#include <list>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <thread>
#include <time.h>

using namespace std;

// TODO: unordered_map or binary/bitmap more efficient?

#define NUM_MOVIES 17770
#define NUM_USERS 458293
#define NUM_FEATURES 10
#define NUM_TOTAL_PTS 102416306
#define NUM_TEST_PTS 2749898
#define MU 3.6   // Baseline thing
#define LAMBDA 1 // Regularization
#define LRATE 0.001
#define NUM_EPOCHS 5

//unsigned char ratings_[NUM_USERS][NUM_MOVIES];
double users_f_[NUM_USERS][NUM_FEATURES];
double movies_f_[NUM_MOVIES][NUM_FEATURES];
int user_order_[NUM_USERS];
int movie_order_[NUM_MOVIES];

double *userValue;
double *movieValue;

void init()
{
  std::cout << "Initializing arrays" << std::endl;
  // Initialize ratings to 0
  /*for (int u = 0; u < NUM_USERS; u++)
    for (int m = 0; m < NUM_MOVIES; m++)
    ratings_[u][m] = 0;*/

  // Initialize user and movie features to some random points between -0.001 and 0.001
  // Initialize random seed
  srand(time(NULL));

  for (int u = 0; u < NUM_USERS; u++)
    for (int f = 0; f < NUM_FEATURES; f++)
      users_f_[u][f] = ( (double)rand() / (double)RAND_MAX ) * 0.002 - 0.001;

  for (int m = 0; m < NUM_MOVIES; m++)
    for (int f = 0; f < NUM_FEATURES; f++)
      movies_f_[m][f] = ( (double)rand() / (double)RAND_MAX ) * 0.002 - 0.001;
  
  // Initialize user and movie order
  for (int i = 0; i < NUM_USERS; i++)
    user_order_[i] = i;
  
  for (int j = 0; j < NUM_MOVIES; j++)
    movie_order_[j] = j;
}

/*void read_in_data(std::string filename)
{
  std::cout << "Reading in data from " << filename << std::endl;

  // Open file
  std::ifstream infile(filename);
  
  // Read in line by line
  int user_num, movie_num, date_num;
  unsigned char rating;

  while (infile >> user_num >> movie_num >> date_num >> rating)
    {
      ratings[user_num][movie_num] = rating;
    }
  
    }*/

double predictRating(int movie, int user)
{
  return userValue[user] * movieValue[movie];
}


int main()
{
  std::cout << "Running SVD" << std::endl;
  double err;
  int u, m, date_num;
  double rating;
  const char* filename = "all.dta";
  
  // Initialize
  init();
  
  // Read in data
  //read_in_data("all.dta");

  // TODO: Run epochs until convergence as opposed to a set number?
  for (int e = 0; e < NUM_EPOCHS; e++)
    {
      std::cout << "Epoch " << e << std::endl;
      // TODO: Pick some random ordering of the points
      // Pick some random ordering of the users and movies

      // For each point in the dataset
      // Open file
      std::cout << "Reading in data from " << filename << std::endl;
      std::ifstream infile(filename);

      // Update values
      // Read in line by line
      while (infile >> u >> m >> date_num >> rating)
	{
	  for (int f = 0; f < NUM_FEATURES; f++)
	    {
	      userValue = users_f_[f];
	      movieValue = movies_f_[f];
	      
	      err = LRATE * pow( rating - predictRating(m, u), 2 );
	      
	      users_f_[f][u] += err * movieValue[m] - LAMBDA * userValue[u];
	      movies_f_[f][m] += err * userValue[u] - LAMBDA * movieValue[m];
	    }
	}
      
	  /*for (int u = 0; u < NUM_USERS; u++)
	    {
	      for (int m = 0; m < NUM_MOVIES; m++)
		{
		  err = LRATE * ( ratings_[u][m] - predictRating(m, u) )^2;
		  
		  users_f_[f][u] += err * movieValue[m] - LAMBDA * userValue[u];
		  movies_f_[f][m] += err * userValue[u] - LAMBDA * movieValue[m];
		}
		}*/
    }

  return 0;

}
