# Super-Paper-Mario-Bad-Translations
Super Paper Mario Dialogue turned into a fun mess.

Example image to be added later here.

## Purpose
This project takes the text within the game Super Paper Mario and translates it multiple times to produce very funny dialogue within the game in broken English which may include sarcasm, contradictions to the game's plot, innuendos, and things that may not make any sense at all. 
This is intended to work with the game Super Paper Mario, but it may possibly work on other games that stores their dialogues in files following a similar format.

## How-To
First, the files of the game are extracted with an ISO tool such as [Wiimm's ISO Tools](https://wit.wiimm.de/) or WiiScrubber. The text files are located in the texts/ folder. Each file is then run through in the program one by one by modifying the SPM_TEXT variable in BadTranslation.java to represent the filename. Once the program is run, a new file with the suffix "_finalTranslation" is created. This is the translated version of the original text. The original text is then replaced with the newly generated text file, and the ISO tools are used to copy the changes back into an ISO or WBFS format ready to play. Alternatively, a tool like [Riivolution](https://www.wiibrew.org/wiki/Riivolution) for the Nintendo Wii can be used to patch the game files using the xml template under the xml/ directory in this repository. 

This project is inspired by FatGuy703's [Book of Mario Series](https://www.youtube.com/watch?v=sqw0CKKRlJE) - badly translated Paper Mario.
