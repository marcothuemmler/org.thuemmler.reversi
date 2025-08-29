import { useRouter } from 'vue-router'

type GameMode = 'Singleplayer' | 'Multiplayer'

export function useHome() {
  const router = useRouter()

  function goToGame(mode: GameMode) {
    router.push({
      path: '/game',
      state: { mode, currentPlayer: 'BLACK' },
    })
  }

  return {
    goToGame,
  }
}
