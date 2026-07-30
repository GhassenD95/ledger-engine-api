import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { ArrowLeft } from 'lucide-react'
import { useAccount } from '../hooks/use-account'
import { getAccountEntries } from '../lib/api'
import type { AccountEntry } from '../types'
import { Badge } from '../components/ui/badge'
import { Skeleton } from '../components/ui/skeleton'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/ui/table'
import { Button } from '../components/ui/button'

export function AccountPage() {
  const { id } = useParams<{ id: string }>()
  const { account, loading, error } = useAccount(id)
  const [entries, setEntries] = useState<AccountEntry[]>([])
  const [entriesLoading, setEntriesLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    setEntriesLoading(true)
    getAccountEntries(id)
      .then(data => setEntries(data.content))
      .finally(() => setEntriesLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto p-6 space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-24 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (error || !account) {
    return (
      <div className="max-w-4xl mx-auto p-6 text-center">
        <p className="text-destructive text-lg">{error || 'Account not found'}</p>
        <Button variant="outline" className="mt-4" asChild>
          <Link to="/">Back to Dashboard</Link>
        </Button>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <Button variant="ghost" size="sm" asChild>
        <Link to="/" className="gap-1">
          <ArrowLeft className="h-4 w-4" /> Back
        </Link>
      </Button>

      <div className="rounded-xl border bg-card p-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold">{account.ownerName}</h1>
            <p className="text-sm text-muted-foreground mt-1">{account.currency} Account</p>
          </div>
          <Badge variant={account.balance >= 0 ? 'success' : 'destructive'}>
            {account.balance >= 0 ? 'Active' : 'Negative'}
          </Badge>
        </div>
        <div className="mt-4 flex items-baseline gap-2">
          <span className="text-4xl font-bold tracking-tight">
            {new Intl.NumberFormat('en-US', {
              style: 'currency',
              currency: account.currency,
            }).format(account.balance)}
          </span>
          <span className="text-sm text-muted-foreground">{account.currency}</span>
        </div>
        <p className="text-xs text-muted-foreground mt-2 font-mono">ID: {account.id}</p>
      </div>

      <div>
        <h2 className="text-lg font-semibold mb-3">Entry History</h2>
        {entriesLoading ? (
          <div className="space-y-2">
            {[1, 2, 3].map(i => <Skeleton key={i} className="h-10 w-full" />)}
          </div>
        ) : entries.length === 0 ? (
          <p className="text-muted-foreground text-sm">No entries yet.</p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Journal ID</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {entries.map(e => (
                <TableRow key={e.id}>
                  <TableCell className="text-sm">
                    {new Date(e.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className={e.amount < 0 ? 'text-red-600 font-medium' : 'text-emerald-600 font-medium'}>
                    {e.amount < 0 ? '-' : '+'}
                    {new Intl.NumberFormat('en-US', {
                      style: 'currency',
                      currency: 'USD',
                    }).format(Math.abs(e.amount))}
                  </TableCell>
                  <TableCell>
                    <Badge variant={e.amount < 0 ? 'destructive' : 'success'}>
                      {e.amount < 0 ? 'Debit' : 'Credit'}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    {e.journalEntryId.slice(0, 8)}...
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    </div>
  )
}
