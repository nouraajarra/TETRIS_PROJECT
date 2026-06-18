# Tetris Game - User Guide

## Overview

This project is a Java Swing desktop version of Tetris with player accounts, score tracking, game history, multiple stages, and Gmail confirmation emails after registration.

## Requirements

- Java 17 or later
- Windows, Linux, or macOS with Java installed
- Internet access only if email confirmation is enabled
- A Gmail account with an app password for email sending

## How To Run

From the project folder, run:

```powershell
java -jar tetris.jar
```

On Windows, if an executable version is provided, you can run:

```powershell
.\dist\Tetris\Tetris.exe
```

## Account Registration

When the application starts, the login screen is displayed.

To create an account:

1. Click **Create account**.
2. Enter a pseudo, email address, password, and password confirmation.
3. The password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number.
4. After successful registration, the game sends a confirmation email to the registered email address.

To log in:

1. Enter your email address.
2. Enter your password.
3. Click **Log in**.

## Email Configuration

The application reads email credentials from `email.properties`.

Example:

```properties
mail.from=yourgmail@gmail.com
mail.password=yourgmailapppassword
```

Important notes:

- Use a Gmail app password, not your normal Gmail password.
- Gmail requires 2-Step Verification before app passwords can be created.
- Do not share your real `email.properties` file with your password inside it.
- If Gmail displays the app password with spaces, the application can still handle it.
- For the packaged Windows app, place `email.properties` beside `Tetris.exe` or inside `dist/Tetris/app`.

## Game Controls

The app also includes an English guide in the menu:

```text
Help > User Guide
```

| Key | Action |
| --- | --- |
| Left Arrow | Move piece left |
| Right Arrow | Move piece right |
| Down Arrow | Soft drop |
| Up Arrow or X | Rotate clockwise |
| Z | Rotate counter-clockwise |
| Space | Hard drop |
| P or Escape | Pause or resume |
| R | Restart game |

## Stages

The game includes multiple stages based on cleared lines:

| Stage | Condition | Behavior |
| --- | --- | --- |
| Stage 1 | 0 to 9 cleared lines | Normal Tetris gameplay |
| Stage 2 | 10 to 19 cleared lines | Faster falling speed |
| Stage 3 | 20 to 29 cleared lines | Two active pieces |
| Stage 4 | 30 or more cleared lines | Four active pieces |

## Score

The score increases when:

- The player clears lines.
- The player performs soft drops.
- The player performs hard drops.

Line clear points are multiplied by the current level.

## History

After a game ends, the application saves:

- Player
- Score
- Level
- Cleared lines
- Game duration

The history screen shows previous games and player statistics.

## Build

To rebuild the jar on Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

The build script compiles the Java source files and creates `tetris.jar`.

## Project Structure

```text
src/main/java/tetris
  Main.java
  controller/
  database/
  model/
  service/
  view/
```

Main parts:

- `Main.java`: starts the application
- `controller`: game loop, keyboard input, and game state
- `database`: SQLite database access
- `model`: grid, pieces, score, player, and history
- `service`: email sending
- `view`: Swing user interface

## Before Submission

Before sending the project to another person:

1. Remove your real Gmail app password from `email.properties`.
2. Keep only an example configuration file.
3. Make sure `tetris.jar` or `Tetris.exe` runs correctly.
4. Test account registration and login.
5. Test that game history is saved after game over.
