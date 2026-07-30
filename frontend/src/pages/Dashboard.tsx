import { useAccounts } from '../hooks/use-accounts'
import { AccountList } from '../components/accounts/AccountList'
import { Wallet, ArrowLeftRight, FileText, AlertTriangle } from 'lucide-react'

export function Dashboard() {
  const { accounts, loading, error } = useAccounts()

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Financial Dashboard</h1>
        <p className="text-muted-foreground">
          Real-time account balances powered by double-entry bookkeeping
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-3 rounded-lg border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/50 p-4 text-sm text-red-700 dark:text-red-400">
          <AlertTriangle className="h-5 w-5 shrink-0" />
          <span>Could not load accounts: {error}. Make sure the backend is running on port 8081.</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-xl border bg-card p-5 flex items-center gap-4">
          <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
            <Wallet className="h-5 w-5 text-primary" />
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Total Accounts</p>
            <p className="text-2xl font-bold">{loading ? '...' : accounts.length}</p>
          </div>
        </div>
        <div className="rounded-xl border bg-card p-5 flex items-center gap-4">
          <div className="h-10 w-10 rounded-lg bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
            <ArrowLeftRight className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Currencies</p>
            <p className="text-2xl font-bold">
              {new Set(accounts.map(a => a.currency)).size || '...'}
            </p>
          </div>
        </div>
        <div className="rounded-xl border bg-card p-5 flex items-center gap-4">
          <div className="h-10 w-10 rounded-lg bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
            <FileText className="h-5 w-5 text-amber-600 dark:text-amber-400" />
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Engine Status</p>
            <p className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">Online</p>
          </div>
        </div>
      </div>

      <div>
        <h2 className="text-xl font-semibold mb-4">Accounts</h2>
        <AccountList accounts={accounts} loading={loading} />
      </div>
    </div>
  )
}
