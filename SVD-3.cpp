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
//#define NUM_TOTAL_PTS 102416306
//#define NUM_TEST_PTS 2749898
#define MU 0.0   // Baseline thing
#define LAMBDA 0.025 // Regularization
#define LRATE 0.001
#define NUM_EPOCHS 5

double users_f_[NUM_FEATURES][NUM_USERS];
double movies_f_[NUM_FEATURES][NUM_MOVIES];

double *userValue;
double *movieValue;

void init()
{
  std::cout << "Initializing arrays" << std::endl;
  // Initialize user and movie features to some random points between -0.001 and 0.001
  // Initialize random seed
  srand(time(NULL));

  for (int f = 0; f < NUM_FEATURES; f++)
    for (int u = 0; u < NUM_USERS; u++)
      {
	//users_f_[f][u] = ( (double)rand() / (double)RAND_MAX ) * 0.002 - 0.001;
	users_f_[f][u] = ( (double)rand() / (double)RAND_MAX );
      }

  for (int f = 0; f < NUM_FEATURES; f++)
    for (int m = 0; m < NUM_MOVIES; m++)
      {
	movies_f_[f][m] = ( (double)rand() / (double)RAND_MAX );
      }
}

double predictRating(int movie, int user)
{
  double prediction = 0.0;
  for (int f = 0; f < NUM_FEATURES; f++)
    {
      //std::cout << prediction << " " << users_f_[f][user] << " " << movies_f_[f][movie] << " " << users_f_[f][user] * movies_f_[f][movie] << std::endl;
      prediction += users_f_[f][user] * movies_f_[f][movie];
    }
  
  //return userValue[user] * movieValue[movie];
  return prediction;
}

void predict(const char* filename)
{
  std::cout << "Predicting on " << filename << std::endl;
  int u, m, date_num, rating;
  double prediction= 0.0;

  // Outfile
  ofstream outfile;
  outfile.open("probe_predictions.dta");

  // Write out prediction
  std::ifstream infile(filename);

  // Read in line by line
  while (infile >> u >> m >> date_num >> rating)
  {
    prediction = 0.0;
    for (int f = 0; f < NUM_FEATURES; f++)
      {
	prediction += users_f_[f][u] * movies_f_[f][m];
	//std::cout << users_f_[f][u] << " " << movies_f_[f][m] << " " << prediction << std::endl;
      }
		
    outfile << MU + prediction << std::endl;
  }	

  outfile.close();
  
  // Simulatenously calculate cumulative 
}

int main()
{  
  std::cout << "Running SVD" << std::endl;
  double err;
  int u, m, date_num;
  double rating;
  const char* filename = "training.dta";
  
  // Initialize
  init();
  
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
	      //std::cout << "Before: " << users_f_[f][u] << " " << movies_f_[f][m] << std::endl;
	      userValue = users_f_[f];
	      movieValue = movies_f_[f];
	      
	      err = LRATE * (rating - predictRating(m, u));
	      //std::cout << err << std::endl;
	      
	      userValue[u] += err * movieValue[m] - LAMBDA * userValue[u];
	      movieValue[m] += err * userValue[u] - LAMBDA * movieValue[m];
	      //std::cout << "After: " << users_f_[f][u] << " " << movies_f_[f][m] << std::endl << std::endl;
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

  predict("probe.dta");

  return 0;

}
