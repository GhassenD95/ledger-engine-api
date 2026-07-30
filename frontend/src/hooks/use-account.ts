import { useState, useEffect } from 'react'
import type { Account } from '../types'
import { getAccount } from '../lib/api'

export function useAccount(id: string | undefined) {
  const [account, setAccount] = useState<Account | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    getAccount(id)
      .then(setAccount)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  return { account, loading, error }
}
