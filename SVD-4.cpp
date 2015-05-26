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
#define NUM_FEATURES 25
//#define NUM_TOTAL_PTS 102416306
//#define NUM_TEST_PTS 2749898
#define MU 0.0   // Baseline thing
#define LAMBDA 0.025 // Regularization
#define LRATE 0.001
#define NUM_EPOCHS 5

double users_f_[NUM_USERS][NUM_FEATURES];
double movies_f_[NUM_MOVIES][NUM_FEATURES];

double *userValue;
double *movieValue;

void init()
{
  std::cout << "Initializing arrays" << std::endl;
  // Initialize user and movie features to some random points between -0.001 and 0.001
  // Initialize random seed
  srand(time(NULL));

  for (int u = 0; u < NUM_USERS; u++)
    for (int f = 0; f < NUM_FEATURES; f++)
      {
	//users_f_[f][u] = ( (double)rand() / (double)RAND_MAX ) * 0.002 - 0.001;
	users_f_[f][u] = ( (double)rand() / (double)RAND_MAX );
      }

  for (int m = 0; m < NUM_MOVIES; m++)
    for (int f = 0; f < NUM_FEATURES; f++)
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
      prediction += users_f_[user][f] * movies_f_[movie][f];
    }
  
  return prediction;
}

void predict(const char* filename)
{
  std::cout << "Predicting on " << filename << std::endl;
  int u, m, date_num, rating;
  double prediction = 0.0;
  double error = 0.0;

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
	prediction += users_f_[u][f] * movies_f_[m][f];
	//std::cout << users_f_[f][u] << " " << movies_f_[f][m] << " " << prediction << std::endl;
      }
		
    outfile << MU + prediction << std::endl;
  }	

  outfile.close();
  
  // Simulatenously calculate cumulative error
}

int main()
{  
  std::cout << "Running SVD" << std::endl;
  double err;
  int u, m, date_num;
  double rating;
  double error = 0.0;
  const char* filename = "training.dta";
  std::cout << "Training on " << filename << std::endl;
  
  // Initialize
  init();
  
  // TODO: Run epochs until convergence as opposed to a set number?
  for (int e = 0; e < NUM_EPOCHS; e++)
    {
      std::cout << "Epoch " << e << std::endl;
      error = 0.0;
      // TODO: Pick some random ordering of the points
      // Pick some random ordering of the users and movies

      // For each point in the dataset
      // Open file
      std::ifstream infile(filename);

      // Update values
      // Read in line by line
      while (infile >> u >> m >> date_num >> rating)
	{
	  for (int f = 0; f < NUM_FEATURES; f++)
	    {
	      //std::cout << "Before: " << users_f_[f][u] << " " << movies_f_[f][m] << std::endl;
	      userValue = users_f_[u];
	      movieValue = movies_f_[m];
	      
	      err = LRATE * ( rating - predictRating(m, u) );
	      //std::cout << err << std::endl;
	      
	      userValue[f] += err * movieValue[f] - LAMBDA * userValue[f];
	      movieValue[f] += err * userValue[f] - LAMBDA * movieValue[f];
	      //std::cout << "After: " << users_f_[f][u] << " " << movies_f_[f][m] << std::endl << std::endl;

	      error += rating - predictRating(m, u);
	    }
	}

      std::cout << "Error: " << error << std::endl;
    }

  predict("probe.dta");

  return 0;

}
