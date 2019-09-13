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
| /friends           | simplebungee.command.friends                                    | full friend system (can join, and msg, and more)     |
| /simplebungee      | simplebungee.command.simplebungee  simplebungee.command.reload  | includes reload of config, maybe more commands later |
| /global [message]  | simplebungee.command.global                                     | Allows access to global chat

### Configuration

| Name                             |  Type           | Default |  Description |
|----------------------------------|-----------------|---------|--------------|
| connection-messages.bungee       | boolean         | true    |  Send \<player> joined the network messages            |
| connection-messages.serverswitch | boolean         | true    |  Send \<player> switched servers from X to Y messages            |
| connection-messages.friends      | boolean         | true    |  Send \<friend> joined/switched servers (only if above are off)            |
| kick-players-on-shutdown         | boolean         | false   |  Should the plugin kick players on server shutdown?
| report.reasons                   | List\<String>   |         |  List of reasons to use in reporting if use_reason_list = false
| report.use_reason_list           | List\<String>   | true    |  Should players use predefined reasons, or enter a reason manually?
| server_shortcuts                 | Custom          |         |  See config.yml on spigot page for example
| show_restricted_servers          | boolean         | false   | Should /servers show restricted servers?
## Development Builds
View development builds on jenkins at https://ci.jackz.me/view/Java/job/SimpleBungee/

