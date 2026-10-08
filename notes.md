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
clear()
↓
register()
↓
login()
↓
logout()
↓
listGames()
↓
createGame()
↓
joinGame()


Suppose /session receives:
{
"username": "Tony",
"password": "abc123"
}
Then:
record LoginRequest(String username, String password) {}
record LoginResult(String username, String authToken) {}


Server
↓
Handler
↓
Service
↓
DAO interfaces
↓
Memory DAO implementations

interface UserDAO
class UserDAOMemory implements UserDAO

interface GameDAO
class GameDAOMemory implements GameDAO

interface AuthDAO
class AuthDAOMemory implements AuthDAO

UserService uses UserDAO
GameService uses GameDAO
AuthService uses AuthDAO


server/
├── Server
├── Handler(s)
├── Service
└── dataaccess/
    ├── UserDAO
    ├── UserDAOMemory
    ├── GameDAO
    ├── GameDAOMemory
    ├── AuthDAO
    └── AuthDAOMemory

shared/
└── model/
    ├── UserData
    ├── GameData
    └── AuthData