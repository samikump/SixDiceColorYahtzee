Six Dice Colored Yahtzee
------------------------
In a nutshell, it's like a traditional yahtzee, but with 6 colored dice (instead of 5) and some new scoring categories.

But first things first.
In order to make it work fully, you need the database that the program works on.
I strongly suggest you do it this way:
1. Install MySQL
2. Run MySQL Command Line Client and login with your MySQL root password
3. Create the database with the command
CREATE DATABASE Yahtzee;
4. Switch to the database with the command
USE Yahtzee;
5. Create the table hiscore with the command
CREATE TABLE hiscore (playerName VARCHAR(20), score INT, date DATETIME);
6. Edit SixDiceColorYahtzee.java so that you replace the text 'yourRootPassword' with the password you chose for your MySQL root user.

That should do the trick!

But WHY six dice and colored?
Well, first of all, I was introduced to the traditional Yahtzee in the 90s.
Then, in early 2000s, I found an online game that had colored dice and some cool scoring categories, such as Painted House, Flush and Rainbow.
I also found a variation of the traditional (non-colored) Yahtzee that had six dice instead of five and scoring categories such as 3 Pairs, 2x 3 of a Kind etc.
Although I really liked the colored (five dice) Yahtzee, I noticed a problem in it... there are six sides on each die, but only five colors.
This means that on each die, one color is over-represented.
Consequently, this would result in either unbalanced colors or each die have exactly same number combinations double-colored.
Solution: combine 6 dice traditional Yahtzee with colored Yahtzee!

So, what are the main differences to traditional Yahtzee and/or colored Yahtzee?
Like I already mentioned that there are 6 dice instead of 5 and the dice are colored, meaning every side of a die has a different background color.
This gives several new scoring categories, which are explained below.

3 Pairs			        : this means 3 distinct pairs, for example 2-2-4-4-6-6 (this would give a score of 24)
2x 3 of a Kind		  : this means 2 distinct sets of 3, for example 4-4-4-5-5-5 (this would give a score of 27)
5 of a Kind		      : self-explanatory, but this is not yahtzee in the 6 dice version, for example 5-5-5-5-5-6 (this would give a score of 25)
Huge Straight		    : even longer than the large straight in traditional yahtzee, requires all six dice to make 1-2-3-4-5-6, gives a score of 35
Extended Full House	: a set of four combined with a pair, for example 1-1-1-1-3-3, gives a score of 35
Yahtzee			        : requires all six dice to have the same number of eyes, worth 50 points

Then we have those where the color matters.

Just below the traditional 1s - 6s (plus bonus), there's another section:
Purples, Reds, Oranges, Yellows, Greens, Blues.
Here you get the score based on the sum of the eyes on the background of the corresponding color.
And by the way, as there are more dice, the bonus requirement has been raised from 63 to 84 in both of these sections.
Bonuses are 50 points in each section.

We have the following ones remaining:

3 of a Color		        : self-explanatory
4 of a Color		        : self-explanatory
5 of a Color		        : self-explanatory
2x 3 of a Color		      : almost self explanatory... 2 sets of 3 of a Color, for example 3 purples and 3 blues
Painted House		        : the full house of colors, for example 3 greens with 2 yellows, worth 25 points
Extended Painted House	: the color version of extended full house, for example 4 oranges with 2 reds, worth 35 points
Rainbow			            : all dice have different background color, worth 40 points
Flush			              : all dice have the same background color, worth 50 points

Special rule:
If a player has already assigned a Yahtzee to the Yahtzee cell and throws another Yahtzee,
he/she gets an extra +100 points to the Yahtzee cell no matter where he/she assigns the subsequent Yahtzee.
This special bonus is cumulative, i.e. it can be obtained several times in a game.

Also... since there are more dice and the bonuses and other scores are more difficult to receive, I've given the player 4 rolls per turn instead of 3.

And this version is for a single player only... perhaps I'll change it to multiplayer some day.

Finally, I know the dice are not as beautiful as they could be... I might change that too.
