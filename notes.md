# En passant
If an opposing pawn moved forward two spaces last turn,
and if that pawn is now directly beside (same row) one of your pawns,
    the square that was passed is added to the list of valid moves of your pawn
    if you do move your pawn to that square,
        the opposing pawn is removed from the board

# Castling
if neither the king nor the chosen rook have moved yet,
and if there are no pieces between the king and rook,
and if the king is not in check,
and if the square the king passes through is not attacked,
and if the square the king ends on is not attacked,
    the king moves two squares toward the rook
    the rook jumps to the other side of the king to sit right next to it (same row)