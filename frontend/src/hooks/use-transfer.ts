import { useState } from 'react'
import type { TransferRequest, TransferResponse } from '../types'
import { executeTransfer } from '../lib/api'

export function useTransfer() {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<TransferResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  const transfer = async (req: TransferRequest) => {
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      const res = await executeTransfer(req)
      setResult(res)
      return res
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Transfer failed'
      setError(msg)
      throw e
    } finally {
      setLoading(false)
    }
  }

  return { transfer, loading, result, error }
}
