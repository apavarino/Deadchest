## Deadchest - Worldguard Integration 

Deadchest allow you to manage permission to generate chest with the region system of **WorldGuard**.
This section explains how configure Deadchest with Worldguard.

### Prerequisite
First of all, be sure to have Deadchest version `4.3.0` or higher.

### Update configuration
Open the file `config.yml` of Deadchest and set : `EnableWorldGuardDetection: true`

### Verify that Worldguard support is working
Start/restart your server. Deadchest will display a message on the console to tell you if the worldguard detection was successful

### Manage flag
Set Deadchest flags for your regions.

Command | Description 
--- | --- | 
`dc_owner` | Owner of the region can generate chest
`dc_member` | Member of the region can generate chest
`dc_guest` | Everyone can generate chest on this region

> If no flag is specified for a region, nobody can generate except OP.
### Next step
See [troubleshooting part](https://apavarino.github.io/Deadchest/troubleshooting) or go to [home page](https://apavarino.github.io/Deadchest)
