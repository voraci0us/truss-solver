# truss-solver

Simple tool, written in Java with Graphics2D, to determine the force in each member of a truss.

## Input:
  - A text file containing
    - Maximum beam length
    - Joint locations
    - Edges between joints (beams)
    - External forces

## Output:
  - For each beam,
    - Force in the beam (tension or compression)
    - Length
    - Required cross-sectional area
    - Volume
    - Density
   - Total volume of truss
   - Total density of truss

  ## How To Run:
  ```
  git clone https://github.com/voraci0us/truss-solver
  cd truss-solver
  javac *.java
  java TrussSolver [input.txt]
  ```
  Where [input.txt] is replaced with the file containing user input. The examples folder contains sample input files.
