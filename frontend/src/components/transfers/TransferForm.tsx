import { useState } from 'react'
import { ArrowRight, Loader2 } from 'lucide-react'
import { Button } from '../ui/button'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card'
import type { Account } from '../../types'

interface TransferFormProps {
  accounts: Account[]
  onSubmit: (data: {
    referenceId: string
    sourceAccountId: string
    destinationAccountId: string
    amount: number
    description: string
  }) => void
  loading: boolean
}

export function TransferForm({ accounts, onSubmit, loading }: TransferFormProps) {
  const [sourceId, setSourceId] = useState('')
  const [destId, setDestId] = useState('')
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit({
      referenceId: `TXN-${Date.now().toString(36).toUpperCase()}`,
      sourceAccountId: sourceId,
      destinationAccountId: destId,
      amount: parseFloat(amount),
      description,
    })
  }

  const inputClass = "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"

  return (
    <Card className="max-w-lg mx-auto">
      <CardHeader>
        <CardTitle>New Transfer</CardTitle>
        <CardDescription>Execute a double-entry fund transfer between accounts</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <label className="text-sm font-medium">Source Account</label>
            <select
              value={sourceId}
              onChange={e => setSourceId(e.target.value)}
              className={inputClass}
              required
            >
              <option value="" disabled>Select source account...</option>
              {accounts.map(a => (
                <option key={a.id} value={a.id} disabled={a.id === destId}>
                  {a.ownerName} — {a.currency} ({new Intl.NumberFormat('en-US', { style: 'currency', currency: a.currency }).format(a.balance)})
                </option>
              ))}
            </select>
          </div>

          <div className="flex justify-center">
            <ArrowRight className="h-5 w-5 text-muted-foreground" />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium">Destination Account</label>
            <select
              value={destId}
              onChange={e => setDestId(e.target.value)}
              className={inputClass}
              required
            >
              <option value="" disabled>Select destination account...</option>
              {accounts.map(a => (
                <option key={a.id} value={a.id} disabled={a.id === sourceId}>
                  {a.ownerName} — {a.currency} ({new Intl.NumberFormat('en-US', { style: 'currency', currency: a.currency }).format(a.balance)})
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium">Amount</label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={e => setAmount(e.target.value)}
              placeholder="100.00"
              className={inputClass}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium">Description</label>
            <input
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Payment for services"
              className={inputClass}
              required
            />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin mr-1" />
                Processing...
              </>
            ) : (
              'Execute Transfer'
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
