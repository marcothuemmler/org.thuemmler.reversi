export type PlayerType = 'HUMAN' | 'AI'
export type Player = 'BLACK' | 'WHITE'
export type CellState = Player | 'EMPTY'

export interface Move {
  row: number
  col: number
}

export interface GameState {
  id: string
  currentPlayer: Player
  isFinished: boolean
  board: { grid: CellState[][] }
  validMoves: Move[]
}

export interface PlayerTypes {
  BLACK: PlayerType
  WHITE: PlayerType
}

export interface NewGameRequest {
  id?: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  playerTypes: PlayerTypes
  currentPlayer: Player
  preferredSide: Player
}
