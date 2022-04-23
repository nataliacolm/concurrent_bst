
## To Compile the Code in Command Line:
Test Insert:
* g++ -std=c++17 TestInsertConcurrent.cpp
Test Delete:
* g++ -std=c++17 TestDeleteConcurrent.cpp
Test Search:
* g++ -std=c++17 TestSearchConcurrent.cpp
Test Insert & Delete:
* g++ -std=c++17 TestInsertAndRemoveConcurrent.cpp
Test Sequentially:
* g++ -std=c++17 TestSequential.cpp

To run: ./a.out

## Current Testing Situation: 
Constants MAX_ITERATION1, MAX_ITERATION2, MAX_ITERATION3 can be changed to other numbers that represent the number of iterations that will occur in the testing program. The variable should also be changed in the for loop in the run() function to detail which of the max iterations will be used.
