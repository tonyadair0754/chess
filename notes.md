# En passant

If an opposing pawn moved forward two spaces last turn,
and if that pawn is now directly beside (same row) one of your pawns,
the square that was passed is added to the list of valid moves of your pawn
if you do use your pawn to attack the opponent's pawn,
your pawn performs a regular diagonal capture
the opposing pawn is removed from the board

# Castling

if neither the king nor the chosen rook have moved yet,
and if there are no pieces between the king and rook,
and if the king is not in check,
and if the square the king passes through is not attacked,
and if the square the king ends on is not attacked,
the king moves two squares toward the rook
the rook jumps to the other side of the king to sit right next to it (same row)

# Phase 3 outline

Setup
↓
Model records
↓
DAO interfaces + memory implementations
UserData, GameData, AuthData records
↓
clear ()
↓
register ()
↓
login ()
↓
logout ()
↓
listGames ()
↓
createGame ()
↓
joinGame ()

# Notes on how to write tests

1. @Test: lets JUnit know it's a test to run

2. AAA pattern:
   Arrange: Create objects and set up test data
   Act: Call the method you want to test
   Assert: Verify that the result matches what you expect

3. Assertions: helper methods that pass or fail the test
   assertEquals (expected, actual) : Checks if two values are equal
   assertNull (value) : Passes if value is null
   assertTrue (condition) : Passes if condition is true

# Phase 4 outline

1. Design and initialize the schema

- Write CREATE DATABASE and CREATE TABLE IF NOT EXISTS statements
- Ensure startup initializes the database and tables automatically

2. Implement one DAO at a time

- Create SQL implementations of the existing DAO interfaces
- Start with user persistence, then authentication, then games—or follow the dependency order your interfaces require
- For each method, implement the SQL, handle failures, and write tests before moving on

3. Integrate the SQL layer

- Wire the SQL implementations into the services and server startup
- Verify that data survives a server restart

4. Test, measure, and submit

- Reach at least 80% line coverage in dataaccess.sql