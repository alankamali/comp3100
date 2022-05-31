
## Overview
ds-sim is a discrete-event simulator that has been developed primarily for leveraging scheduling algorithm design. It adopts a minimalist design explicitly taking into account modularity in that it uses the client-server model. The client-side simulator acts as a job scheduler while the server-side simulator simulates everything else including users (job submissions) and servers (job execution).

---
## How to run a simulation
1. run server `$ ds-server [OPTION]...` -n
2. run client `$ java AppStage2 OR java App

## Usage
`$ ds-server -c ds-config01.xml -v brief`

`$ ds-client -a bf`

