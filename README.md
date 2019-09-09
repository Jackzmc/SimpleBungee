# SimpleBungee [![Build Status](https://ci.jackz.me/view/Java/job/SimpleBungee/badge/icon)](https://ci.jackz.me/view/Java/job/SimpleBungee/)
A set of simple tools for a bungeecoord setup

Currently in development, features are still in development and may change.

### Commands
|  Name              |  Permission                                                     |  Description                                         |
|--------------------|-----------------------------------------------------------------|------------------------------------------------------|
| /uuid [player]     | simplebungee.command.uuid                                       | get users uuid                                       |
| /ping [player]     | simplebungee.command.ping                                       | get users ping to bungeecoord                        |
| /lookup <username> | simplebungee.command.lookup                                     | gets (last, or current) information for player       |
| /servers           | simplebungee.command.servers                                    | lists all servers and has one click join button      |
|  /friends          | simplebungee.command.friends                                    | full friend system (can join, and msg, and more)     |
| /simplebungee      |  simplebungee.command.simplebungee  simplebungee.command.reload | includes reload of config, maybe more commands later |

### Configuration

| Name                             |  Type   | Default |  Description |
|----------------------------------|---------|---------|--------------|
| connection-messages.bungee       | boolean | true    |  Send \<player> joined the network messages            |
| connection-messages.serverswitch | boolean | true    |  Send \<player> switched servers from X to Y messages            |
| connection-messages.friends      | boolean | true    |  Send \<friend> joined/switched servers (only if above are off)            |


## Development Builds
View development builds on jenkins at https://ci.jackz.me/view/Java/job/SimpleBungee/

