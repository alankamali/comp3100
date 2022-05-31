
## Overview
Stage2 implements the BF and FF algorithm to in avg beat both algorithms in turnaround-time. 
The algorithm first finds the BF for the jobs coming in from ds-server and then allocates them to the first server which can take them (FF).

---
## How to run a simulation
1. run server `$ ds-server [OPTION]...` -n
2. run client `$ java AppStage2 OR java App

## Usage
`$ ds-server -c ds-config01.xml -v brief -n`

`$ java AppStage2 $ for stage 2 of this assignment

