import { useState, useEffect } from 'react'
import type { Account } from '../types'
import { getAccounts } from '../lib/api'

export function useAccounts() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getAccounts()
      .then(setAccounts)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load accounts'))
      .finally(() => setLoading(false))
  }, [])

  return { accounts, loading, error }
}
