import { useSearchParams } from 'react-router-dom'
import { TransactionLookup } from '../components/transactions/TransactionLookup'

export function TransactionLookupPage() {
  const [searchParams] = useSearchParams()
  const ref = searchParams.get('ref') ?? undefined

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Transaction Lookup</h1>
        <p className="text-muted-foreground">
          Query journal entries by reference ID to view debit/credit details
        </p>
      </div>
      <TransactionLookup initialRef={ref} />
    </div>
  )
}
