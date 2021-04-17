# I Remember When...

_I Remember When…_ is a social, “get-to-know-you”, conversation-based Android game that allows participants to share funny stories based on prompts. The game is useful as a social game for seniors both in concept and in its design.

## Intended Audience

While _I Remember When…_ is designed to be playable by anybody with some level of computer skill, the game is designed primarily with seniors in mind. Aspects such as its simple to understand GUI, minimal use of keyboard, and focus on utilization of a microphone and camera make the app accessible to those who typically do not enjoy mobile games. The game focuses on personal narrative and communication, and removes the need for dexterity, quick reaction time, and sharp vision. By excluding these typical mobile game factors, _I Remember When…_ creates a level playing field for multiple generations to play together and connect.

# Demo
https://youtu.be/HOCaHMsrhu0

# Guidelines

- Development has been done in Android Studio Version 4.x.x
- All code was implemented using Java and XML following the Cornell University Dept. of CS JavaHyperText and Data Structures Java Code Style Guidelines
- Cloud FireStore is used as the online real-time database
- Jitsi WebRTC is used for voice communication between players

# Game Description

## Game Creation Procedure
Game sessions are created through the following procedure:
- One player, the host, opens the game application and types in the name they want to be referred by during the game session
- Select the ‘Create Room’ button 
- This takes them to a screen that shows a unique 4-digit room code, which they share with the other players (up to 6 other players)
- Once all expected players have joined in to the game, the host selects the ‘Ready’ button

## Joining Procedure
The other players join the session through the following procedure: 
- They use their personal device to open the app and type in the name they want to be referred by during the game session
- Select the ‘Join Room’ button
- This takes them to a screen which accepts the 4-digit code that the host has shared with them
- This puts them into a waiting room with the host, where they will be able to converse through the audio I/O of their device until the host has selected the ‘Ready’ button

## Gameplay Procedure 
1. All players are then shown a screen that explains the rules and objectives of the game, which will also be accessible by any player during the game session by way of an icon displayed on their screen
2. The game starts with the host being chosen by the system as the storyteller; the rest of the players will be listeners for the duration of the turn
3. The storyteller is given a novel prompt to tell a story, which they decide will either be something that truly happened to them, or a story that they’ve made up. They will inform the system of this decision by selecting either the ‘Tell a true story’ or ‘Make it up’ buttons. The listeners will not be aware of this decision, though they will be aware of the prompt. 
4. The storyteller will then proceed to tell the other players this story, using the microphone in their device. They will be given 2 minutes to tell the story, and will also be provided with options to get more time (30 second intervals) or to stop telling the story, by selecting the ‘Story is finished’ button
5. Once the story is finished there is a 1 minute deliberation period, where the listeners can probe the storyteller for details and, if appropriate, accuse them of lying. The storyteller must defend their story by any means necessary during this period, whether it is true or not. At the end of the deliberation period the listeners must choose whether they think it is a true story or not, and select either the ‘True story’ or ‘Fake story’ button
6. The turn ends by revealing whether or not the story was true and displaying the updated scoreboard. 
7. The next storyteller is chosen based on who joined the game room first, and their turn repeats the previous sequence. Once all players, in order of room joining, have each had a turn (steps 2-6) the host becomes storyteller once again. 
8. Players loop through rounds (steps 2-7) until a player wins the game. This can occur if someone reaches the maximum score or the game reaches the maximum number of rounds and the player with the highest score wins
9. Once a player has won the game the final scoreboard is shown, and the players will have the option of playing again (where they are taken back to step 2) or ending, exiting the game session

## Points Breakdown
Each round a player will receive points based on their role:
### Storyteller 
- 10 points per listener convinced of your true story
- 20 points per listener convinced of your fake story
- -20 points if no players are convinced of your fake story

### Listener
- 10 points for correctly recognizing whether a story is true or fake
- -5 points for being duped by a fake story
- -10 points for falsely accuse a player of lying
- -10 points for failing to answer within the deliberation period

# References
Jitsi SDK implementation was done using resources from the samples that Jitsi provides in the following link:
https://github.com/jitsi/jitsi-meet-sdk-samples

Firebase features were implemented by following the Firebase documentation
https://firebase.google.com/docs
