import type { Account } from '../../types'
import { AccountCard } from './AccountCard'
import { Skeleton } from '../ui/skeleton'

export function AccountList({ accounts, loading }: { accounts: Account[]; loading: boolean }) {
  if (loading) {
    return (
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {[1, 2, 3].map(i => (
          <div key={i} className="rounded-xl border p-6 space-y-4">
            <Skeleton className="h-5 w-32" />
            <Skeleton className="h-3 w-20" />
            <Skeleton className="h-9 w-40" />
            <Skeleton className="h-1 w-full" />
          </div>
        ))}
      </div>
    )
  }

  if (accounts.length === 0) {
    return (
      <div className="text-center py-16">
        <p className="text-muted-foreground text-lg">No accounts yet</p>
        <p className="text-sm text-muted-foreground mt-1">Seed data is created automatically when the backend starts.</p>
      </div>
    )
  }

  return (
    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {accounts.map(a => (
        <AccountCard key={a.id} account={a} />
      ))}
    </div>
  )
}
