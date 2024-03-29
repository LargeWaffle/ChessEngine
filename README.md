# Chess Engine project
Usable in the Arena GUI. As time was of the essence, this codebase is not clean and has not been optimized with professional methods, but rather with intuition and heavy debugging trials.

We also mainly relied on Ben-Hur Carlos Vieira Langoni Junior's [chesslib](https://github.com/bhlangonijr/chesslib) library
(Thank you for your great work by the way!).

## Objectives
* Functionning chess engine capable of making a move under a second
* In its core, creating a Minimax algorithm
* Experiment with different optimizations techniques

## Concepts used
* Alpha beta pruning
* Dynamic evaluation function
  Tapered evaluation
  Lazy evaluation
* Move ordering
  Most Valuable Victim/Least Valuable Attacker (MVV/LVA)
  Considering promoting moves
  Considering castling
  Killer Heuristic
  History Moves Heuristic
  Progress at reaching the other side
  King moves

* Transposition tables
* Futility pruning
* Null move pruning
* Delta pruning

## How to run
To recreate our process, you will need to have Arena installed.  
Then, create an .jar artefact through your IDE of choice (in our case IntelliJ).  
Upload the jar file in Arena and have fun !
