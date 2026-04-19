# Placeholders

Elytra Racing provides PlaceholderAPI support to display race statistics in other plugins (like scoreboards, chat, or holograms).

## Placeholder Formats

All placeholders follow one of these formats:

1.  **Statistic Value:** `%elytraracing_<race>_<stat>_<pos>%`
2.  **Player Name:** `%elytraracing_<race>_<stat>_player_<pos>%`

*   `<race>`: The name of the race (case-sensitive).
*   `<stat>`: The category to sort by and display.
*   `<pos>`: The leaderboard position (starting from 1).

## Supported Statistics

When you request a placeholder, the system automatically sorts the leaderboard based on that specific statistic.

| Statistic | Description | Sorting |
| :--- | :--- | :--- |
| `time` | Total race completion time. | Lowest to Highest |
| `bestlap` | Fastest single lap time. | Lowest to Highest |
| `wins` | Total number of first-place finishes. | Highest to Lowest |
| `rounds` | Total number of times a player started a race. | Highest to Lowest |
| `finishes` | Total number of times a player finished a race. | Highest to Lowest |

## Examples

| Goal | Placeholder |
| :--- | :--- |
| Show the fastest time for 'SkyDash' | `%elytraracing_SkyDash_time_1%` |
| Show the name of the player with the most wins in 'CanyonRun' | `%elytraracing_CanyonRun_wins_player_1%` |
| Show the 2nd best lap time in 'OceanDrive' | `%elytraracing_OceanDrive_bestlap_2%` |
| Show who has played 'SkyDash' the most | `%elytraracing_SkyDash_rounds_player_1%` |
| Show how many rounds the top player played in 'SkyDash' | `%elytraracing_SkyDash_rounds_1%` |

## Note on Race Names

It is strongly recommended to **not use underscores** in race names. While the system attempts to handle them, race names that end with statistic keywords (e.g., `My_Race_wins`) will cause parsing conflicts and may not work correctly as placeholders.

## Legacy Support

The old placeholder format `%elytraracing_<race>_player_<pos>%` is still supported and defaults to sorting by completion time.
