import { useRouter } from 'vue-router'

type GameMode = 'Singleplayer' | 'Multiplayer'

export function useHome() {
  const router = useRouter()

  async function goToGame(mode: GameMode) {
    await router.push({
      path: '/game',
      state: { mode, currentPlayer: 'BLACK' },
    })
  }

  return {
    goToGame,
  }
}
