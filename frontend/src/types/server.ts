export interface ServerMessage<T> {
  type: string
  gameId?: string
  payload: T
}

export class ClientMessage<T> {
  constructor(
    private readonly type: string,
    private readonly payload?: T,
    private readonly gameId?: string,
  ) {}

  static create<T>(type: string) {
    return new ClientMessage<T>(type)
  }

  withPayload(payload: T) {
    return new ClientMessage<T>(this.type, payload, this.gameId)
  }

  withGameId(gameId?: string) {
    return new ClientMessage<T>(this.type, this.payload, gameId)
  }

  asString(): string {
    return JSON.stringify(this.toJSON())
  }

  private toJSON() {
    const result: Record<string, unknown> = { type: this.type }
    if (this.payload !== undefined) result.payload = this.payload
    if (this.gameId !== undefined) result.gameId = this.gameId
    return result
  }
}
