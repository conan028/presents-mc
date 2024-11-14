# Presents
## Dependencies
Be sure to install Fabric Language Kotlin when installing this mod.

## Description
This side-mod introduces presents that players can discover throughout your server. Server owners can customize and place presents by adding new present entries in the config.json file. After that, they can use the /presents give command to receive the defined present by the identifier. Once placed, players can search for these hidden presents across the server. Upon finding one, they can right-click to claim their rewards!

Server owners can use the /presents command to open up all active presents, and by pressing on them, it will teleport them.

## Commands

_All commands require either ```Permission Level 2``` or ```present.admin``` as LuckPerms permission_

- ```/presents``` -> Opens the GUI with all active presents in the server.
- ```/presents give <identifier>``` -> Gives the item that can be placed to create a new present.
- ```/presents reload``` -> Reloads the config.


## Menu Item Configuration
Each item available in the menu can be configured as follows:
```json
{
  "name": "Menu Item",
  "material": "stone",
  "lore": ["Lore line 1", "Lore line 2"],
  "nbt": "{}",
  "amount": 1
}

```

## Config

```json
{
  "messages": {
    "prefix": "<red>[<dark_green>Presents<red>] <dark_gray>»",
    "alreadyFoundPresent": "%prefix% <red>You've already found this present.",
    "foundPresent": "%prefix% You found a present."
  },
  "menu": {
    "closeItem": {
      "name": "<red>Close",
      "material": "barrier"
    },
    "fillItem": {
      "name": "<gray> ",
      "material": "gray_stained_glass_pane"
    },
    "barItem": {
      "name": "<gray> ",
      "material": "black_stained_glass_pane"
    },
    "nextPageItem": {
      "name": "<green>Next",
      "material": "arrow"
    },
    "lastPageItem": {
      "name": "<green>Back",
      "material": "arrow"
    },
    "presentLore": [
      "<green>Dimension: <gray>%dimension%",
      "<green>Location: <gray>%x%</gray>, <gray>%y%</gray>, <gray>%z%</gray>"
    ]
  },
  "presents": [
    {
      "identifier": "christmas",
      "item": {
        "name": "Christmas Present",
        "material": "minecraft:player_head",
        "nbt": "{\"minecraft:profile\":{\"id\":[I;-2002873815,1388858326,-1525192356,172033830],\"name\":\"\",\"properties\":[{\"name\":\"textures\",\"value\":\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIxZWM3ZGM3NTMwNjFjYTE3NDQyNGVhNDVjZjk0OTBiMzljZDVkY2NhNDc3ZDEzOGE2MDNlNmJlNzU1ZWM3MiJ9fX0=\"}]}}"
      },
      "rewards": [
        "give %player% diamond 1"
      ]
    }
  ]
}
```
