# Kashmiri Wikipedia TypoFixer Bot

## Overview

The Kashmiri Wikipedia Typo Fixer Bot is a Java application designed to automatically correct typo errors on pages within the Kashmiri Wikipedia. It operates using a set of configurable parameters provided in a `cred.cnf` config file, allowing you to customize its behavior.

## Features

- Automatically corrects typo errors on Kashmiri Wikipedia pages.
- Configurable parameters for fine-tuning the bot's behavior.
- Ability to limit the number of edits per run.

## Configuration

You can configure the bot using the `cred.cnf` config file. Here's an explanation of each parameter:

- `debug`: Set to `true` for debug mode, which may provide additional logging or information for debugging purposes.
- `max_edits`:  The upper limit for the total number of edits the bot is permitted to perform.
- `max_edits_per_run`: The maximum number of edits the bot should attempt in a single run.
- `username`: Your Kashmiri Wikipedia account username for making edits.
- `password`: Your account password.
- `scan_revision_days`: Specify the number of past days to fetch the revisions from.


## Getting Started
### Follow these steps to get started with the TypoFixerBot:
- Clone this repository to your local machine:
   ```bash
   git clone https://github.com/nadeemulhaqmir/TypoFixer.git
- Configure the cred.cnf file with your Kashmiri Wikipedia account information and desired settings.
- Build the Java application (if required) and run the bot, specifying the cred.cnf file as follows:
- java -jar typo-fixer-bot.jar --credentials cred.cnf

## Support
If you have any questions or need assistance, please open an issue.
